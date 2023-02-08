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
                long ref = loadSoundImpl(fileLocation);
                if (ref != 0)
                    return Optional.of(new IOSAudio(ref, music));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    private static native void initAudio(); // init IDs for java callbacks from native
    private static native long loadSoundImpl(String url);
    private static native void setLooping(long ref, boolean looping);
    private static native void setVolume(long ref, double volume);
    private static native void play(long ref, boolean music);
    private static native void pause(long ref);
    private static native void stop(long ref);
    private static native void dispose(long ref);

    private File privateStorage;

    private String copyToPrivateStorageIfNeeded(URL url) throws Exception {
        if (privateStorage == null) {
            privateStorage = StorageService.create()
                    .flatMap(StorageService::getPrivateStorage)
                    .orElseThrow(() -> new RuntimeException("Error accessing Private Storage folder"));
        }

        String extForm = url.toExternalForm();
        String fileName = extForm.substring(extForm.lastIndexOf("/") + 1);

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

        return file.toAbsolutePath().toString();
    }

    private final static ScheduledExecutorService nativeExecutor = Executors.newSingleThreadScheduledExecutor();

    private static class IOSAudio implements Audio {

        private final long ref;
        private final boolean music;
        private boolean disposed;

        public IOSAudio(long ref, boolean music) {
            this.ref = ref;
            this.music = music;
        }

        @Override
        public void setLooping(boolean looping) {
            nativeExecutor.execute(() -> IOSAudioService.setLooping(ref, looping));
        }

        @Override
        public void setVolume(double volume) {
            nativeExecutor.execute(() -> IOSAudioService.setVolume(ref, volume));
        }

        @Override
        public void play() {
            nativeExecutor.execute(() -> IOSAudioService.play(ref, music));
        }

        @Override
        public void pause() {
            nativeExecutor.execute(() -> IOSAudioService.pause(ref));
        }

        @Override
        public void stop() {
            nativeExecutor.execute(() -> IOSAudioService.stop(ref));
        }

        @Override
        public void dispose() {
            nativeExecutor.execute(() -> IOSAudioService.dispose(ref));
            disposed = true;
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }
    }
}
