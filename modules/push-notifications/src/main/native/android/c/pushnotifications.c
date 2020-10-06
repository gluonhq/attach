/*
 * Copyright (c) 2020, Gluon
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
#include "pushnotifications.h"

// Graal handles
static jclass jGraalPushNotificationsClass;
static jmethodID jGraalSetTokenMethod;
static jmethodID jGraalPushNotificationsProcessMethod;

static jobject jDalvikPushNotificationsService;
jmethodID jDalvikPushNotificationsServiceGetPackageName;
jmethodID jDalvikPushNotificationsServiceIsGooglePlayServicesAvailable;
jmethodID jDalvikPushNotificationsServiceGetErrorString;
jmethodID jDalvikPushNotificationsServiceInitializeFirebase;

static void initializeGraalHandles(JNIEnv* env) {
    jGraalPushNotificationsClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/pushnotifications/impl/AndroidPushNotificationsService"));
    jGraalSetTokenMethod = (*env)->GetStaticMethodID(env, jGraalPushNotificationsClass, "setToken", "(Ljava/lang/String;)V");
    jGraalPushNotificationsProcessMethod = (*env)->GetStaticMethodID(env, jGraalPushNotificationsClass, "processRuntimeArgs", "(Ljava/lang/String;Ljava/lang/String;)V");
}

static void initializePushNotificationsDalvikHandles() {
    jclass activityClass = substrateGetActivityClass();
    jclass jPushNotificationsServiceClass = substrateGetPushNotificationsServiceClass();

    ATTACH_DALVIK();
    jDalvikPushNotificationsServiceGetPackageName = (*dalvikEnv)->GetMethodID(dalvikEnv, jPushNotificationsServiceClass, "getPackageName", "()Ljava/lang/String;");
    jDalvikPushNotificationsServiceIsGooglePlayServicesAvailable = (*dalvikEnv)->GetMethodID(dalvikEnv, jPushNotificationsServiceClass, "isGooglePlayServicesAvailable", "()I");
    jDalvikPushNotificationsServiceGetErrorString = (*dalvikEnv)->GetMethodID(dalvikEnv, jPushNotificationsServiceClass, "getErrorString", "(I)Ljava/lang/String;");
    jDalvikPushNotificationsServiceInitializeFirebase = (*dalvikEnv)->GetMethodID(dalvikEnv, jPushNotificationsServiceClass, "initializeFirebase", "(Ljava/lang/String;Ljava/lang/String;)V");

    jmethodID jPushNotificationsServiceInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jPushNotificationsServiceClass, "<init>", "(Landroid/app/Activity;)V");
    jthrowable t = (*dalvikEnv)->ExceptionOccurred(dalvikEnv);
    if (t) {
        ATTACH_LOG_INFO("EXCEPTION occurred when dealing with dalvik handles\n");
        (*dalvikEnv)->ExceptionClear(dalvikEnv);
    }

    jobject jActivity = substrateGetActivity();
    jobject jObj = (*dalvikEnv)->NewObject(dalvikEnv, jPushNotificationsServiceClass, jPushNotificationsServiceInitMethod, jActivity);
    jDalvikPushNotificationsService = (jobject)(*dalvikEnv)->NewGlobalRef(dalvikEnv, jObj);
    DETACH_DALVIK();
}

//////////////////////////
// From Graal to native //
//////////////////////////

JNIEXPORT jint JNICALL
JNI_OnLoad_pushnotifications(JavaVM *vm, void *reserved)
{
#ifdef JNI_VERSION_1_8
    JNIEnv* graalEnv;
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native PushNotifications from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("Initializing native PushNotifications from OnLoad");
    initializeGraalHandles(graalEnv);
    initializePushNotificationsDalvikHandles();
    ATTACH_LOG_FINE("Initializing native PushNotifications from OnLoad Done");
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

// from Java to Android

JNIEXPORT jstring JNICALL Java_com_gluonhq_attach_pushnotifications_impl_AndroidPushNotificationsService_getPackageName
(JNIEnv *env, jobject service) {
    ATTACH_DALVIK();
    jstring dalvikPackageName = (jstring) (*dalvikEnv)->CallObjectMethod(dalvikEnv, jDalvikPushNotificationsService, jDalvikPushNotificationsServiceGetPackageName);
    const char *packageNameChars = (*dalvikEnv)->GetStringUTFChars(dalvikEnv, dalvikPackageName, NULL);
    jstring graalPackageName = (*env)->NewStringUTF(env, packageNameChars);
    (*dalvikEnv)->ReleaseStringUTFChars(dalvikEnv, dalvikPackageName, packageNameChars);
    DETACH_DALVIK();
    return graalPackageName;
}

JNIEXPORT jint JNICALL Java_com_gluonhq_attach_pushnotifications_impl_AndroidPushNotificationsService_isGooglePlayServicesAvailable
(JNIEnv *env, jobject service) {
    ATTACH_DALVIK();
    jint available = (*dalvikEnv)->CallIntMethod(dalvikEnv, jDalvikPushNotificationsService, jDalvikPushNotificationsServiceIsGooglePlayServicesAvailable);
    DETACH_DALVIK();
    return available;
}

JNIEXPORT jstring JNICALL Java_com_gluonhq_attach_pushnotifications_impl_AndroidPushNotificationsService_getErrorString
(JNIEnv *env, jobject service, jint resultCode) {
    ATTACH_DALVIK();
    jstring dalvikErrorString = (jstring) (*dalvikEnv)->CallObjectMethod(dalvikEnv, jDalvikPushNotificationsService, jDalvikPushNotificationsServiceGetErrorString,
                resultCode);
    const char *errorStringChars = (*dalvikEnv)->GetStringUTFChars(dalvikEnv, dalvikErrorString, NULL);
    if (debugAttach) {
        ATTACH_LOG_FINE("PushNotification error string: %s", errorStringChars);
    }
    jstring graalErrorString = (*env)->NewStringUTF(env, errorStringChars);
    (*dalvikEnv)->ReleaseStringUTFChars(dalvikEnv, dalvikErrorString, errorStringChars);
    DETACH_DALVIK();
    return graalErrorString;
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_pushnotifications_impl_AndroidPushNotificationsService_initializeFirebase
(JNIEnv *env, jobject service, jstring applicationId, jstring projectNumber) {
    const char *applicationIdChars = (*env)->GetStringUTFChars(env, applicationId, NULL);
    const char *projectNumberChars = (*env)->GetStringUTFChars(env, projectNumber, NULL);
    if (debugAttach) {
        ATTACH_LOG_FINE("PushNotification::initializeFirebase with app Id: %s and project number: %s", applicationIdChars, projectNumberChars);
    }
    ATTACH_DALVIK();
    jstring dalvikApplicationId = (*dalvikEnv)->NewStringUTF(dalvikEnv, applicationIdChars);
    jstring dalvikProjectNumber = (*dalvikEnv)->NewStringUTF(dalvikEnv, projectNumberChars);
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikPushNotificationsService, jDalvikPushNotificationsServiceInitializeFirebase,
                                                        dalvikApplicationId, dalvikProjectNumber);
    (*dalvikEnv)->DeleteLocalRef(dalvikEnv, dalvikApplicationId);
    (*dalvikEnv)->DeleteLocalRef(dalvikEnv, dalvikProjectNumber);
    DETACH_DALVIK();
    (*env)->ReleaseStringUTFChars(env, applicationId, applicationIdChars);
    (*env)->ReleaseStringUTFChars(env, projectNumber, projectNumberChars);
}

///////////////////////////
// From Dalvik to native //
///////////////////////////

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_PushInstanceIdService_sendToken
    (JNIEnv *env, jobject service, jstring jtoken) {
    const char *tokenChars = (*env)->GetStringUTFChars(env, jtoken, NULL);
    if (debugAttach) {
        ATTACH_LOG_FINE("PushNotifications:: Native layer got token: %s", tokenChars);
    }
    ATTACH_GRAAL();
    jstring jTokenChars = (*graalEnv)->NewStringUTF(graalEnv, tokenChars);
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalPushNotificationsClass, jGraalSetTokenMethod, jTokenChars);
    (*graalEnv)->DeleteLocalRef(graalEnv, jTokenChars);
    DETACH_GRAAL();
    (*env)->ReleaseStringUTFChars(env, jtoken, tokenChars);
}

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_DalvikPushNotificationsService_processRuntimeArgs
    (JNIEnv *env, jobject service, jstring jkey, jstring jvalue) {
    const char *keyChars = (*env)->GetStringUTFChars(env, jkey, NULL);
    const char *valueChars = (*env)->GetStringUTFChars(env, jvalue, NULL);
    if (debugAttach) {
        ATTACH_LOG_FINE("PushNotifications:: Native layer got key: %s, value: %s", keyChars, valueChars);
    }
    ATTACH_GRAAL();
    jstring jKeyChars = (*graalEnv)->NewStringUTF(graalEnv, keyChars);
    jstring jValueChars = (*graalEnv)->NewStringUTF(graalEnv, valueChars);
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalPushNotificationsClass, jGraalPushNotificationsProcessMethod, jKeyChars, jValueChars);
    (*graalEnv)->DeleteLocalRef(graalEnv, jKeyChars);
    (*graalEnv)->DeleteLocalRef(graalEnv, jValueChars);
    DETACH_GRAAL();
    (*env)->ReleaseStringUTFChars(env, jkey, keyChars);
    (*env)->ReleaseStringUTFChars(env, jvalue, valueChars);
}