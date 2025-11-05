package com.pixelcrater.Diaro.entries.viewedit;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.activitytypes.TypeActivity;
import com.pixelcrater.Diaro.model.AttachmentInfo;
import com.pixelcrater.Diaro.entries.EntriesStatic;
import com.pixelcrater.Diaro.entries.attachments.AttachmentsStatic;
import com.pixelcrater.Diaro.generaldialogs.ConfirmDialog;
import com.pixelcrater.Diaro.storage.OnStorageDataChangeListener;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.AppLog;

import java.util.ArrayList;

public class PhotoGridActivity extends TypeActivity implements OnStorageDataChangeListener {

    public static final String IS_MULTI_SELECT_MODE_STATE_KEY = "IS_MULTI_SELECT_MODE_STATE_KEY";
    private static final String SELECTED_PHOTOS_PATHS_ARRAY_LIST_STATE_KEY = "SELECTED_PHOTOS_PATHS_ARRAY_LIST_STATE_KEY";

    public ActionMode actionMode;
    private String entryUid;
    private PhotoGridDraggableAdapter photoGridDraggableAdapter;
    private GetPhotoGridAsync getPhotoGridAsync;
    private RecyclerView recyclerView;
    private TextView noPhotosFoundTextView;
    private ProgressBar photosProgressBar;

    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            // Inflate our menu from a resource file
            actionMode.getMenuInflater().inflate(R.menu.action_mode_photo_grid, menu);

            showHideMenuIcons(actionMode, menu);

            // Return true so that the action mode is shown
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            showHideMenuIcons(actionMode, menu);

            // Return false if nothing is done
            return false;
        }

        private void showHideMenuIcons(ActionMode actionMode, Menu menu) {
            // Show selected items count
            actionMode.setTitle(String.valueOf(getSelectedPhotosCount()));

            boolean isListEmpty = getEntryPhotosCount() == 0;
            boolean isAllSelected = getSelectedPhotosCount() == getEntryPhotosCount();
//            AppLog.d("isListEmpty: " + isListEmpty + ", isAllSelected: " + isAllSelected);

            menu.findItem(R.id.item_select_all).setVisible(!isListEmpty && !isAllSelected);
            menu.findItem(R.id.item_unselect_all).setVisible(!isListEmpty && isAllSelected);
            menu.findItem(R.id.item_delete).setVisible(!isListEmpty);
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem item) {
            int itemId = item.getItemId();

            if (itemId == R.id.item_unselect_all) {
                unselectAllEntries();
                return true;
            }

            else if (itemId == R.id.item_select_all) {
                selectAllEntries();
                return true;
            }

            else if (itemId == R.id.item_delete) {
                if (getSelectedPhotosCount() == 0) {
                    // Show error
                    Static.showToast(getString(R.string.no_entries_selected), Toast.LENGTH_SHORT);
                } else {
                    showSelectedPhotosDeleteConfirmDialog();
                }
                return true;
            }

            else {
                return false;
            }
        }

        // Allows you to be notified when the action mode is dismissed
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            turnOffMultiSelectMode();
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(addViewToContentContainer(R.layout.entry_photo_grid));
        activityState.setLayoutBackground();

        noPhotosFoundTextView = (TextView) findViewById(R.id.no_photos_found);

        Bundle bundle = getIntent().getExtras();
        entryUid = bundle.getString("entryUid");

        photosProgressBar = (ProgressBar) findViewById(R.id.photos_progress);

        // Photo grid RecyclerView
        recyclerView =  findViewById(R.id.photo_grid);
        recyclerView.setLayoutManager(new GridLayoutManager(this, Static.isLandscape() ? 3 : 2));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        int thumbWidth = Static.getThumbnailWidthForGrid();
        int thumbHeight = (int) (thumbWidth * Static.PHOTO_PROPORTION);
//        AppLog.d("thumbWidth: " + thumbWidth + ", thumbHeight: " + thumbHeight);

        photoGridDraggableAdapter = new PhotoGridDraggableAdapter(PhotoGridActivity.this, thumbWidth, thumbHeight, entryUid, recyclerView);
        recyclerView.setAdapter(photoGridDraggableAdapter);

        if (!getEntryData()) {
            finish();
            return;
        }

        if (savedInstanceState != null) {
            if (savedInstanceState.getBoolean(IS_MULTI_SELECT_MODE_STATE_KEY)) {
                turnOnMultiSelectMode();
            }

            setSelectedPhotosPathsArrayList(savedInstanceState.getStringArrayList(SELECTED_PHOTOS_PATHS_ARRAY_LIST_STATE_KEY));
        }

        // Get photos to the grid
        executeGetPhotoGridAsync();

        MyApp.getInstance().storageMgr.addOnStorageDataChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        notifyPhotoGridAdapter();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_MULTI_SELECT_MODE_STATE_KEY, isMultiSelectMode());
        outState.putStringArrayList(SELECTED_PHOTOS_PATHS_ARRAY_LIST_STATE_KEY, getSelectedPhotosPathsArrayList());
    }

    public void executeGetPhotoGridAsync() {
        cancelGetPhotoGridAsync();

        getPhotoGridAsync = new GetPhotoGridAsync(entryUid);
        getPhotoGridAsync.setOnAsyncFinishListener(entryPhotosArrayList -> {
            recyclerView.setVisibility(View.VISIBLE);

            // Hide progress bar
            photosProgressBar.setVisibility(View.GONE);

            // Add photos
            setEntryPhotosArrayList(entryPhotosArrayList);

            // Check if all selected photos were not deleted
            checkIfSelectedPhotosExist();

            notifyPhotoGridAdapter();

            if (entryPhotosArrayList.size() == 0) {
                recyclerView.setVisibility(View.GONE);
                noPhotosFoundTextView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                noPhotosFoundTextView.setVisibility(View.GONE);
            }
        });
        getPhotoGridAsync.execute();
    }

    public void cancelGetPhotoGridAsync() {
        try {
            if (getPhotoGridAsync != null) {
                getPhotoGridAsync.cancel(true);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        MyApp.getInstance().storageMgr.removeOnStorageDataChangeListener(this);
    }

    private boolean getEntryData() {
        if (entryUid != null) {
            Cursor entryCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSingleEntryCursorByUid(entryUid, true);

            if (entryCursor.getCount() > 0) {
                int photoCount = entryCursor.getInt(entryCursor.getColumnIndex("photo_count"));
                String primaryPhotoUid = entryCursor.getString(entryCursor.getColumnIndex(Tables.KEY_ENTRY_PRIMARY_PHOTO_UID));
                entryCursor.close();

                photoGridDraggableAdapter.setPrimaryPhotoUid(primaryPhotoUid);
                activityState.setActionBarTitle(getSupportActionBar(), getString(R.string.entry_photos) + ": " + photoCount);

                return true;
            }
            entryCursor.close();
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_photo_grid, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        AppLog.d("item: " + item);
        if (activityState.isActivityPaused) {
            return true;
        }

        // Handle presses on the action bar items
        int itemId = item.getItemId();

        // Back
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }

        // Multi select
        else if (itemId == R.id.item_multiselect) {
            turnOnMultiSelectMode();
            return true;
        }

        else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void selectAllEntries() {
        setSelectedPhotosPathsArrayList(getPhotosPathsArrayList());
        // Refresh selected entries count
        actionMode.invalidate();
        notifyPhotoGridAdapter();
    }

    private void unselectAllEntries() {
        clearSelectedPhotos();
        // Refresh selected entries count
        actionMode.invalidate();
        notifyPhotoGridAdapter();
    }

    private void clearSelectedPhotos() {
        photoGridDraggableAdapter.getSelectedPhotosPathsArrayList().clear();
    }

    public void turnOnMultiSelectMode() {
        actionMode = startSupportActionMode(actionModeCallback);
        notifyPhotoGridAdapter();
    }

    private void turnOffMultiSelectMode() {
        actionMode = null;
        clearSelectedPhotos();
        notifyPhotoGridAdapter();
    }

    public boolean isMultiSelectMode() {
        return actionMode != null;
    }

    private ArrayList<String> getPhotosPathsArrayList() {
        ArrayList<String> photosPathsArrayList = new ArrayList<>();

        ArrayList<AttachmentInfo> entryPhotosArrayList = getEntryPhotosArrayList();
        for (AttachmentInfo o : entryPhotosArrayList) {
            photosPathsArrayList.add(o.getFilePath());
        }

        return photosPathsArrayList;
    }

    private ArrayList<AttachmentInfo> getEntryPhotosArrayList() {
        return photoGridDraggableAdapter.getEntryPhotosArrayList();
    }

    private void setEntryPhotosArrayList(ArrayList<AttachmentInfo> entryPhotosArrayList) {
        photoGridDraggableAdapter.setEntryPhotosArrayList(entryPhotosArrayList);
    }

    private int getEntryPhotosCount() {
        return getEntryPhotosArrayList().size();
    }

    private ArrayList<String> getSelectedPhotosPathsArrayList() {
        return photoGridDraggableAdapter.getSelectedPhotosPathsArrayList();
    }

    private void setSelectedPhotosPathsArrayList(ArrayList<String> entryPhotosArrayList) {
        photoGridDraggableAdapter.setSelectedPhotosPathsArrayList(entryPhotosArrayList);
    }

    private void checkIfSelectedPhotosExist() {
        if (isMultiSelectMode()) {
            ArrayList<String> photosPathsArrayList = getPhotosPathsArrayList();
            ArrayList<String> selectedPhotosPathsArrayList = getSelectedPhotosPathsArrayList();

            for (int i = 0; i < selectedPhotosPathsArrayList.size(); i++) {
                if (!photosPathsArrayList.contains(selectedPhotosPathsArrayList.get(i))) {
//                AppLog.d("Photo does not exist in grid: " +  getSelectedPhotosPathsArrayList().get(i));
//                    // Remove from selected photos
                    selectedPhotosPathsArrayList.remove(i);
                }
            }
        }
    }

    private int getSelectedPhotosCount() {
        return getSelectedPhotosPathsArrayList().size();
    }

    private void notifyPhotoGridAdapter() {
        photoGridDraggableAdapter.notifyDataSetChanged();
        if (isMultiSelectMode()) {
            // Refresh selected entries count
            actionMode.invalidate();
        }
    }

    /**
     * Delete selected entries dialog
     */
    private void showSelectedPhotosDeleteConfirmDialog() {
        String dialogTag = Static.DIALOG_CONFIRM_SELECTED_ENTRIES_DELETE;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setTitle(getString(R.string.delete));
            dialog.setMessage(getString(R.string.delete_selected_entries));
            dialog.show(getSupportFragmentManager(), dialogTag);

            // Set dialog listener
            setSelectedPhotosDeleteConfirmDialogListener(dialog);
        }
    }

    private void setSelectedPhotosDeleteConfirmDialogListener(final ConfirmDialog dialog) {
        dialog.setDialogPositiveClickListener(() -> {
            deleteSelectedPhotos();
            finishActionMode();
        });
    }

    private void deleteSelectedPhotos() {
        try {
            ArrayList<AttachmentInfo> photosToDeleteArrayList = new ArrayList<>();

            for (int i = 0; i < getEntryPhotosCount(); i++) {
                AttachmentInfo o = getEntryPhotosArrayList().get(i);

                if (getSelectedPhotosPathsArrayList().contains(o.getFilePath())) {
                    photosToDeleteArrayList.add(o);
                }
            }

            AttachmentsStatic.deleteAttachments(photosToDeleteArrayList);

            for (int i = 0; i < photosToDeleteArrayList.size(); i++) {
                AttachmentInfo o = photosToDeleteArrayList.get(i);
                getEntryPhotosArrayList().remove(o);

                // Clear entry primary photo uid field if this was entry primary photo
                EntriesStatic.clearEntryPrimaryPhotoUidOnPhotoDelete(entryUid, o.uid);
            }
        } catch (Exception e) {
            AppLog.e("Exception: " + e);

            // Show error
            Static.showToast(String.format("%s: %s", getString(R.string.error), e.getMessage()),
                    Toast.LENGTH_SHORT);
        }
    }

    private void finishActionMode() {
        if (isMultiSelectMode()) {
            actionMode.finish();
        }
    }

    @Override
    public void onStorageDataChange() {
        if (!getEntryData()) {
            finish();
            return;
        }

        // Get photos to the grid
        executeGetPhotoGridAsync();
    }
}
