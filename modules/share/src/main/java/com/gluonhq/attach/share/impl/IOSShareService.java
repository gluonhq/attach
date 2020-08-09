/*
 * Copyright (c) 2017, Gluon
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

import java.io.File;


public class IOSShareService implements ShareService {

    static {
        System.loadLibrary("Share");
        initShare();
    }

    public IOSShareService() {
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
            System.out.println("Error: contentText not valid");
            return;
        }
        nativeShare(subject, contentText, "");
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
            System.out.println("File to share: " + file);
        } else {
            System.out.println("Error: URL not valid");
            return;
        }
        nativeShare(subject, contentText, file.getAbsolutePath());
    }

    // native
    private static native void initShare(); // init IDs for java callbacks from native
    private static native void nativeShare(String subject, String message, String filePath);

}