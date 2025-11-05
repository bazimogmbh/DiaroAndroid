package com.pixelcrater.Diaro.storage.dropbox;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.PowerManager;

import androidx.core.content.ContextCompat;

import com.birbit.android.jobqueue.network.NetworkEventProvider;
import com.birbit.android.jobqueue.network.NetworkUtil;
import com.pixelcrater.Diaro.MyApp;

/**
 * Created by abhishek9851 on 15.05.17.
 */

public class NetworkUtilImplCustom implements NetworkUtil, NetworkEventProvider {

    private Listener listener;

    public NetworkUtilImplCustom(Context context) {
        context = context.getApplicationContext();
        ContextCompat.registerReceiver(MyApp.getInstance(),new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                dispatchNetworkChange(context);
            }
        }, getNetworkIntentFilter(), ContextCompat.RECEIVER_EXPORTED);
    }


    void dispatchNetworkChange(Context context) {
        if (listener == null) {//shall not be but just be safe
            return;
        }
        //http://developer.android.com/reference/android/net/ConnectivityManager.html#EXTRA_NETWORK_INFO
        //Since NetworkInfo can vary based on UID, applications should always obtain network information
        // through getActiveNetworkInfo() or getAllNetworkInfo().
        listener.onNetworkChange(getNetworkStatus(context));
    }

    @Override
    public int getNetworkStatus(Context context) {
        if (isDozing(context)) {
            return NetworkUtil.DISCONNECTED;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo == null) {
            return NetworkUtil.DISCONNECTED;
        }
        if (netInfo.getType() == ConnectivityManager.TYPE_WIFI ||
                netInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
            return NetworkUtil.UNMETERED;
        }
        return NetworkUtil.METERED;
    }

    private static IntentFilter getNetworkIntentFilter() {
        IntentFilter networkIntentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        return networkIntentFilter;
    }

    /**
     * Returns true if the device is in Doze/Idle mode. Should be called before checking the network connection because
     * the ConnectionManager may report the device is connected when it isn't during Idle mode.
     */
    @TargetApi(23)
    private static boolean isDozing(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return powerManager.isDeviceIdleMode() &&
                    !powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        } else {
            return false;
        }
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }
}
