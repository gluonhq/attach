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
        Log.v(TAG, "Initializing Google Mobile Ads...");

        MobileAds.initialize(activity, initializationStatus -> {
            Log.v(TAG, "Initialization of Google Mobile Ads completed");
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
        if (debug) {
            Log.d(TAG, "bannerAdNew(" + id + ")");
        }

        AdView adView = new AdView(activity);
        FrameLayout layout = new FrameLayout(activity);

        layout.addView(adView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL));

        registry.add(id, adView);
        bannerAdLayouts.put(id, layout);
    }

    private void bannerAdShow(long id) {
        if (debug) {
            Log.d(TAG, "bannerAdShow(" + id + ")");
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
            Log.d(TAG, "bannerAdHide(" + id + ")");
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
            Log.d(TAG, "bannerAdLoad(" + id + ")");
        }

        activity.runOnUiThread(() ->
                registry.<AdView>get(id).loadAd(new AdRequest.Builder().build()));
    }

    private void bannerAdSetLayout(long id, String layout) {
        if (debug) {
            Log.d(TAG, "bannerAdSetLayout(" + id + ", " + layout + ")");
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
            Log.d(TAG, "bannerAdSetAdSize(" + id + ", " + size + ")");
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
            Log.d(TAG, "bannerAdSetAdUnitId(" + id + ", " + adUnitId + ")");
        }

        activity.runOnUiThread(() ->
                registry.<AdView>get(id).setAdUnitId(adUnitId));
    }

    private void bannerAdSetAdListener(long id) {
        if (debug) {
            Log.d(TAG, "bannerAdSetAdListener(" + id + ")");
        }

        activity.runOnUiThread(() -> registry.<AdView>get(id).setAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                invokeCallback(id, "AdListener", "onAdClicked");
            }

            @Override
            public void onAdClosed() {
                invokeCallback(id, "AdListener", "onAdClosed");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                invokeCallback(id, "AdListener", "onAdFailedToLoad");
            }

            @Override
            public void onAdImpression() {
                invokeCallback(id, "AdListener", "onAdImpression");
            }

            @Override
            public void onAdLoaded() {
                invokeCallback(id, "AdListener", "onAdLoaded");
            }

            @Override
            public void onAdOpened() {
                invokeCallback(id, "AdListener", "onAdOpened");
            }

            @Override
            public void onAdSwipeGestureClicked() {
                invokeCallback(id, "AdListener", "onAdSwipeGestureClicked");
            }
        }));
    }

    private void interstitialAdLoad(long id, String adUnitId) {
        if (debug) {
            Log.d(TAG, "interstitialAdLoad(" + id + ", " + adUnitId + ")");
        }

        activity.runOnUiThread(() -> InterstitialAd.load(activity, adUnitId, new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                invokeCallback(id, "InterstitialAdLoadCallback", "onAdFailedToLoad");
            }

            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                registry.add(id, interstitialAd);
                invokeCallback(id, "InterstitialAdLoadCallback", "onAdLoaded");
            }
        }));
    }

    private void interstitialAdShow(long id) {
        if (debug) {
            Log.d(TAG, "interstitialAdShow(" + id + ")");
        }

        activity.runOnUiThread(() ->
                registry.<InterstitialAd>get(id).show(activity));
    }

    private void interstitialAdSetFullScreenContentCallback(long id) {
        if (debug) {
            Log.d(TAG, "interstitialAdSetFullScreenContentCallback(" + id + ")");
        }

        activity.runOnUiThread(() -> registry.<InterstitialAd>get(id).setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdClicked() {
                invokeCallback(id, "FullScreenContentCallback", "onAdClicked");
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                invokeCallback(id, "FullScreenContentCallback", "onAdDismissedFullScreenContent");
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                invokeCallback(id, "FullScreenContentCallback", "onAdFailedToShowFullScreenContent");
            }

            @Override
            public void onAdImpression() {
                invokeCallback(id, "FullScreenContentCallback", "onAdImpression");
            }

            @Override
            public void onAdShowedFullScreenContent() {
                invokeCallback(id, "FullScreenContentCallback", "onAdShowedFullScreenContent");
            }
        }));
    }

    private void rewardedAdLoad(long id, String adUnitId) {
        if (debug) {
            Log.d(TAG, "rewardedAdLoad(" + id + ", " + adUnitId + ")");
        }

        activity.runOnUiThread(() -> RewardedAd.load(activity, adUnitId, new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                invokeCallback(id, "RewardedAdLoadCallback", "onAdFailedToLoad");
            }

            @Override
            public void onAdLoaded(@NonNull RewardedAd interstitialAd) {
                registry.add(id, interstitialAd);
                invokeCallback(id, "RewardedAdLoadCallback", "onAdLoaded");
            }
        }));
    }

    private void rewardedAdShow(long id) {
        if (debug) {
            Log.d(TAG, "rewardedAdShow(" + id + ")");
        }
        
        activity.runOnUiThread(() -> registry.<RewardedAd>get(id).show(activity, new OnUserEarnedRewardListener() {
            @Override
            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                invokeCallback(id, "OnUserEarnedRewardListener", "onUserEarnedReward", new String[] { rewardItem.getType(), String.valueOf(rewardItem.getAmount()) });
            }
        }));
    }

    private void rewardedAdSetFullScreenContentCallback(long id) {
        if (debug) {
            Log.d(TAG, "rewardedAdSetFullScreenContentCallback(" + id + ")");
        }

        activity.runOnUiThread(() -> registry.<RewardedAd>get(id).setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdClicked() {
                invokeCallback(id, "FullScreenContentCallback", "onAdClicked");
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                invokeCallback(id, "FullScreenContentCallback", "onAdDismissedFullScreenContent");
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                invokeCallback(id, "FullScreenContentCallback", "onAdFailedToShowFullScreenContent");
            }

            @Override
            public void onAdImpression() {
                invokeCallback(id, "FullScreenContentCallback", "onAdImpression");
            }

            @Override
            public void onAdShowedFullScreenContent() {
                invokeCallback(id, "FullScreenContentCallback", "onAdShowedFullScreenContent");
            }
        }));
    }

    private void invokeCallback(long id, String callback, String method) {
        invokeCallback(id, callback, method, new String[0]);
    }

    private void invokeCallback(long id, String callback, String method, String[] params) {
        if (debug) {
            Log.d(TAG, "invokeCallback(" + id + ", " + callback + ", " + method + ", " + params + ")");
        }

        nativeInvokeCallback(id, callback, method, params);
    }

    private native void nativeInvokeCallback(long id, String callback, String method, String[] params);
}
