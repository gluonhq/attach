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
#include "browser.h"


static jobject jDalvikBrowserService;
static jmethodID jBrowserServiceLaunchMethod;

static void initializeDalvikHandles() {
    jclass jBrowserServiceClass = substrateGetBrowserServiceClass();
    ATTACH_DALVIK();
    jmethodID jBrowserServiceInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jBrowserServiceClass, "<init>", "(Landroid/app/Activity;)V");
    jBrowserServiceLaunchMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jBrowserServiceClass, "launchURL", "(Ljava/lang/String;)Z");

    jobject jActivity = substrateGetActivity();
    jobject jtmpobj = (*dalvikEnv)->NewObject(dalvikEnv, jBrowserServiceClass, jBrowserServiceInitMethod, jActivity);
    jDalvikBrowserService = (*dalvikEnv)->NewGlobalRef(dalvikEnv, jtmpobj);
    DETACH_DALVIK();
}

//////////////////////////
// From Graal to native //
//////////////////////////


JNIEXPORT jint JNICALL
JNI_OnLoad_browser(JavaVM *vm, void *reserved)
{
    ATTACH_LOG_INFO("JNI_OnLoad_browser called");
#ifdef JNI_VERSION_1_8
    JNIEnv* graalEnv;
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native Browser from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("[Browser Service] Initializing native Browser from OnLoad");
    initializeDalvikHandles();
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

// from Java to Android

JNIEXPORT jboolean JNICALL Java_com_gluonhq_attach_browser_impl_AndroidBrowserService_launchURL
(JNIEnv *env, jclass jClass, jstring jurl)
{
    const char *urlChars = (*env)->GetStringUTFChars(env, jurl, NULL);
    ATTACH_DALVIK();
    jstring durl = (*dalvikEnv)->NewStringUTF(dalvikEnv, urlChars);
    jboolean result = (*dalvikEnv)->CallBooleanMethod(dalvikEnv, jDalvikBrowserService, jBrowserServiceLaunchMethod, durl);
    DETACH_DALVIK();
    // (*env)->ReleaseStringUTFChars(env, jurl, urlChars);
    return result;
}
