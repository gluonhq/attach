#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
@import GoogleMobileAds;

@implementation AdsService

#pragma mark - Init

- (instancetype)initWithViewController:(UIViewController*)vc {
    self = [super init];
    self.rootVC = vc;
    self.registry = [NSMutableDictionary dictionary];
    self.bannerViews = [NSMutableDictionary dictionary];
    return self;
}

#pragma mark - Native callback bridge (same as Android)

- (void)invokeCallback:(long)adId :(NSString*)clazz :(NSString*)method {
    [self invokeCallback:adId :clazz :method :@[]];
}

- (void)invokeCallback:(long)adId :(NSString*)clazz :(NSString*)method :(NSArray*)params {
    // JNI bridge implemented elsewhere
}

#pragma mark - MobileAds init

- (void)initialize {
    [[GADMobileAds sharedInstance] startWithCompletionHandler:^(GADInitializationStatus * _Nonnull status) {
        [self invokeCallback:-1 :@"MobileAds" :@"onInitialized"];
    }];
}

#pragma mark =========================================================
#pragma mark BANNER ADS
#pragma mark =========================================================

- (void)bannerAdNew:(long)adId {
    dispatch_async(dispatch_get_main_queue(), ^{
        GADBannerView *banner = [[GADBannerView alloc] initWithAdSize:GADAdSizeBanner];
        banner.rootViewController = self.rootVC;

        UIView *container = [[UIView alloc] initWithFrame:CGRectZero];
        [container addSubview:banner];

        self.registry[@(adId)] = banner;
        self.bannerViews[@(adId)] = container;
    });
}

- (void)bannerAdSetAdUnitId:(long)adId :(NSString*)unitId {
    dispatch_async(dispatch_get_main_queue(), ^{
        GADBannerView *banner = self.registry[@(adId)];
        banner.adUnitID = unitId;
    });
}

- (void)bannerAdLoad:(long)adId {
    dispatch_async(dispatch_get_main_queue(), ^{
        GADBannerView *banner = self.registry[@(adId)];
        [banner loadRequest:[GADRequest request]];
    });
}

- (void)bannerAdShow:(long)adId {
    dispatch_async(dispatch_get_main_queue(), ^{
        UIView *view = self.bannerViews[@(adId)];
        view.frame = CGRectMake(0,
                                self.rootVC.view.bounds.size.height - 50,
                                self.rootVC.view.bounds.size.width,
                                50);
        [self.rootVC.view addSubview:view];
    });
}

- (void)bannerAdHide:(long)adId {
    dispatch_async(dispatch_get_main_queue(), ^{
        UIView *view = self.bannerViews[@(adId)];
        [view removeFromSuperview];
    });
}

#pragma mark =========================================================
#pragma mark INTERSTITIAL ADS
#pragma mark =========================================================

- (void)interstitialAdLoad:(long)adId :(NSString*)unitId {
    [GADInterstitialAd loadWithAdUnitID:unitId
                                request:[GADRequest request]
                      completionHandler:^(GADInterstitialAd *ad, NSError *error) {

        if (error) {
            [self invokeCallback:adId :@"InterstitialAdLoadCallback" :@"onAdFailedToLoad"];
            return;
        }

        self.registry[@(adId)] = ad;
        [self invokeCallback:adId :@"InterstitialAdLoadCallback" :@"onAdLoaded"];
    }];
}

- (void)interstitialAdShow:(long)adId {
    dispatch_async(dispatch_get_main_queue(), ^{
        GADInterstitialAd *ad = self.registry[@(adId)];
        [ad presentFromRootViewController:self.rootVC];
    });
}

#pragma mark =========================================================
#pragma mark REWARDED ADS
#pragma mark =========================================================

- (void)rewardedAdLoad:(long)adId :(NSString*)unitId {
    [GADRewardedAd loadWithAdUnitID:unitId
                            request:[GADRequest request]
                  completionHandler:^(GADRewardedAd *ad, NSError *error) {

        if (error) {
            [self invokeCallback:adId :@"RewardedAdLoadCallback" :@"onAdFailedToLoad"];
            return;
        }

        self.registry[@(adId)] = ad;
        [self invokeCallback:adId :@"RewardedAdLoadCallback" :@"onAdLoaded"];
    }];
}

- (void)rewardedAdShow:(long)adId {
    dispatch_async(dispatch_get_main_queue(), ^{
        GADRewardedAd *ad = self.registry[@(adId)];

        [ad presentFromRootViewController:self.rootVC
                 userDidEarnRewardHandler:^{
            GADAdReward *reward = ad.adReward;
            [self invokeCallback:adId
                                :@"OnUserEarnedRewardListener"
                                :@"onUserEarnedReward"
                                :@[reward.type, [NSString stringWithFormat:@"%ld",(long)reward.amount]]];
        }];
    });
}

@end