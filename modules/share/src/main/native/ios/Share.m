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
#include "Share.h"

JNIEnv *env;

JNIEXPORT jint JNICALL
JNI_OnLoad_Share(JavaVM *vm, void *reserved)
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

static int ShareInited = 0;

Share *_share;

JNIEXPORT void JNICALL Java_com_gluonhq_attach_share_impl_IOSShareService_initShare
(JNIEnv *env, jclass jClass)
{
    if (ShareInited)
    {
        return;
    }
    ShareInited = 1;
    
    AttachLog(@"Init Share");
    _share = [[Share alloc] init];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_share_impl_IOSShareService_nativeShare
(JNIEnv *env, jclass jClass, jstring jsubject, jstring jmessage, jstring jfilePath) {

    const jchar *charsSubject = (*env)->GetStringChars(env, jsubject, NULL);
    NSString *subject = [NSString stringWithCharacters:(UniChar *)charsSubject length:(*env)->GetStringLength(env, jsubject)];
    (*env)->ReleaseStringChars(env, jsubject, charsSubject);
    
    const jchar *charsMessage = (*env)->GetStringChars(env, jmessage, NULL);
    NSString *message = [NSString stringWithCharacters:(UniChar *)charsMessage length:(*env)->GetStringLength(env, jmessage)];
    (*env)->ReleaseStringChars(env, jmessage, charsMessage);

    const jchar *charsFilePath = (*env)->GetStringChars(env, jfilePath, NULL);
    NSString *filePath = [NSString stringWithCharacters:(UniChar *)charsFilePath length:(*env)->GetStringLength(env, jfilePath)];
    (*env)->ReleaseStringChars(env, jfilePath, charsFilePath);

    [_share shareText: subject message:message filePath:filePath];
    
}

@implementation Share

- (void) shareText: (NSString *)subject message:(NSString *)message filePath:(NSString *)filePath
{
    _subject = [[NSString alloc] initWithString:subject];
    _message = [[NSString alloc] initWithString:message];
    _filePath = [[NSString alloc] initWithString:filePath];
    
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

    UIViewController *rootViewController = [[[UIApplication sharedApplication] keyWindow] rootViewController];
    if(!rootViewController)
    {
        AttachLog(@"rootViewController was nil");
        return;
    }

    NSMutableArray *items = [[NSMutableArray alloc] init];
    [items addObject:self];

    if ([_filePath length] > 0) {
        [self logMessage:@"Share file: %@", _filePath];
        NSURL *fileUrl = [NSURL fileURLWithPath:_filePath];
        NSError *err;
        if ([fileUrl checkResourceIsReachableAndReturnError:&err] == YES)
        {
            [self logMessage:@"Share fileUrl: %@", fileUrl];
            [items addObject:fileUrl];
        } else {
            AttachLog(@"File resource not reachable: %@", err);
        }
    }
    
    NSArray *itemsToShare = [NSArray arrayWithArray:items];
    
    UIActivityViewController *activityViewController = [[UIActivityViewController alloc] initWithActivityItems:itemsToShare applicationActivities:nil];
    if ([activityViewController respondsToSelector:@selector(popoverPresentationController)]) { 
        activityViewController.popoverPresentationController.sourceView = views[0];
    }

    activityViewController.completionWithItemsHandler = ^(NSString *activityType, BOOL completed, NSArray *returnedItems, NSError *activityError){
        [self logMessage:@"Activity Type selected: %@", activityType];
        if (completed) {
            [self logMessage:@"Selected activity was performed."];
        } else {
            if (activityType == NULL) {
                [self logMessage:@"User dismissed the view controller without making a selection."];
            } else {
                [self logMessage:@"Activity was not performed."];
            }
        }
    };

    [rootViewController presentViewController:activityViewController animated:YES completion:nil];

}

- (id)activityViewControllerPlaceholderItem:(UIActivityViewController *)activityViewController
{
    [self logMessage:@"Share activityViewControllerPlaceholderItem"];
    return @"";
}

- (NSString *) activityViewController:(UIActivityViewController *)activityViewController subjectForActivityType:(NSString *)activityType
{
    [self logMessage:@"Share subjectForActivityType %@ - Subject: %@", activityType, _subject];
    return _subject;
}

- (nullable id)activityViewController:(UIActivityViewController *)activityViewController itemForActivityType:(UIActivityType)activityType
{
    [self logMessage:@"Share itemForActivityType %@ - Message: %@", activityType, _message];
    return _message;
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

