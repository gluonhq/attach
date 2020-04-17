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
#include "Util.h"

JavaVM* graalVM;

// Dalvik handles
static jclass jUtilClass;
static jclass jPermissionActivityClass;
static jmethodID jUtilOnActivityResultMethod;
static jmethodID jUtilOnLifecycleEventMethod;
static jmethodID jUtilRequestPermissionsMethod;

static void initializeUtilDalvikHandles() {
    ATTACH_LOG_FINE("Init Util");
    jUtilClass = substrateGetUtilClass();
    ATTACH_LOG_FINE("Init Util, class at %p\n",jUtilClass);
    jPermissionActivityClass = substrateGetPermissionActivityClass();
    ATTACH_DALVIK();
    jmethodID jUtilInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jUtilClass, "<init>", "()V");
    ATTACH_LOG_FINE("Init Util, juim = %p\n", jUtilInitMethod);
    jUtilOnActivityResultMethod = (*dalvikEnv)->GetStaticMethodID(dalvikEnv, jUtilClass, "onActivityResult", "(IILandroid/content/Intent;)V");
    ATTACH_LOG_FINE("Init Util, juim = %p\n", jUtilOnActivityResultMethod);
    jUtilOnLifecycleEventMethod = (*dalvikEnv)->GetStaticMethodID(dalvikEnv, jUtilClass, "lifecycleEvent", "(Ljava/lang/String;)V");
    ATTACH_LOG_FINE("Init Util, juim = %p\n", jUtilOnLifecycleEventMethod);
    jUtilRequestPermissionsMethod = (*dalvikEnv)->GetStaticMethodID(dalvikEnv, jPermissionActivityClass, "verifyPermissions", "(Landroid/app/Activity;[Ljava/lang/String;)Z");
    ATTACH_LOG_FINE("Init Util, juim = %p\n", jUtilRequestPermissionsMethod);
    jthrowable t = (*dalvikEnv)->ExceptionOccurred(dalvikEnv);
    if (t) {
        ATTACH_LOG_INFO("EXCEPTION occurred when dealing with dalvik handles\n");
        (*dalvikEnv)->ExceptionClear(dalvikEnv);
    }

    jobject util = (*dalvikEnv)->NewObject(dalvikEnv, jUtilClass, jUtilInitMethod);
    DETACH_DALVIK();
    ATTACH_LOG_FINE("Dalvik Util init was called");
}

JNIEXPORT jint JNICALL
JNI_OnLoad_Util(JavaVM *vm, void *reserved)
{
#ifdef JNI_VERSION_1_8
    graalVM = vm;
    JNIEnv* graalEnv;
    //min. returned JNI_VERSION required by JDK8 for builtin libraries
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native Util from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("Initializing native Util from OnLoad");
    initializeUtilDalvikHandles();
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

JavaVM* getGraalVM() {
    return graalVM;
}

//////////////////////////////////
// native (Substrate) to Dalvik //
//////////////////////////////////

void attach_setActivityResult(jint requestCode, jint resultCode, jobject intent)
{
    ATTACH_LOG_FINE("call Attach::nativeDispatchActivityResult method from native: %d %d", requestCode, resultCode);
    ATTACH_DALVIK();
    (*dalvikEnv)->CallStaticVoidMethod(dalvikEnv, jUtilClass, jUtilOnActivityResultMethod, requestCode, resultCode, intent);
    DETACH_DALVIK();
    ATTACH_LOG_FINE("call Attach::nativeDispatchActivityResult method from native done");
}

void attach_setLifecycleEvent(const char* event) {
    ATTACH_LOG_FINE("Call Attach method from native Lifecycle: %s", event);
    ATTACH_DALVIK();
    jstring jchars = (*dalvikEnv)->NewStringUTF(dalvikEnv, event);
    ATTACH_LOG_FINE("Call2 Attach method from native Lifecycle: %s, class at %p and method at %p and jc at %p\n", event, jUtilClass, jUtilOnLifecycleEventMethod, jchars);
    (*dalvikEnv)->CallStaticVoidMethod(dalvikEnv, jUtilClass, jUtilOnLifecycleEventMethod, jchars);
    ATTACH_LOG_FINE("Call3 Attach method from native Lifecycle: %s", event);
    (*dalvikEnv)->DeleteLocalRef(dalvikEnv, jchars);
    ATTACH_LOG_FINE("Call4 Attach method from native Lifecycle: %s", event);
    DETACH_DALVIK();
    ATTACH_LOG_FINE("called Attach method from native Lifecycle done");
}

////////////////////////////
// From Dalvik to Dalvik  //
////////////////////////////

JNIEXPORT jboolean JNICALL Java_com_gluonhq_helloandroid_Util_nativeVerifyPermissions(JNIEnv *env, jobject activity, jobjectArray jpermissionsArray)
{
    ATTACH_LOG_FINE("Calling Verify Permissions from Attach::Util");
    jboolean jresult = (*env)->CallStaticBooleanMethod(env, jPermissionActivityClass, jUtilRequestPermissionsMethod, substrateGetActivity(), jpermissionsArray);
    ATTACH_LOG_FINE("Verify Permissions from native Attach::Util done");
    return jresult;
}
