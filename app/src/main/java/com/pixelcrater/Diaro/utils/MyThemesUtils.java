package com.pixelcrater.Diaro.utils;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.widget.ImageView;

import androidx.annotation.AttrRes;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.config.Prefs;

import java.util.ArrayList;

public class MyThemesUtils {

    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String THEME_BLACK = "black";

    private static final String DEFAULT_UI_ACCENT_COLOR_CODE = "#41BEFF";
    private static final String DEFAULT_UI_ACCENT_NAME = "light_blue_500";

    private static ArrayList<KeyValuePair> uiAccentColorsArrayList;

    public static String getHexColor(int colorIntVal) {
        return String.format("#%06X", 0xFFFFFF & colorIntVal);
    }

    public static String getHexColorFromResId(int colorResId) {
        return String.format("#%06X", 0xFFFFFF & MyApp.getInstance().getResources().getColor(colorResId));
    }

    public static int getDarkColor(String colorStr) {
        float[] hsv = new float[3];
        int color = Color.parseColor(colorStr);
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f; // value component
        color = Color.HSVToColor(hsv);

        return color;
    }

    public static String getPrimaryColorCode() {
        // Get UI primary color from preferences
        return MyApp.getInstance().prefs.getString(Prefs.PREF_UI_COLOR, getHexColorFromResId(R.color.diaro_default));

    }

    public static void setPrimaryColorCode(String uiColorCode) {
        // Set UI primary color from preferences
        MyApp.getInstance().prefs.edit().putString(Prefs.PREF_UI_COLOR, uiColorCode).apply();

    }

    public static int getThemeAwarePrimartyTextColor() {

        int retVal = MyThemesUtils.getPrimaryColor();

        // black ui color & amoled
        if (MyThemesUtils.getTheme().compareTo(THEME_BLACK) == 0 || MyThemesUtils.getTheme().compareTo(THEME_DARK) == 0) {
            if (MyThemesUtils.getPrimaryColorCode().compareTo(MyThemesUtils.getHexColorFromResId(R.color.md_black_1000)) == 0 || MyThemesUtils.getPrimaryColorCode().compareTo(MyThemesUtils.getHexColorFromResId(R.color.md_grey_900)) == 0) {
                retVal = MyThemesUtils.getTextColor();
            }

        }

        return retVal;
    }

    public static int getPrimaryColor() {
        return Color.parseColor(getPrimaryColorCode());
    }

    public static int getOverlayPrimaryColor() {
        int primaryColor = getPrimaryColor();
        return Color.argb(70, Color.red(primaryColor), Color.green(primaryColor), Color.blue(primaryColor));
    }

    public static int getPrimaryColorTransparent() {
        int primaryColor = getPrimaryColor();
        return Color.argb(120, Color.red(primaryColor), Color.green(primaryColor), Color.blue(primaryColor));
    }

    public static int getOverlayBackgroundColor() {
        int bgColor = MyApp.getInstance().getResources().getColor(getBackgroundColorResId());
        return Color.argb(200, Color.red(bgColor), Color.green(bgColor), Color.blue(bgColor));
    }

    public static String getAccentColorCode() {
        // Get UI accent color from preferences
        return MyApp.getInstance().prefs.getString(Prefs.PREF_UI_ACCENT_COLOR, DEFAULT_UI_ACCENT_COLOR_CODE);

    }

    public static int getAccentColor() {
        return Color.parseColor(getAccentColorCode());
    }

    public static int getTextColor() {
        return MyApp.getInstance().getResources().getColor(getTextColorResId());
    }


    public static int getThemeAwareBWColor() {
        switch (getTheme()) {
            case THEME_DARK:
                return  MyApp.getInstance().getResources().getColor(android.R.color.white);
            case THEME_BLACK:
                return   MyApp.getInstance().getResources().getColor(android.R.color.white);
            // THEME_LIGHT
            default:
                return    MyApp.getInstance().getResources().getColor(android.R.color.black);
        }

    }

    public static int getTextColorDark() {
        return MyApp.getInstance().getResources().getColor(getTextColorResIdDark());
    }

    public static String getSeparatorColor() {
        switch (getTheme()) {
            case THEME_DARK:
                return "#4f4f4f";
            case THEME_BLACK:
                return "#262626";
            // THEME_LIGHT
            default:
                return "#d6d6d6";
        }
    }

    private static int getTextColorResId() {
        switch (getTheme()) {
            case THEME_DARK:
                return R.color.grey_500;
            case THEME_BLACK:
                return android.R.color.white;
            // THEME_LIGHT
            default:
                return R.color.grey_600;
        }
    }

    private static int getTextColorResIdDark() {
        switch (getTheme()) {
            case THEME_DARK:
                return R.color.grey_500;
            case THEME_BLACK:
                return android.R.color.white;
            // THEME_LIGHT
            default:
                return R.color.md_grey_900;
        }
    }

    public static int getListItemTextColor() {
        return MyApp.getInstance().getResources().getColor(getListItemTextColorResId());
    }

    private static int getListItemTextColorResId() {
        switch (getTheme()) {
            case THEME_DARK:
                return R.color.grey_500;
            case THEME_BLACK:
                return R.color.grey_300;
            // THEME_LIGHT
            default:
                return R.color.grey_600;
        }
    }

    public static int getSelectedListItemTextColor() {
        return MyApp.getInstance().getResources().getColor(getSelectedListItemTextColorResId());
    }

    private static int getSelectedListItemTextColorResId() {
        switch (getTheme()) {
            case THEME_DARK:
                return R.color.md_grey_300;
            case THEME_BLACK:
                return android.R.color.white;
            // THEME_LIGHT
            default:
                return R.color.md_grey_900;
        }
    }

    public static int getBackgroundColorResId() {
//        AppLog.d("getThemeFromPrefs(): " + getThemeFromPrefs());

        switch (getTheme()) {
            case THEME_DARK:
                return R.color.dark_material_background_color;
            case THEME_BLACK:
                return android.R.color.black;
            // THEME_LIGHT
            default:
                return android.R.color.white;
        }
    }

    public static int getBackgroundColorResIdMainView() {
//        AppLog.d("getThemeFromPrefs(): " + getThemeFromPrefs());

        switch (getTheme()) {
            case THEME_DARK:
                return R.color.dark_material_background_color;
            case THEME_BLACK:
                return android.R.color.black;
            // THEME_LIGHT
            default:
                return android.R.color.white;
        }
    }

    public static int getBackgroundHeaderColorResIdMainView() {
//        AppLog.d("getThemeFromPrefs(): " + getThemeFromPrefs());

        switch (getTheme()) {
            case THEME_DARK:
                return R.color.dark_material_background_color;
            case THEME_BLACK:
                return android.R.color.black;
            // THEME_LIGHT
            default:
                return R.color.light_material_background_color;
        }
    }

    public static int getDrawableResId(String drawableName) {
        return MyApp.getInstance().getResources().getIdentifier(getDrawableName(drawableName), "drawable", MyApp.getInstance().getPackageName());
    }

    public static void setTint(ImageView view) {
        switch (getTheme()) {
            case THEME_LIGHT:
                break;
            // THEME_DARK, THEME_BLACK
            default:
                view.setColorFilter(Color.argb(255, 255, 255, 255));
        }
    }


    private static String getDrawableName(String drawableName) {
        switch (getTheme()) {
            case THEME_LIGHT:
                return String.format(drawableName, "grey600");
            // THEME_DARK, THEME_BLACK
            default:
                return String.format(drawableName, "white");
        }
    }

    public static int getEntryListItemColorResId() {
        switch (getTheme()) {
            case THEME_LIGHT:
                return  android.R.color.white;
            case THEME_DARK:
                return R.color.dark_material_dialog_color;
            // THEME_BLACK
            default:
                return android.R.color.black;
        }
    }

    public static ArrayList<KeyValuePair> getUiAccentColorsArrayList() {
        if (uiAccentColorsArrayList == null) {
            uiAccentColorsArrayList = new ArrayList<>();
            uiAccentColorsArrayList.add(new KeyValuePair(getHexColorFromResId(R.color.red_500), "red_500"));
            uiAccentColorsArrayList.add(new KeyValuePair(getHexColorFromResId(R.color.pink_500), "pink_500"));
            uiAccentColorsArrayList.add(new KeyValuePair(getHexColorFromResId(R.color.purple_500), "purple_500"));
            uiAccentColorsArrayList.add(new KeyValuePair(getHexColorFromResId(R.color.deep_purple_500), "deep_purple_500"));
            uiAccentColorsArrayList.add(new KeyValuePair(getHexColorFromResId(R.color.indigo_500), "indigo_500"));
            uiAccentColorsArrayList.add(new KeyValuePair(getHexColorFromResId(R.color.blue_500), "blue_500"));
            uiAccentColorsArrayList.add(new KeyValuePair(getHexColorFromResId(R.color.light_blue_500), "light_blue_500"));
            uiAccentColorsArrayList.add(new KeyValuePair(getHexColorFromResId(R.color.cyan_500), "cyan_500"));
            uiAccentColorsArrayList.add(new KeyValuePair(getHexColorFromResId(R.color.teal_500), "teal_500"));
            uiAccentColorsArrayList.add(new KeyValuePair(getHexColorFromResId(R.color.light_green_500), "light_green_500"));
            uiAccentColorsArrayList.add(new KeyValuePair(getHexColorFromResId(R.color.lime_500), "lime_500"));
            uiAccentColorsArrayList.add(new KeyValuePair(getHexColorFromResId(R.color.yellow_500), "yellow_500"));
            uiAccentColorsArrayList.add(new KeyValuePair(getHexColorFromResId(R.color.amber_500), "amber_500"));
            uiAccentColorsArrayList.add(new KeyValuePair(getHexColorFromResId(R.color.orange_500), "orange_500"));
            uiAccentColorsArrayList.add(new KeyValuePair(getHexColorFromResId(R.color.brown_500), "brown_500"));
            uiAccentColorsArrayList.add(new KeyValuePair(getHexColorFromResId(R.color.blue_grey_500), "blue_grey_500"));
            uiAccentColorsArrayList.add(new KeyValuePair(getHexColorFromResId(R.color.grey_300), "grey_300"));
        }
        return uiAccentColorsArrayList;
    }

    private static String getUiAccentColorName() {
        if (KeyValuePair.getValue(getUiAccentColorsArrayList(), getAccentColorCode()) != null) {
            return KeyValuePair.getValue(getUiAccentColorsArrayList(), getAccentColorCode());
        }
        return DEFAULT_UI_ACCENT_NAME;
    }

    public static String getTheme() {
        // Get UI color from preferences
        return MyApp.getInstance().prefs.getString(Prefs.PREF_UI_THEME, THEME_LIGHT);
    }

    private static String getThemeName() {
        switch (getTheme()) {
            case THEME_DARK:
                return "AppTheme_Dark";
            case THEME_BLACK:
                return "AppTheme_Dark";
            // THEME_LIGHT
            default:
                return "AppTheme_Light";
        }
    }

    public static int getStyleResId() {
        return MyApp.getInstance().getResources().getIdentifier(getThemeName() + "_" + getUiAccentColorName(), "style", MyApp.getInstance().getPackageName());
    }

    public static int getGoogleMapsStyle() {
        switch (getTheme()) {
            case THEME_DARK:
                return R.raw.style_night_mode;
            case THEME_BLACK:
                return R.raw.style_dark_mode;
            // THEME_LIGHT
            default:
                return R.raw.style_light_mode;
        }

    }

    public static int resolveOrThrow(Context context, @AttrRes int attributeResId) {
        TypedValue typedValue = new TypedValue();
        if (context.getTheme().resolveAttribute(attributeResId, typedValue, true)) {
            return typedValue.data;
        }
        throw new IllegalArgumentException(context.getResources().getResourceName(attributeResId));
    }


}
