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

public class AndroidCredentialsService implements CredentialsService {

    private static final Logger LOG = Logger.getLogger(AndroidCredentialsService.class.getName());

    // operations, as sent to and received from the Dalvik layer
    private static final int OP_CREATE = 0;
    private static final int OP_GET = 1;
    private static final int OP_CLEAR = 2;

    private static final Pending[] PENDING = new Pending[3];

    private static final class Pending {
        private final Consumer<String> onSuccess;
        private final Consumer<CredentialsException> onFailure;

        private Pending(Consumer<String> onSuccess, Consumer<CredentialsException> onFailure) {
            this.onSuccess = onSuccess;
            this.onFailure = onFailure;
        }
    }

    static {
        System.loadLibrary("credentials");
    }

    public AndroidCredentialsService() {
        if (Util.DEBUG) {
            LOG.info("AndroidCredentialsService <init>");
        }
    }

    @Override
    public void createRestoreCredential(String request, boolean cloudBackup, Consumer<String> onSuccess,
            Consumer<CredentialsException> onFailure) {
        if (request == null || request.isEmpty()) {
            fail(onFailure, CredentialsException.INVALID_REQUEST, "The creation options cannot be null or empty");
            return;
        }
        if (!register(OP_CREATE, onSuccess, onFailure)) {
            return;
        }
        if (Util.DEBUG) {
            LOG.info("Create restore credential, cloud backup: " + cloudBackup);
        }
        nativeCreateRestoreCredential(request, cloudBackup);
    }

    @Override
    public void getRestoreCredential(String request, Consumer<String> onSuccess,
            Consumer<CredentialsException> onFailure) {
        if (request == null || request.isEmpty()) {
            fail(onFailure, CredentialsException.INVALID_REQUEST, "The request options cannot be null or empty");
            return;
        }
        if (!register(OP_GET, onSuccess, onFailure)) {
            return;
        }
        if (Util.DEBUG) {
            LOG.info("Get restore credential");
        }
        nativeGetRestoreCredential(request);
    }

    @Override
    public void clearRestoreCredential(Runnable onSuccess, Consumer<CredentialsException> onFailure) {
        if (!register(OP_CLEAR, s -> {
            if (onSuccess != null) {
                onSuccess.run();
            }
        }, onFailure)) {
            return;
        }
        if (Util.DEBUG) {
            LOG.info("Clear restore credential");
        }
        nativeClearRestoreCredential();
    }

    private static synchronized boolean register(int operation, Consumer<String> onSuccess,
            Consumer<CredentialsException> onFailure) {
        if (PENDING[operation] != null) {
            fail(onFailure, CredentialsException.UNKNOWN, "The same restore credential operation is already in progress");
            return false;
        }
        PENDING[operation] = new Pending(onSuccess, onFailure);
        return true;
    }

    private static synchronized Pending take(int operation) {
        Pending pending = PENDING[operation];
        PENDING[operation] = null;
        return pending;
    }

    private static void fail(Consumer<CredentialsException> onFailure, String type, String message) {
        LOG.warning("Restore credential operation failed [" + type + "]: " + message);
        if (onFailure != null) {
            Platform.runLater(() -> onFailure.accept(new CredentialsException(type, message)));
        }
    }

    // native
    private static native void nativeCreateRestoreCredential(String requestJson, boolean cloudBackup);
    private static native void nativeGetRestoreCredential(String requestJson);
    private static native void nativeClearRestoreCredential();

    // callback
    public static void setResult(int operation, String result, String errorType, String errorMessage) {
        if (operation < 0 || operation >= PENDING.length) {
            LOG.warning("Unknown restore credential operation: " + operation);
            return;
        }
        final Pending pending = take(operation);
        if (pending == null) {
            LOG.warning("No callback registered for restore credential operation " + operation);
            return;
        }
        if (errorType != null) {
            fail(pending.onFailure, errorType, errorMessage);
            return;
        }
        if (Util.DEBUG) {
            LOG.info("Restore credential operation " + operation + " succeeded");
        }
        if (pending.onSuccess != null) {
            Platform.runLater(() -> pending.onSuccess.accept(result));
        }
    }
}
