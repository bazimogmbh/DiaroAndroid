package com.pixelcrater.Diaro.utils.storage;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.utils.AppLog;

import org.apache.commons.lang3.StringUtils;

import java.io.File;

import static com.pixelcrater.Diaro.config.GlobalConstants.DIR_MEDIA;
import static com.pixelcrater.Diaro.config.GlobalConstants.DIR_MEDIA_PHOTO;
import static com.pixelcrater.Diaro.config.GlobalConstants.DIR_PROFILE;
import static com.pixelcrater.Diaro.config.GlobalConstants.FILENAME_PROFILE;

/**
 * This class manages app lifetime storage
 * "/data/Android/com.pixelcrater.Diaro/files" directory, which gets delete on app uninstall
 */
public class AppLifetimeStorageUtils {

    /**
     * This directory is deleted on app uninstall by Android
     */
    public static String getAppFilesDirPath() {
        return getAppFilesDirPathByStorage(getAppLifetimeStoragePref());
    }

    public static String getAppFilesDirPathByStorage(String storagePath) {
        if(storagePath.contains("com.pixelcrater.Diaro/files")) {
            return storagePath;
        }
        else{
            return StringUtils.equals(storagePath, "/data/data") ? storagePath + "/com.pixelcrater.Diaro/files" : storagePath + "/Android/data/com.pixelcrater.Diaro/files";
        }

    }

    public static String getAppLifetimeStoragePref() {
        return MyApp.getInstance().prefs.getString(Prefs.PREF_APP_LIFETIME_STORAGE, StorageUtils.getDefaultExternalStorage());

    }

    public static String getMediaDirPath() {
        return getAppFilesDirPath() + "/" + DIR_MEDIA;
    }

    public static String getMediaPhotosDirPath() {
        return getAppFilesDirPath() + "/" + DIR_MEDIA_PHOTO ;
    }

    public static String getProfilePhotoDirPath() {
        return getAppFilesDirPath() + "/" + DIR_PROFILE;
    }

    public static String getProfilePhotoFilePath() {
        return getProfilePhotoDirPath() + "/" + FILENAME_PROFILE;
    }

    public static String getCacheDirPath() {
        return getAppFilesDirPath() + "/cache";
    }

    public static String getCacheBackupDirPath() {
        return getCacheDirPath() + "/backup";
    }

    public static String getCacheRestoreDirPath() {
        return getCacheDirPath() + "/restore";
    }

    public static String getCacheRestoreMediaDirPath() {
        return getCacheRestoreDirPath() + "/" + DIR_MEDIA;
    }

    public static String getDeprecatedCacheRestoreMediaPhotosDirPath() {
        return getCacheRestoreMediaDirPath() + "/photos";
    }

    public static void deleteCacheDir() {
        StorageUtils.deleteFileOrDirectory(new File(getCacheDirPath()));
    }

    public static void deleteCacheBackupDir() {
        StorageUtils.deleteFileOrDirectory(new File(getCacheBackupDirPath()));
    }

    public static void deleteCacheDirectory() {
        StorageUtils.deleteFileOrDirectory(new File(getCacheDirPath()));
    }

    public static void createCacheDirectory() {
        AppLog.d("getCacheDirPath(): " + getCacheDirPath());
        boolean cacheDirCreated = new File(getCacheDirPath()).mkdirs();
        AppLog.d("cacheDirCreated: " + cacheDirCreated);
    }
}