package com.pixelcrater.Diaro.gallery.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.activitytypes.TypeActivity;
import com.pixelcrater.Diaro.entries.async.DeleteEntriesAsync;
import com.pixelcrater.Diaro.entries.viewedit.EntryViewEditActivity;
import com.pixelcrater.Diaro.gallery.Constants;
import com.pixelcrater.Diaro.gallery.GalleryItem;
import com.pixelcrater.Diaro.gallery.adapters.GridImagesAdapter;
import com.pixelcrater.Diaro.gallery.adapters.listeners.GridClickListener;
import com.pixelcrater.Diaro.main.AppMainActivity;
import com.pixelcrater.Diaro.storage.sqlite.SQLiteQueryHelper;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.SectionedGridRecyclerViewAdapter;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by mohamedzakaria on 8/6/16.
 */
public final class MediaActivity extends TypeActivity implements GridClickListener {
    private RecyclerView mRecyclerView;
    private GridImagesAdapter adapter;

    private int imgPlaceHolderResId;
    private int spanCount = 2;

    protected ArrayList<GalleryItem> mGalleryItems = new ArrayList<>();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(addViewToContentContainer(R.layout.z_activity_grid));

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        // get extra values
        imgPlaceHolderResId = getIntent().getIntExtra(Constants.IntentPassingParams.IMG_PLACEHOLDER, -1);
        spanCount = getIntent().getIntExtra(Constants.IntentPassingParams.COUNT, 2);

        // Fetch the data
        ArrayList<GalleryItem> galleryItems = SQLiteQueryHelper.getGalleryItems();
        // Make sure that the photos actually exist
        for (GalleryItem galleryItem : galleryItems) {
            //   AppLog.e(galleryItem.getFilename() + " - " + galleryItem.getEntryUid());
            String filePath = AppLifetimeStorageUtils.getMediaPhotosDirPath() + "/" + galleryItem.getFilename();
            File f = new File(filePath);
            if (f.exists()) {
                mGalleryItems.add(galleryItem);
            }
        }

        adapter = new GridImagesAdapter(this, mGalleryItems, imgPlaceHolderResId);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));
        Toolbar toolbar = (Toolbar) toolbarLayout.findViewById(R.id.toolbar);
        toolbar.setTitle("Photos" + " (" + mGalleryItems.size() + ")");
        Objects.requireNonNull(getSupportActionBar()).setTitle("Photos" + " (" + mGalleryItems.size() + ")");

        //This is the code to provide a sectioned grid
        List<SectionedGridRecyclerViewAdapter.Section> sections = new ArrayList<SectionedGridRecyclerViewAdapter.Section>();
        Cursor yearsCountCursor = null;
        int initialPositon = 0;
        try {
            yearsCountCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getGalleryYearSection();
            while (yearsCountCursor.moveToNext()) {
                //Sections
                String yearName = yearsCountCursor.getString(yearsCountCursor.getColumnIndex("year"));
                int imagesCount = yearsCountCursor.getInt(yearsCountCursor.getColumnIndex("imagesCount"));
                sections.add(new SectionedGridRecyclerViewAdapter.Section(initialPositon, yearName + " (" + imagesCount + ")"));

                initialPositon += imagesCount;
            }
        } finally {
            if (yearsCountCursor != null) {
                yearsCountCursor.close();
            }
        }

        //Add your adapter to the sectionAdapter
        SectionedGridRecyclerViewAdapter.Section[] dummy = new SectionedGridRecyclerViewAdapter.Section[sections.size()];
        SectionedGridRecyclerViewAdapter mSectionedAdapter = new SectionedGridRecyclerViewAdapter(this, R.layout.grid_section, R.id.section_text, mRecyclerView, adapter);
        mSectionedAdapter.setSections(sections.toArray(dummy));


        //Apply this adapter to the RecyclerView
        mRecyclerView.setAdapter(mSectionedAdapter);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(int pos) {
        if (mGalleryItems.isEmpty())
            return;

        GalleryItem galleryItem = mGalleryItems.get(pos);

        Intent intent = new Intent(this, EntryViewEditActivity.class);
        intent.putExtra(Static.EXTRA_SKIP_SC, true);
        intent.putExtra("entryUid", galleryItem.getEntryUid());
        startActivityForResult(intent, Static.REQUEST_VIEW_EDIT_ENTRY);
    }

    // Delete the entries properly
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        AppLog.e("requestCode: " + requestCode + ", resultCode: " + requestCode);
        // Result from view/edit entry activity
        if (requestCode == Static.REQUEST_VIEW_EDIT_ENTRY) {
            if (resultCode == FragmentActivity.RESULT_OK) {
                Bundle extras = data.getExtras();
                String entryUid = extras.getString("entryUid");
                boolean entryArchived = extras.getBoolean("entryArchived");
                // AppLog.d("entryUid: " + entryUid + ", entryArchived: " + entryArchived);
                if (!TextUtils.isEmpty(entryUid)) {
                    if (entryArchived) {
                        showUndoDeleteEntries(entryUid);
                    }
                }
            }
        }
    }

    private void showUndoDeleteEntries(String serializedUids) {
        AppLog.d("serializedUids: " + serializedUids);
        final ArrayList<String> entriesUids = serializedUids.equals("") ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(serializedUids.split(",")));
        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.layout_container), R.string.deleted, Snackbar.LENGTH_LONG);
        mySnackbar.setAction(R.string.undo_delete, new AppMainActivity.MyUndoListener(entriesUids));
        mySnackbar.show();

        mySnackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                    // Delete entries in background
                    AppLog.e("deleting -> " + entriesUids.get(0));
                    DeleteEntriesAsync deleteEntriesAsync = new DeleteEntriesAsync(entriesUids);
                    deleteEntriesAsync.execute();
                }
            }

            @Override
            public void onShown(Snackbar snackbar) {
            }
        });
    }
}
