package com.pixelcrater.Diaro.storage.dropbox;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderLongpollResult;
import com.dropbox.core.v2.files.Metadata;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.AppLog;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DbxFsAdapter {

    private List<OnFsSyncStatusChangeListener> onFsSyncStatusChangeListeners;
    private String visibleSyncStatus;
    private String visibleSyncPercents = "";

    private DbxClientV2 mDbxLongpollClient;
    private boolean mRunning = true;

    public DbxFsAdapter() {
        mDbxLongpollClient = DropboxAccountManager.getDropboxLongpollClient(MyApp.getInstance());
        // If Dropbox account not connected
        if (!DropboxAccountManager.isLoggedIn(MyApp.getInstance())) {
            return;
        }

        longpoll();
    }

    public String getVisibleSyncStatusText() {
        return StringUtils.isEmpty(visibleSyncStatus) ? MyApp.getInstance().getString(R.string.syncing_with_ellipsis) : visibleSyncStatus;

    }

    public String getVisibleSyncPercents() {
        return visibleSyncPercents;
    }

    public void setVisibleSyncStatus(String visibleSyncStatus, String visibleSyncPercents) {
        if (!StringUtils.equals(this.visibleSyncStatus, visibleSyncStatus) || !StringUtils.equals(this.visibleSyncPercents, visibleSyncPercents)) {
            this.visibleSyncStatus = visibleSyncStatus;
            this.visibleSyncPercents = visibleSyncPercents;

            // Notify FS sync status listeners
            notifyOnFsSyncStatusChangeListeners();
        }
    }

    private void longpoll() {
        runInBackground(() -> {
            try {
                String latestCursor = "";

                List<Metadata> filesToDelete ;
                List<FileMetadata> filesToDownload;

                while (mRunning) {
                    latestCursor = DropboxLocalHelper.getLatestCursor();

                    AppLog.i("hey we got a new cursor " + latestCursor);

                    if (latestCursor != null) {

                        ListFolderLongpollResult longPollResult = mDbxLongpollClient.files().listFolderLongpoll(latestCursor, TimeUnit.MINUTES.toSeconds(5));
                        if (longPollResult.getChanges()) {
                            AppLog.i("------longpoll changed for" + latestCursor);

                            filesToDelete = new ArrayList<>();
                            filesToDownload = new ArrayList<>();
                            //1) Get the changes from server
                            DropboxAccountManager.deltaCheck(filesToDownload, filesToDelete);

                           long time = System.currentTimeMillis();

                            try {
                                SyncAsync.handleDropboxChanges(filesToDownload, filesToDelete);
                            } catch (Exception e) {
                                AppLog.e("Error occured" + e.toString());
                                // rollback to old cursor
                                DropboxLocalHelper.setLatestCursor(latestCursor);
                                mRunning = false;
                            }

                            AppLog.i("handleDropboxChanges took " + (System.currentTimeMillis() - time));
                            try {
                                Thread.sleep(500);
                            } catch (Exception ignored) {
                            }

                            if (longPollResult.getBackoff() != null) {
                                try {
                                    Thread.sleep(longPollResult.getBackoff() * 1000);
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    } else {

                        filesToDelete = new ArrayList<>();
                        filesToDownload = new ArrayList<>();
                        //1) Get the changes from server
                        DropboxAccountManager.deltaCheck(filesToDownload, filesToDelete);
                    }

                }
            } catch (DbxException e) {
                AppLog.e("Error monitoring Dropbox profile" + e.getMessage());
            }
        });
    }

    private void runInBackground(Runnable runnable) {
        new Thread(runnable).start();
    }

    // *** Listeners ***
    public void notifyOnFsSyncStatusChangeListeners() {
        if (onFsSyncStatusChangeListeners != null) {
            // Listener can be removed during 'for' loop
            for (int i = 0; i < onFsSyncStatusChangeListeners.size(); i++) {
                onFsSyncStatusChangeListeners.get(i).onFsSyncStatusChange();
            }
        }
    }

    public void addOnFsSyncStatusChangeListener(OnFsSyncStatusChangeListener listener) {
        if (onFsSyncStatusChangeListeners == null) {
            onFsSyncStatusChangeListeners = new LinkedList<>();
        }
        onFsSyncStatusChangeListeners.add(listener);
    }

    public void removeOnFsSyncStatusChangeListener(OnFsSyncStatusChangeListener listener) {
        if (onFsSyncStatusChangeListeners != null) {
            onFsSyncStatusChangeListeners.remove(listener);
        }
    }
}
