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
#include "position.h"

static JavaVM* graalVM;
static JNIEnv *graalEnv;

// Graal handles
static jclass jGraalPositionClass;
jmethodID jGraalSetLocationMethod;

// Dalvik handles
static jobject jDalvikPositionService;
jmethodID jDalvikPositionServiceEnableDebug;
jmethodID jDalvikPositionServiceStartMethod;
jmethodID jDalvikPositionServiceStopMethod;

static void initializeGraalHandles(JNIEnv* env) {
    jGraalPositionClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/position/impl/AndroidPositionService"));
    jGraalSetLocationMethod = (*env)->GetStaticMethodID(env, jGraalPositionClass, "setLocation", "(DDD)V");
}

static void initializeDalvikHandles() {
    androidVM = substrateGetAndroidVM();
    jclass activityClass = substrateGetActivityClass();
    jobject jActivity = substrateGetActivity();
    jclass jPositionServiceClass = substrateGetPositionServiceClass();
    
    if ((*androidVM)->GetEnv(androidVM, (void **)&androidEnv, JNI_VERSION_1_6) != JNI_OK) {
        ATTACH_LOG_FINE("initializeDalvikHandles, thread is not linked to JNIEnv, doing that now.\n");
        (*androidVM)->AttachCurrentThread(androidVM, (void **)&androidEnv, NULL);
    }
    jmethodID jPositionServiceInitMethod = (*androidEnv)->GetMethodID(androidEnv, jPositionServiceClass, "<init>", "(Landroid/app/Activity;)V");
    jDalvikPositionServiceEnableDebug = (*androidEnv)->GetMethodID(androidEnv, jPositionServiceClass, "enableDebug", "()V");
    jDalvikPositionServiceStartMethod = (*androidEnv)->GetMethodID(androidEnv, jPositionServiceClass, "start", "(JFZ)V");
    jDalvikPositionServiceStopMethod = (*androidEnv)->GetMethodID(androidEnv, jPositionServiceClass, "stop", "()V");
    jthrowable t = (*androidEnv)->ExceptionOccurred(androidEnv);
    if (t) {
        ATTACH_LOG_INFO("EXCEPTION occured when dealing with dalvik handles\n");
        (*androidEnv)->ExceptionClear(androidEnv);
    }

    jobject jObj = (*androidEnv)->NewObject(androidEnv, jPositionServiceClass, jPositionServiceInitMethod, jActivity);
    jDalvikPositionService = (jobject)(*androidEnv)->NewGlobalRef(androidEnv, jObj);
}

///////////////////////////
// From native to dalvik //
///////////////////////////

void enableDalvikDebug() {
    if ((*androidVM)->GetEnv(androidVM, (void **)&androidEnv, JNI_VERSION_1_6) != JNI_OK) {
        ATTACH_LOG_WARNING("enableDalvikDebug called from not-attached thread\n");
        (*androidVM)->AttachCurrentThread(androidVM, (void **)&androidEnv, NULL);
    }  else {
        ATTACH_LOG_FINE("enableDalvikDebug called from attached thread %p\n", androidEnv);
    }
    (*androidEnv)->CallVoidMethod(androidEnv, jDalvikPositionService, jDalvikPositionServiceEnableDebug);
}

void startDalvikObserving(jlong jInterval, jfloat jDistance, jboolean jBackground) {
    if ((*androidVM)->GetEnv(androidVM, (void **)&androidEnv, JNI_VERSION_1_6) != JNI_OK) {
        ATTACH_LOG_WARNING("startDalvikObserving called from not-attached thread\n");
        (*androidVM)->AttachCurrentThread(androidVM, (void **)&androidEnv, NULL);
    }  else {
        ATTACH_LOG_FINE("startDalvikObserving called from attached thread %p\n", androidEnv);
    }
    (*androidEnv)->CallVoidMethod(androidEnv, jDalvikPositionService, jDalvikPositionServiceStartMethod, jInterval, jDistance, jBackground);
}

void stopDalvikObserving() {
    if ((*androidVM)->GetEnv(androidVM, (void **)&androidEnv, JNI_VERSION_1_6) != JNI_OK) {
        ATTACH_LOG_WARNING("stopDalvikObserving called from not-attached thread\n");
        (*androidVM)->AttachCurrentThread(androidVM, (void **)&androidEnv, NULL);
    }  else {
        ATTACH_LOG_WARNING("stopDalvikObserving called from attached thread %p\n", androidEnv);
    }
    (*androidEnv)->CallVoidMethod(androidEnv, jDalvikPositionService, jDalvikPositionServiceStopMethod);
}

//////////////////////////
// From Graal to native //
//////////////////////////

JNIEXPORT jint JNICALL
JNI_OnLoad_Position(JavaVM *vm, void *reserved)
{
#ifdef JNI_VERSION_1_8
    graalVM = vm;
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native position from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("Initializing native position from OnLoad");
    initializeGraalHandles(graalEnv);
    initializeDalvikHandles();
    ATTACH_LOG_FINE("Initializing native position from OnLoad Done");
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_position_impl_AndroidPositionService_enableDebug
(JNIEnv *env, jclass jClass) {
    enableDalvikDebug();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_position_impl_AndroidPositionService_startObserver
(JNIEnv *env, jclass jClass, jlong jInterval, jfloat jDistance, jboolean jBackground) {
    ATTACH_LOG_FINE("Start listening for location changes");
    startDalvikObserving(jInterval, jDistance, jBackground);
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_position_impl_AndroidPositionService_stopObserver
(JNIEnv *env, jclass jClass) {
    ATTACH_LOG_FINE("Stop listening for location changes");
    stopDalvikObserving();
    ATTACH_LOG_FINE("Stopped listening for location changes");
}

///////////////////////////
// From Dalvik to native //
///////////////////////////

// the JNIEnv passed here is the Dalvik JNIEnv, do not use it to call into GraalVM!
JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_DalvikPositionService_updatePositionNative(JNIEnv *env, jobject service, jdouble jlat, jdouble jlon, jdouble jalt) {
    ATTACH_LOG_FINE("Native layer got new position: %lf, %lf, %lf\n", jlat, jlon, jalt);
    (*graalVM)->AttachCurrentThread(graalVM, (void **)&graalEnv, NULL);
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalPositionClass, jGraalSetLocationMethod, jlat, jlon, jalt);
}

