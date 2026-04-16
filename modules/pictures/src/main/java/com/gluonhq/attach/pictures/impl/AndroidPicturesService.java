/*
 * Copyright (c) 2016, 2026, Gluon
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
package com.gluonhq.attach.pictures.impl;

import com.gluonhq.attach.pictures.PicturesService;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;

import java.io.File;
import java.io.FileInputStream;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * <p>Create the file {@code /src/android/res/xml/file_provider_paths.xml} with
 * the following content that allows access to the external storage or to
 * a temporal cache in case the picture is not saved:</p>
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
public class AndroidPicturesService implements PicturesService {

    private static final Logger LOG = Logger.getLogger(AndroidPicturesService.class.getName());

    static {
        System.loadLibrary("pictures");
    }

    private static final ObjectProperty<File> imageFile = new SimpleObjectProperty<>();
    private static final ReadOnlyObjectWrapper<Image> imageProperty = new ReadOnlyObjectWrapper<>();
    private static final int MAX_IMAGE_DIMENSION = 1280;
    private static ObjectProperty<Image> result;
    private static boolean enteredLoop;

    @Override
    public Optional<Image> takePhoto(boolean savePhoto) {
        result = new SimpleObjectProperty<>();
        takePicture(savePhoto);
        try {
            enteredLoop = true;
            Platform.enterNestedEventLoop(result);
        } catch (Exception e) {
            LOG.severe("GalleryActivity: enterNestedEventLoop failed: " + e);
        }
        return Optional.ofNullable(result.get());
    }

    @Override
    public void asyncTakePhoto(boolean savePhoto) {
        imageProperty.setValue(null);
        takePicture(savePhoto);
    }

    @Override
    public Optional<Image> loadImageFromGallery() {
        result = new SimpleObjectProperty<>();
        selectPicture();
        try {
            enteredLoop = true;
            Platform.enterNestedEventLoop(result);
        } catch (Exception e) {
            LOG.severe("GalleryActivity: enterNestedEventLoop failed: " + e);
        }
        return Optional.ofNullable(result.get());
    }

    @Override
    public void asyncLoadImageFromGallery() {
        imageProperty.setValue(null);
        selectPicture();
    }

    @Override
    public Optional<File> getImageFile() {
        return Optional.ofNullable(imageFile.get());
    }

    @Override
    public ReadOnlyObjectProperty<Image> imageProperty() {
        return imageProperty.getReadOnlyProperty();
    }

    // native
    public static native void takePicture(boolean savePhoto);
    public static native void selectPicture();

    // callback
    public static void setResult(String filePath) {
        LOG.fine("Got photo file at: " + filePath);
        File photoFile = new File(filePath);
        imageFile.set(photoFile);

        // Release the old image reference and try to free resources
        imageProperty.setValue(null);
        System.gc();

        Image image = null;
        try (FileInputStream fis = new FileInputStream(photoFile)) {
            image = new Image(fis, MAX_IMAGE_DIMENSION, MAX_IMAGE_DIMENSION, true, true);
        } catch (Exception e) {
            LOG.severe("GalleryActivity: error loading image: " + e);
        }

        final Image finalImage = image;
        if (enteredLoop) {
            result.set(finalImage);
        }
        Platform.runLater(() -> {
            imageProperty.setValue(finalImage);
            if (enteredLoop) {
                enteredLoop = false;
                try {
                    Platform.exitNestedEventLoop(result, null);
                } catch (Exception e) {
                    LOG.severe("GalleryActivity: exitNestedEventLoop failed: " + e);
                }
            }
        });
    }
}
