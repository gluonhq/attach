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
package com.gluonhq.attach.keyboard;

import com.gluonhq.attach.util.Services;
import javafx.beans.property.ReadOnlyFloatProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.scene.Node;
import javafx.scene.Parent;

import java.util.Optional;

/**
 * With the keyboard service, you can query the height of the software
 * keyboard, whenever it becomes visible.
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code KeyboardService.create().ifPresent(service -> {
 *      service.visibleHeightProperty().addListener((obs, ov, nv) ->
 *          System.out.println("height: " + nv));
 *  });}</pre>
 *
 * <p><b>Android Configuration</b>: none</p>
 * <p><b>iOS Configuration</b>: none</p>
 *
 * @since 4.0.7
 */
public interface KeyboardService {

    /**
     * Returns an instance of {@link KeyboardService}.
     * @return An instance of {@link KeyboardService}.
     */
    static Optional<KeyboardService> create() {
        return Services.get(KeyboardService.class);
    }

    /**
     * Node is adjusted vertically when software keyboard shows up, so it
     * is visible and not covered by it. For that, the root parent will
     * be moved accordingly.
     *
     * @param node the Node to adjust, so it is always visible
     */
    void keepVisibilityForNode(Node node);

    /**
     * Node is adjusted vertically when software keyboard shows up, so it
     * is visible and not covered by it
     *
     * @param node the Node to adjust, so it is always visible
     * @param parent the Parent of the node that will be moved
     */
    void keepVisibilityForNode(Node node, Parent parent);

    /**
     * Gets the visible height of the Keyboard, so scene or views can adjusted
     * to prevent some of their content from being covered by the keyboard.
     *
     * @return A ReadOnlyFloatProperty with the height of the soft keyboard
     */
    ReadOnlyFloatProperty visibleHeightProperty();

    /**
     * Assigns a keyboard type to a specific node. When the node gains focus,
     * the keyboard type is applied automatically. When it loses focus, the
     * keyboard reverts to {@link KeyboardType#ASCII}.
     *
     * <p>Nodes that have not been registered default to
     * {@link KeyboardType#ASCII}.</p>
     *
     * @param node the node (typically a text input control) to configure
     * @param type the {@link KeyboardType} to use when this node has focus
     * @since 4.0.25
     */
    void setKeyboardTypeForNode(Node node, KeyboardType type);

    /**
     * Returns a read-only property that reflects the current composing /
     * committed text for the given node, as reported by the native IME.
     *
     * <p>Internally, focus changes on the node are tracked so the native
     * layer knows which control is active.</p>
     *
     * @param node the node whose text to observe
     * @return a ReadOnlyStringProperty with the text for the given node
     * @since 4.0.25
     */
    ReadOnlyStringProperty textPropertyForNode(Node node);

}
