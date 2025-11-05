package com.pixelcrater.Diaro.premium.amazon;

import android.widget.Toast;

import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.FulfillmentResult;
import com.amazon.device.iap.model.Receipt;
import com.amazon.device.iap.model.UserData;
import com.pixelcrater.Diaro.config.GlobalConstants;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.AppLog;

import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.Set;

/**
 * This is a sample of how an application may handle InAppPurchasing.
 * The major functions includes
 * <ul>
 * <li>Simple user and order history management</li>
 * <li>Enable/disable purchases from GUI</li>
 * <li>Save persistent order data</li>
 * </ul>
 */
public class AmazonIapManager {

    public AmazonIapManager() {

        AppLog.d("PurchasingService.IS_SANDBOX_MODE: " + PurchasingService.IS_SANDBOX_MODE);

        final AmazonPurchasingListener purchasingListener = new AmazonPurchasingListener(this);
        PurchasingService.registerListener(MyApp.getInstance().getApplicationContext(), purchasingListener);

        // Get in-app item data to show price in UI
        final Set<String> productSkus = new HashSet<>();
        productSkus.add(GlobalConstants.MANAGED_PURCHASE_PRO_VERSION);
        PurchasingService.getProductData(productSkus);

        // Get user data and user's purchases
        PurchasingService.getUserData();
    }

    /**
     * Method to handle receipts
     */
    public void handleReceipt(final String requestId, final Receipt receipt, final UserData userData) {
        AppLog.d("requestId: " + requestId + ", receipt: " + receipt + ", userData: " + userData);

        switch (receipt.getProductType()) {
            case CONSUMABLE:
                // check consumable sample for how to handle consumable purchases
                break;

            case ENTITLED:
                handleEntitlementPurchase(receipt, userData);
                break;

            case SUBSCRIPTION:
                // check subscription sample for how to handle consumable purchases
                break;
        }
    }

    /**
     * Show purchase failed message
     */
    public void purchaseFailed() {
        Static.showToast(MyApp.getInstance().getString(R.string.errorInPurchase),
                Toast.LENGTH_SHORT);
    }

    /**
     * This method contains the business logic to fulfill the customer's
     * purchase based on the receipt received from InAppPurchase SDK's
     * {@link PurchasingListener#onPurchaseResponse} or
     * {@link PurchasingListener#onPurchaseUpdatesResponse} method.
     */
    private void handleEntitlementPurchase(final Receipt receipt, final UserData userData) {
        AppLog.d("receipt: " + receipt + ", userData: " + userData);

        try {
            // Save the entitlement purchase record to server
            saveEntitlementPurchase(receipt, userData.getUserId());
            // Notify Amazon Appstore that purchase record has been fulfilled
            PurchasingService.notifyFulfillment(receipt.getReceiptId(), FulfillmentResult.FULFILLED);
        } catch (final Throwable e) {
            // If for any reason the app is not able to fulfill the purchase,
            // add your own error handling code here.
            AppLog.d("Failed to grant entitlement purchase, with error " + e.getMessage());
        }
    }

    /**
     * Use Receipt.isCanceled() to determine whether the receipt is in a "CANCELED" state
     */
    private void saveEntitlementPurchase(final Receipt receipt, final String userId) {
        // Replace with your own implementation
        final long purchaseDate = receipt.getPurchaseDate() != null ?
                receipt.getPurchaseDate().getTime() : -1;
        AppLog.d("purchaseDate: " + purchaseDate);

        final long cancelDate = receipt.isCanceled() ?
                receipt.getCancelDate().getTime() : -1;
        AppLog.d("cancelDate: " + cancelDate);

        // If signed in
        if (MyApp.getInstance().userMgr.isSignedIn()) {
            // Send payment transaction information to API
            String date = new DateTime().toString("yyyy.MM.dd HH:mm:ss");
            sendAmazonInAppPaymentToAPI(userId, date, receipt);
        }

        if (receipt.isCanceled()) {
            // Turn OFF PRO
            Static.turnOffPro();
        } else {
            // Turn ON PRO
            Static.turnOnPro();

            Static.showToast(MyApp.getInstance().getString(R.string.pro_version_active),
                    Toast.LENGTH_SHORT);
        }
    }


    public  void sendAmazonInAppPaymentToAPI(String userId, String date, Receipt receipt) {
//		AppLog.d("userId: " + userId + ", date: " + date + ", receipt: " + receipt);

        // If not signed in or missing payment information, return
        if (!MyApp.getInstance().userMgr.isSignedIn() || userId == null || receipt == null) {
            return;
        }

        String price = "5.99"; //item.getPrice();
        String currencyCode = "USD";
        AppLog.d("price: " + price + ", currencyCode: " + currencyCode);

        String system = GlobalConstants.PAYMENT_SYSTEM_ANDROID;
        String type = GlobalConstants.PAYMENT_TYPE_AMAZON;
        String email = userId;
        String product = GlobalConstants.PAYMENT_PRODUCT_DIARO_PRO;
        String description = "";//item.getDescription();
        String orderId = receipt.getReceiptId();
        String token ="";

        // Get purchase state
        String refunded = receipt.isCanceled() ? "1" : "0";

        String sku = receipt.getSku();

        // Send PRO payment to API
        MyApp.getInstance().asyncsMgr.executeSendPaymentAsync(sku, date, system, type, email, product, token, description, price, currencyCode, orderId, refunded);
    }

}
