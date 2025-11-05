package com.pixelcrater.Diaro.entries.async;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.widget.Toast;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.config.GlobalConstants;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.model.AttachmentInfo;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.storage.StorageUtils;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileOutputStream;

public class RotatePhotoAsync extends AsyncTask<Object, String, Boolean> {

    private final int mDegree;
    private final AttachmentInfo mAttachmentInfo;
    private String error;
    private File photoFile;

    public RotatePhotoAsync(AttachmentInfo attachmentInfo, int degree) {

        mAttachmentInfo = attachmentInfo;
        mDegree = degree;
    }

    @Override
    protected Boolean doInBackground(Object... params) {

        String path = mAttachmentInfo.getFilePath();

        photoFile = new File(path);

        try {
            androidx.exifinterface.media.ExifInterface oldExif = new androidx.exifinterface.media.ExifInterface(path);

            Bitmap newBitmap = BitmapFactory.decodeFile(path);
            Matrix matrix = new Matrix();
            matrix.postRotate(mDegree);

            Bitmap rotatedBitmap = Bitmap.createBitmap(newBitmap, 0, 0, newBitmap.getWidth(), newBitmap.getHeight(), matrix, true);

            FileOutputStream out = new FileOutputStream(path);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            rotatedBitmap.recycle();
            out.close();

            StorageUtils.copyExif(oldExif, path);

            // Set new last modified date
            long nowMillis = new DateTime().getMillis();
            AppLog.d("nowMillis: " + nowMillis);
            photoFile.setLastModified(nowMillis);

           // setNewOrientationValue();

            // Clear KEY_ATTACHMENT_FILE_SYNC_ID
            ContentValues cv = new ContentValues();
            cv.put(Tables.KEY_ATTACHMENT_FILE_SYNC_ID, "");
            cv.put(Tables.KEY_ATTACHMENT_FILE_SYNCED, 0);
            MyApp.getInstance().storageMgr.getSQLiteAdapter().updateRowByUid(Tables.TABLE_ATTACHMENTS, mAttachmentInfo.uid, cv);

            MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners();
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
        if (!result) {
            // Show error toast
            Static.showToast(String.format("%s: %s", MyApp.getInstance().getString(R.string.error), error), Toast.LENGTH_SHORT);

        }
    }
}
