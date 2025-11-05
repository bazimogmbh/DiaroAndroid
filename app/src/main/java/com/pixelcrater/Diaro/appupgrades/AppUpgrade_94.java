package com.pixelcrater.Diaro.appupgrades;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.utils.AES256Cipher;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyDevice;

public class AppUpgrade_94 {
    public AppUpgrade_94() {
        updatePref(Prefs.PREF_ENC_SIGNED_IN_EMAIL);
        updatePref(Prefs.PREF_ENC_SIGNED_IN_ACCOUNT_TYPE);
        updatePref(Prefs.PREF_ENC_PRO);
        updatePref(Prefs.PREF_ENC_FORGOT_SECURITY_CODE_EMAIL);
        updatePref(Prefs.PREF_ENC_SECURITY_CODE);
    }

    private void updatePref(String preferenceName) {
        try {
            String encryptedPrefValue = MyApp.getInstance().prefs.getString(preferenceName, null);
            if (encryptedPrefValue != null) {
                String decryptedString = AES256Cipher.decodeString(encryptedPrefValue,  Static.getDeprecatedDeviceUid());
                // Save pref with new device uid
                String encryptedString = AES256Cipher.encodeString(decryptedString, MyDevice.getInstance().deviceUid);
                MyApp.getInstance().prefs.edit().putString(preferenceName, encryptedString).apply();
            }
        } catch (Exception e) {
            AppLog.d("Exception: " + e);
        }
    }
}
