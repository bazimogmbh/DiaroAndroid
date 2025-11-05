package com.pixelcrater.Diaro.locations;

import android.content.ContentValues;
import android.database.Cursor;
import android.location.Address;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.AppLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class LocationsStatic {

    public static void deleteLocationInBackground(final String locationUid) {
        MyApp.executeInBackground(() -> {
            MyApp.getInstance().storageMgr.deleteRowByUid(Tables.TABLE_LOCATIONS, locationUid);

            // Clear location_uid for entries
            ContentValues cv = new ContentValues();
            cv.put(Tables.KEY_ENTRY_LOCATION_UID, "");

            String[] whereArgs = new String[1];
            whereArgs[0] = locationUid;

            MyApp.getInstance().storageMgr.updateRows(Tables.TABLE_ENTRIES, "WHERE " + Tables.KEY_ENTRY_LOCATION_UID + "=?", whereArgs, cv);
        });
    }

    public static void deleteSelectedLocationsInBackground(final String locationsUidList) {
        MyApp.executeInBackground(() -> {
            String[] locationsArray = locationsUidList.split(",");
            for (String location : locationsArray) {
                MyApp.getInstance().storageMgr.deleteRowByUid(Tables.TABLE_LOCATIONS, location);
            }

            // Clear locations for entries
            ContentValues cv = new ContentValues();
            cv.put(Tables.KEY_ENTRY_LOCATION_UID, "");

            String[] whereArgs = new String[2];
            whereArgs[0] = "";

            for (int i = 0; i < locationsArray.length; i++) {
                if (i == locationsArray.length - 1) {
                    whereArgs[0] += "'" + locationsArray[i] + "'";
                } else {
                    whereArgs[0] += "'" + locationsArray[i] + "',";
                }
            }

            MyApp.getInstance().storageMgr.updateRows(Tables.TABLE_ENTRIES, "WHERE " + Tables.KEY_ENTRY_LOCATION_UID + " IN " + "(" + whereArgs[0] + ") " + "AND " + Tables.KEY_ENTRY_LOCATION_UID + "!=?", new String[]{""}, cv);
        });
    }

    public static void insertLocationAndUpdateEntry(String location, String locationCoords, String entryUid) {

        AppLog.d("location: " + location + ", locationCoords: " + locationCoords +   ", entryUid: " + entryUid);


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

            Cursor locationCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSingleRowCursor(Tables.TABLE_LOCATIONS, "WHERE " + Tables.KEY_LOCATION_ADDRESS + "=?", whereArgs);

            String locationUid = null;
            if (locationCursor.getCount() == 1) {
                locationUid = locationCursor.getString(locationCursor.getColumnIndex(Tables.KEY_UID));
            } else {
                ContentValues cv = new ContentValues();

                // Generate uid
                cv.put(Tables.KEY_UID, Static.generateRandomUid());
                cv.put(Tables.KEY_LOCATION_ADDRESS, location);
                cv.put(Tables.KEY_LOCATION_LATITUDE, latitude);
                cv.put(Tables.KEY_LOCATION_LONGITUDE, longitude);
                cv.put(Tables.KEY_LOCATION_ZOOM, 6);

                // Insert location
                String uid = MyApp.getInstance().storageMgr.insertRow(Tables.TABLE_LOCATIONS, cv);
                if (uid != null) {
                    locationUid = uid;
                }
            }

            AppLog.d("locationUid: " + locationUid);

            if (locationUid != null) {
                // Update entry location_uid in database
                ContentValues entryCv = new ContentValues();
                entryCv.put(Tables.KEY_ENTRY_LOCATION_UID, locationUid);
                MyApp.getInstance().storageMgr.getSQLiteAdapter().updateRowByUid(Tables.TABLE_ENTRIES, entryUid, entryCv);
            }

            locationCursor.close();
        }
    }



    public static ArrayList<String> getActiveLocationsUidsArrayList() {
        String activeTags = MyApp.getInstance().prefs.getString(Prefs.PREF_ACTIVE_LOCATIONS, "");
        if (!activeTags.equals("")) {
            return new ArrayList<>(Arrays.asList(activeTags.split(",")));
        }
        return new ArrayList<>();
    }
}
