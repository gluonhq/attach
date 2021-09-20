/*
 * Copyright (c) 2021, Gluon
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

static jclass jVersionServiceClass;
static jobject jDalvikVersionService;
static jmethodID jVersionServiceGetVersion;
static jmethodID jVersionServiceGetBuild;

static void initializeDalvikHandles() {
    jVersionServiceClass = GET_REGISTER_DALVIK_CLASS(jVersionServiceClass, "com/gluonhq/helloandroid/DalvikVersionService");
    ATTACH_DALVIK();
    jmethodID jVersionServiceInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jVersionServiceClass, "<init>", "(Landroid/app/Activity;)V");
    jVersionServiceGetVersion = (*dalvikEnv)->GetMethodID(dalvikEnv, jVersionServiceClass, "getVersion", "()Ljava/lang/String;");
    jVersionServiceGetBuild = (*dalvikEnv)->GetMethodID(dalvikEnv, jVersionServiceClass, "getBuild", "()Ljava/lang/String;");

    jobject jActivity = substrateGetActivity();
    jobject jtmpobj = (*dalvikEnv)->NewObject(dalvikEnv, jVersionServiceClass, jVersionServiceInitMethod, jActivity);
    jDalvikVersionService = (*dalvikEnv)->NewGlobalRef(dalvikEnv, jtmpobj);
    DETACH_DALVIK();
}

//////////////////////////
// From Graal to native //
//////////////////////////


JNIEXPORT jint JNICALL
JNI_OnLoad_version(JavaVM *vm, void *reserved)
{
    ATTACH_LOG_INFO("JNI_OnLoad_version called");
#ifdef JNI_VERSION_1_8
    JNIEnv* graalEnv;
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native Version from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("[Version Service] Initializing native Version from OnLoad");
    initializeDalvikHandles();
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

// from Java to Android

JNIEXPORT jstring JNICALL Java_com_gluonhq_attach_version_impl_AndroidVersionService_nativeVersion
(JNIEnv *env, jclass jClass)
{
    if (isDebugAttach()) {
        ATTACH_LOG_FINE("Retrieving Version number\n");
    }
    ATTACH_DALVIK();
    jstring version = (*dalvikEnv)->CallObjectMethod(dalvikEnv, jDalvikVersionService, jVersionServiceGetVersion);
    const char *responseVersionChars = (*dalvikEnv)->GetStringUTFChars(dalvikEnv, version, 0);
    DETACH_DALVIK();
    return (*env)->NewStringUTF(env, responseVersionChars);
}

JNIEXPORT jstring JNICALL Java_com_gluonhq_attach_version_impl_AndroidVersionService_nativeBuild
(JNIEnv *env, jclass jClass)
{
    if (isDebugAttach()) {
        ATTACH_LOG_FINE("Retrieving Build number\n");
    }
    ATTACH_DALVIK();
    jstring build = (*dalvikEnv)->CallObjectMethod(dalvikEnv, jDalvikVersionService, jVersionServiceGetBuild);
    const char *responseBuildChars = (*dalvikEnv)->GetStringUTFChars(dalvikEnv, build, 0);
    DETACH_DALVIK();
    return (*env)->NewStringUTF(env, responseBuildChars);
}
