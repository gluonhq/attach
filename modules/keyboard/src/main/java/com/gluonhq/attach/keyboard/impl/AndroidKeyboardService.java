/*
 * Copyright (c) 2020, 2026, Gluon
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
package com.gluonhq.attach.keyboard.impl;

import com.gluonhq.attach.keyboard.KeyboardService;
import com.gluonhq.attach.keyboard.KeyboardType;
import com.gluonhq.attach.util.Util;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyFloatProperty;
import javafx.beans.property.ReadOnlyFloatWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AndroidKeyboardService implements KeyboardService {

    private static final Logger LOG = Logger.getLogger(AndroidKeyboardService.class.getName());
    private static final ReadOnlyFloatWrapper VISIBLE_HEIGHT = new ReadOnlyFloatWrapper();
    private static final Map<String, ReadOnlyStringWrapper> TEXT_MAP = new HashMap<>();
    private static final boolean debug = Util.DEBUG;

    static {
        System.loadLibrary("keyboard");
    }

    public AndroidKeyboardService() {
    }

    @Override
    public void keepVisibilityForNode(Node node) {
        keepVisibilityForNode(node, null);
    }

    @Override
    public void keepVisibilityForNode(Node node, Parent parent) {
        VISIBLE_HEIGHT.addListener((obs, ov, nv) -> adjustPosition(node, parent, nv.doubleValue()));
    }

    @Override
    public ReadOnlyFloatProperty visibleHeightProperty() {
        return VISIBLE_HEIGHT.getReadOnlyProperty();
    }

    @Override
    public void setKeyboardType(KeyboardType type) {
        if (type == null) {
            throw new IllegalArgumentException("KeyboardType must not be null");
        }
        nativeSetKeyboardType(type.getValue());
    }

    @Override
    public void setActiveNodeId(String id) {
        if (id == null) {
            throw new IllegalArgumentException("Node id must not be null");
        }
        nativeSetActiveNodeId(id);
    }

    @Override
    public ReadOnlyStringProperty textProperty(String id) {
        return getOrCreateWrapper(id).getReadOnlyProperty();
    }

    private static ReadOnlyStringWrapper getOrCreateWrapper(String id) {
        return TEXT_MAP.computeIfAbsent(id, k -> new ReadOnlyStringWrapper(""));
    }

    private static void adjustPosition(Node node, Parent parent, double kh) {
        if (node == null || node.getScene() == null || node.getScene().getWindow() == null) {
            return;
        }
        double tTot = node.getScene().getHeight();
        double ty = node.getLocalToSceneTransform().getTy() + node.getBoundsInParent().getHeight() + 2;
        double y = 1;
        Parent root = parent == null ? node.getScene().getRoot() : parent;
        if (ty > tTot - kh) {
            y = tTot - ty - kh;
        } else if (kh == 0 && root.getTranslateY() != 0) {
            y = 0;
        }
        if (y <= 0) {
            if (debug) {
                LOG.log(Level.INFO, String.format("Moving %s %.2f pixels", root, y));
            }
            final TranslateTransition transition = new TranslateTransition(Duration.millis(50), root);
            transition.setFromY(root.getTranslateY());
            transition.setToY(y);
            transition.setInterpolator(Interpolator.EASE_OUT);
            transition.playFromStart();
        }
    }

    // native
    private static native void nativeSetKeyboardType(int keyboardTypeValue);
    private static native void nativeSetActiveNodeId(String id);

    // callbacks
    private static void notifyVisibleHeight(float height) {
        if (VISIBLE_HEIGHT.get() != height) {
            Platform.runLater(() -> VISIBLE_HEIGHT.set(height));
        }
    }

    /**
     * Called from keyboard.c when the native layer receives composing text
     * tagged with a node id.
     */
    private static void notifyComposingText(String id, String text) {
        ReadOnlyStringWrapper wrapper = getOrCreateWrapper(id);
        if (!Objects.equals(wrapper.get(), text)) {
            Platform.runLater(() -> wrapper.set(text));
        }
    }

}