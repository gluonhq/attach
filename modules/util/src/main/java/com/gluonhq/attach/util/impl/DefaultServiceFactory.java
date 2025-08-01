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

import com.gluonhq.attach.util.Platform;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultServiceFactory<T> implements ServiceFactory<T> {

    private static final Logger LOGGER = Logger.getLogger(DefaultServiceFactory.class.getName());

    private final Class<T> serviceType;
    private T instance;

    public DefaultServiceFactory(Class<T> serviceType) {
        this.serviceType = serviceType;
    }

    @Override
    public Class<T> getServiceType() {
        return serviceType;
    }

    @Override
    public Optional<T> getInstance() {
        if (instance == null) {
            instance = createInstance(Platform.getCurrent());
        }
        return Optional.ofNullable(instance);
    }

    private T createInstance(final Platform platform) {
    	final String fqn = serviceType.getPackageName() + ".impl." + className(platform);
        try {
        	@SuppressWarnings("unchecked")
			final Class<T> clazz = (Class<T>) Class.forName(fqn); // clazz return is NEVER null

        	LOGGER.fine("Service class for: " + clazz.getName());
            return clazz.getDeclaredConstructor().newInstance();

        } catch (final InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        } catch (final ClassNotFoundException ex) {
            // no-op
            LOGGER.log(Level.WARNING, "No new instance for " + serviceType + " and class " + fqn);
        }
        return null;
    }

    private String className(Platform platform) {
        return platform.getName() + serviceType.getSimpleName();
    }
}