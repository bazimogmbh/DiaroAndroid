package com.sandstorm.diary.piceditor.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharePreferenceUtil {

    private static final String PREF_KEY = "editor_pref_key";

    public static void setHeightOfKeyboard(Context context, int i) {
        SharedPreferences.Editor edit = context.getSharedPreferences(PREF_KEY, 0).edit();
        edit.putInt("height_of_keyboard", i);
        edit.apply();
    }

    public static int getHeightOfKeyboard(Context context) {
        return context.getSharedPreferences(PREF_KEY, 0).getInt("height_of_keyboard", -1);
    }

    public static void setHeightOfNotch(Context context, int i) {
        SharedPreferences.Editor edit = context.getSharedPreferences(PREF_KEY, 0).edit();
        edit.putInt("height_of_notch", i);
        edit.apply();
    }

    public static int getHeightOfNotch(Context context) {
        return context.getSharedPreferences(PREF_KEY, 0).getInt("height_of_notch", -1);
    }


    public static boolean isPurchased(Context context) {
        return context.getSharedPreferences(PREF_KEY, 0).getBoolean("is_purchased", false);
    }



}
