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
package com.gluonhq.attach.storage;

import com.gluonhq.attach.util.Services;

import java.io.File;
import java.util.Optional;

/**
 * The storage service provides access to the private and public storage locations for
 * the application offered by the native platform.
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code File privateStorage = StorageService.create()
 *      .flatMap(StorageService::getPrivateStorage)
 *      .orElseThrow(() -> new FileNotFoundException("Could not access private storage."));
 *  });}</pre>
 *
 * <p><b>Android Configuration</b></p>
 * <p>The permissions <code>android.permission.READ_EXTERNAL_STORAGE</code> and
 * <code>android.permission.WRITE_EXTERNAL_STORAGE</code> are required if you want to access the external
 * storage on the device for read and/or write operations respectively. Defining write permissions implicitly
 * activate read permissions as well.</p>
 *
 * Note: these modifications are handled automatically by <a href="https://docs.gluonhq.com/client">Client plugin</a> if it is used.
 * <pre>
 * {@code <manifest ...>
 *    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
 *    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
 *    ...
 *    <activity android:name="com.gluonhq.impl.attach.android.PermissionRequestActivity" />
 *  </manifest>}</pre>
 *
 * <p><b>iOS Configuration</b>: none</p>
 *
 * @since 3.0.0
 */
public interface StorageService {

    /**
     * Returns an instance of {@link StorageService}.
     * @return An instance of {@link StorageService}.
     */
    static Optional<StorageService> create() {
        return Services.get(StorageService.class);
    }

    /**
     * Get a storage directory that is private to the environment that is
     * calling this method. In the case of iOS or Android, the returned
     * directory is private to the enclosing application.
     *
     * @return an optional with a private storage directory for an application
     */
    Optional<File> getPrivateStorage();

    /**
     * Get a public storage directory location.
     *
     * <p>Note that on Android the public location could be mapped to a removable memory device and may
     * not always be available. Users of this method are advised to call {@link #isExternalStorageWritable()}
     * or {@link #isExternalStorageReadable()} to avoid surprises.</p>
     *
     * <p>Note also that on Android, permissions will need to be set to access external storage. See:
     * <a href="https://developer.android.com/training/basics/data-storage/files.html">https://developer.android.com/training/basics/data-storage/files.html</a>.</p>
     *
     * @param subdirectory under the root of public storage that is required. On Android the supplied subdirectory should not be null.
     * @return an Optional of a File representing the requested directory location.  The location may not yet exist. It is the
     * responsibility of the programmer to ensure that the location exists before using it.
     *
     */
    Optional<File> getPublicStorage(String subdirectory);

    /**
     * Checks if external storage is available for read and write access.
     *
     * @return true if the externalStorage is writable (implies readable), false otherwise
     */
    boolean isExternalStorageWritable() ;

    /**
     * Checks if external storage is available for read access.
     *
     * @return true if the externalStorage is at least readable, false otherwise
     */
    boolean isExternalStorageReadable() ;
}
