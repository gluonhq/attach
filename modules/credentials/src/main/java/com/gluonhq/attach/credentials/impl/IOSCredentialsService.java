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
package com.gluonhq.attach.credentials.impl;

import com.gluonhq.attach.credentials.CredentialsException;
import com.gluonhq.attach.credentials.CredentialsService;
import com.gluonhq.attach.util.Util;
import javafx.application.Platform;

import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * iOS implementation, backed by the Keychain: the restore credential is an opaque secret
 * provided by the app, stored as a generic password item private to the app. With cloud
 * backup the item is synchronizable (iCloud Keychain), otherwise it is device only.
 */
public class IOSCredentialsService implements CredentialsService {

    private static final Logger LOG = Logger.getLogger(IOSCredentialsService.class.getName());

    // Security framework OSStatus codes
    private static final int ERR_SEC_SUCCESS = 0;
    private static final int ERR_SEC_PARAM = -50;
    private static final int ERR_SEC_ITEM_NOT_FOUND = -25300;
    private static final int ERR_SEC_MISSING_ENTITLEMENT = -34018;

    static {
        System.loadLibrary("Credentials");
    }

    public IOSCredentialsService() {
        if (Util.DEBUG) {
            LOG.info("IOSCredentialsService <init>");
        }
    }

    @Override
    public void createRestoreCredential(String request, boolean cloudBackup, Consumer<String> onSuccess,
            Consumer<CredentialsException> onFailure) {
        if (request == null || request.isEmpty()) {
            fail(onFailure, CredentialsException.INVALID_REQUEST, "The secret to store cannot be null or empty");
            return;
        }
        if (Util.DEBUG) {
            LOG.info("Store restore credential in the Keychain, synchronizable: " + cloudBackup);
        }
        int status = nativeStore(request, cloudBackup);
        if (status != ERR_SEC_SUCCESS) {
            fail(onFailure, typeFor(status), "Error storing the restore credential in the Keychain, OSStatus: " + status);
            return;
        }
        succeed(onSuccess, request);
    }

    @Override
    public void getRestoreCredential(String request, Consumer<String> onSuccess,
            Consumer<CredentialsException> onFailure) {
        if (Util.DEBUG) {
            LOG.info("Read restore credential from the Keychain");
        }
        int[] status = new int[1];
        String value = nativeGet(status);
        if (status[0] != ERR_SEC_SUCCESS || value == null) {
            fail(onFailure, typeFor(status[0]), status[0] == ERR_SEC_ITEM_NOT_FOUND
                    ? "There is no restore credential in the Keychain"
                    : "Error reading the restore credential from the Keychain, OSStatus: " + status[0]);
            return;
        }
        succeed(onSuccess, value);
    }

    @Override
    public void clearRestoreCredential(Runnable onSuccess, Consumer<CredentialsException> onFailure) {
        if (Util.DEBUG) {
            LOG.info("Delete restore credential from the Keychain");
        }
        int status = nativeClear();
        if (status != ERR_SEC_SUCCESS && status != ERR_SEC_ITEM_NOT_FOUND) {
            fail(onFailure, typeFor(status), "Error deleting the restore credential from the Keychain, OSStatus: " + status);
            return;
        }
        if (onSuccess != null) {
            Platform.runLater(onSuccess);
        }
    }

    private static String typeFor(int status) {
        switch (status) {
            case ERR_SEC_ITEM_NOT_FOUND: return CredentialsException.NO_CREDENTIAL;
            case ERR_SEC_PARAM: return CredentialsException.INVALID_REQUEST;
            case ERR_SEC_MISSING_ENTITLEMENT: return CredentialsException.UNSUPPORTED;
            default: return CredentialsException.UNKNOWN;
        }
    }

    private static void succeed(Consumer<String> onSuccess, String value) {
        if (onSuccess != null) {
            Platform.runLater(() -> onSuccess.accept(value));
        }
    }

    private static void fail(Consumer<CredentialsException> onFailure, String type, String message) {
        LOG.warning("Restore credential operation failed [" + type + "]: " + message);
        if (onFailure != null) {
            Platform.runLater(() -> onFailure.accept(new CredentialsException(type, message)));
        }
    }

    // native
    private static native int nativeStore(String value, boolean synchronizable);
    private static native String nativeGet(int[] status);
    private static native int nativeClear();
}
