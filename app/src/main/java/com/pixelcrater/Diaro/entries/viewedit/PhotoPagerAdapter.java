package com.pixelcrater.Diaro.entries.viewedit;

import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.model.AttachmentInfo;
import com.pixelcrater.Diaro.utils.AppLog;

import java.io.File;
import java.util.ArrayList;

import androidx.viewpager.widget.PagerAdapter;

public class PhotoPagerAdapter extends PagerAdapter {
    private PhotoPagerActivity mPhotoPagerActivity;
    private ArrayList<AttachmentInfo> entryPhotosArrayList = new ArrayList<>();
    private int widthPixels;
    private int heightPixels;

    public PhotoPagerAdapter(PhotoPagerActivity photoPagerActivity) {
        mPhotoPagerActivity = photoPagerActivity;

        calculatePhotoSize();
    }

    private void calculatePhotoSize() {
        DisplayMetrics dm = mPhotoPagerActivity.getResources().getDisplayMetrics();
        widthPixels = dm.widthPixels;
        heightPixels = dm.heightPixels;
//        AppLog.d("widthPixels: " + widthPixels + ", heightPixels: " + heightPixels);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        super.destroyItem(itemContainer, position, object);
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
//		AppLog.d("entryPhotosArrayList.size(): " + entryPhotosArrayList.size());

        return entryPhotosArrayList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup parent, int position) {
        final AttachmentInfo photo = entryPhotosArrayList.get(position);

        View frameLayout = LayoutInflater.from(mPhotoPagerActivity).inflate(R.layout.entry_photo_pager_item, parent, false);
        // Add to ViewPager
        parent.addView(frameLayout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        final PhotoView touchImageView = (PhotoView) frameLayout.findViewById(R.id.photo_pager_image);

        File photoFile = new File(photo.getFilePath());
        if (photoFile.exists() && photoFile.length() > 0) {
            AppLog.d("photoFile.getName(): " + photoFile.getName() + ", photoFile.length(): " + photoFile.length() + ", photoFile.lastModified(): " + photoFile.lastModified());

          /**  final Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getPath());
            touchImageView.setImageBitmap(bitmap, (Matrix)null,  0.1f,  10.0f ); **/

            // Show photo
            Glide.with(mPhotoPagerActivity)
                    .load(photoFile)
                    .signature(Static.getGlideSignature(photoFile))
                    .override(widthPixels, heightPixels)
                    .fitCenter()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .error(R.drawable.ic_photo_red_36dp)
                    .into(touchImageView);
        }
        else {
            AppLog.d("Photo does not exist: " + photo.filename);

            touchImageView.setImageResource(R.drawable.ic_photo_grey600_36dp);
            touchImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            touchImageView.setEnabled(false);

            AppLog.d("");
        }

        return frameLayout;
    }

    public void setEntryPhotosArrayList(ArrayList<AttachmentInfo> entryPhotosArrayList) {
        this.entryPhotosArrayList = entryPhotosArrayList;
        notifyDataSetChanged();
    }
}
