package com.pixelcrater.Diaro.entries.viewedit;

import static com.pixelcrater.Diaro.config.GlobalConstants.PHOTO;
import static com.pixelcrater.Diaro.utils.Static.EXTRA_SKIP_SC;
import static com.pixelcrater.Diaro.utils.Static.REQUEST_CODE_DRAW;
import static com.pixelcrater.Diaro.utils.Static.REQUEST_TEXT_RECOGNITION;
import static com.sandstorm.diary.piceditor.features.collage.photopicker.activity.PickImageActivity.KEY_DATA_RESULT;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.widget.NestedScrollView;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.divyanshu.draw.activity.DrawingActivity;
import com.dropbox.core.util.LangUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.permissionx.guolindev.PermissionX;
import com.pixelcrater.Diaro.BuildConfig;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.analytics.AnalyticsConstants;
import com.pixelcrater.Diaro.config.AppConfig;
import com.pixelcrater.Diaro.config.FontsConfig;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.entries.EntriesStatic;
import com.pixelcrater.Diaro.entries.async.GetEntryPhotosAsync;
import com.pixelcrater.Diaro.entries.async.UpdateCounterAsync;
import com.pixelcrater.Diaro.entries.attachments.AttachmentsStatic;
import com.pixelcrater.Diaro.entries.attachments.EntryPhotosController;
import com.pixelcrater.Diaro.export.ExportOptions;
import com.pixelcrater.Diaro.export.PdfExport;
import com.pixelcrater.Diaro.folders.FolderSelectDialog;
import com.pixelcrater.Diaro.generaldialogs.ConfirmDialog;
import com.pixelcrater.Diaro.generaldialogs.MyDatePickerDialog;
import com.pixelcrater.Diaro.generaldialogs.MyTimePickerDialog;
import com.pixelcrater.Diaro.generaldialogs.OptionsDialog;
import com.pixelcrater.Diaro.locations.GeocodeFromCoordsAsync;
import com.pixelcrater.Diaro.locations.LocationAddEditActivity;
import com.pixelcrater.Diaro.locations.LocationSelectDialog;
import com.pixelcrater.Diaro.model.AttachmentInfo;
import com.pixelcrater.Diaro.model.EntryInfo;
import com.pixelcrater.Diaro.model.FolderInfo;
import com.pixelcrater.Diaro.model.LocationInfo;
import com.pixelcrater.Diaro.model.PersistanceHelper;
import com.pixelcrater.Diaro.model.TagInfo;
import com.pixelcrater.Diaro.moods.MoodSelectDialog;
import com.pixelcrater.Diaro.ocr.TextRecognizerActivity;
import com.pixelcrater.Diaro.settings.PreferencesHelper;
import com.pixelcrater.Diaro.storage.OnStorageDataChangeListener;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.tags.TagsSelectDialog;
import com.pixelcrater.Diaro.templates.TemplateSelectDialog;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyDateTimeUtils;
import com.pixelcrater.Diaro.utils.MyDevice;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.PermissionsUtils;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;
import com.pixelcrater.Diaro.weather.GetWeatherAsync;
import com.sandstorm.diary.piceditor.activities.CollageActivity;
import com.sandstorm.diary.piceditor.features.collage.photopicker.activity.PickImageActivity;
import com.sandstorm.weather.OwmIcons;
import com.sandstorm.weather.WeatherHelper;
import com.sandstorm.weather.WeatherInfo;
import com.sandstorm.weather.WeatherSelectDialog;
import com.yanzhenjie.album.Album;
import com.yanzhenjie.album.AlbumFile;
import com.yariksoffice.lingver.Lingver;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class EntryFragment extends Fragment implements OnClickListener, OnStorageDataChangeListener, OnMapReadyCallback, EntryPhotosController.EntryPhotosCallbacks, GetWeatherAsync.WeatherAsyncCallback {

    private EntryViewEditActivity entryViewEditActivity;
    public String rowUid = null;
    boolean isInEditMode, isFullscreen;
    public EntryInfo entryInfo;
    public LocationInfo locationInfo;

    private View photoPagerLayout;
    private EntryPhotosController photoCtrl;
    private boolean isNewLocation;
    private Bitmap patternBitmap;
    private UpdateCounterAsync updateCounterAsync;
    private GetEntryPhotosAsync getEntryPhotosAsync;
    private GeocodeFromCoordsAsync geocodeFromCoordsAsync;
    private ViewGroup fragmentContainer, fragmentPatternContainer;

    private TextView entryTitleTextView, entryTextTextView;
    public EditText entryTitleEditText, entryTextEditText;

    private View entryFolderColorLine;
    private View shadow;
    private LinearLayout editorTools, aboveTextContainer, belowTextContainer;
    private ImageButton buttonSave, buttonExitFullscreen;

    private LocationManager locationManager;
    private View view;

    private boolean isDetectingLocation;

    private ViewGroup photoThumb1ViewGroup, photoThumb2ViewGroup, photoThumb3ViewGroup, entryAllPhotosViewGroup;
    private TextView allPhotosTextView;
    private ViewGroup smallPhotoThumb1ViewGroup, smallPhotoThumb2ViewGroup, smallPhotoThumb3ViewGroup, smallPhotoThumb4ViewGroup;

    private TextView dateDayField, dateWeekdayField, dateMonthField, dateYearField, dateTimeField;

    private ViewGroup entryTagsView;
    private TextView entryFolderView, entryLocationView, entryMoodView;
    private TextView entryTagsIconView, entryFolderIconView, entryLocationIconView, entryMoodIconView;
    private TextView weatherIconView, weatherTemperatureView, weatherDescriptionView;

    private ViewGroup entryMapView;
    private GoogleMap googleMap;

    private ImageButton undoButton, redoButton;
    private TextView charsCounter, wordsCounter;
    private TextViewUndoRedo entryTitleUndoRedo, entryTextUndoRedo;

    private Typeface mEntryFont, mWeatherFont;

    private FloatingActionButton addPhotoFab;
    private NestedScrollView mainScrollView;
    private boolean folderLineMarginSet = false;

    private boolean isFahrenheit = false;

    private TextToSpeech mTextToSpeech;

    private final Runnable autoSave_r = this::autoSave;

    ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    final int COLLAGE_PICK_IMAGES_RC = 1005;
    final int COLLAGE_RC = 1006;

    public EntryFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.entry_fragment, container, false);
        mEntryFont = PreferencesHelper.getPrefTypeFace(getActivity().getApplicationContext());
        mWeatherFont = Typeface.createFromAsset(getActivity().getAssets(), FontsConfig.FONT_PATH_WEATHERFONT);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AppLog.i(" onActivityCreated rowUid: " + rowUid + ", savedInstanceState: " + savedInstanceState);

        if (PreferencesHelper.isPrefUnitFahrenheit())
            isFahrenheit = true;

        locationManager = (LocationManager) MyApp.getInstance().getSystemService(Activity.LOCATION_SERVICE);
        // Get vars from savedInstanceState
        if (savedInstanceState != null) {
            rowUid = savedInstanceState.getString(ENTRY_UID_STATE_KEY);
        }

        // Main scrollView
        mainScrollView = view.findViewById(R.id.main_entry_scroller);
        mainScrollView.setFillViewport(true);

        //Mini 'add photo' FAB
        addPhotoFab = view.findViewById(R.id.fab_switch_photo_view);
        addPhotoFab.setBackgroundTintList(ColorStateList.valueOf(MyThemesUtils.getAccentColor()));
        addPhotoFab.setRippleColor(MyThemesUtils.getDarkColor(MyThemesUtils.getAccentColorCode()));
        addPhotoFab.setOnClickListener(view -> openAllEntryPhotos());

        // Photo pager
        photoPagerLayout = view.findViewById(R.id.entry_appbar);

        // Layout itemContainer
        fragmentContainer = view.findViewById(R.id.fragment_container);
        fragmentPatternContainer = view.findViewById(R.id.fragment_pattern_container);

        aboveTextContainer = view.findViewById(R.id.above_text_container);
        belowTextContainer = view.findViewById(R.id.below_text_container);

        // Editor tools
        shadow = view.findViewById(R.id.shadow);
        editorTools = view.findViewById(R.id.editor_tools);
        editorTools.setBackgroundResource(MyThemesUtils.getEntryListItemColorResId());

        // Exit fullscreen button
        buttonExitFullscreen = view.findViewById(R.id.button_exit_fullscreen);
        buttonExitFullscreen.setImageResource(MyThemesUtils.getDrawableResId("ic_collapse_%s_24dp"));
        buttonExitFullscreen.setOnClickListener(view -> turnOffFullscreen());

        // Save button
        buttonSave = view.findViewById(R.id.button_save);
        buttonSave.setImageResource(MyThemesUtils.getDrawableResId("ic_done_%s_24dp"));
        buttonSave.setOnClickListener(view -> {
            saveEntry();
            turnOffEditMode();

            if (entryViewEditActivity.clickedEntryUid.equals("")) {
                Static.showToast(getString(R.string.saved), Toast.LENGTH_SHORT);
                entryViewEditActivity.exitActivity(false);
            }
        });

        // Date
        View dateView = view.findViewById(R.id.entry_date);
        dateView.setOnClickListener(this);

        // Time
        View timeLayout = view.findViewById(R.id.entry_time);
        timeLayout.setOnClickListener(this);

        // Entry folder line
        entryFolderColorLine = view.findViewById(R.id.folder_color_line);

        // --- Entry title ---
        // Entry title TextView
        entryTitleTextView = view.findViewById(R.id.entry_title_textview);
        entryTitleTextView.setTypeface(mEntryFont, Typeface.BOLD);

        // Entry title EditText
        entryTitleEditText = view.findViewById(R.id.entry_title_edittext);
        entryTitleEditText.setTypeface(mEntryFont, Typeface.BOLD);
        entryTitleEditText.setHint(getString(R.string.entry_title) + "...");

        // Check if tap to edit enabled in preferences
        if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_TAP_ENTRY_TO_EDIT, true)) {
            setOnTouchListenerToTextView(entryTitleTextView, entryTitleEditText);
        }

        // Set undo/redo for entryTitleEdit
        entryTitleUndoRedo = new TextViewUndoRedo(entryTitleEditText);
        entryTitleUndoRedo.setMaxHistorySize(15);

        entryTitleEditText.setOnFocusChangeListener((view, hasFocus) -> {
            if (entryTitleEditText.isFocused()) {
                // Update undo/redo buttons
                updateUndoRedoButtons(entryTitleUndoRedo);
            }
        });

        // Entry title edit field TextWatcher
        entryTitleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (isInEditMode) {
                    // Update undo/redo buttons
                    updateUndoRedoButtons(entryTitleUndoRedo);

                    // Auto-save after some time
                    postAutoSave();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        // --- Entry text ---

        // Entry text TextView
        entryTextTextView = view.findViewById(R.id.entry_text_textview);
        entryTextTextView.setTypeface(mEntryFont);

        // Entry text EditText
        entryTextEditText = view.findViewById(R.id.entry_text_edittext);
        entryTextEditText.setTypeface(mEntryFont);
        entryTextEditText.setHint(getString(R.string.entry_text) + "...");

        // Check if tap to edit enabled in preferences
        if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_TAP_ENTRY_TO_EDIT, true)) {
            setOnTouchListenerToTextView(entryTextTextView, entryTextEditText);
        }

        // Set undo/redo for entryTextEdit
        entryTextUndoRedo = new TextViewUndoRedo(entryTextEditText);
        entryTextUndoRedo.setMaxHistorySize(15);

        entryTextEditText.setOnFocusChangeListener((view, hasFocus) -> {
            if (entryTextEditText.isFocused()) {
                // Update undo/redo buttons
                updateUndoRedoButtons(entryTextUndoRedo);
            }
        });

        // Entry text edit field TextWatcher
        entryTextEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (isInEditMode) {
                    // Update undo/redo buttons
                    updateUndoRedoButtons(entryTextUndoRedo);

                    // Auto-save after some time
                    postAutoSave();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        // Set detect phone numbers based on preference
        if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_DETECT_PHONE_NUMBERS, true)) {
            entryTitleTextView.setAutoLinkMask(Linkify.ALL);
            entryTextTextView.setAutoLinkMask(Linkify.ALL);

        } else {
            entryTitleTextView.setAutoLinkMask(Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);
            entryTextTextView.setAutoLinkMask(Linkify.EMAIL_ADDRESSES | Linkify.WEB_URLS);
        }

        // Set auto link colors for title and text
        entryTitleTextView.setLinkTextColor(MyThemesUtils.getAccentColor());
        entryTextTextView.setLinkTextColor(MyThemesUtils.getAccentColor());

        // --- Undo/Redo buttons ---
        // Undo button
        undoButton = view.findViewById(R.id.button_undo);
        undoButton.setOnClickListener(view -> {

            if (entryTitleEditText.isFocused()) {
                entryTitleUndoRedo.undo();

                // Update undo/redo buttons
                updateUndoRedoButtons(entryTitleUndoRedo);
            } else if (entryTextEditText.isFocused()) {
                entryTextUndoRedo.undo();

                // update undo/redo buttons
                updateUndoRedoButtons(entryTextUndoRedo);
            }
        });

        // Redo button
        redoButton = view.findViewById(R.id.button_redo);
        redoButton.setOnClickListener(view -> {
            if (entryTitleEditText.isFocused()) {
                entryTitleUndoRedo.redo();

                // Update undo/redo buttons
                updateUndoRedoButtons(entryTitleUndoRedo);
            } else if (entryTextEditText.isFocused()) {
                entryTextUndoRedo.redo();

                // update undo/redo buttons
                updateUndoRedoButtons(entryTextUndoRedo);
            }
        });

        // Chars/words counter
        charsCounter = view.findViewById(R.id.chars_counter);
        wordsCounter = view.findViewById(R.id.words_counter);

        // Entry folder
        entryFolderView = view.findViewById(R.id.entry_folder);
        entryFolderView.setOnClickListener(this);
        entryFolderIconView = view.findViewById(R.id.entry_folder_icon);

        // Entry tags
        entryTagsView = view.findViewById(R.id.entry_tags);
        entryTagsView.setOnClickListener(this);
        entryTagsIconView = view.findViewById(R.id.entry_tags_icon);

        // Entry mood
        entryMoodView = view.findViewById(R.id.entry_mood);
        entryMoodView.setOnClickListener(this);
        entryMoodIconView = view.findViewById(R.id.entry_mood_icon);

        // Entry location
        entryLocationView = view.findViewById(R.id.entry_location);
        entryLocationView.setOnClickListener(this);
        entryLocationIconView = view.findViewById(R.id.entry_location_icon);

        // Entry temprature
        weatherIconView = view.findViewById(R.id.weather_icon);
        weatherTemperatureView = view.findViewById(R.id.weather_temprature);
        weatherDescriptionView = view.findViewById(R.id.weather_description);
        weatherIconView.setTypeface(mWeatherFont);

        RelativeLayout weatherParent = view.findViewById(R.id.entry_weatherParent);
        weatherParent.setOnClickListener(view -> showWeatherDialog());

        // Map view
        entryMapView = view.findViewById(R.id.entry_map_view);
        if (AppConfig.GOOGLE_PLAY_BUILD && MyApp.getInstance().prefs.getBoolean(Prefs.PREF_SHOW_MAP_IN_ENTRY, true)) {
            setupGoogleMap(savedInstanceState);
        }

        // Date fields
        dateDayField = view.findViewById(R.id.date_day);
        dateWeekdayField = view.findViewById(R.id.date_weekday);
        dateMonthField = view.findViewById(R.id.date_month);
        dateYearField = view.findViewById(R.id.date_year);
        dateTimeField = view.findViewById(R.id.time);

        /** dateDayField.setTextColor(Color.parseColor(MyThemesUtils.getAccentColorCode()));
         dateWeekdayField.setTextColor(Color.parseColor(MyThemesUtils.getAccentColorCode()));
         dateMonthField.setTextColor(Color.parseColor(MyThemesUtils.getAccentColorCode()));
         dateYearField.setTextColor(Color.parseColor(MyThemesUtils.getAccentColorCode()));
         dateTimeField.setTextColor(Color.parseColor(MyThemesUtils.getAccentColorCode())); **/

        /**   weatherIconView.setTextColor(Color.parseColor(MyThemesUtils.getAccentColorCode()));
         weatherTemperatureView.setTextColor(Color.parseColor(MyThemesUtils.getAccentColorCode()));
         weatherDescriptionView.setTextColor(Color.parseColor(MyThemesUtils.getAccentColorCode()));**/

        // Entry photos
        photoThumb1ViewGroup = view.findViewById(R.id.photo_thumb1);
        photoThumb2ViewGroup = view.findViewById(R.id.photo_thumb2);
        photoThumb3ViewGroup = view.findViewById(R.id.photo_thumb3);
        entryAllPhotosViewGroup = view.findViewById(R.id.all_entry_photos);
        allPhotosTextView = entryAllPhotosViewGroup.findViewById(R.id.all_photos_text);
        smallPhotoThumb1ViewGroup = entryAllPhotosViewGroup.findViewById(R.id.small_photo_thumb1);
        smallPhotoThumb2ViewGroup = entryAllPhotosViewGroup.findViewById(R.id.small_photo_thumb2);
        smallPhotoThumb3ViewGroup = entryAllPhotosViewGroup.findViewById(R.id.small_photo_thumb3);
        smallPhotoThumb4ViewGroup = entryAllPhotosViewGroup.findViewById(R.id.small_photo_thumb4);

        // Load entry data
        loadEntryData();

        // Create photo layout if needed
        initPhotoLayout();

        // If new entry, detect location automatically
        if (rowUid == null && MyApp.getInstance().prefs.getBoolean(Prefs.PREF_AUTOMATIC_LOCATION, true)) {
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                requestNewLocation();
            } else {
                // Ask for permission
                if (!getActivity().isFinishing()) {
                    PermissionsUtils.askForPermission((AppCompatActivity) getActivity(), Manifest.permission.ACCESS_FINE_LOCATION, Static.PERMISSION_REQUEST_LOCATION, Static.DIALOG_CONFIRM_RATIONALE_LOCATION,
                            R.string.location_permission_rationale_text);
                }
            }
        }

        // Try to get entry extras from shared 3rd party apps intent
        if (rowUid == null && entryViewEditActivity.extras != null) {
            // Get intent, action and MIME type
            Intent intent = entryViewEditActivity.getIntent();
            String action = intent.getAction();
            String type = intent.getType();

            if (type != null && (action.equals(Intent.ACTION_SEND) || action.equals(Intent.ACTION_SEND_MULTIPLE))) {
                createEntryFrom3rdPartyAppSharedInfo(entryViewEditActivity.extras);
            }

            // Widget - Select photo
            if (entryViewEditActivity.extras.getBoolean("selectPhoto")) {
                showPhotoChooser();
            }

            // Widget - Capture photo with camera
            if (entryViewEditActivity.extras.getBoolean("capturePhoto")) {
                takePhotoWithCamera();
            }
        }

        // Go to edit mode if new entry or it is set in extras
        if (rowUid == null || (savedInstanceState == null && entryViewEditActivity.activityState.startedFromWidget)) {
            // Go to entry edit mode
            if (PreferencesHelper.isTitleEnabled()) {
                turnOnEditMode(entryTitleEditText, 0);
            } else {
                turnOnEditMode(entryTextEditText, 0);
            }
        }

        // Return to edit mode if it was active before rotate or before changing fragment position
        restoreEditMode(savedInstanceState);

        // Restore entry title and text undo/redo history
        restoreUndoRedoHistory(savedInstanceState);

        updateFieldsFromPrefs();

        MyApp.getInstance().storageMgr.addOnStorageDataChangeListener(this);

        if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ENTRY_PHOTOS_POSITION, true)) {
            if (entryInfo.photoCount > 0)
                entryTextTextView.setMinLines(5);
            else
                entryTextTextView.setMinLines(10);
        }

        entryViewEditActivity.supportInvalidateOptionsMenu();
    }

    private void updateFieldsFromPrefs() {
        // check if mood is enabled
        if (!PreferencesHelper.isMoodsEnabled()) {
            entryMoodView.setVisibility(View.GONE);
            entryMoodIconView.setVisibility(View.GONE);
        }

        // check if weather is enabled
        if (!PreferencesHelper.isWeatherEnabled()) {
            weatherDescriptionView.setVisibility(View.GONE);
            weatherIconView.setVisibility(View.GONE);
            weatherTemperatureView.setVisibility(View.GONE);
        }

        // check if title is enabled
        if (!PreferencesHelper.isTitleEnabled()) {
            entryTitleTextView.setVisibility(View.GONE);
            entryTitleEditText.setVisibility(View.GONE);
        }
    }

    private void setupGoogleMap(Bundle savedInstanceState) {
        // Check if Google Play Services available on the device
        if (MyDevice.getInstance().isGooglePlayServicesAvailable()) {
            // Google map
            getLayoutInflater(savedInstanceState).inflate(R.layout.mapview_google_lite, entryMapView, true);

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);

            Objects.requireNonNull(mapFragment).getMapAsync(this);
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
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(13.0f));

        try {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireActivity(), MyThemesUtils.getGoogleMapsStyle()));
        } catch (Resources.NotFoundException ignored) {
        }

        googleMap.setOnMarkerClickListener(marker -> {
            startLocationAddEditActivity();

            return true;
        });

        googleMap.setOnMapClickListener(latLng -> startLocationAddEditActivity());

        updateLocationInUI();
    }

    private void startLocationAddEditActivity() {
        if (entryViewEditActivity.activityState.isActivityPaused) {
            return;
        }

        turnOffEditMode();

        Intent intent = new Intent(getActivity(), LocationAddEditActivity.class);
        intent.putExtra(EXTRA_SKIP_SC, true);
        intent.putExtra("locationUid", entryInfo.locationUid);
        startActivityForResult(intent, Static.REQUEST_LOCATION_ADDEDIT);
    }

    private void setOnTouchListenerToTextView(final TextView textView, final EditText editText) {
        textView.setOnTouchListener(new View.OnTouchListener() {
            private boolean allowFocus = true;
            private float x = 0;
            private float y = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    allowFocus = true;
                    x = event.getX();
                    y = event.getY();
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (Math.abs(event.getX() - x) > entryViewEditActivity.touchOffset || Math.abs(event.getY() - y) > entryViewEditActivity.touchOffset) {
                        allowFocus = false;
                    }
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    int offset = EntryFragmentHelper.getOffset(v, event);

                    // Check if clicked on link
                    String linkOnCursor = EntryFragmentHelper.getLinkOnCursor(textView, offset);
                    AppLog.d("linkOnCursor: " + linkOnCursor);

                    if (linkOnCursor != null) {
                        EntryFragmentHelper.openDetectedLink(entryViewEditActivity, linkOnCursor);
                        // showDetectedLinkDialog(linkOnCursor);
                    } else if (allowFocus) {
                        // Go to edit mode
                        turnOnEditMode(editText, offset);
                    }
                }

                return true;
            }

        });
    }

    private void loadEntryData() {
        AppLog.d("rowUid: " + rowUid);

        // New entry
        if (rowUid == null) {
            entryInfo = new EntryInfo();

            // Set folder to activeFolderUid;
            entryInfo.setFolderUid(entryViewEditActivity.activeFolderUid);

            if (!entryInfo.folderUid.equals("")) {
                Cursor folderCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSingleFolderCursorByUid(entryInfo.folderUid);
                if (folderCursor.getCount() != 0) {
                    FolderInfo folderInfo = new FolderInfo(folderCursor);
                    entryInfo.setFolderTitle(folderInfo.title);
                    entryInfo.setFolderColor(folderInfo.color);
                    entryInfo.setFolderPattern(folderInfo.pattern);
                }
                folderCursor.close();
            }

            // Apply Tags
            //AppLog.e("Tags->" + entryViewEditActivity.activeTags);
            ArrayList<TagInfo> entryTagsArrayList = new ArrayList<>();
            String[] entryTagsArray = entryViewEditActivity.activeTags.split(",");

            StringBuilder entryTags = new StringBuilder();
            for (String tagUid : entryTagsArray) {
                if (!tagUid.equals("")) {
                    Cursor tagCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSingleTagCursorByUid(tagUid);

                    // If found
                    if (tagCursor.getCount() > 0) {
                        entryTags.append(",").append(tagUid);

                        TagInfo o = new TagInfo(tagCursor);
                        entryTagsArrayList.add(o);
                    }
                    tagCursor.close();
                }
            }
            if (!entryTags.toString().equals("")) {
                entryTags.append(",");
            }

            if (!entryInfo.tags.equals(entryTags.toString()) || entryInfo.tagCount != entryTagsArrayList.size()) {
                entryInfo.tags = entryTags.toString();
                entryInfo.tagCount = entryTagsArrayList.size();
            }

            // Apply date
            if (entryViewEditActivity.activeDate > 0) {
                // Set date to selected calendar day
                entryInfo.unixEpochMillis = entryViewEditActivity.activeDate;
                entryInfo.tzOffset = MyDateTimeUtils.getCurrentTimeZoneOffset(entryInfo.unixEpochMillis);
            }

            if (entryInfo.unixEpochMillis > 0) {
                if (MyDateTimeUtils.isTodayDate(entryInfo.unixEpochMillis)) {
                    entryInfo.unixEpochMillis = 0;
                } else {
                    // Use 12:00:00 as a time for not today is selected in calendar
                    entryInfo.unixEpochMillis = entryInfo.getLocalDt().withTime(12, 0, 0, 0).getMillis();
                }
            }
        }
        // View entry
        else {
            // Get entry row
            Cursor entryCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSingleEntryCursorByUid(rowUid, false);
            if (entryCursor.getCount() == 0) {
                entryCursor.close();

                // Use try/catch, because crashes with:
                // Fatal Exception: java.lang.IllegalStateException: Recursive entry to executePendingTransactions
                try {
                    entryViewEditActivity.entriesCursorPagerAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    AppLog.e("Exception: " + e);
                }
                return;
            } else {
                entryInfo = new EntryInfo(entryCursor);
                entryCursor.close();

                // Create locationInfo object
                if (!entryInfo.getLocationTitle().equals("")) {
                    locationInfo = new LocationInfo(entryInfo.locationUid, entryInfo.locationTitle, entryInfo.locationAddress, entryInfo.locationLatitude, entryInfo.locationLongitude, entryInfo.locationZoom);
                }

                if (isInEditMode) {
                    entryInfo.title = getEntryTitleEditTextValue();
                    entryInfo.text = getEntryTextEditTextValue();
                } else {
                    // Update EditText fields
                    setupTextFields(true);
                }

                // Get entry photos
                executeGetEntryPhotosAsync();
            }
        }

        // If date not set
        if (entryInfo.unixEpochMillis == 0) {
            entryInfo.unixEpochMillis = DateTime.now().getMillis();
            entryInfo.tzOffset = MyDateTimeUtils.getCurrentTimeZoneOffset(entryInfo.unixEpochMillis);
        }

        // Update entry date, folder, tags, location, mood, temperature in UI
        updateDateAndTimeInUI();
        updateFolderInUI();
        updateTagsInUi();
        updateLocationInUI();
        updateMoodInUI();
        updateTempratureInUi();
    }

    private void putCursorToPosition(EditText focusedEditText, int cursorPos) {
        //  int currentScrollPosition = mainScrollView.getScrollY();
        if (cursorPos < 0) {
            cursorPos = 0;
        } else if (cursorPos > focusedEditText.length()) {
            cursorPos = focusedEditText.length();
        }
        try {
            focusedEditText.setSelection(cursorPos);
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }
    }

    public void highLightText(String text) {

        if (!TextUtils.isEmpty(text))
            hidePhotosAndFAB();
        else
            restorePhotosAndFAB();
        Static.highlightSearchText(entryTitleTextView, entryInfo.title, text);
        Static.highlightSearchText(entryTextTextView, entryInfo.text, text);
    }

    private void setupTextFields(boolean skipUndo) {
        AppLog.d("rowUid: " + rowUid);
        // Edit mode
        if (isInEditMode) {
            // Change entry title TextView to EditText
            entryTitleTextView.setText("");
            if (skipUndo) {
                entryTitleUndoRedo.mIsUndoOrRedo = true;
            }
            entryTitleEditText.setText(entryInfo.title);
            if (skipUndo) {
                entryTitleUndoRedo.mIsUndoOrRedo = false;
            }

            // Change entry text TextView to EditText
            entryTextTextView.setText("");
            if (skipUndo) {
                entryTextUndoRedo.mIsUndoOrRedo = true;
            }

            entryTextEditText.setText(entryInfo.text);
            if (skipUndo) {
                entryTextUndoRedo.mIsUndoOrRedo = false;
            }

            entryTitleTextView.setVisibility(View.GONE);
            entryTextTextView.setVisibility(View.GONE);

            if (PreferencesHelper.isTitleEnabled()) {
                entryTitleEditText.setVisibility(View.VISIBLE);
            }

            entryTextEditText.setVisibility(View.VISIBLE);
        }
        // View mode
        else {
            // Change entry title EditText to TextView
            Static.highlightSearchText(entryTitleTextView, entryInfo.title, entryViewEditActivity.searchKeyword);

            entryTitleUndoRedo.mIsUndoOrRedo = true;
            entryTitleEditText.setText("");
            entryTitleUndoRedo.mIsUndoOrRedo = false;

            // Change entry text EditText to TextView
            Static.highlightSearchText(entryTextTextView, entryInfo.text, entryViewEditActivity.searchKeyword);
            entryTextUndoRedo.mIsUndoOrRedo = true;
            entryTextEditText.setText("");
            entryTextUndoRedo.mIsUndoOrRedo = false;

            entryTitleEditText.setVisibility(View.GONE);
            entryTextEditText.setVisibility(View.GONE);

            if (entryInfo.title.equals("") || !PreferencesHelper.isTitleEnabled()) {
                entryTitleTextView.setVisibility(View.GONE);
            } else {
                entryTitleTextView.setVisibility(View.VISIBLE);
            }
            entryTextTextView.setVisibility(View.VISIBLE);
        }

    }

    private void postAutoSave() {
        MyApp.getInstance().handler.removeCallbacks(autoSave_r);
        MyApp.getInstance().handler.postDelayed(autoSave_r, 1000);
    }

    private void autoSave() {
        if (isInEditMode && titleOrTextChanged()) {
            AppLog.d("rowUid: " + rowUid);
            MyApp.getInstance().handler.removeCallbacks(autoSave_r);
            saveEntry();
        }
    }

    private boolean titleOrTextChanged() {
        return !getEntryTitleEditTextValue().equals(entryInfo.title) || !getEntryTextEditTextValue().equals(entryInfo.text);
    }

    private void updateCountersInBackground() {
        // Cancel task if running
        try {
            if (updateCounterAsync != null) {
                updateCounterAsync.cancel(true);
            }
        } catch (Exception e) {
            AppLog.d("Exception: " + e);
        }

        updateCounterAsync = new UpdateCounterAsync(entryViewEditActivity, getEntryTitleEditTextValue(), getEntryTextEditTextValue(), charsCounter, wordsCounter);
        updateCounterAsync.execute();
    }

    private void updateUndoRedoButtons(TextViewUndoRedo undoRedo) {
        if (!isAdded() || entryInfo == null) {
            return;
        }

        undoButton.setEnabled(false);
        undoButton.setImageResource(R.drawable.ic_undo_grey600_disabled_24dp);
        redoButton.setEnabled(false);
        redoButton.setImageResource(R.drawable.ic_redo_grey600_disabled_24dp);

        if (undoRedo.getCanUndo()) {
            undoButton.setEnabled(true);
            undoButton.setImageResource(R.drawable.ic_undo_grey600_24dp);
            MyThemesUtils.setTint(undoButton);
        }

        if (undoRedo.getCanRedo()) {
            redoButton.setEnabled(true);
            redoButton.setImageResource(R.drawable.ic_redo_grey600_24dp);
            MyThemesUtils.setTint(redoButton);
        }

        // Update chars/words counters
        updateCountersInBackground();
    }

    @Override
    public void onStop() {
        super.onStop();
        AppLog.d("onStop " + "rowUid:" + rowUid);
        stopLocationUpdate();
        cancelGeocodeFromCoordsAsync();
        // Recycle background pattern bitmap
        recycleBackgroundBitmap();
    }

    private void recycleBackgroundBitmap() {
        if (patternBitmap != null) {
            AppLog.d("patternBitmap: " + patternBitmap);
            patternBitmap.recycle();
            patternBitmap = null;
            System.gc();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // AppLog.e("onDestroy " +" rowUid: " + rowUid + ", inEditMode: " + isInEditMode);
        MyApp.getInstance().storageMgr.removeOnStorageDataChangeListener(this);
        if (mTextToSpeech != null) {
            mTextToSpeech.shutdown();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //  AppLog.e("rowUid: " + rowUid + ", inEditMode: " + isInEditMode + ", entryTitleUndoRedo.mIsUndoOrRedo: " + entryTitleUndoRedo.mIsUndoOrRedo);
        autoSave();

        stopReadingTxt();
    }

    @Override
    public void onResume() {
        super.onResume();
        AppLog.d("rowUid: " + rowUid + ", inEditMode: " + isInEditMode);

        if (entryViewEditActivity.isFinishing()) {
            return;
        }

        // Update entry folder in UI
        updateFolderInUI();
    }

    private void restoreEditMode(Bundle savedInstanceState) {
//		AppLog.d("rowUid: " + rowUid);

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(IN_EDIT_MODE_STATE_KEY)) {
                EditText focusEditText = entryTitleEditText;
                int cursorPos = savedInstanceState.getInt(FOCUSED_FIELD_CURSOR_POS_STATE_KEY);
                AppLog.d("cursorPos: " + cursorPos);

                if (savedInstanceState.getString(FOCUSED_FIELD_STATE_KEY) != null && savedInstanceState.getString(FOCUSED_FIELD_STATE_KEY).equals("entryText")) {
                    focusEditText = entryTextEditText;
                }

                // Go to entry edit mode
                turnOnEditMode(focusEditText, cursorPos);

                if (savedInstanceState.getBoolean(IS_FULLSCREEN_STATE_KEY)) {
                    turnOnFullscreen();
                }
            }
        }
    }

    /**
     * Restores entry title and text undo/redo history
     */
    private void restoreUndoRedoHistory(Bundle savedInstanceState) {
        entryTitleUndoRedo.mIsUndoOrRedo = false;
        entryTextUndoRedo.mIsUndoOrRedo = false;

        if (savedInstanceState != null) {
            // Restore entry title and text undo redo
            entryTitleUndoRedo.restorePersistentState(savedInstanceState, ENTRY_TITLE_PREFIX_STATE_KEY);
            entryTextUndoRedo.restorePersistentState(savedInstanceState, ENTRY_TEXT_PREFIX_STATE_KEY);

            if (entryViewEditActivity.getCurrentFocus() == entryTitleEditText) {
                // Update undo/redo buttons
                updateUndoRedoButtons(entryTitleUndoRedo);
            } else if (entryViewEditActivity.getCurrentFocus() == entryTextEditText) {
                // Update undo/redo buttons
                updateUndoRedoButtons(entryTextUndoRedo);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.entry_date) {
            showDatePickerDialog();
        } else if (id == R.id.entry_time) {
            showTimePickerDialog();
        } else if (id == R.id.entry_folder) {
            showFolderSelectDialog();
        } else if (id == R.id.entry_location) {
            showLocationSelectDialog();
        } else if (id == R.id.entry_tags) {
            showTagsSelectDialog();
        } else if (id == R.id.entry_mood) {
            showMoodSelectedDialog();
        }
    }

    private void hidePhotosAndFAB() {
        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) mainScrollView.getLayoutParams();
        marginParams.bottomMargin = Static.getPixelsFromDip(48);
        if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ENTRY_PHOTOS_POSITION, true)) {
            photoCtrl.toggleNestedScroll(false);
        }
    }

    private void restorePhotosAndFAB() {
        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) mainScrollView.getLayoutParams();
        marginParams.bottomMargin = 0;
        if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ENTRY_PHOTOS_POSITION, true) && entryInfo.photoCount > 0) {
            photoCtrl.toggleFillVisibility(false);
            photoCtrl.toggleNestedScroll(true);
            photoCtrl.collapseExpandLayout(false, false);
        }
    }

    void turnOnEditMode(EditText focusEditText, int cursorPos) {
        stopReadingTxt();

        AppLog.d("rowUid: " + rowUid + ", focusEditText: " + focusEditText + ", cursorPos: " + cursorPos);
        isInEditMode = true;

        //Hide photoPager and FAB
        hidePhotosAndFAB();
        if (rowUid != null && MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ENTRY_PHOTOS_POSITION, true)) {
            photoCtrl.collapseExpandLayout(false, true);
        }

        // Show editor tools
        shadow.setVisibility(View.VISIBLE);
        editorTools.setVisibility(View.VISIBLE);

        setupTextFields(true);

        // Disable pager swiping
        entryViewEditActivity.viewPager.setSwipingEnabled(false);

        // Put cursor to position
        putCursorToPosition(focusEditText, cursorPos);

        // Show keyboard
        Static.showSoftKeyboard(focusEditText);

        // Hide banner
        entryViewEditActivity.activityState.hideBanner();
        entryViewEditActivity.activityState.isBannerAllowed = false;

        // Redraw action bar
        entryViewEditActivity.supportInvalidateOptionsMenu();
    }

    private int getToolbarHeight() {
        TypedValue tv = new TypedValue();
        return entryViewEditActivity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true) ? TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics()) : 0;
    }

    void turnOnFullscreen() {
        isFullscreen = true;

        if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ENTRY_PHOTOS_POSITION, true)) {
            photoCtrl.hideLayout();
        } else {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mainScrollView.getLayoutParams();
            params.topMargin = 0;
        }

        aboveTextContainer.setVisibility(View.GONE);
        belowTextContainer.setVisibility(View.GONE);
        buttonExitFullscreen.setVisibility(View.VISIBLE);

        if (entryViewEditActivity.getSupportActionBar() != null) {
            entryViewEditActivity.getSupportActionBar().hide();
        }

        // Redraw action bar
        entryViewEditActivity.supportInvalidateOptionsMenu();
    }

    private void turnOffFullscreen() {
        isFullscreen = false;

        if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ENTRY_PHOTOS_POSITION, true)) {
            photoCtrl.showLayout();
        } else {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mainScrollView.getLayoutParams();
            params.topMargin = getToolbarHeight() + (StringUtils.isNotEmpty(entryInfo.folderColor) ? Static.getPixelsFromDip(6) : 0);

        }

        aboveTextContainer.setVisibility(View.VISIBLE);
        belowTextContainer.setVisibility(View.VISIBLE);
        buttonExitFullscreen.setVisibility(View.GONE);

        if (entryViewEditActivity.getSupportActionBar() != null) {
            entryViewEditActivity.getSupportActionBar().show();
        }

        // Redraw action bar
        entryViewEditActivity.supportInvalidateOptionsMenu();
    }

    private String getEntryTitleEditTextValue() {
        return entryTitleEditText.getText().toString();
    }

    private String getEntryTextEditTextValue() {
        return entryTextEditText.getText().toString();
    }

    private void clearFocus() {
        // Hide keyboard / clear focus
        if (entryTitleEditText.isFocused()) {
            Static.hideSoftKeyboard(entryTitleEditText);
            entryTitleEditText.clearFocus();
        }
        if (entryTextEditText.isFocused()) {
            Static.hideSoftKeyboard(entryTextEditText);
            entryTextEditText.clearFocus();
        }
    }

    void turnOffEditMode() {
        if (!isInEditMode) {
            return;
        }

        autoSave();
        isInEditMode = false;

        clearFocus();

        // Hide editor tools
        shadow.setVisibility(View.GONE);
        editorTools.setVisibility(View.GONE);

        turnOffFullscreen();

        restorePhotosAndFAB();

        setupTextFields(true);

        // Enable pager swiping
        entryViewEditActivity.viewPager.setSwipingEnabled(true);

        // Show banners
        entryViewEditActivity.activityState.showHideBanner();

        // Redraw action bar
        entryViewEditActivity.supportInvalidateOptionsMenu();

    }

    private void saveEntry() {
        AppLog.i("saveEntry rowUid: " + rowUid);

        if (isInEditMode) {
            entryInfo.title = getEntryTitleEditTextValue();
            entryInfo.text = getEntryTextEditTextValue();
        }

        ContentValues cv = new ContentValues();

        // Add archived field
        cv.put(Tables.KEY_ENTRY_ARCHIVED, entryInfo.archived);
        // Save title
        cv.put(Tables.KEY_ENTRY_TITLE, entryInfo.title);
        // Save text
        cv.put(Tables.KEY_ENTRY_TEXT, entryInfo.text);
        // Save date
        cv.put(Tables.KEY_ENTRY_DATE, entryInfo.unixEpochMillis);
        cv.put(Tables.KEY_ENTRY_TZ_OFFSET, entryInfo.tzOffset);
        // Save entry folder
        cv.put(Tables.KEY_ENTRY_FOLDER_UID, entryInfo.folderUid);
        // Save entry location
        if (isNewLocation && locationInfo != null) {
            saveLocation();
            isNewLocation = false;
        }
        cv.put(Tables.KEY_ENTRY_LOCATION_UID, entryInfo.locationUid);
        // Save entry tags
        cv.put(Tables.KEY_ENTRY_TAGS, entryInfo.tags);

        cv.put(Tables.KEY_ENTRY_MOOD_UID, entryInfo.moodUid);

        if (entryInfo.weatherInfo != null) {
            if (entryInfo.weatherInfo.getTemperature() > -122 && entryInfo.weatherInfo.getTemperature() < 122) {
                cv.put(Tables.KEY_ENTRY_WEATHER_TEMPERATURE, entryInfo.weatherInfo.getTemperature());
                cv.put(Tables.KEY_ENTRY_WEATHER_ICON, entryInfo.weatherInfo.getIcon());
                cv.put(Tables.KEY_ENTRY_WEATHER_DESCRIPTION, entryInfo.weatherInfo.getDescription());
            }

        } else {
            // TODO: check this
            cv.put(Tables.KEY_ENTRY_WEATHER_TEMPERATURE, 0.0);
            cv.put(Tables.KEY_ENTRY_WEATHER_ICON, "");
            cv.put(Tables.KEY_ENTRY_WEATHER_DESCRIPTION, "");
        }

        // we will use KEY_SYNC_ID as last edited from now on, truncate the last 4
        long lastEdited = LangUtil.truncateMillis(new Date()).getTime();
        cv.put(Tables.KEY_SYNC_ID, String.valueOf(lastEdited));
        cv.put(Tables.KEY_SYNCED, 0);

        // Insert new entry
        if (rowUid == null) {
            // Generate uid
            cv.put(Tables.KEY_UID, Static.generateRandomUid());

            String uid = MyApp.getInstance().storageMgr.insertRow(Tables.TABLE_ENTRIES, cv);
            if (uid != null) {
                AppLog.i("INSERTED uid: " + uid);
                rowUid = uid;
            }
        }
        // Update current entry
        else {
            MyApp.getInstance().storageMgr.updateRowByUid(Tables.TABLE_ENTRIES, rowUid, cv);
        }


        // Redraw action bar
        entryViewEditActivity.supportInvalidateOptionsMenu();

//		AppLog.d("entryViewEditActivity: " + entryViewEditActivity + ", isAdded(): " + isAdded());
    }

    private void saveLocation() {
        entryInfo.locationUid = PersistanceHelper.saveLocation(locationInfo, false);
    }

    @RequiresApi(19)
    void printPdf() {
        String layout = PreferencesHelper.getExportLayout();
        String photoHeight = PreferencesHelper.getExportPhotoHeight();

        ExportOptions options = new ExportOptions(layout, photoHeight);

        ArrayList<String> uids = new ArrayList<>();
        uids.add(rowUid);

        PdfExport.export(uids, options, null, getActivity());
    }


    void appendTimestamp() {
        entryViewEditActivity.activityState.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_APPEND_TIMESTAMP);

        // Time
        String dateHM = new DateTime().toString(MyDateTimeUtils.getTimeFormat());
        if (entryTitleEditText.isFocused()) {
            int pos = entryTitleEditText.getSelectionStart();
            entryTitleEditText.getText().insert(pos, dateHM);
        } else {
            int pos = entryTextEditText.getSelectionStart();
            entryTextEditText.getText().insert(pos, dateHM);
        }
    }

    void startDrawing() {
        entryViewEditActivity.activityState.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_DRAW);
        Intent intent = new Intent(getActivity(), DrawingActivity.class);
        startActivityForResult(intent, REQUEST_CODE_DRAW);
    }

    void startTextRecognition() {
        entryViewEditActivity.activityState.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_TEXT_RECOGNITION);
        Intent intent = new Intent(getActivity(), TextRecognizerActivity.class);
        startActivityForResult(intent, REQUEST_TEXT_RECOGNITION);
    }

    void startTextToSpeech() {
        final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, requireActivity().getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1000);
        AppLog.e(Lingver.getInstance().getLanguage());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Lingver.getInstance().getLanguage());
        try {
            startActivityForResult(intent, Static.REQUEST_SPEECH_TO_TXT);
        } catch (ActivityNotFoundException a) {
            Static.showToastError("Recogniser not present on Device!");
        }
    }

    void readText() {
        if (mTextToSpeech == null) {
            mTextToSpeech = new TextToSpeech(getActivity(), status -> {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTextToSpeech.setLanguage(Lingver.getInstance().getLocale());
                    if (result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA) {
                        Static.showToastError("Error : Language not supported");
                    } else {
                        read();
                    }
                } else
                    Static.showToastError("Error : TTS failed. Status : " + status);
            }, null);
        } else {
            read();
        }
    }

    public void read() {
        if (mTextToSpeech == null) {
            return;
        }
        if (mTextToSpeech.isLanguageAvailable(Lingver.getInstance().getLocale()) == 0) {
            String textToRead = entryTitleTextView.getText() + " " + entryTextTextView.getText();

            int dividerLimit = 3900;
            /* MAX_SPEECH_ITEM_CHAR_LENGTH = 4000 in TtsService.java */
            if (textToRead.length() < dividerLimit) {
                mTextToSpeech.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null);
            } else {
                int textLength = textToRead.length();
                List<String> texts = new ArrayList<>();
                int count = textLength / dividerLimit + ((textLength % dividerLimit == 0) ? 0 : 1);
                int start = 0;
                int end = textToRead.indexOf(" ", dividerLimit);
                for (int i = 1; i <= count; i++) {
                    texts.add(textToRead.substring(start, end));
                    start = end;
                    if ((start + dividerLimit) < textLength) {
                        end = textToRead.indexOf(" ", start + dividerLimit);
                    } else {
                        end = textLength;
                    }
                }
                for (int i = 0; i < texts.size(); i++) {
                    mTextToSpeech.speak(texts.get(i), TextToSpeech.QUEUE_ADD, null);
                }
            }

        } else {
            Intent installTTSIntent = new Intent();
            installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            startActivity(installTTSIntent);
        }

    }

    private void stopReadingTxt() {
        if (mTextToSpeech != null) {
            if (mTextToSpeech.isSpeaking()) {
                mTextToSpeech.stop();
            }
        }
    }

    void shareEntry() {
        turnOffEditMode();
        EntryFragmentHelper.shareEntry(getActivity(), entryInfo);
    }

    private void showDatePickerDialog() {
        if (!isAdded() || entryInfo == null || isActivityPaused(entryViewEditActivity)) {
            return;
        }
        turnOffEditMode();

        String dialogTag = Static.DIALOG_PICKER_DATE;
        if (getParentFragmentManager().findFragmentByTag(dialogTag) == null) {
            MyDatePickerDialog dialog = new MyDatePickerDialog();
            DateTime localDt = entryInfo.getLocalDt();
            dialog.setSelectedDate(localDt.getYear(), localDt.getMonthOfYear(), localDt.getDayOfMonth());
            dialog.setShowTodayButton(true);
            dialog.show(getParentFragmentManager(), dialogTag);
            setDatePickerDialogListener(dialog);
        }
    }

    void setDatePickerDialogListener(MyDatePickerDialog dialog) {
        dialog.setDialogDateSetListener((year, month, day) -> {
            if (!isAdded() || entryInfo == null) {
                return;
            }
            DateTime localDt = entryInfo.getLocalDt();
            AppLog.d("year: " + year + ", month: " + month + ", day: " + day);
            AppLog.d("localDt.getYear(): " + localDt.getYear() + ", localDt.getMonthOfYear(): " + localDt.getMonthOfYear() + ", localDt.getDayOfMonth(): " + localDt.getDayOfMonth());
            if (localDt.getYear() != year || localDt.getMonthOfYear() != month || localDt.getDayOfMonth() != day) {
                getMillisAndUpdateDate(year, month, day);
            }
        });
    }

    private void showTimePickerDialog() {
        if (!isAdded() || entryInfo == null || isActivityPaused(entryViewEditActivity)) {
            return;
        }
        turnOffEditMode();

        String dialogTag = Static.DIALOG_PICKER_TIME;
        if (getParentFragmentManager().findFragmentByTag(dialogTag) == null) {
            MyTimePickerDialog dialog = new MyTimePickerDialog();
            DateTime localDt = entryInfo.getLocalDt();
            dialog.setSelectedTime(localDt.getHourOfDay(), localDt.getMinuteOfHour());
            dialog.show(getParentFragmentManager(), dialogTag);
            setTimePickerDialogListener(dialog);
        }
    }

    void setTimePickerDialogListener(MyTimePickerDialog dialog) {
        dialog.setDialogTimeSetListener((hour, minute) -> {
            if (!isAdded() || entryInfo == null) {
                return;
            }
            AppLog.d("hour: " + hour + ", minute: " + minute);
            DateTime localDt = entryInfo.getLocalDt();
            if (localDt.getHourOfDay() != hour || localDt.getMinuteOfHour() != minute) {
                getMillisAndUpdateTime(hour, minute);
            }
        });
    }

    void showEntryDuplicateConfirmDialog() {
        AppLog.d("rowUid: " + rowUid);
        if (!isAdded() || entryInfo == null || isActivityPaused(entryViewEditActivity)) {
            return;
        }
        turnOffEditMode();

        String dialogTag = Static.DIALOG_CONFIRM_ENTRY_DUPLICATE;
        if (getParentFragmentManager().findFragmentByTag(dialogTag) == null) {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setCustomString(rowUid);
            dialog.setTitle(getString(R.string.duplicate));
            dialog.setMessage(getString(R.string.confirm_entry_duplicate));
            dialog.show(getParentFragmentManager(), dialogTag);
            setEntryDuplicateConfirmDialogListener(dialog);
        }
    }

    void setEntryDuplicateConfirmDialogListener(final ConfirmDialog dialog) {
        dialog.setDialogPositiveClickListener(() -> {
            if (!isAdded() || entryInfo == null) {
                return;
            }
            duplicate();
        });
    }

    void showWeatherAlertOptions() {
        if (!isAdded() || entryInfo == null || isActivityPaused(entryViewEditActivity)) {
            return;
        }

        if (entryInfo.weatherInfo == null) {
            showWeatherDialog();
        } else {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
            builder.setTitle(getString(R.string.edit_weather) + "?");
            if (entryInfo.weatherInfo != null) {
                builder.setMessage(entryInfo.weatherInfo.getTemperature() + ", " + WordUtils.capitalize(entryInfo.weatherInfo.getDescription()));
                builder.setNeutralButton(R.string.clear, (dialog, which) -> {
                    entryInfo.weatherInfo = null;
                    updateTempratureInUi();
                    saveEntry();
                });
            }

            builder.setPositiveButton(R.string.edit, (dialog, which) -> showWeatherDialog());
            builder.setNegativeButton(android.R.string.cancel, null);
            builder.show();
        }
    }

    void showTemplatesDialog() {
        if (!isAdded() || entryInfo == null || isActivityPaused(entryViewEditActivity)) {
            return;
        }

        entryViewEditActivity.activityState.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_TEMPLATE_SELECT);
        String dialogTag = Static.DIALOG_TEMPLATE_SELECT;
        if (getParentFragmentManager().findFragmentByTag(dialogTag) == null) {
            TemplateSelectDialog dialog = new TemplateSelectDialog();
            dialog.show(getParentFragmentManager(), dialogTag);
            setTemplateSelectDialogListener(dialog);
        }
    }

    private void setTemplateSelectDialogListener(TemplateSelectDialog dialog) {
        dialog.setOnDialogItemClickListener(template -> {
            if (template != null) {
                entryTitleEditText.append(template.getTitle());
                entryTextEditText.append(template.getText());
            }
        });
    }

    private void showWeatherDialog() {
        if (!isAdded() || entryInfo == null || isActivityPaused(entryViewEditActivity)) {
            return;
        }
        turnOffEditMode();

        entryViewEditActivity.activityState.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_WEATHER_SELECT);

        String dialogTag = Static.DIALOG_SELECT_WEATHER;
        if (getParentFragmentManager().findFragmentByTag(dialogTag) == null) {
            String suffix = WeatherHelper.STRING_CELSIUS;
            if (isFahrenheit) {
                suffix = WeatherHelper.STRING_FAHRENHEIT;
            }
            boolean isDay = true;
            DateTime dateTime = entryInfo.getLocalDt();
            int hour = dateTime.getHourOfDay();
            if (hour <= 6)
                isDay = false;
            if (hour >= 18)
                isDay = false;

            WeatherSelectDialog dialog = new WeatherSelectDialog(MyThemesUtils.getPrimaryColorCode(), suffix, isDay, entryInfo.weatherInfo, getString(R.string.clear));
            dialog.show(getParentFragmentManager(), dialogTag);
            setWeatherSelectDialogListener(dialog);
        }
    }

    private void setWeatherSelectDialogListener(WeatherSelectDialog dialog) {
        dialog.setDialogWeatherSelectedListener((weatherInfo) -> {
            if (!isAdded() || entryInfo == null) {
                return;
            }
            this.entryInfo.weatherInfo = weatherInfo;
            updateTempratureInUi();
            saveEntry();
            //AppLog.e("temperature: " + weatherInfo.getTemperature() + ", icon: " + weatherInfo.getIcon() + ", description" + weatherInfo.getDescription());
        });
    }

    void showEntryDeleteConfirmDialog() {
        AppLog.d("rowUid: " + rowUid);
        if (!isAdded() || entryInfo == null || isActivityPaused(entryViewEditActivity)) {
            return;
        }
        turnOffEditMode();
        String dialogTag = Static.DIALOG_CONFIRM_ENTRY_DELETE;
        if (getParentFragmentManager().findFragmentByTag(dialogTag) == null) {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setCustomString(rowUid);
            dialog.setTitle(getString(R.string.delete));
            dialog.setMessage(getString(R.string.delete_entry));
            dialog.show(getParentFragmentManager(), dialogTag);
            setEntryDeleteConfirmDialogListener(dialog);
        }
    }

    void setEntryDeleteConfirmDialogListener(final ConfirmDialog dialog) {
        dialog.setDialogPositiveClickListener(() -> {
            if (!isAdded() || entryInfo == null) {
                return;
            }

            ArrayList<String> entriesUids = new ArrayList<>();
            entriesUids.add(dialog.getCustomString());
            try {
                EntriesStatic.archiveEntries(entriesUids);
            } catch (Exception e) {
                AppLog.e("Exception: " + e);
            }

            // Exit activity
            entryViewEditActivity.exitActivity(true);
        });
    }

    private void updateFolderInUI() {
        if (!isAdded() || entryInfo == null) {
            return;
        }

        // Default entry folder params
        int folderColor = -1;
        int patternPosition = 0;

        // If folder is not set to the entry
        if (entryInfo.folderTitle.equals("")) {
            String text = getActivity().getString(R.string.select_folder);
            entryFolderView.setText(text);
            entryFolderView.setTextColor(MyThemesUtils.getListItemTextColor());
            entryFolderIconView.setTextColor(MyThemesUtils.getListItemTextColor());
        } else {
            String text = entryInfo.folderTitle;
            entryFolderView.setText(text); //  + " " + entryInfo.folderUid
            entryFolderView.setTextColor(MyThemesUtils.getSelectedListItemTextColor());
            entryFolderIconView.setTextColor(MyThemesUtils.getSelectedListItemTextColor());

            try {
                if (StringUtils.isNotEmpty(entryInfo.folderColor)) {
                    folderColor = Color.parseColor(entryInfo.folderColor);
                }
            } catch (Exception e) {
                AppLog.d("Exception: " + e);
            }

            patternPosition = Static.getPatternPosition(entryInfo.folderPattern);
        }

//        AppLog.d("folderColor: " + folderColor);
        if (folderColor == -1) {
            // Hide folder color line
            entryFolderColorLine.setVisibility(View.GONE);
            if (!MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ENTRY_PHOTOS_POSITION, true)) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mainScrollView.getLayoutParams();
                if (params.topMargin != getToolbarHeight()) {
                    params.topMargin = getToolbarHeight();
                }
            }
        } else {
            // Set entry folder line color
            entryFolderColorLine.setBackgroundColor(folderColor);
            entryFolderColorLine.setVisibility(View.VISIBLE);
            if (!MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ENTRY_PHOTOS_POSITION, true)) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mainScrollView.getLayoutParams();
                params.topMargin = getToolbarHeight() + Static.getPixelsFromDip(6);
                if (!folderLineMarginSet) {
                    ViewGroup.MarginLayoutParams folderParams = (ViewGroup.MarginLayoutParams) entryFolderColorLine.getLayoutParams();
                    folderParams.topMargin = getToolbarHeight();
                    folderLineMarginSet = true;
                }
            }
        }

        // Set background color
        Static.setBgColorWithAlpha(folderColor, fragmentContainer);
        // Recycle background pattern bitmap
        recycleBackgroundBitmap();
        // Set background pattern
        patternBitmap = Static.setPattern(entryViewEditActivity, patternPosition, fragmentPatternContainer);
        // Set entry text size
        setEntryTextSize();
    }

    private void updateTagsInUi() {
        if (!isAdded() || entryInfo == null) {
            return;
        }

        ArrayList<TagInfo> entryTagsArrayList = new ArrayList<>();
        String[] entryTagsArray = entryInfo.tags.split(",");
        //AppLog.e("entryInfo.tags: " + entryInfo.tags);
        //AppLog.d("BEFORE stateTagsUids: " + stateTagsUids + "; count: " + (count - 1));

        StringBuilder entryTags = new StringBuilder();
        for (String tagUid : entryTagsArray) {
            if (!tagUid.equals("")) {
                Cursor tagCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSingleTagCursorByUid(tagUid);

                // If found
                if (tagCursor.getCount() > 0) {
                    entryTags.append(",").append(tagUid);

                    TagInfo o = new TagInfo(tagCursor);
                    entryTagsArrayList.add(o);
                }
                tagCursor.close();
            }
        }
        if (!entryTags.toString().equals("")) {
            entryTags.append(",");
        }

        if (!entryInfo.tags.equals(entryTags.toString()) || entryInfo.tagCount != entryTagsArrayList.size()) {
            entryInfo.tags = entryTags.toString();
            entryInfo.tagCount = entryTagsArrayList.size();

            saveEntry();
        }

        // AppLog.d("AFTER stateTagsUids: " + stateTags + ", stateTagsCount: " + stateTagCount);
        // Sort tags by title
        Collections.sort(entryTagsArrayList, new Static.ComparatorTags());

        entryTagsView.removeAllViews();
        // Show/hide tags layout
        if (entryInfo.tagCount == 0) {
            String text = getActivity().getString(R.string.select_tags);

            TextView tag = new TextView(getActivity(), null, R.style.EntryParamTextView);
            tag.setText(text);
            //  tag.setBackgroundResource(R.drawable.textview_background);
            tag.setTextColor(MyThemesUtils.getListItemTextColor());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

            tag.setLayoutParams(params);
            entryTagsView.addView(tag);
            entryTagsIconView.setTextColor(MyThemesUtils.getListItemTextColor());
        } else {

            for (TagInfo tagInfo : entryTagsArrayList) {
                TextView tag = new TextView(getActivity(), null, R.style.EntryParamTextView);
                tag.setText(tagInfo.title);
                tag.setTextColor(MyThemesUtils.getSelectedListItemTextColor());
                tag.setBackgroundResource(R.drawable.textview_background);
                //  tag.setPadding(22, 6, 22, 6);

                GradientDrawable drawable = (GradientDrawable) tag.getBackground();
                drawable.setStroke(2, MyThemesUtils.getSelectedListItemTextColor());
                //     int backgroundColor = ContextCompat.getColor(requireActivity(), MyThemesUtils.getTagBackgroundColorResId());
                //   drawable.setColor(backgroundColor);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 18, 0);
                tag.setLayoutParams(params);

                entryTagsView.addView(tag);

            }
            entryTagsIconView.setTextColor(MyThemesUtils.getSelectedListItemTextColor());
        }
    }

    private void stopLocationUpdate() {
        isDetectingLocation = false;
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (Exception e) {
                AppLog.e("Exception: " + e);
            }
        }
        if (locationInfo == null) {
            String text = getActivity().getString(R.string.select_location);
            entryLocationView.setText(text);
        }
    }

    void requestNewLocation() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        String text = getActivity().getString(R.string.searching_location);
        entryLocationView.setText(text);
        // For faster location detection use NETWORK_PROVIDER first
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
            isDetectingLocation = true;
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
            isDetectingLocation = true;
        }
    }

    private void showMyCurrentLocation(Location location) {
        isDetectingLocation = false;
        if (location == null) {
            String text = getActivity().getString(R.string.select_location);
            entryLocationView.setText(text);
        } else {
            executeGeocodeFromCoordsAsync(location.getLatitude(), location.getLongitude());

            fetchWeatherForLocation(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
        }
    }

    private void fetchWeatherForLocation(String latitude, String longitude) {
        if (PreferencesHelper.isWeatherEnabled()) {
            if (entryInfo.unixEpochMillis > 0) {
                if (MyDateTimeUtils.isTodayDate(entryInfo.unixEpochMillis)) {
                    //  AppLog.e(" same day");
                    if (StringUtils.isEmpty(latitude)) {
                        MyApp.getInstance().asyncsMgr.executeGetWeatherAsync(this, null, null);
                    } else {
                        MyApp.getInstance().asyncsMgr.executeGetWeatherAsync(this, latitude, longitude);
                    }

                } else {
                    AppLog.e(" other day");
                }
            } else {
                AppLog.e(" other day 2");
            }
        }
    }

    private void executeGeocodeFromCoordsAsync(double latitude, double longitude) {
        cancelGeocodeFromCoordsAsync();
        geocodeFromCoordsAsync = new GeocodeFromCoordsAsync(EntryFragment.this, latitude, longitude);
        // Execute on a separate thread
        Static.startMyTask(geocodeFromCoordsAsync);
    }

    private void cancelGeocodeFromCoordsAsync() {
        try {
            if (geocodeFromCoordsAsync != null) {
                geocodeFromCoordsAsync.cancel(true);
            }
        } catch (Exception e) {
            AppLog.d("Exception: " + e);
        }
    }

    public void onAddressGeocode(LocationInfo locationInfo, boolean isNewLocation) {
        AppLog.d("locationInfo: " + locationInfo + ", isNewLocation: " + isNewLocation);
        this.isNewLocation = isNewLocation;
        this.locationInfo = locationInfo;
        // If location was found in database
        if (!isNewLocation) {
            entryInfo.locationUid = locationInfo.uid;
        }
        updateLocationInUI();
    }

    private void updateLocationInUI() {
        if (!isAdded() || entryInfo == null) {
            return;
        }
        AppLog.d("googleMap: " + googleMap + ", locationInfo: " + locationInfo);
        entryMapView.setVisibility(View.GONE);
        // If location is not set to the entry
        if (locationInfo == null) {
            if (!isDetectingLocation) {
                String text = getActivity().getString(R.string.select_location);
                entryLocationView.setText(text);

                // this.entryInfo.weatherInfo = null;
                //  updateTempratureInUi();
            }
            entryLocationView.setTextColor(MyThemesUtils.getListItemTextColor());
            entryLocationIconView.setTextColor(MyThemesUtils.getListItemTextColor());
        } else {
            String text = locationInfo.getLocationTitle();
            entryLocationView.setText(text);
            entryLocationView.setTextColor(MyThemesUtils.getSelectedListItemTextColor());
            entryLocationIconView.setTextColor(MyThemesUtils.getSelectedListItemTextColor());

            // Check if we were successful in obtaining the map
            if (googleMap != null) {
                entryMapView.setVisibility(View.VISIBLE);
                AppLog.d("locationInfo.latitude: " + locationInfo.latitude + ", locationInfo.longitude: " + locationInfo.longitude + ", locationInfo.zoom: " + locationInfo.zoom);
                if (!locationInfo.latitude.equals("") && !locationInfo.longitude.equals("")) {
                    try {
                        double latitude = Double.parseDouble(locationInfo.latitude);
                        double longitude = Double.parseDouble(locationInfo.longitude);
                        showMarkerOnMap(latitude, longitude);

                    } catch (NumberFormatException e) {
                        Static.showToast(e.getMessage(), Toast.LENGTH_LONG);
                    }

                } else {
                    resetMap();
                }
            }
        }
    }

    private void showMarkerOnMap(double latitude, double longitude) {
        // Clear current marker
        googleMap.clear();

        if (latitude != 0 && longitude != 0) {
            LatLng latLng = new LatLng(latitude, longitude);

            googleMap.addMarker(new MarkerOptions().draggable(false).position(latLng));  //.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, locationInfo.zoom));
        }
    }

    private void resetMap() {
        // Clear current marker
        googleMap.clear();

        LatLng latLng = new LatLng(0, 0);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 0));
    }

    private void updateDateAndTimeInUI() {
        if (!isAdded() || entryInfo == null) {
            return;
        }

        DateTime localDt = entryInfo.getLocalDt();
        // Day of month
        String dateD = Static.getDigitWithFrontZero(localDt.getDayOfMonth());
        dateDayField.setText(dateD);

        // Day of week
        int dayOfWeek = localDt.getDayOfWeek();
        String dateWD = Static.getDayOfWeekTitle(Static.convertDayOfWeekFromJodaTimeToCalendar(dayOfWeek));
        dateWeekdayField.setText(dateWD);

        // Month
        int month = localDt.getMonthOfYear();
        String dateM = Static.getMonthTitle(month);
        dateMonthField.setText(dateM);

        // Year
        String dateY = String.valueOf(localDt.getYear());
        dateYearField.setText(dateY);

        // Time
        String dateHM = localDt.toString(MyDateTimeUtils.getTimeFormat());
        dateTimeField.setText(dateHM);
    }

    private void updateMoodInUI() {
        if (!isAdded() || entryInfo == null) {
            return;
        }

        if (entryInfo.moodTitle == null || entryInfo.moodTitle.isEmpty()) {
            entryMoodView.setText(getActivity().getString(R.string.select_mood));
            entryMoodView.setTextColor(MyThemesUtils.getListItemTextColor());
            entryMoodIconView.setTextColor(MyThemesUtils.getListItemTextColor());
        } else {
            entryMoodView.setText(entryInfo.moodTitle);
            entryMoodView.setTextColor(MyThemesUtils.getSelectedListItemTextColor());
            entryMoodIconView.setTextColor(MyThemesUtils.getSelectedListItemTextColor());
        }
    }

    private void updateTempratureInUi() {
        if (!isAdded() || entryInfo == null) {
            return;
        }
        if (entryInfo.weatherInfo != null) {
            if (!entryInfo.weatherInfo.getIcon().isEmpty() && !entryInfo.weatherInfo.getDescription().isEmpty()) {
                String fontName = OwmIcons.getFontCode(entryInfo.weatherInfo.getIcon());
                weatherIconView.setText(fontName);

                double temp = entryInfo.weatherInfo.getTemperature();
                String suffix = WeatherHelper.STRING_CELSIUS;
                if (isFahrenheit) {
                    temp = WeatherHelper.celsiusToFahrenheit(temp);
                    suffix = WeatherHelper.STRING_FAHRENHEIT;
                }
                weatherTemperatureView.setText(String.format("%s%s", String.format("%.1f", temp), suffix));
                if (entryInfo.weatherInfo.getLocalizedDescription() == 0) {
                    weatherDescriptionView.setText(WordUtils.capitalize(entryInfo.weatherInfo.getDescription()));
                } else {
                    weatherDescriptionView.setText(entryInfo.weatherInfo.getLocalizedDescription());
                }
            }
        } else {
            weatherIconView.setText("");
            weatherTemperatureView.setText("");
            weatherDescriptionView.setText("");
        }
    }

    // Gets entry extras from shared 3rd party apps intent
    private void createEntryFrom3rdPartyAppSharedInfo(Bundle extras) {
        AppLog.e("extras: " + extras);
        // Take subject from extras
        String entryTitle = extras.getString(Intent.EXTRA_SUBJECT);
        if (entryTitle != null) {
            entryInfo.title = entryTitle;
        }

        // Take text from extras
        String entryText = extras.getString(Intent.EXTRA_TEXT);
        if (entryText != null) {
            entryInfo.text = entryText;
        }

        // - SINGLE PHOTO -
        Uri singleUri = extras.getParcelable(Intent.EXTRA_STREAM);
        AppLog.d("singleUri: " + singleUri);

        // Single photo
        String photoPath = Static.getPhotoFilePathFromUri(singleUri);
        AppLog.d("photoPath: " + photoPath);

        boolean atLeastOnePhotoCopied = false;

        try {
            savePhoto(photoPath, false);
            atLeastOnePhotoCopied = true;
            showDateTimeSuggestionDialog(photoPath);
        } catch (Exception e) {
            AppLog.d("Exception: " + e);
        }

        if (photoPath == null) {
            // - MULTIPLE PHOTOS -
            // Take several photos from extras
            ArrayList<Uri> multipleUris = extras.getParcelableArrayList(Intent.EXTRA_STREAM);
            AppLog.d("multipleUris.size(): " + (multipleUris == null ? "null" : multipleUris.size()));

            try {
                // Save photos
                if (multipleUris != null) {
                    // saveEntry(false);
                    for (Uri uri : multipleUris) {
                        photoPath = Static.getPhotoFilePathFromUri(uri);

                        if (showDateTimeSuggestionDialog(photoPath)) {
                            break;
                        }
                    }

                    for (Uri uri : multipleUris) {
                        photoPath = Static.getPhotoFilePathFromUri(uri);
                        try {
                            savePhoto(photoPath, false);
                            atLeastOnePhotoCopied = true;
                        } catch (Exception e) {
                            AppLog.d("Exception: " + e);
                        }
                    }
                }
            } catch (Exception e) {
                AppLog.d("Exception: " + e);
            }
        }

        if (entryTitle != null || entryText != null || atLeastOnePhotoCopied) {
            setupTextFields(true);
            saveEntry();

            onStorageDataChange();
        } else {
            entryViewEditActivity.exitActivity(false);
        }
    }

    private void setEntryTextSize() {
        // Normal text size
        int textSize = 16;

        int textSizeFromPrefs = MyApp.getInstance().prefs.getInt(Prefs.PREF_TEXT_SIZE, Prefs.SIZE_NORMAL);
        if (textSizeFromPrefs == Prefs.SIZE_SMALL) {
            textSize -= 2;
        } else if (textSizeFromPrefs == Prefs.SIZE_LARGE) {
            textSize += 2;
        } else if (textSizeFromPrefs == Prefs.SIZE_X_LARGE) {
            textSize += 4;
        }

        entryTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        entryTitleEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        entryTextTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        entryTextEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
    }

    public void takePhotoWithCamera() {
        if (entryViewEditActivity.activityState.isActivityPaused) {
            return;
        }
        turnOffEditMode();
        File file = new File(requireActivity().getExternalCacheDir() + "/image.jpg");
        try {
            Uri photoURI = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", file);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, Static.REQUEST_TAKE_PHOTO);
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }
    }

    private void showPhotoChooser() {
        if (entryViewEditActivity.activityState.isActivityPaused) {
            return;
        }
        turnOffEditMode();

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getText(R.string.select_photo)), Static.REQUEST_SELECT_PHOTO);
    }

    void startMultiplePhotoPickerActivityNew() {
        if (entryViewEditActivity.activityState.isActivityPaused) {
            return;
        }
        turnOffEditMode();

        /**    pickMedia.launch(new PickVisualMediaRequest.Builder()
         .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
         .build());**/

        Album.image(this).multipleChoice().camera(true).columnCount(3).selectCount(50).onResult(result -> {
                    if (MyApp.getInstance().securityCodeMgr.isSecurityCodeSet()) {
                        MyApp.getInstance().securityCodeMgr.setUnlocked();
                    }

                    ArrayList<String> selectedPhotosPathsArrayList = new ArrayList<>();
                    for (AlbumFile albumFile : result) {
                        selectedPhotosPathsArrayList.add(albumFile.getPath());
                    }

                    for (int i = selectedPhotosPathsArrayList.size() - 1; i >= 0; i--) {
                        if (showDateTimeSuggestionDialog(selectedPhotosPathsArrayList.get(i))) {
                            break;
                        }
                    }

                    for (String photoPath : selectedPhotosPathsArrayList) {
                        AppLog.d("photoPath: " + photoPath);
                        try {
                            savePhoto(photoPath, false);
                        } catch (Exception e) {
                            AppLog.e("Exception: " + e);
                            // Show error
                            Static.showToast(String.format("%s: %s", getString(R.string.error_add_photo), e.getMessage()), Snackbar.LENGTH_LONG);
                        }
                    }
                    if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ENTRY_PHOTOS_POSITION, true)) {
                        photoCtrl.setNewPhotosAdded();
                    }

                    onStorageDataChange();
                })
                .onCancel(result -> {
                })
                .start();

    }

    private void handleMultiselectImage(ArrayList<Uri> selectedPhotosPathsArrayList) {
        if (MyApp.getInstance().securityCodeMgr.isSecurityCodeSet()) {
            MyApp.getInstance().securityCodeMgr.setUnlocked();
        }

        for (int i = selectedPhotosPathsArrayList.size() - 1; i >= 0; i--) {
            if (showDateTimeSuggestionDialog2(selectedPhotosPathsArrayList.get(i))) {
                break;
            }
        }

        for (Uri photoPath : selectedPhotosPathsArrayList) {
            AppLog.d("photoPath: " + photoPath);
            try {
                savePhoto2(photoPath);
            } catch (Exception e) {
                AppLog.e("Exception: " + e);
                // Show error
                Static.showToast(String.format("%s: %s", getString(R.string.error_add_photo), e.getMessage()), Snackbar.LENGTH_LONG);
            }
        }
        if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ENTRY_PHOTOS_POSITION, true)) {
            photoCtrl.setNewPhotosAdded();
        }

        onStorageDataChange();
    }

    private boolean isActivityPaused(EntryViewEditActivity activity) {
        return activity != null && activity.activityState.isActivityPaused;
    }

    void showAddPhotoOptionsDialog() {
        if (!isAdded() || entryInfo == null || isActivityPaused(entryViewEditActivity)) {
            return;
        }
        turnOffEditMode();

        String dialogTag = Static.DIALOG_ADD_PHOTO;
        if (getParentFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            OptionsDialog dialog = new OptionsDialog();
            dialog.setTitle(getString(R.string.add_photo));

            ArrayList<String> items = new ArrayList<>();
            items.add(getString(R.string.take_photo));
            items.add(getString(R.string.collage));
            items.add(getString(R.string.select_photo));
            items.add(getString(R.string.select_multiple_photos));

            dialog.setItemsTitles(items);
            dialog.show(getParentFragmentManager(), dialogTag);
            setAddPhotoOptionsDialogListener(dialog);
        }
    }

    void setAddPhotoOptionsDialogListener(OptionsDialog dialog) {
        // Set dialog listener
        dialog.setDialogItemClickListener(which -> {

            // Take photo
            if (which == 0) {
                PermissionX.init(this)
                        .permissions(Manifest.permission.CAMERA)
                        .request((allGranted, grantedList, deniedList) -> takePhotoWithCamera());
            }

            // Collage
            if (which == 1) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PermissionX.init(this)
                            .permissions(Manifest.permission.READ_MEDIA_IMAGES)
                            .request((allGranted, grantedList, deniedList) -> startCollage());
                } else {
                    PermissionX.init(this)
                            .permissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                            .request((allGranted, grantedList, deniedList) -> startCollage());
                }
            }

            // Select photo
            else if (which == 2) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PermissionX.init(this)
                            .permissions(Manifest.permission.READ_MEDIA_IMAGES)
                            .request((allGranted, grantedList, deniedList) -> showPhotoChooser());
                } else {
                    PermissionX.init(this)
                            .permissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                            .request((allGranted, grantedList, deniedList) -> showPhotoChooser());
                }
            }

            // Select multiple images
            else if (which == 3) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PermissionX.init(this)
                            .permissions(Manifest.permission.READ_MEDIA_IMAGES)
                            .request((allGranted, grantedList, deniedList) -> startMultiplePhotoPickerActivityNew());
                } else {
                    PermissionX.init(this)
                            .permissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                            .request((allGranted, grantedList, deniedList) -> startMultiplePhotoPickerActivityNew());
                }
            }
        });
    }

    private void startCollage() {
        Intent intent = new Intent(getActivity(), PickImageActivity.class);
        intent.putExtra(PickImageActivity.KEY_LIMIT_MAX_IMAGE, 9);
        intent.putExtra(PickImageActivity.KEY_LIMIT_MIN_IMAGE, 2);
        startActivityForResult(intent, COLLAGE_PICK_IMAGES_RC);
        entryViewEditActivity.activityState.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_COLLAGE_PICKER_VIEW);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AppLog.e("---" + requestCode + ", " + resultCode + " , " + data);
        if (requestCode != Static.REQUEST_SHARE_ENTRY) {
            MyApp.getInstance().securityCodeMgr.setUnlocked();
        } else {
            MyApp.getInstance().securityCodeMgr.setLocked();
        }

        switch (requestCode) {
            case Static.REQUEST_TAKE_PHOTO:
                try {
                    savePhoto(requireActivity().getExternalCacheDir() + "/image.jpg", true);
                    if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ENTRY_PHOTOS_POSITION, true)) {
                        photoCtrl.setNewPhotosAdded();
                    }

                    onStorageDataChange();
                } catch (Exception e) {
                    AppLog.d("Exception: " + e);
                }

                // Delete cache directory
                AppLifetimeStorageUtils.deleteCacheDir();

                if (MyApp.getInstance().securityCodeMgr.isSecurityCodeSet()) {
                    MyApp.getInstance().securityCodeMgr.setUnlocked();
                }
                break;

            // Result from photo select app
            case Static.REQUEST_SELECT_PHOTO:

                if (MyApp.getInstance().securityCodeMgr.isSecurityCodeSet()) {
                    MyApp.getInstance().securityCodeMgr.setUnlocked();
                }

                if (resultCode == Activity.RESULT_OK) {
                    if (data.getClipData() == null) {
                        Uri uri = data.getData();
                        if (uri != null) {
                            String photoPath = Static.getPhotoFilePathFromUri(uri);
                            showDateTimeSuggestionDialog(photoPath);
                            try {
                                savePhoto(photoPath, false);
                                if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ENTRY_PHOTOS_POSITION, true)) {
                                    photoCtrl.setNewPhotosAdded();
                                }
                                onStorageDataChange();
                            } catch (Exception e) {
                                Static.showToast(String.format("%s: %s", getString(
                                        R.string.error_add_photo), e.getMessage()), Toast.LENGTH_SHORT);
                            }
                        }
                    } else {
                        ClipData mClipData = data.getClipData();
                        int count = mClipData.getItemCount();
                        AppLog.e("multiple ->" + count);
                        ArrayList<String> selectedPhotosPathsArrayList = new ArrayList<>();
                        for (int i = 0; i < count; i++) {
                            Uri uri = data.getClipData().getItemAt(i).getUri();
                            selectedPhotosPathsArrayList.add(Static.getPhotoFilePathFromUri(uri));
                        }
                        for (int i = selectedPhotosPathsArrayList.size() - 1; i >= 0; i--) {
                            if (showDateTimeSuggestionDialog(selectedPhotosPathsArrayList.get(i))) {
                                break;
                            }
                        }
                        for (String photoPath : selectedPhotosPathsArrayList) {
                            AppLog.d("photoPath: " + photoPath);
                            try {
                                savePhoto(photoPath, false);
                            } catch (Exception e) {
                                AppLog.e("Exception: " + e);
                                // Show error
                                Static.showToast(String.format("%s: %s", getString(R.string.error_add_photo), e.getMessage()), Snackbar.LENGTH_LONG);
                            }
                        }
                        if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ENTRY_PHOTOS_POSITION, true)) {
                            photoCtrl.setNewPhotosAdded();
                        }
                        onStorageDataChange();
                    }
                }

                break;

            // Result from photo pager activity
            case Static.REQUEST_PHOTO_PAGER:
                // Result from photo grid activity
            case Static.REQUEST_PHOTO_GRID:
                executeGetEntryPhotosAsync();
                break;
            case Static.REQUEST_SPEECH_TO_TXT:
                if (MyApp.getInstance().securityCodeMgr.isSecurityCodeSet()) {
                    MyApp.getInstance().securityCodeMgr.setUnlocked();
                }
                if (resultCode == Activity.RESULT_OK) {
                    List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (results != null && results.size() > 0) {
                        String spokenText = results.get(0) + " ";
                        if (entryTitleEditText.isFocused()) {
                            int pos = entryTitleEditText.getSelectionStart();
                            entryTitleEditText.getText().insert(pos, spokenText);
                        } else {
                            int pos = entryTextEditText.getSelectionStart();
                            entryTextEditText.getText().insert(pos, spokenText);
                        }
                    }
                }
                break;

            case REQUEST_CODE_DRAW:
                try {
                    if (MyApp.getInstance().securityCodeMgr.isSecurityCodeSet()) {
                        MyApp.getInstance().securityCodeMgr.setUnlocked();
                    }

                    AppLog.i("REQUEST_CODE_DRAW");
                    byte[] result = data.getByteArrayExtra("bitmap");
                    if (result != null) {
                        File filename = new File(requireActivity().getExternalCacheDir() + "/image.jpg");
                        Bitmap bitmap = BitmapFactory.decodeByteArray(result, 0, result.length);
                        try (FileOutputStream out = new FileOutputStream(filename)) {
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
                            // PNG is a lossless format, the compression factor (100) is ignored
                        } catch (IOException e) {
                            AppLog.e(e.getMessage());
                            e.printStackTrace();
                        }
                        // Move captured photo from tmp folder to entry photo folder
                        try {
                            savePhoto(filename.getAbsolutePath(), true);
                            if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ENTRY_PHOTOS_POSITION, true)) {
                                photoCtrl.setNewPhotosAdded();
                            }
                        } catch (Exception e) {
                            AppLog.e("Exception: " + e);
                        }
                        // Delete cache directory
                        AppLifetimeStorageUtils.deleteCacheDir();
                        onStorageDataChange();
                    }
                } catch (Exception e) {
                    // no drawing image available
                }
                break;

            case REQUEST_TEXT_RECOGNITION:
                if (MyApp.getInstance().securityCodeMgr.isSecurityCodeSet()) {
                    MyApp.getInstance().securityCodeMgr.setUnlocked();
                }
                try {
                    if (data != null && data.hasExtra("text")) {
                        String text = data.getStringExtra("text");
                        if (text != null && !text.isEmpty()) {
                            int pos = entryTextEditText.getSelectionStart();
                            entryTextEditText.getText().insert(pos, text);
                        }
                    }

                } catch (Exception ignored) {
                }
                break;

            case COLLAGE_PICK_IMAGES_RC:
                if (MyApp.getInstance().securityCodeMgr.isSecurityCodeSet()) {
                    MyApp.getInstance().securityCodeMgr.setUnlocked();
                }
                if (data != null && data.hasExtra(KEY_DATA_RESULT)) {
                    ArrayList<String> pathsList = data.getStringArrayListExtra(KEY_DATA_RESULT);
                    Intent intent = new Intent(getActivity(), CollageActivity.class);
                    intent.putStringArrayListExtra(KEY_DATA_RESULT, pathsList);
                    startActivityForResult(intent, COLLAGE_RC);
                    entryViewEditActivity.activityState.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_COLLAGE_VIEW);
                }
                break;

            case COLLAGE_RC:
                if (data != null && data.hasExtra("path")) {
                    String photoPath = data.getStringExtra("path");
                    //   Log.e("photoPath" , photoPath);

                    File filename = new File(photoPath);
                    try {
                        savePhoto(filename.getAbsolutePath(), true);
                        if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ENTRY_PHOTOS_POSITION, true)) {
                            photoCtrl.setNewPhotosAdded();
                        }
                    } catch (Exception e) {
                        AppLog.d("Exception: " + e);
                    }
                    // Delete cache directory
                    AppLifetimeStorageUtils.deleteCacheDir();
                    onStorageDataChange();
                }
                break;

        }
    }

    private void savePhoto(String photoPath, boolean move) throws Exception {
        AppLog.i("photoPath: " + photoPath + ", move: " + move);
        if (photoPath == null) {
            throw new Exception();
        }
        // Save entry if not created yet
        if (rowUid == null) {
            saveEntry();
        }
        // Save attachment & Compress the photo with copying of exif data
        File targetFile = AttachmentsStatic.saveAttachment(rowUid, photoPath, PHOTO, move);
        AppLog.i("targetFile: " + targetFile.getPath());
    }

    private void savePhoto2(Uri photoUri) throws Exception {
        if (photoUri == null) {
            throw new Exception();
        }
        // Save entry if not created yet
        if (rowUid == null) {
            saveEntry();
        }
        // Save attachment & Compress the photo with copying of exif data
        File targetFile = AttachmentsStatic.saveAttachment2(rowUid, photoUri, PHOTO);
        AppLog.i("targetFile: " + targetFile.getPath());
    }

    private void getMillisAndUpdateDate(int year, int month, int day) {
        DateTime newDt = entryInfo.getLocalDt().withDate(year, month, day);
        entryInfo.unixEpochMillis = newDt.getMillis();

        updateDateAndTimeInUI();
        saveEntry();
    }

    private void getMillisAndUpdateTime(int hour, int minute) {
        DateTime newDt;
        try {
            newDt = entryInfo.getLocalDt().withTime(hour, minute, 0, 0);
        } catch (Exception e) {
            newDt = entryInfo.getLocalDt().withTime(hour + 1, minute, 0, 0);
        }
        entryInfo.unixEpochMillis = newDt.getMillis();
        updateDateAndTimeInUI();
        saveEntry();
    }

    private void showFolderSelectDialog() {
        if (!isAdded() || entryInfo == null || isActivityPaused(entryViewEditActivity)) {
            return;
        }
        turnOffEditMode();

        String dialogTag = Static.DIALOG_FOLDER_SELECT;
        if (getParentFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            FolderSelectDialog dialog = new FolderSelectDialog();
            dialog.setSelectedFolderUid(entryInfo.folderUid);
            dialog.setIsNewEntry(rowUid == null);
            dialog.show(getParentFragmentManager(), dialogTag);

            // Set dialog listener
            setFolderSelectDialogListener(dialog);
        }
    }

    void setFolderSelectDialogListener(FolderSelectDialog dialog) {
        dialog.setOnDialogItemClickListener(selectedFolderUid -> {
            if (!isAdded() || entryInfo == null) {
                return;
            }

            if (!selectedFolderUid.equals(entryInfo.folderUid)) {
                entryInfo.setFolderUid(selectedFolderUid);
                // Set default values
                entryInfo.setFolderTitle("");
                entryInfo.setFolderColor("");
                entryInfo.setFolderPattern("");

                Cursor folderCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSingleFolderCursorByUid(selectedFolderUid);
                if (folderCursor.getCount() != 0) {
                    FolderInfo folderInfo = new FolderInfo(folderCursor);
                    entryInfo.setFolderTitle(folderInfo.title);
                    entryInfo.setFolderColor(folderInfo.color);
                    entryInfo.setFolderPattern(folderInfo.pattern);
                }
                folderCursor.close();

                updateFolderInUI();
                saveEntry();
            }
        });
    }

    private void showTagsSelectDialog() {
        if (!isAdded() || entryInfo == null || isActivityPaused(entryViewEditActivity)) {
            return;
        }
        turnOffEditMode();

        String dialogTag = Static.DIALOG_TAGS_SELECT;
        if (getParentFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            TagsSelectDialog dialog = new TagsSelectDialog();
            dialog.setSelectedTagsUids(entryInfo.tags);
            dialog.show(getParentFragmentManager(), dialogTag);

            // Set dialog listener
            setTagsSelectDialogListener(dialog);
        }
    }

    void setTagsSelectDialogListener(TagsSelectDialog dialog) {
        dialog.setOnDialogSaveClickListener(selectedTagsUids -> {
            if (!isAdded() || entryInfo == null) {
                return;
            }

            if (!selectedTagsUids.equals(entryInfo.tags)) {
                entryInfo.tags = selectedTagsUids;
                updateTagsInUi();
                saveEntry();
            }
        });
    }

    private void showMoodSelectedDialog() {
        if (!isAdded() || entryInfo == null || isActivityPaused(entryViewEditActivity)) {
            return;
        }
        turnOffEditMode();

        String dialogTag = Static.DIALOG_MOOD_SELECT;
        if (getParentFragmentManager().findFragmentByTag(dialogTag) == null) {
            MoodSelectDialog dialog = new MoodSelectDialog(entryInfo.moodUid);
            dialog.show(getParentFragmentManager(), dialogTag);
            setMoodSelectDialogListener(dialog);
        }
    }

    private void setMoodSelectDialogListener(MoodSelectDialog dialog) {
        dialog.setOnDialogItemClickListener(moodInfo -> {
            if (moodInfo != null) {
                entryInfo.moodUid = moodInfo.uid;
                entryInfo.moodTitle = moodInfo.title;
                entryInfo.moodColor = moodInfo.color;
                entryInfo.moodIcon = moodInfo.icon;
                updateMoodInUI();
                saveEntry();
            }
        });
    }

    private void showLocationSelectDialog() {
        if (!isAdded() || entryInfo == null || isActivityPaused(entryViewEditActivity)) {
            return;
        }
        turnOffEditMode();

        String dialogTag = Static.DIALOG_LOCATION_SELECT;
        if (getParentFragmentManager().findFragmentByTag(dialogTag) == null) {
            stopLocationUpdate();

            // Entry location uid
            String locationUid = entryInfo.locationUid;
            if (entryInfo.locationUid.equals("") && locationInfo != null) {
                locationUid = locationInfo.uid;
            }

            // Show dialog
            LocationSelectDialog dialog = new LocationSelectDialog();
            dialog.setSelectedLocationUid(locationUid);
            dialog.setIsNewEntry(rowUid == null);
            dialog.show(getParentFragmentManager(), dialogTag);
            setLocationSelectDialogListener(dialog);
        }
    }

    void setLocationSelectDialogListener(LocationSelectDialog dialog) {
        dialog.setOnDialogItemClickListener(selectedLocationUid -> {
            if (!isAdded() || entryInfo == null) {
                return;
            }

            if (!selectedLocationUid.equals(entryInfo.locationUid) || isNewLocation) {
                isNewLocation = false;
                entryInfo.setLocationUid(selectedLocationUid);

                if (selectedLocationUid.equals("")) {
                    locationInfo = null;
                    // clear temperature
                    // entryInfo.weatherInfo = null;
                    // updateTempratureInUi();
                } else {
                    Cursor locationCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSingleLocationCursorByUid(selectedLocationUid);
                    if (locationCursor.getCount() != 0) {
                        locationInfo = new LocationInfo(locationCursor);
                    }
                    locationCursor.close();

                    fetchWeatherForLocation(locationInfo.latitude, locationInfo.longitude);
                }

                updateLocationInUI();
                saveEntry();
            }
        });
    }


    private boolean showDateTimeSuggestionDialog(String path) {
        if (!isAdded() || entryViewEditActivity.isFinishing()) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_MEDIA_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            } else {
                // Ask for ACCESS_MEDIA_LOCATION permission
                if (!getActivity().isFinishing()) {
                    PermissionsUtils.askForPermission((AppCompatActivity) getActivity(), Manifest.permission.ACCESS_MEDIA_LOCATION, Static.PERMISSION_REQUEST_LOCATION, Static.DIALOG_CONFIRM_RATIONALE_LOCATION,
                            R.string.location_permission_rationale_text);
                }
            }
        }

        if (path != null) {
            ExifInterface exifInterface;
            File file = new File(path);
            if (file.exists()) {
                AppLog.d("Exif file: " + file.getAbsolutePath());
                try {
                    exifInterface = new ExifInterface(file.getAbsolutePath());

                    final String dateTimeOrigString = exifInterface.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);
                    final String dateTimeModifiedString = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);

                    //   final String gpsDateString = exifInterface.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
                    String photoDate = dateTimeOrigString;
                    if (photoDate == null) {
                        photoDate = dateTimeModifiedString;
                    }

                    double[] exifLatLng = exifInterface.getLatLong();

                    if (exifLatLng != null) {
                        AppLog.d("EXIF GPS location found: " + exifLatLng[0] + ", " + exifLatLng[1]);
                    } else {
                        AppLog.d("EXIF GPS location not found in photo");
                    }

                    if (photoDate != null) {
                        PhotoMetadataSuggestionDialog photoMetadataSuggestionDialog = new PhotoMetadataSuggestionDialog(photoDate, exifLatLng);
                        photoMetadataSuggestionDialog.setDialogPositiveClickListener((dateTime, locationInfo) -> {
                            if (dateTime != null) {
                                getMillisAndUpdateDate(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
                                getMillisAndUpdateTime(dateTime.getHourOfDay(), dateTime.getMinuteOfHour());
                            }
                            if (locationInfo != null) {
                                this.locationInfo = locationInfo;
                                saveLocation();
                                saveEntry();
                                updateLocationInUI();
                                if (StringUtils.isEmpty(locationInfo.title) && StringUtils.isEmpty(locationInfo.address)) {
                                    executeGeocodeFromCoordsAsync(Double.parseDouble(locationInfo.latitude), Double.parseDouble(locationInfo.longitude));
                                }
                            }
                        });

                        try {
                            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                            ft.add(photoMetadataSuggestionDialog, Static.DIALOG_PHOTO_METADATA_SUGGESTION);
                            ft.commitAllowingStateLoss();
                        } catch (Exception e) {
                            AppLog.e(e.getMessage());
                        }
                        return true;
                    }

                } catch (Exception e) {
                    AppLog.e(e.getMessage());
                }
            } else {
                AppLog.d("Exif file does not exist");
            }
        }
        return false;
    }

    private boolean showDateTimeSuggestionDialog2(Uri uri) {
        if (!isAdded() || entryViewEditActivity.isFinishing()) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_MEDIA_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Get original URI with full EXIF data including GPS location
                try {
                    uri = MediaStore.setRequireOriginal(uri);
                } catch (Exception e) {
                    AppLog.e("Failed to get original URI: " + e.getMessage());
                }
            } else {
                // Ask for ACCESS_MEDIA_LOCATION permission
                if (!getActivity().isFinishing()) {
                    PermissionsUtils.askForPermission((AppCompatActivity) getActivity(), Manifest.permission.ACCESS_MEDIA_LOCATION, Static.PERMISSION_REQUEST_LOCATION, Static.DIALOG_CONFIRM_RATIONALE_LOCATION,
                            R.string.location_permission_rationale_text);
                }
            }
        }
        ExifInterface exifInterface;
        try (InputStream inputStream = MyApp.getInstance().getContentResolver().openInputStream(uri)) {
            if (inputStream != null) {
                exifInterface = new ExifInterface(inputStream);
                final String dateTimeOrigString = exifInterface.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);
                final String dateTimeModifiedString = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
                //   final String gpsDateString = exifInterface.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
                String photoDate = dateTimeOrigString;
                if (photoDate == null) {
                    photoDate = dateTimeModifiedString;
                }

                double[] exifLatLng = exifInterface.getLatLong();

                if (exifLatLng != null) {
                    AppLog.d("EXIF GPS location found: " + exifLatLng[0] + ", " + exifLatLng[1]);
                } else {
                    AppLog.d("EXIF GPS location not found in photo");
                }

                if (photoDate != null) {
                    PhotoMetadataSuggestionDialog photoMetadataSuggestionDialog = new PhotoMetadataSuggestionDialog(photoDate, exifLatLng);
                    photoMetadataSuggestionDialog.setDialogPositiveClickListener((dateTime, locationInfo) -> {
                        if (dateTime != null) {
                            getMillisAndUpdateDate(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());
                            getMillisAndUpdateTime(dateTime.getHourOfDay(), dateTime.getMinuteOfHour());
                        }
                        if (locationInfo != null) {
                            this.locationInfo = locationInfo;
                            saveLocation();
                            saveEntry();
                            updateLocationInUI();
                            if (StringUtils.isEmpty(locationInfo.title) && StringUtils.isEmpty(locationInfo.address)) {
                                executeGeocodeFromCoordsAsync(Double.parseDouble(locationInfo.latitude), Double.parseDouble(locationInfo.longitude));
                            }
                        }
                    });

                    try {
                        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                        ft.add(photoMetadataSuggestionDialog, Static.DIALOG_PHOTO_METADATA_SUGGESTION);
                        ft.commitAllowingStateLoss();
                    } catch (Exception e) {
                        AppLog.e(e.getMessage());
                    }
                    return true;
                }

            }

        } catch (Exception e) {
            AppLog.e("Error" + e.getMessage());
        }


        return false;
    }

    @Override
    public void onStorageDataChange() {
        AppLog.d("rowUid: " + rowUid + ", inEditMode: " + isInEditMode);
        if (!isAdded() || entryViewEditActivity.isFinishing()) {
            return;
        }

        // Load entry data
        loadEntryData();
    }

    private void duplicate() {
        // Copy entry
        String originalRowUid = rowUid;
        rowUid = null;
        saveEntry();

        // Copy photos  Get the list of entry photo
        ArrayList<AttachmentInfo> entryPhotosArrayList = AttachmentsStatic.getEntryAttachmentsArrayList(originalRowUid, PHOTO);
        try {
            for (AttachmentInfo entryPhoto : entryPhotosArrayList) {
                savePhoto(entryPhoto.getFilePath(), false);
            }
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
            // Show error toast
            Static.showToastError(String.format("%s: %s", getString(R.string.error), e.getMessage()));
        }
        Snackbar.make(requireView(), R.string.entry_duplicated, Snackbar.LENGTH_SHORT).show();
    }

    // Photo methods
    private void initPhotoLayout() {
        if (entryInfo == null) {
            return;
        }
        if (photoCtrl == null && MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ENTRY_PHOTOS_POSITION, true)) {
            photoCtrl = new EntryPhotosController(entryInfo.photoCount, photoPagerLayout, mainScrollView, entryViewEditActivity, this);
        } else if (!MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ENTRY_PHOTOS_POSITION, true)) {
            photoPagerLayout.findViewById(R.id.entry_collapsing_photo_Layout).setVisibility(View.GONE);
            addPhotoFab.setVisibility(View.GONE);
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mainScrollView.getLayoutParams();
            params.setBehavior(null);
            params.topMargin = getToolbarHeight();
        }
    }

    private void executeGetEntryPhotosAsync() {
        cancelGetEntryPhotosAsync();

        getEntryPhotosAsync = new GetEntryPhotosAsync(EntryFragment.this);
        getEntryPhotosAsync.execute();
    }

    private void cancelGetEntryPhotosAsync() {
        try {
            if (getEntryPhotosAsync != null) {
                getEntryPhotosAsync.cancel(true);
            }
        } catch (Exception e) {
            AppLog.d("Exception: " + e);
        }
    }

    // Photos at the top
    public void showEntryPhotosAtTop(ArrayList<AttachmentInfo> entryPhotosArrayList) {
        photoCtrl.createOrRefreshPhotoList(entryPhotosArrayList, entryInfo);
    }

    // Photos at the bottom
    public void showEntryPhotosAtBottom(ArrayList<AttachmentInfo> entryPhotosArrayList) {
        photoThumb1ViewGroup.setVisibility(View.GONE);
        photoThumb2ViewGroup.setVisibility(View.GONE);
        photoThumb3ViewGroup.setVisibility(View.GONE);
        entryAllPhotosViewGroup.setVisibility(View.GONE);

        if (entryInfo.photoCount >= 1) {
            showPhotoThumb(entryPhotosArrayList, 0, photoThumb1ViewGroup);
        }

        // Landscape
        if (Static.isLandscape()) {
            if (entryInfo.photoCount >= 2) {
                showPhotoThumb(entryPhotosArrayList, 1, photoThumb2ViewGroup);
            }
            if (entryInfo.photoCount == 3) {
                showPhotoThumb(entryPhotosArrayList, 2, photoThumb3ViewGroup);
            } else if (entryInfo.photoCount > 3) {
                showAllPhotosView(entryPhotosArrayList);
            }
        } else {
            // Portrait
            if (entryInfo.photoCount == 2) {
                showPhotoThumb(entryPhotosArrayList, 1, photoThumb2ViewGroup);
            } else if (entryInfo.photoCount > 2) {
                showAllPhotosView(entryPhotosArrayList);
            }
        }
    }

    private void showPhotoThumb(ArrayList<AttachmentInfo> entryPhotosArrayList, final int position, ViewGroup photoViewGroup) {
        photoViewGroup.setVisibility(View.VISIBLE);
        photoViewGroup.getLayoutParams().width = entryViewEditActivity.thumbWidth;
        photoViewGroup.getLayoutParams().height = entryViewEditActivity.thumbHeight;

        ImageView photoImageView = photoViewGroup.findViewById(R.id.image);
        ImageView isPrimaryImageView = photoViewGroup.findViewById(R.id.is_primary);

        File photoFile = null;
        if (entryPhotosArrayList != null && entryPhotosArrayList.size() > 0) {
            photoFile = new File(entryPhotosArrayList.get(position).getFilePath());
        }

        if (photoFile != null && photoFile.exists() && photoFile.length() > 0) {
            // Show photo
            Glide.with(requireActivity()).load(photoFile).transition(DrawableTransitionOptions.withCrossFade()).signature(Static.getGlideSignature(photoFile)).centerCrop().error(R.drawable.ic_photo_red_24dp).into(photoImageView);
//                    .override(entryViewEditActivity.thumbWidth, entryViewEditActivity.thumbHeight)
        } else {
            // Show photo icon
            Glide.with(requireActivity()).load(R.drawable.ic_photo_grey600_24dp).into(photoImageView);
        }

        // Click area with ripple effect
        View clickAreaView = photoViewGroup.findViewById(R.id.click_area);
        clickAreaView.setOnClickListener(view -> {
            if (entryViewEditActivity.activityState.isActivityPaused) {
                return;
            }
            turnOffEditMode();
            // Open photo in PhotoPagerActivity
            Intent intent = new Intent(getActivity(), PhotoPagerActivity.class);
            intent.putExtra(EXTRA_SKIP_SC, true);
            intent.putExtra("entryUid", rowUid);
            intent.putExtra("position", position);
            startActivityForResult(intent, Static.REQUEST_PHOTO_PAGER);
        });

        // Set primary icon
        if (photoFile != null && !entryInfo.firstPhotoFilename.equals("") && entryInfo.getFirstPhotoPath().equals(photoFile.getAbsolutePath())) {
            isPrimaryImageView.setVisibility(View.VISIBLE);
        } else {
            isPrimaryImageView.setVisibility(View.GONE);
        }
    }

    private void showAllPhotosView(ArrayList<AttachmentInfo> entryPhotosArrayList) {
        entryAllPhotosViewGroup.setVisibility(View.VISIBLE);

        entryAllPhotosViewGroup.getLayoutParams().width = entryViewEditActivity.thumbWidth;
        entryAllPhotosViewGroup.getLayoutParams().height = entryViewEditActivity.thumbHeight;
        allPhotosTextView.setText(String.format("%s (%d)", getString(R.string.all_photos), entryInfo.photoCount));

        smallPhotoThumb1ViewGroup.setVisibility(View.GONE);
        smallPhotoThumb2ViewGroup.setVisibility(View.GONE);
        smallPhotoThumb3ViewGroup.setVisibility(View.GONE);
        smallPhotoThumb4ViewGroup.setVisibility(View.GONE);

        // Landscape
        if (Static.isLandscape()) {
            if (entryInfo.photoCount >= 3) {
                showSmallPhotoThumb(entryPhotosArrayList, 2, smallPhotoThumb1ViewGroup);
            }
            if (entryInfo.photoCount >= 4) {
                showSmallPhotoThumb(entryPhotosArrayList, 3, smallPhotoThumb2ViewGroup);
            }
            if (entryInfo.photoCount >= 5) {
                showSmallPhotoThumb(entryPhotosArrayList, 4, smallPhotoThumb3ViewGroup);
            }
            if (entryInfo.photoCount >= 6) {
                showSmallPhotoThumb(entryPhotosArrayList, 5, smallPhotoThumb4ViewGroup);
            }
        } else {
            // Portrait
            if (entryInfo.photoCount >= 2) {
                showSmallPhotoThumb(entryPhotosArrayList, 1, smallPhotoThumb1ViewGroup);
            }
            if (entryInfo.photoCount >= 3) {
                showSmallPhotoThumb(entryPhotosArrayList, 2, smallPhotoThumb2ViewGroup);
            }
            if (entryInfo.photoCount >= 4) {
                showSmallPhotoThumb(entryPhotosArrayList, 3, smallPhotoThumb3ViewGroup);
            }
            if (entryInfo.photoCount >= 5) {
                showSmallPhotoThumb(entryPhotosArrayList, 4, smallPhotoThumb4ViewGroup);
            }
        }

        entryAllPhotosViewGroup.setOnClickListener(v -> {
            if (entryViewEditActivity.activityState.isActivityPaused) {
                return;
            }
            turnOffEditMode();
            openAllEntryPhotos();
        });
    }

    private void showSmallPhotoThumb(ArrayList<AttachmentInfo> entryPhotosArrayList, int position, ViewGroup photoViewGroup) {
        photoViewGroup.setVisibility(View.VISIBLE);

        photoViewGroup.getLayoutParams().width = entryViewEditActivity.smallThumbWidth;
        photoViewGroup.getLayoutParams().height = entryViewEditActivity.smallThumbHeight;

        ImageView photoImageView = photoViewGroup.findViewById(R.id.image);
        ImageView isPrimaryImageView = photoViewGroup.findViewById(R.id.is_primary);

        File photoFile = null;
        if (entryPhotosArrayList != null && entryPhotosArrayList.size() > position) {
            photoFile = new File(entryPhotosArrayList.get(position).getFilePath());
        }

        if (photoFile != null && photoFile.exists() && photoFile.length() > 0) {
            // Show photo
            Glide.with(requireActivity()).load(photoFile).signature(Static.getGlideSignature(photoFile)).centerCrop().error(R.drawable.ic_photo_red_18dp).into(photoImageView);
        } else {
            photoImageView.setImageResource(R.drawable.ic_photo_grey600_18dp);
        }

        // Set primary icon
        if (photoFile != null && !entryInfo.firstPhotoFilename.equals("") && entryInfo.getFirstPhotoPath().equals(photoFile.getAbsolutePath())) {
            isPrimaryImageView.setImageResource(R.drawable.ic_ok_white_disabled_18dp);
            isPrimaryImageView.setVisibility(View.VISIBLE);
        } else {
            isPrimaryImageView.setVisibility(View.GONE);
        }

        // Hide click area
        photoViewGroup.findViewById(R.id.click_area).setVisibility(View.GONE);
    }

    // Open photo in PhotoPagerActivity
    private void openEntryPhoto(int position) {
        Intent intent = new Intent(getActivity(), PhotoPagerActivity.class);
        intent.putExtra(EXTRA_SKIP_SC, true);
        intent.putExtra("entryUid", rowUid);
        intent.putExtra("position", position);
        startActivityForResult(intent, Static.REQUEST_PHOTO_PAGER);
    }

    // Open all photos in PhotoGridActivity
    private void openAllEntryPhotos() {
        Intent intent = new Intent(getActivity(), PhotoGridActivity.class);
        intent.putExtra(EXTRA_SKIP_SC, true);
        intent.putExtra("entryUid", rowUid);
        startActivityForResult(intent, Static.REQUEST_PHOTO_GRID);
    }

    @Override
    public void onPhotoClicked(int position) {
        openEntryPhoto(position);
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            showMyCurrentLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            showMyCurrentLocation(null);
        }

        @Override
        public void onProviderEnabled(String provider) {
            showMyCurrentLocation(null);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            showMyCurrentLocation(null);
        }
    };

    @Override
    public void onWeatherInfoReceived(WeatherInfo weatherInfo) {
        this.entryInfo.weatherInfo = weatherInfo;
        updateTempratureInUi();

        if (!entryInfo.title.isEmpty() || !entryInfo.text.isEmpty())
            saveEntry();
    }

    // Create a new instance of EntryFragment, providing "num" as an argument
    static EntryFragment newInstance(String rowUid) {
        EntryFragment f = new EntryFragment();
        // Supply rowUid as an argument
        Bundle args = new Bundle();
        args.putString("rowUid", rowUid);
        f.setArguments(args);
        return f;
    }


    // When creating, retrieve this instance's rowUid from its arguments
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rowUid = getArguments() != null ? getArguments().getString("rowUid") : null;
        //  AppLog.e("rowUid: " + rowUid + ", getTag(): " + getTag());

        pickMedia = registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(50), uris -> {
            if (!uris.isEmpty()) {
                ArrayList<Uri> selectedPhotosPathsArrayList = new ArrayList<>(uris);
                handleMultiselectImage(selectedPhotosPathsArrayList);
            } else {
                // Log.d("PhotoPicker", "No media selected");
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        entryViewEditActivity = (EntryViewEditActivity) getActivity();
    }

    // Fragment state vars
    private static final String ENTRY_UID_STATE_KEY = "ENTRY_UID_STATE_KEY";
    private static final String IN_EDIT_MODE_STATE_KEY = "IN_EDIT_MODE_STATE_KEY";
    private static final String SCROLLVIEW_Y_STATE_KEY = "SCROLLVIEW_Y_STATE_KEY";
    private static final String FOCUSED_FIELD_STATE_KEY = "FOCUSED_FIELD_STATE_KEY";
    private static final String FOCUSED_FIELD_CURSOR_POS_STATE_KEY = "FOCUSED_FIELD_CURSOR_POS_STATE_KEY";
    private static final String IS_FULLSCREEN_STATE_KEY = "IS_FULLSCREEN_STATE_KEY";
    private static final String ENTRY_TITLE_PREFIX_STATE_KEY = "ENTRY_TITLE_";
    private static final String ENTRY_TEXT_PREFIX_STATE_KEY = "ENTRY_TEXT_";

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        AppLog.i("rowUid: " + rowUid + ", inEditMode: " + isInEditMode + ", entryTitleUndoRedo.mIsUndoOrRedo: " + entryTitleUndoRedo.mIsUndoOrRedo);
        autoSave();

        outState.putString(ENTRY_UID_STATE_KEY, rowUid);
        outState.putBoolean(IN_EDIT_MODE_STATE_KEY, isInEditMode);
        outState.putInt(SCROLLVIEW_Y_STATE_KEY, mainScrollView.getScrollY());
        if (isInEditMode) {
            if (entryTitleEditText.isFocused()) {
                outState.putString(FOCUSED_FIELD_STATE_KEY, "entryTitle");
                outState.putInt(FOCUSED_FIELD_CURSOR_POS_STATE_KEY, entryTitleEditText.getSelectionStart());
            } else if (entryTextEditText.isFocused()) {
                outState.putString(FOCUSED_FIELD_STATE_KEY, "entryText");
                outState.putInt(FOCUSED_FIELD_CURSOR_POS_STATE_KEY, entryTextEditText.getSelectionStart());
            }

            outState.putBoolean(IS_FULLSCREEN_STATE_KEY, isFullscreen);
        }
        // Store entry title and text undo/redo
        entryTitleUndoRedo.storePersistentState(outState, ENTRY_TITLE_PREFIX_STATE_KEY);
        entryTextUndoRedo.storePersistentState(outState, ENTRY_TEXT_PREFIX_STATE_KEY);
    }
}
