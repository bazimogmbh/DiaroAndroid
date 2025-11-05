package com.pixelcrater.Diaro.settings;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.pixelcrater.Diaro.config.GlobalConstants;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;
import com.pixelcrater.Diaro.utils.storage.StorageUtils;

import java.io.File;

public class ChangeAppLifetimeStorageAsync extends AsyncTask<Object, String, Boolean> {

    private final String mOldStoragePrefValue;
    private final String mNewStoragePrefValue;
    public String message;
    protected String error = "";
    private Context mContext;
    private ProgressDialog pleaseWaitDialog;
    private boolean isFinished;

    public ChangeAppLifetimeStorageAsync(Context context, String oldStoragePrefValue,  String newStoragePrefValue) {
        mContext = context;
        mOldStoragePrefValue = oldStoragePrefValue;
        mNewStoragePrefValue = newStoragePrefValue;

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
//            pleaseWaitDialog.setButton(ProgressDialog.BUTTON_NEUTRAL, MyApp.getInstance().getString(R.string.cancel), new OnClickListener() {
//                public void onClick(DialogInterface dialog, int which) {
//                    cancel(true);
//                }
//            });
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
        // /media
        File oldAttachmentsDir = new File(AppLifetimeStorageUtils.getAppFilesDirPathByStorage(mOldStoragePrefValue) + "/" + GlobalConstants.DIR_MEDIA);
        File newAttachmentsDir = new File(AppLifetimeStorageUtils.getAppFilesDirPathByStorage( mNewStoragePrefValue) + "/" + GlobalConstants.DIR_MEDIA);
//        AppLog.d("\noldAttachmentsDir.getPath(): " + oldAttachmentsDir.getPath() + "\nnewAttachmentsDir.getPath(): " + newAttachmentsDir.getPath());
        // /profile
        File oldProfileDir = new File(AppLifetimeStorageUtils.getAppFilesDirPathByStorage( mOldStoragePrefValue) + "/" + GlobalConstants.DIR_PROFILE);
        File newProfileDir = new File(AppLifetimeStorageUtils.getAppFilesDirPathByStorage( mNewStoragePrefValue) + "/" + GlobalConstants.DIR_PROFILE);


        try {
            // Create directories if not exist
            oldAttachmentsDir.mkdirs();
            newAttachmentsDir.mkdirs();
            oldProfileDir.mkdirs();
            newProfileDir.mkdirs();

            // Check for available space
            long neededSpace = StorageUtils.getUsedSizeInBytes(oldAttachmentsDir) +  StorageUtils.getUsedSizeInBytes(oldProfileDir);
            long availableSpace = StorageUtils.getAvailableSpaceInBytes(new File(mNewStoragePrefValue));

            if (neededSpace > availableSpace) {
                throw new Exception(MyApp.getInstance().getString(R.string.not_enough_space));
            }

            if (oldAttachmentsDir.exists()) {
                // Copy media directory
                Static.copyFileOrDirectory(oldAttachmentsDir, newAttachmentsDir);
                // Delete old media directory
                StorageUtils.deleteFileOrDirectory(oldAttachmentsDir);
            }

            if (oldProfileDir.exists()) {
                // Copy profile directory
                Static.copyFileOrDirectory(oldProfileDir, newProfileDir);
                // Delete old profile directory
                StorageUtils.deleteFileOrDirectory(oldProfileDir);
            }

            // Update preference
            MyApp.getInstance().prefs.edit().putString(Prefs.PREF_APP_LIFETIME_STORAGE, mNewStoragePrefValue).apply();

        } catch (Exception e) {
            AppLog.e("Exception: " + e);
           if(!oldAttachmentsDir.equals(newAttachmentsDir)) {
               // Delete new media and profile directory
               StorageUtils.deleteFileOrDirectory(newAttachmentsDir);
               StorageUtils.deleteFileOrDirectory(newProfileDir);
           } else {
               AppLog.e("Exception: " + e.getClass());
           }

            error = e.getMessage();
            if (error == null) {
                error = e.toString();
            }

            return false;
        }

        return true;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        setFinished();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        dismissPleaseWaitDialog();

        setFinished();

        if (result) {
            Static.showToast(MyApp.getInstance().getString(R.string.storage_changed), Toast.LENGTH_LONG);
        } else {
            // Show error toast
            Static.showToastError(String.format("%s: %s", MyApp.getInstance().getString(R.string.error), error));
        }
    }

    public void setFinished() {
        isFinished = true;
//        AppLog.d("Finished");

        dismissPleaseWaitDialog();

        // Send broadcast to update storage path in settings -> Preferences
        Static.sendBroadcast(Static.BR_IN_SETTINGS_DATA, Static.DO_UPDATE_UI, null);
    }

    public boolean isFinished() {
        return isFinished;
    }
}
