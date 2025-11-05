package com.pixelcrater.Diaro.appupgrades;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.utils.AES256Cipher;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.DeprecatedCrypto;
import com.pixelcrater.Diaro.utils.MyDevice;

public class AppUpgrade_50 {
    public AppUpgrade_50() {
        AppLog.d("");

        // Encrypt security code pref
        encryptSecurityCode();

        // Re-encrypt PRO pref with new encryption algorithm
        reEncryptPro();

        // Update locale pref, add regions (moved to OneSky)
        updateLocalePref();

        AppLog.d("");
    }

    private void updateLocalePref() {
        String prefLocale = MyApp.getInstance().prefs.getString(Prefs.PREF_LOCALE, null);

        if (prefLocale != null) {
            String newLocale = null;
            switch (prefLocale) {
                case "bs":
                    newLocale = "bs_BA";
                    break;
                case "bg":
                    newLocale = "bg_BG";
                    break;
                case "gl":
                    newLocale = "gl_ES";
                    break;
                case "lt":
                    newLocale = "lt_lt";
                    break;
                case "lv":
                    newLocale = "lv_LV";
                    break;
                case "sl":
                    newLocale = "sl_SI";
                    break;
                case "zh":
                    newLocale = "zh_CN";
                    break;
            }

            if (newLocale != null) {
                MyApp.getInstance().prefs.edit().putString(Prefs.PREF_LOCALE, newLocale).apply();
            }
        }
    }

    private void encryptSecurityCode() {
        String securityCode = MyApp.getInstance().prefs.getString(Prefs.PREF_ENC_SECURITY_CODE, null);
//		AppLog.d("securityCode: " + securityCode);

        if (securityCode != null && securityCode.length() == 4) {
            // MyApp.getInstance().securityCodeMgr not created yet, so set security code here
            setSecurityCode(securityCode);
        }
    }

    private void setSecurityCode(String securityCode) {
        // Encrypt and set preference
        try {
            String encryptedString = AES256Cipher.encodeString(securityCode,MyDevice.getInstance().deviceUid);
            MyApp.getInstance().prefs.edit().putString(Prefs.PREF_ENC_SECURITY_CODE, encryptedString).apply();

        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }
    }

    private void reEncryptPro() {
        // Check if is Pro encrypted in settings by deprecated method and turn on Pro
        if (isProDeprecated()) {
            Static.turnOnPro();
        }
    }

    private boolean isProDeprecated() {
        boolean isPro = false;

        // Decrypt preference
        String decryptedString;
        try {
            String encryptedPrefValue = MyApp.getInstance().prefs.getString(Prefs.PREF_ENC_PRO, null);
            if (encryptedPrefValue != null) {
                decryptedString = DeprecatedCrypto.decryptString(encryptedPrefValue,Static.getDeprecatedDeviceUid());
                if (decryptedString.equals("true")) {
                    isPro = true;
                }
            }
        } catch (Exception e) {
            AppLog.e("Exception: " + e);

            MyApp.getInstance().prefs.edit().putString(Prefs.PREF_ENC_PRO, null).apply();
            Static.sendBroadcastsAfterPurchaseStateChange();
            MyApp.getInstance().checkAppState();
        }

        return isPro;
    }
}
