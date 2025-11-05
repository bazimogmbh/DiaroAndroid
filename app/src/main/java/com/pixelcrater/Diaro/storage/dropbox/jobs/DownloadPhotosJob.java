package com.pixelcrater.Diaro.storage.dropbox.jobs;

/**
 * Created by abhishek9851 on 15.05.17.
 */

import android.database.Cursor;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.dropbox.core.v2.files.FileMetadata;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.storage.dropbox.DropboxAccountManager;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;

import java.io.File;
import java.io.FileOutputStream;

public class DownloadPhotosJob extends Job {

    private String mDbxPath;
    private String mFileName;

    private String mRowId = "";

    public DownloadPhotosJob(String dbxPath, String fileName) {
        super(new Params(1).requireNetwork().persist());

        mDbxPath = dbxPath;
        mFileName = fileName;
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {

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

      //  AppLog.e("Start downloading photo-> " + mDbxPath + "-> " + localPath);
        File localFile = new File(localPath);

        // TODO : check existance of media/photos dir somewhere else
        if (!localFile.getParentFile().exists()) {
            localFile.getParentFile().mkdirs();
        }

        FileOutputStream out = new FileOutputStream(localFile);

        FileMetadata metadata = DropboxAccountManager.getDropboxClient(MyApp.getInstance()).files().download(mDbxPath).download(out);
        out.close();

        if(!TextUtils.isEmpty(mRowId))
            MyApp.getInstance().storageMgr.getSQLiteAdapter().updateAttachmentRowFileSyncFields(mRowId, String.valueOf(metadata.getClientModified().getTime()), 1);

        AppLog.i("Downloaded photo -> " + mDbxPath +  " written bytes->" + localFile.length());


    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        MyApp.getInstance().storageMgr.getSQLiteAdapter().updateAttachmentRowFileSyncFields(mRowId, Tables.VALUE_ATTACHMENT_NOT_FOUND, 0);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        AppLog.e("mDbxPath " + mDbxPath + " as missing -> " + runCount + " maxRuncount " + maxRunCount);
      //  AppLog.e("DownloadErrorException " + throwable.getMessage());
        return RetryConstraint.createExponentialBackoff(runCount, 5000);
    }
}