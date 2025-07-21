/*
 * Copyright (c) 2025 Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
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
package com.gluonhq.helloandroid;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DalvikAdsService {

    private static final String TAG = Util.TAG;
    private static final boolean debug = Util.isDebug();

    private final Activity activity;

    private final ViewGroup viewGroup;

    private final AdRegistry registry;

    private final Map<Long, FrameLayout> bannerAdLayouts;

    public DalvikAdsService(Activity activity) {
        this.activity = activity;
        this.viewGroup = (ViewGroup) activity.getWindow().getDecorView();
        this.registry = new AdRegistry();
        this.bannerAdLayouts = new HashMap<>();
    }

    private void initialize() {
        Log.v(TAG, "Initializing MobileAds...");

        MobileAds.initialize(activity, initializationStatus -> {
            Log.v(TAG, "Initialization of MobileAds completed");
            invokeCallback(-1, "", "");
        });
    }

    private void setRequestConfiguration(int tagForChildDirectedTreatment, int tagForUnderAgeOfConsent, String maxAdContentRating, String[] testDeviceIds) {
        MobileAds.setRequestConfiguration(MobileAds.getRequestConfiguration().toBuilder()
                .setTagForChildDirectedTreatment(tagForChildDirectedTreatment)
                .setTagForUnderAgeOfConsent(tagForUnderAgeOfConsent)
                .setMaxAdContentRating(maxAdContentRating)
                .setTestDeviceIds(Arrays.asList(testDeviceIds))
                .build());
    }

    private void bannerAdNew(long id) {
        AdView adView = new AdView(activity);
        FrameLayout layout = new FrameLayout(activity);

        layout.addView(adView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL));

        registry.add(id, adView);
        bannerAdLayouts.put(id, layout);

        if (debug) {
            Log.d(TAG, "Created new BannerAd with id: " + id);
        }
    }

    private void bannerAdShow(long id) {
        if (debug) {
            Log.d(TAG, "Showing BannerAd with id: " + id);
        }

        activity.runOnUiThread(() -> {
            FrameLayout layout = bannerAdLayouts.get(id);
            if (layout.getParent() == null) {
                viewGroup.addView(layout);
            }
        });
    }

    private void bannerAdHide(long id) {
        if (debug) {
            Log.d(TAG, "Hiding BannerAd with id: " + id);
        }

        activity.runOnUiThread(() -> {
            FrameLayout layout = bannerAdLayouts.get(id);
            if (layout.getParent() != null) {
                viewGroup.removeView(layout);
            }
        });
    }

    private void bannerAdLoad(long id) {
        if (debug) {
            Log.d(TAG, "Loading BannerAd with id: " + id);
        }

        activity.runOnUiThread(() ->
                registry.<AdView>get(id).loadAd(new AdRequest.Builder().build()));
    }

    private void bannerAdSetLayout(long id, String layout) {
        if (debug) {
            Log.d(TAG, "Setting layout of BannerAd with id: " + id + " to " + layout);
        }

        int gravity;

        switch (layout) {
            case "TOP": gravity = Gravity.TOP; break;
            case "BOTTOM": gravity = Gravity.BOTTOM; break;
            default:
                throw new InvalidParameterException("Layout '" + layout + "' is invalid!");
        }

        activity.runOnUiThread(() ->
                bannerAdLayouts.get(id).updateViewLayout(registry.<AdView>get(id), new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        gravity | Gravity.CENTER_HORIZONTAL)));
    }

    private void bannerAdSetAdSize(long id, String size) {
        if (debug) {
            Log.d(TAG, "Setting AdSize of BannerAd with id: " + id + " to " + size);
        }

        AdSize adSize;

        switch (size) {
            case "BANNER": adSize = AdSize.BANNER; break;
            case "FLUID": adSize = AdSize.FLUID; break;
            case "FULL_BANNER": adSize = AdSize.FULL_BANNER; break;
            case "INVALID": adSize = AdSize.INVALID; break;
            case "LARGE_BANNER": adSize = AdSize.LARGE_BANNER; break;
            case "LEADERBOARD": adSize = AdSize.LEADERBOARD; break;
            case "MEDIUM_RECTANGLE": adSize = AdSize.MEDIUM_RECTANGLE; break;
            case "WIDE_SKYSCRAPER": adSize = AdSize.WIDE_SKYSCRAPER; break;
            default:
                throw new InvalidParameterException("AdSize '" + size + "' is invalid!");
        }

        activity.runOnUiThread(() ->
                registry.<AdView>get(id).setAdSize(adSize));
    }

    private void bannerAdSetAdUnitId(long id, String adUnitId) {
        if (debug) {
            Log.d(TAG, "Setting AdUnitId of BannerAd with id: " + id + " to " + adUnitId);
        }

        activity.runOnUiThread(() ->
                registry.<AdView>get(id).setAdUnitId(adUnitId));
    }

    private void bannerAdSetAdListener(long id) {
        if (debug) {
            Log.d(TAG, "Setting AdListener of BannerAd with id: " + id);
        }

        activity.runOnUiThread(() -> registry.<AdView>get(id).setAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                if (debug) {
                    Log.d(TAG, "BannerAd with id: " + id + " onAdClicked()");
                }
                invokeCallback(id, "AdListener", "onAdClicked");
            }

            @Override
            public void onAdClosed() {
                if (debug) {
                    Log.d(TAG, "BannerAd with id: " + id + " onAdClosed()");
                }
                invokeCallback(id, "AdListener", "onAdClosed");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                if (debug) {
                    Log.d(TAG, "BannerAd with id: " + id + " onAdFailedToLoad() with error: " + loadAdError.getMessage());
                }
                invokeCallback(id, "AdListener", "onAdFailedToLoad");
            }

            @Override
            public void onAdImpression() {
                if (debug) {
                    Log.d(TAG, "BannerAd with id: " + id + " onAdImpression()");
                }
                invokeCallback(id, "AdListener", "onAdImpression");
            }

            @Override
            public void onAdLoaded() {
                if (debug) {
                    Log.d(TAG, "BannerAd with id: " + id + " onAdLoaded()");
                }
                invokeCallback(id, "AdListener", "onAdLoaded");
            }

            @Override
            public void onAdOpened() {
                if (debug) {
                    Log.d(TAG, "BannerAd with id: " + id + " onAdOpened()");
                }
                invokeCallback(id, "AdListener", "onAdOpened");
            }

            @Override
            public void onAdSwipeGestureClicked() {
                if (debug) {
                    Log.d(TAG, "BannerAd with id: " + id + " onAdSwipeGestureClicked()");
                }
                invokeCallback(id, "AdListener", "onAdSwipeGestureClicked");
            }
        }));
    }

    private void interstitialAdLoad(long id, String adUnitId) {
        if (debug) {
            Log.d(TAG, "Loading InterstitialAd with id: " + id);
        }

        activity.runOnUiThread(() -> InterstitialAd.load(activity, adUnitId, new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                if (debug) {
                    Log.d(TAG, "InterstitialAd with id: " + id + " onAdFailedToLoad() with error: " + loadAdError.getMessage());
                }
                invokeCallback(id, "InterstitialAdLoadCallback", "onAdFailedToLoad");
            }

            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                if (debug) {
                    Log.d(TAG, "InterstitialAd with id: " + id + " onAdLoaded()");
                }

                registry.add(id, interstitialAd);
                invokeCallback(id, "InterstitialAdLoadCallback", "onAdLoaded");
            }
        }));
    }

    private void interstitialAdShow(long id) {
        if (debug) {
            Log.d(TAG, "Showing InterstitialAd with id: " + id);
        }

        activity.runOnUiThread(() ->
                registry.<InterstitialAd>get(id).show(activity));
    }

    private void interstitialAdSetFullScreenContentCallback(long id) {
        if (debug) {
            Log.d(TAG, "Setting FullScreenContentCallback of InterstitialAd with id: " + id);
        }

        activity.runOnUiThread(() -> registry.<InterstitialAd>get(id).setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdClicked() {
                if (debug) {
                    Log.d(TAG, "InterstitialAd with id: " + id + " onAdClicked()");
                }
                invokeCallback(id, "FullScreenContentCallback", "onAdClicked");
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                if (debug) {
                    Log.d(TAG, "InterstitialAd with id: " + id + " onAdDismissedFullScreenContent()");
                }
                invokeCallback(id, "FullScreenContentCallback", "onAdDismissedFullScreenContent");
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                if (debug) {
                    Log.d(TAG, "InterstitialAd with id: " + id + " onAdFailedToShowFullScreenContent() with error: " + adError.getMessage());
                }
                invokeCallback(id, "FullScreenContentCallback", "onAdFailedToShowFullScreenContent");
            }

            @Override
            public void onAdImpression() {
                if (debug) {
                    Log.d(TAG, "InterstitialAd with id: " + id + " onAdImpression()");
                }
                invokeCallback(id, "FullScreenContentCallback", "onAdImpression");
            }

            @Override
            public void onAdShowedFullScreenContent() {
                if (debug) {
                    Log.d(TAG, "InterstitialAd with id: " + id + " onAdShowedFullScreenContent()");
                }
                invokeCallback(id, "FullScreenContentCallback", "onAdShowedFullScreenContent");
            }
        }));
    }

    private void rewardedAdLoad(long id, String adUnitId) {
        if (debug) {
            Log.d(TAG, "Loading RewardedAd with id: " + id);
        }

        activity.runOnUiThread(() -> RewardedAd.load(activity, adUnitId, new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                if (debug) {
                    Log.d(TAG, "RewardedAd with id: " + id + " onAdFailedToLoad() with error: " + loadAdError.getMessage());
                }
                invokeCallback(id, "RewardedAdLoadCallback", "onAdFailedToLoad");
            }

            @Override
            public void onAdLoaded(@NonNull RewardedAd interstitialAd) {
                if (debug) {
                    Log.d(TAG, "RewardedAd with id: " + id + " onAdLoaded()");
                }

                registry.add(id, interstitialAd);
                invokeCallback(id, "RewardedAdLoadCallback", "onAdLoaded");
            }
        }));
    }

    private void rewardedAdShow(long id) {
        if (debug) {
            Log.d(TAG, "Showing RewardedAd with id: " + id);
        }
        
        activity.runOnUiThread(() -> registry.<RewardedAd>get(id).show(activity, new OnUserEarnedRewardListener() {
            @Override
            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                if (debug) {
                    Log.d(TAG, "RewardedAd with id: " + id + " onUserEarnedReward() with item: { type: " + rewardItem.getType() + ", amount: " + rewardItem.getAmount() + " }");
                }
                invokeCallback(id, "OnUserEarnedRewardListener", "onUserEarnedReward", new String[] { rewardItem.getType(), String.valueOf(rewardItem.getAmount()) });
            }
        }));
    }

    private void rewardedAdSetFullScreenContentCallback(long id) {
        if (debug) {
            Log.d(TAG, "Setting FullScreenContentCallback of RewardedAd with id: " + id);
        }

        activity.runOnUiThread(() -> registry.<RewardedAd>get(id).setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdClicked() {
                if (debug) {
                    Log.d(TAG, "RewardedAd with id: " + id + " onAdClicked()");
                }
                invokeCallback(id, "FullScreenContentCallback", "onAdClicked");
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                if (debug) {
                    Log.d(TAG, "RewardedAd with id: " + id + " onAdDismissedFullScreenContent()");
                }
                invokeCallback(id, "FullScreenContentCallback", "onAdDismissedFullScreenContent");
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                if (debug) {
                    Log.d(TAG, "RewardedAd with id: " + id + " onAdFailedToShowFullScreenContent() with error: " + adError.getMessage());
                }
                invokeCallback(id, "FullScreenContentCallback", "onAdFailedToShowFullScreenContent");
            }

            @Override
            public void onAdImpression() {
                if (debug) {
                    Log.d(TAG, "RewardedAd with id: " + id + " onAdImpression()");
                }
                invokeCallback(id, "FullScreenContentCallback", "onAdImpression");
            }

            @Override
            public void onAdShowedFullScreenContent() {
                if (debug) {
                    Log.d(TAG, "RewardedAd with id: " + id + " onAdShowedFullScreenContent()");
                }
                invokeCallback(id, "FullScreenContentCallback", "onAdShowedFullScreenContent");
            }
        }));
    }

    private void invokeCallback(long id, String callbackClass, String callbackMethod) {
        invokeCallback(id, callbackClass, callbackMethod, new String[0]);
    }

    private native void invokeCallback(long id, String callbackClass, String callbackMethod, String[] params);
}
