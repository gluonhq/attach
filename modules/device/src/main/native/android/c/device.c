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
#include "device.h"

// Graal handles
static jclass jGraalDeviceInfoClass;
static jmethodID jGraalDeviceInfoInitMethod;

// Dalvik handles
static jobject jDalvikDeviceService;
static jmethodID jDeviceServiceGetModel;
static jmethodID jDeviceServiceGetUuid;
static jmethodID jDeviceServiceGetPlatform;
static jmethodID jDeviceServiceGetVersion;
static jmethodID jDeviceServiceIsWearable;

static void initializeGraalHandles(JNIEnv* env) {
    jGraalDeviceInfoClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/device/impl/DeviceInfo"));
    jGraalDeviceInfoInitMethod = (*env)->GetMethodID(env, jGraalDeviceInfoClass, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V");
}

static void initializeDeviceDalvikHandles() {
    ATTACH_DALVIK();
    jclass jDeviceServiceClass = substrateGetDeviceServiceClass();
    jmethodID jDeviceServiceInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jDeviceServiceClass, "<init>", "(Landroid/app/Activity;)V");
    jDeviceServiceGetModel = (*dalvikEnv)->GetMethodID(dalvikEnv, jDeviceServiceClass, "getModel", "()Ljava/lang/String;");
    jDeviceServiceGetUuid = (*dalvikEnv)->GetMethodID(dalvikEnv, jDeviceServiceClass, "getUuid", "()Ljava/lang/String;");
    jDeviceServiceGetPlatform = (*dalvikEnv)->GetMethodID(dalvikEnv, jDeviceServiceClass, "getPlatform", "()Ljava/lang/String;");
    jDeviceServiceGetVersion = (*dalvikEnv)->GetMethodID(dalvikEnv, jDeviceServiceClass, "getVersion", "()Ljava/lang/String;");
    jDeviceServiceIsWearable = (*dalvikEnv)->GetMethodID(dalvikEnv, jDeviceServiceClass, "isWearable", "()Z");

    jobject jActivity = substrateGetActivity();
    jobject jtmpobj = (*dalvikEnv)->NewObject(dalvikEnv, jDeviceServiceClass, jDeviceServiceInitMethod, jActivity);
    jDalvikDeviceService = (*dalvikEnv)->NewGlobalRef(dalvikEnv, jtmpobj);
    DETACH_DALVIK();
}

//////////////////////////
// From Graal to native //
//////////////////////////

JNIEXPORT jint JNICALL
JNI_OnLoad_device(JavaVM *vm, void *reserved)
{
    JNIEnv* graalEnv;
    ATTACH_LOG_INFO("JNI_OnLoad_device called");
#ifdef JNI_VERSION_1_8
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native Device from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("[Device Service] Initializing native Device from OnLoad");
    initializeGraalHandles(graalEnv);
    initializeDeviceDalvikHandles();
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

// from Java to Android

JNIEXPORT jobject JNICALL Java_com_gluonhq_attach_device_impl_AndroidDeviceService_getDeviceInfo
(JNIEnv *env, jclass jClass)
{
    if (debugAttach) {
        ATTACH_LOG_FINE("Retrieving DeviceInfo\n");
    }

    ATTACH_DALVIK();
    jstring model = (*dalvikEnv)->CallObjectMethod(dalvikEnv, jDalvikDeviceService, jDeviceServiceGetModel);
    const char *responseModelChars = (*dalvikEnv)->GetStringUTFChars(dalvikEnv, model, 0);
    jstring uuid = (*dalvikEnv)->CallObjectMethod(dalvikEnv, jDalvikDeviceService, jDeviceServiceGetUuid);
    const char *responseUuidChars = (*dalvikEnv)->GetStringUTFChars(dalvikEnv, uuid, 0);
    jstring platform = (*dalvikEnv)->CallObjectMethod(dalvikEnv, jDalvikDeviceService, jDeviceServiceGetPlatform);
    const char *responsePlatformChars = (*dalvikEnv)->GetStringUTFChars(dalvikEnv, platform, 0);
    jstring version = (*dalvikEnv)->CallObjectMethod(dalvikEnv, jDalvikDeviceService, jDeviceServiceGetVersion);
    const char *responseVersionChars = (*dalvikEnv)->GetStringUTFChars(dalvikEnv, version, 0);
    jboolean wearable = (*dalvikEnv)->CallBooleanMethod(dalvikEnv, jDalvikDeviceService, jDeviceServiceIsWearable);
    DETACH_DALVIK();

    if (debugAttach) {
        ATTACH_LOG_FINE("Retrieved DeviceInfo: model=%s, uuid=%s, platform=%s, version=%s, wearable=%d\n",
                responseModelChars, responseUuidChars, responsePlatformChars, responseVersionChars, wearable);
    }

    jstring responseModelString = (*env)->NewStringUTF(env, responseModelChars);
    jstring responseUuidString = (*env)->NewStringUTF(env, responseUuidChars);
    jstring responsePlatformString = (*env)->NewStringUTF(env, responsePlatformChars);
    jstring responseVersionString = (*env)->NewStringUTF(env, responseVersionChars);
    jobject jtmpobj = (*env)->NewObject(env, jGraalDeviceInfoClass, jGraalDeviceInfoInitMethod,
            responseModelString, responseUuidString, responsePlatformString, responseVersionString,
            wearable);
    return (*env)->NewGlobalRef(env, jtmpobj);
}
