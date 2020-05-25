/*
 * Copyright (c) 2017, 2019 Gluon
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
package com.gluonhq.attach.video.impl;

import com.gluonhq.attach.util.Services;
import com.gluonhq.attach.storage.StorageService;
import com.gluonhq.attach.util.Util;
import com.gluonhq.attach.video.VideoService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public abstract class DefaultVideoService implements VideoService {

    private static final Logger LOG = Logger.getLogger(DefaultVideoService.class.getName());
    protected final boolean debug = Util.DEBUG;

    private final ExecutorService exec = Executors.newFixedThreadPool(3);

    protected final ObservableList<String> playlist;
    protected final Map<String, Boolean> playlistMap;
    private final File assetsFolder;

    public DefaultVideoService() {
        playlist = FXCollections.observableArrayList();
        playlistMap = new HashMap<>();

        assetsFolder = new File(Services.get(StorageService.class)
                .flatMap(service -> service.getPrivateStorage())
                .orElseThrow(() -> new RuntimeException("Error accessing Private Storage folder")), "assets");

        if (! assetsFolder.exists()) {
            assetsFolder.mkdir();
        }

        playlist.addListener((ListChangeListener.Change<? extends String> c) -> {
            while (c.next()) {
                if (debug) {
                    LOG.log(Level.INFO, String.format("Playlist changed: %s", c.toString()));
                }
                if (c.wasAdded()) {
                    for (String s : c.getAddedSubList()) {
                        if (s == null || s.isEmpty()) {
                            continue;
                        }

                        if (! getFileFromAssets(s).exists()) {
                            FutureTask<Boolean> futureTask = new FutureTask<Boolean>(new CopyFile(s)) {
                                @Override
                                protected void done() {
                                    try {
                                        playlistMap.put(s, get());
                                        if (debug) {
                                            LOG.log(Level.INFO, String.format("Copying video file %s finished with result: %s", s, playlistMap.get(s) ? "ok" : "failed"));
                                        }
                                    } catch (InterruptedException | ExecutionException ex) {
                                        LOG.log(Level.SEVERE, "Error future task", ex);
                                    }
                                }
                            };
                            Future<?> submit = exec.submit(futureTask);
                            if (s.equals(playlist.get(currentIndexProperty().get()))) {
                                // only wait for copy completion if the file is the one to be played
                                try {
                                    submit.get();
                                } catch (InterruptedException | ExecutionException e) {
                                    LOG.log(Level.WARNING, "Error while waiting for thread completion", e);
                                }
                            }
                        } else {
                            if (debug) {
                                LOG.log(Level.INFO, String.format("file: %s already exists", s));
                            }
                            playlistMap.put(s, true);
                        }
                    }
                } else if (c.wasRemoved()) {
                    for (String s : c.getRemoved()) {
                        playlistMap.remove(s);
                    }
                }
            }
        });

    }

    @Override
    public ObservableList<String> getPlaylist() {
        return playlist;
    }

    protected boolean checkFileInResources(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        String pathIni = filePath;
        if (! filePath.startsWith("/")) {
            pathIni = "/" + pathIni;
        }
        return (DefaultVideoService.class.getResource(pathIni) != null);
    }

    protected File getFileFromAssets(String filePath) {
        return new File(assetsFolder, filePath.replaceAll("/", "_"));
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

            File videoFile = getFileFromAssets(filePath);
            if (! videoFile.exists()) {
                if (debug) {
                    LOG.log(Level.INFO, String.format("Copying video file: %s, from resources to %s", filePath, videoFile.getAbsolutePath()));
                }
                String pathIni = filePath;
                if (! filePath.startsWith("/")) {
                    pathIni = "/" + pathIni;
                }

                if (! copyFile(pathIni, videoFile.getAbsolutePath())) {
                    return Boolean.FALSE;
                }
            }
            return Boolean.TRUE;
        }

        private boolean copyFile(String pathIni, String pathEnd)  {
            try (InputStream myInput = DefaultVideoService.class.getResourceAsStream(pathIni)) {
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