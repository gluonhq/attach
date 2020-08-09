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

static void initializeAudioDalvikHandles() {
    ATTACH_DALVIK();

    jclass jAudioServiceClass = substrateGetAudioServiceClass();
    jmethodID jAudioServiceInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jAudioServiceClass, "<init>", "()V");

    jAudioServiceLoadSoundMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jAudioServiceClass, "loadSoundImpl", "(Ljava/lang/String;)Lcom/gluonhq/attach/audio/Audio");
    jAudioServiceLoadMusicMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jAudioServiceClass, "loadMusicImpl", "(Ljava/lang/String;)Lcom/gluonhq/attach/audio/Audio");

    jobject jObj = (*dalvikEnv)->NewObject(dalvikEnv, jAudioServiceClass, jAudioServiceInitMethod);
    jDalvikAudioService = (*dalvikEnv)->NewGlobalRef(dalvikEnv, jObj);

    DETACH_DALVIK();
}

// from Java to Android

JNIEXPORT jobject JNICALL Java_com_gluonhq_attach_audio_impl_AndroidAudioService_loadSoundImpl
(JNIEnv *env, jclass jClass, jstring jURL)
{
    const char *jURLChars = (*env)->GetStringUTFChars(env, jURL, NULL);

    ATTACH_DALVIK();
    jstring jURLString = (*dalvikEnv)->NewStringUTF(dalvikEnv, directoryChars);
    jobject answer = (*dalvikEnv)->CallObjectMethod(dalvikEnv, jDalvikAudioService, jAudioServiceLoadSoundMethod, jURLString);
    DETACH_DALVIK();
    return answer;
}

JNIEXPORT jobject JNICALL Java_com_gluonhq_attach_audio_impl_AndroidAudioService_loadMusicImpl
(JNIEnv *env, jclass jClass, jstring jURL)
{
    const char *jURLChars = (*env)->GetStringUTFChars(env, jURL, NULL);

    ATTACH_DALVIK();
    jstring jURLString = (*dalvikEnv)->NewStringUTF(dalvikEnv, directoryChars);
    jobject answer = (*dalvikEnv)->CallObjectMethod(dalvikEnv, jDalvikAudioService, jAudioServiceLoadMusicMethod, jURLString);
    DETACH_DALVIK();
    return answer;
}