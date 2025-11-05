package com.pixelcrater.Diaro.backuprestore;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.v2.DbxPathV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.storage.dropbox.DropboxAccountManager;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;
import com.pixelcrater.Diaro.utils.storage.PermanentStorageUtils;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class DownloadBackupFileFromDropboxAsync extends AsyncTask<Object, String, Boolean> {

    public String message;
    public long bytesTotal;
    public long bytesTransferred;
    public long startMillis = new DateTime().getMillis();
    private Context mContext;
    private String mDropboxFilePath;
    private boolean mRestore;
    private boolean mDeleteOldData;
    private String localFilePath;
    private ProgressDialog pleaseWaitDialog;
    private String error;
    private boolean isFinished;

    public DownloadBackupFileFromDropboxAsync(Context context, String dropboxFilePath, boolean restore, boolean deleteOldData) {

        AppLog.d("dropboxFilePath: " + dropboxFilePath);
        mContext = context;
        mDropboxFilePath = dropboxFilePath;
        mRestore = restore;
        mDeleteOldData = deleteOldData;

        String filename = DbxPathV2.getName(dropboxFilePath);

        // Local file path
        if (restore || PermanentStorageUtils.shouldUseSaf()) {
            localFilePath = AppLifetimeStorageUtils.getCacheDirPath() + "/" + filename;
        } else {
            localFilePath = PermanentStorageUtils.getDiaroBackupDirPath() + "/" + filename;
        }

        message = MyApp.getInstance().getString(R.string.settings_downloading_file_with_ellipsis).replace("%s", filename);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        showPleaseWaitDialog(mContext);
    }

    @SuppressLint("NewApi")
    public void showPleaseWaitDialog(Context context) {
        dismissPleaseWaitDialog();

        try {
            pleaseWaitDialog = new ProgressDialog(context);
            pleaseWaitDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pleaseWaitDialog.setProgressNumberFormat("%1d/%2d KB");
            pleaseWaitDialog.setMessage(message);
            pleaseWaitDialog.setCancelable(false);
            pleaseWaitDialog.setButton(ProgressDialog.BUTTON_NEUTRAL, MyApp.getInstance().getString(android.R.string.cancel), (dialog, which) -> cancel(true));
            pleaseWaitDialog.show();
        } catch (Exception e) {
            cancel(true);
        }
    }

    public void dismissPleaseWaitDialog() {
        try {
            pleaseWaitDialog.dismiss();
        } catch (Exception e) {
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);

        bytesTotal = Long.valueOf(values[0]);
        bytesTransferred = Long.valueOf(values[1]);

        // Show progress
        pleaseWaitDialog.setMax((int) Static.byteToKB(bytesTotal));
        pleaseWaitDialog.setProgress((int) Static.byteToKB(bytesTransferred));

        MyApp.getInstance().notificationsMgr.backupNotification.showHideDownloadBackupNotification();
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        boolean result = true;

        try {
            File localFile = new File(localFilePath);

            // Create local directory
            //noinspection ResultOfMethodCallIgnored
            localFile.getParentFile().mkdirs();

            DbxDownloader<FileMetadata> downloader = DropboxAccountManager.getDropboxClient(mContext).files().download(mDropboxFilePath);
            long size = downloader.getResult().getSize();

            FileOutputStream fout = new FileOutputStream(localFile);
            downloader.download(new ProgressOutputStream(fout, (numBytes, totalBytes) -> {
                // Publish progress
                String[] values = new String[2];
                values[0] = String.valueOf(totalBytes);
                values[1] = String.valueOf(numBytes);
                publishProgress(values);

            }, size));

            downloader.close();

            if (PermanentStorageUtils.shouldUseSaf()) {
                // Copy backup file from cache to '/Diaro/backup' directory
                Uri uri = Uri.fromFile(localFile);
                PermanentStorageUtils.copyBackupFile(uri, PermanentStorageUtils.getPermanentStoragePref(), PermanentStorageUtils.getPermanentStorageTreeUriPref());
            }
        } catch (Exception e) {
            AppLog.w(String.format("Error downloading backup file from Dropbox: %s", e.getMessage()));
            result = false;
        } finally {
            if (!result) {
                Static.sendBroadcast(Static.BR_IN_BACKUP_RESTORE, Static.DO_ACTIONS_ON_DOWNLOAD_CANCELED, null);
            }
        }

        return result;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        setFinished();
    }

    public void setFinished() {
        isFinished = true;
        dismissPleaseWaitDialog();
        MyApp.getInstance().notificationsMgr.backupNotification.showHideDownloadBackupNotification();
        MyApp.getInstance().checkAppState();
    }

    public boolean isFinished() {
        return isFinished;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            String firstParam = mRestore ? Static.PARAM_RESTORE : "";
            String secondParam = mDeleteOldData ? Static.PARAM_DELETE_OLD_DATA : "";

            if (mRestore) {
                // Send broadcast about download complete to ActivityBackupRestore
                ArrayList<String> params = new ArrayList<>();
                params.add(firstParam);
                params.add(secondParam);
                params.add(localFilePath);
                Static.sendBroadcast(Static.BR_IN_BACKUP_RESTORE, Static.DO_ACTIONS_ON_DOWNLOAD_COMPLETE, params);

            } else {
                Static.showToast(MyApp.getInstance().getString(R.string.settings_download_complete), Toast.LENGTH_SHORT);

            }
        } else if (MyApp.getInstance().isAppVisible()) {
            // Show error
            Static.showToast(String.format("%s: %s", MyApp.getInstance().getString(R.string.error), error), Toast.LENGTH_SHORT);
        }

        setFinished();
    }
}
