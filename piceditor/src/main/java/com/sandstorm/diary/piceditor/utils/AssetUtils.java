package com.sandstorm.diary.piceditor.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AssetUtils {

    public static List<String> lstEmoj() {
        List<String> arrayList = new ArrayList<>();
        arrayList.add("sticker/emoji/001.webp");
        arrayList.add("sticker/emoji/002.webp");
        arrayList.add("sticker/emoji/003.webp");
        arrayList.add("sticker/emoji/004.webp");
        arrayList.add("sticker/emoji/005.webp");
        arrayList.add("sticker/emoji/006.webp");
        arrayList.add("sticker/emoji/007.webp");
        arrayList.add("sticker/emoji/008.webp");
        arrayList.add("sticker/emoji/009.webp");
        arrayList.add("sticker/emoji/010.webp");
        arrayList.add("sticker/emoji/011.webp");
        arrayList.add("sticker/emoji/012.webp");
        arrayList.add("sticker/emoji/013.webp");
        arrayList.add("sticker/emoji/014.webp");
        arrayList.add("sticker/emoji/015.webp");
        arrayList.add("sticker/emoji/016.webp");
        arrayList.add("sticker/emoji/017.webp");
        arrayList.add("sticker/emoji/018.webp");
        arrayList.add("sticker/emoji/019.webp");
        arrayList.add("sticker/emoji/020.webp");
        arrayList.add("sticker/emoji/021.webp");
        arrayList.add("sticker/emoji/022.webp");
        arrayList.add("sticker/emoji/023.webp");
        arrayList.add("sticker/emoji/024.webp");
        arrayList.add("sticker/emoji/025.webp");
        arrayList.add("sticker/emoji/026.webp");
        arrayList.add("sticker/emoji/027.webp");
        arrayList.add("sticker/emoji/028.webp");
        arrayList.add("sticker/emoji/029.webp");
        arrayList.add("sticker/emoji/030.webp");
        arrayList.add("sticker/emoji/031.webp");
        arrayList.add("sticker/emoji/032.webp");
        arrayList.add("sticker/emoji/033.webp");
        arrayList.add("sticker/emoji/034.webp");
        arrayList.add("sticker/emoji/035.webp");
        arrayList.add("sticker/emoji/036.webp");
        arrayList.add("sticker/emoji/037.webp");
        arrayList.add("sticker/emoji/038.webp");
        arrayList.add("sticker/emoji/039.webp");
        arrayList.add("sticker/emoji/040.webp");
        arrayList.add("sticker/emoji/041.webp");
        arrayList.add("sticker/emoji/042.webp");
        arrayList.add("sticker/emoji/043.webp");
        arrayList.add("sticker/emoji/044.webp");
        arrayList.add("sticker/emoji/045.webp");
        arrayList.add("sticker/emoji/046.webp");
        arrayList.add("sticker/emoji/047.webp");
        arrayList.add("sticker/emoji/048.webp");
        arrayList.add("sticker/emoji/049.webp");
        arrayList.add("sticker/emoji/050.webp");
        arrayList.add("sticker/emoji/051.webp");
        arrayList.add("sticker/emoji/052.webp");
        arrayList.add("sticker/emoji/053.webp");
        arrayList.add("sticker/emoji/054.webp");
        arrayList.add("sticker/emoji/055.webp");
        arrayList.add("sticker/emoji/056.webp");
        arrayList.add("sticker/emoji/057.webp");
        arrayList.add("sticker/emoji/058.webp");
        arrayList.add("sticker/emoji/058.webp");
        arrayList.add("sticker/emoji/060.webp");
        arrayList.add("sticker/emoji/063.webp");
        arrayList.add("sticker/emoji/077.webp");
        arrayList.add("sticker/emoji/084.webp");
        arrayList.add("sticker/emoji/085.webp");
        arrayList.add("sticker/emoji/105.webp");
        arrayList.add("sticker/emoji/111.webp");
        arrayList.add("sticker/emoji/112.webp");
        arrayList.add("sticker/emoji/114.webp");
        arrayList.add("sticker/emoji/120.webp");
        arrayList.add("sticker/emoji/131.webp");
        arrayList.add("sticker/emoji/134.webp");
        arrayList.add("sticker/emoji/135.webp");
        arrayList.add("sticker/emoji/136.webp");
        arrayList.add("sticker/emoji/157.webp");
        arrayList.add("sticker/emoji/168.webp");
        arrayList.add("sticker/emoji/169.webp");
        arrayList.add("sticker/emoji/170.webp");
        arrayList.add("sticker/emoji/171.webp");
        arrayList.add("sticker/emoji/172.webp");
        arrayList.add("sticker/emoji/174.webp");
        arrayList.add("sticker/emoji/175.webp");
        arrayList.add("sticker/emoji/177.webp");
        arrayList.add("sticker/emoji/179.webp");
        arrayList.add("sticker/emoji/182.webp");
        arrayList.add("sticker/emoji/183.webp");
        arrayList.add("sticker/emoji/184.webp");
        arrayList.add("sticker/emoji/188.webp");
        arrayList.add("sticker/emoji/190.webp");
        return arrayList;
    }

    public static List<String> lstOthers() {
        List<String> arrayList = new ArrayList<>();
        arrayList.add("sticker/other/01.webp");
        arrayList.add("sticker/other/02.webp");
        arrayList.add("sticker/other/03.webp");
        arrayList.add("sticker/other/04.webp");
        arrayList.add("sticker/other/05.webp");
        arrayList.add("sticker/other/06.webp");
        arrayList.add("sticker/other/07.webp");
        arrayList.add("sticker/other/10.webp");
        arrayList.add("sticker/other/11.webp");
        arrayList.add("sticker/other/12.webp");
        arrayList.add("sticker/other/13.webp");
        arrayList.add("sticker/other/14.webp");
        arrayList.add("sticker/other/15.webp");
        arrayList.add("sticker/other/16.webp");
        arrayList.add("sticker/other/17.webp");
        arrayList.add("sticker/other/18.webp");
        arrayList.add("sticker/other/19.webp");
        arrayList.add("sticker/other/20.webp");
        arrayList.add("sticker/other/21.webp");
        arrayList.add("sticker/other/22.webp");
        arrayList.add("sticker/other/23.webp");
        arrayList.add("sticker/other/24.webp");
        arrayList.add("sticker/other/26.webp");
        arrayList.add("sticker/other/27.webp");
        arrayList.add("sticker/other/28.webp");
        arrayList.add("sticker/other/30.webp");
        arrayList.add("sticker/other/31.webp");
        arrayList.add("sticker/other/32.webp");
        arrayList.add("sticker/other/33.webp");
        arrayList.add("sticker/other/34.webp");
        arrayList.add("sticker/other/35.webp");
        arrayList.add("sticker/other/36.webp");
        arrayList.add("sticker/other/37.webp");
        arrayList.add("sticker/other/38.webp");
        arrayList.add("sticker/other/39.webp");
        arrayList.add("sticker/other/40.webp");
        arrayList.add("sticker/other/41.webp");
        arrayList.add("sticker/other/42.webp");
        arrayList.add("sticker/other/43.webp");
        arrayList.add("sticker/other/44.webp");
        arrayList.add("sticker/other/45.webp");
        arrayList.add("sticker/other/46.webp");
        arrayList.add("sticker/other/47.webp");
        arrayList.add("sticker/other/48.webp");
        arrayList.add("sticker/other/49.webp");
        arrayList.add("sticker/other/50.webp");
        arrayList.add("sticker/other/51.webp");
        arrayList.add("sticker/other/52.webp");
        return arrayList;
    }


    public static Bitmap loadBitmapFromAssets(Context context, String str) {
        InputStream inputStream;
        try {
            inputStream = context.getAssets().open(str);
            Bitmap decodeStream = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            return decodeStream;
        } catch (Exception e) {
            return null;
        }
    }


}
