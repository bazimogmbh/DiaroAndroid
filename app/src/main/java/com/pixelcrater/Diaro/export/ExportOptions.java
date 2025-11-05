package com.pixelcrater.Diaro.export;

import com.pixelcrater.Diaro.utils.MyThemesUtils;

public class ExportOptions {

    public static String LAYOUT_COMPACT = "";  // more than one entry per page
    public static String LAYOUT_NORMAL =  "one_entry_per_page"; // each entry on seperate page

    public static String PHOTO_HEIGHT_SMALL = "190px";
    public static String PHOTO_HEIGHT_MEDIUM = "300px";
    public static String PHOTO_HEIGHT_LARGE = "768px";

    String layout;
    String photo_height;
    String border_color = "#2196f3";

    public ExportOptions(String layout, String photo_height) {
        this.layout = layout;
        this.photo_height = photo_height;
        this.border_color = MyThemesUtils.getPrimaryColorCode();
    }
}
