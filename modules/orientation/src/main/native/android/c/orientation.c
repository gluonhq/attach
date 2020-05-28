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
#include "orientation.h"

// Graal handles
static jclass jGraalOrientationClass;
static jmethodID jGraalNotifyOrientationMethod;

static void initializeGraalHandles(JNIEnv* env) {
    jGraalOrientationClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/orientation/impl/AndroidOrientationService"));
    jGraalNotifyOrientationMethod = (*env)->GetStaticMethodID(env, jGraalOrientationClass, "notifyOrientation", "(Ljava/lang/String;)V");
}

static void initializeOrientationDalvikHandles() {
    ATTACH_DALVIK();
    jclass jOrientationServiceClass = substrateGetOrientationServiceClass();
    jmethodID jOrientationServiceInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jOrientationServiceClass, "<init>", "(Landroid/app/Activity;)V");

    jobject jActivity = substrateGetActivity();
    jobject jtmpobj = (*dalvikEnv)->NewObject(dalvikEnv, jOrientationServiceClass, jOrientationServiceInitMethod, jActivity);
    jobject jDalvikOrientationService = (*dalvikEnv)->NewGlobalRef(dalvikEnv, jtmpobj);
    DETACH_DALVIK();
}

//////////////////////////
// From Graal to native //
//////////////////////////

JNIEXPORT jint JNICALL
JNI_OnLoad_orientation(JavaVM *vm, void *reserved)
{
    JNIEnv* graalEnv;
    ATTACH_LOG_INFO("JNI_OnLoad_orientation called");
#ifdef JNI_VERSION_1_8
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native Orientation from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("[Orientation Service] Initializing native Orientation from OnLoad");
    initializeGraalHandles(graalEnv);
    initializeOrientationDalvikHandles();
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

///////////////////////////
// From Dalvik to native //
///////////////////////////

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_DalvikOrientationService_nativeOrientation(
    JNIEnv *env, jobject service, jstring jorientation) {
    const char *chars = (*env)->GetStringUTFChars(env, jorientation, NULL);
    if (debugAttach) {
        ATTACH_LOG_FINE("Native layer got new orientation: %s\n", chars);
    }
    ATTACH_GRAAL();
    jstring jchars = (*graalEnv)->NewStringUTF(graalEnv, chars);
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalOrientationClass, jGraalNotifyOrientationMethod, jchars);
    (*graalEnv)->DeleteLocalRef(graalEnv, jchars);
    DETACH_GRAAL();
    (*env)->ReleaseStringUTFChars(env, jorientation, chars);
}
