/*
 * Copyright (c) 2017, 2020, Gluon
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
package com.gluonhq.attach.open.impl;


import com.gluonhq.attach.open.OpenService;
import com.gluonhq.attach.util.Util;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * <p>Create the file {@code /src/android/res/xml/file_provider_paths.xml} with
 * the following content that allows access to the external storage:</p>
 *
 * <pre>
 * {@code
 *    <?xml version="1.0" encoding="utf-8"?>
 *    <paths>
 *        <external-path name="external_files" path="." />
 *    </paths>
 * }
 * </pre>
 *
 * <p>Add a {@code provider} to the AndroidManifest:</p>
 * <pre>
 * {@code <manifest package="${application.package.name}" ...>
 *    <application ...>
 *       ...
 *       <activity android:name="com.gluonhq.helloandroid.PermissionRequestActivity" />
 *       <provider
 *           android:name="com.gluonhq.helloandroid.FileProvider"
 *           android:authorities="${application.package.name}.fileprovider"
 *           android:exported="false"
 *           android:grantUriPermissions="true">
 *           <meta-data
 *               android:name="android.support.FILE_PROVIDER_PATHS"
 *               android:resource="@xml/file_provider_paths" />
 *       </provider>
 *   </application>
 * </manifest>}
 * </pre>
 */

public class AndroidOpenService implements OpenService {

    private static final Logger LOGGER = Logger.getLogger(AndroidOpenService.class.getName());

    static {
        System.loadLibrary("open");
    }

    public AndroidOpenService() {
    }

    /**
     * Forces the specified MIME Type.
     * @param type On Android only, the MIME type of the file. It can be
     * '&lowast;/&lowast;', but not empty. On iOS it can be null. Usual types are:
     *  <ul><li>application/xml</li>
     *   <li>application/zip</li>
     *   <li>application/pdf</li>
     *   <li>text/css</li>
     *   <li>text/html</li>
     *   <li>text/csv</li>
     *   <li>text/plain</li>
     *   <li>image/png</li>
     *   <li>image/jpeg</li>
     *   <li>image/gif</li>
     *   <li>image/*</li></ul>
     * @param file A valid file to be opened.
     */
    @Override
    public void open(String type, File file) {
        if (file != null && file.exists()) {
            if (Util.DEBUG) {
                LOGGER.log(Level.INFO, "File to open: " + file);
            }
        } else {
            LOGGER.log(Level.SEVERE, "Error: URL not valid for file: " + file);
            return;
        }
        if (type == null || type.isEmpty()) {
            LOGGER.log(Level.SEVERE, "Error: type not valid");
            return;
        }
        openFileWithType(type,file.getAbsolutePath());
    }

    @Override
    public void open(File file) {
        if (file != null && file.exists()) {
            if (Util.DEBUG) {
                LOGGER.log(Level.INFO, "File to open: " + file);
            }
        } else {
            LOGGER.log(Level.SEVERE, "Error: URL not valid for file: " + file);
            return;
        }
        openFile(file.getAbsolutePath());
    }


    // native
    private static native void openFile(String filePath);
    private static native void openFileWithType(String type, String filePath);

}