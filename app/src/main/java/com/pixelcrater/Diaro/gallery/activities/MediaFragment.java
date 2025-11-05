package com.pixelcrater.Diaro.gallery.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.entries.async.DeleteEntriesAsync;
import com.pixelcrater.Diaro.entries.viewedit.EntryViewEditActivity;
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

public class MediaFragment extends Fragment implements GridClickListener {

    private RecyclerView mRecyclerView;

    RelativeLayout no_entries_found;
    private GridImagesAdapter adapter;

    private int imgPlaceHolderResId;
    private int spanCount = 3;

    protected ArrayList<GalleryItem> mGalleryItems = new ArrayList<>();
    List<SectionedGridRecyclerViewAdapter.Section> sections = new ArrayList<SectionedGridRecyclerViewAdapter.Section>();
    SectionedGridRecyclerViewAdapter mSectionedAdapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.z_activity_grid, container, false);

        no_entries_found = view.findViewById(R.id.no_entries_found);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        // get extra values
        imgPlaceHolderResId = -1;

        mGalleryItems = new ArrayList<>();

        adapter = new GridImagesAdapter(getContext(), this, mGalleryItems, imgPlaceHolderResId);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
        // Toolbar toolbar = (Toolbar) getActivity().toolbarLayout.findViewById(R.id.toolbar);
        //  toolbar.setTitle("Photos" + " (" + mGalleryItems.size() + ")");
        //  Objects.requireNonNull(getSupportActionBar()).setTitle("Photos" + " (" + mGalleryItems.size() + ")");

        //Add your adapter to the sectionAdapter
        SectionedGridRecyclerViewAdapter.Section[] dummy = new SectionedGridRecyclerViewAdapter.Section[sections.size()];
        mSectionedAdapter = new SectionedGridRecyclerViewAdapter(getContext(), R.layout.grid_section, R.id.section_text, mRecyclerView, adapter);
        mSectionedAdapter.setSections(sections.toArray(dummy));

        //Apply this adapter to the RecyclerView
        mRecyclerView.setAdapter(mSectionedAdapter);

        return view;
    }


    public void onViewCreated(View view, Bundle savedInstanceState) {
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fetchData();
    }

    private void fetchData() {
        mGalleryItems.clear();
        sections.clear();

        // Fetch the data
        ArrayList<GalleryItem> galleryItems = SQLiteQueryHelper.getGalleryItems();

        if (galleryItems.size() == 0) {
            no_entries_found.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);

        } else {
            no_entries_found.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);

            // Make sure that the photos actually exist
            for (GalleryItem galleryItem : galleryItems) {
                //   AppLog.e(galleryItem.getFilename() + " - " + galleryItem.getEntryUid());
                String filePath = AppLifetimeStorageUtils.getMediaPhotosDirPath() + "/" + galleryItem.getFilename();
                File f = new File(filePath);
                if (f.exists()) {
                    mGalleryItems.add(galleryItem);
                }
            }

            //This is the code to provide a sectioned grid
            sections = new ArrayList<SectionedGridRecyclerViewAdapter.Section>();
            int initialPositon = 0;
            try (Cursor yearsCountCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getGalleryYearSection()) {
                while (yearsCountCursor.moveToNext()) {
                    //Sections
                    String yearName = yearsCountCursor.getString(yearsCountCursor.getColumnIndex("year"));
                    int imagesCount = yearsCountCursor.getInt(yearsCountCursor.getColumnIndex("imagesCount"));
                    sections.add(new SectionedGridRecyclerViewAdapter.Section(initialPositon, yearName + " (" + imagesCount + ")"));

                    initialPositon += imagesCount;
                }
            }


        }

        adapter.notifyDataSetChanged();
        SectionedGridRecyclerViewAdapter.Section[] dummy = new SectionedGridRecyclerViewAdapter.Section[sections.size()];
        mSectionedAdapter.setSections(sections.toArray(dummy));
        mSectionedAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(int pos) {
        if (mGalleryItems.isEmpty())
            return;

        GalleryItem galleryItem = mGalleryItems.get(pos);

        Intent intent = new Intent(getContext(), EntryViewEditActivity.class);
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

        fetchData();
    }

    private void showUndoDeleteEntries(String serializedUids) {
        AppLog.d("serializedUids: " + serializedUids);
        final ArrayList<String> entriesUids = serializedUids.equals("") ? new ArrayList<String>() : new ArrayList<>(Arrays.asList(serializedUids.split(",")));
        Snackbar mySnackbar = Snackbar.make(getView().findViewById(R.id.layout_container), R.string.deleted, Snackbar.LENGTH_LONG);
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

                    fetchData();
                }
            }

            @Override
            public void onShown(Snackbar snackbar) {
            }
        });
    }
}






