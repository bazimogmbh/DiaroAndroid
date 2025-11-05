package com.pixelcrater.Diaro.brreceivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.AppLog;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;

public class TimeToWriteAlarmBrReceiver extends BroadcastReceiver {

    public static void scheduleNextTimeToWriteAlarm() {
        AppLog.d("");

        // Cancel current alarm
        cancelCurrentAlarm();

        // If notification is disabled
        if (!MyApp.getInstance().prefs.getBoolean(Prefs.PREF_TIME_TO_WRITE_NOTIFICATION_ENABLED, true)) {
            return;
        }

        // Get days of week set in preferences, 1 - SUNDAY, 2 - MONDAY, ..., 7 - SATURDAY
        String serializedWeekDays = MyApp.getInstance().prefs.getString( Prefs.PREF_TIME_TO_WRITE_NOTIFICATION_WEEKDAYS, "1,2,3,4,5,6,7");
        AppLog.d("serializedWeekDays: " + serializedWeekDays);

        // If no weekday selected
        if (serializedWeekDays.equals("")) {
            return;
        }

        ArrayList<String> weekdaysArrayList = new ArrayList<>( Arrays.asList(serializedWeekDays.split(",")));
        AppLog.d("weekdaysArrayList: " + weekdaysArrayList);

        // Time from preferences
        long timeFromPrefs = MyApp.getInstance().prefs.getLong(Prefs.PREF_TIME_TO_WRITE_NOTIFICATION_TIME, 0);

        // Default time
        int hh = 20;
        int mm = 0;

        // If time is set in preferences
        if (timeFromPrefs > 0) {
            DateTime dt = new DateTime(timeFromPrefs);
            hh = dt.getHourOfDay();
            mm = dt.getMinuteOfHour();
        }
//		AppLog.d("hh: " + hh + ", mm: " + mm);

        // Get current time and day of week
        DateTime nextDt = new DateTime();

        int nowHH = nextDt.getHourOfDay();
        int nowMM = nextDt.getMinuteOfHour();

        int nowDayOfWeek = nextDt.getDayOfWeek();
//		AppLog.d("nowHH: " + nowHH + ", nowMM: " + nowMM + ", nowDayOfWeek: " + nowDayOfWeek);

        // Find upcoming day when the next alarm should be set
        int nextDayOfWeekInCalendar = Static.convertDayOfWeekFromJodaTimeToCalendar(nowDayOfWeek);
        int iteration = 0;
        while (true) {
//            AppLog.d("nextDayOfWeekInCalendar: " + nextDayOfWeekInCalendar);

            if (weekdaysArrayList.contains(String.valueOf(nextDayOfWeekInCalendar))) {
                if (iteration > 0 || hh > nowHH || (hh == nowHH && mm > nowMM)) {
                    break;
                }
            }

            // Add one day to the date/calendar
            nextDt = nextDt.plusDays(1);

            nextDayOfWeekInCalendar = Static.convertDayOfWeekFromJodaTimeToCalendar(nextDt.getDayOfWeek());

            iteration++;
        }

        // Set next 'Time to write' alarm
        try {
            nextDt = nextDt.withTime(hh, mm, 0, 0);
        }
        // Throws exception during Daylight Saving Times
        catch (Exception e) {
            nextDt = nextDt.withTime(hh + 1, mm, 0, 0);
        }

        AlarmManager alarmManager = (AlarmManager) MyApp.getInstance().getSystemService( Context.ALARM_SERVICE);

        final int flag =  Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT;
        Intent i = new Intent(MyApp.getInstance(), TimeToWriteAlarmBrReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(MyApp.getInstance(), Static.REQUEST_SHOW_TIME_TO_WRITE_NOTIFICATION, i, flag);
        alarmManager.set(AlarmManager.RTC_WAKEUP, nextDt.getMillis(), pi);

        AppLog.d("Next time to write alarm set at: " + nextDt.toString("yyyy.MM.dd HH:mm:ss"));
    }

    public static void cancelCurrentAlarm() {
        final int flag =  Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT;

        Intent intent = new Intent(MyApp.getInstance(), TimeToWriteAlarmBrReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(MyApp.getInstance(), Static.REQUEST_SHOW_TIME_TO_WRITE_NOTIFICATION, intent, flag);

        AlarmManager alarmManager = (AlarmManager) MyApp.getInstance().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AppLog.d("existsEntryOnThisDay(): " + existsEntryOnThisDay());

        boolean smartReminderEnabled = MyApp.getInstance().prefs.getBoolean(Prefs.PREF_TIME_TO_WRITE_NOTIFICATION_SMART_REMINDER, true);
        if (!(smartReminderEnabled && existsEntryOnThisDay())) {
            // Show notification
            MyApp.getInstance().notificationsMgr.timeToWriteNotification.showNotification();
        }

        // Schedule next 'Time to write' notification in AlarmManager
        scheduleNextTimeToWriteAlarm();
    }

    private boolean existsEntryOnThisDay() {
        DateTime todayDt = new DateTime().withTimeAtStartOfDay();
        long fromMillis = todayDt.getMillis();
        long toMillis = todayDt.plusDays(1).getMillis() - 1;

        String where = "WHERE " + Tables.KEY_ENTRY_ARCHIVED + "=0";
        if (fromMillis > 0) {
            where += " AND " + Tables.KEY_ENTRY_DATE + ">=" + fromMillis;
        }
        if (toMillis > 0) {
            where += " AND " + Tables.KEY_ENTRY_DATE + "<=" + toMillis;
        }

        return !MyApp.getInstance().storageMgr.getSQLiteAdapter().getSingleRowColumnValue(Tables.TABLE_ENTRIES, Tables.KEY_UID, where, null).equals("");
    }
}
