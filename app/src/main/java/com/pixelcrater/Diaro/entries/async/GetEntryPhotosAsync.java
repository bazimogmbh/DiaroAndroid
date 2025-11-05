package com.pixelcrater.Diaro.entries.async;

import android.os.AsyncTask;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.entries.viewedit.EntryFragment;
import com.pixelcrater.Diaro.model.AttachmentInfo;
import com.pixelcrater.Diaro.entries.attachments.AttachmentsStatic;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.utils.AppLog;

import java.util.ArrayList;

import static com.pixelcrater.Diaro.config.GlobalConstants.PHOTO;

public class GetEntryPhotosAsync extends AsyncTask<Object, String, Boolean> {

    private EntryFragment mEntryFragment;
    private ArrayList<AttachmentInfo> entryPhotosArrayList;

    public GetEntryPhotosAsync(EntryFragment entryFragment) {
        mEntryFragment = entryFragment;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
//		Static.makePause(1000);

        try {
            if (mEntryFragment.rowUid != null) {
                // Get the list of entry photos
                entryPhotosArrayList = AttachmentsStatic.getEntryAttachmentsArrayList(mEntryFragment.rowUid, PHOTO);

                // Update statePhotoCount
                mEntryFragment.entryInfo.photoCount = entryPhotosArrayList.size();
                //			Static.log("entryInfo.dayPhotoCount: " + entryInfo.dayPhotoCount);
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (!result || !mEntryFragment.isAdded()) {
            return;
        }

        AppLog.d("rowUid: " + mEntryFragment.rowUid);

        // Show entry photos
        if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ENTRY_PHOTOS_POSITION, true)) {
            mEntryFragment.showEntryPhotosAtTop(entryPhotosArrayList);
        } else {
            mEntryFragment.showEntryPhotosAtBottom(entryPhotosArrayList);
        }


    }
}
