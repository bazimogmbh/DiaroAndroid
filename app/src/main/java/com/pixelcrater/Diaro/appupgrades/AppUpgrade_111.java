package com.pixelcrater.Diaro.appupgrades;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;
import com.pixelcrater.Diaro.utils.storage.StorageUtils;

import java.io.File;

public class AppUpgrade_111 {
    public AppUpgrade_111() {
        moveAppLifetimeDirectoryContentsToFilesDirectory();
        updateSelectedRangeToPref();
    }

    /**
     * Move '/sdcard/Android/data/com.pixelcrater.Diaro/' contents to '/files' directory
     * This should avoid problems with Android cleaner apps
     */
    private void moveAppLifetimeDirectoryContentsToFilesDirectory() {
        try {
            // /media
            File oldMediaDir = new File(getDeprecatedAppLifetimeStorageDiaroDirPath() + "/media");
            if (oldMediaDir.exists()) {
                Static.moveFileOrDirectory(oldMediaDir, new File(AppLifetimeStorageUtils.getMediaDirPath()));
            }

            // /profile
            File oldProfileDir = new File(getDeprecatedAppLifetimeStorageDiaroDirPath() + "/profile");
            if (oldProfileDir.exists()) {
                Static.moveFileOrDirectory(oldProfileDir, new File(AppLifetimeStorageUtils.getProfilePhotoDirPath()));
            }

            // delete /tmp
            File oldTmpDir = new File(getDeprecatedAppLifetimeStorageDiaroDirPath() + "/tmp");
            if (oldTmpDir.exists()) {
                StorageUtils.deleteFileOrDirectory(oldTmpDir);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getDeprecatedAppLifetimeStorageDiaroDirPath() {
        return StorageUtils.getDefaultExternalStorage() + "/Android/data/com.pixelcrater.Diaro";
    }

    private void updateSelectedRangeToPref() {
        long selectedRangeToMillis = MyApp.getInstance().prefs. getLong(Prefs.PREF_ACTIVE_CALENDAR_RANGE_TO_MILLIS, 0);

        if (selectedRangeToMillis > 0) {
            MyApp.getInstance().prefs.edit().putLong(Prefs.PREF_ACTIVE_CALENDAR_RANGE_TO_MILLIS,  selectedRangeToMillis - 1).apply();

        }
    }
}
