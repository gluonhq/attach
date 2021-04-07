/*
 * Copyright (c) 2020 Gluon
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
package com.gluonhq.attach.accelerometer;

/**
 * A data structure that allows configuring the {@link AccelerometerService}.
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 * @since 4.0.11
 */
public class Parameters {

    private final double frequency;

    private final boolean isFilteringGravity;

    /**
     * Construct new parameters for {@link AccelerometerService}.
     *
     * @param frequency the rate with which to update the service
     * @param isFilteringGravity whether to filter gravity
     */
    public Parameters(double frequency, boolean isFilteringGravity) {
        this.frequency = frequency;
        this.isFilteringGravity = isFilteringGravity;
    }

    /**
     * @return the rate with which to update the service
     */
    public double getFrequency() {
        return frequency;
    }

    /**
     * @return true if gravity is being filtered
     */
    public boolean isFilteringGravity() {
        return isFilteringGravity;
    }

    @Override
    public String toString() {
        return "Parameters{" +
                "frequency=" + frequency +
                ", isFilteringGravity=" + isFilteringGravity +
                '}';
    }
}
