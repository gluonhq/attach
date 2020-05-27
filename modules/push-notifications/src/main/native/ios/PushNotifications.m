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

#include "PushNotifications.h"

JNIEnv *env;

JNIEXPORT jint JNICALL
JNI_OnLoad_PushNotifications(JavaVM *vm, void *reserved)
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

static int notificationsInitied = 0;

// Push Notifications

jclass mat_jPushNotificationsClass;
jmethodID mat_failToRegisterForRemoteNotifications = 0;
jmethodID mat_didRegisterForRemoteNotifications = 0;

JNIEXPORT void JNICALL Java_com_gluonhq_attach_pushnotifications_impl_IOSPushNotificationsService_initPushNotifications
(JNIEnv *env, jclass jClass)
{
    if (notificationsInitied)
    {
        return;
    }
    notificationsInitied = 1;
    
    // Push Notifications
    mat_jPushNotificationsClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/pushnotifications/impl/IOSPushNotificationsService"));
    mat_failToRegisterForRemoteNotifications = (*env)->GetStaticMethodID(env, mat_jPushNotificationsClass, "failToRegisterForRemoteNotifications", "(Ljava/lang/String;)V");
    mat_didRegisterForRemoteNotifications = (*env)->GetStaticMethodID(env, mat_jPushNotificationsClass, "didRegisterForRemoteNotifications", "(Ljava/lang/String;)V");

    if (@available(iOS 10.0, *))
    {
        if (debugAttach) {
            AttachLog(@"Initialize UIUserNotificationSettings - Push >= 10");
        }
        UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
        [center requestAuthorizationWithOptions:(UNAuthorizationOptionSound | UNAuthorizationOptionAlert | UNAuthorizationOptionBadge) completionHandler:^(BOOL granted, NSError * _Nullable error){
            if (!error) 
            {
                if (debugAttach) {
                    AttachLog(@"Registering notifications");
                }
                [[UIApplication sharedApplication] registerForRemoteNotifications];
            } 
            else 
            {
                AttachLog(@"Registering notifications failed with error %@", [error localizedDescription]);
            }
        }]; 
    } 
    else 
    {
        #pragma clang diagnostic push
        #pragma clang diagnostic ignored "-Wdeprecated-declarations"

        if (debugAttach) {
            AttachLog(@"Initialize UIUserNotificationSettings - Push < 10");
        }
        UIUserNotificationSettings* settings = [UIUserNotificationSettings settingsForTypes:UIUserNotificationTypeAlert|UIUserNotificationTypeBadge|UIUserNotificationTypeSound categories:nil];
        [[UIApplication sharedApplication] registerUserNotificationSettings: settings];
        [[UIApplication sharedApplication] registerForRemoteNotifications];

        #pragma clang diagnostic pop
    }
}

@implementation GlassApplication (NotificationsAdditions)

- (void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken
{
    NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
    {

#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 130000
        NSString *deviceTokenString = [self stringFromDeviceToken:deviceToken];
#else
        NSString *deviceTokenString = [[[[deviceToken description] stringByReplacingOccurrencesOfString:@"<"withString:@""]
                        stringByReplacingOccurrencesOfString:@">" withString:@""]
                        stringByReplacingOccurrencesOfString: @" " withString: @""];
#endif

        const char *deviceTokenChars = [deviceTokenString UTF8String];
        jstring argToken = (*env)->NewStringUTF(env, deviceTokenChars);

        [self logMessage:@"Sending token %@", deviceTokenString];
        (*env)->CallStaticVoidMethod(env, mat_jPushNotificationsClass, mat_didRegisterForRemoteNotifications, argToken);
        (*env)->DeleteLocalRef(env, argToken);
    }
    [pool drain];
}

- (void)application:(UIApplication *)application didFailToRegisterForRemoteNotificationsWithError:(NSError *)error
{
    AttachLog(@"Error registering remote notifications %@", [error localizedDescription]);
    NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
    {
        NSString *errorDescString = [error localizedDescription];
        const char *errorDescChars = [errorDescString UTF8String];
        jstring arg = (*env)->NewStringUTF(env, errorDescChars);
        [self logMessage:@"Sending error %@", errorDescString];
        (*env)->CallStaticVoidMethod(env, mat_jPushNotificationsClass, mat_failToRegisterForRemoteNotifications, arg);
        (*env)->DeleteLocalRef(env, arg);
    }
    [pool drain];
}

- (NSString *)stringFromDeviceToken:(NSData *)deviceToken {
    NSUInteger length = deviceToken.length;
    if (length == 0) {
        return nil;
    }
    const unsigned char *buffer = deviceToken.bytes;
    NSMutableString *hexString  = [NSMutableString stringWithCapacity:(length * 2)];
    for (int i = 0; i < length; ++i) {
        [hexString appendFormat:@"%02x", buffer[i]];
    }
    return [hexString copy];
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
