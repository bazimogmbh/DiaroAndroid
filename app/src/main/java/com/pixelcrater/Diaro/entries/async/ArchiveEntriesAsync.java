package com.pixelcrater.Diaro.entries.async;

import android.os.AsyncTask;
import android.os.Build;
import android.widget.Toast;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.entries.EntriesStatic;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.Static;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class ArchiveEntriesAsync extends AsyncTask<Object, String, Boolean> {

    private ArrayList<String> mEntriesUids;
    private String error = "";
    private boolean isFinished;

    public ArchiveEntriesAsync(ArrayList<String> entriesUids) {
        // Copy ArrayList
        mEntriesUids = new ArrayList<>(entriesUids);
    }

    @Override
    protected Boolean doInBackground(Object... params) {
//        Static.makePause(1000);

        try {
            EntriesStatic.archiveEntries(mEntriesUids);
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
            error = e.getMessage();
            if (error == null) {
                error = e.toString();
            }
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        setFinished();

        if (result) {
            // Send broadcast to show undo archive toast
            ArrayList<String> params = new ArrayList<>();

            // Objects was added in API 19
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                String val = StringUtils.join(mEntriesUids, ",");
                params.add(val);
            } else {
                String val = mEntriesUids.toString().replace("[", "").replace("]", "").replace(", ", ",");
                params.add(val);
            }

            Static.sendBroadcast(Static.BR_IN_MAIN, Static.DO_SHOW_ENTRY_ARCHIVE_UNDO_TOAST, params);
        } else {
            // Show error toast
            Static.showToast(String.format("%s: %s", MyApp.getInstance().getString(R.string.error), error), Toast.LENGTH_SHORT);

        }
    }

    public void setFinished() {
        isFinished = true;
//        AppLog.d("Finished");
    }

    public boolean isFinished() {
        return isFinished;
    }
}
