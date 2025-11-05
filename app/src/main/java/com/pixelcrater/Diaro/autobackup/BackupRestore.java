package com.pixelcrater.Diaro.autobackup;

import android.net.Uri;

import com.pixelcrater.Diaro.config.GlobalConstants;
import com.pixelcrater.Diaro.backuprestore.ExportToXml;
import com.pixelcrater.Diaro.backuprestore.ZipUtility;
import com.pixelcrater.Diaro.utils.AES256Cipher;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.KeyValuePair;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;
import com.pixelcrater.Diaro.utils.storage.PermanentStorageUtils;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;

public class BackupRestore {

    public static File createBackup(String type, boolean encrypt, boolean skipAttachments) throws Exception {

        String backupZipFilePathStart = PermanentStorageUtils.getDiaroBackupDirPath() + "/Diaro_";
        String backupZipFilePath;
        if (StringUtils.isNotEmpty(type)) {
            backupZipFilePath = backupZipFilePathStart + type + ".zip";
        } else {
            String todayDate = new DateTime().toString("yyyyMMdd");
            backupZipFilePathStart += todayDate;
            backupZipFilePath = backupZipFilePathStart + ".zip";
            // Generate new filename if already exists
            int i = 1;
            while (new File(backupZipFilePath).exists()) {
                backupZipFilePath = backupZipFilePathStart + "_" + i + ".zip";
                i++;
            }
        }
        AppLog.d("backupZipFilePath: " + backupZipFilePath);

        // Newly created backup file
        File backupZipFile = new File(backupZipFilePath);

        // Create '/media' directory (if does not exist)
        boolean mediaDirCreated = new File(AppLifetimeStorageUtils.getMediaDirPath()).mkdirs();
        AppLog.d("mediaDirCreated: " + mediaDirCreated);

        // Create new ArrayList of files to be added to backup ZIP
        ArrayList<KeyValuePair> zipFilesArrayList = new ArrayList<>();

        // Delete and create '/cache/backup' directory
        AppLifetimeStorageUtils.deleteCacheBackupDir();
        boolean cacheBackupDirCreated = new File(AppLifetimeStorageUtils.getCacheBackupDirPath()).mkdirs();
        AppLog.d("cacheBackupDirCreated: " + cacheBackupDirCreated);

        // Export entries, attachments, folders, tags, locations to xml
        new ExportToXml(AppLifetimeStorageUtils.getCacheBackupDirPath() + "/" + GlobalConstants.FILENAME_V2_DIARO_EXPORT_XML);

        String xmlFilename = GlobalConstants.FILENAME_V2_DIARO_EXPORT_XML;

        AppLog.d("encrypt: " + encrypt);

        if (encrypt) {
            // Encrypted xml file will have extension .denc
            xmlFilename = GlobalConstants.FILENAME_V2_DIARO_EXPORT_ENCRYPTED_DENC;

            // Create a file on the SD card
            File dencFile = new File(AppLifetimeStorageUtils.getCacheBackupDirPath() + "/" + xmlFilename);
            boolean dencFileCreated = dencFile.createNewFile();
            AppLog.d("dencFileCreated: " + dencFileCreated);

            // Encrypt xml file bytes
            AES256Cipher.encodeFile(new File(AppLifetimeStorageUtils.getCacheBackupDirPath() + "/" + GlobalConstants.FILENAME_V2_DIARO_EXPORT_XML), dencFile, GlobalConstants.ENCRYPTION_KEY);
        }

        // Add exported xml file to backup ZIP
        zipFilesArrayList.add(new KeyValuePair(AppLifetimeStorageUtils.getCacheBackupDirPath() + "/" + xmlFilename, AppLifetimeStorageUtils.getCacheBackupDirPath()));

        if (!skipAttachments) {
            // Add media folder to backup ZIP
            zipFilesArrayList.add(new KeyValuePair(AppLifetimeStorageUtils.getMediaDirPath(), AppLifetimeStorageUtils.getAppFilesDirPath()));

        }

        // Make ZIP file
        File tmpZipFile = new File(AppLifetimeStorageUtils.getCacheBackupDirPath() + "/" + backupZipFile.getName());

        boolean tmpZipCreated = tmpZipFile.createNewFile();
        AppLog.d("tmpZipCreated: " + tmpZipCreated);

        ZipUtility.zipDirectory(zipFilesArrayList, tmpZipFile);
        // Copy tmp ZIP file as a backup file to '/Diaro/backup' folder
        PermanentStorageUtils.copyBackupFile(Uri.fromFile(tmpZipFile), PermanentStorageUtils.getPermanentStoragePref(), PermanentStorageUtils.getPermanentStorageTreeUriPref());
        // Delete '/cache/backup' directory
        AppLifetimeStorageUtils.deleteCacheBackupDir();

        return backupZipFile;
    }
}
