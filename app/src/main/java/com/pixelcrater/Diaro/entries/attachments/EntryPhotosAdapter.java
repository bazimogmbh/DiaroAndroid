package com.pixelcrater.Diaro.entries.attachments;

import android.content.Context;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.model.AttachmentInfo;
import com.pixelcrater.Diaro.model.EntryInfo;
import com.pixelcrater.Diaro.utils.AppLog;

import java.io.File;
import java.util.ArrayList;

public class EntryPhotosAdapter extends PagerAdapter {

    ClickInterface clickInterface;
    Context mContext;
    LayoutInflater mLayoutInflater;
    public ArrayList<AttachmentInfo> attachmentList;
    public boolean paramsSet = false;
    private EntryInfo entryInfo;

    public EntryPhotosAdapter(Context context, ArrayList<AttachmentInfo> list, ClickInterface clickInterface, EntryInfo entryInfo) {
        this.entryInfo = entryInfo;
        mContext = context;
        attachmentList = list;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.clickInterface = clickInterface;
    }

    public void setEntryInfo(EntryInfo entryInfo) {
        this.entryInfo = entryInfo;
    }

    public interface ClickInterface {
        void onPhotoItemClick(int position);
    }

    @Override
    public int getItemPosition(Object object) {
        int index = attachmentList.indexOf(object);
        if (index == -1)
            return POSITION_NONE;
        else
            return index;
    }

    private void setContainerParams(ViewGroup container, int height) {
        if (!paramsSet) {
            AppLog.d("setContainerParams: " + height);
            ViewGroup.LayoutParams params = container.getLayoutParams();
            params.height = height;
            container.setLayoutParams(params);
            paramsSet = true;
        }
    }

    @Override
    public int getCount() {
        return attachmentList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        View itemView = mLayoutInflater.inflate(R.layout.entry_photo_list_item, container, false);
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        ImageView imageView = (ImageView) itemView.findViewById(R.id.photo);
        ImageView isPrimaryImageView = (ImageView) itemView.findViewById(R.id.is_primary);
        final ProgressBar progress = (ProgressBar) itemView.findViewById(R.id.progress_bar);

        // Set primary icon
        if (!entryInfo.firstPhotoFilename.equals("") && entryInfo.getFirstPhotoPath().equals(attachmentList.get(position).getFilePath())) {
            isPrimaryImageView.setVisibility(View.VISIBLE);
        } else {
            isPrimaryImageView.setVisibility(View.GONE);
        }

        int width = metrics.widthPixels;
        int height = (int) ((double)width * Static.PHOTO_PROPORTION);

        if (Static.isLandscape()) {
            height = metrics.heightPixels / 2;
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        AppLog.d("instantiateItemHeight: " + height);

        setContainerParams(container, height);

        AppLog.d("file: " + attachmentList.get(position));
        File photoFile = new File(attachmentList.get(position).getFilePath());

        if (photoFile.exists() && photoFile.length() > 0) {

            Glide.with(mContext)
                    .load(Uri.fromFile(photoFile))
                    .signature(Static.getGlideSignature(photoFile))
                    .override(width, height)
                    .centerCrop()
                    .error(R.drawable.ic_photo_red_24dp)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);

        }  else {
            Glide.with(mContext).load(R.drawable.ic_photo_grey600_24dp).into(imageView);
        }

        container.addView(itemView);

        itemView.setOnClickListener(v -> clickInterface.onPhotoItemClick(position));

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((FrameLayout) object);
    }
}
