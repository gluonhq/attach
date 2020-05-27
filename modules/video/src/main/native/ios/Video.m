/*
 * Copyright (c) 2017, 2020, Gluon
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
#include "Video.h"

JNIEnv *env;

JNIEXPORT jint JNICALL
JNI_OnLoad_Video(JavaVM *vm, void *reserved)
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

static int VideoInited = 0;

// Video
jclass mat_jVideoServiceClass;
jmethodID mat_jVideoService_updateStatus = 0;
jmethodID mat_jVideoService_updateFullScreen = 0;
jmethodID mat_jVideoService_updateCurrentIndex = 0;

Video *_video;
UIView *_currentView;
UIViewController *rootViewController;

BOOL init;
int currentMediaIndex = 0;
NSString *videoName;
NSURL *urlVideoFile;
BOOL showing;
MediaPlayerStatus videoStatus = (MediaPlayerStatus) MediaPlayerStatusUnknown;
    
BOOL isVideo;
BOOL loop;
BOOL useControls;
BOOL fullScreenMode;
int alignH;
int alignV;
double topPadding, rightPadding, bottomPadding, leftPadding;

JNIEXPORT void JNICALL Java_com_gluonhq_attach_video_impl_IOSVideoService_initVideo
(JNIEnv *env, jclass jClass)
{
    if (VideoInited)
    {
        return;
    }
    VideoInited = 1;
    
    mat_jVideoServiceClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/video/impl/IOSVideoService"));
    mat_jVideoService_updateStatus = (*env)->GetStaticMethodID(env, mat_jVideoServiceClass, "updateStatus", "(I)V");
    mat_jVideoService_updateFullScreen = (*env)->GetStaticMethodID(env, mat_jVideoServiceClass, "updateFullScreen", "(Z)V");
    mat_jVideoService_updateCurrentIndex = (*env)->GetStaticMethodID(env, mat_jVideoServiceClass, "updateCurrentIndex", "(I)V");

    AttachLog(@"Init Video");
    _video = [[Video alloc] init];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_video_impl_IOSVideoService_setVideoPlaylist
(JNIEnv *env, jclass jClass, jobjectArray jPlaylistArray)
{
    int playlistCount = (*env)->GetArrayLength(env, jPlaylistArray);
    NSMutableArray *playItems = [[NSMutableArray alloc] init];

    for (int i = 0; i < playlistCount; i++) {
        jstring jplayItem = (jstring) ((*env)->GetObjectArrayElement(env, jPlaylistArray, i));
        const jchar *playItemString = (*env)->GetStringChars(env, jplayItem, NULL);
        NSString *playItem = [NSString stringWithCharacters:(UniChar *)playItemString length:(*env)->GetStringLength(env, jplayItem)];
        (*env)->ReleaseStringChars(env, jplayItem, playItemString);
        [playItems addObject:playItem];
    }
    if (debugAttach) {
        AttachLog(@"Added video playlist with %lu items", (unsigned long)[playItems count]);
    }
    [_video initPlaylist:playItems];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_video_impl_IOSVideoService_showVideo
(JNIEnv *env, jclass jClass, jstring jTitle)
{
    if (_video) 
    {
        [_video showVideo];
    }
    return;
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_video_impl_IOSVideoService_playVideo
(JNIEnv *env, jclass jClass, jstring jTitle)
{
    if (_video) 
    {
        [_video playVideo];
    }
    return;
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_video_impl_IOSVideoService_pauseVideo
(JNIEnv *env, jclass jClass)
{
    if (_video) 
    {
        [_video pauseVideo];
    }
    return;   
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_video_impl_IOSVideoService_stopVideo
(JNIEnv *env, jclass jClass)
{
    if (_video) 
    {
        [_video stopVideo];
    }
    return;   
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_video_impl_IOSVideoService_hideVideo
(JNIEnv *env, jclass jClass)
{
    if (_video) 
    {
        [_video hideVideo];
    }
    return;   
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_video_impl_IOSVideoService_looping
(JNIEnv *env, jclass jClass, jboolean jLooping)
{
    loop = jLooping;
    return;   
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_video_impl_IOSVideoService_controlsVisible
(JNIEnv *env, jclass jClass, jboolean jControls)
{
    useControls = jControls;
    return;   
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_video_impl_IOSVideoService_setFullScreenMode
(JNIEnv *env, jclass jClass, jboolean jfullscreen)
{
    if (_video) 
    {
        [_video fullScreenVideo:jfullscreen];
    }
    return;   
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_video_impl_IOSVideoService_currentIndex
(JNIEnv *env, jclass jClass, jint jindex)
{
    if (_video) 
    {
        [_video currentIndex:jindex];
    }
    return;   
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_video_impl_IOSVideoService_setPosition
(JNIEnv *env, jclass jClass, jstring jalignmentH, jstring jalignmentV, jdouble jtopPadding, 
        jdouble jrightPadding, jdouble jbottomPadding, jdouble jleftPadding)
{
    const jchar *charsAlignH = (*env)->GetStringChars(env, jalignmentH, NULL);
    NSString *sAlignH = [NSString stringWithCharacters:(UniChar *)charsAlignH length:(*env)->GetStringLength(env, jalignmentH)];
    (*env)->ReleaseStringChars(env, jalignmentH, charsAlignH);

    const jchar *charsAlignV = (*env)->GetStringChars(env, jalignmentV, NULL);
    NSString *sAlignV = [NSString stringWithCharacters:(UniChar *)charsAlignV length:(*env)->GetStringLength(env, jalignmentV)];
    (*env)->ReleaseStringChars(env, jalignmentV, charsAlignV);
    if (debugAttach) {
        AttachLog(@"Video Alignment H: %@, V: %@", sAlignH, sAlignV);
    }

    if ([sAlignH isEqualToString:@"LEFT"]) {
        alignH = -1;
    } else if ([sAlignH isEqualToString:@"RIGHT"]) {
        alignH = 1;
    } else {
        alignH = 0;
    }
    if ([sAlignV isEqualToString:@"TOP"]) {
        alignV = -1;
    } else if ([sAlignV isEqualToString:@"BOTTOM"]) {
        alignV = 1;
    } else {
        alignV = 0;
    }
    topPadding = jtopPadding;
    rightPadding = jrightPadding;
    bottomPadding = jbottomPadding;
    leftPadding = jleftPadding;

    [_video resizeRelocateVideo];
    return;
}

void status(MediaPlayerStatus status) {
    videoStatus = status;
    if (debugAttach) {
        AttachLog(@"Media Player Status: %ld", (long) status);
    }
    (*env)->CallStaticVoidMethod(env, mat_jVideoServiceClass, mat_jVideoService_updateStatus, status);
}

void updateFullScreen(BOOL value) {
    fullScreenMode = value;
    (*env)->CallStaticVoidMethod(env, mat_jVideoServiceClass, mat_jVideoService_updateFullScreen, (value) ? JNI_TRUE : JNI_FALSE);
}

void updateCurrentIndex(int index) {
    currentMediaIndex = index;
    (*env)->CallStaticVoidMethod(env, mat_jVideoServiceClass, mat_jVideoService_updateCurrentIndex, index);
}

@implementation Video 

- (void) initPlaylist:(NSArray *)playlist
{
    if (_arrayOfPlaylist) {
        [self logMessage:@"Update playlist"];
        if ([playlist count] == 0) {
            [self hideVideo];
        } else if ([videoName length] > 0) {
            if (! [playlist containsObject: videoName]) {
                if (currentMediaIndex == 0) {
                    currentMediaIndex = -1;
                }
                [self logMessage:@"Update playlist to index 0"];
                [self currentIndex:0];
            } else {
                NSUInteger index = [playlist indexOfObject:videoName];
                if (index != currentMediaIndex) {
                    [self logMessage:@"Update playlist from index %d to new index %d", currentMediaIndex, index];
                    updateCurrentIndex(index);
                }
            }
        }
    }
    _arrayOfPlaylist = [[NSArray alloc] initWithArray:playlist copyItems:YES];
    [self logMessage:@"Init array %@", _arrayOfPlaylist];
}    

- (void)initVideo
{
    [self logMessage:@"Init window"];
    if(![[UIApplication sharedApplication] keyWindow])
    {
        AttachLog(@"key window was nil");
        return;
    }

    NSArray *views = [[[UIApplication sharedApplication] keyWindow] subviews];
    if(![views count]) {
        AttachLog(@"views size was 0");
        return;
    }

    _currentView = views[0];

    rootViewController = [[[UIApplication sharedApplication] keyWindow] rootViewController];
    if(!rootViewController)
    {
        AttachLog(@"rootViewController was nil");
        return;
    }

    init = YES;
}

- (void)showVideo
{
    if ([_arrayOfPlaylist count] == 0) {
        AttachLog(@"There is no playlist available");
        return;
    }
    
    if (! init) {
        [_video initVideo];
        if (! init) {
            return;
        }
    }
    
    if (showing) {
        [self logMessage:@"Video layer was already added"];
        return;
    }
    
    videoName = [_arrayOfPlaylist objectAtIndex:currentMediaIndex];
    showing = YES;

    if ([self prepareMedia]) {
        [self logMessage:@"Video URL: %@", urlVideoFile.absoluteString];
        [self setupVideo];
    } 
    else {
        [self logMessage:@"Invalid media file found, trying the next one"];
        dispatch_semaphore_signal(_semaphore);
        showing = NO;
        status(MediaPlayerStatusUnknown);
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, 0.3 * NSEC_PER_SEC), dispatch_get_main_queue(), ^{
            [self currentIndex:currentMediaIndex + 1];
        });
    }
}

- (void)playVideo
{
    if ([_arrayOfPlaylist count] == 0) {
        AttachLog(@"There is no playlist available");
        return;
    }
    
    if (videoStatus == MediaPlayerStatusStopped || videoStatus == MediaPlayerStatusDisposed) {
        // rewind
        status(MediaPlayerStatusUnknown);
        [self internalHide];
        updateCurrentIndex(0);
    }

    if (! showing) {
        _semaphore = dispatch_semaphore_create(0);
        runOnMainQueueWithoutDeadlocking(^{
            [self showVideo];
        });
        while (dispatch_semaphore_wait(_semaphore, DISPATCH_TIME_NOW)) { 
            [[NSRunLoop currentRunLoop] runMode:NSDefaultRunLoopMode beforeDate:[NSDate dateWithTimeIntervalSinceNow:10]]; 
        }
        dispatch_release(_semaphore);

        if (videoStatus == MediaPlayerStatusReady) {
            [self logMessage:@"Video start playing [%d/%d]: %@", (currentMediaIndex + 1), [_arrayOfPlaylist count], videoName];
            [_avPlayerViewcontroller.player play];
        }
    } else if (_avPlayerViewcontroller) {
        [self logMessage:@"Video play"];
        [_avPlayerViewcontroller.player play];
    }
}

- (BOOL)prepareMedia
{

    if([[NSFileManager defaultManager] fileExistsAtPath:videoName]) {
        [self logMessage:@"Video from resources"];
        urlVideoFile = [[NSURL alloc] initFileURLWithPath:videoName];
        return YES;
    }
    else if ([[UIApplication sharedApplication] canOpenURL:[NSURL URLWithString:videoName]]) 
    {
        [self logMessage:@"Video from URL"];
        urlVideoFile = [NSURL URLWithString:videoName];
        return YES;
    }
    else
    {
        AttachLog(@"Error: %@ is not a valid name", videoName);
        return NO;
    }
}

- (void) setupVideo
{        
    NSError* error = nil;

     if(_avPlayerViewcontroller)
    {
        runOnMainQueueWithoutDeadlocking(^{
            [self logMessage:@"Adding new item %@", urlVideoFile];
            [_avPlayerViewcontroller.player replaceCurrentItemWithPlayerItem:[AVPlayerItem playerItemWithURL:urlVideoFile]];
            [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(videoDidFinish:) name:AVPlayerItemDidPlayToEndTimeNotification
                object:[_avPlayerViewcontroller.player currentItem]];

            [self resizeRelocateVideo];
            status(MediaPlayerStatusReady);
            [self logMessage:@"Video ready"];
            if (_semaphore) {
                dispatch_semaphore_signal(_semaphore);
            }
        });
    }
    else {
        _avPlayerViewcontroller = [[AVPlayerViewController alloc] init];
        _avPlayerViewcontroller.player = [AVPlayer playerWithURL:urlVideoFile];
    
        if (! useControls) {
            // a pinch gesture allows exiting full screen mode if embedded controls are not available
            // When using embedded controls, a button is provided so the gesture is not required
            _avPlayerViewcontroller.view.userInteractionEnabled = YES;
            UIPinchGestureRecognizer *pinch = [[UIPinchGestureRecognizer alloc] initWithTarget:self action:@selector(pinchAvPlayer:)];
            pinch.delegate = self;
            NSString *ver = [[UIDevice currentDevice] systemVersion];
            float ver_float = [ver floatValue];
            if (ver_float < 8.0) {
                [_avPlayerViewcontroller.view.subviews[0] addGestureRecognizer:pinch];
            } else {
                _avPlayerViewcontroller.contentOverlayView.gestureRecognizers = @[pinch];
            }
        }

        [self resizeRelocateVideo];

        [_currentView addSubview:_avPlayerViewcontroller.view];

        if(!_avPlayerViewcontroller)
        {
            AttachLog(@"Error creating player: %@", error);
            return;
        }
        _avPlayerViewcontroller.showsPlaybackControls = useControls;

        [self logMessage:@"Adding listeners"];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(videoDidFinish:) name:AVPlayerItemDidPlayToEndTimeNotification
            object:[_avPlayerViewcontroller.player currentItem]];

        [_avPlayerViewcontroller.player addObserver:self forKeyPath:@"status" options:0 context:nil];
        [_avPlayerViewcontroller.player addObserver:self forKeyPath:@"rate" options:0 context:nil];
        [_avPlayerViewcontroller.contentOverlayView addObserver:self forKeyPath:@"bounds" options:NSKeyValueObservingOptionNew | NSKeyValueObservingOptionOld context:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(OrientationDidChange:) name:UIDeviceOrientationDidChangeNotification object:nil];
    }
    [self logMessage:@"Finished setupVideo"];
}

- (void)pauseVideo
{
    if(!_avPlayerViewcontroller)
    {
        return;
    }
    [self logMessage:@"Video pause"];
    [_avPlayerViewcontroller.player pause];
}

- (void)stopVideo
{
    if(!_avPlayerViewcontroller)
    {
        return;
    }
    [self logMessage:@"Video stop"];
    [_avPlayerViewcontroller.player pause];
    status(MediaPlayerStatusStopped);
}

- (void)hideVideo
{
    [self internalHide];
    [self dispose];
}

- (void)internalHide
{
    if (showing) {
        @try {
            [[NSNotificationCenter defaultCenter] removeObserver:self name:AVPlayerItemDidPlayToEndTimeNotification object:nil];
        } @catch (NSException *exception) {
            AttachLog(@"Error removing NSNotificationCenter observer: %@", exception);
        }
    }
    if (fullScreenMode) {
        [self fullScreenVideo:NO];
    }
    AttachLog(@"AVPlayer hidden");
    showing = NO;
}

- (void) fullScreenVideo: (BOOL) value
{
    if (value == fullScreenMode || ! isVideo) {
        return;
    }

    if (useControls) {
        AttachLog(@"Please, use the fullscreen button from the embedded controls");
        updateFullScreen(false);
        return;
    }
    fullScreenMode = value;
    [UIView animateKeyframesWithDuration:0.3f
        delay:0.0f
        options:UIViewKeyframeAnimationOptionLayoutSubviews
        animations:^{
            [self resizeRelocateVideo];
        }
        completion:^(BOOL finished){ 
            updateFullScreen(value);
        }
    ];
}

- (void) currentIndex: (int) index
{
    if (index == currentMediaIndex) {
        return;
    }
    [self logMessage:@"Skipping current video from %d to %d", currentMediaIndex, index];
    
    [self pauseVideo];
    [self logMessage:@"Hiding current video file"];
    [self internalHide];
    
    if (0 <= index && index < [_arrayOfPlaylist count]) {
        updateCurrentIndex(index);
        [self logMessage:@"Showing new video file: %d", index];
        [self playVideo];
    } else if (loop) {
        updateCurrentIndex(0);
        [self logMessage:@"Showing first video file"];
        [self playVideo];
    } else {
        [self logMessage:@"Disposing media player"];
        [self dispose];
    }
}

- (void) resizeRelocateVideo 
{
    [self logMessage:@"Video resize and relocate"];
    CGRect theLayerRect = [[UIScreen mainScreen] bounds];
    if (fullScreenMode) {
        [_avPlayerViewcontroller.view setBackgroundColor:[UIColor colorWithRed:0 green:0 blue:0 alpha:1]];
        _avPlayerViewcontroller.view.frame = theLayerRect;
    } 
    else 
    {
        double maxW = theLayerRect.size.width - (leftPadding + rightPadding);
        double maxH = theLayerRect.size.height - (topPadding + bottomPadding);
        [self logMessage:@"Video max size: %f x %f", maxW, maxH];
    
        if ([[_avPlayerViewcontroller.player.currentItem.asset tracksWithMediaType:AVMediaTypeVideo] count] != 0) {
            isVideo = true;
            [_currentView bringSubviewToFront:_avPlayerViewcontroller.view];
            AVAssetTrack *track = [_avPlayerViewcontroller.player.currentItem.asset tracksWithMediaType:AVMediaTypeVideo][0];
            [self logMessage:@"Video track %@", track];

            CGSize theNaturalSize = [track naturalSize];
            theNaturalSize = CGSizeApplyAffineTransform(theNaturalSize, track.preferredTransform);
            if (theNaturalSize.width == 0.0f || theNaturalSize.width == 0.0f) {
                return;
            }
            theNaturalSize.width = fabs(theNaturalSize.width);
            theNaturalSize.height = fabs(theNaturalSize.height);
            [self logMessage:@"Video track natural size %@", NSStringFromCGSize(theNaturalSize)];

            CGFloat movieAspectRatio = theNaturalSize.width / theNaturalSize.height;
            CGFloat viewAspectRatio = maxW / maxH;
            [self logMessage:@"Video movie ratio: %f, view ratio: %f", movieAspectRatio, viewAspectRatio];

            CGRect theVideoRect = CGRectZero;
            [self logMessage:@"Video set video rect: %@", NSStringFromCGRect(theVideoRect)];

            if (viewAspectRatio < movieAspectRatio) {
                theVideoRect.size.width = maxW;
                theVideoRect.size.height = maxW / movieAspectRatio;
                [self logMessage:@"Video video size %@", NSStringFromCGSize(theVideoRect.size)];
                theVideoRect.origin.x = leftPadding;
                if (alignV == -1) {
                    theVideoRect.origin.y = topPadding;
                } else if (alignV == 0) {
                    theVideoRect.origin.y = topPadding + (maxH - theVideoRect.size.height) / 2;
                } else {
                    theVideoRect.origin.y = topPadding + (maxH - theVideoRect.size.height);
                }
            } else  {
                theVideoRect.size.width = movieAspectRatio * maxH;
                theVideoRect.size.height = maxH;
                [self logMessage:@"Video video size %@", NSStringFromCGSize(theVideoRect.size)];
                if (alignH == -1) {
                    theVideoRect.origin.x = leftPadding;
                } else if (alignH == 0) {
                    theVideoRect.origin.x = leftPadding + (maxW - theVideoRect.size.width) / 2;
                } else {
                    theVideoRect.origin.x = leftPadding + (maxW - theVideoRect.size.width);
                }
                theVideoRect.origin.y = topPadding;
            }
            [self logMessage:@"Video video origin %f x %f", theVideoRect.origin.x, theVideoRect.origin.y];

            [self logMessage:@"Video frame: %@", NSStringFromCGRect(theVideoRect)];
            [_avPlayerViewcontroller.view setBackgroundColor:[UIColor colorWithRed:1 green:1 blue:1 alpha:0]];
            _avPlayerViewcontroller.view.frame = theVideoRect;
        } else {
            isVideo = false;
            _avPlayerViewcontroller.view.frame = CGRectZero;
            [_currentView sendSubviewToBack:_avPlayerViewcontroller.view];
            if ([[_avPlayerViewcontroller.player.currentItem.asset tracksWithMediaType:AVMediaTypeAudio] count] != 0) {
                AVAssetTrack *track = [_avPlayerViewcontroller.player.currentItem.asset tracksWithMediaType:AVMediaTypeAudio][0];
                [self logMessage:@"Audio track %@", track];
            }
        }
    }
}

- (void)videoDidFinish:(NSNotification *)notification {
    [self currentIndex:currentMediaIndex + 1];
}

- (void) dispose 
{
    @try {
        [_avPlayerViewcontroller.player removeObserver:self forKeyPath:@"status" context:nil];
    } @catch (NSException *exception) {
        AttachLog(@"Error removing player status observer: %@", exception);
    }
    @try {
        [_avPlayerViewcontroller.player removeObserver:self forKeyPath:@"rate" context:nil];
    } @catch (NSException *exception) {
        AttachLog(@"Error removing player rate observer: %@", exception);
    }
    @try {
        [_avPlayerViewcontroller.contentOverlayView removeObserver:self forKeyPath:@"bounds" context:nil];
    } @catch (NSException *exception) {
        AttachLog(@"Error removing contentOverlayView observer: %@", exception);
    }
    @try {
        [[NSNotificationCenter defaultCenter] removeObserver:self name:UIDeviceOrientationDidChangeNotification object:nil];
    } @catch (NSException *exception) {
        AttachLog(@"Error removing orientation observer: %@", exception);
    }
    [_avPlayerViewcontroller.player replaceCurrentItemWithPlayerItem:nil];
    [_avPlayerViewcontroller dismissViewControllerAnimated:YES completion:nil];
    [_avPlayerViewcontroller.view removeFromSuperview];
    [_avPlayerViewcontroller release];
    _avPlayerViewcontroller = nil;
    status(MediaPlayerStatusDisposed);
}

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context {
    if (object == _avPlayerViewcontroller.player && [keyPath isEqualToString:@"status"]) 
    {
        if (_avPlayerViewcontroller.player.status == AVPlayerStatusFailed) {
            AttachLog(@"AVPlayer Failed");
            status(MediaPlayerStatusUnknown);
        } else if (_avPlayerViewcontroller.player.status == AVPlayerStatusReadyToPlay) {
            AttachLog(@"AVPlayerStatusReadyToPlay");
            status(MediaPlayerStatusReady);
            [self logMessage:@"Video ready"];
        } else if (_avPlayerViewcontroller.player.status == AVPlayerItemStatusUnknown) {
            AttachLog(@"AVPlayer Unknown");
            status(MediaPlayerStatusUnknown);
        }
        if (_semaphore) {
            dispatch_semaphore_signal(_semaphore);
        }
    }
    else if (object == _avPlayerViewcontroller.player && [keyPath isEqualToString:@"rate"]) 
    {
        if ([_avPlayerViewcontroller.player rate]) {
            status(MediaPlayerStatusPlaying);  // This changes the button to Pause
        }
        else {
            status(MediaPlayerStatusPaused);   // This changes the button to Play
        }
    }
    else if (object == _avPlayerViewcontroller.contentOverlayView && [keyPath isEqualToString:@"bounds"]) 
    {
        CGRect oldBounds = [change[NSKeyValueChangeOldKey] CGRectValue];
        CGRect newBounds = [change[NSKeyValueChangeNewKey] CGRectValue];
        BOOL wasFullscreen = CGRectEqualToRect(oldBounds, [UIScreen mainScreen].bounds);
        BOOL isFullscreen = CGRectEqualToRect(newBounds, [UIScreen mainScreen].bounds);
        if (isFullscreen && !wasFullscreen) 
        {
            if (CGRectEqualToRect(oldBounds, CGRectMake(0, 0, newBounds.size.height, newBounds.size.width))) 
            {
                [self logMessage:@"Video rotated fullscreen"];
                return;
            }
            else 
            {
                [self logMessage:@"Video entered fullscreen"];
            }
            fullScreenMode = YES;
        }
        else if (!isFullscreen && wasFullscreen) 
        {
            [self logMessage:@"Video exited fullscreen"];
            fullScreenMode = NO;
        }

        if (useControls && ((isFullscreen && !wasFullscreen) || (!isFullscreen && wasFullscreen))) {
            // workaround to avoid a bug in one of the subviews constraints.

            CMTime currentTime = _avPlayerViewcontroller.player.currentTime;
            [_avPlayerViewcontroller.player seekToTime:CMTimeMake(0, 1)];

            dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.5 * NSEC_PER_SEC));
            dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
                updateFullScreen(isFullscreen);
                [_avPlayerViewcontroller.player seekToTime:currentTime];
                [_avPlayerViewcontroller.player play];
            });
        }
    }
}

- (void)pinchAvPlayer:(UIPinchGestureRecognizer *)pinchGestureRecognizer {
    UIGestureRecognizerState state = [pinchGestureRecognizer state];

    if (state == UIGestureRecognizerStateEnded)
    {
        CGFloat scale = [pinchGestureRecognizer scale];
        [pinchGestureRecognizer setScale:1.0];
        if ((fullScreenMode && scale < 1.0) || (!fullScreenMode && scale > 1)) {
            [self fullScreenVideo:! fullScreenMode];
        }
    } 
}

void runOnMainQueueWithoutDeadlocking(void (^block)(void))
{
    if ([NSThread isMainThread])
    {
        block();
    }
    else
    {
        dispatch_sync(dispatch_get_main_queue(), block);
    }
}

-(void)OrientationDidChange:(NSNotification*)notification
{
    [self logMessage:@"OrientationDidChange, resizing"];
    [self resizeRelocateVideo];
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