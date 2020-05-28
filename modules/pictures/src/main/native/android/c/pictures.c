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

#include "pictures.h"

static jclass jGraalPicturesClass;
static jmethodID jGraalSendPhotoFileMethod;

static jobject jDalvikPicturesService;
static jmethodID jPicturesServiceTakePhotoMethod;
static jmethodID jPicturesServiceSelectPictureMethod;

void initializePicturesGraalHandles(JNIEnv *graalEnv) {
    jGraalPicturesClass = (*graalEnv)->NewGlobalRef(graalEnv, (*graalEnv)->FindClass(graalEnv, "com/gluonhq/attach/pictures/impl/AndroidPicturesService"));
    jGraalSendPhotoFileMethod = (*graalEnv)->GetStaticMethodID(graalEnv, jGraalPicturesClass, "setResult", "(Ljava/lang/String;I)V");
}

void initializePicturesDalvikHandles() {
    jclass jPicturesServiceClass = substrateGetPicturesServiceClass();
    ATTACH_DALVIK();
    jmethodID jPicturesServiceInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jPicturesServiceClass, "<init>", "(Landroid/app/Activity;)V");
    jPicturesServiceTakePhotoMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jPicturesServiceClass, "takePhoto", "(Z)V");
    jPicturesServiceSelectPictureMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jPicturesServiceClass, "selectPicture", "()V");

    jobject jActivity = substrateGetActivity();
    jobject jtmpobj = (*dalvikEnv)->NewObject(dalvikEnv, jPicturesServiceClass, jPicturesServiceInitMethod, jActivity);
    jDalvikPicturesService = (*dalvikEnv)->NewGlobalRef(dalvikEnv, jtmpobj);
    DETACH_DALVIK();
}

//////////////////////////
// From Graal to native //
//////////////////////////


JNIEXPORT jint JNICALL
JNI_OnLoad_pictures(JavaVM *vm, void *reserved)
{
ATTACH_LOG_INFO("JNI_OnLoad_pictures called");
#ifdef JNI_VERSION_1_8
    JNIEnv* graalEnv;
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native Pictures from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("[Pictures] Initializing native Pictures from OnLoad");
    initializePicturesGraalHandles(graalEnv);
    initializePicturesDalvikHandles();
    ATTACH_LOG_FINE("Initializing native Pictures done");
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

// from Java to Android

JNIEXPORT void JNICALL Java_com_gluonhq_attach_pictures_impl_AndroidPicturesService_takePicture
(JNIEnv *env, jclass jClass, jboolean jSavePhoto)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikPicturesService, jPicturesServiceTakePhotoMethod, jSavePhoto);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_pictures_impl_AndroidPicturesService_selectPicture
(JNIEnv *env, jclass jClass)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikPicturesService, jPicturesServiceSelectPictureMethod);
    DETACH_DALVIK();
}

///////////////////////////
// From Dalvik to native //
///////////////////////////

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_DalvikPicturesService_sendPhotoFile(JNIEnv *env, jobject service, jstring path, jint rotate) {
    if (debugAttach) {
        ATTACH_LOG_FINE("Send Photo File\n");
    }
    const char *pathChars = (*env)->GetStringUTFChars(env, path, NULL);
    ATTACH_GRAAL();
    jstring jpath = (*graalEnv)->NewStringUTF(graalEnv, pathChars);
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalPicturesClass, jGraalSendPhotoFileMethod, jpath, rotate);
    DETACH_GRAAL();
    // (*env)->ReleaseStringUTFChars(env, jpath, jpathChars);
}