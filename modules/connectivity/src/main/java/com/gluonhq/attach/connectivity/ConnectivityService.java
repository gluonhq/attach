/*
 * Copyright (c) 2016, Gluon
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
package com.gluonhq.attach.connectivity;

import com.gluonhq.attach.util.Services;
import javafx.beans.property.ReadOnlyBooleanProperty;

import java.util.Optional;

/**
 * The connectivity service can be used to determine whether the device is currently
 * connected to a network or not. The connectivity can be to any networking service,
 * i.e. WiFi, cell phone data, etc.
 *
 * <p>The ConnectivityService provides a read-only {@link #connectedProperty() connected property}
 * that is updated whenever the network connectivity of the device changes. A user of the
 * ConnectivityService can listen to changes of the network connectivity, by registering a
 * {@link javafx.beans.value.ChangeListener ChangeListener} to the
 * {@link #connectedProperty() connected property}.
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code ConnectivityService.create().ifPresent(service -> {
 *      boolean connected = service.isConnected();
 *      System.out.println("Network connectivity available? " + String.valueOf(connected));
 *  });}</pre>
 *
 * <p><b>Android Configuration</b></p>
 * <p>The permission <code>android.permission.ACCESS_NETWORK_STATE</code> needs to be added.</p>
 *
 * Note: these modifications are handled automatically by <a href="https://docs.gluonhq.com/client">Client plugin</a> if it is used.
 * <pre>
 * {@code <manifest ...>
 *    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
 *    ...
 *  </manifest>}</pre>
 *
 * <p><b>iOS Configuration</b>: none</p>
 *
 * @since 3.0.0
 */
public interface ConnectivityService {

    /**
     * Returns an instance of {@link ConnectivityService}.
     * @return An instance of {@link ConnectivityService}.
     */
    static Optional<ConnectivityService> create() {
        return Services.get(ConnectivityService.class);
    }

    /**
     * A read-only property indicating whether there is data connectivity available or not.
     * @return Property will be true if there is data connectivity, and false otherwise.
     */
    ReadOnlyBooleanProperty connectedProperty();

    /**
     * A boolean indicating whether there is data connectivity available or not.
     * @return Will be true if there is data connectivity, and false otherwise.
     */
    boolean isConnected();
}