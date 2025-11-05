package com.pixelcrater.Diaro.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import androidx.preference.PreferenceManager;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.entries.attachments.AttachmentsStatic;
import com.pixelcrater.Diaro.storage.dropbox.DropboxAccountManager;
import com.pixelcrater.Diaro.storage.dropbox.DropboxLocalHelper;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;
import com.pixelcrater.Diaro.utils.storage.PermanentStorageUtils;
import com.pixelcrater.Diaro.utils.storage.StorageUtils;

import java.io.File;
import java.util.ArrayList;

public class AppLaunchHelper {

    public static final Handler handler = new Handler();

    // Copy the 2 demo images to media folder
    public static void copyAssets() {
        String prefString = "_copyAssets";

        //Attachments
        String CONST_DEMO_IMAGE1 = "entry_demo_image1.jpg";
        String CONST_DEMO_IMAGE2 = "entry_demo_image2.jpg";
        String CONST_DEMO_IMAGE3 = "entry_demo_image3.jpg";

        if (!MyApp.getInstance().prefs.getBoolean(prefString, false)) {
            final Runnable r = () -> {
                File dir = new File(AppLifetimeStorageUtils.getMediaPhotosDirPath());
                if (!dir.exists())
                    dir.mkdirs();
                StorageUtils.copyAsset(MyApp.getInstance().getApplicationContext(), CONST_DEMO_IMAGE1, AppLifetimeStorageUtils.getMediaPhotosDirPath() + "/" + CONST_DEMO_IMAGE1);
                StorageUtils.copyAsset(MyApp.getInstance().getApplicationContext(), CONST_DEMO_IMAGE2, AppLifetimeStorageUtils.getMediaPhotosDirPath() + "/" + CONST_DEMO_IMAGE2);
                StorageUtils.copyAsset(MyApp.getInstance().getApplicationContext(), CONST_DEMO_IMAGE3, AppLifetimeStorageUtils.getMediaPhotosDirPath() + "/" + CONST_DEMO_IMAGE3);
            };

            MyApp.getInstance().prefs.edit().putBoolean(prefString, true).apply();

            handler.post(r);

        }
    }

    public static void removeFreeUpDeviceStorageOption() {

        String prefString = "_upgradeDevStorage";
        SharedPreferences preferences = MyApp.getInstance().prefs;
        ;
        if (!preferences.getBoolean(prefString, false)) {
            if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_FREE_UP_DEVICE_STORAGE, false)) {

                preferences.edit().putBoolean(Prefs.PREF_FREE_UP_DEVICE_STORAGE, false).apply();

                File mediaDir = new File(AppLifetimeStorageUtils.getMediaPhotosDirPath());
                ArrayList<String> fileNamesList = new ArrayList<>();

                try {
                    File[] files = mediaDir.listFiles();
                    if (files != null && files.length > 1) {
                        for (File f : files) {
                            if (f.isFile()) {
                                fileNamesList.add(f.getName());
                            }
                        }

                        // List of all attachemnts in media photo folder
                        String filesList = fileNamesList.toString().replace("[", "'").replace("]", "'").replace(", ", "','");
                        // Update all attachments, which were not found in photo folder, to  file_sync_id to "file_not_downloaded"
                        MyApp.getInstance().storageMgr.getSQLiteAdapter().resetAttachmentsFileAsNotDownloaded(filesList);
                    }

                } catch (Exception e) {

                }

                DropboxLocalHelper.clearAllFolderCursors();


            }

            preferences.edit().putBoolean(prefString, true).apply();
        }
    }

    public static void uploadDbtoDropboxOnce(Boolean resetAllTablesSyncedField) {

        String prefString = "x_firsttrya_db";
        SharedPreferences preferences = MyApp.getInstance().prefs;

        if (!preferences.getBoolean(prefString, false)) {
            try {
                Static.uploadDatabaseToDropbox();
            } catch (Exception e) {
                AppLog.e("Exception: " + e);
            }

            // RESET FIELDS
            if (resetAllTablesSyncedField) {
                MyApp.getInstance().storageMgr.getSQLiteAdapter().resetAllTablesSyncedField();
                DropboxLocalHelper.clearAllFolderCursors();
            }

            preferences.edit().putBoolean(prefString, true).apply();
        }

    }

    public static void setDropboxID(Context ctx, String uid, String token) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        if (uid.compareToIgnoreCase("") == 0) return;
        if (token.compareToIgnoreCase("") == 0) return;

        String prefString = "_firsttry__";

        if (!sharedPrefs.getBoolean(prefString, false)) {

            DropboxLocalHelper.clearAllFolderCursors();

            // set new
            sharedPrefs.edit().putString(DropboxAccountManager.PREF_DROPBOX_UID_V1, uid).apply();
            sharedPrefs.edit().putString(DropboxAccountManager.PREF_DROPBOX_TOKEN, token).apply();

            sharedPrefs.edit().putBoolean(prefString, true).apply();
        }

    }

    public static void deleteAttachmentsOfNotExistingEntriesOnce(Context ctx) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String prefString = "deleteAttachmentsOfNotExistingEntries";

        if (!sharedPrefs.getBoolean(prefString, false)) {
            AttachmentsStatic.deleteAttachmentsOfNotExistingEntries();
            sharedPrefs.edit().putBoolean(prefString, true).apply();
        }
    }

    public static void changeBackupFilesDirOnce(Activity activity) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        String prefString = "_changeBackupFilesDirOnce_";

      //  MyApp.getInstance().asyncsMgr.executeChangePermanentStorageAsync(activity, Environment.getExternalStorageDirectory().getAbsolutePath(), null);
        if (!sharedPrefs.getBoolean(prefString, false)) {

            try {
                String mNewStorageTreeUriString = null;
                String mNewStoragePath = activity.getApplicationContext().getExternalFilesDir(null).getAbsolutePath();

                new Thread((Runnable) () -> {
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
                            PermanentStorageUtils.copyDiaroBackupDirectory(mNewStoragePath, null);

                            // Delete old '/Diaro' directory
                            PermanentStorageUtils.deleteDiaroDir();
                        }

                        // Update preference
                        PermanentStorageUtils.updatePermanentStoragePref(mNewStoragePath, mNewStorageTreeUriString);
                    } catch (Exception e) {
                        AppLog.e("Exception: " + e);

                        // Delete new '/Diaro' directory
                        PermanentStorageUtils.deleteDiaroDirByStorage(mNewStoragePath, mNewStorageTreeUriString);
                    }
                }).start();
            } catch (Exception e) {

            }
            sharedPrefs.edit().putBoolean(prefString, true).apply();
        }

    }

}
