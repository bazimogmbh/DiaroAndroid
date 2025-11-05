package com.pixelcrater.Diaro.utils.glide;

import android.content.Context;
import android.os.Build;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.module.GlideModule;
import com.bumptech.glide.request.RequestOptions;

public class GlideConfiguration implements GlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            builder.setDefaultRequestOptions(new RequestOptions().format(DecodeFormat.PREFER_RGB_565));
        } else
            builder.setDefaultRequestOptions(new RequestOptions().format(DecodeFormat.PREFER_ARGB_8888));


    }

    @Override
    public void registerComponents(Context context, Glide glide, Registry reg) {
    }
}