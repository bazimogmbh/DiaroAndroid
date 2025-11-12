package com.pixelcrater.Diaro.entries.viewedit;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.settings.PreferencesHelper;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.generaldialogs.ConfirmDialog;
import com.pixelcrater.Diaro.generaldialogs.OptionsDialog;
import com.pixelcrater.Diaro.layouts.QustomDialogBuilder;
import com.pixelcrater.Diaro.locations.LocationAddEditActivity;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyDevice;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.MyDateTimeUtils;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

public class PhotoDetailsDialog extends DialogFragment implements OnMapReadyCallback {
    // State vars
    private static final String FILE_PATH_STATE_KEY = "FILE_PATH_STATE_KEY";

    private View customView;
    private String filePath;
    private String html;
    private DateTime dt;
    private String formattedDate;
    private String formattedLocation;
    private ExifInterface exif;
    private double[] latLong;

    private int MAP_ZOOM = 15;

    private GoogleMap googleMap;
    private ViewGroup entryMapView;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AppLog.d("");

        if (savedInstanceState != null) {
            filePath = savedInstanceState.getString(FILE_PATH_STATE_KEY);
        }

        // Use the Builder class for convenient dialog construction
        QustomDialogBuilder builder = new QustomDialogBuilder(getActivity());

        // Color
        builder.setHeaderBackgroundColor(MyThemesUtils.getPrimaryColorCode());

        // Icon
        builder.setIcon(R.drawable.ic_info_white_24dp);

        // Title
        builder.setTitle(getActivity().getResources().getString(R.string.details));

        // Set custom view
        builder.setCustomView(R.layout.photo_details_dialog);
        customView = builder.getCustomView();

        builder.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        TextView detailsTextView = (TextView) customView.findViewById(R.id.details);
        detailsTextView.setMovementMethod(LinkMovementMethod.getInstance());
        // Correctly works onClick after on rotate
        detailsTextView.setSaveEnabled(false);

        html = "<b>" + getString(R.string.file_path) + "</b>: " + filePath;

        readExif();

        setTextViewHTML(detailsTextView, html);

        // Map view
        entryMapView = (ViewGroup) customView.findViewById(R.id.photo_details_map_view);

        setupGoogleMap(savedInstanceState);

        // Restore active dialog listeners
        restoreDialogListeners(savedInstanceState);

        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Prevent dismiss on touch outside
        getDialog().setCanceledOnTouchOutside(false);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void restoreDialogListeners(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            ConfirmDialog dialog1 = (ConfirmDialog) getChildFragmentManager().findFragmentByTag(Static.DIALOG_CONFIRM_SAVE_AS_ENTRY_DATE);
            if (dialog1 != null) {
                setSaveAsEntryDateConfirmDialogListener(dialog1);
            }

            OptionsDialog dialog2 = (OptionsDialog) getChildFragmentManager().findFragmentByTag(Static.DIALOG_LOCATION_OPTIONS);
            if (dialog2 != null) {
                setLocationOptionsDialogListener(dialog2);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(FILE_PATH_STATE_KEY, filePath);
    }

    private void readExif() {
        try {
            exif = new ExifInterface(filePath);

            // File size
            String fileSize = new File(filePath).exists() ?
                    Static.readableFileSize(new File(filePath).length()) : getString(R.string.unknown);
            addToDetails("<b>" + getString(R.string.file_size) + "</b>: " + fileSize);

            // Photo size
            addToDetails("<b>" + getString(R.string.photo_size) + "</b>: " + getFormattedImageSize());

            // Device
            addToDetails("<b>" + getString(R.string.device) + "</b>: " + getFormattedDevice(
                    exif.getAttribute(ExifInterface.TAG_MAKE),
                    exif.getAttribute(ExifInterface.TAG_MODEL)));

            // Date taken
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                if(exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)!=null)
                    addToDetails("<b>" + getString(R.string.date_taken) + "</b>: " +
                        getFormattedDate(exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)));
                else
                    addToDetails("<b>" + getString(R.string.date_taken) + "</b>: " +
                            getFormattedDate(exif.getAttribute(ExifInterface.TAG_DATETIME)));
            } else {
                addToDetails("<b>" + getString(R.string.date_taken) + "</b>: " +
                        getFormattedDate(exif.getAttribute(ExifInterface.TAG_DATETIME)));
            }


            addToDetails("<b>" + getString(R.string.location) + "</b>: " + getFormattedLocation());
        } catch (Exception e) {
            AppLog.e("Exception: " + e);

            Static.showToast(String.format("%s: %s", getString(R.string.error), e.getMessage()),
                    Toast.LENGTH_LONG);
        }
    }

    private String getFormattedDate(String dateTime) {
        AppLog.d("dateTime: " + dateTime);

        if (dateTime != null) {
            try {
                DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy:MM:dd kk:mm:ss");
                dt = formatter.parseDateTime(dateTime.substring(0, 19));

                formattedDate = Static.getMonthShortTitle(dt.getMonthOfYear()) + " " +
                        dt.toString("dd, yyyy " + MyDateTimeUtils.getTimeFormatWithSeconds());

                return "<a href=\"date\">" + formattedDate + "</a>";
            } catch (Exception e) {
                AppLog.e("Exception: " + e);
            }
        }

        return getString(R.string.unknown);
    }

    private String getFormattedDevice(String make, String model) {
        return (isNull(make) || isNull(model)) ? getString(R.string.unknown) :
                StringUtils.trim(make) + " " + StringUtils.trim(model);
    }

    private String getFormattedLocation() {
        latLong = exif.getLatLong();

        if (latLong == null) {
            return getString(R.string.unknown);
        }

        AppLog.d("latLong: " + latLong[0] + ", " + latLong[1]);

        double latitude = latLong[0];
        double longitude = latLong[1];
        AppLog.d("latitude: " + latitude + ", longitude: " + longitude);

        if (latitude == 0 || longitude == 0) {
            return getString(R.string.unknown);
        } else {
            formattedLocation = latitude + ", " + longitude;

            return "<a href=\"location\">" + formattedLocation + "</a>";
        }
    }

    private boolean isNull(String s) {
        return s == null || s.equals("null");
    }

    private void addToDetails(String s) {
        html += "<br/><br/>" + s;
    }

    public void setPhotoFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFormattedImageSize() {
        try {
            if (new File(filePath).exists()) {
                int width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0);
                int height = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0);
                DecimalFormat precision = new DecimalFormat("0.00");
                String mp = precision.format((double) (width * height) / (1000 * 1000));

                if (width > 0 && height > 0) {
                    return width + "x" + height + " (" + mp + "MP)";
                } else {
                    // Decode size from bitmap options
                    InputStream in = new FileInputStream(filePath);
                    // decode image size (decode metadata only, not the whole image)
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(in, null, options);
                    in.close();

                    // save width and height
                    width = options.outWidth;
                    height = options.outHeight;

                    if (width > 0 && height > 0) {
                        return width + "x" + height + " (" + mp + "MP)";
                    }
                }
            }
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }

        return getString(R.string.unknown);
    }

    private void setTextViewHTML(TextView textView, String html) {
        CharSequence sequence = Html.fromHtml(html);
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for (URLSpan span : urls) {
            makeLinkClickable(strBuilder, span);
        }
        textView.setText(strBuilder);
    }

    private void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span) {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        ClickableSpan clickable = new ClickableSpan() {
            public void onClick(View view) {
                switch (span.getURL()) {
                    case "date":
                        if (formattedDate != null) {
                            showSaveAsEntryDateConfirmDialog();
                        }
                        break;

                    case "location":
                        if (formattedLocation != null) {
                            showLocationOptionsDialog();
                        }
                        break;
                }
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    private void showSaveAsEntryDateConfirmDialog() {
        String dialogTag = Static.DIALOG_CONFIRM_SAVE_AS_ENTRY_DATE;

        if (getChildFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setTitle(formattedDate);
            dialog.setMessage(getString(R.string.set_as_entry_date));
            dialog.show(getChildFragmentManager(), dialogTag);

            // Set dialog listener
            setSaveAsEntryDateConfirmDialogListener(dialog);
        }
    }

    private void setSaveAsEntryDateConfirmDialogListener(final ConfirmDialog dialog) {
        dialog.setDialogPositiveClickListener(() -> {
            if (!isAdded()) {
                return;
            }

            if (dt != null) {
                // Set date as entry date
                ContentValues cv = new ContentValues();
                cv.put(Tables.KEY_ENTRY_DATE, dt.getMillis());
                cv.put(Tables.KEY_ENTRY_TZ_OFFSET, MyDateTimeUtils.getCurrentTimeZoneOffset(dt.getMillis()));

                MyApp.getInstance().storageMgr.updateRowByUid(Tables.TABLE_ENTRIES, ((PhotoPagerActivity) getActivity()).entryUid, cv);

                Static.showToast(getString(R.string.updated), Toast.LENGTH_SHORT);
            }
        });
    }

    private void showLocationOptionsDialog() {
        String dialogTag = Static.DIALOG_LOCATION_OPTIONS;
        if (getChildFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            OptionsDialog dialog = new OptionsDialog();
            dialog.setTitle(formattedLocation);

            ArrayList<String> items = new ArrayList<>();
            items.add(getString(R.string.show_on_map));
            items.add(getString(R.string.set_as_entry_location));
            dialog.setItemsTitles(items);

            dialog.show(getChildFragmentManager(), dialogTag);

            // Set dialog listener
            setLocationOptionsDialogListener(dialog);
        }
    }

    private void setLocationOptionsDialogListener(OptionsDialog dialog) {
        // Set dialog listener
        dialog.setDialogItemClickListener(which -> {
            // Show on map
            if (which == 0) {
                showOnMap();
            }
            // Set as entry location
            else if (which == 1) {
                startLocationAddEditActivity();
            }
        });
    }

    private void showOnMap() {
        try {
            String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f", latLong[0], latLong[1], latLong[0], latLong[1]);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivityForResult(intent, Static.REQUEST_SHOW_ON_MAP);
        } catch (Exception e) {
            AppLog.e("Exception: " + e);

            String urlAddress = "http://maps.google.com/maps?q=" + latLong[0] + "," + latLong[1];
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlAddress));
            startActivityForResult(intent, Static.REQUEST_SHOW_ON_MAP);
        }
    }

    private void startLocationAddEditActivity() {
        Intent intent = new Intent(getActivity(), LocationAddEditActivity.class);
        intent.putExtra(Static.EXTRA_SKIP_SC, true);

        // Pass latLong as extra
        intent.putExtra("latLong", latLong);

        startActivityForResult(intent, Static.REQUEST_LOCATION_ADDEDIT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result from LocationAddEditActivity
        if (requestCode == Static.REQUEST_LOCATION_ADDEDIT) {
            if (resultCode == Activity.RESULT_OK) {
                Bundle extras = data.getExtras();
                String locationUid = extras.getString("locationUid");

                if (locationUid != null) {
                    // Set location  as entry location
                    ContentValues cv = new ContentValues();
                    cv.put(Tables.KEY_ENTRY_LOCATION_UID, locationUid);
                    MyApp.getInstance().storageMgr.updateRowByUid(Tables.TABLE_ENTRIES, ((PhotoPagerActivity) getActivity()).entryUid, cv);

                    Static.showToast(getString(R.string.updated), Toast.LENGTH_SHORT);
                }
            }
        }
    }


    private void setupGoogleMap(Bundle savedInstanceState) {
        // Check if Google Play Services available on the device
        if (MyDevice.getInstance().isGooglePlayServicesAvailable()) {
            // Google map
            getLayoutInflater(savedInstanceState).inflate(R.layout.mapview_google_lite, entryMapView, true);

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);

            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        googleMap.setMapType(PreferencesHelper.getMapType());

        googleMap.getUiSettings().setAllGesturesEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setIndoorLevelPickerEnabled(false);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(11.0f));

        try {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), MyThemesUtils.getGoogleMapsStyle()));
        } catch (Resources.NotFoundException e) {
        }

        googleMap.setOnMarkerClickListener(marker -> {
            showLocationOptionsDialog();

            return true;
        });

        googleMap.setOnMapClickListener(latLng -> startLocationAddEditActivity());

        if (latLong != null)
            showMarkerOnMap(latLong[0], latLong[1]);
    }


    public void showMarkerOnMap(double latitude, double longitude) {
        // Clear current marker
        googleMap.clear();

        if (latitude != 0 && longitude != 0) {
            LatLng latLng = new LatLng(latitude, longitude);

            entryMapView.setVisibility(View.VISIBLE);

            googleMap.addMarker(new MarkerOptions()
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin))
                    .draggable(false)
                    .position(latLng));

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM));
        } else
            entryMapView.setVisibility(View.GONE);
    }
}
