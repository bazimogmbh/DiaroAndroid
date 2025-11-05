package com.pixelcrater.Diaro.notifications

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.pixelcrater.Diaro.MyApp
import com.pixelcrater.Diaro.R
import com.pixelcrater.Diaro.backuprestore.BackupRestoreActivity
import com.pixelcrater.Diaro.utils.Static

class BackupNotification(private val mNotificationsMgr: NotificationsMgr) {

    /**
     * Upload backup file notifications
     */
    fun showHideUploadBackupNotification() {
        if (!MyApp.getInstance().isAppVisible && MyApp.getInstance().asyncsMgr.isUploadBackupAsyncRunning) {
            // Show upload backup file notification
            showUploadBackupNotification()
        } else {
            cancelUploadNotification()
        }
    }

    private fun showUploadBackupNotification() {
//        // Creates an explicit intent for an Activity in your app
        val intent = Intent(MyApp.getInstance(), BackupRestoreActivity::class.java)
        intent.putExtra(Static.EXTRA_SKIP_SC, true)
        val flag =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getActivity(MyApp.getInstance(), Static.REQUEST_SETTINGS_BACKUP_RESTORE, intent, flag)
        val percents = Static.getPercents(MyApp.getInstance().asyncsMgr.uploadBackupFileToDropboxAsync.bytesTransferred, MyApp.getInstance().asyncsMgr.uploadBackupFileToDropboxAsync.bytesTotal)
        val totalKB = Static.byteToKB(MyApp.getInstance().asyncsMgr.uploadBackupFileToDropboxAsync.bytesTotal).toInt()
        val transferredKB = Static.byteToKB(MyApp.getInstance().asyncsMgr.uploadBackupFileToDropboxAsync.bytesTransferred).toInt()
        val builder = NotificationCompat.Builder(MyApp.getInstance(), NotificationsMgr.channelID)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_stat_diaro_icon)
                .setContentTitle(MyApp.getInstance().getString(R.string.app_title))
                .setContentText(MyApp.getInstance().asyncsMgr.uploadBackupFileToDropboxAsync.message)
                .setContentInfo("$percents%")
                .setUsesChronometer(true)
                .setWhen(MyApp.getInstance().asyncsMgr.uploadBackupFileToDropboxAsync.startMillis)
                .setProgress(totalKB, transferredKB, false)
        builder.setOngoing(true)
        builder.setSound(null)

        // Show notification
        mNotificationsMgr.notificationManager.notify(NotificationsMgr.NOTIFICATION_UPLOADING_BACKUP, builder.build())
    }

    private fun cancelUploadNotification() {
        mNotificationsMgr.notificationManager.cancel(NotificationsMgr.NOTIFICATION_UPLOADING_BACKUP)
    }

    /**
     * Download backup file notifications
     */
    fun showHideDownloadBackupNotification() {
        if (!MyApp.getInstance().isAppVisible && MyApp.getInstance().asyncsMgr.isDownloadBackupAsyncRunning) {
            // Show download backup file notification
            showDownloadBackupNotification()
        } else {
            cancelDownloadNotification()
        }
    }

    private fun showDownloadBackupNotification() {
//        // Creates an explicit intent for an Activity in your app
        val intent = Intent(MyApp.getInstance(), BackupRestoreActivity::class.java)
        intent.putExtra(Static.EXTRA_SKIP_SC, true)
        val flag =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        val pendingIntent = PendingIntent.getActivity(MyApp.getInstance(), Static.REQUEST_SETTINGS_BACKUP_RESTORE, intent, flag)
        val percents = Static.getPercents(MyApp.getInstance().asyncsMgr.downloadBackupFileFromDropboxAsync.bytesTransferred, MyApp.getInstance().asyncsMgr.downloadBackupFileFromDropboxAsync.bytesTotal)
        val builder = NotificationCompat.Builder(MyApp.getInstance(), NotificationsMgr.channelID)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_stat_diaro_icon)
                .setContentTitle(MyApp.getInstance().getString(R.string.app_title))
                .setContentText(MyApp.getInstance().asyncsMgr.downloadBackupFileFromDropboxAsync.message)
                .setContentInfo("$percents%")
                .setUsesChronometer(true)
                .setWhen(MyApp.getInstance().asyncsMgr.downloadBackupFileFromDropboxAsync.startMillis)
                .setProgress(Static.byteToKB(MyApp.getInstance().asyncsMgr.downloadBackupFileFromDropboxAsync.bytesTotal).toInt(),
                        Static.byteToKB(MyApp.getInstance().asyncsMgr.downloadBackupFileFromDropboxAsync.bytesTransferred).toInt(),
                        false)
        builder.setOngoing(true)
        builder.setSound(null)

        // Show notification
        mNotificationsMgr.notificationManager.notify(NotificationsMgr.NOTIFICATION_DOWNLOADING_BACKUP, builder.build())
    }

    private fun cancelDownloadNotification() {
        mNotificationsMgr.notificationManager.cancel(NotificationsMgr.NOTIFICATION_DOWNLOADING_BACKUP)
    }

}