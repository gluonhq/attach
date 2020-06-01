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
#include "dialer.h"


static jobject jDalvikDialerService;
static jmethodID jDialerServiceCallMethod;

static void initializeDalvikHandles() {
    jclass jDialerServiceClass = substrateGetDialerServiceClass();
    ATTACH_DALVIK();
    jmethodID jDialerServiceInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jDialerServiceClass, "<init>", "(Landroid/app/Activity;)V");
    jDialerServiceCallMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jDialerServiceClass, "call", "(Ljava/lang/String;)V");

    jobject jActivity = substrateGetActivity();
    jobject jtmpobj = (*dalvikEnv)->NewObject(dalvikEnv, jDialerServiceClass, jDialerServiceInitMethod, jActivity);
    jDalvikDialerService = (*dalvikEnv)->NewGlobalRef(dalvikEnv, jtmpobj);
    DETACH_DALVIK();
}

//////////////////////////
// From Graal to native //
//////////////////////////


JNIEXPORT jint JNICALL
JNI_OnLoad_dialer(JavaVM *vm, void *reserved)
{
    ATTACH_LOG_INFO("JNI_OnLoad_dialer called");
#ifdef JNI_VERSION_1_8
    JNIEnv* graalEnv;
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native Dialer from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("[Dialer Service] Initializing native Dialer from OnLoad");
    initializeDalvikHandles();
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

// from Java to Android

JNIEXPORT void JNICALL Java_com_gluonhq_attach_dialer_impl_AndroidDialerService_callNumber
(JNIEnv *env, jclass jClass, jstring jnumber)
{
    const char *numberChars = (*env)->GetStringUTFChars(env, jnumber, NULL);
    ATTACH_DALVIK();
    jstring dnumber = (*dalvikEnv)->NewStringUTF(dalvikEnv, numberChars);
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikDialerService, jDialerServiceCallMethod, dnumber);
    DETACH_DALVIK();
    // (*env)->ReleaseStringUTFChars(env, jnumber, numberChars);
}
