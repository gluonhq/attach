/*
 * Copyright (c) 2017, 2019, Gluon
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

#include "AudioRecording.h"

JNIEnv *env;

JNIEXPORT jint JNICALL
JNI_OnLoad_AudioRecording(JavaVM *vm, void *reserved)
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

static int audioRecordingInited = 0;

jclass mat_jAudioRecordingServiceClass;
jmethodID mat_jAudioRecordingService_notifyRecordingStatus = 0;
jmethodID mat_jAudioRecordingService_notifyRecordingChunk = 0;

// AudioRecording
AudioRecording *_audioRecording;

NSMutableString *dataPath;
NSMutableDictionary *recordSettings;
NSDateFormatter *format;

double timerInterval = 20.0f;
int counter = 0;

JNIEXPORT void JNICALL Java_com_gluonhq_attach_audiorecording_impl_IOSAudioRecordingService_initAudioRecording
(JNIEnv *env, jclass jClass)
{
    if (audioRecordingInited)
    {
        return;
    }
    audioRecordingInited = 1;
    
    mat_jAudioRecordingServiceClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/audiorecording/impl/IOSAudioRecordingService"));
    mat_jAudioRecordingService_notifyRecordingStatus = (*env)->GetStaticMethodID(env, mat_jAudioRecordingServiceClass, "notifyRecordingStatus", "(Z)V");
    mat_jAudioRecordingService_notifyRecordingChunk = (*env)->GetStaticMethodID(env, mat_jAudioRecordingServiceClass, "notifyRecordingChunk", "(Ljava/lang/String;)V");

    AttachLog(@"Initialize IOSAudioRecordingService");

    format = [[NSDateFormatter alloc] init];
    [format setDateFormat:@"yyyy-MM-dd.HH-mm-ss.SSS"];

    _audioRecording = [[AudioRecording alloc] init];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_audiorecording_impl_IOSAudioRecordingService_startAudioRecording
(JNIEnv *env, jclass jClass, jstring jAudioFolderName, jfloat sampleRate, jint sampleSizeInBits, jint channels, jint chunkRecordTime)
{
    dataPath = [[NSMutableString alloc] init];
    
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    const jchar *charsAudioFolder = (*env)->GetStringChars(env, jAudioFolderName, NULL);
    NSString *audioFolder = [NSString stringWithCharacters:(UniChar *)charsAudioFolder length:(*env)->GetStringLength(env, jAudioFolderName)];
    (*env)->ReleaseStringChars(env, jAudioFolderName, charsAudioFolder);
    
    [dataPath setString:[[paths objectAtIndex:0] stringByAppendingPathComponent:audioFolder]];

    timerInterval = chunkRecordTime;

    // Define the recorder setting
    recordSettings = [[NSMutableDictionary alloc] init];
    
    [recordSettings setValue:[NSNumber numberWithInt:kAudioFormatLinearPCM] forKey:AVFormatIDKey];
    [recordSettings setValue:[NSNumber numberWithFloat:sampleRate] forKey:AVSampleRateKey];
    [recordSettings setValue:[NSNumber numberWithInt:AVAudioQualityMin] forKey:AVEncoderAudioQualityKey];
    [recordSettings setValue:[NSNumber numberWithInt:sampleSizeInBits] forKey:AVEncoderBitRateKey];
    [recordSettings setValue:[NSNumber numberWithInt:channels] forKey:AVNumberOfChannelsKey];
    
    AttachLog(@"Start Recording");
    [_audioRecording playAudioRecorder];

    return;
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_audiorecording_impl_IOSAudioRecordingService_stopAudioRecording
(JNIEnv *env, jclass jClass)
{
    AttachLog(@"Stop Recording");
    [_audioRecording stopAudioRecorder];
    return;   
}

void sendRecordingStatus(BOOL recording) {
    AttachLog(@"Recording Status is %s", recording ? "true" : "false");
    (*env)->CallStaticVoidMethod(env, mat_jAudioRecordingServiceClass, mat_jAudioRecordingService_notifyRecordingStatus, recording ? JNI_TRUE : JNI_FALSE);
}

void sendRecordingChunk(NSString *fileName) {
    if (debugAttach) {
        AttachLog(@"Send chunk file: %@", fileName);
    }
    const char *chunkChars = [fileName UTF8String];
    jstring arg = (*env)->NewStringUTF(env, chunkChars);
    (*env)->CallStaticVoidMethod(env, mat_jAudioRecordingServiceClass, mat_jAudioRecordingService_notifyRecordingChunk, arg);
    (*env)->DeleteLocalRef(env, arg);
}

@implementation AudioRecording 
@synthesize restart;

- (void)playAudioRecorder 
{
    if(_avAudioRecorderController)
    {
        _avAudioRecorderController = nil;
    }
    
    // set audio session
    AVAudioSession *session = [AVAudioSession sharedInstance];
    [session setCategory:AVAudioSessionCategoryRecord error:nil];
    [session setActive:YES error:nil];

    // Check mic permission
    [session requestRecordPermission:^(BOOL granted) {
        dispatch_async(dispatch_get_main_queue(), ^{
            if (granted) {
                [self logMessage:@"Microphone enabled"];
                [self startRecording:session];
            }
            else {
                AttachLog(@"Microphone disabled");
            }
        });
    }];
}

- (void) startRecording:(AVAudioSession *)session
{
    // Initiate and prepare the recorder
    counter = 0;
    NSString *recorderFilePath = [NSString stringWithFormat:@"%@/audioFile.wav", dataPath];
    [self logMessage:@"recorderFilePath: %@",recorderFilePath];
    NSURL *outputFileURL = [NSURL fileURLWithPath:recorderFilePath];
    
    NSError *error = nil;
    _avAudioRecorderController = [[AVAudioRecorder alloc] initWithURL:outputFileURL settings:recordSettings error:&error];
    _avAudioRecorderController.delegate = self;
    _avAudioRecorderController.meteringEnabled = YES;
    if ([_avAudioRecorderController prepareToRecord] == YES) {
        restart = NO;
        [self logMessage:@"starting timer"];
        _timer = [[NSTimer alloc] initWithFireDate: [NSDate dateWithTimeIntervalSinceNow: timerInterval]
                              interval: timerInterval
                              target: self
                              selector:@selector(restartAudioRecorder)
                              userInfo:nil repeats:YES];
        
        NSRunLoop *runner = [NSRunLoop currentRunLoop];
        [runner addTimer:_timer forMode: NSDefaultRunLoopMode];

        [_avAudioRecorderController record];
        [self logMessage:@"recording started"];
        sendRecordingStatus(YES);
    } else {
        AttachLog(@"Error recorder: %@ %@ %@", [error domain], [error localizedDescription], [[error userInfo] description]);
        AttachLog(@"recording failed");
    }
}

- (void)restartAudioRecorder
{
    if(!_avAudioRecorderController)
    {
        return;
    }
    [self logMessage:@"restart recorder"];
    restart = YES;
    [_avAudioRecorderController stop];
}

- (void)stopAudioRecorder
{
    if(!_avAudioRecorderController)
    {
        return;
    }
    [self logMessage:@"stop recorder"];
    restart = NO;
    [_avAudioRecorderController stop];
}

- (void) audioRecorderDidFinishRecording:(AVAudioRecorder *)avrecorder successfully:(BOOL)flag {
    NSString *recorderFilePath = [NSString stringWithFormat:@"%@/audioFile.wav", dataPath];
    NSString *recorderFileName = [NSString stringWithFormat:@"audioFile-%03d-%@.wav", counter, [format stringFromDate:NSDate.date]];
    NSString *recorderFileFinalPath = [NSString stringWithFormat:@"%@/%@", dataPath, recorderFileName];
    counter = counter + 1;
    [self logMessage:@"Copy to recorderFileFinalPath: %@",recorderFileFinalPath];

    NSFileManager *fileManager = [NSFileManager defaultManager];
    NSError *error;
    BOOL result = [fileManager copyItemAtPath:recorderFilePath toPath:recorderFileFinalPath error:&error];
    if(!result) {
        AttachLog(@"Error copying file: %@", error);
    }
    sendRecordingChunk(recorderFileName);
    if (restart) {
        [fileManager release];
        [self logMessage:@"stopped and restarted"];
     
        if(_avAudioRecorderController)
        {
            // resume recording again
            [_avAudioRecorderController record];
        }
        
    } else {
        [self logMessage:@"Finished recording"];
        [fileManager removeItemAtPath:recorderFilePath error:&error];
        [fileManager release];      

        if(_timer)
        {   
            [_timer invalidate];
            [_timer release];
        }
        if(_avAudioRecorderController)
        {
            [recordSettings release];
            [_avAudioRecorderController release];
            sendRecordingStatus(NO);
        }
    }
}

- (void) logMessage:(NSString *)format, ...;
{
    if (debugAttach) 
    {
        va_list args;
        va_start(args, format);
        NSLogv([@"[Debug] " stringByAppendingString:format], args);
        va_end(args);
    }
}
@end