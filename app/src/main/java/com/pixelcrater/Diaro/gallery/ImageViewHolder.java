package com.pixelcrater.Diaro.gallery;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.pixelcrater.Diaro.R;


/**
 * Created by mohamedzakaria on 8/7/16.
 */
public class ImageViewHolder extends RecyclerView.ViewHolder {

    public ImageView image;
    public TextView date;

    public ImageViewHolder(View itemView) {
        super(itemView);
        image = (ImageView) itemView.findViewById(R.id.imageView);
        date = itemView.findViewById(R.id.date);
    }
}
