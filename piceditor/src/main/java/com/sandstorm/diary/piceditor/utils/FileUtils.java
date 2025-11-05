package com.sandstorm.diary.piceditor.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

import androidx.annotation.NonNull;

import com.sandstorm.diary.piceditor.features.collage.CollageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {

    public static File saveImage(Bitmap bitmap, @NonNull String path, boolean compress) throws IOException {
        //  Log.e("fileUtils", "path should be->" + path);

        int compressRate = 100;
        if(compress)
            compressRate = 90;

        File file = new File(path);
        FileOutputStream fOut = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressRate, fOut);
        fOut.flush();
        fOut.close();
        return file;
    }

    public static Bitmap createBitmap(CollageView collageView, int i) {
        collageView.clearHandling();
        collageView.invalidate();
        Bitmap createBitmap = Bitmap.createBitmap(i, (int) (((float) i) / (((float) collageView.getWidth()) / ((float) collageView.getHeight()))), Bitmap.Config.ARGB_8888);
        collageView.draw(new Canvas(createBitmap));
        return createBitmap;
    }

    public static Bitmap createBitmap(CollageView collageView) {
        collageView.clearHandling();
        collageView.invalidate();
        Bitmap createBitmap = Bitmap.createBitmap(collageView.getWidth(), collageView.getHeight(), Bitmap.Config.ARGB_8888);
        collageView.draw(new Canvas(createBitmap));
        return createBitmap;
    }


}
