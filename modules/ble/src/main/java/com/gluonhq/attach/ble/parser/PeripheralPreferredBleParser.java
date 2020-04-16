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
 * https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.characteristic.gap.peripheral_preferred_connection_parameters.xml
 */
public class PeripheralPreferredBleParser implements BleParser {

    @Override
    public String parse(byte[] value) {
        if (value == null) {
            return null;
        }
        
        if (value.length != 8) {
            return "Incorrect data length (8 bytes expected)";
        }
        
        int offset = 0;
        Integer intValue = BleUtils.getIntValue(value, BleUtils.FORMAT_UINT16, offset);
        if (intValue == null) {
            return null;
        }
        StringBuilder result = new StringBuilder("Minimum Connection Interval: ")
                .append(intValue == 65535 ? "No specific minimum" : String.format("%.02f ms", (intValue * 1.25f)));
        offset += 2;
        
        intValue = BleUtils.getIntValue(value, BleUtils.FORMAT_UINT16, offset);
        if (intValue == null) {
            return null;
        }
        result.append("\n\tMaximum Connection Interval: ")
                .append(intValue == 65535 ? "No specific maximum" : String.format("%.02f ms", (intValue * 1.25f)));
        offset += 2;  
        
        intValue = BleUtils.getIntValue(value, BleUtils.FORMAT_UINT16, offset);
        if (intValue == null) {
            return null;
        }
        result.append("\n\tSlave Latency: ")
                .append(intValue == 65535 ? "Undefined" : intValue);
        offset += 2;  
        
        intValue = BleUtils.getIntValue(value, BleUtils.FORMAT_UINT16, offset);
        if (intValue == null) {
            return null;
        }
        result.append("\n\tConnection Supervision Timeout : ")
                .append(intValue == 65535 ? "No specific value requested" : intValue);
        
        return result.toString();
    }
    
}
