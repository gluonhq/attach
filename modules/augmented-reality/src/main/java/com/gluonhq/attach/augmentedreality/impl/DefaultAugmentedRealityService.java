/*
 * Copyright (c) 2018, 2020, Gluon
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
package com.gluonhq.attach.augmentedreality.impl;

import com.gluonhq.attach.augmentedreality.AugmentedRealityService;
import com.gluonhq.attach.storage.StorageService;
import com.gluonhq.attach.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class DefaultAugmentedRealityService implements AugmentedRealityService {

    private static final Logger LOG = Logger.getLogger(DefaultAugmentedRealityService.class.getName());
    protected static boolean debug = Util.DEBUG;
    private final File assetsFolder;
    private final ExecutorService exec = Executors.newFixedThreadPool(3);

    public DefaultAugmentedRealityService() {
        assetsFolder = new File(StorageService.create()
                .flatMap(StorageService::getPrivateStorage)
                .orElseThrow(() -> new RuntimeException("Error accessing Private Storage folder")), "assets");

        if (!assetsFolder.exists()) {
            assetsFolder.mkdir();
        }
    }

    File getFileFromAssets(String filePath) {
        File file = new File(assetsFolder, filePath);
        if (!file.exists() && checkFileInResources(filePath)) {
            copyFile(filePath);
        }
        return file;
    }

    private boolean checkFileInResources(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        String pathIni = filePath;
        if (!filePath.startsWith("/")) {
            pathIni = "/" + pathIni;
        }
        return (DefaultAugmentedRealityService.class.getResource(pathIni) != null);
    }

    private void copyFile(String filePath) {
        FutureTask<Boolean> futureTask = new FutureTask<>(new CopyFile(filePath)) {
            @Override
            protected void done() {
                if (debug) {
                    LOG.log(Level.INFO, "Finished copying model file " + filePath);
                }
            }
        };
        LOG.log(Level.INFO, "Copying file " + filePath);
        Future<?> submit = exec.submit(futureTask);
        try {
            submit.get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.log(Level.WARNING, "Error while waiting for thread completion", e);
        }
    }

    private class CopyFile implements Callable<Boolean> {

        private final String filePath;

        public CopyFile(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public Boolean call() throws Exception {
            if (filePath == null || filePath.isEmpty()) {
                return Boolean.FALSE;
            }

            File modelFile = new File(assetsFolder, filePath);
            if (!modelFile.exists()) {
                modelFile.getParentFile().mkdirs();
                if (debug) {
                    LOG.log(Level.INFO, String.format("Copying model file: %s, from resources to %s", filePath, modelFile.getAbsolutePath()));
                }
                String pathIni = filePath;
                if (!filePath.startsWith("/")) {
                    pathIni = "/" + pathIni;
                }

                if (!copyFile(pathIni, modelFile.getAbsolutePath())) {
                    return Boolean.FALSE;
                }
            }
            return Boolean.TRUE;
        }

        private boolean copyFile(String pathIni, String pathEnd)  {
            try (InputStream myInput = DefaultAugmentedRealityService.class.getResourceAsStream(pathIni)) {
                if (myInput == null) {
                    return false;
                }
                try (OutputStream myOutput = new FileOutputStream(pathEnd)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = myInput.read(buffer)) > 0) {
                        myOutput.write(buffer, 0, length);
                    }
                    myOutput.flush();
                    return true;
                } catch (IOException ex) {
                    LOG.log(Level.WARNING, "Error copying file", ex);
                }
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Error copying file", ex);
            }
            return false;
        }

    }
}