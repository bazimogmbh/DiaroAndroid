package com.pixelcrater.Diaro.storage.sqlite.upgrade;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.storage.sqlite.helpers.MySQLiteWrapper;
import com.pixelcrater.Diaro.utils.AppLog;

public class SQLiteUpgrade_189 {
    private MySQLiteWrapper mySQLiteWrapper;

    public SQLiteUpgrade_189() {
        mySQLiteWrapper = MyApp.getInstance().storageMgr.getSQLiteAdapter().mySQLiteWrapper;

        // Add synced column to all tables
        addSyncedColumnToTable("diaro_entries");
        addSyncedColumnToTable("diaro_attachments");
        addSyncedColumnToTable("diaro_folders");
        addSyncedColumnToTable("diaro_tags");
        addSyncedColumnToTable("diaro_locations");

        if (!mySQLiteWrapper.existsColumnInTable("diaro_entries", "tz_offset")) {
            AppLog.d("tz_offset column not found in diaro_entries table");
            mySQLiteWrapper.execSQL("ALTER TABLE diaro_entries ADD COLUMN " +
                    "tz_offset TEXT DEFAULT '+00:00' NOT NULL");
        }
        if (!mySQLiteWrapper.existsColumnInTable("diaro_locations", "lng")) {
            AppLog.d("lng column not found in diaro_locations table");
            mySQLiteWrapper.execSQL("ALTER TABLE diaro_locations ADD COLUMN " +
                    "lng TEXT DEFAULT '' NOT NULL");
        }
        if (!mySQLiteWrapper.existsColumnInTable("diaro_attachments", "file_sync_id")) {
            AppLog.d("file_sync_id column not found in diaro_attachments table");
            mySQLiteWrapper.execSQL("ALTER TABLE diaro_attachments ADD COLUMN " +
                    "file_sync_id TEXT DEFAULT '' NOT NULL");
        }

    }

    private void addSyncedColumnToTable(String fullTableName) {
        if (!mySQLiteWrapper.existsColumnInTable(fullTableName, "synced")) {
            mySQLiteWrapper.execSQL("ALTER TABLE " + fullTableName + " ADD COLUMN " +  "synced INTEGER DEFAULT 0 NOT NULL");

        }
    }
}
