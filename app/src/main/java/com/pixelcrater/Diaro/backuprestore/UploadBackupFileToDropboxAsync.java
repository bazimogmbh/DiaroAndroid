package com.pixelcrater.Diaro.backuprestore;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import androidx.documentfile.provider.DocumentFile;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.storage.dropbox.DropboxAccountManager;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;
import com.pixelcrater.Diaro.utils.storage.PermanentStorageUtils;

import org.joda.time.DateTime;

import java.io.File;

import static com.pixelcrater.Diaro.config.GlobalConstants.DROPBOX_PATH_BACKUP;

public class UploadBackupFileToDropboxAsync extends AsyncTask<Object, String, Boolean> implements DropboxAccountManager.ProgressCallback {

    private final Uri fileUri;
    public String message;
    public long bytesTotal;
    public long bytesTransferred;
    public long startMillis = new DateTime().getMillis();
    private Context mContext;
    private String mLocalFileUriString;
    private String mDropboxFilePath;
    private ProgressDialog pleaseWaitDialog;
    private String error;
    private boolean isFinished;

    public UploadBackupFileToDropboxAsync(Context context, String localFileUriString) {
        mContext = context;
        mLocalFileUriString = localFileUriString;
        fileUri = Uri.parse(mLocalFileUriString);
        String filename = PermanentStorageUtils.getBackupFilename(fileUri);
        mDropboxFilePath = DROPBOX_PATH_BACKUP + "/" + filename;

        message = MyApp.getInstance().getString(R.string.settings_uploading_file_with_ellipsis).replace("%s", filename);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        showPleaseWaitDialog(mContext);
    }

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

        MyApp.getInstance().notificationsMgr.backupNotification.showHideUploadBackupNotification();
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        boolean result = true;

        try {
            Static.throwIfNotConnectedToInternet();

            File localFile;
            // Test if given Uri is backed by a android.provider.DocumentsProvider.
            if (DocumentFile.isDocumentUri(MyApp.getInstance(), fileUri)) {
                // Delete '/cache/backup' directory
                AppLifetimeStorageUtils.deleteCacheBackupDir();

                // Copy backup file from extsdcard to cache directory
                // in order to have File for dbxBackupFile.writeFromExistingFile() function
                localFile = PermanentStorageUtils.copyBackupFileToCache(mLocalFileUriString, null);
            } else {
                localFile = new File(mLocalFileUriString);
            }

            if (!localFile.isFile()) {
                AppLog.d("return because localFile.isFile(): " + localFile.isFile() + ", mLocalFileUriString: " + mLocalFileUriString);
                throw new Exception(MyApp.getInstance().getString(R.string.file_not_found));
            }
            // Write local file contents to dbxFile

            long size = localFile.length();
            // assert our file is at least the chunk upload size. We make this assumption in the code
            // below to simplify the logic.
            if (size < DropboxAccountManager.CHUNKED_UPLOAD_CHUNK_SIZE) {
                //File too small, use upload
                DropboxAccountManager.uploadFile(localFile, mDropboxFilePath);
            } else {
                DropboxAccountManager.chunkedUploadFile( localFile, mDropboxFilePath, this);
            }

        } catch (Exception e) {
            AppLog.e("Exception: " + e);

            error = e.getMessage();
            if (error == null) {
                error = e.toString();
            }
            result = false;
        } finally {

            if (!result) {
                Static.sendBroadcast(Static.BR_IN_BACKUP_RESTORE, Static.DO_ACTIONS_ON_DOWNLOAD_CANCELED, null);
            }

            // Delete '/cache/backup' directory
            AppLifetimeStorageUtils.deleteCacheBackupDir();
        }

        return result;
    }

    private void printProgress(long uploaded, long size) {
        //  AppLog.e("uploaded" + (100 * (uploaded / (double) size)));
        // Publish progress
        String[] values = new String[2];
        values[0] = String.valueOf(size);
        values[1] = String.valueOf(uploaded);
        publishProgress(values);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        setFinished();
    }

    public void setFinished() {
        isFinished = true;
        dismissPleaseWaitDialog();
        MyApp.getInstance().notificationsMgr.backupNotification.showHideUploadBackupNotification();
        MyApp.getInstance().checkAppState();
    }

    public boolean isFinished() {
        return isFinished;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Static.showToastSuccess(MyApp.getInstance().getString(R.string.settings_upload_complete));
        } else if (MyApp.getInstance().isAppVisible()) {
            Static.showToastError(String.format("%s: %s", MyApp.getInstance().getString(R.string.error), error));
        }

        setFinished();
    }

    @Override
    public void progressUpdate(long uploaded, long size) {
        printProgress(uploaded, size);
    }

}
