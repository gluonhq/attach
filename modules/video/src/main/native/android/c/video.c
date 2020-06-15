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
#include "video.h"

static jclass jGraalVideoClass;
static jmethodID jGraalStatusMethod;
static jmethodID jGraalFullScreenMethod;
static jmethodID jGraalCurrentIndexMethod;

static jobject jDalvikVideoService;
static jmethodID jVideoPlaylistMethod;
static jmethodID jVideoShowVideoMethod;
static jmethodID jVideoPlayVideoMethod;
static jmethodID jVideoStopVideoMethod;
static jmethodID jVideoPauseVideoMethod;
static jmethodID jVideoHideVideoMethod;
static jmethodID jVideoLoopingVideoMethod;
static jmethodID jVideoControlsVisibleMethod;
static jmethodID jVideoCurrentIndexMethod;
static jmethodID jVideoFullScreenModeMethod;
static jmethodID jVideoPositionMethod;

static void initializeGraalHandles(JNIEnv *graalEnv) {
    jGraalVideoClass = (*graalEnv)->NewGlobalRef(graalEnv, (*graalEnv)->FindClass(graalEnv, "com/gluonhq/attach/video/impl/AndroidVideoService"));
    jGraalStatusMethod = (*graalEnv)->GetStaticMethodID(graalEnv, jGraalVideoClass, "updateStatus", "(I)V");
    jGraalFullScreenMethod = (*graalEnv)->GetStaticMethodID(graalEnv, jGraalVideoClass, "updateFullScreen", "(Z)V");
    jGraalCurrentIndexMethod = (*graalEnv)->GetStaticMethodID(graalEnv, jGraalVideoClass, "updateCurrentIndex", "(I)V");
}

static void initializeVideoDalvikHandles() {
    ATTACH_DALVIK();
    jclass jVideoServiceClass = substrateGetVideoServiceClass();
    jmethodID jVideoServiceInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jVideoServiceClass, "<init>", "(Landroid/app/Activity;)V");
    jVideoPlaylistMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jVideoServiceClass, "setPlaylist", "([Ljava/lang/String;)V");
    jVideoShowVideoMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jVideoServiceClass, "show", "()V");
    jVideoPlayVideoMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jVideoServiceClass, "play", "()V");
    jVideoStopVideoMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jVideoServiceClass, "stop", "()V");
    jVideoPauseVideoMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jVideoServiceClass, "pause", "()V");
    jVideoHideVideoMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jVideoServiceClass, "hide", "()V");
    jVideoLoopingVideoMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jVideoServiceClass, "setLooping", "(Z)V");
    jVideoControlsVisibleMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jVideoServiceClass, "setControlsVisible", "(Z)V");
    jVideoCurrentIndexMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jVideoServiceClass, "setCurrentIndex", "(I)V");
    jVideoFullScreenModeMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jVideoServiceClass, "setFullScreen", "(Z)V");
    jVideoPositionMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jVideoServiceClass, "setPosition", "(Ljava/lang/String;Ljava/lang/String;DDDD)V");

    jobject jActivity = substrateGetActivity();
    jobject jtmpobj = (*dalvikEnv)->NewObject(dalvikEnv, jVideoServiceClass, jVideoServiceInitMethod, jActivity);
    jDalvikVideoService = (*dalvikEnv)->NewGlobalRef(dalvikEnv, jtmpobj);
    DETACH_DALVIK();
}

//////////////////////////
// From Graal to native //
//////////////////////////


JNIEXPORT jint JNICALL
JNI_OnLoad_video(JavaVM *vm, void *reserved)
{
    JNIEnv* graalEnv;
    ATTACH_LOG_INFO("JNI_OnLoad_statusbar called");
#ifdef JNI_VERSION_1_8
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native video from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("[video Service] Initializing native video from OnLoad");
    initializeGraalHandles(graalEnv);
    initializeVideoDalvikHandles();
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

// from Java to Android

JNIEXPORT void JNICALL Java_com_gluonhq_attach_video_impl_AndroidVideoService_setVideoPlaylist
(JNIEnv *env, jclass jClass, jobjectArray jPlaylistArray)
{
    int playlistCount = (*env)->GetArrayLength(env, jPlaylistArray);
    if (debugAttach) {
        ATTACH_LOG_FINE("Adding video playlist with %d items", playlistCount);
    }
    ATTACH_DALVIK();
    jobjectArray result = (jobjectArray) (*dalvikEnv)->NewObjectArray(dalvikEnv, playlistCount,
            (*dalvikEnv)->FindClass(dalvikEnv, "java/lang/String"), NULL);

    for (int i = 0; i < playlistCount; i++) {
        jstring jplayItem = (jstring) ((*env)->GetObjectArrayElement(env, jPlaylistArray, i));
        const char *playItemString = (*env)->GetStringUTFChars(env, jplayItem, NULL);
        (*dalvikEnv)->SetObjectArrayElement(dalvikEnv, result, i,
                (*dalvikEnv)->NewStringUTF(dalvikEnv, playItemString));
        (*env)->ReleaseStringUTFChars(env, jplayItem, playItemString);
    }
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikVideoService, jVideoPlaylistMethod, result);
    (*dalvikEnv)->DeleteLocalRef(dalvikEnv, result);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_video_impl_AndroidVideoService_showVideo
(JNIEnv *env, jclass jClass, jstring jTitle)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikVideoService, jVideoShowVideoMethod);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_video_impl_AndroidVideoService_playVideo
(JNIEnv *env, jclass jClass, jstring jTitle)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikVideoService, jVideoPlayVideoMethod);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_video_impl_AndroidVideoService_pauseVideo
(JNIEnv *env, jclass jClass)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikVideoService, jVideoPauseVideoMethod);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_video_impl_AndroidVideoService_stopVideo
(JNIEnv *env, jclass jClass)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikVideoService, jVideoStopVideoMethod);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_video_impl_AndroidVideoService_hideVideo
(JNIEnv *env, jclass jClass)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikVideoService, jVideoHideVideoMethod);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_video_impl_AndroidVideoService_looping
(JNIEnv *env, jclass jClass, jboolean jLooping)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikVideoService, jVideoLoopingVideoMethod, jLooping);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_video_impl_AndroidVideoService_controlsVisible
(JNIEnv *env, jclass jClass, jboolean jControls)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikVideoService, jVideoControlsVisibleMethod, jControls);
    DETACH_DALVIK();

}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_video_impl_AndroidVideoService_setFullScreenMode
(JNIEnv *env, jclass jClass, jboolean jfullscreen)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikVideoService, jVideoFullScreenModeMethod, jfullscreen);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_video_impl_AndroidVideoService_currentIndex
(JNIEnv *env, jclass jClass, jint jindex)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikVideoService, jVideoCurrentIndexMethod, jindex);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_video_impl_AndroidVideoService_setPosition
(JNIEnv *env, jclass jClass, jstring jalignmentH, jstring jalignmentV, jdouble jtopPadding, 
        jdouble jrightPadding, jdouble jbottomPadding, jdouble jleftPadding)
{

    const char *alignmentHChars = (*env)->GetStringUTFChars(env, jalignmentH, NULL);
    const char *alignmentVChars = (*env)->GetStringUTFChars(env, jalignmentV, NULL);
    if (debugAttach) {
        ATTACH_LOG_FINE("Video Alignment H: %s, V: %s", alignmentHChars, alignmentVChars);
    }
    ATTACH_DALVIK();
    jstring dalignmentH = (*dalvikEnv)->NewStringUTF(dalvikEnv, alignmentHChars);
    jstring dalignmentV = (*dalvikEnv)->NewStringUTF(dalvikEnv, alignmentVChars);
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikVideoService, jVideoPlaylistMethod,
                   dalignmentH, dalignmentV, jtopPadding, jrightPadding, jbottomPadding, jleftPadding);
    DETACH_DALVIK();
    // (*env)->ReleaseStringUTFChars(env, jalignmentH, alignmentHChars);
    // (*env)->ReleaseStringUTFChars(env, jalignmentV, alignmentVChars);
}

///////////////////////////
// From Dalvik to native //
///////////////////////////

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_DalvikVideoService_nativeStatus
    (JNIEnv *env, jobject service, jint status) {
    if (debugAttach) {
        ATTACH_LOG_FINE("Media Player Status: %d", status);
    }
    ATTACH_GRAAL();
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalVideoClass, jGraalStatusMethod, status);
    DETACH_GRAAL();
}

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_DalvikVideoService_nativeFullScreen
    (JNIEnv *env, jobject service, jboolean fullscreen) {
    ATTACH_GRAAL();
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalVideoClass, jGraalFullScreenMethod, fullscreen);
    DETACH_GRAAL();
}

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_DalvikVideoService_nativeCurrentIndex
    (JNIEnv *env, jobject service, jint index) {
    if (debugAttach) {
        ATTACH_LOG_FINE("Media Player index: %d", index);
    }
    ATTACH_GRAAL();
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalVideoClass, jGraalCurrentIndexMethod, index);
    DETACH_GRAAL();
}

