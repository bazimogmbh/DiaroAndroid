package com.pixelcrater.Diaro.entries.viewedit;

import android.os.AsyncTask;

import com.pixelcrater.Diaro.model.AttachmentInfo;
import com.pixelcrater.Diaro.entries.attachments.AttachmentsStatic;

import java.util.ArrayList;

import static com.pixelcrater.Diaro.config.GlobalConstants.PHOTO;

public class GetPhotoGridAsync extends AsyncTask<Object, String, Boolean> {
    private String mEntryUid;
    private ArrayList<AttachmentInfo> entryPhotosArrayList;

    // On async finish listener
    private OnAsyncFinishListener onAsyncFinishListener;

    public GetPhotoGridAsync(String entryUid) {
        mEntryUid = entryUid;
    }

    public void setOnAsyncFinishListener(OnAsyncFinishListener l) {
        onAsyncFinishListener = l;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
//        Static.makePause(1000);

        try {
            if (mEntryUid != null) {
                // Get the list of entry photos
                entryPhotosArrayList = AttachmentsStatic.getEntryAttachmentsArrayList(mEntryUid, PHOTO);

            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (!result) {
            return;
        }

        if (onAsyncFinishListener != null) {
            onAsyncFinishListener.onAsyncFinish(entryPhotosArrayList);
        }
    }

    public interface OnAsyncFinishListener {
        void onAsyncFinish(ArrayList<AttachmentInfo> entryPhotosArrayList);
    }
}
