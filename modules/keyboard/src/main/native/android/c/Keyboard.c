/*
 * Copyright (c) 2020, Gluon
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
JNIEnv* javaEnvKeyboard = NULL;
static JavaVM *graalVM = NULL;
static jclass jAttachKeyboardClass;
static jmethodID jAttach_notifyHeightMethod;
int debugKeyboard = 0;
void initKeyboard();

JNIEXPORT jint JNICALL
JNI_OnLoad_Keyboard(JavaVM *vm, void *reserved)
{
#ifdef JNI_VERSION_1_8
    //min. returned JNI_VERSION required by JDK8 for builtin libraries
    if ((*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native Keyboard from OnLoad");
        return JNI_FALSE;
    }
    graalVM = getGraalVM();
    ATTACH_LOG_FINE("Initializing native Keyboard from OnLoad");
    jAttachKeyboardClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/keyboard/impl/AndroidKeyboardService"));
    jAttach_notifyHeightMethod = (*env)->GetStaticMethodID(env, jAttachKeyboardClass, "notifyVisibleHeight", "(F)V");
    initKeyboard();
    ATTACH_LOG_FINE("Initializing native Keyboard done");
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

static int KeyboardInited = 0;

void initKeyboard()
{
    if (KeyboardInited)
    {
        return;
    }
    KeyboardInited = 1;

    ATTACH_LOG_FINE("Init AndroidKeyboardService");
    JavaVM* androidVM = substrateGetAndroidVM();
    jclass activityClass = substrateGetActivityClass();
    jobject jActivity = substrateGetActivity();
    jclass jKeyboardServiceClass = substrateGetKeyboardServiceClass();

    JNIEnv* androidEnv;
    (*androidVM)->AttachCurrentThread(androidVM, (void **)&androidEnv, NULL);
    jmethodID jKeyboardServiceInitMethod = (*androidEnv)->GetMethodID(androidEnv, jKeyboardServiceClass, "<init>", "(Landroid/app/Activity;)V");
    jobject keyboardservice = (*androidEnv)->NewObject(androidEnv, jKeyboardServiceClass, jKeyboardServiceInitMethod, jActivity);
    (*androidVM)->DetachCurrentThread(androidVM);

    density = android_getDensity(env);
    if (density == 0.0f) {
         density = 1.0f;
    }
    ATTACH_LOG_FINE("Dalvik KeyboardService init was called");
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_keyboard_impl_AndroidKeyboardService_enableDebug
(JNIEnv *env, jclass jClass)
{
    debugKeyboard = 1;
}

void initializeKeyboardFromNative() {
    if (javaEnvKeyboard != NULL) {
        return; // already have a JNIEnv
    }
    if (graalVM == NULL) {
        ATTACH_LOG_FINE("initialize Keyboard from native can't be done without JVM");
        return; // can't initialize from native before we have a graalVM
    }
    if (debugKeyboard == 1) {
        ATTACH_LOG_FINE("Initializing native Keyboard from Android/native code");
    }
    jint error = (*graalVM)->AttachCurrentThread(graalVM, (void **)&javaEnvKeyboard, NULL);
    if (error != 0) {
        ATTACH_LOG_FINE("initializeKeyboardFromNative failed with error %d", error);
    }
}

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_KeyboardService_nativeDispatchKeyboardHeight(JNIEnv *env, jobject activity, jfloat jheight)
{
    if (debugKeyboard == 1) {
        ATTACH_LOG_FINE("Dispatching keyboard height from native Dalvik layer: %.3f", jheight / density);
    }
    initializeKeyboardFromNative();
    if (javaEnvKeyboard == NULL) {
        ATTACH_LOG_FINE("javaEnvKeyboard still null, not ready to process keyboard events");
        return;
    }
    (*javaEnvKeyboard)->CallStaticVoidMethod(javaEnvKeyboard, jAttachKeyboardClass, jAttach_notifyHeightMethod, jheight / density);
    if (debugKeyboard == 1) {
        ATTACH_LOG_FINE("called Attach method from native Keyboard done");
    }
}
