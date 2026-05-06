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
package com.gluonhq.helloandroid;

import android.view.View;

import androidx.camera.view.PreviewView;
import androidx.lifecycle.Observer;

/**
 * Manages the black startup cover shown above the preview surface.
 * When the Camera stream is ready fades the cover out.
 */
final class PreviewStreamCoverController {

    private final PreviewView previewView;
    private final View startupCover;
    private Observer<PreviewView.StreamState> previewStreamObserver;

    PreviewStreamCoverController(PreviewView previewView, View startupCover) {
        this.previewView = previewView;
        this.startupCover = startupCover;
    }

    void prepareForBind() {
        if (startupCover == null) {
            return;
        }
        startupCover.animate().cancel();
        startupCover.setAlpha(1f);
        startupCover.setVisibility(View.VISIBLE);
    }

    void attach() {
        if (previewView == null || previewStreamObserver != null) {
            return;
        }
        previewStreamObserver = new Observer<PreviewView.StreamState>() {
            @Override
            public void onChanged(PreviewView.StreamState streamState) {
                if (streamState == PreviewView.StreamState.STREAMING && startupCover != null) {
                    fadeOutStartupCover();
                }
            }
        };
        previewView.getPreviewStreamState().observeForever(previewStreamObserver);
    }

    void detach() {
        if (previewView != null && previewStreamObserver != null) {
            previewView.getPreviewStreamState().removeObserver(previewStreamObserver);
            previewStreamObserver = null;
        }
    }

    private void fadeOutStartupCover() {
        if (startupCover == null || startupCover.getVisibility() != View.VISIBLE) {
            return;
        }
        startupCover.animate()
                .alpha(0f)
                .setDuration(120)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        if (startupCover != null) {
                            startupCover.setVisibility(View.GONE);
                            startupCover.setAlpha(1f);
                        }
                    }
                })
                .start();
    }
}

