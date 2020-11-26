/*
 * Copyright (c) 2017, 2019, Gluon
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
package com.gluonhq.attach.audiorecording;

import java.io.File;
import java.util.Optional;

import com.gluonhq.attach.storage.StorageService;
import com.gluonhq.attach.util.Services;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyListProperty;

/**
 * AudioRecording Service.
 * 
 * <p>Service to record audio. The recording is saved in wav format in chunk files
 * of a given duration. The files are stored in the public storage, under a folder
 * that the developer will set. 
 * The pattern for these files name will be 
 * {@code audioFile-###-yyyy-MM-dd.HH-mm-ss.SSS,wav}, where ### is the number of
 * chunk, starting from 000.</p>
 * 
 * <p>Note that given the limited space available on the device, it is convenient
 * to remove the content either before starting a new recording or when the files 
 * have been extracted.</p>
 * 
 * <p><b>Dependencies</b></p>
 *
 * <ul><li>{@link StorageService Storage Service}</li></ul>
 * 
 * <p><b>Example</b></p>
 * 
 * <p>The following code snippet shows how to start recording 10 minutes of audio in 
 * 10 files of 1 minute duration each.</p>
 * 
 * <pre>
 * {@code AudioRecordingService.create().ifPresent(audio -> {
 *      audio.clearAudioFolder();
 *      audio.getAudioChunkFiles().addListener((ListChangeListener.Change<? extends String> c) -> {
 *          while (c.next()) {
 *              if (c.wasAdded()) {
 *                  for (String fileName : c.getAddedSubList()) {
 *                      File audioFile = new File(audio.getAudioFolder(), fileName);
 *                      System.out.println("New audio chunk: " + audioFile.getAbsolutePath());
 *                  }
 *              }
 *              if (audio.getAudioChunkFiles().size() == 10) {
 *                  audio.stopRecording();
 *              }
 *          }
 *      });
 * 　　  audio.startRecording(44100f, 16, 2, 60);
 *  });}</pre>
 *
 * <p><b>Android Configuration</b></p>
 * <p>The following <code>permission</code> and activity need to be added to the 
 * android manifest configuration file:</p>
 *
 * Note: these modifications are handled automatically by <a href="https://docs.gluonhq.com/client">Client plugin</a> if it is used.
 * <pre>
 * {@code <manifest ...>
 *    ...
 *    <uses-permission android:name="android.permission.RECORD_AUDIO" />
 *    <application ...>
 *       ...
 *       <activity android:name="com.gluonhq.impl.attach.plugins.android.PermissionRequestActivity" />
 *    </application>
 * </manifest>}</pre>
 *
 * <p><b>iOS Configuration</b></p>
 * 
 * <p>The following keys are required:</p>
 * <pre>
 * {@code 
 * <key>NSMicrophoneUsageDescription</key>
 * <string>Need microphone access for recording audio</string>
 * <key>UIBackgroundModes</key>
 * <array>
 *      <string>audio</string>
 * </array>
 * }</pre>
 * 
 * Note: to get access to the public storage from iTunes these keys can be added
 * as well:
 * <pre>
 * {@code 
 * <key>CFBundleDisplayName</key>
 * <string>$ApplicationName</string>
 * <key>UIFileSharingEnabled</key>
 * <string>YES</string>
 * }</pre>
 *
 * @since 3.5.0
 */
public interface AudioRecordingService {
    
    String DEFAULT_EXTERNAL_FOLDER = "AudioRecording";

    /**
     * Returns an instance of {@link AudioRecordingService}.
     * @return An instance of {@link AudioRecordingService}.
     */
    static Optional<AudioRecordingService> create() {
        return Services.get(AudioRecordingService.class);
    }

    /**
     * Sets the name of the external folder where the audio files will be saved.
     *
     * If not set, by default it will use {@link #DEFAULT_EXTERNAL_FOLDER}. 
     *
     * @param folderName the name of the external folder where the audio files will 
     * be saved
     */
    void setAudioFolderName(String folderName);

    /**
     * Returns the folder where all the audio files will be stored
     *
     * @return a File with the audio folder
     */
    File getAudioFolder();

    /**
     * Removes the content of the audio folder.
     *
     * <p>This method can be called at any time. It is convenient to call it before
     * the audio recording starts, or when it ends and the audio files have been 
     * safely extracted.</p>
     */
    void clearAudioFolder();

    /**
     * Start audio recording with the given parameters. The recorded audio files are saved
     * in wav format in chunks of the specified recording time.
     *
     * @param sampleRate        the number of samples per second (8000.0f, 44100.0f, ...)
     * @param sampleSizeInBits  the number of bits in each sample (16 or 8)
     * @param channels          the number of channels (1 for mono, 2 for stereo)
     * @param chunkRecordTime   the duration in seconds of each chunk (60, 360, ...)
     */
    void startRecording(float sampleRate, int sampleSizeInBits, int channels, int chunkRecordTime);

    /**
     * Stop audio recording.
     */
    void stopRecording();

    /**
     * Returns <code>true</code> when the audio recording is currently active and <code>false</code>
     * if audio recording is stopped.
     *
     * @return a read only boolean property with the recording status
     */
    ReadOnlyBooleanProperty recordingProperty();

    /**
     * Returns a read only observable list of file names. It contains a list of file chunks that
     * are saved in the {@link #getAudioFolder() audio folder}. It can be used during recording
     * to track when new audio chunks are made available in the audio folder.
     *
     * @return A {@code ReadOnlyListProperty} of file names
     */
    ReadOnlyListProperty<String> getAudioChunkFiles();
}
