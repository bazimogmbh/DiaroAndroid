package com.pixelcrater.Diaro.atlas;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.activitytypes.TypeActivity;
import com.pixelcrater.Diaro.model.EntryInfo;
import com.pixelcrater.Diaro.entries.viewedit.EntryViewEditActivity;
import com.pixelcrater.Diaro.storage.sqlite.SQLiteQueryHelper;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.Static;

import java.util.ArrayList;
import java.util.List;

import berlin.volders.badger.BadgeDrawable;
import berlin.volders.badger.BadgeShape;
import berlin.volders.badger.Badger;
import berlin.volders.badger.CountBadge;

public class AtlasActivity extends TypeActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<EntryIDAndLocation> entryIdAndLocationsList;
    private LatLng latLng;
    private LatLngBounds latLngBounds;

    private final int CAMERA_PADDING = 10;

    private RecyclerView mRecyclerView;

    private AtlasBottomsheetAdapter mAdapter;

    private LinearLayout llBottomSheet;
    private BottomSheetBehavior bottomSheetBehavior;

    private ArrayList<EntryInfo> entries = new ArrayList<>();
    private ArrayList<String> uidList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        useCollapsingToolbar = true;
        super.onCreate(savedInstanceState);
        setContentView(addViewToContentContainer(R.layout.atlas));
        activityState.setLayoutBackground();
        activityState.setActionBarTitle(getSupportActionBar(), R.string.atlas);

        entryIdAndLocationsList = SQLiteQueryHelper.getAtlasData();

        llBottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setHideable(true);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        ViewGroup.LayoutParams params = llBottomSheet.getLayoutParams();
        params.height = (int) (height * 0.6);
        llBottomSheet.setLayoutParams(params);

        // set callback for changes
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setBackgroundResource(MyThemesUtils.getBackgroundColorResId());

        mAdapter = new AtlasBottomsheetAdapter(this, entries);
        mAdapter.setOnItemClickListener((view, obj, position) -> {
            Intent intent = new Intent(AtlasActivity.this, EntryViewEditActivity.class);
            intent.putExtra(Static.EXTRA_SKIP_SC, true);
            intent.putExtra("entryUid", obj.uid);
            startActivity(intent);
        });

        mRecyclerView.setAdapter(mAdapter);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, MyThemesUtils.getGoogleMapsStyle()));
        } catch (Resources.NotFoundException e) {
        }

        setUpClustererAndPositionCamera();
    }

    //Converting Vector to Bitmap and displaying the size of Cluster using canvas
    public static BitmapDescriptor getBitmapFromVector(@NonNull Context context, @DrawableRes int vectorResourceId, int size) {

        Bitmap bitmap;
        Drawable vectorDrawable = AppCompatResources.getDrawable(context, vectorResourceId);

        if (vectorDrawable == null) {
            Log.e("Error", "Requested vector resource was not found");
            return BitmapDescriptorFactory.defaultMarker();
        }

        BadgeShape badgeShape = BadgeShape.square(.45f, Gravity.END | Gravity.TOP, 0.5f);

        CountBadge.Factory circleFactory = new CountBadge.Factory(badgeShape, MyThemesUtils.getAccentColor(), context.getResources().getColor(android.R.color.white));

        Badger<BadgeDrawable> badger = Badger.sett(vectorDrawable, circleFactory);

        vectorDrawable = badger.drawable;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            vectorDrawable.setTint(MyThemesUtils.getAccentColor());
            vectorDrawable.setAlpha(230);
        }

        if (size > 1)
            Badger.sett(vectorDrawable, circleFactory).badge.setCount(size);

        bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    //updating the camera position on zoom
    public void updateCameraPos(Cluster<StringClusterItem> cluster, float zoomLevel) {
        //LatLngBound for clicked cluster
        LatLngBounds.Builder latLngBuilderForCluster = new LatLngBounds.Builder();

        for (StringClusterItem clusterItem : cluster.getItems()) {
            latLngBuilderForCluster.include(clusterItem.latLng);
        }

        final LatLngBounds latLngBoundsForZoom = latLngBuilderForCluster.build();
        CameraUpdate updateCameraZoom = CameraUpdateFactory.newLatLngZoom(latLngBoundsForZoom.getCenter(), zoomLevel);
        mMap.setPadding(CAMERA_PADDING, CAMERA_PADDING, CAMERA_PADDING, CAMERA_PADDING);
        mMap.animateCamera(updateCameraZoom);
    }

    //Setting up the clusters on the map and the position of camera, when the map is initialised
    private void setUpClustererAndPositionCamera() {

        ClusterManager<StringClusterItem> clusterManager = new ClusterManager<>(this, mMap);
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);

        //display a cluster for more than two markers
        CustomRenderer<StringClusterItem> customRenderer = new CustomRenderer<>(this, mMap, clusterManager);
        clusterManager.setRenderer(customRenderer);
        LatLngBounds.Builder latLngbuilder = new LatLngBounds.Builder();

        //add LatLng for clusters
        for (int i = 0; i < entryIdAndLocationsList.size(); i++) {
            latLng = new LatLng(entryIdAndLocationsList.get(i).getLatitude(), entryIdAndLocationsList.get(i).getLongitude());
            latLngbuilder.include(latLng);

            StringClusterItem location = new StringClusterItem(entryIdAndLocationsList.get(i).getEntryId(), latLng);
            clusterManager.addItem(location);
        }

        if (entryIdAndLocationsList.size() > 0) {
            //center the camera between the bounds
            latLngBounds = latLngbuilder.build();
            mMap.setOnMapLoadedCallback(() -> {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLngBounds.getCenter(), 3);
                mMap.setMinZoomPreference(1f);
                mMap.moveCamera(cameraUpdate);
                mMap.animateCamera(cameraUpdate);
                mMap.getUiSettings().setZoomControlsEnabled(true);

                // if no clusters are present in view bounds, move camera to last cluster on the list
                if (!mMap.getProjection().getVisibleRegion().latLngBounds.contains(latLng)) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            });
        }

        //Managing Zoom when a cluster is clicked
        clusterManager.setOnClusterClickListener(cluster -> {
            float currentZoom = mMap.getCameraPosition().zoom;
            //   updateCameraPos(cluster, currentZoom + 2);

            uidList.clear();
            entries.clear();

            for (ClusterItem clusterItem : cluster.getItems()) {
                uidList.add(clusterItem.getTitle());
            }

            entries = SQLiteQueryHelper.getEntriesByUids(uidList, true);
            mAdapter.setData(entries);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            return true;
        });

        //Managing Onclick Marker -- Can Jump to another Activity from here --
        clusterManager.setOnClusterItemClickListener(stringClusterItem -> {

            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            Intent intent = new Intent(AtlasActivity.this, EntryViewEditActivity.class);
            intent.putExtra(Static.EXTRA_SKIP_SC, true);
            intent.putExtra("entryUid", stringClusterItem.getTitle());
            startActivityForResult(intent, Static.REQUEST_VIEW_EDIT_ENTRY);

            //   CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(stringClusterItem.getPosition(), 18);
            //   mMap.animateCamera(cameraUpdate);
            return true;

        });
    }

    /**
     * rendering the clusters--
     */
    public class CustomRenderer<T extends ClusterItem> extends DefaultClusterRenderer<T> {

        public CustomRenderer(Context context, GoogleMap map, ClusterManager<T> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected boolean shouldRenderAsCluster(Cluster<T> cluster) {
            return cluster.getSize() > 1;
        }

        protected int getBucket(Cluster<T> cluster) {
            return cluster.getSize();
        }

        protected void onBeforeClusterItemRendered(T item, MarkerOptions markerOptions) {
            markerOptions.icon(getBitmapFromVector(getApplicationContext(), R.drawable.ic_marker, 0));
        }

        protected void onBeforeClusterRendered(Cluster<T> cluster, MarkerOptions markerOptions) {
            markerOptions.icon(getBitmapFromVector(getApplicationContext(), R.drawable.ic_marker, getBucket(cluster)));
        }
    }

    @Override
    public void onBackPressed() {

        if (bottomSheetBehavior != null && bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            return;
        } else
            super.onBackPressed();
    }

}