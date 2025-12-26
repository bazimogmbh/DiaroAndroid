package com.pixelcrater.Diaro.entries.viewedit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.Loader;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.activitytypes.TypeActivity;
import com.pixelcrater.Diaro.analytics.AnalyticsConstants;
import com.pixelcrater.Diaro.config.GlobalConstants;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.folders.FolderSelectDialog;
import com.pixelcrater.Diaro.generaldialogs.ConfirmDialog;
import com.pixelcrater.Diaro.generaldialogs.MyDatePickerDialog;
import com.pixelcrater.Diaro.generaldialogs.MyTimePickerDialog;
import com.pixelcrater.Diaro.generaldialogs.OptionsDialog;
import com.pixelcrater.Diaro.locations.LocationSelectDialog;
import com.pixelcrater.Diaro.settings.PreferencesHelper;
import com.pixelcrater.Diaro.tags.TagsSelectDialog;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.CustomViewPager;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.PermissionsUtils;
import com.pixelcrater.Diaro.utils.Static;

public class EntryViewEditActivity extends TypeActivity implements LoaderCallbacks<Cursor> {

    // State vars
    private static final String CURRENT_ENTRY_UID_STATE_KEY = "IN_EDIT_MODE_STATE_KEY";

    private static final int ENTRIES_CURSOR_LOADER = 0;

    public Bundle extras;
    public String clickedEntryUid;
    public String activeFolderUid;
    public String activeTags;
    public long activeDate;
    public int touchOffset;
    public int thumbWidth;
    public int thumbHeight;
    public int smallThumbWidth;
    public int smallThumbHeight;
    public CustomViewPager viewPager;
    public EntriesPagerCursorAdapter entriesCursorPagerAdapter;
    public String searchKeyword;
    private Bundle mSavedInstanceState;
    private ViewGroup pagerPreloader;
    private boolean startMultiplePhotoPickerActivityOnResume;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        useCollapsingToolbar = true;
        super.onCreate(savedInstanceState);

        setContentView(addViewToContentContainer(R.layout.entry_view_edit));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        activityState.setLayoutBackground();

        // Handle IME (keyboard) insets for editor tools on Android 15+
        setupEditorToolsInsets();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ENTRY_PHOTOS_POSITION, true)) {
                actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, android.R.color.transparent)));
            } else {
                actionBar.setBackgroundDrawable(new ColorDrawable(MyThemesUtils.getPrimaryColor()));
            }
        }

        if (activityState.startedFromWidget) {
            if (getSupportActionBar() != null) {
                // Disable ActionBar back icon
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }

        mSavedInstanceState = savedInstanceState;

        // Get intent extras
        extras = getIntent().getExtras();
//		AppLog.d("extras: " + extras);
        if (getIntent().getAction() != null) {
            String action = getIntent().getAction();
            String type = getIntent().getType();
            if (type != null && (action.equals(Intent.ACTION_SEND) || action.equals(Intent.ACTION_SEND_MULTIPLE))) {
                if (MyApp.getInstance().securityCodeMgr.isSecurityCodeSet()) {
                    MyApp.getInstance().securityCodeMgr.setUnlocked();
                }
            }
        }

        if (extras != null) {
            activeFolderUid = extras.getString("activeFolderUid");
        }
        if (activeFolderUid == null) {
            activeFolderUid = "";
        }
        if (extras != null) {
            activeTags = extras.getString("activeTags");
        }
        if (activeTags == null) {
            activeTags = "";
        }
        // Active date from extras
        if (extras != null) {
            activeDate = extras.getLong("activeDate");
        }
        // Clicked entry uid from extras
        if (extras != null) {
            clickedEntryUid = extras.getString("entryUid");
        }
        if (clickedEntryUid == null) {
            clickedEntryUid = "";
        }
        AppLog.d("clickedEntryUid: " + clickedEntryUid);

        touchOffset = Static.getPixelsFromDip(10);

        // Calculate photo sizes
        calculatePhotoSizes();

        // Pager preloader
        pagerPreloader = findViewById(R.id.pager_preloader);

        // View pager
        viewPager = findViewById(R.id.entry_pager);

        searchKeyword = PreferencesHelper.getActiveSearchText();

        // Pager adapter
        entriesCursorPagerAdapter = new EntriesPagerCursorAdapter(this, getSupportFragmentManager(), null);
        viewPager.setAdapter(entriesCursorPagerAdapter);

        // Init entries cursor loader
        LoaderManager.getInstance(this).initLoader(ENTRIES_CURSOR_LOADER, null, this);
        activityState.showHideBanner();

    }

    private void setupEditorToolsInsets() {
        // Apply keyboard and navigation bar insets to editor tools
        // This is called early, editor_tools will be found when fragment loads
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, windowInsets) -> {
                View editorTools = findViewById(R.id.editor_tools);
                if (editorTools != null) {
                    applyKeyboardAndBottomInsets(editorTools);
                }
                return windowInsets;
            });
        }
    }

    private void hidePagerPreloader() {
        pagerPreloader.setVisibility(View.GONE);
        viewPager.setVisibility(View.VISIBLE);
    }

    private void goToPage(String entryUid) {
        if (entryUid.equals("")) {
            return;
        }

        final int pos = entriesCursorPagerAdapter.findPositionByEntryUid(entryUid);
        if (pos != -1 && pos < entriesCursorPagerAdapter.getCount()) {
            viewPager.setCurrentItem(pos, false);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        AppLog.e("loaderId: " + loaderId);

        if (loaderId == ENTRIES_CURSOR_LOADER) {
            return new EntriesPagerCursorLoader(EntryViewEditActivity.this);
        }// An invalid id was passed in
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        AppLog.d("loader.getId(): " + loader.getId());
        if (cursor != null) {
            AppLog.d("cursor.getCount(): " + cursor.getCount());
        }

        if (loader.getId() == ENTRIES_CURSOR_LOADER) {
            entriesCursorPagerAdapter.changeCursor(cursor);

            if (pagerPreloader.getVisibility() == View.VISIBLE) {
                goToPage(clickedEntryUid);
                hidePagerPreloader();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        AppLog.e(" onLoaderReset loader.getId(): " + loader.getId());
        if (loader.getId() == ENTRIES_CURSOR_LOADER) {
            entriesCursorPagerAdapter.changeCursor(null);
        }
    }

    private void calculatePhotoSizes() {
        thumbWidth = Static.getThumbnailWidthForGrid();
        thumbHeight = (int) (thumbWidth * Static.PHOTO_PROPORTION);

        smallThumbWidth = thumbWidth / 2 - Static.getPixelsFromDip(1);
        smallThumbHeight = (int) (smallThumbWidth * Static.PHOTO_PROPORTION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Restore active dialog listeners
        if (mSavedInstanceState != null) {
            restoreDialogListeners();
        }

        EntryFragment currentEntryFragment = getCurrentEntryFragment();
        AppLog.d("currentEntryFragment: " + currentEntryFragment);

        if (startMultiplePhotoPickerActivityOnResume && currentEntryFragment != null) {
            currentEntryFragment.startMultiplePhotoPickerActivityNew();
            startMultiplePhotoPickerActivityOnResume = false;
        }
    }

    public EntryFragment getCurrentEntryFragment() {
        if (clickedEntryUid.equals("")) {
            return (EntryFragment) entriesCursorPagerAdapter.instantiateItem(viewPager, viewPager.getCurrentItem());
        }

        EntryFragment entryFragment = null;
        if (entriesCursorPagerAdapter != null && entriesCursorPagerAdapter.mDataValid && !entriesCursorPagerAdapter.getCursor().isClosed() && viewPager != null) {
            try {
                entryFragment = (EntryFragment) entriesCursorPagerAdapter.instantiateItem(viewPager, viewPager.getCurrentItem());

            } catch (Exception e) {
                AppLog.e("Exception: " + e);
            }
        }

        return entryFragment;
    }

    @Override
    public void onBackPressed() {
        EntryFragment currentEntryFragment = getCurrentEntryFragment();
        if (currentEntryFragment != null && currentEntryFragment.isInEditMode && currentEntryFragment.rowUid != null) {
            currentEntryFragment.turnOffEditMode();
        } else {
            exitActivity(false);
        }
    }

    public void exitActivity(boolean entryArchived) {
        // Set to ask for security code when activity was started from widget
        if (activityState.startedFromWidget) {
            MyApp.getInstance().securityCodeMgr.setLocked();
        }

        EntryFragment currentEntryFragment = getCurrentEntryFragment();
        if (currentEntryFragment != null) {
            currentEntryFragment.turnOffEditMode();

            // Return results to @MainActivity
            if (currentEntryFragment.rowUid != null) {
                Intent i = new Intent();
                i.putExtra("entryUid", currentEntryFragment.rowUid);
                i.putExtra("entryArchived", entryArchived);

                setResult(RESULT_OK, i);
            }
        }

        // start the sync
        MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners();

        finish();
    }

    // -------------------- MENU --------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_entry_viewedit, menu);

        MenuItem myActionMenuItem = menu.findItem(R.id.item_search);
        final SearchView searchView = (SearchView) myActionMenuItem.getActionView();

        // TODO : Add < > butons to searchview

        /**
         ActionBar.LayoutParams navButtonsParams = new ActionBar.LayoutParams(searchView.getHeight() * 2 / 3, searchView.getHeight() * 2 / 3);
         LinearLayout linearLayoutOfSearchView = (LinearLayout) searchView.getChildAt(0);
         Button btnPrev = new Button(this);
         btnPrev.setBackground(getResources().getDrawable(R.drawable.ic_done_grey600_24dp));
         linearLayoutOfSearchView.addView(btnPrev);
         ((LinearLayout) searchView.getChildAt(0)).setGravity(Gravity.BOTTOM); **/

        searchView.setIconified(false);
        searchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    invalidateOptionsMenu();
            }
        });

        myActionMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                menu.findItem(R.id.item_share).setVisible(false);
                menu.findItem(R.id.item_print).setVisible(false);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                menu.findItem(R.id.item_share).setVisible(true);
                menu.findItem(R.id.item_print).setVisible(true);
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    searchKeyword = newText;
                    getCurrentEntryFragment().highLightText(searchKeyword);
                } else {
                    searchKeyword = newText;
                    getCurrentEntryFragment().turnOffEditMode();
                    getCurrentEntryFragment().highLightText(searchKeyword);
                }
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu != null) {
            showHideMenuIcons(menu);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    private void showHideMenuIcons(Menu menu) {
        EntryFragment currentEntryFragment = getCurrentEntryFragment();

        boolean isInEditMode = currentEntryFragment != null && currentEntryFragment.isInEditMode;
        boolean isSaved = currentEntryFragment != null && currentEntryFragment.rowUid != null;
        boolean isFullscreen = currentEntryFragment != null && isInEditMode && currentEntryFragment.isFullscreen;

        menu.findItem(R.id.item_edit).setVisible(!isInEditMode);
        menu.findItem(R.id.item_duplicate).setVisible(!isInEditMode);
        menu.findItem(R.id.item_diaro_web).setVisible(!isInEditMode);
        menu.findItem(R.id.item_read_txt).setVisible(!isInEditMode);

        menu.findItem(R.id.item_share).setVisible(!isInEditMode && isSaved);
        menu.findItem(R.id.item_delete).setVisible(!isInEditMode && isSaved);

        menu.findItem(R.id.item_fullscreen).setVisible(isInEditMode && !isFullscreen);

        menu.findItem(R.id.item_timestamp).setVisible(isInEditMode);
        menu.findItem(R.id.item_templates).setVisible(isInEditMode);
        menu.findItem(R.id.item_draw).setVisible(isInEditMode);
        // menu.findItem(R.id.item_add_photo).setVisible(isInEditMode);
        menu.findItem(R.id.item_dictate_txt).setVisible(isInEditMode);
        menu.findItem(R.id.item_search).setVisible(!isInEditMode);
        menu.findItem(R.id.item_text_recognition).setVisible(isInEditMode);
        menu.findItem(R.id.item_print).setVisible(!isInEditMode);

        // add vector icons
        addVectorIconToMenu(menu.findItem(R.id.item_add_photo), R.drawable.ic_photo_white_24dp);
        addVectorIconToMenu(menu.findItem(R.id.item_edit), R.drawable.ic_edit_white_24dp);
        addVectorIconToMenu(menu.findItem(R.id.item_print), R.drawable.ic_print_white_24dp);
        addVectorIconToMenu(menu.findItem(R.id.item_share), R.drawable.ic_share_white_24dp);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AppLog.d("item: " + item);
        if (activityState.isActivityPaused) {
            return true;
        }

        EntryFragment currentEntryFragment = getCurrentEntryFragment();
        AppLog.d("currentEntryFragment: " + currentEntryFragment);

        // Display Metrics
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
//        AppLog.d("currentEntryFragment.num: " + currentEntryFragment.num + ", viewPager.getCurrentItem(): " + viewPager.getCurrentItem());

        // Disable menu when pager is not ready or is now swiping
        if (item.getItemId() != android.R.id.home && (currentEntryFragment == null || viewPager.getScrollX() % dm.widthPixels != 0))
        //  || currentEntryFragment.num != viewPager.getCurrentItem())
        {
            return super.onOptionsItemSelected(item);
        }

//		viewPager.setCurrentItem(viewPager.getCurrentItem(), false);

        // Handle presses on the action bar items
        int itemId = item.getItemId();

        // Back
        if (itemId == android.R.id.home) {
            exitActivity(false);
            return true;
        }

        // Add photo
        else if (itemId == R.id.item_add_photo) {
            showFullscreenAd();
            currentEntryFragment.showAddPhotoOptionsDialog();
            return true;
        }

        // Edit
        else if (itemId == R.id.item_edit) {
            currentEntryFragment.turnOnEditMode(currentEntryFragment.entryTitleEditText, 0);
            return true;
        }

        // Fullscreen
        else if (itemId == R.id.item_fullscreen) {
            currentEntryFragment.turnOnFullscreen();
            return true;
        }

        // Select Weather
        else if (itemId == R.id.item_weather) {
            activityState.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_WEATHER);
            currentEntryFragment.showWeatherAlertOptions();
            return true;
        }

        // Duplicate
        else if (itemId == R.id.item_duplicate) {
            activityState.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_DUPLICATE);
            currentEntryFragment.showEntryDuplicateConfirmDialog();
            return true;
        }

        // Share
        else if (itemId == R.id.item_share) {
            activityState.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_SHARE);
            currentEntryFragment.shareEntry();
            return true;
        }

        // Delete
        else if (itemId == R.id.item_delete) {
            currentEntryFragment.showEntryDeleteConfirmDialog();
            return true;
        }

        // Speech to txt
        else if (itemId == R.id.item_dictate_txt) {
            activityState.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_TEXT_TO_SPEECH);
            currentEntryFragment.startTextToSpeech();
            return true;
        }

        // Draw
        else if (itemId == R.id.item_draw) {
            currentEntryFragment.startDrawing();
            return true;
        }

        // Read txt
        else if (itemId == R.id.item_read_txt) {
            activityState.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_SPEECH_TO_TEXT);
            currentEntryFragment.readText();
            return true;
        }

        // Timestamp
        else if (itemId == R.id.item_timestamp) {
            currentEntryFragment.appendTimestamp();
            return true;
        }

        // Templates
        else if (itemId == R.id.item_templates) {
            currentEntryFragment.showTemplatesDialog();
            return true;
        }

        // Text Recognition
        else if (itemId == R.id.item_text_recognition) {
            currentEntryFragment.startTextRecognition();
            return true;
        }

        // Diaro Web
        else if (itemId == R.id.item_diaro_web) {
            activityState.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_DIARO_WEB);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GlobalConstants.DIARO_ENTRY_URL + currentEntryFragment.rowUid));
            startActivity(browserIntent);
            return true;
        }

        // Print
        else if (itemId == R.id.item_print) {
            if (Static.isProUser()) {
                currentEntryFragment.printPdf();
            } else {
                Static.startProActivity(EntryViewEditActivity.this, activityState);
            }
            return true;
        }

        else {
            return super.onOptionsItemSelected(item);
        }
    }

    // -------------------- RESTORE --------------------
    public void restoreDialogListeners() {
        EntryFragment currentEntryFragment = getCurrentEntryFragment();
        if (currentEntryFragment == null) {
            return;
        }

//		AppLog.d("currentEntryFragment.num: " + currentEntryFragment.num);

        MyDatePickerDialog dialog1 = (MyDatePickerDialog) getSupportFragmentManager().findFragmentByTag(Static.DIALOG_PICKER_DATE);
        if (dialog1 != null) {
            currentEntryFragment.setDatePickerDialogListener(dialog1);
        }

        MyTimePickerDialog dialog2 = (MyTimePickerDialog) getSupportFragmentManager().findFragmentByTag(Static.DIALOG_PICKER_TIME);
        if (dialog2 != null) {
            currentEntryFragment.setTimePickerDialogListener(dialog2);
        }

        ConfirmDialog dialog4 = (ConfirmDialog) getSupportFragmentManager().findFragmentByTag(Static.DIALOG_CONFIRM_ENTRY_DUPLICATE);
        if (dialog4 != null) {
            currentEntryFragment.setEntryDuplicateConfirmDialogListener(dialog4);
        }

        ConfirmDialog dialog5 = (ConfirmDialog) getSupportFragmentManager().findFragmentByTag(Static.DIALOG_CONFIRM_ENTRY_DELETE);
        if (dialog5 != null) {
            currentEntryFragment.setEntryDeleteConfirmDialogListener(dialog5);
        }

        FolderSelectDialog dialog6 = (FolderSelectDialog) getSupportFragmentManager().findFragmentByTag(Static.DIALOG_FOLDER_SELECT);
        if (dialog6 != null) {
            currentEntryFragment.setFolderSelectDialogListener(dialog6);
        }

        TagsSelectDialog dialog7 = (TagsSelectDialog) getSupportFragmentManager().findFragmentByTag(Static.DIALOG_TAGS_SELECT);
        if (dialog7 != null) {
            currentEntryFragment.setTagsSelectDialogListener(dialog7);
        }

        LocationSelectDialog dialog8 = (LocationSelectDialog) getSupportFragmentManager().findFragmentByTag(Static.DIALOG_LOCATION_SELECT);
        if (dialog8 != null) {
            currentEntryFragment.setLocationSelectDialogListener(dialog8);
        }

        OptionsDialog dialog9 = (OptionsDialog) getSupportFragmentManager().findFragmentByTag(Static.DIALOG_ADD_PHOTO);
        if (dialog9 != null) {
            currentEntryFragment.setAddPhotoOptionsDialogListener(dialog9);
        }

        ConfirmDialog dialog10 = (ConfirmDialog) getSupportFragmentManager().findFragmentByTag(Static.DIALOG_CONFIRM_RATIONALE_LOCATION);

        if (dialog10 != null) {
            PermissionsUtils.setConfirmRationaleDialogListener(this, Manifest.permission.ACCESS_FINE_LOCATION, Static.PERMISSION_REQUEST_LOCATION, dialog10);
        }

        ConfirmDialog dialog11 = (ConfirmDialog) getSupportFragmentManager().findFragmentByTag(Static.DIALOG_CONFIRM_RATIONALE_STORAGE);
        if (dialog11 != null) {
            PermissionsUtils.setConfirmRationaleDialogListener(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Static.PERMISSION_REQUEST_STORAGE, dialog11);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        EntryFragment currentEntryFragment = getCurrentEntryFragment();
        if (currentEntryFragment != null) {
            outState.putString(CURRENT_ENTRY_UID_STATE_KEY, currentEntryFragment.rowUid);
        }
    }

    // -------------------- PERMISSIONS --------------------
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (MyApp.getInstance().securityCodeMgr.isSecurityCodeSet()) {
            MyApp.getInstance().securityCodeMgr.setUnlocked();
        }

        switch (requestCode) {
            case Static.PERMISSION_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    EntryFragment currentEntryFragment = getCurrentEntryFragment();
                    if (currentEntryFragment != null) {
                        currentEntryFragment.requestNewLocation();
                    }
                }
            }
            break;

            case Static.PERMISSION_REQUEST_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startMultiplePhotoPickerActivityOnResume = true;
                } else {
                    PermissionsUtils.showDeniedOpenSettingsDialog(this, Static.DIALOG_CONFIRM_RATIONALE_STORAGE, R.string.unable_to_access_storage);
                }
            }
            break;
        }
    }

    private void showFullscreenAd() {

    }
}
