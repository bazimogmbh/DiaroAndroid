package com.pixelcrater.Diaro.gallery;

import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.pixelcrater.Diaro.R;

/**
 * Created by mohamedzakaria on 8/7/16.
 */
public class HorizontalImageViewHolder extends RecyclerView.ViewHolder {
    public ImageView image;

    public HorizontalImageViewHolder(View itemView) {
        super(itemView);
        image = (ImageView) itemView.findViewById(R.id.iv);
    }
}
