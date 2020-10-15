/*
 * Copyright (c) 2018, 2020 Gluon
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
package com.gluonhq.attach.augmentedreality;

import com.gluonhq.attach.util.Services;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;

import java.util.Optional;

/**
 * The Augmented Reality Service allows accessing the native AR kit, if it is available.
 * 
 * The developer can check if the device has Augmented Reality support.
 * 
 * <p><b>Example</b></p>
 * <pre>
 * {@code Services.get(AugmentedRealityService.class).ifPresent(service -> {
 *       Availability availability = service.checkAR(() -> {
 *          // perform action after ARCore is installed
 *       });
 *       System.out.println("AR availability: " + availability.name());
 *  });}</pre>
 * 
 * A 3D model with .obj format can be added, and when the user taps on the Augmented Reality
 * world, it will be displayed.
 * 
 * The .obj file can make reference to other .mtl files, and/or to texture files. 
 * All of these files have to be placed in the {@code /src/main/resources/} folder or 
 * subfolders.
 * 
 * <p><b>Example</b></p>
 * <p>The following example includes two files: {@code DukeKing.obj}
 * and {@code DukeKing.mtl} under the {@code src/main/resources/models} folder</p>
 * 
 * <pre>
 * {@code Services.get(AugmentedRealityService.class).ifPresent(service -> {
 *       ARModel model = new ARModel();
 *       model.setName("DukeKing");
 *       model.setObjFilename("models/DukeKing.obj");
 *       model.setScale(0.8);
 *       service.setModel(model);
 *       service.cancelled().addListener((obs, ov, nv) -> {
 *           if (nv) {
 *               System.out.println("AR was cancelled");
 *           }
 *       });
 *       service.showAR();
 *  });}</pre>
 *
 * <p><b>Android Configuration</b></p>
 * <p>Add the following to the manifest</p>
 * <pre>
 * {@code <manifest ...>
 *   <uses-permission android:name="android.permission.CAMERA" />
 *   <!--<uses-feature android:name="android.hardware.camera.ar" android:required="true"/>-->
 *   <application ...>
 *       <meta-data android:name="com.google.ar.core" android:value="optional" />
 *       <!--<meta-data android:name="com.google.ar.core" android:value="required" />-->
 *   </application>
 * </manifest>}
 * </pre>
 * 
 * <p>Note: Uncomment the above commented lines if ARCore is strictly required.</p>
 *
 * <b>Note</b>: All these modifications are handled automatically by the
 * <a href="https://docs.gluonhq.com/client">Gluon Client plugin</a> during the package goal.
 *
 * <p><b>iOS Configuration</b></p>
 *
 * None
 *
 * @since 3.9.0
 */
public interface AugmentedRealityService {
    
    enum Availability {
        AR_NOT_SUPPORTED, ARCORE_NOT_INSTALLED, ARCORE_OUTDATED, IOS_NOT_UPDATED, AR_SUPPORTED
    }

    /**
     * Returns an instance of {@link AugmentedRealityService}.
     * @return An instance of {@link AugmentedRealityService}.
     */
    static Optional<AugmentedRealityService> create() {
        return Services.get(AugmentedRealityService.class);
    }

    /**
     * Checks if device supports AR
     * @return the availability of AR on the device
     */
    Availability getAvailability();

    /**
     * Property that can be used to listen if the AR availability has changed
     * @return a {@link ReadOnlyObjectProperty}
     */
    ReadOnlyObjectProperty<Availability> availabilityProperty();

    /**
     * Sets the 3D model that is going to be used by the AR session.
     * The .obj 3D model can be displayed during the AR session when the user
     * taps on the screen after the camera recognizes a feature plane or point
     * 
     * The model files have to be placed directly or within a folder under 
     * {@code /src/ios/assets/} for iOS, while for Android these can be placed 
     * under {@code /src/android/assets/} or {@code /src/main/resources/assets/}.
     * 
     * @param model the entity model
     */
    void setModel(ARModel model);
    
    /**
     * Opens AR
     */
    void showAR();
    
    /**
     * Shows debug information
     * 
     * @param enable set to true to get verbose output
     */
    void debugAR(boolean enable);
    
    /**
     * Boolean property that can be used to listen if the AR session was cancelled
     * @return a {@link ReadOnlyBooleanProperty}
     */
    ReadOnlyBooleanProperty cancelled();

}
