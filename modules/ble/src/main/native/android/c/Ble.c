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
static jmethodID jGraalSetDeviceDetectionMethod;

static JavaVM *myAndroidVM = NULL;
static jobject jDalvikBleService;
static jmethodID jBleServiceStartScanningMethod;
static jmethodID jBleServiceStopScanningMethod;
static jmethodID jBleServiceStartBroadcastMethod;
static jmethodID jBleServiceStopBroadcastMethod;
static jmethodID jBleServiceEnableDebug;

static jmethodID jBleServiceStartScanningPeripheralsMethod;
static jmethodID jBleServiceStopScanningPeripheralsMethod;
static jmethodID jBleServiceConnectMethod;
static jmethodID jBleServiceDisconnectMethod;


void initializeGraalHandles(JNIEnv *graalEnv) {
    jGraalBleClass = (*graalEnv)->NewGlobalRef(graalEnv, (*graalEnv)->FindClass(graalEnv, "com/gluonhq/attach/ble/impl/AndroidBleService"));
    jGraalSetDetectionMethod = (*graalEnv)->GetStaticMethodID(graalEnv, jGraalBleClass, "setDetection", "(Ljava/lang/String;IIII)V");
    jGraalSetDeviceDetectionMethod = (*graalEnv)->GetStaticMethodID(graalEnv, jGraalBleClass, "gotPeripheral", "(Ljava/lang/String;Ljava/lang/String;)V");
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
    jBleServiceEnableDebug = (*androidEnv)->GetMethodID(androidEnv, jBleServiceClass, "enableDebug", "()V");

    jBleServiceStartScanningPeripheralsMethod = (*androidEnv)->GetMethodID(androidEnv, jBleServiceClass, "startScanningPeripherals", "()V");
    jBleServiceStopScanningPeripheralsMethod = (*androidEnv)->GetMethodID(androidEnv, jBleServiceClass, "stopScanningPeripherals", "()V");
    jBleServiceConnectMethod = (*androidEnv)->GetMethodID(androidEnv, jBleServiceClass, "connect", "(Ljava/lang/String;Ljava/lang/String;)V");
    jBleServiceDisconnectMethod = (*androidEnv)->GetMethodID(androidEnv, jBleServiceClass, "disconnect", "(Ljava/lang/String;Ljava/lang/String;)V");

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

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_AndroidBleService_enableDebug
(JNIEnv *env, jclass jClass) {
    JNIEnv* androidEnv = getSafeAndroidEnv();
    (*androidEnv)->CallVoidMethod(androidEnv, jDalvikBleService, jBleServiceEnableDebug);
}

// BLE BEACONS

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

// BLE DEVICES

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_AndroidBleService_startScanningPeripherals
(JNIEnv *env, jclass jClass)
{
    JNIEnv* androidEnv = getSafeAndroidEnv();
    (*androidEnv)->CallVoidMethod(androidEnv, jDalvikBleService, jBleServiceStartScanningPeripheralsMethod);
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_AndroidBleService_stopScanningPeripherals
(JNIEnv *env, jclass jClass)
{
    JNIEnv* androidEnv = getSafeAndroidEnv();
    (*androidEnv)->CallVoidMethod(androidEnv, jDalvikBleService, jBleServiceStopScanningPeripheralsMethod);
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_AndroidBleService_doConnect
(JNIEnv *env, jclass jClass, jstring jName, jstring jAddress)
{
    const char *nameChars = (*env)->GetStringUTFChars(env, jName, NULL);
    const char *addressChars = (*env)->GetStringUTFChars(env, jAddress, NULL);
    JNIEnv* androidEnv = getSafeAndroidEnv();
    jstring dname = (*androidEnv)->NewStringUTF(androidEnv, nameChars);
    jstring daddress = (*androidEnv)->NewStringUTF(androidEnv, addressChars);
    (*androidEnv)->CallVoidMethod(androidEnv, jDalvikBleService, jBleServiceConnectMethod,
                   dname, daddress);
    (*androidEnv)->DeleteLocalRef(androidEnv, dname);
    (*androidEnv)->DeleteLocalRef(androidEnv, daddress);
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_AndroidBleService_doDisconnect
(JNIEnv *env, jclass jClass, jstring jName, jstring jAddress)
{
    const char *nameChars = (*env)->GetStringUTFChars(env, jName, NULL);
    const char *addressChars = (*env)->GetStringUTFChars(env, jAddress, NULL);
    JNIEnv* androidEnv = getSafeAndroidEnv();
    jstring dname = (*androidEnv)->NewStringUTF(androidEnv, nameChars);
    jstring daddress = (*androidEnv)->NewStringUTF(androidEnv, addressChars);
    (*androidEnv)->CallVoidMethod(androidEnv, jDalvikBleService, jBleServiceDisconnectMethod,
                   dname, daddress);
    (*androidEnv)->DeleteLocalRef(androidEnv, dname);
    (*androidEnv)->DeleteLocalRef(androidEnv, daddress);
}

///////////////////////////
// From Dalvik to native //
///////////////////////////

// BLE BEACONS

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_DalvikBleService_scanDetected(JNIEnv *env, jobject service, jstring uuid, jint major, jint minor, jint ris, jint proxy) {
    ATTACH_LOG_FINE("Scan Detection is now in native layer, major = %d\n", major);
    const char *uuidChars = (*env)->GetStringUTFChars(env, uuid, NULL);
    (*jVMBle)->AttachCurrentThread(jVMBle, (void **)&graalEnv, NULL);
    jstring juuid = (*graalEnv)->NewStringUTF(graalEnv, uuidChars);
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalBleClass, jGraalSetDetectionMethod, 
                 juuid, major, minor, ris, proxy);
    (*graalEnv)->DeleteLocalRef(graalEnv, juuid);
}

// BLE DEVICES

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_DalvikBleService_scanDeviceDetected(JNIEnv *env, jobject service, jstring name, jstring address) {
    const char *nameChars = (*env)->GetStringUTFChars(env, name, NULL);
    jstring jname = (*graalEnv)->NewStringUTF(graalEnv, nameChars);
    const char *addressChars = (*env)->GetStringUTFChars(env, address, NULL);
    jstring jaddress = (*graalEnv)->NewStringUTF(graalEnv, addressChars);
    ATTACH_LOG_FINE("Scan Device Detection, name = %s, address = %s\n", nameChars, addressChars);
    (*jVMBle)->AttachCurrentThread(jVMBle, (void **)&graalEnv, NULL);
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalBleClass, jGraalSetDeviceDetectionMethod,
                 jname, jaddress);
    (*graalEnv)->DeleteLocalRef(graalEnv, jname);
    (*graalEnv)->DeleteLocalRef(graalEnv, jaddress);
}
