package com.pixelcrater.Diaro.premium.amazon;

import com.amazon.device.iap.PurchasingListener;
import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.Product;
import com.amazon.device.iap.model.ProductDataResponse;
import com.amazon.device.iap.model.PurchaseResponse;
import com.amazon.device.iap.model.PurchaseUpdatesResponse;
import com.amazon.device.iap.model.Receipt;
import com.amazon.device.iap.model.UserDataResponse;
import com.pixelcrater.Diaro.config.GlobalConstants;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.AppLog;

import java.util.Map;

/**
 * Implementation of {@link PurchasingListener} that listens to Amazon
 * InAppPurchase SDK's events, and call {@link AmazonIapManager} to handle the
 * purchase business logic.
 */
public class AmazonPurchasingListener implements PurchasingListener {
    private final AmazonIapManager iapManager;

    public AmazonPurchasingListener(final AmazonIapManager iapManager) {
        this.iapManager = iapManager;
    }

    /**
     * This is the callback for {@link PurchasingService#getUserData}.
     * For successful case, load Amazon user's purchase information
     */
    @Override
    public void onUserDataResponse(final UserDataResponse response) {
        AppLog.d("response: " + response);

        final UserDataResponse.RequestStatus status = response.getRequestStatus();
//        AppLog.d("status: " + status);

        switch (status) {
            case SUCCESSFUL:
                AppLog.d("getUserId(): " + response.getUserData().getUserId() +
                        ", getMarketplace(): " + response.getUserData().getMarketplace());

                // Get user's purchases
                PurchasingService.getPurchaseUpdates(false);
                break;

            case FAILED:

            case NOT_SUPPORTED:
                AppLog.d("failed");
                break;
        }
    }

    /**
     * This is the callback for {@link PurchasingService#getProductData}.
     */
    @Override
    public void onProductDataResponse(final ProductDataResponse response) {
        AppLog.d("response: " + response);

        final ProductDataResponse.RequestStatus status = response.getRequestStatus();
//        AppLog.d("status: " + status);

        switch (status) {
            case SUCCESSFUL:
                AppLog.d("SUCCESSFUL. The item data map in this response includes the valid SKUs");

//                if (response.getUnavailableSkus().contains(MyPurchases.MANAGED_PURCHASE_PRO_VERSION)) {
//                    iapManager.mMyPurchases.inAppSupported = false;
//                }

                // Update button price
                final Map<String, Product> products = response.getProductData();
                AppLog.d("products: " + products);

                String price = "-";

                for (final String key : products.keySet()) {
                    AppLog.d("key: " + key);

                    Product product = products.get(key);
                    AppLog.d(String.format("Product: %s\nType: %s\nSKU: %s\nPrice: %s\n" +
                                    "Description: %s\n", product.getTitle(), product.getProductType(),
                            product.getSku(), product.getPrice(), product.getDescription()));

                    if (product.getSku().equals(GlobalConstants.MANAGED_PURCHASE_PRO_VERSION)) {
                        price = product.getPrice();
                    }
                }

                // Show price in UI
                //  iapManager.mMyPurchases.buttonPrice = ": " + price;
                break;

            case FAILED:

            case NOT_SUPPORTED:
                AppLog.d("failed, should retry request");
               // iapManager.mMyPurchases.inAppSupported = false;
                break;
        }

        // Send broadcast to update get pro activity
        Static.sendBroadcast(Static.BR_IN_GET_PRO, Static.DO_UPDATE_UI, null);
    }

    /**
     * This is the callback for {@link PurchasingService#getPurchaseUpdates}.
     * <p/>
     * You will receive Entitlement receipts from this callback.
     */
    @Override
    public void onPurchaseUpdatesResponse(final PurchaseUpdatesResponse response) {
        AppLog.d("response: " + response);

        final PurchaseUpdatesResponse.RequestStatus status = response.getRequestStatus();
//        AppLog.d("status: " + status);

        switch (status) {
            case SUCCESSFUL:
                for (final Receipt receipt : response.getReceipts()) {
                    AppLog.d("receipt: " + receipt);

                    iapManager.handleReceipt(response.getRequestId().toString(), receipt,
                            response.getUserData());
                }
                if (response.hasMore()) {
                    PurchasingService.getPurchaseUpdates(false);
                }
                break;

            case FAILED:

            case NOT_SUPPORTED:
                AppLog.d("failed, should retry request");
                break;
        }
    }

    /**
     * This is the callback for {@link PurchasingService#purchase}.
     * For each time the application sends a purchase request
     * {@link PurchasingService#purchase}, Amazon Appstore will call this
     * callback when the purchase request is completed. If the RequestStatus is
     * Successful or AlreadyPurchased then application needs to call
     * {@link AmazonIapManager#handleReceipt} to handle the purchase
     * fulfillment. If the RequestStatus is INVALID_SKU, NOT_SUPPORTED, or
     * FAILED, notify corresponding method of {@link AmazonIapManager} .
     */
    @Override
    public void onPurchaseResponse(final PurchaseResponse response) {
        final PurchaseResponse.RequestStatus status = response.getRequestStatus();
        AppLog.d("response: " + response);

        final Receipt receipt = response.getReceipt();

        switch (status) {
            case SUCCESSFUL:
                iapManager.handleReceipt(response.getRequestId().toString(), receipt,
                        response.getUserData());
                break;

            case ALREADY_PURCHASED:
                AppLog.d("already purchased, you should verify the entitlement purchase on your " +
                        "side and make sure the purchase was granted to customer");
                iapManager.handleReceipt(response.getRequestId().toString(), receipt,
                        response.getUserData());
                break;

            case INVALID_SKU:
                AppLog.d("invalid SKU! onProductDataResponse should have disabled buy button already.");
                break;

            case FAILED:

            case NOT_SUPPORTED:
                AppLog.d("failed");
                iapManager.purchaseFailed();
                break;
        }

    }

}
