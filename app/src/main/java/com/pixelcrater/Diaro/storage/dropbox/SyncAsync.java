package com.pixelcrater.Diaro.storage.dropbox;

import static com.pixelcrater.Diaro.config.GlobalConstants.DROPBOX_FILEPATH_PROFILE_PHOTO;
import static com.pixelcrater.Diaro.config.GlobalConstants.PHOTO;

import android.content.Context;
import android.os.AsyncTask;

import androidx.preference.PreferenceManager;

import com.dropbox.core.DbxException;
import com.dropbox.core.InvalidAccessTokenException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.entries.attachments.AttachmentsStatic;
import com.pixelcrater.Diaro.storage.SyncStatic;
import com.pixelcrater.Diaro.storage.dropbox.DropboxStatic.DIARO_FILETYPE;
import com.pixelcrater.Diaro.storage.dropbox.jobs.DownloadJsonFiles;
import com.pixelcrater.Diaro.storage.dropbox.jobs.DownloadPhotos;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;
import com.pixelcrater.Diaro.utils.storage.StorageUtils;

import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SyncAsync extends AsyncTask<Object, String, Boolean> {

    public long startMillis = new DateTime().getMillis();
    public String errorMessage;
    private OnAsyncInteractionListener mListener;
    private boolean isFinished;
    private boolean repeatSync = true;

    public SyncAsync(Context context) {
        if (context instanceof OnAsyncInteractionListener) {
            mListener = (OnAsyncInteractionListener) context;
        } else {
          //  throw new RuntimeException(context.toString() + " must implement " + OnAsyncInteractionListener.class);
        }
    }

    public void setRepeatSync() {
        repeatSync = true;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (MyApp.getInstance().storageMgr.getDbxFsAdapter() != null) {
            MyApp.getInstance().storageMgr.getDbxFsAdapter().setVisibleSyncStatus("", "");
        }
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        try {
            throwIfCannotContinue();

            MyApp.getInstance().storageMgr.getDbxFsAdapter().setVisibleSyncStatus("", "");

            int iteration = 0;
            while (repeatSync) {
           //     AppLog.e("Main sync loop, iteration: " + iteration);
                repeatSync = false;
                throwIfCannotContinue();

                MyApp.getInstance().storageMgr.getDbxFsAdapter().setVisibleSyncStatus(MyApp.getInstance().getString(R.string.start_sync), "");

                List<Metadata> filesToDelete = new ArrayList<>();
                List<FileMetadata> filesToDownload = new ArrayList<>();

                MyApp.getInstance().storageMgr.getDbxFsAdapter().setVisibleSyncStatus("Fetching changes..", "");

                //1) Get the changes from server
                DropboxAccountManager.deltaCheck(filesToDownload, filesToDelete);

                MyApp.getInstance().storageMgr.getDbxFsAdapter().setVisibleSyncStatus("Processing changes..", "");

                handleDropboxChanges(filesToDownload, filesToDelete);
                throwIfCannotContinue();

                // 4) Send batchdelete request to server to delete the files which were deleted locally (e.g when the device was offline)
                handleDeletionQueue();

                //6 ) Upload locally added entries/data to server
                new UploadJsonFiles();
                throwIfCannotContinue();

                new UploadAttachments();
                throwIfCannotContinue();

                //upload profile pic if it changed locally
                if (DropboxLocalHelper.hasProfilePicChanged()) {
                    try {
                        File profilePhotoFile = new File(AppLifetimeStorageUtils.getProfilePhotoFilePath());
                        if (profilePhotoFile.exists() && profilePhotoFile.length() > 0) {
                            new DbxUploaderRunnable(profilePhotoFile.getAbsolutePath(), DROPBOX_FILEPATH_PROFILE_PHOTO, DropboxAccountManager.getDropboxClient(MyApp.getInstance())).run();
                        } else {
                            DropboxAccountManager.getDropboxClient(MyApp.getInstance()).files().deleteV2(DROPBOX_FILEPATH_PROFILE_PHOTO);
                        }
                    } catch (DbxException dbx) {
                        DropboxLocalHelper.setProfilePicChanged(false);
                    }
                    DropboxLocalHelper.setProfilePicChanged(false);
                }

                iteration++;
            }
        } catch (InvalidAccessTokenException e) {
            AppLog.e("Exception: " + e, e);

            PreferenceManager.getDefaultSharedPreferences(MyApp.getInstance()).edit().putString(DropboxAccountManager.PREF_DROPBOX_LAST_REVOKED_TOKEN, DropboxAccountManager.getToken(MyApp.getInstance())).apply();
            if (!isFinished) {
                errorMessage = MyApp.getInstance().getString(R.string.error) + ": " + e.getMessage() + " - " + e.getClass();
            }
            return false;
        } catch (Exception e) {
            AppLog.e("Exception: " + e, e);
            if (!isFinished) {
                errorMessage = MyApp.getInstance().getString(R.string.error) + ": " + e.getMessage() + " - " + e.getClass();
            }
            return false;
        }

        return true;
    }

    public static void handleDropboxChanges(List<FileMetadata> filesToDownload, List<Metadata> filesToDelete) throws Exception {
        AppLog.i("Changes detected on server Added->" + filesToDownload.size() + " , Deleted->" + filesToDelete.size());
        boolean backupFilesChanged = false;
        boolean profilePicChanged = false;

        //Handle deleted files
        for (Metadata metadata : filesToDelete) {

            String fileName = metadata.getName();
            DIARO_FILETYPE fileType = DropboxStatic.getFileType(metadata);

            if (fileType == DIARO_FILETYPE.DATA_ATTACHMENTS || fileType == DIARO_FILETYPE.DATA_ENTRIES || fileType == DIARO_FILETYPE.DATA_FOLDERS || fileType == DIARO_FILETYPE.DATA_TAGS || fileType == DIARO_FILETYPE.DATA_MOODS || fileType == DIARO_FILETYPE.DATA_LOCATIONS  || fileType == DIARO_FILETYPE.DATA_TEMPLATES) {
                String fullTableName = DropboxStatic.getFullTableNameFromJsonFilename(fileName);
                String rowId = DropboxStatic.getRowUidFromJsonFilename(fullTableName, fileName);
                if (fullTableName != null) {
                    if (!rowId.isEmpty()) {
                        MyApp.getInstance().storageMgr.getSQLiteAdapter().deleteRowByUid(fullTableName, rowId);
                    } else {
                        AppLog.e("dealing with " + fileName + "rowId was empty");
                    }
                }
            }

            if (fileType == DIARO_FILETYPE.MEDIA_PHOTO) {
                AttachmentsStatic.deleteAttachmentFileFromDevice(PHOTO, fileName);
            }

            if (fileType == DIARO_FILETYPE.PROFILE) {
                profilePicChanged = true;
            }

        }

        if (filesToDelete.size() > 0) {
            MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners();
        }

        // Handle added files

        List<FileMetadata> imageFilestoDownload = new ArrayList<>();

        List<FileMetadata> foldersListToDownload = new ArrayList<>();
        List<FileMetadata> tagsListToDownload = new ArrayList<>();
        List<FileMetadata> moodsListToDownload = new ArrayList<>();
        List<FileMetadata> locationsListToDownload = new ArrayList<>();
        List<FileMetadata> attachmentsListToDownload = new ArrayList<>();
        List<FileMetadata> entriesListToDownload = new ArrayList<>();

        List<FileMetadata> templatesListToDownload = new ArrayList<>();

        int jsonDownloadCount = 0;

        for (FileMetadata metadata : filesToDownload) {

            DIARO_FILETYPE fileType = DropboxStatic.getFileType(metadata);

            if (fileType == DIARO_FILETYPE.DATA_FOLDERS) {
                foldersListToDownload.add(metadata);
                jsonDownloadCount++;
            }
            if (fileType == DIARO_FILETYPE.DATA_TAGS) {
                tagsListToDownload.add(metadata);
                jsonDownloadCount++;
            }
            if (fileType == DIARO_FILETYPE.DATA_MOODS) {
                moodsListToDownload.add(metadata);
                jsonDownloadCount++;
            }
            if (fileType == DIARO_FILETYPE.DATA_LOCATIONS) {
                locationsListToDownload.add(metadata);
                jsonDownloadCount++;
            }
            if (fileType == DIARO_FILETYPE.DATA_ATTACHMENTS) {
                attachmentsListToDownload.add(metadata);
                jsonDownloadCount++;
            }
            if (fileType == DIARO_FILETYPE.DATA_ENTRIES) {
                entriesListToDownload.add(metadata);
                jsonDownloadCount++;
            }

            if (fileType == DIARO_FILETYPE.DATA_TEMPLATES) {
                templatesListToDownload.add(metadata);
                jsonDownloadCount++;
            }

            if (fileType == DIARO_FILETYPE.MEDIA_PHOTO) {
                imageFilestoDownload.add(metadata);
            }

            if (fileType == DIARO_FILETYPE.PROFILE) {
                profilePicChanged = true;
            }
        }

        // 4) Download changes json files from server
        if (jsonDownloadCount > 0) {
            new DownloadJsonFiles(foldersListToDownload, tagsListToDownload, moodsListToDownload, locationsListToDownload, attachmentsListToDownload, entriesListToDownload, templatesListToDownload);
        }

        // 5) Download images from server
        if (imageFilestoDownload.size() > 0) {
            new DownloadPhotos(imageFilestoDownload);
        }


        if (profilePicChanged) {
            profilePhotoPathListenerAction();
        }

    }

    private static void profilePhotoPathListenerAction() {
        File localFile = new File(AppLifetimeStorageUtils.getProfilePhotoFilePath());

        try {
            boolean exists = DropboxLocalHelper.exists(DropboxAccountManager.getDropboxClient(MyApp.getInstance()), DROPBOX_FILEPATH_PROFILE_PHOTO);
            AppLog.d("fs.exists(DROPBOX_FILEPATH_PROFILE_PHOTO): " + exists + ", localFile.exists(): " + localFile.exists());

            if (!exists && localFile.exists()) {
                AppLog.d("Delete profile photo");

                // Delete profile photo file from SD card
                StorageUtils.deleteFileOrDirectory(localFile);

                // Send broadcasts to update UI and profile photo
                Static.sendBroadcastsToUpdateProfilePhoto();
            } else {
                MyApp.getInstance().asyncsMgr.executeSyncProfilePhotoAsync();
            }
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }
    }

    private void handleDeletionQueue() throws DbxException {
        List<String> deletionQueue = DropboxLocalHelper.getDeletionQueue();
        DropboxAccountManager.batchDelete(deletionQueue);
    }


    @Override
    protected void onCancelled() {
        super.onCancelled();
        setFinished();
    }

    public void setFinished() {
        isFinished = true;
        AppLog.d("Finished");

        if (MyApp.getInstance().storageMgr.getDbxFsAdapter() != null) {
            MyApp.getInstance().storageMgr.getDbxFsAdapter().setVisibleSyncStatus("", "");
        }

        if (mListener != null) {
            mListener.onSyncAsyncFinished();
        }

        MyApp.getInstance().checkAppState();
    }

    @Override
    protected void onPostExecute(Boolean success) {
        setFinished();
    }

    void throwIfCannotContinue() throws Exception {
        throwIfHasError();
        throwIfCanceled();
        Static.throwIfDropboxNotConnected();
        Static.throwIfNotPro();
        Static.throwExceptionIfRestoringOrCreatingBackup();
        Static.throwIfNotConnectedToInternet();
        SyncStatic.throwIfSyncOnWiFiOnlyPrefNotOk();
    }

    private void throwIfHasError() throws Exception {
        if (errorMessage != null) {
            throw new Exception(errorMessage);
        }
    }

    private void throwIfCanceled() throws Exception {
        if (isCancelled() || isFinished) {
            throw new Exception(MyApp.getInstance().getString(R.string.error_sync_canceled));
        }
    }

    public boolean isFinished() {
        return isFinished;
    }

    interface OnAsyncInteractionListener {
        void onSyncAsyncFinished();
    }

}
