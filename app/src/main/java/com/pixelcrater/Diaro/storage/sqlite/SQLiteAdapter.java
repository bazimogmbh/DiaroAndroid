package com.pixelcrater.Diaro.storage.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.text.TextUtils;

import androidx.core.util.Pair;


import com.pixelcrater.Diaro.BuildConfig;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.entries.EntriesStatic;
import com.pixelcrater.Diaro.locations.LocationsCursorAdapter;
import com.pixelcrater.Diaro.stats.StatsSqlHelper;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.storage.sqlite.helpers.MySQLiteWrapper;
import com.pixelcrater.Diaro.storage.sqlite.helpers.SQLiteMgr;
import com.pixelcrater.Diaro.tags.TagsCursorAdapter;
import com.pixelcrater.Diaro.utils.AppLog;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.pixelcrater.Diaro.config.AppConfig.USE_PLAIN_SQLITE;
import static com.pixelcrater.Diaro.storage.Tables.KEY_ATTACHMENT_ENTRY_UID;
import static com.pixelcrater.Diaro.storage.Tables.KEY_ENTRY_TAGS;
import static com.pixelcrater.Diaro.storage.Tables.TABLE_ENTRIES;
import static com.pixelcrater.Diaro.storage.Tables.VALUE_ATTACHMENT_NOT_FOUND;

public class SQLiteAdapter {

    public MySQLiteWrapper mySQLiteWrapper = null;

    /**
     * DatabaseHelpers creates database tables only, upgrade is performed in AppUpgradeMgr
     */
    public SQLiteAdapter() {
        SQLiteMgr sqliteMgr = new SQLiteMgr();
        // SQLite Cipher
        try {
            // First init the db libraries with the context
            System.loadLibrary("sqlcipher");

            // Use encrypted database
            mySQLiteWrapper = new MySQLiteWrapper(sqliteMgr.getCipherEncryptedDb());

            if (mySQLiteWrapper.isOpen()) {
                AppLog.d("DATABASE OPENED");
            } else {
                AppLog.e("Database failed to open - database is closed");
                throw new RuntimeException("Failed to open SQLCipher database - database is not open");
            }
        } catch (Error | Exception e) {
            AppLog.e("Error initializing database: " + e.getClass().getName() + " - " + e.getMessage());
           // e.printStackTrace();
            throw new RuntimeException("Failed to initialize SQLCipher database", e);
        }


    }

    public void closeDatabase() {
        try {
            if (mySQLiteWrapper.isOpen()) {
                mySQLiteWrapper.close();
            }
            AppLog.d("DATABASE CLOSED");
        } catch (Exception ignored) {
        }
    }

    public String insertRow(String fullTableName, ContentValues cv) {
//        AppLog.e("fullTableName: " + fullTableName + ", cv: " + cv);
        // If row with the same uid does not exist in this database table
        if (!rowExists(fullTableName, cv.getAsString(Tables.KEY_UID))) {
            try {
                long rowId = mySQLiteWrapper.insertOrThrow(fullTableName, null, cv);
                if (rowId != -1) {
                    return cv.getAsString(Tables.KEY_UID);
                }
            } catch (Exception e) {
                AppLog.e("insertRow Exception: " + e);
            }
        }

        return null;
    }

    public String updateRowByUid(String fullTableName, String rowUid, ContentValues cv) {
        AppLog.i("fullTableName: " + fullTableName + ", rowUid: " + rowUid + ", cv: " + cv);
        if (rowUid == null) {
            rowUid = "";
        }

        try {
            String[] whereArgs = new String[1];
            whereArgs[0] = rowUid;
            int rowsAffected = mySQLiteWrapper.update(fullTableName, cv, Tables.KEY_UID + "=?", whereArgs);
            //  AppLog.e("rowsAffected->" +  rowsAffected);
            if (rowsAffected > 0) {
                return rowUid;
            }
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }
        return null;
    }

    public void deleteRowByUid(String fullTableName, String rowUid) {
        rowUid = (rowUid == null) ? "" : rowUid;

        String[] whereArgs = new String[1];
        whereArgs[0] = rowUid;
        mySQLiteWrapper.delete(fullTableName, Tables.KEY_UID + "=?", whereArgs);
    }

    public void resetAllTablesSyncedField() {
        // Reset all tables rows synced (file_synced) field to 0
        // (if not reset, rows will be deleted from sqlite because json files may  not exist in Dropbox)
        resetSyncedField(TABLE_ENTRIES);
        resetSyncedField(Tables.TABLE_ATTACHMENTS);
        resetSyncedField(Tables.TABLE_FOLDERS);
        resetSyncedField(Tables.TABLE_TAGS);
        resetSyncedField(Tables.TABLE_LOCATIONS);
        resetSyncedField(Tables.TABLE_MOODS);
        resetSyncedField(Tables.TABLE_TEMPLATES);
        resetFileSyncedField();
    }

    public void resetSyncedField(String fullTableName) {
        ContentValues cv = new ContentValues();
        cv.put(Tables.KEY_SYNCED, 0);
        mySQLiteWrapper.update(fullTableName, cv, "", null);
    }

    public void markAttachmentsAsSynced() {
        ContentValues cv = new ContentValues();
        cv.put(Tables.KEY_SYNCED, 1);
        cv.put(Tables.KEY_ATTACHMENT_FILE_SYNCED, 1);
        cv.put(Tables.KEY_ATTACHMENT_FILE_SYNC_ID, VALUE_ATTACHMENT_NOT_FOUND);
        int count = mySQLiteWrapper.update(Tables.TABLE_ATTACHMENTS, cv, "file_sync_id = 'file_not_downloaded'", null);

    }

    public void resetFileSyncedField() {
        ContentValues cv = new ContentValues();
        cv.put(Tables.KEY_ATTACHMENT_FILE_SYNCED, 0);
        mySQLiteWrapper.update(Tables.TABLE_ATTACHMENTS, cv, "", null);
    }

    public void resetAttachmentsFileAsNotDownloaded(String filesList) {

        // filename NOT IN ('photo_20150726_794200.jpg', 'photo_20150726_794200.jpg');
        ContentValues cv = new ContentValues();
        //  cv.put(Tables.KEY_ATTACHMENT_FILE_SYNC_ID, Tables.VALUE_FILE_NOT_DOWNLOADED);
        int count = mySQLiteWrapper.update(Tables.TABLE_ATTACHMENTS, cv, "filename NOT IN (" + filesList + ")", null);
    }

    /**
     * Updates file_sync_id and file_synced fields
     *
     * @param fullTableName
     * @param rowUid
     * @param syncId        - pass null if do not want to update
     * @param synced        - pass -1 if do not want to update
     */
    public void updateRowSyncFields(String fullTableName, String rowUid, String syncId, int synced) {
        ContentValues cv = new ContentValues();
        if (syncId != null) {
            cv.put(Tables.KEY_SYNC_ID, syncId);
        }
        if (synced != -1) {
            cv.put(Tables.KEY_SYNCED, synced);
        }
        updateRowByUid(fullTableName, rowUid, cv);
    }

    /**
     * Updates file_sync_id and file_synced fields
     *
     * @param rowUid
     * @param fileSyncId - pass null if do not want to update
     * @param fileSynced - pass -1 if do not want to update
     */
    public void updateAttachmentRowFileSyncFields(String rowUid, String fileSyncId, int fileSynced) {
        ContentValues cv = new ContentValues();
        if (fileSyncId != null) {
            cv.put(Tables.KEY_ATTACHMENT_FILE_SYNC_ID, fileSyncId);
        }
        if (fileSynced != -1) {
            cv.put(Tables.KEY_ATTACHMENT_FILE_SYNCED, fileSynced);
        }
        updateRowByUid(Tables.TABLE_ATTACHMENTS, rowUid, cv);
    }

    public void truncateTable(String fullTableName) {
        try {
            // There is a bug in sqlcipher 4.3.0
            mySQLiteWrapper.delete(fullTableName, null, null);
        } catch (Exception e) {
            mySQLiteWrapper.execSQL("delete from " + fullTableName);
        }
        mySQLiteWrapper.execSQL("DELETE FROM sqlite_sequence WHERE name='" + fullTableName + "'");
    }

    public void truncateAllTables() {
        truncateTable(TABLE_ENTRIES);
        truncateTable(Tables.TABLE_ATTACHMENTS);
        truncateTable(Tables.TABLE_FOLDERS);
        truncateTable(Tables.TABLE_TAGS);
        truncateTable(Tables.TABLE_LOCATIONS);
        //truncateTable(Tables.TABLE_MOODS);
        truncateTable(Tables.TABLE_TEMPLATES);
    }

    public Cursor getRowsCursor(String fullTableName, String fullWhere, String[] whereArgs) {
        return mySQLiteWrapper.rawQuery("SELECT * FROM " + fullTableName + " " + fullWhere, whereArgs);
    }

    public Cursor getRowsUidsCursor(String fullTableName, String fullWhere, String[] whereArgs) {
        return mySQLiteWrapper.rawQuery("SELECT " + Tables.KEY_UID + " FROM " + fullTableName + " " + fullWhere, whereArgs);
    }

    // new String[]{Tables.KEY_UID}
    public Cursor getRowsColumnsCursor(String fullTableName, String[] columns, String fullWhere, String[] whereArgs) {
        return mySQLiteWrapper.rawQuery("SELECT " + StringUtils.join(columns, ",") + " FROM " + fullTableName + " " + fullWhere, whereArgs);
    }

    public int getRowsCount(String fullTableName, String fullWhere, String[] whereArgs) {
        Cursor cursor = null;
        int count = 0;

        try {
            cursor = mySQLiteWrapper.rawQuery("SELECT COUNT(" + Tables.KEY_UID + ")" + " FROM " + fullTableName + " " + fullWhere, whereArgs);
            cursor.moveToFirst();
            count = cursor.getInt(0);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }

    public Cursor getSingleRowCursor(String fullTableName, String fullWhere, String[] whereArgs) {
        Cursor cursor = null;
        try {
            cursor = getRowsCursor(fullTableName, fullWhere + " LIMIT 1", whereArgs);
            cursor.moveToFirst();
            return cursor;
        } catch (Exception e) {
            AppLog.e(String.format("Error getting single row cursor: %s", e.getMessage()));
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }
    }

    public Cursor getSyncIdsForTable(String table, String uidList) {
        return mySQLiteWrapper.rawQuery("select uid, sync_id, synced from " + table + " WHERE uid IN (" + uidList + ")", null);
    }

    public Cursor getSyncIDByUid(String fullTableName, String uid) {
        String q = "Select uid, sync_id from " + fullTableName + " WHERE uid = ?";
        return mySQLiteWrapper.rawQuery(q, new String[]{uid});
    }

    public Cursor getSingleRowCursorByUid(String fullTableName, String rowUid) {
        rowUid = (rowUid == null) ? "" : rowUid;

        String[] whereArgs = new String[1];
        whereArgs[0] = rowUid;
        return getSingleRowCursor(fullTableName, "WHERE " + Tables.KEY_UID + "=?", whereArgs);
    }

    public String getSingleRowColumnValue(String fullTableName, String column, String fullWhere, String[] whereArgs) {
        String value = "";

        try {
            Cursor rowCursor = mySQLiteWrapper.rawQuery("SELECT " + column + " FROM " + fullTableName + " " + fullWhere + " LIMIT 1", whereArgs);
            rowCursor.moveToFirst();
            if (rowCursor.getCount() == 1 && rowCursor.getColumnCount() == 1) {
                value = rowCursor.getString(rowCursor.getColumnIndex(column));
            }
            rowCursor.close();
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }
        return value;
    }

    public String getSingleRowColumnValueByUid(String fullTableName, String column, String rowUid) {
        if (rowUid == null) {
            rowUid = "";
        }
        String[] whereArgs = new String[1];
        whereArgs[0] = rowUid;
        return getSingleRowColumnValue(fullTableName, column, "WHERE " + Tables.KEY_UID + "=?", whereArgs);
    }

    public boolean rowExists(String fullTableName, String rowUid) {
        rowUid = (rowUid == null) ? "" : rowUid;
        String[] whereArgs = new String[1];
        whereArgs[0] = rowUid;
        return getRowsCount(fullTableName, "WHERE " + Tables.KEY_UID + "=?", whereArgs) > 0;
    }

    ///////////////////////////////////////
    public Cursor getEntriesCursorForCalendar(long fromMillis, long toMillis) {
        return mySQLiteWrapper.rawQuery("SELECT e." + Tables.KEY_ENTRY_DATE +
                        ", " + Tables.getLocalTime("e." + Tables.KEY_ENTRY_DATE) + " AS localtime" +
                        ", " + Tables.getOnlyMs("e." + Tables.KEY_ENTRY_DATE) + " AS only_ms" +
                        ", COUNT(a." + Tables.KEY_ATTACHMENT_FILENAME + ") AS photo_count" +
                        ", f." + Tables.KEY_FOLDER_COLOR + " AS folder_color" +
                        " FROM " + TABLE_ENTRIES + " e" +
                        " LEFT JOIN " + Tables.TABLE_FOLDERS + " f ON" +
                        " e." + Tables.KEY_ENTRY_FOLDER_UID + "=f." + Tables.KEY_UID +
                        " LEFT JOIN " + Tables.TABLE_ATTACHMENTS + " a ON" +
                        " e." + Tables.KEY_UID + "=a." + KEY_ATTACHMENT_ENTRY_UID +
                        " WHERE e." + Tables.KEY_ENTRY_ARCHIVED + "=0" +
                        " AND " + Tables.getLocalTime("e." + Tables.KEY_ENTRY_DATE) +
                        " BETWEEN " + Tables.getLocalTime(fromMillis) + " AND " + Tables.getLocalTime(toMillis) +
                        " GROUP BY e." + Tables.KEY_UID +
                        " ORDER BY localtime " + getOrderBy() +
                        ", only_ms " + getOrderBy() + ", e." + Tables.KEY_UID
                , null);
    }

    /**
     * Called when loading or searching entries
     */
    public Cursor getEntriesCursorIdsAndUidsOnly(String andSql, String[] whereArgs) {
        AppLog.i("getEntriesCursorIdsAndUidsOnly-> " + andSql + " ---- " + Arrays.toString(whereArgs));
        return mySQLiteWrapper.rawQuery("SELECT " +
                "e._id, e.uid , " +
                Tables.getLocalTime("e.date") + " AS localtime ," +
                Tables.getOnlyMs("e.date") + " AS only_ms" +
                " FROM diaro_entries e" +
                " WHERE e.archived = 0 " +
                andSql +
                " ORDER BY localtime " + getOrderBy() +
                ", only_ms " + getOrderBy() +
                ", e.uid", whereArgs);
    }

    public Cursor getEntriesCursorUidsOnly(String andSql, String[] whereArgs) {
        AppLog.d("getEntriesCursorUidsOnly-> " + andSql + " ---- " + Arrays.toString(whereArgs));
        return mySQLiteWrapper.rawQuery("SELECT e.uid," +
                Tables.getLocalTime("e." + Tables.KEY_ENTRY_DATE) + " AS localtime," +
                Tables.getOnlyMs("e." + Tables.KEY_ENTRY_DATE) + " AS only_ms" +
                " FROM diaro_entries e " +
                " WHERE e.archived = 0 " +
                andSql +
                " ORDER BY localtime " + getOrderBy() +
                ", only_ms " + getOrderBy() +
                ", e.uid", whereArgs);
    }

    public Cursor getAllEntriesCursorUidsOnly() {
        return mySQLiteWrapper.rawQuery("SELECT e.uid," +
                Tables.getLocalTime("e." + Tables.KEY_ENTRY_DATE) + " AS localtime," +
                Tables.getOnlyMs("e." + Tables.KEY_ENTRY_DATE) + " AS only_ms" +
                " FROM diaro_entries e " +
                " WHERE e.archived = 0 " +
                " ORDER BY localtime " + getOrderBy() +
                ", only_ms " + getOrderBy(), null);

    }

    public String getOrderBy() {
        int sort = MyApp.getInstance().prefs.getInt(Prefs.PREF_ENTRIES_SORT, Prefs.SORT_NEWEST_FIRST);
        return (sort == Prefs.SORT_OLDEST_FIRST) ? "ASC" : "DESC";
    }

    public Cursor getEntriesSectionsCursor(String andSql, String[] whereArgs) {
        return mySQLiteWrapper.rawQuery("SELECT " +
                "strftime('%Y', datetime(e.date/1000, 'unixepoch', e.tz_offset)) as year, " +
                "strftime('%m', datetime(e.date/1000, 'unixepoch', e.tz_offset)) as month, " +
                //   "strftime('%d', datetime(e.date/1000, 'unixepoch', e.tz_offset)) as day, " +
                "COUNT(e.uid) as section_entries_count " +
                "FROM diaro_entries e " +
                "WHERE e.archived = 0 " +
                andSql +
                " GROUP by year||month" +
                " ORDER by year||month " + getOrderBy(), whereArgs);
    }

    private final int titleCropLength = 150;
    private final int textCropLength = 600;

    public Cursor getSingleEntryCursorByUid(String rowUid, boolean cropText) {
        String[] whereArgs = new String[1];
        whereArgs[0] = rowUid;

        //  AppLog.e("test" + "getSingleEntryCursorByUid");

        String sqlStatement = "SELECT " +
                "e." + BaseColumns._ID +
                ", e." + Tables.KEY_UID +

                ", e." + Tables.KEY_ENTRY_WEATHER_TEMPERATURE +
                ", e." + Tables.KEY_ENTRY_WEATHER_ICON +
                ", e." + Tables.KEY_ENTRY_WEATHER_DESCRIPTION +
                ", e." + Tables.KEY_ENTRY_MOOD_UID +

                ", e." + Tables.KEY_SYNCED +
                ", e." + Tables.KEY_ENTRY_ARCHIVED +
                ", e." + Tables.KEY_ENTRY_DATE +
                ", e." + Tables.KEY_ENTRY_TZ_OFFSET +
                (cropText ?
                        ", substr(e." + Tables.KEY_ENTRY_TITLE + ",1," + titleCropLength + ") " :
                        ", e." + Tables.KEY_ENTRY_TITLE) + " AS " + Tables.KEY_ENTRY_TITLE +
                (cropText ?
                        ", substr(e." + Tables.KEY_ENTRY_TEXT + ",1," + textCropLength + ") " :
                        ", e." + Tables.KEY_ENTRY_TEXT) + " AS " + Tables.KEY_ENTRY_TEXT +
                ", e." + Tables.KEY_ENTRY_FOLDER_UID +
                ", e." + Tables.KEY_ENTRY_LOCATION_UID +
                ", e." + KEY_ENTRY_TAGS +
                ", e." + Tables.KEY_ENTRY_PRIMARY_PHOTO_UID +
                ", " + Tables.getLocalTime("e." + Tables.KEY_ENTRY_DATE) + " AS localtime" +
                ", " + Tables.getOnlyMs("e." + Tables.KEY_ENTRY_DATE) + " AS only_ms" +

                ", COUNT(DISTINCT(a1." + Tables.KEY_ATTACHMENT_FILENAME + " )) AS photo_count" +
                ", COUNT(DISTINCT(a4." + Tables.KEY_ATTACHMENT_FILENAME + " )) AS audio_count" +
                ", COUNT(DISTINCT(a5." + Tables.KEY_ATTACHMENT_FILENAME + " )) AS docs_count" +

                ", a2." + Tables.KEY_ATTACHMENT_FILENAME + " AS primary_photo_filename" +
                ", a2." + Tables.KEY_ATTACHMENT_FILE_SYNC_ID + " AS primary_photo_file_sync_id" +
                ", a3." + Tables.KEY_ATTACHMENT_FILENAME + " AS first_photo_filename" +
                ", a3." + Tables.KEY_ATTACHMENT_FILE_SYNC_ID + " AS first_photo_file_sync_id" +
                ", f." + Tables.KEY_FOLDER_COLOR + " AS folder_color" +
                ", f." + Tables.KEY_FOLDER_TITLE + " AS folder_title" +
                ", f." + Tables.KEY_FOLDER_PATTERN + " AS folder_pattern" +
                ", l." + Tables.KEY_LOCATION_TITLE + " AS location_title" +
                ", l." + Tables.KEY_LOCATION_ADDRESS + " AS location_address" +
                ", l." + Tables.KEY_LOCATION_LATITUDE + " AS location_latitude" +
                ", l." + Tables.KEY_LOCATION_LONGITUDE + " AS location_longitude" +
                ", l." + Tables.KEY_LOCATION_ZOOM + " AS location_zoom" +
                ", m." + Tables.KEY_MOOD_TITLE+ " AS mood_title" +
                ", m." + Tables.KEY_MOOD_ICON + " AS mood_icon" +
                ", m." + Tables.KEY_MOOD_COLOR+ " AS mood_color" +
                " FROM " + TABLE_ENTRIES + " e" +
                " LEFT JOIN " + Tables.TABLE_FOLDERS + " f" +
                " ON e." + Tables.KEY_ENTRY_FOLDER_UID + "=f." + Tables.KEY_UID +
                " LEFT JOIN " + Tables.TABLE_LOCATIONS + " l" +
                " ON e." + Tables.KEY_ENTRY_LOCATION_UID + "=l." + Tables.KEY_UID +
                " LEFT JOIN " + Tables.TABLE_MOODS + " m" +
                " ON e." + Tables.KEY_ENTRY_MOOD_UID + "=m." + Tables.KEY_UID +

                " LEFT JOIN " + Tables.TABLE_ATTACHMENTS + " a1" +
                " ON (e." + Tables.KEY_UID + "=a1." + KEY_ATTACHMENT_ENTRY_UID + " AND a1." + Tables.KEY_ATTACHMENT_TYPE + "='photo')" +
                " LEFT JOIN " + Tables.TABLE_ATTACHMENTS + " a4" +
                " ON (e." + Tables.KEY_UID + "=a4." + KEY_ATTACHMENT_ENTRY_UID + " AND a4." + Tables.KEY_ATTACHMENT_TYPE + "='audio')" +
                " LEFT JOIN " + Tables.TABLE_ATTACHMENTS + " a5" +
                " ON (e." + Tables.KEY_UID + "=a5." + KEY_ATTACHMENT_ENTRY_UID + " AND a5." + Tables.KEY_ATTACHMENT_TYPE + "='docs')" +

                " LEFT JOIN " + Tables.TABLE_ATTACHMENTS + " a2" +
                " ON e." + Tables.KEY_ENTRY_PRIMARY_PHOTO_UID + "=a2." + Tables.KEY_UID +
                " LEFT JOIN " + Tables.TABLE_ATTACHMENTS + " a3" +
                " ON e." + Tables.KEY_UID + "=a3." + KEY_ATTACHMENT_ENTRY_UID +
                " AND a3." + Tables.KEY_ATTACHMENT_POSITION +
                "=(SELECT MIN(" + Tables.KEY_ATTACHMENT_POSITION + ")" +
                " FROM " + Tables.TABLE_ATTACHMENTS +
                " WHERE " + KEY_ATTACHMENT_ENTRY_UID + "=e.uid)" +
                " WHERE e." + Tables.KEY_ENTRY_ARCHIVED + "=0" +
                " AND e." + Tables.KEY_UID + "=?" +
                " GROUP BY e." + Tables.KEY_UID +
                " ORDER BY localtime " + getOrderBy() +
                ", only_ms " + getOrderBy() +
                ", e." + Tables.KEY_UID ;

      //  AppLog.e(sqlStatement);

        Cursor cursor = mySQLiteWrapper.rawQuery(sqlStatement, whereArgs);
        cursor.moveToFirst();
        return cursor;
    }

    //** get all the entries for a given uid list in a single query
    public Cursor getEntriesCursorByUids(List<String> rowUids, boolean cropText) {
        String whereClause = " IN ( " + SQLiteQueryHelper.getSqlArrayFromList(rowUids) + ") ";
        // AppLog.e("whereClause ->" + whereClause);
        String sqlStatement = "SELECT " +
                "e." + BaseColumns._ID +
                ", e." + Tables.KEY_UID +

                ", e." + Tables.KEY_ENTRY_WEATHER_TEMPERATURE +
                ", e." + Tables.KEY_ENTRY_WEATHER_ICON +
                ", e." + Tables.KEY_ENTRY_WEATHER_DESCRIPTION +
                ", e." + Tables.KEY_ENTRY_MOOD_UID +

                ", e." + Tables.KEY_SYNCED +
                ", e." + Tables.KEY_ENTRY_ARCHIVED +
                ", e." + Tables.KEY_ENTRY_DATE +
                ", e." + Tables.KEY_ENTRY_TZ_OFFSET +
                (cropText ?
                        ", substr(e." + Tables.KEY_ENTRY_TITLE + ",1," + titleCropLength + ") " :
                        ", e." + Tables.KEY_ENTRY_TITLE) + " AS " + Tables.KEY_ENTRY_TITLE +
                (cropText ?
                        ", substr(e." + Tables.KEY_ENTRY_TEXT + ",1," + textCropLength + ") " :
                        ", e." + Tables.KEY_ENTRY_TEXT) + " AS " + Tables.KEY_ENTRY_TEXT +
                ", e." + Tables.KEY_ENTRY_FOLDER_UID +
                ", e." + Tables.KEY_ENTRY_LOCATION_UID +
                ", e." + KEY_ENTRY_TAGS +
                ", e." + Tables.KEY_ENTRY_PRIMARY_PHOTO_UID +
                ", " + Tables.getLocalTime("e." + Tables.KEY_ENTRY_DATE) + " AS localtime" +
                ", " + Tables.getOnlyMs("e." + Tables.KEY_ENTRY_DATE) + " AS only_ms" +

                ", COUNT(DISTINCT(a1." + Tables.KEY_ATTACHMENT_FILENAME + " )) AS photo_count" +
                ", COUNT(DISTINCT(a4." + Tables.KEY_ATTACHMENT_FILENAME + " )) AS audio_count" +
                ", COUNT(DISTINCT(a5." + Tables.KEY_ATTACHMENT_FILENAME + " )) AS docs_count" +

                ", a2." + Tables.KEY_ATTACHMENT_FILENAME + " AS primary_photo_filename" +
                ", a2." + Tables.KEY_ATTACHMENT_FILE_SYNC_ID + " AS primary_photo_file_sync_id" +
                ", a3." + Tables.KEY_ATTACHMENT_FILENAME + " AS first_photo_filename" +
                ", a3." + Tables.KEY_ATTACHMENT_FILE_SYNC_ID + " AS first_photo_file_sync_id" +
                ", f." + Tables.KEY_FOLDER_COLOR + " AS folder_color" +
                ", f." + Tables.KEY_FOLDER_TITLE + " AS folder_title" +
                ", f." + Tables.KEY_FOLDER_PATTERN + " AS folder_pattern" +
                ", l." + Tables.KEY_LOCATION_TITLE + " AS location_title" +
                ", l." + Tables.KEY_LOCATION_ADDRESS + " AS location_address" +
                ", l." + Tables.KEY_LOCATION_LATITUDE + " AS location_latitude" +
                ", l." + Tables.KEY_LOCATION_LONGITUDE + " AS location_longitude" +
                ", l." + Tables.KEY_LOCATION_ZOOM + " AS location_zoom" +
                ", m." + Tables.KEY_MOOD_TITLE+ " AS mood_title" +
                ", m." + Tables.KEY_MOOD_ICON + " AS mood_icon" +
                ", m." + Tables.KEY_MOOD_COLOR+ " AS mood_color" +
                " FROM " + TABLE_ENTRIES + " e" +
                " LEFT JOIN " + Tables.TABLE_FOLDERS + " f" +
                " ON e." + Tables.KEY_ENTRY_FOLDER_UID + "=f." + Tables.KEY_UID +
                " LEFT JOIN " + Tables.TABLE_LOCATIONS + " l" +
                " ON e." + Tables.KEY_ENTRY_LOCATION_UID + "=l." + Tables.KEY_UID +
                " LEFT JOIN " + Tables.TABLE_MOODS + " m" +
                " ON e." + Tables.KEY_ENTRY_MOOD_UID + "=m." + Tables.KEY_UID +

                " LEFT JOIN " + Tables.TABLE_ATTACHMENTS + " a1" +
                " ON (e." + Tables.KEY_UID + "=a1." + KEY_ATTACHMENT_ENTRY_UID + " AND a1." + Tables.KEY_ATTACHMENT_TYPE + "='photo')" +
                " LEFT JOIN " + Tables.TABLE_ATTACHMENTS + " a4" +
                " ON (e." + Tables.KEY_UID + "=a4." + KEY_ATTACHMENT_ENTRY_UID + " AND a4." + Tables.KEY_ATTACHMENT_TYPE + "='audio')" +
                " LEFT JOIN " + Tables.TABLE_ATTACHMENTS + " a5" +
                " ON (e." + Tables.KEY_UID + "=a5." + KEY_ATTACHMENT_ENTRY_UID + " AND a5." + Tables.KEY_ATTACHMENT_TYPE + "='docs')" +

                " LEFT JOIN " + Tables.TABLE_ATTACHMENTS + " a2" +
                " ON e." + Tables.KEY_ENTRY_PRIMARY_PHOTO_UID + "=a2." + Tables.KEY_UID +
                " LEFT JOIN " + Tables.TABLE_ATTACHMENTS + " a3" +
                " ON e." + Tables.KEY_UID + "=a3." + KEY_ATTACHMENT_ENTRY_UID +
                " AND a3." + Tables.KEY_ATTACHMENT_POSITION +
                "=(SELECT MIN(" + Tables.KEY_ATTACHMENT_POSITION + ")" +
                " FROM " + Tables.TABLE_ATTACHMENTS +
                " WHERE " + KEY_ATTACHMENT_ENTRY_UID + "=e.uid)" +
                " WHERE e." + Tables.KEY_ENTRY_ARCHIVED + "=0" +
                " AND e." + Tables.KEY_UID + whereClause +
                " GROUP BY e." + Tables.KEY_UID +
                " ORDER BY localtime " + getOrderBy() +
                ", only_ms " + getOrderBy() +
                ", e." + Tables.KEY_UID;

        //        AppLog.e("cursor count-> " + cursor.getCount());
        AppLog.e("test" + sqlStatement);
        return mySQLiteWrapper.rawQuery(sqlStatement, null);
    }

    public Cursor getUnassignedAttachmentsCursor() {
        return mySQLiteWrapper.rawQuery("SELECT * FROM diaro_attachments WHERE entry_uid NOT IN (SELECT uid FROM diaro_entries)", null);
    }

    public Cursor getAttachmentsCursor(String andSql, String[] whereArgs) {
        return mySQLiteWrapper.rawQuery("SELECT * FROM diaro_attachments WHERE uid !='' " + andSql + " ORDER BY type, entry_uid, position, uid", whereArgs);
    }

    public Cursor getAttachmentUidByFileNameAndType(String filename, String fileType) {
        String q = "SELECT uid FROM diaro_attachments WHERE filename = ? AND type = ? ";
        return mySQLiteWrapper.rawQuery(q, new String[]{filename, fileType});
    }

    public String getEntryIdByPhotoFilename(String photofilename) {
        String retVal = null;
        try (Cursor cursor = mySQLiteWrapper.rawQuery("SELECT " + Tables.KEY_ATTACHMENT_ENTRY_UID + " FROM " + Tables.TABLE_ATTACHMENTS + " WHERE " + Tables.KEY_ATTACHMENT_FILENAME
                + " = '" + photofilename + "'", null)) {
            // select entry_uid from diaro_attachments where filename = 'photo_20170515_664202.jpg';
            if (cursor != null) {
                if (cursor.getCount() != 0) {
                    while (cursor.moveToNext()) {
                        retVal = cursor.getString(0);
                    }
                }
            }
        } catch (Exception e) {
        }
        return retVal;
    }


    public Cursor getFoldersCursor(String andSql, String[] whereArgs, boolean unionNoFolder) {
        int sort = MyApp.getInstance().prefs.getInt(Prefs.PREF_FOLDERS_SORT, Prefs.SORT_ALPHABETICALLY);

        return mySQLiteWrapper.rawQuery("SELECT" +
                        " 0 AS order_key," +
                        " f." + BaseColumns._ID + " AS " + BaseColumns._ID + "," +
                        " f." + Tables.KEY_UID + " AS " + Tables.KEY_UID + "," +
                        " f." + Tables.KEY_FOLDER_TITLE + " AS " + Tables.KEY_FOLDER_TITLE + "," +
                        " f." + Tables.KEY_FOLDER_COLOR + " AS " + Tables.KEY_FOLDER_COLOR + "," +
                        " f." + Tables.KEY_FOLDER_PATTERN + " AS " + Tables.KEY_FOLDER_PATTERN + "," +
                        " COUNT(e." + Tables.KEY_UID + ") AS entries_count" +
                        " FROM " + Tables.TABLE_FOLDERS + " f" +
                        " LEFT JOIN " + TABLE_ENTRIES + " e ON f." + Tables.KEY_UID + "=e." + Tables.KEY_ENTRY_FOLDER_UID +
                        " AND e." + Tables.KEY_ENTRY_ARCHIVED + "=0" +
                        " WHERE f." + Tables.KEY_UID + "!=''" +
                        andSql +
                        " GROUP BY f." + Tables.KEY_UID +
                        // Union 'No folder'
                        (unionNoFolder ? " UNION ALL SELECT" +
                                " 1 AS order_key," +
                                " 0," +
                                " ''," +
                                " '" + MyApp.getInstance().getString(R.string.no_folder) + "'," +
                                " ''," +
                                " ''," +
                                " (SELECT COUNT(" + Tables.KEY_UID + ") FROM " + TABLE_ENTRIES +
                                " WHERE " + Tables.KEY_ENTRY_FOLDER_UID + "=''" +
                                " AND " + Tables.KEY_ENTRY_ARCHIVED + "=0)" : "") +
                        " ORDER BY order_key," +
                        (sort == Prefs.SORT_ALPHABETICALLY ? "" : " entries_count DESC,") +
                        " f." + Tables.KEY_FOLDER_TITLE + " COLLATE NOCASE," +
                        " f." + Tables.KEY_UID
                , whereArgs);
    }

    public Cursor getSingleFolderCursorByUid(String rowUid) {
        String[] whereArgs = new String[1];
        whereArgs[0] = rowUid;

        Cursor cursor = getFoldersCursor(" AND f." + Tables.KEY_UID + "=?", whereArgs, false);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getTagsCursor(String andSql, String[] whereArgs, boolean unionNoTags) {
        int sort = MyApp.getInstance().prefs.getInt(Prefs.PREF_TAGS_SORT, Prefs.SORT_ALPHABETICALLY);

        return mySQLiteWrapper.rawQuery("SELECT" +
                        " 0 AS order_key," +
                        " t." + BaseColumns._ID + " AS " + BaseColumns._ID + "," +
                        " t." + Tables.KEY_UID + " AS " + Tables.KEY_UID + "," +
                        " t." + Tables.KEY_TAG_TITLE + " AS " + Tables.KEY_TAG_TITLE + "," +
                        " COUNT(e." + Tables.KEY_UID + ") AS entries_count" +
                        " FROM " + Tables.TABLE_TAGS + " t" +
                        " LEFT JOIN " + TABLE_ENTRIES + " e ON e." + KEY_ENTRY_TAGS +
                        " LIKE '%,'||t." + Tables.KEY_UID + "||',%'" +
                        " AND e." + Tables.KEY_ENTRY_ARCHIVED + "=0" +
                        " WHERE t." + Tables.KEY_UID + "!=''" +
                        andSql +
                        " GROUP BY t." + Tables.KEY_UID +
                        // Union 'No tags'
                        (unionNoTags ? " UNION ALL SELECT" +
                                " 1 AS order_key," +
                                " 0," +
                                " '" + TagsCursorAdapter.NO_TAGS_UID + "'," +
                                " '" + MyApp.getInstance().getString(R.string.no_tags) + "'," +
                                " (SELECT COUNT(" + Tables.KEY_UID + ") FROM " + TABLE_ENTRIES +
                                " WHERE " + KEY_ENTRY_TAGS + "=''" +
                                " AND " + Tables.KEY_ENTRY_ARCHIVED + "=0)" : "") +
                        " ORDER BY order_key," +
                        (sort == Prefs.SORT_ALPHABETICALLY ? "" : " entries_count DESC,") +
                        " t." + Tables.KEY_TAG_TITLE + " COLLATE NOCASE," +
                        " t." + Tables.KEY_UID
                , whereArgs);
    }

    public Cursor getSingleTagCursorByUid(String rowUid) {
        String[] whereArgs = new String[1];
        whereArgs[0] = rowUid;

        Cursor cursor = getTagsCursor(" AND t." + Tables.KEY_UID + "=?", whereArgs, false);
        cursor.moveToFirst();
        return cursor;
    }

    public Cursor getAllTagsCursor() {
        return mySQLiteWrapper.rawQuery("select * from diaro_tags", null);
    }

    public Cursor getLocationsCursor(String andSql, String[] whereArgs, boolean unionNoLocation) {
        int sort = MyApp.getInstance().prefs.getInt(Prefs.PREF_LOCATIONS_SORT, Prefs.SORT_ALPHABETICALLY);

        return mySQLiteWrapper.rawQuery("SELECT" +
                        " 0 AS order_key," +
                        " l." + Tables.KEY_LOCATION_TITLE + "||l." + Tables.KEY_LOCATION_ADDRESS + " AS order_title," +
                        " l." + BaseColumns._ID + " AS " + BaseColumns._ID + "," +
                        " l." + Tables.KEY_UID + " AS " + Tables.KEY_UID + "," +
                        " l." + Tables.KEY_LOCATION_TITLE + " AS " + Tables.KEY_LOCATION_TITLE + "," +
                        " l." + Tables.KEY_LOCATION_ADDRESS + " AS " + Tables.KEY_LOCATION_ADDRESS + "," +
                        " l." + Tables.KEY_LOCATION_LATITUDE + " AS " + Tables.KEY_LOCATION_LATITUDE + "," +
                        " l." + Tables.KEY_LOCATION_LONGITUDE + " AS " + Tables.KEY_LOCATION_LONGITUDE + "," +
                        " l." + Tables.KEY_LOCATION_ZOOM + " AS " + Tables.KEY_LOCATION_ZOOM + "," +
                        " COUNT(e." + Tables.KEY_UID + ") AS entries_count" +
                        " FROM " + Tables.TABLE_LOCATIONS + " l" +
                        " LEFT JOIN " + TABLE_ENTRIES + " e ON l." + Tables.KEY_UID + "=e." + Tables.KEY_ENTRY_LOCATION_UID +
                        " AND e." + Tables.KEY_ENTRY_ARCHIVED + "=0" +
                        " WHERE l." + Tables.KEY_UID + "!=''" +
                        andSql +
                        " GROUP BY l." + Tables.KEY_UID +
                        // Union 'No location'
                        (unionNoLocation ? " UNION ALL SELECT" +
                                " 1 AS order_key," +
                                " '' AS order_title," +
                                " 0," +
                                " '" + LocationsCursorAdapter.NO_LOCATION_UID + "'," +
                                " '" + MyApp.getInstance().getString(R.string.no_location) + "'," +
                                " ''," +
                                " ''," +
                                " ''," +
                                " 0," +
                                " (SELECT COUNT(" + Tables.KEY_UID + ") FROM " + TABLE_ENTRIES +
                                " WHERE " + Tables.KEY_ENTRY_LOCATION_UID + "=''" +
                                " AND " + Tables.KEY_ENTRY_ARCHIVED + "=0)" : "") +
                        " ORDER BY order_key," +
                        (sort == Prefs.SORT_ALPHABETICALLY ? "" : " entries_count DESC,") +
                        " order_title COLLATE NOCASE," +
                        " l." + Tables.KEY_UID
                , whereArgs);
    }

    public Cursor getSingleLocationCursorByUid(String rowUid) {
        String[] whereArgs = new String[1];
        whereArgs[0] = rowUid;

        Cursor cursor = getLocationsCursor(" AND l." + Tables.KEY_UID + "=?", whereArgs, false);
        cursor.moveToFirst();
        return cursor;
    }

    // Moods
    public Cursor getMoodsCursor() {
        return getMoodsCursor("", null, true);
    }

    public Cursor getMoodsCursor(String andSql, String[] whereArgs, boolean unionNoFolder) {
        int sort = MyApp.getInstance().prefs.getInt(Prefs.PREF_MOODS_SORT, Prefs.SORT_ALPHABETICALLY);

        return mySQLiteWrapper.rawQuery("SELECT" +
                        " 0 AS order_key," +
                        " f." + BaseColumns._ID + " AS " + BaseColumns._ID + "," +
                        " f." + Tables.KEY_UID + " AS " + Tables.KEY_UID + "," +
                        " f." + Tables.KEY_MOOD_TITLE + " AS " + Tables.KEY_MOOD_TITLE + "," +
                        " f." + Tables.KEY_MOOD_COLOR + " AS " + Tables.KEY_MOOD_COLOR + "," +
                        " f." + Tables.KEY_MOOD_ICON + " AS " + Tables.KEY_MOOD_ICON + "," +
                        " f." + Tables.KEY_MOOD_WEIGHT + " AS " + Tables.KEY_MOOD_WEIGHT + "," +
                        " COUNT(e." + Tables.KEY_UID + ") AS entries_count" +
                        " FROM " + Tables.TABLE_MOODS + " f" +
                        " LEFT JOIN " + TABLE_ENTRIES + " e ON f." + Tables.KEY_UID + "=e." + "mood" +
                        " AND e." + Tables.KEY_ENTRY_ARCHIVED + "=0" +
                        " WHERE f." + Tables.KEY_UID + "!=''" +
                        andSql +
                        " GROUP BY f." + Tables.KEY_UID +
                        // Union 'No Mood'
                        (unionNoFolder ? " UNION ALL SELECT" +
                                " 1 AS order_key," +
                                " 0," +
                                " ''," +
                                " '" + MyApp.getInstance().getString(R.string.mood_none).replaceAll("'", "''") + "'," +
                                " ''," +
                                " ''," +
                                " ''," +
                                " (SELECT COUNT(" + Tables.KEY_UID + ") FROM " + TABLE_ENTRIES +
                                " WHERE " + "mood" + "=''" +
                                " AND " + Tables.KEY_ENTRY_ARCHIVED + "=0)" : "") +
                        " ORDER BY order_key," +
                        (sort == Prefs.SORT_ALPHABETICALLY ? "" : " entries_count DESC,") +
                        " f." + Tables.KEY_MOOD_TITLE + " COLLATE NOCASE," +
                        " f." + Tables.KEY_UID
                , whereArgs);
    }

    public Cursor getSingleMoodCursorByUid(String rowUid) {
        String[] whereArgs = new String[1];
        whereArgs[0] = rowUid;
        Cursor cursor = getMoodsCursor(" AND f." + Tables.KEY_UID + "=?", whereArgs, false);
        cursor.moveToFirst();
        return cursor;
    }


    public void updateMoodUserPositionField(String rowUid, int userPosition) {
        ContentValues cv = new ContentValues();
        if (userPosition != -1) {
            cv.put(Tables.KEY_MOOD_WEIGHT, userPosition);
        }

        updateRowByUid(Tables.TABLE_MOODS, rowUid, cv);
    }

    public String findSameFolder(String rowUid, String folderTitle) {
        rowUid = (rowUid == null) ? "" : rowUid;
        folderTitle = (folderTitle == null) ? "" : folderTitle;

        String matchedRowUid = null;

        String[] whereArgs = new String[2];
        whereArgs[0] = folderTitle.toUpperCase(Locale.getDefault());
        whereArgs[1] = rowUid;

        String where = "WHERE UPPER(" + Tables.KEY_FOLDER_TITLE + ")=?" + " AND " + Tables.KEY_UID + "!=?";
        Cursor folderCursor = getSingleRowCursor(Tables.TABLE_FOLDERS, where, whereArgs);
        if (folderCursor.getCount() == 1) {
            matchedRowUid = folderCursor.getString(folderCursor.getColumnIndex(Tables.KEY_UID));
        }
        folderCursor.close();
        return matchedRowUid;
    }

    public String findSameTag(String rowUid, String tagTitle) {
        rowUid = (rowUid == null) ? "" : rowUid;
        tagTitle = (tagTitle == null) ? "" : tagTitle;

        String matchedRowUid = null;

        String[] whereArgs = new String[2];
        whereArgs[0] = tagTitle.toUpperCase(Locale.getDefault());
        whereArgs[1] = rowUid;

        String where = "WHERE UPPER(" + Tables.KEY_TAG_TITLE + ")=?" + " AND " + Tables.KEY_UID + "!=?";
        Cursor tagCursor = getSingleRowCursor(Tables.TABLE_TAGS, where, whereArgs);
        if (tagCursor.getCount() == 1) {
            matchedRowUid = tagCursor.getString(tagCursor.getColumnIndex(Tables.KEY_UID));
        }
        tagCursor.close();
        return matchedRowUid;
    }

    public String findTagByTitle(String tagTitle) {
        tagTitle = (tagTitle == null) ? "" : tagTitle;
        String matchedRowUid = null;

        // AppLog.e("findTagByTitle-> " + "SELECT uid FROM diaro_tags WHERE UPPER(title)  = '" + tagTitle.toUpperCase() + "'");
        Cursor cursor = null;
        try {
            String q = "SELECT uid FROM diaro_tags WHERE UPPER(title) = ? OR title = ? ";
            cursor = mySQLiteWrapper.rawQuery(q, new String[]{tagTitle.toUpperCase(), tagTitle});
            // cursor = mySQLiteWrapper.rawQuery("SELECT uid FROM diaro_tags WHERE UPPER(title)  = '" + tagTitle.toUpperCase() + "' OR title = '" + tagTitle + "'", null);
            cursor.moveToFirst();

            if (cursor.getCount() == 1) {
                matchedRowUid = cursor.getString(cursor.getColumnIndex(Tables.KEY_UID));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return matchedRowUid;
    }

    public String findFolderByTitle(String folderTitle) {
        folderTitle = (folderTitle == null) ? "" : folderTitle;
        String matchedRowUid = null;

        Cursor cursor = null;
        try {
            String q = "SELECT uid FROM diaro_folders WHERE UPPER(title)  = ? OR title = ? ";
            cursor = mySQLiteWrapper.rawQuery(q, new String[]{folderTitle.toUpperCase(), folderTitle});

            cursor.moveToFirst();

            if (cursor.getCount() == 1) {
                matchedRowUid = cursor.getString(cursor.getColumnIndex(Tables.KEY_UID));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return matchedRowUid;
    }

    // precision of 6
    public String findLocationByLatLng(String latitude, String longitude, String title) {
        title = (title == null) ? "" : title;
        //title = title.replaceAll("'","\\'");
        String matchedRowUid = null;
        //TODO : round it up to 4 places?

        Cursor cursor1 = null;
        Cursor cursor2 = null;
        try {
            String[] selectionArgs = new String[]{latitude, longitude};
            //"SELECT uid FROM diaro_locations WHERE printf(\"%.3f\",lat) = ? AND printf(\"%.3f\",lng) = ?", selectionArgs);
            cursor1 = mySQLiteWrapper.rawQuery("SELECT uid FROM diaro_locations WHERE lat = ? AND lng = ?", selectionArgs);
            cursor1.moveToFirst();

            if (cursor1.getCount() == 1) {
                matchedRowUid = cursor1.getString(cursor1.getColumnIndex(Tables.KEY_UID));
                AppLog.w("found a mathch with matchedRowUid: " + matchedRowUid);
            }

            if (cursor1.getCount() == 0 && !TextUtils.isEmpty(title)) {
                String q = "SELECT uid FROM diaro_locations WHERE title = ?";
                cursor2 = mySQLiteWrapper.rawQuery(q, new String[]{title});
                cursor2.moveToFirst();

                if (cursor2.getCount() == 1) {
                    matchedRowUid = cursor2.getString(cursor2.getColumnIndex(Tables.KEY_UID));
                }

            }
        } finally {
            if (cursor1 != null) {
                cursor1.close();
            }
            if (cursor2 != null) {
                cursor2.close();
            }
        }
        return matchedRowUid;
    }

    public String getTagsForEntry(String entryUid) {
        String tags = "";

        Cursor rowCursor;
        String q = "SELECT tags FROM diaro_entries WHERE uid = ?";
        //       rowCursor = mySQLiteWrapper.rawQuery(q, new String[]{entryUid});
        rowCursor = mySQLiteWrapper.rawQuery("SELECT tags FROM diaro_entries WHERE uid = '" + entryUid + "'", null);
        rowCursor.moveToFirst();
        if (rowCursor.getCount() == 1) {
            tags = rowCursor.getString(rowCursor.getColumnIndex(Tables.KEY_ENTRY_TAGS));
        }
        rowCursor.close();
        return tags;
    }

    public Cursor getSidemenuGroupsCursor() {
        String moods = MyApp.getInstance().getString(R.string.settings_moods);
        if (moods.contains("'")) {
            // Italian of moods contains '
            moods = moods.replaceAll("'", "''");
        }

        return mySQLiteWrapper.rawQuery(
                "SELECT 0 as " + BaseColumns._ID + ", '" + MyApp.getInstance().getString(R.string.folders) + "' as title" +
                        " UNION SELECT 1 as " + BaseColumns._ID + ", '" + MyApp.getInstance().getString(R.string.tags) + "' as title" +
                        " UNION SELECT 2 as " + BaseColumns._ID + ", '" + MyApp.getInstance().getString(R.string.locations) + "' as title" +
                        " UNION SELECT 3 as " + BaseColumns._ID + ", '" + moods + "' as title",
                null);


        /**  if (PreferencesHelper.isMoodsEnabled()) {


         } else {
         return mySQLiteWrapper.rawQuery(
         "SELECT 0 as " + BaseColumns._ID + ", '" + MyApp.getInstance().getString(R.string.folders) + "' as title" +
         " UNION SELECT 1 as " + BaseColumns._ID + ", '" + MyApp.getInstance().getString(R.string.tags) + "' as title" +
         " UNION SELECT 2 as " + BaseColumns._ID + ", '" + MyApp.getInstance().getString(R.string.locations) + "' as title",
         null);
         } **/

    }


    public String findSameMood(String rowUid, String moodTitle) {
        rowUid = (rowUid == null) ? "" : rowUid;
        moodTitle = (moodTitle == null) ? "" : moodTitle;

        String matchedRowUid = null;

        String[] whereArgs = new String[2];
        whereArgs[0] = moodTitle.toUpperCase(Locale.getDefault());
        whereArgs[1] = rowUid;

        String where = "WHERE UPPER(" + Tables.KEY_MOOD_TITLE + ")=?" + " AND " + Tables.KEY_UID + "!=?";
        Cursor cursor = getSingleRowCursor(Tables.TABLE_MOODS, where, whereArgs);
        if (cursor.getCount() == 1) {
            matchedRowUid = cursor.getString(cursor.getColumnIndex(Tables.KEY_UID));
        }
        cursor.close();
        return matchedRowUid;
    }

    public  Cursor getMoodsCountCursor(){
        return mySQLiteWrapper.rawQuery("SELECT COUNT(*) from diaro_moods", null);
    }


    //TODO
    public Cursor getEntriesCountByHour(int lastDaysCount) {
        return mySQLiteWrapper.rawQuery(
                " SELECT count (uid) , strftime('%H', datetime(e.date/1000, 'unixepoch', e.tz_offset)) as hour  FROM diaro_entries e" +
                        " WHERE strftime(datetime(e.date/1000, 'unixepoch', e.tz_offset)) > strftime(date('now', '-" + lastDaysCount + " day'))" +
                        " GROUP BY hour", null);
    }

    // Stats data  { entry word mood , by weekday and by month }
    public Cursor getStatsCursor(StatsSqlHelper.ChartDataType dataType, long startDate, long endDate, boolean lifeTime, boolean filtered) {
        String[] selectionArgs = null;
        String query = "";

        Pair<String, String[]> pair = EntriesStatic.getEntriesAndSqlByActiveFilters(null);
        String andQuery = ""; // andquery is empty if filtered is false.

        if (filtered) {
            andQuery += pair.first;
            selectionArgs = pair.second;
        }
        if (lifeTime) {
            andQuery = "";
            selectionArgs = null;
        }

        if (dataType == StatsSqlHelper.ChartDataType.ENTRYBYWEEKDAY) {  // 	entry count by weekday 0-6 with Sunday==0 (( %w))
            query = "SELECT strftime('%w', datetime(e.date/1000, 'unixepoch', e.tz_offset)) as weekday, COUNT(*) , date, datetime(e.date / 1000, 'unixepoch') AS utctime, datetime(e.date / 1000, 'unixepoch', tz_offset) AS localtime FROM diaro_entries e WHERE e.archived = 0 ";

            if (startDate != -1 && endDate != -1) {
                query += " AND  localtime BETWEEN datetime( ?/1000 , 'unixepoch') AND datetime( ?/1000 , 'unixepoch') ";
                selectionArgs = new String[]{String.valueOf(startDate), String.valueOf(endDate)};
            }

            query += andQuery;
            query += "  GROUP BY weekday";
        }

        if (dataType == StatsSqlHelper.ChartDataType.ENTRYBYMONTH) {  // entry count by month 01-12 (Jan-Dec) (( %m))
            query = "SELECT strftime('%m', datetime(e.date/1000, 'unixepoch', e.tz_offset)) as month, COUNT(*) , date, datetime(e.date / 1000, 'unixepoch') AS utctime, datetime(e.date / 1000, 'unixepoch', tz_offset) AS localtime FROM diaro_entries e WHERE e.archived = 0 ";
            if (startDate != -1 && endDate != -1) {
                query += " AND  localtime BETWEEN datetime( ?/1000 , 'unixepoch') AND datetime( ?/1000 , 'unixepoch') ";
                selectionArgs = new String[]{String.valueOf(startDate), String.valueOf(endDate)};
            }

            query += andQuery;
            query += "  GROUP BY month";
        }

        if (dataType == StatsSqlHelper.ChartDataType.WORDBYWEEKDAY) {   //word count by weekday 0-6 with Sunday==0 (( %w))
            query = "SELECT strftime('%w', datetime(e.date/1000, 'unixepoch', e.tz_offset)) as weekday , sum( length(title) - length(replace(title, ' ', '')) +1 + length(text) - length(replace(text, ' ', '')) +1 ) , date, datetime(e.date / 1000, 'unixepoch') AS utctime, datetime(e.date / 1000, 'unixepoch', tz_offset) AS localtime FROM diaro_entries e WHERE e.archived = 0 ";
            if (startDate != -1 && endDate != -1) {
                query += " AND  localtime BETWEEN datetime( ?/1000 , 'unixepoch') AND datetime( ?/1000 , 'unixepoch') ";
                selectionArgs = new String[]{String.valueOf(startDate), String.valueOf(endDate)};
            }

            query += andQuery;
            query += "  GROUP BY weekday";
        }

        if (dataType == StatsSqlHelper.ChartDataType.WORDBYMONTH) {     // word count by month 01-12 (Jan-Dec) (( %m))
            query = "SELECT strftime('%m', datetime(e.date/1000, 'unixepoch', e.tz_offset)) as month , sum( length(title) - length(replace(title, ' ', '')) +1 + length(text) - length(replace(text, ' ', '')) +1 ) , date, datetime(e.date / 1000, 'unixepoch') AS utctime, datetime(e.date / 1000, 'unixepoch', tz_offset) AS localtime FROM diaro_entries e WHERE e.archived = 0 ";
            if (startDate != -1 && endDate != -1) {
                query += " AND  localtime BETWEEN datetime( ?/1000 , 'unixepoch') AND datetime( ?/1000 , 'unixepoch') ";
                selectionArgs = new String[]{String.valueOf(startDate), String.valueOf(endDate)};
            }

            query += andQuery;
            query += "  GROUP BY month";
        }

        if (dataType == StatsSqlHelper.ChartDataType.MOODCOUNTBYTYPE) {
            query = "SELECT mood, count(mood), date, datetime(e.date / 1000, 'unixepoch') AS utctime, datetime(e.date / 1000, 'unixepoch', tz_offset) AS localtime FROM diaro_entries e WHERE e.archived = 0 ";
            if (startDate != -1 && endDate != -1) {
                query += " AND  localtime BETWEEN datetime( ?/1000 , 'unixepoch') AND datetime( ?/1000 , 'unixepoch') ";
                query += " AND mood != '' AND mood != '0'";
                selectionArgs = new String[]{String.valueOf(startDate), String.valueOf(endDate)};
            }

            query += andQuery;
            query += "  GROUP BY mood";
        }

        if (dataType == StatsSqlHelper.ChartDataType.MOODAVGBYWEEKDAY) {
            query = "SELECT strftime('%w', datetime(e.date/1000, 'unixepoch', e.tz_offset)) as weekday, avg(mood), date, datetime(e.date / 1000, 'unixepoch') AS utctime, datetime(e.date / 1000, 'unixepoch', tz_offset) AS localtime FROM diaro_entries e WHERE e.archived = 0 ";
            if (startDate != -1 && endDate != -1) {
                query += " AND  localtime BETWEEN datetime( ?/1000 , 'unixepoch') AND datetime( ?/1000 , 'unixepoch') ";
                query += " AND mood != '' AND mood != '0'";
                selectionArgs = new String[]{String.valueOf(startDate), String.valueOf(endDate)};
            }

            query += andQuery;
            query += "  GROUP BY weekday";
        }

        return mySQLiteWrapper.rawQuery(query, selectionArgs);
    }

    // Atlas data
    public Cursor getAtlastData() {
        return mySQLiteWrapper.rawQuery("select de.uid as entry_uid, lat, lng, de.location_uid from diaro_entries de  LEFT JOIN diaro_locations" +
                "  ON de.location_uid = diaro_locations.uid  WHERE de.location_uid != \"\"", null);
    }

    // Memories data
    // Get the count of memories available
    public Cursor getEntriesMemoryCursorCount(boolean includeCurrentYear) {
        String andSQL = " AND  strftime('%Y', datetime(e.date/1000, 'unixepoch', e.tz_offset)) != strftime('%Y', date('now'))"; // exculde current year
        if (includeCurrentYear) {
            andSQL = "";
        }

        return mySQLiteWrapper.rawQuery("SELECT COUNT(*) FROM diaro_entries e " +
                " WHERE strftime('%d-%m', datetime(e.date/1000, 'unixepoch', e.tz_offset)) = strftime('%d-%m', date('now')) " + andSQL, null);
    }

    /**
     * Get the memories available, including the ones of current day and future
     * FUTURE IDEA ->  "AND year !=  strftime('%Y', datetime('now', 'localtime')) " +   // exclude current year, based on prefs?
     */
    public Cursor getEntriesMemoryCursor(boolean includeCurrentYear) {

        String andSQL = " AND year != strftime('%Y', date('now'))"; // exculde current year
        if (includeCurrentYear) {
            andSQL = "";
        }

        // NOTE: strftime('%d-%m', date('now'))  can be replaced by dd-mm provided by java e.g '14-05'
        return mySQLiteWrapper.rawQuery("SELECT uid, strftime('%d-%m', datetime(e.date/1000, 'unixepoch', e.tz_offset)) as daymonth, strftime('%Y', datetime(e.date/1000, 'unixepoch', e.tz_offset)) as year FROM diaro_entries e " +
                " WHERE daymonth = strftime('%d-%m', date('now')) " + andSQL, null);
    }

    // Export
    //48b6afc3a21d255ef0cbe3ec758be441	1573124813000	2019-11-07 11:06:53	2019-11-07 18:06:53	0
    //7892e9c739edd1429aba81c2a11a9022	1572978339754	2019-11-05 18:25:39	2019-11-06 01:25:39	754
    public Cursor getEntriesCursorUidsOnlyByDateRange(long startDate, long endDate) {
        String[] selectionArgs = null;
        selectionArgs = new String[]{String.valueOf(startDate), String.valueOf(endDate)};
        return mySQLiteWrapper.rawQuery("SELECT e.uid, date, datetime(e.date / 1000, 'unixepoch') AS utctime, datetime(e.date / 1000, 'unixepoch', tz_offset) AS localtime,  e.date - e.date/1000*1000 AS only_ms FROM diaro_entries e " +
                " WHERE e.archived = 0 " +
                " AND localtime BETWEEN datetime(  ? /1000  ,'unixepoch') AND datetime(  ? /1000 , 'unixepoch') " +
                " ORDER BY utctime " + getOrderBy() +
                " , only_ms " + getOrderBy(), selectionArgs);
    }

    // Memories data
    // Get the count of memories available
    public Cursor getUnsyncedCursorCount() {
        return mySQLiteWrapper.rawQuery("SELECT COUNT(*) from diaro_entries where synced = 0", null);
    }

    // Tempelates
    public Cursor getTemplatesCursor() {
        return mySQLiteWrapper.rawQuery("SELECT * from diaro_templates ORDER BY date_created DESC", null);
    }

    // Gallery data
    public Cursor getGalleryData() {
        return mySQLiteWrapper.rawQuery("select attachment.filename, datetime(entry.date / 1000, 'unixepoch', tz_offset) AS localtime, entry.uid as entryUid from diaro_entries entry  LEFT JOIN  diaro_attachments attachment ON  entry.uid = attachment.entry_uid where attachment.filename != ''  " +
                "ORDER by entry.date DESC", null);
    }

    // Gallery year sections data
    public Cursor getGalleryYearSection() {
        return mySQLiteWrapper.rawQuery(
                "select COUNT(entry.uid) as imagesCount, strftime('%Y', datetime(entry.date/1000, 'unixepoch', entry.tz_offset)) as year from diaro_entries entry LEFT JOIN  diaro_attachments attachment ON  entry.uid = attachment.entry_uid where attachment.filename != ''  GROUP by year ORDER BY year DESC ", null);
    }

}
