package com.pixelcrater.Diaro.storage;

import android.content.ContentValues;
import android.database.Cursor;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.storage.dropbox.DbxFsAdapter;
import com.pixelcrater.Diaro.storage.dropbox.DropboxAccountManager;
import com.pixelcrater.Diaro.storage.dropbox.DropboxLocalHelper;
import com.pixelcrater.Diaro.storage.dropbox.DropboxStatic;
import com.pixelcrater.Diaro.storage.dropbox.SyncService;
import com.pixelcrater.Diaro.storage.sqlite.SQLiteAdapter;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.Static;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StorageMgr {

    // Dropbox filesystem adapter
    public DbxFsAdapter dbxFsAdapter;
    // Diaro SQLite database adapter
    private SQLiteAdapter sqliteAdapter;
    // Listeners
    private ConcurrentHashMap<String, OnStorageDataChangeListener> onStorageDataChangeListenersMap = new ConcurrentHashMap<>();

    private Runnable notifyOnStorageDataChangeListeners_r = () -> {
        notifyOnStorageDataChangeListeners();

        if (MyApp.getInstance().networkStateMgr.isConnectedToInternet() && isStorageDropbox()) {
            // Start sync service
            SyncService.startService();
        }
    };

    public StorageMgr() {
        getSQLiteAdapter();
    }

    public synchronized SQLiteAdapter getSQLiteAdapter() {
        if (sqliteAdapter == null || sqliteAdapter.mySQLiteWrapper == null || !sqliteAdapter.mySQLiteWrapper.isOpen()) {
            sqliteAdapter = new SQLiteAdapter();
        }

        return sqliteAdapter;
    }

    public void resetSQLiteAdapter() {
        getSQLiteAdapter().closeDatabase();
        sqliteAdapter = null;
    }

    public synchronized DbxFsAdapter getDbxFsAdapter() {
        if (DropboxAccountManager.isLoggedIn(MyApp.getInstance())) {
            if (dbxFsAdapter == null) {
                dbxFsAdapter = new DbxFsAdapter();
            }
        } else {
            dbxFsAdapter = null;
        }

        return dbxFsAdapter;
    }

    public boolean isStorageDropbox() {
        return DropboxAccountManager.isLoggedIn(MyApp.getInstance()) && Static.isProUser();
    }

    public String insertRow(String fullTableName, ContentValues cv) {
        return getSQLiteAdapter().insertRow(fullTableName, cv);
    }

    public void updateRowByUid(String fullTableName, String rowUid, ContentValues cv) {
        // Clear sync_id field and reset synced field to 0
        if (!cv.containsKey(Tables.KEY_SYNC_ID)) {
            cv.put(Tables.KEY_SYNC_ID, "");
        }
        if (!cv.containsKey(Tables.KEY_SYNCED)) {
            cv.put(Tables.KEY_SYNCED, 0);
        }
        getSQLiteAdapter().updateRowByUid(fullTableName, rowUid, cv);
    }

    public void updateRows(String fullTableName, String fullWhere, String[] whereArgs, ContentValues cv) {

        Cursor cursor = getSQLiteAdapter().getRowsUidsCursor(fullTableName, fullWhere, whereArgs);
        int uidColumnIndex = cursor.getColumnIndex(Tables.KEY_UID);

        while (cursor.moveToNext()) {
            updateRowByUid(fullTableName, cursor.getString(uidColumnIndex), cv);
        }
        cursor.close();
    }

    public void deleteRowByUid(String fullTableName, String rowUid) {
        getSQLiteAdapter().deleteRowByUid(fullTableName, rowUid);

        if (isStorageDropbox()) {
            DropboxLocalHelper.markFileForDeletion(DropboxStatic.getDbxJsonFilePath(fullTableName, rowUid));
        }

        scheduleNotifyOnStorageDataChangeListeners();
    }

    public void clearAllData() {
        clearTableData(Tables.TABLE_ENTRIES);
        clearTableData(Tables.TABLE_ATTACHMENTS);
        clearTableData(Tables.TABLE_FOLDERS);
        clearTableData(Tables.TABLE_TAGS);
        //clearTableData(Tables.TABLE_MOODS);
        clearTableData(Tables.TABLE_LOCATIONS);
        clearTableData(Tables.TABLE_TEMPLATES);

        // Truncate tables to reset sqlite_sequence
        getSQLiteAdapter().truncateAllTables();
    }

    public void clearTableData(String fullTableName) {
        Cursor cursor = getSQLiteAdapter().getRowsUidsCursor(fullTableName, "", null);

        int uidColumnIndex = cursor.getColumnIndex(Tables.KEY_UID);

        while (cursor.moveToNext()) {
            deleteRowByUid(fullTableName, cursor.getString(uidColumnIndex));
        }
        cursor.close();
    }

    // *** Listeners ***
    public void scheduleNotifyOnStorageDataChangeListeners() {

        MyApp.getInstance().handler.removeCallbacks(notifyOnStorageDataChangeListeners_r);
        MyApp.getInstance().handler.postDelayed(notifyOnStorageDataChangeListeners_r, 250);
    }

    private void notifyOnStorageDataChangeListeners() {
        AppLog.d("onStorageDataChangeListenersMap: " + onStorageDataChangeListenersMap);

        for (Map.Entry<String, OnStorageDataChangeListener> mapEntry : onStorageDataChangeListenersMap.entrySet()) {
            mapEntry.getValue().onStorageDataChange();
        }
    }

    public void addOnStorageDataChangeListener(OnStorageDataChangeListener listener) {
        onStorageDataChangeListenersMap.put(listener.toString(), listener);
    }

    public void removeOnStorageDataChangeListener(OnStorageDataChangeListener listener) {
        onStorageDataChangeListenersMap.remove(listener.toString());
    }
}
