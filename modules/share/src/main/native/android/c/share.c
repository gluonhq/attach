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
#include "share.h"

static jobject jDalvikShareService;
static jmethodID jShareServiceShareText;
static jmethodID jShareServiceShareFile;

static void initializeShareDalvikHandles() {
    ATTACH_DALVIK();
    jclass jShareServiceClass = substrateGetShareServiceClass();
    jmethodID jShareServiceInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jShareServiceClass, "<init>", "(Landroid/app/Activity;)V");
    jShareServiceShareText = (*dalvikEnv)->GetMethodID(dalvikEnv, jShareServiceClass, "shareText", "(Ljava/lang/String;Ljava/lang/String;)V");
    jShareServiceShareFile = (*dalvikEnv)->GetMethodID(dalvikEnv, jShareServiceClass, "shareFile", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");

    jobject jActivity = substrateGetActivity();
    jobject jtmpobj = (*dalvikEnv)->NewObject(dalvikEnv, jShareServiceClass, jShareServiceInitMethod, jActivity);
    jDalvikShareService = (*dalvikEnv)->NewGlobalRef(dalvikEnv, jtmpobj);
    DETACH_DALVIK();
}

//////////////////////////
// From Graal to native //
//////////////////////////


JNIEXPORT jint JNICALL
JNI_OnLoad_share(JavaVM *vm, void *reserved)
{
    JNIEnv* graalEnv;
    ATTACH_LOG_INFO("JNI_OnLoad_share called");
#ifdef JNI_VERSION_1_8
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native Share from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("[Share Service] Initializing native Share from OnLoad");
    initializeShareDalvikHandles();
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

// from Java to Android

JNIEXPORT void JNICALL Java_com_gluonhq_attach_share_impl_AndroidShareService_shareText
(JNIEnv *env, jclass jClass, jstring jsubject, jstring jcontent)
{
    const char *subjectChars = (*env)->GetStringUTFChars(env, jsubject, NULL);
    const char *contentChars = (*env)->GetStringUTFChars(env, jcontent, NULL);
    ATTACH_DALVIK();
    jstring dsubject = (*dalvikEnv)->NewStringUTF(dalvikEnv, subjectChars);
    jstring dcontent = (*dalvikEnv)->NewStringUTF(dalvikEnv, contentChars);
    if (debugAttach) {
        ATTACH_LOG_FINE("Share text, subject = %s, content = %s\n", subjectChars, contentChars);
    }
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikShareService, jShareServiceShareText, dsubject, dcontent);
    DETACH_DALVIK();
    // (*env)->ReleaseStringUTFChars(env, jsubject, subjectChars);
    // (*env)->ReleaseStringUTFChars(env, jcontent, contentChars);
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_share_impl_AndroidShareService_shareFile
(JNIEnv *env, jclass jClass, jstring jsubject, jstring jcontent, jstring jtype, jstring jfilename)
{
    const char *subjectChars = (*env)->GetStringUTFChars(env, jsubject, NULL);
    const char *contentChars = (*env)->GetStringUTFChars(env, jcontent, NULL);
    const char *typeChars = (*env)->GetStringUTFChars(env, jtype, NULL);
    const char *filenameChars = (*env)->GetStringUTFChars(env, jfilename, NULL);
    ATTACH_DALVIK();
    jstring dsubject = (*dalvikEnv)->NewStringUTF(dalvikEnv, subjectChars);
    jstring dcontent = (*dalvikEnv)->NewStringUTF(dalvikEnv, contentChars);
    jstring dtype = (*dalvikEnv)->NewStringUTF(dalvikEnv, typeChars);
    jstring dfilename = (*dalvikEnv)->NewStringUTF(dalvikEnv, filenameChars);
    if (debugAttach) {
        ATTACH_LOG_FINE("Share file, subject = %s, content = %s, type = %s, filename = %s\n",
            subjectChars, contentChars, typeChars, filenameChars);
    }
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikShareService, jShareServiceShareFile, dsubject, dcontent, dtype, dfilename);
    DETACH_DALVIK();
    // (*env)->ReleaseStringUTFChars(env, jsubject, subjectChars);
    // (*env)->ReleaseStringUTFChars(env, jcontent, contentChars);
    // (*env)->ReleaseStringUTFChars(env, jtype, typeChars);
    // (*env)->ReleaseStringUTFChars(env, jfilename, filenameChars);
}
