package com.pixelcrater.Diaro.storage.dropbox.jobs;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.dropbox.core.v2.files.FileMetadata;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.storage.dropbox.DropboxAccountManager;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;

import java.io.File;
import java.io.FileOutputStream;

public class DownloadPhotosWorker extends Worker {

    private String mRowId = "";

    public DownloadPhotosWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {

        // Get the input
        Data taskData = getInputData();

        String mDbxPath = taskData.getString("dbxPath");
        String  mFileName = taskData.getString("fileName");

        Cursor cursor = null;
        try {
            cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getAttachmentUidByFileNameAndType(mFileName, "photo");

            int attachmentUidColumnIndex = cursor.getColumnIndex(Tables.KEY_UID);
            while (cursor.moveToNext()) {
                mRowId = cursor.getString(attachmentUidColumnIndex);
            }

        } catch (Exception e) {
            AppLog.e("Error checking if file should be downloaded: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        String localPath = String.format("%s/%s", AppLifetimeStorageUtils.getMediaPhotosDirPath(), mFileName);

        AppLog.e("Start downloading photo-> " + mDbxPath + "-> " + localPath);
        File localFile = new File(localPath);

        // TODO : check existance of media/photos dir somewhere else
        if (!localFile.getParentFile().exists()) {
            localFile.getParentFile().mkdirs();
        }

        try  {
            FileOutputStream out = new FileOutputStream(localFile);

            FileMetadata metadata = DropboxAccountManager.getDropboxClient(MyApp.getInstance()).files().download(mDbxPath).download(out);
            out.close();

            if(!TextUtils.isEmpty(mRowId))
                MyApp.getInstance().storageMgr.getSQLiteAdapter().updateAttachmentRowFileSyncFields(mRowId, String.valueOf(metadata.getClientModified().getTime()), 1);

            AppLog.e("Done downloading photo -> " + mDbxPath + " ,rowId->"+ mRowId + " ,written bytes->" + localFile.length());
        } catch ( Exception e) {
            Result.failure();
        }



        // Return the output
        return Result.success();
    }
}


