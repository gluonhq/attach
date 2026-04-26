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

import java.util.ArrayList;
import java.util.List;

/**
 * Global configuration that will be used for every {@code AdRequest}.
 */
public class RequestConfiguration {

    /**
     * Provides no indication whether ad requests should be treated as
     * child-directed for purposes of the Children’s Online Privacy
     * Protection Act (COPPA).
     */
    public static final int TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED = -1;

    /**
     * Indicates that ad requests should not be treated as child-directed
     * for purposes of the Children’s Online Privacy Protection Act (COPPA).
     */
    public static final int TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE = 0;

    /**
     * Indicates that ad requests should be treated as child-directed for
     * purposes of the Children’s Online Privacy Protection Act (COPPA).
     */
    public static final int TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE = 1;

    /**
     * Indicates the publisher specified that the ad request should
     * receive treatment for users in the European Economic Area (EEA)
     * under the age of consent.
     */
    public static final int TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE = 1;

    /**
     * Indicates the publisher specified that the ad request should
     * not receive treatment for users in the European Economic Area
     * (EEA) under the age of consent.
     */
    public static final int TAG_FOR_UNDER_AGE_OF_CONSENT_FALSE = 0;

    /**
     * Indicates that the publisher has not specified whether the ad
     * request should receive treatment for users in the European
     * Economic Area (EEA) under the age of consent.
     */
    public static final int TAG_FOR_UNDER_AGE_OF_CONSENT_UNSPECIFIED = -1;

    /**
     * No specified content rating.
     */
    public static final String MAX_AD_CONTENT_RATING_UNSPECIFIED = "";

    /**
     * Content suitable for general audiences, including families.
     */
    public static final String MAX_AD_CONTENT_RATING_G = "G";

    /**
     * Content suitable for most audiences with parental guidance.
     */
    public static final String MAX_AD_CONTENT_RATING_PG = "PG";

    /**
     * Content suitable for teen and older audiences.
     */
    public static final String MAX_AD_CONTENT_RATING_T = "T";

    /**
     * Content suitable only for mature audiences.
     */
    public static final String MAX_AD_CONTENT_RATING_MA = "MA";

    /**
     * Builds a {@code RequestConfiguration}.
     */
    public static class Builder {

        /**
         * The {@code RequestConfiguration} to build.
         */
        private final RequestConfiguration config;

        /**
         * Constructs a new builder and initializes the config.
         */
        public Builder() {
            config = new RequestConfiguration();
        }

        /**
         * Builds the {@code RequestConfig}.
         *
         * @return the final {@code RequestConfiguration}
         */
        public RequestConfiguration build() {
            return config;
        }

        /**
         * Sets the tag for child directed treatment.
         *
         * @param tag the tag to use
         * @return this builder
         */
        public Builder setTagForChildDirectedTreatment(int tag) {
            config.tagForChildDirectedTreatment = tag;
            return this;
        }

        /**
         * Sets the tag for under age of consent.
         *
         * @param tag the tag to use
         * @return this builder
         */
        public Builder setTagForUnderAgeOfConsent(int tag) {
            config.tagForUnderAgeOfConsent = tag;
            return this;
        }

        /**
         * Sets the max content rating.
         *
         * @param rating the rating to use
         * @return this builder
         */
        public Builder setMaxAdContentRating(String rating) {
            config.maxAdContentRating = rating;
            return this;
        }

        /**
         * List of devices that are test devices.
         *
         * @param ids the ids of the test devices
         * @return this builder
         */
        public Builder setTestDeviceIds(List<String> ids) {
            config.testDeviceIds = ids;
            return this;
        }
    }

    /**
     * The tag for child directed treatment.
     */
    private int tagForChildDirectedTreatment;

    /**
     * The tag for under age of consent.
     */
    private int tagForUnderAgeOfConsent;

    /**
     * The max ad content rating.
     */
    private String maxAdContentRating;

    /**
     * A list of devices that are used as test devices.
     */
    private List<String> testDeviceIds;

    /**
     * Constructs a new request configuration with all tags / ratings set to unspecified.
     */
    private RequestConfiguration() {
        tagForChildDirectedTreatment = TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED;
        tagForUnderAgeOfConsent = TAG_FOR_UNDER_AGE_OF_CONSENT_UNSPECIFIED;
        maxAdContentRating = MAX_AD_CONTENT_RATING_UNSPECIFIED;
        testDeviceIds = new ArrayList<>();
    }

    /**
     * Get the tag for child directed treatment.
     *
     * @return the tag for child directed treatment
     */
    public int getTagForChildDirectedTreatment() {
        return tagForChildDirectedTreatment;
    }

    /**
     * Get the tag for under age of consent.
     *
     * @return the tag for under age of consent
     */
    public int getTagForUnderAgeOfConsent() {
        return tagForUnderAgeOfConsent;
    }

    /**
     * Get the max ad content rating.
     *
     * @return the max ad content rating
     */
    public String getMaxAdContentRating() {
        return maxAdContentRating;
    }

    /**
     * Get the list of test device ids.
     *
     * @return the list of test device ids
     */
    public List<String> getTestDeviceIds() {
        return testDeviceIds;
    }
}
