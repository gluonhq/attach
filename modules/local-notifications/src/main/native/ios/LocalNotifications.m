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

#include "LocalNotifications.h"

JNIEnv *env;

JNIEXPORT jint JNICALL
JNI_OnLoad_LocalNotifications(JavaVM *vm, void *reserved)
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

static int notificationsInited = 0;

// Notifications

JNIEXPORT void JNICALL Java_com_gluonhq_attach_localnotifications_impl_IOSLocalNotificationsService_initLocalNotification
(JNIEnv *env, jclass jClass)
{
    if (notificationsInited)
    {
        return;
    }
    notificationsInited = 1;
    
    if (@available(iOS 10.0, *))
    {
        AttachLog(@"Initialize UNUserNotificationCenter iOS 10+");
        UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
        UNAuthorizationOptions options = UNAuthorizationOptionAlert + UNAuthorizationOptionBadge + UNAuthorizationOptionSound;
        [center requestAuthorizationWithOptions:options completionHandler:^(BOOL granted, NSError * _Nullable error) {
            if (!granted) {
                AttachLog(@"Error granting notification options");
            }
        }];

        [center getNotificationSettingsWithCompletionHandler:^(UNNotificationSettings *settings) {
            // Authorization status of the UNNotificationSettings object
            switch (settings.authorizationStatus) {
            case UNAuthorizationStatusAuthorized:
                AttachLog(@"UNNotificationSettings Status Authorized");
                break;
            case UNAuthorizationStatusDenied:
                AttachLog(@"UNNotificationSettings Status Denied");
                break;
            case UNAuthorizationStatusNotDetermined:
                AttachLog(@"UNNotificationSettings Status Undetermined");
                break;
            default:
                break;
            }

            // Status of specific settings
            if (settings.alertSetting != UNAuthorizationStatusAuthorized) {
                AttachLog(@"Alert settings not authorized");
            }

            if (settings.badgeSetting != UNAuthorizationStatusAuthorized) {
                AttachLog(@"Badge settings not authorized");
            }

            if (settings.soundSetting != UNAuthorizationStatusAuthorized) {
                AttachLog(@"Sound settings not authorized");
            }
        }];
    }
    else
    {
        #pragma clang diagnostic push
        #pragma clang diagnostic ignored "-Wdeprecated-declarations"

        AttachLog(@"Initialize UIUserNotificationSettings iOS 10-");
        UIUserNotificationSettings* settings = [UIUserNotificationSettings settingsForTypes:UIUserNotificationTypeAlert|UIUserNotificationTypeBadge|UIUserNotificationTypeSound categories:nil];
        [[UIApplication sharedApplication] registerUserNotificationSettings: settings];
        #pragma clang diagnostic pop
    }
    
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_localnotifications_impl_IOSLocalNotificationsService_registerNotification
(JNIEnv *env, jobject obj, jstring jTitle, jstring jText, jstring jIdentifier, jdouble seconds)
{
    if (debugAttach) {
        AttachLog(@"Register notification");
    }
    const jchar *charsTitle = (*env)->GetStringChars(env, jTitle, NULL);
    NSString *name = [NSString stringWithCharacters:(UniChar *)charsTitle length:(*env)->GetStringLength(env, jTitle)];
    (*env)->ReleaseStringChars(env, jTitle, charsTitle);
    const jchar *charsText = (*env)->GetStringChars(env, jText, NULL);
    NSString *text = [NSString stringWithCharacters:(UniChar *)charsText length:(*env)->GetStringLength(env, jText)];
    (*env)->ReleaseStringChars(env, jText, charsText);
    const jchar *charsIdentifier = (*env)->GetStringChars(env, jIdentifier, NULL);
    NSString *identifier = [NSString stringWithCharacters:(UniChar *)charsIdentifier length:(*env)->GetStringLength(env, jIdentifier)];
    (*env)->ReleaseStringChars(env, jIdentifier, charsIdentifier);

    if (@available(iOS 10.0, *))
    {
        UNMutableNotificationContent *content = [UNMutableNotificationContent new];
        content.title = name;
        content.body = text;
        content.sound = [UNNotificationSound defaultSound];
        content.userInfo = [NSDictionary dictionaryWithObjectsAndKeys:identifier, @"userId", nil];

        NSDate *date = [NSDate dateWithTimeIntervalSince1970:seconds];
        NSDateComponents *triggerDate = [[NSCalendar currentCalendar]
                      components:NSCalendarUnitYear +
                      NSCalendarUnitMonth + NSCalendarUnitDay +
                      NSCalendarUnitHour + NSCalendarUnitMinute +
                      NSCalendarUnitSecond fromDate:date];
        UNCalendarNotificationTrigger *trigger = [UNCalendarNotificationTrigger triggerWithDateMatchingComponents:triggerDate
                                                 repeats:NO];
        UNNotificationRequest *request = [UNNotificationRequest requestWithIdentifier:identifier
                                           content:content trigger:trigger];

        UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
        [center addNotificationRequest:request withCompletionHandler:^(NSError * _Nullable error) {
             if (error != nil) {
                 AttachLog(@"Something went wrong scheduling local notification: %@",error);
             } else if (debugAttach) {
                 AttachLog(@"done register notifications for %@ with identifier %@", name, identifier);
             }
        }];
    }
    else
    {
       #pragma clang diagnostic push
       #pragma clang diagnostic ignored "-Wdeprecated-declarations"

        UILocalNotification* localNotification = [[UILocalNotification alloc] init];
        localNotification.fireDate = [NSDate dateWithTimeIntervalSince1970:seconds];
        // Not supported by iOS 8.1
        // localNotification.alertTitle = name;
        if ([name length] == 0) {
            localNotification.alertBody = text;
        } else {
            localNotification.alertBody = [name stringByAppendingFormat:@"%@%@",@"\n",text];
        }
        localNotification.soundName = UILocalNotificationDefaultSoundName;
        localNotification.userInfo = [NSDictionary dictionaryWithObjectsAndKeys:identifier, @"userId", nil];
        localNotification.timeZone = [NSTimeZone defaultTimeZone];
        localNotification.category = @"sessionReminderCategory";
        [[UIApplication sharedApplication] scheduleLocalNotification: localNotification];
        if (debugAttach) {
            AttachLog(@"done register notifications for %@ with identifier %@", name, identifier);
        }
       #pragma clang diagnostic pop
    }
    return;
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_localnotifications_impl_IOSLocalNotificationsService_unregisterNotification
(JNIEnv *env, jclass jClass, jstring jIdentifier)
{
    const jchar *charsIdentifier = (*env)->GetStringChars(env, jIdentifier, NULL);
    NSString *identifier = [NSString stringWithCharacters:(UniChar *)charsIdentifier length:(*env)->GetStringLength(env, jIdentifier)];
    (*env)->ReleaseStringChars(env, jIdentifier, charsIdentifier);

    if (@available(iOS 10.0, *))
    {
        UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
        NSArray *array = [NSArray arrayWithObjects:identifier, nil];
        [center removePendingNotificationRequestsWithIdentifiers:array];
        if (debugAttach) {
            AttachLog(@"We did remove the notification with id: %@", identifier);
        }
    } else {
        #pragma clang diagnostic push
        #pragma clang diagnostic ignored "-Wdeprecated-declarations"

        NSArray *nots = [[UIApplication sharedApplication] scheduledLocalNotifications];
        for (int i=0; i<[nots count]; i++) {
            UILocalNotification* candidate = [nots objectAtIndex:i];
            NSDictionary *myUserInfo = candidate.userInfo;
            NSString *myId = [myUserInfo objectForKey:@"userId"];
            if ([myId isEqualToString:identifier]) {
                [[UIApplication sharedApplication] cancelLocalNotification:candidate];
                if (debugAttach) {
                    AttachLog(@"We did remove the notification with id: %@", identifier);
                }
            }
        }
        #pragma clang diagnostic pop
    }
}

@implementation LocalNotifications

- (void)userNotificationCenter:(UNUserNotificationCenter *)center didReceiveNotificationResponse:(UNNotificationResponse *)response withCompletionHandler:(void (^)(void))completionHandler
{
    // called when a user selects an action in a delivered notification
    [self logMessage:@"didReceiveNotificationResponse %@", response];
    completionHandler();
}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center willPresentNotification:(UNNotification *)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions options))completionHandler
{
   // called when a notification is delivered to a foreground app
   [self logMessage:@"willPresentNotification %@", notification];
   completionHandler(UNNotificationPresentationOptionAlert + UNNotificationPresentationOptionSound);
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
