package com.pixelcrater.Diaro.gallery.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.gallery.GalleryItem;
import com.pixelcrater.Diaro.gallery.ImageViewHolder;
import com.pixelcrater.Diaro.gallery.adapters.listeners.GridClickListener;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class GridImagesAdapter extends RecyclerView.Adapter<ImageViewHolder> {

    private ArrayList<GalleryItem> mGalleryItems;
    private Context mActivity;
    private int imgPlaceHolderResId = -1;
    private GridClickListener clickListener;
    SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat displayDateFormat;
    String mediaDirPath = "";

    public GridImagesAdapter(Context activity, final ArrayList<GalleryItem> galleryItems, int imgPlaceHolderResId) {
        this.mGalleryItems = galleryItems;
        this.mActivity = activity;
        this.imgPlaceHolderResId = imgPlaceHolderResId;
        this.clickListener = (GridClickListener) activity;
        displayDateFormat = new SimpleDateFormat("dd MMM yyyy");
        mediaDirPath = AppLifetimeStorageUtils.getMediaPhotosDirPath();
    }

    public GridImagesAdapter(Context activity, GridClickListener listener, final ArrayList<GalleryItem> galleryItems, int imgPlaceHolderResId) {
        this.mGalleryItems = galleryItems;
        this.mActivity = activity;
        this.imgPlaceHolderResId = imgPlaceHolderResId;
        this.clickListener = listener;
        displayDateFormat = new SimpleDateFormat("dd MMM yyyy");
        mediaDirPath = AppLifetimeStorageUtils.getMediaPhotosDirPath();
    }


    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.z_item_image, null));
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, final int position) {
        if (position >= mGalleryItems.size())
            return;

        GalleryItem galleryItem = mGalleryItems.get(position);

        String filePath = mediaDirPath + "/" + galleryItem.getFilename();

        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(mActivity);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(30f);
        circularProgressDrawable.start();

        RequestOptions requestOptions = new RequestOptions().placeholder(circularProgressDrawable).transform(new CenterCrop(), new RoundedCorners(26));
        Glide.with(mActivity).load(filePath)
                .apply(requestOptions)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.image);

        holder.itemView.setOnClickListener(view -> clickListener.onClick(position));

        try {
            String reformattedStr =  DateFormat.format("dd MMM yyyy", sqlDateFormat.parse(galleryItem.getEntryDate())).toString();
           // String reformattedStr = displayDateFormat.format(Objects.requireNonNull());
            holder.date.setText(reformattedStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return mGalleryItems != null ? mGalleryItems.size() : 0;
    }
}