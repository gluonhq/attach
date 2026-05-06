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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Performs image manipulations after capture or import.
 * It reduces large images, applies the requested rotation,
 * and rewrites a compact JPEG for faster loading on the Java side.
 */
final class ImagePreprocessor {

    private static final int TARGET_SIZE = 1280;
    private static final int JPEG_QUALITY = 85;
    private static final byte[] DECODE_BUFFER = new byte[16 * 1024];

    private ImagePreprocessor() {
    }

    static void preprocessImage(File imageFile, int rotation, boolean debug, String tag) {
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            opts.inTempStorage = DECODE_BUFFER;
            BitmapFactory.decodeFile(imageFile.getAbsolutePath(), opts);

            int srcW = opts.outWidth;
            int srcH = opts.outHeight;
            if (rotation == 90 || rotation == 270) {
                srcW = opts.outHeight;
                srcH = opts.outWidth;
            }
            opts.inSampleSize = calculateInSampleSize(srcW, srcH, TARGET_SIZE);
            opts.inJustDecodeBounds = false;
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            opts.inTempStorage = DECODE_BUFFER;

            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), opts);
            if (bitmap == null) {
                Log.e(tag, "preprocessImage: failed to decode bitmap");
                return;
            }

            try {
                Matrix matrix = new Matrix();
                float scale = Math.min(
                        (float) TARGET_SIZE / bitmap.getWidth(),
                        (float) TARGET_SIZE / bitmap.getHeight());
                if (scale < 1.0f) {
                    matrix.postScale(scale, scale);
                }
                if (rotation != 0) {
                    float cx = bitmap.getWidth() * Math.max(scale, 1.0f) / 2f;
                    float cy = bitmap.getHeight() * Math.max(scale, 1.0f) / 2f;
                    matrix.postRotate(rotation, cx, cy);
                }

                if (!matrix.isIdentity()) {
                    Bitmap transformed = Bitmap.createBitmap(bitmap, 0, 0,
                            bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    bitmap.recycle();
                    bitmap = transformed;
                }

                try (FileOutputStream fos = new FileOutputStream(imageFile)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos);
                }
                if (debug) {
                    Log.v(tag, "preprocessImage: wrote " + bitmap.getWidth() + "x" + bitmap.getHeight()
                            + " (rotation=" + rotation + ") to " + imageFile.getName());
                }
            } finally {
                bitmap.recycle();
            }
        } catch (Exception e) {
            Log.e(tag, "preprocessImage failed, falling back to original: " + e.getMessage());
        }
    }

    private static int calculateInSampleSize(int width, int height, int targetSize) {
        int inSampleSize = 1;
        if (height > targetSize || width > targetSize) {
            int halfH = height / 2;
            int halfW = width / 2;
            while ((halfH / inSampleSize) >= targetSize && (halfW / inSampleSize) >= targetSize) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}

