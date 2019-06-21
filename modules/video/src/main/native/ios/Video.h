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
#import <UIKit/UIKit.h>
#include "jni.h"
#include "AttachMacros.h"
#import <AVFoundation/AVFoundation.h>
#import <AVKit/AVKit.h>

typedef NS_ENUM(NSInteger, MediaPlayerStatus) {
    MediaPlayerStatusUnknown,
    MediaPlayerStatusReady,
    MediaPlayerStatusPaused,
    MediaPlayerStatusPlaying,
    MediaPlayerStatusStopped,
    MediaPlayerStatusDisposed
};

@interface Video :UIViewController <AVPlayerViewControllerDelegate, UIGestureRecognizerDelegate>
{
}
    @property (nonatomic, strong) NSArray *arrayOfPlaylist;
    @property (nonatomic, strong) AVPlayerViewController *avPlayerViewcontroller;
    @property (nonatomic, strong) dispatch_semaphore_t semaphore;

    - (void) initPlaylist:(NSArray *)playlist;
    - (void) showVideo;
    - (void) playVideo;
    - (void) pauseVideo;
    - (void) stopVideo;
    - (void) hideVideo;
    - (void) internalHide;
    - (void) fullScreenVideo: (BOOL) value;
    - (void) currentIndex: (int) index;
    - (void) resizeRelocateVideo;
@end

void status(MediaPlayerStatus status);
void updateFullScreen(BOOL value);
void updateCurrentIndex(int index);
