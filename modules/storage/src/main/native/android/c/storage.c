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
#include "storage.h"

static jobject jDalvikStorageService;
static jmethodID jStorageServicePublicStorage;
static jmethodID jStorageServiceStorageWritable;
static jmethodID jStorageServiceStorageReadable;

static void initializeStorageDalvikHandles() {
    ATTACH_DALVIK();
    jclass jStorageServiceClass = substrateGetStorageServiceClass();
    jmethodID jStorageServiceInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jStorageServiceClass, "<init>", "(Landroid/app/Activity;)V");
    jStorageServicePublicStorage = (*dalvikEnv)->GetMethodID(dalvikEnv, jStorageServiceClass, "getPublicStorage", "(Ljava/lang/String;)Ljava/lang/String;");
    jStorageServiceStorageWritable = (*dalvikEnv)->GetMethodID(dalvikEnv, jStorageServiceClass, "isExternalStorageWritable", "()Z");
    jStorageServiceStorageReadable = (*dalvikEnv)->GetMethodID(dalvikEnv, jStorageServiceClass, "isExternalStorageReadable", "()Z");

    jobject jActivity = substrateGetActivity();
    jobject jtmpobj = (*dalvikEnv)->NewObject(dalvikEnv, jStorageServiceClass, jStorageServiceInitMethod, jActivity);
    jDalvikStorageService = (*dalvikEnv)->NewGlobalRef(dalvikEnv, jtmpobj);
    DETACH_DALVIK();
}

//////////////////////////
// From Graal to native //
//////////////////////////


JNIEXPORT jint JNICALL
JNI_OnLoad_storage(JavaVM *vm, void *reserved)
{
    JNIEnv* graalEnv;
    ATTACH_LOG_INFO("JNI_OnLoad_storage called");
#ifdef JNI_VERSION_1_8
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native Storage from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("[Storage Service] Initializing native Storage from OnLoad");
    initializeStorageDalvikHandles();
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

// from Java to Android

JNIEXPORT jstring JNICALL Java_com_gluonhq_attach_storage_impl_AndroidStorageService_publicStorage
(JNIEnv *env, jclass jClass, jstring jdirectory)
{
    const char *directoryChars = (*env)->GetStringUTFChars(env, jdirectory, NULL);
    ATTACH_DALVIK();
    jstring ddirectory = (*dalvikEnv)->NewStringUTF(dalvikEnv, directoryChars);
    if (debugAttach) {
        ATTACH_LOG_FINE("Retrieving external storage for = %s\n", directoryChars);
    }
    jstring answer = (*dalvikEnv)->CallObjectMethod(dalvikEnv, jDalvikStorageService, jStorageServicePublicStorage, ddirectory);
    const char *answerChars = (*dalvikEnv)->GetStringUTFChars(dalvikEnv, answer, 0);
    if (debugAttach) {
        ATTACH_LOG_FINE("Retrieved external storage at = %s\n", answerChars);
    }
    DETACH_DALVIK();
    // (*env)->ReleaseStringUTFChars(env, jdirectory, directoryChars);
    return (*env)->NewStringUTF(env, answerChars);
}

JNIEXPORT jboolean JNICALL Java_com_gluonhq_attach_storage_impl_AndroidStorageService_externalStorageWritable
(JNIEnv *env, jclass jClass)
{
    ATTACH_DALVIK();
    jboolean answer = (*dalvikEnv)->CallBooleanMethod(dalvikEnv, jDalvikStorageService, jStorageServiceStorageWritable);
    DETACH_DALVIK();
    return answer;
}

JNIEXPORT jboolean JNICALL Java_com_gluonhq_attach_storage_impl_AndroidStorageService_externalStorageReadable
(JNIEnv *env, jclass jClass)
{
    ATTACH_DALVIK();
    jboolean answer = (*dalvikEnv)->CallBooleanMethod(dalvikEnv, jDalvikStorageService, jStorageServiceStorageReadable);
    DETACH_DALVIK();
    return answer;
}
