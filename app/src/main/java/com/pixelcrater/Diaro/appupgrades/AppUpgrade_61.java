package com.pixelcrater.Diaro.appupgrades;

import android.content.ContentValues;
import android.database.Cursor;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.AppLog;

import org.apache.commons.lang3.StringUtils;

public class AppUpgrade_61 {
    public AppUpgrade_61() {
        copyLocationsFromEntriesToLocationsTable();
    }

    private void copyLocationsFromEntriesToLocationsTable() {
        Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getRowsUidsCursor("diaro_entries", "", null);
        int entryUidColumnIndex = cursor.getColumnIndex("uid");

        while (cursor.moveToNext()) {
            if (!StringUtils.isEmpty(cursor.getString(entryUidColumnIndex))) {
                String location = MyApp.getInstance().storageMgr.getSQLiteAdapter() .getSingleRowColumnValueByUid("diaro_entries", "location",
                                cursor.getString(entryUidColumnIndex));

                String locationCoords = MyApp.getInstance().storageMgr.getSQLiteAdapter() .getSingleRowColumnValueByUid("diaro_entries", "location_coords",
                                cursor.getString(entryUidColumnIndex));

                insertLocationAndUpdateEntry(location, locationCoords,  cursor.getString(entryUidColumnIndex));
            }
        }
        cursor.close();
    }

    private void insertLocationAndUpdateEntry(String location, String locationCoords, String entryUid) {

        AppLog.d("location: " + location + ", locationCoords: " + locationCoords + ", entryUid: " + entryUid);

        if (!location.equals("") || !locationCoords.equals("")) {
            String latitude = "";
            String longitude = "";
            try {
                String[] coordsSplitted = locationCoords.split(",");
                latitude = coordsSplitted[0];
                longitude = coordsSplitted[1];
            } catch (Exception e) {
            }

            // Try to find this location by address
            String[] whereArgs = new String[1];
            whereArgs[0] = location;

            Cursor locationCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter()
                    .getSingleRowCursor("diaro_locations",
                            "WHERE address=?",
                            whereArgs);

            String locationUid = null;
            if (locationCursor.getCount() == 1) {
                locationUid = locationCursor.getString(locationCursor.getColumnIndex("uid"));
            } else {
                ContentValues cv = new ContentValues();

                // Generate uid
                cv.put("uid", Static.generateRandomUid());
                cv.put("address", location);
                cv.put("lat", latitude);
                cv.put("long", longitude);
                cv.put("zoom", 6);

                // Insert location
                String uid = MyApp.getInstance().storageMgr.insertRow("diaro_locations", cv);
                if (uid != null) {
                    locationUid = uid;
                }
            }

            AppLog.d("locationUid: " + locationUid);

            if (locationUid != null) {
                // Update entry location_uid in database
                ContentValues entryCv = new ContentValues();
                entryCv.put("location_uid", locationUid);
                MyApp.getInstance().storageMgr.getSQLiteAdapter().updateRowByUid("diaro_entries",
                        entryUid, entryCv);
            }

            locationCursor.close();
        }
    }
}
