package com.pixelcrater.Diaro.premium.billing;

import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.SkuDetails;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.config.GlobalConstants;
import com.pixelcrater.Diaro.utils.AppLog;

import org.joda.time.DateTime;


public class PaymentUtils {

    public static void sendGoogleInAppPaymentToAPI(Purchase purchase, SkuDetails skuDetails) {
        AppLog.e("sendGoogleInAppPaymentToAPI -> " + purchase);

        //   Purchase: Purchase. Json: {"orderId":"GPA.3381-1956-4899-30775","packageName":"com.pixelcrater.Diaro","productId":"subscription_pro_yearly","purchaseTime":1602251039252,"purchaseState":0,"purchaseToken":"cpnokhbloengildpgblkookp.AO-J1Oyanq0hlB2OzH5r5TSD1Oye4F3fdo7PUzzlhTQhU7ag-iznRudCmaSgYfx8-rMVvnUyEP1AYl7FMlbckWnybLnpCaPc2RX_QGMIcNJhyqkC0eQQzKnl8KPGxZi8o0YGxtoO1iWA","autoRenewing":true,"acknowledged":false}

        String product = getProductIdFromSku(purchase.getSkus().get(0));

        AppLog.e(product + " , " + purchase.getSkus().get(0));
        String purchaseToken = purchase.getPurchaseToken();
        String orderId = purchase.getOrderId();
        String sku = purchase.getSkus().get(0);

        String price = "5.49";
        String currencyCode = "EUR";
        String description = "Diaro Pro";
        // the price info is only available in sku details
        if (skuDetails != null) {
            //SkuDetails: {"skuDetailsToken":"AEuhp4I3pC1QKBnevZs2iMNmAXbBdtLy3AyE26gRXt3ltzrSMXVLJGl_1aiON_I83e9m","productId":"subscription_pro_yearly","type":"subs","price":"€5.49","price_amount_micros":5490000,"price_currency_code":"EUR","subscriptionPeriod":"P1Y","title":"Diaro PRO yearly subscription (Diaro - Diary, Journal, Notepad, Mood Tracker)","description":"Yearly subscription of Diaro PRO version"}
            AppLog.e("SKU: " + skuDetails);

            long priceLong = skuDetails.getPriceAmountMicros();
            double priceDouble = priceLong / 1000000d;
            price = String.valueOf(priceDouble);

            currencyCode = skuDetails.getPriceCurrencyCode();
            description = skuDetails.getDescription();
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
