package com.pixelcrater.Diaro.entries.viewedit;

import android.app.Dialog;
import android.database.Cursor;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.layouts.QustomDialogBuilder;
import com.pixelcrater.Diaro.model.LocationInfo;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.locations.LocationUtils;
import com.pixelcrater.Diaro.utils.MyDateTimeUtils;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.Static;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;


/**
 * Show when User adds a photo or multiple photos in an entry
 * User is show the metadata info of the picture, and asked if
 * he wants to modify entries data to the data from exif in photo
 */
public class PhotoMetadataSuggestionDialog extends DialogFragment implements CompoundButton.OnCheckedChangeListener {

    private TextView dateText, locationText;
    private CheckBox dateCBox, locationCBox;
    private RelativeLayout dateLayout, locationLayout;

    private double[] exifLatLng;
    private DateTime dateTime = null;
    private LocationInfo locationInfo = null;

    public PhotoMetadataSuggestionDialog() {
    }

    public PhotoMetadataSuggestionDialog(String dateTimeString, double[] exifLatLng) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());
            this.dateTime = new DateTime(simpleDateFormat.parse(dateTimeString));
        } catch (Exception e) {

        }

        this.exifLatLng = exifLatLng;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        QustomDialogBuilder builder = new QustomDialogBuilder(getActivity());
        builder.setHeaderBackgroundColor(MyThemesUtils.getPrimaryColorCode());
        builder.setTitle(getString(R.string.modify_entry));

        // Set custom view
        builder.setCustomView(R.layout.photo_metadata_suggestion_dialog);
        View customView = builder.getCustomView();

        dateLayout = customView.findViewById(R.id.dateLayout);
        locationLayout = customView.findViewById(R.id.locationLayout);

        dateText = customView.findViewById(R.id.dateText);
        locationText = customView.findViewById(R.id.locationText);

        dateCBox = customView.findViewById(R.id.dateTimeCBox);
        dateCBox.setOnCheckedChangeListener(this);

        locationCBox = customView.findViewById(R.id.locationCBox);
        locationCBox.setOnCheckedChangeListener(this);

        if (dateTime != null) {
            String dateTimeString = Static.getMonthShortTitle(dateTime.getMonthOfYear()) + " " + dateTime.toString("dd, yyyy " + MyDateTimeUtils.getTimeFormatWithSeconds());
            dateText.setText(dateTimeString);

        } else {
            dateLayout.setVisibility(View.GONE);
        }

        if (exifLatLng != null) {
            exifLatLng = LocationUtils.normalizeLatLng(exifLatLng);
            AppLog.e("exif location : " + " (" + exifLatLng[0] + " , " + exifLatLng[1] + ")");
            locationInfo = new LocationInfo(null, null, null, LocationUtils.getFormattedCoord(exifLatLng[0]), LocationUtils.getFormattedCoord(exifLatLng[1]), 10);

            //  location lat / lng combination must be unique
            String locationUidExisiting = MyApp.getInstance().storageMgr.getSQLiteAdapter().findLocationByLatLng(String.valueOf(exifLatLng[0]), String.valueOf(exifLatLng[1]), "");

            if (!StringUtils.isEmpty(locationUidExisiting)) {
                Cursor locationCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSingleLocationCursorByUid(locationUidExisiting);
                if (locationCursor.getCount() != 0) {
                    locationInfo = new LocationInfo(locationCursor);
                    AppLog.e("database address is - " + locationUidExisiting + " -  " + locationInfo.getLocationTitle());
                } else {
                    decodeAddressAsync();
                }

                locationCursor.close();
            } else {
                decodeAddressAsync();
            }

            locationText.setText(locationInfo.getLocationTitle());

        } else {
            locationLayout.setVisibility(View.GONE);
        }

        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        });

        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            if (onDialogPositiveClickListener != null) {

                if (!dateCBox.isChecked()) {
                    dateTime = null;
                }

                if (!locationCBox.isChecked()) {
                    locationInfo = null;
                }
                onDialogPositiveClickListener.onDialogPositiveClick(dateTime, locationInfo);
            }

        });

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {
        });

        return dialog;
    }

    private void decodeAddressAsync() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                locationInfo = decodeAddress(exifLatLng[0], exifLatLng[1]);
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (locationInfo != null) {
                    AppLog.e("decoded address is - " + locationInfo.getLocationTitle());
                    locationText.setText(locationInfo.getLocationTitle());
                }
            }
        }.execute();
    }


    private LocationInfo decodeAddress(double lat, double lng) {
        LocationInfo locationInfo = new LocationInfo(null, null, null, LocationUtils.getFormattedCoord(exifLatLng[0]), LocationUtils.getFormattedCoord(exifLatLng[1]), 10);
        Geocoder geocoder = new Geocoder(MyApp.getInstance(), Locale.getDefault());
        try {
            List<Address> listAddress = geocoder.getFromLocation(lat, lng, 1);

            if (listAddress != null && listAddress.size() > 0) {
                Address address = listAddress.get(0);
                AppLog.e("address: " + address);

                String formattedAddress = LocationUtils.getFormattedAddress(address);
                String formattedLatitude = LocationUtils.getFormattedCoord(lat);
                String formattedLongitude = LocationUtils.getFormattedCoord(lng);

                locationInfo = new LocationInfo("", "", formattedAddress, formattedLatitude, formattedLongitude, 13);
            } else {
                AppLog.e("My Current loction address" + "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AppLog.e("My Current loction address" + "Canont get Address!");
        }
        return locationInfo;
    }

    private OnDialogPositiveClickListener onDialogPositiveClickListener;

    public void setDialogPositiveClickListener(OnDialogPositiveClickListener l) {
        onDialogPositiveClickListener = l;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.dateTimeCBox) {
            updateTextView(dateText, isChecked);
        }

        if (buttonView.getId() == R.id.locationCBox) {
            updateTextView(locationText, isChecked);
        }
    }

    private void updateTextView(TextView tv, boolean isChecked) {
        if (!isChecked)
            tv.setPaintFlags(locationText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        else
            tv.setPaintFlags(locationText.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
    }

    public interface OnDialogPositiveClickListener {
        void onDialogPositiveClick(DateTime dateTime, LocationInfo locationInfo);
    }

}
