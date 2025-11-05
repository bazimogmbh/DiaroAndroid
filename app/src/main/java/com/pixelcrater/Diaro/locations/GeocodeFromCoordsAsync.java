package com.pixelcrater.Diaro.locations;

import android.content.ContentValues;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.text.TextUtils;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.entries.viewedit.EntryFragment;
import com.pixelcrater.Diaro.model.LocationInfo;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.AppLog;

import java.util.List;

public class GeocodeFromCoordsAsync extends AsyncTask<Object, String, Boolean> {

    private LocationWrapper mLocationWrapper;
    private EntryFragment mEntryFragment;
    private double mLatitude;
    private double mLongitude;
    private Address address = null;
    private LocationInfo locationInfo;
    private boolean isNewLocation;

    public GeocodeFromCoordsAsync(LocationWrapper locationWrapper, double latitude, double longitude) {
        //		AppLog.d("latitude: " + latitude + ", longitude: " + longitude);
        mLocationWrapper = locationWrapper;
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public GeocodeFromCoordsAsync(EntryFragment entryFragment, double latitude, double longitude) {
        //		AppLog.d("latitude: " + latitude + ", longitude: " + longitude);
        mEntryFragment = entryFragment;
        mLatitude = latitude;
        mLongitude = longitude;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        Geocoder geocoder = new Geocoder(MyApp.getInstance());

        List<Address> listAddress = null;

        try {
            listAddress = geocoder.getFromLocation(mLatitude, mLongitude, 1);
//            AppLog.d("listAddress: " + listAddress);
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }

        if (listAddress != null && listAddress.size() > 0) {
            address = listAddress.get(0);
        }
        AppLog.d("address: " + address);

        if (mEntryFragment != null) {
            String formattedAddress = LocationUtils.getFormattedAddress(address);
            String formattedLatitude = LocationUtils.getFormattedCoord(mLatitude);
            String formattedLongitude = LocationUtils.getFormattedCoord(mLongitude);

            // Try to find this location in sqlite
            locationInfo = findLocationInSQLite(formattedAddress, formattedLatitude, formattedLongitude);

            if (locationInfo == null) {
                isNewLocation = true;
                locationInfo = new LocationInfo("", "", formattedAddress, formattedLatitude, formattedLongitude, 13);

            } else {
                // If the found location doesn't have the address set already, update it
                if (TextUtils.isEmpty(locationInfo.address) && !TextUtils.isEmpty(formattedAddress)) {
                    isNewLocation = true;
                    ContentValues cv = new ContentValues();
                    cv.put(Tables.KEY_LOCATION_ADDRESS, formattedAddress);
                    MyApp.getInstance().storageMgr.updateRowByUid(Tables.TABLE_LOCATIONS, locationInfo.uid, cv);
                    locationInfo.address = formattedAddress;
                }
            }
        }

        return true;
    }

    private LocationInfo findLocationInSQLite(String formattedAddress, String formattedLatitude, String formattedLongitude) {

        Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getLocationsCursor("", null, false);
        try {
            while (cursor.moveToNext()) {
                LocationInfo locationInfo = new LocationInfo(cursor);

                if ((!formattedAddress.equals("") && locationInfo.address.equals(formattedAddress)) || isNearbyCoords(locationInfo, formattedLatitude, formattedLongitude)) {
                    return locationInfo;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return null;
    }

    private boolean isNearbyCoords(LocationInfo locationInfo, String formattedLatitude, String formattedLongitude) {
        try {
            double lat1 = Double.parseDouble(locationInfo.latitude);
            double lon1 = Double.parseDouble(locationInfo.longitude);

            double lat2 = Double.parseDouble(formattedLatitude);
            double lon2 = Double.parseDouble(formattedLongitude);

            double accuracy = 0.001;
            if (Math.abs(lat1 - lat2) <= accuracy && Math.abs(lon1 - lon2) <= accuracy) {
                return true;
            }
        } catch (Exception e) {
            AppLog.e("Exception: " + e + "("+formattedLatitude +","+ formattedLongitude+")");
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
//		AppLog.d("address: " + address + ",  mLatitude: " + mLatitude + ", mLongitude: " + mLongitude);

        try {
            // Location edit
            if (mLocationWrapper != null) {
                mLocationWrapper.onAddressGeocode(address, mLatitude, mLongitude);
            }
            // Entry view/edit
            else if (mEntryFragment != null && locationInfo != null) {
                mEntryFragment.onAddressGeocode(locationInfo, isNewLocation);
            }
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }
    }
}
