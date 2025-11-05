package com.pixelcrater.Diaro.backuprestore;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.autobackup.BackupRestore;
import com.pixelcrater.Diaro.storage.dropbox.DropboxAccountManager;
import com.pixelcrater.Diaro.utils.AppLog;

import java.io.File;
import java.io.IOException;

public class CreateBackupAsync extends AsyncTask<Object, String, Boolean> {

    public String message;
    protected String error = "";
    private Context mContext;
    private int mTabId;
    private boolean mEncrypt;
    private boolean mSkipAttachments;
    private ProgressDialog pleaseWaitDialog;
    private File backupZipFile;

    public CreateBackupAsync(Context context, int tabId, boolean encrypt, boolean skipAttachments) {
//		AppLog.d("tabId: " + tabId + ", encrypt: " + encrypt + ", skipAttachments: " + skipAttachments);

        mContext = context;
        mTabId = tabId;
        mEncrypt = encrypt;
        mSkipAttachments = skipAttachments;

        message = MyApp.getInstance().getString(R.string.settings_creating_backup_with_ellipsis);
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
            pleaseWaitDialog.setMessage(message);
            pleaseWaitDialog.setCancelable(false);
            pleaseWaitDialog.setButton(ProgressDialog.BUTTON_NEUTRAL, MyApp.getInstance().getString(android.R.string.cancel), (dialog, which) -> cancel(true));
            pleaseWaitDialog.show();
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
            cancel(true);
        }
    }

    public void dismissPleaseWaitDialog() {
        try {
            pleaseWaitDialog.dismiss();
        } catch (Exception e) {
        }
    }

    protected Boolean doInBackground(Object... params) {
        try {
            backupZipFile = BackupRestore.createBackup(null, mEncrypt, mSkipAttachments);
        } catch (IOException e) {
            AppLog.d("IOException: " + e);

            error = e.getMessage();
            if (error == null) {
                error = e.toString();
            }
            return false;
        } catch (Exception e) {
            AppLog.e("Exception: " + e);

            error = e.getMessage();
            if (error == null) {
                error = e.toString();
            }
            return false;
        }

        return true;
    }

    private void onCancel() {
        dismissPleaseWaitDialog();

        Static.sendBroadcast(Static.BR_IN_BACKUP_RESTORE, Static.DO_REFRESH_BACKUP_FILES_LIST, null);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        onCancel();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        dismissPleaseWaitDialog();

        if (result) {
            // Send broadcast to refresh backup files list
            Static.sendBroadcast(Static.BR_IN_BACKUP_RESTORE, Static.DO_REFRESH_BACKUP_FILES_LIST, null);

            // Show success message toast
            Static.showToastSuccess(MyApp.getInstance().getString(R.string.settings_file_created).replace("%s", backupZipFile.getName()));

            // Upload backup file to Dropbox
            if (mTabId == BackupRestoreActivity.TAB_DROPBOX && DropboxAccountManager.isLoggedIn(mContext)) {
                // Upload backup file to Dropbox
                MyApp.getInstance().asyncsMgr.executeUploadBackupFileToDropboxAsync(mContext, backupZipFile.getPath());
            }
        } else {
            // Show error toast
            Static.showToastError(String.format("%s: %s", MyApp.getInstance().getString(R.string.settings_error_file_not_created), error));
        }
    }
}
