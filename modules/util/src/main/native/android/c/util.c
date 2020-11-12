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
#include "util.h"

JavaVM* graalVM;

// Dalvik handles
static jclass jUtilClass;
static jclass jPermissionActivityClass;
static jmethodID jUtilEnableDebugMethod;
static jmethodID jUtilOnActivityResultMethod;
static jmethodID jUtilOnLifecycleEventMethod;
static jmethodID jUtilRequestPermissionsMethod;

static jboolean initialized;

// Attach classes
static jclass jAccelerometerServiceClass;
static jclass jAudioServiceClass;
static jclass jAugmentedRealityServiceClass;
static jclass jBatteryServiceClass;
static jclass jBarcodeScanServiceClass;
static jclass jBleServiceClass;
static jclass jBrowserServiceClass;
static jclass jConnectivityServiceClass;
static jclass jDeviceServiceClass;
static jclass jDialerServiceClass;
static jclass jDisplayServiceClass;
static jclass jKeyboardServiceClass;
static jclass jLifecycleServiceClass;
static jclass jLocalNotificationsServiceClass;
static jclass jMagnetometerServiceClass;
static jclass jOrientationServiceClass;
static jclass jPicturesServiceClass;
static jclass jPositionServiceClass;
static jclass jPushNotificationsServiceClass;
static jclass jRuntimeArgsServiceClass;
static jclass jSettingsServiceClass;
static jclass jShareServiceClass;
static jclass jStatusBarServiceClass;
static jclass jStorageServiceClass;
static jclass jVibrationServiceClass;
static jclass jVideoServiceClass;

static jmethodID loadClassMethod;
static jobject classLoaderObject;

static jclass registerClass(const char* className) {
    ATTACH_DALVIK();
    if (loadClassMethod == NULL || classLoaderObject == NULL) {
        jmethodID getClassLoaderMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, activityClass, "getClassLoader", "()Ljava/lang/ClassLoader;");
        classLoaderObject = (*dalvikEnv)->NewGlobalRef(dalvikEnv, (*dalvikEnv)->CallObjectMethod(dalvikEnv, activity, getClassLoaderMethod));
        jclass classLoader = (*dalvikEnv)->FindClass(dalvikEnv, "java/lang/ClassLoader");
        loadClassMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, classLoader, "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;");
    }

    ATTACH_LOG_FINE("Util :: Load className %s", className);
    jstring strClassName = (*dalvikEnv)->NewStringUTF(dalvikEnv, className);
    jclass classRetrieved = (jclass) (*dalvikEnv)->CallObjectMethod(dalvikEnv, classLoaderObject, loadClassMethod, strClassName);
    jthrowable t = (*dalvikEnv)->ExceptionOccurred(dalvikEnv);
    if (t) {
        (*dalvikEnv)->ExceptionClear(dalvikEnv);
    }
    (*dalvikEnv)->DeleteLocalRef(dalvikEnv, strClassName);
    if ((t == NULL) && (classRetrieved != NULL)) {
        return (jclass)(*dalvikEnv)->NewGlobalRef(dalvikEnv, classRetrieved);
    }
    return NULL;
}

static void initializeUtilDalvikHandles() {
    ATTACH_LOG_FINE("Init Util");
    jUtilClass = substrateGetUtilClass();
    jPermissionActivityClass = substrateGetPermissionActivityClass();
    ATTACH_DALVIK();
    jmethodID jUtilInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jUtilClass, "<init>", "()V");
    jUtilEnableDebugMethod = (*dalvikEnv)->GetStaticMethodID(dalvikEnv, jUtilClass, "enableDebug", "()V");
    jUtilOnActivityResultMethod = (*dalvikEnv)->GetStaticMethodID(dalvikEnv, jUtilClass, "onActivityResult", "(IILandroid/content/Intent;)V");
    jUtilOnLifecycleEventMethod = (*dalvikEnv)->GetStaticMethodID(dalvikEnv, jUtilClass, "lifecycleEvent", "(Ljava/lang/String;)V");
    jUtilRequestPermissionsMethod = (*dalvikEnv)->GetStaticMethodID(dalvikEnv, jPermissionActivityClass, "verifyPermissions", "(Landroid/app/Activity;[Ljava/lang/String;)Z");
    jthrowable t = (*dalvikEnv)->ExceptionOccurred(dalvikEnv);
    if (t) {
        ATTACH_LOG_INFO("EXCEPTION occurred when dealing with dalvik handles\n");
        (*dalvikEnv)->ExceptionClear(dalvikEnv);
    }

    jobject util = (*dalvikEnv)->NewObject(dalvikEnv, jUtilClass, jUtilInitMethod);
    DETACH_DALVIK();
    ATTACH_LOG_FINE("Dalvik Util init was called");
    initialized = JNI_TRUE;
}

jclass substrateGetUtilClass() {
    return GETREGISTERCLASS(jUtilClass, "com/gluonhq/helloandroid/Util");
}

jclass substrateGetAccelerometerServiceClass() {
    return GETREGISTERCLASS(jAccelerometerServiceClass, "com/gluonhq/helloandroid/DalvikAccelerometerService");
}

jclass substrateGetAudioServiceClass() {
    return GETREGISTERCLASS(jAudioServiceClass, "com/gluonhq/helloandroid/DalvikAudioService");
}

jclass substrateGetAugmentedRealityServiceClass() {
    return GETREGISTERCLASS(jAugmentedRealityServiceClass, "com/gluonhq/helloandroid/DalvikAugmentedRealityService");
}

jclass substrateGetBatteryServiceClass() {
    return GETREGISTERCLASS(jBatteryServiceClass, "com/gluonhq/helloandroid/DalvikBatteryService");
}

jclass substrateGetBarcodeScanServiceClass() {
    return GETREGISTERCLASS(jBarcodeScanServiceClass, "com/gluonhq/helloandroid/DalvikBarcodeScanService");
}

jclass substrateGetBleServiceClass() {
    return GETREGISTERCLASS(jBleServiceClass, "com/gluonhq/helloandroid/DalvikBleService");
}

jclass substrateGetBrowserServiceClass() {
    return GETREGISTERCLASS(jBrowserServiceClass, "com/gluonhq/helloandroid/DalvikBrowserService");
}

jclass substrateGetConnectivityServiceClass() {
    return GETREGISTERCLASS(jConnectivityServiceClass, "com/gluonhq/helloandroid/DalvikConnectivityService");
}

jclass substrateGetDeviceServiceClass() {
    return GETREGISTERCLASS(jDeviceServiceClass, "com/gluonhq/helloandroid/DalvikDeviceService");
}

jclass substrateGetDialerServiceClass() {
    return GETREGISTERCLASS(jDialerServiceClass, "com/gluonhq/helloandroid/DalvikDialerService");
}

jclass substrateGetDisplayServiceClass() {
    return GETREGISTERCLASS(jDisplayServiceClass, "com/gluonhq/helloandroid/DalvikDisplayService");
}

jclass substrateGetKeyboardServiceClass() {
    return GETREGISTERCLASS(jKeyboardServiceClass, "com/gluonhq/helloandroid/KeyboardService");
}

jclass substrateGetLifecycleServiceClass() {
    return GETREGISTERCLASS(jLifecycleServiceClass, "com/gluonhq/helloandroid/DalvikLifecycleService");
}

jclass substrateGetLocalNotificationsServiceClass() {
    return GETREGISTERCLASS(jLocalNotificationsServiceClass, "com/gluonhq/helloandroid/DalvikLocalNotificationsService");
}

jclass substrateGetMagnetometerServiceClass() {
    return GETREGISTERCLASS(jMagnetometerServiceClass, "com/gluonhq/helloandroid/DalvikMagnetometerService");
}

jclass substrateGetOrientationServiceClass() {
    return GETREGISTERCLASS(jOrientationServiceClass, "com/gluonhq/helloandroid/DalvikOrientationService");
}

jclass substrateGetPicturesServiceClass() {
    return GETREGISTERCLASS(jPicturesServiceClass, "com/gluonhq/helloandroid/DalvikPicturesService");
}

jclass substrateGetPositionServiceClass() {
    return GETREGISTERCLASS(jPositionServiceClass, "com/gluonhq/helloandroid/DalvikPositionService");
}

jclass substrateGetPushNotificationsServiceClass() {
    return GETREGISTERCLASS(jPushNotificationsServiceClass, "com/gluonhq/helloandroid/DalvikPushNotificationsService");
}

jclass substrateGetRuntimeArgsServiceClass() {
    return GETREGISTERCLASS(jRuntimeArgsServiceClass, "com/gluonhq/helloandroid/DalvikRuntimeArgsService");
}

jclass substrateGetSettingsServiceClass() {
    return GETREGISTERCLASS(jSettingsServiceClass, "com/gluonhq/helloandroid/DalvikSettingsService");
}

jclass substrateGetShareServiceClass() {
    return GETREGISTERCLASS(jShareServiceClass, "com/gluonhq/helloandroid/DalvikShareService");
}

jclass substrateGetStatusBarServiceClass() {
    return GETREGISTERCLASS(jStatusBarServiceClass, "com/gluonhq/helloandroid/DalvikStatusBarService");
}

jclass substrateGetStorageServiceClass() {
    return GETREGISTERCLASS(jStorageServiceClass, "com/gluonhq/helloandroid/DalvikStorageService");
}

jclass substrateGetVibrationServiceClass() {
    return GETREGISTERCLASS(jVibrationServiceClass, "com/gluonhq/helloandroid/DalvikVibrationService");
}

jclass substrateGetVideoServiceClass() {
    return GETREGISTERCLASS(jVideoServiceClass, "com/gluonhq/helloandroid/DalvikVideoService");
}

JNIEXPORT jint JNICALL
JNI_OnLoad_util(JavaVM *vm, void *reserved)
{
#ifdef JNI_VERSION_1_8
    graalVM = vm;
    JNIEnv* graalEnv;
    //min. returned JNI_VERSION required by JDK8 for builtin libraries
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native Util from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("Initializing native Util from OnLoad");
    initializeUtilDalvikHandles();
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

JavaVM* getGraalVM() {
    return graalVM;
}

// from Java to Android

JNIEXPORT void JNICALL Java_com_gluonhq_attach_util_impl_Debug_enableDebug
(JNIEnv *env, jclass jClass)
{
    ATTACH_LOG_FINE("Enabling debug for all Attach services");
    debugAttach = JNI_TRUE;
    ATTACH_DALVIK();
    (*dalvikEnv)->CallStaticVoidMethod(dalvikEnv, jUtilClass, jUtilEnableDebugMethod);
    DETACH_DALVIK();
}

//////////////////////////////////
// native (Substrate) to Dalvik //
//////////////////////////////////

void attach_setActivityResult(jint requestCode, jint resultCode, jobject intent)
{
    if (!initialized) {
        initializeUtilDalvikHandles();
    }
    ATTACH_LOG_FINE("call Attach::nativeDispatchActivityResult method from native: %d %d", requestCode, resultCode);
    ATTACH_DALVIK();
    (*dalvikEnv)->CallStaticVoidMethod(dalvikEnv, jUtilClass, jUtilOnActivityResultMethod, requestCode, resultCode, intent);
    DETACH_DALVIK();
    ATTACH_LOG_FINE("call Attach::nativeDispatchActivityResult method from native done");
}

void attach_setLifecycleEvent(const char* event) {
    if (!initialized) {
        initializeUtilDalvikHandles();
    }
    ATTACH_LOG_FINE("Call Attach method from native Lifecycle: %s", event);
    ATTACH_DALVIK();
    jstring jchars = (*dalvikEnv)->NewStringUTF(dalvikEnv, event);
    (*dalvikEnv)->CallStaticVoidMethod(dalvikEnv, jUtilClass, jUtilOnLifecycleEventMethod, jchars);
    (*dalvikEnv)->DeleteLocalRef(dalvikEnv, jchars);
    DETACH_DALVIK();
    ATTACH_LOG_FINE("called Attach method from native Lifecycle done");
}

////////////////////////////
// From Dalvik to Dalvik  //
////////////////////////////

JNIEXPORT jboolean JNICALL Java_com_gluonhq_helloandroid_Util_nativeVerifyPermissions(JNIEnv *env, jobject activity, jobjectArray jpermissionsArray)
{
    if (!initialized) {
        initializeUtilDalvikHandles();
    }
    ATTACH_LOG_FINE("Calling Verify Permissions from Attach::Util");
    jboolean jresult = (*env)->CallStaticBooleanMethod(env, jPermissionActivityClass, jUtilRequestPermissionsMethod, substrateGetActivity(), jpermissionsArray);
    ATTACH_LOG_FINE("Verify Permissions from native Attach::Util done");
    return jresult;
}
