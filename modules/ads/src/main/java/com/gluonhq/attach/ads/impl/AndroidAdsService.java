/*
 * Copyright (c) 2026, Gluon
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

package com.gluonhq.attach.ads.impl;

import com.gluonhq.attach.ads.BannerAd;
import com.gluonhq.attach.ads.InterstitialAd;
import com.gluonhq.attach.ads.RewardedAd;

public class AndroidAdsService extends DefaultAdsService {

    static {
        System.loadLibrary("ads");
    }

    @Override
    protected String getAdUnitId(String adUnitId) {
        switch (adUnitId) {
            case BannerAd.TEST_AD_UNIT_ID: return "ca-app-pub-3940256099942544/6300978111";
            case InterstitialAd.TEST_AD_UNIT_ID: return "ca-app-pub-3940256099942544/1033173712";
            case RewardedAd.TEST_AD_UNIT_ID: return "ca-app-pub-3940256099942544/5224354917";
            default: return adUnitId;
        }
    }

    @Override
    protected native void nativeInitialize();

    @Override
    protected native void nativeSetRequestConfiguration(String ageRestrictedTreatment, String maxAdContentRating, String[] testDeviceIds);

    @Override
    protected native void nativeRemoveAd(long id);

    @Override
    protected native void nativeBannerAdNew(long id);

    @Override
    protected native void nativeBannerAdLoad(long id);

    @Override
    protected native void nativeBannerAdShow(long id);

    @Override
    protected native void nativeBannerAdHide(long id);

        @Override
    protected native void nativeBannerAdSetAdLayout(long id, String layout);

    @Override
    protected native void nativeBannerAdSetAdSize(long id, String size);

    @Override
    protected native void nativeBannerAdSetAdUnitId(long id, String adUnitId);

    @Override
    protected native void nativeInterstitialAdLoad(long id, String adUnitId);

    @Override
    protected native void nativeInterstitialAdShow(long id);

    @Override
    protected native void nativeRewardedAdLoad(long id, String adUnitId);

    @Override
    protected native void nativeRewardedAdShow(long id);
}
