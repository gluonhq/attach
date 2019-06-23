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
#include "Lifecycle.h"

JNIEnv *env;

JNIEXPORT jint JNICALL
JNI_OnLoad_Lifecycle(JavaVM *vm, void *reserved)
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

static int lifecycleInited = 0;

// Lifecycle
jclass mat_jLifecycleServiceClass;
jmethodID mat_jLifecycleService_setEvent = 0;
Lifecycle *_lifecycle;


JNIEXPORT void JNICALL Java_com_gluonhq_attach_lifecycle_impl_IOSLifecycleService_initLifecycle
(JNIEnv *env, jclass jClass)
{
    if (lifecycleInited)
    {
        return;
    }
    lifecycleInited = 1;

    mat_jLifecycleServiceClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/lifecycle/impl/IOSLifecycleService"));
    mat_jLifecycleService_setEvent = (*env)->GetStaticMethodID(env, mat_jLifecycleServiceClass, "setEvent", "(Ljava/lang/String;)V");

    _lifecycle = [[Lifecycle alloc] init];
    [_lifecycle initEvents];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_lifecycle_impl_IOSLifecycleService_stopEvents
(JNIEnv *env, jclass jClass)
{
    [_lifecycle stopEvents];
    return;
}

void sendLifecycleEvent(NSString *event) {
    if (event)
    {
        const char *eventChars = [event UTF8String];
        jstring arg = (*env)->NewStringUTF(env, eventChars);
        (*env)->CallStaticVoidMethod(env, mat_jLifecycleServiceClass, mat_jLifecycleService_setEvent, arg);
        (*env)->DeleteLocalRef(env, arg);
    } else
    {
        (*env)->CallStaticVoidMethod(env, mat_jLifecycleServiceClass, mat_jLifecycleService_setEvent, NULL);
    }
}

@implementation Lifecycle

- (void)initEvents {
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(applicationDidBecomeActiveNotification:)
        name:UIApplicationDidBecomeActiveNotification object:[UIApplication sharedApplication]];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(applicationDidEnterBackgroundNotification:)
        name:UIApplicationDidEnterBackgroundNotification object:[UIApplication sharedApplication]];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(applicationWillTerminateNotification:)
        name:UIApplicationWillTerminateNotification object:[UIApplication sharedApplication]];
}

- (void)stopEvents {
    AttachLog(@"Unregistering sending event");
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIApplicationDidBecomeActiveNotification
        object:[UIApplication sharedApplication]];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIApplicationDidEnterBackgroundNotification
        object:[UIApplication sharedApplication]];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIApplicationWillTerminateNotification
        object:[UIApplication sharedApplication]];
}

- (void)applicationDidBecomeActiveNotification:(NSNotification *)notification {
    sendLifecycleEvent(@"resume");
}

- (void)applicationDidEnterBackgroundNotification:(NSNotification *)notification {
    sendLifecycleEvent(@"pause");
}

- (void)applicationWillTerminateNotification:(NSNotification *)notification {
    [self stopEvents];
}

@end