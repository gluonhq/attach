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

package com.gluonhq.attach.ads;

import java.util.*;

/**
 * Stores all ads with their unique ids.
 */
public class AdRegistry {

    /**
     * Handles the conversion from a callback from the native code
     * to a method from the Java side.
     */
    public interface CallbackAdapter {

        /**
         * Converts a callback from native code to java.
         *
         * @param ad the ad the callback is intended for
         * @param callback the callback class
         * @param callbackMethod the callback method inside the
         *                       callback class
         * @param params the parameters for the method call
         */
        void invoke(Ad<?> ad, Object callback, String callbackMethod, String[] params);
    }

    /**
     * The unique id of the next created ad. This is always
     * incremented by one after an ad got created.
     */
    private long id;

    /**
     * Contains the callback handlers for the ads.
     */
    private final Map<String, CallbackAdapter> handlers;

    /**
     * Contains all ads accessible by their ids.
     */
    private final Map<Long, Ad<?>> ads;

    /**
     * Contains all registered callbacks for ads.
     */
    private final Map<Long, List<Callback>> callbacks;

    /**
     * Constructs a new instance and initializes the callback
     * handlers.
     */
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

    /**
     * Add an ad to the registry and assign it a unique id.
     *
     * @param ad the ad to add
     */
    public void addAd(Ad<?> ad) {
        if (ads.containsKey(ad.getId())) {
            throw new IllegalArgumentException("Id '" + ad.getId() + "' is already inside the ad registry");
        }

        ads.put(ad.getId(), ad);
    }

    /**
     * Remove the ad with the provided id.
     *
     * @param id the id of the ad to remove
     */
    public void removeAd(long id) {
        ads.remove(id);
        callbacks.remove(id);
    }

    /**
     * Get the ad with the specified id.
     *
     * @param id the id of the ad
     * @return the ad with the specified id
     * @param <T> the exact type of the ad
     */
    @SuppressWarnings("unchecked")
    public <T extends Ad<?>> T getAd(long id) {
        return (T) ads.get(id);
    }

    /**
     * Add a callback to the ad with the specified id.
     *
     * @param id the id of the ad
     * @param callbackObject the callback that is added to the ad
     */
    public void addCallback(long id, Object callbackObject) {
        Callback callback = new Callback(callbackObject);
        List<Callback> callbackList;

        if (callbacks.containsKey(id)) {
            callbackList = callbacks.get(id);
        } else {
            callbackList = new ArrayList<>();
            callbacks.put(id, callbackList);
        }

        callbackList.removeIf(c -> c.getName().equals(callback.getName()));
        callbackList.add(callback);
    }

    /**
     * Called from the native side when a callback occurs.
     *
     * @param id the id of the ad the callback is intended for
     * @param callbackClass the name of the callback class
     * @param callbackMethod the name of the callback method
     * @param params the params for the callback method
     */
    public void invokeCallback(long id, String callbackClass, String callbackMethod, String[] params) {
        CallbackAdapter handler = handlers.get(callbackClass);
        Callback callback = callbacks.get(id)
                .stream()
                .filter(c -> c.getName().equals(callbackClass))
                .findFirst()
                .orElseThrow();

        handler.invoke(getAd(id), callback.getCallback(), callbackMethod, params);
    }

    /**
     * Get the next unique id.
     *
     * @return the next unique id
     */
    public long nextId() {
        return id++;
    }
}
