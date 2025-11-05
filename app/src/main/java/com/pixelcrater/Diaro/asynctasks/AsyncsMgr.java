package com.pixelcrater.Diaro.asynctasks;

import android.content.Context;
import android.os.AsyncTask;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.backuprestore.CreateBackupAsync;
import com.pixelcrater.Diaro.backuprestore.DownloadBackupFileFromDropboxAsync;
import com.pixelcrater.Diaro.backuprestore.RestoreFromBackupFileAsync;
import com.pixelcrater.Diaro.backuprestore.UploadBackupFileToDropboxAsync;
import com.pixelcrater.Diaro.entries.async.ArchiveEntriesAsync;
import com.pixelcrater.Diaro.premium.SendPaymentAsync;
import com.pixelcrater.Diaro.profile.CheckProAsync;
import com.pixelcrater.Diaro.profile.ForgotPasswordAsync;
import com.pixelcrater.Diaro.profile.SignInAsync;
import com.pixelcrater.Diaro.profile.SignUpAsync;
import com.pixelcrater.Diaro.securitycode.ForgotSecurityCodeAsync;
import com.pixelcrater.Diaro.settings.ChangeAppLifetimeStorageAsync;
import com.pixelcrater.Diaro.settings.ChangePermanentStorageAsync;
import com.pixelcrater.Diaro.storage.SyncStatic;
import com.pixelcrater.Diaro.storage.dropbox.SyncAsync;
import com.pixelcrater.Diaro.storage.dropbox.SyncProfilePhotoAsync;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.weather.GetWeatherAsync;

import java.util.ArrayList;

public class AsyncsMgr {
    // AsyncTasks
    public DataIntegrityAsync dataIntegrityAsync;
    public CreateBackupAsync createBackupAsync;
    public ChangePermanentStorageAsync changePermanentStorageAsync;
    public ChangeAppLifetimeStorageAsync changeAppLifetimeStorageAsync;
    public RestoreFromBackupFileAsync restoreFromBackupFileAsync;
    public SignInAsync signInAsync;
    public SignUpAsync signUpAsync;
    public DownloadBackupFileFromDropboxAsync downloadBackupFileFromDropboxAsync;
    public UploadBackupFileToDropboxAsync uploadBackupFileToDropboxAsync;
    public ForgotPasswordAsync forgotPasswordAsync;
    public ForgotSecurityCodeAsync forgotSecurityCodeAsync;
    public CheckProAsync checkProAsync;

    public SyncAsync syncAsync;
    public ArchiveEntriesAsync archiveEntriesAsync;
    public SelectAllEntriesAsync selectAllEntriesAsync;
    public CheckIfAllEntriesExistAsync checkIfAllEntriesExistAsync;
    public SyncProfilePhotoAsync syncProfilePhotoAsync;

    public GetWeatherAsync getWeatherAsync;

    public AsyncsMgr() {
    }

    public boolean isAsyncRunning(AsyncTask<Object, String, Boolean> asyncTask) {
        return asyncTask != null && asyncTask.getStatus() == AsyncTask.Status.RUNNING;
    }

    /**
     * Data integrity async
     */
    public void executeDataIntegrityAsync() {
        cancelDataIntegrityAsync();
        dataIntegrityAsync = new DataIntegrityAsync();
        // Execute on a separate thread
        Static.startMyTask(dataIntegrityAsync);
    }

    public void cancelDataIntegrityAsync() {
        try {
            if (dataIntegrityAsync != null) {
                dataIntegrityAsync.cancel(true);
            }
        } catch (Exception e) {
        }
    }

    /**
     * (API) Check Pro async
     */
    public void executeCheckProAsync(String email) {
        if (!MyApp.getInstance().networkStateMgr.isConnectedToInternet()) {
            return;
        }

        cancelCheckProAsync();
        checkProAsync = new CheckProAsync(email);
        // Execute on a separate thread
        Static.startMyTask(checkProAsync);
    }

    public void cancelCheckProAsync() {
        try {
            if (checkProAsync != null) {
                checkProAsync.cancel(true);
            }
        } catch (Exception e) {
        }
    }

    /**
     * (API) Send payment async
     */
    public void executeSendPaymentAsync(String sku, String date, String system, String type, String email, String product, String purchaseToken, String description, String price, String currencyCode, String orderId, String refunded) {
        if (!MyApp.getInstance().networkStateMgr.isConnectedToInternet()) {
            return;
        }
        String signedInEmail = MyApp.getInstance().userMgr.getSignedInEmail();
        SendPaymentAsync sendPaymentAsync = new SendPaymentAsync(sku, signedInEmail, date, system, type, email, product, purchaseToken, description, price, currencyCode, orderId, refunded);
        // Execute on a separate thread
        Static.startMyTask(sendPaymentAsync);
    }


    /**
     * (API) Forgot password async
     */
    public void executeForgotPasswordAsync(Context context, String email) {
        cancelForgotPasswordAsync();

        forgotPasswordAsync = new ForgotPasswordAsync(context, email);
        // Execute on a separate thread
        Static.startMyTask(forgotPasswordAsync);
    }

    public void cancelForgotPasswordAsync() {
        try {
            if (forgotPasswordAsync != null) {
                forgotPasswordAsync.cancel(true);
            }
        } catch (Exception e) {
        }
    }

    /**
     * (API) Sign in async
     */
    public void executeSignInAsync(Context context, String email, String password, String googleId, String name, String surname, String gender, String birthday) {
        cancelSignInAsync();
        signInAsync = new SignInAsync(context, email, password, googleId, name, surname, gender, birthday);
        // Execute on a separate thread
        Static.startMyTask(signInAsync);
    }

    public void cancelSignInAsync() {
        try {
            if (signInAsync != null) {
                signInAsync.cancel(true);
            }
        } catch (Exception e) {
        }
    }

    /**
     * (API) Sign up async
     */
    public void executeSignUpAsync(Context context, String email, String password) {
        cancelSignUpAsync();
        signUpAsync = new SignUpAsync(context, email, password);
        // Execute on a separate thread
        Static.startMyTask(signUpAsync);
    }

    public void cancelSignUpAsync() {
        try {
            if (signUpAsync != null) {
                signUpAsync.cancel(true);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Change permanent storage async
     */
    public void executeChangePermanentStorageAsync(Context context, String newStoragePath, String newStorageTreeUriString) {

        cancelChangePermanentStorageAsync();

        // Cancel download/upload backup file from/to Dropbox
        cancelDownloadBackupFileFromDropboxAsync();
        cancelUploadBackupFileToDropboxAsync();

        changePermanentStorageAsync = new ChangePermanentStorageAsync(context, newStoragePath, newStorageTreeUriString);
        // Execute on a separate thread
        Static.startMyTask(changePermanentStorageAsync);
    }

    public void cancelChangePermanentStorageAsync() {
        try {
            if (changePermanentStorageAsync != null) {
                changePermanentStorageAsync.cancel(true);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Change app lifetime storage async
     */
    public void executeChangeAppLifetimeStorageAsync(Context context, String oldStoragePrefValue, String newStoragePrefValue) {

        cancelChangeAppLifetimeStorageAsync();

        // Cancel Dropbox sync
        cancelSyncAsync();
        changeAppLifetimeStorageAsync = new ChangeAppLifetimeStorageAsync(context, oldStoragePrefValue, newStoragePrefValue);
        // Execute on a separate thread
        Static.startMyTask(changeAppLifetimeStorageAsync);
    }

    public void cancelChangeAppLifetimeStorageAsync() {
        try {
            if (changeAppLifetimeStorageAsync != null) {
                changeAppLifetimeStorageAsync.cancel(true);
                changeAppLifetimeStorageAsync.setFinished();
            }
        } catch (Exception e) {
        }
    }

    /**
     * Create backup async
     */
    public void executeCreateBackupAsync(Context context, int tabId, boolean encrypt, boolean skipAttachments) {

        cancelCreateBackupAsync();
        createBackupAsync = new CreateBackupAsync(context, tabId, encrypt, skipAttachments);
        // Execute on a separate thread
        Static.startMyTask(createBackupAsync);
    }

    public void cancelCreateBackupAsync() {
        try {
            if (createBackupAsync != null) {
                createBackupAsync.cancel(true);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Restore from backup async
     */
    public void executeRestoreFromBackupFileAsync(Context context, String fileUriString, boolean deleteOldData) {

        cancelRestoreFromBackupFileAsync();
        restoreFromBackupFileAsync = new RestoreFromBackupFileAsync(context, fileUriString, deleteOldData);
        // Execute on a separate thread
        Static.startMyTask(restoreFromBackupFileAsync);
    }

    public void cancelRestoreFromBackupFileAsync() {
        try {
            if (restoreFromBackupFileAsync != null) {
                restoreFromBackupFileAsync.cancel(true);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Download backup file from Dropbox async
     */
    public void executeDownloadBackupFileFromDropboxAsync(Context context, String dropboxFilePath, boolean restore, boolean deleteOldData) {

        cancelDownloadBackupFileFromDropboxAsync();
        downloadBackupFileFromDropboxAsync = new DownloadBackupFileFromDropboxAsync(context, dropboxFilePath, restore, deleteOldData);
        // Execute on a separate thread
        Static.startMyTask(downloadBackupFileFromDropboxAsync);
    }

    public void cancelDownloadBackupFileFromDropboxAsync() {
        try {
            if (downloadBackupFileFromDropboxAsync != null) {
                downloadBackupFileFromDropboxAsync.cancel(true);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Upload backup file to Dropbox async
     */
    public void executeUploadBackupFileToDropboxAsync(Context context, String localFileUriString) {
        cancelUploadBackupFileToDropboxAsync();
        uploadBackupFileToDropboxAsync = new UploadBackupFileToDropboxAsync(context, localFileUriString);
        // Execute on a separate thread
        Static.startMyTask(uploadBackupFileToDropboxAsync);
    }

    public void cancelUploadBackupFileToDropboxAsync() {
        try {
            if (uploadBackupFileToDropboxAsync != null) {
                uploadBackupFileToDropboxAsync.cancel(true);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Archive entries async
     */
    public void executeArchiveEntriesAsync(ArrayList<String> entriesUids) {
        cancelUploadBackupFileToDropboxAsync();
        archiveEntriesAsync = new ArchiveEntriesAsync(entriesUids);
        // Execute on a separate thread
        Static.startMyTask(archiveEntriesAsync);
    }

    /**
     * Select all entries async
     */
    public void executeSelectAllEntriesAsync(Context context) {
        cancelSelectAllEntriesAsync();
        selectAllEntriesAsync = new SelectAllEntriesAsync(context);
        // Execute on a separate thread
        Static.startMyTask(selectAllEntriesAsync);
    }

    public void cancelSelectAllEntriesAsync() {
        try {
            if (selectAllEntriesAsync != null) {
                selectAllEntriesAsync.cancel(true);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Check if all entries exist async
     */
    public void executeCheckIfAllEntriesExistAsync(Context context, ArrayList<String> multiSelectedEntriesUids) {
        cancelCheckIfAllEntriesExistAsync();
        checkIfAllEntriesExistAsync = new CheckIfAllEntriesExistAsync(context,  multiSelectedEntriesUids);
        // Execute on a separate thread
        Static.startMyTask(checkIfAllEntriesExistAsync);
    }

    public void cancelCheckIfAllEntriesExistAsync() {
        try {
            if (checkIfAllEntriesExistAsync != null) {
                checkIfAllEntriesExistAsync.cancel(true);
            }
        } catch (Exception e) {
        }
    }

    /**
     * (API) Forgot security code async
     */
    public void executeForgotSecurityCodeAsync(Context context, String email, String securityCode) {
        cancelForgotSecurityCodeAsync();
        forgotSecurityCodeAsync = new ForgotSecurityCodeAsync(context, email, securityCode);
        // Execute on a separate thread
        Static.startMyTask(forgotSecurityCodeAsync);
    }

    public void cancelForgotSecurityCodeAsync() {
        try {
            if (forgotSecurityCodeAsync != null) {
                forgotSecurityCodeAsync.cancel(true);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Sync profile photo
     */
    public void executeSyncProfilePhotoAsync() {
        cancelSyncProfilePhotoAsync();
        syncProfilePhotoAsync = new SyncProfilePhotoAsync();
        // Execute on a separate thread
        Static.startMyTask(syncProfilePhotoAsync);
    }

    public void cancelSyncProfilePhotoAsync() {
        try {
            if (syncProfilePhotoAsync != null) {
                syncProfilePhotoAsync.cancel(true);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Sync profile photo
     */
    public void executeGetWeatherAsync(GetWeatherAsync.WeatherAsyncCallback listner, String lat , String lon) {
        cancelGetWeatherAsync();
        getWeatherAsync = new GetWeatherAsync(listner, lat, lon );
        // Execute on a separate thread
        Static.startMyTask(getWeatherAsync);
    }

    public void cancelGetWeatherAsync() {
        try {
            if (getWeatherAsync != null) {
                getWeatherAsync.cancel(true);
            }
        } catch (Exception e) {
        }
    }

    /**
     * Sync async
     */
    public void executeIfNotRunningSyncAsync(Context context) {
        AppLog.d("SyncStatic.isSyncPrefsOk(): " + SyncStatic.isSyncPrefsOk() +
                ", isSyncAsyncRunning(): " + isSyncAsyncRunning() +
                ", isDownloadBackupAsyncRunning(): " + isDownloadBackupAsyncRunning() +
                ", isUploadBackupAsyncRunning(): " + isUploadBackupAsyncRunning() +
                ", isChangeAppLifetimeStorageAsyncRunning(): " +
                isChangeAppLifetimeStorageAsyncRunning());

        if (MyApp.getInstance().storageMgr.isStorageDropbox() &&  SyncStatic.isSyncPrefsOk() &&
                !isDownloadBackupAsyncRunning() && !isUploadBackupAsyncRunning() &&  !isChangeAppLifetimeStorageAsyncRunning()) {
            if (isSyncAsyncRunning()) {
                syncAsync.setRepeatSync();
            } else {
                syncAsync = new SyncAsync(context);
                // Execute on a separate thread
                Static.startMyTask(syncAsync);
            }
        }
    }

    public void cancelSyncAsync() {
        try {
            if (syncAsync != null) {
                syncAsync.cancel(true);
                syncAsync.setFinished();
            }
        } catch (Exception e) {
        }
    }

    public boolean isArchiveEntriesAsyncRunning() {
        return isAsyncRunning(archiveEntriesAsync) && !archiveEntriesAsync.isFinished();
    }

    public boolean isChangeAppLifetimeStorageAsyncRunning() {
        return isAsyncRunning(changeAppLifetimeStorageAsync) &&
                !changeAppLifetimeStorageAsync.isFinished();
    }

    public boolean isDownloadBackupAsyncRunning() {
        return isAsyncRunning(downloadBackupFileFromDropboxAsync) && !downloadBackupFileFromDropboxAsync.isFinished();

    }

    public boolean isUploadBackupAsyncRunning() {
        return isAsyncRunning(uploadBackupFileToDropboxAsync) && !uploadBackupFileToDropboxAsync.isFinished();

    }

    public boolean isSyncAsyncRunning() {
        return isAsyncRunning(syncAsync) && !syncAsync.isFinished();
    }
}
