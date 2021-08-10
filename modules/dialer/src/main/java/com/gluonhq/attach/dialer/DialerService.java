/*
 * Copyright (c) 2016, 2019 Gluon
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
package com.gluonhq.attach.dialer;

import com.gluonhq.attach.util.Services;

import java.util.Optional;

/**
 * Using the dialer service, you can initiate a call to the provided phone number. The implementation
 * will use the device's default dial application to make the phone call.
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code DialerService.create().ifPresent(service -> {
 *      service.call("+32987123456");
 *  });}</pre>
 *
 * <p><b>Android Configuration</b></p>
 * <p>The permission <code>android.permission.CALL_PHONE</code> needs to be added.</p>
 *
 * Note: these modifications are handled automatically by <a href="https://docs.gluonhq.com/">GluonFX plugin</a> if it is used.
 * <pre>
 * {@code <manifest ...>
 *    <uses-permission android:name="android.permission.CALL_PHONE"/>
 *    ...
 *   <activity android:name="com.gluonhq.impl.attach.plugins.android.PermissionRequestActivity" />
 *  </manifest>}</pre>
 *
 * <p><b>iOS Configuration</b>: none</p>
 *
 * @since 3.0.0
 */
public interface DialerService {

    /**
     * Returns an instance of {@link DialerService}.
     * @return An instance of {@link DialerService}.
     */
    static Optional<DialerService> create() {
        return Services.get(DialerService.class);
    }

    /**
     * Starts a phone call to the given number with the native dial application.
     *
     * @param number A valid telephone number, without spaces, optionally including an international prefix.
     */
    void call(String number);
}
