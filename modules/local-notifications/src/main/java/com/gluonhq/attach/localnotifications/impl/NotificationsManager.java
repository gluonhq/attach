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

import com.gluonhq.attach.localnotifications.Notification;

import java.util.HashMap;
import java.util.Map;

import javafx.application.Platform;

public class NotificationsManager {

    private static final Map<String, Notification> NOTIFICATION_MAP;

    static {
        NOTIFICATION_MAP = new HashMap<>();
    }

    /**
     * This method is called from the iOS/Android layer, in a different
     * thread, so we need to call the runnable in the JavaFX Application
     * Thread.
     *
     * If this method is called too early, and the map doesn't contain yet
     * the notifications, it will be added to the unprocessed list, for later
     * evaluation
     * @param id the id of the notification
     */
    public static void processNotification(String id) {
        if (NOTIFICATION_MAP.containsKey(id)) {
            Runnable runnable = NOTIFICATION_MAP.remove(id).getRunnable();
            if (runnable != null) {
                Platform.runLater(runnable::run);
            }
        }
    }

    /**
     * Register a Notification by its id, for later processing
     * @param n the Notification
     * @return Returns true if the notification was scheduled, or false if 
     *  - the notification was already scheduled in the notifications manager.
     *  - the notification didn't have valid text or scheduled date.
     */
    public static boolean registerNotification(Notification n) {
        if (n == null || n.getId() == null) {
            return false;
        }

        if (NOTIFICATION_MAP.containsKey(n.getId())) {

            // check if notification fields are different, and warn the developer
            // of trying to register a new notification with an existing ID
            Notification m = NOTIFICATION_MAP.get(n.getId());
            if ((m.getDateTime() != null && !m.getDateTime().equals(n.getDateTime())) ||
                    (m.getTitle() != null && !m.getTitle().equals(n.getTitle())) ||
                    (m.getText()!= null && !m.getText().equals(n.getText()))) {

                throw new IllegalArgumentException("Error trying to schedule a new notification with an existing ID");
            }

            // already has a valid notification, don't store/schedule it again
            if (m.getDateTime() != null && m.getText() != null && !m.getText().isEmpty()) {
                return false;
            } // else, override notification:
        }

        NOTIFICATION_MAP.put(n.getId(), n);

        // only schedule valid notifications
        return (n.getDateTime() != null && n.getText() != null && !n.getText().isEmpty());
    }

    /**
     * Unregister a Notification by its id
     * @param id the id of the Notification
     * @return the Notification being unregistered
     */
    public static Notification unregisterNotification(String id) {
        return NOTIFICATION_MAP.remove(id);
    }
}