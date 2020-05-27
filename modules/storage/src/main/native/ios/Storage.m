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
#import <UIKit/UIKit.h>
#include "jni.h"
#include "AttachMacros.h"

JNIEnv *env;

JNIEXPORT jint JNICALL
JNI_OnLoad_Storage(JavaVM *vm, void *reserved)
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

JNIEXPORT jstring JNICALL Java_com_gluonhq_attach_storage_impl_IOSStorageService_privateStorageURL
(JNIEnv *env, jclass jClass)
{
    NSArray *documentPaths = NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES);
    NSString *documentsDir = [documentPaths objectAtIndex:0];
    NSString *folder = @"gluon";
    NSString *folderPath = [documentsDir stringByAppendingPathComponent:folder];

    if (!folderPath) {
        AttachLog(@"Error getting the private storage path");
        return NULL;
    }   

    NSFileManager *manager = [NSFileManager defaultManager];
    [manager createDirectoryAtPath: folderPath withIntermediateDirectories: NO attributes: nil error: nil];

    if (debugAttach) {
        AttachLog(@"Done creating private storage %@", folderPath);
    }
    const char *valueChars = [folderPath UTF8String];
    return (*env)->NewStringUTF(env, valueChars);
}

JNIEXPORT jstring JNICALL Java_com_gluonhq_attach_storage_impl_IOSStorageService_publicStorageURL
(JNIEnv *env, jclass jClass, jstring jDir)
{
    NSArray *documentPaths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSString *documentsDir = [documentPaths objectAtIndex:0];
    
    const jchar *charsDir = (*env)->GetStringChars(env, jDir, NULL);
    NSString *dir = [NSString stringWithCharacters:(UniChar *)charsDir length:(*env)->GetStringLength(env, jDir)];
    (*env)->ReleaseStringChars(env, jDir, charsDir);
    
    NSString *folderPath = [documentsDir stringByAppendingPathComponent:dir];

    NSFileManager *manager = [NSFileManager defaultManager];
    [manager createDirectoryAtPath: folderPath withIntermediateDirectories: NO attributes: nil error: nil];

    if (!folderPath) {
        AttachLog(@"Error creating public storage path");
        return NULL;
    }   
    if (debugAttach) {
        AttachLog(@"Done creating public storage %@", folderPath);
    }
    const char *valueChars = [folderPath UTF8String];
    return (*env)->NewStringUTF(env, valueChars);
}