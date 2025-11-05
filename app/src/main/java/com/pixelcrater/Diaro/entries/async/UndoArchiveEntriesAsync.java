package com.pixelcrater.Diaro.entries.async;

import android.os.AsyncTask;

import com.pixelcrater.Diaro.entries.EntriesStatic;
import com.pixelcrater.Diaro.utils.AppLog;

import java.util.ArrayList;

public class UndoArchiveEntriesAsync extends AsyncTask<Object, String, Boolean> {

    private ArrayList<String> mEntriesUids;

    public UndoArchiveEntriesAsync(ArrayList<String> entriesUids) {
        mEntriesUids = entriesUids;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        try {
            EntriesStatic.undoArchiveEntries(mEntriesUids);
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
    }
}
