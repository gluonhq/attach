/*
 * Copyright (c) 2018, 2020, Gluon
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
package com.gluonhq.helloandroid.ar;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.gluonhq.helloandroid.Util;
import de.javagl.obj.Mtl;
import de.javagl.obj.MtlReader;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjSplitting;
import de.javagl.obj.ObjUtils;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * https://github.com/javagl/Obj/issues/4#issuecomment-356424533
 */
public class ComplexObjectRenderer {

    private static final String TAG = Util.TAG;
    private final boolean debug = Util.isDebug();;

    /**
     * The list of ObjectRenderer instances that will render the individual
     * parts
     */
    private final List<ObjectRenderer> materialGroupObjectRenderers;

    /**
     * The path of the obj file under the assets folder
     */
    private String path = "";

    /**
     * Default constructor
     */
    public ComplexObjectRenderer() {
        this.materialGroupObjectRenderers = new ArrayList<>();
    }

    /**
     * Creates and initializes OpenGL resources needed for rendering the model.
     *
     * @param context Context for loading the shader and below-named model and
     *        texture assets.
     * @param objAssetName Name of the OBJ file containing the model geometry.
     * @param defaultTextureFileName Name of the PNG file containing the diffuse
     *        texture map, to be used as a fallback when the asset does not have
     *        an associated MTL file, or one of the textures referred to from
     *        the MTL file cannot be found.
     * @throws IOException If an IO error occurs
     */
    public void createOnGlThread(Context context, String objAssetName,
                                 String defaultTextureFileName) throws IOException {

        // get the path of the obj file
        path = objAssetName.contains("/") ? objAssetName.substring(0, objAssetName.lastIndexOf("/") + 1) : "";
        if (debug) Log.v(TAG, "resources path: " + path);

        // Read the obj file.
        try (InputStream objInputStream = new FileInputStream(objAssetName)) {
            Obj obj = ObjReader.read(objInputStream);

            // Prepare the Obj so that its structure is suitable for
            // rendering with OpenGL:
            // 1. Triangulate it
            // 2. Make sure that texture coordinates are not ambiguous
            // 3. Make sure that normals are not ambiguous
            // 4. Convert it to single-indexed data
            Obj renderableObj = ObjUtils.convertToRenderable(obj);
            // When there are no material groups, then just render the object
            // using the default texture
            if (renderableObj.getNumMaterialGroups() == 0) {
                createRenderers(context, renderableObj, defaultTextureFileName, null);
            } else {
                // Otherwise, create one renderer for each material
                createMaterialBasedRenderers(context, renderableObj, defaultTextureFileName);
            }
        }
    }

    /**
     * Creates the renderers for the given OBJ. If the given OBJ has less than
     * 65k vertices, a single renderer will be created. Otherwise, it will be
     * split into multiple parts, each having at most 65k vertices, and one
     * renderer for each part will be created.
     *
     * @param context Context for loading the shader and below-named model and
     *        texture assets.
     * @param obj The OBJ
     * @param textureFileName The texture file name
     * @throws IOException If an IO error occurs
     */
    private void createRenderers(Context context, Obj obj, String textureFileName, String materialName) throws IOException {
        if (obj.getNumVertices() <= 65000) {
            createRenderer(context, obj, textureFileName, materialName);
        } else {
            // If there are more than 65k vertices, then the object has to be
            // split into multiple parts, each having at most 65k vertices
            List<Obj> objParts = ObjSplitting.splitByMaxNumVertices(obj, 65000);
            for (int j = 0; j < objParts.size(); j++) {
                Obj objPart = objParts.get(j);
                createRenderer(context, objPart, textureFileName, materialName);
            }
        }
    }

    /**
     * Creates one renderer for each material group that is contained in the
     * given OBJ. This will read the MTL files that are associated with the
     * given OBJ and extract the texture file name from them (using the given
     * default as a fallback).
     *
     * @param context Context for loading the shader and below-named model and
     *        texture assets.
     * @param obj The OBJ
     * @param defaultTextureFileName The default texture file name
     * @throws IOException If an IO error occurs
     */
    private void createMaterialBasedRenderers(Context context, Obj obj, String defaultTextureFileName) throws IOException {

        // Read the MTL files that are referred to from the OBJ, and
        // extract all their MTL definitions
        List<String> mtlFileNames = obj.getMtlFileNames();
        List<Mtl> allMtls = new ArrayList<>();
        for (String mtlFileName : mtlFileNames) {
            try (InputStream mtlInputStream = new FileInputStream(path + mtlFileName)) {
                List<Mtl> mtls = MtlReader.read(mtlInputStream);
                allMtls.addAll(mtls);
            }
        }

        // Obtain the material groups from the OBJ, and create renderers for
        // each of them
        Map<String, Obj> materialGroupObjs = ObjSplitting.splitByMaterialGroups(obj);
        for (Map.Entry<String, Obj> entry : materialGroupObjs.entrySet()) {
            String materialName = entry.getKey();
            String textureFileName = findTextureFileName(materialName, allMtls, defaultTextureFileName);
            Obj materialGroupObj = entry.getValue();
            createRenderers(context, materialGroupObj, textureFileName, materialName);
        }
    }

    /**
     * Returns the texture file name (the "map_kd" property) of the MTL that has
     * the given name, or the default texture file name if no such MTL is found
     *
     * @param materialName The material name
     * @param mtls The sequence of all available MTLs
     * @param defaultTextureFileName The default texture file name
     * @return The texture file name
     */
    private String findTextureFileName(String materialName, Iterable<? extends Mtl> mtls, String defaultTextureFileName) {
        for (Mtl mtl : mtls) {
            if (Objects.equals(materialName, mtl.getName())) {
                final String mapKd = mtl.getMapKd();
                if (mapKd == null) {
                    // In case no texture file is supplied, return a color string:
                    int r = (int)Math.round(mtl.getKd().getX() * 255.0);
                    int g = (int)Math.round(mtl.getKd().getY() * 255.0);
                    int b = (int)Math.round(mtl.getKd().getZ() * 255.0);
                    return String.format("#%02x%02x%02x" , r, g, b);
                }
                return mapKd;
            }
        }
        return defaultTextureFileName;
    }

    /**
     * Create one ObjectRenderer for the given OBJ
     *
     * @param context Context for loading the shader and below-named model and
     *        texture assets.
     * @param obj The OBJ
     * @param textureFileName The texture file name
     * @throws IOException If an IO error occurs
     */
    private void createRenderer(Context context, Obj obj, String textureFileName, String materialName) throws IOException {

        if (debug) {
            Log.v(TAG, "Rendering part with " + obj.getNumVertices() + " vertices, texture file: " + textureFileName + " and material name: " + materialName);
        }

        ObjectRenderer objectRenderer = new ObjectRenderer();
        objectRenderer.createOnGlThread(context, obj, textureFileName, materialName);
        materialGroupObjectRenderers.add(objectRenderer);
    }

    /**
     * Draws the model.
     *
     * @param cameraView A 4x4 view matrix, in column-major order.
     * @param cameraPerspective A 4x4 projection matrix, in column-major order.
     * @param colorCorrectionRgba
     */
    public void draw(float[] cameraView, float[] cameraPerspective, float[] colorCorrectionRgba) {
        draw(cameraView, cameraPerspective, colorCorrectionRgba, new float[] {0f, 0f, 0f, 0f});
    }

    public void draw(float[] cameraView, float[] cameraPerspective, float[] colorCorrectionRgba, float[] objColor) {
        for (ObjectRenderer renderer : materialGroupObjectRenderers) {
            renderer.draw(cameraView, cameraPerspective, colorCorrectionRgba, objColor);
        }
    }

    /**
     * Updates the object model matrix and applies scaling.
     *
     * @param modelMatrix A 4x4 model-to-world transformation matrix, stored in column-major order.
     * @param scaleFactor A separate scaling factor to apply before the {@code modelMatrix}.
     * @see android.opengl.Matrix
     */
    public void updateModelMatrix(float[] modelMatrix, float scaleFactor) {
        for (ObjectRenderer renderer : materialGroupObjectRenderers) {
            renderer.updateModelMatrix(modelMatrix, scaleFactor);
        }
    }

    public void rotateModelX(float angle) {
        for (ObjectRenderer renderer : materialGroupObjectRenderers) {
            renderer.rotateModelX(angle);
        }
    }
    public void rotateModelY(float angle) {
        for (ObjectRenderer renderer : materialGroupObjectRenderers) {
            renderer.rotateModelY(angle);
        }
    }
    public void rotateModelZ(float angle) {
        for (ObjectRenderer renderer : materialGroupObjectRenderers) {
            renderer.rotateModelZ(angle);
        }
    }
}
