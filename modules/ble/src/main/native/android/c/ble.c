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
#include "ble.h"

static jclass jGraalBleClass;
static jmethodID jGraalSetDetectionMethod;
static jmethodID jGraalSetDeviceDetectionMethod;
static jmethodID jGraalSetDeviceStateMethod;
static jmethodID jGraalSetDeviceProfileMethod;
static jmethodID jGraalSetDeviceCharMethod;
static jmethodID jGraalSetDeviceDescMethod;
static jmethodID jGraalSetDeviceValueMethod;

static jobject jDalvikBleService;
static jmethodID jBleServiceStartScanningMethod;
static jmethodID jBleServiceStopScanningMethod;
static jmethodID jBleServiceStartBroadcastMethod;
static jmethodID jBleServiceStopBroadcastMethod;

static jmethodID jBleServiceStartScanningPeripheralsMethod;
static jmethodID jBleServiceStopScanningPeripheralsMethod;
static jmethodID jBleServiceConnectMethod;
static jmethodID jBleServiceDisconnectMethod;
static jmethodID jBleServiceReadMethod;
static jmethodID jBleServiceWriteMethod;
static jmethodID jBleServiceSubscribeMethod;

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

void initializeBleDalvikHandles() {
    jclass jBleServiceClass = substrateGetBleServiceClass();
    ATTACH_DALVIK();
    jmethodID jBleServiceInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jBleServiceClass, "<init>", "(Landroid/app/Activity;)V");
    jBleServiceStartScanningMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jBleServiceClass, "startScanning", "()V");
    jBleServiceStopScanningMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jBleServiceClass, "stopScanning", "()V");
    jBleServiceStartBroadcastMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jBleServiceClass, "startBroadcast", "(Ljava/lang/String;IILjava/lang/String;)V");
    jBleServiceStopBroadcastMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jBleServiceClass, "stopBroadcast", "()V");

    jBleServiceStartScanningPeripheralsMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jBleServiceClass, "startScanningPeripherals", "()V");
    jBleServiceStopScanningPeripheralsMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jBleServiceClass, "stopScanningPeripherals", "()V");
    jBleServiceConnectMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jBleServiceClass, "connect", "(Ljava/lang/String;Ljava/lang/String;)V");
    jBleServiceDisconnectMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jBleServiceClass, "disconnect", "(Ljava/lang/String;Ljava/lang/String;)V");
    jBleServiceReadMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jBleServiceClass, "read", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
    jBleServiceWriteMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jBleServiceClass, "write", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;[B)V");
    jBleServiceSubscribeMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jBleServiceClass, "subscribe", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V");

    jobject jActivity = substrateGetActivity();
    jobject jtmpobj = (*dalvikEnv)->NewObject(dalvikEnv, jBleServiceClass, jBleServiceInitMethod, jActivity);
    jDalvikBleService = (*dalvikEnv)->NewGlobalRef(dalvikEnv, jtmpobj);
    DETACH_DALVIK();
}

//////////////////////////
// From Graal to native //
//////////////////////////


JNIEXPORT jint JNICALL
JNI_OnLoad_ble(JavaVM *vm, void *reserved)
{
ATTACH_LOG_INFO("JNI_OnLoad_ble called");
#ifdef JNI_VERSION_1_8
    JNIEnv* graalEnv;
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native Ble from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("[BLESERVICE] Initializing native BLE from OnLoad");
    initializeGraalHandles(graalEnv);
    initializeBleDalvikHandles();
    ATTACH_LOG_FINE("Initializing native Ble done");
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

// from Java to Android

// BLE BEACONS

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_AndroidBleService_startObserver
(JNIEnv *env, jclass jClass, jobjectArray jUuidsArray)
{
    int uuidCount = (*env)->GetArrayLength(env, jUuidsArray);
    fprintf(stderr, "ble.c startObserver\n");
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikBleService, jBleServiceStartScanningMethod);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_AndroidBleService_stopObserver
(JNIEnv *env, jclass jClass)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikBleService, jBleServiceStopScanningMethod);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_AndroidBleService_startBroadcast
(JNIEnv *env, jclass jClass, jstring jUuid, jint major, jint minor, jstring jId) {
    const char *uuidChars = (*env)->GetStringUTFChars(env, jUuid, NULL);
    const char *idChars = (*env)->GetStringUTFChars(env, jId, NULL);
    ATTACH_DALVIK();
    jstring duuid = (*dalvikEnv)->NewStringUTF(dalvikEnv, uuidChars);
    jstring did = (*dalvikEnv)->NewStringUTF(dalvikEnv, idChars);
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikBleService, jBleServiceStartBroadcastMethod,
                   duuid, major, minor, did);
    DETACH_DALVIK();
    // (*env)->ReleaseStringUTFChars(env, jUuid, uuidChars);
    // (*env)->ReleaseStringUTFChars(env, jId, idChars);
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_AndroidBleService_stopBroadcast
(JNIEnv *env, jclass jClass) {
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikBleService, jBleServiceStopBroadcastMethod);
    DETACH_DALVIK();
}

// BLE DEVICES

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_AndroidBleService_startScanningPeripherals
(JNIEnv *env, jclass jClass)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikBleService, jBleServiceStartScanningPeripheralsMethod);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_AndroidBleService_stopScanningPeripherals
(JNIEnv *env, jclass jClass)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikBleService, jBleServiceStopScanningPeripheralsMethod);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_AndroidBleService_doConnect
(JNIEnv *env, jclass jClass, jstring jName, jstring jAddress)
{
    const char *nameChars = (*env)->GetStringUTFChars(env, jName, NULL);
    const char *addressChars = (*env)->GetStringUTFChars(env, jAddress, NULL);
    ATTACH_DALVIK();
    jstring dname = (*dalvikEnv)->NewStringUTF(dalvikEnv, nameChars);
    jstring daddress = (*dalvikEnv)->NewStringUTF(dalvikEnv, addressChars);
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikBleService, jBleServiceConnectMethod,
                   dname, daddress);
    DETACH_DALVIK();
    // (*env)->ReleaseStringUTFChars(env, jName, nameChars);
    // (*env)->ReleaseStringUTFChars(env, jAddress, addressChars);
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_AndroidBleService_doDisconnect
(JNIEnv *env, jclass jClass, jstring jName, jstring jAddress)
{
    const char *nameChars = (*env)->GetStringUTFChars(env, jName, NULL);
    const char *addressChars = (*env)->GetStringUTFChars(env, jAddress, NULL);
    ATTACH_DALVIK();
    jstring dname = (*dalvikEnv)->NewStringUTF(dalvikEnv, nameChars);
    jstring daddress = (*dalvikEnv)->NewStringUTF(dalvikEnv, addressChars);
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikBleService, jBleServiceDisconnectMethod,
                   dname, daddress);
    DETACH_DALVIK();
    // (*env)->ReleaseStringUTFChars(env, jName, nameChars);
    // (*env)->ReleaseStringUTFChars(env, jAddress, addressChars);
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_AndroidBleService_doRead
(JNIEnv *env, jclass jClass, jstring jAddress, jstring jProfile, jstring jCharacteristic)
{
    const char *addressChars = (*env)->GetStringUTFChars(env, jAddress, NULL);
    const char *profileChars = (*env)->GetStringUTFChars(env, jProfile, NULL);
    const char *characteristicChars = (*env)->GetStringUTFChars(env, jCharacteristic, NULL);
    ATTACH_DALVIK();
    jstring daddress = (*dalvikEnv)->NewStringUTF(dalvikEnv, addressChars);
    jstring dprofile = (*dalvikEnv)->NewStringUTF(dalvikEnv, profileChars);
    jstring dcharacteristic = (*dalvikEnv)->NewStringUTF(dalvikEnv, characteristicChars);
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikBleService, jBleServiceReadMethod,
                   daddress, dprofile, dcharacteristic);
    DETACH_DALVIK();
    // (*env)->ReleaseStringUTFChars(env, jAddress, addressChars);
    // (*env)->ReleaseStringUTFChars(env, jProfile, profileChars);
    // (*env)->ReleaseStringUTFChars(env, jCharacteristic, characteristicChars);
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_AndroidBleService_doWrite
(JNIEnv *env, jclass jClass, jstring jAddress, jstring jProfile, jstring jCharacteristic, jbyteArray value)
{
    const char *addressChars = (*env)->GetStringUTFChars(env, jAddress, NULL);
    const char *profileChars = (*env)->GetStringUTFChars(env, jProfile, NULL);
    const char *characteristicChars = (*env)->GetStringUTFChars(env, jCharacteristic, NULL);
    jbyte *valueBytes = (*env)->GetByteArrayElements(env, value, NULL);
    int size = (*env)->GetArrayLength(env, value);

    ATTACH_DALVIK();
    jstring daddress = (*dalvikEnv)->NewStringUTF(dalvikEnv, addressChars);
    jstring dprofile = (*dalvikEnv)->NewStringUTF(dalvikEnv, profileChars);
    jstring dcharacteristic = (*dalvikEnv)->NewStringUTF(dalvikEnv, characteristicChars);
    jbyteArray jvalue = (*dalvikEnv)->NewByteArray(dalvikEnv, size);
    (*dalvikEnv)->SetByteArrayRegion(dalvikEnv, jvalue, 0, size, valueBytes);

    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikBleService, jBleServiceWriteMethod,
                   daddress, dprofile, dcharacteristic, jvalue);
    DETACH_DALVIK();
    // (*env)->ReleaseStringUTFChars(env, jAddress, addressChars);
    // (*env)->ReleaseStringUTFChars(env, jProfile, profileChars);
    // (*env)->ReleaseStringUTFChars(env, jCharacteristic, characteristicChars);
    // (*env)->ReleaseByteArrayElements(env, value, valueBytes, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_AndroidBleService_doSubscribe
(JNIEnv *env, jclass jClass, jstring jAddress, jstring jProfile, jstring jCharacteristic, jboolean value)
{
    const char *addressChars = (*env)->GetStringUTFChars(env, jAddress, NULL);
    const char *profileChars = (*env)->GetStringUTFChars(env, jProfile, NULL);
    const char *characteristicChars = (*env)->GetStringUTFChars(env, jCharacteristic, NULL);

    ATTACH_DALVIK();
    jstring daddress = (*dalvikEnv)->NewStringUTF(dalvikEnv, addressChars);
    jstring dprofile = (*dalvikEnv)->NewStringUTF(dalvikEnv, profileChars);
    jstring dcharacteristic = (*dalvikEnv)->NewStringUTF(dalvikEnv, characteristicChars);

    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikBleService, jBleServiceSubscribeMethod,
                   daddress, dprofile, dcharacteristic, value);
    DETACH_DALVIK();
    // (*env)->ReleaseStringUTFChars(env, jAddress, addressChars);
    // (*env)->ReleaseStringUTFChars(env, jProfile, profileChars);
    // (*env)->ReleaseStringUTFChars(env, jCharacteristic, characteristicChars);
}

///////////////////////////
// From Dalvik to native //
///////////////////////////

// BLE BEACONS

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_DalvikBleService_scanDetected(JNIEnv *env, jobject service, jstring uuid, jint major, jint minor, jint ris, jint proxy) {
    if (debugAttach) {
        ATTACH_LOG_FINE("Scan Detection is now in native layer, major = %d\n", major);
    }
    const char *uuidChars = (*env)->GetStringUTFChars(env, uuid, NULL);
    ATTACH_GRAAL();
    jstring juuid = (*graalEnv)->NewStringUTF(graalEnv, uuidChars);
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalBleClass, jGraalSetDetectionMethod, 
                 juuid, major, minor, ris, proxy);
    DETACH_GRAAL();
    // (*env)->ReleaseStringUTFChars(env, uuid, uuidChars);
}

// BLE DEVICES

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_DalvikBleService_scanDeviceDetected(JNIEnv *env, jobject service,
        jstring name, jstring address) {
    const char *nameChars = (*env)->GetStringUTFChars(env, name, NULL);
    const char *addressChars = (*env)->GetStringUTFChars(env, address, NULL);
    if (debugAttach) {
        ATTACH_LOG_FINE("Scan Device Detection, name = %s, address = %s\n", nameChars, addressChars);
    }

    ATTACH_GRAAL();
    jstring jname = (*graalEnv)->NewStringUTF(graalEnv, nameChars);
    jstring jaddress = (*graalEnv)->NewStringUTF(graalEnv, addressChars);
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalBleClass, jGraalSetDeviceDetectionMethod,
                 jname, jaddress);
    DETACH_GRAAL();
    // (*env)->ReleaseStringUTFChars(env, name, nameChars);
    // (*env)->ReleaseStringUTFChars(env, address, addressChars);
}

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_BleGattCallback_setState(JNIEnv *env, jobject service,
        jstring name, jstring state) {
    const char *nameChars = (*env)->GetStringUTFChars(env, name, NULL);
    const char *stateChars = (*env)->GetStringUTFChars(env, state, NULL);
    if (debugAttach) {
        ATTACH_LOG_FINE("Device state, name = %s, state = %s\n", nameChars, stateChars);
    }
    ATTACH_GRAAL();
    jstring jname = (*graalEnv)->NewStringUTF(graalEnv, nameChars);
    jstring jstate = (*graalEnv)->NewStringUTF(graalEnv, stateChars);
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalBleClass, jGraalSetDeviceStateMethod, jname, jstate);
    DETACH_GRAAL();
    // (*env)->ReleaseStringUTFChars(env, name, nameChars);
    // (*env)->ReleaseStringUTFChars(env, state, stateChars);
}

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_BleGattCallback_addProfile(JNIEnv *env, jobject service,
        jstring name, jstring uuid, jstring type) {
    const char *nameChars = (*env)->GetStringUTFChars(env, name, NULL);
    const char *uuidChars = (*env)->GetStringUTFChars(env, uuid, NULL);
    const char *typeChars = (*env)->GetStringUTFChars(env, type, NULL);
    if (debugAttach) {
        ATTACH_LOG_FINE("Device type, name = %s, service: uuid = %s, type = %s\n", nameChars, uuidChars, typeChars);
    }
    ATTACH_GRAAL();
    jstring jname = (*graalEnv)->NewStringUTF(graalEnv, nameChars);
    jstring juuid = (*graalEnv)->NewStringUTF(graalEnv, uuidChars);
    jstring jtype = (*graalEnv)->NewStringUTF(graalEnv, typeChars);
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalBleClass, jGraalSetDeviceProfileMethod, jname, juuid, jtype);
    DETACH_GRAAL();
    // (*env)->ReleaseStringUTFChars(env, name, nameChars);
    // (*env)->ReleaseStringUTFChars(env, uuid, uuidChars);
    // (*env)->ReleaseStringUTFChars(env, type, typeChars);
}

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_BleGattCallback_addCharacteristic(JNIEnv *env, jobject service,
        jstring name, jstring profileUuid, jstring charUuid, jstring properties) {
    const char *nameChars = (*env)->GetStringUTFChars(env, name, NULL);
    const char *profileUuidChars = (*env)->GetStringUTFChars(env, profileUuid, NULL);
    const char *charUuidChars = (*env)->GetStringUTFChars(env, charUuid, NULL);
    const char *propertiesChars = (*env)->GetStringUTFChars(env, properties, NULL);
    if (debugAttach) {
        ATTACH_LOG_FINE("Device name = %s, service: profileUuid = %s, charUuid = %s, properties = %s\n",
            nameChars, profileUuidChars, charUuidChars, propertiesChars);
    }

    ATTACH_GRAAL();
    jstring jname = (*graalEnv)->NewStringUTF(graalEnv, nameChars);
    jstring jprofileUuid = (*graalEnv)->NewStringUTF(graalEnv, profileUuidChars);
    jstring jcharUuid = (*graalEnv)->NewStringUTF(graalEnv, charUuidChars);
    jstring jproperties = (*graalEnv)->NewStringUTF(graalEnv, propertiesChars);
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalBleClass, jGraalSetDeviceCharMethod, jname, jprofileUuid, jcharUuid, jproperties);
    DETACH_GRAAL();
    // (*env)->ReleaseStringUTFChars(env, name, nameChars);
    // (*env)->ReleaseStringUTFChars(env, profileUuid, profileUuidChars);
    // (*env)->ReleaseStringUTFChars(env, charUuid, charUuidChars);
    // (*env)->ReleaseStringUTFChars(env, properties, profileUuidChars);
}

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_BleGattCallback_addDescriptor(JNIEnv *env, jobject service,
        jstring name, jstring profileUuid, jstring charUuid, jstring descUuid, jbyteArray value) {
    const char *nameChars = (*env)->GetStringUTFChars(env, name, NULL);
    const char *profileUuidChars = (*env)->GetStringUTFChars(env, profileUuid, NULL);
    const char *charUuidChars = (*env)->GetStringUTFChars(env, charUuid, NULL);
    const char *descUuidChars = (*env)->GetStringUTFChars(env, descUuid, NULL);
    jbyte *valueBytes = (*env)->GetByteArrayElements(env, value, NULL);
    int size = (*env)->GetArrayLength(env, value);
    if (debugAttach) {
        ATTACH_LOG_FINE("Device name = %s, service: profileUuid = %s, charUuid = %s, descUuid = %s\n",
            nameChars, profileUuidChars, charUuidChars, descUuidChars);
    }

    ATTACH_GRAAL();
    jstring jname = (*graalEnv)->NewStringUTF(graalEnv, nameChars);
    jstring jprofileUuid = (*graalEnv)->NewStringUTF(graalEnv, profileUuidChars);
    jstring jcharUuid = (*graalEnv)->NewStringUTF(graalEnv, charUuidChars);
    jstring jdescUuid = (*graalEnv)->NewStringUTF(graalEnv, descUuidChars);
    jbyteArray jvalue = (*graalEnv)->NewByteArray(graalEnv, size);
    (*graalEnv)->SetByteArrayRegion(graalEnv, jvalue, 0, size, valueBytes);

    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalBleClass, jGraalSetDeviceDescMethod, jname, jprofileUuid, jcharUuid, jdescUuid, jvalue);
    // (*graalEnv)->ReleaseByteArrayElements(graalEnv, value, valueBytes, JNI_ABORT);
    DETACH_GRAAL();
    // (*env)->ReleaseStringUTFChars(env, name, nameChars);
    // (*env)->ReleaseStringUTFChars(env, profileUuid, profileUuidChars);
    // (*env)->ReleaseStringUTFChars(env, charUuid, charUuidChars);
    // (*env)->ReleaseStringUTFChars(env, descUuid, descUuidChars);
}

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_BleGattCallback_setValue(JNIEnv *env, jobject service,
        jstring name, jstring charUuid, jbyteArray value) {
    const char *nameChars = (*env)->GetStringUTFChars(env, name, NULL);
    const char *charUuidChars = (*env)->GetStringUTFChars(env, charUuid, NULL);

    jbyte *valueBytes = (*env)->GetByteArrayElements(env, value, NULL);
    int size = (*env)->GetArrayLength(env, value);

    ATTACH_GRAAL();
    jstring jname = (*graalEnv)->NewStringUTF(graalEnv, nameChars);
    jstring jcharUuid = (*graalEnv)->NewStringUTF(graalEnv, charUuidChars);
    jbyteArray jvalue = (*graalEnv)->NewByteArray(graalEnv, size);
    (*graalEnv)->SetByteArrayRegion(graalEnv, jvalue, 0, size, valueBytes);

    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalBleClass, jGraalSetDeviceValueMethod, jname, jcharUuid, jvalue);
    // (*graalEnv)->ReleaseByteArrayElements(graalEnv, value, valueBytes, JNI_ABORT);
    DETACH_GRAAL();
    // (*env)->ReleaseStringUTFChars(env, name, nameChars);
    // (*env)->ReleaseStringUTFChars(env, charUuid, charUuidChars);
}
