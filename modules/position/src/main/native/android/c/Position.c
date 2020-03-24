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
static jclass jAttachPositionClass;
static jobject jPositionService;
jmethodID jPositionServiceStartMethod;

void initializeHandles(JNIEnv* env) {
    jAttachPositionClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/position/impl/AndroidPositionService"));
}

void initializeDalvikClass() {
    JavaVM* androidVM = substrateGetAndroidVM();
    jclass activityClass = substrateGetActivityClass();
    jobject jActivity = substrateGetActivity();
    jclass jPositionServiceClass = substrateGetPositionServiceClass();
    
    JNIEnv* androidEnv;
    (*androidVM)->AttachCurrentThread(androidVM, (JNIEnv **)&androidEnv, NULL);
    jmethodID jPositionServiceInitMethod = (*androidEnv)->GetMethodID(androidEnv, jPositionServiceClass, "<init>", "(Landroid/app/Activity;)V");
    jPositionServiceStartMethod = (*androidEnv)->GetMethodID(androidEnv, jPositionServiceClass, "start", "()V");
fprintf(stderr, "JPSSM at %p\n", jPositionServiceStartMethod);
    jthrowable t = (*androidEnv)->ExceptionOccurred(androidEnv);
    if (t) {
ATTACH_LOG_INFO("EXCEPTION 1\n");
        (*androidEnv)->ExceptionClear(androidEnv);
    }

    jobject jObj = (*androidEnv)->NewObject(androidEnv, jPositionServiceClass, jPositionServiceInitMethod, jActivity);
    jPositionService = (jobject)(*androidEnv)->NewGlobalRef(androidEnv, jObj);

    ATTACH_LOG_FINE("positionserviceobj: %p and init method = %p and class = %p\n", jPositionService, jPositionServiceInitMethod, jPositionServiceClass);
    ATTACH_LOG_FINE("positionservice Starting: %p\n", jPositionService);
    (*androidEnv)->CallVoidMethod(androidEnv, jPositionService, jPositionServiceStartMethod);
    (*androidVM)->DetachCurrentThread(androidVM);
    // fprintf(stderr, "We have a positionservice at %p\n", jPositionService);
    ATTACH_LOG_FINE("positionservice started!!: %p\n", jPositionService);
}

JNIEXPORT jint JNICALL
JNI_OnLoad_Position(JavaVM *vm, void *reserved)
{
#ifdef JNI_VERSION_1_8
    JNIEnv *env;
    if ((*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native position from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("Initializing native position from OnLoad");
    initializeHandles(env);
    initializeDalvikClass();
    ATTACH_LOG_FINE("Initializing native position from OnLoad Done");
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_position_impl_AndroidPositionService_startObserver
(JNIEnv *env, jclass jClass, jstring jAccuracy, jlong jInterval, jfloat jDistance, jboolean jBackground)
{
    fprintf(stderr, "START OBSERVER CALLED\n");
    initializeDalvikClass();
}

