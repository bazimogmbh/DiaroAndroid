package com.pixelcrater.Diaro.profile;

import android.os.AsyncTask;

import com.pixelcrater.Diaro.config.GlobalConstants;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
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

public class CheckProAsync extends AsyncTask<Object, String, Boolean> {

    private String responseText = null;
    private String mEmail;

    public CheckProAsync(String email) {
        mEmail = email;
    }

    @Override
    protected Boolean doInBackground(Object... params) {

        Request request = null;
        try {
            String encodedEmail = AES256Cipher.encodeString(mEmail, GlobalConstants.ENCRYPTION_KEY);

            // Connect to API
            RequestBody formBody = new FormBody.Builder()
                    .add("encodedEmail", encodedEmail)
                    .add("system", "Android")
                    .build();

            request = new Request.Builder()
                    .url(Static.getApiUrl() + GlobalConstants.API_CHECK_PRO)
                    .post(formBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            responseText = response.body().string();
        } catch (final SSLHandshakeException e) {
            try {
                Response response = Static.getUnsafeOkHttpClient().newCall(request).execute();
                responseText = response.body().string();
            } catch (IOException ex) {
                AppLog.e("Exception: " + e);
                return false;
            }

            return true;
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean succeeded) {
        AppLog.d("succeeded: " + succeeded + ", responseText: " + responseText);

        if (succeeded && responseText != null) {
            if (responseText.startsWith("error:")) {
                String errorMsg;
                if (responseText.endsWith("account_not_found")) {
                    errorMsg = MyApp.getInstance().getString(R.string.diaro_account_not_found_error);
                } else {
                    errorMsg = MyApp.getInstance().getString(R.string.error) + ": " + responseText;
                }

                Static.showToastError(errorMsg);
            } else if (responseText.equals("yes")) {
                // Turn ON PRO
                Static.turnOnPro();
            } else if (responseText.equals("no")) {
                // Turn OFF PRO
                Static.turnOffPro();
            }
        }
    }
}
