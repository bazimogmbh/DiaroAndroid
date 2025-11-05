package com.pixelcrater.Diaro.storage.dropbox.jobs;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.callback.JobManagerCallback;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.log.CustomLogger;
import com.dropbox.core.v2.files.FileMetadata;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.storage.dropbox.DropboxStatic;
import com.pixelcrater.Diaro.storage.dropbox.NetworkUtilImplCustom;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.Static;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DownloadJsonFiles {

    private final int WORKER_COUNT = 15;

    public DownloadJsonFiles(List<FileMetadata> foldersListToDownload, List<FileMetadata> tagsListToDownload, List<FileMetadata> moodsListToDownload,  List<FileMetadata> locationsListToDownload, List<FileMetadata> attachmentsListToDownload, List<FileMetadata> entriesListToDownload, List<FileMetadata> templatesListToDownload) {

        mJobManager.stop();

        mJobManager.addCallback(new JobManagerCallback() {
            @Override
            public void onJobAdded(@NonNull Job job) {
            }

            @Override
            public void onJobRun(@NonNull Job job, int resultCode) {
            }

            @Override
            public void onJobCancelled(@NonNull Job job, boolean byCancelRequest, @Nullable Throwable throwable) {
            }

            @Override
            public void onDone(@NonNull Job job) {
                int currentCount = mJobManager.count();
                if (currentCount > 0) {
                    MyApp.getInstance().storageMgr.getDbxFsAdapter().setVisibleSyncStatus(MyApp.getInstance().getString(R.string.downloading_data) + "…", "" + currentCount);
                }
            }

            @Override
            public void onAfterJobRun(@NonNull Job job, int resultCode) {
            }
        });

        long start = System.currentTimeMillis();

        addJobs(foldersListToDownload, Tables.TABLE_FOLDERS);
        addJobs(tagsListToDownload, Tables.TABLE_TAGS);
        addJobs(moodsListToDownload, Tables.TABLE_MOODS);
        addJobs(locationsListToDownload, Tables.TABLE_LOCATIONS);
        addJobs(attachmentsListToDownload, Tables.TABLE_ATTACHMENTS);
        addJobs(entriesListToDownload, Tables.TABLE_ENTRIES);

        addJobs(templatesListToDownload, Tables.TABLE_TEMPLATES);

        long ms = System.currentTimeMillis() - start;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(ms);

        AppLog.e("Adding jobs took-> " + ms + " ms or " + seconds + " secs");

        start = System.currentTimeMillis();

        int totalCount = mJobManager.countReadyJobs();
        AppLog.e(String.format(Locale.getDefault(), "Total Json to be downloaded = %d", totalCount));

        if (mJobManager.countReadyJobs() > 0) {
            mJobManager.start();

            while (mJobManager.getActiveConsumerCount() == 0) {
                Static.makePause(100);
            }
            mJobManager.waitUntilConsumersAreFinished();
            MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners();
        }

        mJobManager.stop();
        mJobManager.destroy();

        ms = System.currentTimeMillis() - start;
        seconds = TimeUnit.MILLISECONDS.toSeconds(ms);

        if (totalCount > 0)
            AppLog.e("Finished Json downloadeds in " + ms + " ms or " + seconds + " secs");
    }

    private void addJobs(List<FileMetadata> metadataList, String table) {

        if (metadataList.size() == 0)
            return;

        HashMap<String, FileMetadata> metadataDownloadablesMap = new HashMap<>();

        ArrayList<String> uidsList = new ArrayList<>();
        for (FileMetadata fileMetadata : metadataList) {
            String uid = DropboxStatic.getRowUidFromJsonFilename(table, fileMetadata.getName());
            uidsList.add(uid);
            metadataDownloadablesMap.put(uid, fileMetadata);
        }

        String uids = uidsList.toString().replace("[", "'").replace("]", "'").replace(", ", "','");

        int initialCount = metadataDownloadablesMap.size();
        AppLog.e(table + " metadataHashMap has -> " + initialCount + "  here are the uids-> " + uids);
        Cursor cursor = null;
        try {
            cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSyncIdsForTable(table, uids);

            AppLog.e("we got query cursor of -> " + cursor.getCount());

            while (cursor.moveToNext()) {
                String uid = cursor.getString(0);
                long currentTimestamp = Long.valueOf(cursor.getString(1));
                int isSynced = cursor.getInt(2);

                FileMetadata fileMetadata = metadataDownloadablesMap.get(uid);
                long remoteTimestamp = fileMetadata.getClientModified().getTime();

                //    AppLog.e(remoteTimestamp + " " + currentTimestamp);

                if (remoteTimestamp > currentTimestamp) {
                    // file should be downloaded
                    AppLog.e("-> Bigger remoteTimestamp: " + remoteTimestamp + ", currentTimestamp: " + currentTimestamp + ", diff:" + (remoteTimestamp - currentTimestamp));
                    // dont remove it from download list
                } else {

                    if (remoteTimestamp == currentTimestamp) {
                        // file should be downloaded
                        AppLog.e("Same remoteTimestamp: " + remoteTimestamp + ", currentTimestamp: " + currentTimestamp + ", diff:" + (remoteTimestamp - currentTimestamp));
                        // dont remove it from download list

                        if (isSynced == 0) {
                            MyApp.getInstance().storageMgr.getSQLiteAdapter().updateRowSyncFields(table, uid, String.valueOf(remoteTimestamp), 1);
                        }

                    }

                    if (remoteTimestamp < currentTimestamp) {
                        AppLog.e("Smaller remoteTimestamp: " + remoteTimestamp + ", currentTimestamp: " + currentTimestamp + ", diff:" + (remoteTimestamp - currentTimestamp));
                    }

                    metadataDownloadablesMap.remove(uid);
                }

                /**   if(!dbxFileShouldBeDownloaded(table,fileMetadata.getName(), remoteTimestamp)) {
                 metadataDownloadablesMap.remove(uid);
                 } **/


            }

        } catch (Exception e) {
            Static.printCursor(cursor);
            AppLog.e("Got exception for cursor " + cursor.toString() + " msg->" + e.getMessage() + " ->" + e.getStackTrace().toString());

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        AppLog.e(table + " metadataHashMap now has -> " + metadataDownloadablesMap.size() + " removed item count-> " + (initialCount - metadataDownloadablesMap.size()));

        for (Map.Entry<String, FileMetadata> stringFileMetadataEntry : metadataDownloadablesMap.entrySet()) {
            FileMetadata fileMetadata = (FileMetadata) ((Map.Entry) stringFileMetadataEntry).getValue();
           // AppLog.e("...should download" + fileMetadata.getPathLower());
            mJobManager.addJobInBackground(new DownloadJsonJob(fileMetadata.getPathLower(), table));
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


    private boolean dbxFileShouldBeDownloaded(String table, String filename, long remoteTimestamp) {
        //        AppLog.d("fullTableName: " + fullTableName + ", dbxFileInfo: " + dbxFileInfo);

        boolean isValidFile = DropboxStatic.isJsonFilenameCorrect(table, filename);
        if (!isValidFile)
            return false;

        boolean fileShouldbeDownloaded = false;

        String uid = DropboxStatic.getRowUidFromJsonFilename(table, filename);

        Cursor cursor = null;
        try {
            cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSingleRowCursorByUid(table, uid);
            if (cursor.getCount() == 0) {
                fileShouldbeDownloaded = true;
            }

            // If sync_id is not empty
            else if (StringUtils.isNotEmpty(cursor.getString(cursor.getColumnIndex(Tables.KEY_SYNC_ID)))) {

                long currentTimestamp = Long.valueOf(cursor.getString(cursor.getColumnIndex(Tables.KEY_SYNC_ID)));

                if (remoteTimestamp > currentTimestamp) {
                    fileShouldbeDownloaded = true;
                    // AppLog.e("-> Bigger remoteTimestamp: " + remoteTimestamp + ", currentTimestamp: " + currentTimestamp + ", diff:" + (remoteTimestamp - currentTimestamp));
                }

                // If synced field is 0
                else if (cursor.getInt(cursor.getColumnIndex(Tables.KEY_SYNCED)) == 0) {
                    // Set synced field to 1
                    AppLog.e("->syced was 0: " + ", currentTimestamp: " + currentTimestamp + ", diff:" + (remoteTimestamp - currentTimestamp));
                    MyApp.getInstance().storageMgr.getSQLiteAdapter().updateRowSyncFields(table, cursor.getString(cursor.getColumnIndex(Tables.KEY_UID)), String.valueOf(remoteTimestamp), 1);
                    // MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners();
                } else {
                    // we already have the a equal timestamp or later version, no need to download the file
                    fileShouldbeDownloaded = false;
                    AppLog.e("-> Smaller or same remoteTimestamp: " + remoteTimestamp + ", currentTimestamp: " + currentTimestamp + ", diff:" + (remoteTimestamp - currentTimestamp));
                }
            }
        } catch (Exception e) {
            AppLog.w("Error checking if file should be downloaded: " + e.getMessage() + " ->" + filename);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }


        return fileShouldbeDownloaded;
    }

}