package com.pixelcrater.Diaro.entries.viewedit;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Point;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.makeramen.dragsortadapter.DragSortAdapter;
import com.makeramen.dragsortadapter.NoForegroundShadowBuilder;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.model.AttachmentInfo;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;

public class PhotoGridDraggableAdapter extends DragSortAdapter<PhotoGridDraggableAdapter.MainViewHolder> {

    private final PhotoGridActivity mPhotoGridActivity;
    private final int overlayUiColor;
    private final int overlayColor;
    private final int mThumbWidth;
    private final int mThumbHeight;
    private final String mEntryUid;
    private String primaryPhotoUid = "";
    private ArrayList<String> selectedPhotosPathsArrayList = new ArrayList<>();
    private ArrayList<AttachmentInfo> entryPhotosArrayList = new ArrayList<>();

    public PhotoGridDraggableAdapter(PhotoGridActivity photoGridActivity, int thumbWidth, int thumbHeight, String entryUid, RecyclerView recyclerView) {
        super(recyclerView);
//        AppLog.d("thumbWidth: " + thumbWidth + ", thumbHeight: " + thumbHeight);

        mPhotoGridActivity = photoGridActivity;
        mThumbWidth = thumbWidth;
        mThumbHeight = thumbHeight;
        mEntryUid = entryUid;

        overlayUiColor = MyThemesUtils.getOverlayPrimaryColor();
        overlayColor = photoGridActivity.getResources().getColor(R.color.grid_photo_overlay);
    }

    public ArrayList<AttachmentInfo> getEntryPhotosArrayList() {
        return entryPhotosArrayList;
    }

    public void setEntryPhotosArrayList(ArrayList<AttachmentInfo> entryPhotosArrayList) {
        this.entryPhotosArrayList = entryPhotosArrayList;
    }

    public ArrayList<String> getSelectedPhotosPathsArrayList() {
        return selectedPhotosPathsArrayList;
    }

    public void setSelectedPhotosPathsArrayList(ArrayList<String> selectedPhotosPathsArrayList) {
        this.selectedPhotosPathsArrayList = selectedPhotosPathsArrayList;
    }

    public void setPrimaryPhotoUid(String primaryPhotoUid) {
        this.primaryPhotoUid = primaryPhotoUid;
    }

    @Override
    public long getItemId(int position) {
        return entryPhotosArrayList.get(position).uid.hashCode();
    }

    @Override
    public int getPositionForId(long id) {
        for (int i = 0; i < entryPhotosArrayList.size(); i++) {
            if (entryPhotosArrayList.get(i).uid.hashCode() == id) {
                return i;
            }
        }

        return 0;
    }

    @Override
    public boolean move(int fromPosition, int toPosition) {
//        AppLog.d("fromPosition: " + fromPosition + ", toPosition: " + toPosition);

        if (entryPhotosArrayList.size() > fromPosition && toPosition >= 0) {
            entryPhotosArrayList.add(toPosition, entryPhotosArrayList.remove(fromPosition));
        }
        return true;
    }

    @SuppressWarnings("unused")
    public void logArrayList() {
        for (int i = 0; i < entryPhotosArrayList.size(); i++) {
            final AttachmentInfo o = entryPhotosArrayList.get(i);

            AppLog.d("i + 1: " + (i + 1) + ", o.position: " + o.position +
                    ", o.filename: " + o.filename);
        }
    }

    @Override
    public void onDrop() {
        super.onDrop();

//        AppLog.d("- ON DROP -");
//        logArrayList();

        // Update attachments positions
        for (int i = 0; i < entryPhotosArrayList.size(); i++) {
            final AttachmentInfo o = entryPhotosArrayList.get(i);

            if (o.position != i + 1) {
//                AppLog.d("Position changed! i + 1: " + (i + 1) + ", o.position: " + o.position);

                // Update attachment row
                ContentValues cv = new ContentValues();
                cv.put(Tables.KEY_ATTACHMENT_POSITION, i + 1);
                MyApp.getInstance().storageMgr.updateRowByUid(Tables.TABLE_ATTACHMENTS, o.uid, cv);
            }
        }
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        AppLog.d("viewType: " + viewType);

        View view = LayoutInflater.from(mPhotoGridActivity).inflate(
                R.layout.entry_photo_grid_item, parent, false);
        view.getLayoutParams().width = mThumbWidth;
        view.getLayoutParams().height = mThumbHeight;

        MainViewHolder holder = new MainViewHolder(this, view);
        view.setOnClickListener(holder);
        view.setOnLongClickListener(holder);

        return holder;
    }

    @Override
    public void onBindViewHolder(final MainViewHolder holder, final int position) {
        long itemId = getItemId(position);
        // NOTE: check for getDraggingId() match to set an "invisible space" while dragging
        holder.container.setVisibility(getDraggingId() == itemId ? View.INVISIBLE : View.VISIBLE);
        holder.container.postInvalidate();

        final AttachmentInfo o = entryPhotosArrayList.get(position);

        // Photo
        File photoFile = new File(o.getFilePath());
//        AppLog.d("position: " + position + ", o.getFilePath(): " + o.getFilePath());
        if (photoFile.exists() && photoFile.length() > 0) {
            // Show photo
            Glide.with(mPhotoGridActivity)
                    .load(photoFile)
                    .signature(Static.getGlideSignature(photoFile))
//                    .override(mThumbWidth, mThumbHeight)
                    .centerCrop()
                    .error(R.drawable.ic_photo_red_24dp)
                    .into(holder.imageView);
        } else {
            holder.imageView.setImageResource(R.drawable.ic_photo_grey600_24dp);
        }

        // Primary photo icon
        if (StringUtils.equals(primaryPhotoUid, o.uid)) {
            holder.isPrimaryImageView.setVisibility(View.VISIBLE);
        } else {
            holder.isPrimaryImageView.setVisibility(View.GONE);
        }

        // CheckBox and overlay
        holder.checkBox.setVisibility(View.GONE);

        if (mPhotoGridActivity.isMultiSelectMode()) {
            holder.overlayView.setVisibility(View.VISIBLE);

            if (selectedPhotosPathsArrayList.contains(
                    entryPhotosArrayList.get(position).getFilePath())) {
                holder.overlayView.setBackgroundColor(overlayUiColor);
                holder.overlayCheckedView.setVisibility(View.VISIBLE);
                holder.checkBox.setChecked(true);
            } else {
                holder.overlayView.setBackgroundColor(overlayColor);
                holder.overlayCheckedView.setVisibility(View.GONE);
                holder.checkBox.setChecked(false);
            }
        } else {
            holder.overlayView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return entryPhotosArrayList.size();
    }

    class MainViewHolder extends DragSortAdapter.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        final View container;
        final ImageView imageView;
        final ProgressBar progressBar;
        final CheckBox checkBox;
        final ImageView isPrimaryImageView;
        final ViewGroup overlayView;
        final View overlayCheckedView;
        final View clickAreaView;

        public MainViewHolder(DragSortAdapter adapter, View v) {
            super(adapter, v);

            container = v;
            imageView = (ImageView) v.findViewById(R.id.image);
            progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
            checkBox = (CheckBox) v.findViewById(R.id.item_checkbox);
            isPrimaryImageView = (ImageView) v.findViewById(R.id.is_primary);
            overlayView = (ViewGroup) v.findViewById(R.id.item_overlay);
            overlayCheckedView = overlayView.getChildAt(0);
            clickAreaView = v.findViewById(R.id.click_area);
        }

        @Override
        public void onClick(@NonNull View v) {
            int position = getAdapterPosition();
//            AppLog.d(v + " clicked! getAdapterPosition(): " + getAdapterPosition());

            if (mPhotoGridActivity.isMultiSelectMode()) {
                if (entryPhotosArrayList.size() > position) {
                    AttachmentInfo attachmentInfo = entryPhotosArrayList.get(position);
                    if (attachmentInfo != null) {
                        selectUnselectPhoto(attachmentInfo.getFilePath());
                    }
                }
            } else {
                // Open photo in PhotoPagerActivity
                Intent intent = new Intent(mPhotoGridActivity, PhotoPagerActivity.class);
                intent.putExtra(Static.EXTRA_SKIP_SC, true);
                intent.putExtra("entryUid", mEntryUid);
                intent.putExtra("position", position);
                intent.putExtra("openedFromPhotoGrid", true);
                mPhotoGridActivity.startActivityForResult(intent, Static.REQUEST_PHOTO_PAGER);
            }
        }

        private void selectUnselectPhoto(String filePath) {
            if (checkBox.isChecked()) {
                selectedPhotosPathsArrayList.remove(filePath);
            } else if (!selectedPhotosPathsArrayList.contains(filePath)) {
                selectedPhotosPathsArrayList.add(filePath);
            }

            notifyDataSetChanged();

            // Redraw action bar to show selected items count
            mPhotoGridActivity.actionMode.invalidate();
        }

        @Override
        public boolean onLongClick(@NonNull View v) {
//            AppLog.d(v + " long clicked, starting drag!");
            if (!mPhotoGridActivity.isMultiSelectMode()) {
                try {
                    startDrag();
                } catch (Exception e) {
                    AppLog.e("Exception: " + e);
                }
            }
            return true;
        }

        @Override
        public View.DragShadowBuilder getShadowBuilder(View itemView, Point touchPoint) {
            return new NoForegroundShadowBuilder(itemView, touchPoint);
        }
    }
}
