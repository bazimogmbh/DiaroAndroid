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

public class SignInAsync extends AsyncTask<Object, String, Boolean> {

    private String responseText = null;
    private Context mContext;
    private String mEmail;
    private String mPassword;
    private String mGoogleId;
    private String mName;
    private String mSurname;
    private String mGender;
    private String mBirthday;
    private ProgressDialog pleaseWaitDialog;

    private String errorMsg = "";

    public SignInAsync(Context context, @NonNull String email, @NonNull String password, @NonNull String googleId, @NonNull String name, @NonNull String surname, @NonNull String gender, @NonNull String birthday) {
//		AppLog.d("email: " + email);
        mContext = context;
        mEmail = email;
        mPassword = password;
        mGoogleId = googleId;
        mName = name;
        mSurname = surname;
        mGender = gender;
        mBirthday = birthday;

        FirebaseAnalytics.getInstance(context).logEvent(FirebaseAnalytics.Event.LOGIN, new Bundle());
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
            pleaseWaitDialog.setMessage(MyApp.getInstance().getString(R.string.signing_in_with_ellipsis));
            pleaseWaitDialog.setCancelable(false);
            pleaseWaitDialog.setButton(ProgressDialog.BUTTON_NEUTRAL, MyApp.getInstance().getString(android.R.string.cancel), (dialog, which) -> cancel(true));
            pleaseWaitDialog.show();
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
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
//		AppLog.d("");

        Request request = null;
        try {

            String encodedEmail = AES256Cipher.encodeString(mEmail, GlobalConstants.ENCRYPTION_KEY);
            String encodedPassword = mPassword.equals("") ? "" : AES256Cipher.encodeString(mPassword, GlobalConstants.ENCRYPTION_KEY);
//            AppLog.d("encodedEmail: " + encodedEmail);
//            AppLog.d("encodedPassword: " + encodedPassword);
            AppLog.d("mGoogleId: " + mGoogleId);

            // Connect to API
            RequestBody formBody = new FormBody.Builder()
                    .add("encodedEmail", encodedEmail)
                    .add("encodedPassword", encodedPassword)
                    .add("googleId", mGoogleId)
                    .add("name", mName)
                    .add("surname", mSurname)
                    .add("gender", mGender)
                    .add("birthday", mBirthday)
                    .build();

            request = new Request.Builder()
                    .url(Static.getApiUrl() + GlobalConstants.API_SIGNIN)
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
                errorMsg = "IOException: " + e.getMessage() + e.getClass().getCanonicalName();
                return false;
            }

            return true;
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
            errorMsg = "Exception: PreExecute" + e.getMessage() + e.getClass().getCanonicalName();
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean succeeded) {
        AppLog.d("responseText: " + responseText);

        dismissPleaseWaitDialog();

        if (!succeeded) {
            Static.showToastError("" + errorMsg);
        }

        if (succeeded && responseText != null) {
            if (responseText.startsWith("error:")) {
                String errorMsg;
                if (responseText.endsWith("account_not_found")) {
                    errorMsg = MyApp.getInstance().getString(R.string.diaro_account_not_found_error);
                } else if (responseText.endsWith("password_incorrect")) {
                    errorMsg = MyApp.getInstance().getString(R.string.password_incorrect_error);
                } else {
                    errorMsg = MyApp.getInstance().getString(R.string.error) + " Response: " + responseText;
                }

                Static.showToastError(errorMsg);
            } else if (responseText.equals("ok")) {
                String signedInAccountType = mPassword.equals("") ? UserMgr.SIGNED_IN_WITH_GOOGLE : UserMgr.SIGNED_IN_WITH_DIARO_ACCOUNT;

                // Set signed in user
                MyApp.getInstance().userMgr.setSignedInUser(mEmail, signedInAccountType);

                Static.showToastSuccess(MyApp.getInstance().getString(R.string.signed_in_successfully));

            } else {
                Static.showToastError(MyApp.getInstance().getString(R.string.server_error));

            }
        }
    }
}
