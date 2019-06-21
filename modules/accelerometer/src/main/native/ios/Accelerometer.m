/*
 * Copyright (c) 2016, 2019 Gluon
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

#include "Accelerometer.h"

JNIEnv *env;

JNIEXPORT jint JNICALL
JNI_OnLoad_Accelerometer(JavaVM *vm, void *reserved)
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

static int AccelerometerInited = 0;

// Accelerometer
jclass mat_jAccelerometerServiceClass;
jmethodID mat_jAccelerometerService_notifyAcceleration = 0;
Accelerometer *_accelerometer;
BOOL filterGravity;
double rate = 0.01;
double gravity[3];
double alpha = 0.8;
double offset = 0;
double g = 9.81;

JNIEXPORT void JNICALL Java_com_gluonhq_attach_accelerometer_impl_IOSAccelerometerService_initAccelerometer
(JNIEnv *env, jclass jClass)
{
    if (AccelerometerInited)
    {
        return;
    }
    AccelerometerInited = 1;
    
    mat_jAccelerometerServiceClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/accelerometer/impl/IOSAccelerometerService"));
    mat_jAccelerometerService_notifyAcceleration = (*env)->GetStaticMethodID(env, mat_jAccelerometerServiceClass, "notifyAcceleration", "(DDDD)V");

     _accelerometer = [[Accelerometer alloc] init];

    NSTimeInterval uptime = [NSProcessInfo processInfo].systemUptime;
    NSTimeInterval nowTimeIntervalSince1970 = [[NSDate date] timeIntervalSince1970];
    offset = nowTimeIntervalSince1970 - uptime;
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_accelerometer_impl_IOSAccelerometerService_startObserver
(JNIEnv *env, jclass jClass, jboolean jfilterGravity, jint jfrequency)
{
    filterGravity = jfilterGravity;
    if (jfrequency > 0) {
        rate = 1.0 / ((double) jfrequency);
    }

    dispatch_async(dispatch_get_main_queue(), ^{
        [_accelerometer startObserver];
    });
    return;   
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_accelerometer_impl_IOSAccelerometerService_stopObserver
(JNIEnv *env, jclass jClass)
{
    [_accelerometer stopObserver];
    return;   
}

void sendAcceleration(CMAccelerometerData  *accelerometerData) {
    double x = accelerometerData.acceleration.x * g;
    double y = accelerometerData.acceleration.y * g;
    double z = accelerometerData.acceleration.z * g;

    if (filterGravity) {
        // filter to remove gravity
        gravity[0] = alpha * gravity[0] + (1 - alpha) * x;
        gravity[1] = alpha * gravity[1] + (1 - alpha) * y;
        gravity[2] = alpha * gravity[2] + (1 - alpha) * z;

        x -= gravity[0];
        y -= gravity[1];
        z -= gravity[2];
    }
    double t = (accelerometerData.timestamp + offset) * 1000;
    (*env)->CallStaticVoidMethod(env, mat_jAccelerometerServiceClass, mat_jAccelerometerService_notifyAcceleration, x, y, z, t);
}

@implementation Accelerometer 

- (void) startObserver
{   

    if (!self.motionManager) {
        self.motionManager = [[CMMotionManager alloc] init];
    }

    if (self.motionManager.accelerometerAvailable)
    {
        self.motionManager.accelerometerUpdateInterval = rate; // in seconds
        [self.motionManager startAccelerometerUpdatesToQueue:[NSOperationQueue mainQueue]
                                     withHandler:^(CMAccelerometerData *accelerometerData, NSError *error) {
            sendAcceleration(accelerometerData);
        }];
    } else
    {
        AttachLog(@"Error: No Accelerometer or Gyroscope Available");
    }

}

- (void) stopObserver 
{
    if (self.motionManager) 
    {
        [self.motionManager stopAccelerometerUpdates];
    }
}

@end

