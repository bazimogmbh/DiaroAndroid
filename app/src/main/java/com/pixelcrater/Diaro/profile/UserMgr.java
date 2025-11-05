package com.pixelcrater.Diaro.profile;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.pixelcrater.Diaro.BuildConfig;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.utils.AES256Cipher;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyDevice;

public class UserMgr {

    public static final String SIGNED_IN_WITH_DIARO_ACCOUNT = "diaro_account";
    public static final String SIGNED_IN_WITH_GOOGLE = "google";

    private boolean isSignedIn = false;
    private String signedInEmail;
    private String signedInAccountType;

    public UserMgr() {
        getSignedInUserData();
    }

    private void getSignedInUserData() {
        try {
            String encodedSignedInEmail = MyApp.getInstance().prefs.getString(Prefs.PREF_ENC_SIGNED_IN_EMAIL, null);
            String encodedSignedInAccountType = MyApp.getInstance().prefs.getString(Prefs.PREF_ENC_SIGNED_IN_ACCOUNT_TYPE, null);

            signedInEmail = AES256Cipher.decodeString(encodedSignedInEmail,MyDevice.getInstance().deviceUid);
            signedInAccountType = AES256Cipher.decodeString(encodedSignedInAccountType, MyDevice.getInstance().deviceUid);

            if (signedInEmail != null || signedInAccountType != null) {
                if (Static.isValidEmail(signedInEmail) && isValidAccountType(signedInAccountType)) {
                    isSignedIn = true;
                } else {
                    throw new Exception("Wrong account type");
                }
            } else {
                isSignedIn = false;
            }
        } catch (Exception e) {
            AppLog.e("Exception: " + e);

            updateSignedInUserPrefs(null, null);
        }

        if (!BuildConfig.DEBUG) {
            // Move this to where you establish a user session
          //  FirebaseAnalytics.getInstance(MyApp.getInstance()).setUserId(getSignedInEmail());
        }
    }

    public boolean isSignedIn() {
        return isSignedIn;
    }

    public void setSignedInUser(String signedInEmail, String signedInAccountType) {
        AppLog.d("signedInEmail: " + signedInEmail + ", signedInAccountType: " + signedInAccountType);

        String encodedEmail = null;
        String encodedSignedInAccountType = null;

        try {
            // Encode
            encodedEmail = AES256Cipher.encodeString(signedInEmail, MyDevice.getInstance().deviceUid);
            encodedSignedInAccountType = AES256Cipher.encodeString(signedInAccountType, MyDevice.getInstance().deviceUid);

        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }

        updateSignedInUserPrefs(encodedEmail, encodedSignedInAccountType);

        Static.sendBroadcast(Static.BR_IN_SIGN_IN, Static.DO_DISMISS_SIGNIN_DIALOG, null);
        Static.sendBroadcast(Static.BR_IN_SIGN_IN, Static.DO_DISMISS_SIGNUP_DIALOG, null);
        Static.sendBroadcast(Static.BR_IN_SIGN_IN, Static.DO_CHECK_STATUS, null);
    }

    private boolean isValidAccountType(String signedInAccountType) {
        return signedInAccountType.equals(SIGNED_IN_WITH_DIARO_ACCOUNT) || signedInAccountType.equals(SIGNED_IN_WITH_GOOGLE);
    }

    private void updateSignedInUserPrefs(String encodedSignedInEmail, String encodedSignedInAccountType) {
        MyApp.getInstance().prefs.edit().putString( Prefs.PREF_ENC_SIGNED_IN_EMAIL, encodedSignedInEmail).apply();
        MyApp.getInstance().prefs.edit().putString(Prefs.PREF_ENC_SIGNED_IN_ACCOUNT_TYPE, encodedSignedInAccountType).apply();

        getSignedInUserData();
    }

    public String getSignedInEmail() {
        return signedInEmail;
    }

    public String getSignedInAccountType() {
        return signedInAccountType;
    }
}
