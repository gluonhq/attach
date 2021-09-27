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
package com.gluonhq.attach.filechooser;

import java.io.File;
import java.util.Optional;

import com.gluonhq.attach.util.Services;

/**
 * The picture service allows the developer to load a picture from the device's local file
 * system or from a picture taken directly using the device's camera.
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code ImageView imageView = new ImageView();
 *  PicturesService.create().ifPresent(service -> {
 *      service.takePhoto(false).ifPresent(image -> imageView.setImage(image));
 *  });}</pre>
 *
 * <p>It also allows the developer to retrieve the original file and work with it
 * as needed, for instance sharing it with the ShareService.</p>
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code ImageView imageView = new ImageView();
 *  PicturesService.create().ifPresent(service -> {
 *      service.loadImageFromGallery().ifPresent(image -> imageView.setImage(image));
 *      service.getImageFile().ifPresent(file ->
 *          ShareService.create().ifPresent(share ->
 *              share.share("image/jpeg", file)));
 *  });}</pre>
 *
 * <p><b>Android Configuration</b></p>
 *
 * <p>Create the file {@code /src/android/res/xml/file_provider_paths.xml} with
 * the following content that allows access to the external storage:</p>
 * <pre>
 * {@code
 *    <?xml version="1.0" encoding="utf-8"?>
 *    <paths>
 *        <external-path name="external_files" path="." />
 *    </paths>
 * }
 * </pre>
 *
 * <p>The permission <code>android.permission.CAMERA</code> needs to be added as well as the permissions
 * <code>android.permission.READ_EXTERNAL_STORAGE</code> and <code>android.permission.WRITE_EXTERNAL_STORAGE</code>
 * to be able to read and write images. Also a {@code provider} is required:</p>
 *
 * Note: these modifications are handled automatically by <a href="https://docs.gluonhq.com/client">Client plugin</a> if it is used.
 * <pre>
 * {@code <manifest package="${application.package.name}" ...>
 *    <uses-permission android:name="android.permission.CAMERA"/>
 *    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
 *    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
 *    <application ...>
 *       ...
 *       <activity android:name="com.gluonhq.attach.android.PermissionRequestActivity" />
 *       <provider
 *           android:name="android.support.v4.content.FileProvider"
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
 *
 *
 * <p><b>iOS Configuration</b></p>
 * <p>The following keys are required:</p>
 * <pre>
 * {@code <key>NSCameraUsageDescription</key>
 *  <string>Reason to use Camera Service (iOS 10+)</string>
 *  <key>NSPhotoLibraryUsageDescription</key>
 *  <string>Reason to use Photo Library (iOS 10+)</string>
 *  <key>NSPhotoLibraryAddUsageDescription</key>
 *  <string>Reason to use Photo Library (iOS 10+)</string>}</pre>
 *
 * @since 3.0.0
 */
public interface FileChooserService {

    /**
     * Returns an instance of {@link FileChooserService}.
     * @return An instance of {@link FileChooserService}.
     */
    static Optional<FileChooserService> create() {
        return Services.get(FileChooserService.class);
    }

    /**
     * Retrieve an image from the device's gallery of images
     * @return an Optional with the Image or empty if it failed or it was cancelled
     */
    Optional<File> loadFile();

}
