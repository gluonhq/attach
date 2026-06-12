/*
 * Copyright (c) 2016, 2026, Gluon
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
#import <UIKit/UIKit.h>
#import <AuthenticationServices/AuthenticationServices.h>
#include "jni.h"
#include "AttachMacros.h"

JNIEnv *env;

static int BrowserInited = 0;
jclass mat_jBrowserServiceClass;
jmethodID mat_jBrowserService_setAuthResult = 0;

API_AVAILABLE(ios(12.0))
static ASWebAuthenticationSession *_authSession;

API_AVAILABLE(ios(13.0))
@interface AttachAuthContextProvider : NSObject <ASWebAuthenticationPresentationContextProviding>
@end

API_AVAILABLE(ios(13.0))
@implementation AttachAuthContextProvider
- (ASPresentationAnchor)presentationAnchorForWebAuthenticationSession:(ASWebAuthenticationSession *)session {
    return [[UIApplication sharedApplication] keyWindow];
}
@end

API_AVAILABLE(ios(13.0))
static AttachAuthContextProvider *_authContextProvider;

JNIEXPORT jint JNICALL
JNI_OnLoad_Browser(JavaVM *vm, void *reserved)
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

JNIEXPORT void JNICALL Java_com_gluonhq_attach_browser_impl_IOSBrowserService_initBrowser
(JNIEnv *env, jclass jClass)
{
    if (BrowserInited)
    {
        return;
    }
    BrowserInited = 1;

    mat_jBrowserServiceClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/browser/impl/IOSBrowserService"));
    mat_jBrowserService_setAuthResult = (*env)->GetStaticMethodID(env, mat_jBrowserServiceClass, "setAuthResult", "(Ljava/lang/String;)V");
}

void sendAuthResult(NSString *callbackUrl) {
    jstring arg = NULL;
    if (callbackUrl != nil) {
        const char *callbackChars = [callbackUrl UTF8String];
        arg = (*env)->NewStringUTF(env, callbackChars);
    }
    (*env)->CallStaticVoidMethod(env, mat_jBrowserServiceClass, mat_jBrowserService_setAuthResult, arg);
    if (arg != NULL) {
        (*env)->DeleteLocalRef(env, arg);
    }
    AttachLog(@"Finished sending web authentication result");
}

JNIEXPORT jboolean JNICALL Java_com_gluonhq_attach_browser_impl_IOSBrowserService_launchURL
(JNIEnv *env, jclass jClass, jstring jUrl)
{
    const jchar *chars = (*env)->GetStringChars(env, jUrl, NULL);
    NSString *url = [NSString stringWithCharacters:(UniChar *)chars length:(*env)->GetStringLength(env, jUrl)];
    (*env)->ReleaseStringChars(env, jUrl, chars);

    NSURL *nsUrl = [NSURL URLWithString:url];
    if ([[UIApplication sharedApplication] canOpenURL:nsUrl]) {
        if (@available(iOS 10.0, *))
        {
            [[UIApplication sharedApplication] openURL:nsUrl options:@{}
                completionHandler:^(BOOL success) {
                     if (success) {
                          AttachLog(@"Opened url successfully");
                     }
                 }];
        }
        else {
            #pragma clang diagnostic push
            #pragma clang diagnostic ignored "-Wdeprecated-declarations"

            [[UIApplication sharedApplication] openURL:nsUrl];

            #pragma clang diagnostic pop
        }
        AttachLog(@"Done opening url %@", url);
        return JNI_TRUE;
    } else {
        AttachLog(@"Can't open url %@", url);
        return JNI_FALSE;
    }
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_browser_impl_IOSBrowserService_startWebAuthentication
(JNIEnv *env, jclass jClass, jstring jUrl, jstring jScheme)
{
    const jchar *charsUrl = (*env)->GetStringChars(env, jUrl, NULL);
    NSString *url = [NSString stringWithCharacters:(UniChar *)charsUrl length:(*env)->GetStringLength(env, jUrl)];
    (*env)->ReleaseStringChars(env, jUrl, charsUrl);

    const jchar *charsScheme = (*env)->GetStringChars(env, jScheme, NULL);
    NSString *scheme = [NSString stringWithCharacters:(UniChar *)charsScheme length:(*env)->GetStringLength(env, jScheme)];
    (*env)->ReleaseStringChars(env, jScheme, charsScheme);

    NSURL *nsUrl = [NSURL URLWithString:url];
    if (nsUrl == nil) {
        AttachLog(@"Invalid authentication URL: %@", url);
        sendAuthResult(nil);
        return;
    }

    if (@available(iOS 12.0, *)) {
        void (^completionHandler)(NSURL * _Nullable, NSError * _Nullable) =
                ^(NSURL * _Nullable callbackURL, NSError * _Nullable error) {
                    if (error != nil || callbackURL == nil) {
                        AttachLog(@"Web authentication finished without a callback url: %@", error);
                        sendAuthResult(nil);
                    } else {
                        AttachLog(@"Web authentication succeeded with callback url: %@", callbackURL);
                        sendAuthResult([callbackURL absoluteString]);
                    }
                    _authSession = nil;
                };

        BOOL isHttps = [[scheme lowercaseString] hasPrefix:@"https"];
        if (isHttps) {
            // Verified Universal Link (https://example.com/callback): requires iOS 17.4+ and an apple-app-site-association
            // file associating the domain with the app.
            if (@available(iOS 17.4, *)) {
                NSURLComponents *components = [NSURLComponents componentsWithString:scheme];
                NSString *host = components.host;
                NSString *path = (components.path != nil && [components.path length] > 0) ? components.path : @"/";
                if (host == nil || [host length] == 0) {
                    AttachLog(@"Invalid https callback, a host is required: %@", scheme);
                    sendAuthResult(nil);
                    return;
                }
                ASWebAuthenticationSessionCallback *httpsCallback =
                        [ASWebAuthenticationSessionCallback callbackWithHTTPSHost:host path:path];
                _authSession = [[ASWebAuthenticationSession alloc] initWithURL:nsUrl
                        callback:httpsCallback completionHandler:completionHandler];
            } else {
                AttachLog(@"https callback URLs require iOS 17.4 or higher");
                sendAuthResult(nil);
                return;
            }
        } else {
            // Custom scheme callback ("myapp")
            _authSession = [[ASWebAuthenticationSession alloc] initWithURL:nsUrl
                    callbackURLScheme:scheme completionHandler:completionHandler];
        }

        if (@available(iOS 13.0, *)) {
            _authContextProvider = [[AttachAuthContextProvider alloc] init];
            _authSession.presentationContextProvider = _authContextProvider;
            _authSession.prefersEphemeralWebBrowserSession = NO;
        }

        dispatch_async(dispatch_get_main_queue(), ^{
            if (![_authSession start]) {
                AttachLog(@"Failed to start the web authentication session");
                sendAuthResult(nil);
                _authSession = nil;
            }
        });
    } else {
        AttachLog(@"ASWebAuthenticationSession requires iOS 12.0 or higher");
        sendAuthResult(nil);
    }
}
