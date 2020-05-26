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

#include "Position.h"

JNIEnv *env;

JNIEXPORT jint JNICALL
JNI_OnLoad_Position(JavaVM *vm, void *reserved)
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

static int positionInited = 0;

// Position
jclass mat_jPositionServiceClass;
jmethodID mat_jPositionService_setLocation = 0;
Position *_position;

JNIEXPORT void JNICALL Java_com_gluonhq_attach_position_impl_IOSPositionService_initPosition
(JNIEnv *env, jclass jClass)
{
    if (positionInited)
    {
        return;
    }
    positionInited = 1;
    
    mat_jPositionServiceClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/position/impl/IOSPositionService"));
    mat_jPositionService_setLocation = (*env)->GetStaticMethodID(env, mat_jPositionServiceClass, "setLocation", "(DDD)V");

    _position = [[Position alloc] init];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_position_impl_IOSPositionService_startObserver
(JNIEnv *env, jclass jClass, jstring jAccuracy, jlong jInterval, jfloat jDistance, jboolean jBackground)
{
    const jchar *charsAccuracy = (*env)->GetStringChars(env, jAccuracy, NULL);
    NSString *sAccuracy = [NSString stringWithCharacters:(UniChar *)charsAccuracy length:(*env)->GetStringLength(env, jAccuracy)];
    (*env)->ReleaseStringChars(env, jAccuracy, charsAccuracy);

    dispatch_async(dispatch_get_main_queue(), ^{
        [_position startObserver:sAccuracy interval:jInterval distance:jDistance background:jBackground];
    });
    return;   
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_position_impl_IOSPositionService_stopObserver
(JNIEnv *env, jclass jClass)
{
    [_position stopObserver];
    return;   
}

void setLocation(CLLocation *newLocation) {
    if (newLocation)
    {
        double lat = newLocation.coordinate.latitude;
        double lon = newLocation.coordinate.longitude;
        double alt = newLocation.altitude;
        (*env)->CallStaticVoidMethod(env, mat_jPositionServiceClass, mat_jPositionService_setLocation, lat, lon, alt);
    }

}

@implementation Position

- (void)startObserver:(NSString *)accuracy interval:(long)interval distance:(CGFloat)distance background:(BOOL)background 
{

    self.locationManager = [[CLLocationManager alloc] init];
    self.locationManager.delegate = self;
    self.locationManager.distanceFilter = distance;
    if ([accuracy isEqualToString:@"HIGHEST"]) {
        self.locationManager.desiredAccuracy = kCLLocationAccuracyBestForNavigation;
    } else if ([accuracy isEqualToString:@"HIGH"]) {
        self.locationManager.desiredAccuracy = kCLLocationAccuracyBest;
    } else if ([accuracy isEqualToString:@"MEDIUM"]) {
        self.locationManager.desiredAccuracy = kCLLocationAccuracyNearestTenMeters;
    } else if ([accuracy isEqualToString:@"LOW"]) {
        self.locationManager.desiredAccuracy = kCLLocationAccuracyHundredMeters;
    } else if ([accuracy isEqualToString:@"LOWEST"]) {
        self.locationManager.desiredAccuracy = kCLLocationAccuracyKilometer;
    } 
    if (background) {
        self.locationManager.allowsBackgroundLocationUpdates = YES;
        if ([[[UIDevice currentDevice] systemVersion] floatValue] >= 9.0) {
#ifdef __IPHONE_11_0
            if (@available(iOS 11, *)) {
                self.locationManager.showsBackgroundLocationIndicator = YES;
            }
#endif
        }
    }

    if ([[[UIDevice currentDevice] systemVersion] floatValue] >= 8.0)
    {
        if (background) {
            [self.locationManager requestAlwaysAuthorization];
        } else {
            // try to save battery by using GPS only when app is used:
            [self.locationManager requestWhenInUseAuthorization];
        }
    }

    if (debugAttach)
    {
        AttachLog(@"Start updating location with accuracy: %f", self.locationManager.desiredAccuracy);
    }
    [self.locationManager startUpdatingLocation];

    // Request a location update
    [self.locationManager requestLocation];
  
}

- (void)stopObserver 
{
    if (debugAttach)
    {
        AttachLog(@"Stop updating location");
    }
    [self.locationManager stopUpdatingLocation];
}

- (void)locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray *)locations {
    CLLocation *newLocation = [locations lastObject];
    if (debugAttach)
    {
        AttachLog(@"NewLocation: %f, %f, %f", newLocation.coordinate.latitude, newLocation.coordinate.longitude, newLocation.altitude);
    }
    if (newLocation.horizontalAccuracy < 0) 
    {
        if (debugAttach)
        {
            AttachLog(@"iOS location update, horizontal accuracy too small: %.2f", newLocation.horizontalAccuracy);
        }
        // return;
    }

    NSTimeInterval interval = [newLocation.timestamp timeIntervalSinceNow];
    if (interval < -5) 
    {
        if (debugAttach)
        {
            AttachLog(@"iOS location update, time interval to large (probably cached): %.2f", interval);
        }
        // return;
    }

    setLocation(newLocation);
}

- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error
{
    if ([CLLocationManager authorizationStatus] == kCLAuthorizationStatusDenied)
    {
        AttachLog(@"User has denied location services");
    } 
    else 
    {
        AttachLog(@"Location manager did fail with error: %@", error);
        switch([error code])
        {
            case kCLErrorNetwork: // general, network-related error
            {
                AttachLog(@"ErrorNetwork");
            }
            break;
            case kCLErrorDenied:
            {
                AttachLog(@"ErrorDenied");
            }
            break;
            case kCLErrorLocationUnknown:
            {
                AttachLog(@"ErrorLocationUnknown");
            }
            break;
            default:
            {
                AttachLog(@"Unknown error: %@", error);
            }
            break;
        }
    }
}

@end
