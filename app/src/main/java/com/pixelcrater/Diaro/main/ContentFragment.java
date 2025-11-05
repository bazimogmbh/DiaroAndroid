package com.pixelcrater.Diaro.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.view.ActionMode;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.Loader;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.entries.async.UpdateEntriesFolderAsync;
import com.pixelcrater.Diaro.entries.async.UpdateEntriesLocationAsync;
import com.pixelcrater.Diaro.entries.async.UpdateEntriesTagAsync;
import com.pixelcrater.Diaro.export.ExportOptions;
import com.pixelcrater.Diaro.export.PdfExport;
import com.pixelcrater.Diaro.folders.FolderSelectDialog;
import com.pixelcrater.Diaro.generaldialogs.ConfirmDialog;
import com.pixelcrater.Diaro.generaldialogs.OptionsDialog;
import com.pixelcrater.Diaro.locations.LocationSelectDialog;
import com.pixelcrater.Diaro.locations.LocationsCursorAdapter;
import com.pixelcrater.Diaro.locations.LocationsStatic;
import com.pixelcrater.Diaro.model.LocationInfo;
import com.pixelcrater.Diaro.model.TagInfo;
import com.pixelcrater.Diaro.settings.PreferencesHelper;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.storage.dropbox.DropboxAccountManager;
import com.pixelcrater.Diaro.storage.dropbox.OnFsSyncStatusChangeListener;
import com.pixelcrater.Diaro.storage.dropbox.SyncService;
import com.pixelcrater.Diaro.tags.TagsCursorAdapter;
import com.pixelcrater.Diaro.tags.TagsSelectDialog;
import com.pixelcrater.Diaro.tags.TagsStatic;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.KeyValuePair;
import com.pixelcrater.Diaro.utils.MyPinnedSectionListView;
import com.pixelcrater.Diaro.utils.MySimpleSectionedListAdapter;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.Static;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import dev.dworks.libs.astickyheader.SimpleSectionedListAdapter;

import static com.pixelcrater.Diaro.utils.Static.EXTRA_SKIP_SC;

public class ContentFragment extends Fragment implements LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String IS_MULTI_SELECT_MODE_STATE_KEY = "IS_MULTI_SELECT_MODE_STATE_KEY";
    public static final String MULTI_SELECTED_ENTRIES_STATE_KEY = "MULTI_SELECTED_PHOTOS_STATE_KEY";
    public static final String TODAY_ENTRIES_COUNT_STATE_KEY = "TODAY_ENTRIES_COUNT_STATE_KEY";

    private static final int ENTRIES_LIST_LOADER = 0;

    public MyPinnedSectionListView entriesListView;
    public LinearLayout topLine;
    public TextView topLineSearch, topLineCalendar, topLineFolder, topLineTags, topLineLocations, topLineMoods, topLineCount;
    public LinearLayout entriesLoaderView;
    public RelativeLayout noEntriesFoundView;
    public boolean isMultiSelectMode;
    public ArrayList<String> multiSelectedEntriesUids = new ArrayList<>();
    public EntriesCursorAdapter entriesCursorAdapter;
    private ImageView topLineClearAll;
    private int todayEntriesCount;
    private MySimpleSectionedListAdapter mySimpleSectionedListAdapter;
    private View view;
    private OnFragmentInteractionListener mListener;
    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.action_mode_main, menu);
            showHideMenuIcons(menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            actionMode.setTitle(String.valueOf(multiSelectedEntriesUids.size()));
            showHideMenuIcons(menu);

            return false;
        }

        private void showHideMenuIcons(Menu menu) {
            int entriesCount = entriesCursorAdapter.getCount();
            boolean isAllSelected = entriesCount != 0 && multiSelectedEntriesUids.size() == entriesCount;

            menu.findItem(R.id.item_select_all).setVisible(!isAllSelected);
            menu.findItem(R.id.item_unselect_all).setVisible(isAllSelected);
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem item) {
            int itemId = item.getItemId();

            if (itemId == R.id.item_unselect_all) {
                unselectAllEntries();
                return true;
            }

            else if (itemId == R.id.item_select_all) {
                int entriesCount = entriesCursorAdapter.getCount();
                if (entriesCount > 0) {
                    selectAllEntries();
                }
                return true;
            }

            else if (itemId == R.id.item_select_folder) {
                if (multiSelectedEntriesUids.size() == 0) {
                    Snackbar.make(view, R.string.no_entries_selected, Snackbar.LENGTH_SHORT).show();
                } else {
                    showSelectedEntriesSetFolderDialog();
                }
                return true;
            }

            else if (itemId == R.id.item_select_tags) {
                if (multiSelectedEntriesUids.size() == 0) {
                    Snackbar.make(view, R.string.no_entries_selected, Snackbar.LENGTH_SHORT).show();
                } else {
                    showSelectedEntriesSetTagsDialog();
                }
                return true;
            }

            else if (itemId == R.id.item_select_location) {
                if (multiSelectedEntriesUids.size() == 0) {
                    Snackbar.make(view, R.string.no_entries_selected, Snackbar.LENGTH_SHORT).show();
                } else {
                    showSelectedEntriesSetLocationDialog();
                }
                return true;
            }

            else if (itemId == R.id.item_print) {
                if (multiSelectedEntriesUids.size() == 0) {
                    Snackbar.make(view, R.string.no_entries_selected, Snackbar.LENGTH_SHORT).show();
                } else {
                    print();
                }
                return true;
            }

            else if (itemId == R.id.item_delete) {
                if (multiSelectedEntriesUids.size() == 0) {
                    Snackbar.make(view, R.string.no_entries_selected, Snackbar.LENGTH_SHORT).show();
                } else {
                    showSelectedEntriesDeleteConfirmDialog();
                }
                return true;
            }

            else {
                return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            turnOffMultiSelectMode();
        }
    };
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public static ContentFragment newInstance() {
        ContentFragment fragment = new ContentFragment();
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
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
        view = inflater.inflate(R.layout.content_fragment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AppLog.d("savedInstanceState: " + savedInstanceState);

        init();
        showEntriesPreloader();

        // Init entries cursor loader
        LoaderManager.getInstance(this).initLoader(ENTRIES_LIST_LOADER, null, this);

        if (savedInstanceState != null) {
            todayEntriesCount = savedInstanceState.getInt(TODAY_ENTRIES_COUNT_STATE_KEY);
            isMultiSelectMode = savedInstanceState.getBoolean(IS_MULTI_SELECT_MODE_STATE_KEY);
            multiSelectedEntriesUids = savedInstanceState.getStringArrayList(MULTI_SELECTED_ENTRIES_STATE_KEY);
            if (isMultiSelectMode) {
                turnOnMultiSelectMode();
            }
        }

        // Restore active dialog listeners
        restoreDialogListeners(savedInstanceState);

        MyApp.getInstance().prefs.registerOnSharedPreferenceChangeListener(this);
    }

    private void restoreDialogListeners(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            ConfirmDialog dialog1 = (ConfirmDialog) getChildFragmentManager().findFragmentByTag(Static.DIALOG_CONFIRM_SELECTED_ENTRIES_DELETE);
            if (dialog1 != null) {
                setSelectedEntriesDeleteConfirmDialogListener(dialog1);
            }

            FolderSelectDialog dialog2 = (FolderSelectDialog) getChildFragmentManager().findFragmentByTag(Static.DIALOG_SELECTED_ENTRIES_SET_FOLDER);
            if (dialog2 != null) {
                setSelectedEntriesSetFolderDialogListener(dialog2);
            }

            OptionsDialog dialog3 = (OptionsDialog) getChildFragmentManager().findFragmentByTag(Static.DIALOG_SORT);
            if (dialog3 != null) {
                setSortDialogListener(dialog3);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(TODAY_ENTRIES_COUNT_STATE_KEY, todayEntriesCount);
        outState.putBoolean(IS_MULTI_SELECT_MODE_STATE_KEY, isMultiSelectMode);
        outState.putStringArrayList(MULTI_SELECTED_ENTRIES_STATE_KEY, multiSelectedEntriesUids);
    }

    private void updateTopLineCount(int count) {
        topLineCount.setText(count == -1 ? "…" : String.valueOf(count));
    }

    private void setTopLineText() {
        // AppLog.d("");

        boolean isAnyFilterActive = false;

        // Hide all views
        topLine.setVisibility(View.GONE);
        topLineSearch.setVisibility(View.GONE);
        topLineCalendar.setVisibility(View.GONE);
        topLineFolder.setVisibility(View.GONE);
        topLineTags.setVisibility(View.GONE);
        topLineLocations.setVisibility(View.GONE);
        topLineMoods.setVisibility(View.GONE);
        topLineClearAll.setVisibility(View.VISIBLE);

        // Search
        String activeSearchText = PreferencesHelper.getActiveSearchText();

        // If search is active
        if (!activeSearchText.equals("")) {
            isAnyFilterActive = true;

            topLineSearch.setVisibility(View.VISIBLE);
            topLineSearch.setText(activeSearchText);
        }

        // Calendar
        long selectedRangeFromMillis = MyApp.getInstance().prefs.getLong(Prefs.PREF_ACTIVE_CALENDAR_RANGE_FROM_MILLIS, 0);
        long selectedRangeToMillis = MyApp.getInstance().prefs.getLong(Prefs.PREF_ACTIVE_CALENDAR_RANGE_TO_MILLIS, 0);

        // If calendar date range is selected
        if (selectedRangeFromMillis != 0 || selectedRangeToMillis != 0) {
            isAnyFilterActive = true;
            topLineCalendar.setVisibility(View.VISIBLE);
            topLineCalendar.setText(String.format("%s - %s", Static.getFormattedDate(selectedRangeFromMillis), Static.getFormattedDate(selectedRangeToMillis)));

        }

        // Folder
        String activeFolderUid = MyApp.getInstance().prefs.getString(Prefs.PREF_ACTIVE_FOLDER_UID, null);
        AppLog.d("activeFolderUid: " + activeFolderUid);

        // If any folder is selected
        if (activeFolderUid != null) {
            if (activeFolderUid.equals("")) {
                String folderTitle = (String) getText(R.string.no_folder);

                isAnyFilterActive = true;
                topLineFolder.setVisibility(View.VISIBLE);
                topLineFolder.setText(folderTitle);
            } else {
                Cursor folderCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSingleFolderCursorByUid(activeFolderUid);
                if (folderCursor.getCount() == 0) {
                    folderCursor.close();

                    if (mListener != null) {
                        mListener.onActiveFolderNotFound();
                    }

                    refreshEntriesList();
                    return;
                } else {
                    isAnyFilterActive = true;
                    topLineFolder.setVisibility(View.VISIBLE);
                    topLineFolder.setText(folderCursor.getString(folderCursor.getColumnIndex(Tables.KEY_FOLDER_TITLE)));
                }
                folderCursor.close();
            }
        }

        // Tags
        ArrayList<String> activeTagsUidsArrayList = TagsStatic.getActiveTagsUidsArrayList();
        AppLog.d("activeTagsUidsArrayList: " + activeTagsUidsArrayList);

        // If any tag is selected
        if (activeTagsUidsArrayList.size() > 0) {
            ArrayList<TagInfo> selectedTagsArrayList = new ArrayList<>();

            boolean isSetNoTags = false;

            for (int i = 0; i < activeTagsUidsArrayList.size(); i++) {
                String tagUid = activeTagsUidsArrayList.get(i);
                // AppLog.d("tagUid: " + tagUid);

                if (TextUtils.equals(tagUid, TagsCursorAdapter.NO_TAGS_UID)) {
                    isSetNoTags = true;
                } else if (!tagUid.equals("")) {
                    // Get tag row
                    Cursor tagCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSingleTagCursorByUid(tagUid);

                    // If not found
                    if (tagCursor.getCount() == 0) {
                        tagCursor.close();

                        if (mListener != null) {
                            mListener.onActiveTagNotFound(tagUid);
                        }

                        refreshEntriesList();
                        return;
                    } else {
                        TagInfo o = new TagInfo(tagCursor);
                        selectedTagsArrayList.add(o);
                    }
                    tagCursor.close();
                }
            }

            // If selected tags array list not empty
            int selectedTagsCount = selectedTagsArrayList.size();
            if (selectedTagsCount > 0 || isSetNoTags) {
                isAnyFilterActive = true;

                // Sort tags by title
                Collections.sort(selectedTagsArrayList, new Static.ComparatorTags());

                StringBuilder tagsTitlesList = new StringBuilder();

                for (int i = 0; i < selectedTagsCount; i++) {
                    if (!tagsTitlesList.toString().equals(""))
                        tagsTitlesList.append(", ");
                    tagsTitlesList.append(selectedTagsArrayList.get(i).title);
                }

                if (isSetNoTags) {
                    if (!tagsTitlesList.toString().equals("")) {
                        tagsTitlesList.append(", ");
                    }
                    tagsTitlesList.append(getText(R.string.no_tags).toString());
                }

                topLineTags.setVisibility(View.VISIBLE);
                topLineTags.setText(tagsTitlesList.toString());
            }
        }

        // Locations
        ArrayList<String> activeLocationsUidsArrayList = LocationsStatic.getActiveLocationsUidsArrayList();
        AppLog.d("activeLocationsUidsArrayList: " + activeLocationsUidsArrayList);

        // If any location is selected
        if (activeLocationsUidsArrayList.size() > 0) {
            ArrayList<LocationInfo> selectedLocationsArrayList = new ArrayList<>();

            boolean isSetNoLocation = false;

            for (int i = 0; i < activeLocationsUidsArrayList.size(); i++) {
                String locationUid = activeLocationsUidsArrayList.get(i);
                // AppLog.d("locationUid: " + locationUid);

                if (locationUid.equals(LocationsCursorAdapter.NO_LOCATION_UID)) {
                    isSetNoLocation = true;
                } else if (!locationUid.equals("")) {
                    // Get location row
                    Cursor locationCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSingleLocationCursorByUid(locationUid);

                    // If not found
                    if (locationCursor.getCount() == 0) {
                        locationCursor.close();

                        if (mListener != null) {
                            mListener.onActiveLocationNotFound(locationUid);
                        }

                        refreshEntriesList();
                        return;
                    } else {
                        LocationInfo o = new LocationInfo(locationCursor);
                        selectedLocationsArrayList.add(o);
                    }
                    locationCursor.close();
                }
            }

            // If selected locations array list not empty
            int selectedLocationsCount = selectedLocationsArrayList.size();
            if (selectedLocationsCount > 0 || isSetNoLocation) {
                isAnyFilterActive = true;

                // Sort locations
                Collections.sort(selectedLocationsArrayList, new Static.ComparatorLocations());

                StringBuilder locationsTitlesList = new StringBuilder();

                for (int i = 0; i < selectedLocationsCount; i++) {
                    LocationInfo locationInfo = selectedLocationsArrayList.get(i);

                    if (i > 0) {
                        locationsTitlesList.append(", ");
                    }
                    String locationTitle = locationInfo.title.equals("") ? locationInfo.address : locationInfo.title;
                    locationsTitlesList.append(locationTitle);
                }

                if (isSetNoLocation) {
                    if (!locationsTitlesList.toString().equals("")) {
                        locationsTitlesList.append(", ");
                    }
                    locationsTitlesList.append(getText(R.string.no_location).toString());
                }

                topLineLocations.setVisibility(View.VISIBLE);
                topLineLocations.setText(locationsTitlesList.toString());
            }
        }

        // Mood
        String activeMoodUid = MyApp.getInstance().prefs.getString(Prefs.PREF_ACTIVE_MOOD_UID, null);
        AppLog.d("activeMoodUid: " + activeMoodUid);

        // If any folder is selected
        if (activeMoodUid != null) {
            if (activeMoodUid.equals("")) {
                String moodTitle = (String) getText(R.string.mood_none);

                isAnyFilterActive = true;
                topLineMoods.setVisibility(View.VISIBLE);
                topLineMoods.setText(moodTitle);
            } else {
                Cursor moodCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSingleMoodCursorByUid(activeMoodUid);
                if (moodCursor.getCount() == 0) {
                    moodCursor.close();

                    if (mListener != null) {
                        mListener.onActiveMoodNotFound();
                    }

                    refreshEntriesList();
                    return;
                } else {
                    isAnyFilterActive = true;
                    topLineMoods.setVisibility(View.VISIBLE);
                    topLineMoods.setText(moodCursor.getString(moodCursor.getColumnIndex(Tables.KEY_MOOD_TITLE)));
                }
                moodCursor.close();
            }
        }

        if (isAnyFilterActive) {
            topLine.setVisibility(View.VISIBLE);
        }
    }

    private void init() {
        // Top line view
        topLine = (LinearLayout) view.findViewById(R.id.top_line);
        topLineSearch = (TextView) view.findViewById(R.id.top_line_search);
        topLineCalendar = (TextView) view.findViewById(R.id.top_line_calendar);
        topLineFolder = (TextView) view.findViewById(R.id.top_line_folder);
        topLineTags = (TextView) view.findViewById(R.id.top_line_tags);
        topLineLocations = (TextView) view.findViewById(R.id.top_line_locations);
        topLineMoods = (TextView) view.findViewById(R.id.top_line_moods);
        topLineCount = (TextView) view.findViewById(R.id.top_line_count);
        topLineClearAll = (ImageView) view.findViewById(R.id.top_line_clear_all);

        // Clear search
        topLineSearch.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onTopLineClearSearchButtonClicked();
            }
            refreshEntriesList();
        });

        // Clear calendar date range
        topLineCalendar.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.openSideMenu();
                //  mListener.onTopLineClearCalendarButtonClicked();
            }
            refreshEntriesList();
        });

        // Clear folder
        topLineFolder.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onTopLineClearFolderButtonClicked();
            }
            refreshEntriesList();
        });

        // Clear tags
        topLineTags.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onTopLineClearTagsButtonClicked();
            }
            refreshEntriesList();
        });

        // Clear locations
        topLineLocations.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onTopLineClearLocationsButtonClicked();
            }
            refreshEntriesList();
        });

        // Clear moods
        topLineMoods.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onTopLineClearMoodButtonClicked();
            }
            refreshEntriesList();
        });

        // Clear all filters
        topLineClearAll.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onTopLineClearAllFiltersButtonClicked();
            }
            refreshEntriesList();
        });

        // Entries preloader
        entriesLoaderView = (LinearLayout) view.findViewById(R.id.entries_loader);
        entriesLoaderView.setBackgroundColor(MyThemesUtils.getOverlayBackgroundColor());

        // No entries found text
        noEntriesFoundView = (RelativeLayout) view.findViewById(R.id.no_entries_found);

        // Entries list
        entriesListView = (MyPinnedSectionListView) view.findViewById(R.id.entries_list);
        entriesListView.setFastScrollEnabled(PreferencesHelper.isFastScrollEnabled());

        int dispayDensity = PreferencesHelper.getDisplayDensity();
        if (dispayDensity == 1) {
            int val = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
          //  entriesListView.setDividerHeight(val);
        }

        // Set list adapter
        entriesCursorAdapter = new EntriesCursorAdapter(getActivity(), this, null, 0);

        mySimpleSectionedListAdapter = new MySimpleSectionedListAdapter(getActivity(), entriesCursorAdapter, R.layout.entry_list_item_header, R.id.header);

        entriesListView.setAdapter(mySimpleSectionedListAdapter);

        // OnItemClick
        entriesListView.setOnItemClickListener((a, view, position, id) -> {
            AppLog.d("position: " + position);

            if (position != -1) {
                int cursorPos = mySimpleSectionedListAdapter.sectionedPositionToPosition(position);
                String entryUid = entriesCursorAdapter.getItemUid(cursorPos);
                if (!TextUtils.isEmpty(entryUid)) {
                    if (isMultiSelectMode) {
                        selectUnselectEntry(entryUid);
                    } else {
                        if (mListener != null) {
                            mListener.onShouldStartEntryViewEditActivity(entryUid);
                        }
                    }
                }
            }
        });

        // OnItemLongClick
        entriesListView.setOnItemLongClickListener((parent, view, position, id) -> {
            AppLog.d("position: " + position);

            if (!isMultiSelectMode) {
                turnOnMultiSelectMode();
            }

            int cursorPos = mySimpleSectionedListAdapter.sectionedPositionToPosition(position);
            String entryUid = entriesCursorAdapter.getItemUid(cursorPos);

            if (!TextUtils.isEmpty(entryUid)) {
                selectUnselectEntry(entryUid);
            }

            return true;
        });

        // FAB
        initFab();

        mSwipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        if (DropboxAccountManager.isLoggedIn(getContext()) && Static.isProUser()) {
            mSwipeRefreshLayout.setEnabled(true);
            mSwipeRefreshLayout.setColorSchemeResources(R.color.md_pink_500, R.color.md_indigo_500, R.color.md_lime_500);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    //   DropboxLocalHelper.clearAllFolderCursors();
                    // Toggle a sync
                    SyncService.startService();
                    try {
                        MyApp.getInstance().storageMgr.dbxFsAdapter.addOnFsSyncStatusChangeListener(new OnFsSyncStatusChangeListener() {
                            @Override
                            public void onFsSyncStatusChange() {
                                boolean running = MyApp.getInstance().asyncsMgr.isSyncAsyncRunning();
                                if (!running) {
                                    mSwipeRefreshLayout.setRefreshing(false);
                                    MyApp.getInstance().storageMgr.dbxFsAdapter.removeOnFsSyncStatusChangeListener(this);
                                }
                            }
                        });
                    } catch (Exception e) {
                        if (mSwipeRefreshLayout != null)
                            mSwipeRefreshLayout.setRefreshing(false);
                    }

                }
            });
            entriesListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    mSwipeRefreshLayout.setEnabled(firstVisibleItem == 0);
                    /** if (firstVisibleItem > 2 && fab.getVisibility() == View.VISIBLE) {
                     fab.hide();
                     } else {
                     if (fab.getVisibility() != View.VISIBLE)
                     fab.show();
                     }
                     *               */
                }
            });
        } else {
            mSwipeRefreshLayout.setEnabled(false);
        }
    }


    public void initFab() {
        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setVisibility(View.VISIBLE);
        fab.setBackgroundTintList(ColorStateList.valueOf(MyThemesUtils.getAccentColor()));
        fab.setRippleColor(MyThemesUtils.getDarkColor(MyThemesUtils.getAccentColorCode()));

        // OnClickListener
        fab.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onShouldStartEntryViewEditActivity(null);
            }
        });

    }

    protected void selectUnselectEntry(String entryUid) {
        if (multiSelectedEntriesUids.contains(entryUid)) {
            multiSelectedEntriesUids.remove(entryUid);
        } else {
            multiSelectedEntriesUids.add(entryUid);
        }

        if (mListener != null) {
            mListener.onSelectUnselectEntry();
        }

        entriesListView.invalidateViews();
    }

    private void showEntriesPreloader() {
        // AppLog.d("");

        if (entriesLoaderView != null)
            entriesLoaderView.setVisibility(View.VISIBLE);

        if (noEntriesFoundView != null)
            noEntriesFoundView.setVisibility(View.GONE);

        try{
            setTopLineText();
        } catch (Exception e){
        }

        updateTopLineCount(-1);
    }

    private void hideEntriesPreloader() {
        int entriesCount = entriesCursorAdapter.getCount();
        AppLog.d("entriesCount: " + entriesCount);

        if (MyApp.getInstance().asyncsMgr.isArchiveEntriesAsyncRunning()) {
            return;
        }

        entriesLoaderView.setVisibility(View.GONE);
        noEntriesFoundView.setVisibility(View.GONE);

        if (entriesCount == 0) {
            noEntriesFoundView.setVisibility(View.VISIBLE);
        }

        updateTopLineCount(entriesCount);
    }

    void refreshEntriesList() {

        if (entriesCursorAdapter != null)
            entriesCursorAdapter.swapCursor(null);

        showEntriesPreloader();

        if (mListener != null) {
            mListener.onStartedEntriesRefresh();
        }

        // Refresh loader
        Loader<Object> entriesLoader = LoaderManager.getInstance(this).getLoader(ENTRIES_LIST_LOADER);
        if (entriesLoader != null) {
            entriesLoader.onContentChanged();
        }
    }

    /**
     * Delete selected entries dialog
     */

    private void showSelectedEntriesDeleteConfirmDialog() {
        String dialogTag = Static.DIALOG_CONFIRM_SELECTED_ENTRIES_DELETE;
        if (getChildFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setTitle(getString(R.string.delete));
            dialog.setMessage(getString(R.string.delete_selected_entries));
            dialog.show(getChildFragmentManager(), dialogTag);

            // Set dialog listener
            setSelectedEntriesDeleteConfirmDialogListener(dialog);
        }
    }

    private void setSelectedEntriesDeleteConfirmDialogListener(final ConfirmDialog dialog) {
        dialog.setDialogPositiveClickListener(() -> {
            showEntriesPreloader();
            // Archive entries in background
            MyApp.getInstance().asyncsMgr.executeArchiveEntriesAsync(multiSelectedEntriesUids);

            if (mListener != null) {
                mListener.onTurnOffMultiSelectMode();
            }
        });
    }

    /**
     * Set folder for selected entries dialog
     */
    private void showSelectedEntriesSetFolderDialog() {
        String dialogTag = Static.DIALOG_SELECTED_ENTRIES_SET_FOLDER;
        if (getChildFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            FolderSelectDialog dialog = new FolderSelectDialog();
            String folderUid = getSelectedEntriesFolderUid();
            if (folderUid != null) {
                dialog.setSelectedFolderUid(folderUid);
            }
            dialog.show(getChildFragmentManager(), dialogTag);

            // Set dialog listener
            setSelectedEntriesSetFolderDialogListener(dialog);
        }
    }

    private void setSelectedEntriesSetFolderDialogListener(FolderSelectDialog dialog) {
        dialog.setOnDialogItemClickListener(selectedFolderUid -> {
            // Update entries folder in background
            UpdateEntriesFolderAsync updateEntriesFolderAsync = new UpdateEntriesFolderAsync(getActivity(), multiSelectedEntriesUids, selectedFolderUid);
            updateEntriesFolderAsync.execute();

            if (mListener != null) {
                mListener.onTurnOffMultiSelectMode();
            }
        });
    }

    private String getSelectedEntriesFolderUid() {
        String folderUid = null;

        String[] whereArgs = new String[1];
        whereArgs[0] = "";

        for (int i = 0; i < multiSelectedEntriesUids.size(); i++) {
            if (i == multiSelectedEntriesUids.size() - 1) {
                whereArgs[0] += "'" + multiSelectedEntriesUids.get(i) + "'";
            } else {
                whereArgs[0] += "'" + multiSelectedEntriesUids.get(i) + "',";
            }
        }

        Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getRowsColumnsCursor("diaro_entries", new String[]{"folder_uid"}, "WHERE uid IN (" + whereArgs[0] + ") AND uid!=?", new String[]{""});
        AppLog.d("cursor.getCount(): " + cursor.getCount());

        while (cursor.moveToNext()) {
            String currentFolderUid = cursor.getString(cursor.getColumnIndex("folder_uid"));
            AppLog.d("currentFolderUid: " + currentFolderUid);
            if (folderUid == null) {
                folderUid = currentFolderUid;
                continue;
            }
            if (!folderUid.equals(currentFolderUid)) {
                return null;
            }
        }

        cursor.close();
        return folderUid;
    }

    /**
     * Set tags for selected entries dialog
     */

    private void showSelectedEntriesSetTagsDialog() {
        String dialogTag = Static.DIALOG_SELECTED_ENTRIES_SET_TAGS;
        if (getChildFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            TagsSelectDialog dialog = new TagsSelectDialog();
            dialog.setSelectedTagsUids("");
            dialog.show(getChildFragmentManager(), dialogTag);

            // Set dialog listener
            setSelectedEntriesSetTagsDialogListener(dialog);
        }
    }

    private void setSelectedEntriesSetTagsDialogListener(TagsSelectDialog dialog) {
        dialog.setOnDialogSaveClickListener(selectedTags -> {
            // Update entries folder in background
            UpdateEntriesTagAsync updateEntriesTagAsync = new UpdateEntriesTagAsync(getActivity(), multiSelectedEntriesUids, selectedTags);
            updateEntriesTagAsync.execute();

            if (mListener != null) {
                mListener.onTurnOffMultiSelectMode();
            }
        });
    }

    /**
     * Set location for selected entries dialog
     */
    private void showSelectedEntriesSetLocationDialog() {
        String dialogTag = Static.DIALOG_SELECTED_ENTRIES_SET_LOCATION;
        if (getChildFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            LocationSelectDialog dialog = new LocationSelectDialog();
            String selectedLocation = getSelectedEntriesLocationUid();
            if (selectedLocation != null) {
                dialog.setSelectedLocationUid(selectedLocation);
            }
            dialog.show(getChildFragmentManager(), dialogTag);

            setSelectedEntriesLocationDialogListener(dialog);
        }
    }

    public void setSelectedEntriesLocationDialogListener(LocationSelectDialog dialog) {
        dialog.setOnDialogItemClickListener(selectedFolderUid -> {
            UpdateEntriesLocationAsync updateEntriesLocationAsync = new UpdateEntriesLocationAsync(getActivity(), multiSelectedEntriesUids, selectedFolderUid);
            updateEntriesLocationAsync.execute();

            if (mListener != null) {
                mListener.onTurnOffMultiSelectMode();
            }
        });
    }

    private String getSelectedEntriesLocationUid() {
        String locationUid = null;

        String[] whereArgs = new String[1];
        whereArgs[0] = "";

        for (int i = 0; i < multiSelectedEntriesUids.size(); i++) {
            if (i == multiSelectedEntriesUids.size() - 1) {
                whereArgs[0] += "'" + multiSelectedEntriesUids.get(i) + "'";
            } else {
                whereArgs[0] += "'" + multiSelectedEntriesUids.get(i) + "',";
            }
        }

        Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getRowsColumnsCursor("diaro_entries", new String[]{"location_uid"}, "WHERE uid IN (" + whereArgs[0] + ") AND uid!=?", new String[]{""});
        AppLog.d("cursor.getCount(): " + cursor.getCount());

        while (cursor.moveToNext()) {
            String currentLocationUid = cursor.getString(cursor.getColumnIndex("location_uid"));
            AppLog.d("currentLocationUid: " + currentLocationUid);
            if (locationUid == null) {
                locationUid = currentLocationUid;
                continue;
            }
            if (!locationUid.equals(currentLocationUid)) {
                return null;
            }
        }

        cursor.close();

        return locationUid;
    }

    private void print() {
        String layout = PreferencesHelper.getExportLayout();
        String photoHeight = PreferencesHelper.getExportPhotoHeight();
        ExportOptions options = new ExportOptions(layout, photoHeight);

        PdfExport.export(multiSelectedEntriesUids, options, null, getActivity());
    }

    private void selectAllEntries() {
        if (entriesCursorAdapter == null) {
            return;
        }

        // Select all entries in background
        MyApp.getInstance().asyncsMgr.executeSelectAllEntriesAsync(getActivity());
    }

    private void unselectAllEntries() {
        multiSelectedEntriesUids.clear();

        if (mListener != null) {
            mListener.onSelectUnselectEntry();
        }

        entriesListView.invalidateViews();
    }

    void turnOnMultiSelectMode() {
        isMultiSelectMode = true;
        entriesListView.invalidateViews();

        if (mListener != null) {
            mListener.onTurnOnMultiSelectMode(actionModeCallback);
        }
    }

    private void turnOffMultiSelectMode() {
        MyApp.getInstance().asyncsMgr.cancelSelectAllEntriesAsync();
        MyApp.getInstance().asyncsMgr.cancelCheckIfAllEntriesExistAsync();

        isMultiSelectMode = false;
        multiSelectedEntriesUids.clear();
        entriesListView.invalidateViews();
    }

    private ArrayList<KeyValuePair> getSortOptions() {
        ArrayList<KeyValuePair> options = new ArrayList<>();
        options.add(new KeyValuePair(String.valueOf(Prefs.SORT_NEWEST_FIRST), getString(R.string.newest_first)));
        options.add(new KeyValuePair(String.valueOf(Prefs.SORT_OLDEST_FIRST), getString(R.string.oldest_first)));

        return options;
    }

    void showSortDialog() {
        String dialogTag = Static.DIALOG_SORT;
        if (getChildFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            OptionsDialog dialog = new OptionsDialog();
            dialog.setTitle(getString(R.string.sort));

            // Set selected value
            String selectedValue = String.valueOf(MyApp.getInstance().prefs.getInt(Prefs.PREF_ENTRIES_SORT, Prefs.SORT_NEWEST_FIRST));
            int selectedIndex = 0;

            ArrayList<KeyValuePair> options = getSortOptions();

            ArrayList<String> items = new ArrayList<>();
            for (int i = 0; i < options.size(); i++) {
                KeyValuePair option = options.get(i);
                items.add(option.value);
                if (option.key.equals(selectedValue)) {
                    selectedIndex = i;
                }
            }
            dialog.setItemsTitles(items);
            dialog.setSelectedIndex(selectedIndex);

            dialog.show(getChildFragmentManager(), dialogTag);

            // Set dialog listener
            setSortDialogListener(dialog);
        }
    }

    private void setSortDialogListener(OptionsDialog dialog) {
        dialog.setDialogItemClickListener(which -> {
            MyApp.getInstance().prefs.edit().putInt(Prefs.PREF_ENTRIES_SORT, Integer.parseInt(getSortOptions().get(which).key)).apply();
            // Refresh entries list
            refreshEntriesList();

            if (mListener != null) {
                mListener.onSortChanged();
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        AppLog.d("loaderId: " + loaderId);

        if (loaderId == ENTRIES_LIST_LOADER) {
            return new EntriesCursorLoader(getActivity());
        }// An invalid id was passed in
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        AppLog.d("loader.getId(): " + loader.getId());
        // if (cursor != null) {
        // AppLog.d("cursor.getCount(): " + cursor.getCount() +
        // ", entriesSectionsArray: " +
        // MyApp.getInstance().entriesSectionsArray);
        // }

        if (loader.getId() == ENTRIES_LIST_LOADER) {
            EntriesCursorLoader entriesCursorLoader = (EntriesCursorLoader) loader;

            if (entriesCursorLoader.entriesSectionsArray != null) {
                // Make a copy of entriesCursorLoader.entriesSectionsArray
                SimpleSectionedListAdapter.Section[] entriesSectionsArray = Arrays.copyOf(entriesCursorLoader.entriesSectionsArray, entriesCursorLoader.entriesSectionsArray.length);

                mySimpleSectionedListAdapter.setSections(entriesSectionsArray);
                entriesCursorAdapter.swapCursor(cursor);

                // Set search keyword
                entriesCursorAdapter.setSearchKeyword(PreferencesHelper.getActiveSearchText());

                setTopLineText();
            }

            hideEntriesPreloader();

            if (mListener != null) {
                mListener.onSelectUnselectEntry();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        AppLog.d("loader.getId(): " + loader.getId());

        if (loader.getId() == ENTRIES_LIST_LOADER) {
            entriesCursorAdapter.swapCursor(null);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (!isAdded() || getActivity() == null)
            return;

        if (key.equals(Prefs.PREF_UNITS)) {
            if (entriesCursorAdapter != null)
                entriesCursorAdapter.setIsFahrenheit(PreferencesHelper.isPrefUnitFahrenheit());
        }

        if (key.equals(Prefs.PREF_FONT)) {
            try {
                MyApp.getInstance().prefs.unregisterOnSharedPreferenceChangeListener(this);
                Intent intent = requireActivity().getIntent();
                intent.putExtra(EXTRA_SKIP_SC, true);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                getActivity().finish();
                startActivity(intent);
            } catch (Exception ignored) {
            }

        }

        if (key.equals(Prefs.PREF_DISPLAY_DENSITY)) {
            if (entriesListView != null) {
                int dispayDensity = PreferencesHelper.getDisplayDensity();
                if (dispayDensity == 1) {
                    int val = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, MyApp.getInstance().getResources().getDisplayMetrics());
                 //   entriesListView.setDividerHeight(val);
                } else {
                    int val = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, MyApp.getInstance().getResources().getDisplayMetrics());
                  //  entriesListView.setDividerHeight(val);
                }

                entriesListView.invalidate();
            }

        }
        if (key.equals(Prefs.PREF_FAST_SCROLL_ENABLED)) {
            entriesListView.setFastScrollEnabled(PreferencesHelper.isFastScrollEnabled());
        }
    }

    public interface OnFragmentInteractionListener {
        void onEntryArchived(String entryUid);

        void onTopLineClearSearchButtonClicked();

        void onTopLineClearCalendarButtonClicked();

        void onTopLineClearFolderButtonClicked();

        void onTopLineClearTagsButtonClicked();

        void onTopLineClearLocationsButtonClicked();

        void onTopLineClearMoodButtonClicked();

        void onTopLineClearAllFiltersButtonClicked();

        void openSideMenu();

        void onSortChanged();

        void onActiveFolderNotFound();

        void onActiveTagNotFound(String tagUid);

        void onActiveLocationNotFound(String locationUid);

        void onActiveMoodNotFound();

        void onStartedEntriesRefresh();

        void onSelectUnselectEntry();

        void onTurnOnMultiSelectMode(ActionMode.Callback actionModeCallback);

        void onTurnOffMultiSelectMode();

        void onShouldStartEntryViewEditActivity(String entryUid);


    }
}
