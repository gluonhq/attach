/*
 * Copyright (c) 2022 Gluon
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
#import <StoreKit/StoreKit.h>
#include "AttachMacros.h"
#include "jni.h"

JNIEnv *env;

JNIEXPORT jint JNICALL
JNI_OnLoad_StoreReview(JavaVM *vm, void *reserved)
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


JNIEXPORT void JNICALL Java_com_gluonhq_attach_storereview_impl_IOSStoreReviewService_nativeRequestStoreReview
(JNIEnv *env, jclass jClass, jclass jUrl) {

    NSUInteger windowCount = [UIApplication sharedApplication].windows.count;
    // Note: It can be used only three times a year for the same app and version
    if (@available(iOS 14.0, *)) {
        UIWindowScene *activeScene;
        for (UIScene *scene in [[UIApplication sharedApplication] connectedScenes]) {
            if ([scene activationState] == UISceneActivationStateForegroundActive) {
                activeScene = (UIWindowScene *)scene;
                break;
            }
        }
        if (activeScene != nil) {
            if (debugAttach) {
                AttachLog(@"Calling requestReview for scene");
            }
            [SKStoreReviewController requestReviewInScene:activeScene];
        } else {
            AttachLog(@"Error calling requestReview for scene");
        }
    } else if (@available(iOS 10.3, *)) {
         if (debugAttach) {
             AttachLog(@"Calling requestReview");
         }
         [SKStoreReviewController requestReview];
    }
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 2 * NSEC_PER_SEC), dispatch_get_main_queue(), ^{
        if (windowCount == [UIApplication sharedApplication].windows.count) {
            // there was no popup, fallback to manual review process: go to app store to leave review
            const jchar *charsUrl = (*env)->GetStringChars(env, jUrl, NULL);
            NSString *stringUrl = [NSString stringWithCharacters:(UniChar *)charsUrl length:(*env)->GetStringLength(env, jUrl)];
            (*env)->ReleaseStringChars(env, jUrl, charsUrl);

            NSURL *reviewUrl = [NSURL URLWithString:stringUrl];
            if ([[UIApplication sharedApplication] canOpenURL:reviewUrl]) {
                if (debugAttach) {
                    AttachLog(@"Fallback to url %@", reviewUrl);
                }
                [[UIApplication sharedApplication] openURL:reviewUrl options:@{}
                     completionHandler:^(BOOL success) {
                          if (!success) {
                               AttachLog(@"Error opening url %@", reviewUrl);
                          }
                      }];
            } else {
                AttachLog(@"Error opening fallback url %@", reviewUrl);
            }
        }
    });
}