package com.pixelcrater.Diaro.entries.viewedit;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.activitytypes.TypeActivity;
import com.pixelcrater.Diaro.utils.AppLog;

import java.util.ArrayList;

public class MultiplePhotoPickerActivity extends TypeActivity {
    // State vars
    private static final String SELECTED_PHOTOS_PATHS_ARRAY_LIST_STATE_KEY = "SELECTED_PHOTOS_PATHS_ARRAY_LIST_STATE_KEY";

    private ArrayList<String> photosPathsArrayList = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView photoNumberTextView;
    private TextView noPhotosFoundTextView;
    private MultiplePhotoPickerAdapter multiplePhotoPickerAdapter;
    private Cursor internalImagesCursor;
    private Cursor externalImagesCursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(addViewToContentContainer(R.layout.multiple_photo_picker));
        activityState.setLayoutBackground();

        photoNumberTextView = (TextView) LayoutInflater.from(this).inflate(R.layout.photo_numbering,
                null);

        // Add custom view to ActionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(photoNumberTextView);

        noPhotosFoundTextView = (TextView) findViewById(R.id.no_photos_found);

        final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATE_TAKEN};
        final String orderBy = MediaStore.Images.Media._ID;

        // Photos from internal storage
        internalImagesCursor = getContentResolver().query(MediaStore.Images.Media.INTERNAL_CONTENT_URI, columns, null, null, orderBy + " DESC");
        addToPhotosArrayList(internalImagesCursor);

        // Photos from external storage
        externalImagesCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy + " DESC");
        addToPhotosArrayList(externalImagesCursor);

        // Photo grid RecyclerView
        recyclerView = (RecyclerView) findViewById(R.id.photo_grid);
        recyclerView.setLayoutManager(new GridLayoutManager(this, Static.isLandscape() ? 3 : 2));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        int thumbWidth = Static.getThumbnailWidthForGrid();
        int thumbHeight = (int) (thumbWidth * Static.PHOTO_PROPORTION);
        AppLog.d("thumbWidth: " + thumbWidth + ", thumbHeight: " + thumbHeight);

        multiplePhotoPickerAdapter = new MultiplePhotoPickerAdapter(MultiplePhotoPickerActivity.this, thumbWidth, thumbHeight);
        multiplePhotoPickerAdapter.setPhotosPathsArrayList(photosPathsArrayList);

        if (savedInstanceState != null) {
            multiplePhotoPickerAdapter.setSelectedPhotosPathsArrayList(savedInstanceState.getStringArrayList(SELECTED_PHOTOS_PATHS_ARRAY_LIST_STATE_KEY));
        }

        recyclerView.setAdapter(multiplePhotoPickerAdapter);

        updateSelectedPhotosNumber();
    }

    private void addToPhotosArrayList(Cursor imagesCursor) {
        if (imagesCursor == null) {
            return;
        }

        int count = imagesCursor.getCount();
//        AppLog.d("count: " + count);
        if (count > 0) {
            int dataColumnIndex = imagesCursor.getColumnIndex(MediaStore.Images.Media.DATA);
            for (int i = 0; i < count; i++) {
                imagesCursor.moveToPosition(i);
                photosPathsArrayList.add(imagesCursor.getString(dataColumnIndex));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (internalImagesCursor != null) {
            internalImagesCursor.close();
        }

        if (externalImagesCursor != null) {
            externalImagesCursor.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_multiple_photo_picker, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        showHideMenuIcons(menu);

        return super.onPrepareOptionsMenu(menu);
    }

    private void showHideMenuIcons(Menu menu) {
        menu.findItem(R.id.item_select).setVisible(multiplePhotoPickerAdapter.getSelectedPhotosPathsArrayList().size() > 0);
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

        // Save folder
        else if (itemId == R.id.item_select) {
            finishSelection();
            return true;
        }

        else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void finishSelection() {
        Intent i = new Intent();
        i.putStringArrayListExtra("selected_photos",
                multiplePhotoPickerAdapter.getSelectedPhotosPathsArrayList());
        setResult(RESULT_OK, i);
        finish();
    }

    public void updateSelectedPhotosNumber() {
        String text = "";
        int photoCount = photosPathsArrayList.size();
//        AppLog.d("dayPhotoCount: " + dayPhotoCount);

        if (photoCount > 0) {
            text = getString(R.string.selected) + ": " + multiplePhotoPickerAdapter.getSelectedPhotosPathsArrayList().size();
            recyclerView.setVisibility(View.VISIBLE);
            noPhotosFoundTextView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            noPhotosFoundTextView.setVisibility(View.VISIBLE);
        }
        photoNumberTextView.setText(text);

        // Redraw action bar to show/hide select icon
        supportInvalidateOptionsMenu();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putStringArrayList(SELECTED_PHOTOS_PATHS_ARRAY_LIST_STATE_KEY, multiplePhotoPickerAdapter.getSelectedPhotosPathsArrayList());
    }
}
