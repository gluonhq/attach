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
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

public class Services<T> {

    private static final Logger LOGGER = Logger.getLogger(Services.class.getName());

    private static final Map<Class<?>, ServiceFactory<?>> FACTORY_MAP        = new HashMap<>();
	private static final Map<Class<?>, Optional      <?>> SERVICE_MAP        = new HashMap<>();

	private static final               Optional      <?>  SERVICE_NULL_VALUE = Optional.of(new Object());

    // not public API
    private Services() { }

    /**
     * Required call to register a ServiceFactory for a given service of type T. 
     * ServiceFactory instance is cached.
     * In case a service is requested without its ServiceFactory being registered first
     * and no Default Service could be obtained,
     * a Warning will be logged.
     *
     * @param <T> The type of service
     * @param factory The ServiceFactory instance
     * @throws NullPointerException if {@code factory.getServiceType()} is {@code null}
     */
    public static <T> void registerServiceFactory(ServiceFactory<T> factory) {
        LOGGER.fine("Register " + factory);
		FACTORY_MAP.put(Objects.requireNonNull(factory.getServiceType()), factory);
    }

    /**
     * Returns an optional with a service, if previously a ServiceFactory was registered.
     * Otherwise, it will try to find a service factory in the same package as the service, and
     * otherwise a Warning will be logged.
     * Both serviceFactory and service instances are cached, so only one service
     * is created for the given factory
     *
     * @param <T> the type of service
     * @param serviceClass the class of service
     * @return An optional with the service (which may be {@code Optional.empty()})
     */
    public static <T> Optional<T> get(final Class<T> serviceClass) {
        LOGGER.fine("Get Service " + serviceClass.getName());
		/*
		 * Note: SERVICE_MAP is used & updated SOLELY within this Method.
		 */
		if (SERVICE_MAP.containsKey(serviceClass)) {
			/*
			 * Service already Mapped & instantiated, so we don't need the (albeit known) Factory.
			 * Just return the Optional containing the (NOT null) Service Singleton instance...
			 */
			@SuppressWarnings("unchecked")
			final Optional<T>         uncheckedResult = (Optional<T>) SERVICE_MAP.get(serviceClass);

			if (SERVICE_NULL_VALUE != uncheckedResult) {
				return                uncheckedResult;
			} else {
				return                Optional.empty();
			}
		}
		/*
		 * Note.: for any particular Service, the following logic will be executed EXACTLY once
		 * except for the case where serviceFactory.getInstance() returns Optional.empty().
		 * (not all Services are available on all Platforms for example)
		 * 
		 * TODO This behaviour is EXACTLY THE SAME as the previous implementation of this Method! (remove this TODO when merging to Repo)
		 * 
		 * To avoid this, SERVICE_NULL_VALUE is used as a Placeholder
		 * & ensures that next time this Service is requested, a result will be found very quickly.
		 * 
		 * TODO the disadvantage of this is that if a Service requires explicit registration
		 * in advance (via registerServiceFactory) & should the Service be requested
		 * BEFORE its ServiceFactory has been registered, the Service will never be found.
		 * If that is not desired, the SERVICE_NULL_VALUE logic will have to be removed & a Performance-hit taken)
		 */

		/*
		 * Service not yet Mapped: first try to find ServiceFactory...
		 */
		final ServiceFactory<?> serviceFactory;

		if (FACTORY_MAP.containsKey(serviceClass)) {
			/*
			 * This may be a SPECIFIC ServiceFactory as registered
			 * by a previous EXTERNAL-TO-THIS-CLASS invocation of registerServiceFactory(serviceFactory)
			 * or it may be the DefaultServiceFactory as registered in the "else" below...
			 */
			serviceFactory = FACTORY_MAP.get(serviceClass); // serviceFactory in Map can NEVER be null
		} else {
			serviceFactory = new DefaultServiceFactory<>(serviceClass);

			registerServiceFactory(serviceFactory);
		}
		/*
		 * Use ServiceFactory (NEVER null here) to get Service Singleton...
		 */
		@SuppressWarnings("unchecked")
		final Optional<T> optionalServiceInstance = (Optional<T>) serviceFactory.getInstance();

		if (optionalServiceInstance.isPresent()) {
			SERVICE_MAP.put(serviceClass, optionalServiceInstance);
		} else {
			SERVICE_MAP.put(serviceClass, SERVICE_NULL_VALUE);

			LOGGER.warning("NULL Instance created by " + serviceFactory.getClass().getSimpleName()
            		+ " on current Platform (" + Platform.getCurrent() + ")");
		}

        LOGGER.fine("Return service: " + optionalServiceInstance);
		return                           optionalServiceInstance;  // (may be Optional.empty)
    }
}