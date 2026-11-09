/*
 * Copyright (c) 2026, Gluon
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
#import <Foundation/Foundation.h>
#import <Security/Security.h>
#include "jni.h"
#include "AttachMacros.h"

JNIEnv *env;

JNIEXPORT jint JNICALL
JNI_OnLoad_Credentials(JavaVM *vm, void *reserved)
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

// The restore credential is a generic password item, private to the app, identified by
// a service derived from the bundle identifier and a fixed account name.
static NSString *credentialsService() {
    NSString *bundleId = [[NSBundle mainBundle] bundleIdentifier];
    if (bundleId == nil) {
        bundleId = @"com.gluonhq.attach";
    }
    return [bundleId stringByAppendingString:@".credentials"];
}

static NSString *const kRestoreCredentialAccount = @"restore-credential";

// Base query matching the restore credential item, whether synchronizable or not
static NSMutableDictionary *restoreCredentialQuery() {
    NSMutableDictionary *query = [NSMutableDictionary dictionary];
    query[(__bridge id)kSecClass] = (__bridge id)kSecClassGenericPassword;
    query[(__bridge id)kSecAttrService] = credentialsService();
    query[(__bridge id)kSecAttrAccount] = kRestoreCredentialAccount;
    query[(__bridge id)kSecAttrSynchronizable] = (__bridge id)kSecAttrSynchronizableAny;
    return query;
}

JNIEXPORT jint JNICALL Java_com_gluonhq_attach_credentials_impl_IOSCredentialsService_nativeStore
(JNIEnv *env, jclass jClass, jstring jValue, jboolean jSynchronizable)
{
    const jchar *chars = (*env)->GetStringChars(env, jValue, NULL);
    NSString *value = [NSString stringWithCharacters:(UniChar *)chars length:(*env)->GetStringLength(env, jValue)];
    (*env)->ReleaseStringChars(env, jValue, chars);

    NSData *data = [value dataUsingEncoding:NSUTF8StringEncoding];
    if (data == nil || [data length] == 0) {
        AttachLog(@"Restore credential cannot be empty");
        return (jint) errSecParam;
    }
    BOOL synchronizable = (jSynchronizable == JNI_TRUE);

    // replace any previous item, synchronizable or not
    OSStatus deleteStatus = SecItemDelete((__bridge CFDictionaryRef) restoreCredentialQuery());
    if (deleteStatus != errSecSuccess && deleteStatus != errSecItemNotFound) {
        AttachLog(@"Error deleting previous restore credential, OSStatus: %d", (int) deleteStatus);
    }

    NSMutableDictionary *attributes = [NSMutableDictionary dictionary];
    attributes[(__bridge id)kSecClass] = (__bridge id)kSecClassGenericPassword;
    attributes[(__bridge id)kSecAttrService] = credentialsService();
    attributes[(__bridge id)kSecAttrAccount] = kRestoreCredentialAccount;
    attributes[(__bridge id)kSecValueData] = data;
    attributes[(__bridge id)kSecAttrSynchronizable] = synchronizable ? @YES : @NO;
    // readable after first unlock, so the app can restore the session in the background;
    // the device-only class is never migrated to another device
    attributes[(__bridge id)kSecAttrAccessible] = synchronizable
            ? (__bridge id)kSecAttrAccessibleAfterFirstUnlock
            : (__bridge id)kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly;

    OSStatus status = SecItemAdd((__bridge CFDictionaryRef) attributes, NULL);
    if (status != errSecSuccess) {
        AttachLog(@"Error storing restore credential, OSStatus: %d", (int) status);
    } else if (debugAttach) {
        AttachLog(@"Restore credential stored, synchronizable: %d", synchronizable);
    }
    return (jint) status;
}

JNIEXPORT jstring JNICALL Java_com_gluonhq_attach_credentials_impl_IOSCredentialsService_nativeGet
(JNIEnv *env, jclass jClass, jintArray jStatus)
{
    NSMutableDictionary *query = restoreCredentialQuery();
    query[(__bridge id)kSecReturnData] = @YES;
    query[(__bridge id)kSecMatchLimit] = (__bridge id)kSecMatchLimitOne;

    CFTypeRef result = NULL;
    OSStatus status = SecItemCopyMatching((__bridge CFDictionaryRef) query, &result);
    jint statusValue = (jint) status;
    (*env)->SetIntArrayRegion(env, jStatus, 0, 1, &statusValue);

    if (status != errSecSuccess) {
        if (status == errSecItemNotFound) {
            if (debugAttach) {
                AttachLog(@"No restore credential found");
            }
        } else {
            AttachLog(@"Error reading restore credential, OSStatus: %d", (int) status);
        }
        return NULL;
    }

    // non-ARC: SecItemCopyMatching returns a +1 reference
    NSData *data = (NSData *) result;
    NSString *value = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    CFRelease(result);
    if (value == nil) {
        AttachLog(@"Restore credential is not a valid UTF-8 string");
        statusValue = (jint) errSecDecode;
        (*env)->SetIntArrayRegion(env, jStatus, 0, 1, &statusValue);
        return NULL;
    }
    if (debugAttach) {
        AttachLog(@"Restore credential read");
    }
    jstring jValue = (*env)->NewStringUTF(env, [value UTF8String]);
    [value release];
    return jValue;
}

JNIEXPORT jint JNICALL Java_com_gluonhq_attach_credentials_impl_IOSCredentialsService_nativeClear
(JNIEnv *env, jclass jClass)
{
    OSStatus status = SecItemDelete((__bridge CFDictionaryRef) restoreCredentialQuery());
    if (status == errSecSuccess) {
        if (debugAttach) {
            AttachLog(@"Restore credential deleted");
        }
    } else if (status == errSecItemNotFound) {
        if (debugAttach) {
            AttachLog(@"No restore credential to delete");
        }
    } else {
        AttachLog(@"Error deleting restore credential, OSStatus: %d", (int) status);
    }
    return (jint) status;
}
