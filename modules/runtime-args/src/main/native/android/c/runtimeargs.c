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
#include "runtimeargs.h"

// Graal handles
static jclass jGraalRuntimeArgsClass;
static jmethodID jGraalRuntimeArgsProcessMethod;

static void initializeGraalHandles(JNIEnv* env) {
    jGraalRuntimeArgsClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/runtimeargs/impl/AndroidRuntimeArgsService"));
    jGraalRuntimeArgsProcessMethod = (*env)->GetStaticMethodID(env, jGraalRuntimeArgsClass, "processRuntimeArgs", "(Ljava/lang/String;Ljava/lang/String;)V");
}

static void initializeRuntimeArgsDalvikHandles() {
    jclass activityClass = substrateGetActivityClass();
    jclass jRuntimeArgsServiceClass = substrateGetRuntimeArgsServiceClass();

    ATTACH_DALVIK();
    jmethodID jRuntimeArgsServiceInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jRuntimeArgsServiceClass, "<init>", "(Landroid/app/Activity;)V");
    jthrowable t = (*dalvikEnv)->ExceptionOccurred(dalvikEnv);
    if (t) {
        ATTACH_LOG_INFO("EXCEPTION occurred when dealing with dalvik handles\n");
        (*dalvikEnv)->ExceptionClear(dalvikEnv);
    }

    jobject jActivity = substrateGetActivity();
    jobject jObj = (*dalvikEnv)->NewObject(dalvikEnv, jRuntimeArgsServiceClass, jRuntimeArgsServiceInitMethod, jActivity);
    jobject jDalvikRuntimeArgsService = (jobject)(*dalvikEnv)->NewGlobalRef(dalvikEnv, jObj);
    DETACH_DALVIK();
}

//////////////////////////
// From Graal to native //
//////////////////////////

JNIEXPORT jint JNICALL
JNI_OnLoad_runtimeargs(JavaVM *vm, void *reserved)
{
#ifdef JNI_VERSION_1_8
    JNIEnv* graalEnv;
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native RuntimeArgs from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("Initializing native RuntimeArgs from OnLoad");
    initializeGraalHandles(graalEnv);
    initializeRuntimeArgsDalvikHandles();
    ATTACH_LOG_FINE("Initializing native RuntimeArgs from OnLoad Done");
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

///////////////////////////
// From Dalvik to native //
///////////////////////////

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_LaunchURLActivity_processRuntimeArgs
    (JNIEnv *env, jobject service, jstring jkey, jstring jvalue) {
    const char *keyChars = (*env)->GetStringUTFChars(env, jkey, NULL);
    const char *valueChars = (*env)->GetStringUTFChars(env, jvalue, NULL);
    if (debugAttach) {
        ATTACH_LOG_FINE("RuntimeArgs:: Native layer got key: %s, value: %s", keyChars, valueChars);
    }
    ATTACH_GRAAL();
    jstring jKeyChars = (*graalEnv)->NewStringUTF(graalEnv, keyChars);
    jstring jValueChars = (*graalEnv)->NewStringUTF(graalEnv, valueChars);
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalRuntimeArgsClass, jGraalRuntimeArgsProcessMethod, jKeyChars, jValueChars);
    (*graalEnv)->DeleteLocalRef(graalEnv, jKeyChars);
    (*graalEnv)->DeleteLocalRef(graalEnv, jValueChars);
    DETACH_GRAAL();
    (*env)->ReleaseStringUTFChars(env, jkey, keyChars);
    (*env)->ReleaseStringUTFChars(env, jvalue, valueChars);
}
