/*
 * Copyright (c) 2016, 2019 Gluon
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
#include "Audio.h"

JNIEnv *env;

JNIEXPORT jint JNICALL
JNI_OnLoad_Audio(JavaVM *vm, void *reserved)
{
#ifdef JNI_VERSION_1_8
    //min. returned JNI_VERSION required by JDK8 for builtin libraries
    if ((*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_8) != JNI_OK) {
        return JNI_VERSION_1_4;
    }
    return JNI_VERSION_1_8;
#else
    return JNI_VERSION_1_4;
#endif
}

static int audioInited = 0;

// Audio
jclass mat_jAudioServiceClass;
jmethodID mat_jAudioService_loadSoundImpl = 0;
jmethodID mat_jAudioService_setLooping = 0;
jmethodID mat_jAudioService_setVolume = 0;
jmethodID mat_jAudioService_play = 0;
jmethodID mat_jAudioService_pause = 0;
jmethodID mat_jAudioService_stop = 0;
jmethodID mat_jAudioService_dispose = 0;
Audio *_audio;


JNIEXPORT void JNICALL Java_com_gluonhq_attach_audio_impl_IOSAudioService_initAudio
(JNIEnv *env, jclass jClass)
{
    if (audioInited)
    {
        return;
    }
    audioInited = 1;

    mat_jAudioServiceClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/audio/impl/IOSAudioService"));
    mat_jAudioService_loadSoundImpl = (*env)->GetStaticMethodID(env, mat_jAudioServiceClass, "loadSoundImpl", "(Ljava/lang/String;)J");
    mat_jAudioService_setLooping = (*env)->GetStaticMethodID(env, mat_jAudioServiceClass, "setLooping", "(JZ)V");
    mat_jAudioService_setVolume = (*env)->GetStaticMethodID(env, mat_jAudioServiceClass, "setVolume", "(JD)V");
    mat_jAudioService_play = (*env)->GetStaticMethodID(env, mat_jAudioServiceClass, "play", "(JZ)V");
    mat_jAudioService_pause = (*env)->GetStaticMethodID(env, mat_jAudioServiceClass, "pause", "(J)V");
    mat_jAudioService_stop = (*env)->GetStaticMethodID(env, mat_jAudioServiceClass, "stop", "(J)V");
    mat_jAudioService_stop = (*env)->GetStaticMethodID(env, mat_jAudioServiceClass, "dispose", "(J)V");

    _audio = [[Audio alloc] init];
}

JNIEXPORT AVAudioPlayer * JNICALL Java_com_gluonhq_attach_audio_impl_IOSAudioService_loadSoundImpl
(JNIEnv *env, jclass jClass, jstring jUrl)
{
    const jchar *urlChars = (*env)->GetStringChars(env, jUrl, NULL);
    NSString *url = [NSString stringWithCharacters:(UniChar *)urlChars length:(*env)->GetStringLength(env, jUrl)];
    (*env)->ReleaseStringChars(env, jUrl, urlChars);

    AVAudioPlayer *audioPlayer = [_audio loadSoundImpl:url];
    return audioPlayer;
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_audio_impl_IOSAudioService_setLooping
(JNIEnv *env, jclass jClass, AVAudioPlayer *audioPlayer, bool looping)
{
    [_audio setLooping:audioPlayer looping:looping];
    return;
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_audio_impl_IOSAudioService_setVolume
(JNIEnv *env, jclass jClass, AVAudioPlayer *audioPlayer, double volume)
{
    [_audio setVolume:audioPlayer volume:volume];
    return;
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_audio_impl_IOSAudioService_play
(JNIEnv *env, jclass jClass, AVAudioPlayer *audioPlayer, bool music)
{
    [_audio play:audioPlayer music:music];
    return;
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_audio_impl_IOSAudioService_pause
(JNIEnv *env, jclass jClass, AVAudioPlayer *audioPlayer)
{
    [_audio pause:audioPlayer];
    return;
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_audio_impl_IOSAudioService_stop
(JNIEnv *env, jclass jClass, AVAudioPlayer *audioPlayer)
{
    [_audio stop:audioPlayer];
    return;
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_audio_impl_IOSAudioService_dispose
(JNIEnv *env, jclass jClass, AVAudioPlayer *audioPlayer)
{
    [_audio dispose:audioPlayer];
    return;
}

@implementation Audio

- (AVAudioPlayer *)loadSoundImpl:(NSString *)url {
    NSURL *nsurl = [NSURL fileURLWithPath:url];
    AVAudioPlayer *audioPlayer = [[AVAudioPlayer alloc]initWithContentsOfURL:nsurl error:NULL];
    return audioPlayer;
}

- (void)setLooping:(AVAudioPlayer *)audioPlayer looping:(bool)looping {
    if (looping)
        audioPlayer.numberOfLoops = -1;
    else
        audioPlayer.numberOfLoops = 0;
}

- (void)setVolume:(AVAudioPlayer *)audioPlayer volume:(double)volume {
    audioPlayer.volume = volume;
}

- (void)play:(AVAudioPlayer *)audioPlayer music:(bool)music {
    if (!music && [audioPlayer isPlaying]) {
        audioPlayer.currentTime = 0;
    } else {
        [audioPlayer play];
    }
}

- (void)pause:(AVAudioPlayer *)audioPlayer {
    [audioPlayer pause];
}

- (void)stop:(AVAudioPlayer *)audioPlayer {
    [audioPlayer stop];
}

- (void)dispose:(AVAudioPlayer *)audioPlayer {
    [audioPlayer release];
}

@end