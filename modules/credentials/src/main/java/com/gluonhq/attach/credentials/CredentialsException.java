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

/**
 * Signals that a {@link CredentialsService} operation failed. {@link #getType()} identifies the
 * cause with one of the constants defined in this class, so the app can react in the same way on
 * every platform, while {@link #getMessage()} carries the platform-specific details.
 *
 * @since 4.0.27
 */
public class CredentialsException extends Exception {

    /** There is no restore credential on this device. */
    public static final String NO_CREDENTIAL = "NO_CREDENTIAL";

    /** The request was rejected: on Android the JSON is invalid, on iOS the secret is empty. */
    public static final String INVALID_REQUEST = "INVALID_REQUEST";

    /**
     * Cloud backup was requested but it is not available: on Android the device has no backup
     * or no screen lock, and creating the credential without cloud backup failed as well.
     */
    public static final String E2EE_UNAVAILABLE = "E2EE_UNAVAILABLE";

    /**
     * Restore credentials are not supported on this device: on Android it runs a version older
     * than Android 9, or Google Play services are missing or outdated.
     */
    public static final String UNSUPPORTED = "UNSUPPORTED";

    /** The operation was cancelled. */
    public static final String CANCELLED = "CANCELLED";

    /** Any other failure. */
    public static final String UNKNOWN = "UNKNOWN";

    private final String type;

    /**
     * Creates a new exception.
     *
     * @param type one of the constants of this class
     * @param message the platform-specific details
     */
    public CredentialsException(String type, String message) {
        super(message);
        this.type = type == null ? UNKNOWN : type;
    }

    /**
     * Returns the cause of the failure, as one of the constants of this class.
     *
     * @return the type of the failure
     */
    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "CredentialsException[" + type + "]: " + getMessage();
    }
}
