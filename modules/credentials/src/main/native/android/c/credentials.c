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
#include "util.h"

// Graal handles
static jclass jGraalCredentialsClass;
static jmethodID jGraalSetResultMethod;

// Dalvik handles
static jclass jCredentialsServiceClass;
static jobject jDalvikCredentialsService;
static jmethodID jCredentialsServiceCreateMethod;
static jmethodID jCredentialsServiceGetMethod;
static jmethodID jCredentialsServiceClearMethod;

static void initializeGraalHandles(JNIEnv* env) {
    jGraalCredentialsClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/credentials/impl/AndroidCredentialsService"));
    jGraalSetResultMethod = (*env)->GetStaticMethodID(env, jGraalCredentialsClass, "setResult", "(ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
}

static void initializeDalvikHandles() {
    jCredentialsServiceClass = GET_REGISTER_DALVIK_CLASS(jCredentialsServiceClass, "com/gluonhq/helloandroid/DalvikCredentialsService");
    ATTACH_DALVIK();
    jmethodID jCredentialsServiceInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jCredentialsServiceClass, "<init>", "(Landroid/app/Activity;)V");
    jCredentialsServiceCreateMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jCredentialsServiceClass, "createRestoreCredential", "(Ljava/lang/String;Z)V");
    jCredentialsServiceGetMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jCredentialsServiceClass, "getRestoreCredential", "(Ljava/lang/String;)V");
    jCredentialsServiceClearMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jCredentialsServiceClass, "clearRestoreCredential", "()V");

    jobject jActivity = substrateGetActivity();
    jobject jtmpobj = (*dalvikEnv)->NewObject(dalvikEnv, jCredentialsServiceClass, jCredentialsServiceInitMethod, jActivity);
    jDalvikCredentialsService = (*dalvikEnv)->NewGlobalRef(dalvikEnv, jtmpobj);
    DETACH_DALVIK();
}

//////////////////////////
// From Graal to native //
//////////////////////////


JNIEXPORT jint JNICALL
JNI_OnLoad_credentials(JavaVM *vm, void *reserved)
{
    ATTACH_LOG_INFO("JNI_OnLoad_credentials called");
#ifdef JNI_VERSION_1_8
    JNIEnv* graalEnv;
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native Credentials from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("[Credentials Service] Initializing native Credentials from OnLoad");
    initializeGraalHandles(graalEnv);
    initializeDalvikHandles();
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

// from Java to Android

JNIEXPORT void JNICALL Java_com_gluonhq_attach_credentials_impl_AndroidCredentialsService_nativeCreateRestoreCredential
(JNIEnv *env, jclass jClass, jstring jrequest, jboolean jcloudBackup)
{
    const char *requestChars = (*env)->GetStringUTFChars(env, jrequest, NULL);
    if (isDebugAttach()) {
        ATTACH_LOG_FINE("Credentials create restore credential, cloud backup %d\n", jcloudBackup);
    }
    ATTACH_DALVIK();
    jstring drequest = (*dalvikEnv)->NewStringUTF(dalvikEnv, requestChars);
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikCredentialsService, jCredentialsServiceCreateMethod, drequest, jcloudBackup);
    DETACH_DALVIK();
    // (*env)->ReleaseStringUTFChars(env, jrequest, requestChars);
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_credentials_impl_AndroidCredentialsService_nativeGetRestoreCredential
(JNIEnv *env, jclass jClass, jstring jrequest)
{
    const char *requestChars = (*env)->GetStringUTFChars(env, jrequest, NULL);
    if (isDebugAttach()) {
        ATTACH_LOG_FINE("Credentials get restore credential\n");
    }
    ATTACH_DALVIK();
    jstring drequest = (*dalvikEnv)->NewStringUTF(dalvikEnv, requestChars);
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikCredentialsService, jCredentialsServiceGetMethod, drequest);
    DETACH_DALVIK();
    // (*env)->ReleaseStringUTFChars(env, jrequest, requestChars);
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_credentials_impl_AndroidCredentialsService_nativeClearRestoreCredential
(JNIEnv *env, jclass jClass)
{
    if (isDebugAttach()) {
        ATTACH_LOG_FINE("Credentials clear restore credential\n");
    }
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikCredentialsService, jCredentialsServiceClearMethod);
    DETACH_DALVIK();
}

///////////////////////////
// From Dalvik to native //
///////////////////////////

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_DalvikCredentialsService_nativeResult(
    JNIEnv *env, jobject service, jint joperation, jstring jresult, jstring jerrorType, jstring jerrorMessage) {
    const char *resultChars = (jresult == NULL) ? NULL : (*env)->GetStringUTFChars(env, jresult, NULL);
    const char *errorTypeChars = (jerrorType == NULL) ? NULL : (*env)->GetStringUTFChars(env, jerrorType, NULL);
    const char *errorMessageChars = (jerrorMessage == NULL) ? NULL : (*env)->GetStringUTFChars(env, jerrorMessage, NULL);
    if (isDebugAttach()) {
        ATTACH_LOG_FINE("Credentials result for operation %d, error type %s\n", joperation,
                (errorTypeChars == NULL) ? "none" : errorTypeChars);
    }
    ATTACH_GRAAL();
    jstring gresult = (resultChars == NULL) ? NULL : (*graalEnv)->NewStringUTF(graalEnv, resultChars);
    jstring gerrorType = (errorTypeChars == NULL) ? NULL : (*graalEnv)->NewStringUTF(graalEnv, errorTypeChars);
    jstring gerrorMessage = (errorMessageChars == NULL) ? NULL : (*graalEnv)->NewStringUTF(graalEnv, errorMessageChars);
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalCredentialsClass, jGraalSetResultMethod, joperation, gresult, gerrorType, gerrorMessage);
    DETACH_GRAAL();
    // if (resultChars != NULL) (*env)->ReleaseStringUTFChars(env, jresult, resultChars);
    // if (errorTypeChars != NULL) (*env)->ReleaseStringUTFChars(env, jerrorType, errorTypeChars);
    // if (errorMessageChars != NULL) (*env)->ReleaseStringUTFChars(env, jerrorMessage, errorMessageChars);
}
