/*
 * Copyright (c) 2018, 2019, Gluon
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
#include "jni.h"
#import <UIKit/UIKit.h>
#import <SceneKit/SceneKit.h>
#import <ARKit/ARKit.h>
#include <stdarg.h>

static __inline__ void CharmLog(const char *file, int lineNumber, const char *funcName, NSString* format, ...)
{
    va_list argList;
    va_start(argList, format);
    NSString* formattedMessage = [[NSString alloc] initWithFormat: format arguments: argList];
    va_end(argList);
    NSLog(@"%@", formattedMessage);

    /* Uncomment for GluonVM:
    static NSDateFormatter* dateFormatter;
    if (!dateFormatter) {
        dateFormatter = [[NSDateFormatter alloc] init];
        [dateFormatter setDateFormat:@"yyyy-MM-dd HH:mm:ss.SSS"];
        [dateFormatter setTimeZone:[NSTimeZone systemTimeZone]];
    }
    fprintf(stderr, "%s %s:%3d %s\n", [[dateFormatter stringFromDate:[NSDate date]] UTF8String], funcName, lineNumber, [formattedMessage UTF8String]);
    [formattedMessage release];
    */
}

#define NSLog(MSG, ...) CharmLog(__FILE__, __LINE__, __PRETTY_FUNCTION__, MSG, ## __VA_ARGS__ )

API_AVAILABLE(ios(11.3))
@interface AugmentedReality : UIViewController <ARSCNViewDelegate, ARSessionDelegate>
{
}

    @property(nonatomic, strong) IBOutlet ARSCNView *sceneView API_AVAILABLE(ios(11.3));
    @property(nonatomic, strong) IBOutlet UIButton *cancelButton;
    @property(nonatomic, strong) IBOutlet NSString *modelFileName;

    - (void) showAR;
    - (void)setARModel:(NSString *)fileName scale:(double)scale;
    - (void) hideAR;
@end

void sendCancelled();