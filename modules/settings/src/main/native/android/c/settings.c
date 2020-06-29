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
#include "settings.h"

static jobject jDalvikSettingsService;
static jmethodID jSettingsServiceStore;
static jmethodID jSettingsServiceRemove;
static jmethodID jSettingsServiceRetrieve;

static void initializeSettingsDalvikHandles() {
    ATTACH_DALVIK();
    jclass jSettingsServiceClass = substrateGetSettingsServiceClass();
    jmethodID jSettingsServiceInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jSettingsServiceClass, "<init>", "(Landroid/app/Activity;)V");
    jSettingsServiceStore = (*dalvikEnv)->GetMethodID(dalvikEnv, jSettingsServiceClass, "store", "(Ljava/lang/String;Ljava/lang/String;)V");
    jSettingsServiceRemove = (*dalvikEnv)->GetMethodID(dalvikEnv, jSettingsServiceClass, "remove", "(Ljava/lang/String;)V");
    jSettingsServiceRetrieve = (*dalvikEnv)->GetMethodID(dalvikEnv, jSettingsServiceClass, "retrieve", "(Ljava/lang/String;)Ljava/lang/String;");

    jobject jActivity = substrateGetActivity();
    jobject jtmpobj = (*dalvikEnv)->NewObject(dalvikEnv, jSettingsServiceClass, jSettingsServiceInitMethod, jActivity);
    jDalvikSettingsService = (*dalvikEnv)->NewGlobalRef(dalvikEnv, jtmpobj);
    DETACH_DALVIK();
}

//////////////////////////
// From Graal to native //
//////////////////////////


JNIEXPORT jint JNICALL
JNI_OnLoad_settings(JavaVM *vm, void *reserved)
{
    JNIEnv* graalEnv;
    ATTACH_LOG_INFO("JNI_OnLoad_settings called");
#ifdef JNI_VERSION_1_8
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native Settings from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("[Settings Service] Initializing native Settings from OnLoad");
    initializeSettingsDalvikHandles();
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

// from Java to Android

JNIEXPORT void JNICALL Java_com_gluonhq_attach_settings_impl_AndroidSettingsService_settingsStore
(JNIEnv *env, jclass jClass, jstring jkey, jstring jvalue)
{
    const char *keyChars = (*env)->GetStringUTFChars(env, jkey, NULL);
    const char *valueChars = (*env)->GetStringUTFChars(env, jvalue, NULL);
    ATTACH_DALVIK();
    jstring dkey = (*dalvikEnv)->NewStringUTF(dalvikEnv, keyChars);
    jstring dvalue = (*dalvikEnv)->NewStringUTF(dalvikEnv, valueChars);
    if (debugAttach) {
        ATTACH_LOG_FINE("Storing settings for = %s, %s\n", keyChars, valueChars);
    }
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikSettingsService, jSettingsServiceStore, dkey, dvalue);
    DETACH_DALVIK();
    // (*env)->ReleaseStringUTFChars(env, jkey, keyChars);
    // (*env)->ReleaseStringUTFChars(env, jvalue, valueChars);
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_settings_impl_AndroidSettingsService_settingsRemove
(JNIEnv *env, jclass jClass, jstring jkey)
{
    const char *keyChars = (*env)->GetStringUTFChars(env, jkey, NULL);
    ATTACH_DALVIK();
    jstring dkey = (*dalvikEnv)->NewStringUTF(dalvikEnv, keyChars);
    if (debugAttach) {
        ATTACH_LOG_FINE("Remove settings for = %s\n", keyChars);
    }
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikSettingsService, jSettingsServiceRemove, dkey);
    DETACH_DALVIK();
    // (*env)->ReleaseStringUTFChars(env, jkey, keyChars);
}

JNIEXPORT jstring JNICALL Java_com_gluonhq_attach_settings_impl_AndroidSettingsService_settingsRetrieve
(JNIEnv *env, jclass jClass, jstring jkey)
{
    const char *keyChars = (*env)->GetStringUTFChars(env, jkey, NULL);
    ATTACH_DALVIK();
    jstring dkey = (*dalvikEnv)->NewStringUTF(dalvikEnv, keyChars);
    if (debugAttach) {
        ATTACH_LOG_FINE("Retrieving settings for = %s\n", keyChars);
    }
    jstring answer = (*dalvikEnv)->CallObjectMethod(dalvikEnv, jDalvikSettingsService, jSettingsServiceRetrieve, dkey);
    if (answer == NULL) {
        if (debugAttach) {
            ATTACH_LOG_FINE("Error: Settings for = %s not found\n", keyChars);
        }
        DETACH_DALVIK();
        return NULL;
    }
    const char *answerChars = (*dalvikEnv)->GetStringUTFChars(dalvikEnv, answer, 0);
    if (debugAttach) {
        ATTACH_LOG_FINE("Retrieved settings for = %s\n", answerChars);
    }
    DETACH_DALVIK();
    // (*env)->ReleaseStringUTFChars(env, jkey, keyChars);
    return (*env)->NewStringUTF(env, answerChars);
}
