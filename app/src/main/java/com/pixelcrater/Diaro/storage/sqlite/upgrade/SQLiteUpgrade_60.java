package com.pixelcrater.Diaro.storage.sqlite.upgrade;

import android.provider.BaseColumns;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.storage.sqlite.helpers.MySQLiteWrapper;
import com.pixelcrater.Diaro.utils.AppLog;

public class SQLiteUpgrade_60 {
    private MySQLiteWrapper mySQLiteWrapper;

    public SQLiteUpgrade_60() {
        AppLog.d("");
        mySQLiteWrapper = MyApp.getInstance().storageMgr.getSQLiteAdapter().mySQLiteWrapper;

        // Add location_uid, tag_count, photo_count columns
        // Cannot recreate table in order to copy locations
        // from diaro_entries to diaro_locations table
        updateEntriesTable();
        createLocationsTable();

        AppLog.d("");
    }

    private void updateEntriesTable() {
        if (!mySQLiteWrapper.existsColumnInTable("diaro_entries", "location_uid")) {
            mySQLiteWrapper.execSQL("ALTER TABLE diaro_entries ADD COLUMN " +
                    "location_uid TEXT DEFAULT '' NOT NULL");
        }
        if (!mySQLiteWrapper.existsColumnInTable("diaro_entries", "tag_count")) {
            mySQLiteWrapper.execSQL("ALTER TABLE diaro_entries ADD COLUMN " +
                    "tag_count INTEGER DEFAULT 0 NOT NULL");
        }
        if (!mySQLiteWrapper.existsColumnInTable("diaro_entries", "photo_count")) {
            mySQLiteWrapper.execSQL("ALTER TABLE diaro_entries ADD COLUMN " +
                    "photo_count INTEGER DEFAULT 0 NOT NULL");
        }
    }

    private void createLocationsTable() {
        mySQLiteWrapper.execSQL("CREATE TABLE IF NOT EXISTS diaro_locations_temp (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "uid TEXT DEFAULT '' NOT NULL," +
                "title TEXT DEFAULT '' NOT NULL," +
                "address TEXT DEFAULT '' NOT NULL," +
                "lat TEXT DEFAULT '' NOT NULL," +
                "long TEXT DEFAULT '' NOT NULL," +
                "zoom INTEGER DEFAULT 0 NOT NULL)");

        mySQLiteWrapper.execSQL("ALTER TABLE diaro_locations_temp RENAME TO diaro_locations");
    }
}
