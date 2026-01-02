package com.pixelcrater.Diaro.storage.dropbox;

import android.os.AsyncTask;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.WriteMode;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;
import com.pixelcrater.Diaro.utils.storage.StorageUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;

import static com.pixelcrater.Diaro.config.GlobalConstants.DROPBOX_FILENAME_PROFILE;
import static com.pixelcrater.Diaro.config.GlobalConstants.DROPBOX_FILEPATH_PROFILE_PHOTO;
import static com.pixelcrater.Diaro.config.GlobalConstants.DROPBOX_PATH_PROFILE;

public class SyncProfilePhotoAsync extends AsyncTask<Object, String, Boolean> {
    @Override
    protected Boolean doInBackground(Object... params) {
        AppLog.d("");

        syncProfilePhoto();
        deleteNotProfilePhotoFiles();

        return true;
    }

    private void syncProfilePhoto() {
        AppLog.d("");

        try {
            // Get local file size
            File localFile = new File(AppLifetimeStorageUtils.getProfilePhotoFilePath());
            long localSize = localFile.length();
            AppLog.d("localSize: " + localSize);

            DbxClientV2 dbxClient = DropboxAccountManager.getDropboxClient(MyApp.getInstance());
            if (DropboxLocalHelper.exists(dbxClient, DROPBOX_FILEPATH_PROFILE_PHOTO)) {
                // Get file size in FS
                FileMetadata metadata = (FileMetadata) dbxClient.files().getMetadata(DROPBOX_FILEPATH_PROFILE_PHOTO);
                long fsSize = metadata.getSize();
                AppLog.d("localSize: " + localSize + ", fsSize: " + fsSize);

                if (fsSize > 0 && localSize != fsSize) {
                    // Download profile photo file from DbxFilesystem
                    downloadFileFromDropbox(DROPBOX_FILEPATH_PROFILE_PHOTO, AppLifetimeStorageUtils.getProfilePhotoFilePath());

                    // Send broadcasts to update UI and profile photo
                    Static.sendBroadcastsToUpdateProfilePhoto();
                }
            } else {
                if (localFile.exists()) {
                    AppLog.d("Upload local profile photo");

                    // Write profile photo file to DbxFilesystem
                    uploadFileToDbxFs(AppLifetimeStorageUtils.getProfilePhotoFilePath(), DROPBOX_FILEPATH_PROFILE_PHOTO);
                }
            }
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }
    }

    private boolean downloadFileFromDropbox(String dropboxFilePath, String localFilePath) {
        AppLog.d("dropboxFilePath: " + dropboxFilePath + ", localFilePath: " + localFilePath);

        DbxClientV2 dbxClient = DropboxAccountManager.getDropboxClient(MyApp.getInstance());
        boolean result = false;
        try {
            File localFile = new File(localFilePath);
            //noinspection ResultOfMethodCallIgnored
            localFile.getParentFile().mkdirs();

            if (DropboxAccountManager.isLoggedIn(MyApp.getInstance()) &&
                    DropboxLocalHelper.exists(dbxClient, dropboxFilePath)) {
                Metadata metadata = dbxClient.files().getMetadata(dropboxFilePath);
                if (metadata instanceof FileMetadata) {
                    // Open file (start downloading)
                    DbxDownloader<FileMetadata> downloader = dbxClient.files().download(dropboxFilePath);
                    StorageUtils.copyInputStreamToFile(downloader.getInputStream(), localFile);
                    downloader.close();
                    result = true;
                }
            }
        } catch (Exception e) {
            AppLog.e(String.format("Error downloading file from Dropbox: %s", e.getMessage()));
        }

        AppLog.d("Download end dropboxFilePath: " + dropboxFilePath);
        return result;
    }

    private void throwIfCannotContinue() throws Exception {
        Static.throwIfDropboxNotConnected();
        Static.throwIfNotConnectedToInternet();
    }

    private boolean uploadFileToDbxFs(String localFilePath, String dropboxFilePath) {
        AppLog.d("localFilePath: " + localFilePath + ", dropboxFilePath: " + dropboxFilePath);

        DbxClientV2 dbxClient = DropboxAccountManager.getDropboxClient(MyApp.getInstance());
        boolean result = false;
        try {
            File localFile = new File(localFilePath);

            if (!localFile.isFile()) {
                AppLog.d("return because localFile.isFile(): " + localFile.isFile() +
                        ", localFilePath: " + localFilePath);
                return false;
            }

            if (DropboxAccountManager.isLoggedIn(MyApp.getInstance())) {
                FileInputStream in = new FileInputStream(localFile);
                dbxClient.files()
                        .uploadBuilder(localFilePath)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(in);
                in.close();
            }

            result = true;
        } catch (Exception e) {
            AppLog.e(String.format("Error uploading file to Dropbox: %s", e.getMessage()));
        }

        AppLog.d("Upload end localFilePath: " + localFilePath);
        return result;
    }

    private void deleteNotProfilePhotoFiles() {
        AppLog.d("");

        DbxClientV2 dbxClient = DropboxAccountManager.getDropboxClient(MyApp.getInstance());

        try {
            if (DropboxLocalHelper.exists(dbxClient, DROPBOX_PATH_PROFILE)) {
                ListFolderResult listResult = dbxClient.files().listFolder(DROPBOX_PATH_PROFILE);
                for (Metadata metadata : listResult.getEntries()) {
                    throwIfCannotContinue();
                    if (metadata instanceof FileMetadata) {
                        if (!StringUtils.equals(metadata.getName(), DROPBOX_FILENAME_PROFILE)) {
                            dbxClient.files().deleteV2(metadata.getPathLower());
                        }
                    }
                }
            }
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }
    }
}
