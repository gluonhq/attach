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

package com.gluonhq.attach.ads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdRegistry {

    private long id;

    private final Map<String, CallbackAdapter> handlers;

    private final Map<Long, Ad<?>> ads;

    private final Map<Long, Map<String, Callback>> callbacks;

    public AdRegistry() {
        handlers = new HashMap<>();
        ads = new HashMap<>();
        callbacks = new HashMap<>();

        handlers.put(AdListener.class.getSimpleName(), new AdListener.Adapter());
        handlers.put(InterstitialAdLoadCallback.class.getSimpleName(), new AdLoadCallback.Adapter<InterstitialAd>());
        handlers.put(RewardedAdLoadCallback.class.getSimpleName(), new AdLoadCallback.Adapter<RewardedAd>());
        handlers.put(OnUserEarnedRewardListener.class.getSimpleName(), new OnUserEarnedRewardListener.Adapter());
        handlers.put(FullScreenContentCallback.class.getSimpleName(), new FullScreenContentCallback.Adapter());
    }

    public void addAd(Ad<?> ad) {
        ads.put(ad.getId(), ad);
        callbacks.put(ad.getId(), new HashMap<>());
    }

    public void removeAd(long id) {
        ads.remove(id);
        callbacks.remove(id);
    }

    @SuppressWarnings("unchecked")
    public <T extends Ad<?>> T getAd(long id) {
        return (T) ads.get(id);
    }

    public <T extends Callback> void setCallback(long id, Class<T> callbackClass, T callback) {
        Map<String, Callback> values = callbacks.get(id);
        String name = callbackClass.getSimpleName();

        values.remove(name);

        if (callback != null) {
            values.put(name, callback);
        }
    }

    public void invokeCallback(long id, String callback, String method, String[] params) {
        CallbackAdapter handler = handlers.get(callback);
        Callback value = callbacks.get(id).get(callback);

        if (value != null) {
            handler.invoke(ads.get(id), value, method, params);
        }
    }

    public long nextId() {
        return id++;
    }
}
