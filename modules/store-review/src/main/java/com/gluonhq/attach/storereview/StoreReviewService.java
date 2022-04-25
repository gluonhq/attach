/*
 * Copyright (c) 2022 Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

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
package com.gluonhq.attach.storereview;

import com.gluonhq.attach.util.Services;

import java.util.Optional;

/**
 *
 * The StoreReviewService provides a way to request store ratings and reviews
 * from users.
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code StoreReviewService.create().ifPresent(service -> {
 *      service.requestStoreReview();
 *  });}</pre>
 *
 * @since 4.0.15
 */
public interface StoreReviewService {

    /**
     * Returns an instance of {@link StoreReviewService}.
     * @return An instance of {@link StoreReviewService}.
     */
    static Optional<StoreReviewService> create() {
        return Services.get(StoreReviewService.class);
    }

    /**
     * Prompts the user with a request to rate and do a review
     * of the current app in the store, without leaving the app
     *
     * Warning: on iOS it can be used only up to three times a year for the same app and version,
     * and also the user could have disabled it from Settings
     *
     * @param fallbackURL A string with a URL to access directly the store to leave a review,
     *                    in case the request failed
     */
    void requestStoreReview(String fallbackURL);

}