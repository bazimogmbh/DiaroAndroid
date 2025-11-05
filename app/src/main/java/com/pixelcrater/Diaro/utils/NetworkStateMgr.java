package com.pixelcrater.Diaro.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.core.content.ContextCompat;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.storage.dropbox.DropboxAccountManager;
import com.pixelcrater.Diaro.storage.dropbox.SyncService;

public class NetworkStateMgr {
    private final ConnectivityManager cm;
    private NetworkStateChangeBrReceiver networkStateChangeBrReceiver;
    private NetworkInfo activeNetwork;

    public NetworkStateMgr() {
        cm = (ConnectivityManager) MyApp.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = cm.getActiveNetworkInfo();
    }

    public synchronized boolean isConnectedToInternetUsingDataNetwork() {
        return activeNetwork != null && activeNetwork.isConnected() && activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE;

    }

    public synchronized boolean isConnectedToInternet() {
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public synchronized boolean isConnectedToInternetUsingWiFi() {
        return activeNetwork != null && activeNetwork.isConnected() && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public void registerOnNetworkStateChangeReceiver() {
        if (networkStateChangeBrReceiver != null) {
            return;
        }

        // Register network state changed broadcast receiver
        IntentFilter filters = new IntentFilter();
        filters.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkStateChangeBrReceiver = new NetworkStateChangeBrReceiver();
        ContextCompat.registerReceiver(MyApp.getInstance(),networkStateChangeBrReceiver, filters, ContextCompat.RECEIVER_EXPORTED);
    }

    public void unregisterOnNetworkStateChangeReceiver() {
        try {
            MyApp.getInstance().unregisterReceiver(networkStateChangeBrReceiver);
            networkStateChangeBrReceiver = null;
        } catch (Exception e) {
        }
    }

    private class NetworkStateChangeBrReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            activeNetwork = cm.getActiveNetworkInfo();

            AppLog.d("isConnectedToInternet(): " + isConnectedToInternet() + ", isConnectedToInternetUsingWiFi(): " + isConnectedToInternetUsingWiFi());

            if (DropboxAccountManager.isLoggedIn(MyApp.getInstance())) {
                if (isConnectedToInternet()) {
                    // Start sync service
                    SyncService.startService();
                } else {
                    if (MyApp.getInstance().storageMgr.getDbxFsAdapter() != null) {
                        // Notify FS sync status listeners
                        MyApp.getInstance().storageMgr.getDbxFsAdapter().notifyOnFsSyncStatusChangeListeners();

                    }
                }
            }

        }
    }
}
