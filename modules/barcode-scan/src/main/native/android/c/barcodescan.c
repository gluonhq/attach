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
#include "barcodescan.h"

// Graal handles
static jclass jGraalBarcodeScanClass;
static jmethodID jGraalResultBarcodeScanMethod;

static jobject jDalvikBarcodeScanService;
static jmethodID jBarcodeScanServiceScanMethod;


static void initializeGraalHandles(JNIEnv* env) {
    jGraalBarcodeScanClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/barcodescan/impl/AndroidBarcodeScanService"));
    jGraalResultBarcodeScanMethod = (*env)->GetStaticMethodID(env, jGraalBarcodeScanClass, "setResult", "(Ljava/lang/String;)V");
}

static void initializeBarcodeScanDalvikHandles() {
    ATTACH_DALVIK();
    jclass jBarcodeScanServiceClass = substrateGetBarcodeScanServiceClass();
    jmethodID jBarcodeScanServiceInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jBarcodeScanServiceClass, "<init>", "(Landroid/app/Activity;)V");
    jBarcodeScanServiceScanMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jBarcodeScanServiceClass, "scan", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
        
    jobject jActivity = substrateGetActivity();
    jobject jtmpobj = (*dalvikEnv)->NewObject(dalvikEnv, jBarcodeScanServiceClass, jBarcodeScanServiceInitMethod, jActivity);
    jDalvikBarcodeScanService = (*dalvikEnv)->NewGlobalRef(dalvikEnv, jtmpobj);
    DETACH_DALVIK();
}

//////////////////////////
// From Graal to native //
//////////////////////////

JNIEXPORT jint JNICALL
JNI_OnLoad_barcodescan(JavaVM *vm, void *reserved)
{
    JNIEnv* graalEnv;
    ATTACH_LOG_INFO("JNI_OnLoad_barcodescan called");
#ifdef JNI_VERSION_1_8
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native BarcodeScan from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("[BarcodeScan Service] Initializing native BarcodeScan from OnLoad");
    initializeGraalHandles(graalEnv);
    initializeBarcodeScanDalvikHandles();
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

// from Java to Android
JNIEXPORT void JNICALL Java_com_gluonhq_attach_barcodescan_impl_AndroidBarcodeScanService_startBarcodeScan
(JNIEnv *env, jclass jClass, jstring jtitle, jstring jlegend, jstring jresulttext) {
    const char *titleChars = (*env)->GetStringUTFChars(env, jtitle, NULL);
    const char *legendChars = (*env)->GetStringUTFChars(env, jlegend, NULL);
    const char *resulttextChars = (*env)->GetStringUTFChars(env, jresulttext, NULL);
    if (debugAttach) {
        ATTACH_LOG_FINE("BarcodeScan start for title %s, legend %s, resulttext %s\n", titleChars, legendChars, resulttextChars);
    }
    ATTACH_DALVIK();
    jstring dtitle = (*dalvikEnv)->NewStringUTF(dalvikEnv, titleChars);
    jstring dlegend = (*dalvikEnv)->NewStringUTF(dalvikEnv, legendChars);
    jstring dresulttext = (*dalvikEnv)->NewStringUTF(dalvikEnv, resulttextChars);
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikBarcodeScanService, jBarcodeScanServiceScanMethod,
                   dtitle, dlegend, dresulttext);
    DETACH_DALVIK();
    // (*env)->ReleaseStringUTFChars(env, jtitle, titleChars);
    // (*env)->ReleaseStringUTFChars(env, jlegend, legendChars);
    // (*env)->ReleaseStringUTFChars(env, jresulttext, resulttextChars);
}

///////////////////////////
// From Dalvik to native //
///////////////////////////

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_DalvikBarcodeScanService_nativeBarcodeScanResult(
    JNIEnv *env, jobject service, jstring result) {
    const char *resultChars = (*env)->GetStringUTFChars(env, result, NULL);
    if (debugAttach) {
        ATTACH_LOG_FINE("BarcodeScan result %s\n", resultChars);
    }
    ATTACH_GRAAL();
    jstring jresult = (*graalEnv)->NewStringUTF(graalEnv, resultChars);
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalBarcodeScanClass, jGraalResultBarcodeScanMethod, jresult);
    DETACH_GRAAL();
    // (*env)->ReleaseStringUTFChars(env, result, resultChars);
}
