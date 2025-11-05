package com.pixelcrater.Diaro.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.storage.Tables;

import org.json.JSONObject;

public class MoodInfo {

    public String uid;
    public String title;
    public String icon;
    public String color;
    public int weight;
    public int entriesCount;

    public MoodInfo(String uid, String title, String icon, int weight) {
        this.uid = uid;
        this.title = title;
        this.icon = icon;
        this.weight = weight;
    }

    public MoodInfo(Cursor cursor) {
        setUid(cursor.getString(cursor.getColumnIndex(Tables.KEY_UID)));
        setTitle(cursor.getString(cursor.getColumnIndex(Tables.KEY_MOOD_TITLE)));
        setColor(cursor.getString(cursor.getColumnIndex(Tables.KEY_MOOD_COLOR)));
        setIcon(cursor.getString(cursor.getColumnIndex(Tables.KEY_MOOD_ICON)));
        setWeight(cursor.getInt(cursor.getColumnIndex(Tables.KEY_MOOD_WEIGHT)));
        setEntriesCount(cursor.getInt(cursor.getColumnIndex("entries_count")));
    }

    public static String createJsonString(Cursor cursor) throws Exception {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(Tables.KEY_UID, cursor.getString(cursor.getColumnIndex(Tables.KEY_UID)));
        jsonObject.put(Tables.KEY_MOOD_TITLE, cursor.getString(cursor.getColumnIndex(Tables.KEY_MOOD_TITLE)));
        jsonObject.put(Tables.KEY_MOOD_COLOR, cursor.getString(cursor.getColumnIndex(Tables.KEY_MOOD_COLOR)));
        jsonObject.put(Tables.KEY_MOOD_ICON, cursor.getString(cursor.getColumnIndex(Tables.KEY_MOOD_ICON)));
        jsonObject.put(Tables.KEY_MOOD_WEIGHT, cursor.getString(cursor.getColumnIndex(Tables.KEY_MOOD_WEIGHT)));

        return jsonObject.toString();
    }

    public static ContentValues createRowCvFromJsonString(String jsonString) throws Exception {
        JSONObject jsonObject = new JSONObject(jsonString);

        ContentValues cv = new ContentValues();

        cv.put(Tables.KEY_UID, jsonObject.getString(Tables.KEY_UID));
        if (jsonObject.has(Tables.KEY_MOOD_TITLE)) {
            cv.put(Tables.KEY_MOOD_TITLE, jsonObject.getString(Tables.KEY_MOOD_TITLE));
        }
        if (jsonObject.has(Tables.KEY_MOOD_COLOR)) {
            cv.put(Tables.KEY_MOOD_COLOR, jsonObject.getString(Tables.KEY_MOOD_COLOR));
        }
        if (jsonObject.has(Tables.KEY_MOOD_ICON)) {
            cv.put(Tables.KEY_MOOD_ICON, jsonObject.getString(Tables.KEY_MOOD_ICON));
        }
        if (jsonObject.has(Tables.KEY_MOOD_WEIGHT)) {
            cv.put(Tables.KEY_MOOD_WEIGHT, jsonObject.getString(Tables.KEY_MOOD_WEIGHT));
        }

        return cv;
    }

    public void setUid(String uid) {
        this.uid = uid == null ? "" : uid;
    }

    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }

    public void setIcon(String icon) {
        this.icon = icon == null ? "" : icon;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setColor(String color) {
        this.color = color == null ? "" : color;
    }

    public void setEntriesCount(int entriesCount) {
        this.entriesCount = entriesCount;
    }


}
