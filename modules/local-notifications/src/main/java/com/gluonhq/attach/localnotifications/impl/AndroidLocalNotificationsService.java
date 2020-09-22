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
package com.gluonhq.attach.localnotifications.impl;

import com.gluonhq.attach.localnotifications.Notification;
import com.gluonhq.attach.runtimeargs.RuntimeArgsService;
import com.gluonhq.attach.storage.StorageService;
import com.gluonhq.attach.util.Util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 *  Android implementation of LocalNotificationsService.
 */
public class AndroidLocalNotificationsService extends LocalNotificationsServiceBase {

    private static final Logger LOG = Logger.getLogger(AndroidLocalNotificationsService.class.getName());

    private static final boolean debug = Util.DEBUG;

    static {
        System.loadLibrary("localnotifications");
    }

    private File privateStorage;

    public AndroidLocalNotificationsService() {
    }

    @Override
    protected void unscheduleNotification(Notification notification) {
        if (notification != null) {
            if (debug) {
                LOG.fine("Unregistering notification id: " + notification.getId());
            }
            unregisterNotification(notification.getId());
        }
    }
    
    @Override
    protected void scheduleNotification(Notification notification) {
        if (debug) {
            LOG.info("Registering notification: " + notification);
        }
        registerNotification(notification.getTitle() == null ? "" : notification.getTitle(),
                notification.getText() == null ? "" : notification.getText(),
                notification.getId() == null ? "" : notification.getId(),
                getImagePath(notification.getImageInputStream()),
                notification.getDateTime().toEpochSecond() * 1000L);
    }

    private String getImagePath(InputStream imageInputStream) {
        if (imageInputStream == null) {
            return "";
        }

        if (privateStorage == null) {
            privateStorage = StorageService.create()
                    .flatMap(StorageService::getPrivateStorage)
                    .orElseThrow(() -> new RuntimeException("Error accessing Private Storage folder"));
        }

        Path file = privateStorage.toPath()
                .resolve("assets")
                .resolve("notifications")
                .resolve("icon.png");

        try {
            if (!Files.exists(file.getParent())) {
                Files.createDirectories(file.getParent());
            }
            Files.copy(imageInputStream, file, REPLACE_EXISTING);

            if (debug) {
                LOG.fine("Icon.png file: " + file);
            }
            return file.toString();
        } catch (IOException e) {
            LOG.severe("Error creating icon.png: " + e.getMessage());
        }
        return "";

    }

    // native
    private native void registerNotification(String title, String text, String identifier,
                                             String imagePath, long millis);
    private native void unregisterNotification(String identifier);

    // callback
    private static void processRuntimeArgs(String key, String value) {
        RuntimeArgsService.create().ifPresent(ra -> ra.fire(key, value));
    }
}
