package com.pixelcrater.Diaro.settings;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.Toast;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;
import com.pixelcrater.Diaro.utils.storage.PermanentStorageUtils;
import com.pixelcrater.Diaro.utils.storage.StorageUtils;

import java.io.File;

public class ChangePermanentStorageAsync extends AsyncTask<Object, String, Boolean> {

    private final String mNewStoragePath;
    private final String mNewStorageTreeUriString;
    public String message;
    protected String error = "";
    private Context mContext;
    private ProgressDialog pleaseWaitDialog;

    public ChangePermanentStorageAsync(Context context, String newStoragePath,
                                       String newStorageTreeUriString) {
        AppLog.e("newStoragePath: " + newStoragePath + ", newStorageTreeUriString: " + newStorageTreeUriString);

        mContext = context;
        mNewStoragePath = newStoragePath;
        mNewStorageTreeUriString = newStorageTreeUriString;

        message = MyApp.getInstance().getString(R.string.please_wait);
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

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {

            File diaroBackupDir = new File(PermanentStorageUtils.getDiaroBackupDirPath());
            File newDiaroBackupDir = new File(AppLifetimeStorageUtils.getAppFilesDirPathByStorage(mNewStoragePath) +  "/Diaro/backup" ) ;

            AppLog.e("diaroBackupDir.getPath(): " + diaroBackupDir.getPath());
            AppLog.e("newDiaroBackupDir.getPath(): " + newDiaroBackupDir.getPath());

            try {
                AppLog.d("diaroBackupDir.exists(): " + diaroBackupDir.exists());
                AppLog.d("newDiaroBackupDir.exists(): " + newDiaroBackupDir.exists());

                // Check for available space
                long neededSpace = StorageUtils.getUsedSizeInBytes(diaroBackupDir);
                long availableSpace = StorageUtils.getAvailableSpaceInBytes(new File(mNewStoragePath));
                AppLog.d("neededSpace: " + neededSpace + ", availableSpace: " + availableSpace);

                if (neededSpace > availableSpace) {
                    throw new Exception(MyApp.getInstance().getString(R.string.not_enough_space));
                }

                newDiaroBackupDir.mkdirs();
                if (diaroBackupDir.exists()) {
                    // Copy directory contents
                    Static.copyFileOrDirectory(diaroBackupDir, newDiaroBackupDir);

                    // Delete old '/Diaro' directory
                    StorageUtils.deleteFileOrDirectory(diaroBackupDir);
                }

                PermanentStorageUtils.updatePermanentStoragePref(mNewStoragePath, mNewStorageTreeUriString);
            } catch (Exception e) {
                AppLog.e("Exception: " + e.getMessage());
                e.getStackTrace();
            }

        } else  {
            // '/Diaro/backup'
            File diaroBackupDir = new File(PermanentStorageUtils.getDiaroBackupDirPath());
            AppLog.d("diaroBackupDir.getPath(): " + diaroBackupDir.getPath());

            File newDiaroBackupDir = new File(PermanentStorageUtils.getDiaroBackupDirPathByStorage(mNewStoragePath));
            AppLog.d("newDiaroBackupDir.getPath(): " + newDiaroBackupDir.getPath());

            try {
                // Create '/Diaro/backup' directory in new storage
                //  boolean newDirCreated = PermanentStorageUtils.createDiaroBackupDirectoryByStorage(mNewStoragePath, mNewStorageTreeUriString);
                //   AppLog.d("newDirCreated: " + newDirCreated);

                // Check for available space
                long neededSpace = StorageUtils.getUsedSizeInBytes(diaroBackupDir);
                long availableSpace = StorageUtils.getAvailableSpaceInBytes(new File(mNewStoragePath));
                AppLog.d("neededSpace: " + neededSpace + ", availableSpace: " + availableSpace);

                if (neededSpace > availableSpace) {
                    throw new Exception(MyApp.getInstance().getString(R.string.not_enough_space));
                }

                AppLog.d("diaroBackupDir.exists(): " + diaroBackupDir.exists());
                AppLog.d("newDiaroBackupDir.exists(): " + newDiaroBackupDir.exists());
                if (diaroBackupDir.exists()) {
                    // Copy directory contents
                    PermanentStorageUtils.copyDiaroBackupDirectory(mNewStoragePath, mNewStorageTreeUriString);

                    // Delete old '/Diaro' directory
                    PermanentStorageUtils.deleteDiaroDir();
                }

                // Update preference
                PermanentStorageUtils.updatePermanentStoragePref(mNewStoragePath, mNewStorageTreeUriString);
            } catch (Exception e) {
                AppLog.e("Exception: " + e);

                // Delete new '/Diaro' directory
                PermanentStorageUtils.deleteDiaroDirByStorage(mNewStoragePath, mNewStorageTreeUriString);

                error = e.getMessage();
                if (error == null) {
                    error = e.toString();
                }

                return false;
            }
        }

        return true;
    }

    private void onCancel() {
        dismissPleaseWaitDialog();

        // Send broadcast to update storage path in settings -> Preferences
        Static.sendBroadcast(Static.BR_IN_SETTINGS_DATA, Static.DO_UPDATE_UI, null);
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
            // Send broadcast to update storage path in settings -> Data
            Static.sendBroadcast(Static.BR_IN_SETTINGS_DATA, Static.DO_UPDATE_UI, null);

            Static.showToast(MyApp.getInstance().getString(R.string.storage_changed), Toast.LENGTH_SHORT);
        } else {
            // Show error toast
            Static.showToastError(String.format("%s: %s", MyApp.getInstance().getString(R.string.error), error));
        }
    }
}