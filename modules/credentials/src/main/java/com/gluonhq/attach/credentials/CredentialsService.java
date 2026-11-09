/*
 * Copyright (c) 2026, Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.attach.credentials;

import com.gluonhq.attach.util.Services;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * The CredentialsService lets an app keep its users signed in when they move to a new
 * device, by creating a <i>restore credential</i> once the user has signed in, retrieving it
 * on the first launch on the new device, and clearing it when the user signs out.
 *
 * <p>The three operations have the same shape on every platform, but what travels through
 * them is platform specific:</p>
 *
 * <p>On <b>Android</b> this is implemented with
 * <a href="https://developer.android.com/identity/sign-in/restore-credentials">Restore
 * Credentials</a> from Credential Manager. A restore credential is a FIDO2/WebAuthn public key
 * credential, handled by the same server code that handles passkeys, which the system backs up
 * and transfers silently to the user's new device (cloud backup or cable transfer). The app
 * exchanges WebAuthn JSON with its own relying party server:</p>
 * <ul>
 *   <li>{@link #createRestoreCredential(String, boolean, Consumer, Consumer)}: {@code request} is a
 *   {@code PublicKeyCredentialCreationOptionsJSON} produced by the server, and the result is the
 *   registration response JSON that must be sent back to the server to register the key.</li>
 *   <li>{@link #getRestoreCredential(String, Consumer, Consumer)}: {@code request} is a
 *   {@code PublicKeyCredentialRequestOptionsJSON} produced by the server, and the result is the
 *   authentication response JSON that must be sent back to the server to sign the user in.</li>
 *   <li>{@link #clearRestoreCredential(Runnable, Consumer)} deletes the restore credential from the
 *   device and its backup.</li>
 * </ul>
 * <p>Restore Credentials requires Android 9 (API 28) or higher and Google Play services. On devices
 * that do not meet this requirement the calls fail with {@link CredentialsException#UNSUPPORTED}.
 * Only one restore credential per app is kept: creating a new one replaces the previous one.
 * From April 2027, Google Play requires apps with user sign-in to restore the signed-in state on a
 * new device, and Restore Credentials is the primary way to meet that requirement.</p>
 *
 * <p>On <b>iOS</b> there is no system restore credential, so this is implemented with the
 * Keychain: {@code request} passed to {@code createRestoreCredential} is an opaque secret chosen
 * by the app (typically a revocable refresh token or session token, never a password), stored in
 * the app's Keychain. When {@code cloudBackup} is {@code true} the item is marked synchronizable,
 * so iCloud Keychain carries it to any device signed in with the same Apple Account, and it is also
 * included in encrypted backups and device-to-device transfers. When {@code false} the item is
 * device only. {@code getRestoreCredential} ignores its {@code request} argument and returns the
 * stored secret, which the app then exchanges with its server for a session.
 * {@code clearRestoreCredential} deletes the item.</p>
 *
 * <p><b>Several devices.</b> The two platforms differ when the same user runs the app on more
 * than one device at a time:</p>
 * <ul>
 *   <li>On Android the restore credential is a migration mechanism only. It reaches another
 *   device exclusively through a backup restore during device setup or a device-to-device
 *   transfer. Devices in use are never kept in sync: a credential created or cleared on one
 *   device is not seen on another, and each device ends up with its own credential.</li>
 *   <li>On iOS a synchronizable item is replicated by iCloud Keychain to every device signed in
 *   with the same Apple Account, so creating or clearing it on one device is reflected on the
 *   others, usually within a minute or two.</li>
 * </ul>
 * <p>In both cases the service never notifies the app of a change: it only reads the credential
 * when {@code getRestoreCredential} is called, so a change made on another device is observed on
 * the next call, typically on the next launch. Apps that want to pick it up sooner can call
 * {@code getRestoreCredential} again when they resume. Servers should treat a restore credential
 * as a bootstrap for a new session and not as proof of a single device: on iOS the same secret
 * can be presented from several devices, and on Android the old device keeps its credential until
 * it is wiped.</p>
 *
 * <p>On other platforms the service is not available.</p>
 *
 * <p>Callbacks are always invoked on the JavaFX Application Thread.</p>
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code CredentialsService.create().ifPresent(service -> {
 *      // right after the user signs in
 *      service.createRestoreCredential(creationOptionsFromServer, true,
 *              response -> sendRegistrationToServer(response),
 *              e -> LOG.warning("Restore credential not created: " + e.getMessage()));
 *
 *      // on first launch
 *      service.getRestoreCredential(requestOptionsFromServer,
 *              response -> signInWithServer(response),
 *              e -> {
 *                  if (CredentialsException.NO_CREDENTIAL.equals(e.getType())) {
 *                      showSignInScreen();
 *                  }
 *              });
 *
 *      // on sign out
 *      service.clearRestoreCredential(() -> showSignInScreen(),
 *              e -> LOG.warning("Restore credential not cleared: " + e.getMessage()));
 *  });}</pre>
 *
 * <p><b>Android Configuration</b>: none. The service adds the {@code androidx.credentials}
 * libraries to the app.</p>
 *
 * <p><b>iOS Configuration</b>: none. The Keychain item is private to the app, and a synchronizable
 * item travels through iCloud Keychain only if the user has it enabled on both devices.</p>
 *
 * @since 4.0.27
 */
public interface CredentialsService {

    /**
     * Returns an instance of {@link CredentialsService}.
     * @return An instance of {@link CredentialsService}.
     */
    static Optional<CredentialsService> create() {
        return Services.get(CredentialsService.class);
    }

    /**
     * Creates a restore credential for the signed-in user, with cloud backup enabled.
     * Equivalent to {@code createRestoreCredential(request, true, onSuccess, onFailure)}.
     *
     * @param request on Android, the WebAuthn {@code PublicKeyCredentialCreationOptionsJSON}
     *                from the server; on iOS, the opaque secret to store
     * @param onSuccess called with the registration response JSON (Android) or the stored
     *                  secret (iOS)
     * @param onFailure called with a {@link CredentialsException} if the credential could not
     *                  be created
     */
    default void createRestoreCredential(String request, Consumer<String> onSuccess,
            Consumer<CredentialsException> onFailure) {
        createRestoreCredential(request, true, onSuccess, onFailure);
    }

    /**
     * Creates a restore credential for the signed-in user. Call it right after the user signs in,
     * and also on launch if the user is signed in and no restore credential has been created yet.
     * Creating a new restore credential replaces the existing one.
     *
     * <p>On Android, when {@code cloudBackup} is {@code true} but the device has no backup or
     * no screen lock, the credential is created without cloud backup instead, so it is still
     * transferred by cable to a new device.</p>
     *
     * @param request on Android, the WebAuthn {@code PublicKeyCredentialCreationOptionsJSON}
     *                from the server; on iOS, the opaque secret to store
     * @param cloudBackup if true, the credential is backed up to the cloud: on Android it is
     *                    included in the app backup and restored on a new device, on iOS it is
     *                    synchronized by iCloud Keychain to all the user's devices; if false, it
     *                    stays on the device and is not transferred or synchronized
     * @param onSuccess called with the registration response JSON (Android) or the stored
     *                  secret (iOS)
     * @param onFailure called with a {@link CredentialsException} if the credential could not
     *                  be created
     */
    void createRestoreCredential(String request, boolean cloudBackup, Consumer<String> onSuccess,
            Consumer<CredentialsException> onFailure);

    /**
     * Retrieves the restore credential, if any, to sign the user in silently. Call it on the
     * first launch of the app on a device.
     *
     * <p>When there is no restore credential on the device, {@code onFailure} is called with a
     * {@link CredentialsException} of type {@link CredentialsException#NO_CREDENTIAL}, and the
     * app should show its regular sign-in flow.</p>
     *
     * <p>The credential is read only when this method is called; there is no notification when
     * it is created, replaced or cleared, whether by this app or, on iOS, by the same app on
     * another device synchronized through iCloud Keychain.</p>
     *
     * @param request on Android, the WebAuthn {@code PublicKeyCredentialRequestOptionsJSON} from
     *                the server; ignored on iOS
     * @param onSuccess called with the authentication response JSON (Android) or the stored
     *                  secret (iOS)
     * @param onFailure called with a {@link CredentialsException} if the credential could not
     *                  be retrieved
     */
    void getRestoreCredential(String request, Consumer<String> onSuccess,
            Consumer<CredentialsException> onFailure);

    /**
     * Deletes the restore credential from the device and from its backup. Call it when the user
     * signs out, or when the server invalidates the session.
     *
     * <p>On iOS a synchronizable credential is also removed from the other devices that share it
     * through iCloud Keychain. On Android other devices are not affected.</p>
     *
     * @param onSuccess called when the credential has been deleted, or there was none
     * @param onFailure called with a {@link CredentialsException} if the credential could not
     *                  be deleted
     */
    void clearRestoreCredential(Runnable onSuccess, Consumer<CredentialsException> onFailure);
}
