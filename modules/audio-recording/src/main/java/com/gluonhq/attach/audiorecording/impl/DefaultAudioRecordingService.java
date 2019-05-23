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
package com.gluonhq.attach.audiorecording.impl;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.gluonhq.attach.util.Services;
import com.gluonhq.attach.audiorecording.AudioRecordingService;
import com.gluonhq.attach.storage.StorageService;
import java.io.File;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.FXCollections;

public abstract class DefaultAudioRecordingService implements AudioRecordingService {

    private static final Logger LOG = Logger.getLogger(DefaultAudioRecordingService.class.getName());

    private String audioFolderName;
    private File audioFolder;

    private static ReadOnlyBooleanWrapper recording;
    private final ReadOnlyListWrapper<String> chunkList;

    public DefaultAudioRecordingService() {
        recording = new ReadOnlyBooleanWrapper();
        chunkList = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());
    }

    @Override
    public void startRecording(float sampleRate, int sampleSizeInBits, int channels, int chunkRecordTime) {
        stopRecording();
        initialize();
        recording.set(true);
        start(sampleRate, sampleSizeInBits, channels, chunkRecordTime, chunkList::add);
    }

    @Override
    public void stopRecording() {
        stop();
    }

    @Override
    public ReadOnlyBooleanProperty recordingProperty() {
        return recording.getReadOnlyProperty();
    }

    @Override
    public void setAudioFolderName(String folderName) {
        audioFolderName = folderName;
        initialize();
    }

    @Override
    public File getAudioFolder() {
        if (audioFolder == null) {
            initialize();
        }
        return audioFolder;
    }

    @Override
    public void clearAudioFolder() {
        try {
            for (File f : getAudioFolder().listFiles()) {
                if (f.getName().startsWith("audioFile")) {
                    f.delete();
                }
            }
        } catch (SecurityException se) {
            LOG.log(Level.SEVERE, "Error clearing external folder", se);
        }

        chunkList.clear();
    }

    @Override
    public ReadOnlyListProperty<String> getAudioChunkFiles() {
        return chunkList.getReadOnlyProperty();
    }

    private void initialize() {
        if (audioFolderName == null || audioFolderName.isEmpty()) {
            audioFolderName = AudioRecordingService.DEFAULT_EXTERNAL_FOLDER;
        }

        audioFolder = Services.get(StorageService.class)
                .flatMap(service -> service.getPublicStorage(audioFolderName))
                .orElseThrow(() -> new RuntimeException("Error accessing Public Storage folder"));

        if (! audioFolder.exists()) {
            try {
                audioFolder.mkdir();
            } catch (SecurityException se) {
                LOG.log(Level.SEVERE, "Error creating external folder", se);
            }
        }

        for (File f : audioFolder.listFiles()) {
            if (f.getName().startsWith("audioFile")) {
                chunkList.add(f.getName());
            }
        }
    }

    protected abstract void start(float sampleRate, int sampleSizeInBits, int channels, int chunkRecordTime, Function<String, Boolean> addChunk);
    protected abstract void stop();

    protected static void updateRecordingStatus(boolean recording) {
        DefaultAudioRecordingService.recording.set(recording);
    }
}
