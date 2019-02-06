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

import java.util.LinkedList;
import java.util.List;

public class Configuration {
    /**
     * uuid: set the initial uuid of beacons that will be scanned
     */
    private final List<String> uuids = new LinkedList<>();
    
    /**
     * Create a configuration that allows to listen to the specified UUID
     * @param uuid the uuid of the beacons we will be monitoring/scanning
     */
    public Configuration(String uuid) {
        this.uuids.add(uuid);
    }
    
    /**
     * Adds a uuid to the list of beacons we want to monitor/scan
     * @param uuid the UUID of the beacons we want to follow
     */
    public void addUuid(String uuid) {
        this.uuids.add(uuid);
    }
    
    /**
     * Remove this uuid from the list of uuid-beacons we want to monitor/scan
     * @param uuid the uuid we want to remove
     */
    public void removeUuid(String uuid) {
        this.uuids.remove(uuid);
    }
    
    /**
     * Returns a list of uuids this configuration allows to monitor/scan.
     * This method never returns null.
     * @return the list of uuids, or an empty list of we don't want to follow a
     * specific uuid. 
     */
    public List<String> getUuids() {
        return this.uuids;
    }
    
}
