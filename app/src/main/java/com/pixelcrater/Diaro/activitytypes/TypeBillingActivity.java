package com.pixelcrater.Diaro.activitytypes;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryProductDetailsResult;
import com.android.billingclient.api.QueryPurchasesParams;
import com.pixelcrater.Diaro.config.GlobalConstants;
import com.pixelcrater.Diaro.premium.billing.BillingUpdateListener;
import com.pixelcrater.Diaro.premium.billing.Security;
import com.pixelcrater.Diaro.utils.AppLog;

import java.util.ArrayList;
import java.util.List;

public class TypeBillingActivity extends TypeActivity implements PurchasesUpdatedListener, ProductDetailsResponseListener {

    private static final String TAG = "TypeBillingActivity";
    private BillingClient mBillingClient;
    private BillingUpdateListener eventHandler;

    public void setBillingHandler(BillingUpdateListener eventHandler){
        this.eventHandler = eventHandler;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBillingClient = BillingClient.newBuilder(this)
                .setListener(this)
                .enablePendingPurchases(
                        PendingPurchasesParams.newBuilder()
                                .enableOneTimeProducts()
                                .enablePrepaidPlans()  // Added in v8.1.0 for prepaid subscriptions
                                .build()
                )
                .enableAutoServiceReconnection()  // Automatically reconnects if connection is lost
                .build();

        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                int responseCode = billingResult.getResponseCode();
                // The BillingClient is ready. You can query purchases here.
                if (responseCode == BillingClient.BillingResponseCode.OK) {
                    if(eventHandler!=null){
                        eventHandler.onBillingInitialized();
                    }
                } else  {
                    if(eventHandler !=null){
                        eventHandler.onBillingUnavailable(billingResult.getDebugMessage(), responseCode);
                    }
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Auto-reconnection is handled by enableAutoServiceReconnection()
                AppLog.e("onBillingServiceDisconnected");
            }
        });
    }

    // ---------------------- QUERY AVAILABLE PRODUCTS ----------------------
    /**
     * In order to make purchases, you need the {@link ProductDetails} for the item or subscription.
     * This is an asynchronous call that will receive a result in {@link #onProductDetailsResponse}.
     */
    public void querySkuDetails(List<String> inAppsList, String productType) {
        if( !inAppsList.isEmpty()){
            // Build product list
            List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
            for (String productId : inAppsList) {
                productList.add(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(productType)
                        .build()
                );
            }

            QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                    .setProductList(productList)
                    .build();

            mBillingClient.queryProductDetailsAsync(params, this);
        }

    }


    // ----------- QUERY PURCHASES  ---------------
    /**
     * Query Google Play Billing for existing purchases.
     * <p>
     * New purchases will be provided to the PurchasesUpdatedListener.
     * You still need to check the Google Play Billing API to know when purchase tokens are removed.
     */
    public void queryPurchases(String productType) {
        AppLog.e( "queryPurchases");

        QueryPurchasesParams params = QueryPurchasesParams.newBuilder()
                .setProductType(productType)
                .build();

        mBillingClient.queryPurchasesAsync(params, (billingResult, list) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                if(eventHandler!=null){
                    eventHandler.onOwnedProductsResponse(list);
                }
            }
        });
    }

    // ----------- PURCHASE  ---------------
    public int launchBillingFlow(BillingFlowParams params) {
        if (!mBillingClient.isReady()) {
            Log.e(TAG, "launchBillingFlow: BillingClient is not ready");
            return BillingClient.BillingResponseCode.SERVICE_DISCONNECTED;
        }
        BillingResult billingResult = mBillingClient.launchBillingFlow(this, params);
        int responseCode = billingResult.getResponseCode();
        String debugMessage = billingResult.getDebugMessage();
        Log.d(TAG, "launchBillingFlow: BillingResponse " + responseCode + " " + debugMessage);
        return responseCode;
    }

    // This receives updates for all purchases in your app.
    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        int responseCode = billingResult.getResponseCode();
        String debugMessage = billingResult.getDebugMessage();
        AppLog.e("onPurchasesUpdated: " +  responseCode  + ", " + debugMessage);

        if (responseCode == BillingClient.BillingResponseCode.OK) {
            if (purchases == null) {
                AppLog.e("onPurchasesUpdated: null purchase list");
            } else {
                AppLog.e("onPurchasesUpdated: successful, verifying signatures");

                // ✅ SECURITY: Verify purchase signatures
                List<Purchase> verifiedPurchases = new ArrayList<>();
                String base64PublicKey = getBase64PublicKey();

                for (Purchase purchase : purchases) {
                    if (Security.verifyPurchase(base64PublicKey, purchase.getOriginalJson(), purchase.getSignature())) {
                        verifiedPurchases.add(purchase);
                        AppLog.e("Purchase signature VERIFIED for: " + purchase.getOrderId());
                    } else {
                        AppLog.e("SECURITY ALERT: Purchase signature INVALID for: " + purchase.getOrderId());
                        // Don't process invalid purchases - potential fraud
                    }
                }

                if (!verifiedPurchases.isEmpty() && eventHandler != null) {
                    eventHandler.onPurchase(verifiedPurchases);
                } else if (verifiedPurchases.isEmpty()) {
                    AppLog.e("No verified purchases to process");
                }
            }
        } else if (responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            AppLog.e("onPurchasesUpdated: User canceled the purchase");
        } else if (responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            AppLog.e("onPurchasesUpdated: The user already owns this item");
        } else if (responseCode == BillingClient.BillingResponseCode.DEVELOPER_ERROR) {
            AppLog.e("onPurchasesUpdated: Developer error means that Google Play does not recognize the configuration. ");
        }
    }


    public void acknowledgePurchase(String purchaseToken) {
        AppLog.e("Acknowledging purchase");
        AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchaseToken).build();
        mBillingClient.acknowledgePurchase(params, new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
                int responseCode = billingResult.getResponseCode();
                String debugMessage = billingResult.getDebugMessage();
                AppLog.e( "Acknowledged purchase : " + responseCode + " " + debugMessage);
            }
        });
    }

    /**
     * Gets the base64-encoded public key for purchase signature verification.
     * @return The public key string from GlobalConstants
     */
    private String getBase64PublicKey() {
        return GlobalConstants.base64EncodedPublicKey;
    }

    // ---------------------- DESTROY  ----------------------
    @Override
    protected void onDestroy() {
        super.onDestroy();
        eventHandler = null;
        if (mBillingClient != null) {
            if (mBillingClient.isReady()) {
                AppLog.e("ending billing connection..");
                mBillingClient.endConnection();
            }
            mBillingClient = null;
        }
    }


    @Override
    public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull QueryProductDetailsResult queryProductDetailsResult) {
        int responseCode = billingResult.getResponseCode();
        String debugMessage = billingResult.getDebugMessage();

        if (responseCode == BillingClient.BillingResponseCode.OK) {
            List<ProductDetails> productDetailsList = queryProductDetailsResult.getProductDetailsList();
            if (productDetailsList == null || productDetailsList.isEmpty()) {
                AppLog.e("onProductDetailsResponse: null or empty ProductDetails list");
            } else {
                if (eventHandler != null) {
                    eventHandler.onAvailableProductsResponse(productDetailsList);
                }
            }
        } else if (responseCode == BillingClient.BillingResponseCode.SERVICE_DISCONNECTED
                || responseCode == BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE
                || responseCode == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE
                || responseCode == BillingClient.BillingResponseCode.ITEM_UNAVAILABLE
                || responseCode == BillingClient.BillingResponseCode.DEVELOPER_ERROR
                || responseCode == BillingClient.BillingResponseCode.ERROR) {
            AppLog.e("onProductDetailsResponse: " + responseCode + " " + debugMessage);
        } else if (responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            AppLog.e("onProductDetailsResponse: " + responseCode + " " + debugMessage);
        } else {
            // These response codes are not expected.
            // FEATURE_NOT_SUPPORTED, ITEM_ALREADY_OWNED, ITEM_NOT_OWNED, etc.
            Log.wtf(TAG, "onProductDetailsResponse: " + responseCode + " " + debugMessage);
        }
    }
}
