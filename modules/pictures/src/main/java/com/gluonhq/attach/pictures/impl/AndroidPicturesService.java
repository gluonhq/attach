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
package com.gluonhq.attach.pictures.impl;

import com.gluonhq.attach.pictures.PicturesService;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
    private static ObjectProperty<Image> result;

    public AndroidPicturesService() {
    }

    @Override
    public Optional<Image> takePhoto(boolean savePhoto) {
        result = new SimpleObjectProperty<>();
        takePicture(savePhoto);
        try {
            Platform.enterNestedEventLoop(result);
        } catch (Exception e) {
            LOG.severe("GalleryActivity: enterNestedEventLoop failed: " + e);
        }
        return Optional.ofNullable(result.get());
    }

    @Override
    public Optional<Image> loadImageFromGallery() {
        result = new SimpleObjectProperty<>();
        selectPicture();
        try {
            Platform.enterNestedEventLoop(result);
        } catch (Exception e) {
            LOG.severe("GalleryActivity: enterNestedEventLoop failed: " + e);
        }
        return Optional.ofNullable(result.get());
    }

    @Override
    public Optional<File> getImageFile() {
        return Optional.ofNullable(imageFile.get());
    }

    // native
    public static native void takePicture(boolean savePhoto);
    public static native void selectPicture();

    // callback
    public static void setResult(String filePath, int rotate) {
        LOG.fine("Got photo file at: " + filePath);
        File photoFile = new File(filePath);
        imageFile.set(photoFile);
        Image initialImage = null;
        try {
            initialImage = new Image(new FileInputStream(photoFile));
        } catch (FileNotFoundException e) {
            LOG.severe("GalleryActivity: file not found: " + e);
        }
        if (initialImage == null || rotate == 0) {
            result.set(initialImage);
        }
        final Image finalImage = initialImage;
        Platform.runLater(() -> {
            if (finalImage != null && rotate != 0) {
                result.set(rotateImage(finalImage, rotate));
            }
            try {
                Platform.exitNestedEventLoop(result, null);
            } catch (Exception e) {
                LOG.severe("GalleryActivity: exitNestedEventLoop failed: " + e);
            }
        });
    }

    private static Image rotateImage(Image image, int rotate) {
        if (image == null || rotate == 0) {
            return image;
        }
        ImageView iv = new ImageView(image);
        iv.setFitWidth(1280);
        iv.setFitHeight(1280);
        iv.setPreserveRatio(true);
        iv.setRotate(rotate);
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        return iv.snapshot(params, null);
    }
}
