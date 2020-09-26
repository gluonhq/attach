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
package com.gluonhq.attach.cache;

import com.gluonhq.attach.util.Services;

import java.util.Optional;

/**
 * The cache service provides a simple API to weakly cache objects in memory. There is no
 * guarantee that items being put in the cache will be retained for any minimum amount of time.
 * They will be garbage collected when necessary.
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code CacheService.create().ifPresent(service -> {
 *      Cache<String, String> cache = service.getCache("simpleCache");
 *      cache.put("key", "value");
 *      String value = cache.get("key");
 *  });}</pre>
 *
 * <p><b>Android Configuration</b>: none</p>
 * <p><b>iOS Configuration</b>: none</p>
 *
 * @since 3.0.0
 */
public interface CacheService {

    /**
     * Returns an instance of {@link CacheService}.
     * @return An instance of {@link CacheService}.
     */
    static Optional<CacheService> create() {
        return Services.get(CacheService.class);
    }

    /**
     * Returns a {@link Cache} instance. The name is used to allow for multiple cache instances to be created,
     * with the name being a unique identifier, returning the same cache every time when given the same name.
     *
     * @param cacheName The name of the cache.
     * @param <K> The key type for the cache
     * @param <V> The value type for the cache
     * @return A named {@link Cache} instance.
     */
    <K,V> Cache<K,V> getCache(String cacheName);

}
