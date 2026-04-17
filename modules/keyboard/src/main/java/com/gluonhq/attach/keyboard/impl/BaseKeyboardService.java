/*
 * Copyright (c) 2026, Gluon
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
import javafx.beans.property.ReadOnlyFloatProperty;
import javafx.beans.property.ReadOnlyFloatWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.util.Duration;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class that provides common functionality for iOS and Android implementations.
 */
public abstract class BaseKeyboardService implements KeyboardService {

    private static final Logger LOG = Logger.getLogger(BaseKeyboardService.class.getName());
    protected static final ReadOnlyFloatWrapper VISIBLE_HEIGHT = new ReadOnlyFloatWrapper();
    protected static final boolean debug = Util.DEBUG;

    /** Map of nodes and keyboard types. */
    private final Map<Node, KeyboardType> nodeKeyboardTypes = new WeakHashMap<>();

    /** Map of nodes to their visibility listeners. */
    private final Map<Node, ChangeListener<Number>> visibilityListeners = new WeakHashMap<>();

    /** Scenes for which a focusOwner listener has already been installed. */
    private final Set<Scene> trackedScenes = Collections.newSetFromMap(new WeakHashMap<>());

    BaseKeyboardService() {
    }

    @Override
    public void keepVisibilityForNode(Node node) {
        keepVisibilityForNode(node, null);
    }

    @Override
    public void keepVisibilityForNode(Node node, Parent parent) {
        Objects.requireNonNull(node, "node must not be null");
        releaseVisibilityForNode(node);
        ChangeListener<Number> listener = (obs, ov, nv) -> adjustPosition(node, parent, nv.doubleValue());
        visibilityListeners.put(node, listener);
        VISIBLE_HEIGHT.addListener(listener);
    }

    @Override
    public void releaseVisibilityForNode(Node node) {
        Objects.requireNonNull(node, "node must not be null");
        ChangeListener<Number> listener = visibilityListeners.remove(node);
        if (listener != null) {
            VISIBLE_HEIGHT.removeListener(listener);
        }
    }

    @Override
    public ReadOnlyFloatProperty visibleHeightProperty() {
        return VISIBLE_HEIGHT.getReadOnlyProperty();
    }

    @Override
    public void setKeyboardTypeForNode(Node node, KeyboardType type) {
        Objects.requireNonNull(node, "node must not be null");
        Objects.requireNonNull(type, "type must not be null");
        nodeKeyboardTypes.put(node, type);
        attachFocusTracker(node);
    }

    @Override
    public void removeKeyboardTypeForNode(Node node) {
        Objects.requireNonNull(node, "node must not be null");
        nodeKeyboardTypes.remove(node);
    }

    /**
     * Ensures a single focusOwner listener is installed on the scene that
     * contains {@code node}. The listener drives the native keyboard type for
     * every focus change in that scene, whether the newly focused node was
     * explicitly registered via {@link #setKeyboardTypeForNode} or not.
     * If {@code node} is not yet in a scene, the installation is deferred
     * until it is.
     */
    private void attachFocusTracker(Node node) {
        Scene scene = node.getScene();
        if (scene != null) {
            trackScene(scene);
            // If this node is already the focus owner, apply its type now
            if (scene.getFocusOwner() == node) {
                applyTypeFor(node);
            }
            return;
        }
        node.sceneProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Scene> obs, Scene ov, Scene nv) {
                if (nv != null) {
                    trackScene(nv);
                    if (nv.getFocusOwner() == node) {
                        applyTypeFor(node);
                    }
                    obs.removeListener(this);
                }
            }
        });
    }

    private void trackScene(Scene scene) {
        if (!trackedScenes.add(scene)) {
            return;
        }
        scene.focusOwnerProperty().addListener((obs, ov, newNode) -> applyTypeFor(newNode));
    }

    /**
     * Pushes the id and keyboard type for {@code focused} down to the native
     * layer. Registered nodes use their stored {@link KeyboardType}; any other
     * focus owner (including {@code null}) falls back to {@link KeyboardType#ASCII}.
     */
    private void applyTypeFor(Node focused) {
        if (focused == null) {
            if (debug) {
                LOG.info("Focus cleared, applying default ASCII keyboard");
            }
            applyActiveNodeId("");
            applyKeyboardType(KeyboardType.ASCII.getValue());
            return;
        }
        KeyboardType type = nodeKeyboardTypes.getOrDefault(focused, KeyboardType.ASCII);
        String id = syntheticId(focused);
        if (debug) {
            LOG.info(String.format("Active keyboard type: %s for id %s", type, id));
        }
        applyActiveNodeId(id);
        applyKeyboardType(type.getValue());
    }

    /**
     * Uses the node's own {@link Node#getId() id} if set, otherwise falls back to an
     * id based on its identity hash code.
     */
    protected static String syntheticId(Node node) {
        String id = node.getId();
        return id != null ? id : "attach-kb-" + System.identityHashCode(node);
    }

    protected static void adjustPosition(Node node, Parent parent, double kh) {
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

    /**
     * Apply the keyboard type on the native side.
     * @param nativeValue the integer value from {@link KeyboardType#getValue()}
     */
    protected abstract void applyKeyboardType(int nativeValue);

    /**
     * Pass to the native layer the id of the currently active node.
     * @param id the string id of the active node
     */
    protected abstract void applyActiveNodeId(String id);
}

