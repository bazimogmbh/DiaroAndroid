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
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.pixelcrater.Diaro.premium.billing.BillingUpdateListener;
import com.pixelcrater.Diaro.utils.AppLog;

import java.util.List;

public class TypeBillingActivity extends TypeActivity implements PurchasesUpdatedListener, BillingClientStateListener, SkuDetailsResponseListener {

    private static final String TAG = "TypeBillingActivity";
    private BillingClient mBillingClient;
    private BillingUpdateListener eventHandler;

    public void setBillingHandler(BillingUpdateListener eventHandler){
        this.eventHandler = eventHandler;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBillingClient = BillingClient.newBuilder(this).setListener(this).enablePendingPurchases().build();
        if (!mBillingClient.isReady()) {
            mBillingClient.startConnection(this);
        }

    }

    // ---------------------- SETUP  ----------------------
    @Override
    public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
        int responseCode = billingResult.getResponseCode();
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

    // ---------------------- QUERY AVAILABLE PRODUCTS ----------------------
    /**
     * In order to make purchases, you need the {@link SkuDetails} for the item or subscription.
     * This is an asynchronous call that will receive a result in {@link #onSkuDetailsResponse}.
     */
    public void querySkuDetails(List<String> inAppsList, String skuType) {
        if( !inAppsList.isEmpty()){
            SkuDetailsParams inAppsParams = SkuDetailsParams.newBuilder().setType(skuType).setSkusList(inAppsList).build();
            mBillingClient.querySkuDetailsAsync(inAppsParams, this);
        }

    }

    @Override
    public void onSkuDetailsResponse(@NonNull BillingResult billingResult, @Nullable List<SkuDetails> skuDetailsList) {
        if (billingResult == null) {
            Log.wtf(TAG, "onSkuDetailsResponse: null BillingResult");
            return;
        }

        int responseCode = billingResult.getResponseCode();
        String debugMessage = billingResult.getDebugMessage();

        switch (responseCode) {
            case BillingClient.BillingResponseCode.OK:
                if (skuDetailsList == null) {
                    AppLog.e( "onSkuDetailsResponse: null SkuDetails list");
                } else {
                   if(eventHandler !=null){
                       eventHandler.onAvailableProductsResponse(skuDetailsList);
                   }
                }
                break;
            case BillingClient.BillingResponseCode.SERVICE_DISCONNECTED:
            case BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE:
            case BillingClient.BillingResponseCode.BILLING_UNAVAILABLE:
            case BillingClient.BillingResponseCode.ITEM_UNAVAILABLE:
            case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
            case BillingClient.BillingResponseCode.ERROR:
                AppLog.e("onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                break;
            case BillingClient.BillingResponseCode.USER_CANCELED:
                AppLog.e( "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
                break;
            // These response codes are not expected.
            case BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED:
            case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
            case BillingClient.BillingResponseCode.ITEM_NOT_OWNED:
            default:
                Log.wtf(TAG, "onSkuDetailsResponse: " + responseCode + " " + debugMessage);
        }
    }

    // ----------- QUERY PURCHASES  ---------------
    /**
     * Query Google Play Billing for existing purchases.
     * <p>
     * New purchases will be provided to the PurchasesUpdatedListener.
     * You still need to check the Google Play Billing API to know when purchase tokens are removed.
     */
    public void queryPurchases(String skuType) {
        if (!mBillingClient.isReady()) {
            AppLog.e("queryPurchases: BillingClient is not ready");
        }
        AppLog.e( "queryPurchases");

        mBillingClient.queryPurchasesAsync(skuType, (billingResult, list) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK){
                if(eventHandler!=null){
                    eventHandler.onOwnedProductsResponse(list);
                }
            }
        });
    }

    // ----------- PURCHASE  ---------------
    public int launchBillingFlow(BillingFlowParams params) {
     //   Log.e(TAG, "launchBillingFlow: sku: " + params.getSkus() );
        if (!mBillingClient.isReady()) {
            Log.e(TAG, "launchBillingFlow: BillingClient is not ready");
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
        if (billingResult == null) {
            Log.wtf(TAG, "onPurchasesUpdated: null BillingResult");
            return;
        }
        int responseCode = billingResult.getResponseCode();
        String debugMessage = billingResult.getDebugMessage();
        AppLog.e("onPurchasesUpdated: " +  responseCode  + ", " + debugMessage);
        switch (responseCode) {
            case BillingClient.BillingResponseCode.OK:
                if (purchases == null) {
                    AppLog.e( "onPurchasesUpdated: null purchase list");
                } else {
                    AppLog.e( "onPurchasesUpdated: successful");
                    if(eventHandler!=null){
                        eventHandler.onPurchase(purchases);
                    }
                }
                break;
            case BillingClient.BillingResponseCode.USER_CANCELED:
                AppLog.e("onPurchasesUpdated: User canceled the purchase");
                break;
            case BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED:
                AppLog.e( "onPurchasesUpdated: The user already owns this item");
                break;
            case BillingClient.BillingResponseCode.DEVELOPER_ERROR:
                AppLog.e( "onPurchasesUpdated: Developer error means that Google Play does not recognize the configuration. ");
                break;
        }
    }

    /**
     * Acknowledge a purchase.
     * <p>
     * https://developer.android.com/google/play/billing/billing_library_releases_notes#2_0_acknowledge
     * <p>
     * Apps should acknowledge the purchase after confirming that the purchase token
     * has been associated with a user. This app only acknowledges purchases after
     * successfully receiving the subscription data back from the server.
     * <p>
     * Developers can choose to acknowledge purchases from a server using the
     * Google Play Developer API. The server has direct access to the user database,
     * so using the Google Play Developer API for acknowledgement might be more reliable.
     * TODO(134506821): Acknowledge purchases on the server.
     * <p>
     * If the purchase token is not acknowledged within 3 days,
     * then Google Play will automatically refund and revoke the purchase.
     * This behavior helps ensure that users are not charged for subscriptions unless the
     * user has successfully received access to the content.
     * This eliminates a category of issues where users complain to developers
     * that they paid for something that the app is not giving to them.
     */
    public void acknowledgePurchase(String purchaseToken) {
        AppLog.e("Acknowledging purchase with token : " + purchaseToken);
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


    // ---------------------- DESTROY  ----------------------
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBillingClient != null) {
            if (mBillingClient.isReady()) {
                AppLog.e("ending billing connection..");
                mBillingClient.endConnection();
            }
        }
    }

    @Override
    public void onBillingServiceDisconnected() {
        AppLog.e("onBillingServiceDisconnected");
    }
}
