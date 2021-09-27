/*
 * Copyright (c) 2016, 2020, Gluon
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
package com.gluonhq.attach.filechooser.impl;

import com.gluonhq.attach.filechooser.FileChooserService;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * <p>Create the file {@code /src/android/res/xml/file_provider_paths.xml} with
 * the following content that allows access to the external storage or to
 * a temporal cache in case the file is not saved:</p>
 *
 * <pre>
 * {@code
 *    <?xml version="1.0" encoding="utf-8"?>
 *    <paths>
 *        <external-path name="external_files" path="." />
 *        <external-cache-path name="external_cache_files" path="." />
 *    </paths>
 * }
 * </pre>
 *
 * <p>The permission <code>android.permission.CAMERA</code> needs to be added as well as the permissions
 * <code>android.permission.READ_EXTERNAL_STORAGE</code> and <code>android.permission.WRITE_EXTERNAL_STORAGE</code>
 * to be able to read and write images. Also a {@code provider} is required:</p>
 * <pre>
 * {@code <manifest package="${application.package.name}" ...>
 *    <uses-permission android:name="android.permission.CAMERA"/>
 *    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
 *    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
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
public class AndroidFileChooserService implements FileChooserService {

    private static final Logger LOG = Logger.getLogger(AndroidFileChooserService.class.getName());

    static {
        System.loadLibrary("filechooser");
    }

    private static final ObjectProperty<File> selectedFile = new SimpleObjectProperty<>();
    private static ObjectProperty<File> result;


    public AndroidFileChooserService() {
    }

    @Override
    public Optional<File> loadFile() {
        LOG.severe("Load image from gallery has been called.");
        result = new SimpleObjectProperty<>();
        selectFile();
        try {
            Platform.enterNestedEventLoop(result);
        } catch (Exception e) {
            LOG.severe("GalleryActivity: enterNestedEventLoop failed: " + e);
        }

        return Optional.ofNullable(result.get());

    }

    // native
    public static native void selectFile();

    // callback
    public static void setResult(String filePath, int rotate) {
        File file = new File(filePath);
        selectedFile.set(file);
        if (selectedFile.get() == null) {
            result.set(selectedFile.get());
        }
        final File finalImage = selectedFile.get();
        LOG.severe("Final File:");
        LOG.severe(finalImage.getAbsolutePath());
        Platform.runLater(() -> {
            if (finalImage != null) {
                result.set(selectedFile.get());
            }
            try {
                Platform.exitNestedEventLoop(result, null);
            } catch (Exception e) {
                LOG.severe("GalleryActivity: exitNestedEventLoop failed: " + e);
            }
        });
    }

}
