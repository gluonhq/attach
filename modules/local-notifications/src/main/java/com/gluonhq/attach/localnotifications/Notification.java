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
package com.gluonhq.attach.localnotifications;

import java.io.InputStream;
import java.time.ZonedDateTime;

/**
 * Represents a single notification that can be installed onto the system using
 * {@link LocalNotificationsService}.
 *
 * @see LocalNotificationsService
 * @since 3.0.0
 */
public class Notification {

    private final String id;
    private final String title;
    private final String text;
    private final InputStream imageInputStream;
    private final ZonedDateTime dateTime;
    private final Runnable runnable;

    /**
     * Creates a new Notification instance with the specified id and text, set to be displayed on the users device as
     * the given date/time, and when clicked will execute the given Runnable.
     *
     * @param id The unique ID of this notification. It should remain the same through multiple executions of the application.
     * @param text The text to show in the native notification popup.
     * @param dateTime The date and time to show the notification.
     * @param runnable The code to run when the user clicks on the notification.
     */
    public Notification(String id, String text, ZonedDateTime dateTime, Runnable runnable) {
        this(id, null, text, null, dateTime, runnable);
    }

    /**
     * Creates a new Notification instance with the specified id, title, and text, set to be displayed on the users device as
     * the given date/time, and when clicked will execute the given Runnable.
     *
     * @param id The unique ID of this notification. It should remain the same through multiple executions of the application.
     * @param title The title text to show in the native notification popup.
     * @param text The text to show in the native notification popup.
     * @param dateTime The date and time to show the notification.
     * @param runnable The code to run when the user clicks on the notification.
     */
    public Notification(String id, String title, String text, ZonedDateTime dateTime, Runnable runnable) {
        this(id, title, text, null, dateTime, runnable);
    }

    /**
     * Creates a new Notification instance with the specified id, title, text, and image, set to be displayed on the
     * users device as the given date/time, and when clicked will execute the given Runnable.
     *
     * @param id The unique ID of this notification. It should remain the same through multiple executions of the application.
     * @param title The title text to show in the native notification popup.
     * @param text The text to show in the native notification popup.
     * @param imageInputStream An input stream containing image data that can be displayed in the native notification popup.
     * @param dateTime The date and time to show the notification.
     * @param runnable The code to run when the user clicks on the notification.
     */
    public Notification(String id, String title, String text, InputStream imageInputStream, ZonedDateTime dateTime, Runnable runnable) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.imageInputStream = imageInputStream;
        this.dateTime = dateTime;
        this.runnable = runnable;
    }

    /**
     * The id of the notification. 
     * A unique id that will be used to schedule or find the notification on the 
     * device
     *
     * Note: This id will be used over multiple runs of the application so
     * it must remain the same, and not be reused for other notifications
     *
     * @return The unique ID of this notification.
     */
    public final String getId() {
        return id;
    }

    /**
     * The title of the notification
     * @return a String with the title
     */
    public final String getTitle() {
        return title;
    }

    /**
     * The content of the notification
     * @return a String with the text
     */
    public final String getText() {
        return text;
    }

    /**
     * The {@link InputStream} with an image for the notification's logo
     * @return an InputStream
     */
    public final InputStream getImageInputStream() {
        return imageInputStream;
    }

    /**
     * The {@link ZonedDateTime} with the time when the notification is scheduled
     * @return a ZonedDateTime
     */
    public final ZonedDateTime getDateTime() {
        return dateTime;
    }

    /**
     * A runnable to be executed when the notification is displayed and clicked 
     * by the user on the device
     * @return a Runnable
     */
    public final Runnable getRunnable() {
        return runnable;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", dateTime=" + dateTime +
                '}';
    }
}
