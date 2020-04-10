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
 * https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.heart_rate_measurement.xml
 */
public class HeartRateMeasurementBleParser implements BleParser {

    @Override
    public String parse(byte[] value) {
        if (value == null) {
            return null;
        }
        
        int offset = 0;
        int flag = BleUtils.getIntValue(value, BleUtils.FORMAT_UINT8, offset);
        offset += 1;

        // bit1: Heart Rate Value Format bit
        int bit1 = flag & 0x1;
        
        int intValue;
        if (bit1 == 0) { // Heart Rate Value Format is set to UINT8, units bpm
            // 1 byte
            intValue = BleUtils.getIntValue(value, BleUtils.FORMAT_UINT8, offset);
            offset += 1;
        } else {  // Heart Rate Value Format is set to UINT16, units bpm
            // 2 bytes
            intValue = BleUtils.getIntValue(value, BleUtils.FORMAT_UINT16, offset);
            offset += 2;
        }
         
        StringBuilder result = new StringBuilder("Heart Rate Measurement: ")
                .append(intValue)
                .append(" bpm");
            
        int bit23 = (flag >> 1) & 0x3; // Sensor Contact Status bits
        switch (bit23)  {
            case 0: // Sensor Contact feature is not supported in the current connection
            case 1: // Sensor Contact feature is not supported in the current connection
                result.append("\n\tSensor Contact not supported");
                break;
            case 2: // Sensor Contact feature is supported, but contact is not detected
                result.append("\n\tSensor Contact supported but not detected");
                break;
            case 3: // Sensor Contact feature is supported and contact is detected
                result.append("\n\tSensor Contact supported and detected");
                break;
        }
        int bit4 = (flag >> 3) & 0x1; // Energy Expended Status bit
        if (bit4 == 0) { // Energy Expended field is not present
            result.append("\n\tEnergy Expended field not present");
        } else {  // Energy Expended field is present. Units: kilo Joules
            // 2 bytes
            intValue = BleUtils.getIntValue(value, BleUtils.FORMAT_UINT16, offset);
            result.append("\n\tEnergy Expended: " ).append(intValue).append(" kJ");
            offset += 2;
        }
        int bit5 = (flag >> 4) & 0x1; // RR-Interval bit
        if (bit5 == 0) { // RR-Interval values are not present
            result.append("\n\tRR-Interval values not present");
        } else {  // One or more RR-Interval values are present, Resolution of 1/1024 second
            result.append("\n\tRR-Interval values: ");
            while (offset < value.length - 2) {
                // 2 bytes
                intValue = BleUtils.getIntValue(value, BleUtils.FORMAT_UINT16, offset);
                offset += 2;
                result.append(String.format("%.02f ms", (intValue * 1000f / 1024f)));
                if (offset < value.length - 2) {
                    result.append(", ");
                }
            }
        }
        
        return result.toString();
    }
    
}
