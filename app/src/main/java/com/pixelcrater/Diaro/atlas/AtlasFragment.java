package com.pixelcrater.Diaro.atlas;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.entries.async.DeleteEntriesAsync;
import com.pixelcrater.Diaro.entries.viewedit.EntryViewEditActivity;
import com.pixelcrater.Diaro.main.AppMainActivity;
import com.pixelcrater.Diaro.model.EntryInfo;
import com.pixelcrater.Diaro.storage.sqlite.SQLiteQueryHelper;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.Static;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import berlin.volders.badger.BadgeDrawable;
import berlin.volders.badger.BadgeShape;
import berlin.volders.badger.Badger;
import berlin.volders.badger.CountBadge;

public class AtlasFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    private LatLng latLng;

    private RecyclerView mRecyclerView;

    private AtlasBottomsheetAdapter mAdapter;

    private List<EntryIDAndLocation> entryIdAndLocationsList = new ArrayList<>();
    private LinearLayout llBottomSheet;
    private BottomSheetBehavior bottomSheetBehavior;

    private ArrayList<EntryInfo> entries = new ArrayList<>();
    private ArrayList<String> uidList = new ArrayList<>();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.atlas, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        llBottomSheet = view.findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        ViewGroup.LayoutParams params = llBottomSheet.getLayoutParams();
        params.height = (int) (height * 0.6);
        llBottomSheet.setLayoutParams(params);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

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

        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setBackgroundResource(MyThemesUtils.getBackgroundColorResId());

        mAdapter = new AtlasBottomsheetAdapter(getActivity(), entries);
        mAdapter.setOnItemClickListener((view2, obj, position) -> {
            Intent intent = new Intent(getActivity(), EntryViewEditActivity.class);
            intent.putExtra(Static.EXTRA_SKIP_SC, true);
            intent.putExtra("entryUid", obj.uid);
            startActivityForResult(intent, Static.REQUEST_VIEW_EDIT_ENTRY);
        });

        mRecyclerView.setAdapter(mAdapter);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setHideable(true);
    }


    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        fetchData();
    }

    private void fetchData() {
        entryIdAndLocationsList.clear();
        entryIdAndLocationsList = SQLiteQueryHelper.getAtlasData();

        mAdapter.notifyDataSetChanged();

        loadClusterData();
    }

    Cluster<StringClusterItem> mCurrentCluster;

    private void loadClusterData() {

        if (mCurrentCluster != null) {
            uidList.clear();
            entries.clear();

            for (ClusterItem clusterItem : mCurrentCluster.getItems()) {
                uidList.add(clusterItem.getTitle());
            }

            entries = SQLiteQueryHelper.getEntriesByUids(uidList, true);
            mAdapter.setData(entries);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireActivity(), MyThemesUtils.getGoogleMapsStyle()));
            mMap.clear();
        } catch (Resources.NotFoundException e) {
        }

        clusterManager = new ClusterManager<>(requireActivity(), mMap);

        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);

        //Managing Zoom when a cluster is clicked
        clusterManager.setOnClusterClickListener(cluster -> {
            mCurrentCluster = cluster;
            loadClusterData();
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            return true;
        });

        //Managing Onclick Marker -- Can Jump to another Activity from here --
        clusterManager.setOnClusterItemClickListener(stringClusterItem -> {

            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            Intent intent = new Intent(getActivity(), EntryViewEditActivity.class);
            intent.putExtra(Static.EXTRA_SKIP_SC, true);
            intent.putExtra("entryUid", stringClusterItem.getTitle());
            startActivityForResult(intent, Static.REQUEST_VIEW_EDIT_ENTRY);
            return true;

        });

        setUpClustererAndPositionCamera();
    }


    ClusterManager<StringClusterItem> clusterManager;
    CustomRenderer<StringClusterItem> customRenderer;
    LatLngBounds.Builder latLngbuilder = new LatLngBounds.Builder();

    //Setting up the clusters on the map and the position of camera, when the map is initialised
    private void setUpClustererAndPositionCamera() {

        latLngbuilder = new LatLngBounds.Builder();
        clusterManager.clearItems();

        //display a cluster for more than two markers
        customRenderer = new CustomRenderer<>(getContext(), mMap, clusterManager);
        clusterManager.setRenderer(customRenderer);

        //add LatLng for clusters
        for (int i = 0; i < entryIdAndLocationsList.size(); i++) {
            latLng = new LatLng(entryIdAndLocationsList.get(i).getLatitude(), entryIdAndLocationsList.get(i).getLongitude());
            latLngbuilder.include(latLng);

            StringClusterItem location = new StringClusterItem(entryIdAndLocationsList.get(i).getEntryId(), latLng);
            clusterManager.addItem(location);
        }

        if (entryIdAndLocationsList.size() > 0) {
            //center the camera between the bounds
            LatLngBounds latLngBounds = latLngbuilder.build();

            mMap.setOnMapLoadedCallback(() -> {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLngBounds.getCenter(), 3);
                mMap.setMinZoomPreference(1f);
                mMap.getUiSettings().setZoomControlsEnabled(true);

                /**    mMap.moveCamera(cameraUpdate);
                 mMap.animateCamera(cameraUpdate);  **/

                //  if no clusters are present in view bounds, move camera to last cluster on the list
                if (!mMap.getProjection().getVisibleRegion().latLngBounds.contains(latLng)) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                } else
                    clusterManager.cluster();
            });
        }


    }

    private void resetDataOnMap() {

        if (clusterManager == null)
            clusterManager = new ClusterManager<>(requireActivity(), mMap);

        clusterManager.clearItems();

        entryIdAndLocationsList = SQLiteQueryHelper.getAtlasData();

        LatLngBounds.Builder latLngbuilder = new LatLngBounds.Builder();

        for (int i = 0; i < entryIdAndLocationsList.size(); i++) {
            LatLng latLng = new LatLng(entryIdAndLocationsList.get(i).getLatitude(), entryIdAndLocationsList.get(i).getLongitude());
            latLngbuilder.include(latLng);

            StringClusterItem location = new StringClusterItem(entryIdAndLocationsList.get(i).getEntryId(), latLng);
            clusterManager.addItem(location);
        }

        clusterManager.cluster();
    }

    /**
     * Rendering the clusters--
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
            markerOptions.icon(getBitmapFromVector(getActivity(), R.drawable.ic_marker, 0));
        }

        protected void onBeforeClusterRendered(Cluster<T> cluster, MarkerOptions markerOptions) {
            markerOptions.icon(getBitmapFromVector(getActivity(), R.drawable.ic_marker, getBucket(cluster)));
        }
    }

    //Converting Vector to Bitmap and displaying the size of Cluster using canvas
    public static BitmapDescriptor getBitmapFromVector(@NonNull Context context, @DrawableRes int vectorResourceId, int size) {

        Bitmap bitmap;
        Drawable vectorDrawable = AppCompatResources.getDrawable(context, vectorResourceId);

        if (vectorDrawable == null) {
            Log.e("Error", "Requested vector resource was not found");
            return BitmapDescriptorFactory.defaultMarker();
        }

        BadgeShape badgeShape = BadgeShape.square(.45f, Gravity.END | Gravity.TOP, 0.3f);

        CountBadge.Factory circleFactory = new CountBadge.Factory(badgeShape, context.getResources().getColor(R.color.pink_500), context.getResources().getColor(R.color.color_white));
        // CountBadge.Factory circleFactory=   new CountBadge.Factory(context, BadgeShape.circle(0.55f, Gravity.END | Gravity.TOP));
        Badger<BadgeDrawable> badger = Badger.sett(vectorDrawable, circleFactory);

        vectorDrawable = badger.drawable;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            vectorDrawable.setTint(context.getResources().getColor(R.color.diaro_default));
            vectorDrawable.setAlpha(230);
        }

        if (size > 1)
            Badger.sett(vectorDrawable, circleFactory).badge.setCount(size);

        bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_4444);

        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result from view/edit entry activity
        if (requestCode == Static.REQUEST_VIEW_EDIT_ENTRY) {
            if (resultCode == FragmentActivity.RESULT_OK) {
                Bundle extras = data.getExtras();
                String entryUid = extras.getString("entryUid");
                boolean entryArchived = extras.getBoolean("entryArchived");
                // AppLog.d("entryUid: " + entryUid + ", entryArchived: " + entryArchived);
                if (!TextUtils.isEmpty(entryUid)) {
                    if (entryArchived) {
                        showUndoDeleteEntries(entryUid);
                    }
                }
            }
        }

        resetDataOnMap();
    }

    private void showUndoDeleteEntries(String serializedUids) {
        AppLog.d("serializedUids: " + serializedUids);
        final ArrayList<String> entriesUids = serializedUids.equals("") ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(serializedUids.split(",")));
        Snackbar mySnackbar = Snackbar.make(requireView().findViewById(R.id.layout_container), R.string.deleted, Snackbar.LENGTH_LONG);
        mySnackbar.setAction(R.string.undo_delete, new AppMainActivity.MyUndoListener(entriesUids));
        mySnackbar.show();

        mySnackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                    // Delete entries in background
                    AppLog.e("deleting -> " + entriesUids.get(0));
                    DeleteEntriesAsync deleteEntriesAsync = new DeleteEntriesAsync(entriesUids);
                    deleteEntriesAsync.execute();

                    resetDataOnMap();

                }
            }

            @Override
            public void onShown(Snackbar snackbar) {
            }
        });
    }


    public boolean onBackPressed() {
        if (bottomSheetBehavior != null && bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            return true;
        } else {
            return false;
        }

    }

}