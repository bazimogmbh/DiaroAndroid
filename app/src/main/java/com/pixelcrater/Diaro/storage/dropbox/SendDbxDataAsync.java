package com.pixelcrater.Diaro.storage.dropbox;

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

public class SendDbxDataAsync extends AsyncTask<Object, String, Boolean> {
    private String responseText = null;
    private String m_email;
    private String m_dbxEmail;
    private String m_dbxUIDv1;
    private String m_dbxToken;

    public SendDbxDataAsync(String email, String dbxEmail, String dbxUIDv1, String dbxToken) {
        AppLog.e("signedInEmail: " + email + " dbxEmail " + dbxEmail);
        m_email = email == null ? "" : email;
        m_dbxEmail = dbxEmail == null ? "" : dbxEmail;
        m_dbxUIDv1 = dbxUIDv1 == null ? "" : dbxUIDv1;
        m_dbxToken = dbxToken == null ? "" : dbxToken;
    }

    @Override
    protected Boolean doInBackground(Object... params) {

        if(m_dbxEmail.isEmpty() || m_dbxUIDv1.isEmpty() || m_dbxToken.isEmpty())
            return false;

        Request request = null;
        try {
            // Connect to API
            RequestBody formBody = new FormBody.Builder()
                    // .add("encodedEmail", encodedEmail)
                    .add("encodedEmail", AES256Cipher.encodeString(m_email, GlobalConstants.ENCRYPTION_KEY))
                    .add("encodedDbxEmail", AES256Cipher.encodeString(m_dbxEmail, GlobalConstants.ENCRYPTION_KEY))
                    .add("encodedDbxUIDv1", AES256Cipher.encodeString(m_dbxUIDv1, GlobalConstants.ENCRYPTION_KEY))
                    .add("encodedDbxToken", AES256Cipher.encodeString(m_dbxToken, GlobalConstants.ENCRYPTION_KEY))
                    .build();

            request = new Request.Builder()
                    .url(Static.getApiUrl() + GlobalConstants.API_SET_DROPBOX_CREDENTIALS)
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
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
            return false;
        }

        return true;
    }

}
