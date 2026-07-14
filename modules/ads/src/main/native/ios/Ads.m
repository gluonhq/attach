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

jclass jadsServiceClass;
jmethodID jadsService_invokeCallback = 0;

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

        jadsServiceClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/ads/impl/IOSAdsService"));
        jadsService_invokeCallback = (*env)->GetStaticMethodID(env, jadsServiceClass, "invokeCallback", "(JLjava/lang/String;Ljava/lang/String;[Ljava/lang/String;)V");

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
(JNIEnv *env, jclass jClass, jstring jageRestrictedTreatment, jstring jmaxAdContentRating, jobjectArray jtestDeviceIds)
{
    const char *ageRestrictedTreatmentChars = (*env)->GetStringUTFChars(env, jageRestrictedTreatment, NULL);
    NSString *ageRestrictedTreatment = [NSString stringWithCharacters:(UniChar *)ageRestrictedTreatmentChars length:(*env)->GetStringLength(env, jageRestrictedTreatment)];
    (*env)->ReleaseStringUTFChars(env, jageRestrictedTreatment, ageRestrictedTreatmentChars);

    const char *maxAdContentRatingChars = (*env)->GetStringUTFChars(env, jmaxAdContentRating, NULL);
    NSString *maxAdContentRating = [NSString stringWithCharacters:(UniChar *)maxAdContentRatingChars length:(*env)->GetStringLength(env, jmaxAdContentRating)];
    (*env)->ReleaseStringUTFChars(env, jmaxAdContentRating, maxAdContentRatingChars);

    int count = (*env)->GetArrayLength(env, jtestDeviceIds);
    NSMutableArray<NSString*> *testDeviceIds = [NSMutableArray arrayWithCapacity:count];

    for (jsize i = 0; i < count; i++) {
        jstring jtestDeviceId = (jstring)(*env)->GetObjectArrayElement(env, jtestDeviceIds, i);
        const char *testDeviceIdString = (*env)->GetStringUTFChars(env, jtestDeviceId, NULL);
        NSString *testDeviceId = [NSString stringWithCharacters:(UniChar *)testDeviceIdString length:(*env)->GetStringLength(env, jtestDeviceId)];
        (*env)->ReleaseStringUTFChars(env, jtestDeviceId, testDeviceIdString);

        [testDeviceIds addObject:testDeviceId];
    }

    [adsService setRequestConfiguration:ageRestrictedTreatment maxAdContentRating:maxAdContentRating testDeviceIds:testDeviceIds];
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
    (*env)->ReleaseStringUTFChars(env, jlayout, layoutChars);

    [adsService bannerAdSetAdLayout:adId layout:layout];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_IOSAdsService_nativeBannerAdSetAdSize
(JNIEnv *env, jclass jClass, long adId, jstring jsize)
{
    const char *sizeChars = (*env)->GetStringUTFChars(env, jsize, NULL);
    NSString *size = [NSString stringWithCharacters:(UniChar *)sizeChars length:(*env)->GetStringLength(env, jsize)];
    (*env)->ReleaseStringUTFChars(env, jsize, sizeChars);

    [adsService bannerAdSetAdSize:adId size:size];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_IOSAdsService_nativeBannerAdSetAdUnitId
(JNIEnv *env, jclass jClass, long adId, jstring jadUnitId)
{
    const char *adUnitIdChars = (*env)->GetStringUTFChars(env, jadUnitId, NULL);
    NSString *adUnitId = [NSString stringWithCharacters:(UniChar *)adUnitIdChars length:(*env)->GetStringLength(env, jadUnitId)];
    (*env)->ReleaseStringUTFChars(env, jadUnitId, adUnitIdChars);

    [adsService bannerAdSetAdUnitId:adId adUnitId:adUnitId];
}

// interstitial

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_IOSAdsService_nativeInterstitialAdLoad
(JNIEnv *env, jclass jClass, long adId, jstring jadUnitId)
{
    const char *adUnitIdChars = (*env)->GetStringUTFChars(env, jadUnitId, NULL);
    NSString *adUnitId = [NSString stringWithCharacters:(UniChar *)adUnitIdChars length:(*env)->GetStringLength(env, jadUnitId)];
    (*env)->ReleaseStringUTFChars(env, jadUnitId, adUnitIdChars);

    [adsService interstitialAdLoad:adId adUnitId:adUnitId];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_IOSAdsService_nativeInterstitialAdShow
(JNIEnv *env, jclass jClass, long adId)
{
    [adsService interstitialAdShow:adId];
}

// rewarded

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_IOSAdsService_nativeRewardedAdLoad
(JNIEnv *env, jclass jClass, long adId, jstring jadUnitId)
{
    const char *adUnitIdChars = (*env)->GetStringUTFChars(env, jadUnitId, NULL);
    NSString *adUnitId = [NSString stringWithCharacters:(UniChar *)adUnitIdChars length:(*env)->GetStringLength(env, jadUnitId)];
    (*env)->ReleaseStringUTFChars(env, jadUnitId, adUnitIdChars);

    [adsService rewardedAdLoad:adId adUnitId:adUnitId];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_IOSAdsService_nativeRewardedAdShow
(JNIEnv *env, jclass jClass, long adId)
{
    [adsService rewardedAdShow:adId];
}

// from native to Java

- (void) initialize {
    [[GADMobileAds sharedInstance] startWithCompletionHandler:^(GADInitializationStatus * _Nonnull status) {
        [self invokeCallback:-1 callback:@"" method:@"" params:@[]];
    }];
}

- (void) setRequestConfiguration:(NSString*)ageRestrictedTreatment maxAdContentRating:(NSString*)rating testDeviceIds:(NSArray<NSString*>*)testDevices {
    GADRequestConfiguration *config = GADMobileAds.sharedInstance.requestConfiguration;

    if ([ageRestrictedTreatment isEqualToString:@"CHILD"]) {
        config.ageRestrictedTreatment = GADAgeRestrictedTreatmentChild;
    } else if ([ageRestrictedTreatment isEqualToString:@"TEEN"]) {
        config.ageRestrictedTreatment = GADAgeRestrictedTreatmentTeen;
    } else if ([ageRestrictedTreatment isEqualToString:@"UNSPECIFIED"]) {
        config.ageRestrictedTreatment = GADAgeRestrictedTreatmentUnspecified;
    }

    config.maxAdContentRating = rating;
    config.testDeviceIdentifiers = testDevices;
}

- (void) bannerAdNew:(long)adId {
    GADBannerView *banner = [[GADBannerView alloc] initWithAdSize:GADAdSizeBanner];

    UIView *container = [[UIView alloc] init];
    [container addSubview:banner];

    banner.rootViewController = UIApplication.sharedApplication.keyWindow.rootViewController;

    adRegistry[@(adId)] = banner;
    bannerContainers[@(adId)] = container;
}

- (void) bannerAdShow:(long)adId {
    UIView *container = bannerContainers[@(adId)];
    UIViewController *root = UIApplication.sharedApplication.keyWindow.rootViewController;
    [root.view addSubview:container];

    CGRect frame = container.frame;
    frame.origin.y = root.view.frame.size.height - 50;
    frame.origin.x = (root.view.frame.size.width - 320) / 2;
    container.frame = frame;
}

- (void) bannerAdHide:(long)adId {
    UIView *container = bannerContainers[@(adId)];
    [container removeFromSuperview];
}

- (void) bannerAdLoad:(long)adId {
    GADBannerView *banner = adRegistry[@(adId)];
    GADRequest *request = [GADRequest request];

    [banner loadRequest:request];
}

- (void) bannerAdSetAdLayout:(long)adId layout:(NSString*)layout {
    GADBannerView *banner = adRegistry[@(adId)];
    UIView *container = bannerContainers[@(adId)];

    banner.translatesAutoresizingMaskIntoConstraints = NO;
    [NSLayoutConstraint deactivateConstraints:banner.constraints];

    [NSLayoutConstraint activateConstraints:@[
        [banner.centerXAnchor constraintEqualToAnchor:container.centerXAnchor],
        [layout isEqualToString:@"TOP"]
            ? [banner.topAnchor constraintEqualToAnchor:container.topAnchor]
            : [banner.bottomAnchor constraintEqualToAnchor:container.bottomAnchor]
    ]];
}

- (void) bannerAdSetAdSize:(long)adId size:(NSString*)size {
    GADBannerView *banner = adRegistry[@(adId)];

    if ([size isEqualToString:@"BANNER"]) {
        banner.adSize = GADAdSizeBanner;
    } else if ([size isEqualToString:@"FLUID"]) {
        banner.adSize = GADAdSizeFluid;
    } else if ([size isEqualToString:@"FULL_BANNER"]) {
        banner.adSize = GADAdSizeFullBanner;
    } else if ([size isEqualToString:@"INVALID"]) {
        banner.adSize = GADAdSizeInvalid;
    } else if ([size isEqualToString:@"LARGE_BANNER"]) {
        banner.adSize = GADAdSizeLargeBanner;
    } else if ([size isEqualToString:@"LEADERBOARD"]) {
        banner.adSize = GADAdSizeLeaderboard;
    } else if ([size isEqualToString:@"MEDIUM_RECTANGLE"]) {
        banner.adSize = GADAdSizeMediumRectangle;
    } else if ([size isEqualToString:@"WIDE_SKYSCRAPER"]) {
        banner.adSize = GADAdSizeSkyscraper;
    }
}

- (void) bannerAdSetAdUnitId:(long)adId adUnitId:(NSString*)unitId {
    GADBannerView *banner = adRegistry[@(adId)];
    banner.adUnitID = unitId;
}

- (void) interstitialAdLoad:(long)adId adUnitId:(NSString*)unitId {
    [GADInterstitialAd loadWithAdUnitID:unitId request:[GADRequest request] completionHandler:^(GADInterstitialAd *ad, NSError *error) {
        if (error) {
            [self invokeCallback:adId callback:@"InterstitialAd" method:@"onAdFailedToLoad" params:@[]];
        } else {
            adRegistry[@(adId)] = ad;
            [self invokeCallback:adId callback:@"InterstitialAd" method:@"onAdLoaded" params:@[]];

            Delegate *delegate = [[Delegate alloc] init];
            delegate.adId = adId;
            delegate.service = self;

            ad.fullScreenContentDelegate = delegate;
        }
    }];
}

- (void) interstitialAdShow:(long)adId {
    GADInterstitialAd *ad = adRegistry[@(adId)];
    UIViewController *root = UIApplication.sharedApplication.keyWindow.rootViewController;

    [ad presentFromRootViewController:root];
}

- (void) rewardedAdLoad:(long)adId adUnitId:(NSString*)unitId {
    [GADRewardedAd loadWithAdUnitID:unitId request:[GADRequest request] completionHandler:^(GADRewardedAd *ad, NSError *error) {
        if (error) {
            [self invokeCallback:adId callback:@"RewardedAd" method:@"onAdFailedToLoad" params:@[]];
        } else {
            adRegistry[@(adId)] = ad;
            [self invokeCallback:adId callback:@"RewardedAd" method:@"onAdLoaded" params:@[]];

            Delegate *delegate = [[Delegate alloc] init];
            delegate.adId = adId;
            delegate.service = self;

            ad.fullScreenContentDelegate = delegate;
        }
    }];
}

- (void) rewardedAdShow:(long)adId {
    GADRewardedAd *ad = adRegistry[@(adId)];
    UIViewController *root = UIApplication.sharedApplication.keyWindow.rootViewController;

    [ad presentFromRootViewController:root userDidEarnRewardHandler:^{
        GADAdReward *reward = ad.adReward;
        [self invokeCallback:adId callback:@"Rewarded" method:@"onUserEarnedReward" params:@[reward.type, [NSString stringWithFormat:@"%ld", (long)reward.amount]]];
    }];
}

- (void) invokeCallback:(long)adId callback:(NSString*)callback method:(NSString*)method params:(NSArray<NSString*>*)params {
    const char *callbackChars = [callback UTF8String];
    jstring jcallback = (*env)->NewStringUTF(env, callbackChars);

    const char *methodChars = [method UTF8String];
    jstring jmethod = (*env)->NewStringUTF(env, methodChars);

    int size = [params count];
    jobjectArray jparams = (*env)->NewObjectArray(env, size, (*env)->FindClass(env, "java/lang/String"), NULL);

    for (int i = 0; i < size; i++) {
        const char *paramChars = [params[i] UTF8String];
        (*env)->SetObjectArrayElement(env, jparams, i, (*env)->NewStringUTF(env, paramChars));
    }

    (*env)->CallStaticVoidMethod(env, jadsServiceClass, jadsService_invokeCallback, adId, jcallback, jmethod);

    (*env)->DeleteLocalRef(env, jcallback);
    (*env)->DeleteLocalRef(env, jmethod);
    (*env)->DeleteLocalRef(env, jparams);
}

@end

@implementation Delegate

- (void)adDidRecordClick:(id<GADFullScreenPresentingAd>)ad {
    [self.service invokeCallback:self.adId
                        callback:@"FullScreenContentCallback"
                          method:@"onAdClicked"
                          params:nil];
}

- (void)adDidDismissFullScreenContent:(id<GADFullScreenPresentingAd>)ad {
    [self.service invokeCallback:self.adId
                        callback:@"FullScreenContentCallback"
                          method:@"onAdDismissedFullScreenContent"
                          params:nil];
}

- (void)ad:(id<GADFullScreenPresentingAd>)ad
didFailToPresentFullScreenContentWithError:(NSError *)error {
    [self.service invokeCallback:self.adId
                        callback:@"FullScreenContentCallback"
                          method:@"onAdFailedToShowFullScreenContent"
                          params:nil];
}

- (void)adDidRecordImpression:(id<GADFullScreenPresentingAd>)ad {
    [self.service invokeCallback:self.adId
                        callback:@"FullScreenContentCallback"
                          method:@"onAdImpression"
                          params:nil];
}

- (void)adWillPresentFullScreenContent:(id<GADFullScreenPresentingAd>)ad {
    [self.service invokeCallback:self.adId
                        callback:@"FullScreenContentCallback"
                          method:@"onAdShowedFullScreenContent"
                          params:nil];
}

@end