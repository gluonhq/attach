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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.gluonhq.attach.util.Util;
import javafx.concurrent.Task;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class DesktopAudioRecordingService extends DefaultAudioRecordingService {

    private static final Logger LOG = Logger.getLogger(DesktopAudioRecordingService.class.getName());
    private final boolean debug = Util.DEBUG;

    // record duration for a chunk, in seconds
    private long CHUNK_RECORD_TIME = 60;  // 1 minute

    private RecorderTask recorderTask;
    private int counter;

    private ExecutorService recordingExecutor;
    private Function<String, Boolean> addChunk;

    public DesktopAudioRecordingService() {
    }

    @Override
    protected void start(float sampleRate, int sampleSizeInBits, int channels, int chunkRecordTime, Function<String, Boolean> addChunk) {
        CHUNK_RECORD_TIME = chunkRecordTime;
        recorderTask = new RecorderTask(sampleRate, sampleSizeInBits, channels);
        recordingExecutor = createExecutor("AudioRecording");
        recordingExecutor.execute(recorderTask);
        this.addChunk = addChunk;
    }

    @Override
    protected void stop() {
        if (recorderTask != null) {
            recorderTask.stop();
        }

        if (recordingExecutor != null) {
            recordingExecutor.shutdown();
            try {
                recordingExecutor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException ex) { }
        }
    }

    private class RecorderTask extends Task<Void> {

        private TargetDataLine line;
        private AudioFormat format;
        private TimedAudioCapture timedAudioCapture;
        private ScheduledExecutorService scheduler;
        private CountDownLatch latch;
        private final float sampleRate;
        private final int sampleSizeInBits;
        private final int channels;

        public RecorderTask(float sampleRate, int sampleSizeInBits, int channels) {
            this.sampleRate = sampleRate;
            this.sampleSizeInBits = sampleSizeInBits;
            this.channels = channels;
        }

        @Override
        protected Void call() {
            format = new AudioFormat(sampleRate, sampleSizeInBits, channels, true, true);

            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

            if (!AudioSystem.isLineSupported(info)) {
                LOG.log(Level.WARNING, String.format("Line matching %s not supported", info.toString()));
                return null;
            }

            try {
                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format, line.getBufferSize());
            } catch (LineUnavailableException | SecurityException ex) {
                LOG.log(Level.WARNING, "Error opening line", ex);
                return null;
            }

            LOG.log(Level.INFO, "Starting recording");
            line.start();

            latch = new CountDownLatch(1);

            scheduler = Executors.newScheduledThreadPool(1);
            scheduler.scheduleAtFixedRate(() -> {
                if (timedAudioCapture != null) {
                    timedAudioCapture.stop();
                }
                timedAudioCapture = new TimedAudioCapture(line, format, counter++);
                timedAudioCapture.start();
            }, 0, CHUNK_RECORD_TIME, TimeUnit.SECONDS);

            try {
                latch.await();
            } catch (InterruptedException ex) {
                LOG.log(Level.WARNING, "Error count down latch", ex);
            }
            return null;
        }

        @Override
        protected void cancelled() {
            super.cancelled();
            LOG.log(Level.WARNING, "Recording task was cancelled");
            updateRecordingStatus(false);
            stop();
        }

        @Override
        protected void failed() {
            super.cancelled();
            LOG.log(Level.WARNING, "Recording task failed");
            updateRecordingStatus(false);
            stop();
        }

        @Override
        protected void succeeded() {
            super.succeeded();
            LOG.log(Level.INFO, "Recording task was succeeded");
            closeLine();
        }

        private void closeLine() {
            if (line != null) {
                line.flush();
                line.close();
                line = null;
            }
            if (scheduler != null) {
                scheduler.shutdown();
                try {
                    scheduler.awaitTermination(5, TimeUnit.SECONDS);
                } catch (InterruptedException ex) { }
            }
            if (debug) {
                LOG.log(Level.INFO, "Finished recording");
            }
            updateRecordingStatus(false);
        }

        public void stop() {
            if (debug) {
                LOG.log(Level.INFO, "Stop recording");
            }
            if (timedAudioCapture != null) {
                timedAudioCapture.stop();
            }
            if (latch != null) {
                latch.countDown();
            }
        }
    }

    /**
     * Reads data from the input channel and writes to the output stream, with a
     * maximum time of CHUNK_RECORD_TIME
     */
    private class TimedAudioCapture {

        private DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd.HH-mm-ss.SSS");
        private static final int BUFFER_SIZE = 4096;
        private ByteArrayOutputStream recordBytes;
        private final AudioFormat format;
        private volatile boolean isRunning;

        private final Thread thread;

        public TimedAudioCapture(TargetDataLine line, AudioFormat format, int chunk) {
            this.format = format;
            thread = new Thread(() -> {
                try {
                    isRunning = line != null;
                    final String fileName = String.format("audioFile-%03d-%s", chunk, LocalDateTime.now().format(pattern));
                    if (debug) {
                        LOG.log(Level.INFO, String.format("Start recording chunk %d", chunk));
                    }

                    byte[] buffer = new byte[BUFFER_SIZE];
                    recordBytes = new ByteArrayOutputStream();
                    while (isRunning && line != null) {
                        int bytesRead = line.read(buffer, 0, buffer.length);
                        recordBytes.write(buffer, 0, bytesRead);
                    }

                    if (debug) {
                        LOG.log(Level.INFO, String.format("Save recorded chunk %d", chunk));
                    }
                    save(fileName);
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, "Error in timedAudioCapture ", ex);
                }
            });
            thread.setName("TimedAudioCapture");
        }

        public void stop() {
            isRunning = false;
        }

        public void start() {
            thread.start();
        }

        private void save(String fileName) throws IOException {
            byte[] audioData = recordBytes.toByteArray();
            final File wavFile = new File(getAudioFolder(), fileName + ".wav");
            ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
            try (AudioInputStream audioInputStream = new AudioInputStream(bais, format, audioData.length / format.getFrameSize())) {
                AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, wavFile);
            }
            recordBytes.close();
            if (debug) {
                LOG.log(Level.INFO, String.format("File %s.wav added to %s", fileName, getAudioFolder()));
            }
            addChunk.apply(fileName + ".wav");
        }

    }

    private ExecutorService createExecutor(final String name) {
        ThreadFactory factory = r -> {
            Thread t = new Thread(r);
            t.setName(name);
            t.setDaemon(true);
            return t;
        };
        return Executors.newSingleThreadExecutor(factory);
    }
}
