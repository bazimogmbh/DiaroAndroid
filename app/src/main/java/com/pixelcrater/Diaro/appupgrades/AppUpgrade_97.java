package com.pixelcrater.Diaro.appupgrades;

import android.provider.Settings;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.utils.AES256Cipher;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyDevice;
import com.pixelcrater.Diaro.utils.storage.StorageUtils;

import java.io.File;

public class AppUpgrade_97 {
    public AppUpgrade_97() {
        // Move all photos from deprecated directory (if exists)
        moveAllPhotosFromDeprecatedDirectory();

        updatePref(Prefs.PREF_ENC_SIGNED_IN_EMAIL);
        updatePref(Prefs.PREF_ENC_SIGNED_IN_ACCOUNT_TYPE);
        updatePref(Prefs.PREF_ENC_PRO);
        updatePref(Prefs.PREF_ENC_FORGOT_SECURITY_CODE_EMAIL);
        updatePref(Prefs.PREF_ENC_SECURITY_CODE);
    }

    private void moveAllPhotosFromDeprecatedDirectory() {
        try {
            File deprecatedDir = new File(getDeprecatedMediaPhotosDirPath());
            AppLog.d("deprecatedDir.getPath(): " + deprecatedDir.getPath() +  ", deprecatedDir.exists(): " + deprecatedDir.exists());

            if (deprecatedDir.exists()) {
                // Move photos to new directory
                Static.moveAllPhotos(getDeprecatedMediaPhotosDirPath());

                // Delete deprecated "/media/photos" directory
                StorageUtils.deleteFileOrDirectory(new File(getDeprecatedMediaPhotosDirPath()));
            }
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }
    }

    private String getDeprecatedMediaPhotosDirPath() {
        return StorageUtils.getDefaultExternalStorage() +  "/Android/data/com.pixelcrater.Diaro/media/photos";
    }

    private void updatePref(String preferenceName) {
        try {
            String androidId = Settings.Secure.getString(MyApp.getInstance().getContentResolver(),  Settings.Secure.ANDROID_ID);
            String encryptedPrefValue = MyApp.getInstance().prefs.getString(preferenceName, null);
            if (encryptedPrefValue != null) {
                String decryptedString = AES256Cipher.decodeString(encryptedPrefValue, androidId);

                // Save pref with new device uid
                String encryptedString = AES256Cipher.encodeString(decryptedString, MyDevice.getInstance().deviceUid);
                MyApp.getInstance().prefs.edit().putString(preferenceName, encryptedString).apply();
            }
        } catch (Exception e) {
            AppLog.d("Exception: " + e);
        }
    }
}
