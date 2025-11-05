package com.pixelcrater.Diaro.utils;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import com.pixelcrater.Diaro.config.GlobalConstants;
import com.pixelcrater.Diaro.MyApp;

public class MyAppGuard {

    public static void killApp() {
        // Kill the process without warning. If someone changed the certificate
        // is better not to give a hint about why the app stopped working
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * Checks APK signature to protect from unauthorized APK rebuild
     */
    public static String checkSignature() {
        String sigChk = "B";

        Signature[] signature = new Signature[0];

        try {
            signature = MyApp.getInstance().getPackageManager().getPackageInfo(MyApp.getInstance().getPackageName(), PackageManager.GET_SIGNATURES).signatures;

            // Prints your signature. Remove once you know this and have changed it below.
            AppLog.d("Signature: " + signature[0].hashCode());
        } catch (Exception ignored) {
        }

        // Debug, Release and Amazon DRM signatures
        if (signature[0].hashCode() == 1127808326
                || signature[0].hashCode() == 848587711
                || signature[0].hashCode() == -1093954028
                || signature[0].hashCode() == 1904986168
                || signature[0].hashCode() == 1041990297
                || signature[0].hashCode() == -1666549197
                || signature[0].hashCode() == -1492216391
                || signature[0].hashCode() == 1276035103
                || signature[0].hashCode() == -261209257
                || signature[0].hashCode() == -415390247
                || signature[0].hashCode() == -1083521806
        ) {
            sigChk = "G";
        }

        return sigChk;
    }

    public static void killIfInvalidSignature() {
        // 'G' encrypted is 'qYzVYNwvyK84T9osh6CsRQ==' (Lets make it difficult for crackers)
        try {
            if (!AES256Cipher.encodeString(MyAppGuard.checkSignature(), GlobalConstants.ENCRYPTION_KEY).equalsIgnoreCase("qYzVYNwvyK84T9osh6CsRQ==")) {
//            AppLog.e("Invalid build!");
             //   MyAppGuard.killApp();
            }

//            if (MyAppGuard.checkSignature().equalsIgnoreCase("B")) {
//                MyAppGuard.killApp();
//            }
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }
    }

    private boolean checkLuckyPatcher() {
        if (packageExists("com.dimonvideo.luckypatcher")) {
            return true;
        }

        if (packageExists("com.chelpus.lackypatch")) {
            return true;
        }

        if (packageExists("com.android.vending.billing.InAppBillingService.LACK")) {
            return true;
        }

        return false;
    }

    private boolean packageExists(final String packageName) {
        try {
            ApplicationInfo info = MyApp.getInstance().getPackageManager().getApplicationInfo(packageName, 0);

            if (info == null) {
                // No need really to test for null, if the package does not
                // exist it will really rise an exception. but in case Google
                // changes the API in the future lets be safe and test it
                return false;
            }

            return true;
        } catch (Exception ex) {
            // If we get here only means the Package does not exist
        }

        return false;
    }
}