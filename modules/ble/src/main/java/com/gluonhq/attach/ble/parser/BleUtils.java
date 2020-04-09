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
 * https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/bluetooth/BluetoothGattCharacteristic.java
 */
public class BleUtils {
    /**
     * Characteristic value format type uint8
     */
    public static final int FORMAT_UINT8 = 0x11;
    /**
     * Characteristic value format type uint16
     */
    public static final int FORMAT_UINT16 = 0x12;
    /**
     * Characteristic value format type uint32
     */
    public static final int FORMAT_UINT32 = 0x14;
    /**
     * Characteristic value format type sint8
     */
    public static final int FORMAT_SINT8 = 0x21;
    /**
     * Characteristic value format type sint16
     */
    public static final int FORMAT_SINT16 = 0x22;
    /**
     * Characteristic value format type sint32
     */
    public static final int FORMAT_SINT32 = 0x24;
    /**
     * Characteristic value format type sfloat (16-bit float)
     */
    public static final int FORMAT_SFLOAT = 0x32;
    /**
     * Characteristic value format type float (32-bit float)
     */
    public static final int FORMAT_FLOAT = 0x34;

    /**
     * Return the stored value of this characteristic.
     *
     * <p>The formatType parameter determines how the characteristic value
     * is to be interpreted. For example, setting formatType to
     * {@link #FORMAT_UINT16} specifies that the first two bytes of the
     * characteristic value at the given offset are interpreted to generate the
     * return value.
     *
     * @param value byte array with the stored value
     * @param formatType The format type used to interpret the characteristic
     *                   value.
     * @param offset Offset at which the integer value can be found.
     * @return Cached value of the characteristic or null of offset exceeds
     *         value size.
     */
    static Integer getIntValue(byte[] value, int formatType, int offset) {
        if ((offset + getTypeLen(formatType)) > value.length) {
            return null;
        }
        
        switch (formatType) {
            case FORMAT_UINT8:
                return unsignedByteToInt(value[offset]);
            case FORMAT_UINT16:
                return unsignedBytesToInt(value[offset], value[offset+1]);
            case FORMAT_UINT32:
                return unsignedBytesToInt(value[offset], value[offset+1], value[offset+2], value[offset+3]);
            case FORMAT_SINT8:
                return unsignedToSigned(unsignedByteToInt(value[offset]), 8);
            case FORMAT_SINT16:
                return unsignedToSigned(unsignedBytesToInt(value[offset],
                                                           value[offset+1]), 16);
            case FORMAT_SINT32:
                return unsignedToSigned(unsignedBytesToInt(value[offset],
                        value[offset+1], value[offset+2], value[offset+3]), 32);
        }
        return null;
    }
    
    /**
     * Returns the size of a give value type.
     * @param formatType
     * @return 
     */
    private static int getTypeLen(int formatType) {
        return formatType & 0xF;
    }
    
    /**
     * Convert a signed byte to an unsigned int.
     */
    private static int unsignedByteToInt(byte b) {
        return b & 0xFF;
    }
    /**
     * Convert signed bytes to a 16-bit unsigned int.
     */
    private static int unsignedBytesToInt(byte b0, byte b1) {
        return unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8);
    }
    
    /**
     * Convert signed bytes to a 32-bit unsigned int.
     */
    private static int unsignedBytesToInt(byte b0, byte b1, byte b2, byte b3) {
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8))
             + (unsignedByteToInt(b2) << 16) + (unsignedByteToInt(b3) << 24);
    }
    
    /**
     * Convert an unsigned integer value to a two's-complement encoded
     * signed value.
     */
    private static int unsignedToSigned(int unsigned, int size) {
        if ((unsigned & (1 << size-1)) != 0) {
            unsigned = -1 * ((1 << size-1) - (unsigned & ((1 << size-1) - 1)));
        }
        return unsigned;
    }
}
