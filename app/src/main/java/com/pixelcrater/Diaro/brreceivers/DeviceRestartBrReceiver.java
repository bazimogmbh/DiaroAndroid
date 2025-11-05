package com.pixelcrater.Diaro.brreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pixelcrater.Diaro.utils.AppLog;

/**
 * This receiver is called on device restart
 * It's needed to create application context to show Diaro ongoing notification icon
 */
public class DeviceRestartBrReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AppLog.d("intent.getAction(): " + intent.getAction());

//        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {}
    }
}
