package com.pixelcrater.Diaro.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.pixelcrater.Diaro.MyApp
import com.pixelcrater.Diaro.R
import com.pixelcrater.Diaro.config.Prefs
import com.pixelcrater.Diaro.main.SplashActivity
import com.pixelcrater.Diaro.utils.AppLog
import java.util.*

class TimeToWriteNotification(private val mNotificationsMgr: NotificationsMgr) {

    fun showNotification() {
        val intent = Intent(MyApp.getInstance(), SplashActivity::class.java)
        intent.action = "ACTION_NEW"
        intent.putExtra("widget", true)
        val flag =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getActivity(MyApp.getInstance(), 0, intent, flag)
        val builder = NotificationCompat.Builder(MyApp.getInstance(), NotificationsMgr.channelID)
        builder.setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_stat_diaro_icon)
                .setContentTitle(greetingMessage)
                .setContentText(MyApp.getInstance().getText(R.string.notification_reminder_text))
        // .addAction(R.drawable.ic_snooze, getString(R.string.snooze),  snoozePendingIntent);
        if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_TIME_TO_WRITE_NOTIFICATION_MUTE_SOUND, false)) {
            builder.setSound(null)
        } else {
            builder.setDefaults(Notification.DEFAULT_SOUND)
        }

        // Show notification
        mNotificationsMgr.notificationManager.notify(NotificationsMgr.NOTIFICATION_TIME_TO_WRITE, builder.build())
    }

    private val greetingMessage: String
        get() {
            var retVal = MyApp.getInstance().getText(R.string.notification_hello_default).toString()
            when (Calendar.getInstance()[Calendar.HOUR_OF_DAY]) {
                in 0..11 -> {
                    retVal = MyApp.getInstance().getText(R.string.notification_hello_good_morning).toString()
                }
                in 12..15 -> {
                    retVal = MyApp.getInstance().getText(R.string.notification_hello_good_afternoon).toString()
                }
                in 16..24 -> {
                    retVal = MyApp.getInstance().getText(R.string.notification_hello_good_evening).toString() + " " + MyApp.getInstance().getText(R.string.notification_how_was_your_day).toString()
                }
            }
            return retVal
        }

}