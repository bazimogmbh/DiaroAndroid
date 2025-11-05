package com.pixelcrater.Diaro.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.pixelcrater.Diaro.MyApp
import com.pixelcrater.Diaro.R
import com.pixelcrater.Diaro.profile.ProfileActivity
import com.pixelcrater.Diaro.utils.Static

class SyncNotification(notificationsMgr: NotificationsMgr?) {
    // Creates an explicit intent for an Activity in your app
    val notification: Notification
        get() {
            // Creates an explicit intent for an Activity in your app
            val intent = Intent(MyApp.getInstance(), ProfileActivity::class.java)
            intent.putExtra(Static.EXTRA_SKIP_SC, true)
            val flag =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
            val pendingIntent = PendingIntent.getActivity(MyApp.getInstance(), Static.REQUEST_PROFILE, intent, flag)

            // This is the intent that is supposed to be called when the Stop sync button is clicked
            val stopSyncIntent = Intent(MyApp.getInstance(), StopSyncButtonListener::class.java)
            val pendingStopSyncIntent = PendingIntent.getBroadcast(MyApp.getInstance(), 0, stopSyncIntent, flag)
            val builder = NotificationCompat.Builder(MyApp.getInstance(), NotificationsMgr.channelID)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_stat_sync)
                    .setContentTitle(MyApp.getInstance().getString(R.string.diaro_sync))
                    .setUsesChronometer(true)
                    .setWhen(MyApp.getInstance().asyncsMgr.syncAsync.startMillis)
                    .addAction(R.drawable.ic_stop_grey600_32dp, MyApp.getInstance().getString(R.string.stop_sync), pendingStopSyncIntent)


            // Show progress in content info
            builder.setContentText(MyApp.getInstance().storageMgr.getDbxFsAdapter().visibleSyncStatusText)
            builder.setContentInfo(MyApp.getInstance().storageMgr.getDbxFsAdapter().visibleSyncPercents)
            builder.setOngoing(true)
            builder.setSound(null)
            return builder.build()
        }

    class StopSyncButtonListener : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            // Cancel sync
            MyApp.getInstance().asyncsMgr.cancelSyncAsync()
        }
    }
}