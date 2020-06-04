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
#import <UIKit/UIKit.h>
#include "jni.h"
#include "AttachMacros.h"

JNIEnv *env;

JNIEXPORT jint JNICALL
JNI_OnLoad_StatusBar(JavaVM *vm, void *reserved)
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

JNIEXPORT void JNICALL Java_com_gluonhq_attach_statusbar_impl_IOSStatusBarService_setNativeColor
(JNIEnv *env, jclass jClass, jdouble red, jdouble green, jdouble blue, jdouble opacity)
{
    if (@available(iOS 13.0, *))
    {
        UIWindow* window = [UIApplication sharedApplication].keyWindow;
        if(!window)
        {
            AttachLog(@"key window was nil");
            return;
        }
        CGRect statusBarFrame = window.windowScene.statusBarManager.statusBarFrame;
        UIView *statusBarView = [window viewWithTag:123456];
        if (!statusBarView) {
            if (debugAttach) {
                AttachLog(@"Creating new statusBarView");
            }
            statusBarView = [[UIView alloc] initWithFrame:statusBarFrame];
            statusBarView.tag = 123456;
            [window addSubview:statusBarView];

            statusBarView.translatesAutoresizingMaskIntoConstraints = NO;
            [statusBarView.heightAnchor constraintEqualToConstant:statusBarFrame.size.height].active = YES;
            [statusBarView.widthAnchor constraintEqualToAnchor:window.widthAnchor multiplier:1.0].active = YES;
            [statusBarView.topAnchor constraintEqualToAnchor:window.topAnchor].active = YES;
            [statusBarView.centerXAnchor constraintEqualToAnchor:window.centerXAnchor].active = YES;
        }
        statusBarView.backgroundColor = [UIColor colorWithRed:red green:green blue:blue alpha:opacity];
    }
    else
    {
        UIView *statusBar = [[[UIApplication sharedApplication] valueForKey:@"statusBarWindow"] valueForKey:@"statusBar"];

        if ([statusBar respondsToSelector:@selector(setBackgroundColor:)]) {
            statusBar.backgroundColor = [UIColor colorWithRed:red green:green blue:blue alpha:opacity];
        }
    }
}