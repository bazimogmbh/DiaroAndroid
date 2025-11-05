package com.pixelcrater.Diaro.storage.dropbox.jobs;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.dropbox.core.v2.files.FileMetadata;
import com.pixelcrater.Diaro.storage.dropbox.DropboxAccountManager;
import com.pixelcrater.Diaro.utils.AppLog;

import java.io.File;

/**
 * Upload a file to dropbox in background
 */
public class UploadFileToDbxWorker extends Worker {

    public UploadFileToDbxWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    public Result doWork() {
        // Get the input
        Data taskData = getInputData();

        String mFileName = taskData.getString("localPath");
        String mDropboxFilePath = taskData.getString("dbxPath");

        if(mFileName == null){
            AppLog.e("UploadBackupFileWorker got a null filename");
            return  Result.failure();
        }

        File backupFile = new File(mFileName);

        try  {
           FileMetadata metadata =  DropboxAccountManager.uploadFile(backupFile, mDropboxFilePath);
           AppLog.e(metadata.toString());
        } catch ( Exception e) {
            return Result.failure();
        }

        // Return the output
        return Result.success();
    }
}


