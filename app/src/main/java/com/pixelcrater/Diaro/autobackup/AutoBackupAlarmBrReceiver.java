package com.pixelcrater.Diaro.autobackup;

import static com.pixelcrater.Diaro.config.GlobalConstants.DROPBOX_PATH_BACKUP;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.analytics.AnalyticsConstants;
import com.pixelcrater.Diaro.storage.dropbox.DropboxAccountManager;
import com.pixelcrater.Diaro.storage.dropbox.jobs.UploadFileToDbxWorker;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.storage.PermanentStorageUtils;

import org.joda.time.DateTime;

import java.io.File;
import java.util.Objects;

public class AutoBackupAlarmBrReceiver extends BroadcastReceiver {

    public static void scheduleNextAutoBackupAlarm() {
        AppLog.d("");

        // Cancel current alarm
        cancelCurrentAlarm();

        DateTime nextDt = new DateTime().withTimeAtStartOfDay().plusDays(1);
        AlarmManager alarmManager = (AlarmManager) MyApp.getInstance().getSystemService(Context.ALARM_SERVICE);

        final int flag =  Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT;

        Intent i = new Intent(MyApp.getInstance(), AutoBackupAlarmBrReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(MyApp.getInstance(), Static.REQUEST_AUTO_BACKUP, i, flag);
        Objects.requireNonNull(alarmManager).set(AlarmManager.RTC_WAKEUP, nextDt.getMillis(), pi);

        AppLog.e("Next auto backup alarm set at: " + nextDt.toString("yyyy.MM.dd HH:mm:ss"));
    }

    public static void cancelCurrentAlarm() {
        AppLog.d("");
        final int flag =  Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT;

        Intent intent = new Intent(MyApp.getInstance(), AutoBackupAlarmBrReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(MyApp.getInstance(), Static.REQUEST_AUTO_BACKUP, intent, flag);
        AlarmManager alarmManager = (AlarmManager) MyApp.getInstance().getSystemService(Context.ALARM_SERVICE);
        Objects.requireNonNull(alarmManager).cancel(sender);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AppLog.d("");
        createBackupInBackground();
    }

    @SuppressLint("MissingPermission")
    private static void createBackupInBackground() {
        MyApp.executeInBackground(() -> {
            try {
                // Create daily backup for current day of week
                DateTime dt = DateTime.now();
                int dayOfWeek = dt.getDayOfWeek();
                String dayOfWeekTitle = Static.getEnglishWeekDaysArrayList().get(dayOfWeek);
                AppLog.e("dayOfWeek: " + dayOfWeek + ", dayOfWeekTitle: " + dayOfWeekTitle);
              //  BackupRestore.createBackup("auto_" + dayOfWeekTitle, true, true);

                // Create monthly backup  || dt.getDayOfMonth() == 15
                if (dt.getDayOfMonth() == 1  ) {
                    File backupFile = BackupRestore.createBackup("auto_" + dt.toString("yyyyMMdd"), false, true);
                    if (DropboxAccountManager.isLoggedIn(MyApp.getInstance())) {

                        // Upload backup file to Dropbox, make sure the backups are not lost if non pro users lose device !
                        Uri fileUri = Uri.parse(backupFile.getPath());
                        String filename = PermanentStorageUtils.getBackupFilename(fileUri);
                        String mDropboxFilePath = DROPBOX_PATH_BACKUP + "/" + filename;

                        Data data = new Data.Builder().putString("localPath", backupFile.getPath()).putString("dbxPath", mDropboxFilePath).build();
                        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
                        OneTimeWorkRequest uploadWorkRequest = new OneTimeWorkRequest.Builder(UploadFileToDbxWorker.class).setConstraints(constraints).setInputData(data).build();
                        WorkManager.getInstance(MyApp.getInstance().getApplicationContext()).enqueue(uploadWorkRequest);

                        FirebaseAnalytics.getInstance(MyApp.getInstance()).logEvent(AnalyticsConstants.EVENT_LOG_UPLOAD_BACKUP_DROPBOX, new Bundle());
                    }
                }
            } catch (Exception e) {
                AppLog.e("Exception: " + e);
            }

            // Schedule next auto backup in AlarmManager
            scheduleNextAutoBackupAlarm();
        });
    }
}
