package com.pixelcrater.Diaro.profile;

import android.app.ProgressDialog;
import android.content.Context;
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

public class ForgotPasswordAsync extends AsyncTask<Object, String, Boolean> {

    private String responseText = null;
    private Context mContext;
    private String mEmail;
    private ProgressDialog pleaseWaitDialog;

    public ForgotPasswordAsync(Context context, String email) {

        mContext = context;
        mEmail = email;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        showPleaseWaitDialog(mContext);
    }

    public void showPleaseWaitDialog(Context context) {
        dismissPleaseWaitDialog();

        try {
            pleaseWaitDialog = new ProgressDialog(context);
            pleaseWaitDialog.setMessage(MyApp.getInstance().getString(R.string.please_wait));
            pleaseWaitDialog.setCancelable(false);
            pleaseWaitDialog.setButton(ProgressDialog.BUTTON_NEUTRAL, MyApp.getInstance().getString(android.R.string.cancel), (dialog, which) -> cancel(true));
            pleaseWaitDialog.show();
        } catch (Exception e) {
            cancel(true);
        }
    }

    private void dismissPleaseWaitDialog() {
        try {
            pleaseWaitDialog.dismiss();
        } catch (Exception e) {
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        onCancel();
    }

    private void onCancel() {
        dismissPleaseWaitDialog();
    }

    @Override
    protected Boolean doInBackground(Object... params) {

        Request request = null;
        try {
            String encodedEmail = AES256Cipher.encodeString(mEmail, GlobalConstants.ENCRYPTION_KEY);
            AppLog.d("encodedEmail: " + encodedEmail);

            // Connect to API
            RequestBody formBody = new FormBody.Builder()
                    .add("encodedEmail", encodedEmail)
                    .build();

            request = new Request.Builder()
                    .url(Static.getApiUrl() + GlobalConstants.API_FORGOT_PASSWORD)
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
        AppLog.d("responseText: " + responseText);

        dismissPleaseWaitDialog();

        if (succeeded && responseText != null) {
            if (responseText.startsWith("error:")) {
                String errorMsg;
                if (responseText.endsWith("account_not_found")) {
                    errorMsg = MyApp.getInstance().getString(R.string.diaro_account_not_found_error);
                } else {
                    errorMsg = MyApp.getInstance().getString(R.string.error) + ": " + responseText;
                }

                Static.showToastError(errorMsg);
            } else if (responseText.equals("ok")) {
                Static.showToastError(MyApp.getInstance().getString(R.string.email_with_password_change_instructions_sent));
            } else {
                Static.showToastError(MyApp.getInstance().getString(R.string.server_error));

            }
        }
    }
}
