/*
 * Copyright (c) 2017, 2019, Gluon
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

#include "InAppBilling.h"

JNIEnv *env;

JNIEXPORT jint JNICALL
JNI_OnLoad_InAppBilling(JavaVM *vm, void *reserved)
{
#ifdef JNI_VERSION_1_8
    //min. returned JNI_VERSION required by JDK8 for builtin libraries
    if ((*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_8) != JNI_OK) {
        return JNI_VERSION_1_4;
    }
    return JNI_VERSION_1_8;
#else
    return JNI_VERSION_1_4;
#endif
}

static int InAppBillingInited = 0;

// InAppBilling
jclass mat_jInAppBillingServiceClass;
jmethodID mat_jInAppBillingService_setInAppReady = 0;
jmethodID mat_jInAppBillingService_setProduct = 0;
jmethodID mat_jInAppBillingService_doneFetching = 0;
jmethodID mat_jInAppBillingService_restorePurchases = 0;
jmethodID mat_jInAppBillingService_setPurchase = 0;

InAppBilling *_InAppBilling;
NSArray *arrayOfProductIds;
NSMutableArray *arrayOfProducts;
NSNumberFormatter *numberFormatter;
NSMutableDictionary *orders;

JNIEXPORT void JNICALL Java_com_gluonhq_attach_inappbilling_impl_IOSInAppBillingService_initInAppBilling
(JNIEnv *env, jclass jClass)
{
    if (InAppBillingInited)
    {
        return;
    }
    InAppBillingInited = 1;
    
    mat_jInAppBillingServiceClass = (*env)->NewGlobalRef(env, (*env)->FindClass(env, "com/gluonhq/attach/inappbilling/impl/IOSInAppBillingService"));
    mat_jInAppBillingService_setInAppReady = (*env)->GetStaticMethodID(env, mat_jInAppBillingServiceClass, "setInAppReady", "(Z)V");
    mat_jInAppBillingService_setProduct    = (*env)->GetStaticMethodID(env, mat_jInAppBillingServiceClass, "setProduct", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
    mat_jInAppBillingService_doneFetching  = (*env)->GetStaticMethodID(env, mat_jInAppBillingServiceClass, "doneFetching", "(Z)V");
    mat_jInAppBillingService_restorePurchases  = (*env)->GetStaticMethodID(env, mat_jInAppBillingServiceClass, "restorePurchases", "([Ljava/lang/String;)V");
    mat_jInAppBillingService_setPurchase  = (*env)->GetStaticMethodID(env, mat_jInAppBillingServiceClass, "setPurchase", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");

    debugAttach = NO;
    orders = [[NSMutableDictionary alloc] init];

    AttachLog(@"Init InAppBilling");
    _InAppBilling = [[InAppBilling alloc] init];

    [_InAppBilling setup];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_inappbilling_impl_IOSInAppBillingService_fetchProducts
(JNIEnv *env, jclass jClass, jobjectArray jProductIdsArray)
{
    int productIdCount = (*env)->GetArrayLength(env, jProductIdsArray);
    NSMutableArray *productIds = [[NSMutableArray alloc] init];

    for (int i=0; i<productIdCount; i++) {
        jstring jproductId = (jstring) ((*env)->GetObjectArrayElement(env, jProductIdsArray, i));
        const jchar *productIdString = (*env)->GetStringChars(env, jproductId, NULL);
        NSString *productId = [NSString stringWithCharacters:(UniChar *)productIdString length:(*env)->GetStringLength(env, jproductId)];
        (*env)->ReleaseStringChars(env, jproductId, productIdString);
        [productIds addObject:productId];
    }
    arrayOfProductIds = [NSArray arrayWithArray:productIds];

    AttachLog(@"Fetching products");
    [_InAppBilling fetchProducts];
}

JNIEXPORT void JNICALL Java_com_gluonhq_attach_inappbilling_impl_IOSInAppBillingService_purchaseProduct
(JNIEnv *env, jclass jClass, jstring jKey, jstring jProductId)
{
    const jchar *keyString = (*env)->GetStringChars(env, jKey, NULL);
    NSString *key = [NSString stringWithCharacters:(UniChar *)keyString length:(*env)->GetStringLength(env, jKey)];
    (*env)->ReleaseStringChars(env, jKey, keyString);

    const jchar *productIdString = (*env)->GetStringChars(env, jProductId, NULL);
    NSString *productId = [NSString stringWithCharacters:(UniChar *)productIdString length:(*env)->GetStringLength(env, jProductId)];
    (*env)->ReleaseStringChars(env, jProductId, productIdString);

    AttachLog(@"Purchasing product %@ with key %@", productId, key);
    [orders setObject:productId forKey:key];
    [_InAppBilling purchaseProduct:productId];
}

void ready(BOOL value) {
    (*env)->CallStaticVoidMethod(env, mat_jInAppBillingServiceClass, mat_jInAppBillingService_setInAppReady, (value) ? JNI_TRUE : JNI_FALSE);
}

void sendProduct(SKProduct *product) {
    const char *product0Chars = [product.productIdentifier UTF8String];
    jstring arg0 = (*env)->NewStringUTF(env, product0Chars);
    const char *product1Chars = [product.localizedTitle UTF8String];
    jstring arg1 = (*env)->NewStringUTF(env, product1Chars);
    const char *product2Chars = [product.localizedDescription UTF8String];
    jstring arg2 = (*env)->NewStringUTF(env, product2Chars);
    [numberFormatter setLocale:product.priceLocale];
    const char *product3Chars = [[numberFormatter stringFromNumber:product.price] UTF8String];
    jstring arg3 = (*env)->NewStringUTF(env, product3Chars);
    const char *product4Chars = [product.priceLocale.currencyCode UTF8String];
    jstring arg4 = (*env)->NewStringUTF(env, product4Chars);
    (*env)->CallStaticVoidMethod(env, mat_jInAppBillingServiceClass, mat_jInAppBillingService_setProduct, arg0, arg1, arg2, arg3, arg4);
    (*env)->DeleteLocalRef(env, arg0);
    (*env)->DeleteLocalRef(env, arg1);
    (*env)->DeleteLocalRef(env, arg2);
    (*env)->DeleteLocalRef(env, arg3);
    (*env)->DeleteLocalRef(env, arg4);

    AttachLog(@"Finished sending product");
}

void doneFetching(BOOL value)
{
    (*env)->CallStaticVoidMethod(env, mat_jInAppBillingServiceClass, mat_jInAppBillingService_doneFetching, (value) ? JNI_TRUE : JNI_FALSE);
}

void sendPurchases(NSArray *purchasedIDs) 
{
    int size = [purchasedIDs count];
    jobjectArray ret = (*env)->NewObjectArray(env, size, (*env)->FindClass(env, "java/lang/String"), NULL);
  
    int i;
    for (i = 0; i < size; i++) 
    {
        const char *purchasedID = [purchasedIDs[i] UTF8String];
        (*env)->SetObjectArrayElement(env, ret, i, (*env)->NewStringUTF(env, purchasedID));
    }
    (*env)->CallStaticVoidMethod(env, mat_jInAppBillingServiceClass, mat_jInAppBillingService_restorePurchases, ret);
    (*env)->DeleteLocalRef(env, ret);
}

void sendPurchase(NSString *purchasedID, NSString *transactionId, NSString *transactionReceipt)
{
    AttachLog(@"Sending purchase %@", purchasedID);
    NSString* key;
    for (NSString* k in orders)
    {
        NSString *v = [orders objectForKey:k];
        if ([v isEqualToString:purchasedID])
        {
            key = k;
            break;
        }
    }
    if (!key) 
    {
        AttachLog(@"Error retrieving key from orders for product %@", purchasedID);
        return;
    }

    const char *keyChars = [key UTF8String];
    jstring arg0 = (*env)->NewStringUTF(env, keyChars);
    [orders removeObjectForKey:key];
    const char *productIdChars = [purchasedID UTF8String];
    jstring arg1 = (*env)->NewStringUTF(env, productIdChars);
    const char *transactionIdChars = [transactionId UTF8String];
    jstring arg2 = (*env)->NewStringUTF(env, transactionIdChars);
    const char *transactionReceiptChars = [transactionReceipt UTF8String];
    jstring arg3 = (*env)->NewStringUTF(env, transactionReceiptChars);
    (*env)->CallStaticVoidMethod(env, mat_jInAppBillingServiceClass, mat_jInAppBillingService_setPurchase, arg0, arg1, arg2, arg3);
    (*env)->DeleteLocalRef(env, arg0);
    (*env)->DeleteLocalRef(env, arg1);
    (*env)->DeleteLocalRef(env, arg2);
    (*env)->DeleteLocalRef(env, arg3);
}

@implementation InAppBilling

- (void) setup
{
    if (![SKPaymentQueue canMakePayments]) 
    {
        AttachLog(@"Can't make payments. Please enable In App Purchase in Settings");
        ready(NO);
    } 
    else 
    {
        [self logMessage:@"In App Purchase enabled"];

        [[SKPaymentQueue defaultQueue] addTransactionObserver:self];

        numberFormatter = [[NSNumberFormatter alloc] init];
        [numberFormatter setFormatterBehavior:NSNumberFormatterBehavior10_4];
        [numberFormatter setNumberStyle:NSNumberFormatterDecimalStyle];

        ready(YES);
    }
}

- (void) fetchProducts
{
    productsRequest = [[SKProductsRequest alloc] initWithProductIdentifiers:[NSSet setWithArray:arrayOfProductIds]];
    productsRequest.delegate = self;

    [self logMessage:@"Start fetching products"];
    [productsRequest start];
}

- (void) purchaseProduct:(NSString *)productId
{
    [self logMessage:@"Purchase product %@", productId];
    if ([arrayOfProducts count]) {
        [self logMessage:@"Available products %d", [arrayOfProducts count]];
        for (SKProduct *product in arrayOfProducts)
        {
            [self logMessage:@"Trying product %@", product.productIdentifier];
            if ([product.productIdentifier isEqualToString:productId]) 
            {
                [self logMessage:@"Start purchasing product %@", productId];
                SKPayment *payment = [SKPayment paymentWithProduct:product];
                [[SKPaymentQueue defaultQueue] addPayment:payment];
                break;
            }
        }
    }
    else 
    {
        [self logMessage:@"No products to purchase"];
    }
}

-(void)productsRequest:(SKProductsRequest *)request didReceiveResponse:(SKProductsResponse *)response
{

    arrayOfProducts = [[NSMutableArray alloc] init];

    NSArray *products = response.products;

    if (products.count != 0)
    {
        for (SKProduct *product in products)
        {
            [arrayOfProducts addObject:product];
            sendProduct(product);
        }
    } else {
        AttachLog(@"Products not found");
    }

    for (SKProduct *product in response.invalidProductIdentifiers)
    {
        [self logMessage:@"Invalid product found: %@", product];
    }

    [productsRequest release];
    
    // restoring purchases
    [[SKPaymentQueue defaultQueue] restoreCompletedTransactions];

}

// SKPaymentTransactionObserver methods
// called when the transaction status is updated
- (void)paymentQueue:(SKPaymentQueue *)queue updatedTransactions:(NSArray *)transactions
{
    NSString *state, *transactionIdentifier, *transactionReceipt, *productId;
    NSData *receiptData;

    [self logMessage:@"paymentQueue"];
    for (SKPaymentTransaction *transaction in transactions)
    {
        switch (transaction.transactionState)
        {
            case SKPaymentTransactionStatePurchased:
                [self logMessage:@"completeTransaction"];
                state = @"PaymentTransactionStatePurchased";
                transactionIdentifier = transaction.transactionIdentifier;
                receiptData = [NSData dataWithContentsOfURL:[[NSBundle mainBundle] appStoreReceiptURL]];
                if (!receiptData) {
                    transactionReceipt = @"No receipt";
                } else {
                    transactionReceipt = [receiptData base64EncodedStringWithOptions:0];
                }
                productId = transaction.payment.productIdentifier;
                [self logMessage:@"transaction state: %@ with id: %@ for product: %@", state, transactionIdentifier, productId];

                sendPurchase(productId, transactionIdentifier, transactionReceipt);

                [[SKPaymentQueue defaultQueue] finishTransaction:transaction];
                break;
            case SKPaymentTransactionStateFailed:
                [self logMessage:@"failedTransaction: error %d %@", transaction.error.code, transaction.error.localizedDescription];
                state = @"PaymentTransactionStateFailed";
                sendPurchase(@"", @"", @"");

                [[SKPaymentQueue defaultQueue] finishTransaction:transaction];
                break;
            case SKPaymentTransactionStateRestored:
                [self logMessage:@"restoreTransaction"];
                state = @"PaymentTransactionStateRestored";
                transactionIdentifier = transaction.originalTransaction.transactionIdentifier;
                receiptData = [NSData dataWithContentsOfURL:[[NSBundle mainBundle] appStoreReceiptURL]];
                if (!receiptData) {
                    transactionReceipt = @"No receipt";
                } else {
                    transactionReceipt = [receiptData base64EncodedStringWithOptions:0];
                }
                productId = transaction.originalTransaction.payment.productIdentifier;
                [self logMessage:@"transaction state: %@ with id: %@ for product: %@", state, transactionIdentifier, productId];
                
                [[SKPaymentQueue defaultQueue] finishTransaction:transaction];
                break;
            default:
                break;
        }

    }
}

- (void)paymentQueueRestoreCompletedTransactionsFinished:(SKPaymentQueue *)queue
{
    [self logMessage:@"paymentQueueRestoreCompletedTransactionsFinished"];
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    dateFormatter.dateStyle = NSDateFormatterMediumStyle;
    dateFormatter.timeStyle = NSDateFormatterShortStyle;

    NSMutableArray *purchasedItemIDs = [[NSMutableArray alloc] init];

    [self logMessage:@"received restored transactions: %li", queue.transactions.count];
    for (SKPaymentTransaction *transaction in queue.transactions)
    {
        if (transaction.transactionState == SKPaymentTransactionStateRestored || transaction.transactionState == SKPaymentTransactionStatePurchased) {
            NSString *productID = transaction.payment.productIdentifier;
            [self logMessage:@"product %@ purchased on %@", productID, [dateFormatter stringFromDate:transaction.transactionDate]];
            [purchasedItemIDs addObject:productID];
        } else {
            [self logMessage:@"Transaction for %@ failed, error: %d %@", transaction.payment.productIdentifier, transaction.error.code, transaction.error.localizedDescription];
        }
    }
    sendPurchases([purchasedItemIDs copy]);

    doneFetching(YES);

}

-(void)paymentQueue:(SKPaymentQueue *)queue restoreCompletedTransactionsFailedWithError:(NSError *)error
{
    AttachLog(@"restoreCompletedTransactionsFailedWithError %@", error);
    doneFetching(NO);
}

- (void) logMessage:(NSString *)format, ...;
{
    if (debugAttach) 
    {
        va_list args;
        va_start(args, format);
        NSLogv(format, args);
        va_end(args);
    }
}
@end
