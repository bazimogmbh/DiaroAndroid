package com.pixelcrater.Diaro.storage.dropbox;

import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by abhishek9851 on 23.01.17.
 */

public class DbxUploaderRunnable implements Runnable {

    String localFilePath;
    String remoteFilePath;
    DbxClientV2 mDbxCleint ;

    DbxUploaderRunnable(String localFilePath, String remoteFilePath, DbxClientV2 dbxClient) {
        this.localFilePath = localFilePath;
        this.remoteFilePath = remoteFilePath;
        this.mDbxCleint = dbxClient;
    }

    @Override
    public void run() {
        upload();
    }

    private void upload() {
        try (InputStream in = new FileInputStream(localFilePath)) {
            FileMetadata metadata = mDbxCleint .files().uploadBuilder(remoteFilePath).withMode(WriteMode.OVERWRITE).uploadAndFinish(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}