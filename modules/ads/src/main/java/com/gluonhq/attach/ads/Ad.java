/*
 * Copyright (c) 2026, Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
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

package com.gluonhq.attach.ads;

/**
 * The base class for all ad types.
 *
 * @param <T> the type of service the ad uses
 */
public abstract class Ad<T extends Ad.Service> {

    /**
     * The unique id of the ad.
     */
    protected final long id;

    /**
     * The service the ad uses.
     */
    protected final T service;

    /**
     * Constructs an ad.
     *
     * @param id the unique id of the ad
     * @param service the service the ad uses
     */
    public Ad(long id, T service) {
        this.id = id;
        this.service = service;
    }

    /**
     * Disposes the ad.
     */
    public void dispose() {
        service.dispose(this);
    }

    /**
     * Returns the unique id of the ad.
     *
     * @return the unique id of the ad
     */
    public long getId() {
        return id;
    }

    public interface Service {

        void dispose(Ad<?> ad);
    }
}
