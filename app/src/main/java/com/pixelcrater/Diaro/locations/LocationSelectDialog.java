package com.pixelcrater.Diaro.locations;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.DialogFragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.model.LocationInfo;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.generaldialogs.ConfirmDialog;
import com.pixelcrater.Diaro.layouts.QustomDialogBuilder;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

import org.apache.commons.lang3.StringUtils;

public class LocationSelectDialog extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor>, QustomDialogBuilder.OnSearchTextChangeListener {

    private static final String SELECTED_LOCATION_UID_STATE_KEY = "SELECTED_LOCATION_UID_STATE_KEY";
    private static final String IS_NEW_ENTRY_STATE_KEY = "IS_NEW_ENTRY_STATE_KEY";

    private static final int LOCATIONS_LIST_LOADER = 0;
    private LocationsCursorAdapter locationsCursorAdapter;
    private boolean isNewEntry;
    private androidx.appcompat.app.AlertDialog dialog;
    private String selectedLocationUid;
    private ListView locationsListView;
    private ProgressBar locationsListProgressBar;
    private QustomDialogBuilder builder;

    // Item click listener
    private OnDialogItemClickListener onDialogItemClickListener;

    public void setOnDialogItemClickListener(OnDialogItemClickListener l) {
        onDialogItemClickListener = l;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            selectedLocationUid = savedInstanceState.getString(SELECTED_LOCATION_UID_STATE_KEY);
            isNewEntry = savedInstanceState.getBoolean(IS_NEW_ENTRY_STATE_KEY);
        }

        // Use the Builder class for convenient dialog construction
        builder = new QustomDialogBuilder(getActivity());

        // Color
        builder.setHeaderBackgroundColor(MyThemesUtils.getPrimaryColorCode());

        // Title
        builder.setTitle(getActivity().getResources().getString(R.string.select_location));

        // Set custom view
        builder.setCustomView(R.layout.locations_list);
        View customView = builder.getCustomView();

        // Cancel button
        builder.setNegativeButton(android.R.string.cancel, null);

        // Search button
        builder.showSearchButton();
        builder.setOnSearchTextChangeListener(this);

        // Add new button
        builder.setAddNewButtonOnClick(v -> startLocationAddEditActivity(null));

        // Locations list
        locationsListView = customView.findViewById(R.id.locations_list);

        locationsListProgressBar = customView.findViewById(R.id.locations_list_progress);

        // Set list adapter
        locationsCursorAdapter = new LocationsCursorAdapter(getActivity(), null, 0);
        locationsListView.setAdapter(locationsCursorAdapter);

        // OnItemClickListener
        locationsListView.setOnItemClickListener((a, v, position, id) -> {
            selectedLocationUid = locationsCursorAdapter.getItemUid(position);
            if (onDialogItemClickListener != null) {
                if (StringUtils.equals(selectedLocationUid, LocationsCursorAdapter.NO_LOCATION_UID)) {
                    selectedLocationUid = "";
                }
                onDialogItemClickListener.onDialogItemClick(selectedLocationUid);
            }

            dialog.dismiss();
        });

        // OnOverflowItemClickListener
        locationsCursorAdapter.setOverflowItemClickListener(this::showLocationPopupMenu);

        // Highlight location
        if (selectedLocationUid != null) {
            locationsCursorAdapter.setSelectedLocationUid(StringUtils.isEmpty(selectedLocationUid) ? LocationsCursorAdapter.NO_LOCATION_UID : selectedLocationUid);
        }

        // Restore active dialog listeners
        restoreDialogListeners(savedInstanceState);

        dialog = builder.create();

        // Create the AlertDialog object and return it
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Prevent dismiss on touch outside
        getDialog().setCanceledOnTouchOutside(false);

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Init locations cursor loader
        LoaderManager.getInstance(this).initLoader(LOCATIONS_LIST_LOADER, null, this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!builder.getSearchFieldText().equals("")) {
            builder.showHideSearchField(true);
        }
    }

    private void restoreDialogListeners(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            ConfirmDialog dialog1 = (ConfirmDialog) getChildFragmentManager().findFragmentByTag(Static.DIALOG_CONFIRM_LOCATION_DELETE);
            if (dialog1 != null) {
                setLocationDeleteConfirmDialogListener(dialog1);
            }
        }
    }

    public void setSelectedLocationUid(String locationUid) {
        selectedLocationUid = locationUid;
    }

    public void setIsNewEntry(boolean isNewEntry) {
        this.isNewEntry = isNewEntry;
    }

    private void showLocationPopupMenu(View v, final String locationUid) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), v);
        popupMenu.getMenuInflater().inflate(R.menu.popupmenu_location, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                // Edit location
                case R.id.edit:
                    startLocationAddEditActivity(locationUid);
                    return true;

                // Delete location
                case R.id.delete:
                    showLocationDeleteConfirmDialog(locationUid);
                    return true;
                default:
                    return false;
            }
        });

        popupMenu.show();
    }

    private void startLocationAddEditActivity(String locationUid) {
        Intent intent = new Intent(getActivity(), LocationAddEditActivity.class);
        intent.putExtra(Static.EXTRA_SKIP_SC, true);
        intent.putExtra("locationUid", locationUid);
        startActivityForResult(intent, Static.REQUEST_LOCATION_ADDEDIT);
    }

    private void showLocationDeleteConfirmDialog(final String locationUid) {
        String dialogTag = Static.DIALOG_CONFIRM_LOCATION_DELETE;
        if (getChildFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setCustomString(locationUid);
            dialog.setTitle(getString(R.string.delete));
            dialog.setMessage(getString(R.string.location_confirm_delete));
            dialog.show(getChildFragmentManager(), dialogTag);

            // Set dialog listener
            setLocationDeleteConfirmDialogListener(dialog);
        }
    }

    private void setLocationDeleteConfirmDialogListener(final ConfirmDialog dialog) {
        dialog.setDialogPositiveClickListener(() -> {
            if (!isAdded()) {
                return;
            }

            // Delete location in background
            LocationsStatic.deleteLocationInBackground(dialog.getCustomString());
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(SELECTED_LOCATION_UID_STATE_KEY, selectedLocationUid);
        outState.putBoolean(IS_NEW_ENTRY_STATE_KEY, isNewEntry);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        return new LocationsCursorLoader(getActivity(), args == null ? "" : args.getString("searchKeyword"));
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        locationsCursorAdapter.swapCursor(cursor);
        locationsListView.setVisibility(View.VISIBLE);
        locationsListProgressBar.setVisibility(View.GONE);

        int index = 0;
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                LocationInfo locationInfo = new LocationInfo(cursor);
                if (locationInfo.uid != null && selectedLocationUid != null) {
                    if (locationInfo.uid.compareToIgnoreCase(selectedLocationUid) == 0) {
                        locationsListView.setSelection(index);
                        break;

                    }
                    index++;
                }
            }
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        locationsCursorAdapter.swapCursor(null);
    }

    @Override
    public void onSearchTextChange(String text) {
        // Init loader
        Bundle bundle = new Bundle();
        bundle.putString("searchKeyword", text);
       LoaderManager.getInstance(this).restartLoader(LOCATIONS_LIST_LOADER, bundle, this);
    }

    public interface OnDialogItemClickListener {
        void onDialogItemClick(String selectedFolderUid);
    }
}
