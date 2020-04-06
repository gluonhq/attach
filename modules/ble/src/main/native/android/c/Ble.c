/*
 * Copyright (c) 2020 Gluon
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
#include "Ble.h"

static JNIEnv *graalEnv;
JNIEnv* javaEnvBle = NULL;
JavaVM *jVMBle = NULL;
static jclass jGraalBleClass;
static jmethodID jGraalSetDetectionMethod;

static JavaVM *myAndroidVM = NULL;
static jobject jDalvikBleService;
static jmethodID jBleServiceStartScanningMethod;
static jmethodID jBleServiceStopScanningMethod;
static jmethodID jBleServiceStartBroadcastMethod;
static jmethodID jBleServiceStopBroadcastMethod;
static jmethodID attach_setEvent;

void initializeGraalHandles(JNIEnv *graalEnv) {
    jGraalBleClass = (*graalEnv)->NewGlobalRef(graalEnv, (*graalEnv)->FindClass(graalEnv, "com/gluonhq/attach/ble/impl/AndroidBleService"));
    jGraalSetDetectionMethod = (*graalEnv)->GetStaticMethodID(graalEnv, jGraalBleClass, "setDetection", "(Ljava/lang/String;IIII)V");
}

void initializeDalvikHandles() {
    myAndroidVM = substrateGetAndroidVM();
    jclass jBleServiceClass = substrateGetBleServiceClass();
    JNIEnv* androidEnv;
    (*myAndroidVM)->AttachCurrentThread(myAndroidVM, (void **)&androidEnv, NULL);
    jmethodID jBleServiceInitMethod = (*androidEnv)->GetMethodID(androidEnv, jBleServiceClass, "<init>", "(Landroid/app/Activity;)V");
    jBleServiceStartScanningMethod = (*androidEnv)->GetMethodID(androidEnv, jBleServiceClass, "startScanning", "()V");
    jBleServiceStopScanningMethod = (*androidEnv)->GetMethodID(androidEnv, jBleServiceClass, "stopScanning", "()V");
    jBleServiceStartBroadcastMethod = (*androidEnv)->GetMethodID(androidEnv, jBleServiceClass, "startBroadcast", "(Ljava/lang/String;IILjava/lang/String;)V");
    jBleServiceStopBroadcastMethod = (*androidEnv)->GetMethodID(androidEnv, jBleServiceClass, "stopBroadcast", "()V");

    jobject jActivity = substrateGetActivity();
    jobject jtmpobj = (*androidEnv)->NewObject(androidEnv, jBleServiceClass, jBleServiceInitMethod, jActivity);
    jDalvikBleService = (*androidEnv)->NewGlobalRef(androidEnv, jtmpobj);
    (*myAndroidVM)->DetachCurrentThread(myAndroidVM);
}

//////////////////////////
// From Graal to native //
//////////////////////////


JNIEXPORT jint JNICALL
JNI_OnLoad_Ble(JavaVM *vm, void *reserved)
{
fprintf(stderr, "JNI_OnLoad_BLE called\n");
#ifdef JNI_VERSION_1_8
    jVMBle = vm;
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native Ble from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("[BLESERVICE] Initializing native BLE from OnLoad");
    initializeGraalHandles(graalEnv);
    initializeDalvikHandles();
    ATTACH_LOG_FINE("Initializing native Ble done");
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

// from Java to Android

JNIEnv* getSafeAndroidEnv() {
    JNIEnv* androidEnv;
    if ((*myAndroidVM)->GetEnv(myAndroidVM, (void **)&androidEnv, JNI_VERSION_1_6) != JNI_OK) {
        ATTACH_LOG_WARNING("enableDalvikDebug called from not-attached thread\n");
        (*myAndroidVM)->AttachCurrentThread(myAndroidVM, (void **)&androidEnv, NULL);
    }  else {
        ATTACH_LOG_FINE("enableDalvikDebug called from attached thread %p\n", androidEnv);
    }
    return androidEnv;
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_AndroidBleService_startScanningPeripherals
(JNIEnv *env, jclass jClass)
{
    jclass activityClass = substrateGetActivityClass();
    jobject jActivity = substrateGetActivity();
    jclass jBleServiceClass = substrateGetBleServiceClass();

    JNIEnv* androidEnv = getSafeAndroidEnv();
    jmethodID jBleServiceInitMethod = (*androidEnv)->GetMethodID(androidEnv, jBleServiceClass, "<init>", "(Landroid/app/Activity;)V");
    jDalvikBleService = (*androidEnv)->NewObject(androidEnv, jBleServiceClass, jBleServiceInitMethod, jActivity);
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_AndroidBleService_startObserver
(JNIEnv *env, jclass jClass, jobjectArray jUuidsArray)
{
    int uuidCount = (*env)->GetArrayLength(env, jUuidsArray);
    fprintf(stderr, "ble.c startObserver\n");
    JNIEnv* androidEnv = getSafeAndroidEnv();
    (*androidEnv)->CallVoidMethod(androidEnv, jDalvikBleService, jBleServiceStartScanningMethod);
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_AndroidBleService_stopObserver
(JNIEnv *env, jclass jClass)
{
    fprintf(stderr, "ble.c stopObserver\n");
    JNIEnv* androidEnv = getSafeAndroidEnv();
    (*androidEnv)->CallVoidMethod(androidEnv, jDalvikBleService, jBleServiceStopScanningMethod);
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_AndroidBleService_startBroadcast
(JNIEnv *env, jclass jClass, jstring jUuid, jint major, jint minor, jstring jId) {
    const char *uuidChars = (*env)->GetStringUTFChars(env, jUuid, NULL);
    const char *idChars = (*env)->GetStringUTFChars(env, jId, NULL);
    JNIEnv* androidEnv = getSafeAndroidEnv();
    jstring duuid = (*androidEnv)->NewStringUTF(androidEnv, uuidChars);
    jstring did = (*androidEnv)->NewStringUTF(androidEnv, idChars);
    (*androidEnv)->CallVoidMethod(androidEnv, jDalvikBleService, jBleServiceStartBroadcastMethod,
                   duuid, major, minor, did);
    (*androidEnv)->DeleteLocalRef(androidEnv, duuid);
    (*androidEnv)->DeleteLocalRef(androidEnv, did);
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_AndroidBleService_stopBroadcast
(JNIEnv *env, jclass jClass) {
    JNIEnv* androidEnv = getSafeAndroidEnv();
    (*androidEnv)->CallVoidMethod(androidEnv, jDalvikBleService, jBleServiceStopBroadcastMethod);
}

///////////////////////////
// From Dalvik to native //
///////////////////////////

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_BleService_scanDetected(JNIEnv *env, jobject service, jstring uuid, jint major, jint minor, jint ris, jint proxy) {
    ATTACH_LOG_FINE("Scan Detection is now in native layer, major = %d\n", major);
    const char *uuidChars = (*env)->GetStringUTFChars(env, uuid, NULL);
    (*jVMBle)->AttachCurrentThread(jVMBle, (void **)&graalEnv, NULL);
    jstring juuid = (*graalEnv)->NewStringUTF(graalEnv, uuidChars);
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalBleClass, jGraalSetDetectionMethod, 
                 juuid, major, minor, ris, proxy);
    (*graalEnv)->DeleteLocalRef(graalEnv, juuid);
}

// from Android to Java

void initializeBleFromNative() {
    if (javaEnvBle != NULL) {
        return; // already have a JNIEnv
    }
    if (jVMBle == NULL) {
        ATTACH_LOG_FINE("initialize Ble from native can't be done without JVM");
        return; // can't initialize from native before we have a jVMBle
    }
    ATTACH_LOG_FINE("Initializing native Ble from Android/native code");
    jint error = (*jVMBle)->AttachCurrentThread(jVMBle, (void **)&javaEnvBle, NULL);
    if (error != 0) {
        ATTACH_LOG_FINE("initializeBleFromNative failed with error %d", error);
    }
}

void attach_setBleEvent(const char* event) {
    initializeBleFromNative();
    if (javaEnvBle == NULL) {
        ATTACH_LOG_FINE("javaEnvBle still null, not ready to process Ble events");
        return;
    }
    ATTACH_LOG_FINE("call Attach method from native Ble: %s", event);
}

