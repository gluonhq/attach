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
static jmethodID jGraalSetDeviceStateMethod;
static jmethodID jGraalSetDeviceProfileMethod;
static jmethodID jGraalSetDeviceCharMethod;
static jmethodID jGraalSetDeviceDescMethod;
static jmethodID jGraalSetDeviceValueMethod;

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
static jmethodID jBleServiceReadMethod;
static jmethodID jBleServiceWriteMethod;
static jmethodID jBleServiceSubscribeMethod;

static jboolean debugBLE;

void initializeGraalHandles(JNIEnv *graalEnv) {
    jGraalBleClass = (*graalEnv)->NewGlobalRef(graalEnv, (*graalEnv)->FindClass(graalEnv, "com/gluonhq/attach/ble/impl/AndroidBleService"));
    jGraalSetDetectionMethod = (*graalEnv)->GetStaticMethodID(graalEnv, jGraalBleClass, "setDetection", "(Ljava/lang/String;IIII)V");
    jGraalSetDeviceDetectionMethod = (*graalEnv)->GetStaticMethodID(graalEnv, jGraalBleClass, "gotPeripheral", "(Ljava/lang/String;Ljava/lang/String;)V");
    jGraalSetDeviceStateMethod = (*graalEnv)->GetStaticMethodID(graalEnv, jGraalBleClass, "gotState", "(Ljava/lang/String;Ljava/lang/String;)V");
    jGraalSetDeviceProfileMethod = (*graalEnv)->GetStaticMethodID(graalEnv, jGraalBleClass, "gotProfile", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
    jGraalSetDeviceCharMethod = (*graalEnv)->GetStaticMethodID(graalEnv, jGraalBleClass, "gotCharacteristic", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
    jGraalSetDeviceDescMethod = (*graalEnv)->GetStaticMethodID(graalEnv, jGraalBleClass, "gotDescriptor", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[B)V");
    jGraalSetDeviceValueMethod = (*graalEnv)->GetStaticMethodID(graalEnv, jGraalBleClass, "gotValue", "(Ljava/lang/String;Ljava/lang/String;[B)V");
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
    jBleServiceReadMethod = (*androidEnv)->GetMethodID(androidEnv, jBleServiceClass, "read", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
    jBleServiceWriteMethod = (*androidEnv)->GetMethodID(androidEnv, jBleServiceClass, "write", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[B)V");
    jBleServiceSubscribeMethod = (*androidEnv)->GetMethodID(androidEnv, jBleServiceClass, "subscribe", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V");

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
    }
    return androidEnv;
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_AndroidBleService_enableDebug
(JNIEnv *env, jclass jClass) {
    JNIEnv* androidEnv = getSafeAndroidEnv();
    debugBLE = JNI_TRUE;
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
    (*env)->ReleaseStringUTFChars(env, jUuid, uuidChars);
    (*env)->ReleaseStringUTFChars(env, jId, idChars);
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
    (*env)->ReleaseStringUTFChars(env, jName, nameChars);
    (*env)->ReleaseStringUTFChars(env, jAddress, addressChars);
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
    (*env)->ReleaseStringUTFChars(env, jName, nameChars);
    (*env)->ReleaseStringUTFChars(env, jAddress, addressChars);
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_AndroidBleService_doRead
(JNIEnv *env, jclass jClass, jstring jAddress, jstring jProfile, jstring jCharacteristic)
{
    const char *addressChars = (*env)->GetStringUTFChars(env, jAddress, NULL);
    const char *profileChars = (*env)->GetStringUTFChars(env, jProfile, NULL);
    const char *characteristicChars = (*env)->GetStringUTFChars(env, jCharacteristic, NULL);
    JNIEnv* androidEnv = getSafeAndroidEnv();
    jstring daddress = (*androidEnv)->NewStringUTF(androidEnv, addressChars);
    jstring dprofile = (*androidEnv)->NewStringUTF(androidEnv, profileChars);
    jstring dcharacteristic = (*androidEnv)->NewStringUTF(androidEnv, characteristicChars);
    (*androidEnv)->CallVoidMethod(androidEnv, jDalvikBleService, jBleServiceReadMethod,
                   daddress, dprofile, dcharacteristic);
    (*androidEnv)->DeleteLocalRef(androidEnv, daddress);
    (*androidEnv)->DeleteLocalRef(androidEnv, dprofile);
    (*androidEnv)->DeleteLocalRef(androidEnv, dcharacteristic);
    (*env)->ReleaseStringUTFChars(env, jAddress, addressChars);
    (*env)->ReleaseStringUTFChars(env, jProfile, profileChars);
    (*env)->ReleaseStringUTFChars(env, jCharacteristic, characteristicChars);
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_AndroidBleService_doWrite
(JNIEnv *env, jclass jClass, jstring jAddress, jstring jProfile, jstring jCharacteristic, jbyteArray value)
{
    const char *addressChars = (*env)->GetStringUTFChars(env, jAddress, NULL);
    const char *profileChars = (*env)->GetStringUTFChars(env, jProfile, NULL);
    const char *characteristicChars = (*env)->GetStringUTFChars(env, jCharacteristic, NULL);
    jbyte *valueBytes = (*env)->GetByteArrayElements(env, value, NULL);
    int size = (*env)->GetArrayLength(env, value);

    JNIEnv* androidEnv = getSafeAndroidEnv();
    jstring daddress = (*androidEnv)->NewStringUTF(androidEnv, addressChars);
    jstring dprofile = (*androidEnv)->NewStringUTF(androidEnv, profileChars);
    jstring dcharacteristic = (*androidEnv)->NewStringUTF(androidEnv, characteristicChars);
    jbyteArray jvalue = (*androidEnv)->NewByteArray(androidEnv, size);
    (*androidEnv)->SetByteArrayRegion(androidEnv, jvalue, 0, size, valueBytes);

    (*androidEnv)->CallVoidMethod(androidEnv, jDalvikBleService, jBleServiceWriteMethod,
                   daddress, dprofile, dcharacteristic, jvalue);
    (*androidEnv)->DeleteLocalRef(androidEnv, daddress);
    (*androidEnv)->DeleteLocalRef(androidEnv, dprofile);
    (*androidEnv)->DeleteLocalRef(androidEnv, dcharacteristic);
    (*androidEnv)->DeleteLocalRef(androidEnv, jvalue);
    (*env)->ReleaseStringUTFChars(env, jAddress, addressChars);
    (*env)->ReleaseStringUTFChars(env, jProfile, profileChars);
    (*env)->ReleaseStringUTFChars(env, jCharacteristic, characteristicChars);
    (*env)->ReleaseByteArrayElements(env, value, valueBytes, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_AndroidBleService_doSubscribe
(JNIEnv *env, jclass jClass, jstring jAddress, jstring jProfile, jstring jCharacteristic, jboolean value)
{
    const char *addressChars = (*env)->GetStringUTFChars(env, jAddress, NULL);
    const char *profileChars = (*env)->GetStringUTFChars(env, jProfile, NULL);
    const char *characteristicChars = (*env)->GetStringUTFChars(env, jCharacteristic, NULL);

    JNIEnv* androidEnv = getSafeAndroidEnv();
    jstring daddress = (*androidEnv)->NewStringUTF(androidEnv, addressChars);
    jstring dprofile = (*androidEnv)->NewStringUTF(androidEnv, profileChars);
    jstring dcharacteristic = (*androidEnv)->NewStringUTF(androidEnv, characteristicChars);

    (*androidEnv)->CallVoidMethod(androidEnv, jDalvikBleService, jBleServiceSubscribeMethod,
                   daddress, dprofile, dcharacteristic, value);
    (*androidEnv)->DeleteLocalRef(androidEnv, daddress);
    (*androidEnv)->DeleteLocalRef(androidEnv, dprofile);
    (*androidEnv)->DeleteLocalRef(androidEnv, dcharacteristic);
    (*env)->ReleaseStringUTFChars(env, jAddress, addressChars);
    (*env)->ReleaseStringUTFChars(env, jProfile, profileChars);
    (*env)->ReleaseStringUTFChars(env, jCharacteristic, characteristicChars);
}

///////////////////////////
// From Dalvik to native //
///////////////////////////

// BLE BEACONS

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_DalvikBleService_scanDetected(JNIEnv *env, jobject service, jstring uuid, jint major, jint minor, jint ris, jint proxy) {
    if (debugBLE) {
        ATTACH_LOG_FINE("Scan Detection is now in native layer, major = %d\n", major);
    }
    const char *uuidChars = (*env)->GetStringUTFChars(env, uuid, NULL);
    (*jVMBle)->AttachCurrentThread(jVMBle, (void **)&graalEnv, NULL);
    jstring juuid = (*graalEnv)->NewStringUTF(graalEnv, uuidChars);
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalBleClass, jGraalSetDetectionMethod, 
                 juuid, major, minor, ris, proxy);
    (*graalEnv)->DeleteLocalRef(graalEnv, juuid);
    (*env)->ReleaseStringUTFChars(env, uuid, uuidChars);
}

// BLE DEVICES

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_DalvikBleService_scanDeviceDetected(JNIEnv *env, jobject service,
        jstring name, jstring address) {
    const char *nameChars = (*env)->GetStringUTFChars(env, name, NULL);
    const char *addressChars = (*env)->GetStringUTFChars(env, address, NULL);
    if (debugBLE) {
        ATTACH_LOG_FINE("Scan Device Detection, name = %s, address = %s\n", nameChars, addressChars);
    }

    (*jVMBle)->AttachCurrentThread(jVMBle, (void **)&graalEnv, NULL);
    jstring jname = (*graalEnv)->NewStringUTF(graalEnv, nameChars);
    jstring jaddress = (*graalEnv)->NewStringUTF(graalEnv, addressChars);
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalBleClass, jGraalSetDeviceDetectionMethod,
                 jname, jaddress);
    (*graalEnv)->DeleteLocalRef(graalEnv, jname);
    (*graalEnv)->DeleteLocalRef(graalEnv, jaddress);
    (*env)->ReleaseStringUTFChars(env, name, nameChars);
    (*env)->ReleaseStringUTFChars(env, address, addressChars);
}

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_BleGattCallback_setState(JNIEnv *env, jobject service,
        jstring name, jstring state) {
    const char *nameChars = (*env)->GetStringUTFChars(env, name, NULL);
    const char *stateChars = (*env)->GetStringUTFChars(env, state, NULL);
    if (debugBLE) {
        ATTACH_LOG_FINE("Device state, name = %s, state = %s\n", nameChars, stateChars);
    }

    (*jVMBle)->AttachCurrentThread(jVMBle, (void **)&graalEnv, NULL);
    jstring jname = (*graalEnv)->NewStringUTF(graalEnv, nameChars);
    jstring jstate = (*graalEnv)->NewStringUTF(graalEnv, stateChars);
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalBleClass, jGraalSetDeviceStateMethod, jname, jstate);
    (*graalEnv)->DeleteLocalRef(graalEnv, jname);
    (*graalEnv)->DeleteLocalRef(graalEnv, jstate);
    (*env)->ReleaseStringUTFChars(env, name, nameChars);
    (*env)->ReleaseStringUTFChars(env, state, stateChars);
}

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_BleGattCallback_addProfile(JNIEnv *env, jobject service,
        jstring name, jstring uuid, jstring type) {
    const char *nameChars = (*env)->GetStringUTFChars(env, name, NULL);
    const char *uuidChars = (*env)->GetStringUTFChars(env, uuid, NULL);
    const char *typeChars = (*env)->GetStringUTFChars(env, type, NULL);
    if (debugBLE) {
        ATTACH_LOG_FINE("Device type, name = %s, service: uuid = %s, type = %s\n", nameChars, uuidChars, typeChars);
    }

    (*jVMBle)->AttachCurrentThread(jVMBle, (void **)&graalEnv, NULL);
    jstring jname = (*graalEnv)->NewStringUTF(graalEnv, nameChars);
    jstring juuid = (*graalEnv)->NewStringUTF(graalEnv, uuidChars);
    jstring jtype = (*graalEnv)->NewStringUTF(graalEnv, typeChars);
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalBleClass, jGraalSetDeviceProfileMethod, jname, juuid, jtype);
    (*graalEnv)->DeleteLocalRef(graalEnv, jname);
    (*graalEnv)->DeleteLocalRef(graalEnv, juuid);
    (*graalEnv)->DeleteLocalRef(graalEnv, jtype);
    (*env)->ReleaseStringUTFChars(env, name, nameChars);
    (*env)->ReleaseStringUTFChars(env, uuid, uuidChars);
    (*env)->ReleaseStringUTFChars(env, type, typeChars);
}

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_BleGattCallback_addCharacteristic(JNIEnv *env, jobject service,
        jstring name, jstring profileUuid, jstring charUuid, jstring properties) {
    const char *nameChars = (*env)->GetStringUTFChars(env, name, NULL);
    const char *profileUuidChars = (*env)->GetStringUTFChars(env, profileUuid, NULL);
    const char *charUuidChars = (*env)->GetStringUTFChars(env, charUuid, NULL);
    const char *propertiesChars = (*env)->GetStringUTFChars(env, properties, NULL);
    if (debugBLE) {
        ATTACH_LOG_FINE("Device name = %s, service: profileUuid = %s, charUuid = %s, properties = %s\n",
            nameChars, profileUuidChars, charUuidChars, propertiesChars);
    }

    (*jVMBle)->AttachCurrentThread(jVMBle, (void **)&graalEnv, NULL);
    jstring jname = (*graalEnv)->NewStringUTF(graalEnv, nameChars);
    jstring jprofileUuid = (*graalEnv)->NewStringUTF(graalEnv, profileUuidChars);
    jstring jcharUuid = (*graalEnv)->NewStringUTF(graalEnv, charUuidChars);
    jstring jproperties = (*graalEnv)->NewStringUTF(graalEnv, propertiesChars);
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalBleClass, jGraalSetDeviceCharMethod, jname, jprofileUuid, jcharUuid, jproperties);
    (*graalEnv)->DeleteLocalRef(graalEnv, jname);
    (*graalEnv)->DeleteLocalRef(graalEnv, jprofileUuid);
    (*graalEnv)->DeleteLocalRef(graalEnv, jcharUuid);
    (*graalEnv)->DeleteLocalRef(graalEnv, jproperties);
    (*env)->ReleaseStringUTFChars(env, name, nameChars);
    (*env)->ReleaseStringUTFChars(env, profileUuid, profileUuidChars);
    (*env)->ReleaseStringUTFChars(env, charUuid, charUuidChars);
    (*env)->ReleaseStringUTFChars(env, properties, profileUuidChars);
}

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_BleGattCallback_addDescriptor(JNIEnv *env, jobject service,
        jstring name, jstring profileUuid, jstring charUuid, jstring descUuid, jbyteArray value) {
    const char *nameChars = (*env)->GetStringUTFChars(env, name, NULL);
    const char *profileUuidChars = (*env)->GetStringUTFChars(env, profileUuid, NULL);
    const char *charUuidChars = (*env)->GetStringUTFChars(env, charUuid, NULL);
    const char *descUuidChars = (*env)->GetStringUTFChars(env, descUuid, NULL);
    jbyte *valueBytes = (*env)->GetByteArrayElements(env, value, NULL);
    int size = (*env)->GetArrayLength(env, value);
    if (debugBLE) {
        ATTACH_LOG_FINE("Device name = %s, service: profileUuid = %s, charUuid = %s, descUuid = %s\n",
            nameChars, profileUuidChars, charUuidChars, descUuidChars);
    }

    (*jVMBle)->AttachCurrentThread(jVMBle, (void **)&graalEnv, NULL);
    jstring jname = (*graalEnv)->NewStringUTF(graalEnv, nameChars);
    jstring jprofileUuid = (*graalEnv)->NewStringUTF(graalEnv, profileUuidChars);
    jstring jcharUuid = (*graalEnv)->NewStringUTF(graalEnv, charUuidChars);
    jstring jdescUuid = (*graalEnv)->NewStringUTF(graalEnv, descUuidChars);
    jbyteArray jvalue = (*graalEnv)->NewByteArray(graalEnv, size);
    (*graalEnv)->SetByteArrayRegion(graalEnv, jvalue, 0, size, valueBytes);

    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalBleClass, jGraalSetDeviceDescMethod, jname, jprofileUuid, jcharUuid, jdescUuid, jvalue);
    (*graalEnv)->DeleteLocalRef(graalEnv, jname);
    (*graalEnv)->DeleteLocalRef(graalEnv, jprofileUuid);
    (*graalEnv)->DeleteLocalRef(graalEnv, jcharUuid);
    (*graalEnv)->DeleteLocalRef(graalEnv, jdescUuid);
    (*graalEnv)->ReleaseByteArrayElements(graalEnv, value, valueBytes, JNI_ABORT);
    (*env)->ReleaseStringUTFChars(env, name, nameChars);
    (*env)->ReleaseStringUTFChars(env, profileUuid, profileUuidChars);
    (*env)->ReleaseStringUTFChars(env, charUuid, charUuidChars);
    (*env)->ReleaseStringUTFChars(env, descUuid, descUuidChars);
}

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_BleGattCallback_setValue(JNIEnv *env, jobject service,
        jstring name, jstring charUuid, jbyteArray value) {
    const char *nameChars = (*env)->GetStringUTFChars(env, name, NULL);
    const char *charUuidChars = (*env)->GetStringUTFChars(env, charUuid, NULL);

    jbyte *valueBytes = (*env)->GetByteArrayElements(env, value, NULL);
    int size = (*env)->GetArrayLength(env, value);

    (*jVMBle)->AttachCurrentThread(jVMBle, (void **)&graalEnv, NULL);
    jstring jname = (*graalEnv)->NewStringUTF(graalEnv, nameChars);
    jstring jcharUuid = (*graalEnv)->NewStringUTF(graalEnv, charUuidChars);
    jbyteArray jvalue = (*graalEnv)->NewByteArray(graalEnv, size);
    (*graalEnv)->SetByteArrayRegion(graalEnv, jvalue, 0, size, valueBytes);

    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalBleClass, jGraalSetDeviceValueMethod, jname, jcharUuid, jvalue);
    (*graalEnv)->DeleteLocalRef(graalEnv, jname);
    (*graalEnv)->DeleteLocalRef(graalEnv, jcharUuid);
    (*graalEnv)->ReleaseByteArrayElements(graalEnv, value, valueBytes, JNI_ABORT);
    (*env)->ReleaseStringUTFChars(env, name, nameChars);
    (*env)->ReleaseStringUTFChars(env, charUuid, charUuidChars);
}
