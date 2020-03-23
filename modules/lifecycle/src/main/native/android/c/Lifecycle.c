/*
 * Copyright (c) 2016, 2020 Gluon
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
#include "Lifecycle.h"

static JNIEnv *env;
JNIEnv* javaEnvLifecycle = NULL;
static JavaVM *graalVM = NULL;
static jclass jAttachLifecycleClass;
static jmethodID attach_setEvent;
JNIEXPORT jint JNICALL
JNI_OnLoad_Lifecycle(JavaVM *vm, void *reserved)
{
#ifdef JNI_VERSION_1_8
    //min. returned JNI_VERSION required by JDK8 for builtin libraries
    if ((*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native Lifecycle from OnLoad");
        return JNI_FALSE;
    }
    graalVM = getGraalVM();
    ATTACH_LOG_FINE("Initializing native Lifecycle from OnLoad");
    jAttachLifecycleClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/lifecycle/impl/AndroidLifecycleService"));
    attach_setEvent = (*env)->GetStaticMethodID(env, jAttachLifecycleClass, "setEvent", "(Ljava/lang/String;)V");
    ATTACH_LOG_FINE("Initializing native Lifecycle done");
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

void initializeLifecycleFromNative() {
    if (javaEnvLifecycle != NULL) {
        return; // already have a JNIEnv
    }
    if (graalVM == NULL) {
        ATTACH_LOG_FINE("initialize Lifecycle from native can't be done without JVM");
        return; // can't initialize from native before we have a graalVM
    }
    ATTACH_LOG_FINE("Initializing native Lifecycle from Android/native code");
    jint error = (*graalVM)->AttachCurrentThread(graalVM, (void **)&javaEnvLifecycle, NULL);
    if (error != 0) {
        ATTACH_LOG_FINE("initializeLifecycleFromNative failed with error %d", error);
    }
}

void attach_setLifecycleEvent(const char* event) {
    initializeLifecycleFromNative();
    if (javaEnvLifecycle == NULL) {
        ATTACH_LOG_FINE("javaEnvLifecycle still null, not ready to process lifecycle events");
        return;
    }
    ATTACH_LOG_FINE("call Attach method from native Lifecycle: %s", event);
    jstring jchars = (*javaEnvLifecycle)->NewStringUTF(javaEnvLifecycle, event);
    (*javaEnvLifecycle)->CallStaticVoidMethod(javaEnvLifecycle, jAttachLifecycleClass, attach_setEvent, jchars);
    (*javaEnvLifecycle)->DeleteLocalRef(javaEnvLifecycle, jchars);
    ATTACH_LOG_FINE("called Attach method from native Lifecycle done");
}