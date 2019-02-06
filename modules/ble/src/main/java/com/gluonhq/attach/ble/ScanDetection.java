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
package com.gluonhq.attach.ble;

/**
 * ScanDetection is a wrapper class to hold the main data obtained when scanning a beacon: 
 * uuid, major and minor ids, rssi and proximity estimation
 */
public class ScanDetection {
    
    /**
     * Each beacon transmits its uuid, major and minor id values, and measured power.
     
     * - Measured Power: a factory-calibrated, read-only constant which indicates what's the expected RSSI at a 
     *   distance of 1 meter to the beacon
     * - Broadcasting Power is the power with which the beacon broadcasts its signal. The more power, the longer the range, 
     *   affecting the battery life.
     * - Received Signal Strength Indicator (RSSI). It is the strength of the beacon's signal as seen on the receiving device. 
     *   The signal strength depends on distance and Broadcasting Power value.
     * 
     */
    private String uuid;
    private int major;
    private int minor;
    private int rssi;
    private Proximity proximity;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public Proximity getProximity() {
        return proximity;
    }

    public void setProximity(int proximity) {
        for( Proximity p :  Proximity.values()) {
            if (p.getProximity() == proximity) {
                this.proximity = p;
                break;
            }
        }
    }

    @Override
    public String toString() {
        return "ScanDetection{" + ", uuid=" + uuid + ", major=" + major + ", minor=" + minor + 
                "rssi=" + rssi + ", proximity=" + proximity.name() + '}';
    }

}
