package com.pixelcrater.Diaro.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import com.pixelcrater.Diaro.MyApp;

import java.util.Objects;

public class NotificationsMgr {

    // Notification IDs
    public static final int NOTIFICATION_DIARO_ONGOING = 1;
    public static final int NOTIFICATION_TIME_TO_WRITE = 2;
    public static final int NOTIFICATION_SYNCING = 4;
    public static final int NOTIFICATION_UPLOADING_BACKUP = 5;
    public static final int NOTIFICATION_DOWNLOADING_BACKUP = 6;
    public static final int NOTIFICATION_ON_THIS_DAY = 6;

    public final NotificationManager notificationManager;
    public final BackupNotification backupNotification;
    public final SyncNotification syncNotification;
    public final AddEntryOngoingNotification addEntryOngoingNotification;
    public final TimeToWriteNotification timeToWriteNotification;

    public static String channelID = "DiaroNotificationsChannel";

    public NotificationsMgr() {
        notificationManager = (NotificationManager)MyApp.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        backupNotification = new BackupNotification(this);
        syncNotification = new SyncNotification(this);
        addEntryOngoingNotification = new AddEntryOngoingNotification(this);
        timeToWriteNotification = new TimeToWriteNotification(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelID, "Diaro Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Diaro");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            Objects.requireNonNull(notificationManager).createNotificationChannel(notificationChannel);
        }
    }

}
