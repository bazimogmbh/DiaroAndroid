package com.pixelcrater.Diaro.utils;

import com.pixelcrater.Diaro.config.GlobalConstants;
import com.pixelcrater.Diaro.MyApp;

import java.io.IOException;

import javax.net.ssl.SSLHandshakeException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiUtils {

    // Send a async ping to server
    public static void ping(){

        String signedInEmail = MyApp.getInstance().userMgr.getSignedInEmail();

        if(signedInEmail != null && !signedInEmail.isEmpty()) {

            AppLog.d("SignedInUser is " + signedInEmail);

            Request request = null;

            try {
                String encodedEmail = AES256Cipher.encodeString(signedInEmail, GlobalConstants.ENCRYPTION_KEY);

                RequestBody formBody = new FormBody.Builder().add("encodedEmail", encodedEmail) .build();

                request = new Request.Builder()
                        .url(Static.getApiUrl() + GlobalConstants.API_PING)
                        .post(formBody).build();

                OkHttpHelper.sharedInstance().getClient().newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        /** if (!response.isSuccessful()) {
                         throw new IOException("Unexpected code " + response);
                         } else {
                         // do something wih the result
                         AppLog.e("" +response);
                         } **/
                    }
                });


            } catch (final SSLHandshakeException e) {
                try {
                    Response response = Static.getUnsafeOkHttpClient().newCall(request).execute();
                } catch (IOException ex) {
                }
            }catch (Exception e ) {
                e.printStackTrace();
                AppLog.e("" + e.getMessage());
            }

        }

    }

    public static void sendPaymentInfo(){


    }

}
