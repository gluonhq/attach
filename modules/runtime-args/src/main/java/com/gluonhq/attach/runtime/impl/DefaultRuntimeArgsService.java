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
package com.gluonhq.attach.runtime.impl;

import com.gluonhq.attach.runtime.RuntimeArgsService;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultRuntimeArgsService implements RuntimeArgsService {

    private static final Logger LOG = Logger.getLogger(DefaultRuntimeArgsService.class.getName());

    private final static Map<String, Consumer<String>> RUNTIME_MAP;

    static {
        RUNTIME_MAP = new HashMap<>();
    }

    @Override public void fire(String key, String value) {
        if (RUNTIME_MAP.containsKey(key)) {
            if (RUNTIME_MAP.get(key) != null) {
                RUNTIME_MAP.get(key).accept(value);
            } else {
                LOG.log(Level.WARNING, String.format("Consumer for the key %s is null", key));
            }
        } else {
            LOG.log(Level.WARNING, String.format("The key %s was not found", key));
        }
    }

    @Override public void addListener(String key, Consumer<String> consumer) {
        if (consumer == null) {
            throw new IllegalArgumentException("Consumer can not be null");
        }
        if (RUNTIME_MAP.containsKey(key)) {
            throw new IllegalArgumentException("Key already registered");
        }
        if (RUNTIME_MAP.containsValue(consumer)) {
            throw new IllegalArgumentException("Consumer already registered");
        }
        RUNTIME_MAP.put(key, consumer);

        // consume unprocessed items if available at launch time
        String value = System.getProperty(key, null);
        if (value != null && !value.isEmpty()) {
            // clear properties
            try {
                System.clearProperty(key);
            } catch (Exception e) {
                LOG.log(Level.WARNING, String.format("Error clearing system property for key %s: %s", key, e));
            }
            fire(key, value);
        }
    }

    @Override public void removeListener(String key) {
        RUNTIME_MAP.remove(key);
    }
}
