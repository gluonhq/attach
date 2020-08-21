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
#include "audio.h"

static jobject jDalvikAudioService;
static jmethodID jAudioServiceLoadSoundMethod;
static jmethodID jAudioServiceLoadMusicMethod;
static jmethodID jAudioServiceSetLoopingMethod;
static jmethodID jAudioServiceSetVolumeMethod;
static jmethodID jAudioServicePlayMethod;
static jmethodID jAudioServicePauseMethod;
static jmethodID jAudioServiceStopMethod;
static jmethodID jAudioServiceDisposeMethod;

static void initializeAudioDalvikHandles() {
    ATTACH_DALVIK();

    jclass jAudioServiceClass = substrateGetAudioServiceClass();
    jmethodID jAudioServiceInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jAudioServiceClass, "<init>", "()V");

    jAudioServiceLoadSoundMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jAudioServiceClass, "loadSoundImpl", "(Ljava/lang/String;)I");
    jAudioServiceLoadMusicMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jAudioServiceClass, "loadMusicImpl", "(Ljava/lang/String;)I");

    jAudioServiceSetLoopingMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jAudioServiceClass, "setLooping", "(IZ)V");
    jAudioServiceSetVolumeMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jAudioServiceClass, "setVolume", "(ID)V");
    jAudioServicePlayMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jAudioServiceClass, "play", "(I)V");
    jAudioServicePauseMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jAudioServiceClass, "pause", "(I)V");
    jAudioServiceStopMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jAudioServiceClass, "stop", "(I)V");
    jAudioServiceDisposeMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jAudioServiceClass, "dispose", "(I)V");

    jobject jObj = (*dalvikEnv)->NewObject(dalvikEnv, jAudioServiceClass, jAudioServiceInitMethod);
    jDalvikAudioService = (*dalvikEnv)->NewGlobalRef(dalvikEnv, jObj);

    DETACH_DALVIK();
}

// From Graal to native

JNIEXPORT jint JNICALL
JNI_OnLoad_audio(JavaVM *vm, void *reserved)
{
    JNIEnv* graalEnv;
    ATTACH_LOG_INFO("JNI_OnLoad_audio called");
#ifdef JNI_VERSION_1_8
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native Audio from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("[Audio Service] Initializing native Audio from OnLoad");
    initializeAudioDalvikHandles();
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

// from Java to Android

JNIEXPORT jint JNICALL Java_com_gluonhq_attach_audio_impl_AndroidAudioService_loadSoundImpl
(JNIEnv *env, jclass jClass, jstring jURL)
{
    const char *jURLChars = (*env)->GetStringUTFChars(env, jURL, NULL);

    if (debugAttach) {
        ATTACH_LOG_FINE("Loading sound from file. Absolute path: %s", jURLChars);
    }

    ATTACH_DALVIK();
    jstring jURLString = (*dalvikEnv)->NewStringUTF(dalvikEnv, jURLChars);
    jint result = (*dalvikEnv)->CallIntMethod(dalvikEnv, jDalvikAudioService, jAudioServiceLoadSoundMethod, jURLString);
    DETACH_DALVIK();

    return result;
}

JNIEXPORT jint JNICALL Java_com_gluonhq_attach_audio_impl_AndroidAudioService_loadMusicImpl
(JNIEnv *env, jclass jClass, jstring jURL)
{
    const char *jURLChars = (*env)->GetStringUTFChars(env, jURL, NULL);

    if (debugAttach) {
        ATTACH_LOG_FINE("Loading music from file. Absolute path: %s", jURLChars);
    }

    ATTACH_DALVIK();
    jstring jURLString = (*dalvikEnv)->NewStringUTF(dalvikEnv, jURLChars);
    jint result = (*dalvikEnv)->CallIntMethod(dalvikEnv, jDalvikAudioService, jAudioServiceLoadMusicMethod, jURLString);
    DETACH_DALVIK();

    return result;
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_audio_impl_AndroidAudioService_setLooping
(JNIEnv *env, jclass jClass, jint jAudioId, jboolean jlooping)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikAudioService, jAudioServiceSetLoopingMethod, jAudioId, jlooping);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_audio_impl_AndroidAudioService_setVolume
(JNIEnv *env, jclass jClass, jint jAudioId, jdouble jvolume)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikAudioService, jAudioServiceSetVolumeMethod, jAudioId, jvolume);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_audio_impl_AndroidAudioService_play
(JNIEnv *env, jclass jClass, jint jAudioId)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikAudioService, jAudioServicePlayMethod, jAudioId);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_audio_impl_AndroidAudioService_pause
(JNIEnv *env, jclass jClass, jint jAudioId)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikAudioService, jAudioServicePauseMethod, jAudioId);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_audio_impl_AndroidAudioService_stop
(JNIEnv *env, jclass jClass, jint jAudioId)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikAudioService, jAudioServiceStopMethod, jAudioId);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_audio_impl_AndroidAudioService_dispose
(JNIEnv *env, jclass jClass, jint jAudioId)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikAudioService, jAudioServiceDisposeMethod, jAudioId);
    DETACH_DALVIK();
}