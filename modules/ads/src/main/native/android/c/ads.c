/*
 * Copyright (c) 2020, 2023, Gluon
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
#include "util.h"

static jclass jGraalAdsClass;
static jmethodID jGraalInvokeCallbackMethod;

static jclass jAdsServiceClass;
static jobject jDalvikAdsService;
static jmethodID jAdsServiceInitialize;
static jmethodID jAdsServiceSetRequestConfiguration;
static jmethodID jAdsServiceBannerAdNew;
static jmethodID jAdsServiceBannerAdLoad;
static jmethodID jAdsServiceBannerAdShow;
static jmethodID jAdsServiceBannerAdHide;
static jmethodID jAdsServiceBannerAdSetLayout;
static jmethodID jAdsServiceBannerAdSetAdSize;
static jmethodID jAdsServiceBannerAdSetAdUnitId;
static jmethodID jAdsServiceBannerAdSetAdListener;
static jmethodID jAdsServiceInterstitialAdLoad;
static jmethodID jAdsServiceInterstitialAdShow;
static jmethodID jAdsServiceInterstitialAdSetFullScreenContentCallback;
static jmethodID jAdsServiceRewardedAdLoad;
static jmethodID jAdsServiceRewardedAdShow;
static jmethodID jAdsServiceRewardedAdSetFullScreenContentCallback;

void initializeGraalHandles(JNIEnv *graalEnv) {
    jGraalAdsClass = (*graalEnv)->NewGlobalRef(graalEnv, (*graalEnv)->FindClass(graalEnv, "com/gluonhq/attach/ads/impl/AndroidAdsService"));
    jGraalInvokeCallbackMethod = (*graalEnv)->GetStaticMethodID(graalEnv, jGraalAdsClass, "invokeCallback", "(JLjava/lang/String;Ljava/lang/String;[Ljava/lang/String;)V");
}

void initializeAdsDalvikHandles() {
    jAdsServiceClass = GET_REGISTER_DALVIK_CLASS(jAdsServiceClass, "com/gluonhq/helloandroid/DalvikAdsService");
    ATTACH_DALVIK();
    jmethodID jAdsServiceInitMethod = (*dalvikEnv)->GetMethodID(dalvikEnv, jAdsServiceClass, "<init>", "(Landroid/app/Activity;)V");

    jAdsServiceInitialize = (*dalvikEnv)->GetMethodID(dalvikEnv, jAdsServiceClass, "initialize", "()V");
    jAdsServiceSetRequestConfiguration = (*dalvikEnv)->GetMethodID(dalvikEnv, jAdsServiceClass, "setRequestConfiguration", "(IILjava/lang/String;[Ljava/lang/String;)V");
    jAdsServiceBannerAdNew = (*dalvikEnv)->GetMethodID(dalvikEnv, jAdsServiceClass, "bannerAdNew", "(J)V");
    jAdsServiceBannerAdLoad = (*dalvikEnv)->GetMethodID(dalvikEnv, jAdsServiceClass, "bannerAdLoad", "(J)V");
    jAdsServiceBannerAdShow =(*dalvikEnv)->GetMethodID(dalvikEnv, jAdsServiceClass, "bannerAdShow", "(J)V");
    jAdsServiceBannerAdHide =(*dalvikEnv)->GetMethodID(dalvikEnv, jAdsServiceClass, "bannerAdHide", "(J)V");
    jAdsServiceBannerAdSetLayout = (*dalvikEnv)->GetMethodID(dalvikEnv, jAdsServiceClass, "bannerAdSetLayout", "(JLjava/lang/String;)V");
    jAdsServiceBannerAdSetAdSize = (*dalvikEnv)->GetMethodID(dalvikEnv, jAdsServiceClass, "bannerAdSetAdSize", "(JLjava/lang/String;)V");
    jAdsServiceBannerAdSetAdUnitId = (*dalvikEnv)->GetMethodID(dalvikEnv, jAdsServiceClass, "bannerAdSetAdUnitId", "(JLjava/lang/String;)V");
    jAdsServiceBannerAdSetAdListener = (*dalvikEnv)->GetMethodID(dalvikEnv, jAdsServiceClass, "bannerAdSetAdListener", "(J)V");
    jAdsServiceInterstitialAdLoad = (*dalvikEnv)->GetMethodID(dalvikEnv, jAdsServiceClass, "interstitialAdLoad", "(JLjava/lang/String;)V");
    jAdsServiceInterstitialAdShow = (*dalvikEnv)->GetMethodID(dalvikEnv, jAdsServiceClass, "interstitialAdShow", "(J)V");
    jAdsServiceInterstitialAdSetFullScreenContentCallback = (*dalvikEnv)->GetMethodID(dalvikEnv, jAdsServiceClass, "interstitialAdSetFullScreenContentCallback", "(J)V");
    jAdsServiceRewardedAdLoad = (*dalvikEnv)->GetMethodID(dalvikEnv, jAdsServiceClass, "rewardedAdLoad", "(JLjava/lang/String;)V");
    jAdsServiceRewardedAdShow = (*dalvikEnv)->GetMethodID(dalvikEnv, jAdsServiceClass, "rewardedAdShow", "(J)V");
    jAdsServiceRewardedAdSetFullScreenContentCallback = (*dalvikEnv)->GetMethodID(dalvikEnv, jAdsServiceClass, "rewardedAdSetFullScreenContentCallback", "(J)V");

    jobject jActivity = substrateGetActivity();
    jobject jObj = (*dalvikEnv)->NewObject(dalvikEnv, jAdsServiceClass, jAdsServiceInitMethod, jActivity);
    jDalvikAdsService = (*dalvikEnv)->NewGlobalRef(dalvikEnv, jObj);

    DETACH_DALVIK();
}

JNIEXPORT jint JNICALL
JNI_OnLoad_ads(JavaVM *vm, void *reserved)
{
    JNIEnv* graalEnv;
    ATTACH_LOG_INFO("JNI_OnLoad_ads called");
#ifdef JNI_VERSION_1_8
    if ((*vm)->GetEnv(vm, (void **)&graalEnv, JNI_VERSION_1_8) != JNI_OK) {
        ATTACH_LOG_WARNING("Error initializing native Ads from OnLoad");
        return JNI_FALSE;
    }
    ATTACH_LOG_FINE("[Ads Service] Initializing native Ads from OnLoad");
    initializeGraalHandles(graalEnv);
    initializeAdsDalvikHandles();
    return JNI_VERSION_1_8;
#else
    #error Error: Java 8+ SDK is required to compile Attach
#endif
}

// from Java to Android

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_AndroidAdsService_nativeInitialize
(JNIEnv *env, jclass jClass)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikAdsService, jAdsServiceInitialize);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_AndroidAdsService_nativeSetRequestConfiguration
(JNIEnv *env, jclass jClass, jint jtagForChildDirectedTreatment, jint jtagForUnderAgeOfConsent, jstring jmaxAdContentRating, jobjectArray jtestDeviceIds)
{
    const char *maxAdContentRatingChars = (*env)->GetStringUTFChars(env, jmaxAdContentRating, NULL);
    int count = (*env)->GetArrayLength(env, jtestDeviceIds);

    ATTACH_DALVIK();
    jstring maxAdContentRating = (*dalvikEnv)->NewStringUTF(dalvikEnv, maxAdContentRatingChars);
    jobjectArray result = (jobjectArray) (*dalvikEnv)->NewObjectArray(dalvikEnv, count,
            (*dalvikEnv)->FindClass(dalvikEnv, "java/lang/String"), NULL);

    for (int i = 0; i < count; i++) {
        jstring id = (jstring) ((*env)->GetObjectArrayElement(env, jtestDeviceIds, i));
        const char *idString = (*env)->GetStringUTFChars(env, id, NULL);
        (*dalvikEnv)->SetObjectArrayElement(dalvikEnv, result, i,
                (*dalvikEnv)->NewStringUTF(dalvikEnv, idString));
        (*env)->ReleaseStringUTFChars(env, id, idString);
    }

    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikAdsService, jAdsServiceSetRequestConfiguration, jtagForChildDirectedTreatment, jtagForUnderAgeOfConsent, maxAdContentRating, result);
    (*dalvikEnv)->DeleteLocalRef(dalvikEnv, result);
    DETACH_DALVIK();
}

// banner ad

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_AndroidAdsService_nativeBannerAdNew
(JNIEnv *env, jclass jClass, jlong jid)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikAdsService, jAdsServiceBannerAdNew, jid);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_AndroidAdsService_nativeBannerAdLoad
(JNIEnv *env, jclass jClass, jlong jid)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikAdsService, jAdsServiceBannerAdLoad, jid);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_AndroidAdsService_nativeBannerAdShow
(JNIEnv *env, jclass jClass, jlong jid)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikAdsService, jAdsServiceBannerAdShow, jid);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_AndroidAdsService_nativeBannerAdHide
(JNIEnv *env, jclass jClass, jlong jid)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikAdsService, jAdsServiceBannerAdHide, jid);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_AndroidAdsService_nativeBannerAdSetLayout
(JNIEnv *env, jclass jClass, jlong jid, jstring jlayout)
{
    const char *layoutChars = (*env)->GetStringUTFChars(env, jlayout, NULL);

    ATTACH_DALVIK();
    jstring layout = (*dalvikEnv)->NewStringUTF(dalvikEnv, layoutChars);
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikAdsService, jAdsServiceBannerAdSetLayout, jid, layout);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_AndroidAdsService_nativeBannerAdSetAdSize
(JNIEnv *env, jclass jClass, jlong jid, jstring jsize)
{
    const char *sizeChars = (*env)->GetStringUTFChars(env, jsize, NULL);

    ATTACH_DALVIK();
    jstring size = (*dalvikEnv)->NewStringUTF(dalvikEnv, sizeChars);
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikAdsService, jAdsServiceBannerAdSetAdSize, jid, size);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_AndroidAdsService_nativeBannerAdSetAdUnitId
(JNIEnv *env, jclass jClass, jlong jid, jstring jadUnitId)
{
    const char *adUnitIdChars = (*env)->GetStringUTFChars(env, jadUnitId, NULL);

    ATTACH_DALVIK();
    jstring adUnitId = (*dalvikEnv)->NewStringUTF(dalvikEnv, adUnitIdChars);
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikAdsService, jAdsServiceBannerAdSetAdUnitId, jid, adUnitId);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_AndroidAdsService_nativeBannerAdSetAdListener
(JNIEnv *env, jclass jClass, jlong jid)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikAdsService, jAdsServiceBannerAdSetAdListener, jid);
    DETACH_DALVIK();
}

// interstitial ad

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_AndroidAdsService_nativeInterstitialAdLoad
(JNIEnv *env, jclass jClass, jlong jid, jstring jadUnitId)
{
    const char *adUnitIdChars = (*env)->GetStringUTFChars(env, jadUnitId, NULL);

    ATTACH_DALVIK();
    jstring adUnitId = (*dalvikEnv)->NewStringUTF(dalvikEnv, adUnitIdChars);
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikAdsService, jAdsServiceInterstitialAdLoad, jid, adUnitId);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_AndroidAdsService_nativeInterstitialAdShow
(JNIEnv *env, jclass jClass, jlong jid)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikAdsService, jAdsServiceInterstitialAdShow, jid);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_AndroidAdsService_nativeInterstitialAdSetFullScreenContentCallback
(JNIEnv *env, jclass jClass, jlong jid)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikAdsService, jAdsServiceInterstitialAdSetFullScreenContentCallback, jid);
    DETACH_DALVIK();
}

// rewarded ad

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_AndroidAdsService_nativeRewardedAdLoad
(JNIEnv *env, jclass jClass, jlong jid, jstring jadUnitId)
{
    const char *adUnitIdChars = (*env)->GetStringUTFChars(env, jadUnitId, NULL);

    ATTACH_DALVIK();
    jstring adUnitId = (*dalvikEnv)->NewStringUTF(dalvikEnv, adUnitIdChars);
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikAdsService, jAdsServiceRewardedAdLoad, jid, adUnitId);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_AndroidAdsService_nativeRewardedAdShow
(JNIEnv *env, jclass jClass, jlong jid)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikAdsService, jAdsServiceRewardedAdShow, jid);
    DETACH_DALVIK();
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_AndroidAdsService_nativeRewardedAdSetFullScreenContentCallback
(JNIEnv *env, jclass jClass, jlong jid)
{
    ATTACH_DALVIK();
    (*dalvikEnv)->CallVoidMethod(dalvikEnv, jDalvikAdsService, jAdsServiceRewardedAdSetFullScreenContentCallback, jid);
    DETACH_DALVIK();
}

// from Dalvik to native

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_DalvikAdsService_invokeCallback
(JNIEnv *env, jobject service, jlong id, jstring callbackClass, jstring callbackMethod, jobjectArray params)
{
    const char *callbackClassChars = (*env)->GetStringUTFChars(env, callbackClass, NULL);
    const char *callbackMethodChars = (*env)->GetStringUTFChars(env, callbackMethod, NULL);
    int count = (*env)->GetArrayLength(env, params);

    ATTACH_GRAAL();

    jobjectArray result = (jobjectArray) (*graalEnv)->NewObjectArray(graalEnv, count,
                (*graalEnv)->FindClass(graalEnv, "java/lang/String"), NULL);

    for (int i = 0; i < count; i++) {
        jstring param = (jstring) ((*env)->GetObjectArrayElement(env, params, i));
        const char *paramString = (*env)->GetStringUTFChars(env, param, NULL);
        (*graalEnv)->SetObjectArrayElement(graalEnv, result, i,
                (*graalEnv)->NewStringUTF(graalEnv, paramString));
        (*env)->ReleaseStringUTFChars(env, param, paramString);
    }

    jstring jcallbackClass = (*graalEnv)->NewStringUTF(graalEnv, callbackClassChars);
    jstring jcallbackMethod = (*graalEnv)->NewStringUTF(graalEnv, callbackMethodChars);
    (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalAdsClass, jGraalInvokeCallbackMethod, id, jcallbackClass, jcallbackMethod, result);
    DETACH_GRAAL();

    (*graalEnv)->DeleteLocalRef(graalEnv, result);
    (*env)->ReleaseStringUTFChars(env, callbackMethod, callbackMethodChars);
    (*env)->ReleaseStringUTFChars(env, callbackClass, callbackClassChars);
}