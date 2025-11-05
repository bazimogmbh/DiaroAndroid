package com.pixelcrater.Diaro.storage.dropbox.jobs;

/**
 * Created by abhishek9851 on 15.05.17.
 */

import android.content.ContentValues;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.storage.dropbox.DropboxAccountManager;
import com.pixelcrater.Diaro.storage.dropbox.DropboxStatic;
import com.pixelcrater.Diaro.utils.AES256Cipher;

import java.io.ByteArrayOutputStream;

public class DownloadJsonJob extends Job {

    private String mDbxFile;
    private String mTable;


    public DownloadJsonJob(String dbxFile, String table) {
        super(new Params(1).requireNetwork().persist());
        mDbxFile = dbxFile;
        mTable = table;
    }

    @Override
    public void onAdded() {
        // Job has been saved to disk.
    }

    @Override
    public void onRun() throws Throwable {
        //a job is removed from the queue once  onRun() finishes.
        insertOrUpdateSQLiteRow();
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        // Job has exceeded retry attempts or shouldReRunOnThrowable() has decided to cancel.
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        return RetryConstraint.createExponentialBackoff(runCount, 2000);
    }

    private void insertOrUpdateSQLiteRow() throws Exception {
//        AppLog.d("fullTableName: " + fullTableName +  ", dbxFile.readString(): " + dbxFile.readString());
//
        DbxClientV2 dbxClient = DropboxAccountManager.getDropboxClient(MyApp.getInstance());

        ContentValues cv;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileMetadata metadata = dbxClient.files().download(mDbxFile).download(out);
        String json = out.toString();

   //     AppLog.e("downloaded -> " + AES256Cipher.decodeString(json, DropboxStatic.getEncryptionKey(MyApp.getInstance())));

        cv = DropboxStatic.createRowCvFromJsonString(mTable, AES256Cipher.decodeString(json, DropboxStatic.getEncryptionKey(MyApp.getInstance())));

     //   AppLog.w("Downloaded json-> " + mDbxFile);

        // Set sync_id and synced fields
        cv.put(Tables.KEY_SYNC_ID, String.valueOf(metadata.getClientModified().getTime()));
        cv.put(Tables.KEY_SYNCED, 1);

//      AppLog.d("dbxFile.getPath().toString(): " + dbxFile.getPath().toString() + "\ncv: " + cv);

        // If exists in SQLite database
        if (MyApp.getInstance().storageMgr.getSQLiteAdapter().rowExists(mTable, cv.getAsString(Tables.KEY_UID))) {
            MyApp.getInstance().storageMgr.getSQLiteAdapter().updateRowByUid(mTable, cv.getAsString(Tables.KEY_UID), cv);
        } else {
            MyApp.getInstance().storageMgr.getSQLiteAdapter().insertRow(mTable, cv);
        }
    }




}