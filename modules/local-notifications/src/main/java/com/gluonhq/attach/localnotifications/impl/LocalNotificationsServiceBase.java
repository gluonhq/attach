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
package com.gluonhq.attach.localnotifications.impl;

import com.gluonhq.attach.runtime.RuntimeArgsService;
import java.util.logging.Logger;
import com.gluonhq.attach.util.Services;
import com.gluonhq.attach.localnotifications.LocalNotificationsService;
import com.gluonhq.attach.localnotifications.Notification;
//import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * RAS is added after changes in the notifications list to prevent an empty list if the listener
 * is added too soon, before the notifications are provided.
 */
public abstract class LocalNotificationsServiceBase implements LocalNotificationsService {
    private static final Logger LOGGER = Logger.getLogger(LocalNotificationsServiceBase.class.getName());

    private final ObservableList<Notification> notifications;

    public LocalNotificationsServiceBase() {
        notifications = FXCollections.observableArrayList();
        notifications.addListener((ListChangeListener.Change<? extends Notification> c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (Notification n : c.getAddedSubList()) {
                        try {
                            boolean needsScheduling = NotificationsManager.registerNotification(n);
                            if (needsScheduling) {
                                scheduleNotification(n);
                            }
                        }
                        catch (Exception e) {
                            LOGGER.severe("Registering a notification created a (non-fatal) exception.");
                            e.printStackTrace();
                        }
                    }
                } else if (c.wasRemoved()) {
                    for (Notification n : c.getRemoved()) {
                        unscheduleNotification(NotificationsManager.unregisterNotification(n.getId()));
                    }
                }

                // With each change, auto register to listen to notifications
                Services.get(RuntimeArgsService.class).ifPresent(service -> {
                    service.removeListener(RuntimeArgsService.LAUNCH_LOCAL_NOTIFICATION_KEY);
                    service.addListener(RuntimeArgsService.LAUNCH_LOCAL_NOTIFICATION_KEY,
                            NotificationsManager::processNotification);
                });
            }
        });
    }

    /**
     * An Observable List of Notifications, that can be used to
     * add or remove notifications
     *
     * If the notification is marked as scheduled, it won't be
     * added to the device
     *
     * @return ObservableList of Notification
     */
    @Override
    public final ObservableList<Notification> getNotifications() {
        return notifications;
    }

    /**
     * Used by the platform-dependent implementation to scheduleNotification the notification
     * @param notification to be scheduled
     */
    protected abstract void scheduleNotification(Notification notification);

    /**
     * Used by the platform-dependent implementation to unscheduleNotification the notification
     * @param notification to be unscheduled
     */
    protected abstract void unscheduleNotification(Notification notification);

}
