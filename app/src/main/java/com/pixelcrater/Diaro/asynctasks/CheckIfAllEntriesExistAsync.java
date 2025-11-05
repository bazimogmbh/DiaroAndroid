package com.pixelcrater.Diaro.asynctasks;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import androidx.core.util.Pair;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.entries.EntriesStatic;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.AppLog;

import java.util.ArrayList;

public class CheckIfAllEntriesExistAsync extends AsyncTask<Object, String, Boolean> {
    private final ArrayList<String> entriesUids;
    private OnAsyncInteractionListener mListener;
    private ArrayList<String> notExistingEntriesUids = new ArrayList<>();

    public CheckIfAllEntriesExistAsync(Context context, ArrayList<String> multiSelectedEntriesUids) {
        // AppLog.d("");

        entriesUids = new ArrayList<>(multiSelectedEntriesUids);

        if (context instanceof OnAsyncInteractionListener) {
            mListener = (OnAsyncInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement " + OnAsyncInteractionListener.class);
        }
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        // AppLog.d("");

        try {
            Pair<String, String[]> pair = EntriesStatic.getEntriesAndSqlByActiveFilters(null);

            Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getEntriesCursorUidsOnly(pair.first, pair.second);
            ArrayList<String> entriesUidsArrayList = getEntriesUidsArrayList(cursor);

            for (int i = 0; i < entriesUids.size(); i++) {
                if (!entriesUidsArrayList.contains(entriesUids.get(i))) {
                    // AppLog.d("Entry does not exist in list: " +
                    // entriesUids.get(i));
                    notExistingEntriesUids.add(entriesUids.get(i));
                }
            }

            cursor.close();
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
            return false;
        }

        return true;
    }

    private ArrayList<String> getEntriesUidsArrayList(Cursor cursor) {
        ArrayList<String> entriesArrayList = new ArrayList<>();

        int entryUidColumnIndex = cursor.getColumnIndex(Tables.KEY_UID);

        while (cursor.moveToNext()) {
            entriesArrayList.add(cursor.getString(entryUidColumnIndex));
        }
        // don't close cursor

        return entriesArrayList;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        // AppLog.d("result: " + result);

        if (mListener != null) {
            mListener.onCheckIfAllEntriesExistAsyncFinished(notExistingEntriesUids);
        }
    }

    public interface OnAsyncInteractionListener {
        void onCheckIfAllEntriesExistAsyncFinished(ArrayList<String> notExistingEntriesUids);
    }
}
