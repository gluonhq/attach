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
package com.gluonhq.attach.util.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A thread that watches certain properties in the native platform to determine whether
 * to notify the user of their change (so that JavaFX properties may be used).
 */
public class PropertyWatcher {

    private static final int THREAD_SLEEP_DURATION = 5000;
    private static final List<Runnable> PROPERTY_WATCHERS = new ArrayList<>();

    private static boolean running = false;
    private static Thread watcherThread;

    private PropertyWatcher() { }

    public static void addPropertyWatcher(Runnable r) {
        // before we add this to the thread for constant checking, we immediately check to set the property
        // correctly from the beginning
        r.run();
        PROPERTY_WATCHERS.add(r);
        updateState();
    }

    public static void removePropertyWatcher(Runnable r) {
        PROPERTY_WATCHERS.remove(r);
        updateState();
    }

    private static void updateState() {
        if (PROPERTY_WATCHERS.isEmpty()) {
            stopThread();
        } else if (!running) {
            startThread();
        }
    }

    private static void startThread() {
        running = true;

        if (watcherThread == null) {
            watcherThread = new Thread(() -> {
                while (running) {
                    for (Runnable r : PROPERTY_WATCHERS) {
                        r.run();
                    }

                    try {
                        Thread.sleep(THREAD_SLEEP_DURATION);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(PropertyWatcher.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                watcherThread = null;
            });
            watcherThread.setDaemon(true);
            watcherThread.start();
        }
    }

    private static void stopThread() {
        running = false;
    }
}
