/*
 * Copyright (c) 2016, 2020 Gluon
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

static JavaVM* graalVM;
static JNIEnv *graalEnv;

// Graal handles
static jclass jGraalLifecycleClass;
static jmethodID jGraalSetLifecycleEventMethod;

static void initializeGraalHandles(JNIEnv* env) {
    jGraalLifecycleClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/lifecycle/impl/AndroidLifecycleService"));
    jGraalSetLifecycleEventMethod = (*env)->GetStaticMethodID(env, jGraalLifecycleClass, "setEvent", "(Ljava/lang/String;)V");
}

static void initializeDalvikHandles() {
    androidVM = substrateGetAndroidVM();
    jclass activityClass = substrateGetActivityClass();
    jobject jActivity = substrateGetActivity();
    jclass jLifecycleServiceClass = substrateGetLifecycleServiceClass();

    if ((*androidVM)->GetEnv(androidVM, (void **)&androidEnv, JNI_VERSION_1_6) != JNI_OK) {
        ATTACH_LOG_FINE("initializeDalvikHandles, thread is not linked to JNIEnv, doing that now.\n");
        (*androidVM)->AttachCurrentThread(androidVM, (void **)&androidEnv, NULL);
    }
    jmethodID jLifecycleServiceInitMethod = (*androidEnv)->GetMethodID(androidEnv, jLifecycleServiceClass, "<init>", "()V");
    jthrowable t = (*androidEnv)->ExceptionOccurred(androidEnv);
    if (t) {
        ATTACH_LOG_INFO("EXCEPTION occurred when dealing with dalvik handles\n");
        (*androidEnv)->ExceptionClear(androidEnv);
    }

    jobject jObj = (*androidEnv)->NewObject(androidEnv, jLifecycleServiceClass, jLifecycleServiceInitMethod);
    jobject jDalvikLifecycleService = (jobject)(*androidEnv)->NewGlobalRef(androidEnv, jObj);
}

//////////////////////////
// From Graal to native //
//////////////////////////

JNIEXPORT jint JNICALL
JNI_OnLoad_Lifecycle(JavaVM *vm, void *reserved)
{
#ifdef JNI_VERSION_1_8
    graalVM = vm;
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native Lifecycle from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("Initializing native Lifecycle from OnLoad");
    initializeGraalHandles(graalEnv);
    initializeDalvikHandles();
    ATTACH_LOG_FINE("Initializing native Lifecycle from OnLoad Done");
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

///////////////////////////
// From Dalvik to native //
///////////////////////////

// the JNIEnv passed here is the Dalvik JNIEnv, do not use it to call into GraalVM!
JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_DalvikLifecycleService_setLifecycleEventNative(JNIEnv *env, jobject service, jstring jevent) {
    const char *chars = (*env)->GetStringUTFChars(env, jevent, NULL);
    ATTACH_LOG_FINE("Native layer got new event: %s\n", chars);
    (*graalVM)->AttachCurrentThread(graalVM, (void **)&graalEnv, NULL);
    jstring jchars = (*graalEnv)->NewStringUTF(graalEnv, chars);
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalLifecycleClass, jGraalSetLifecycleEventMethod, jchars);
    (*env)->ReleaseStringUTFChars(env, jevent, chars);
    (*graalEnv)->DeleteLocalRef(graalEnv, jchars);
}