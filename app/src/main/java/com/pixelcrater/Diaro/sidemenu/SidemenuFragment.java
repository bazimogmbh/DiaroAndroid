package com.pixelcrater.Diaro.sidemenu;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.pixelcrater.Diaro.config.AppConfig;
import com.pixelcrater.Diaro.asynctasks.GetTotalAndTodayCountsAsync;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.moods.MoodStatic;
import com.pixelcrater.Diaro.moods.MoodsCursorLoader;
import com.pixelcrater.Diaro.settings.PreferencesHelper;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.calendar.CalendarView;
import com.pixelcrater.Diaro.folders.FoldersCursorLoader;
import com.pixelcrater.Diaro.folders.FoldersStatic;
import com.pixelcrater.Diaro.generaldialogs.ConfirmDialog;
import com.pixelcrater.Diaro.generaldialogs.MyDatePickerDialog;
import com.pixelcrater.Diaro.utils.glide.GlideCircleTransform;
import com.pixelcrater.Diaro.locations.LocationsCursorAdapter;
import com.pixelcrater.Diaro.locations.LocationsCursorLoader;
import com.pixelcrater.Diaro.locations.LocationsStatic;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.storage.OnStorageDataChangeListener;
import com.pixelcrater.Diaro.storage.SyncStatic;
import com.pixelcrater.Diaro.storage.dropbox.DbxUserInfo;
import com.pixelcrater.Diaro.storage.dropbox.DropboxAccountManager;
import com.pixelcrater.Diaro.storage.dropbox.OnFsSyncStatusChangeListener;
import com.pixelcrater.Diaro.tags.TagsCursorLoader;
import com.pixelcrater.Diaro.tags.TagsStatic;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.io.File;

public class SidemenuFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, OnFsSyncStatusChangeListener, OnStorageDataChangeListener, GetTotalAndTodayCountsAsync.OnAsyncInteractionListener {

    public static final int FOLDERS_GROUP_LIST_LOADER = 0;
    public static final int TAGS_GROUP_LIST_LOADER = 1;
    public static final int LOCATIONS_GROUP_LIST_LOADER = 2;
    public static final int MOODS_GROUP_LIST_LOADER = 3;

    // State vars
    private static final String CALENDAR_VISIBLE_MONTH_MILLIS_STATE_KEY = "CALENDAR_VISIBLE_MONTH_MILLIS_STATE_KEY";

    public CalendarView calendarView;
    public EditText searchEditText;
    public int uiColor;
    public ExpandableListView expandableListView;
    public SidemenuCursorTreeAdapter cursorTreeAdapter;
    public LoaderManager loaderManager;
    public int profilePhotoHeightPx;
    public OnFragmentInteractionListener mListener;
    private TextView totalEntriesCountView;
    private TextView todayEntriesCountView;
    private TextView calendarHeaderTitle;
    private ImageButton calendarIcoButton;
    private LinearLayout calendarViewContainer;
    private ViewGroup userContainerViewGroup;
    private TextView userInfoFirstLine;
    private TextView userInfoSecondLine;
    private TextView userInfoSecondLinePercents;
    private ImageView profilePhotoBgImageView;
    private ImageView profilePhotoSmallImageView;
    private ImageView profilePhotoImageView;
    private ImageButton homeIcoButton;
    private ImageButton clearSearchButton;
    private View upgradeToProView;
    private ImageView fsSyncIndicatorView;
    private GetTotalAndTodayCountsAsync getTotalAndTodayCountsAsync;
    private ViewGroup view;
    private LayoutInflater inflater;
    private int scrollY;
    private View listHeaderView;
    private View dateRangeContainerView;
    private TextView fromDateTextView;
    private TextView toDateTextView;
    private ImageButton clearFromDateButton;
    private ImageButton clearToDateButton;
    private Cursor sidemenuGroupsCursor;
    private DbxUserInfo mDbxUserInfo;

    public static SidemenuFragment newInstance() {
        return new SidemenuFragment();
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.d("");
        // Inflate the layout for this fragment
        view = (ViewGroup) inflater.inflate(R.layout.sidemenu_fragment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//		AppLog.d("savedInstanceState: " + savedInstanceState);

        inflater = getLayoutInflater(savedInstanceState);
        loaderManager = LoaderManager.getInstance(this);

        // Init ExpandableListView
        initExpandableListView();

        // Settings icon
        ImageButton settingsButton = (ImageButton) view.findViewById(R.id.settings);
        settingsButton.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onSettingsButtonClicked();
            }
        });

        // Get UI color
        uiColor = Color.parseColor(MyThemesUtils.getPrimaryColorCode());

        // Home icon
        homeIcoButton = (ImageButton) view.findViewById(R.id.home);
        homeIcoButton.setOnClickListener(v -> {
            clearAllActiveFilters();

            if (mListener != null) {
                mListener.onHomeButtonClicked();
            }
        });

        // User stats
        fsSyncIndicatorView = view.findViewById(R.id.fs_sync_indicator);
        try {
            fsSyncIndicatorView.setImageResource(R.drawable.oval);
        } catch (Exception ignored) {
        }

        // Total entries count
        ((TextView) view.findViewById(R.id.total_text)).setText(String.format("%s:", getString(R.string.total)));
        totalEntriesCountView = (TextView) view.findViewById(R.id.total_entries);

        // Today entries count
        ((TextView) view.findViewById(R.id.today_text)).setText(String.format("%s:", getString(R.string.today)));
        todayEntriesCountView = (TextView) view.findViewById(R.id.today_entries);

        // Profile photo
        View profilePhotoClickArea = view.findViewById(R.id.profile_photo_click_area);
        profilePhotoClickArea.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onProfilePhotoAreaClicked();
            }
        });

        profilePhotoBgImageView = (ImageView) view.findViewById(R.id.profile_photo_bg);
        profilePhotoSmallImageView = (ImageView) view.findViewById(R.id.profile_photo_small);
        profilePhotoImageView = (ImageView) view.findViewById(R.id.profile_photo);

        updateProfilePhoto();

        userContainerViewGroup = (ViewGroup) view.findViewById(R.id.user_container);
        userContainerViewGroup.setBackgroundColor(Color.argb(155, 34, 34, 34));

        // Upgrade to Pro
        upgradeToProView = view.findViewById(R.id.upgrade_to_pro);
        upgradeToProView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onUpgradeToProButtonClicked();
            }
        });

        // Search
        initSearch();

        // Calendar
        initCalendar();

        // Create calendar
        createCalendarView(savedInstanceState);

        // Folders
        initFolders();

        // Tags
        initTags();

        // Locations
        initLocations();

        // Moods
        //if(PreferencesHelper.isMoodsEnabled()){
        initMoods();
        //}

        // Update home icon
        updateHomeIcon();

        // --- User info button ---
        LinearLayout userInfoButton = (LinearLayout) view.findViewById(R.id.user_info_button);
        userInfoButton.setOnClickListener(arg0 -> {
            if (mListener != null) {
                mListener.onProfilePhotoAreaClicked();
            }
        });

        userInfoFirstLine = (TextView) userInfoButton.findViewById(R.id.user_button_first_line);
        userInfoSecondLine = (TextView) userInfoButton.findViewById(R.id.user_button_second_line);
        userInfoSecondLinePercents = (TextView) userInfoButton.findViewById(R.id.second_line_percents);

        // Show DEBUG mode indicator
        if (AppConfig.isDeveloperMode()) {
            view.findViewById(R.id.debug_indicator).setVisibility(View.VISIBLE);
        }

        if (AppConfig.isDeveloperMode()) {
            // Show Amazon build indicator
            if (AppConfig.AMAZON_BUILD) {
                view.findViewById(R.id.amazon_indicator).setVisibility(View.VISIBLE);
            }
        }

        MyApp.getInstance().storageMgr.addOnStorageDataChangeListener(this);

        // Restore active dialog listeners
        restoreDialogListeners(savedInstanceState);

        // Update upgrade to Pro label
        showHideProLabel();
    }

    private void updateUiColorOnScroll() {
        int y = -listHeaderView.getTop();
//        AppLog.d("y: " + y + ", listHeaderView: " + listHeaderView);

        if (expandableListView.getFirstVisiblePosition() > 0) {
            y = profilePhotoHeightPx;
        }

        if (scrollY != y) {
            scrollY = y;
//            AppLog.d("scrollY: " + scrollY);

            int alpha = 255;
            double percents = 1;
            if (scrollY < profilePhotoHeightPx) {
                percents = (double) scrollY / profilePhotoHeightPx;
                alpha = (int) Math.floor(percents * 150 + 105);
            }
            //			AppLog.d("alpha: " + alpha);
            int uiColorByPercents = getUiColorByPercents(percents);
            userContainerViewGroup.setBackgroundColor(Color.argb(alpha, Color.red(uiColorByPercents), Color.green(uiColorByPercents), Color.blue(uiColorByPercents)));
        }
    }

    private int getUiColorByPercents(double percents) {
        float[] hsv = new float[3];
        int color = uiColor;
        Color.colorToHSV(color, hsv);
        hsv[2] *= percents; // value component
        return Color.HSVToColor(hsv);
    }

    private void restoreDialogListeners(Bundle savedInstanceState) {
//		AppLog.d("savedInstanceState: " + savedInstanceState);

        if (savedInstanceState != null) {
            ConfirmDialog dialog1 = (ConfirmDialog) getParentFragmentManager().findFragmentByTag(Static.DIALOG_CONFIRM_FOLDER_DELETE);
            if (dialog1 != null) {
                setFolderDeleteConfirmDialogListener(dialog1);
            }

            ConfirmDialog dialog2 = (ConfirmDialog) getParentFragmentManager().findFragmentByTag(Static.DIALOG_CONFIRM_TAG_DELETE);
            if (dialog2 != null) {
                setTagDeleteConfirmDialogListener(dialog2);
            }

            ConfirmDialog dialog3 = (ConfirmDialog) getParentFragmentManager().findFragmentByTag(Static.DIALOG_CONFIRM_LOCATION_DELETE);
            if (dialog3 != null) {
                setLocationDeleteConfirmDialogListener(dialog3);
            }

            MyDatePickerDialog dialog4 = (MyDatePickerDialog) getParentFragmentManager().findFragmentByTag(Static.DIALOG_PICKER_DATE_FROM);
            if (dialog4 != null) {
                setDatePickerDialogListener(dialog4);
            }

            MyDatePickerDialog dialog5 = (MyDatePickerDialog) getParentFragmentManager().findFragmentByTag(Static.DIALOG_PICKER_DATE_TO);

            if (dialog5 != null) {
                setDatePickerDialogListener(dialog5);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyApp.getInstance().storageMgr.removeOnStorageDataChangeListener(this);

        sidemenuGroupsCursor.close();
        calendarView.cancelGetDaysMarkersAsync();
    }

    @Override
    public void onPause() {
        super.onPause();

        removeDbxListeners();
    }


    @Override
    public void onResume() {
        super.onResume();
        addDbxListeners();

        updateUserInfoButton();
        updateUiColorOnScroll();

        // Get total and today counts
        executeGetTotalAndTodayCountsAsync();
    }

    @Override
    public void onStorageDataChange() {
        // Get total and today counts
        executeGetTotalAndTodayCountsAsync();

        // Get calendar days markers
        calendarView.executeGetDaysMarkersAsync();
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLong(CALENDAR_VISIBLE_MONTH_MILLIS_STATE_KEY, calendarView.getVisibleDt().getMillis());
    }

    private void initSearch() {
        // Clear search
        clearSearchButton = (ImageButton) view.findViewById(R.id.clear_search);
        clearSearchButton.setImageResource(MyThemesUtils.getDrawableResId("ic_close_%s_18dp"));

        clearSearchButton.setOnClickListener(v -> {
            clearActiveSearch();

            if (mListener != null) {
                mListener.onActiveFiltersChanged();
            }
        });

        // Search
        searchEditText = (EditText) view.findViewById(R.id.search_field);

        // VectorDrawableCompat vectorDrawableCompatSearch = VectorDrawableCompat.create(getResources(), R.drawable.ic_search_white_24dp, null);
        //  vectorDrawableCompatSearch.mutate();
        //   vectorDrawableCompatSearch.setTint(ContextCompat.getColor(getContext(), R.color.grey_500));

        Drawable search = getResources().getDrawable(R.drawable.ic_search_grey600_24dp);

        searchEditText.setCompoundDrawablesWithIntrinsicBounds(search, null, null, null);

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (searchEditText.hasFocus()) {
                    saveActiveSearchInPrefs();
                    updateHomeIcon();

                    if (mListener != null) {
                        mListener.onActiveFiltersChanged();
                    }
                }

                // Show/hide clear search button
                if (getSearchValue().equals("")) {
                    clearSearchButton.setVisibility(View.GONE);
                } else {
                    clearSearchButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        // Get active search text from prefs
        String activeSearchText = PreferencesHelper.getActiveSearchText();
//        AppLog.d("activeSearchText: " + activeSearchText);

        searchEditText.setText(activeSearchText);
    }

    private String getSearchValue() {
        return searchEditText.getText().toString();
    }

    private void initCalendar() {
        calendarViewContainer = (LinearLayout) view.findViewById(R.id.calendar_container);

        // Calendar header
        ViewGroup sidemenuCalendar = (ViewGroup) view.findViewById(R.id.calendar_header);
        sidemenuCalendar.setBackgroundColor(uiColor);

        // Calendar icon
        calendarIcoButton = (ImageButton) view.findViewById(R.id.calendar_ico);
        calendarIcoButton.setOnClickListener(v -> {
            clearActiveCalendarRange();

            if (mListener != null) {
                mListener.onActiveFiltersChanged();
            }
        });

        calendarHeaderTitle = (TextView) view.findViewById(R.id.calendar_header_title);
        calendarHeaderTitle.setOnClickListener(v -> {
            boolean isOpen = MyApp.getInstance().prefs.getBoolean(Prefs.PREF_SIDEMENU_CALENDAR_OPEN, true);
            MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_SIDEMENU_CALENDAR_OPEN, !isOpen).apply();

            openCloseCalendar();
        });

        openCloseCalendar();
    }

    public void createCalendarView(Bundle savedInstanceState) {
        DateTime visibleDt = null;

        if (savedInstanceState != null) {
            long visibleDtMillis = savedInstanceState.getLong(CALENDAR_VISIBLE_MONTH_MILLIS_STATE_KEY, 0);
            if (visibleDtMillis > 0) {
                visibleDt = new DateTime(visibleDtMillis);
            }
        } else if (calendarView != null) {
            visibleDt = calendarView.getVisibleDt();
            calendarViewContainer.removeAllViews();
        }
        calendarView = new CalendarView(getActivity());
        calendarViewContainer.addView(calendarView);
        calendarView.setVisibleDt(visibleDt);

        // Today click
        calendarView.getTodayTextView().setOnClickListener(v -> {
            DateTime fromDt = new DateTime().withTimeAtStartOfDay();
            DateTime toDt = fromDt.plusDays(1);
            selectDateRange(fromDt, toDt);

            // Show current month
            calendarView.setVisibleDt(null);
        });


        // Month click
        calendarView.getMonthLabelTextView().setOnClickListener(v -> {
            DateTime fromDt = calendarView.getVisibleDt().withDayOfMonth(1);
            DateTime toDt = fromDt.plusMonths(1);
            selectDateRange(fromDt, toDt);
        });

        // Year click
        calendarView.getYearLabelTextView().setOnClickListener(v -> {
            DateTime fromDt = calendarView.getVisibleDt().withDayOfYear(1);
            DateTime toDt = fromDt.plusYears(1);
            selectDateRange(fromDt, toDt);
        });

        // Day click
        calendarView.setOnDayClickedListener(dayDt -> selectDateRange(dayDt, dayDt.plusDays(1)));

        // Date range changed listener
        calendarView.setOnDateRangeChangedListener(() -> {
            updateCalendarHeaderIcon();
            updateDateRangeViews();
        });

        // From - To date range
        dateRangeContainerView = view.findViewById(R.id.date_range_container);

        fromDateTextView = (TextView) view.findViewById(R.id.from_date);
        fromDateTextView.setOnClickListener(v -> showDatePickerDialog(Static.DIALOG_PICKER_DATE_FROM));

        toDateTextView = (TextView) view.findViewById(R.id.to_date);
        toDateTextView.setOnClickListener(v -> showDatePickerDialog(Static.DIALOG_PICKER_DATE_TO));

        clearFromDateButton = (ImageButton) view.findViewById(R.id.clear_from_date);
        clearFromDateButton.setImageResource(MyThemesUtils.getDrawableResId("ic_close_%s_18dp"));
        clearFromDateButton.setOnClickListener(v -> {
            calendarView.clearSelectedRangeFrom();
            saveActiveCalendarRangeInPrefs();

            if (mListener != null) {
                mListener.onActiveFiltersChanged();
            }
        });

        clearToDateButton = (ImageButton) view.findViewById(R.id.clear_to_date);
        clearToDateButton.setImageResource(MyThemesUtils.getDrawableResId("ic_close_%s_18dp"));
        clearToDateButton.setOnClickListener(v -> {
            calendarView.clearSelectedRangeTo();
            saveActiveCalendarRangeInPrefs();

            if (mListener != null) {
                mListener.onActiveFiltersChanged();
            }
        });

        // Get active calendar selected date range from prefs
        long selectedRangeFromMillis = MyApp.getInstance().prefs.getLong(Prefs.PREF_ACTIVE_CALENDAR_RANGE_FROM_MILLIS, 0);
        long selectedRangeToMillis = MyApp.getInstance().prefs.getLong(Prefs.PREF_ACTIVE_CALENDAR_RANGE_TO_MILLIS, 0);
        calendarView.setSelectedDateRange(selectedRangeFromMillis, selectedRangeToMillis);
    }

    private void selectDateRange(DateTime fromDt, DateTime toDt) {
        long fromMillis = fromDt.getMillis();
        long toMillis = toDt.getMillis() - 1;

        // Clear selection if the same range selected again
        if (fromMillis == calendarView.getSelectedRangeFromMillis() && toMillis == calendarView.getSelectedRangeToMillis()) {
            fromMillis = 0;
            toMillis = 0;
        }

        calendarView.setSelectedDateRange(fromMillis, toMillis);
        saveActiveCalendarRangeInPrefs();

        if (mListener != null) {
            mListener.onActiveFiltersChanged();
        }
    }

    private void updateDateRangeViews() {
        long selectedRangeFromMillis = calendarView.getSelectedRangeFromMillis();
        long selectedRangeToMillis = calendarView.getSelectedRangeToMillis();

        dateRangeContainerView.setVisibility(View.GONE);
        if (selectedRangeFromMillis != 0 || selectedRangeToMillis != 0) {
            dateRangeContainerView.setVisibility(View.VISIBLE);

            // From
            fromDateTextView.setText(Static.getFormattedDate(selectedRangeFromMillis));
            clearFromDateButton.setVisibility(View.GONE);
            if (selectedRangeFromMillis != 0) {
                clearFromDateButton.setVisibility(View.VISIBLE);
            }

            // To
            toDateTextView.setText(Static.getFormattedDate(selectedRangeToMillis));
            clearToDateButton.setVisibility(View.GONE);
            if (selectedRangeToMillis != 0) {
                clearToDateButton.setVisibility(View.VISIBLE);
            }
        }
    }

    public void refreshSideMenuItems(){
        // CursorTreeAdapter
        sidemenuGroupsCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSidemenuGroupsCursor();
//        AppLog.d("sidemenuGroupsCursor.getCount(): " + sidemenuGroupsCursor.getCount());
        cursorTreeAdapter.setGroupCursor(sidemenuGroupsCursor);
        expandableListView.invalidate();
    }

    public void initExpandableListView() {
        // ExpandableListView
        expandableListView = (ExpandableListView) view.findViewById(R.id.sidemenu_list);

        // Add list header
        listHeaderView = inflater.inflate(R.layout.sidemenu_list_header, expandableListView, false);
        expandableListView.addHeaderView(listHeaderView, null, false);

        sidemenuGroupsCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSidemenuGroupsCursor();
       // AppLog.d("sidemenuGroupsCursor.getCount(): " + sidemenuGroupsCursor.getCount());
        cursorTreeAdapter = new SidemenuCursorTreeAdapter(sidemenuGroupsCursor, getActivity(), this);

        expandableListView.setAdapter(cursorTreeAdapter);
        expandableListView.setGroupIndicator(null);

        // OnChildClickListener
        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            String itemUid = cursorTreeAdapter.getChildItemUid(groupPosition, childPosition);
            AppLog.d("itemUid: " + itemUid);

            switch (groupPosition) {
                case SidemenuCursorTreeAdapter.GROUP_FOLDERS:
                    cursorTreeAdapter.sidemenuFolders.setSelectedFolderUid(itemUid);
                    cursorTreeAdapter.sidemenuFolders.saveActiveFolderInPrefs();
                    break;

                case SidemenuCursorTreeAdapter.GROUP_TAGS:
                    cursorTreeAdapter.sidemenuTags.markUnmarkTag(itemUid);
                    cursorTreeAdapter.sidemenuTags.saveActiveTagsInPrefs();
                    break;

                case SidemenuCursorTreeAdapter.GROUP_LOCATIONS:
                    cursorTreeAdapter.sidemenuLocations.markUnmarkLocation(itemUid);
                    cursorTreeAdapter.sidemenuLocations.saveActiveLocationsInPrefs();
                    break;

                case SidemenuCursorTreeAdapter.GROUP_MOODS:
                    cursorTreeAdapter.sidemenuMoods.setSelectedMoodUid(itemUid);
                    cursorTreeAdapter.sidemenuMoods.saveActiveMoodInPrefs();
                    break;
            }

            if (mListener != null) {
                mListener.onActiveFiltersChanged();
            }

            return true;
        });

        expandableListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                updateUiColorOnScroll();
            }
        });
    }

    private void initFolders() {
        // Get active folder uid from prefs
        String activeFolderUid = MyApp.getInstance().prefs.getString(Prefs.PREF_ACTIVE_FOLDER_UID, null);

        // Mark active folder
        if (activeFolderUid != null) {
            AppLog.d("activeFolderUid: " + activeFolderUid);
            cursorTreeAdapter.sidemenuFolders.setSelectedFolderUid(activeFolderUid);
        }

        openCloseFolders();
    }

    private void initTags() {
        // Get active tags from prefs
        String activeTags = MyApp.getInstance().prefs.getString(Prefs.PREF_ACTIVE_TAGS, "");

        // Mark active tags
        if (StringUtils.isNotEmpty(activeTags)) {
            AppLog.d("activeTags: " + activeTags);
            cursorTreeAdapter.sidemenuTags.setSelectedTagsUids(activeTags);
        }

        openCloseTags();
    }

    private void initLocations() {
        // Get active locations from prefs
        String activeLocations = MyApp.getInstance().prefs.getString(Prefs.PREF_ACTIVE_LOCATIONS, "");

        // Mark active locations
        if (StringUtils.isNotEmpty(activeLocations)) {
            AppLog.d("activeLocations: " + activeLocations);
            cursorTreeAdapter.sidemenuLocations.setSelectedLocationsUids(activeLocations);
        }

        openCloseLocations();
    }

    private void initMoods() {
        // Get active folder uid from prefs
        String activeMoodUid = MyApp.getInstance().prefs.getString(Prefs.PREF_ACTIVE_MOOD_UID, null);

        // Mark active folder
        if (activeMoodUid != null) {
            AppLog.e("activeMoodUid: " + activeMoodUid);
            cursorTreeAdapter.sidemenuMoods.setSelectedMoodUid(activeMoodUid);
        }

        openCloseMoods();
    }

    public void showHideProLabel() {
        // If Pro
        if (Static.isProUser()) {
            upgradeToProView.setVisibility(View.GONE);
        } else {
            upgradeToProView.setVisibility(View.VISIBLE);
        }
    }

    private void updateUserInfoButton() {
//        AppLog.d("");

        if (MyApp.getInstance().userMgr.isSignedIn()) {
            userInfoFirstLine.setText(MyApp.getInstance().userMgr.getSignedInEmail());
        } else {
            userInfoFirstLine.setText(R.string.sign_in);
        }

        fsSyncIndicatorView.setVisibility(View.GONE);
        userInfoSecondLinePercents.setText("");

        // If Dropbox connected
        if (DropboxAccountManager.isLoggedIn(getContext())) {
            // Dropbox account info
            if (MyApp.getInstance().storageMgr.isStorageDropbox()) {
                // Sync indicator
                int fsSyncStatus = SyncStatic.getFsSyncStatus();
                fsSyncIndicatorView.setColorFilter(getResources().getColor(SyncStatic.getSyncStatusColorResId(fsSyncStatus)), PorterDuff.Mode.SRC_ATOP);
                fsSyncIndicatorView.setVisibility(View.VISIBLE);

                if (fsSyncStatus == SyncStatic.STATUS_SYNCING) {
                    userInfoSecondLine.setText(MyApp.getInstance().storageMgr.getDbxFsAdapter().getVisibleSyncStatusText());
                    userInfoSecondLinePercents.setText(MyApp.getInstance().storageMgr.getDbxFsAdapter().getVisibleSyncPercents());
                } else if (mDbxUserInfo != null) {
                    userInfoSecondLine.setText(mDbxUserInfo.getFullaccount().getName().getDisplayName());
                    userInfoSecondLinePercents.setText(null);
                }
            }
            if (mDbxUserInfo == null) {
                new DropboxAccountManager.GetCurrentUserAsyncTask(getContext()) {
                    @Override
                    protected void onPostExecute(DbxUserInfo dbxUserInfo) {
                        super.onPostExecute(dbxUserInfo);
                        mDbxUserInfo = dbxUserInfo;

                        if (isAdded()) {
                            // Account info not yet downloaded
                            if (mDbxUserInfo == null) {
                                userInfoSecondLine.setText(getString(R.string.connecting_to_dropbox_with_ellipsis));
                            } else {
                                userInfoSecondLine.setText(mDbxUserInfo.getFullaccount().getName().getDisplayName());
                                AppLog.d("account: " + mDbxUserInfo.getFullaccount() + ", displayName: " + mDbxUserInfo.getFullaccount().getName().getDisplayName());

                            }
                        }

                    }
                }.execute();
            }
        } else {
            userInfoSecondLine.setText(getString(R.string.settings_dropbox_connect));
        }
    }

    private void updateCalendarHeaderIcon() {
        if (calendarView.getSelectedRangeFromMillis() > 0 || calendarView.getSelectedRangeToMillis() > 0) {
            calendarIcoButton.setEnabled(true);
            calendarIcoButton.setImageResource(R.drawable.ic_calendar_clear_white_24dp);
        } else {
            calendarIcoButton.setEnabled(false);
            calendarIcoButton.setImageResource(R.drawable.ic_today_white_24dp);
        }

        updateHomeIcon();
    }

    public void updateHomeIcon() {
        if (isAnyFilterActive()) {
            homeIcoButton.setEnabled(true);
            homeIcoButton.setImageResource(R.drawable.ic_home_clear_white_24dp);
        } else {
            homeIcoButton.setEnabled(false);
            homeIcoButton.setImageResource(R.drawable.ic_home_white_24dp);
        }
    }

    private void openCloseCalendar() {
        boolean isOpen = MyApp.getInstance().prefs.getBoolean(Prefs.PREF_SIDEMENU_CALENDAR_OPEN, true);
        int resId;
        if (isOpen) {
            calendarViewContainer.setVisibility(View.VISIBLE);
            resId = R.drawable.ic_keyboard_arrow_down_white_18dp;
        } else {
            calendarViewContainer.setVisibility(View.GONE);
            resId = R.drawable.ic_keyboard_arrow_right_white_18dp;
        }
        calendarHeaderTitle.setCompoundDrawablesWithIntrinsicBounds(MyApp.getInstance().getResources().getDrawable(resId), null, null, null);
    }

    public void openCloseFolders() {
        boolean isOpen = MyApp.getInstance().prefs.getBoolean(Prefs.PREF_SIDEMENU_FOLDERS_OPEN, true);
        AppLog.d("isOpen: " + isOpen);

        if (isOpen) {
            expandableListView.expandGroup(SidemenuCursorTreeAdapter.GROUP_FOLDERS);
        } else {
            expandableListView.collapseGroup(SidemenuCursorTreeAdapter.GROUP_FOLDERS);
        }
    }

    public void openCloseTags() {
        boolean isOpen = MyApp.getInstance().prefs.getBoolean(Prefs.PREF_SIDEMENU_TAGS_OPEN, true);
        if (isOpen) {
            expandableListView.expandGroup(SidemenuCursorTreeAdapter.GROUP_TAGS);
        } else {
            expandableListView.collapseGroup(SidemenuCursorTreeAdapter.GROUP_TAGS);
        }
    }

    public void openCloseLocations() {
        boolean isOpen = MyApp.getInstance().prefs.getBoolean(Prefs.PREF_SIDEMENU_LOCATIONS_OPEN, true);
        if (isOpen) {
            expandableListView.expandGroup(SidemenuCursorTreeAdapter.GROUP_LOCATIONS);
        } else {
            expandableListView.collapseGroup(SidemenuCursorTreeAdapter.GROUP_LOCATIONS);
        }
    }

    public void openCloseMoods() {
        boolean isOpen = MyApp.getInstance().prefs.getBoolean(Prefs.PREF_SIDEMENU_MOODS_OPEN, true);
        if (isOpen) {
            expandableListView.expandGroup(SidemenuCursorTreeAdapter.GROUP_MOODS);
        } else {
            expandableListView.collapseGroup(SidemenuCursorTreeAdapter.GROUP_MOODS);
        }
    }

    public void showMoodsHeaderPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), v);
        popupMenu.getMenuInflater().inflate(R.menu.popupmenu_folders_header, popupMenu.getMenu());

        int currentSort = MyApp.getInstance().prefs.getInt(Prefs.PREF_MOODS_SORT, Prefs.SORT_ALPHABETICALLY);
        popupMenu.getMenu().findItem(R.id.sort_alphabetically).setVisible(currentSort != Prefs.SORT_ALPHABETICALLY);
        popupMenu.getMenu().findItem(R.id.sort_by_entries_count).setVisible(currentSort != Prefs.SORT_BY_ENTRIES_COUNT);

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            // Sort alphabetically
            if (itemId == R.id.sort_alphabetically) {
                saveToPrefs(Prefs.PREF_MOODS_SORT, Prefs.SORT_ALPHABETICALLY);
                refreshMoods();
                return true;
            }

            // Sort by entries count
            else if (itemId == R.id.sort_by_entries_count) {
                saveToPrefs(Prefs.PREF_MOODS_SORT, Prefs.SORT_BY_ENTRIES_COUNT);
                refreshMoods();
                return true;
            }

            else {
                return false;
            }
        });

        popupMenu.show();
    }

    private void refreshMoods() {
        if (LoaderManager.getInstance(this).getLoader(MOODS_GROUP_LIST_LOADER) != null) {
            // Refresh loader
            LoaderManager.getInstance(this).getLoader(MOODS_GROUP_LIST_LOADER).onContentChanged();
        }
    }

    public void showFoldersHeaderPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), v);
        popupMenu.getMenuInflater().inflate(R.menu.popupmenu_folders_header, popupMenu.getMenu());

        int currentSort = MyApp.getInstance().prefs.getInt(Prefs.PREF_FOLDERS_SORT, Prefs.SORT_ALPHABETICALLY);
        popupMenu.getMenu().findItem(R.id.sort_alphabetically).setVisible(currentSort != Prefs.SORT_ALPHABETICALLY);
        popupMenu.getMenu().findItem(R.id.sort_by_entries_count).setVisible(currentSort != Prefs.SORT_BY_ENTRIES_COUNT);

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            // Sort alphabetically
            if (itemId == R.id.sort_alphabetically) {
                saveToPrefs(Prefs.PREF_FOLDERS_SORT, Prefs.SORT_ALPHABETICALLY);
                refreshFolders();
                return true;
            }

            // Sort by entries count
            else if (itemId == R.id.sort_by_entries_count) {
                saveToPrefs(Prefs.PREF_FOLDERS_SORT, Prefs.SORT_BY_ENTRIES_COUNT);
                refreshFolders();
                return true;
            }

            else {
                return false;
            }
        });

        popupMenu.show();
    }

    private void refreshFolders() {
        if (LoaderManager.getInstance(this).getLoader(FOLDERS_GROUP_LIST_LOADER) != null) {
            // Refresh loader
            LoaderManager.getInstance(this).getLoader(FOLDERS_GROUP_LIST_LOADER).onContentChanged();
        }
    }

    private void saveToPrefs(String prefName, int value) {
        MyApp.getInstance().prefs.edit().putInt(prefName, value).apply();
    }

    public void showFolderPopupMenu(View v, final String folderUid) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), v);
        popupMenu.getMenuInflater().inflate(R.menu.popupmenu_folder, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            // Edit folder
            if (itemId == R.id.edit) {
                if (mListener != null) {
                    mListener.onShouldStartFolderAddEditActivity(folderUid);
                }
                return true;
            }

            // Delete folder
            else if (itemId == R.id.delete) {
                showFolderDeleteConfirmDialog(folderUid);
                return true;
            }

            else {
                return false;
            }
        });

        popupMenu.show();
    }

    private void showFolderDeleteConfirmDialog(final String folderUid) {
        String dialogTag = Static.DIALOG_CONFIRM_FOLDER_DELETE;
        if (getParentFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setCustomString(folderUid);
            dialog.setTitle(getString(R.string.delete));
            dialog.setMessage(getString(R.string.folder_confirm_delete));
            dialog.show(getParentFragmentManager(), dialogTag);

            // Set dialog listener
            setFolderDeleteConfirmDialogListener(dialog);
        }
    }

    private void setFolderDeleteConfirmDialogListener(final ConfirmDialog dialog) {
        dialog.setDialogPositiveClickListener(() -> {
            if (!isAdded()) {
                return;
            }

            // Delete folder in background
            FoldersStatic.deleteFolderInBackground(dialog.getCustomString());
        });
    }

    public void showTagsHeaderPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), v);
        popupMenu.getMenuInflater().inflate(R.menu.popupmenu_tags_header, popupMenu.getMenu());

        int sort = MyApp.getInstance().prefs.getInt(Prefs.PREF_TAGS_SORT, Prefs.SORT_ALPHABETICALLY);
        popupMenu.getMenu().findItem(R.id.sort_alphabetically).setVisible(sort != Prefs.SORT_ALPHABETICALLY);
        popupMenu.getMenu().findItem(R.id.sort_by_entries_count).setVisible(sort != Prefs.SORT_BY_ENTRIES_COUNT);

        int logic = MyApp.getInstance().prefs.getInt(Prefs.PREF_TAGS_LOGIC, Prefs.FILTER_LOGIC_OR);

        popupMenu.getMenu().findItem(R.id.filter_by_or_logic).setVisible(logic != Prefs.FILTER_LOGIC_OR);
        popupMenu.getMenu().findItem(R.id.filter_by_and_logic).setVisible(logic != Prefs.FILTER_LOGIC_AND);


        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            // Sort alphabetically
            if (itemId == R.id.sort_alphabetically) {
                saveToPrefs(Prefs.PREF_TAGS_SORT, Prefs.SORT_ALPHABETICALLY);
                refreshTags();
                return true;
            }

            // Sort by entries count
            else if (itemId == R.id.sort_by_entries_count) {
                saveToPrefs(Prefs.PREF_TAGS_SORT, Prefs.SORT_BY_ENTRIES_COUNT);
                refreshTags();
                return true;
            }

            // Filter by OR logic
            else if (itemId == R.id.filter_by_or_logic) {
                saveToPrefs(Prefs.PREF_TAGS_LOGIC, Prefs.FILTER_LOGIC_OR);

                if (!MyApp.getInstance().prefs.getString(Prefs.PREF_ACTIVE_TAGS, "")
                        .equals("")) {
                    if (mListener != null) {
                        mListener.onActiveFiltersChanged();
                    }
                }

                Snackbar.make(view, R.string.showing_entries_with_any_selected_tag, Snackbar.LENGTH_LONG).show();

                return true;
            }

            // Filter by AND logic
            else if (itemId == R.id.filter_by_and_logic) {
                saveToPrefs(Prefs.PREF_TAGS_LOGIC, Prefs.FILTER_LOGIC_AND);

                if (!MyApp.getInstance().prefs.getString(Prefs.PREF_ACTIVE_TAGS, "").equals("")) {

                    if (mListener != null) {
                        mListener.onActiveFiltersChanged();
                    }
                }

                Snackbar.make(view, R.string.showing_entries_with_all_selected_tags, Snackbar.LENGTH_LONG).show();

                return true;
            }

            else {
                return false;
            }
        });

        popupMenu.show();
    }

    private void refreshTags() {
        if (LoaderManager.getInstance(this).getLoader(TAGS_GROUP_LIST_LOADER) != null) {
            // Refresh loader
            LoaderManager.getInstance(this).getLoader(TAGS_GROUP_LIST_LOADER).onContentChanged();
        }
    }

    public void showTagPopupMenu(View v, final String tagUid) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), v);
        popupMenu.getMenuInflater().inflate(R.menu.popupmenu_tag, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            // Edit tag
            if (itemId == R.id.edit) {
                if (mListener != null) {
                    mListener.onShouldStartTagAddEditActivity(tagUid);
                }
                return true;
            }

            // Delete tag
            else if (itemId == R.id.delete) {
                showTagDeleteConfirmDialog(tagUid);
                return true;
            }

            else {
                return false;
            }
        });

        popupMenu.show();
    }

    private void showTagDeleteConfirmDialog(final String tagUid) {
        String dialogTag = Static.DIALOG_CONFIRM_TAG_DELETE;
        if (getParentFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setCustomString(tagUid);
            dialog.setTitle(getString(R.string.delete));
            dialog.setMessage(getString(R.string.tag_confirm_delete));
            dialog.show(getParentFragmentManager(), dialogTag);

            // Set dialog listener
            setTagDeleteConfirmDialogListener(dialog);
        }
    }

    private void setTagDeleteConfirmDialogListener(final ConfirmDialog dialog) {
        dialog.setDialogPositiveClickListener(() -> {
            if (!isAdded()) {
                return;
            }

            // Delete tag in background
            TagsStatic.deleteTagInBackground(dialog.getCustomString());
        });
    }

    public void showLocationsHeaderPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), v);
        popupMenu.getMenuInflater().inflate(R.menu.popupmenu_locations_header, popupMenu.getMenu());

        int currentSort = MyApp.getInstance().prefs.getInt(Prefs.PREF_LOCATIONS_SORT, Prefs.SORT_ALPHABETICALLY);
        popupMenu.getMenu().findItem(R.id.sort_alphabetically).setVisible(currentSort != Prefs.SORT_ALPHABETICALLY);
        popupMenu.getMenu().findItem(R.id.sort_by_entries_count).setVisible(currentSort != Prefs.SORT_BY_ENTRIES_COUNT);


        if (cursorTreeAdapter.sidemenuLocations.getSelectedLocationsUidsArrayList().size() > 0 && !(cursorTreeAdapter.sidemenuLocations.getSelectedLocationsUidsArrayList().size() == 1 &&
                cursorTreeAdapter.sidemenuLocations.getSelectedLocationsUidsArrayList().get(0).equals(LocationsCursorAdapter.NO_LOCATION_UID))) {
            popupMenu.getMenu().findItem(R.id.delete_selected).setVisible(true);
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            // Sort alphabetically
            if (itemId == R.id.sort_alphabetically) {
                saveToPrefs(Prefs.PREF_LOCATIONS_SORT, Prefs.SORT_ALPHABETICALLY);
                refreshLocations();
                return true;
            }

            // Sort by entries count
            else if (itemId == R.id.sort_by_entries_count) {
                saveToPrefs(Prefs.PREF_LOCATIONS_SORT, Prefs.SORT_BY_ENTRIES_COUNT);
                refreshLocations();
                return true;
            }

            // Delete selected locations in background
            else if (itemId == R.id.delete_selected) {
                showSelectedLocationsDeleteConfirmDialog();
                return true;
            }

            else {
                return false;
            }
        });

        popupMenu.show();
    }

    private void refreshLocations() {
        if (LoaderManager.getInstance(this).getLoader(LOCATIONS_GROUP_LIST_LOADER) != null) {
            // Refresh loader
            LoaderManager.getInstance(this).getLoader(LOCATIONS_GROUP_LIST_LOADER).onContentChanged();
        }
    }

    public void showLocationPopupMenu(View v, final String locationUid) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), v);
        popupMenu.getMenuInflater().inflate(R.menu.popupmenu_location, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            // Edit location
            if (itemId == R.id.edit) {
                if (mListener != null) {
                    mListener.onShouldStartLocationAddEditActivity(locationUid);
                }
                return true;
            }

            // Delete location
            else if (itemId == R.id.delete) {
                showLocationDeleteConfirmDialog(locationUid);
                return true;
            }

            else {
                return false;
            }
        });

        popupMenu.show();
    }

    private void showLocationDeleteConfirmDialog(final String locationUid) {
        String dialogTag = Static.DIALOG_CONFIRM_LOCATION_DELETE;
        if (getParentFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setCustomString(locationUid);
            dialog.setTitle(getString(R.string.delete));
            dialog.setMessage(getString(R.string.location_confirm_delete));
            dialog.show(getParentFragmentManager(), dialogTag);

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

    private void showSelectedLocationsDeleteConfirmDialog() {
        String dialogTag = Static.DIALOG_CONFIRM_LOCATION_DELETE;
        if (getParentFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setTitle(getString(R.string.delete));
            String text = getString(R.string.locations_confirm_delete);
            dialog.setMessage(text);
            dialog.show(getParentFragmentManager(), dialogTag);

            // Set dialog listener
            setSelectedLocationsDeleteConfirmDialogListener(dialog);
        }
    }

    private void setSelectedLocationsDeleteConfirmDialogListener(final ConfirmDialog dialog) {
        dialog.setDialogPositiveClickListener(() -> {
            if (!isAdded()) {
                return;
            }

            // Delete locations in background
            LocationsStatic.deleteSelectedLocationsInBackground(cursorTreeAdapter.sidemenuLocations.getSelectedLocationsUids());
            cursorTreeAdapter.sidemenuLocations.clearActiveLocations();
            mListener.onActiveFiltersChanged();
        });
    }


    public void showMoodsPopupMenu(View v, final String moodUid) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), v);
        popupMenu.getMenuInflater().inflate(R.menu.popupmenu_mood, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            // Edit folder
            if (itemId == R.id.edit) {
                if (mListener != null) {
                    mListener.onShouldStartMoodAddEditActivity(moodUid);
                }
                return true;
            }

            // Delete folder
            else if (itemId == R.id.delete) {
                showMoodDeleteConfirmDialog(moodUid);
                return true;
            }

            else {
                return false;
            }
        });

        popupMenu.show();
    }


    private void showMoodDeleteConfirmDialog(final String moodUid) {
        String dialogTag = Static.DIALOG_CONFIRM_MOOD_DELETE;
        if (getParentFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setCustomString(moodUid);
            dialog.setTitle(getString(R.string.delete));
            dialog.setMessage(getString(R.string.confirm_delete));
            dialog.show(getParentFragmentManager(), dialogTag);

            // Set dialog listener
            setMoodDeleteConfirmDialogListener(dialog);
        }
    }

    private void setMoodDeleteConfirmDialogListener(final ConfirmDialog dialog) {
        dialog.setDialogPositiveClickListener(() -> {
            if (!isAdded()) {
                return;
            }

            // Delete mood in background
            MoodStatic.deleteMoodInBackground(dialog.getCustomString());
        });
    }



    //---------

    public void saveActiveCalendarRangeInPrefs() {
        // Save active calendar range to prefs
        MyApp.getInstance().prefs.edit().putLong(Prefs.PREF_ACTIVE_CALENDAR_RANGE_FROM_MILLIS, calendarView.getSelectedRangeFromMillis()).apply();
        MyApp.getInstance().prefs.edit().putLong(Prefs.PREF_ACTIVE_CALENDAR_RANGE_TO_MILLIS, calendarView.getSelectedRangeToMillis()).apply();

    }

    public void clearActiveCalendarRange() {
        calendarView.clearSelectedRange();
        saveActiveCalendarRangeInPrefs();
    }

    public void saveActiveSearchInPrefs() {
        // Save active search text to prefs
        PreferencesHelper.setActiveSearchText(getSearchValue());

    }

    public void clearActiveSearch() {
        if (!getSearchValue().equals("")) {
            searchEditText.setText("");
            searchEditText.clearFocus();
            saveActiveSearchInPrefs();

            // Hide keyboard
            Static.hideSoftKeyboard(searchEditText);

            updateHomeIcon();
        }
    }

    public void clearAllActiveFilters() {
        if (isAnyFilterActive()) {
            if (!getSearchValue().equals("")) {
                clearActiveSearch();
            }
            if (calendarView.getSelectedRangeFromMillis() != 0 || calendarView.getSelectedRangeToMillis() != 0) {
                clearActiveCalendarRange();
            }
            if (cursorTreeAdapter.sidemenuFolders.getSelectedFolderUid() != null) {
                cursorTreeAdapter.sidemenuFolders.clearActiveFolder();
            }
            if (cursorTreeAdapter.sidemenuTags.getSelectedTagsUidsArrayList().size() > 0) {
                cursorTreeAdapter.sidemenuTags.clearActiveTags();
            }
            if (cursorTreeAdapter.sidemenuLocations.getSelectedLocationsUidsArrayList().size() > 0) {
                cursorTreeAdapter.sidemenuLocations.clearActiveLocations();
            }
            if (cursorTreeAdapter.sidemenuMoods.getSelectedMoodUid() != null) {
                cursorTreeAdapter.sidemenuMoods.clearActiveMood();
            }
        }
    }

    public boolean isAnyFilterActive() {
        return calendarView.getSelectedRangeFromMillis() != 0 ||
                calendarView.getSelectedRangeToMillis() != 0 ||
                !getSearchValue().equals("") ||
                cursorTreeAdapter.sidemenuFolders.getSelectedFolderUid() != null ||
                cursorTreeAdapter.sidemenuTags.getSelectedTagsUidsArrayList().size() > 0 ||
                cursorTreeAdapter.sidemenuLocations.getSelectedLocationsUidsArrayList().size() > 0 ||
                cursorTreeAdapter.sidemenuMoods.getSelectedMoodUid() != null;
    }

    public void updateProfilePhoto() {
        boolean showProfilePhoto = MyApp.getInstance().prefs.getBoolean(Prefs.PREF_SHOW_PROFILE_PHOTO, true);

        int height = 72;
        if (showProfilePhoto) {
            height = 158;
            profilePhotoSmallImageView.setVisibility(View.GONE);
            profilePhotoImageView.setVisibility(View.VISIBLE);
        } else {
            profilePhotoSmallImageView.setVisibility(View.VISIBLE);
            profilePhotoImageView.setVisibility(View.GONE);
        }

        profilePhotoHeightPx = Static.getPixelsFromDip(height);
        profilePhotoBgImageView.getLayoutParams().height = profilePhotoHeightPx;

        int paddingTop = Static.getPixelsFromDip(24);
        File profilePhotoFile = new File(AppLifetimeStorageUtils.getProfilePhotoFilePath());

        if (DropboxAccountManager.isLoggedIn(getContext()) && profilePhotoFile.exists() && profilePhotoFile.length() > 0) {
            profilePhotoImageView.setPadding(0, 0, 0, 0);

            // Show photo
            if (showProfilePhoto) {
                // Large profile photo
                Glide.with(this).load(profilePhotoFile).signature(Static.getGlideSignature(profilePhotoFile)).centerCrop().error(R.drawable.ic_photo_red_24dp).into(profilePhotoImageView);

            } else {
                // Small profile photo
                Glide.with(this).load(profilePhotoFile).signature(Static.getGlideSignature(profilePhotoFile)).transform(new GlideCircleTransform()).error(R.drawable.ic_photo_red_18dp).into(profilePhotoSmallImageView);

            }
        } else {
            profilePhotoSmallImageView.setImageResource(R.drawable.ic_profile_no_padding_white_disabled_24dp);
            profilePhotoImageView.setPadding(0, paddingTop, 0, 0);
            profilePhotoImageView.setImageResource(R.drawable.ic_profile_no_padding_white_disabled_48dp);
        }
    }

    public void showDatePickerDialog(String dialogTag) {
        if (getParentFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            MyDatePickerDialog dialog = new MyDatePickerDialog();

            long millis;
            long nowMillis = DateTime.now().withTimeAtStartOfDay().getMillis();
            boolean showTodayButton = true;

            if (dialogTag.equals(Static.DIALOG_PICKER_DATE_FROM)) {
                long fromMillis = calendarView.getSelectedRangeFromMillis();
                long toMillis = calendarView.getSelectedRangeToMillis();

                // Set max date
                if (toMillis != 0) {
                    dialog.setMaxDateMillis(toMillis);

                    if (dialog.getMaxDateMillis() < nowMillis) {
                        showTodayButton = false;
                    }
                }

                millis = fromMillis;
            } else {
                long fromMillis = calendarView.getSelectedRangeFromMillis();
                long toMillis = calendarView.getSelectedRangeToMillis();

                // Set min date
                if (fromMillis != 0) {
                    dialog.setMinDateMillis(fromMillis);

                    if (dialog.getMinDateMillis() > nowMillis) {
                        showTodayButton = false;
                    }
                }

                millis = toMillis;
            }

            DateTime dt = new DateTime();
            if (millis != 0) {
                dt = new DateTime(millis);
            }

            dialog.setSelectedDate(dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth());
            dialog.setShowTodayButton(showTodayButton);
            dialog.show(getParentFragmentManager(), dialogTag);

            // Set dialog listener
            setDatePickerDialogListener(dialog);
        }
    }

    public void setDatePickerDialogListener(final MyDatePickerDialog dialog) {
        dialog.setDialogDateSetListener((year, month, day) -> {
            if (!isAdded()) {
                return;
            }

            DateTime newDt = new DateTime().withDate(year, month, day).withTimeAtStartOfDay();

            if (dialog.getTag().equals(Static.DIALOG_PICKER_DATE_FROM)) {
                calendarView.setSelectedDateRange(newDt.getMillis(), calendarView.getSelectedRangeToMillis());
            } else {
                calendarView.setSelectedDateRange(calendarView.getSelectedRangeFromMillis(), newDt.plusDays(1).getMillis() - 1);

            }
            saveActiveCalendarRangeInPrefs();

            if (mListener != null) {
                mListener.onActiveFiltersChanged();
            }
        });
    }

    private void addDbxListeners() {
        if (DropboxAccountManager.isLoggedIn(getContext())) {
            if (MyApp.getInstance().storageMgr.getDbxFsAdapter() != null) {
                MyApp.getInstance().storageMgr.getDbxFsAdapter().addOnFsSyncStatusChangeListener(this);

            }
        } else {
            removeDbxListeners();
        }
    }

    private void removeDbxListeners() {
        if (DropboxAccountManager.isLoggedIn(getContext())) {
            if (MyApp.getInstance().storageMgr.getDbxFsAdapter() != null) {
                MyApp.getInstance().storageMgr.getDbxFsAdapter().removeOnFsSyncStatusChangeListener(this);
            }
        }
    }

    @Override
    public void onFsSyncStatusChange() {
        getActivity().runOnUiThread(() -> {
            if (isAdded()) {
                updateUserInfoButton();
            }
        });
    }

    public void executeGetTotalAndTodayCountsAsync() {
        cancelGetTotalAndTodayCountsAsync();

        getTotalAndTodayCountsAsync = new GetTotalAndTodayCountsAsync(SidemenuFragment.this);
        // Execute on a separate thread
        Static.startMyTask(getTotalAndTodayCountsAsync);
    }

    public void cancelGetTotalAndTodayCountsAsync() {
        try {
            if (getTotalAndTodayCountsAsync != null) {
                getTotalAndTodayCountsAsync.setCancelled(true);
                getTotalAndTodayCountsAsync.cancel(true);
            }
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        AppLog.d("onCreateLoader loaderId: " + loaderId);

        switch (loaderId) {
            case FOLDERS_GROUP_LIST_LOADER:
                return new FoldersCursorLoader(getActivity(), "");

            case TAGS_GROUP_LIST_LOADER:
                return new TagsCursorLoader(getActivity(), true, "");

            case LOCATIONS_GROUP_LIST_LOADER:
                return new LocationsCursorLoader(getActivity(), "");

            case MOODS_GROUP_LIST_LOADER:
                return new MoodsCursorLoader(getActivity(), "");


            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
   //     AppLog.d("loader.getId(): " + loader.getId() + ", cursor.isClosed(): " + cursor.isClosed());
        cursorTreeAdapter.setChildrenCursor(loader.getId(), cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    //    AppLog.d("loader.getId(): " + loader.getId());
        cursorTreeAdapter.setChildrenCursor(loader.getId(), null);
    }

    @Override
    public void onGetTotalAndTodayCountsAsyncFinished(int totalEntriesCount, int todayEntriesCount) {
        totalEntriesCountView.setText(String.valueOf(totalEntriesCount));
        todayEntriesCountView.setText(String.valueOf(todayEntriesCount));
    }

    public interface OnFragmentInteractionListener {
        void onSettingsButtonClicked();

        void onHomeButtonClicked();

        void onProfilePhotoAreaClicked();

        void onUpgradeToProButtonClicked();

        void onActiveFiltersChanged();

        void onShouldStartFolderAddEditActivity(String folderUid);

        void onShouldStartTagAddEditActivity(String tagUid);

        void onShouldStartLocationAddEditActivity(String locationUid);

        void onShouldStartMoodAddEditActivity(String moodUid);
    }
}
