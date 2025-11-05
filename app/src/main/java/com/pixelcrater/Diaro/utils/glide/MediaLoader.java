package com.pixelcrater.Diaro.utils.glide;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.pixelcrater.Diaro.R;
import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.AlbumLoader;

public class MediaLoader implements AlbumLoader {

    @Override
    public void load(ImageView imageView, AlbumFile albumFile) {
        load(imageView, albumFile.getPath());
    }

    @Override
    public void load(ImageView imageView, String url) {
        Glide.with(imageView.getContext())
                .load(url)
                .error(R.drawable.ic_photo_red_24dp)
                .placeholder(R.drawable.placeholder)
                .centerInside()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);
    }
}