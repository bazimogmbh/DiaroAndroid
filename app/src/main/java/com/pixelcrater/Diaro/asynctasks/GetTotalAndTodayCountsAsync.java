package com.pixelcrater.Diaro.asynctasks;

import android.os.AsyncTask;
import androidx.fragment.app.Fragment;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.AppLog;

import org.joda.time.DateTime;

public class GetTotalAndTodayCountsAsync extends AsyncTask<Object, String, Boolean> {
    private boolean isCancelled;
    private int totalEntriesCount;
    private int todayEntriesCount;
    private OnAsyncInteractionListener mListener;

    public GetTotalAndTodayCountsAsync(Fragment fragment) {
        if (fragment instanceof OnAsyncInteractionListener) {
            mListener = (OnAsyncInteractionListener) fragment;
        } else {
            throw new RuntimeException(fragment.toString() + " must implement " + OnAsyncInteractionListener.class);
        }
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    protected Boolean doInBackground(Object... params) {

        DateTime todayDt = new DateTime().withTimeAtStartOfDay();
        totalEntriesCount = MyApp.getInstance().storageMgr.getSQLiteAdapter().getRowsCount(Tables.TABLE_ENTRIES, "WHERE " + Tables.KEY_ENTRY_ARCHIVED + "=0", null);

        if (isCancelled) {
            return false;
        }

        todayEntriesCount = MyApp.getInstance().storageMgr.getSQLiteAdapter().
                getRowsCount(Tables.TABLE_ENTRIES, "WHERE " + Tables.KEY_ENTRY_ARCHIVED + "=0"
                                + " AND " + Tables.getLocalTime(Tables.KEY_ENTRY_DATE) +
                                " BETWEEN " + Tables.getLocalTime(todayDt.getMillis()) +
                                " AND " + Tables.getLocalTime(todayDt.plusDays(1).getMillis())
                        , null);

        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        AppLog.d("success: " + success);

        if (isCancelled) {
            return;
        }

        if (success) {
            if (mListener != null) {
                mListener.onGetTotalAndTodayCountsAsyncFinished(totalEntriesCount, todayEntriesCount);
            }
        }
    }

    public interface OnAsyncInteractionListener {
        void onGetTotalAndTodayCountsAsyncFinished(int totalEntriesCount, int todayEntriesCount);
    }
}
