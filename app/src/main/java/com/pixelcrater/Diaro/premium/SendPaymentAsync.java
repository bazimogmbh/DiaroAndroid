package com.pixelcrater.Diaro.premium;

import android.os.AsyncTask;

import com.pixelcrater.Diaro.config.GlobalConstants;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.AES256Cipher;
import com.pixelcrater.Diaro.utils.AppLog;

import java.io.IOException;

import javax.net.ssl.SSLHandshakeException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendPaymentAsync extends AsyncTask<Object, String, Boolean> {

    private String responseText = null;
    private String mSku;
    private String mSignedInEmail;
    private String mDate;
    private String mSystem;
    private String mType;
    private String mEmail;
    private String mProduct;
    private String mPurchaseToken;
    private String mDescription;
    private String mPrice;
    private String mCurrencyCode;
    private String mOrderId;
    private String mRefunded;

    public SendPaymentAsync(String sku, String signedInEmail, String date, String system, String type,
                            String email, String product, String purchaseToken,  String description, String price,
                            String currencyCode, String orderId, String refunded) {
        AppLog.d("sku:" + sku+  ", signedInEmail: " + signedInEmail + ", date: " + date + ", system: " + system +
                ", type: " + type + ", email: " + email + ", product: " + product +  ", purchaseToken: " + purchaseToken +
                ", description: " + description + ", price: " + price +
                ", currencyCode: " + currencyCode + ", orderId: " + orderId +
                ", refunded: " + refunded);

        mSku = sku  == null ? "" : sku;
        mSignedInEmail = signedInEmail == null ? "" : signedInEmail;
        mDate = date == null ? "" : date;
        mSystem = system == null ? "" : system;
        mType = type == null ? "" : type;
        mEmail = email == null ? "" : email;
        mProduct = product == null ? "" : product;
        mPurchaseToken = purchaseToken == null ? "" : purchaseToken;
        mDescription = description == null ? "" : description;
        mPrice = price == null ? "" : price;
        mCurrencyCode = currencyCode == null ? "" : currencyCode;
        mOrderId = orderId == null ? "" : orderId;
        mRefunded = refunded == null ? "0" : refunded; // 0 - not refunded, 1 - refunded

    }

    @Override
    protected Boolean doInBackground(Object... params) {

        Request request = null;
        try {
            String encodedEmail = AES256Cipher.encodeString(mSignedInEmail, GlobalConstants.ENCRYPTION_KEY);

            // Connect to API
            RequestBody formBody = new FormBody.Builder()
                    .add("encodedEmail", encodedEmail)
                    .add("date", mDate)
                    .add("sku", mSku)
                    .add("system", mSystem)
                    .add("type", mType)
                    .add("email", mEmail)
                    .add("product", mProduct)
                    .add("purchaseToken", mPurchaseToken)
                    .add("description", mDescription)
                    .add("price", mPrice)
                    .add("currencyCode", mCurrencyCode)
                    .add("orderId", mOrderId)
                    .add("refunded", mRefunded)
                    .build();

           request = new Request.Builder()
                    .url(Static.getApiUrl() + GlobalConstants.API_ADD_PAYMENT)
                    .post(formBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();

            responseText = response.body().string();
            AppLog.d("responseText: " + responseText);
        } catch (final SSLHandshakeException e) {
            try {
                Response response = Static.getUnsafeOkHttpClient().newCall(request).execute();
                responseText = response.body().string();
            } catch (IOException ex) {
                AppLog.e("Exception: " + e);
                return false;
            }
            return true;
        }catch (Exception e) {
            AppLog.e("Exception: " + e);
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean succeeded) {
    }
}
