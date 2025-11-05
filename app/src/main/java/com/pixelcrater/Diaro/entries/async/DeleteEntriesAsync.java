package com.pixelcrater.Diaro.entries.async;

import android.os.AsyncTask;

import com.pixelcrater.Diaro.entries.EntriesStatic;
import com.pixelcrater.Diaro.utils.AppLog;

import java.util.ArrayList;

public class DeleteEntriesAsync extends AsyncTask<Object, String, Boolean> {

    private ArrayList<String> mEntriesUidsArrayList;

    public DeleteEntriesAsync(ArrayList<String> entriesUidsArrayList) {
        mEntriesUidsArrayList = entriesUidsArrayList;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        try {
            if (mEntriesUidsArrayList.size() > 0) {
                EntriesStatic.deleteEntries(mEntriesUidsArrayList);
            }
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
