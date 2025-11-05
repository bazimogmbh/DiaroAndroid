package com.pixelcrater.Diaro.utils.storage;

import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;

import androidx.documentfile.provider.DocumentFile;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.Static;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class manages permanent storage
 * "/Diaro" directory, which is not deleted on app uninstall
 */
public class PermanentStorageUtils {

    public static final String DIR_BACKUP = "backup";

    public static String getPermanentStoragePref() {
        return MyApp.getInstance().prefs.getString(Prefs.PREF_PERMANENT_STORAGE, StorageUtils.getDefaultExternalStorage());
    }

    public static String getPermanentStorageTreeUriPref() {
        return MyApp.getInstance().prefs.getString(Prefs.PREF_PERMANENT_STORAGE_TREE_URI, null);
    }

    public static void updatePermanentStoragePref(String newStoragePath, String newStorageTreeUriString) {

        AppLog.d("newStoragePath: " + newStoragePath + ", newStorageTreeUriString: " + newStorageTreeUriString);
        MyApp.getInstance().prefs.edit().putString(Prefs.PREF_PERMANENT_STORAGE, newStoragePath).apply();
        // Persist URI in shared preference so that you can use it later.
        MyApp.getInstance().prefs.edit().putString(Prefs.PREF_PERMANENT_STORAGE_TREE_URI, newStorageTreeUriString).apply();
    }

    /**
     * This directory is not deleted on app uninstall
     */
    public static String getDiaroDirPath() {
//        AppLog.d("getDefaultExternalStorage(): " + getDefaultExternalStorage());
//        AppLog.d("getPermanentStoragePref(): " + getPermanentStoragePref());
//        AppLog.d("getDiaroDirPathByStorage(getPermanentStoragePref()): " +
//                getDiaroDirPathByStorage(getPermanentStoragePref()));
        return getDiaroDirPathByStorage(getPermanentStoragePref());
    }

    public static String getDiaroDirPathByStorage(String storagePath) {
        return storagePath + "/Diaro";
    }

    public static String getDiaroBackupDirPath() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            String storagePath = getPermanentStoragePref();
            if (storagePath.contains("com.pixelcrater.Diaro/files")) {
                storagePath = storagePath;
            } else {
                storagePath = StringUtils.equals(storagePath, "/data/data") ? storagePath + "/com.pixelcrater.Diaro/files" : storagePath + "/Android/data/com.pixelcrater.Diaro/files";
            }

            return storagePath + "/Diaro/" + DIR_BACKUP;
        } else {
            return getDiaroDirPath() + "/" + DIR_BACKUP;
        }
    }

    public static String getDiaroBackupDirPathByStorage(String storagePath) {
        return getDiaroDirPathByStorage(storagePath) + "/" + DIR_BACKUP;
    }

    /**
     * Check if Storage Access Framework should be used
     */
    public static boolean shouldUseSaf() {
        return getPermanentStorageTreeUriPref() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static Uri getPermanentStorageTreeUri() {
        return Uri.parse(getPermanentStorageTreeUriPref());
    }

    public static DocumentFile getPermanentStorageDf() {
        return DocumentFile.fromTreeUri(MyApp.getInstance(), getPermanentStorageTreeUri());
    }

    public static DocumentFile getPermanentStorageDiaroDf() {
        DocumentFile permanentStorageDf = getPermanentStorageDf();
        if (permanentStorageDf != null) {
            return getPermanentStorageDf().findFile("Diaro");
        }
        return null;
    }

    public static DocumentFile getPermanentStorageDiaroBackupDf() {
        DocumentFile permanentStorageDiaroDf = getPermanentStorageDiaroDf();
        if (permanentStorageDiaroDf != null) {
            return permanentStorageDiaroDf.findFile(DIR_BACKUP);
        }
        return null;
    }

    public static boolean deleteBackupFile(Uri backupFileUri) {
        if (DocumentFile.isDocumentUri(MyApp.getInstance(), backupFileUri)) {
            DocumentFile df = DocumentFile.fromSingleUri(MyApp.getInstance(), backupFileUri);
            try {
                return DocumentsContract.deleteDocument(MyApp.getInstance().getContentResolver(), df.getUri());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            File backupFile = new File(backupFileUri.getPath());
            return StorageUtils.deleteFileOrDirectory(backupFile);
        }
    }

    public static boolean deleteDiaroDir() {
        if (shouldUseSaf()) {
            DocumentFile permanentStorageDiaroDf = getPermanentStorageDiaroDf();
            if (permanentStorageDiaroDf != null) {
                try {
                    return DocumentsContract.deleteDocument(MyApp.getInstance().getContentResolver(), permanentStorageDiaroDf.getUri());

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        } else {
            return StorageUtils.deleteFileOrDirectory(new File(getDiaroDirPath()));
        }
        return false;
    }

    public static boolean deleteDiaroDirByStorage(String storagePath, String storageTreeUriString) {
        if (storageTreeUriString != null) {
            Uri storageTreeUri = Uri.parse(storageTreeUriString);
            if (storageTreeUri != null) {
                DocumentFile storageDf = DocumentFile.fromTreeUri(MyApp.getInstance(), storageTreeUri);

                // Find '/Diaro' folder
                DocumentFile storageDiaroDf = storageDf.findFile("Diaro");
                if (storageDiaroDf != null) {
                    return storageDf.delete();
                }
            }
        } else if (storagePath != null) {
            return StorageUtils.deleteFileOrDirectory(new File(getDiaroDirPathByStorage(storagePath)));
        }
        return false;
    }

    public static boolean createDiaroBackupDirectory() {
        return createDiaroBackupDirectoryByStorage(getPermanentStoragePref(), getPermanentStorageTreeUriPref());
    }

    public static boolean createDiaroBackupDirectoryByStorage(String newStoragePath, String newStorageTreeUriString) {
        if (newStorageTreeUriString != null) {
            Uri newStorageTreeUri = Uri.parse(newStorageTreeUriString);
            if (newStorageTreeUri != null) {
                DocumentFile newStorageDf = DocumentFile.fromTreeUri(MyApp.getInstance(), newStorageTreeUri);

                // Create '/Diaro' folder if does not exist
                DocumentFile newStorageDiaroDf;
                if (newStorageDf.findFile("Diaro") == null) {
                    newStorageDiaroDf = newStorageDf.createDirectory("Diaro");
                } else {
                    newStorageDiaroDf = newStorageDf.findFile("Diaro");
                }

                // Create '/backup' folder if does not exist
                DocumentFile newStorageDiaroBackupDf;
                if (newStorageDiaroDf.findFile(DIR_BACKUP) == null) {
                    newStorageDiaroBackupDf = newStorageDiaroDf.createDirectory(DIR_BACKUP);
                } else {
                    newStorageDiaroBackupDf = newStorageDiaroDf.findFile(DIR_BACKUP);
                }

                return newStorageDiaroBackupDf != null && newStorageDiaroBackupDf.exists();
            }
        } else if (newStoragePath != null) {
            File diaroBackupDir = new File(getDiaroBackupDirPathByStorage(newStoragePath));
            AppLog.d("diaroBackupDir: " + diaroBackupDir + ", diaroBackupDir.exists(): " + diaroBackupDir.exists());
            if (diaroBackupDir.exists()) {
                return true;
            }
            return diaroBackupDir.mkdirs();
        }
        return false;
    }

    public static Uri[] getBackupFilesUris() {
        final Set<Uri> hashSet = new HashSet<>();

        AppLog.d("shouldUseSaf(): " + shouldUseSaf() + ", getPermanentStoragePref(): " + getPermanentStoragePref() + ", getPermanentStorageTreeUriPref(): " + getPermanentStorageTreeUriPref());

        if (shouldUseSaf()) {
            DocumentFile storageDiaroBackupDf = getPermanentStorageDiaroBackupDf();
            if (storageDiaroBackupDf != null) {
                DocumentFile[] dfs = storageDiaroBackupDf.listFiles();
                if (dfs != null) {
                    for (final DocumentFile df : dfs) {
                        if (df.isFile()) {
                            AppLog.d("df.getUri(): " + df.getUri());
                            hashSet.add(df.getUri());
                        }
                    }
                }
            }
        } else {
            AppLog.d("getDiaroBackupDirPath(): " + getDiaroBackupDirPath());

            File[] backupFilesDir = new File(getDiaroBackupDirPath()).listFiles();
            if (backupFilesDir != null) {
                AppLog.d("backupFilesDir.length: " + backupFilesDir.length);
                for (final File file : backupFilesDir) {
                    if (file.isFile()) {
                        hashSet.add(Uri.parse(file.getPath()));
                    }
                }
            }
        }

        AppLog.d("hashSet: " + hashSet);
        return hashSet.toArray(new Uri[hashSet.size()]);
    }

    public static void copyDiaroBackupDirectory(String newStoragePath, String newStorageTreeUriString) throws Exception {
        Uri[] backupFilesUris = getBackupFilesUris();
        for (final Uri backupFileUri : backupFilesUris) {
            copyBackupFile(backupFileUri, newStoragePath, newStorageTreeUriString);
        }
    }

    public static String getBackupFilename(Uri backupFileUri) {
        if (DocumentFile.isDocumentUri(MyApp.getInstance(), backupFileUri)) {
            DocumentFile df = DocumentFile.fromSingleUri(MyApp.getInstance(), backupFileUri);
            return df.getName();
        } else {
            File backupFile = new File(backupFileUri.getPath());
            return backupFile.getName();
        }
    }

    public static long getBackupFileLength(Uri backupFileUri) {
        if (DocumentFile.isDocumentUri(MyApp.getInstance(), backupFileUri)) {
            DocumentFile df = DocumentFile.fromSingleUri(MyApp.getInstance(), backupFileUri);
            return df.length();
        } else {
            File backupFile = new File(backupFileUri.getPath());
            return backupFile.length();
        }
    }

    public static long getBackupFileLastModified(Uri backupFileUri) {
        if (DocumentFile.isDocumentUri(MyApp.getInstance(), backupFileUri)) {
            DocumentFile df = DocumentFile.fromSingleUri(MyApp.getInstance(), backupFileUri);
            return df.lastModified();
        } else {
            File backupFile = new File(backupFileUri.getPath());
            return backupFile.lastModified();
        }
    }

    public static InputStream getBackupFileInputStream(Uri backupFileUri) throws Exception {
        if (DocumentFile.isDocumentUri(MyApp.getInstance(), backupFileUri)) {
            return MyApp.getInstance().getContentResolver().openInputStream(backupFileUri);
        } else {
            File backupFile = new File(backupFileUri.getPath());
            return new FileInputStream(backupFile);
        }
    }

    public static File copyBackupFileToCache(String fileUriString, File toFile) throws Exception {
        AppLog.d("fileUriString: " + fileUriString);

        Uri backupFileUri = Uri.parse(fileUriString);
        if (DocumentFile.isDocumentUri(MyApp.getInstance(), backupFileUri)) {
            // Create '/cache/backup' directory
            boolean cacheBackupDirCreated = new File(AppLifetimeStorageUtils.getCacheBackupDirPath()).mkdirs();
            AppLog.d("cacheBackupDirCreated: " + cacheBackupDirCreated);

            String backupFileName = getBackupFilename(backupFileUri);
            AppLog.d("backupFileName: " + backupFileName);

            if (toFile == null) {
                toFile = new File(AppLifetimeStorageUtils.getCacheBackupDirPath() + "/" + backupFileName);
            }

            InputStream inputStream = getBackupFileInputStream(backupFileUri);
            OutputStream outputStream = new FileOutputStream(toFile);
            AppLog.d("inputStream: " + inputStream + ", outputStream: " + outputStream);

            // Copy contents
            IOUtils.copy(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
            AppLog.d("Streams closed");
        } else {
            Static.copyFileOrDirectory(new File(fileUriString), toFile);
        }
        AppLog.d("toFile.getPath(): " + toFile.getPath());
        return toFile;
    }

    public static void copyBackupFile(Uri backupFileUri, String newStoragePath, String newStorageTreeUriString) throws Exception {
        AppLog.d("backupFileUri: " + backupFileUri + ", newStoragePath: " + newStoragePath + ", newStorageTreeUriString: " + newStorageTreeUriString);

        // Test if given Uri is backed by a android.provider.DocumentsProvider.
        boolean isDocumentUri = DocumentFile.isDocumentUri(MyApp.getInstance(), backupFileUri);
        AppLog.d("isDocumentUri: " + isDocumentUri);

        InputStream inputStream = getBackupFileInputStream(backupFileUri);
        AppLog.d("inputStream: " + inputStream);

        String backupFileName = getBackupFilename(backupFileUri);
        AppLog.d("backupFileName: " + backupFileName);

        OutputStream outputStream = null;

        // to SAF
        if (newStorageTreeUriString != null) {
            AppLog.d("should use SAF for output stream");

            Uri newStorageTreeUri = Uri.parse(newStorageTreeUriString);
            if (newStorageTreeUri != null) {
                DocumentFile newStorageDf = DocumentFile.fromTreeUri(MyApp.getInstance(), newStorageTreeUri);

                if (newStorageDf != null) {
                    DocumentFile newStorageDiaroBackupDf = newStorageDf.findFile("Diaro").findFile(DIR_BACKUP);
                    // Create '/backup' folder if does not exist
                    if (newStorageDiaroBackupDf == null) {
                        newStorageDiaroBackupDf = newStorageDf.createDirectory(DIR_BACKUP);
                    }

                    if (newStorageDiaroBackupDf != null) {
                        DocumentFile backupFileDf = newStorageDiaroBackupDf.findFile(backupFileName);

                        // If backup file with the same name exists
                        if (backupFileDf != null && backupFileDf.exists()) {
                            backupFileDf.delete();
                            AppLog.d("DELETED backupFileName: " + backupFileName);
                        }

                        String mimeType = MyApp.getInstance().getContentResolver().getType(backupFileUri);
                        if (mimeType == null) {
                            if (Static.getFileExtension(backupFileName).equals("zip")) {
                                mimeType = "application/zip";
                            } else if (Static.getFileExtension(backupFileName).equals("xml")) {
                                mimeType = "application/xml";
                            }
                        }
                        AppLog.d("mimeType: " + mimeType);

                        DocumentFile newBackupFileDf = newStorageDiaroBackupDf.createFile(mimeType, backupFileName);
                        AppLog.d("newBackupFileDf: " + newBackupFileDf);

                        Uri newBackupFileUri = newBackupFileDf.getUri();
                        AppLog.d("newBackupFileUri: " + newBackupFileUri);

                        outputStream = MyApp.getInstance().getContentResolver().openOutputStream(newBackupFileUri);
                    } else {
                        Static.showToastError("Could not backup create directory at " + newStorageTreeUri + ", " + newStorageDf.getName());
                    }


                } else {
                    Static.showToastError("DocumentFile.fromTreeUri is null" + newStorageTreeUri);
                }


            }
        }
        // to NoSAF
        else if (newStoragePath != null) {
            AppLog.d("should not use SAF for output stream");

            String diaroBackupDirPath = getDiaroBackupDirPathByStorage(newStoragePath);
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                diaroBackupDirPath = getDiaroBackupDirPath();
            }
            AppLog.d("diaroBackupDirPath: " + diaroBackupDirPath);

            boolean dirCreated = new File(diaroBackupDirPath).mkdirs();
            AppLog.d("dirCreated: " + dirCreated);

            File toFile = new File(diaroBackupDirPath + "/" + backupFileName);
            boolean fileCreated = toFile.createNewFile();
            AppLog.d("fileCreated: " + fileCreated);

            outputStream = new FileOutputStream(toFile);
        }

        AppLog.d("inputStream: " + inputStream + ", outputStream: " + outputStream);

        if (outputStream != null) {
            // Copy contents
            IOUtils.copy(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
            AppLog.d("Streams closed");
        }
    }

    public static String getReplacedStorageTreePath(String treePath) {
        String replaceTreePath = treePath.replace("tree", "storage");
        if (replaceTreePath.endsWith(":")) {
            // Delete ':' at the end
            replaceTreePath = replaceTreePath.substring(0, replaceTreePath.length() - 1);
        }
        return replaceTreePath;
    }

    public static Uri getStorageTreeUriFromPersistedPermissions(String storagePath) {
        AppLog.d("storagePath: " + storagePath);

        List<UriPermission> persistedUriPermissions = MyApp.getInstance().getContentResolver().getPersistedUriPermissions();

        AppLog.d("persistedUriPermissions: " + persistedUriPermissions);
        for (final UriPermission uriPermission : persistedUriPermissions) {
            String treePath = uriPermission.getUri().getPath();
            AppLog.d("treePath: " + treePath);

            String replacedStorageTreePath = PermanentStorageUtils.getReplacedStorageTreePath(treePath);
            if (replacedStorageTreePath.equals(storagePath)) {
                AppLog.d("has permission for this storagePath");

//                DocumentFile df = DocumentFile.fromTreeUri(MyApp.getInstance(),uriPermission.getUri());
//                AppLog.d("df.exists(): " + df.exists() + ", df.canWrite(): " + df.canWrite());

                return uriPermission.getUri();
            }
        }
        return null;
    }
}