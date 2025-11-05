package com.pixelcrater.Diaro.storage.dropbox;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.notifications.NotificationsMgr;
import com.pixelcrater.Diaro.utils.AppLog;

public class SyncWorker extends Worker implements SyncAsync.OnAsyncInteractionListener, OnFsSyncStatusChangeListener, MyApp.onAppStateChangedListener  {

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (MyApp.getInstance().networkStateMgr.isConnectedToInternet()) {
            // Sync with Dropbox
            MyApp.getInstance().asyncsMgr.executeIfNotRunningSyncAsync(MyApp.getInstance());

            if (MyApp.getInstance().asyncsMgr.isSyncAsyncRunning()) {
                if (MyApp.getInstance().isAppVisible()) {
                    // stopForeground(true);
                } else {
                    if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_SHOW_SYNC_NOTIFICATION, false)) {
                        setForegroundAsync(createForegroundInfo());
                        // startForeground(NotificationsMgr.NOTIFICATION_SYNCING, MyApp.getInstance().notificationsMgr.syncNotification.getNotification());
                    }
                }

                // Set MyApp listener
                MyApp.getInstance().setOnAppStateChangedListenerNew(this);
            } else {
                //  stopSelf();
            }
        }

        return Result.success();
    }

    @Override
    public void onSyncAsyncFinished() {
          AppLog.e("onSyncAsyncFinished");

      //  stopSelf();
    }

    @Override
    public void onFsSyncStatusChange() {
        if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_SHOW_SYNC_NOTIFICATION, false)) {
            MyApp.getInstance().notificationsMgr.notificationManager.notify(NotificationsMgr.NOTIFICATION_SYNCING, MyApp.getInstance().notificationsMgr.syncNotification.getNotification());
        }
    }

    @Override
    public void onAppStateChanged() {
       // startStopForeground();
    }

    @NonNull
    private ForegroundInfo createForegroundInfo() {
        return new ForegroundInfo(NotificationsMgr.NOTIFICATION_SYNCING,  MyApp.getInstance().notificationsMgr.syncNotification.getNotification());
    }


}
