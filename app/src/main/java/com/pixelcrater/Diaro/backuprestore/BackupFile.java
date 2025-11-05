package com.pixelcrater.Diaro.backuprestore;

import android.net.Uri;

public class BackupFile {

    public String filename;
    public Uri fileUri;
    public String fileSize;
    public long lastModified;

    public BackupFile(String filename, Uri fileUri, String fileSize, long lastModified) {
        this.filename = filename;
        this.fileUri = fileUri;
        this.fileSize = fileSize;
        this.lastModified = lastModified;
    }
}
