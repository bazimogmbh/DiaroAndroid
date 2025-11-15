package com.pixelcrater.Diaro.storage.dropbox;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;

import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.WorkManager;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.notifications.NotificationsMgr;
import com.pixelcrater.Diaro.utils.AppLog;

public class SyncService extends Service implements SyncAsync.OnAsyncInteractionListener, OnFsSyncStatusChangeListener, MyApp.onAppStateChangedListener {

    public SyncService() {
    }

    public static void startService() {
        Intent intent = new Intent(MyApp.getInstance(), SyncService.class);

        MyApp.getInstance().startService (intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        AppLog.d("intent: " + intent);

        if (MyApp.getInstance().networkStateMgr.isConnectedToInternet()) {
            // Sync with Dropbox
            MyApp.getInstance().asyncsMgr.executeIfNotRunningSyncAsync(this);

            startStopForeground();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        AppLog.d("");

        // Clear MyApp listener
        if (MyApp.getInstance().storageMgr.dbxFsAdapter != null) {
            MyApp.getInstance().storageMgr.dbxFsAdapter.notifyOnFsSyncStatusChangeListeners();
        }
        MyApp.getInstance().setOnAppStateChangedListener(null);
    }

    @Override
    public void onSyncAsyncFinished() {
        //  AppLog.e("onSyncAsyncFinished");

        stopSelf();
    }

    @Override
    public void onFsSyncStatusChange() {
        if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_SHOW_SYNC_NOTIFICATION, false)) {
            MyApp.getInstance().notificationsMgr.notificationManager.notify(NotificationsMgr.NOTIFICATION_SYNCING, MyApp.getInstance().notificationsMgr.syncNotification.getNotification());
        }
    }

    @Override
    public void onAppStateChanged() {
        startStopForeground();
    }

    private void startStopForeground() {
//        AppLog.d("isAppVisible: " + MyApp.getInstance().isAppVisible() +
//                ", isSyncAsyncRunning(): " + MyApp.getInstance().asyncsMgr.isSyncAsyncRunning());

        if (MyApp.getInstance().asyncsMgr.isSyncAsyncRunning()) {
            if (MyApp.getInstance().isAppVisible()) {
                // Use new API for stopping foreground service
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(Service.STOP_FOREGROUND_REMOVE);
                } else {
                    stopForeground(true);
                }
            } else {
                if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_SHOW_SYNC_NOTIFICATION, false)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        try {
                            startForeground(NotificationsMgr.NOTIFICATION_SYNCING,
                                MyApp.getInstance().notificationsMgr.syncNotification.getNotification(),
                                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
                        } catch (Exception e){
                            AppLog.e("Foreground service error: " + e.getMessage());
                        }
                    } else {
                        startForeground(NotificationsMgr.NOTIFICATION_SYNCING,
                            MyApp.getInstance().notificationsMgr.syncNotification.getNotification());
                    }

                }
            }

            // Set MyApp listener
            MyApp.getInstance().setOnAppStateChangedListener(this);
        } else {
            stopSelf();
        }
    }


}
