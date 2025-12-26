package com.pixelcrater.Diaro.storage.dropbox;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.log.CustomLogger;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CommitInfo;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.UploadSessionCursor;
import com.dropbox.core.v2.files.UploadSessionFinishArg;
import com.dropbox.core.v2.files.UploadSessionFinishBatchJobStatus;
import com.dropbox.core.v2.files.UploadSessionFinishBatchResult;
import com.dropbox.core.v2.files.UploadSessionFinishBatchResultEntry;
import com.dropbox.core.v2.files.UploadSessionFinishError;
import com.dropbox.core.v2.files.UploadSessionStartResult;
import com.dropbox.core.v2.files.UploadSessionStartUploader;
import com.dropbox.core.v2.files.WriteMode;
import com.pixelcrater.Diaro.config.AppConfig;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.AES256Cipher;
import com.pixelcrater.Diaro.utils.AppLog;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.pixelcrater.Diaro.config.GlobalConstants.BATCH_UPLOAD_SIZE;

class UploadJsonFiles {

    private final int WORKER_COUNT = 15;
    private List<List<UploadData>> mBatchUploads = new ArrayList<>();
    private List<UploadSessionFinishArg> mUploadSessionFinishArgList;

    UploadJsonFiles() throws Exception {
        mJobManager.stop();
        addFilesToUploadQueue();
        while (mBatchUploads.size() > 0) {
            batchUpload();
        }
        mJobManager.stop();
        mJobManager.destroy();
    }

    private void addFilesToUploadQueue() throws Exception {

        int foldersCount = addFilesToUploadQueueByTable(Tables.TABLE_FOLDERS);
        int tagsCount = addFilesToUploadQueueByTable(Tables.TABLE_TAGS);
        int moodsCount = addFilesToUploadQueueByTable(Tables.TABLE_MOODS);
        int locationsCount = addFilesToUploadQueueByTable(Tables.TABLE_LOCATIONS);
        int entriesCount = addFilesToUploadQueueByTable(Tables.TABLE_ENTRIES);
        int attachmentsCount = addFilesToUploadQueueByTable(Tables.TABLE_ATTACHMENTS);

        int templatesCount = addFilesToUploadQueueByTable(Tables.TABLE_TEMPLATES);

        int totalCount = foldersCount + tagsCount + moodsCount + locationsCount + attachmentsCount + entriesCount+ templatesCount;
        if (totalCount > 0)
            AppLog.e("Total UploadJsonFiles count" + totalCount);

        // Update sync status
        updateCountInSyncStatus();
    }


    private void batchUpload() throws Exception {
        DbxClientV2 dbxClient = DropboxAccountManager.getDropboxClient(MyApp.getInstance());
        mJobManager.stop();

        mUploadSessionFinishArgList = Collections.synchronizedList(new ArrayList<>());

        List<UploadData> thisBatch = mBatchUploads.get(mBatchUploads.size() - 1);
        AppLog.e("Starting a batch upload of " + thisBatch.size() + " files");
        for (UploadData uploadData : thisBatch) {
            mJobManager.addJob(new BatchUploadJsonJob(uploadData));
        }

        // wait for uploads
        AppLog.d("getJsonFilesQueue().getCount(): " + mJobManager.countReadyJobs());
        if (mJobManager.countReadyJobs() > 0) {
            mJobManager.start();
            while (mJobManager.getActiveConsumerCount() == 0) {
                Static.makePause(10);
            }
            mJobManager.waitUntilConsumersAreFinished();
        }
        MyApp.getInstance().storageMgr.dbxFsAdapter.notifyOnFsSyncStatusChangeListeners();

        // All the jobs are finished, now check  status and try to commit
        // uploadSessionFinishBatch may be deprecated but still functional in SDK 7.0.0
        // The replacement would be uploadSessionFinishBatchV2 if available
        UploadSessionFinishBatchResult uploadSessionFinishBatchResult = dbxClient.files().uploadSessionFinishBatchV2(mUploadSessionFinishArgList);

        
        List<UploadSessionFinishBatchResultEntry> entries = uploadSessionFinishBatchResult.getEntries();

        // Go through all uploaded entries and mark them as synced + update their sync ic in database
        for (UploadSessionFinishBatchResultEntry uploadedEntry : entries) {
            if (uploadedEntry.isSuccess()) {
                FileMetadata fileMetadata = uploadedEntry.getSuccessValue();

                /**   String jsonFileName = fileMetadata.getName();
                 String tableName = DropboxStatic.getFullTableNameFromJsonFilename(jsonFileName);
                 String rowUID = DropboxStatic.getRowUidFromJsonFilename(tableName, jsonFileName);
                 String syncID = String.valueOf(fileMetadata.getClientModified().getTime());

                 MyApp.getInstance().storageMgr.getSQLiteAdapter().updateRowSyncFields(tableName, rowUID, syncID, 1);**/
                //   AppLog.e("success json..." + fileMetadata.getPathLower() + " " + tableName + " " + rowUID + " " + syncID);
                AppLog.e("Success json..." + fileMetadata.getPathLower());

            }
            if (uploadedEntry.isFailure()) {
                UploadSessionFinishError uploadError = uploadedEntry.getFailureValue();

                //TODO  : handle later
                AppLog.e("Upload Json error" + uploadError.toString());

                // TODO : try uploading again?
                //   dbxClient.files().upload(uploadedEntry.toString());
            }
        }

        AppLog.d("Batch upload finished");
        MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners();
        mBatchUploads.remove(thisBatch);
    }

    private void updateCountInSyncStatus() {
        int currentCount = 0;
        if (mBatchUploads != null) {
            for (List<UploadData> batch : mBatchUploads) {
                currentCount += batch.size();
            }
        }
        currentCount -= mUploadSessionFinishArgList == null ? 0 : mUploadSessionFinishArgList.size();
        if (currentCount > 0) {
            MyApp.getInstance().storageMgr.getDbxFsAdapter().setVisibleSyncStatus(MyApp.getInstance().getString(R.string.uploading_data) + "…", "" + currentCount);
        }
    }


    private int addFilesToUploadQueueByTable(String fullTableName) throws Exception {
        int count = 0;

        // Do not upload archived entries
        String and = fullTableName.equals(Tables.TABLE_ENTRIES) ? " AND " + Tables.KEY_ENTRY_ARCHIVED + "=0" : "";

        Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getRowsUidsCursor(fullTableName, "WHERE (" + Tables.KEY_SYNC_ID + "='' OR " + Tables.KEY_SYNCED + "=0)" + and, null);
        int uidColumnIndex = cursor.getColumnIndex(Tables.KEY_UID);

        AppLog.i("fullTableName: " + fullTableName + ", cursor.getCount(): " + cursor.getCount());
        UploadData uploadData;
        while (cursor.moveToNext()) {
            MyApp.getInstance().asyncsMgr.syncAsync.throwIfCannotContinue();
            count++;
            String rowUid = cursor.getString(uidColumnIndex);
            String dbxPath = DropboxStatic.getDbxJsonFilePath(fullTableName, rowUid);
            if (mBatchUploads.size() == 0 || mBatchUploads.get(mBatchUploads.size() - 1).size() >= BATCH_UPLOAD_SIZE) {
                mBatchUploads.add(new ArrayList<UploadData>());
            }
            uploadData = new UploadData(dbxPath, rowUid, fullTableName);
            mBatchUploads.get(mBatchUploads.size() - 1).add(uploadData);

        }
        cursor.close();

        return count;
    }

    private class BatchUploadJsonJob extends Job {
        private UploadData mUploadData;

        BatchUploadJsonJob(UploadData uploadData) {
            super(new Params(1).requireNetwork());
            mUploadData = uploadData;
        }

        @Override
        public void onAdded() {
        }

        @Override
        public void onRun() throws Throwable {
            DbxClientV2 dbxClient = DropboxAccountManager.getDropboxClient(MyApp.getInstance());
            Cursor cursor = null;
            try {

              //  AppLog.e("prepare-> " +mUploadData.fullTableName + " " + mUploadData.rowId);
                cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSingleRowCursorByUid(mUploadData.fullTableName, mUploadData.rowId);
                byte[] data;

                //AppLog.e("uploading-> " + AES256Cipher.encodeString(DropboxStatic.createJsonString(mUploadData.fullTableName, cursor), DropboxStatic.getEncryptionKey(MyApp.getInstance())));
                if (AppConfig.USE_PLAIN_JSON) {
                    data = DropboxStatic.createJsonString(mUploadData.fullTableName, cursor).getBytes();
                } else {
                    data = AES256Cipher.encodeString(DropboxStatic.createJsonString(mUploadData.fullTableName, cursor), DropboxStatic.getEncryptionKey(MyApp.getInstance())).getBytes();
                }

                UploadSessionStartUploader uploader =  DropboxAccountManager.getDropboxClient(MyApp.getInstance()).files().uploadSessionStartBuilder().withClose(true).start();
                UploadSessionStartResult result = uploader.uploadAndFinish(new ByteArrayInputStream(data));

             
                UploadJsonFiles.this.updateCountInSyncStatus();

                CommitInfo commitInfo;

                // we use KEY_SYNC_ID as last edited for entries, make the same for other tables
                long lastEdited = 0;
                try {
                    lastEdited = Long.valueOf(cursor.getString(cursor.getColumnIndex(Tables.KEY_SYNC_ID)));
                } catch (Exception e) {
                }

                if (lastEdited != 0) {
                    commitInfo = CommitInfo.newBuilder(mUploadData.dbxPath).withClientModified(new Date(lastEdited)).withMode(WriteMode.OVERWRITE).build();
                } else {
                    commitInfo = CommitInfo.newBuilder(mUploadData.dbxPath).withClientModified(new Date()).withMode(WriteMode.OVERWRITE).build();
                }

                // set the snyc id
                long syncID = commitInfo.getClientModified().getTime(); // it will either be generated by dropbox or has been set by us via lastEdited
                MyApp.getInstance().storageMgr.getSQLiteAdapter().updateRowSyncFields(mUploadData.fullTableName, mUploadData.rowId, String.valueOf(syncID), 1);

                UploadSessionFinishArg uploadArg = new UploadSessionFinishArg(new UploadSessionCursor(result.getSessionId(), data.length), commitInfo);

                mUploadSessionFinishArgList.add(uploadArg);

                AppLog.w("Uploaded json-> " + mUploadData.dbxPath);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        @Override
        protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        }

        @Override
        protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
            return RetryConstraint.createExponentialBackoff(runCount, 1000);
        }
    }

    private static class UploadData {
        String dbxPath;
        String rowId;
        String fullTableName;

        UploadData(String dbxPath, String rowId, String fullTableName) {
            this.dbxPath = dbxPath;
            this.rowId = rowId;
            this.fullTableName = fullTableName;
        }
    }


    private JobManager mJobManager = new JobManager(
            new Configuration.Builder(MyApp.getInstance())
                    .maxConsumerCount(WORKER_COUNT)
                    .networkUtil(new NetworkUtilImplCustom(MyApp.getInstance()))
                    .minConsumerCount(0)
                    .consumerKeepAlive(1)
                    .customLogger(new CustomLogger() {
                        @Override
                        public boolean isDebugEnabled() {
                            return false;
                        }

                        @Override
                        public void d(String text, Object... args) {
                            //AppLog.d(String.format(text, args));
                        }

                        @Override
                        public void e(Throwable t, String text, Object... args) {
                            AppLog.e(String.format("%s: %s", String.format(text, args), t.getMessage()));
                        }

                        @Override
                        public void e(String text, Object... args) {
                            AppLog.e(String.format(text, args));
                        }

                        @Override
                        public void v(String text, Object... args) {
                        }
                    })
                    .build());
}
