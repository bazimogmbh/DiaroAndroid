package com.pixelcrater.Diaro.entries.viewedit;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

import java.io.File;
import java.util.ArrayList;

public class MultiplePhotoPickerAdapter extends RecyclerView.Adapter<MultiplePhotoPickerAdapter.MainViewHolder> {
    public final MultiplePhotoPickerActivity mMultiplePhotoPickerActivity;
    private final int mThumbWidth;
    private final int mThumbHeight;
    private final int overlayUiColor;
    private ArrayList<String> selectedPhotosPathsArrayList = new ArrayList<>();
    private ArrayList<String> photosPathsArrayList = new ArrayList<>();

    public MultiplePhotoPickerAdapter(MultiplePhotoPickerActivity multiplePhotoPickerActivity, int thumbWidth, int thumbHeight) {
        AppLog.d("thumbWidth: " + thumbWidth + ", thumbHeight: " + thumbHeight);

        mMultiplePhotoPickerActivity = multiplePhotoPickerActivity;
        mThumbWidth = thumbWidth;
        mThumbHeight = thumbHeight;

        overlayUiColor = MyThemesUtils.getOverlayPrimaryColor();
    }

    public void setPhotosPathsArrayList(ArrayList<String> photosPathsArrayList) {
        this.photosPathsArrayList = photosPathsArrayList;
    }

    public ArrayList<String> getSelectedPhotosPathsArrayList() {
        return selectedPhotosPathsArrayList;
    }

    public void setSelectedPhotosPathsArrayList(ArrayList<String> selectedPhotosPathsArrayList) {
        this.selectedPhotosPathsArrayList = selectedPhotosPathsArrayList;
    }

    @Override
    public int getItemCount() {
        return photosPathsArrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        AppLog.d("viewType: " + viewType);

        View view = LayoutInflater.from(mMultiplePhotoPickerActivity).inflate(R.layout.entry_photo_grid_item, parent, false);
        view.getLayoutParams().width = mThumbWidth;
        view.getLayoutParams().height = mThumbHeight;

        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MainViewHolder holder, final int position) {
        AppLog.d("position: " + position);

        File photoFile = new File(photosPathsArrayList.get(position));

        holder.isPrimaryImageView.setVisibility(View.GONE);

        // CheckBox and overlay
        holder.overlayCheckedView.setVisibility(View.GONE);
        holder.checkBox.setVisibility(View.VISIBLE);

        if (selectedPhotosPathsArrayList.contains(photosPathsArrayList.get(position))) {
            holder.overlayView.setVisibility(View.VISIBLE);
            holder.overlayView.setBackgroundColor(overlayUiColor);
            holder.checkBox.setChecked(true);
        } else {
            holder.overlayView.setVisibility(View.GONE);
            holder.checkBox.setChecked(false);
        }

        if (photoFile.exists() && photoFile.length() > 0) {
            // Show photo
            Glide.with(mMultiplePhotoPickerActivity)
                    .load(photoFile)
                    .signature(Static.getGlideSignature(photoFile))
//                    .override(mThumbWidth, mThumbHeight)
                    .centerCrop()
                    .error(R.drawable.ic_photo_red_24dp)
                    .into(holder.imageView);
        } else {
            Glide.with(mMultiplePhotoPickerActivity).load(R.drawable.ic_photo_grey600_24dp).into(holder.imageView);
        }

        // Click area with ripple effect
        holder.clickAreaView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.checkBox.isChecked()) {
                    selectedPhotosPathsArrayList.remove(photosPathsArrayList.get(position));
                } else if (!selectedPhotosPathsArrayList.contains(
                        photosPathsArrayList.get(position))) {
                    selectedPhotosPathsArrayList.add(photosPathsArrayList.get(position));
                }

                notifyDataSetChanged();

                // Update selected count
                mMultiplePhotoPickerActivity.updateSelectedPhotosNumber();
            }
        });
    }

    class MainViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;
        final CheckBox checkBox;
        final ImageView isPrimaryImageView;
        final ViewGroup overlayView;
        final View overlayCheckedView;
        final View clickAreaView;

        public MainViewHolder(View v) {
            super(v);

            imageView = (ImageView) v.findViewById(R.id.image);
            checkBox = (CheckBox) v.findViewById(R.id.item_checkbox);
            isPrimaryImageView = (ImageView) v.findViewById(R.id.is_primary);
            overlayView = (ViewGroup) v.findViewById(R.id.item_overlay);
            overlayCheckedView = overlayView.getChildAt(0);
            clickAreaView = v.findViewById(R.id.click_area);
        }
    }
}
