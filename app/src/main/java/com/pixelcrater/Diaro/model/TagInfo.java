package com.pixelcrater.Diaro.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.pixelcrater.Diaro.storage.Tables;

import org.json.JSONObject;

public class TagInfo {

    public String uid;
    public String title;
    public int entriesCount;

    public TagInfo(String uid, String title) {
        this.uid = uid;
        this.title = title;
    }

    public TagInfo(Cursor cursor) {
        setUid(cursor.getString(cursor.getColumnIndex(Tables.KEY_UID)));
        setTitle(cursor.getString(cursor.getColumnIndex(Tables.KEY_TAG_TITLE)));
        setEntriesCount(cursor.getInt(cursor.getColumnIndex("entries_count")));
    }

    public static String createJsonString(Cursor cursor) throws Exception {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(Tables.KEY_UID, cursor.getString(cursor.getColumnIndex(Tables.KEY_UID)));
        jsonObject.put(Tables.KEY_TAG_TITLE, cursor.getString(cursor.getColumnIndex(Tables.KEY_TAG_TITLE)));

        return jsonObject.toString();
    }

    public static ContentValues createRowCvFromJsonString(String jsonString) throws Exception {
        JSONObject jsonObject = new JSONObject(jsonString);

        ContentValues cv = new ContentValues();

        cv.put(Tables.KEY_UID, jsonObject.getString(Tables.KEY_UID));

        if (jsonObject.has(Tables.KEY_TAG_TITLE)) {
            cv.put(Tables.KEY_TAG_TITLE, jsonObject.getString(Tables.KEY_TAG_TITLE));
        }

        return cv;
    }

    public void setUid(String uid) {
        this.uid = uid == null ? "" : uid;
    }

    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }

    public void setEntriesCount(int entriesCount) {
        this.entriesCount = entriesCount;
    }
}
