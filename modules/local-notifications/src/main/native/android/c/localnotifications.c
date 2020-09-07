/*
 * Copyright (c) 2020, Gluon
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
#include "localnotifications.h"

// Graal handles
static jclass jGraalLocalNotificationsClass;
static jmethodID jGraalLocalNotificationsProcessMethod;

static jobject jDalvikLocalNotificationsService;
static jmethodID jLocalNotificationsServiceScheduleNotification;
static jmethodID jLocalNotificationsServiceUnscheduleNotification;

static void initializeGraalHandles(JNIEnv* env) {
    jGraalLocalNotificationsClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/localnotifications/impl/AndroidLocalNotificationsService"));
    jGraalLocalNotificationsProcessMethod = (*env)->GetStaticMethodID(env, jGraalLocalNotificationsClass, "processRuntimeArgs", "(Ljava/lang/String;Ljava/lang/String;)V");
}

static void initializeLocalNotificationsDalvikHandles() {
    jclass activityClass = substrateGetActivityClass();
    jclass jLocalNotificationsServiceClass = substrateGetLocalNotificationsServiceClass();

    ATTACH_DALVIK();
    jLocalNotificationsServiceScheduleNotification = (*dalvikEnv)->GetMethodID(dalvikEnv, jLocalNotificationsServiceClass, "scheduleNotification", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;J)V");
    jLocalNotificationsServiceUnscheduleNotification = (*dalvikEnv)->GetMethodID(dalvikEnv, jLocalNotificationsServiceClass, "unscheduleNotification", "(Ljava/lang/String;)V");

    jmethodID jLocalNotificationsServiceInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jLocalNotificationsServiceClass, "<init>", "(Landroid/app/Activity;)V");
    jthrowable t = (*dalvikEnv)->ExceptionOccurred(dalvikEnv);
    if (t) {
        ATTACH_LOG_INFO("EXCEPTION occurred when dealing with dalvik handles\n");
        (*dalvikEnv)->ExceptionClear(dalvikEnv);
    }

    jobject jActivity = substrateGetActivity();
    jobject jObj = (*dalvikEnv)->NewObject(dalvikEnv, jLocalNotificationsServiceClass, jLocalNotificationsServiceInitMethod, jActivity);
    jDalvikLocalNotificationsService = (jobject)(*dalvikEnv)->NewGlobalRef(dalvikEnv, jObj);
    DETACH_DALVIK();
}

//////////////////////////
// From Graal to native //
//////////////////////////

JNIEXPORT jint JNICALL
JNI_OnLoad_localnotifications(JavaVM *vm, void *reserved)
{
#ifdef JNI_VERSION_1_8
    JNIEnv* graalEnv;
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native LocalNotifications from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("Initializing native LocalNotifications from OnLoad");
    initializeGraalHandles(graalEnv);
    initializeLocalNotificationsDalvikHandles();
    ATTACH_LOG_FINE("Initializing native LocalNotifications from OnLoad Done");
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

// from Java to Android

JNIEXPORT void JNICALL Java_com_gluonhq_attach_localnotifications_impl_AndroidLocalNotificationsService_registerNotification
(JNIEnv *env, jobject obj, jstring jTitle, jstring jText, jstring jIdentifier, jstring jImagePath, jlong millis)
{
    const char *titleChars = (*env)->GetStringUTFChars(env, jTitle, NULL);
    const char *textChars = (*env)->GetStringUTFChars(env, jText, NULL);
    const char *idChars = (*env)->GetStringUTFChars(env, jIdentifier, NULL);
    const char *imageChars = (*env)->GetStringUTFChars(env, jImagePath, NULL);
    ATTACH_DALVIK();
    jstring dtitle = (*dalvikEnv)->NewStringUTF(dalvikEnv, titleChars);
    jstring dtext = (*dalvikEnv)->NewStringUTF(dalvikEnv, textChars);
    jstring did = (*dalvikEnv)->NewStringUTF(dalvikEnv, idChars);
    jstring dimage = (*dalvikEnv)->NewStringUTF(dalvikEnv, imageChars);
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikLocalNotificationsService, jLocalNotificationsServiceScheduleNotification, dtitle, dtext, did, dimage, millis);
    DETACH_DALVIK();
    (*env)->ReleaseStringUTFChars(env, jTitle, titleChars);
    (*env)->ReleaseStringUTFChars(env, jText, textChars);
    (*env)->ReleaseStringUTFChars(env, jIdentifier, idChars);
    (*env)->ReleaseStringUTFChars(env, jImagePath, imageChars);
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_localnotifications_impl_IOSLocalNotificationsService_unregisterNotification
(JNIEnv *env, jclass jClass, jstring jIdentifier)
{
    const char *idChars = (*env)->GetStringUTFChars(env, jIdentifier, NULL);
    ATTACH_DALVIK();
    jstring did = (*dalvikEnv)->NewStringUTF(dalvikEnv, idChars);
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikLocalNotificationsService, jLocalNotificationsServiceUnscheduleNotification, did);
    DETACH_DALVIK();
    (*env)->ReleaseStringUTFChars(env, jIdentifier, idChars);
}

///////////////////////////
// From Dalvik to native //
///////////////////////////

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_NotificationActivity_processRuntimeArgs
    (JNIEnv *env, jobject service, jstring jkey, jstring jvalue) {
    const char *keyChars = (*env)->GetStringUTFChars(env, jkey, NULL);
    const char *valueChars = (*env)->GetStringUTFChars(env, jvalue, NULL);
    if (debugAttach) {
        ATTACH_LOG_FINE("LocalNotifications:: Native layer got key: %s, value: %s", keyChars, valueChars);
    }
    ATTACH_GRAAL();
    jstring jKeyChars = (*graalEnv)->NewStringUTF(graalEnv, keyChars);
    jstring jValueChars = (*graalEnv)->NewStringUTF(graalEnv, valueChars);
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalLocalNotificationsClass, jGraalLocalNotificationsProcessMethod, jKeyChars, jValueChars);
    (*graalEnv)->DeleteLocalRef(graalEnv, jKeyChars);
    (*graalEnv)->DeleteLocalRef(graalEnv, jValueChars);
    DETACH_GRAAL();
    (*env)->ReleaseStringUTFChars(env, jkey, keyChars);
    (*env)->ReleaseStringUTFChars(env, jvalue, valueChars);
}