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

#include "Orientation.h"

JNIEnv *env;

JNIEXPORT jint JNICALL
JNI_OnLoad_Orientation(JavaVM *vm, void *reserved)
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

static int OrientationInited = 0;

// Orientation
jclass mat_jOrientationServiceClass;
jmethodID mat_jOrientationService_notifyOrientation = 0;
Orientation *_orientation;


JNIEXPORT void JNICALL Java_com_gluonhq_attach_orientation_impl_IOSOrientationService_initOrientation
(JNIEnv *env, jclass jClass)
{
    if (OrientationInited)
    {
        return;
    }
    OrientationInited = 1;
    
    mat_jOrientationServiceClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/orientation/impl/IOSOrientationService"));
    mat_jOrientationService_notifyOrientation = (*env)->GetStaticMethodID(env, mat_jOrientationServiceClass, "notifyOrientation", "(Ljava/lang/String;)V");

    _orientation = [[Orientation alloc] init];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_orientation_impl_IOSOrientationService_startObserver
(JNIEnv *env, jclass jClass)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        [_orientation startObserver];
    });
    return;   
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_orientation_impl_IOSOrientationService_stopObserver
(JNIEnv *env, jclass jClass)
{
    [_orientation stopObserver];
    return;   
}

void sendOrientation() {
    NSString *orientation = [_orientation getOrientation];
    AttachLog(@"Orientation is %@", orientation);
    const char *orientationChars = [orientation UTF8String];
    jstring arg = (*env)->NewStringUTF(env, orientationChars);
    (*env)->CallStaticVoidMethod(env, mat_jOrientationServiceClass, mat_jOrientationService_notifyOrientation, arg);
    (*env)->DeleteLocalRef(env, arg);
}

@implementation Orientation 

- (void) startObserver 
{
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(OrientationDidChange:) name:UIDeviceOrientationDidChangeNotification object:nil];
    sendOrientation();
}

- (void) stopObserver 
{
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIDeviceOrientationDidChangeNotification object:nil];
}

- (NSString*) getOrientation
{
    UIDeviceOrientation orientation = [[UIDevice currentDevice] orientation];
    
    NSMutableString *value; 
    if(orientation == UIDeviceOrientationPortraitUpsideDown)
        value = [NSMutableString stringWithString: @"PortraitUpsideDown"];
    else if(orientation == UIInterfaceOrientationPortrait)
        value = [NSMutableString stringWithString: @"Portrait"];
    else if(orientation == UIInterfaceOrientationLandscapeLeft)
        value = [NSMutableString stringWithString: @"LandscapeLeft"];
    else if(orientation == UIInterfaceOrientationLandscapeRight)
        value = [NSMutableString stringWithString: @"LandscapeRight"];
    else 
        value = [NSMutableString stringWithString: @"Unknown"];
    return value;
}

-(void)OrientationDidChange:(NSNotification*)notification
{
    sendOrientation();
}

@end

