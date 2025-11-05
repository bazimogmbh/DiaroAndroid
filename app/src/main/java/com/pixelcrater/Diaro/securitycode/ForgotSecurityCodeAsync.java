package com.pixelcrater.Diaro.securitycode;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.pixelcrater.Diaro.config.GlobalConstants;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.AES256Cipher;
import com.pixelcrater.Diaro.utils.AppLog;

import java.lang.ref.WeakReference;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ForgotSecurityCodeAsync extends AsyncTask<Object, String, Boolean> {

    private String responseText = null;
    private WeakReference<Context> weakContext;
    private String mEmail;
    private String mSecurityCode;
    private ProgressDialog pleaseWaitDialog;

    public ForgotSecurityCodeAsync(Context context, String email, String securityCode) {
        weakContext = new WeakReference<>(context);
        mEmail = email;
        mSecurityCode = securityCode;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (weakContext != null) {
            showPleaseWaitDialog(weakContext.get());
        }
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

        try {
            String encodedEmail = AES256Cipher.encodeString(mEmail, GlobalConstants.ENCRYPTION_KEY);
            String encodedSecurityCode = AES256Cipher.encodeString(mSecurityCode, GlobalConstants.ENCRYPTION_KEY);

            // Connect to API
            RequestBody formBody = new FormBody.Builder()
                    .add("encodedEmail", encodedEmail)
                    .add("encodedSecurityCode", encodedSecurityCode)
                    .build();

            Request request = new Request.Builder()
                    .url(Static.getApiUrl() + GlobalConstants.API_FORGOT_SECURITY_CODE)
                    .post(formBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            responseText = response.body().string();
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
                if (responseText.endsWith("email_invalid")) {
                    errorMsg = MyApp.getInstance().getString(R.string.invalid_email);
                } else {
                    errorMsg = String.format("%s: %s", MyApp.getInstance().getString(R.string.error), responseText);
                }

                Static.showToastError(errorMsg);
            } else if (responseText.equals("ok")) {

                String maskedEmail = mEmail.replaceAll("(^[^@]{3}|(?!^)\\G)[^@]", "$1*");
                Static.showToastSuccess(MyApp.getInstance().getString(R.string.email_sent) + " : " + maskedEmail);
            } else {
                Static.showToastError(MyApp.getInstance().getString(R.string.server_error));
            }
        }
    }
}
