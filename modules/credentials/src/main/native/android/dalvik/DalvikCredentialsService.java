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
package com.gluonhq.helloandroid;

import android.app.Activity;
import android.util.Log;

import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CreateCredentialResponse;
import androidx.credentials.CreateRestoreCredentialRequest;
import androidx.credentials.CreateRestoreCredentialResponse;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.GetRestoreCredentialOption;
import androidx.credentials.RestoreCredential;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.credentials.exceptions.ClearCredentialProviderConfigurationException;
import androidx.credentials.exceptions.ClearCredentialUnsupportedException;
import androidx.credentials.exceptions.CreateCredentialCancellationException;
import androidx.credentials.exceptions.CreateCredentialException;
import androidx.credentials.exceptions.CreateCredentialProviderConfigurationException;
import androidx.credentials.exceptions.CreateCredentialUnsupportedException;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException;
import androidx.credentials.exceptions.GetCredentialUnsupportedException;
import androidx.credentials.exceptions.NoCredentialException;
import androidx.credentials.exceptions.restorecredential.CreateRestoreCredentialDomException;
import androidx.credentials.exceptions.restorecredential.E2eeUnavailableException;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Restore Credentials, through Credential Manager (androidx.credentials). All calls are
 * asynchronous and report back through {@link #nativeResult}, on a background thread.
 */
public class DalvikCredentialsService {

    private static final String TAG = Util.TAG;

    // operations, matching AndroidCredentialsService
    private static final int OP_CREATE = 0;
    private static final int OP_GET = 1;
    private static final int OP_CLEAR = 2;

    // error types, matching CredentialsException
    private static final String NO_CREDENTIAL = "NO_CREDENTIAL";
    private static final String INVALID_REQUEST = "INVALID_REQUEST";
    private static final String E2EE_UNAVAILABLE = "E2EE_UNAVAILABLE";
    private static final String UNSUPPORTED = "UNSUPPORTED";
    private static final String CANCELLED = "CANCELLED";
    private static final String UNKNOWN = "UNKNOWN";

    private final Activity activity;
    private final boolean debug;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private CredentialManager credentialManager;

    public DalvikCredentialsService(Activity activity) {
        this.activity = activity;
        this.debug = Util.isDebug();
    }

    private CredentialManager getCredentialManager() {
        if (credentialManager == null) {
            credentialManager = CredentialManager.create(activity);
        }
        return credentialManager;
    }

    private void createRestoreCredential(final String requestJson, final boolean cloudBackup) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createRestoreCredential(requestJson, cloudBackup, cloudBackup);
            }
        });
    }

    private void createRestoreCredential(final String requestJson, final boolean cloudBackup,
            final boolean fallbackToLocal) {
        final CreateRestoreCredentialRequest request;
        try {
            request = new CreateRestoreCredentialRequest(requestJson, cloudBackup);
        } catch (IllegalArgumentException e) {
            nativeResult(OP_CREATE, null, INVALID_REQUEST, "Invalid creation options: " + e.getMessage());
            return;
        }
        if (debug) {
            Log.v(TAG, "Creating restore credential, cloud backup: " + cloudBackup);
        }
        try {
            getCredentialManager().createCredentialAsync(activity, request, null, executor,
                    new CredentialManagerCallback<CreateCredentialResponse, CreateCredentialException>() {
                @Override
                public void onResult(CreateCredentialResponse response) {
                    if (response instanceof CreateRestoreCredentialResponse) {
                        if (debug) {
                            Log.v(TAG, "Restore credential created");
                        }
                        nativeResult(OP_CREATE, ((CreateRestoreCredentialResponse) response).getResponseJson(), null, null);
                    } else {
                        nativeResult(OP_CREATE, null, UNKNOWN, "Unexpected response type: " + response.getType());
                    }
                }

                @Override
                public void onError(CreateCredentialException e) {
                    if (e instanceof E2eeUnavailableException && fallbackToLocal) {
                        Log.w(TAG, "Cloud backup is not available (backup or screen lock disabled), "
                                + "creating a device-only restore credential instead");
                        createRestoreCredential(requestJson, false, false);
                        return;
                    }
                    String type;
                    if (e instanceof E2eeUnavailableException) {
                        type = E2EE_UNAVAILABLE;
                    } else if (e instanceof CreateRestoreCredentialDomException) {
                        type = INVALID_REQUEST;
                    } else if (e instanceof CreateCredentialProviderConfigurationException
                            || e instanceof CreateCredentialUnsupportedException) {
                        type = UNSUPPORTED;
                    } else if (e instanceof CreateCredentialCancellationException) {
                        type = CANCELLED;
                    } else {
                        type = UNKNOWN;
                    }
                    Log.e(TAG, "Error creating restore credential: " + describe(e.getType(), e));
                    nativeResult(OP_CREATE, null, type, describe(e.getType(), e));
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error creating restore credential", e);
            nativeResult(OP_CREATE, null, UNKNOWN, describe(null, e));
        }
    }

    private void getRestoreCredential(final String requestJson) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                doGetRestoreCredential(requestJson);
            }
        });
    }

    private void doGetRestoreCredential(String requestJson) {
        final GetCredentialRequest request;
        try {
            request = new GetCredentialRequest.Builder()
                    .addCredentialOption(new GetRestoreCredentialOption(requestJson))
                    .build();
        } catch (IllegalArgumentException e) {
            nativeResult(OP_GET, null, INVALID_REQUEST, "Invalid request options: " + e.getMessage());
            return;
        }
        if (debug) {
            Log.v(TAG, "Getting restore credential");
        }
        try {
            getCredentialManager().getCredentialAsync(activity, request, null, executor,
                    new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                @Override
                public void onResult(GetCredentialResponse response) {
                    Credential credential = response.getCredential();
                    if (credential instanceof RestoreCredential) {
                        if (debug) {
                            Log.v(TAG, "Restore credential retrieved");
                        }
                        nativeResult(OP_GET, ((RestoreCredential) credential).getAuthenticationResponseJson(), null, null);
                    } else {
                        nativeResult(OP_GET, null, UNKNOWN, "Unexpected credential type: " + credential.getType());
                    }
                }

                @Override
                public void onError(GetCredentialException e) {
                    String type;
                    if (e instanceof NoCredentialException) {
                        type = NO_CREDENTIAL;
                    } else if (e instanceof GetCredentialProviderConfigurationException
                            || e instanceof GetCredentialUnsupportedException) {
                        type = UNSUPPORTED;
                    } else if (e instanceof GetCredentialCancellationException) {
                        type = CANCELLED;
                    } else {
                        type = UNKNOWN;
                    }
                    if (NO_CREDENTIAL.equals(type)) {
                        if (debug) {
                            Log.v(TAG, "No restore credential available");
                        }
                    } else {
                        Log.e(TAG, "Error getting restore credential: " + describe(e.getType(), e));
                    }
                    nativeResult(OP_GET, null, type, describe(e.getType(), e));
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error getting restore credential", e);
            nativeResult(OP_GET, null, UNKNOWN, describe(null, e));
        }
    }

    private void clearRestoreCredential() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                doClearRestoreCredential();
            }
        });
    }

    private void doClearRestoreCredential() {
        if (debug) {
            Log.v(TAG, "Clearing restore credential");
        }
        try {
            ClearCredentialStateRequest request =
                    new ClearCredentialStateRequest(ClearCredentialStateRequest.TYPE_CLEAR_RESTORE_CREDENTIAL);
            getCredentialManager().clearCredentialStateAsync(request, null, executor,
                    new CredentialManagerCallback<Void, ClearCredentialException>() {
                @Override
                public void onResult(Void unused) {
                    if (debug) {
                        Log.v(TAG, "Restore credential cleared");
                    }
                    nativeResult(OP_CLEAR, null, null, null);
                }

                @Override
                public void onError(ClearCredentialException e) {
                    String type;
                    if (e instanceof ClearCredentialProviderConfigurationException
                            || e instanceof ClearCredentialUnsupportedException) {
                        type = UNSUPPORTED;
                    } else {
                        type = UNKNOWN;
                    }
                    Log.e(TAG, "Error clearing restore credential: " + describe(e.getType(), e));
                    nativeResult(OP_CLEAR, null, type, describe(e.getType(), e));
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error clearing restore credential", e);
            nativeResult(OP_CLEAR, null, UNKNOWN, describe(null, e));
        }
    }

    private static String describe(String type, Exception e) {
        String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        return type == null ? message : type + ": " + message;
    }

    private native void nativeResult(int operation, String result, String errorType, String errorMessage);
}
