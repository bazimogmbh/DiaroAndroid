package com.pixelcrater.Diaro.profile;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.config.GlobalConstants;
import com.pixelcrater.Diaro.utils.AES256Cipher;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.Static;

import java.io.IOException;

import javax.net.ssl.SSLHandshakeException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUpAsync extends AsyncTask<Object, String, Boolean> {

    private Context mContext;
    private String responseText = null;
    private String mEmail;
    private String mPassword;
    private ProgressDialog pleaseWaitDialog;

    public SignUpAsync(Context context, @NonNull String email, @NonNull String password) {
        mContext = context;
        mEmail = email;
        mPassword = password;

        FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.SIGN_UP, new Bundle());
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
            String encodedPassword = mPassword.equals("") ? "" : AES256Cipher.encodeString(mPassword, GlobalConstants.ENCRYPTION_KEY);

            // Connect to API
            RequestBody formBody = new FormBody.Builder()
                    .add("encodedEmail", encodedEmail)
                    .add("encodedPassword", encodedPassword)
                    .build();

            request = new Request.Builder()
                    .url(Static.getApiUrl() + GlobalConstants.API_SIGN_UP)
                    .post(formBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            responseText = response.body().string();
        }catch (final SSLHandshakeException e) {
            try {
                Response response = Static.getUnsafeOkHttpClient().newCall(request).execute();
                responseText = response.body().string();
            } catch (IOException ex) {
                AppLog.e("Exception: " + e);
                return false;
            }

            return true;
        }
        catch (Exception e) {
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
                if (responseText.endsWith("email_empty")) {
                    errorMsg = MyApp.getInstance().getString(R.string.invalid_email);
                } else if (responseText.endsWith("account_exists")) {
                    errorMsg = MyApp.getInstance().getString(R.string.account_exists_error);
                } else {
                    errorMsg = MyApp.getInstance().getString(R.string.error) + ": " + responseText;
                }

                Static.showToastError(errorMsg);
            } else if (responseText.equals("ok")) {
                // Set signed in user
                MyApp.getInstance().userMgr.setSignedInUser(mEmail, UserMgr.SIGNED_IN_WITH_DIARO_ACCOUNT);
                Static.showToastSuccess(MyApp.getInstance().getString(R.string.signed_up_successfully));

            } else {
                Static.showToastError(MyApp.getInstance().getString(R.string.server_error));

            }
        }
    }
}
