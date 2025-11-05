package com.pixelcrater.Diaro.premium.billing;

import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;

import java.util.List;

public interface BillingUpdateListener {

    // The billing client is ready. You can query purchases here.
    void onBillingInitialized();

    void onBillingUnavailable(String debugMessage, int responseCode);

    void onAvailableProductsResponse(List<ProductDetails> availableProductsSkuList);

    void onOwnedProductsResponse(List<Purchase> ownedProductsList);

    void onPurchase(List<Purchase> purchasesList);


}
