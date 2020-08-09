/*
 * Copyright (c) 2020, Gluon
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
package com.gluonhq.attach.ble;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * A BleDevice is defined by a name and an address, and a list of profiles/services, 
 * and a connection state 
 * 
 * @see BleProfile
 */
public class BleDevice {
    
    public enum State {
        STATE_UNKNOWN,
        STATE_CONNECTED, 
        STATE_CONNECTING, 
        STATE_DISCONNECTED, 
        STATE_DISCONNECTING;

        public static State fromName(String stateName) {
            for (State state : values()) {
                if (stateName.equals(state.name())) {
                    return state;
                }
            }
            return STATE_UNKNOWN;
        }

    }
    
    private String name;
    private String address;
    
    private final ObservableList<BleProfile> profiles;
    private final ObjectProperty<State> state;

    public BleDevice() {
        profiles = FXCollections.observableArrayList();
        state = new SimpleObjectProperty<>(State.STATE_UNKNOWN);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public final ObservableList<BleProfile> getProfiles() { return profiles; }
    
    public final ObjectProperty<State> stateProperty() { return state; }
    public final State getState() { return state.get(); }
    public final void setState(State value) { state.set(value); }

    @Override
    public String toString() {
        return "BleDevice{" + "name=" + name + ", address=" + address + '}';
    }
    
}
