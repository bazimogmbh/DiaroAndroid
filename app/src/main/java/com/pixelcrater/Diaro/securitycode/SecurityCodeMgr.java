package com.pixelcrater.Diaro.securitycode;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.utils.AES256Cipher;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyDevice;

public class SecurityCodeMgr {

    private boolean isLocked = true;
    private Runnable setLocked_r = () -> setLocked();

    public SecurityCodeMgr() {
    }

    public void setLocked() {
        isLocked = true;
    }

    public void setUnlocked() {
        isLocked = false;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setPostDelayedLock() {
        int delaySeconds = MyApp.getInstance().prefs.getInt(Prefs.PREF_SC_REQUEST_PERIOD, 0);
        AppLog.d("delaySeconds: " + delaySeconds);

        MyApp.getInstance().handler.postDelayed(setLocked_r, delaySeconds * 1000);
    }

    public void clearPostDelayedLock() {
        MyApp.getInstance().handler.removeCallbacks(setLocked_r);
    }

    public boolean isSecurityCodeSet() {
        return getSecurityCode() != null;
    }

    public String getSecurityCode() {
        // Decrypt preference
        String decryptedString = null;
        try {
            String encryptedPrefValue = MyApp.getInstance().prefs. getString(Prefs.PREF_ENC_SECURITY_CODE, null);
            if (encryptedPrefValue != null) {
                decryptedString = AES256Cipher.decodeString(encryptedPrefValue, MyDevice.getInstance().deviceUid);

            }
        } catch (Exception e) {
            AppLog.e("Exception: " + e);

            // Clear security code and forgot email
            setSecurityCode(null);
            setForgotEmail(null);
        }

        return decryptedString;
    }

    public void setSecurityCode(String securityCode) {
        // Encrypt and set preference
        try {
            String encryptedString = AES256Cipher.encodeString(securityCode,  MyDevice.getInstance().deviceUid);
            MyApp.getInstance().prefs.edit().putString( Prefs.PREF_ENC_SECURITY_CODE, encryptedString).apply();

        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }
    }

    public boolean isForgotEmailSet() {
        return Static.isValidEmail(getForgotEmail());
    }

    public String getForgotEmail() {
        // Decrypt preference
        String decryptedString = null;
        try {
            String encryptedPrefValue = MyApp.getInstance().prefs.getString(Prefs.PREF_ENC_FORGOT_SECURITY_CODE_EMAIL, null);
            if (encryptedPrefValue != null) {
                decryptedString = AES256Cipher.decodeString(encryptedPrefValue,  MyDevice.getInstance().deviceUid);

            }
        } catch (Exception e) {
            AppLog.e("Exception: " + e);

            // Clear forgot email
            setForgotEmail(null);
        }

        return decryptedString;
    }

    public void setForgotEmail(String email) {
        // Encrypt and set preference
        try {
            String encryptedString = AES256Cipher.encodeString(email,  MyDevice.getInstance().deviceUid);
            MyApp.getInstance().prefs.edit().putString( Prefs.PREF_ENC_FORGOT_SECURITY_CODE_EMAIL, encryptedString).apply();

        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }
    }
}
