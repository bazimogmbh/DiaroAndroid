package com.pixelcrater.Diaro.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;

import org.json.JSONObject;

public class AttachmentInfo {

    public String uid;
    public String entryUid;
    public String type;
    public String filename;
    public String fileSyncId;
    public Long position;

    private AttachmentInfo() {
    }

    public AttachmentInfo(String entryUid, String type, String filename, long position) {
        this.uid = Static.generateRandomUid();
        this.entryUid = entryUid;
        this.type = type;
        this.filename = filename;
        this.position = position;
    }

    public AttachmentInfo(Cursor cursor) {
        setUid(cursor.getString(cursor.getColumnIndex(Tables.KEY_UID)));
        setEntryUid(cursor.getString(cursor.getColumnIndex(Tables.KEY_ATTACHMENT_ENTRY_UID)));
        setType(cursor.getString(cursor.getColumnIndex(Tables.KEY_ATTACHMENT_TYPE)));
        setFilename(cursor.getString(cursor.getColumnIndex(Tables.KEY_ATTACHMENT_FILENAME)));
        setFileSyncId(cursor.getString(cursor.getColumnIndex(Tables.KEY_ATTACHMENT_FILE_SYNC_ID)));
        setPosition(cursor.getInt(cursor.getColumnIndex(Tables.KEY_ATTACHMENT_POSITION)));
    }

    public static String createJsonString(Cursor cursor) throws Exception {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(Tables.KEY_UID, cursor.getString(cursor.getColumnIndex(Tables.KEY_UID)));
        jsonObject.put(Tables.KEY_ATTACHMENT_ENTRY_UID, cursor.getString(cursor.getColumnIndex(Tables.KEY_ATTACHMENT_ENTRY_UID)));
        jsonObject.put(Tables.KEY_ATTACHMENT_TYPE, cursor.getString(cursor.getColumnIndex(Tables.KEY_ATTACHMENT_TYPE)));
        jsonObject.put(Tables.KEY_ATTACHMENT_FILENAME, cursor.getString(cursor.getColumnIndex(Tables.KEY_ATTACHMENT_FILENAME)));
        jsonObject.put(Tables.KEY_ATTACHMENT_POSITION, cursor.getLong(cursor.getColumnIndex(Tables.KEY_ATTACHMENT_POSITION)));

        return jsonObject.toString();
    }

    public static ContentValues createRowCvFromJsonString(String jsonString) throws Exception {
        JSONObject jsonObject = new JSONObject(jsonString);

        ContentValues cv = new ContentValues();

        cv.put(Tables.KEY_UID, jsonObject.getString(Tables.KEY_UID));

        if (jsonObject.has(Tables.KEY_ATTACHMENT_ENTRY_UID)) {
            cv.put(Tables.KEY_ATTACHMENT_ENTRY_UID, jsonObject.getString(Tables.KEY_ATTACHMENT_ENTRY_UID));
        }
        if (jsonObject.has(Tables.KEY_ATTACHMENT_TYPE)) {
            cv.put(Tables.KEY_ATTACHMENT_TYPE, jsonObject.getString(Tables.KEY_ATTACHMENT_TYPE));
        }
        if (jsonObject.has(Tables.KEY_ATTACHMENT_FILENAME)) {
            cv.put(Tables.KEY_ATTACHMENT_FILENAME, jsonObject.getString(Tables.KEY_ATTACHMENT_FILENAME));
        }
        if (jsonObject.has(Tables.KEY_ATTACHMENT_POSITION)) {
            cv.put(Tables.KEY_ATTACHMENT_POSITION, jsonObject.getLong(Tables.KEY_ATTACHMENT_POSITION));
        }

        return cv;
    }

    public void setUid(String uid) {
        this.uid = uid == null ? "" : uid;
    }

    public void setEntryUid(String entryUid) {
        this.entryUid = entryUid == null ? "" : entryUid;
    }

    public void setType(String type) {
        this.type = type == null ? "" : type;
    }

    public void setFilename(String filename) {
        this.filename = filename == null ? "" : filename;
    }

    public void setFileSyncId(String fileSyncId) {
        this.fileSyncId = fileSyncId == null ? "" : fileSyncId;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public String getFilePath() {
        return AppLifetimeStorageUtils.getMediaPhotosDirPath() + "/" + filename;
    }
}
