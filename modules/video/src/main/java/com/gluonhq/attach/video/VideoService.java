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
package com.gluonhq.attach.video;

import com.gluonhq.attach.util.Services;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;

import java.util.Optional;

/**
 * With the video service you can play media files, both video and audio,
 * on your device. 
 *
 * <p><b>Important note:</b> The video files will be displayed on top of the JavaFX
 * layer. This means that the developer should take care of disabling view 
 * switching any other UI interaction that could lead to overlapping nodes  
 * while the video is playing.</p>
 *
 * <p>If the user calls {@link #hide() } or the playlist finishes, the media layer
 * will be removed, and the user will be able to resume normal interaction.</p>
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code Services.get(VideoService.class).ifPresent(service -> {
 *      service.setControlsVisible(true);
 * 　　  service.getPlaylist().add("my.video.mp4");
 *      service.play();
 *  });}</pre>
 *
 * <p><b>Android Configuration</b>: none</p>
 * <p><b>iOS Configuration</b>: none</p>
 *
 * @since 3.4.0
 */
public interface VideoService {

    /**
     * Returns an instance of {@link VideoService}.
     * @return An instance of {@link VideoService}.
     */
    static Optional<VideoService> create() {
        return Services.get(VideoService.class);
    }

    /**
     * Returns an observable list with media file names.
     *
     * <p>Supported formats:</p>
     * <ul>
     * <li><a href="https://developer.android.com/guide/topics/media/media-formats.html">Android</a></li>
     * <li><a href="https://developer.apple.com/library/content/documentation/Miscellaneous/Conceptual/iPhoneOSTechOverview/MediaLayer/MediaLayer.html#//apple_ref/doc/uid/TP40007898-CH9-SW6">iOS</a></li>
     * </ul>
     *
     * <p>The media files (video and audio) can either be a valid URL or they can be provided 
     * in the resources folder. </p>
     *
     * <p>For example, the following media files:</p>
     * <ul>
     * <li>/src/main/resources/media1.mp4</li>
     * <li>/src/main/resources/video/media2.mp4</li>
     * <li>http://www.host.com/media3.mp4</li>
     * </ul>
     *
     * can be added to the playlist as follows:
     *
     * <pre>
     * {@code getPlaylist().addAll("media1.mp4", "video/media2.mp4",
     * "http://www.host.com/media3.mp4");}
     * </pre>
     *
     * @return an {@code ObservableList} of media file names, either in the resource
     * folder or valid URLs
     */
    ObservableList<String> getPlaylist();

    /**
     * After {@link #getPlaylist() adding items to the the playlist}, this method can be
     * called to prepare the media player and the native layer on top of the JavaFX
     * one.
     *
     * <p>Calling this method doesn't start playing. Alternatively, call {@link #play()} directly.</p>
     */
    void show();

    /**
     * Plays a valid media file.
     *
     * <p>If the media player control wasn't created yet, it will be created first and
     * placed in a layer on top of the JavaFX layer. If it was already created,
     * it will start or resume playing.</p>
     */
    void play();

    /**
     * Stops a playing media file. It doesn't remove the media player.
     *
     * <p>If {@link #play()} is called again, the playlist will start from the
     * beginning of the playlist.</p>
     */
    void stop();

    /**
     * Pauses a playing media file.
     *
     * <p>If {@link #play()} is called again, the playlist will resume from the
     * position the media file was at when it was paused.</p>
     */
    void pause();

    /**
     * Removes the layer with the control, so the JavaFX layer can resume normal 
     * interaction. If a media file is currently playing, it will be stopped.
     *
     * <p>This method can be called at any time to stop and hide the media player.
     * It will be called automatically when the last media file in the playlist has
     * ended, unless {@link #setLooping(boolean) looping} is set to <code>true</code>.</p>
     */
    void hide();

    /**
     * Allows setting the position of the media file. Only valid when full screen
     * is disabled.
     *
     * @param alignment values for describing vertical and horizontal positioning
     * and alignment
     * @param topPadding the top padding value, relative to the screen
     * @param rightPadding the right padding value, relative to the screen
     * @param bottomPadding the bottom padding value, relative to the screen
     * @param leftPadding the left padding value, relative to the screen
     */
    void setPosition(Pos alignment, double topPadding, double rightPadding, double bottomPadding, double leftPadding);

    /**
     * When looping is set to <code>true</code> the media files in the playlist will
     * be played indefinitely until {@link #stop()} or {@link #hide()} is called.
     *
     * <p><b>Note:</b> calling this method must be done before starting media playback.</p>
     *
     * @param looping When <code>true</code> the playlist will restart from the beginning
     *                after all media files have been played. Default value is <code>false</code>.
     */
    void setLooping(boolean looping);

    /**
     * When set to <code>true</code> the native embedded controls will be
     * displayed on top of the media file. Note that the controls are hidden
     * automatically after a few seconds and a tap on the screen might be
     * required to bring the controls back.
     *
     * <p><b>Note:</b> calling this method must be done before starting media playback.</p>
     *
     * <p>Media controls are also only visible for video files, not for audio.</p>
     *
     * @param controlsVisible true to show the native embedded controls. Default
     *                        value is <code>false</code>.
     */
    void setControlsVisible(boolean controlsVisible);

    /**
     * Called while the video is visible, if true displays the media file in full 
     * screen mode. It is displayed centered, without padding and with a black 
     * background to keep its aspect ratio.
     *
     * A pinch gesture can be used to switch from/to normal mode to/from full 
     * screen mode.
     *
     * FullScreen mode can be enabled only with video files, not with audio.
     *
     * @param fullScreen Sets the media file in full screen. Default is false
     */
    void setFullScreen(boolean fullScreen);

    /**
     * Boolean property that indicates if the media file is playing in full
     * screen mode or in normal mode.
     *
     * @return A {@link BooleanProperty} with the full screen mode status
     */
    BooleanProperty fullScreenProperty();

    /**
     * Read only property that indicates the media player status. 
     *
     * @return A {@link ReadOnlyObjectProperty} with the 
     * {@link Status} of the media player
     */
    ReadOnlyObjectProperty<Status> statusProperty();

    /**
     * Specifies the media file to be played at the specified index in the
     * initial playlist. The current media file will be stopped and the media
     * file that is located at the provided index will start playing.
     *
     * @param index The index from the playlist that will start playing
     */
    void setCurrentIndex(int index);

    /**
     * Integer property that indicates the current index on the playlist, 
     * starting from 0.
     *
     * @return an {@link IntegerProperty} indicating the current index on the playlist.
     */
    IntegerProperty currentIndexProperty();

}
