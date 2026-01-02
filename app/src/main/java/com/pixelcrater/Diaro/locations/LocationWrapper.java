package com.pixelcrater.Diaro.locations;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.model.LocationInfo;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.PermissionsUtils;
import com.pixelcrater.Diaro.utils.Static;

import java.util.Locale;

public class LocationWrapper {

    public final EditText locationTitleEditText, searchMapEditText;
    public final TextView formattedAddressTextView, coordsTextView;
    public final ViewGroup mapView;
    public final EditText naAddressEditText;
    public final TextView naCoordsTextView;

    public boolean isMapAvailable = true;
    public String locationUid;
    private Activity mActivity;
    private LayoutInflater inflater;
    private LocationManager locationManager;
    private String stateTitle = "";
    private String stateAddress = "";
    private String stateLatitude = "";
    private String stateLongitude = "";
    private int stateZoom = 0;
    private GeocodeFromCoordsAsync geocodeFromCoordsAsync;
    private GeocodeFromLocationNameAsync geocodeFromLocationNameAsync;

    private LocationListener locationListener = new LocationListener() {

        public void onLocationChanged(Location location) {
            AppLog.d("location: " + location);
        }

        public void onProviderDisabled(String provider) {
            AppLog.d("provider: " + provider);
        }

        public void onProviderEnabled(String provider) {
            AppLog.d("provider: " + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            AppLog.d("provider: " + provider + ", status: " + status + ", extras: " + extras);
        }
    };

    public LocationWrapper(final Activity activity, Bundle savedInstanceState) {
        // Get intent extras
        Bundle extras = activity.getIntent().getExtras();
        locationUid = extras.getString("locationUid");
//		AppLog.d("locationUid: " + locationUid);

        mActivity = activity;
        inflater = activity.getLayoutInflater();

        // Get the location manager
        locationManager = (LocationManager) mActivity.getSystemService(Activity.LOCATION_SERVICE);

        formattedAddressTextView = (TextView) activity.findViewById(R.id.formatted_address);
        coordsTextView = (TextView) activity.findViewById(R.id.coords);
        locationTitleEditText = (EditText) activity.findViewById(R.id.location_title);

        searchMapEditText = (EditText) activity.findViewById(R.id.search_map);

        ImageButton findMeImageButton = (ImageButton) activity.findViewById(R.id.find_me);
        findMeImageButton.setImageResource(MyThemesUtils.getDrawableResId("ic_gps_fixed_%s_24dp"));

        ImageButton pickPlaceImageButton = (ImageButton) activity.findViewById(R.id.pick_place);
        pickPlaceImageButton.setImageResource(MyThemesUtils.getDrawableResId("ic_geo_fence_%s_24dp"));

        ImageButton openMapImageButton =  activity.findViewById(R.id.open_maps);
        openMapImageButton.setOnClickListener(v -> {
            try {
                String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f", stateLatitude, stateLongitude, stateLatitude, stateLongitude);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
               activity.startActivityForResult(intent, Static.REQUEST_SHOW_ON_MAP);
            } catch (Exception e) {
                AppLog.e("Exception: " + e);

                String urlAddress = "http://maps.google.com/maps?q=" + stateLatitude + "," + stateLongitude;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlAddress));
                activity.startActivityForResult(intent, Static.REQUEST_SHOW_ON_MAP);
            }
        });

        mapView = (ViewGroup) activity.findViewById(R.id.map_view);
        naAddressEditText = (EditText) activity.findViewById(R.id.na_address);
        naAddressEditText.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                stateLatitude = "";
                stateLongitude = "";
                naCoordsTextView.setText("");

                return false;
            }
        });
        naCoordsTextView = (TextView) activity.findViewById(R.id.na_coords);

        // Get latLong from extras if passed
        if (extras.containsKey("latLong")) {
            float[] latLong = extras.getFloatArray("latLong");
//            AppLog.d("latLong: " + latLong);

            if (latLong != null && latLong.length >= 2) {
                setAddress(null, latLong[0], latLong[1]);

                // Try to get address from latLong
                executeGeocodeFromCoordsAsync(latLong[0], latLong[1]);
            }
        }

        if (savedInstanceState != null) {
            stateAddress = savedInstanceState.getString(ADDRESS_STATE_KEY);
            stateLatitude = savedInstanceState.getString(LATITUDE_STATE_KEY);
            stateLongitude = savedInstanceState.getString(LONGITUDE_STATE_KEY);
        } else if (locationUid != null) {
            Cursor locationCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSingleLocationCursorByUid(locationUid);

            // If not found
            if (locationCursor.getCount() == 0) {
                locationCursor.close();
                mActivity.finish();
                return;
            }

            LocationInfo locationInfo = new LocationInfo(locationCursor);
            locationCursor.close();

            stateTitle = locationInfo.title;
            stateAddress = locationInfo.address;
            stateLatitude = locationInfo.latitude;
            stateLongitude = locationInfo.longitude;
            stateZoom = locationInfo.zoom;
        }

        // Search map
        searchMapEditText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                clearFocus();
                executeGeocodeFromLocationNameAsync(getSearchFieldText());
            }
            return false;
        });

        // Find me button
        findMeImageButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                findMe();
            } else {
                // Ask for permission
                PermissionsUtils.askForPermission((AppCompatActivity) mActivity, Manifest.permission.ACCESS_FINE_LOCATION, Static.PERMISSION_REQUEST_LOCATION, null, -1);
            }
        });

        // Pick place button
        //pickPlaceImageButton.setOnClickListener(v -> openPlacePicker());
        pickPlaceImageButton.setVisibility(View.GONE);
    }

    /**
     * public void openPlacePicker(){
     * if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) ==  PackageManager.PERMISSION_GRANTED) {
     * onPickButtonClick();
     * } else {
     * // Ask for permission
     * PermissionsUtils.askForPermission(  (AppCompatActivity) mActivity,  Manifest.permission.ACCESS_FINE_LOCATION, Static.PERMISSION_REQUEST_LOCATION_FOR_PLACES_PICKER, null,-1);
     * }
     * }
     **/
    private Location getLastKnownLocation() {
        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        LocationManager locationManager = (LocationManager) MyApp.getInstance().getSystemService(Activity.LOCATION_SERVICE);
        // Obtain the last known location
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
//		AppLog.d("location: " + location);

        return location;
    }

    public void onPickButtonClick() {
        // Construct an intent for the place picker
        /** LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090)); **/
        /**  PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
         Intent intent = intentBuilder.build(mActivity);

         // intentBuilder.setLatLngBounds(BOUNDS_MOUNTAIN_VIEW);
         // Start the intent by requesting a result, identified by a request code.
         mActivity.startActivityForResult(intent, Static.REQUEST_PLACE_PICKER); **/
    }

    public void onAddressGeocode(Address address, double latitude, double longitude) {
//		AppLog.d("address: " + address);

        if (address == null && latitude != 0 && longitude != 0) {
            setAddressCoordsOnly(latitude, longitude);
        } else {
            setAddress(address, latitude, longitude);
            showAddress();

            showMarkerOnMap(latitude, longitude);
        }
    }

    public void setMapAvailable(boolean isMapAvailable) {
        this.isMapAvailable = isMapAvailable;

        showHideMap();
    }

    public void showHideMap() {
        AppLog.d("");

        if (isMapAvailable) {
            mActivity.findViewById(R.id.search_layout_container).setVisibility(View.VISIBLE);
            mActivity.findViewById(R.id.map_layout_container).setVisibility(View.VISIBLE);
            ((View) naAddressEditText.getParent().getParent()).setVisibility(View.GONE);
            naCoordsTextView.setVisibility(View.GONE);
        } else {
            mActivity.findViewById(R.id.search_layout_container).setVisibility(View.GONE);
            mActivity.findViewById(R.id.map_layout_container).setVisibility(View.GONE);
            ((View) naAddressEditText.getParent().getParent()).setVisibility(View.VISIBLE);
            naCoordsTextView.setVisibility(View.VISIBLE);
        }
    }

    private void showMarkerOnMap(double latitude, double longitude) {
//		AppLog.d("latitude: " + latitude + ", longitude: " + longitude);
        ((LocationAddEditActivity) mActivity).showMarkerOnMap(latitude, longitude);
    }

    private int getCurrentMapZoom() {
        if (isMapAvailable) {
            return ((LocationAddEditActivity) mActivity).getCurrentMapZoom();
        }

        return 0;
    }

    private void setMapZoom(int zoom) {
        if (isMapAvailable) {
            ((LocationAddEditActivity) mActivity).setMapZoom(zoom);
        }
    }

    public void inflateMap(int mapLayoutResId) {
        inflater.inflate(mapLayoutResId, mapView, true);
    }

    private void stopLocationUpdate() {
        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager.removeUpdates(locationListener);
    }

    public void requestNewLocation() {
        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
        }
    }

    public LatLng getLastKnownLatLng() {
        Location location = getLastKnownLocation();

        if (location != null) {
            return new LatLng(location.getLatitude(), location.getLongitude());
        }

        return null;
    }

    public void findMe() {
        Location location = getLastKnownLocation();

        if (location == null) {
            // Show error toast
            Static.showToast(MyApp.getInstance().getString(
                    R.string.could_not_obtain_current_location), Toast.LENGTH_SHORT);
        } else {
            executeGeocodeFromCoordsAsync(location.getLatitude(), location.getLongitude());
        }
    }

    public void updateUi() {
//		AppLog.d("stateLatitude: " + stateLatitude + ", stateLongitude: " + stateLongitude + ", stateAddress: " + stateAddress);

        if (!stateLatitude.equals("") && !stateLongitude.equals("")) {
            double latitude = Double.parseDouble(stateLatitude);
            double longitude = Double.parseDouble(stateLongitude);
            showMarkerOnMap(latitude, longitude);
        } else if (!stateAddress.equals("")) {
            executeGeocodeFromLocationNameAsync(stateAddress);
        } else if (!stateTitle.equals("")) {
            executeGeocodeFromLocationNameAsync(stateTitle);
        }

        locationTitleEditText.setText(stateTitle);
        locationTitleEditText.setSelection(locationTitleEditText.getText().length());

        showAddress();
        if (stateZoom > 0) {
            setMapZoom(stateZoom);
        }
    }

    public void showAddress() {
//		AppLog.d("isAddressEmpty(): " + isAddressEmpty() + ", isMapAvailable: " + isMapAvailable);

        if (isAddressEmpty()) {
            if (isMapAvailable) {
                // Show initial text
//                int textResId = (AppConfig.AMAZON_BUILD) ? R.string.amazon_location_initial_text : R.string.google_location_initial_text;
                formattedAddressTextView.setText(R.string.google_location_initial_text);

                coordsTextView.setVisibility(View.GONE);
                coordsTextView.setText("");
            }
        } else {
            String coords = "";
            if (!stateLatitude.equals("") && !stateLongitude.equals("")) {
                coords = stateLatitude + ", " + stateLongitude;
            }

            if (isMapAvailable) {
                if (stateAddress.equals("")) {
                    formattedAddressTextView.setText(R.string.unable_to_get_address);
                } else {
                    formattedAddressTextView.setText(stateAddress);

                    try {
                        // Get title
                        String[] addressDAta = stateAddress.split(",");
                        String titleFromAddress = addressDAta[0];
                        locationTitleEditText.setText(titleFromAddress);
                    } catch (Exception e) {

                    }

                }

                coordsTextView.setVisibility(View.VISIBLE);
                coordsTextView.setText(coords);
            } else {
                naAddressEditText.setText(stateAddress);
                naCoordsTextView.setText(coords);
            }
        }
    }

    private boolean isAddressEmpty() {
        return stateAddress.equals("") && stateLatitude.equals("") && stateLongitude.equals("");
    }

    public void setAddress(Address address, double latitude, double longitude) {
        stateAddress = "";
        stateLatitude = "";
        stateLongitude = "";

        if (address != null) {
            stateAddress = LocationUtils.getFormattedAddress(address);
        }
        if (latitude != 0) {
            stateLatitude = LocationUtils.getFormattedCoord(latitude);
        }
        if (longitude != 0) {
            stateLongitude = LocationUtils.getFormattedCoord(longitude);
        }
    }

    public void clearFocus() {
//		AppLog.d("");

        // Hide keyboard / clear focus
        if (locationTitleEditText.isFocused()) {
            Static.hideSoftKeyboard(locationTitleEditText);
            locationTitleEditText.clearFocus();
        }
        if (searchMapEditText.isFocused()) {
            Static.hideSoftKeyboard(searchMapEditText);
            searchMapEditText.clearFocus();
        }
    }

    public String getLocationTitleFieldText() {
        return locationTitleEditText.getText().toString();
    }

    public String getSearchFieldText() {
        return searchMapEditText.getText().toString();
    }

    public void saveLocation() {
        if (!isMapAvailable) {
            stateAddress = naAddressEditText.getText().toString();
        }

        if (isAddressEmpty() && getLocationTitleFieldText().equals("")) {
            // Show error toast
            Static.showToast(MyApp.getInstance().getString(R.string.location_not_set_error), Toast.LENGTH_SHORT);
        } else {

            ContentValues cv = new ContentValues();

            cv.put(Tables.KEY_LOCATION_TITLE, getLocationTitleFieldText());
            cv.put(Tables.KEY_LOCATION_ADDRESS, stateAddress);
            cv.put(Tables.KEY_LOCATION_LATITUDE, stateLatitude);
            cv.put(Tables.KEY_LOCATION_LONGITUDE, stateLongitude);
            cv.put(Tables.KEY_LOCATION_ZOOM, getCurrentMapZoom());

            if (locationUid == null) {
                // check if lat lng exists already in db before creating a new one
                locationUid = MyApp.getInstance().storageMgr.getSQLiteAdapter().findLocationByLatLng(stateLatitude, stateLongitude, getLocationTitleFieldText());

                if (locationUid == null) {
                    // Generate uid
                    cv.put(Tables.KEY_UID, Static.generateRandomUid());

                    // Insert row
                    String uid = MyApp.getInstance().storageMgr.insertRow(Tables.TABLE_LOCATIONS, cv);
                    if (uid != null) {
                        locationUid = uid;
                    }
                } else {
                    // Update row
                    MyApp.getInstance().storageMgr.updateRowByUid(Tables.TABLE_LOCATIONS, locationUid, cv);
                }

            } else {
                // Update row
                MyApp.getInstance().storageMgr.updateRowByUid(Tables.TABLE_LOCATIONS, locationUid, cv);
            }

            MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners();

            // Finish
            Intent i = new Intent();
            i.putExtra("locationUid", locationUid);
            mActivity.setResult(Activity.RESULT_OK, i);
            mActivity.finish();
        }
    }

    private void showPleaseWait() {
        formattedAddressTextView.setText(R.string.please_wait);
        coordsTextView.setVisibility(View.GONE);
        coordsTextView.setText("");
    }

    public void executeGeocodeFromCoordsAsync(double latitude, double longitude) {
        cancelGeocodeFromCoordsAsync();

        if (MyApp.getInstance().networkStateMgr.isConnectedToInternet()) {
            showPleaseWait();

            geocodeFromCoordsAsync = new GeocodeFromCoordsAsync(LocationWrapper.this, latitude, longitude);
            // Execute on a separate thread
            Static.startMyTask(geocodeFromCoordsAsync);
        } else {
            // Show error toast
            Static.showToast(MyApp.getInstance().getString(R.string.error_internet_connection), Toast.LENGTH_SHORT);

            // If offline, set location coords only
            setAddressCoordsOnly(latitude, longitude);
        }
    }

    private void setAddressCoordsOnly(double latitude, double longitude) {
        stateAddress = "";
        stateLatitude = LocationUtils.getFormattedCoord(latitude);
        stateLongitude = LocationUtils.getFormattedCoord(longitude);

        showAddress();
        showMarkerOnMap(latitude, longitude);
    }

    public void cancelGeocodeFromCoordsAsync() {
        try {
            if (geocodeFromCoordsAsync != null) {
                geocodeFromCoordsAsync.cancel(true);
            }
        } catch (Exception e) {
        }
    }

    public void executeGeocodeFromLocationNameAsync(String locationName) {
        cancelGeocodeFromLocationNameAsync();

        if (MyApp.getInstance().networkStateMgr.isConnectedToInternet()) {
            showPleaseWait();

            geocodeFromLocationNameAsync = new GeocodeFromLocationNameAsync(mActivity, this, locationName);

            // Execute on a separate thread
            Static.startMyTask(geocodeFromLocationNameAsync);
        } else {
            Snackbar.make(mActivity.findViewById(R.id.layout_container), R.string.error_internet_connection, Snackbar.LENGTH_SHORT).show();
        }
    }

    public void cancelGeocodeFromLocationNameAsync() {
        try {
            if (geocodeFromLocationNameAsync != null) {
                geocodeFromLocationNameAsync.cancel(true);
            }
        } catch (Exception e) {
        }
    }

    public void onStop() {
        stopLocationUpdate();
        cancelGeocodeFromCoordsAsync();
        cancelGeocodeFromLocationNameAsync();
    }

    /**
     * public void fillFieldsFromPlace(Place place) {
     * final CharSequence name = place.getName();
     * LatLng latLng = place.getLatLng();
     * final CharSequence address = place.getAddress();
     * //            String attributions = PlacePicker.getAttributions(data);
     * //            if (attributions == null) {
     * //                attributions = "";
     * //            }
     * //            Html.fromHtml(attributions)
     * <p>
     * AppLog.d("name: " + name + ", address: " + address + ", latLng.latitude: " + latLng.latitude + ", latLng.longitude: " + latLng.longitude);
     * <p>
     * stateTitle = name.toString();
     * stateAddress = address.toString();
     * stateLatitude = LocationUtils.getFormattedCoord(latLng.latitude);
     * stateLongitude = LocationUtils.getFormattedCoord(latLng.longitude);
     * stateZoom = 15;
     * <p>
     * updateUi();
     * }
     **/

    private static final String ADDRESS_STATE_KEY = "ADDRESS_STATE_KEY";
    private static final String LATITUDE_STATE_KEY = "LATITUDE_STATE_KEY";
    private static final String LONGITUDE_STATE_KEY = "LONGITUDE_STATE_KEY";

    public void onSaveInstanceState(Bundle outState) {
        outState.putString(ADDRESS_STATE_KEY, stateAddress);
        outState.putString(LATITUDE_STATE_KEY, stateLatitude);
        outState.putString(LONGITUDE_STATE_KEY, stateLongitude);
    }
}
