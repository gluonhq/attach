#import "AdsService.h"

@interface AdsService ()
@property NSMutableDictionary<NSNumber*, id> *registry;
@property NSMutableDictionary<NSNumber*, UIView*> *bannerContainers;
@end

@implementation AdsService

+ (instancetype)shared {
    static AdsService *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[AdsService alloc] init];
        sharedInstance.registry = [NSMutableDictionary dictionary];
        sharedInstance.bannerContainers = [NSMutableDictionary dictionary];
    });
    return sharedInstance;
}

- (void)initialize {
    [[GADMobileAds sharedInstance] startWithCompletionHandler:^(GADInitializationStatus * _Nonnull status) {
        [self invokeCallback:-1 callback:@"Init" method:@"onInitialized" params:@[]];
    }];
}

- (void)setRequestConfiguration:(int)tagForChildDirectedTreatment
      tagForUnderAgeOfConsent:(int)tagForUnderAgeOfConsent
      maxAdContentRating:(NSString*)rating
      testDeviceIds:(NSArray<NSString*>*)testDevices {

    GADRequestConfiguration *config = GADMobileAds.sharedInstance.requestConfiguration;
    config.tagForChildDirectedTreatment = tagForChildDirectedTreatment;
    config.tagForUnderAgeOfConsent = tagForUnderAgeOfConsent;
    config.maxAdContentRating = rating;
    config.testDeviceIdentifiers = testDevices;
}

- (void)bannerAdNew:(long)adId {

    dispatch_async(dispatch_get_main_queue(), ^{
        GADBannerView *banner = [[GADBannerView alloc] initWithAdSize:kGADAdSizeBanner];

        UIView *container = [[UIView alloc] init];
        [container addSubview:banner];

        banner.rootViewController = UIApplication.sharedApplication.keyWindow.rootViewController;

        self.registry[@(adId)] = banner;
        self.bannerContainers[@(adId)] = container;
    });
}

- (void)bannerAdShow:(long)adId {
    dispatch_async(dispatch_get_main_queue(), ^{
        UIView *container = self.bannerContainers[@(adId)];
        UIViewController *root = UIApplication.sharedApplication.keyWindow.rootViewController;
        [root.view addSubview:container];

        CGRect frame = container.frame;
        frame.origin.y = root.view.frame.size.height - 50;
        frame.origin.x = (root.view.frame.size.width - 320) / 2;
        container.frame = frame;
    });
}

- (void)bannerAdHide:(long)adId {
    dispatch_async(dispatch_get_main_queue(), ^{
        UIView *container = self.bannerContainers[@(adId)];
        [container removeFromSuperview];
    });
}

- (void)bannerAdLoad:(long)adId {
    dispatch_async(dispatch_get_main_queue(), ^{
        GADBannerView *banner = self.registry[@(adId)];
        GADRequest *request = [GADRequest request];
        [banner loadRequest:request];
    });
}

- (void)bannerAdSetAdUnitId:(long)adId adUnitId:(NSString*)unitId {
    dispatch_async(dispatch_get_main_queue(), ^{
        GADBannerView *banner = self.registry[@(adId)];
        banner.adUnitID = unitId;
    });
}

- (void)interstitialAdLoad:(long)adId adUnitId:(NSString*)unitId {

    [GADInterstitialAd loadWithAdUnitID:unitId
                                request:[GADRequest request]
                      completionHandler:^(GADInterstitialAd *ad, NSError *error) {

        if (error) {
            [self invokeCallback:adId callback:@"InterstitialAd" method:@"onAdFailedToLoad" params:@[]];
            return;
        }

        self.registry[@(adId)] = ad;
        [self invokeCallback:adId callback:@"InterstitialAd" method:@"onAdLoaded" params:@[]];
    }];
}

- (void)interstitialAdShow:(long)adId {
    dispatch_async(dispatch_get_main_queue(), ^{
        GADInterstitialAd *ad = self.registry[@(adId)];
        UIViewController *root = UIApplication.sharedApplication.keyWindow.rootViewController;
        [ad presentFromRootViewController:root];
    });
}

- (void)rewardedAdLoad:(long)adId adUnitId:(NSString*)unitId {

    [GADRewardedAd loadWithAdUnitID:unitId
                            request:[GADRequest request]
                  completionHandler:^(GADRewardedAd *ad, NSError *error) {

        if (error) {
            [self invokeCallback:adId callback:@"RewardedAd" method:@"onAdFailedToLoad" params:@[]];
            return;
        }

        self.registry[@(adId)] = ad;
        [self invokeCallback:adId callback:@"RewardedAd" method:@"onAdLoaded" params:@[]];
    }];
}

- (void)rewardedAdShow:(long)adId {

    dispatch_async(dispatch_get_main_queue(), ^{
        GADRewardedAd *ad = self.registry[@(adId)];
        UIViewController *root = UIApplication.sharedApplication.keyWindow.rootViewController;

        [ad presentFromRootViewController:root
                 userDidEarnRewardHandler:^{
            GADAdReward *reward = ad.adReward;
            [self invokeCallback:adId
                        callback:@"Rewarded"
                          method:@"onUserEarnedReward"
                          params:@[reward.type, [NSString stringWithFormat:@"%ld", (long)reward.amount]]];
        }];
    });
}

- (void)invokeCallback:(long)adId
              callback:(NSString*)callback
                method:(NSString*)method
                params:(NSArray<NSString*>*)params {

    // This calls your JNI bridge generated by Gluon Attach
    // Same concept as nativeInvokeCallback on Android
}