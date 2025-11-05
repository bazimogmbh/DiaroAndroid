package com.pixelcrater.Diaro.storage.dropbox.jobs;

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
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.storage.dropbox.NetworkUtilImplCustom;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DownloadPhotos {

    private final int WORKER_COUNT = 7;

    public int photosCount = 0;

    public DownloadPhotos(List<FileMetadata> files)  {

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
                    MyApp.getInstance().storageMgr.getDbxFsAdapter().setVisibleSyncStatus(MyApp.getInstance().getString(R.string.downloading_photo) + "…", "" + currentCount);
                }
            }

            @Override
            public void onAfterJobRun(@NonNull Job job, int resultCode) {
            }
        });

        long start = System.currentTimeMillis();

        for (FileMetadata fileMetadata : files) {

            String dbxPath = fileMetadata.getPathLower();
            String fileName = fileMetadata.getName();

            String localPath = String.format("%s/%s", AppLifetimeStorageUtils.getMediaPhotosDirPath(), fileName);
            File localFile = new File(localPath);

            if (localFile.exists() && localFile.length() == fileMetadata.getSize()) {

             //   AppLog.e(" file exists with same content " + localFile.length() + " , " + fileMetadata.getSize());
                // TODO : update its sync id and exit , if the sync id is not set.
            } else {

                // download the file
                photosCount++;
                mJobManager.addJob(new DownloadPhotosJob(dbxPath, fileName));
            }
        }


        AppLog.e("Total Images to be downloaded: " + photosCount);

        // wait for downloads
        AppLog.e("getAttachmentFilesQueue().getCount(): " + mJobManager.count());
        if (mJobManager.countReadyJobs() > 0) {
            mJobManager.start();
            while (mJobManager.getActiveConsumerCount() == 0) {
                Static.makePause(200);
            }
            mJobManager.waitUntilConsumersAreFinished();
            MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners();
        }

        mJobManager.stop();
        mJobManager.destroy();

        long ms = System.currentTimeMillis() - start;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(ms);

        AppLog.e( "Finished Images downloaded in " + ms + " ms or " + seconds + " secs");
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
