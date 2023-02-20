/*
 * Copyright (c) 2023, Gluon
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

static bool audioInited = false;

AudioService *audioService; // Singleton instance of the native AudioService
NSMutableArray *audios; // Singleton array that will hold the different audios. The index will be used as identifier of each Audio instance for the java IOSAudioService.

/***********************************************************************************************************************
****************************** JNI methods that just call the native AudioService **************************************
***********************************************************************************************************************/

JNIEXPORT void JNICALL Java_com_gluonhq_attach_audio_impl_IOSAudioService_initAudio
(JNIEnv *env, jclass jClass)
{
    // Note: there is no need for callbacks from native to Java
    if (!audioInited) {
        audioService = [[AudioService alloc] init];
        audios = [[NSMutableArray alloc] init];
        audioInited = true;
    }
}

JNIEXPORT int JNICALL Java_com_gluonhq_attach_audio_impl_IOSAudioService_loadSoundImpl
(JNIEnv *env, jclass jClass, jstring jUrl, bool music)
{
    const jchar *urlChars = (*env)->GetStringChars(env, jUrl, NULL);
    NSString *url = [NSString stringWithCharacters:(UniChar *)urlChars length:(*env)->GetStringLength(env, jUrl)];
    (*env)->ReleaseStringChars(env, jUrl, urlChars);

    return [audioService loadSoundImpl:url music:music];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_audio_impl_IOSAudioService_setLooping
(JNIEnv *env, jclass jClass, int index, bool looping)
{
    [audioService setLooping:index looping:looping];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_audio_impl_IOSAudioService_setVolume
(JNIEnv *env, jclass jClass, int index, double volume)
{
    [audioService setVolume:index volume:volume];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_audio_impl_IOSAudioService_play
(JNIEnv *env, jclass jClass, int index)
{
    [audioService play:index];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_audio_impl_IOSAudioService_pause
(JNIEnv *env, jclass jClass, int index)
{
    [audioService pause:index];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_audio_impl_IOSAudioService_stop
(JNIEnv *env, jclass jClass, int index)
{
    [audioService stop:index];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_audio_impl_IOSAudioService_dispose
(JNIEnv *env, jclass jClass, int index)
{
    [audioService dispose:index];
}


/***********************************************************************************************************************
******************************** Implementation of the native AudioService  ********************************************
***********************************************************************************************************************/

@implementation Audio
@synthesize musicPlayer;
@synthesize soundBuffer;
@synthesize soundPlayers;
-(void)dealloc {
    [musicPlayer release]; musicPlayer = nil;
    [soundBuffer release]; soundBuffer = nil;
    [soundPlayers release]; soundPlayers = nil;
    [super dealloc];
}
@end

@implementation AudioService

- (int)loadSoundImpl:(NSString *)url music:(bool)music {
    NSURL *audioUrl = [NSURL URLWithString:url]; // Note: AVAudioPlayer supports only URL to local files
    // Creating a new audio object and filling its properties
    Audio *audio = [[Audio alloc] init];
    NSError *error = nil;
    if (music) { // music = true for long files (JavaFX equivalent = Media)
        audio.musicPlayer = [[AVAudioPlayer alloc] initWithContentsOfURL:audioUrl error:&error];
        audio.soundBuffer = nil;
        audio.soundPlayers = nil;
    } else { // music = false for short sounds (JavaFX equivalent = AudioClip -> can be played multiple times simultaneously)
        audio.musicPlayer = nil;
        // We load the sound in memory for better performance (otherwise play takes much longer)
        audio.soundBuffer = [NSData dataWithContentsOfURL:audioUrl];
        // We create the list of sound players
        audio.soundPlayers = [[NSMutableArray alloc] init];
        // and populate it with a first instance (which will also act as a reference for settings such as volume and
        // numberOfLoops when creating new ones in multiple play)
        AVAudioPlayer *soundPlayer = [[AVAudioPlayer alloc] initWithData:audio.soundBuffer error:&error];
        [audio.soundPlayers addObject:soundPlayer];
    }
    // If there was an error during the creation, we release the audio object and return -1 as a value to report the problem.
    if (error != nil) {
        [audio release];
        [self logMessage:@"Error while loading audio %@: %@", url, error];
        return -1;
    }
    // Everything went well, we just need now to store the audio object in the mutable array.
    // Note: we can't remove a row in that array as this would change the indexes of Audios (we can't do that as this
    // index identifies each Audio instance in the IOSAudioService.java). So when an audio is disposed, we just set its
    // row to null (see dispose method).
    // So we try first to recycle the slots that may have been disposed (to prevent the array to grow if possible)
    for (int i = 0; i < [audios count]; i++) {
        if ([audios objectAtIndex: i] == (id)[NSNull null]) {
            [audios replaceObjectAtIndex:i withObject:audio];
            [self logMessage:@"Assigned audioId=%d to %@", i, url];
            return i; // In that case, we can reuse that index for a new Audio
        }
    }
    // Otherwise, we add the audio object at the end of the mutable array and return that position
    [audios addObject: audio];
    int newIndex = [audios count] - 1;
    [self logMessage:@"Assigned audioId=%d to %@", newIndex, url];
    return newIndex;
}

- (void)setLooping:(int)index looping:(bool)looping {
    Audio *audio = [audios objectAtIndex:index];
    int numberOfLoops = looping ? -1 : 0;
    if (audio.musicPlayer != nil) { // music
        audio.musicPlayer.numberOfLoops = numberOfLoops;
    } else { // sound
        for (int i = 0; i < [audio.soundPlayers count]; i++) { // There is always at least one element
            ((AVAudioPlayer *)[audio.soundPlayers objectAtIndex: i]).numberOfLoops = numberOfLoops;
        }
    }
    [self logMessage:@"Applied looping=%d to audioId %d", looping, index];
}

- (void)setVolume:(int)index volume:(double)volume {
    Audio *audio = [audios objectAtIndex:index];
    if (audio.musicPlayer != nil) { // music
        audio.musicPlayer.volume = volume;
    } else { // sound
        for (int i = 0; i < [audio.soundPlayers count]; i++) { // There is always at least one element
            ((AVAudioPlayer *)[audio.soundPlayers objectAtIndex: i]).volume = volume;
        }
    }
    [self logMessage:@"Applied volume=%f to audioId %d", volume, index];
}

- (void)play:(int)index {
    Audio *audio = [audios objectAtIndex:index];
    if (audio.musicPlayer != nil) { // music
        [audio.musicPlayer play];
    } else { // sound
        // We first iterate the sound players to see if one finished playing, and in that case, we play it again
        AVAudioPlayer *oldestPlayer = nil; // will be used if all players are busy
        for (int i = 0; i < [audio.soundPlayers count]; i++) {
            AVAudioPlayer *soundPlayer = [audio.soundPlayers objectAtIndex: i];
            if (![soundPlayer isPlaying]) { // If one is not playing
                [soundPlayer play]; // we play it again
                return; // and that's it
            }
            if (oldestPlayer == nil || soundPlayer.currentTime > oldestPlayer.currentTime) {
                oldestPlayer = soundPlayer;
            }
        }
        // We reach that point when all players created so far are busy playing.
        // We limit the number of multiple players to 4 (without a limit, performance problems have been observed)
        if ([audio.soundPlayers count] >= 4) { // it probably doesn't make sense anyway to have more than 4 players for the same sound
            oldestPlayer.currentTime = 0; // So in that case, we reset the currentTime of the oldest player instead of creating a new one
        } else { // if we haven't reached to limit of 4 players yet, we can create a new player for this sound
            NSError *error = nil;
            AVAudioPlayer *soundPlayer = [[AVAudioPlayer alloc]initWithData:audio.soundBuffer error:&error];
            if (error == nil) { // Creation was ok
                // We apply the same settings as the first player
                AVAudioPlayer *refPlayer = [audio.soundPlayers objectAtIndex: 0];
                soundPlayer.volume = refPlayer.volume;
                soundPlayer.numberOfLoops = refPlayer.numberOfLoops;
                // We add it to the list
                [audio.soundPlayers addObject:soundPlayer];
                // And we play it
                [soundPlayer play];
            }
        }
    }
    [self logMessage:@"Playing audioId %d", index];
}

- (void)pause:(int)index {
    Audio *audio = [audios objectAtIndex:index];
    if (audio.musicPlayer) { // music
        [audio.musicPlayer pause];
    } else { // sound
        for (int i = 0; i < [audio.soundPlayers count]; i++) { // There is always at least one element
            [((AVAudioPlayer *)[audio.soundPlayers objectAtIndex: i]) pause];
        }
    }
    [self logMessage:@"Paused audioId %d", index];
}

- (void)stop:(int)index {
    Audio *audio = [audios objectAtIndex:index];
    if (audio.musicPlayer) { // music
        [audio.musicPlayer stop];
        audio.musicPlayer.currentTime = 0; // Behaving like JavaFX mediaPlayer.stop() -> doc says: This operation resets playback to startTime, and resets currentCount to zero
    } else { // sound
        for (int i = 0; i < [audio.soundPlayers count]; i++) { // There is always at least one element
            AVAudioPlayer * soundPlayer = [audio.soundPlayers objectAtIndex: i];
            [soundPlayer stop];
            soundPlayer.currentTime = 0; // Behaving like JavaFX mediaPlayer.stop() -> doc says: This operation resets playback to startTime, and resets currentCount to zero
        }
    }
    [self logMessage:@"Stopped audioId %d", index];
}

- (void)dispose:(int)index {
    // We release the audio object at the specified index
    [[audios objectAtIndex:index] release];
    // And mark the slot as free in the array for possible reuse
    [audios replaceObjectAtIndex:index withObject:[NSNull null]];
    // And log that it's done
    [self logMessage:@"Disposed audioId %d", index];
}

- (void)logMessage:(NSString *)format, ...; {
    if (debugAttach) {
        va_list args;
        va_start(args, format);
        NSLogv(format, args);
        va_end(args);
    }
}

@end
