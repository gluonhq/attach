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

import javafx.application.Platform;

public class AndroidKeyboardService extends BaseKeyboardService {

    static {
        System.loadLibrary("keyboard");
    }

    public AndroidKeyboardService() {
    }


    @Override
    protected void applyKeyboardType(int nativeValue) {
        nativeSetKeyboardType(nativeValue);
    }

    @Override
    protected void applyActiveNodeId(String id) {
        nativeSetActiveNodeId(id);
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
        updateTextForId(id, text);
    }

}