package com.pixelcrater.Diaro.entries.attachments;

import static com.pixelcrater.Diaro.config.GlobalConstants.DROPBOX_PATH_MEDIA;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.config.GlobalConstants;
import com.pixelcrater.Diaro.model.AttachmentInfo;
import com.pixelcrater.Diaro.model.PersistanceHelper;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.storage.dropbox.DropboxLocalHelper;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;
import com.pixelcrater.Diaro.utils.storage.StorageUtils;

import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;

public class AttachmentsStatic {

    public static void deleteAttachments(ArrayList<String> fileNames, String type) {
        for (String filename : fileNames) {
            AppLog.e("delete media file" + filename);
            deleteAttachmentFileFromDevice(type, filename);
        }

        MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners();
    }

    public static void deleteAttachments(ArrayList<AttachmentInfo> attachmentsArrayList) throws Exception {
        for (AttachmentInfo attachmentInfo : attachmentsArrayList) {
            MyApp.getInstance().storageMgr.deleteRowByUid(Tables.TABLE_ATTACHMENTS, attachmentInfo.uid);
            deleteAttachmentFileFromDevice(attachmentInfo.type, attachmentInfo.filename);
            deleteAttachmentFileFromDbxFs(attachmentInfo.type, attachmentInfo.filename);
        }

        MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners();
    }

    public static void deleteAttachmentFileFromDevice(String attachmentType, String filename) {
        AppLog.d("attachmentType: " + attachmentType + ", filename: " + filename);

        // Delete file from SD card
        StorageUtils.deleteFileOrDirectory(new File(AppLifetimeStorageUtils.getMediaDirPath() + "/" + attachmentType + "/" + filename));
    }

    public static void deleteAttachmentFileFromDbxFs(String attachmentType, String filename) {
        AppLog.d("attachmentType: " + attachmentType + ", filename: " + filename);

        if (MyApp.getInstance().storageMgr.isStorageDropbox()) {
            DropboxLocalHelper.markFileForDeletion(String.format("%s/%s/%s", DROPBOX_PATH_MEDIA, attachmentType, filename));
        }
    }

    public static void deleteAllAttachmentsFiles() {
        Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getAttachmentsCursor("", null);
        while (cursor.moveToNext()) {
            AttachmentInfo attachmentInfo = new AttachmentInfo(cursor);
            deleteAttachmentFileFromDevice(attachmentInfo.type, attachmentInfo.filename);
            deleteAttachmentFileFromDbxFs(attachmentInfo.type, attachmentInfo.filename);
        }
        cursor.close();

        MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners();
    }



    public static File saveAttachment(String entryUid, String filePath, String attachmentType, boolean move) throws Exception {
        AppLog.i(" saveAttachment fileUri: " + filePath + ", move: " + move);

        File attachmentFile = new File(filePath);
        DateTime dt = new DateTime();
        String millis = String.valueOf(dt.getMillis());
        String filename = attachmentType + "_" + dt.toString("yyyyMMdd") + "_" + millis.substring(millis.length() - 6) + "." + Static.getFileExtension(attachmentFile.getName());

        // Check if there is no file with the same filename in attachments table
        filename = getNewAttachmentFilenameIfExists(filename, attachmentType);
//		AppLog.d("filename: " + filename);

        File targetFile = new File(AppLifetimeStorageUtils.getMediaDirPath() + "/" + attachmentType + "/" + filename);
        if (move) {
            // Move file to attachments directory
            Static.moveFileOrDirectory(attachmentFile, targetFile);
        } else {
            // Copy file to attachments directory
            Static.copyFileOrDirectory(attachmentFile, targetFile);
        }

        // Compress the photo and preserve exif tag
        AppLog.i(String.format("Compressing photo: %s", targetFile.getPath()));
        try {
            StorageUtils.compressPhoto(targetFile.getPath(),  GlobalConstants.IMAGE_MAX_W_H, GlobalConstants.IMAGE_COMPRESS_QUALITY);
        } catch (Exception ex) {
            Static.showToastError("Error compressing " +  ex.getMessage());
            AppLog.e("Error compressing " + ex.getMessage());
        }

        // Calculate attachment position
        long position = getMaxAttachmentPosition(entryUid, attachmentType) + 1;
        AppLog.d("position: " + position);

        // Insert attachment to storage
        AttachmentInfo attachmentInfo = new AttachmentInfo (entryUid, attachmentType, filename, position );
        PersistanceHelper.saveAttachment(attachmentInfo);

        return targetFile;
    }

    public static File saveAttachment2(String entryUid, Uri fileURI, String attachmentType)  {
        String fileName = StorageUtils.getFilenameFromUri(fileURI);

        DateTime dt = new DateTime();
        String millis = String.valueOf(dt.getMillis());
        String filename = attachmentType + "_" + dt.toString("yyyyMMdd") + "_" + millis.substring(millis.length() - 6) + "." + Static.getFileExtension(fileName);

        // Check if there is no file with the same filename in attachments table
        filename = getNewAttachmentFilenameIfExists(filename, attachmentType);

        File targetFile = new File(AppLifetimeStorageUtils.getMediaDirPath() + "/" + attachmentType + "/" + filename);
       // Write the image to our Diaro storage
        StorageUtils.writeImageUriToFile(fileURI, targetFile);

        // Compress the photo and preserve exif tag
        AppLog.i(String.format("Compressing photo: %s", targetFile.getPath()));
        try {
            StorageUtils.compressPhoto(targetFile.getPath(),  GlobalConstants.IMAGE_MAX_W_H, GlobalConstants.IMAGE_COMPRESS_QUALITY);
        } catch (Exception ex) {
            Static.showToastError("Error compressing " +  ex.getMessage());
            AppLog.e("Error compressing " + ex.getMessage());
        }

        // Calculate attachment position
        long position = getMaxAttachmentPosition(entryUid, attachmentType) + 1;
        AppLog.d("position: " + position);

        // Insert attachment to storage
        AttachmentInfo attachmentInfo = new AttachmentInfo (entryUid, attachmentType, filename, position );
        PersistanceHelper.saveAttachment(attachmentInfo);

        return targetFile;
    }

    public static ArrayList<AttachmentInfo> getEntryAttachmentsArrayList(String entryUid, String attachmentType) {
        ArrayList<AttachmentInfo> entryAttachmentsArrayList = new ArrayList<>();
        if (entryUid == null) {
            entryUid = "";
        }

        String ANDSQL = "";

        if(entryUid.isEmpty()){
            if(attachmentType != null && !attachmentType.isEmpty())
                ANDSQL = " AND type = '" + attachmentType + "'";
        } else {
            if(attachmentType != null && !attachmentType.isEmpty()){
                ANDSQL =   " AND entry_uid  = '"+ entryUid +  "'" + "AND type = '" + attachmentType + "'";
            } else
                ANDSQL =   " AND entry_uid  = '"+ entryUid +  "'" ;

        }
     //   AppLog.e("ANDSQL " + ANDSQL);
        Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getAttachmentsCursor(ANDSQL, null);
        AppLog.e("Photos cursor count " + cursor.getCount());
        while (cursor.moveToNext()) {
            AttachmentInfo attachmentInfo = new AttachmentInfo(cursor);
//            AppLog.d("entryUid: " + entryUid + ", attachmentInfo.entryUid: " + attachmentInfo.entryUid + ", attachmentInfo.type: " + attachmentInfo.type);
            entryAttachmentsArrayList.add(attachmentInfo);

        }
        cursor.close();

        AppLog.e("Photos count " + entryAttachmentsArrayList.size());
        return entryAttachmentsArrayList;
    }

    public static void setPhotoAsPrimary(View view, String photoUid, String entryUid) {
        // Update entry primary_photo_uid field
        ContentValues cv = new ContentValues();
        cv.put(Tables.KEY_ENTRY_PRIMARY_PHOTO_UID, photoUid);
        MyApp.getInstance().storageMgr.updateRowByUid(Tables.TABLE_ENTRIES, entryUid, cv);

        Snackbar.make(view, R.string.primary_photo_changed, Snackbar.LENGTH_SHORT).show();
    }

    public static String getNewAttachmentFilenameIfExists(String filename, String attachmentType) {
        if (filename == null || attachmentType == null) {
            return null;
        }

        String filenameWithoutExtension = Static.getFilenameWithoutExtension(filename);
        String fileExtension = Static.getFileExtension(filename);

        int i = 1;
        while (true) {
            String[] whereArgs = new String[1];
            whereArgs[0] = filename;

            int count = MyApp.getInstance().storageMgr.getSQLiteAdapter().getRowsCount(Tables.TABLE_ATTACHMENTS,
                    "WHERE " + Tables.KEY_ATTACHMENT_TYPE + "='" + attachmentType + "'" + " AND " + Tables.KEY_ATTACHMENT_FILENAME + "=?", whereArgs);

            if (count > 0) {
                filename = filenameWithoutExtension + "_" + i + "." + fileExtension;
                i++;
            } else break;
        }

        return filename;
    }

    /**
     * Returns max position of provided entryUid and attachmentType
     */
    public static long getMaxAttachmentPosition(String entryUid, String attachmentType) {
        if (entryUid == null || attachmentType == null) {
            return 0;
        }

        long maxPosition = 0;

        String[] whereArgs = new String[1];
        whereArgs[0] = entryUid;

        String position = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSingleRowColumnValue(Tables.TABLE_ATTACHMENTS, Tables.KEY_ATTACHMENT_POSITION,
                        "WHERE " + Tables.KEY_ATTACHMENT_TYPE + "='" + attachmentType + "'" + " AND " + Tables.KEY_ATTACHMENT_ENTRY_UID + "=?" + " ORDER BY " + Tables.KEY_ATTACHMENT_POSITION + " DESC", whereArgs);
        if (!position.equals("")) {
            maxPosition = Long.parseLong(position);
        }

        return maxPosition;
    }

    public static void deleteAttachmentsOfNotExistingEntries() {
//		AppLog.d("");

        Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getUnassignedAttachmentsCursor();

        while (cursor.moveToNext()) {
            AttachmentInfo attachmentInfo = new AttachmentInfo(cursor);

            try {
                // Delete attachment
                ArrayList<AttachmentInfo> attachmentsArrayList = new ArrayList<>();
                attachmentsArrayList.add(attachmentInfo);
                deleteAttachments(attachmentsArrayList);
            } catch (Exception e) {
            }
        }
        cursor.close();
    }

}
