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
package com.gluonhq.attach.lifecycle;

import com.gluonhq.attach.util.Services;

import java.util.Optional;

/**
 * The lifecycle service provides a way to listen for events when the application is
 * being paused (put in the background) and resumed (brought back to the foreground).
 * It also allows the developer to properly shutdown the application.
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code LifecycleService.create().ifPresent(service -> {
 *      service.addListener(LifecycleEvent.PAUSE, () -> System.out.println("Application is paused."));
 *      service.addListener(LifecycleEvent.RESUME, () -> System.out.println("Application is resumed."));
 *  });}</pre>
 *
 * <p><b>Android Configuration</b>: none</p>
 * <p><b>iOS Configuration</b>: none</p>
 *
 * @see LifecycleEvent
 * @since 3.0.0
 */
public interface LifecycleService {

    /**
     * Returns an instance of {@link LifecycleService}.
     * @return An instance of {@link LifecycleService}.
     */
    static Optional<LifecycleService> create() {
        return Services.get(LifecycleService.class);
    }

    /**
     * Adds a life cycle event listener to the native platform, to be notified of
     * {@link LifecycleEvent} events.
     *
     * @param lifecycleEvent The type of event to listen for.
     * @param eventHandler The event handler that will be called when the event fires.
     */
    void addListener(LifecycleEvent lifecycleEvent, Runnable eventHandler);

    /**
     * Removes a previously installed event handler. If no such event handler is found,
     * this method is a no-op.
     *
     * @param lifecycleEvent The type of event that was being listened to.
     * @param eventHandler The event handler that should be removed.
     */
    void removeListener(LifecycleEvent lifecycleEvent, Runnable eventHandler);

    /**
     * Initiates the process of shutting down the application that called this method.
     * This removes the need to perform any platform-specific shutdown routines.
     */
    void shutdown();
}
