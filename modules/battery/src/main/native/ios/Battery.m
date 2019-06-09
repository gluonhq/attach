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

#include "Battery.h"

JNIEnv *env;

JNIEXPORT jint JNICALL
JNI_OnLoad_Battery(JavaVM *vm, void *reserved)
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

static int BatteryInited = 0;

// Battery
jclass mat_jBatteryServiceClass;
jmethodID mat_jBatteryService_notifyBatteryLevel = 0;
jmethodID mat_jBatteryService_notifyBatteryState = 0;
Battery *_battery;


JNIEXPORT void JNICALL Java_com_gluonhq_attach_battery_impl_IOSBatteryService_initBattery
(JNIEnv *env, jclass jClass)
{
    if (BatteryInited)
    {
        return;
    }
    BatteryInited = 1;
    
    mat_jBatteryServiceClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/battery/impl/IOSBatteryService"));
    mat_jBatteryService_notifyBatteryLevel = (*env)->GetMethodID(env, mat_jBatteryServiceClass, "notifyBatteryLevel", "(F)V");
    mat_jBatteryService_notifyBatteryState = (*env)->GetMethodID(env, mat_jBatteryServiceClass, "notifyBatteryState", "(Ljava/lang/String;)V");

    _battery = [[Battery alloc] init];
    
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_battery_impl_IOSBatteryService_startObserver
(JNIEnv *env, jclass jClass)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        [_battery startObserver];
    });
    return;   
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_battery_impl_IOSBatteryService_stopObserver
(JNIEnv *env, jclass jClass)
{
    [_battery stopObserver];
    return;   
}

void sendBatteryState() {
    NSString *battery = [_battery getBatteryState];
    const char *batteryChars = [battery UTF8String];
    jstring arg = (*env)->NewStringUTF(env, batteryChars);
        
    (*env)->CallVoidMethod(env, mat_jBatteryServiceClass, mat_jBatteryService_notifyBatteryState, arg);
    (*env)->DeleteLocalRef(env, arg);
}

void sendBatteryLevel() {
    float batteryLevel = [[UIDevice currentDevice] batteryLevel];
    (*env)->CallVoidMethod(env, mat_jBatteryServiceClass, mat_jBatteryService_notifyBatteryLevel, batteryLevel);
}

@implementation Battery 

- (void) startObserver 
{   
    [[UIDevice currentDevice] setBatteryMonitoringEnabled:YES];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(BatteryStateDidChange:) name:UIDeviceBatteryStateDidChangeNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(BatteryLevelDidChange:) name:UIDeviceBatteryLevelDidChangeNotification object:nil];
    sendBatteryState();
    sendBatteryLevel();
}

- (void) stopObserver 
{
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIDeviceBatteryStateDidChangeNotification object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIDeviceBatteryLevelDidChangeNotification object:nil];
    [[UIDevice currentDevice] setBatteryMonitoringEnabled:NO];
}

- (NSString*) getBatteryState
{
    int state = [[UIDevice currentDevice] batteryState];
    
    NSMutableString *value; 
    if(state == UIDeviceBatteryStateUnknown)
        value = [NSMutableString stringWithString: @"Unknown"];
    else if(state == UIDeviceBatteryStateUnplugged)
        value = [NSMutableString stringWithString: @"Unplugged"];
    else if(state == UIDeviceBatteryStateCharging)
        value = [NSMutableString stringWithString: @"Charging"];
    else if(state == UIDeviceBatteryStateFull)
        value = [NSMutableString stringWithString: @"Full"];
    else 
        value = [NSMutableString stringWithString: @"Unknown"];
    return value;
}

-(void)BatteryStateDidChange:(NSNotification*)notification
{
    sendBatteryState();
}

-(void)BatteryLevelDidChange:(NSNotification*)notification
{
    sendBatteryLevel();
}

@end

