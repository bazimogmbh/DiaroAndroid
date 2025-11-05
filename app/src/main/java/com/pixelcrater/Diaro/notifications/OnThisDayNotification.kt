package com.pixelcrater.Diaro.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.pixelcrater.Diaro.MyApp
import com.pixelcrater.Diaro.R
import com.pixelcrater.Diaro.config.Prefs
import com.pixelcrater.Diaro.onthisday.OnThisDayActivity
import com.pixelcrater.Diaro.settings.PreferencesHelper
import com.pixelcrater.Diaro.storage.sqlite.SQLiteQueryHelper

class OnThisDayNotification(private val mNotificationsMgr: NotificationsMgr) {

    fun showNotification() {

        if (PreferencesHelper.isOnThisDayEnabled()) {

            val onThisDayEntryCount = SQLiteQueryHelper.getEntriesByThisDayCount(false)
            if (onThisDayEntryCount > 0) {

                val title = MyApp.getInstance().getText(R.string.on_this_day)
                val msg = MyApp.getInstance().resources.getQuantityString(R.plurals.entry_on_this_day, onThisDayEntryCount, onThisDayEntryCount)

                val intent = Intent(MyApp.getInstance(), OnThisDayActivity::class.java)
                val flag =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
                val pendingIntent = PendingIntent.getActivity(MyApp.getInstance(), 0, intent, flag)
                val builder = NotificationCompat.Builder(MyApp.getInstance(), NotificationsMgr.channelID)
                builder.setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(R.drawable.ic_stat_diaro_icon)
                        .setContentTitle(title)
                        .setContentText(msg)
                if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_TIME_TO_WRITE_NOTIFICATION_MUTE_SOUND, false)) {
                    builder.setSound(null)
                } else {
                    builder.setDefaults(Notification.DEFAULT_SOUND)
                }

                // Show notification
                mNotificationsMgr.notificationManager.notify(NotificationsMgr.NOTIFICATION_ON_THIS_DAY, builder.build())

            }


        }



    }

}