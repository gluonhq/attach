/*
 * Copyright (c) 2021 Gluon
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
package com.gluonhq.attach.version;

import com.gluonhq.attach.util.Services;
import java.util.Optional;

/**
 * The version service provides access to the public and internal version
 * numbers of the current application.
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code VersionService.create()
 *      .ifPresent(version -> {
 *          System.out.println("Public version number: " + version.getVersionNumber());
 *          System.out.println("Internal build number: " + version.getBuildNumber());
 *      });
 * }</pre>
 *
 * <p><b>Android Configuration</b>: none</p>
 *
 * <p><b>iOS Configuration</b>: none</p>
 *
 * @since 4.0.13
 */
public interface VersionService {

    /**
     * Returns an instance of {@link VersionService}.
     * @return An instance of {@link VersionService}.
     */
    static Optional<VersionService> create() {
        return Services.get(VersionService.class);
    }

    /**
     * Retrieves the current version number of the application, which is
     * typically visible to the users.
     *
     * @return A string with information of the public version of the application,
     *         or -1 if unknown
     */
    String getVersionNumber();

    /**
     * Retrieves the current internal build number of the application, which is
     * typically not shown to the users
     *
     * @return A string with information of the internal version of the application,
     *         or -1 if unknown
     */
    String getBuildNumber();

}
