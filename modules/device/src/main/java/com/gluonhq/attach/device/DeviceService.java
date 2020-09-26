/*
 * Copyright (c) 2016, 2019 Gluon
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
package com.gluonhq.attach.device;

import com.gluonhq.attach.util.Services;

import java.util.Optional;

/**
 * The device service provides generic properties of the device on which the application is running.
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code DeviceService.create().ifPresent(service -> {
 *      System.out.printf("Device Model Name: %s", service.getModel());
 *  });}</pre>
 *
 * <p><b>Android Configuration</b>: none</p>
 * <p><b>iOS Configuration</b>: none</p>
 *
 * @since 3.0.0
 */
public interface DeviceService {

    /**
     * Returns an instance of {@link DeviceService}.
     * @return An instance of {@link DeviceService}.
     */
    static Optional<DeviceService> create() {
        return Services.get(DeviceService.class);
    }

    /**
     * Returns the name of the device's model or product. The value is set by the device
     * manufacturer and may be different across versions of the same product.
     *
     * @return The device model.
     */
    String getModel();

    /**
     * Returns the device's universally unique identifier.
     *
     * @return The device UUID.
     */
    String getUuid();

    /**
     * Returns the platform string that the operating system uses to identify itself.
     *
     * @return The device platform.
     */
    String getPlatform();

    /**
     * Returns the version number of the device platform.
     *
     * @return The device version.
     */
    String getVersion();

    /**
     * Returns true if the device is a wearable
     *
     * @return true if the device is a wearable
     * @since 3.3.0
     */
    boolean isWearable();
}
