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
package com.gluonhq.attach.share.impl;


import com.gluonhq.attach.share.ShareService;
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

public class AndroidShareService implements ShareService {

    private static final Logger LOGGER = Logger.getLogger(AndroidShareService.class.getName());

    static {
        System.loadLibrary("share");
    }

    public AndroidShareService() {
    }
    
    @Override
    public void share(String contentText) {
        share(null, contentText);
    }

    @Override
    public void share(String subject, String contentText) {
        if (subject == null) {
            subject = "";
        }
        if (contentText == null || contentText.isEmpty()) {
            LOGGER.log(Level.SEVERE, "Error: contentText not valid");
            return;
        }
        shareText(subject, contentText);
    }

    @Override
    public void share(String type, File file) {
        share(null, null, type, file);
    }

    @Override
    public void share(String subject, String contentText, String type, File file) {
        if (subject == null) {
            subject = "";
        }
        if (contentText == null) {
            contentText = "";
        }
        if (file != null && file.exists()) {
            if (Util.DEBUG) {
                LOGGER.log(Level.INFO, "File to share: " + file);
            }
        } else {
            LOGGER.log(Level.SEVERE, "Error: URL not valid for file: " + file);
            return;
        }
        if (type == null || type.isEmpty()) {
            LOGGER.log(Level.SEVERE, "Error: type not valid");
            return;
        }
        shareFile(subject, contentText, type, file.getAbsolutePath());
    }

    // native
    private static native void shareText(String subject, String message);
    private static native void shareFile(String subject, String message, String type, String filePath);

}