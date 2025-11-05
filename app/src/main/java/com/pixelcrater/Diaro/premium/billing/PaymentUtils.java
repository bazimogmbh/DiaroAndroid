package com.pixelcrater.Diaro.premium.billing;

import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.config.GlobalConstants;
import com.pixelcrater.Diaro.utils.AppLog;

import org.joda.time.DateTime;


public class PaymentUtils {

    public static void sendGoogleInAppPaymentToAPI(Purchase purchase, ProductDetails productDetails) {
        AppLog.e("sendGoogleInAppPaymentToAPI -> " + purchase);

        //   Purchase: Purchase. Json: {"orderId":"GPA.3381-1956-4899-30775","packageName":"com.pixelcrater.Diaro","productId":"subscription_pro_yearly","purchaseTime":1602251039252,"purchaseState":0,"purchaseToken":"cpnokhbloengildpgblkookp.AO-J1Oyanq0hlB2OzH5r5TSD1Oye4F3fdo7PUzzlhTQhU7ag-iznRudCmaSgYfx8-rMVvnUyEP1AYl7FMlbckWnybLnpCaPc2RX_QGMIcNJhyqkC0eQQzKnl8KPGxZi8o0YGxtoO1iWA","autoRenewing":true,"acknowledged":false}

        String product = getProductIdFromSku(purchase.getProducts().get(0));

        AppLog.e(product + " , " + purchase.getProducts().get(0));
        String purchaseToken = purchase.getPurchaseToken();
        String orderId = purchase.getOrderId();
        String sku = purchase.getProducts().get(0);

        String price = "5.49";
        String currencyCode = "EUR";
        String description = "Diaro Pro";
        // the price info is only available in product details
        if (productDetails != null) {
            AppLog.e("ProductDetails: " + productDetails);

            // Get pricing information from subscription offer details
            if (productDetails.getSubscriptionOfferDetails() != null &&
                !productDetails.getSubscriptionOfferDetails().isEmpty()) {

                ProductDetails.SubscriptionOfferDetails offerDetails =
                    productDetails.getSubscriptionOfferDetails().get(0);

                ProductDetails.PricingPhase pricingPhase =
                    offerDetails.getPricingPhases().getPricingPhaseList().get(0);

                long priceLong = pricingPhase.getPriceAmountMicros();
                double priceDouble = priceLong / 1000000d;
                price = String.valueOf(priceDouble);

                currencyCode = pricingPhase.getPriceCurrencyCode();
            }

            description = productDetails.getDescription();
        }

//      AppLog.d("price: " + price + ", currencyCode: " + currencyCode);

        String date = new DateTime(purchase.getPurchaseTime()).toString("yyyy.MM.dd HH:mm:ss");
        String system = GlobalConstants.PAYMENT_SYSTEM_ANDROID;
        String type = GlobalConstants.PAYMENT_TYPE_GOOGLE;
        String email = "";
        String refunded = "0";

        // If not signed in, return
        if (!MyApp.getInstance().userMgr.isSignedIn()) {
            return;
        }

        // Send PRO payment to API
        MyApp.getInstance().asyncsMgr.executeSendPaymentAsync(sku, date, system, type, email, product, purchaseToken, description, price, currencyCode, orderId, refunded);
    }

    private static String getProductIdFromSku(String sku) {
        String product = GlobalConstants.PAYMENT_PRODUCT_DIARO_PRO_Yearly;

        // One time Pro payment
        if (sku.compareTo(GlobalConstants.MANAGED_PURCHASE_PRO_VERSION) == 0) {
            product = GlobalConstants.PAYMENT_PRODUCT_DIARO_PRO;
        }

        // old pro yearly version ( premium) for 4.59 gbp
        if (sku.compareTo(GlobalConstants.SUBSCRIPTION_PURCHASE_PRO_YEARLY) == 0) {
            product = GlobalConstants.PAYMENT_PRODUCT_DIARO_PRO_Yearly;
        }

        // monthly premium
        if (sku.compareTo(GlobalConstants.SUBSCRIPTION_PURCHASE_PREMIUM_MONTHLY) == 0) {
            product = GlobalConstants.PAYMENT_PRODUCT_DIARO_PREMIUM_MONTHLY;
        }

        // quarterly premium
        if (sku.compareTo(GlobalConstants.SUBSCRIPTION_PURCHASE_PREMIUM_3_MONTHLY) == 0) {
            product = GlobalConstants.PAYMENT_PRODUCT_DIARO_PREMIUM_QUARTERLY;
        }

        // yearly premium new for 6.99 gbp
        if (sku.compareTo(GlobalConstants.SUBSCRIPTION_PURCHASE_PREMIUM_YEARLY) == 0) {
            product = GlobalConstants.PAYMENT_PRODUCT_DIARO_PREMIUM_YEARLY;
        }

        // yearly premium with trail
        if (sku.compareTo(GlobalConstants.SUBSCRIPTION_PURCHASE_PREMIUM_YEARLY_TRAIL) == 0) {
            product = GlobalConstants.PAYMENT_PRODUCT_DIARO_PREMIUM_YEARLY_TRAIL;
        }


        return product;
    }

}
