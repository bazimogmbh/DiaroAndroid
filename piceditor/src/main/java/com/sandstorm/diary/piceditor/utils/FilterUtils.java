package com.sandstorm.diary.piceditor.utils;

import android.graphics.Bitmap;

import org.wysaid.common.SharedContext;
import org.wysaid.nativePort.CGEImageHandler;

import java.text.MessageFormat;

public class FilterUtils {

    public static Bitmap getBlurImageFromBitmap(Bitmap bitmap, float f) {
        SharedContext create = SharedContext.create();
        create.makeCurrent();
        CGEImageHandler cGEImageHandler = new CGEImageHandler();
        cGEImageHandler.initWithBitmap(bitmap);
        cGEImageHandler.setFilterWithConfig(MessageFormat.format("@blur lerp {0}", (f / 10.0f) + ""));
        cGEImageHandler.processFilters();
        Bitmap resultBitmap = cGEImageHandler.getResultBitmap();
        create.release();
        return resultBitmap;
    }

    public static Bitmap cloneBitmap(Bitmap bitmap) {
        SharedContext create = SharedContext.create();
        create.makeCurrent();
        CGEImageHandler cGEImageHandler = new CGEImageHandler();
        cGEImageHandler.initWithBitmap(bitmap);
        cGEImageHandler.setFilterWithConfig("");
        cGEImageHandler.processFilters();
        Bitmap resultBitmap = cGEImageHandler.getResultBitmap();
        create.release();
        return resultBitmap;
    }
}
