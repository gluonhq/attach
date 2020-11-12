/*
 * Copyright (c) 2017, 2019 Gluon
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
package com.gluonhq.attach.inappbilling;

import com.gluonhq.attach.util.Services;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.concurrent.Worker;

import java.util.List;
import java.util.Optional;

/**
 * With the in-app billing service you can query information about in-app products and
 * purchases that were done and place new orders for those products.
 *
 * <p>Your application must be properly configured with the list of in-app products
 * when you want to use this service. The list of available in-app products must be
 * passed in first by {@link #initialize(String, List) initializing} the service. Each
 * registered product must have the identifier and type that matches the configuration
 * of your in-app product on iOS and Android. For your convenience, you should use the
 * same identifier for each product on both iOS and Android.</p>
 *
 * <p><b>Example</b></p>
 * <pre>
 * {@code InAppBillingService service = InAppBillingService.create()
 *          .orElseThrow(() -> new RuntimeException("Could not load In-App Billing service"));
 *
 *      // initialize and register available products
 *      service.initialize(androidBase64LicenseKey, Arrays.asList(
 *              new Product("com.sample.appone.product1_identifier", Product.Type.CONSUMABLE),
 *              new Product("com.sample.appone.product2_identifier", Product.Type.NON_CONSUMABLE)
 *      ));
 *
 *      // construct UI based on available in-app products
 *      Worker<List<Product>> productDetails = service.fetchProductDetails();
 *      productDetails.stateProperty().addListener((obs, ov, nv) -> {
 *          for (Product product : productDetails.getValue()) {
 *              Label price = new Label(product.getDetails().getCurrency() + product.getDetails().getPrice());
 *              Label title = new Label(product.getDetails().getTitle());
 *              Button buy = new Button("buy");
 *              buy.setOnAction(e -> order(product));
 *              HBox productControls = new HBox(10.0, price, title, buy);
 *              productList.getChildren().add(productControls);
 *          }
 *      });
 *
 *      // place an order
 *      private void order(Product product) {
 *          Worker<ProductOrder> order = service.order(product1);
 *          order.stateProperty().addListener((obs, ov, nv) -> {
 *              if (nv == Worker.State.FAILED) {
 *                  order.getException().printStackTrace();
 *              } else if (nv == Worker.State.SUCCEEDED) {
 *                  Product boughtProduct = order.getValue().getProduct();
 *                  if (boughtProduct.getType() == Product.Type.CONSUMABLE) {
 *                      Worker<Product> finishOrder = service.finish(order.getValue());
 *                      finishOrder.stateProperty().addListener((obs2, ov2, nv2) -> {
 *                          if (nv2 == Worker.State.FAILED) {
 *                              finishOrder.getException().printStackTrace();
 *                          } else if (nv2 == Worker.State.SUCCEEDED) {
 *                              Product finishedProduct = finishOrder.getValue();
 *                              new Toast("You bought: " + finishedProduct.getTitle()).show();
 *                          }
 *                      });
 *                  } else {
 *                      new Toast("You bought: " + boughtProduct.getTitle()).show();
 *                  }
 *              }
 *          }
 *      });
 *  });}</pre>
 *
 * <p><b>Android Configuration</b></p>
 * <p>The permission <code>com.android.vending.BILLING</code> needs to be added.</p>
 *
 * Note: these modifications are handled automatically by <a href="https://docs.gluonhq.com/client">Client plugin</a> if it is used.
 * <pre>
 * {@code <manifest ...>
 *    <uses-permission android:name="com.android.vending.BILLING"/>
 *    ...
 *  </manifest>}</pre>
 *
 * <p><b>iOS Configuration</b>: none</p>
 *
 * @since 3.4.0
 */
public interface InAppBillingService {

    /**
     * Returns an instance of {@link InAppBillingService}.
     * @return An instance of {@link InAppBillingService}.
     */
    static Optional<InAppBillingService> create() {
        return Services.get(InAppBillingService.class);
    }

    /**
     * Returns true if the device supports in-app billing.
     * @return True if the device supports in-app billing.
     */
    boolean isSupported();

    /**
     * Set a query listener to listen for results of asynchronous queries
     * of the registered in-app products.
     *
     * @param listener the query listener to set
     */
    void setQueryResultListener(InAppBillingQueryResultListener listener);

    /**
     * Updates the list of available products that are configured in the in-app
     * product sections of the application configuration for iOS and Android.
     *
     * @param registeredProducts a list of available in-app products
     */
    void setRegisteredProducts(List<Product> registeredProducts);

    /**
     * Correctly initialize the In-App Billing service on the device. This makes
     * sure that everything is in place before you can start interacting with
     * the methods that deal with the actual in-app products.
     *
     * <p>When initialization completed successfully, a query will be triggered
     * to fetch the initial purchase details for each registered in-app product. You
     * should {@link #setQueryResultListener(InAppBillingQueryResultListener) set a query listener}
     * to act upon the results of that query.</p>
     *
     * @param androidPublicKey the license key of your Android application which can be found in the
     *                         Google Play developer console.
     * @param registeredProducts a list of available in-app products that are configured in your
     *                           application configuration for iOS and Android
     */
    void initialize(String androidPublicKey, List<Product> registeredProducts);

    boolean isReady();

    ReadOnlyBooleanProperty readyProperty();

    /**
     * Retrieves the details for the current list of available in-app products. In
     * case a product from the list of registered products could not be queried, they
     * will not be listed in the returned list. This method must be called to get the
     * details of each registered product, like the title, description and localised
     * price and currency. You can use these details to build up your UI.
     *
     * @return A list of products for which the details could be fetched, wrapped in a Worker.
     */
    Worker<List<Product>> fetchProductDetails();

    /**
     * Place a new order for the specified product. The returned Worker can be used to
     * determine if the order was successful.
     *
     * @param product the product to place an order for
     * @return A ProductOrder containing details about the processed order, wrapped in a Worker.
     */
    Worker<ProductOrder> order(Product product);

    /**
     * Finish a product order for a {@link Product.Type#CONSUMABLE consumable} product. The product
     * order will not have been processed (and no money will have been transferred) until the order
     * is finished.
     *
     * @param productOrder the product order to finish
     * @return The product that was ordered with in the provided ProductOrder with updated details,
     * wrapped in a Worker.
     */
    Worker<Product> finish(ProductOrder productOrder);

}
