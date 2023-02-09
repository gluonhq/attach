package com.gluonhq.attach.audio.impl;

import com.gluonhq.attach.audio.Audio;
import com.gluonhq.attach.audio.AudioService;
import com.gluonhq.attach.storage.StorageService;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author Bruno Salmon
 */
public class IOSAudioService implements AudioService {

    static {
        System.loadLibrary("Audio");
        initAudio();
    }

    @Override
    public Optional<Audio> loadSound(URL url) {
        return loadAudio(url, false);
    }

    @Override
    public Optional<Audio> loadMusic(URL url) {
        return loadAudio(url, true);
    }

    private Optional<Audio> loadAudio(URL url, boolean music) {
        try {
            String fileLocation = copyToPrivateStorageIfNeeded(url);
            if (!fileLocation.isEmpty()) {
                int id = loadSoundImpl(fileLocation, music);
                if (id >= 0)
                    return Optional.of(new IOSAudio(id));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    private static native void initAudio(); // init IDs for java callbacks from native
    private static native int loadSoundImpl(String url, boolean music);
    private static native void setLooping(int id, boolean looping);
    private static native void setVolume(int id, double volume);
    private static native void play(int id);
    private static native void pause(int id);
    private static native void stop(int id);
    private static native void dispose(int id);

    private File privateStorage;

    private String copyToPrivateStorageIfNeeded(URL url) throws Exception {
        String extForm = url.toExternalForm();
        // iOS only supports audio local files, it doesn't support http streaming for example. So when it's not a local
        // file, we need to copy it first to the private storage before being able to play it.
        if (!extForm.startsWith("file:")) { // Note: this also applies for "resource:" as GraalVM resources can't be directly accessed by iOS

            String fileName = extForm.substring(extForm.lastIndexOf("/") + 1);

            if (privateStorage == null) {
                privateStorage = StorageService.create()
                        .flatMap(StorageService::getPrivateStorage)
                        .orElseThrow(() -> new RuntimeException("Error accessing Private Storage folder"));
            }

            Path file = privateStorage.toPath()
                    .resolve("assets")
                    .resolve("audio")
                    .resolve(fileName);

            if (!Files.exists(file)) {
                if (!Files.exists(file.getParent())) {
                    Files.createDirectories(file.getParent());
                }

                try (InputStream input = url.openStream()) {
                    Files.copy(input, file);
                }
            }

            extForm = file.toUri().toString();
        }

        return extForm;
    }

    // All native calls are executed in a background thread to not hold the caller thread (which is probably the UI
    // thread). In particular the call to the play() method has been observed to take between 20ms and 50ms which is
    // noticeable in apps like games with 60 FPS (where time between frames = 16ms).
    private final static ScheduledExecutorService nativeExecutor = Executors.newSingleThreadScheduledExecutor();

    private static class IOSAudio implements Audio {

        private final int id;
        private boolean disposed;

        public IOSAudio(int id) {
            this.id = id;
        }

        @Override
        public void setLooping(boolean looping) {
            if (!disposed)
                nativeExecutor.execute(() -> IOSAudioService.setLooping(id, looping));
        }

        @Override
        public void setVolume(double volume) {
            if (!disposed)
                nativeExecutor.execute(() -> IOSAudioService.setVolume(id, volume));
        }

        @Override
        public void play() {
            if (!disposed)
                nativeExecutor.execute(() -> IOSAudioService.play(id));
        }

        @Override
        public void pause() {
            if (!disposed)
                nativeExecutor.execute(() -> IOSAudioService.pause(id));
        }

        @Override
        public void stop() {
            if (!disposed)
                nativeExecutor.execute(() -> IOSAudioService.stop(id));
        }

        @Override
        public void dispose() {
            if (!disposed)
                nativeExecutor.execute(() -> IOSAudioService.dispose(id));
            disposed = true;
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
    }
}
