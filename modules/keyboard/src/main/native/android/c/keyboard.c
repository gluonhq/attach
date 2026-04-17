/*
 * Copyright (c) 2020, 2026, Gluon
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
#include "util.h"

static jclass jKeyboardServiceClass;
static jclass jAttachKeyboardClass;
static jclass jActivityClass;
static jmethodID jAttach_notifyHeightMethod;
static jmethodID jAttach_notifyComposingTextMethod;
static jmethodID jActivity_setKeyboardTypeMethod;
static jmethodID jActivity_setActiveNodeIdMethod;

void initKeyboard();
static jfloat density;
jfloat android_getDensity(JNIEnv *env);


JNIEXPORT jint JNICALL
JNI_OnLoad_keyboard(JavaVM *vm, void *reserved)
{
#ifdef JNI_VERSION_1_8
    //min. returned JNI_VERSION required by JDK8 for builtin libraries
    JNIEnv* env;
    if ((*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native Keyboard from OnLoad");
        return JNI_FALSE;
    }
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
    jActivityClass = substrateGetActivityClass();
    jobject jActivity = substrateGetActivity();
    jKeyboardServiceClass = GET_REGISTER_DALVIK_CLASS(jKeyboardServiceClass, "com/gluonhq/helloandroid/KeyboardService");

    ATTACH_DALVIK();
    jmethodID jKeyboardServiceInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jKeyboardServiceClass, "<init>", "(Landroid/app/Activity;)V");
    jobject keyboardservice = (*dalvikEnv)->NewObject(dalvikEnv, jKeyboardServiceClass, jKeyboardServiceInitMethod, jActivity);
    jActivity_setKeyboardTypeMethod = (*dalvikEnv)->GetStaticMethodID(dalvikEnv, jActivityClass, "setKeyboardType", "(I)V");
    jActivity_setActiveNodeIdMethod = (*dalvikEnv)->GetStaticMethodID(dalvikEnv, jActivityClass, "setActiveNodeId", "(Ljava/lang/String;)V");
    density = android_getDensity(dalvikEnv);
    DETACH_DALVIK();

    if (density == 0.0f) {
         density = 1.0f;
    }
    ATTACH_LOG_FINE("Dalvik KeyboardService init was called");
}

// from Java to Android

JNIEXPORT void JNICALL Java_com_gluonhq_attach_keyboard_impl_AndroidKeyboardService_nativeSetKeyboardType(JNIEnv *env, jclass cls, jint keyboardTypeValue)
{
    ATTACH_LOG_FINE("nativeSetKeyboardType: keyboardTypeValue = %d", keyboardTypeValue);
    ATTACH_DALVIK();
    (*dalvikEnv)->CallStaticVoidMethod(dalvikEnv, jActivityClass, jActivity_setKeyboardTypeMethod, keyboardTypeValue);
    DETACH_DALVIK();
    ATTACH_LOG_FINE("nativeSetKeyboardType done");
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_keyboard_impl_AndroidKeyboardService_nativeSetActiveNodeId(JNIEnv *env, jclass cls, jstring id)
{
    const char *idChars = (*env)->GetStringUTFChars(env, id, NULL);
    ATTACH_LOG_FINE("nativeSetActiveNodeId: id = %s", idChars);
    ATTACH_DALVIK();
    jstring dalvikId = (*dalvikEnv)->NewStringUTF(dalvikEnv, idChars);
    (*dalvikEnv)->CallStaticVoidMethod(dalvikEnv, jActivityClass, jActivity_setActiveNodeIdMethod, dalvikId);
    (*dalvikEnv)->DeleteLocalRef(dalvikEnv, dalvikId);
    DETACH_DALVIK();
    (*env)->ReleaseStringUTFChars(env, id, idChars);
    ATTACH_LOG_FINE("nativeSetActiveNodeId done");
}

///////////////////////////
// From Dalvik to native //
///////////////////////////

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_KeyboardService_nativeDispatchKeyboardHeight(JNIEnv *env, jobject activity, jfloat jheight)
{
    ATTACH_LOG_FINE("Dispatching keyboard height from native Dalvik layer: %.3f", jheight / density);
    ATTACH_GRAAL();
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jAttachKeyboardClass, jAttach_notifyHeightMethod, jheight / density);
    DETACH_GRAAL();
    ATTACH_LOG_FINE("called Attach method from native Keyboard done");
}
