package com.pixelcrater.Diaro.sidemenu;

import android.database.Cursor;
import android.view.View;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.model.LocationInfo;
import com.pixelcrater.Diaro.locations.LocationsCursorAdapter;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class SidemenuLocations {
    private final int listItemTextColor;
    private final int listSelectedItemTextColor;
    private ArrayList<String> selectedLocationsUidsArrayList = new ArrayList<>();

    // OnOverflowItemClickListener
    private OnOverflowItemClickListener onOverflowItemClickListener;

    public SidemenuLocations() {
        listItemTextColor = MyThemesUtils.getListItemTextColor();
        listSelectedItemTextColor = MyThemesUtils.getSelectedListItemTextColor();
    }

    public void setOnOverflowItemClickListener(OnOverflowItemClickListener l) {
        onOverflowItemClickListener = l;
    }

    public void bindView(SidemenuCursorTreeAdapter.ChildViewHolder holder, Cursor cursor) {
//        AppLog.d("cursor.getPosition(): " + cursor.getPosition());

        final LocationInfo locationInfo = new LocationInfo(cursor);

        // Color
        holder.colorView.setVisibility(View.GONE);

        // Icon
        holder.iconView.setVisibility(View.GONE);

        // Checkbox
        holder.checkboxView.setVisibility(View.VISIBLE);

        // Title
        holder.titleTextView.setText(locationInfo.getLocationTitle());

        // Count
        holder.countTextView.setText(String.valueOf(locationInfo.entriesCount));

        // Overflow
        if (locationInfo.uid.equals(LocationsCursorAdapter.NO_LOCATION_UID)) {
            holder.overflowView.setVisibility(View.INVISIBLE);
        } else {
            holder.overflowView.setVisibility(View.VISIBLE);
            holder.overflowView.setOnClickListener(v -> {
                // Send the onClick event back to the host activity
                if (onOverflowItemClickListener != null) {
                    onOverflowItemClickListener.onOverflowItemClick(v, locationInfo.uid);
                }
            });
        }

        // Check selected locations
        if (selectedLocationsUidsArrayList.contains(locationInfo.uid)) {
            if (!holder.checkboxView.isChecked()) {
                holder.checkboxView.setChecked(true);
            }
            holder.titleTextView.setTextColor(listSelectedItemTextColor);
            holder.countTextView.setTextColor(listSelectedItemTextColor);
        } else {
            if (holder.checkboxView.isChecked()) {
                holder.checkboxView.setChecked(false);
            }
            holder.titleTextView.setTextColor(listItemTextColor);
            holder.countTextView.setTextColor(listItemTextColor);
        }
    }

    public void markUnmarkLocation(String locationUid) {
//        AppLog.d("locationUid: " + locationUid + ", selectedLocationsUidsArrayList: " + selectedLocationsUidsArrayList);

        if (selectedLocationsUidsArrayList.contains(locationUid)) {
            selectedLocationsUidsArrayList.remove(locationUid);
        } else {
            selectedLocationsUidsArrayList.add(locationUid);
        }
        //		AppLog.d("selectedLocationsUidsArrayList: " + selectedLocationsUidsArrayList);
    }

    public ArrayList<String> getSelectedLocationsUidsArrayList() {
        return selectedLocationsUidsArrayList;
    }

    public String getSelectedLocationsUids() {
        String selectedLocations = "";

        int size = selectedLocationsUidsArrayList.size();
        for (int i = 0; i < size; i++) {
            String locationUid = selectedLocationsUidsArrayList.get(i);
            if (!locationUid.equals("")) {
                selectedLocations += locationUid;
            }
            if (StringUtils.isNotEmpty(selectedLocations)) {
                selectedLocations += ",";
            }
        }

        return selectedLocations;
    }

    public void setSelectedLocationsUids(String locationsUids) {
        selectedLocationsUidsArrayList.clear();

        if (locationsUids != null && !locationsUids.equals("")) {
            ArrayList<String> splittedArrayList = new ArrayList<>(Arrays.asList(locationsUids.split(",")));

            for (int i = 0; i < splittedArrayList.size(); i++) {
                String location = splittedArrayList.get(i);
                if (location != null && !location.equals("")) {
                    selectedLocationsUidsArrayList.add(location);
                }
            }
        }
    }

    public void clearSelectedLocations() {
        selectedLocationsUidsArrayList.clear();
        saveActiveLocationsInPrefs();
    }

    public void clearActiveLocations() {
        clearSelectedLocations();
        saveActiveLocationsInPrefs();
    }

    public void saveActiveLocationsInPrefs() {
        // Save active locations to prefs
        MyApp.getInstance().prefs.edit().putString(Prefs.PREF_ACTIVE_LOCATIONS, getSelectedLocationsUids()).apply();
    }

    public void removeSelectedLocation(String locationUid) {
        selectedLocationsUidsArrayList.remove(locationUid);
    }

    public interface OnOverflowItemClickListener {
        void onOverflowItemClick(View v, String locationUid);
    }
}
