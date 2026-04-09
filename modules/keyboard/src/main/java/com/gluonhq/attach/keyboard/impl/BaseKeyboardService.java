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
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyFloatWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class that provides per-node keyboard-type tracking and per-node
 * text properties with automatic focus management.
 *
 * <p>Subclasses only need to implement the three {@code apply*} hooks
 * that bridge to the native layer.</p>
 */
public abstract class BaseKeyboardService implements KeyboardService {

    private static final Logger LOG = Logger.getLogger(BaseKeyboardService.class.getName());
    protected static final ReadOnlyFloatWrapper VISIBLE_HEIGHT = new ReadOnlyFloatWrapper();
    protected static final boolean debug = Util.DEBUG;

    /** Per-node keyboard type registrations. */
    private final Map<Node, KeyboardType> nodeKeyboardTypes = new WeakHashMap<>();

    /** Per-node text properties keyed by Node. */
    private static final Map<Node, ReadOnlyStringWrapper> nodeTextProperties = new WeakHashMap<>();

    /** Reverse map: synthetic string id → Node, for native callback dispatch. */
    private static final Map<String, Node> idToNode = new HashMap<>();

    BaseKeyboardService() {
        VISIBLE_HEIGHT.addListener((obs, ov, nv) -> {
            if (nv != null && nv.doubleValue() <= 0) {
                if (debug) {
                    LOG.info("Keyboard hidden, reset default type");
                }
                applyKeyboardType(KeyboardType.ASCII.getValue());
                applyActiveNodeId(""); // reset active node
            }
        });
    }

    @Override
    public void setKeyboardTypeForNode(Node node, KeyboardType type) {
        Objects.requireNonNull(node, "node must not be null");
        Objects.requireNonNull(type, "type must not be null");
        nodeKeyboardTypes.put(node, type);
        installEventFilter(node);
    }

    @Override
    public ReadOnlyStringProperty textPropertyForNode(Node node) {
        Objects.requireNonNull(node, "node must not be null");
        installEventFilter(node);
        return nodeTextProperties.computeIfAbsent(node, n -> {
            idToNode.put(syntheticId(n), n);
            return new ReadOnlyStringWrapper("");
        }).getReadOnlyProperty();
    }

    private void installEventFilter(Node node) {
        node.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            KeyboardType type = nodeKeyboardTypes.getOrDefault(node, KeyboardType.ASCII);
            if (debug) {
                LOG.info(String.format("Focused keyboard type: %s for id %s", type, syntheticId(node)));
            }
            applyActiveNodeId(syntheticId(node));
            applyKeyboardType(type.getValue());
        });
    }

    /**
     * Generates a stable string id for a node.  Uses the node's own
     * {@link Node#getId() id} if set, otherwise falls back to a
     * synthetic id based on identity hash code.
     */
    protected static String syntheticId(Node node) {
        String id = node.getId();
        return id != null ? id : "attach-kb-" + System.identityHashCode(node);
    }

    /**
     * Called from the native callback to update the text property for the
     * node identified by {@code id}.
     */
    protected static void updateTextForId(String id, String text) {
        Node node = idToNode.get(id);
        if (node == null) {
            return;
        }
        ReadOnlyStringWrapper wrapper = nodeTextProperties.get(node);
        if (wrapper != null && !Objects.equals(wrapper.get(), text)) {
            Platform.runLater(() -> wrapper.set(text));
        }
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
     * Tell the native layer which node id is currently active.
     * @param id the string id of the focused node
     */
    protected abstract void applyActiveNodeId(String id);
}

