package com.pixelcrater.Diaro.locations;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.cursoradapter.widget.CursorAdapter;

import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.model.LocationInfo;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class LocationsCursorAdapter extends CursorAdapter {

    public static final String NO_LOCATION_UID = "no_location";

    private final LayoutInflater inflater;
    private final int listItemTextColor;
    private final int listSelectedItemTextColor;
    private ArrayList<String> selectedLocationsUidsArrayList = new ArrayList<>();

    // OnOverflowItemClickListener
    private OnOverflowItemClickListener onOverflowItemClickListener;

    public LocationsCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);

        inflater = ((Activity) mContext).getLayoutInflater();
        listItemTextColor = MyThemesUtils.getListItemTextColor();
        listSelectedItemTextColor = MyThemesUtils.getSelectedListItemTextColor();
    }

    public void setOverflowItemClickListener(OnOverflowItemClickListener l) {
        onOverflowItemClickListener = l;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
//        AppLog.d("cursor.getPosition(): " + cursor.getPosition());

        return inflateView(parent);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
//        AppLog.d("cursor.getPosition(): " + cursor.getPosition());

        final LocationInfo locationInfo = new LocationInfo(cursor);
        final LocationViewHolder holder = (LocationViewHolder) view.getTag();

        // Title
        holder.titleTextView.setText(locationInfo.getLocationTitle());

        // Count
        holder.countTextView.setText(String.valueOf(locationInfo.entriesCount));

        if (!StringUtils.equals(locationInfo.uid, NO_LOCATION_UID)) {
            // Overflow
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
//        AppLog.d("selectedLocationsUidsArrayList: " + selectedLocationsUidsArrayList);

            if (!holder.checkboxView.isChecked()) {
                holder.checkboxView.setChecked(true);
                holder.radioButtonView.setChecked(true);
            }
            holder.titleTextView.setTextColor(listSelectedItemTextColor);
            holder.countTextView.setTextColor(listSelectedItemTextColor);
        } else {
            if (holder.checkboxView.isChecked()) {
                holder.checkboxView.setChecked(false);
                holder.radioButtonView.setChecked(false);
            }
            holder.titleTextView.setTextColor(listItemTextColor);
            holder.countTextView.setTextColor(listItemTextColor);
        }

        // Radio button
        holder.checkboxView.setVisibility(View.GONE);
        holder.radioButtonView.setVisibility(View.VISIBLE);
    }

    private View inflateView(ViewGroup parent) {
        final View view = inflater.inflate(R.layout.checkbox_list_item, parent, false);
        view.setTag(new LocationViewHolder(view));
        return view;
    }

    public String getItemUid(int position) {
        Cursor cursor = (Cursor) getItem(position);
        return cursor.getString(cursor.getColumnIndex(Tables.KEY_UID));
    }

    public void setSelectedLocationUid(String locationUid) {
        AppLog.d("locationUid: " + locationUid);

        selectedLocationsUidsArrayList.clear();
        selectedLocationsUidsArrayList.add(locationUid);
    }

    public interface OnOverflowItemClickListener {
        void onOverflowItemClick(View v, String folderUID);
    }

    static class LocationViewHolder {
        final RadioButton radioButtonView;
        final CheckBox checkboxView;
        final TextView titleTextView;
        final TextView countTextView;
        final ImageView overflowView;

        LocationViewHolder(View view) {
            radioButtonView = (RadioButton) view.findViewById(R.id.radio_button);
            checkboxView = (CheckBox) view.findViewById(R.id.checkbox);
            titleTextView = (TextView) view.findViewById(R.id.title);
            countTextView = (TextView) view.findViewById(R.id.count);
            overflowView = (ImageView) view.findViewById(R.id.overflow);
        }
    }
}
