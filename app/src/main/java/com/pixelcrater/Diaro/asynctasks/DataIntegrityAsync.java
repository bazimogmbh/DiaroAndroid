package com.pixelcrater.Diaro.asynctasks;

import android.database.Cursor;
import android.os.AsyncTask;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.entries.EntriesStatic;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.AppLog;

import java.util.ArrayList;

public class DataIntegrityAsync extends AsyncTask<Object, String, Boolean> {

    public DataIntegrityAsync() {
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        // Delete archived entries rows
        deleteArchivedEntries();

        return true;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        onFinish();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        onFinish();
    }

    private void onFinish() {
        MyApp.getInstance().checkAppState();
    }

    private void deleteArchivedEntries() {
        ArrayList<String> entriesUids = new ArrayList<>();

        Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getRowsUidsCursor(Tables.TABLE_ENTRIES, "WHERE " + Tables.KEY_ENTRY_ARCHIVED + "=1", null);
        int entryUidColumnIndex = cursor.getColumnIndex(Tables.KEY_UID);
        AppLog.d("cursor.getCount(): " + cursor.getCount());

        while (cursor.moveToNext()) {
            entriesUids.add(cursor.getString(entryUidColumnIndex));
        }
        cursor.close();

        // Delete entries
        try {
            if (entriesUids.size() > 0) {
                EntriesStatic.deleteEntries(entriesUids);
            }
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }
    }
}
