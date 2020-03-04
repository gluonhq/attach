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

import java.util.Arrays;
import java.util.UUID;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Characteristics are defined attribute types that contain a single logical value. 
 * 
 * A BleCharacteristic is defined by an UUID, a list of properties, a value, and 
 * a list of descriptors
 * 
 * @see BleDescriptor
 */
public class BleCharacteristic {

    private final UUID uuid;
    private String properties;
    private final ObjectProperty<byte[]> value;
    private final ObservableList<BleDescriptor> descriptors;

    public BleCharacteristic(UUID uuid) {
        value = new SimpleObjectProperty<>();
        descriptors = FXCollections.observableArrayList();
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }
    
    public String getProperties() {
        return properties;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public final byte[] getValue() {
        return value.get();
    }

    public final void setValue(byte[] value) {
        this.value.set(value);
    }
    
    public final ObjectProperty<byte[]> valueProperty() {
        return value;
    }

    public final ObservableList<BleDescriptor> getDescriptors() { 
        return descriptors; 
    }

    @Override
    public String toString() {
        return "BleCharacteristic{" + "UUID=" + uuid + ", properties=" + properties + 
                ", value=" + Arrays.toString(value.get()) + '}';
    }
    
}
