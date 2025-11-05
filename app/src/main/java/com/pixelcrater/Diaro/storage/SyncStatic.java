package com.pixelcrater.Diaro.storage;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.config.Prefs;

public class SyncStatic {

    public static final int STATUS_OFF = 0;
    public static final int STATUS_CONNECTED = 1;
    public static final int STATUS_DISCONNECTED = 2;
    public static final int STATUS_SYNCING = 3;
    public static final int STATUS_NOT_ON_WIFI = 4;
    public static final int STATUS_ROAMING = 5;

    public static int getFsSyncStatus() {
        if (MyApp.getInstance().storageMgr.getDbxFsAdapter() != null) {
            if (MyApp.getInstance().networkStateMgr.isConnectedToInternet()) {
                if (isRoamingSyncPrefOk()) {
                    if (isWifiOnlySyncPrefOk()) {
                        if (MyApp.getInstance().asyncsMgr.isSyncAsyncRunning()) {
                            return STATUS_SYNCING;
                        }
                        return STATUS_CONNECTED;
                    }
                    return STATUS_NOT_ON_WIFI;
                }
                return STATUS_ROAMING;
            }
            return STATUS_DISCONNECTED;
        }
        return STATUS_OFF;
    }

    public static boolean isSyncPrefsOk() {
        return isWifiOnlySyncPrefOk() && isRoamingSyncPrefOk();
    }

    public static boolean isWifiOnlySyncPrefOk() {
        boolean syncOnWiFiOnlyPref = MyApp.getInstance().prefs.getBoolean(Prefs.PREF_SYNC_ON_WIFI_ONLY, true);
        return !(syncOnWiFiOnlyPref && !MyApp.getInstance().networkStateMgr.isConnectedToInternetUsingWiFi());
    }

    public static boolean isRoamingSyncPrefOk() {
        boolean allowRoamingSyncPref = MyApp.getInstance().prefs.getBoolean( Prefs.PREF_ALLOW_ROAMING_SYNC, true);
        return !(!allowRoamingSyncPref && MyApp.getInstance().networkStateMgr.isConnectedToInternetUsingDataNetwork() && Static.IsNetworkRoaming());
    }

    public static int getSyncStatusColorResId(int syncStatus) {
        switch (syncStatus) {
            case STATUS_CONNECTED:
                return R.color.md_green_400;

            case STATUS_DISCONNECTED:
                return R.color.md_red_400;

            case STATUS_SYNCING:
                return R.color.md_orange_700;
        }

        return R.color.md_grey_500;
    }

    public static String getFsSyncStatusText(int syncStatus) {
        switch (syncStatus) {
            case STATUS_CONNECTED:
                return MyApp.getInstance().getString(R.string.connected);

            case STATUS_DISCONNECTED:
                return MyApp.getInstance().getString(R.string.disconnected);

            case STATUS_SYNCING:
                return MyApp.getInstance().storageMgr.getDbxFsAdapter().getVisibleSyncStatusText() + " " + MyApp.getInstance().storageMgr.getDbxFsAdapter().getVisibleSyncPercents();

            case STATUS_NOT_ON_WIFI:
                return MyApp.getInstance().getString(R.string.waiting_for_wifi);

            case STATUS_ROAMING:
                return MyApp.getInstance().getString(R.string.sync_while_roaming_not_allowed);
        }

        return MyApp.getInstance().getString(R.string.turned_off);
    }

    public static void throwIfSyncOnWiFiOnlyPrefNotOk() throws Exception {
        if (!SyncStatic.isSyncPrefsOk()) {
            throw new Exception(MyApp.getInstance().getString(
                    R.string.wifi_only_sync_enabled_but_not_connected_on_wifi));
        }
    }
}
