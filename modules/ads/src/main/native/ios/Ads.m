#include "Ads.h"

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

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_IOSAdsService_initAds
(JNIEnv *env, jclass jClass)
{
    // Note: there is no need for callbacks from native to Java
    if (!adsInitialized) {
        adsInitialized = true;

        jadsServiceClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/ads/impl/DefaultAdsService"));
        jadsService_invokeCallback = (*env)->GetStaticMethodID(env, jadsServiceClass, "invokeCallback", "(JLjava/lang/String;Ljava/lang/String;[Ljava/lang/String;)V");

        adsService = [[AdsService alloc] init];
        adRegistry = [[NSMutableDictionary alloc] init];
        bannerContainers = [[NSMutableDictionary alloc] init];
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
    const jchar *ageRestrictedTreatmentChars = (*env)->GetStringChars(env, jageRestrictedTreatment, NULL);
    NSString *ageRestrictedTreatment = [NSString stringWithCharacters:(UniChar *)ageRestrictedTreatmentChars length:(*env)->GetStringLength(env, jageRestrictedTreatment)];
    (*env)->ReleaseStringChars(env, jageRestrictedTreatment, ageRestrictedTreatmentChars);

    const jchar *maxAdContentRatingChars = (*env)->GetStringChars(env, jmaxAdContentRating, NULL);
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

    [adsService setRequestConfiguration:ageRestrictedTreatment maxAdContentRating:maxAdContentRating testDeviceIds:testDeviceIds];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_IOSAdsService_nativeRemoveAd
(JNIEnv *env, jclass jClass, jlong adId)
{
    [adsService removeAd:adId];
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
    const jchar *layoutChars = (*env)->GetStringChars(env, jlayout, NULL);
    NSString *layout = [NSString stringWithCharacters:(UniChar *)layoutChars length:(*env)->GetStringLength(env, jlayout)];
    (*env)->ReleaseStringChars(env, jlayout, layoutChars);

    [adsService bannerAdSetAdLayout:adId layout:layout];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_IOSAdsService_nativeBannerAdSetAdSize
(JNIEnv *env, jclass jClass, long adId, jstring jsize)
{
    const jchar *sizeChars = (*env)->GetStringChars(env, jsize, NULL);
    NSString *size = [NSString stringWithCharacters:(UniChar *)sizeChars length:(*env)->GetStringLength(env, jsize)];
    (*env)->ReleaseStringChars(env, jsize, sizeChars);

    [adsService bannerAdSetAdSize:adId size:size];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_IOSAdsService_nativeBannerAdSetAdUnitId
(JNIEnv *env, jclass jClass, long adId, jstring jadUnitId)
{
    AttachLog(@"bannerAdSetAdUnitIdNative: %i", adId);

    const jchar *adUnitIdChars = (*env)->GetStringChars(env, jadUnitId, NULL);
    NSString *adUnitId = [NSString stringWithCharacters:(UniChar *)adUnitIdChars length:(*env)->GetStringLength(env, jadUnitId)];
    (*env)->ReleaseStringChars(env, jadUnitId, adUnitIdChars);

    [adsService bannerAdSetAdUnitId:adId adUnitId:adUnitId];
}

// interstitial

JNIEXPORT void JNICALL Java_com_gluonhq_attach_ads_impl_IOSAdsService_nativeInterstitialAdLoad
(JNIEnv *env, jclass jClass, long adId, jstring jadUnitId)
{
    const jchar *adUnitIdChars = (*env)->GetStringChars(env, jadUnitId, NULL);
    NSString *adUnitId = [NSString stringWithCharacters:(UniChar *)adUnitIdChars length:(*env)->GetStringLength(env, jadUnitId)];
    (*env)->ReleaseStringChars(env, jadUnitId, adUnitIdChars);

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
    const jchar *adUnitIdChars = (*env)->GetStringChars(env, jadUnitId, NULL);
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

@implementation AdsService

- (void) initialize {
    AttachLog(@"Initializing ads service...");

    [[GADMobileAds sharedInstance] startWithCompletionHandler:^(GADInitializationStatus * _Nonnull status) {
        AttachLog(@"Ads service initialized");
        [self invokeCallback:-1 callback:@"" method:@"" params:@[]];
    }];
}

- (void) setRequestConfiguration:(NSString*)ageRestrictedTreatment maxAdContentRating:(NSString*)rating testDeviceIds:(NSArray<NSString*>*)testDevices {
    AttachLog(@"setRequestConfiguration");

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

- (void) removeAd:(long)adId {
    AttachLog(@"removeAd");

    UIView *container = bannerContainers[@(adId)];
    [container removeFromSuperview];

    [bannerContainers removeObjectForKey:@(adId)];
    [adRegistry removeObjectForKey:@(adId)];
}

- (void) bannerAdNew:(long)adId {
    AttachLog(@"bannerAdNew");

    UIViewController *root = UIApplication.sharedApplication.keyWindow.rootViewController;
    GADBannerView *banner = [[GADBannerView alloc] initWithAdSize:GADAdSizeBanner];
    UIView *container = [[UIView alloc] init];

    banner.translatesAutoresizingMaskIntoConstraints = NO;
    banner.rootViewController = root;

    container.translatesAutoresizingMaskIntoConstraints = NO;
    container.hidden = YES;

    [container addSubview:banner];
    [root.view addSubview:container];

    [NSLayoutConstraint activateConstraints:@[
        [banner.leadingAnchor constraintEqualToAnchor:container.leadingAnchor],
        [banner.trailingAnchor constraintEqualToAnchor:container.trailingAnchor],
        [banner.topAnchor constraintEqualToAnchor:container.topAnchor],
        [banner.bottomAnchor constraintEqualToAnchor:container.bottomAnchor],
        [container.centerXAnchor constraintEqualToAnchor:root.view.safeAreaLayoutGuide.centerXAnchor]
    ]];

    adRegistry[@(adId)] = banner;
    bannerContainers[@(adId)] = container;
}

- (void) bannerAdShow:(long)adId {
    AttachLog(@"bannerAdShow");

    UIView *container = bannerContainers[@(adId)];
    container.hidden = NO;
}

- (void) bannerAdHide:(long)adId {
    AttachLog(@"bannerAdHide");

    UIView *container = bannerContainers[@(adId)];
    container.hidden = YES;
}

- (void) bannerAdLoad:(long)adId {
    AttachLog(@"bannerAdLoad");

    GADBannerView *banner = adRegistry[@(adId)];
    GADRequest *request = [GADRequest request];

    [banner loadRequest:request];
}

- (void) bannerAdSetAdLayout:(long)adId layout:(NSString*)layout {
    AttachLog(@"bannerAdSetAdLayout");

    UIView *container = bannerContainers[@(adId)];
    UIView *parent = container.superview;

    NSLayoutConstraint *position = [layout isEqualToString:@"TOP"]
        ? [container.topAnchor constraintEqualToAnchor:parent.safeAreaLayoutGuide.topAnchor]
        : [container.bottomAnchor constraintEqualToAnchor:parent.safeAreaLayoutGuide.bottomAnchor];

    position.active = YES;
}

- (void) bannerAdSetAdSize:(long)adId size:(NSString*)size {
    AttachLog(@"bannerAdSetAdSize");

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
    AttachLog(@"bannerAdSetAdUnitId");

    GADBannerView *banner = adRegistry[@(adId)];
    banner.adUnitID = unitId;
}

- (void) interstitialAdLoad:(long)adId adUnitId:(NSString*)unitId {
    AttachLog(@"interstitialAdLoad");

    [GADInterstitialAd loadWithAdUnitID:unitId request:[GADRequest request] completionHandler:^(GADInterstitialAd *ad, NSError *error) {
        if (error) {
            [self invokeCallback:adId callback:@"InterstitialAdLoadCallback" method:@"onAdFailedToLoad" params:@[]];
        } else {
            adRegistry[@(adId)] = ad;
            [self invokeCallback:adId callback:@"InterstitialAdLoadCallback" method:@"onAdLoaded" params:@[]];

            Delegate *delegate = [[Delegate alloc] init];
            delegate.adId = adId;
            delegate.service = self;

            ad.fullScreenContentDelegate = delegate;
        }
    }];
}

- (void) interstitialAdShow:(long)adId {
    AttachLog(@"interstitialAdShow");

    GADInterstitialAd *ad = adRegistry[@(adId)];
    UIViewController *root = UIApplication.sharedApplication.keyWindow.rootViewController;

    [ad presentFromRootViewController:root];
}

- (void) rewardedAdLoad:(long)adId adUnitId:(NSString*)unitId {
    AttachLog(@"rewardedAdLoad");

    [GADRewardedAd loadWithAdUnitID:unitId request:[GADRequest request] completionHandler:^(GADRewardedAd *ad, NSError *error) {
        if (error) {
            [self invokeCallback:adId callback:@"RewardedAdLoadCallback" method:@"onAdFailedToLoad" params:@[]];
        } else {
            adRegistry[@(adId)] = ad;
            [self invokeCallback:adId callback:@"RewardedAdLoadCallback" method:@"onAdLoaded" params:@[]];

            Delegate *delegate = [[Delegate alloc] init];
            delegate.adId = adId;
            delegate.service = self;

            ad.fullScreenContentDelegate = delegate;
        }
    }];
}

- (void) rewardedAdShow:(long)adId {
    AttachLog(@"rewardedAdShow");

    GADRewardedAd *ad = adRegistry[@(adId)];
    UIViewController *root = UIApplication.sharedApplication.keyWindow.rootViewController;

    [ad presentFromRootViewController:root userDidEarnRewardHandler:^{
        GADAdReward *reward = ad.adReward;
        [self invokeCallback:adId callback:@"Rewarded" method:@"onUserEarnedReward" params:@[reward.type, [NSString stringWithFormat:@"%ld", (long)reward.amount.integerValue]]];
    }];
}

- (void) invokeCallback:(long)adId callback:(NSString*)callback method:(NSString*)method params:(NSArray<NSString*>*)params {
    AttachLog(@"invokeCallback");

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

    (*env)->CallStaticVoidMethod(env, jadsServiceClass, jadsService_invokeCallback, adId, jcallback, jmethod, jparams);

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
