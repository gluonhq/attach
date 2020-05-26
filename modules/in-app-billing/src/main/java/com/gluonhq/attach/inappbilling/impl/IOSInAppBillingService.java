/*
 * Copyright (c) 2017, 2019, Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
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
package com.gluonhq.attach.inappbilling.impl;

import com.gluonhq.attach.inappbilling.InAppBillingException;
import com.gluonhq.attach.inappbilling.InAppBillingQueryResult;
import com.gluonhq.attach.inappbilling.InAppBillingQueryResultListener;
import com.gluonhq.attach.inappbilling.InAppBillingService;
import com.gluonhq.attach.inappbilling.Product;
import com.gluonhq.attach.inappbilling.ProductDetails;
import com.gluonhq.attach.inappbilling.ProductOrder;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class IOSInAppBillingService implements InAppBillingService {

    static {
        System.loadLibrary("InAppBilling");
        initInAppBilling();
    }

    private static final ReadOnlyBooleanWrapper ready = new ReadOnlyBooleanWrapper(false);

    private static InAppBillingQueryResultListener queryResultListener;

    private static List<Product> registeredProducts = new LinkedList<>();
    private final List<String> productIds = new LinkedList<>();
    private final List<String> subscriptionIds = new LinkedList<>();
    private final static List<Product> detailedProducts = new LinkedList<>();
    private static boolean supported = true;
    private static final BooleanProperty FETCH = new SimpleBooleanProperty();
    private static final ObservableMap<String, ProductOrder> MAP_ORDERS = FXCollections.observableMap(new HashMap<>());

    @Override
    public boolean isSupported() {
        return supported;
    }

    @Override
    public void setQueryResultListener(InAppBillingQueryResultListener listener) {
        this.queryResultListener = listener;
    }

    @Override
    public void setRegisteredProducts(List<Product> registeredProducts) {
        this.registeredProducts = registeredProducts;
    }

    @Override
    public void initialize(String androidPublicKey, List<Product> registeredProducts) {
        this.registeredProducts = registeredProducts;
    }

    @Override
    public boolean isReady() {
        return ready.get();
    }

    @Override
    public ReadOnlyBooleanProperty readyProperty() {
        return ready.getReadOnlyProperty();
    }

    @Override
    public Worker<List<Product>> fetchProductDetails() {
        if (!isReady()) {
            return null;
        }
        Task<List<Product>> task = new Task<List<Product>>() {

            @Override
            protected List<Product> call() throws Exception {
                for (Product registeredProduct : registeredProducts) {
                    switch (registeredProduct.getType()) {
                        case CONSUMABLE:
                        case NON_CONSUMABLE:
                            productIds.add(registeredProduct.getId());
                            break;
                        case FREE_SUBSCRIPTION:
                        case RENEWABLE_SUBSCRIPTION:
                        case NON_RENEWABLE_SUBSCRIPTION:
                            subscriptionIds.add(registeredProduct.getId());
                            break;
                    }
                }
                
                CountDownLatch latch = new CountDownLatch(1);
                
                FETCH.addListener(new ChangeListener<Boolean>() {
                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                        FETCH.removeListener(this);
                        latch.countDown();
                    }
                });

                String[] ids = new String[productIds.size()];
                fetchProducts(productIds.toArray(ids));
                
                if (latch.await(5, TimeUnit.MINUTES)) {
                    return detailedProducts;
                } else {
                    throw new InAppBillingException("Products fetch operation timed out.");
                }
            }
        };
        FETCH.set(false);
        Thread thread = new Thread(task);
        thread.start();
        return task;
    }

    @Override
    public Worker<ProductOrder> order(Product product) {
        if (!isReady()) {
            return null;
        }
        
        final String key = UUID.randomUUID().toString();
        MAP_ORDERS.put(key, null);
        Task<ProductOrder> task = new Task<ProductOrder>() {

            @Override
            protected ProductOrder call() throws Exception {
                CountDownLatch latch = new CountDownLatch(1);
                
                MAP_ORDERS.addListener(new MapChangeListener<String, ProductOrder>() {
                    @Override
                    public void onChanged(MapChangeListener.Change<? extends String, ? extends ProductOrder> change) {
                        if (key.equals(change.getKey())) {
                            MAP_ORDERS.removeListener(this);
                            latch.countDown();
                        }
                    }
                });
                
                purchaseProduct(key, product.getId());
                
                if (latch.await(15, TimeUnit.MINUTES)) {
                    ProductOrder productOrder = MAP_ORDERS.remove(key);
                    if (productOrder == null) {
                        throw new InAppBillingException("There was an error purchasing the product");
                    }
                    return productOrder;
                } else {
                    throw new InAppBillingException("Product order operation timed out.");
                }
            }
        };
        Thread thread = new Thread(task);
        thread.start();
        return task;
    }

    @Override
    public Worker<Product> finish(ProductOrder productOrder) {
        if (!isReady()) {
            return null;
        }
        Task<Product> task = new Task<Product>() {

            @Override
            protected Product call() throws Exception {
                if (productOrder != null && productOrder.getProduct() != null) {
                    Product product = productOrder.getProduct();
                    product.getDetails().setState(ProductDetails.State.FINISHED);
                    return product;
                }
                return null;
            }
        };
        Thread thread = new Thread(task);
        thread.start();
        return task;
    }
    
    // native
    private static native void initInAppBilling(); // init IDs for java callbacks from native
    private static native void fetchProducts(String[] ids);
    private static native void purchaseProduct(String key, String id);

    // callbacks
    private static void setInAppReady(boolean value) {
        supported = value;
        Platform.runLater(() -> ready.set(value));
    }
    
    private static void setProduct(String id, String title, String description, String price, String currency) {
        ProductDetails details = new ProductDetails();
        details.setTitle(title);
        details.setDescription(description);
        details.setPrice(price);
        details.setCurrency(currency);
        
        for (Product product : registeredProducts) { 
            if (product.getId().equals(id)) {
                Product detailedProduct = new Product(product.getId(), product.getType());
                detailedProduct.setDetails(details);
                detailedProducts.add(detailedProduct);
                break;
            }
        }
    }
    
    private static void doneFetching(boolean value) {
        if (!value) {
            System.out.println("There was an error fetching products");
        }
        Platform.runLater(() -> FETCH.set(true));
    }
    
    private static void restorePurchases(String[] puchasesId) {
        InAppBillingQueryResult result = new InAppBillingQueryResult();
        for (String id: puchasesId) {
            for (Product product : detailedProducts) { 
                if (product.getId().equals(id)) {
                    product.getDetails().setState(ProductDetails.State.APPROVED);
                    
                    ProductOrder productOrder = new ProductOrder();
                    productOrder.setPlatform(com.gluonhq.attach.util.Platform.IOS);
                    productOrder.setProduct(product);
                    // TODO: Restoring purchases doesn't take the fields for now
                    //productOrder.setFields(fields);
                    result.getProductOrders().add(productOrder);
                    break;
                }
            }
        }
        Platform.runLater(() -> queryResultListener.onQueryResultReceived(result));
    }
    
    private static void setPurchase(String key, String purchasedId, String transactionId, String transactionReceipt) {
        if (purchasedId == null || purchasedId.isEmpty()) {
            Platform.runLater(() -> MAP_ORDERS.put(key, null));
            return;
        }
        
        ProductOrder productOrder = new ProductOrder();
        productOrder.setPlatform(com.gluonhq.attach.util.Platform.IOS);
        for (Product product : detailedProducts) { 
            if (product.getId().equals(purchasedId)) {
                productOrder.setProduct(product);
                break;
            }
        }
        Map<String, Object> fields = new HashMap<>();
        fields.put("productId", purchasedId);
        fields.put("orderId", transactionId);
        fields.put("token", transactionReceipt);
        
        // TODO: validate transactionReceipt from the server side
        // https://developer.apple.com/library/content/releasenotes/General/ValidateAppStoreReceipt/Chapters/ValidateRemotely.html#//apple_ref/doc/uid/TP40010573-CH104-SW1
        productOrder.setFields(fields);
        Platform.runLater(() -> MAP_ORDERS.put(key, productOrder));
    }
}
