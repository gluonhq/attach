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
import java.util.List;

/**
 * Global configuration that will be used for every AdRequest.
 */
public class RequestConfiguration {

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
     * The age restricted treatment.
     */
    private AgeRestrictedTreatment ageRestrictedTreatment;

    /**
     * The max ad content rating.
     */
    private String maxAdContentRating;

    /**
     * The test device ID's.
     */
    private List<String> testDeviceIds;

    /**
     * Constructs a RequestConfiguration.
     */
    private RequestConfiguration() {
        ageRestrictedTreatment = AgeRestrictedTreatment.UNSPECIFIED;
        maxAdContentRating = MAX_AD_CONTENT_RATING_UNSPECIFIED;
        testDeviceIds = new ArrayList<>();
    }

    /**
     * Returns the value set by the corresponding setAgeRestrictedTreatment.
     *
     * @return the value set by the corresponding setAgeRestrictedTreatment.
     */
    public AgeRestrictedTreatment getAgeRestrictedTreatment() {
        return ageRestrictedTreatment;
    }

    /**
     * Returns the max ad content rating.
     *
     * @return the max ad content rating
     */
    public String getMaxAdContentRating() {
        return maxAdContentRating;
    }

    /**
     * Returns the test device ID's.
     *
     * @return the test device ID's
     */
    public List<String> getTestDeviceIds() {
        return testDeviceIds;
    }

    /**
     * Builder for RequestConfiguration.
     */
    public static class Builder {

        /**
         * The request configuration.
         */
        private final RequestConfiguration config;

        /**
         * Constructs a Builder.
         */
        public Builder() {
            config = new RequestConfiguration();
        }

        /**
         * Builds the RequestConfiguration.
         *
         * @return the request configuration
         */
        public RequestConfiguration build() {
            return config;
        }

        /**
         * Set the age-restricted treatment configuration.
         *
         * @param ageRestrictedTreatment the age restricted treatment
         * @return the builder
         */
        public Builder setAgeRestrictedTreatment(AgeRestrictedTreatment ageRestrictedTreatment) {
            config.ageRestrictedTreatment = ageRestrictedTreatment;
            return this;
        }

        /**
         * Sets a maximum ad content rating.
         *
         * @param rating the rating
         * @return the builder
         */
        public Builder setMaxAdContentRating(String rating) {
            config.maxAdContentRating = rating;
            return this;
        }

        /**
         * Sets a list of test device IDs corresponding to test devices which will always request test ads.
         *
         * @param ids the ids
         * @return the builder
         */
        public Builder setTestDeviceIds(List<String> ids) {
            config.testDeviceIds = ids;
            return this;
        }
    }
}
