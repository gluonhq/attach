/*
 * Copyright (c) 2020, 2021, Gluon
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
#include "util.h"

static jclass jOpenServiceClass;
static jobject jDalvikOpenService;
static jmethodID jOpenServiceOpenFile;
static jmethodID jOpenServiceOpenFileWithType;

static void initializeOpenDalvikHandles() {
    jOpenServiceClass = GET_REGISTER_DALVIK_CLASS(jOpenServiceClass, "com/gluonhq/helloandroid/DalvikOpenService");
    ATTACH_DALVIK();
    jmethodID jOpenServiceInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jOpenServiceClass, "<init>", "(Landroid/app/Activity;)V");
    jOpenServiceOpenFile = (*dalvikEnv)->GetMethodID(dalvikEnv, jOpenServiceClass, "openFile", "(Ljava/lang/String;)V");
    jOpenServiceOpenFileWithType = (*dalvikEnv)->GetMethodID(dalvikEnv, jOpenServiceClass, "openFileWithType", "(Ljava/lang/String;Ljava/lang/String;)V");
    jobject jActivity = substrateGetActivity();
    jobject jtmpobj = (*dalvikEnv)->NewObject(dalvikEnv, jOpenServiceClass, jOpenServiceInitMethod, jActivity);
    jDalvikOpenService = (*dalvikEnv)->NewGlobalRef(dalvikEnv, jtmpobj);
    DETACH_DALVIK();
}

//////////////////////////
// From Graal to native //
//////////////////////////


JNIEXPORT jint JNICALL
JNI_OnLoad_open(JavaVM *vm, void *reserved)
{
    JNIEnv* graalEnv;
    ATTACH_LOG_INFO("JNI_OnLoad_open called");
#ifdef JNI_VERSION_1_8
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native Open from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("[Open Service] Initializing native Open from OnLoad");
    initializeOpenDalvikHandles();
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

// from Java to Android

JNIEXPORT void JNICALL Java_com_gluonhq_attach_open_impl_AndroidOpenService_openFile
(JNIEnv *env, jclass jClass, jstring jfilename)
{
    const char *filenameChars = (*env)->GetStringUTFChars(env, jfilename, NULL);
    ATTACH_DALVIK();
    jstring dfilename = (*dalvikEnv)->NewStringUTF(dalvikEnv, filenameChars);
    if (isDebugAttach()) {
        ATTACH_LOG_FINE("Open file, filename = %s\n",
            filenameChars);
    }
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikOpenService, jOpenServiceOpenFile, dfilename);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_open_impl_AndroidOpenService_openFileWithType
(JNIEnv *env, jclass jClass, jstring jtype, jstring jfilename)
{

    const char *typeChars = (*env)->GetStringUTFChars(env, jtype, NULL);
    const char *filenameChars = (*env)->GetStringUTFChars(env, jfilename, NULL);
    ATTACH_DALVIK();
    jstring dtype = (*dalvikEnv)->NewStringUTF(dalvikEnv, typeChars);
    jstring dfilename = (*dalvikEnv)->NewStringUTF(dalvikEnv, filenameChars);
    if (isDebugAttach()) {
        ATTACH_LOG_FINE("Open file, type = %s, filename = %s\n",
            typeChars, filenameChars);
    }
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikOpenService, jOpenServiceOpenFileWithType, dtype, dfilename);
    DETACH_DALVIK();
}

