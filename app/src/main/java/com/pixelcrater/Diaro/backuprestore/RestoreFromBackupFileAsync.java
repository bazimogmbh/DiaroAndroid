package com.pixelcrater.Diaro.backuprestore;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.pixelcrater.Diaro.config.GlobalConstants;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.entries.attachments.AttachmentsStatic;
import com.pixelcrater.Diaro.utils.AES256Cipher;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.DeprecatedCrypto;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;
import com.pixelcrater.Diaro.utils.storage.PermanentStorageUtils;
import com.pixelcrater.Diaro.utils.storage.StorageUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class RestoreFromBackupFileAsync extends AsyncTask<Object, String, Boolean> {

    private static final String FILEPATH_TEMP_ZIP = AppLifetimeStorageUtils.getCacheRestoreDirPath() + "/backup.zip";
    // v1
    private static final String v1xmlFilePath = AppLifetimeStorageUtils.getCacheRestoreDirPath() +  "/" + GlobalConstants.FILENAME_V1_DIARO_EXPORT_XML;
    private static final String v1drxmlFilePath = AppLifetimeStorageUtils.getCacheRestoreDirPath() + "/" + GlobalConstants.FILENAME_V1_DIARO_EXPORT_ENCRYPTED_DRXML;
    // v2
    private static final String v2xmlFilePath = AppLifetimeStorageUtils.getCacheRestoreDirPath() +"/" + GlobalConstants.FILENAME_V2_DIARO_EXPORT_XML;
    private static final String v2dencFilePath = AppLifetimeStorageUtils.getCacheRestoreDirPath() + "/" + GlobalConstants.FILENAME_V2_DIARO_EXPORT_ENCRYPTED_DENC;


    private Context mContext;
    private String mFileUriString;
    private boolean mDeleteOldData;
    private ProgressDialog pleaseWaitDialog;
    private String message;
    private String error = "";
    private String mFileName;

    public RestoreFromBackupFileAsync(Context context, String fileUriString, boolean deleteOldData) {
        AppLog.d("fileUriString: " + fileUriString + ", deleteOldData: " + deleteOldData);

        mContext = context;
        mFileUriString = fileUriString;
        mDeleteOldData = deleteOldData;

        Uri fileUri = Uri.parse(mFileUriString);
        mFileName = PermanentStorageUtils.getBackupFilename(fileUri);

        message = MyApp.getInstance().getString(R.string.settings_restoring_with_ellipsis).replace("%s", mFileName);
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
        File appCacheRestoreDirFile = new File(AppLifetimeStorageUtils.getCacheRestoreDirPath());

        File v1xmlFile = new File(v1xmlFilePath);
        File v1drxmlFile = new File(v1drxmlFilePath);
        File v2xmlFile = new File(v2xmlFilePath);
        File v2dencFile = new File(v2dencFilePath);

        try {
            // Delete '/cache/restore' directory
            StorageUtils.deleteFileOrDirectory(appCacheRestoreDirFile);

            // Create empty '/cache/restore' directory
            appCacheRestoreDirFile.mkdirs();

            String fileExtension = Static.getFileExtension(mFileName);
            Uri uri = Uri.parse(mFileUriString);

            switch (fileExtension) {
                // Extract encrypted file with extension '.diaro'
                case "diaro":
                    InputStream encryptedFis = PermanentStorageUtils.getBackupFileInputStream(uri);
                    DeprecatedCrypto.decryptFile(encryptedFis, new File(FILEPATH_TEMP_ZIP), GlobalConstants.ENCRYPTION_KEY);
                    ZipUtility.unzip(new FileInputStream(FILEPATH_TEMP_ZIP), appCacheRestoreDirFile);
                    break;

                // Extract zip file
                case "zip":
                    InputStream inputStream = PermanentStorageUtils.getBackupFileInputStream(uri);
                    ZipUtility.unzip(inputStream, appCacheRestoreDirFile);
                    break;

                // Copy xml file to '/cache/restore' directory
                case "xml":
                    PermanentStorageUtils.copyBackupFileToCache(mFileUriString, v2xmlFile);
                    break;

                // Copy xml file to '/cache/restore' directory
                case "denc":
                    PermanentStorageUtils.copyBackupFileToCache(mFileUriString, v2dencFile);
                    break;
            }

            // Rename DiaroExport.xml to DiaroBackup.xml
            if (v1xmlFile.exists()) {
                v1xmlFile.renameTo(v2xmlFile);
            }
            // If deprecated 128-bit encrypted .drxml file is found
            else if (v1drxmlFile.exists()) {
                InputStream encryptedFis = new FileInputStream(v1drxmlFile);
                DeprecatedCrypto.decryptFile(encryptedFis, v2xmlFile, GlobalConstants.ENCRYPTION_KEY);
            }
            // If 256-bit encrypted .denc file is found
            else if (v2dencFile.exists()) {
                AES256Cipher.decodeFile(v2dencFile, v2xmlFile, GlobalConstants.ENCRYPTION_KEY);
            }

            AppLog.d("mDeleteOldData: " + mDeleteOldData + ", v2dencFile.exists(): " + v2dencFile.exists());

            // Delete current media directory
            if (mDeleteOldData) {
                // Delete all attachments
                AttachmentsStatic.deleteAllAttachmentsFiles();
                StorageUtils.deleteFileOrDirectory(new File(AppLifetimeStorageUtils.getMediaDirPath()));

                // Delete current data from all tables
                MyApp.getInstance().storageMgr.clearAllData();
            }

            AppLog.d("v2xmlFilePath: " + v2xmlFilePath + ", v2xmlFile.exists(): " + v2xmlFile.exists());

            // Import data from xml
            new ImportFromXML(v2xmlFilePath);

            // Support old backup file
            if (new File(AppLifetimeStorageUtils.getDeprecatedCacheRestoreMediaPhotosDirPath()).exists()) {
                // Move photos from '/media/photos' directory to Diaro app '/media/photo' directory
                Static.moveAllPhotos(AppLifetimeStorageUtils.getDeprecatedCacheRestoreMediaPhotosDirPath());
            }

            // If backup has '/media' directory
            if (new File(AppLifetimeStorageUtils.getCacheRestoreMediaDirPath()).exists()) {
                // Move attachments directory
                Static.moveFileOrDirectory(new File(AppLifetimeStorageUtils.getCacheRestoreMediaDirPath()), new File(AppLifetimeStorageUtils.getMediaDirPath()));
            }

            // Delete '/cache/restore' directory
            StorageUtils.deleteFileOrDirectory(appCacheRestoreDirFile);
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
        // Send broadcast to refresh backup files list
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

        // Start indexing async
        MyApp.getInstance().asyncsMgr.executeDataIntegrityAsync();

        if (result) {
            // Show success message toast
            Static.showToastSuccess(MyApp.getInstance().getString(R.string.settings_restore_complete));
        } else {
            // Show error toast
            Static.showToastError(String.format("%s: %s", MyApp.getInstance().getString(R.string.error), error));

        }
    }
}
