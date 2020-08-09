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
package com.gluonhq.attach.ble.parser;

/**
 * https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.gap.appearance.xml
 */
public class AppearanceBleParser implements BleParser {

    @Override
    public String parse(byte[] value) {
        if (value == null) {
            return null;
        }
        
        if (value.length != 2) {
            return "Incorrect data length (2 bytes expected)";
        }
        final Integer intValue = BleUtils.getIntValue(value, BleUtils.FORMAT_SINT16, 0);
        if (intValue == null) {
            return null;
        }
        
        switch (intValue) {
            case 0:    return "[" + intValue + "] Unknown";
            case 64:   return "[" + intValue + "] Generic Phone (Generic category)";
            case 128:  return "[" + intValue + "] Generic Computer (Generic category)";
            case 192:  return "[" + intValue + "] Generic Watch (Generic category)";
            case 193:  return "[" + intValue + "] Watch: Sports Watch (Watch subtype)";
            case 256:  return "[" + intValue + "] Generic Clock (Generic category)";
            case 320:  return "[" + intValue + "] Generic Display (Generic category)";
            case 384:  return "[" + intValue + "] Generic Remote Control (Generic category)";
            case 448:  return "[" + intValue + "] Generic Eye-glasses (Generic category)";
            case 512:  return "[" + intValue + "] Generic Tag (Generic category)";
            case 576:  return "[" + intValue + "] Generic Keyring (Generic category)";
            case 640:  return "[" + intValue + "] Generic Media Player (Generic category)";
            case 704:  return "[" + intValue + "] Generic Barcode Scanner (Generic category)";
            case 768:  return "[" + intValue + "] Generic Thermometer (Generic category)";
            case 769:  return "[" + intValue + "] Thermometer: Ear (Thermometer subtype)";
            case 832:  return "[" + intValue + "] Generic Heart rate Sensor (Generic category)";
            case 833:  return "[" + intValue + "] Heart Rate Sensor: Heart Rate Belt (Heart Rate Sensor subtype)";
            case 896:  return "[" + intValue + "] Generic Blood Pressure (Generic category)";
            case 897:  return "[" + intValue + "] Blood Pressure: Arm (Blood Pressure subtype)";
            case 898:  return "[" + intValue + "] Blood Pressure: Wrist (Blood Pressure subtype)";
            case 960:  return "[" + intValue + "] Human Interface Device (HID) (HID Generic)";
            case 961:  return "[" + intValue + "] Keyboard (HID subtype)";
            case 962:  return "[" + intValue + "] Mouse (HID subtype)";
            case 963:  return "[" + intValue + "] Joystick (HID subtype)";
            case 964:  return "[" + intValue + "] Gamepad (HID subtype)";
            case 965:  return "[" + intValue + "] Digitizer Tablet (HID subtype)";
            case 966:  return "[" + intValue + "] Card Reader (HID subtype)";
            case 967:  return "[" + intValue + "] Digital Pen (HID subtype)";
            case 968:  return "[" + intValue + "] Barcode Scanner (HID subtype)";
            case 1024: return "[" + intValue + "] Generic Glucose Meter (Generic category)";
            case 1088: return "[" + intValue + "] Generic: Running Walking Sensor (Generic category)";
            case 1089: return "[" + intValue + "] Running Walking Sensor: In-Shoe (Running Walking Sensor subtype)";
            case 1090: return "[" + intValue + "] Running Walking Sensor: On-Shoe (Running Walking Sensor subtype)";
            case 1091: return "[" + intValue + "] Running Walking Sensor: On-Hip (Running Walking Sensor subtype)";
            case 1152: return "[" + intValue + "] Generic: Cycling (Generic category)";
            case 1153: return "[" + intValue + "] Cycling: Cycling Computer (Cycling subtype)";
            case 1154: return "[" + intValue + "] Cycling: Speed Sensor (Cycling subtype)";
            case 1155: return "[" + intValue + "] Cycling: Cadence Sensor (Cycling subtype)";
            case 1156: return "[" + intValue + "] Cycling: Power Sensor (Cycling subtype)";
            case 1157: return "[" + intValue + "] Cycling: Speed and Cadence Sensor (Cycling subtype)";
            case 3136: return "[" + intValue + "] Generic (Pulse Oximeter subtype)";
            case 3137: return "[" + intValue + "] Fingertip (Pulse Oximeter subtype)";
            case 3138: return "[" + intValue + "] Wrist Worn (Pulse Oximeter subtype)";
            case 5184: return "[" + intValue + "] Generic (Outdoor Sports Activity subtype)";
            case 5185: return "[" + intValue + "] Location Display Device (Outdoor Sports Activity subtype)";
            case 5186: return "[" + intValue + "] Location and Navigation Display Device (Outdoor Sports Activity subtype)";
            case 5187: return "[" + intValue + "] Location Pod (Outdoor Sports Activity subtype)";
            default:   return "[" + intValue + "] Unknown";
        }
    }
    
}
