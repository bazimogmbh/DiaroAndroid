package com.pixelcrater.Diaro.locations;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import com.pixelcrater.Diaro.utils.AppLog;

import java.util.List;

public class GeocodeFromLocationNameAsync extends AsyncTask<Object, String, Boolean> {

    private Context mContext;
    private LocationWrapper mLocationWrapper;
    private String mLocationName;
    private Address address = null;

    public GeocodeFromLocationNameAsync(Context context, LocationWrapper locationWrapper, String locationName) {
        mContext = context;
        mLocationWrapper = locationWrapper;
        mLocationName = locationName;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        Geocoder geocoder = new Geocoder(mContext);
        try {
//            AppLog.d("mLocationName: " + mLocationName);

            List<Address> listAddress = geocoder.getFromLocationName(mLocationName, 1);
//            AppLog.d("listAddress: " + listAddress);

            if (listAddress.size() > 0) {
                address = listAddress.get(0);
            }
            AppLog.d("address: " + address);
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        try {
            double latitude = 0;
            double longitude = 0;

            if (address != null) {
                latitude = address.getLatitude();
                longitude = address.getLongitude();
            }

            mLocationWrapper.onAddressGeocode(address, latitude, longitude);
        } catch (Exception e) {
        }
    }
}
