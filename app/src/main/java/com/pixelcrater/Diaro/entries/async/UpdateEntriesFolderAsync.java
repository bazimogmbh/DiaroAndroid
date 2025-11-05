package com.pixelcrater.Diaro.entries.async;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.entries.EntriesStatic;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.AppLog;

import java.util.ArrayList;

public class UpdateEntriesFolderAsync extends AsyncTask<Object, String, Boolean> {

    private Context mContext;
    private ArrayList<String> mEntriesUids;
    private String mSelectedFolderUid;
    private String error = "";

    public UpdateEntriesFolderAsync(Context context, ArrayList<String> entriesUids, String selectedFolderUid) {
        mContext = context;
        mSelectedFolderUid = selectedFolderUid;

        // Copy ArrayList
        mEntriesUids = new ArrayList<>(entriesUids);
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        try {
            EntriesStatic.updateEntriesFolder(mEntriesUids, mSelectedFolderUid);
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
        if (mContext == null) {
            return;
        }

        if (!result) {
            // Show error toast
            Static.showToast(String.format("%s: %s", MyApp.getInstance().getString(R.string.error), error), Toast.LENGTH_SHORT);
        }
    }
}
