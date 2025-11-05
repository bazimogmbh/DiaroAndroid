package com.pixelcrater.Diaro.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.pixelcrater.Diaro.storage.Tables;

import org.json.JSONObject;

public class FolderInfo {

    public String uid;
    public String title;
    public String color;
    public String pattern;
    public int entriesCount;

    public FolderInfo(String uid, String title, String color) {
        this.uid = uid;
        this.title = title;
        this.color = color;
    }

    public FolderInfo(Cursor cursor) {
        setUid(cursor.getString(cursor.getColumnIndex(Tables.KEY_UID)));
        setTitle(cursor.getString(cursor.getColumnIndex(Tables.KEY_FOLDER_TITLE)));
        setColor(cursor.getString(cursor.getColumnIndex(Tables.KEY_FOLDER_COLOR)));
        setPattern(cursor.getString(cursor.getColumnIndex(Tables.KEY_FOLDER_PATTERN)));
        setEntriesCount(cursor.getInt(cursor.getColumnIndex("entries_count")));
    }

    public static String createJsonString(Cursor cursor) throws Exception {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(Tables.KEY_UID, cursor.getString(cursor.getColumnIndex(Tables.KEY_UID)));
        jsonObject.put(Tables.KEY_FOLDER_TITLE, cursor.getString(cursor.getColumnIndex(Tables.KEY_FOLDER_TITLE)));
        jsonObject.put(Tables.KEY_FOLDER_COLOR, cursor.getString(cursor.getColumnIndex(Tables.KEY_FOLDER_COLOR)));
        jsonObject.put(Tables.KEY_FOLDER_PATTERN, cursor.getString(cursor.getColumnIndex(Tables.KEY_FOLDER_PATTERN)));

        return jsonObject.toString();
    }

    public static ContentValues createRowCvFromJsonString(String jsonString) throws Exception {
        JSONObject jsonObject = new JSONObject(jsonString);

        ContentValues cv = new ContentValues();

        cv.put(Tables.KEY_UID, jsonObject.getString(Tables.KEY_UID));
        if (jsonObject.has(Tables.KEY_FOLDER_TITLE)) {
            cv.put(Tables.KEY_FOLDER_TITLE, jsonObject.getString(Tables.KEY_FOLDER_TITLE));
        }
        if (jsonObject.has(Tables.KEY_FOLDER_COLOR)) {
            cv.put(Tables.KEY_FOLDER_COLOR, jsonObject.getString(Tables.KEY_FOLDER_COLOR));
        }
        if (jsonObject.has(Tables.KEY_FOLDER_PATTERN)) {
            cv.put(Tables.KEY_FOLDER_PATTERN, jsonObject.getString(Tables.KEY_FOLDER_PATTERN));
        }

        return cv;
    }

    public void setUid(String uid) {
        this.uid = uid == null ? "" : uid;
    }

    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }

    public void setColor(String color) {
        this.color = color == null ? "" : color;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern == null ? "" : pattern;
    }

    public void setEntriesCount(int entriesCount) {
        this.entriesCount = entriesCount;
    }
}
