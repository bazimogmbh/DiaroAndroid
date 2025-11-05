package com.pixelcrater.Diaro.premium.billing;


import com.android.billingclient.api.SkuDetails;


//SkuDetails: {"skuDetailsToken":"AEuhp4I3pC1QKBnevZs2iMNmAXbBdtLy3AyE26gRXt3ltzrSMXVLJGl_1aiON_I83e9m",
// "productId":"subscription_pro_yearly",
// "type":"subs",
// "price":"€5.49",
// "price_amount_micros":5490000,
// "price_currency_code":"EUR",
// "subscriptionPeriod":"P1Y",
// "title":"Diaro PRO yearly subscription (Diaro - Diary, Journal, Notepad, Mood Tracker)",
// "description":"Yearly subscription of Diaro PRO version"}


public class ProductSku {

    public final String productId;
    public final String type;
    public final String title;


    public ProductSku(SkuDetails skuDetails){
        this.productId  = skuDetails.getSku();
        this.type = skuDetails.getType();
        this.title = skuDetails.getTitle();
    }


}
