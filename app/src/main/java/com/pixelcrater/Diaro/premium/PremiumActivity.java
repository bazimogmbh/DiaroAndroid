package com.pixelcrater.Diaro.premium;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.amazon.device.iap.PurchasingService;
import com.amazon.device.iap.model.RequestId;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.activitytypes.TypeBillingActivity;
import com.pixelcrater.Diaro.config.AppConfig;
import com.pixelcrater.Diaro.config.GlobalConstants;
import com.pixelcrater.Diaro.premium.billing.BillingUpdateListener;
import com.pixelcrater.Diaro.premium.billing.PaymentUtils;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.Static;

import java.text.DecimalFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

// Upgrade To Premium
public class PremiumActivity extends TypeBillingActivity implements BillingUpdateListener {

    public LinearLayout buttonBuy1Month, buttonBuy3Months, buttonBuy1Year;

    TextView tv_buy_pro_button1, tv_buy_pro_button2, tv_buy_pro_button3;
    TextView tv_buy_pro_button1_info, tv_buy_pro_button2_info, tv_buy_pro_button3_info;
    TextView tv_buy_pro_button3_info2;

    TextView tvSubscriptionInfo;

    Button thank_you_pro;
    ImageView close;

    private final BroadcastReceiver brReceiver = new BrReceiver();

    private final HashMap<String, ProductDetails> mProductsMap = new HashMap<>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.color_blue2)); //status bar or the time bar at the top

        setBillingHandler(this);

        setContentView(addViewToContentContainer(R.layout.pro_responsive));

        getSupportActionBar().hide();

        thank_you_pro = findViewById(R.id.thank_you_pro);
        tvSubscriptionInfo = findViewById(R.id.tvSubscriptionInfo);

        tv_buy_pro_button1 = findViewById(R.id.tv_buy_pro_button1);
        tv_buy_pro_button2 = findViewById(R.id.tv_buy_pro_button2);
        tv_buy_pro_button3 = findViewById(R.id.tv_buy_pro_button3);

        tv_buy_pro_button1_info = findViewById(R.id.tv_buy_pro_button1_info);
        tv_buy_pro_button2_info = findViewById(R.id.tv_buy_pro_button2_info);
        tv_buy_pro_button3_info = findViewById(R.id.tv_buy_pro_button3_info);

        tv_buy_pro_button3_info2 = findViewById(R.id.tv_buy_pro_button3_info2);
        //  tv_buy_pro_button3_info2.setPaintFlags(tv_buy_pro_button3_info2.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        // Buy PRO button
        buttonBuy1Month = findViewById(R.id.buy_pro_button1);
        buttonBuy1Month.setOnClickListener(v -> startPurchaseFlow(GlobalConstants.SUBSCRIPTION_PURCHASE_PREMIUM_MONTHLY));

        buttonBuy3Months = findViewById(R.id.buy_pro_button2);
        buttonBuy3Months.setOnClickListener(v -> startPurchaseFlow(GlobalConstants.SUBSCRIPTION_PURCHASE_PREMIUM_3_MONTHLY));

        buttonBuy1Year = findViewById(R.id.buy_pro_button3);
        buttonBuy1Year.setOnClickListener(v -> startPurchaseFlow(GlobalConstants.SUBSCRIPTION_PURCHASE_PREMIUM_YEARLY));

        close = findViewById(R.id.iv_close);
        close.setOnClickListener(view -> finish());

        // Handle window insets for edge-to-edge on Android 15+
        setupWindowInsets();

        // Register broadcast receiver
        ContextCompat.registerReceiver(this, brReceiver, new IntentFilter(Static.BR_IN_GET_PRO), ContextCompat.RECEIVER_NOT_EXPORTED);

        if (Static.isPlayNboSubscription()) {
            showProActive();
        }
    }

    private void showProActive() {
        runOnUiThread(() -> {
            buttonBuy1Month.setVisibility(View.GONE);
            buttonBuy3Months.setVisibility(View.GONE);
            buttonBuy1Year.setVisibility(View.GONE);

            tvSubscriptionInfo.setText(R.string.diaro_pro_version);

            thank_you_pro.setVisibility(View.VISIBLE);
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (activityState.isActivityPaused)
            return true;

        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(brReceiver);
    }

    /**
     * Sets up window insets for edge-to-edge display on Android 15+
     */
    private void setupWindowInsets() {
        // Handle top inset for close button (16dp original margin)
        applyTopInsetsAsMargin(close, 16);

        // Handle bottom inset for bottom layout (20dp original padding)
        applyBottomInsetsWithPadding(findViewById(R.id.bottomLayout), 20);
    }

    /**
     * Updates UI depending if PRO is purchased or not
     */
    public void updateUi() {

        // - Turn on/off PRO button -
        // If Subscribed
        if (Static.isSubscribedCurrently() || Static.isPlayNboSubscription()) {
            activityState.setActionBarTitle(Objects.requireNonNull(getSupportActionBar()), getString(R.string.diaro_pro_version));
            showProActive();
        } else {
            activityState.setActionBarTitle(Objects.requireNonNull(getSupportActionBar()), getString(R.string.get_diaro_pro));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        activityState.onActivityResult(requestCode, resultCode, data); // needed for hiding SecurityCode activity
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startPurchaseFlow(String sku) {
        // Google Play
        if (mProductsMap.isEmpty())
            return;

        ProductDetails productDetails = mProductsMap.get(sku);
        if (productDetails != null && productDetails.getSubscriptionOfferDetails() != null &&
                !productDetails.getSubscriptionOfferDetails().isEmpty()) {

            ProductDetails.SubscriptionOfferDetails offerDetails =
                    productDetails.getSubscriptionOfferDetails().get(0);
            String offerToken = offerDetails.getOfferToken();

            BillingFlowParams.ProductDetailsParams productDetailsParams =
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(offerToken)
                            .build();

            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(java.util.Collections.singletonList(productDetailsParams))
                    .build();

            launchBillingFlow(flowParams);
        }

    }

    @Override
    public void onBillingInitialized() {
        querySkuDetails(GlobalConstants.activeSubscriptionsList, BillingClient.ProductType.SUBS);
        queryPurchases(BillingClient.ProductType.SUBS);
    }

    @Override
    public void onBillingUnavailable(String debugMessage, int responseCode) {
        AppLog.e(debugMessage + " , code : " + responseCode);

        //   proVersionText.setText(R.string.error_inapp_billing_not_supported);
        if (MyApp.getInstance().userMgr.isSignedIn()) {
            MyApp.getInstance().asyncsMgr.executeCheckProAsync(MyApp.getInstance().userMgr.getSignedInEmail());
        }
    }

    @Override
    public void onAvailableProductsResponse(@NonNull List<ProductDetails> availableProductsSkuList) {
        AppLog.e("Available products count -> " + availableProductsSkuList.size());

        for (ProductDetails productDetails : availableProductsSkuList) {
            String productId = productDetails.getProductId();
            AppLog.e("Product -> " + productId);

            if (mProductsMap.get(productId) == null) {
                mProductsMap.put(productId, productDetails);
            }

            // Get subscription offer details (for subscriptions)
            if (productDetails.getSubscriptionOfferDetails() != null &&
                    !productDetails.getSubscriptionOfferDetails().isEmpty()) {

                ProductDetails.SubscriptionOfferDetails offerDetails =
                        productDetails.getSubscriptionOfferDetails().get(0);

                ProductDetails.PricingPhase pricingPhase =
                        offerDetails.getPricingPhases().getPricingPhaseList().get(0);

                String formattedPrice = pricingPhase.getFormattedPrice();
                String priceCurrencyCode = pricingPhase.getPriceCurrencyCode();
                Currency currency = Currency.getInstance(priceCurrencyCode);
                String currencySymbol = currency.getSymbol();

                float totalPrice = pricingPhase.getPriceAmountMicros() / 1000000.0f;

                runOnUiThread(() -> {
                    double pricePerMonth;
                    // Monthly
                    if (productId.equals(GlobalConstants.SUBSCRIPTION_PURCHASE_PREMIUM_MONTHLY)) {
                        pricePerMonth = totalPrice;

                        String pricePerMonthString = getPricePerMonthString(pricePerMonth, currencySymbol) + "/" + getString(R.string.month);
                        tv_buy_pro_button1.setText(pricePerMonthString);
                        tv_buy_pro_button1_info.setText(getString(R.string.subscription_billed_monthly));
                    }

                    // Quarterly
                    if (productId.equals(GlobalConstants.SUBSCRIPTION_PURCHASE_PREMIUM_3_MONTHLY)) {
                        pricePerMonth = totalPrice / 3;

                        String pricePerMonthString = getPricePerMonthString(pricePerMonth, currencySymbol);
                        tv_buy_pro_button2.setText(pricePerMonthString);

                        String billedAtInfo = getString(R.string.subscription_billed_quarterly_at, currencySymbol + totalPrice);
                        tv_buy_pro_button2_info.setText(billedAtInfo);
                    }

                    // Yearly
                    if (productId.equals(GlobalConstants.SUBSCRIPTION_PURCHASE_PREMIUM_YEARLY)) {
                        pricePerMonth = totalPrice / 12;

                        String pricePerYearString = getPricePerMonthString(totalPrice, currencySymbol) + "/" + getString(R.string.year);
                        tv_buy_pro_button3.setText(pricePerYearString);

                        String billedAtInfo = getString(R.string.subscription_billed_annualy_at, currencySymbol + totalPrice);
                        tv_buy_pro_button3_info.setText(billedAtInfo);
                        //  tv_buy_pro_button3_info2.setText(String.format("(%s%s)", currencySymbol, totalPrice * 1.5));

                        tv_buy_pro_button3_info2.setText(String.format("(%s%s)", "Save", " 70%"));
                    }
                });
            }
        }

    }

    DecimalFormat df = new DecimalFormat("0.00");

    private String getPricePerMonthString(double pricePerMonth, String symbol) {

        String priceAmountPerMonthString = "";
        try {
            priceAmountPerMonthString = df.format(pricePerMonth);
        } catch (Exception e) {
            priceAmountPerMonthString = Math.round(pricePerMonth * 100.0) / 100.0 + "";
        }

        if (priceAmountPerMonthString.length() > 5) {
            priceAmountPerMonthString = priceAmountPerMonthString.substring(0, 5);
        }

        return symbol + priceAmountPerMonthString;
    }

    @Override
    public void onOwnedProductsResponse(@NonNull List<Purchase> ownedProductsList) {
        AppLog.e("Owned products count -> " + ownedProductsList.size());
        processPurchases(ownedProductsList);
    }

    @Override
    public void onPurchase(@NonNull List<Purchase> purchasesList) {
        AppLog.e("Purchase products count -> " + purchasesList.size());

        runOnUiThread(() -> Static.showToastLong(MyApp.getInstance().getString(R.string.pro_version_active)));

        processPurchases(purchasesList);
    }

    private void processPurchases(@NonNull List<Purchase> purchasesList) {
        if (purchasesList.size() > 0) {
            // PURCHASES FOUND
            for (Purchase purchase : purchasesList) {
                AppLog.e("processPurchases " + purchase.getProducts().get(0) + " -> " + purchase);

                ProductDetails productDetails = mProductsMap.get(purchase.getProducts().get(0));

                // Handle purchase based on state
                switch (purchase.getPurchaseState()) {
                    case Purchase.PurchaseState.PURCHASED:
                        // ✅ Purchase completed successfully
                        AppLog.e("Purchase PURCHASED: " + purchase.getOrderId());

                        // Acknowledge the purchase if it hasn't already been acknowledged.
                        if (!purchase.isAcknowledged()) {
                            acknowledgePurchase(purchase.getPurchaseToken());
                        }

                        // Turn ON PRO
                        Static.turnOnSubscribedCurrently();

                        showProActive();

                        // If signed in, send payment transaction information to API
                        if (MyApp.getInstance().userMgr.isSignedIn()) {
                            PaymentUtils.sendGoogleInAppPaymentToAPI(purchase, productDetails);
                        }
                        break;

                    case Purchase.PurchaseState.PENDING:
                        // ⏳ Purchase is pending (e.g., pending payment method like cash)
                        AppLog.e("Purchase PENDING: " + purchase.getOrderId());

                        // Acknowledge the pending purchase but DO NOT grant access yet
                        if (!purchase.isAcknowledged()) {
                            acknowledgePurchase(purchase.getPurchaseToken());
                        }

                        // Show pending message to user
                        runOnUiThread(() -> {
                            Static.showToastLong(MyApp.getInstance().getString(R.string.purchase_pending));
                        });

                        // Do NOT turn on PRO for pending purchases
                        // Access will be granted when state changes to PURCHASED
                        break;

                    case Purchase.PurchaseState.UNSPECIFIED_STATE:
                        // ❓ Unspecified state - should not happen normally
                        AppLog.e("Purchase UNSPECIFIED_STATE: " + purchase.getOrderId());

                        // Check with server if signed in
                        if (MyApp.getInstance().userMgr.isSignedIn()) {
                            MyApp.getInstance().asyncsMgr.executeCheckProAsync(MyApp.getInstance().userMgr.getSignedInEmail());
                        } else {
                            Static.turnOffSubscribedCurrently();
                        }
                        break;

                    default:
                        AppLog.e("Unknown purchase state: " + purchase.getPurchaseState());
                        break;
                }
            }
        } else {
            // NO PURCHASES FOUND
            Static.turnOffSubscribedCurrently();
        }
    }

    private class BrReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String doWhat = intent.getStringExtra(Static.BROADCAST_DO);

            // - Update UI -
            if (Objects.requireNonNull(doWhat).equals(Static.DO_UPDATE_UI)) {
                updateUi();
            }
        }
    }
}