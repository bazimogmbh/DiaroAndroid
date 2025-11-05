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

public class SelectAllEntriesAsync extends AsyncTask<Object, String, Boolean> {
    private OnAsyncInteractionListener mListener;
    private ArrayList<String> entriesArrayList = new ArrayList<>();

    public SelectAllEntriesAsync(Context context) {
//		AppLog.d("");

        if (context instanceof OnAsyncInteractionListener) {
            mListener = (OnAsyncInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()+ " must implement " + OnAsyncInteractionListener.class);
        }
    }

    @Override
    protected Boolean doInBackground(Object... params) {
//		AppLog.d("");

        try {
            Pair<String, String[]> pair = EntriesStatic.getEntriesAndSqlByActiveFilters(null);

            Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getEntriesCursorUidsOnly(pair.first, pair.second);
            int entryUidColumnIndex = cursor.getColumnIndex(Tables.KEY_UID);

            while (cursor.moveToNext()) {
                entriesArrayList.add(cursor.getString(entryUidColumnIndex));
            }
            cursor.close();
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
//		AppLog.d("result: " + result);

        if (mListener != null) {
            mListener.onSelectAllEntriesAsyncFinished(entriesArrayList);
        }
    }

    public interface OnAsyncInteractionListener {
        void onSelectAllEntriesAsyncFinished(ArrayList<String> entriesArrayList);
    }
}
