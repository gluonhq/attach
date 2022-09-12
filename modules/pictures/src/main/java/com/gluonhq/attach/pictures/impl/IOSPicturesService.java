/*
 * Copyright (c) 2016, 2022, Gluon
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Base64;
import java.util.Optional;

/**
 * Note:Since iOS 10 requires {@code NSCameraUsageDescription},  
 * {@code NSPhotoLibraryUsageDescription} and 
 * {@code NSPhotoLibraryAddUsageDescription} in pList.
 */
public class IOSPicturesService implements PicturesService {
    
    static {
        System.loadLibrary("Pictures");
        initPictures();
    }
    
    private static final ObjectProperty<File> imageFile = new SimpleObjectProperty<>();
    private static final ReadOnlyObjectWrapper<Image> imageProperty = new ReadOnlyObjectWrapper<>();
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
            System.out.println("GalleryActivity: enterNestedEventLoop failed: " + e);
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
            System.out.println("GalleryActivity: enterNestedEventLoop failed: " + e);
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
    private static native void initPictures(); // init IDs for java callbacks from native
    public static native void takePicture(boolean savePhoto);
    public static native void selectPicture();

    // callback
    public static void setResult(String v, String filePath) {
        if (v != null && !v.isEmpty()) {
            try {
                byte[] imageBytes = Base64.getDecoder().decode(v.replaceAll("\\s+", "").getBytes());
                imageFile.set(new File(filePath));
                Image image = new Image(new ByteArrayInputStream(imageBytes));
                if (enteredLoop) {
                    result.set(image);
                } else {
                    Platform.runLater(() -> imageProperty.setValue(image));
                }
            } catch (Exception ex) {
                System.err.println("Error setResult: " + ex);
            }
        }
        if (enteredLoop) {
            enteredLoop = false;
            Platform.runLater(() -> {
                try {
                    Platform.exitNestedEventLoop(result, null);
                } catch (Exception e) {
                    System.out.println("GalleryActivity: exitNestedEventLoop failed: " + e);
                }
            });
        }
    }
}
