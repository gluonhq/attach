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

/**
 * Cache is a simple Map-like API for caching in-memory on the local platform in a way roughly equivalent
 * to using {@link java.lang.ref.SoftReference soft references}. Because of the quirks of some platforms,
 * it is better to use this Cache API instead of a {@link java.util.WeakHashMap} or a Map with weak or
 * soft references.
 * 
 * @param <K> type for the key
 * @param <V> type for the value
 */
public interface Cache<K, V>  {

    /**
     * Get the value for the specified key, or null when there is no such key.
     * The latter can be because there never was an entry with this key stored, or the
     * entry with this key has been reclaimed.
     *
     * @param key the key for which we need the value
     * @return the value for the specified key, <code>null</code> in case there is no
     * value corresponding to this key.
     */
    V get(K key);

    /**
     * Stores a key-value pair in the cache. A <code>NullPointerException</code> will be
     * thrown if the key or the value are <code>null</code>.
     * @param key the key, should not be <code>null</code>
     * @param value the value, should not be <code>null</code>
     */
    void put(K key, V value);

    /**
     * Remove the entry associated with this key.
     * @param key the key for which the entry is requested
     * @return true if the cache contained an entry with this key
     */
    boolean remove(K key);

    /**
     * Remove all entries from the cache
     */
    void removeAll();

}
