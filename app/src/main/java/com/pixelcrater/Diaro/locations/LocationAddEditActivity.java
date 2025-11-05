package com.pixelcrater.Diaro.locations;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.activitytypes.TypeActivity;
import com.pixelcrater.Diaro.settings.PreferencesHelper;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.PermissionsUtils;
import com.pixelcrater.Diaro.utils.Static;

public class LocationAddEditActivity extends TypeActivity implements OnMapReadyCallback {

    public static final String IS_NEW_LOCATION_REQUESTED_STATE_KEY = "IS_NEW_LOCATION_REQUESTED_STATE_KEY";
    private static final int INITIAL_ZOOM = 15;
    private LocationWrapper locationWrapper;
    private SupportMapFragment mapFragment;
    private GoogleMap googleMap;
    private boolean isNewLocationRequested;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(addViewToContentContainer(R.layout.location_addedit));
        activityState.setLayoutBackground();

        // Get vars from savedInstanceState
        if (savedInstanceState != null) {
            isNewLocationRequested = savedInstanceState.getBoolean(IS_NEW_LOCATION_REQUESTED_STATE_KEY);
        }

        locationWrapper = new LocationWrapper(LocationAddEditActivity.this, savedInstanceState);
        locationWrapper.inflateMap(R.layout.mapview_google);

        // Activity title
        int titleResId = R.string.location_add;
        if (locationWrapper.locationUid != null) {
            titleResId = R.string.location_edit;
        } else {
            //locationWrapper.openPlacePicker();
            //  locationWrapper.findMe();
        }
        activityState.setActionBarTitle(getSupportActionBar(), getString(titleResId));

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);

        mapFragment.getMapAsync(this);

        updateUi();
    }

    private void updateUi() {
        AppLog.d("googleMap: " + googleMap);

        // Check if we were successful in obtaining the map
        if (googleMap != null) {
            locationWrapper.setMapAvailable(true);

            // The Map is verified. It is now safe to manipulate the map.
            googleMap.setMyLocationEnabled(false);

            // Set map listeners
            googleMap.setOnMapLongClickListener(latLng -> locationWrapper.executeGeocodeFromCoordsAsync(latLng.latitude, latLng.longitude));

            googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

                @Override
                public void onMarkerDragStart(Marker marker) {
                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    LatLng latLng = marker.getPosition();
                    locationWrapper.executeGeocodeFromCoordsAsync(latLng.latitude, latLng.longitude);
                }

                @Override
                public void onMarkerDrag(Marker marker) {
                }
            });

            if (!isNewLocationRequested && locationWrapper.locationUid == null) {
                isNewLocationRequested = true;

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationWrapper.requestNewLocation();
                }

                setInitialMapPosition();
            }
        } else {
            locationWrapper.setMapAvailable(false);
        }

        locationWrapper.updateUi();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        AppLog.d("map: " + map);

        googleMap = map;
        googleMap.setMapType(PreferencesHelper.getMapType());

        try {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, MyThemesUtils.getGoogleMapsStyle()));
        } catch (Resources.NotFoundException e) {
        }

        if (locationWrapper.locationUid == null) {
            locationWrapper.findMe();
        }
        updateUi();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_location_addedit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (activityState.isActivityPaused) {
            return true;
        }

        // Handle presses on the action bar items
        int itemId = item.getItemId();

        // Back
        if (itemId == android.R.id.home) {
            locationWrapper.clearFocus();
            finish();
            return true;
        }

        // Save location
        else if (itemId == R.id.item_save) {
            locationWrapper.clearFocus();
            locationWrapper.saveLocation();
            return true;
        }

        else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void setInitialMapPosition() {
        AppLog.d("");

        LatLng latLng = locationWrapper.getLastKnownLatLng();

        if (latLng != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, INITIAL_ZOOM));
        } else {
            setMapZoom(INITIAL_ZOOM);
        }
    }

    public void showMarkerOnMap(double latitude, double longitude) {
        if (!locationWrapper.isMapAvailable) {
            return;
        }

        // Clear current marker
        googleMap.clear();

        if (latitude != 0 && longitude != 0) {
            LatLng latLng = new LatLng(latitude, longitude);

            googleMap.addMarker(new MarkerOptions().draggable(true).position(latLng));  //    .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, getCurrentMapZoom()));
        }
    }

    public int getCurrentMapZoom() {
        if (!locationWrapper.isMapAvailable) {
            return 0;
        }

        return (int) googleMap.getCameraPosition().zoom;
    }

    public void setMapZoom(int zoom) {
        if (!locationWrapper.isMapAvailable) {
            return;
        }

        googleMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationWrapper.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        locationWrapper.onSaveInstanceState(outState);
        outState.putBoolean(IS_NEW_LOCATION_REQUESTED_STATE_KEY, isNewLocationRequested);
    }

   /** @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Static.REQUEST_PLACE_PICKER && resultCode == Activity.RESULT_OK) {
            // The user has selected a place. Extract the name and address.
            final Place place = PlacePicker.getPlace(data, this);
            locationWrapper.fillFieldsFromPlace(place);

            if (MyApp.getInstance().securityCodeMgr.isSecurityCodeSet()) {
                MyApp.getInstance().securityCodeMgr.setUnlocked();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }**/

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Static.PERMISSION_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationWrapper.findMe();
                } else {
                    PermissionsUtils.showDeniedOpenSettingsDialog(this, Static.DIALOG_CONFIRM_RATIONALE_LOCATION , R.string.unable_to_access_location);
                   // Static.showToast(getString(R.string.unable_to_access_location), Toast.LENGTH_SHORT);
                }
            }
            break;

        }
    }
}
