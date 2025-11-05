package com.sandstorm.diary.piceditor.features.picker.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.sandstorm.diary.piceditor.R;
import com.sandstorm.diary.piceditor.features.picker.PhotoPicker;
import com.sandstorm.diary.piceditor.features.picker.adapter.PhotoGridAdapter;
import com.sandstorm.diary.piceditor.features.picker.adapter.PopupDirectoryListAdapter;
import com.sandstorm.diary.piceditor.features.picker.entity.Photo;
import com.sandstorm.diary.piceditor.features.picker.entity.PhotoDirectory;
import com.sandstorm.diary.piceditor.features.picker.utils.AndroidLifecycleUtils;
import com.sandstorm.diary.piceditor.features.picker.utils.ImageCaptureManager;
import com.sandstorm.diary.piceditor.features.picker.utils.MediaStoreHelper;
import com.sandstorm.diary.piceditor.features.picker.utils.PermissionsUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PhotoPickerFragment extends Fragment {
    public static int COUNT_MAX = 4;
    private static final String EXTRA_CAMERA = "camera";
    private static final String EXTRA_COLUMN = "column";
    private static final String EXTRA_COUNT = "count";
    private static final String EXTRA_GIF = "gif";
    private static final String EXTRA_ORIGIN = "origin";

    public int SCROLL_THRESHOLD = 30;
    private ImageCaptureManager captureManager;
    int column;

    public List<PhotoDirectory> directories;
    private PopupDirectoryListAdapter listAdapter;

    public ListPopupWindow listPopupWindow;

    public RequestManager mGlideRequestManager;
    private ArrayList<String> originalPhotos;

    public PhotoGridAdapter photoGridAdapter;

    public static PhotoPickerFragment newInstance(boolean z, boolean z2, boolean z3, int i, int i2, ArrayList<String> arrayList) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(EXTRA_CAMERA, z);
        bundle.putBoolean(EXTRA_GIF, z2);
        bundle.putBoolean(PhotoPicker.EXTRA_PREVIEW_ENABLED, z3);
        bundle.putInt("column", i);
        bundle.putInt(EXTRA_COUNT, i2);
        bundle.putStringArrayList("origin", arrayList);
        PhotoPickerFragment photoPickerFragment = new PhotoPickerFragment();
        photoPickerFragment.setArguments(bundle);
        return photoPickerFragment;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setRetainInstance(true);
        this.mGlideRequestManager = Glide.with(this);
        this.directories = new ArrayList();
        this.originalPhotos = getArguments().getStringArrayList("origin");
        this.column = getArguments().getInt("column", 3);
        boolean z = getArguments().getBoolean(EXTRA_CAMERA, true);
        boolean z2 = getArguments().getBoolean(PhotoPicker.EXTRA_PREVIEW_ENABLED, true);
        this.photoGridAdapter = new PhotoGridAdapter(getActivity(), this.mGlideRequestManager, this.directories, this.originalPhotos, this.column);
        this.photoGridAdapter.setShowCamera(z);
        this.photoGridAdapter.setPreviewEnable(z2);
        this.listAdapter = new PopupDirectoryListAdapter(this.mGlideRequestManager, this.directories);
        Bundle bundle2 = new Bundle();
        bundle2.putBoolean(PhotoPicker.EXTRA_SHOW_GIF, getArguments().getBoolean(EXTRA_GIF));
        MediaStoreHelper.getPhotoDirs(getActivity(), bundle2, new MediaStoreHelper.PhotosResultCallback() {
            public final void onResultCallback(List list) {
                directories.clear();
                directories.addAll(list);
                photoGridAdapter.notifyDataSetChanged();
                listAdapter.notifyDataSetChanged();
                adjustHeight();
            }
        });
        this.captureManager = new ImageCaptureManager(getActivity());
    }


    public void onResume() {
        super.onResume();
    }

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(R.layout.__picker_fragment_photo_picker, viewGroup, false);



        RecyclerView recyclerView = inflate.findViewById(R.id.rv_photos);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(this.column, 1);
        staggeredGridLayoutManager.setGapStrategy(2);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        recyclerView.setAdapter(this.photoGridAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        LinearLayout linearLayout = inflate.findViewById(R.id.wrap_folder);
        final ImageView imageView = inflate.findViewById(R.id.directIcon);
        final TextView textView = inflate.findViewById(R.id.folder);
        this.listPopupWindow = new ListPopupWindow(getActivity());
        this.listPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            public void onDismiss() {
                imageView.setImageResource(R.drawable.arrow_up);
            }
        });
        this.listPopupWindow.setWidth(-1);
        this.listPopupWindow.setAnchorView(linearLayout);
        this.listPopupWindow.setAdapter(this.listAdapter);
        this.listPopupWindow.setModal(true);
        this.listPopupWindow.setDropDownGravity(80);
        this.listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
                PhotoPickerFragment.this.listPopupWindow.dismiss();
                textView.setText((PhotoPickerFragment.this.directories.get(i)).getName());
                PhotoPickerFragment.this.photoGridAdapter.setCurrentDirectoryIndex(i);
                PhotoPickerFragment.this.photoGridAdapter.notifyDataSetChanged();
            }
        });
        this.photoGridAdapter.setOnCameraClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (PermissionsUtils.checkCameraPermission(PhotoPickerFragment.this) && PermissionsUtils.checkWriteStoragePermission((Fragment) PhotoPickerFragment.this)) {
                    PhotoPickerFragment.this.openCamera();
                }
            }
        });
        linearLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (PhotoPickerFragment.this.listPopupWindow.isShowing()) {
                    PhotoPickerFragment.this.listPopupWindow.dismiss();
                    imageView.setImageResource(R.drawable.arrow_up);
                } else if (!PhotoPickerFragment.this.getActivity().isFinishing()) {
                    PhotoPickerFragment.this.adjustHeight();
                    imageView.setImageResource(R.drawable.arrow);
                    PhotoPickerFragment.this.listPopupWindow.show();
                }
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrolled(RecyclerView recyclerView, int i, int i2) {
                super.onScrolled(recyclerView, i, i2);
                if (Math.abs(i2) > PhotoPickerFragment.this.SCROLL_THRESHOLD) {
                    PhotoPickerFragment.this.mGlideRequestManager.pauseRequests();
                } else {
                    PhotoPickerFragment.this.resumeRequestsIfNotDestroyed();
                }
            }

            public void onScrollStateChanged(RecyclerView recyclerView, int i) {
                if (i == 0) {
                    PhotoPickerFragment.this.resumeRequestsIfNotDestroyed();
                }
            }
        });
        return inflate;
    }


    public void openCamera() {
        try {
            startActivityForResult(this.captureManager.dispatchTakePictureIntent(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ActivityNotFoundException e2) {
            Log.e("PhotoPickerFragment", "No Activity Found to handle Intent", e2);
        }
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        if (i == 1 && i2 == -1) {
            if (this.captureManager == null) {
                this.captureManager = new ImageCaptureManager(getActivity());
            }
            this.captureManager.galleryAddPic();
            if (this.directories.size() > 0) {
                String currentPhotoPath = this.captureManager.getCurrentPhotoPath();
                PhotoDirectory photoDirectory = this.directories.get(0);
                photoDirectory.getPhotos().add(0, new Photo(currentPhotoPath.hashCode(), currentPhotoPath));
                photoDirectory.setCoverPath(currentPhotoPath);
                this.photoGridAdapter.notifyDataSetChanged();
            }
        }
    }

    public void onRequestPermissionsResult(int i, @NonNull String[] strArr, @NonNull int[] iArr) {
        if (iArr.length > 0 && iArr[0] == 0) {
            if ((i == 1 || i == 3) && PermissionsUtils.checkWriteStoragePermission((Fragment) this) && PermissionsUtils.checkCameraPermission((Fragment) this)) {
                openCamera();
            }
        }
    }

    public PhotoGridAdapter getPhotoGridAdapter() {
        return this.photoGridAdapter;
    }

    public void onSaveInstanceState(Bundle bundle) {
        this.captureManager.onSaveInstanceState(bundle);
        super.onSaveInstanceState(bundle);
    }

    public void onViewStateRestored(Bundle bundle) {
        this.captureManager.onRestoreInstanceState(bundle);
        super.onViewStateRestored(bundle);
    }

    public void adjustHeight() {
        if (this.listAdapter != null) {
            int count = this.listAdapter.getCount();
            if (count >= COUNT_MAX) {
                count = COUNT_MAX;
            }
            if (this.listPopupWindow != null) {
                this.listPopupWindow.setHeight(count * getResources().getDimensionPixelOffset(R.dimen.__picker_item_directory_height));
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.directories != null) {
            for (PhotoDirectory next : this.directories) {
                next.getPhotoPaths().clear();
                next.getPhotos().clear();
                next.setPhotos(null);
            }
            this.directories.clear();
            this.directories = null;
        }
    }


    public void resumeRequestsIfNotDestroyed() {
        if (AndroidLifecycleUtils.canLoadImage((Fragment) this)) {
            this.mGlideRequestManager.resumeRequests();
        }
    }
}
