/*
 * Copyright (c) 2016, 2020, Gluon
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

#import <UIKit/UIKit.h>
#include "jni.h"
#include "AttachMacros.h"
#import <CoreLocation/CoreLocation.h>
#import <CoreBluetooth/CoreBluetooth.h>

@interface Ble : UIViewController <CLLocationManagerDelegate, CBCentralManagerDelegate, CBPeripheralManagerDelegate, CBPeripheralDelegate>
{
}
    @property(nonatomic, strong) CLLocationManager *locationManager;
    @property(nonatomic, strong) CBCentralManager *bluetoothManager;
    @property(nonatomic, strong) CBPeripheral *peripheral;
    @property(nonatomic, strong) CBPeripheralManager *peripheralManager;
    @property(nonatomic, strong) CLBeaconRegion *localBeacon;
    - (void) startObserver;
    - (void) stopObserver;
    - (void) startBroadcast:(NSString *)uuidString major:(NSInteger)major minor:(NSInteger)minor id:(NSString *)idString;
    - (void) stopBroadcast;

    - (void) startScanningPeripherals;
    - (void) stopScanningPeripherals;
    - (void) connect:(NSString *)name uuid:(NSString *)uuid;
    - (void) disconnect:(NSString *)name uuid:(NSString *)uuid;
    - (void) read:(NSString *)name uuidService:(NSString *)uuidService uuidChar:(NSString *)uuidChar;
    - (void) write:(NSString *)name uuidService:(NSString *)uuidService uuidChar:(NSString *)uuidChar data:(NSData *)data;
    - (void) subscribe:(NSString *)name uuidService:(NSString *)uuidService uuidChar:(NSString *)uuidChar notify:(BOOL)notify;

@end

void setDetection(CLBeacon *foundBeacon);

void discoveredPeripheral(CBPeripheral *peripheral);
void stateChanged(CBPeripheral *peripheral);
void discoveredProfile(CBPeripheral *peripheral, CBService *service);
void removeProfile(CBPeripheral *peripheral, CBService *service);
void discoveredCharacteristic(CBPeripheral *peripheral, CBService *service, CBCharacteristic *aChar);
void setData(CBPeripheral *peripheral, CBCharacteristic *aChar);
void discoveredDescriptor(CBPeripheral *peripheral, CBDescriptor *aDesc);