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
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.DbxPathV2;
import com.dropbox.core.v2.files.CommitInfo;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.UploadSessionCursor;
import com.dropbox.core.v2.files.UploadSessionFinishArg;
import com.dropbox.core.v2.files.UploadSessionFinishBatchJobStatus;
import com.dropbox.core.v2.files.UploadSessionFinishBatchLaunch;
import com.dropbox.core.v2.files.UploadSessionFinishBatchResult;
import com.dropbox.core.v2.files.UploadSessionFinishBatchResultEntry;
import com.dropbox.core.v2.files.UploadSessionFinishError;
import com.dropbox.core.v2.files.UploadSessionStartResult;
import com.dropbox.core.v2.files.UploadSessionStartUploader;
import com.dropbox.core.v2.files.WriteMode;
import com.pixelcrater.Diaro.config.GlobalConstants;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import static com.pixelcrater.Diaro.config.GlobalConstants.BATCH_UPLOAD_SIZE;
import static com.pixelcrater.Diaro.config.GlobalConstants.DROPBOX_PATH_MEDIA;

class UploadAttachments {

    private final int WORKER_COUNT = 7;
    private List<List<UploadData>> mBatchUploads = new ArrayList<>();
    private List<UploadSessionFinishArg> mUploadSessionFinishArgList;

    private ConcurrentHashMap<String, String> uploadFilesSet = new ConcurrentHashMap<>();

    UploadAttachments() throws Exception {
        mJobManager.stop();
        addFilesToUploadQueue();
        while (mBatchUploads.size() > 0) {
            batchUpload();
        }
        mJobManager.stop();
        mJobManager.destroy();
    }

    private void batchUpload() throws Exception {
        DbxClientV2 dbxClient = DropboxAccountManager.getDropboxClient(MyApp.getInstance());
        mJobManager.stop();
        mUploadSessionFinishArgList = new ArrayList<>();
        uploadFilesSet = new ConcurrentHashMap<>();

        List<UploadData> thisBatch = mBatchUploads.get(mBatchUploads.size() - 1);
        AppLog.d(String.format(Locale.US, "Starting a batch upload of %d files", thisBatch.size()));
        for (UploadData uploadData : thisBatch) {
            mJobManager.addJob(new BatchUploadAttachmentJob(uploadData));

            uploadFilesSet.put(uploadData.dbxPath.toLowerCase(), uploadData.rowId);
        }
        waitForUploads();
        AppLog.d(String.format(Locale.US, "Commit batch upload of %d files", mUploadSessionFinishArgList.size()));

        UploadSessionFinishBatchLaunch result = dbxClient.files().uploadSessionFinishBatch(mUploadSessionFinishArgList);

        while (!dbxClient.files().uploadSessionFinishBatchCheck(result.getAsyncJobIdValue()).isComplete()) {
            Static.makePause(100);
        }

        UploadSessionFinishBatchJobStatus status = dbxClient.files().uploadSessionFinishBatchCheck(result.getAsyncJobIdValue());

        UploadSessionFinishBatchResult uploadResult = status.getCompleteValue();
        List<UploadSessionFinishBatchResultEntry> entries = uploadResult.getEntries();

        AppLog.e("UploadSessionFinishBatchJobStatus " + status.toString() + " " + entries.size());

        for (UploadSessionFinishBatchResultEntry uploadedEntry : entries) {

            AppLog.d("uploadedEntry " + uploadedEntry.toString());
            if (uploadedEntry.isSuccess()) {
                FileMetadata fileMetadata = uploadedEntry.getSuccessValue();

                String fileName = fileMetadata.getName();
                String syncID = String.valueOf(fileMetadata.getClientModified().getTime());

                String rowID = uploadFilesSet.get(fileMetadata.getPathLower());

                // Update attachment file_sync_id and file_synced
                MyApp.getInstance().storageMgr.getSQLiteAdapter().updateAttachmentRowFileSyncFields(rowID, syncID, 1);

              //  AppLog.e("success attachment ..." + fileMetadata.getPathLower() + " " + rowID + " " + String.valueOf(fileMetadata.getClientModified().getTime()));
            }

            if (uploadedEntry.isFailure()) {
                UploadSessionFinishError uploadError = uploadedEntry.getFailureValue();
                //TODO  : handle later
                AppLog.e("Upload Json error" + uploadError.toString());
            }
        }

        MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners();
        AppLog.d("Batch upload completed");
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
            MyApp.getInstance().storageMgr.getDbxFsAdapter().setVisibleSyncStatus(MyApp.getInstance().getString(R.string.uploading_photo) + "…", "" + currentCount);
        }
    }

    private void addFilesToUploadQueue() throws Exception {
        addFilesToUploadQueueByType(GlobalConstants.PHOTO);

        // Update sync status
        updateCountInSyncStatus();

    }

    private int addFilesToUploadQueueByType(String attachmentType) throws Exception {
        int count = 0;

        Cursor cursor = null;
        try {
            String andSQL = " AND type = '" + attachmentType + "'";
            cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getAttachmentsCursor( andSQL , null);
            AppLog.d("addFilesToUploadQueueByType cursor.getCount(): " + cursor.getCount());

            List<Metadata> folderList = new ArrayList<>();

            try {
                folderList = DropboxLocalHelper.listFolder(DROPBOX_PATH_MEDIA + "/" + attachmentType + "/");
            } catch (DbxException dbxe) {
                // the folder does not exisit yet
            }

            int attachmentUidColumnIndex = cursor.getColumnIndex(Tables.KEY_UID);
            while (cursor.moveToNext()) {
                MyApp.getInstance().asyncsMgr.syncAsync.throwIfCannotContinue();

                if (StringUtils.isEmpty(cursor.getString(cursor.getColumnIndex(Tables.KEY_ATTACHMENT_FILENAME)))) {
                    continue;
                }

                File localFile = new File(AppLifetimeStorageUtils.getMediaDirPath() + "/" + attachmentType + "/" + cursor.getString(cursor.getColumnIndex(Tables.KEY_ATTACHMENT_FILENAME)));

                String dbxPath = DROPBOX_PATH_MEDIA + "/" + attachmentType + "/" + cursor.getString(cursor.getColumnIndex(Tables.KEY_ATTACHMENT_FILENAME));

                if (fileShouldBeUploaded(dbxPath, cursor, localFile, folderList)) {
                    count++;
                    addSingleFileToUploadQueue(cursor.getString(attachmentUidColumnIndex), localFile, dbxPath);
                }
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return count;
    }

    private boolean fileShouldBeUploaded(String dbxPath, Cursor cursor, File localFile, List<Metadata> folderList) throws Exception {

        boolean result = false;
        long localSize = localFile.length();
        if (localFile.exists() && localSize > 0 ) {
            if (StringUtils.isEmpty(cursor.getString(cursor.getColumnIndex(Tables.KEY_ATTACHMENT_FILE_SYNC_ID))) || cursor.getInt(cursor.getColumnIndex(Tables.KEY_ATTACHMENT_FILE_SYNCED)) == 0) {
                // the file exists locally and is markes as unsync
                result = true;
            } else if (DropboxLocalHelper.exists(folderList, dbxPath)) {
                FileMetadata metadata = (FileMetadata) DropboxLocalHelper.getMetadata(folderList, dbxPath);
                long fsSize = metadata.getSize();
                if (fsSize != localSize && StringUtils.equals(String.valueOf(metadata.getClientModified().getTime()), cursor.getString(cursor.getColumnIndex(Tables.KEY_ATTACHMENT_FILE_SYNC_ID)))) {
                    AppLog.d("fsSize: " + fsSize + ", localSize: " + localSize);
                    result = true;
                }
            }
        }
        return result;
    }

    private void addSingleFileToUploadQueue(String rowUid, File localFile, String dbxPath) {
        AppLog.d("rowUid: " + rowUid + ", localFile.getPath(): " + localFile.getPath() + ", dbxPath: " + dbxPath);

        if (mBatchUploads.size() == 0 || mBatchUploads.get(mBatchUploads.size() - 1).size() >= BATCH_UPLOAD_SIZE) {
            mBatchUploads.add(new ArrayList<UploadData>());
        }
        mBatchUploads.get(mBatchUploads.size() - 1).add(new UploadData(dbxPath, rowUid));

    }

    private void waitForUploads() {
        AppLog.d("getAttachmentFilesQueue().getCount(): " + mJobManager.countReadyJobs());

        if (mJobManager.countReadyJobs() > 0) {
            mJobManager.start();
            while (mJobManager.getActiveConsumerCount() == 0) {
                Static.makePause(10);
            }
            mJobManager.waitUntilConsumersAreFinished();
        }
        MyApp.getInstance().storageMgr.dbxFsAdapter.notifyOnFsSyncStatusChangeListeners();
    }

    private static class UploadData {
        String dbxPath;
        String rowId;

        UploadData(String dbxPath, String rowId) {
            this.dbxPath = dbxPath;
            this.rowId = rowId;
        }
    }

    private class BatchUploadAttachmentJob extends Job {

        private UploadData mUploadData;

        BatchUploadAttachmentJob(UploadData uploadData) {
            super(new Params(100).requireNetwork());
            mUploadData = uploadData;
        }

        @Override
        public void onAdded() {
        }

        @Override
        public void onRun() throws Throwable {
            String parent = DbxPathV2.getName(DbxPathV2.getParent(mUploadData.dbxPath));
            byte[] data = FileUtils.readFileToByteArray(new File(String.format("%s/%s/%s", AppLifetimeStorageUtils.getMediaDirPath(), parent, DbxPathV2.getName(mUploadData.dbxPath))));
            AppLog.d("Start upload attachment " + mUploadData.dbxPath);

            UploadSessionStartUploader uploader =  DropboxAccountManager.getDropboxClient(MyApp.getInstance()).files().uploadSessionStartBuilder().withClose(true).start();
            UploadSessionStartResult result = uploader.uploadAndFinish(new ByteArrayInputStream(data));
           // UploadSessionStartResult result = DropboxAccountManager.getDropboxClient(MyApp.getInstance()).files().uploadSessionStart().uploadAndFinish(new ByteArrayInputStream(data));
            UploadAttachments.this.updateCountInSyncStatus();

            mUploadSessionFinishArgList.add(new UploadSessionFinishArg(new UploadSessionCursor(result.getSessionId(), data.length), CommitInfo.newBuilder(mUploadData.dbxPath).withMode(WriteMode.OVERWRITE).build()));

            AppLog.w("Uploaded photo " + mUploadData.dbxPath);
        }

        @Override
        protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
            // MyApp.getInstance().storageMgr.getSQLiteAdapter().updateAttachmentRowFileSyncFields(mUploadData.rowId, String.valueOf(0), 0);
        }

        @Override
        protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
            return RetryConstraint.createExponentialBackoff(runCount, 1000);
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
