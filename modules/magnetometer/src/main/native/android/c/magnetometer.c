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
#include "magnetometer.h"

// Graal handles
static jclass jGraalMagnetometerClass;
static jmethodID jGraalNotifyReadingMethod;

static jobject jDalvikMagnetometerService;
static jmethodID jMagnetometerServiceSetupMethod;

static void initializeGraalHandles(JNIEnv* env) {
    jGraalMagnetometerClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/magnetometer/impl/AndroidMagnetometerService"));
    jGraalNotifyReadingMethod = (*env)->GetStaticMethodID(env, jGraalMagnetometerClass, "notifyReading", "(DDDDDDD)V");
}

static void initializeMagnetometerDalvikHandles() {
    ATTACH_DALVIK();
    jclass jMagnetometerServiceClass = substrateGetMagnetometerServiceClass();
    jmethodID jMagnetometerServiceInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jMagnetometerServiceClass, "<init>", "(Landroid/app/Activity;)V");
    jMagnetometerServiceSetupMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jMagnetometerServiceClass, "setup", "(I)V");

    jobject jActivity = substrateGetActivity();
    jobject jtmpobj = (*dalvikEnv)->NewObject(dalvikEnv, jMagnetometerServiceClass, jMagnetometerServiceInitMethod, jActivity);
    jDalvikMagnetometerService = (*dalvikEnv)->NewGlobalRef(dalvikEnv, jtmpobj);
    DETACH_DALVIK();
}

//////////////////////////
// From Graal to native //
//////////////////////////

JNIEXPORT jint JNICALL
JNI_OnLoad_magnetometer(JavaVM *vm, void *reserved)
{
    JNIEnv* graalEnv;
    ATTACH_LOG_INFO("JNI_OnLoad_magnetometer called");
#ifdef JNI_VERSION_1_8
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native Magnetometer from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("[Magnetometer Service] Initializing native Magnetometer from OnLoad");
    initializeGraalHandles(graalEnv);
    initializeMagnetometerDalvikHandles();
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_magnetometer_impl_AndroidMagnetometerService_initMagnetometer
    (JNIEnv *env, jclass jClass, jint jfrequency)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikMagnetometerService, jMagnetometerServiceSetupMethod, jfrequency);
    DETACH_DALVIK();
}

///////////////////////////
// From Dalvik to native //
///////////////////////////

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_DalvikMagnetometerService_notifyReading
    (JNIEnv *env, jobject service, jdouble x, jdouble y, jdouble z, jdouble m, jdouble a, jdouble p, jdouble r) {
    ATTACH_GRAAL();
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalMagnetometerClass, jGraalNotifyReadingMethod, x, y, z, m, a, p, r);
    DETACH_GRAAL();
}
