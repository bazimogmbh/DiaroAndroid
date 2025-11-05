package com.pixelcrater.Diaro.backuprestore;

import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.KeyValuePair;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtility {

    public static void zipDirectory(ArrayList<KeyValuePair> zipFilesArrayList, File zip) throws Exception {
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip));

        // Add files/folders to ZIP file
        for (KeyValuePair zipFileKeyValuePair : zipFilesArrayList) {
            File fileToAdd = new File(zipFileKeyValuePair.key);
            File base = new File(zipFileKeyValuePair.value);

            addFileToZip(fileToAdd, base, zos);
        }
        zos.close();
    }

    private static void addFileToZip(File fileToAdd, File base, ZipOutputStream zos) throws Exception {
//		AppLod.d("ZipUtility addFileToZip() fileToAdd.getPath(): "+fileToAdd.getPath());
//		AppLod.d("ZipUtility addFileToZip() base.getPath(): "+base.getPath());

        if (fileToAdd.isDirectory()) {
            addFolderToZip(fileToAdd, base, zos);
        } else {
            byte[] buffer = new byte[8192];
            int read = 0;

            FileInputStream in = new FileInputStream(fileToAdd);
            ZipEntry entry = new ZipEntry(fileToAdd.getPath().substring(base.getPath().length() + 1));
            zos.putNextEntry(entry);

            while (-1 != (read = in.read(buffer))) {
                zos.write(buffer, 0, read);
            }
            in.close();
        }
    }

    private static void addFolderToZip(File directory, File base, ZipOutputStream zos) throws Exception {
//		AppLod.d("ZipUtility addFolderToZip() directory.getPath(): "+directory.getPath());

        try {
            for (File file : directory.listFiles()) {
                addFileToZip(file, base, zos);
            }
        } catch (Exception e) {
            throw new ZipException("addFolderToZip() e: " + e);
        }
    }

    public static void unzip(InputStream in, File extractTo) throws IOException {
        AppLog.d("in: " + in + ", extractTo.getPath(): " + extractTo.getPath());

        ZipInputStream zis = new ZipInputStream(in);

        ZipEntry zipEntry;
        // Iterate through each item in the stream. The get next
        // entry call will return a ZipEntry for each file in the stream
        while ((zipEntry = zis.getNextEntry()) != null) {
            AppLog.d(String.format("Entry: %s len %d added %TD", zipEntry.getName(), zipEntry.getSize(), new Date(zipEntry.getTime())));

            File file = new File(extractTo, zipEntry.getName());
            String canonicalPath = file.getCanonicalPath();

            if (!canonicalPath.startsWith(extractTo.getAbsolutePath())) {
                // SecurityException
                throw new SecurityException();
            }

            if (zipEntry.isDirectory() && !file.exists()) {
                file.mkdirs();
            } else {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }

                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));

                byte[] buffer = new byte[8192];
                int read;

                while (-1 != (read = zis.read(buffer))) {
                    out.write(buffer, 0, read);
                }

                out.close();
            }
            zis.closeEntry();
        }
        zis.close();
    }
}
