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

static JNIEnv *env;
JavaVM *jVMUtil = NULL;
static jclass jUtilClass;
static jclass jPermissionActivityClass;
static jmethodID jUtilOnActivityResultMethod;
static jmethodID jUtilRequestPermissionsMethod;
void initUtil();

JNIEXPORT jint JNICALL
JNI_OnLoad_Util(JavaVM *vm, void *reserved)
{
#ifdef JNI_VERSION_1_8
    //min. returned JNI_VERSION required by JDK8 for builtin libraries
    if ((*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native Util from OnLoad");
        return JNI_FALSE;
    }
    (*env)->GetJavaVM(env, &jVMUtil);
    ATTACH_LOG_FINE("Initializing native Util from OnLoad");
    initUtil();
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

static int UtilInited = 0;

void initUtil() {
    if (UtilInited) {
        return;
    }
    UtilInited = 1;

    ATTACH_LOG_FINE("Init Util");
    JavaVM* androidVM = substrateGetAndroidVM();
    jUtilClass = substrateGetUtilClass();
    jPermissionActivityClass = substrateGetPermissionActivityClass();

    JNIEnv* androidEnv;
    (*androidVM)->AttachCurrentThread(androidVM, (JNIEnv **)&androidEnv, NULL);
    jmethodID jUtilInitMethod = (*androidEnv)->GetMethodID(androidEnv, jUtilClass, "<init>", "()V");
    jUtilOnActivityResultMethod = (*androidEnv)->GetStaticMethodID(androidEnv, jUtilClass, "onActivityResult", "(IILandroid/content/Intent;)Z");
    jUtilRequestPermissionsMethod = (*androidEnv)->GetStaticMethodID(androidEnv, jPermissionActivityClass, "verifyPermissions", "(Landroid/app/Activity;[Ljava/lang/String;)V");
    jobject util = (*androidEnv)->NewObject(androidEnv, jUtilClass, jUtilInitMethod);
    (*androidVM)->DetachCurrentThread(androidVM);
    ATTACH_LOG_FINE("Dalvik Util init was called");
}

JNIEXPORT jboolean JNICALL Java_com_gluonhq_helloandroid_Util_nativeVerifyPermissions(JNIEnv *env, jobject activity, jobjectArray jpermissionsArray)
{
    ATTACH_LOG_FINE("Calling Verify Permissions from Attach::Util");
    jboolean jresult = (*env)->CallStaticBooleanMethod(env, jPermissionActivityClass, jUtilRequestPermissionsMethod, substrateGetActivity(), jpermissionsArray);
    ATTACH_LOG_FINE("Verify Permissions from native Attach::Util done");
    return jresult;
}

void attach_setActivityResult(JNIEnv *env, jint requestCode, jint resultCode, jobject intent)
{
    ATTACH_LOG_FINE("call Attach::nativeDispatchActivityResult method from native: %d %d", requestCode, resultCode);
    (*env)->CallStaticVoidMethod(env, jUtilClass, jUtilOnActivityResultMethod, requestCode, resultCode, intent);
    ATTACH_LOG_FINE("call Attach::nativeDispatchActivityResult method from native done");
}