package com.pixelcrater.Diaro.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.pixelcrater.Diaro.storage.Tables;

import org.json.JSONObject;

public class LocationInfo {

    public String uid;
    public String title;
    public String address;
    public String latitude;
    public String longitude;
    public int zoom;
    public int entriesCount;

    public LocationInfo(String uid, String title, String address, String latitude, String longitude, int zoom) {
        setUid(uid);
        setTitle(title);
        setAddress(address);
        setLatitude(latitude);
        setLongitude(longitude);
        setZoom(zoom);
    }

    public LocationInfo(Cursor cursor) {
        setUid(cursor.getString(cursor.getColumnIndex(Tables.KEY_UID)));
        setTitle(cursor.getString(cursor.getColumnIndex(Tables.KEY_LOCATION_TITLE)));
        setAddress(cursor.getString(cursor.getColumnIndex(Tables.KEY_LOCATION_ADDRESS)));
        setLatitude(cursor.getString(cursor.getColumnIndex(Tables.KEY_LOCATION_LATITUDE)));
        setLongitude(cursor.getString(cursor.getColumnIndex(Tables.KEY_LOCATION_LONGITUDE)));
        setZoom(cursor.getInt(cursor.getColumnIndex(Tables.KEY_LOCATION_ZOOM)));
        setEntriesCount(cursor.getInt(cursor.getColumnIndex("entries_count")));
    }

    public static String createJsonString(Cursor cursor) throws Exception {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(Tables.KEY_UID, cursor.getString(cursor.getColumnIndex(Tables.KEY_UID)));
        jsonObject.put(Tables.KEY_LOCATION_TITLE, cursor.getString(cursor.getColumnIndex(Tables.KEY_LOCATION_TITLE)));
        jsonObject.put(Tables.KEY_LOCATION_ADDRESS, cursor.getString(cursor.getColumnIndex(Tables.KEY_LOCATION_ADDRESS)));
        jsonObject.put(Tables.KEY_LOCATION_LATITUDE, cursor.getString(cursor.getColumnIndex(Tables.KEY_LOCATION_LATITUDE)));
        jsonObject.put(Tables.KEY_LOCATION_LONGITUDE, cursor.getString(cursor.getColumnIndex(Tables.KEY_LOCATION_LONGITUDE)));
        jsonObject.put(Tables.KEY_LOCATION_ZOOM, cursor.getInt(cursor.getColumnIndex(Tables.KEY_LOCATION_ZOOM)));

        return jsonObject.toString();
    }

    public static ContentValues createRowCvFromJsonString(String jsonString) throws Exception {
        JSONObject jsonObject = new JSONObject(jsonString);

        ContentValues cv = new ContentValues();

        cv.put(Tables.KEY_UID, jsonObject.getString(Tables.KEY_UID));

        if (jsonObject.has(Tables.KEY_LOCATION_TITLE)) {
            cv.put(Tables.KEY_LOCATION_TITLE, jsonObject.getString(Tables.KEY_LOCATION_TITLE));
        }
        if (jsonObject.has(Tables.KEY_LOCATION_ADDRESS)) {
            cv.put(Tables.KEY_LOCATION_ADDRESS, jsonObject.getString(Tables.KEY_LOCATION_ADDRESS));
        }
        if (jsonObject.has(Tables.KEY_LOCATION_LATITUDE)) {
            cv.put(Tables.KEY_LOCATION_LATITUDE, jsonObject.getString(Tables.KEY_LOCATION_LATITUDE));
        }
        if (jsonObject.has(Tables.KEY_LOCATION_LONGITUDE)) {
            cv.put(Tables.KEY_LOCATION_LONGITUDE, jsonObject.getString(Tables.KEY_LOCATION_LONGITUDE));
        }
        if (jsonObject.has(Tables.KEY_LOCATION_ZOOM)) {
            cv.put(Tables.KEY_LOCATION_ZOOM, jsonObject.getInt(Tables.KEY_LOCATION_ZOOM));
        }

        return cv;
    }

    public void setUid(String uid) {
        this.uid = uid == null ? "" : uid;
    }

    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }

    public void setAddress(String address) {
        this.address = address == null ? "" : address;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude == null ? "" : latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude == null ? "" : longitude;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    public String getLocationTitle() {
        String locationTitle = title;
        if (locationTitle.equals("")) {
            locationTitle = address;
        }
        if (locationTitle.equals("") && !latitude.equals("") && !longitude.equals("")) {
            locationTitle = latitude + ", " + longitude;
        }

        return locationTitle;
    }

    public void setEntriesCount(int entriesCount) {
        this.entriesCount = entriesCount;
    }
}
