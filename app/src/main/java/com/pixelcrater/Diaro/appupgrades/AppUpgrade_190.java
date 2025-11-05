package com.pixelcrater.Diaro.appupgrades;

import android.content.ContentValues;
import android.database.Cursor;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyDateTimeUtils;

public class AppUpgrade_190 {
    public AppUpgrade_190() {
        fixEntriesTimeZones();
    }

    private void fixEntriesTimeZones() {
        String[] whereArgs = new String[1];
        whereArgs[0] = "+00:00";

        Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getRowsColumnsCursor("diaro_entries", new String[]{"uid", "date"}, "WHERE tz_offset=?", whereArgs);

        AppLog.d("cursor.getCount(): " + cursor.getCount());

        int entryUidColumnIndex = cursor.getColumnIndex("uid");
        int entryDateIndex = cursor.getColumnIndex("date");

        while (cursor.moveToNext()) {
            long millis = cursor.getLong(entryDateIndex);

            if (millis > 0) {
                ContentValues cv = new ContentValues();
                // Get timezone offset for current timezone and for this entry time
                cv.put("tz_offset", MyDateTimeUtils.getCurrentTimeZoneOffset(millis));

                MyApp.getInstance().storageMgr.getSQLiteAdapter().updateRowByUid( "diaro_entries", cursor.getString(entryUidColumnIndex), cv);

            }
        }
        cursor.close();
    }
}
