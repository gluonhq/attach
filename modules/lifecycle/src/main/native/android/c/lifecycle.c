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
#include "lifecycle.h"

// Graal handles
static jclass jGraalLifecycleClass;
static jmethodID jGraalSetLifecycleEventMethod;

// Dalvik handles
static jmethodID jDalvikFinishMethod;

static void initializeGraalHandles(JNIEnv* env) {
    jGraalLifecycleClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/lifecycle/impl/AndroidLifecycleService"));
    jGraalSetLifecycleEventMethod = (*env)->GetStaticMethodID(env, jGraalLifecycleClass, "setEvent", "(Ljava/lang/String;)V");
}

static void initializeLifecycleDalvikHandles() {
    jclass activityClass = substrateGetActivityClass();
    jclass jLifecycleServiceClass = substrateGetLifecycleServiceClass();

    ATTACH_DALVIK();
    jmethodID jLifecycleServiceInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jLifecycleServiceClass, "<init>", "()V");
    jDalvikFinishMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, activityClass, "finish", "()V");
    jthrowable t = (*dalvikEnv)->ExceptionOccurred(dalvikEnv);
    if (t) {
        ATTACH_LOG_INFO("EXCEPTION occurred when dealing with dalvik handles\n");
        (*dalvikEnv)->ExceptionClear(dalvikEnv);
    }

    jobject jObj = (*dalvikEnv)->NewObject(dalvikEnv, jLifecycleServiceClass, jLifecycleServiceInitMethod);
    jobject jDalvikLifecycleService = (jobject)(*dalvikEnv)->NewGlobalRef(dalvikEnv, jObj);
    DETACH_DALVIK();
}

//////////////////////////
// From Graal to native //
//////////////////////////

JNIEXPORT jint JNICALL
JNI_OnLoad_lifecycle(JavaVM *vm, void *reserved)
{
#ifdef JNI_VERSION_1_8
    JNIEnv* graalEnv;
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native Lifecycle from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("Initializing native Lifecycle from OnLoad");
    initializeGraalHandles(graalEnv);
    initializeLifecycleDalvikHandles();
    ATTACH_LOG_FINE("Initializing native Lifecycle from OnLoad Done");
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

// from Java to Android

JNIEXPORT void JNICALL Java_com_gluonhq_attach_lifecycle_impl_AndroidLifecycleService_nativeShutdown
(JNIEnv *env, jclass jClass)
{
    jobject jActivity = substrateGetActivity();

    ATTACH_DALVIK();
    ATTACH_LOG_FINE("Finishing application");
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jActivity, jDalvikFinishMethod);
    DETACH_DALVIK();
}

///////////////////////////
// From Dalvik to native //
///////////////////////////

// the JNIEnv passed here is the Dalvik JNIEnv, do not use it to call into GraalVM!
JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_DalvikLifecycleService_setLifecycleEventNative(JNIEnv *env, jobject service, jstring jevent) {
    const char *chars = (*env)->GetStringUTFChars(env, jevent, NULL);
    ATTACH_LOG_FINE("Native layer got new event: %s\n", chars);
    ATTACH_GRAAL();
    jstring jchars = (*graalEnv)->NewStringUTF(graalEnv, chars);
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalLifecycleClass, jGraalSetLifecycleEventMethod, jchars);
    (*graalEnv)->DeleteLocalRef(graalEnv, jchars);
    DETACH_GRAAL();
    (*env)->ReleaseStringUTFChars(env, jevent, chars);
}
