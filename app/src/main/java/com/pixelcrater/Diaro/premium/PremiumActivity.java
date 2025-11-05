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
//    private TextView proVersionText;
    //  private TextView signInLink;

    private final BroadcastReceiver brReceiver = new BrReceiver();

    private final HashMap<String, ProductDetails> mProductsMap = new HashMap<>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //    getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.dark_nav)); // Navigation bar the soft bottom of some phones like nexus and some Samsung note series
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.color_blue2)); //status bar or the time bar at the top

        setBillingHandler(this);

        setContentView(addViewToContentContainer(R.layout.pro_responsive));

        getSupportActionBar().hide();

        // PRO version text
        //   proVersionText = findViewById(R.id.pro_version_text);

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

        /**  signInLink = findViewById(R.id.sign_in_link);
         signInLink.setOnClickListener(v -> {
         // If not signed in
         if (!MyApp.getInstance().userMgr.isSignedIn()) {
         Static.startSignInActivity(PremiumActivity.this, activityState);
         }
         }); **/

        // Register broadcast receiver
        ContextCompat.registerReceiver(this,brReceiver, new IntentFilter(Static.BR_IN_GET_PRO), ContextCompat.RECEIVER_NOT_EXPORTED);

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
     * Updates UI depending if PRO is purchased or not
     */
    public void updateUi() {
        //  signInLink.setVisibility(View.GONE);

        // If PRO purchased
        if (Static.isProUser()) {
            // If not signed in
            if (!MyApp.getInstance().userMgr.isSignedIn()) {
                // signInLink.setVisibility(View.VISIBLE);
                //  signInLink.setText(R.string.sign_in_to_use_diaro_pro_on_other_devices);
            }
        } else {
            // If not signed in
            if (!MyApp.getInstance().userMgr.isSignedIn()) {
                //  signInLink.setVisibility(View.VISIBLE);
                //  signInLink.setText(String.format("%s %s", getString(R.string.question_already_have_diaro_pro), getString(R.string.sign_in)));
            }
        }

        // - Turn on/off PRO button -
        // If Subscribed
        if (Static.isSubscribedCurrently() || Static.isPlayNboSubscription()) {
            activityState.setActionBarTitle(Objects.requireNonNull(getSupportActionBar()), getString(R.string.diaro_pro_version));
            //   proVersionText.setText(getString(R.string.pro_version_active));
            showProActive();
        } else {
            activityState.setActionBarTitle(Objects.requireNonNull(getSupportActionBar()), getString(R.string.get_diaro_pro));
            //    proVersionText.setText(R.string.buy_pro_version_text);


            /**       if (yearlyPrice.isEmpty())
             buttonBuy1Month.setText(String.format("%s%s", getString(R.string.get_diaro_pro), yearlyPrice));
             else
             buttonBuy1Month.setText(yearlyPrice); **/
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        activityState.onActivityResult(requestCode, resultCode, data); // needed for hiding SecurityCode activity
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startPurchaseFlow(String sku) {
        // Amazon
        if (AppConfig.AMAZON_BUILD) {
            final RequestId requestId = PurchasingService.purchase(sku);
            AppLog.d("requestId: " + requestId);
        }
        // Google Play
        else if (AppConfig.GOOGLE_PLAY_BUILD) {
            if (mProductsMap.isEmpty())
                return;

            // TODO : adapt monthly or yearly
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
    }

    @Override
    public void onBillingInitialized() {
        querySkuDetails(GlobalConstants.activeSubscriptionsList, BillingClient.SkuType.SUBS);
        queryPurchases(BillingClient.SkuType.SUBS);
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
                        pricePerMonth = totalPrice / 1;

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

                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    // Acknowledge the purchase if it hasn't already been acknowledged.
                    if (!purchase.isAcknowledged()) {
                        acknowledgePurchase(purchase.getPurchaseToken());
                    }
                }

                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    // Turn ON PRO
                    Static.turnOnSubscribedCurrently();

                    //    Static.showToastLong(MyApp.getInstance().getString(R.string.pro_version_active));

                    showProActive();
                    //  If signed in Send payment transaction information (purchased or canceled/refunded) to API
                    if (MyApp.getInstance().userMgr.isSignedIn()) {
                        PaymentUtils.sendGoogleInAppPaymentToAPI(purchase, productDetails);
                    }
                } else {
                    if (MyApp.getInstance().userMgr.isSignedIn()) {
                        MyApp.getInstance().asyncsMgr.executeCheckProAsync(MyApp.getInstance().userMgr.getSignedInEmail());
                    } else {
                        Static.turnOffSubscribedCurrently();
                    }
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