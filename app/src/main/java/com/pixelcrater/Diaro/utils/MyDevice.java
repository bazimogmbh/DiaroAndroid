package com.pixelcrater.Diaro.utils;

import android.os.Build;
import android.provider.Settings;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.pixelcrater.Diaro.MyApp;

public class MyDevice {

    private static MyDevice instance = new MyDevice();

    public String deviceUid;
    public String deviceName;
    public String deviceOS;
    public String appVersion;

    /**
     * Gets current device information
     */
    public MyDevice() {
        // This is a 64-bit quantity that is generated and stored when the device first boots.
        // It is reset when the device is wiped.
        String androidId = Settings.Secure.getString(MyApp.getInstance().getContentResolver(),  Settings.Secure.ANDROID_ID);

        if (androidId == null) {
            androidId = "";
        }
        deviceUid = Static.md5(androidId);

        // Device name
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;

        deviceName = manufacturer + " " + model;
        if (model.startsWith(manufacturer)) {
            deviceName = capitalize(model);
        } else {
            deviceName = capitalize(manufacturer) + " " + model;
        }

        // Android version
        deviceOS = "Android " + android.os.Build.VERSION.RELEASE +  " (" + android.os.Build.VERSION.SDK_INT + ")";
        // App version name
        appVersion = Static.getAppVersionName();

        // App version code
        int versionCode = Static.getAppVersionCode();
        if (versionCode != 0) {
            appVersion += " (" + versionCode + ")";
        }

        AppLog.d("deviceUid: " + deviceUid +  ", deviceName: " + deviceName +   ", deviceOS: " + deviceOS + ", appVersion: " + appVersion +", heapSize: " + Static.getHeapSizeInMB() + "MB" +
                 ", isGooglePlayServicesAvailable(): " + isGooglePlayServicesAvailable());
    }

    public static MyDevice getInstance() {
        return instance;
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public boolean isGooglePlayServicesAvailable() {
        try {
            return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MyApp.getInstance()) == ConnectionResult.SUCCESS;
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }
        return false;
    }
}
