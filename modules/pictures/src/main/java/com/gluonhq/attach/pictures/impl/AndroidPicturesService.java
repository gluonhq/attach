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
 * Android implementation of the PicturesService.
 *
 * <p>Uses a custom camera implementation via CameraController for taking photos
 * and the system gallery for selecting existing images.</p>
 *
 * <p>Required permissions are automatically declared in AndroidManifest.xml:</p>
 * <ul>
 *     <li><code>android.permission.CAMERA</code></li>
 *     <li><code>android.permission.READ_MEDIA_IMAGES</code> (Android 13+)</li>
 *     <li><code>android.permission.READ_MEDIA_AUDIO</code> (Android 13+)</li>
 *     <li><code>android.permission.READ_MEDIA_VIDEO</code> (Android 13+)</li>
 *     <li><code>android.permission.READ_EXTERNAL_STORAGE</code> (Android 12 and below)</li>
 * </ul>
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
    /**
     * Called from native code with two file paths:
     * @param originalFilePath  the full-resolution original file (for {@link #getImageFile()})
     * @param processedFilePath the preprocessed (scaled+rotated) file (for {@link Image} loading)
     */
    public static void setResult(String originalFilePath, String processedFilePath) {
        if (originalFilePath == null || originalFilePath.isEmpty()
                || processedFilePath == null || processedFilePath.isEmpty()) {
            LOG.fine("Picture request cancelled");
            imageFile.set(null);
            imageProperty.setValue(null);
            if (enteredLoop) {
                result.set(null);
                Platform.runLater(() -> {
                    enteredLoop = false;
                    try {
                        Platform.exitNestedEventLoop(result, null);
                    } catch (Exception e) {
                        LOG.severe("GalleryActivity: exitNestedEventLoop failed: " + e);
                    }
                });
            }
            return;
        }

        LOG.fine("Got photo file at: " + originalFilePath + " (processed: " + processedFilePath + ")");
        File originalFile = new File(originalFilePath);
        File processedFile = new File(processedFilePath);
        imageFile.set(originalFile);

        // Release the old image reference and try to free resources
        imageProperty.setValue(null);
        // ugly, but effective preventing vram pool from growing when taking many pictures
        System.gc();

        Image image = null;
        try (FileInputStream fis = new FileInputStream(processedFile)) {
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
