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
 */package com.gluonhq.attach.device.impl;

import com.gluonhq.attach.device.DeviceService;
import com.gluonhq.attach.util.Constants;

/**
 * An implementation of the {@link DeviceService} for the Android
 * platform.
 */
public class AndroidDeviceService implements DeviceService {

    static {
        System.loadLibrary("Device");
    }
    private static final DeviceInfo deviceInfo = getDeviceInfo();

    public AndroidDeviceService() {
        if (Boolean.getBoolean(Constants.ATTACH_DEBUG)) {
            enableDebug();
        }
    }

    @Override
    public String getModel() {
        return deviceInfo.getModel();
    }

    @Override
    public String getUuid() {
        return deviceInfo.getUuid();
    }

    @Override
    public String getPlatform() {
        return deviceInfo.getPlatform();
    }

    @Override
    public String getVersion() {
        return deviceInfo.getVersion();
    }

    @Override
    public String getSerial() {
        return deviceInfo.getSerial();
    }

    @Override
    public boolean isWearable() {
        return deviceInfo.isWearable(); 
    }

    private static native void enableDebug();
    private static native DeviceInfo getDeviceInfo();
}