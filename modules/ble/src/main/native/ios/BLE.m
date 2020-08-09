/*
 * Copyright (c) 2016, 2020, Gluon
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

#include "BLE.h"

JNIEnv *env;

JNIEXPORT jint JNICALL
JNI_OnLoad_Ble(JavaVM *vm, void *reserved)
{
#ifdef JNI_VERSION_1_8
    //min. returned JNI_VERSION required by JDK8 for builtin libraries
    if ((*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_8) != JNI_OK) {
        return JNI_VERSION_1_4;
    }
    return JNI_VERSION_1_8;
#else
    return JNI_VERSION_1_4;
#endif
}

static int BleInited = 0;

// Ble
jclass mat_jBleServiceClass;
jmethodID mat_jBleService_setDetection = 0;

jmethodID mat_jBleService_gotPeripheral = 0;
jmethodID mat_jBleService_gotState = 0;
jmethodID mat_jBleService_gotProfile = 0;
jmethodID mat_jBleService_removeProfile = 0;
jmethodID mat_jBleService_gotCharacteristic = 0;
jmethodID mat_jBleService_gotValue = 0;
jmethodID mat_jBleService_gotDescriptor = 0;

Ble *_Ble;
NSArray *arrayOfUuids;

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_IOSBleService_initBle
(JNIEnv *env, jclass jClass)
{
    if (BleInited)
    {
        return;
    }
    BleInited = 1;

    mat_jBleServiceClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/ble/impl/IOSBleService"));
    mat_jBleService_setDetection = (*env)->GetStaticMethodID(env, mat_jBleServiceClass, "setDetection", "(Ljava/lang/String;IIII)V");
    mat_jBleService_gotPeripheral = (*env)->GetStaticMethodID(env, mat_jBleServiceClass, "gotPeripheral", "(Ljava/lang/String;Ljava/lang/String;)V");
    mat_jBleService_gotState = (*env)->GetStaticMethodID(env, mat_jBleServiceClass, "gotState", "(Ljava/lang/String;Ljava/lang/String;)V");
    mat_jBleService_gotProfile = (*env)->GetStaticMethodID(env, mat_jBleServiceClass, "gotProfile", "(Ljava/lang/String;Ljava/lang/String;Z)V");
    mat_jBleService_removeProfile = (*env)->GetStaticMethodID(env, mat_jBleServiceClass, "removeProfile", "(Ljava/lang/String;Ljava/lang/String;)V");
    mat_jBleService_gotCharacteristic = (*env)->GetStaticMethodID(env, mat_jBleServiceClass, "gotCharacteristic", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
    mat_jBleService_gotValue = (*env)->GetStaticMethodID(env, mat_jBleServiceClass, "gotValue", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
    mat_jBleService_gotDescriptor = (*env)->GetStaticMethodID(env, mat_jBleServiceClass, "gotDescriptor", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");

    NSLog(@"Init Ble");
    _Ble = [[Ble alloc] init];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_IOSBleService_startObserver
(JNIEnv *env, jclass jClass, jobjectArray jUuidsArray)
{
    int uuidCount = (*env)->GetArrayLength(env, jUuidsArray);
    NSMutableArray *uuids = [[NSMutableArray alloc] init];

    for (int i=0; i<uuidCount; i++) {
        jstring juuid = (jstring) ((*env)->GetObjectArrayElement(env, jUuidsArray, i));
        const jchar *uuidString = (*env)->GetStringChars(env, juuid, NULL);
        NSString *uuid = [NSString stringWithCharacters:(UniChar *)uuidString length:(*env)->GetStringLength(env, juuid)];
        (*env)->ReleaseStringChars(env, juuid, uuidString);
        [uuids addObject:uuid];
    }
    arrayOfUuids = [NSArray arrayWithArray:uuids];

    [_Ble startObserver];
    return;
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_IOSBleService_stopObserver
(JNIEnv *env, jclass jClass)
{
    [_Ble stopObserver];
    return;
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_IOSBleService_startBroadcast
(JNIEnv *env, jclass jClass, jstring jUuid, jint major, jint minor, jstring jId)
{
    const jchar *uuidchars = (*env)->GetStringChars(env, jUuid, NULL);
    NSString *uuid = [NSString stringWithCharacters:(UniChar *)uuidchars length:(*env)->GetStringLength(env, jUuid)];
    (*env)->ReleaseStringChars(env, jUuid, uuidchars);

    const jchar *idchars = (*env)->GetStringChars(env, jId, NULL);
    NSString *id = [NSString stringWithCharacters:(UniChar *)idchars length:(*env)->GetStringLength(env, jId)];
    (*env)->ReleaseStringChars(env, jId, idchars);

    NSLog(@"Start Broadcasting %@ - %@", uuid, id);
    [_Ble startBroadcast:uuid major:major minor:minor id:id];
    return;
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_IOSBleService_stopBroadcast
(JNIEnv *env, jclass jClass)
{
    NSLog(@"Stop Broadcasting");
    [_Ble stopBroadcast];
    return;
}

// DEVICES

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_IOSBleService_startScanningPeripherals
(JNIEnv *env, jclass jClass)
{
    NSLog(@"startScanningPeripherals");
    [_Ble startScanningPeripherals];
    return;
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_IOSBleService_stopScanningPeripherals
(JNIEnv *env, jclass jClass)
{
    NSLog(@"stopScanningPeripherals");
    [_Ble stopScanningPeripherals];
    return;
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_IOSBleService_doConnect
(JNIEnv *env, jclass jClass, jstring jName, jstring jUiid)
{
    const jchar *namechars = (*env)->GetStringChars(env, jName, NULL);
    NSString *name = [NSString stringWithCharacters:(UniChar *)namechars length:(*env)->GetStringLength(env, jName)];
    (*env)->ReleaseStringChars(env, jName, namechars);

    const jchar *uuidchars = (*env)->GetStringChars(env, jUiid, NULL);
    NSString *uuid = [NSString stringWithCharacters:(UniChar *)uuidchars length:(*env)->GetStringLength(env, jUiid)];
    (*env)->ReleaseStringChars(env, jUiid, uuidchars);

    NSLog(@"Connect %@ - %@", name, uuid);
    [_Ble connect:name uuid:uuid];
    return;
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_IOSBleService_doDisconnect
(JNIEnv *env, jclass jClass, jstring jName, jstring jUiid)
{
    const jchar *namechars = (*env)->GetStringChars(env, jName, NULL);
    NSString *name = [NSString stringWithCharacters:(UniChar *)namechars length:(*env)->GetStringLength(env, jName)];
    (*env)->ReleaseStringChars(env, jName, namechars);

    const jchar *uuidchars = (*env)->GetStringChars(env, jUiid, NULL);
    NSString *uuid = [NSString stringWithCharacters:(UniChar *)uuidchars length:(*env)->GetStringLength(env, jUiid)];
    (*env)->ReleaseStringChars(env, jUiid, uuidchars);

    NSLog(@"Disconnect %@ - %@", name, uuid);
    [_Ble disconnect:name uuid:uuid];
    return;
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_IOSBleService_doRead
(JNIEnv *env, jclass jClass, jstring jName, jstring jUiidService, jstring jUiidChar)
{
    const jchar *namechars = (*env)->GetStringChars(env, jName, NULL);
    NSString *name = [NSString stringWithCharacters:(UniChar *)namechars length:(*env)->GetStringLength(env, jName)];
    (*env)->ReleaseStringChars(env, jName, namechars);

    const jchar *uuidServicechars = (*env)->GetStringChars(env, jUiidService, NULL);
    NSString *uuidService = [NSString stringWithCharacters:(UniChar *)uuidServicechars length:(*env)->GetStringLength(env, jUiidService)];
    (*env)->ReleaseStringChars(env, jUiidService, uuidServicechars);

    const jchar *uuidCharchars = (*env)->GetStringChars(env, jUiidChar, NULL);
    NSString *uuidChar = [NSString stringWithCharacters:(UniChar *)uuidCharchars length:(*env)->GetStringLength(env, jUiidChar)];
    (*env)->ReleaseStringChars(env, jUiidChar, uuidCharchars);

    if (debugAttach) {
        NSLog(@"Read %@ - %@ - %@", name, uuidService, uuidChar);
    }
    [_Ble read:name uuidService:uuidService uuidChar:uuidChar];
    return;
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_IOSBleService_doWrite
(JNIEnv *env, jclass jClass, jstring jName, jstring jUiidService, jstring jUiidChar, jbyteArray jArray)
{
    const jchar *namechars = (*env)->GetStringChars(env, jName, NULL);
    NSString *name = [NSString stringWithCharacters:(UniChar *)namechars length:(*env)->GetStringLength(env, jName)];
    (*env)->ReleaseStringChars(env, jName, namechars);

    const jchar *uuidServicechars = (*env)->GetStringChars(env, jUiidService, NULL);
    NSString *uuidService = [NSString stringWithCharacters:(UniChar *)uuidServicechars length:(*env)->GetStringLength(env, jUiidService)];
    (*env)->ReleaseStringChars(env, jUiidService, uuidServicechars);

    const jchar *uuidCharchars = (*env)->GetStringChars(env, jUiidChar, NULL);
    NSString *uuidChar = [NSString stringWithCharacters:(UniChar *)uuidCharchars length:(*env)->GetStringLength(env, jUiidChar)];
    (*env)->ReleaseStringChars(env, jUiidChar, uuidCharchars);

    jbyte* bytes = (*env)->GetByteArrayElements(env, jArray, NULL);
    const jsize lengthOfBytes = (*env)->GetArrayLength(env, jArray);
    NSData *data = [NSData dataWithBytes:bytes length:lengthOfBytes];
    (*env)->ReleaseByteArrayElements(env, jArray, bytes, JNI_ABORT);

    if (debugAttach) {
        NSLog(@"write %@ - %@ - %@: %@", name, uuidService, uuidChar, data);
    }
    [_Ble write:name uuidService:uuidService uuidChar:uuidChar data:data];
    return;
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_IOSBleService_doSubscribe
(JNIEnv *env, jclass jClass, jstring jName, jstring jUiidService, jstring jUiidChar, jboolean subscribe)
{
    const jchar *namechars = (*env)->GetStringChars(env, jName, NULL);
    NSString *name = [NSString stringWithCharacters:(UniChar *)namechars length:(*env)->GetStringLength(env, jName)];
    (*env)->ReleaseStringChars(env, jName, namechars);

    const jchar *uuidServicechars = (*env)->GetStringChars(env, jUiidService, NULL);
    NSString *uuidService = [NSString stringWithCharacters:(UniChar *)uuidServicechars length:(*env)->GetStringLength(env, jUiidService)];
    (*env)->ReleaseStringChars(env, jUiidService, uuidServicechars);

    const jchar *uuidCharchars = (*env)->GetStringChars(env, jUiidChar, NULL);
    NSString *uuidChar = [NSString stringWithCharacters:(UniChar *)uuidCharchars length:(*env)->GetStringLength(env, jUiidChar)];
    (*env)->ReleaseStringChars(env, jUiidChar, uuidCharchars);

    BOOL notify = subscribe ? YES : NO;
    if (debugAttach) {
        NSLog(@"Subscribe %@ - %@ - %@ - %d", name, uuidService, uuidChar, notify);
    }
    [_Ble subscribe:name uuidService:uuidService uuidChar:uuidChar notify:notify];
    return;
}

void setDetection(CLBeacon *foundBeacon) {
    if (foundBeacon)
    {
        NSString *uuid = foundBeacon.proximityUUID.UUIDString;
        int major = [foundBeacon.major intValue];
        int minor = [foundBeacon.minor intValue];
        int rssi = foundBeacon.rssi;
        int proximity = 0;
        switch (foundBeacon.proximity)
        {
            case CLProximityUnknown:  { proximity = 0; } break;
            case CLProximityImmediate:  { proximity = 1; } break;
            case CLProximityNear:  { proximity = 2; } break;
            case CLProximityFar:  { proximity = 3; } break;
            default:  { proximity = 0; } break;
        }
        const char *uuidChars = [uuid UTF8String];
        jstring juuid = (*env)->NewStringUTF(env, uuidChars);
        (*env)->CallStaticVoidMethod(env, mat_jBleServiceClass, mat_jBleService_setDetection, juuid, major, minor, rssi, proximity);
        (*env)->DeleteLocalRef(env, juuid);
    }
}

void discoveredPeripheral(CBPeripheral *peripheral) {
    NSString *pname = peripheral.name;
    const char *pnamechars = [pname UTF8String];
    jstring jpname = (*env)->NewStringUTF(env,pnamechars);

    NSString *puuid = [NSString stringWithFormat:@"%@", [[peripheral identifier] UUIDString]];
    const char *puuidchars = [puuid UTF8String];
    jstring jpuuid = (*env)->NewStringUTF(env,puuidchars);

    (*env)->CallStaticVoidMethod(env, mat_jBleServiceClass, mat_jBleService_gotPeripheral, jpname, jpuuid);
    (*env)->DeleteLocalRef(env, jpname);
    (*env)->DeleteLocalRef(env, jpuuid);
}

void stateChanged(CBPeripheral *peripheral) {
    NSString *pname = peripheral.name;
    const char *pnamechars = [pname UTF8String];
    jstring jpname = (*env)->NewStringUTF(env,pnamechars);

    NSString *stateString = nil;
    switch (peripheral.state) {
        case CBPeripheralStateConnected: stateString = @"STATE_CONNECTED"; break;
        case CBPeripheralStateConnecting: stateString = @"STATE_CONNECTING"; break;
        case CBPeripheralStateDisconnected: stateString = @"STATE_DISCONNECTED"; break;
        case CBPeripheralStateDisconnecting: stateString = @"STATE_DISCONNECTING"; break;
        default: stateString = @"STATE_UNKOWN"; break;
    }
    const char *pstatechars = [stateString UTF8String];
    jstring jpstate = (*env)->NewStringUTF(env,pstatechars);

    if (debugAttach) {
        NSLog(@"Sending State of %@: %@", pname, stateString);
    }
    (*env)->CallStaticVoidMethod(env, mat_jBleServiceClass, mat_jBleService_gotState, jpname, jpstate);
    (*env)->DeleteLocalRef(env, jpname);
    (*env)->DeleteLocalRef(env, jpstate);
}

void discoveredProfile(CBPeripheral *peripheral, CBService *service) {
    NSString *pname = peripheral.name;
    const char *pnamechars = [pname UTF8String];
    jstring jpname = (*env)->NewStringUTF(env,pnamechars);

    NSString *puuid = [NSString stringWithFormat:@"%@", service.UUID];
    const char *puuidchars = [puuid UTF8String];
    jstring jpuuid = (*env)->NewStringUTF(env,puuidchars);

    jboolean jprimary = service.isPrimary;

    if (debugAttach) {
        NSLog(@"Sending profile of %@: %@", pname, puuid);
    }
    (*env)->CallStaticVoidMethod(env, mat_jBleServiceClass, mat_jBleService_gotProfile, jpname, jpuuid, jprimary);
    (*env)->DeleteLocalRef(env, jpname);
    (*env)->DeleteLocalRef(env, jpuuid);
}

void removeProfile(CBPeripheral *peripheral, CBService *service) {
    NSString *pname = peripheral.name;
    const char *pnamechars = [pname UTF8String];
    jstring jpname = (*env)->NewStringUTF(env,pnamechars);

    NSString *puuid = [NSString stringWithFormat:@"%@", service.UUID];
    const char *puuidchars = [puuid UTF8String];
    jstring jpuuid = (*env)->NewStringUTF(env,puuidchars);

    if (debugAttach) {
        NSLog(@"Removing profile of %@: %@", pname, puuid);
    }
    (*env)->CallStaticVoidMethod(env, mat_jBleServiceClass, mat_jBleService_removeProfile, jpname, jpuuid);
    (*env)->DeleteLocalRef(env, jpname);
    (*env)->DeleteLocalRef(env, jpuuid);
}

void discoveredCharacteristic(CBPeripheral *peripheral, CBService *service, CBCharacteristic *aChar) {
    NSString *pname = peripheral.name;
    const char *pnamechars = [pname UTF8String];
    jstring jpname = (*env)->NewStringUTF(env,pnamechars);

    NSString *puuid = [NSString stringWithFormat:@"%@", service.UUID];
    const char *puuidchars = [puuid UTF8String];
    jstring jpuuid = (*env)->NewStringUTF(env,puuidchars);

    NSString *puuid2 = [NSString stringWithFormat:@"%@", aChar.UUID];
    const char *puuid2chars = [puuid2 UTF8String];
    jstring jpuuid2 = (*env)->NewStringUTF(env,puuid2chars);

    NSMutableString *pprops = [[NSMutableString alloc]init];
    CBCharacteristicProperties properties = [aChar properties];
    if (properties & CBCharacteristicPropertyBroadcast) {
        [pprops appendString:@"broadcast, "];
    }
    if (properties & CBCharacteristicPropertyRead) {
        [pprops appendString:@"read, "];
    }
    if (properties & CBCharacteristicPropertyWriteWithoutResponse) {
        [pprops appendString:@"write without response, "];
    }
    if (properties & CBCharacteristicPropertyWrite) {
        [pprops appendString:@"write, "];
    }
    if (properties & CBCharacteristicPropertyNotify) {
        [pprops appendString:@"notify, "];
    }
    if (properties & CBCharacteristicPropertyIndicate) {
        [pprops appendString:@"indicate, "];
    }
    if (properties & CBCharacteristicPropertyAuthenticatedSignedWrites) {
        [pprops appendString:@"authenticated signed writes, "];
    }
    if (properties & CBCharacteristicPropertyExtendedProperties) {
        [pprops appendString:@"extended properties, "];
    }
    if (properties & CBCharacteristicPropertyNotifyEncryptionRequired) {
        [pprops appendString:@"notify encryption required, "];
    }
    if (properties & CBCharacteristicPropertyIndicateEncryptionRequired) {
        [pprops appendString:@"indicate encryption required, "];
    }

    [pprops deleteCharactersInRange:NSMakeRange([pprops length]-2, 2)];
    const char *ppropschars = [pprops UTF8String];
    jstring jpprops = (*env)->NewStringUTF(env,ppropschars);

    if (debugAttach) {
        NSLog(@"Sending characteristic of %@: %@: %@ and properties %@", pname, puuid, puuid2, pprops);
    }
    (*env)->CallStaticVoidMethod(env, mat_jBleServiceClass, mat_jBleService_gotCharacteristic, jpname, jpuuid, jpuuid2, jpprops);
    (*env)->DeleteLocalRef(env, jpname);
    (*env)->DeleteLocalRef(env, jpuuid);
    (*env)->DeleteLocalRef(env, jpuuid2);
    (*env)->DeleteLocalRef(env, jpprops);
}

void setData(CBPeripheral *peripheral, CBCharacteristic *aChar) {
    NSString *pname = peripheral.name;
    const char *pnamechars = [pname UTF8String];
    jstring jpname = (*env)->NewStringUTF(env,pnamechars);

    NSString *puuid = [NSString stringWithFormat:@"%@", aChar.UUID];
    const char *puuidchars = [puuid UTF8String];
    jstring jpuuid = (*env)->NewStringUTF(env,puuidchars);

    NSString *pdata = [aChar.value base64EncodedStringWithOptions:NSDataBase64EncodingEndLineWithLineFeed];
    const char *pdatachars = [pdata UTF8String];
    jstring jpdata = (*env)->NewStringUTF(env,pdatachars);

    if (debugAttach) {
        NSLog(@"Sending value of %@: %@ and properties %@", pname, puuid, pdata);
    }
    (*env)->CallStaticVoidMethod(env, mat_jBleServiceClass, mat_jBleService_gotValue, jpname, jpuuid, jpdata);
    (*env)->DeleteLocalRef(env, jpname);
    (*env)->DeleteLocalRef(env, jpuuid);
    (*env)->DeleteLocalRef(env, jpdata);
}

void discoveredDescriptor(CBPeripheral *peripheral, CBDescriptor *aDesc) {
    NSString *pname = peripheral.name;
    const char *pnamechars = [pname UTF8String];
    jstring jpname = (*env)->NewStringUTF(env,pnamechars);

    NSString *puuidDesc = [NSString stringWithFormat:@"%@", aDesc.UUID];
    const char *puuidDescchars = [puuidDesc UTF8String];
    jstring jpuuidDesc = (*env)->NewStringUTF(env,puuidDescchars);

    NSString *pdata = @"Unknown";
    if([aDesc.UUID isEqual:[CBUUID UUIDWithString:CBUUIDCharacteristicExtendedPropertiesString]] ||
       [aDesc.UUID isEqual:[CBUUID UUIDWithString:CBUUIDClientCharacteristicConfigurationString]] ||
       [aDesc.UUID isEqual:[CBUUID UUIDWithString:CBUUIDServerCharacteristicConfigurationString]]) {
        // value is NSNumber
        pdata = [aDesc.value stringValue];
    } else if ([aDesc.UUID isEqual:[CBUUID UUIDWithString:CBUUIDCharacteristicUserDescriptionString]]) {
        // value is NSString
        pdata = aDesc.value;
    } else if ([aDesc.UUID isEqual:[CBUUID UUIDWithString:CBUUIDCharacteristicFormatString]] ||
               [aDesc.UUID isEqual:[CBUUID UUIDWithString:CBUUIDCharacteristicAggregateFormatString]]) {
        // value is NSData
        pdata = [[NSString alloc] initWithData:aDesc.value encoding:NSUTF8StringEncoding];
    }

    const char *pdatachars = [pdata UTF8String];
    jstring jpdata = (*env)->NewStringUTF(env,pdatachars);

    if (debugAttach) {
        NSLog(@"Sending value of %@ and properties %@ %@", pname, puuidDesc, pdata);
    }
    (*env)->CallStaticVoidMethod(env, mat_jBleServiceClass, mat_jBleService_gotDescriptor, jpname, jpuuidDesc, jpdata);
    (*env)->DeleteLocalRef(env, jpname);
    (*env)->DeleteLocalRef(env, jpuuidDesc);
    (*env)->DeleteLocalRef(env, jpdata);
}

@implementation Ble

- (void)startObserver
{
    if (!self.locationManager) {
        self.locationManager = [[CLLocationManager alloc] init];
        self.locationManager.delegate = self;

        _bluetoothManager = [[CBCentralManager alloc] initWithDelegate:self queue:nil];
    }

    if ([[[UIDevice currentDevice] systemVersion] floatValue] >= 8.0)
    {
        // Requires Always. With WhenInUse it won't work
        [self.locationManager requestAlwaysAuthorization];
    }

    AttachLog(@"Start monitoring for regions");
    for (NSString* uuidString in arrayOfUuids)
    {
        NSUUID *uuid = [[NSUUID alloc] initWithUUIDString:uuidString];
        CLBeaconRegion *beaconRegion = [[CLBeaconRegion alloc] initWithProximityUUID:uuid
                identifier:[NSString stringWithFormat:@"com.gluonhq.beacon.%@", uuidString]];
        [self.locationManager startMonitoringForRegion:beaconRegion];
        [self.locationManager startRangingBeaconsInRegion:beaconRegion];
    }
}

- (void)stopObserver
{
    AttachLog(@"Stop monitoring for regions");
    if (self.locationManager)
    {
        NSSet *setOfRegions = [self.locationManager monitoredRegions];
        for (CLRegion *region in setOfRegions) {
            [self.locationManager stopMonitoringForRegion:(CLBeaconRegion *) region];
            [self.locationManager stopRangingBeaconsInRegion:(CLBeaconRegion *) region];
        }
    }
}

- (void)locationManager:(CLLocationManager*)manager didEnterRegion:(CLRegion*)region
{
    [self.locationManager startRangingBeaconsInRegion:(CLBeaconRegion *) region];
}

-(void)locationManager:(CLLocationManager*)manager didExitRegion:(CLRegion*)region
{
    [self.locationManager stopRangingBeaconsInRegion:(CLBeaconRegion *) region];
}

- (void)locationManager:(CLLocationManager *)manager rangingBeaconsDidFailForRegion:(CLBeaconRegion *)region withError:(NSError *)error
{
    AttachLog(@"Ranging Beacons failed with error: %@", [error localizedDescription]);
}

-(void)locationManager:(CLLocationManager*)manager didRangeBeacons:(NSArray*)beacons inRegion:(CLBeaconRegion*)region
{
    // sorted by proximity
    for (CLBeacon* beacon in beacons) {
      setDetection(beacon);
    }
}

- (void)locationManager:(CLLocationManager *)manager monitoringDidFailForRegion:(CLRegion *)region withError:(NSError *)error
{
    AttachLog(@"Region monitoring failed with error: %@", [error localizedDescription]);
}

- (void) startBroadcast:(NSString *)uuidString major:(NSInteger)major minor:(NSInteger)minor id:(NSString *)idString
{
    [self logMessage:@"startBroadcast %@ %d - %d - %@", uuidString, major, minor, idString];

    NSUUID *uuid = [[NSUUID alloc] initWithUUIDString:uuidString];
    _localBeacon = [[CLBeaconRegion alloc] initWithProximityUUID:uuid
            major:(CLBeaconMajorValue)major
            minor:(CLBeaconMinorValue)minor
            identifier:[NSString stringWithFormat:@"com.gluonhq.beacon.%@", idString]];
    [self logMessage:@"localBeacon: %@", _localBeacon];
    if (_peripheralManager) {
        [_peripheralManager stopAdvertising];
    }
    _peripheralManager = [[CBPeripheralManager alloc] initWithDelegate:(id<CBPeripheralManagerDelegate>)self queue:nil options:nil];
}

- (void) stopBroadcast
{
    [self logMessage:@"stopBroadcast"];
    if (_peripheralManager) {
        [_peripheralManager stopAdvertising];
        _peripheralManager = nil;
        _localBeacon = nil;
    }
}

- (void)peripheralManagerDidUpdateState:(CBPeripheralManager *)peripheral
{
    int state = peripheral.state;
    [self logMessage:@"Peripheral manager state =  %d for beacon %@", state, _localBeacon];

    switch (state) {
    case CBManagerStatePoweredOn:
        if (_localBeacon) {
            NSDictionary *peripheralData = [self.localBeacon peripheralDataWithMeasuredPower:nil];
            [self logMessage:@"Starting to advertise... %@", peripheralData];
            [peripheral startAdvertising:peripheralData];
        }
        break;
    case CBManagerStatePoweredOff:
        [self logMessage:@"Stopping advertising..."];
        [peripheral stopAdvertising];
        break;
    }
}

- (void)peripheralManagerDidStartAdvertising:(CBPeripheralManager *)peripheral error:(NSError *)error
{
    if (error)
    {
        NSLog(@"Error advertising: %@", [error localizedDescription]);
    }
    else
    {
       [self logMessage:@"BLE started advertising successfully"];
    }
}


// BLEDEVICES

bool requestPeripheralScan = false;
NSMutableArray *discoveredDevices;

-(void)startScanningPeripherals
{
    // clear out the list
    discoveredDevices = [[NSMutableArray alloc] init];

    NSLog(@"Start scanning for peripherals");
    requestPeripheralScan = true;
    _bluetoothManager = [[CBCentralManager alloc] initWithDelegate:self queue:nil];
}

-(void)stopScanningPeripherals
{
    NSLog(@"stop scanning for peripherals");
    requestPeripheralScan = false;
    [_bluetoothManager stopScan];
}

- (void) connect:(NSString *)name uuid:(NSString *)uuid
{
    [self logMessage:@"iOS_connect %@ - %@", name, uuid];

    for (CBPeripheral *p in discoveredDevices) {
        NSString *puuid = [NSString stringWithFormat:@"%@", [[p identifier] UUIDString]];
        if ([p.name isEqualToString:name] && [puuid isEqualToString:uuid]) {
            _peripheral = p;
            p.delegate = self;
            stateChanged(p);
            [_bluetoothManager connectPeripheral:p options:nil];
            break;
        }
    }
}

- (void) disconnect:(NSString *)name uuid:(NSString *)uuid
{
    [self logMessage:@"iOS_disconnect %@ - %@", name, uuid];

    for (CBPeripheral *p in discoveredDevices) {
        NSString *puuid = [NSString stringWithFormat:@"%@", [[p identifier] UUIDString]];
        if ([p.name isEqualToString:name] || [puuid isEqualToString:uuid]) {
            p.delegate = self;
            stateChanged(p);
            [_bluetoothManager cancelPeripheralConnection:p];
            break;
        }
    }
}

- (void) read:(NSString *)name uuidService:(NSString *)uuidService uuidChar:(NSString *)uuidChar
{
    [self logMessage:@"iOS_read %@ - %@- %@", name, uuidService, uuidChar];

    for (CBPeripheral *p in discoveredDevices)
    {
        if ([p.name isEqualToString:name])
        {
            [self logMessage:@"1. Reading for peripheral %@", p.name];
            _peripheral = p;
            p.delegate = self;
            for (CBService *s in p.services)
            {
                if ([s.UUID isEqual:[CBUUID UUIDWithString:uuidService]])
                {
                    [self logMessage:@"2. Reading for service %@", s.UUID];
                    for (CBCharacteristic *aChar in s.characteristics)
                    {
                        if ([aChar.UUID isEqual:[CBUUID UUIDWithString:uuidChar]])
                        {
                            [self logMessage:@"3. Reading for char %@", aChar.UUID];
                            //[_peripheral setNotifyValue:YES forCharacteristic:aChar];
                            [_peripheral readValueForCharacteristic:aChar];
                            break;
                        }
                    }
                    break;
                }
            }
            break;
        }
    }
}

- (void) write:(NSString *)name uuidService:(NSString *)uuidService uuidChar:(NSString *)uuidChar data:(NSData *)data
{
    [self logMessage:@"iOS_write %@ - %@- %@ : %@", name, uuidService, uuidChar, data];

    for (CBPeripheral *p in discoveredDevices)
    {
        if ([p.name isEqualToString:name])
        {
            [self logMessage:@"1. Reading for peripheral %@", p.name];
            _peripheral = p;
            p.delegate = self;
            for (CBService *s in p.services)
            {
                if ([s.UUID isEqual:[CBUUID UUIDWithString:uuidService]])
                {
                    [self logMessage:@"2. Reading for service %@", s.UUID];
                    for (CBCharacteristic *aChar in s.characteristics)
                    {
                        if ([aChar.UUID isEqual:[CBUUID UUIDWithString:uuidChar]])
                        {
                            [self logMessage:@"3. Writing for char %@ with bytes %@", aChar.UUID, data];

                            [_peripheral writeValue:data forCharacteristic:aChar type:CBCharacteristicWriteWithResponse];
                            break;
                        }
                    }
                    break;
                }
            }
            break;
        }
    }
}

- (void) subscribe:(NSString *)name uuidService:(NSString *)uuidService uuidChar:(NSString *)uuidChar notify:(BOOL)notify
{
   [self logMessage:@"iOS_read %@ - %@- %@", name, uuidService, uuidChar];

    for (CBPeripheral *p in discoveredDevices)
    {
        if ([p.name isEqualToString:name])
        {
            [self logMessage:@"1. Reading for peripheral %@", p.name];
            _peripheral = p;
            p.delegate = self;
            for (CBService *s in p.services)
            {
                if ([s.UUID isEqual:[CBUUID UUIDWithString:uuidService]])
                {
                    [self logMessage:@"2. Reading for service %@", s.UUID];
                    for (CBCharacteristic *aChar in s.characteristics)
                    {
                        if ([aChar.UUID isEqual:[CBUUID UUIDWithString:uuidChar]])
                        {
                            [self logMessage:@"3. Subscribing for char %@", aChar.UUID];
                            [_peripheral setNotifyValue:notify forCharacteristic:aChar];
                            break;
                        }
                    }
                    break;
                }
            }
            break;
        }
    }
}

- (void)centralManager:(CBCentralManager *)central didDiscoverPeripheral:(CBPeripheral *)peripheral
                    advertisementData:(NSDictionary *)advertisementData RSSI:(NSNumber *)RSSI {

    [self logMessage:@"Discovered %@", peripheral];
    [discoveredDevices addObject:peripheral];
    discoveredPeripheral(peripheral);
}

- (void)peripheralDidUpdateName:(CBPeripheral *)peripheral
{
    [self logMessage:@"Peripheral updated name: %@", peripheral];
    NSInteger index = [discoveredDevices indexOfObject:peripheral];
    if (NSNotFound == index) {
        [discoveredDevices addObject:peripheral];
    } else {
        [discoveredDevices replaceObjectAtIndex:index withObject:peripheral];
    }
    discoveredPeripheral(peripheral);
}

- (void)centralManager:(CBCentralManager *)central didConnectPeripheral:(CBPeripheral *)peripheral
{
    [self logMessage:@"Peripheral %@ Connected: %@", peripheral.name, peripheral.state == CBPeripheralStateConnected ? @"YES" : @"NO"];
    [peripheral setDelegate:self];
    [peripheral discoverServices:nil];
    stateChanged(peripheral);
}

- (void)centralManagerDidUpdateState:(CBCentralManager *)central
{
    NSString *stateString = nil;
    if (@available(iOS 10.0, *))
    {
        switch(_bluetoothManager.state)
        {
            case CBManagerStateResetting: stateString = @"The connection with the system service was momentarily lost, update imminent."; break;
            case CBManagerStateUnsupported: stateString = @"The platform doesn't support Bluetooth Low Energy."; break;
            case CBManagerStateUnauthorized: stateString = @"The app is not authorized to use Bluetooth Low Energy."; break;
            case CBManagerStatePoweredOff: stateString = @"Bluetooth is currently powered off."; break;
            case CBManagerStatePoweredOn:
                    {
                        stateString = @"Bluetooth is currently powered on and available to use.";
                        if (requestPeripheralScan) {
                            [self logMessage:@"Scanning now..."];
                            [_bluetoothManager scanForPeripheralsWithServices:nil options:nil];
                            requestPeripheralScan = false;
                        }

                    }
                    break;
            default: stateString = @"State unknown, update imminent."; break;
        }
    }
    else
    {
        #pragma clang diagnostic push
        #pragma clang diagnostic ignored "-Wdeprecated-declarations"

        switch(_bluetoothManager.state)
        {
            case CBCentralManagerStateResetting: stateString = @"The connection with the system service was momentarily lost, update imminent."; break;
            case CBCentralManagerStateUnsupported: stateString = @"The platform doesn't support Bluetooth Low Energy."; break;
            case CBCentralManagerStateUnauthorized: stateString = @"The app is not authorized to use Bluetooth Low Energy."; break;
            case CBCentralManagerStatePoweredOff: stateString = @"Bluetooth is currently powered off."; break;
            case CBCentralManagerStatePoweredOn:
                    {
                        stateString = @"Bluetooth is currently powered on and available to use.";
                        if (requestPeripheralScan) {
                            [self logMessage:@"Scanning now..."];
                            [_bluetoothManager scanForPeripheralsWithServices:nil options:nil];
                            requestPeripheralScan = false;
                        }

                    }
                    break;
            default: stateString = @"State unknown, update imminent."; break;
        }

        #pragma clang diagnostic pop
    }
    AttachLog(@"Bluetooth State: %@",stateString);
}

- (void)peripheral:(CBPeripheral *)peripheral didDiscoverServices:(NSError *)error
{
    for (CBService *service in peripheral.services) {
        [self logMessage:@"Discovered service: %@", service];
        discoveredProfile(peripheral, service);
        [peripheral discoverCharacteristics:nil forService:service];
    }
}

- (void)peripheral:(CBPeripheral *)peripheral didModifyServices:(NSArray<CBService *> *)invalidatedServices
 {
    for (CBService *service in invalidatedServices) {
         [self logMessage:@"Removed service: %@", service];
         removeProfile(peripheral, service);
     }
 }

- (void)peripheral:(CBPeripheral *)peripheral didDiscoverCharacteristicsForService:(CBService *)service error:(NSError *)error
{
    for (CBCharacteristic *aChar in service.characteristics) {
        [self logMessage:@"Discovered characteristic: %@", aChar];
        discoveredCharacteristic(peripheral, service, aChar);
        [peripheral discoverDescriptorsForCharacteristic:aChar];
    }
}

- (void)peripheral:(CBPeripheral *)peripheral didUpdateValueForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error
{
    [self logMessage:@"Characteristic value : %@ with ID %@", characteristic.value, characteristic.UUID];
    setData(peripheral, characteristic);
}

- (void)peripheral:(CBPeripheral *)peripheral didDiscoverDescriptorsForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error
{
    for (CBDescriptor *aDesc in characteristic.descriptors) {
        [self logMessage:@"Characteristic with ID %@ has descriptor %@", characteristic.UUID, aDesc];
        peripheral.delegate = self;
        [peripheral readValueForDescriptor:aDesc];
    }
}

- (void)peripheral:(CBPeripheral *)peripheral didUpdateValueForDescriptor:(CBDescriptor *)descriptor error:(NSError *)error
{
    [self logMessage:@"descriptor with ID %@ has new value: %@", descriptor.UUID, descriptor.value];
    discoveredDescriptor(peripheral, descriptor);
}

- (void)peripheral:(CBPeripheral *)peripheral didUpdateNotificationStateForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error
{
    [self logMessage:@"Notifications enabled for characteristic %@: %@", characteristic.UUID, characteristic.isNotifying ? @"YES" : @"NO"];
}

-(void)peripheral:(CBPeripheral *)peripheral didWriteValueForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error
{
    [peripheral readValueForCharacteristic:characteristic];
    [self logMessage:@"Did Write for characteristic %@ the value: %@", characteristic.UUID, characteristic.value];
}

- (void)centralManager:(CBCentralManager *)central didDisconnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error
{
    [self logMessage:@"Peripheral %@ Disconnected: %@", peripheral.name, peripheral.state == CBPeripheralStateConnected ? @"YES" : @"NO"];
    stateChanged(peripheral);
}

- (void) logMessage:(NSString *)format, ...;
{
    if (debugAttach)
    {
        va_list args;
        va_start(args, format);
        NSLogv([@"[Debug] " stringByAppendingString:format], args);
        va_end(args);
    }
}

@end
