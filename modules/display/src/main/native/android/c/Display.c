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
#include "Display.h"

static JNIEnv *graalEnv;
JavaVM *jVMDisplay = NULL;

static JavaVM *myAndroidVM = NULL;
static jobject jDalvikDisplayService;
static jmethodID jDisplayServiceWidthMethod;
static jmethodID jDisplayServiceHeightMethod;
static jmethodID jDisplayServiceFactorMethod;

void initializeDalvikHandles() {
    myAndroidVM = substrateGetAndroidVM();
    jclass jDisplayServiceClass = substrateGetDisplayServiceClass();
    JNIEnv* androidEnv;
    (*myAndroidVM)->AttachCurrentThread(myAndroidVM, (void **)&androidEnv, NULL);
    jmethodID jDisplayServiceInitMethod = (*androidEnv)->GetMethodID(androidEnv, jDisplayServiceClass, "<init>", "(Landroid/app/Activity;)V");
    jDisplayServiceWidthMethod = (*androidEnv)->GetMethodID(androidEnv, jDisplayServiceClass, "screenWidth", "()D");
    jDisplayServiceHeightMethod = (*androidEnv)->GetMethodID(androidEnv, jDisplayServiceClass, "screenHeight", "()D");
    jDisplayServiceFactorMethod = (*androidEnv)->GetMethodID(androidEnv, jDisplayServiceClass, "isPhoneFactor", "()Z");

    jobject jActivity = substrateGetActivity();
    jobject jtmpobj = (*androidEnv)->NewObject(androidEnv, jDisplayServiceClass, jDisplayServiceInitMethod, jActivity);
    jDalvikDisplayService = (*androidEnv)->NewGlobalRef(androidEnv, jtmpobj);
    (*myAndroidVM)->DetachCurrentThread(myAndroidVM);
}

//////////////////////////
// From Graal to native //
//////////////////////////


JNIEXPORT jint JNICALL
JNI_OnLoad_Display(JavaVM *vm, void *reserved)
{
fprintf(stderr, "JNI_OnLoad_Display called\n");
#ifdef JNI_VERSION_1_8
    jVMDisplay = vm;
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native Display from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("[Display Service] Initializing native Display from OnLoad");
    initializeDalvikHandles();
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

// from Java to Android

JNIEnv* getSafeAndroidEnv() {
    JNIEnv* androidEnv;
    if ((*myAndroidVM)->GetEnv(myAndroidVM, (void **)&androidEnv, JNI_VERSION_1_6) != JNI_OK) {
        ATTACH_LOG_WARNING("AndroidEnv called from not-attached thread\n");
        (*myAndroidVM)->AttachCurrentThread(myAndroidVM, (void **)&androidEnv, NULL);
    }
    return androidEnv;
}

JNIEXPORT jdoubleArray JNICALL Java_com_gluonhq_attach_display_impl_AndroidDisplayService_screenSize
(JNIEnv *env, jclass jClass)
{
    JNIEnv* androidEnv = getSafeAndroidEnv();
    jdouble w = (*androidEnv)->CallDoubleMethod(androidEnv, jDalvikDisplayService, jDisplayServiceWidthMethod);
    jdouble h = (*androidEnv)->CallDoubleMethod(androidEnv, jDalvikDisplayService, jDisplayServiceHeightMethod);

    jdoubleArray output = (*env)->NewDoubleArray(env, 2);
    if (output == NULL)
    {
        return NULL;
    }
    jdouble res[] = {w, h};
    (*env)->SetDoubleArrayRegion(env, output, 0, 2, res);
    return output;
}

JNIEXPORT jboolean JNICALL Java_com_gluonhq_attach_display_impl_AndroidDisplayService_isPhoneFactor
(JNIEnv *env, jclass jClass)
{
    JNIEnv* androidEnv = getSafeAndroidEnv();
    return (*androidEnv)->CallBooleanMethod(androidEnv, jDalvikDisplayService, jDisplayServiceFactorMethod);
}
