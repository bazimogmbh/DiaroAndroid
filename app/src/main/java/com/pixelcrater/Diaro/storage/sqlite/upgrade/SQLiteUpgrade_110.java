package com.pixelcrater.Diaro.storage.sqlite.upgrade;

import android.provider.BaseColumns;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.storage.sqlite.helpers.MySQLiteWrapper;
import com.pixelcrater.Diaro.utils.AppLog;

public class SQLiteUpgrade_110 {
    private MySQLiteWrapper mySQLiteWrapper;

    public SQLiteUpgrade_110() {
        AppLog.d("");
        mySQLiteWrapper = MyApp.getInstance().storageMgr.getSQLiteAdapter().mySQLiteWrapper;

        // Recreate entries table to drop photo_count and tag_count columns, add tz_offset column
        recreateEntriesTable();
        // Add file_sync_id
        recreateAttachmentsTable();
        // Change column long to lng
        recreateLocationsTable();

        AppLog.d("");
    }

    private void recreateEntriesTable() {
        mySQLiteWrapper.execSQL("CREATE TABLE IF NOT EXISTS diaro_entries_temp (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "uid TEXT DEFAULT '' NOT NULL UNIQUE," +
                "sync_id TEXT DEFAULT '' NOT NULL," +
                "archived INTEGER DEFAULT 0 NOT NULL," +
                "date LONG DEFAULT 0 NOT NULL," +
                "tz_offset TEXT DEFAULT '+00:00' NOT NULL," +
                "title TEXT DEFAULT '' NOT NULL," +
                "text TEXT DEFAULT '' NOT NULL," +
                "folder_uid TEXT DEFAULT '' NOT NULL," +
                "location_uid TEXT DEFAULT '' NOT NULL," +
                "tags TEXT DEFAULT '' NOT NULL," +
                "primary_photo_uid TEXT DEFAULT '' NOT NULL);");

        mySQLiteWrapper.execSQL("INSERT INTO diaro_entries_temp (" +
                "uid, sync_id, archived, date, title, text, folder_uid, " +
                "location_uid, tags, primary_photo_uid" +
                ") SELECT " +
                "uid, sync_id, archived, date, title, text, folder_uid, " +
                "location_uid, tags, primary_photo_uid" +
                " FROM diaro_entries");

        mySQLiteWrapper.execSQL("DROP TABLE diaro_entries");
        mySQLiteWrapper.execSQL("ALTER TABLE diaro_entries_temp RENAME TO diaro_entries");
    }

    private void recreateAttachmentsTable() {
        mySQLiteWrapper.execSQL("CREATE TABLE IF NOT EXISTS diaro_attachments_temp (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "uid TEXT DEFAULT '' NOT NULL UNIQUE," +
                "sync_id TEXT DEFAULT '' NOT NULL," +
                "file_sync_id TEXT DEFAULT '' NOT NULL," +
                "entry_uid TEXT DEFAULT '' NOT NULL," +
                "type TEXT DEFAULT '' NOT NULL," +
                "filename TEXT DEFAULT '' NOT NULL," +
                "position INTEGER DEFAULT 0 NOT NULL);");

        mySQLiteWrapper.execSQL("INSERT INTO diaro_attachments_temp (" +
                "uid, sync_id, entry_uid, type, filename, position" +
                ") SELECT " +
                "uid, sync_id, entry_uid, type, filename, position" +
                " FROM diaro_attachments");

        mySQLiteWrapper.execSQL("DROP TABLE diaro_attachments");
        mySQLiteWrapper.execSQL("ALTER TABLE diaro_attachments_temp RENAME TO diaro_attachments");
    }

    private void recreateLocationsTable() {
        mySQLiteWrapper.execSQL("CREATE TABLE IF NOT EXISTS diaro_locations_temp (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "uid TEXT DEFAULT '' NOT NULL UNIQUE," +
                "sync_id TEXT DEFAULT '' NOT NULL," +
                "title TEXT DEFAULT '' NOT NULL," +
                "address TEXT DEFAULT '' NOT NULL," +
                "lat TEXT DEFAULT '' NOT NULL," +
                "lng TEXT DEFAULT '' NOT NULL," +
                "zoom INTEGER DEFAULT 0 NOT NULL);");

        String longitudeField = "lng";
        // Check if `long` field exists in diaro_locations table
        if (mySQLiteWrapper.existsColumnInTable("diaro_locations", "long")) {
            longitudeField = "long";
        }
        mySQLiteWrapper.execSQL("INSERT INTO diaro_locations_temp (" +
                "uid, sync_id, title, address, lat, lng, zoom" +
                ") SELECT " +
                "uid, sync_id, title, address, lat, " + longitudeField + ", zoom" +
                " FROM diaro_locations");

        mySQLiteWrapper.execSQL("DROP TABLE diaro_locations");
        mySQLiteWrapper.execSQL("ALTER TABLE diaro_locations_temp RENAME TO diaro_locations");
    }
}
