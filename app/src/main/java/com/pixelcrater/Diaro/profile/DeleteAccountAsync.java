package com.pixelcrater.Diaro.profile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.ClearCredentialException;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.entries.attachments.AttachmentsStatic;
import com.pixelcrater.Diaro.storage.sqlite.helpers.SQLiteMgr;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;
import com.pixelcrater.Diaro.utils.storage.StorageUtils;

import java.io.File;
import java.util.concurrent.Executors;

public class DeleteAccountAsync extends AsyncTask<Void, Void, Boolean> {

    private ProfileActivity activity;
    private ProgressDialog progressDialog;
    private String errorMessage = null;

    public DeleteAccountAsync(ProfileActivity activity) {
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        showProgressDialog();
    }

    private void showProgressDialog() {
        try {
            progressDialog = new ProgressDialog(activity);
            progressDialog.setMessage(activity.getString(R.string.deleting_account));
            progressDialog.setCancelable(false);
            progressDialog.show();
        } catch (Exception e) {
            AppLog.e("Error showing progress dialog: " + e);
        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            AppLog.d("Starting account deletion process");

            // 1. Cancel any ongoing sync operations
            MyApp.getInstance().asyncsMgr.cancelSyncAsync();

            // 2. Delete all diary entries and related data from database
            MyApp.getInstance().storageMgr.clearAllData();

            // 3. Delete all attachment files (photos, media)
            AttachmentsStatic.deleteAllAttachmentsFiles();
            StorageUtils.deleteFileOrDirectory(new File(AppLifetimeStorageUtils.getMediaDirPath()));

            // 4. Delete profile photo
            File profilePhotoFile = new File(AppLifetimeStorageUtils.getProfilePhotoFilePath());
            if (profilePhotoFile.exists()) {
                profilePhotoFile.delete();
            }

            // 5. Delete cache directories
            AppLifetimeStorageUtils.deleteCacheDirectory();

            // 6. Clear user-specific preferences (keep app settings intact)
            clearUserPreferences();

            AppLog.d("Account deletion completed successfully");
            return true;

        } catch (Exception e) {
            AppLog.e("Error during account deletion: " + e);
            errorMessage = e.getMessage() != null ? e.getMessage() : e.toString();
            return false;
        }
    }

    private void clearUserPreferences() {
        // Clear only user-related preferences, keep app settings
        android.content.SharedPreferences.Editor editor = MyApp.getInstance().prefs.edit();

        // Remove user account data
        editor.remove("diaro.signed_in_email");
        editor.remove("diaro.signed_in_account_type");
        editor.remove("diaro.pro");
        editor.remove("diaro.pro_subs_yearly");

        // Remove Dropbox data
        editor.remove("dropbox.access_token");
        editor.remove("dropbox.refresh_token");
        editor.remove("dropbox.expires_at");

        // Apply changes
        editor.apply();

        // Also clear user sign-in through UserMgr
        MyApp.getInstance().userMgr.setSignedInUser(null, null);

        // Turn off PRO status
        Static.turnOffPro();
        Static.turnOffSubscribedCurrently();
    }

    @Override
    protected void onPostExecute(Boolean success) {
        dismissProgressDialog();

        if (success) {
            // Clear Google Credential Manager state
            clearCredentialState();
        } else {
            // Show error
            String error = errorMessage != null ? errorMessage : activity.getString(R.string.error_deleting_account);
            Static.showToastError(error);
        }
    }

    private void clearCredentialState() {
        CredentialManager credentialManager = CredentialManager.create(activity);
        credentialManager.clearCredentialStateAsync(
                new ClearCredentialStateRequest(),
                new android.os.CancellationSignal(),
                Executors.newSingleThreadExecutor(),
                new CredentialManagerCallback<Void, ClearCredentialException>() {
                    @Override
                    public void onResult(Void result) {
                        activity.runOnUiThread(() -> completeAccountDeletion());
                    }

                    @Override
                    public void onError(@NonNull ClearCredentialException e) {
                        // Even if clearing credential state fails, proceed with completion
                        activity.runOnUiThread(() -> completeAccountDeletion());
                    }
                }
        );
    }

    private void completeAccountDeletion() {
        // Show success message
        Static.showToastSuccess(activity.getString(R.string.account_deleted_successfully));

        // Finish activity and return to sign-in screen
        activity.setResult(Activity.RESULT_OK);
        activity.finish();
    }

    private void dismissProgressDialog() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            AppLog.e("Error dismissing progress dialog: " + e);
        }
    }
}
