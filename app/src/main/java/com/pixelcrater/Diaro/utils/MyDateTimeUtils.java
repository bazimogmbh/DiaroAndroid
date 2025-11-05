package com.pixelcrater.Diaro.utils;

import android.text.format.DateFormat;

import com.pixelcrater.Diaro.MyApp;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.FormatUtils;

import java.util.Calendar;
import java.util.Date;

public class MyDateTimeUtils {

    public static boolean isTodayDate(long millis) {
        DateTime dt = new DateTime(millis).withTimeAtStartOfDay();
        DateTime todayDt = new DateTime().withTimeAtStartOfDay();

        return todayDt.getMillis() == dt.getMillis();
    }

    public static String getTimeFormat() {
        return DateFormat.is24HourFormat(MyApp.getInstance()) ? "HH:mm" : "hh:mm aaa";
    }

    public static String getTimeFormatWithSeconds() {
        return DateFormat.is24HourFormat(MyApp.getInstance()) ? "HH:mm:ss" : "hh:mm:ss aaa";
    }

    public static void fixTimeZone() {
        DateTime dt = new DateTime();

        Calendar calendar = Calendar.getInstance();
        int cOffset = calendar.getTimeZone().getOffset(dt.getMillis());
        int offset = DateTimeZone.getDefault().getOffset(dt.getMillis());
//        AppLog.d("cOffset: " + cOffset + ", offset: " + offset);

        if (cOffset != offset) {
            Period cPeriod = new Period(cOffset);
            int cOffsetHours = cPeriod.getHours();
            int cOffsetMinutes = cPeriod.getMinutes();
//            AppLog.d("cOffsetHours: " + cOffsetHours + ", cOffsetMinutes: " + cOffsetMinutes);

            DateTimeZone.setDefault(DateTimeZone.forOffsetHoursMinutes(cOffsetHours, cOffsetMinutes));
        }

    }

    /**
     * Returns current timezone offset in '+00:00' format
     */
    public static String getCurrentTimeZoneOffset(long millis) {
        int offset = DateTimeZone.getDefault().getOffset(millis);
        return MyDateTimeUtils.printTimeZoneOffset(offset);
    }

    /**
     * Returns given offset in seconds in '+00:00' format
     */
    public static String printTimeZoneOffset(int var0) {
        StringBuffer var1 = new StringBuffer();
        if (var0 >= 0) {
            var1.append('+');
        } else {
            var1.append('-');
            var0 = -var0;
        }

        int var2 = var0 / 3600000;
        FormatUtils.appendPaddedInteger(var1, var2, 2);
        var0 -= var2 * 3600000;
        int var3 = var0 / '\uea60';
        var1.append(':');
        FormatUtils.appendPaddedInteger(var1, var3, 2);
        var0 -= var3 * '\uea60';
        if (var0 == 0) {
            return var1.toString();
        } else {
            int var4 = var0 / 1000;
            var1.append(':');
            FormatUtils.appendPaddedInteger(var1, var4, 2);
            var0 -= var4 * 1000;
            if (var0 == 0) {
                return var1.toString();
            } else {
                var1.append('.');
                FormatUtils.appendPaddedInteger(var1, var0, 3);
                return var1.toString();
            }
        }
    }


    public static String getMillisAsString(long milliseconds, String pattern) {
        if (pattern.isEmpty()) {
            pattern = "dd-MM-yy";
        }
        return android.text.format.DateFormat.format(pattern, new Date(milliseconds)).toString();

        //DateTimeFormatter dtfOut = DateTimeFormat.forPattern(pattern);
       // DateTime dateTime = new DateTime(milliseconds);
       // return dtfOut.print(dateTime);
    }

    public static void printDate(long ms1, long ms2) {
        String pattern = "dd-MM-yyyy, HH:mm:ss";
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern(pattern);
        DateTime dateTime1 = new DateTime(ms1);
        DateTime dateTime2 = new DateTime(ms2);
        AppLog.e(String.format(dtfOut.print(dateTime1) + " to " + dtfOut.print(dateTime2)));
    }


    public static void getCurrentLocalTimeInfo() {
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("dd-MM");

        long unixEpochMillis = DateTime.now().getMillis(); //1557850805144
        String tzOffset = MyDateTimeUtils.getCurrentTimeZoneOffset(unixEpochMillis); //-03:00
        DateTimeZone dateTimeZone = DateTimeZone.forID(tzOffset); //-03:00
        DateTime dateTime = new DateTime(unixEpochMillis).withZone(dateTimeZone); //2019-05-14T13:20:05.144-03:00
        String dayMonth = dtfOut.print(dateTime);     //14-05
        //     String dayOfMonth = Static.getDigitWithFrontZero(dateTime.getDayOfMonth()); //
        AppLog.e("dateTime" + dateTime + " --- " + dayMonth + " ,offset: "+ tzOffset);
    }

    // Return the starting millisec for any day
    public static long getStartOfTheDay(DateTime datetime) {
        return datetime.withMillisOfDay(0).getMillis();
    }

    // Return the starting millisec for any day
    public static long getStartOfTheDay(long millisec) {
        return new DateTime(millisec).withMillisOfDay(0).getMillis();
    }

    // Return the ending millisec for any day
    public static long getEndoftheDay(DateTime datetime) {
        return datetime.withTime(23, 59, 59, 999).getMillis();
    }

    // Return the ending millisec for any day
    public static long getEndoftheDay(long millisec) {
        return new DateTime(millisec).withTime(23, 59, 59, 999).getMillis();
    }
}
