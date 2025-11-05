package com.pixelcrater.Diaro.storage.sqlite.upgrade;

import android.provider.BaseColumns;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.storage.sqlite.helpers.MySQLiteWrapper;
import com.pixelcrater.Diaro.utils.AppLog;

public class SQLiteUpgrade_76 {
    private MySQLiteWrapper mySQLiteWrapper;

    public SQLiteUpgrade_76() {
        AppLog.d("");
        mySQLiteWrapper = MyApp.getInstance().storageMgr.getSQLiteAdapter().mySQLiteWrapper;

        // Recreate all tables to set UNIQUE to uid column
        recreateEntriesTable();
        recreateAttachmentsTable();
        recreateFoldersTable();
        recreateTagsTable();
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
                "title TEXT DEFAULT '' NOT NULL," +
                "text TEXT DEFAULT '' NOT NULL," +
                "folder_uid TEXT DEFAULT '' NOT NULL," +
                "location_uid TEXT DEFAULT '' NOT NULL," +
                "tags TEXT DEFAULT '' NOT NULL," +
                "tag_count INTEGER DEFAULT 0 NOT NULL," +
                "photo_count INTEGER DEFAULT 0 NOT NULL," +
                "primary_photo_uid TEXT DEFAULT '' NOT NULL);");

        mySQLiteWrapper.execSQL("INSERT INTO diaro_entries_temp (" +
                "uid, archived, date, title, text, folder_uid, location_uid, tags, " +
                "primary_photo_uid" +
                ") SELECT " +
                "uid, archived, date, title, text, folder_uid, location_uid, tags, " +
                "primary_photo_uid" +
                " FROM diaro_entries");

        mySQLiteWrapper.execSQL("DROP TABLE diaro_entries");
        mySQLiteWrapper.execSQL("ALTER TABLE diaro_entries_temp RENAME TO diaro_entries");
    }

    private void recreateAttachmentsTable() {
        mySQLiteWrapper.execSQL("CREATE TABLE IF NOT EXISTS diaro_attachments_temp (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "uid TEXT DEFAULT '' NOT NULL UNIQUE," +
                "sync_id TEXT DEFAULT '' NOT NULL," +
                "entry_uid TEXT DEFAULT '' NOT NULL," +
                "type TEXT DEFAULT '' NOT NULL," +
                "filename TEXT DEFAULT '' NOT NULL," +
                "size_bytes LONG DEFAULT 0 NOT NULL," +
                "position INTEGER DEFAULT 0 NOT NULL);");

        mySQLiteWrapper.execSQL("INSERT INTO diaro_attachments_temp (" +
                "uid, entry_uid, type, filename, position" +
                ") SELECT " +
                "uid, entry_uid, type, filename, position" +
                " FROM diaro_attachments");

        mySQLiteWrapper.execSQL("DROP TABLE diaro_attachments");
        mySQLiteWrapper.execSQL("ALTER TABLE diaro_attachments_temp RENAME TO diaro_attachments");
    }

    private void recreateFoldersTable() {
        mySQLiteWrapper.execSQL("CREATE TABLE IF NOT EXISTS diaro_folders_temp (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "uid TEXT DEFAULT '' NOT NULL UNIQUE," +
                "sync_id TEXT DEFAULT '' NOT NULL," +
                "title TEXT DEFAULT '' NOT NULL," +
                "color TEXT DEFAULT '' NOT NULL," +
                "pattern TEXT DEFAULT '' NOT NULL);");

        mySQLiteWrapper.execSQL("INSERT INTO diaro_folders_temp (" +
                "uid, title, color, pattern" +
                ") SELECT " +
                "uid, title, color, pattern" +
                " FROM diaro_folders");

        mySQLiteWrapper.execSQL("DROP TABLE diaro_folders");
        mySQLiteWrapper.execSQL("ALTER TABLE diaro_folders_temp RENAME TO diaro_folders");
    }

    private void recreateTagsTable() {
        mySQLiteWrapper.execSQL("CREATE TABLE IF NOT EXISTS diaro_tags_temp (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "uid TEXT DEFAULT '' NOT NULL UNIQUE," +
                "sync_id TEXT DEFAULT '' NOT NULL," +
                "title TEXT DEFAULT '' NOT NULL);");

        mySQLiteWrapper.execSQL("INSERT INTO diaro_tags_temp (" +
                "uid, title" +
                ") SELECT " +
                "uid, title" +
                " FROM diaro_tags");

        mySQLiteWrapper.execSQL("DROP TABLE diaro_tags");
        mySQLiteWrapper.execSQL("ALTER TABLE diaro_tags_temp RENAME TO diaro_tags");
    }

    private void recreateLocationsTable() {
        mySQLiteWrapper.execSQL("CREATE TABLE IF NOT EXISTS diaro_locations_temp (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "uid TEXT DEFAULT '' NOT NULL UNIQUE," +
                "sync_id TEXT DEFAULT '' NOT NULL," +
                "title TEXT DEFAULT '' NOT NULL," +
                "address TEXT DEFAULT '' NOT NULL," +
                "lat TEXT DEFAULT '' NOT NULL," +
                "long TEXT DEFAULT '' NOT NULL," +
                "zoom INTEGER DEFAULT 0 NOT NULL);");

        mySQLiteWrapper.execSQL("INSERT INTO diaro_locations_temp (" +
                "uid, title, address, lat, long, zoom" +
                ") SELECT " +
                "uid, title, address, lat, long, zoom" +
                " FROM diaro_locations");

        mySQLiteWrapper.execSQL("DROP TABLE diaro_locations");
        mySQLiteWrapper.execSQL("ALTER TABLE diaro_locations_temp RENAME TO diaro_locations");
    }
}
