package com.pixelcrater.Diaro.appupgrades;

import android.content.ContentValues;
import android.database.Cursor;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.utils.AppLog;

import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class AppUpgrade_209 {
    private SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.getDefault());


    public AppUpgrade_209() {
        AppLog.d("");

        fixSyncId("diaro_entries");
        fixSyncId("diaro_folders");
        fixSyncId("diaro_tags");
        fixSyncId("diaro_locations");
        fixSyncId("diaro_attachments");
        fixFileSyncId();

        AppLog.d("");
    }

    private void fixSyncId(String fullTableName) {
        String[] whereArgs = new String[1];
        whereArgs[0] = "";

        Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getRowsColumnsCursor( fullTableName, new String[]{"uid", "sync_id"}, "WHERE sync_id!=?", whereArgs);
        AppLog.d("cursor.getCount(): " + cursor.getCount());

        int uIdColumnIndex = cursor.getColumnIndex("uid");
        int syncIdIndex = cursor.getColumnIndex("sync_id");

        while (cursor.moveToNext()) {
            if (StringUtils.isNotEmpty(cursor.getString(syncIdIndex))) {
                ContentValues cv = new ContentValues();
                try {
                    cv.put("sync_id", formatter.parse(cursor.getString(syncIdIndex)).getTime());
                    MyApp.getInstance().storageMgr.getSQLiteAdapter().updateRowByUid( fullTableName, cursor.getString(uIdColumnIndex), cv);

                } catch (ParseException e) {
//                    e.printStackTrace();
                }
            }
        }
        cursor.close();
    }

    private void fixFileSyncId() {
        String[] whereArgs = new String[1];
        whereArgs[0] = "";

        Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getRowsColumnsCursor(
                "diaro_attachments", new String[]{"uid", "file_sync_id"}, "WHERE file_sync_id!=?",  whereArgs);

        AppLog.d("cursor.getCount(): " + cursor.getCount());

        int uIdColumnIndex = cursor.getColumnIndex("uid");
        int fileSyncIdIndex = cursor.getColumnIndex("file_sync_id");

        while (cursor.moveToNext()) {
            if (StringUtils.isNotEmpty(cursor.getString(fileSyncIdIndex))) {
                ContentValues cv = new ContentValues();
                try {
                    cv.put("file_sync_id", formatter.parse(cursor.getString(fileSyncIdIndex)).getTime());
                    MyApp.getInstance().storageMgr.getSQLiteAdapter().updateRowByUid( "diaro_attachments", cursor.getString(uIdColumnIndex), cv);

                } catch (ParseException e) {
//                    e.printStackTrace();
                }

            }
        }
        cursor.close();
    }

}
