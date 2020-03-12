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
#include "Ble.h"
#include <jni.h>
#include "grandroid.h"

static JNIEnv *env;
JNIEnv* javaEnvBle = NULL;
JavaVM *jVMBle = NULL;
static jclass jBleClass;
static jmethodID attach_setEvent;
JNIEXPORT jint JNICALL
JNI_OnLoad_Ble(JavaVM *vm, void *reserved)
{
#ifdef JNI_VERSION_1_8
    if ((*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native Ble from OnLoad");
        return JNI_FALSE;
    }
    (*env)->GetJavaVM(env, &jVMBle);
    ATTACH_LOG_FINE("[BLESERVICE] Initializing native BLE from OnLoad");
    jBleClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/ble/impl/AndroidBleService"));
    ATTACH_LOG_FINE("Initializing native Ble done");
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

// from Java to Android

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ble_impl_AndroidBleService_startScanningPeripherals
(JNIEnv *env, jclass jClass)
{
    JavaVM* androidVM = substrateGetAndroidVM();
    jclass activityClass = substrateGetActivityClass();
    jobject jActivity = substrateGetActivity();
    jclass jBleServiceClass = substrateGetBleServiceClass();


    JNIEnv* androidEnv;
    (*androidVM)->AttachCurrentThread(androidVM, (JNIEnv **)&androidEnv, NULL);

    jmethodID jBleServiceInitMethod = (*androidEnv)->GetMethodID(androidEnv, jBleServiceClass, "<init>", "(Lcom/gluonhq/helloandroid/MainActivity;)V");
    jobject bleservice = (*androidEnv)->NewObject(androidEnv, jBleServiceClass, jBleServiceInitMethod, jActivity);
    (*androidVM)->DetachCurrentThread(androidVM);

}
// from Android to Java

void initializeBleFromNative() {
    if (javaEnvBle != NULL) {
        return; // already have a JNIEnv
    }
    if (jVMBle == NULL) {
        ATTACH_LOG_FINE("initialize Ble from native can't be done without JVM");
        return; // can't initialize from native before we have a jVMBle
    }
    ATTACH_LOG_FINE("Initializing native Ble from Android/native code");
    jint error = (*jVMBle)->AttachCurrentThread(jVMBle, (void **)&javaEnvBle, NULL);
    if (error != 0) {
        ATTACH_LOG_FINE("initializeBleFromNative failed with error %d", error);
    }
}

void attach_setBleEvent(const char* event) {
    initializeBleFromNative();
    if (javaEnvBle == NULL) {
        ATTACH_LOG_FINE("javaEnvBle still null, not ready to process Ble events");
        return;
    }
    ATTACH_LOG_FINE("call Attach method from native Ble: %s", event);
}

