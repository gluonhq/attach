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
package com.gluonhq.attach.util;

import com.gluonhq.attach.util.impl.DefaultServiceFactory;
import com.gluonhq.attach.util.impl.ServiceFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Services<T> {

    private static final Map<Class, ServiceFactory> FACTORY_MAP = new HashMap<>();
    private static final Map<Class, Object> SERVICE_MAP = new HashMap<>();

    // not public API
    private Services() { }

    /**
     * Required call to register a ServiceFactory for a given service of type T. 
     * ServiceFactory instance is cached.
     * In case a service is called without its ServiceFactory being registered first,
     * a Runtime Exception will be thrown
     *
     * @param <T> The type of service
     * @param factory The ServiceFactory instance
     */
    public static <T> void registerServiceFactory(ServiceFactory<T> factory) {
        FACTORY_MAP.put(factory.getServiceType(), factory);
    }

    /**
     * Returns an optional with a service, if previously a ServiceFactory was registered.
     * Otherwise, it will try to find a service factory in the same package as the service, and
     * otherwise a Runtime Exception will be thrown.
     * Both serviceFactory and service instances are cached, so only one service
     * is created for the given factory
     *
     * @param <T> the type of service
     * @param service the class of service
     * @return An optional with the service 
     */
    public static <T> Optional<T> get(Class<T> service) {
        if (!FACTORY_MAP.containsKey(service)) {
            final ServiceFactory<T> factory = getFactory(service);
            if (factory != null) {
                registerServiceFactory(factory);
            } else {
                throw new RuntimeException("The service " + service.getSimpleName() + " can't be registered. "
                        + "Call Services.registerServiceFactory() with a valid ServiceFactory");
            }
        }
        if (!SERVICE_MAP.containsKey(service)) {
            FACTORY_MAP.get(service).getInstance()
                    .ifPresent(t -> SERVICE_MAP.put(service, t));
        }
        return Optional.ofNullable((T) SERVICE_MAP.get(service));
    }

    private static <T> ServiceFactory<T> getFactory(Class<T> service) {
        return new DefaultServiceFactory<>(service);
    }
}