package com.pixelcrater.Diaro.notifications

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.pixelcrater.Diaro.MyApp
import com.pixelcrater.Diaro.R
import com.pixelcrater.Diaro.config.Prefs
import com.pixelcrater.Diaro.main.SplashActivity

class AddEntryOngoingNotification(private val mNotificationsMgr: NotificationsMgr) {
    fun showNotification() {

        // Creates an explicit intent for an Activity in your app
        val intent = Intent(MyApp.getInstance(), SplashActivity::class.java)
        intent.action = "ACTION_NEW"
        intent.putExtra("widget", true)
        val flag =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getActivity(MyApp.getInstance(), 0, intent, flag)
        val builder = NotificationCompat.Builder(MyApp.getInstance(), NotificationsMgr.channelID).setContentIntent(pendingIntent).setSmallIcon(R.drawable.ic_stat_diaro_icon).setContentTitle(MyApp.getInstance().getText(R.string.app_title)).setContentText(MyApp.getInstance().getText(R.string.app_notification))
        builder.setOngoing(true)
        builder.setSound(null)

        // Show notification
        mNotificationsMgr.notificationManager.notify(NotificationsMgr.NOTIFICATION_DIARO_ONGOING, builder.build())
    }

    fun cancelNotification() {
        mNotificationsMgr.notificationManager.cancel(NotificationsMgr.NOTIFICATION_DIARO_ONGOING)
    }

    init {
        if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ADD_ENTRY_ONGOING_NOTIFICATION_ICON, false)) {
            showNotification()
        }
    }
}