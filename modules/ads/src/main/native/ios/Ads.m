#import "Ads.h"

JNIEnv *env;

JNIEXPORT int JNICALL
JNI_OnLoad_Ads(JavaVM *vm, void *reserved)
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

static bool adsInitialized = false;

AdsService *adsService; // singleton instance of the native AdsService
NSMutableDictionary *adRegistry;
NSMutableDictionary *bannerContainers;

@implementation AdsService

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_IOSAdsService_initAds
(JNIEnv *env, jclass jClass)
{
    // Note: there is no need for callbacks from native to Java
    if (!adsInitialized) {
        adsInitialized = true;
        adsService = [[AdsService alloc] init];
        adRegistry = [NSMutableDictionary dictionary];
        bannerContainers = [NSMutableDictionary dictionary];
    }
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_IOSAdsService_nativeInitialize
(JNIEnv *env, jclass jClass)
{
    [adsService initialize];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_IOSAdsService_nativeSetRequestConfiguration
(JNIEnv *env, jclass jClass, int jtagForChildDirectedTreatment, int jtagForUnderAgeOfConsent, jstring jmaxAdContentRating, jobjectArray jtestDeviceIds)
{
    const char *maxAdContentRatingChars = (*env)->GetStringUTFChars(env, jmaxAdContentRating, NULL);
    NSString *maxAdContentRating = [NSString stringWithCharacters:(UniChar *)maxAdContentRatingChars length:(*env)->GetStringLength(env, jmaxAdContentRating)];
    (*env)->ReleaseStringChars(env, jmaxAdContentRating, maxAdContentRatingChars);

    int count = (*env)->GetArrayLength(env, jtestDeviceIds);
    NSMutableArray<NSString*> *testDeviceIds = [NSMutableArray arrayWithCapacity:count];

    for (jsize i = 0; i < count; i++) {
        jstring jtestDeviceId = (jstring)(*env)->GetObjectArrayElement(env, jtestDeviceIds, i);
        const jchar *testDeviceIdString = (*env)->GetStringChars(env, jtestDeviceId, NULL);
        NSString *testDeviceId = [NSString stringWithCharacters:(UniChar *)testDeviceIdString length:(*env)->GetStringLength(env, jtestDeviceId)];
        (*env)->ReleaseStringChars(env, jtestDeviceId, testDeviceIdString);

        [testDeviceIds addObject:testDeviceId];
    }

    [adsService setRequestConfiguration:jtagForChildDirectedTreatment tagForUnderAgeOfConsent:jtagForUnderAgeOfConsent maxAdContentRating:jmaxAdContentRating testDeviceIds:testDeviceIds];
}

// banner

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_IOSAdsService_nativeBannerAdNew
(JNIEnv *env, jclass jClass, long adId)
{
    [adsService bannerAdNew:adId];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_IOSAdsService_nativeBannerAdLoad
(JNIEnv *env, jclass jClass, long adId)
{
    [adsService bannerAdLoad:adId];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_IOSAdsService_nativeBannerAdShow
(JNIEnv *env, jclass jClass, long adId)
{
    [adsService bannerAdShow:adId];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_IOSAdsService_nativeBannerAdHide
(JNIEnv *env, jclass jClass, long adId)
{
    [adsService bannerAdHide:adId];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_IOSAdsService_nativeBannerAdSetAdLayout
(JNIEnv *env, jclass jClass, long adId, jstring jlayout)
{
    const char *layoutChars = (*env)->GetStringUTFChars(env, jlayout, NULL);
    NSString *layout = [NSString stringWithCharacters:(UniChar *)layoutChars length:(*env)->GetStringLength(env, jlayout)];
    (*env)->ReleaseStringChars(env, jlayout, layoutChars);

    [adsService bannerAdSetAdLayout:adId layout:layout];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_IOSAdsService_nativeBannerAdSetAdSize
(JNIEnv *env, jclass jClass, long adId, jstring jsize)
{
    const char *sizeChars = (*env)->GetStringUTFChars(env, jsize, NULL);
    NSString *size = [NSString stringWithCharacters:(UniChar *)sizeChars length:(*env)->GetStringLength(env, jsize)];
    (*env)->ReleaseStringChars(env, jsize, sizeChars);

    [adsService bannerAdSetAdSize:adId size:size];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_IOSAdsService_nativeBannerAdSetAdUnitId
(JNIEnv *env, jclass jClass, long adId, jstring jadUnitId)
{
    const char *adUnitIdChars = (*env)->GetStringUTFChars(env, jadUnitId, NULL);
    NSString *adUnitId = [NSString stringWithCharacters:(UniChar *)adUnitIdChars length:(*env)->GetStringLength(env, jadUnitId)];
    (*env)->ReleaseStringChars(env, jadUnitId, adUnitIdChars);

    [adsService bannerAdSetAdUnitId:adId adUnitId:adUnitId];
}

// interstitial

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_IOSAdsService_nativeInterstitialAdLoad
(JNIEnv *env, jclass jClass, long adId, jstring jadUnitId)
{
    const char *adUnitIdChars = (*env)->GetStringUTFChars(env, jadUnitId, NULL);
    NSString *adUnitId = [NSString stringWithCharacters:(UniChar *)adUnitIdChars length:(*env)->GetStringLength(env, jadUnitId)];
    (*env)->ReleaseStringChars(env, jadUnitId, adUnitIdChars);

    [adsService interstitialAdLoad:adId adUnitId:adUnitId];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_IOSAdsService_nativeInterstitialAdShow
(JNIEnv *env, jclass jClass, long adId)
{
    [adsService interstitialAdShow:adId];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_IOSAdsService_nativeInterstitialAdSetFullScreenContentCallback
(JNIEnv *env, jclass jClass, long adId)
{
    [adsService interstitialAdSetFullScreenContentCallback:adId];
}

// rewarded

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_IOSAdsService_nativeRewardedAdLoad
(JNIEnv *env, jclass jClass, long adId, jstring jadUnitId)
{
    const char *adUnitIdChars = (*env)->GetStringUTFChars(env, jadUnitId, NULL);
    NSString *adUnitId = [NSString stringWithCharacters:(UniChar *)adUnitIdChars length:(*env)->GetStringLength(env, jadUnitId)];
    (*env)->ReleaseStringChars(env, jadUnitId, adUnitIdChars);

    [adsService rewardedAdLoad:adId adUnitId:adUnitId];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_IOSAdsService_nativeRewardedAdShow
(JNIEnv *env, jclass jClass, long adId)
{
    [adsService rewardedAdShow:adId];
}

// from native to Java

JNIEXPORT void JNICALL Java_com_gluonhq_helloandroid_DalvikAdsService_nativeInvokeCallback
(JNIEnv *env, jobject service, long adId, jstring callbackClass, jstring callbackMethod, jobjectArray params)
{
//     const char *callbackClassChars = (*env)->GetStringUTFChars(env, callbackClass, NULL);
//     const char *callbackMethodChars = (*env)->GetStringUTFChars(env, callbackMethod, NULL);
//     int count = (*env)->GetArrayLength(env, params);
//
//     ATTACH_GRAAL();
//
//     jobjectArray result = (jobjectArray) (*graalEnv)->NewObjectArray(graalEnv, count,
//                 (*graalEnv)->FindClass(graalEnv, "java/lang/String"), NULL);
//
//     for (int i = 0; i < count; i++) {
//         jstring param = (jstring) ((*env)->GetObjectArrayElement(env, params, i));
//         const char *paramString = (*env)->GetStringUTFChars(env, param, NULL);
//         (*graalEnv)->SetObjectArrayElement(graalEnv, result, i,
//                 (*graalEnv)->NewStringUTF(graalEnv, paramString));
//         (*env)->ReleaseStringUTFChars(env, param, paramString);
//     }
//
//     jstring jcallbackClass = (*graalEnv)->NewStringUTF(graalEnv, callbackClassChars);
//     jstring jcallbackMethod = (*graalEnv)->NewStringUTF(graalEnv, callbackMethodChars);
//     (*graalEnv)->CallStaticVoidMethod(graalEnv, jGraalAdsClass, jGraalInvokeCallbackMethod, adId, jcallbackClass, jcallbackMethod, result);
//     DETACH_GRAAL();
//
//     (*graalEnv)->DeleteLocalRef(graalEnv, result);
//     (*env)->ReleaseStringUTFChars(env, callbackMethod, callbackMethodChars);
//     (*env)->ReleaseStringUTFChars(env, callbackClass, callbackClassChars);
}

- (void) initialize {
//     [[GADMobileAds sharedInstance] startWithCompletionHandler:^(GADInitializationStatus * _Nonnull status) {
//         [self invokeCallback:-1 callback:@"" method:@"" params:@[]];
//     }];
}

- (void) setRequestConfiguration:(int)tagForChildDirectedTreatment tagForUnderAgeOfConsent:(int)tagForUnderAgeOfConsent maxAdContentRating:(NSString*)rating testDeviceIds:(NSArray<NSString*>*)testDevices {
//     GADRequestConfiguration *config = GADMobileAds.sharedInstance.requestConfiguration;
//     config.tagForChildDirectedTreatment = tagForChildDirectedTreatment;
//     config.tagForUnderAgeOfConsent = tagForUnderAgeOfConsent;
//     config.maxAdContentRating = rating;
//     config.testDeviceIdentifiers = testDevices;
}

- (void) bannerAdNew:(long)adId {
//     GADBannerView *banner = [[GADBannerView alloc] initWithAdSize:kGADAdSizeBanner];
//
//     UIView *container = [[UIView alloc] init];
//     [container addSubview:banner];
//
//     banner.rootViewController = UIApplication.sharedApplication.keyWindow.rootViewController;
//
//     self.adRegistry[@(adId)] = banner;
//     self.bannerContainers[@(adId)] = container;
}

- (void) bannerAdShow:(long)adId {
//     UIView *container = self.bannerContainers[@(adId)];
//     UIViewController *root = UIApplication.sharedApplication.keyWindow.rootViewController;
//     [root.view addSubview:container];
//
//     CGRect frame = container.frame;
//     frame.origin.y = root.view.frame.size.height - 50;
//     frame.origin.x = (root.view.frame.size.width - 320) / 2;
//     container.frame = frame;
}

- (void) bannerAdHide:(long)adId {
//     UIView *container = self.bannerContainers[@(adId)];
//     [container removeFromSuperview];
}

- (void) bannerAdLoad:(long)adId {
//     GADBannerView *banner = self.adRegistry[@(adId)];
//     GADRequest *request = [GADRequest request];
//
//     [banner loadRequest:request];
}

- (void) bannerAdSetAdUnitId:(long)adId adUnitId:(NSString*)unitId {
//     GADBannerView *banner = self.adRegistry[@(adId)];
//     banner.adUnitID = unitId;
}

- (void) interstitialAdLoad:(long)adId adUnitId:(NSString*)unitId {
//     [GADInterstitialAd loadWithAdUnitID:unitId request:[GADRequest request] completionHandler:^(GADInterstitialAd *ad, NSError *error) {
//         if (error) {
//             [self invokeCallback:adId callback:@"InterstitialAd" method:@"onAdFailedToLoad" params:@[]];
//         } else {
//             self.adRegistry[@(adId)] = ad;
//             [self invokeCallback:adId callback:@"InterstitialAd" method:@"onAdLoaded" params:@[]];
//         }
//     }];
}

- (void) interstitialAdShow:(long)adId {
//     GADInterstitialAd *ad = self.adRegistry[@(adId)];
//     UIViewController *root = UIApplication.sharedApplication.keyWindow.rootViewController;
//
//     [ad presentFromRootViewController:root];
}

- (void) rewardedAdLoad:(long)adId adUnitId:(NSString*)unitId {
//     [GADRewardedAd loadWithAdUnitID:unitId request:[GADRequest request] completionHandler:^(GADRewardedAd *ad, NSError *error) {
//         if (error) {
//             [self invokeCallback:adId callback:@"RewardedAd" method:@"onAdFailedToLoad" params:@[]];
//         } else {
//             self.adRegistry[@(adId)] = ad;
//             [self invokeCallback:adId callback:@"RewardedAd" method:@"onAdLoaded" params:@[]];
//         }
//     }];
}

- (void) rewardedAdShow:(long)adId {
//     GADRewardedAd *ad = self.adRegistry[@(adId)];
//     UIViewController *root = UIApplication.sharedApplication.keyWindow.rootViewController;
//
//     [ad presentFromRootViewController:root userDidEarnRewardHandler:^{
//         GADAdReward *reward = ad.adReward;
//         [self invokeCallback:adId callback:@"Rewarded" method:@"onUserEarnedReward" params:@[reward.type, [NSString stringWithFormat:@"%ld", (long)reward.amount]]];
//     }];
}

- (void) invokeCallback:(long)adId callback:(NSString*)callback method:(NSString*)method params:(NSArray<NSString*>*)params {
    // This calls your JNI bridge generated by Gluon Attach
    // Same concept as nativeInvokeCallback on IOS
}

@end