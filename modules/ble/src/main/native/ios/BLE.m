/*
 * Copyright (c) 2016, 2019, Gluon
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
    mat_jBleService_setDetection = (*env)->GetMethodID(env, mat_jBleServiceClass, "setDetection", "(Ljava/lang/String;IIII)V");

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
        (*env)->CallVoidMethod(env, mat_jBleServiceClass, mat_jBleService_setDetection, juuid, major, minor, rssi, proximity);
        (*env)->DeleteLocalRef(env, juuid);
    }
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

    NSLog(@"Start monitoring for regions");
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
    NSLog(@"Stop monitoring for regions");
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
    NSLog(@"Ranging Beacons failed with error: %@", [error localizedDescription]);
}

-(void)locationManager:(CLLocationManager*)manager didRangeBeacons:(NSArray*)beacons inRegion:(CLBeaconRegion*)region
{
    // sorted by proximity
    CLBeacon *foundBeacon = [beacons firstObject];
    setDetection(foundBeacon);
}

- (void)locationManager:(CLLocationManager *)manager monitoringDidFailForRegion:(CLRegion *)region withError:(NSError *)error
{
    NSLog(@"Region monitoring failed with error: %@", [error localizedDescription]);
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
            case CBManagerStatePoweredOn: stateString = @"Bluetooth is currently powered on and available to use."; break;
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
            case CBCentralManagerStatePoweredOn: stateString = @"Bluetooth is currently powered on and available to use."; break;
            default: stateString = @"State unknown, update imminent."; break;
        }

        #pragma clang diagnostic pop
    }
    NSLog(@"Bluetooth State: %@",stateString);
}

@end
