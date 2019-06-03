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
package com.gluonhq.attach.settings.impl;

import com.gluonhq.attach.settings.SettingsService;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of SettingService that stores all settings in a Properties
 * file on the local file system.
 */
public class DesktopSettingsService implements SettingsService {

    private static final Logger LOG = Logger.getLogger(DesktopSettingsService.class.getName());

    private String storageDirectory;
    private final Properties settings = new Properties();

    public DesktopSettingsService(/* String storageDirectory */) {
        try {
            this.storageDirectory = getPrivateStorage().getAbsolutePath();

            Path settingsFile = getSettingsFile();

            settings.load(Files.newInputStream(settingsFile, StandardOpenOption.READ));
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Error in DesktopSettingService: ", ex);
        }

        LOG.log(Level.INFO, "The following settings were successfully read from file: {0}", settings);
    }

    @Override
    public void store(String key, String value) {
        settings.setProperty(key, value);
        saveSettings();

        LOG.log(Level.INFO, "Updated setting {0} = \"{1}\"", new Object[]{key, value});
    }

    @Override
    public void remove(String key) {
        Object value = settings.remove(key);
        saveSettings();

        LOG.log(Level.INFO, "Removed setting {0} = \"{1}\"", new Object[]{key, value});
    }

    @Override
    public String retrieve(String key) {
        return settings.getProperty(key);
    }

    /**
     * Saves the current properties to the settings.properties file on the file
     * system.
     */
    private void saveSettings() {
        Path settingsFile = getSettingsFile();
        try (BufferedWriter writer = Files.newBufferedWriter(settingsFile, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            settings.store(writer, null);

            LOG.log(Level.FINE, "The settings were successfully written to file.");
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Failed to store settings.properties file.", ex);
        }
    }

    /**
     * Gets the path to the properties file and creates it if does not yet exist
     * on the file system.
     *
     * @return the path to the settings.properties file
     */
    private Path getSettingsFile() {
        Path settingsFile = Paths.get(storageDirectory, "settings.properties");
        if (!Files.exists(settingsFile)) {
            try {
                Files.createFile(settingsFile);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, "Failed to create settings.properties file.", ex);
            }
        }

        LOG.log(Level.INFO, "settings.properties location is {0}", settingsFile);

        return settingsFile;
    }

    private File getPrivateStorage() throws IOException {
        String home = System.getProperty("user.home");
        File f = new File(home, ".gluon");
        if (!f.isDirectory()) {
            f.mkdirs();
        }
        return f;
    }
}
