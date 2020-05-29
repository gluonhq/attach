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
#include "statusbar.h"

static jobject jDalvikStatusBarService;
static jmethodID jStatusBarServiceColorMethod;

static void initializeStatusBarDalvikHandles() {
    ATTACH_DALVIK();
    jclass jStatusBarServiceClass = substrateGetStatusBarServiceClass();
    jmethodID jStatusBarServiceInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jStatusBarServiceClass, "<init>", "(Landroid/app/Activity;)V");
    jStatusBarServiceColorMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jStatusBarServiceClass, "setColor", "(I)V");

    jobject jActivity = substrateGetActivity();
    jobject jtmpobj = (*dalvikEnv)->NewObject(dalvikEnv, jStatusBarServiceClass, jStatusBarServiceInitMethod, jActivity);
    jDalvikStatusBarService = (*dalvikEnv)->NewGlobalRef(dalvikEnv, jtmpobj);
    DETACH_DALVIK();
}

//////////////////////////
// From Graal to native //
//////////////////////////


JNIEXPORT jint JNICALL
JNI_OnLoad_statusbar(JavaVM *vm, void *reserved)
{
    JNIEnv* graalEnv;
    ATTACH_LOG_INFO("JNI_OnLoad_statusbar called");
#ifdef JNI_VERSION_1_8
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native StatusBar from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("[StatusBar Service] Initializing native StatusBar from OnLoad");
    initializeStatusBarDalvikHandles();
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

// from Java to Android

JNIEXPORT void JNICALL Java_com_gluonhq_attach_statusbar_impl_AndroidStatusBarService_setNativeColor
(JNIEnv *env, jclass jClass, jint color)
{
    ATTACH_DALVIK();
    if (debugAttach) {
        ATTACH_LOG_FINE("Set native color, value: %d", color);
    }
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikStatusBarService, jStatusBarServiceColorMethod, color);
    DETACH_DALVIK();
}