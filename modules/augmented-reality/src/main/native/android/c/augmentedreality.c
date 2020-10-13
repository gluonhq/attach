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
#include "augmentedreality.h"

// Graal handles
static jclass jGraalAugmentedRealityClass;
static jmethodID jGraalAvailabilityAugmentedRealityMethod;
static jmethodID jGraalCancelAugmentedRealityMethod;

static jobject jDalvikAugmentedRealityService;
static jmethodID jAugmentedRealityServiceCheckARMethod;
static jmethodID jAugmentedRealityServiceShowARMethod;
static jmethodID jAugmentedRealityServiceDebugARMethod;
static jmethodID jAugmentedRealityServiceModelARMethod;


static void initializeGraalHandles(JNIEnv* env) {
    jGraalAugmentedRealityClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/augmentedreality/impl/AndroidAugmentedRealityService"));
    jGraalAvailabilityAugmentedRealityMethod = (*env)->GetStaticMethodID(env, jGraalAugmentedRealityClass, "notifyAvailability", "(Ljava/lang/String;)V");
    jGraalCancelAugmentedRealityMethod = (*env)->GetStaticMethodID(env, jGraalAugmentedRealityClass, "notifyCancel", "()V");
}

static void initializeAugmentedRealityDalvikHandles() {
    ATTACH_DALVIK();
    jclass jAugmentedRealityServiceClass = substrateGetAugmentedRealityServiceClass();
    jmethodID jAugmentedRealityServiceInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jAugmentedRealityServiceClass, "<init>", "(Landroid/app/Activity;)V");
    jAugmentedRealityServiceCheckARMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jAugmentedRealityServiceClass, "checkAR", "()Ljava/lang/String;");
    jAugmentedRealityServiceShowARMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jAugmentedRealityServiceClass, "showAR", "()V");
    jAugmentedRealityServiceDebugARMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jAugmentedRealityServiceClass, "enableDebugAR", "(Z)V");
    jAugmentedRealityServiceModelARMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jAugmentedRealityServiceClass, "setARModel", "(Ljava/lang/String;Ljava/lang/String;D)V");

    jobject jActivity = substrateGetActivity();
    jobject jtmpobj = (*dalvikEnv)->NewObject(dalvikEnv, jAugmentedRealityServiceClass, jAugmentedRealityServiceInitMethod, jActivity);
    jDalvikAugmentedRealityService = (*dalvikEnv)->NewGlobalRef(dalvikEnv, jtmpobj);
    DETACH_DALVIK();
}

//////////////////////////
// From Graal to native //
//////////////////////////

JNIEXPORT jint JNICALL
JNI_OnLoad_augmentedreality(JavaVM *vm, void *reserved)
{
    JNIEnv* graalEnv;
    ATTACH_LOG_INFO("JNI_OnLoad_augmentedreality called");
#ifdef JNI_VERSION_1_8
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native AugmentedReality from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("[AugmentedReality Service] Initializing native AugmentedReality from OnLoad");
    initializeGraalHandles(graalEnv);
    initializeAugmentedRealityDalvikHandles();
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

// from Java to Android
JNIEXPORT jstring JNICALL Java_com_gluonhq_attach_augmentedreality_impl_AndroidAugmentedRealityService_checkAR
(JNIEnv *env, jclass jClass) {
    if (debugAttach) {
        ATTACH_LOG_FINE("AugmentedReality check AR\n");
    }
    ATTACH_DALVIK();
    jstring answer = (*dalvikEnv)->CallObjectMethod(dalvikEnv, jDalvikAugmentedRealityService, jAugmentedRealityServiceCheckARMethod);
    if (answer == NULL) {
        if (debugAttach) {
            ATTACH_LOG_FINE("Error: AugmentedReality:: availability is null\n");
        }
        DETACH_DALVIK();
        return NULL;
    }
    const char *answerChars = (*dalvikEnv)->GetStringUTFChars(dalvikEnv, answer, 0);
    if (debugAttach) {
        ATTACH_LOG_FINE("AugmentedReality availability is %s", answerChars);
    }
    DETACH_DALVIK();
    return (*env)->NewStringUTF(env, answerChars);
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_augmentedreality_impl_AndroidAugmentedRealityService_showNativeAR
(JNIEnv *env, jclass jClass) {
    if (debugAttach) {
        ATTACH_LOG_FINE("AugmentedReality show AR");
    }
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikAugmentedRealityService, jAugmentedRealityServiceShowARMethod);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_augmentedreality_impl_AndroidAugmentedRealityService_enableDebugAR
(JNIEnv *env, jclass jClass, jboolean enable) {
    if (debugAttach) {
        ATTACH_LOG_FINE("AugmentedReality enable debug AR");
    }
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikAugmentedRealityService, jAugmentedRealityServiceDebugARMethod, enable);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_augmentedreality_impl_AndroidAugmentedRealityService_setARModel
(JNIEnv *env, jclass jClass, jstring jobjfile, jstring jtexturefile, jdouble scale) {
    if (debugAttach) {
        ATTACH_LOG_FINE("AugmentedReality AR model");
    }
    const char *objfileChars = (*env)->GetStringUTFChars(env, jobjfile, NULL);
    const char *texturefileChars = (*env)->GetStringUTFChars(env, jtexturefile, NULL);

    ATTACH_DALVIK();
    jstring dobjfile = (*dalvikEnv)->NewStringUTF(dalvikEnv, objfileChars);
    jstring dtexturefile = (*dalvikEnv)->NewStringUTF(dalvikEnv, texturefileChars);

    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikAugmentedRealityService, jAugmentedRealityServiceModelARMethod, dobjfile, dtexturefile, scale);
    DETACH_DALVIK();
    (*env)->ReleaseStringUTFChars(env, jobjfile, objfileChars);
    (*env)->ReleaseStringUTFChars(env, jtexturefile, texturefileChars);
}

///////////////////////////
// From Dalvik to native //
///////////////////////////

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_DalvikAugmentedRealityService_nativeAugmentedRealityAvailability(
    JNIEnv *env, jobject service, jstring result) {
    const char *resultChars = (*env)->GetStringUTFChars(env, result, NULL);
    if (debugAttach) {
        ATTACH_LOG_FINE("AugmentedReality result %s", resultChars);
    }
    ATTACH_GRAAL();
    jstring jresult = (*graalEnv)->NewStringUTF(graalEnv, resultChars);
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalAugmentedRealityClass, jGraalAvailabilityAugmentedRealityMethod, jresult);
    DETACH_GRAAL();
    (*env)->ReleaseStringUTFChars(env, result, resultChars);
}

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_ar_ARRenderer_nativeCancelAR(
    JNIEnv *env, jobject service) {
    if (debugAttach) {
        ATTACH_LOG_FINE("AugmentedReality cancel AR");
    }
    ATTACH_GRAAL();
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalAugmentedRealityClass, jGraalCancelAugmentedRealityMethod);
    DETACH_GRAAL();
}
