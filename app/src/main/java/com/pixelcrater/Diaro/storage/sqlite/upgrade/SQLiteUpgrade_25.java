package com.pixelcrater.Diaro.storage.sqlite.upgrade;

import android.provider.BaseColumns;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.storage.sqlite.helpers.MySQLiteWrapper;
import com.pixelcrater.Diaro.utils.AppLog;

public class SQLiteUpgrade_25 {
    private MySQLiteWrapper mySQLiteWrapper;

    /**
     * Updates database and prepares Diaro for syncing entries in separate files
     */
    public SQLiteUpgrade_25() {
        AppLog.d("");
        mySQLiteWrapper = MyApp.getInstance().storageMgr.getSQLiteAdapter().mySQLiteWrapper;

        // Change entry_name, parent, gps_location, gps_coord columns
        recreateEntriesTable();
        // Change diaro_categories table to diaro_folders
        recreateFoldersTable();
        // Change tag_name column
        recreateTagsTable();
        createAttachmentsTable();

        mySQLiteWrapper.execSQL("DROP TABLE diaro_deleted");
        mySQLiteWrapper.execSQL("DROP TABLE diaro_photos");

        AppLog.d("");
    }

    private void recreateEntriesTable() {
        mySQLiteWrapper.execSQL("CREATE TABLE IF NOT EXISTS diaro_entries_temp (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "uid TEXT DEFAULT '' NOT NULL," +
                "archived INTEGER DEFAULT 0 NOT NULL," +
                "date LONG DEFAULT 0 NOT NULL," +
                "title TEXT DEFAULT '' NOT NULL," +
                "text TEXT DEFAULT '' NOT NULL," +
                "folder_uid TEXT DEFAULT '' NOT NULL," +
                "location TEXT DEFAULT '' NOT NULL," +
                "location_coords TEXT DEFAULT '' NOT NULL," +
                "tags TEXT DEFAULT '' NOT NULL," +
                "primary_photo_uid TEXT DEFAULT '' NOT NULL)");

        mySQLiteWrapper.execSQL("INSERT INTO diaro_entries_temp (" +
                "uid, date, title, text, folder_uid, location, location_coords, tags" +
                ") SELECT " +
                "uid," +
                "IFNULL(date, 0)," +
                "IFNULL(entry_name, '')," +
                "IFNULL(text, '')," +
                "IFNULL(parent, '')," +
                "IFNULL(gps_location, '')," +
                "IFNULL(gps_coord, '')," +
                "IFNULL(tags, '')" +
                " FROM diaro_entries");

        mySQLiteWrapper.execSQL("DROP TABLE diaro_entries");
        mySQLiteWrapper.execSQL("ALTER TABLE diaro_entries_temp RENAME TO diaro_entries");
    }

    private void recreateFoldersTable() {
        mySQLiteWrapper.execSQL("CREATE TABLE IF NOT EXISTS diaro_folders_temp (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "uid TEXT DEFAULT ''," +
                "archived INTEGER DEFAULT 0," +
                "title TEXT DEFAULT ''," +
                "color TEXT DEFAULT ''," +
                "pattern TEXT DEFAULT ''," +
                "font TEXT DEFAULT ''," +
                "count INTEGER DEFAULT 0)");

        mySQLiteWrapper.execSQL("INSERT INTO diaro_folders_temp (" +
                "uid, title, color, pattern" +
                ") SELECT " +
                "uid," +
                "IFNULL(category_name, '')," +
                "IFNULL(category_color, '')," +
                "IFNULL(pattern, '')" +
                " FROM diaro_categories");

        mySQLiteWrapper.execSQL("DROP TABLE diaro_categories");
        mySQLiteWrapper.execSQL("ALTER TABLE diaro_folders_temp RENAME TO diaro_folders");
    }

    private void recreateTagsTable() {
        mySQLiteWrapper.execSQL("CREATE TABLE IF NOT EXISTS diaro_tags_temp (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "uid TEXT DEFAULT ''," +
                "archived INTEGER DEFAULT 0," +
                "title TEXT DEFAULT '')");

        mySQLiteWrapper.execSQL("INSERT INTO diaro_tags_temp (" +
                "uid, title" +
                ") SELECT " +
                "uid," +
                "IFNULL(tag_name, '')" +
                " FROM diaro_tags");

        mySQLiteWrapper.execSQL("DROP TABLE diaro_tags");
        mySQLiteWrapper.execSQL("ALTER TABLE diaro_tags_temp RENAME TO diaro_tags");
    }

    private void createAttachmentsTable() {
        mySQLiteWrapper.execSQL("CREATE TABLE IF NOT EXISTS diaro_attachments_temp (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "uid TEXT DEFAULT ''," +
                "archived INTEGER DEFAULT 0," +
                "entry_uid TEXT DEFAULT ''," +
                "type TEXT DEFAULT ''," +
                "filename TEXT DEFAULT ''," +
                "size_bytes LONG DEFAULT 0," +
                "position INTEGER DEFAULT 0," +
                "reupload INTEGER DEFAULT 0," +
                "cached INTEGER DEFAULT 0)");

        mySQLiteWrapper.execSQL("ALTER TABLE diaro_attachments_temp RENAME TO diaro_attachments");
    }
}
