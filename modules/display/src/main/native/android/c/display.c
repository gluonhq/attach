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
#include "display.h"

static jobject jDalvikDisplayService;
static jmethodID jDisplayServiceWidthMethod;
static jmethodID jDisplayServiceHeightMethod;
static jmethodID jDisplayServiceFactorMethod;
static jmethodID jDisplayServiceRoundMethod;

static void initializeDisplayDalvikHandles() {
    ATTACH_DALVIK();
    jclass jDisplayServiceClass = substrateGetDisplayServiceClass();
    jmethodID jDisplayServiceInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jDisplayServiceClass, "<init>", "(Landroid/app/Activity;)V");
    jDisplayServiceWidthMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jDisplayServiceClass, "screenWidth", "()D");
    jDisplayServiceHeightMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jDisplayServiceClass, "screenHeight", "()D");
    jDisplayServiceFactorMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jDisplayServiceClass, "isPhoneFactor", "()Z");
    jDisplayServiceRoundMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jDisplayServiceClass, "isScreenRound", "()Z");

    jobject jActivity = substrateGetActivity();
    jobject jtmpobj = (*dalvikEnv)->NewObject(dalvikEnv, jDisplayServiceClass, jDisplayServiceInitMethod, jActivity);
    jDalvikDisplayService = (*dalvikEnv)->NewGlobalRef(dalvikEnv, jtmpobj);
    DETACH_DALVIK();
}

//////////////////////////
// From Graal to native //
//////////////////////////


JNIEXPORT jint JNICALL
JNI_OnLoad_display(JavaVM *vm, void *reserved)
{
    JNIEnv* graalEnv;
    ATTACH_LOG_INFO("JNI_OnLoad_display called");
#ifdef JNI_VERSION_1_8
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native Display from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("[Display Service] Initializing native Display from OnLoad");
    initializeDisplayDalvikHandles();
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

// from Java to Android

JNIEXPORT jdoubleArray JNICALL Java_com_gluonhq_attach_display_impl_AndroidDisplayService_screenSize
(JNIEnv *env, jclass jClass)
{
    ATTACH_DALVIK();
    jdouble w = (*dalvikEnv)->CallDoubleMethod(dalvikEnv, jDalvikDisplayService, jDisplayServiceWidthMethod);
    jdouble h = (*dalvikEnv)->CallDoubleMethod(dalvikEnv, jDalvikDisplayService, jDisplayServiceHeightMethod);

    jdoubleArray output = (*env)->NewDoubleArray(env, 2);
    if (output == NULL)
    {
        return NULL;
    }
    jdouble res[] = {w, h};
    (*env)->SetDoubleArrayRegion(env, output, 0, 2, res);
    DETACH_DALVIK();
    return output;
}

JNIEXPORT jboolean JNICALL Java_com_gluonhq_attach_display_impl_AndroidDisplayService_isPhoneFactor
(JNIEnv *env, jclass jClass)
{
    ATTACH_DALVIK();
    jboolean answer = (*dalvikEnv)->CallBooleanMethod(dalvikEnv, jDalvikDisplayService, jDisplayServiceFactorMethod);
    DETACH_DALVIK();
    return answer;
}

JNIEXPORT jboolean JNICALL Java_com_gluonhq_attach_display_impl_AndroidDisplayService_screenRound
(JNIEnv *env, jclass jClass)
{
    ATTACH_DALVIK();
    jboolean answer = (*dalvikEnv)->CallBooleanMethod(dalvikEnv, jDalvikDisplayService, jDisplayServiceRoundMethod);
    DETACH_DALVIK();
    return answer;
}
