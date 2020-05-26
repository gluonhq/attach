/*
 * Copyright (c) 2020 Gluon
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

#include "Keyboard.h"

static JNIEnv *env;
static jclass jAttachKeyboardClass;
static jmethodID jAttachKeyboardMethod_notifyVisibleHeight = 0;

JNIEXPORT jint JNICALL
JNI_OnLoad_Keyboard(JavaVM *vm, void *reserved)
{
#ifdef JNI_VERSION_1_8
    //min. returned JNI_VERSION required by JDK8 for builtin libraries
    if ((*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_8) != JNI_OK) {
        AttachLog(@"Error initializing native Keyboard from OnLoad");
        return JNI_FALSE;
    }
    AttachLog(@"Initializing native Keyboard from OnLoad");
    jAttachKeyboardClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/keyboard/impl/IOSKeyboardService"));
    jAttachKeyboardMethod_notifyVisibleHeight = (*env)->GetStaticMethodID(env, jAttachKeyboardClass, "notifyVisibleHeight", "(F)V");
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

// Keyboard
Keyboard *_keyboard;
CGFloat currentKeyboardHeight = 0.0f;

JNIEXPORT void JNICALL Java_com_gluonhq_attach_keyboard_impl_IOSKeyboardService_startObserver
(JNIEnv *env, jclass jClass)
{
    if (!_keyboard) {
        _keyboard = [[Keyboard alloc] init];
    }

    dispatch_async(dispatch_get_main_queue(), ^{
        [_keyboard startObserver];
    });
    return;   
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_keyboard_impl_IOSKeyboardService_stopObserver
(JNIEnv *env, jclass jClass)
{
    if (!_keyboard) {
        _keyboard = [[Keyboard alloc] init];
    }

    [_keyboard stopObserver];
    return;   
}

void sendVisibleHeight() {
    (*env)->CallStaticVoidMethod(env, jAttachKeyboardClass, jAttachKeyboardMethod_notifyVisibleHeight, currentKeyboardHeight);
}

@implementation Keyboard 

- (void) startObserver 
{   
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillShow:)
        name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillHide:)
        name:UIKeyboardWillHideNotification object:nil];
}

- (void) stopObserver 
{
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:UIKeyboardWillHideNotification object:nil];
}

- (void)keyboardWillShow:(NSNotification*)notification {
    NSDictionary *info = [notification userInfo];
    CGSize kbSize = [[info objectForKey:UIKeyboardFrameEndUserInfoKey] CGRectValue].size;
    currentKeyboardHeight = kbSize.height;
    [self logMessage:@"Keyboard will show: %f",kbSize];
    sendVisibleHeight();
}

- (void)keyboardWillHide:(NSNotification*)notification {
    currentKeyboardHeight = 0.0f;
    [self logMessage:@"Keyboard will hide"];
    sendVisibleHeight();
}

- (void) logMessage:(NSString *)format, ...;
{
    if (debugAttach)
    {
        va_list args;
        va_start(args, format);
        NSLogv([@"[Attach Debug] " stringByAppendingString:format], args);
        va_end(args);
    }
}

@end

