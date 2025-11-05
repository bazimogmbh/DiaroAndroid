package com.pixelcrater.Diaro.appupgrades;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

import java.util.ArrayList;

public class AppUpgrade_58 {
    public AppUpgrade_58() {
        // Update ui color pref from color name to color code
        updateUiColorPref();
    }

    private void updateUiColorPref() {
        String uiColorCode = getDeprecatedUiColorCode();

        if (uiColorCode != null) {
            MyApp.getInstance().prefs.edit().putString(Prefs.PREF_UI_COLOR, uiColorCode).apply();
        }
    }

    private ArrayList<UiColor> getDeprecatedUiColorsArrayList() {
        ArrayList<UiColor> deprecatedUiColorsArrayList = new ArrayList<>();

        deprecatedUiColorsArrayList.add(new UiColor("Diaro Blue1", MyThemesUtils.getHexColorFromResId(R.color.diaro_default)));
        deprecatedUiColorsArrayList.add(new UiColor("Diaro Blue2", "#467f8e"));
        deprecatedUiColorsArrayList.add(new UiColor("Diaro Blue3", "#456067"));
        deprecatedUiColorsArrayList.add(new UiColor("Royal Blue1", "#306EFF"));
        deprecatedUiColorsArrayList.add(new UiColor("Medium Slate Blue", "#5E5A80"));
        deprecatedUiColorsArrayList.add(new UiColor("Steel Blue", "#4863A0"));
        deprecatedUiColorsArrayList.add(new UiColor("Deep Sky Blue2", "#38ACEC"));
        deprecatedUiColorsArrayList.add(new UiColor("Light Sky Blue4", "#566D7E"));
        deprecatedUiColorsArrayList.add(new UiColor("Turquoise", "#3A99AB"));

        deprecatedUiColorsArrayList.add(new UiColor("Purple", "#8E35EF"));
        deprecatedUiColorsArrayList.add(new UiColor("Medium Purple", "#8467D7"));
        deprecatedUiColorsArrayList.add(new UiColor("Medium Purple1", "#9E7BFF"));
        deprecatedUiColorsArrayList.add(new UiColor("Maroon1", "#F535AA"));
        deprecatedUiColorsArrayList.add(new UiColor("Hot Pink", "#F660AB"));
        deprecatedUiColorsArrayList.add(new UiColor("Deep Pink", "#F52887"));
        deprecatedUiColorsArrayList.add(new UiColor("Deep Pink4", "#7D053F"));
        deprecatedUiColorsArrayList.add(new UiColor("Plum4", "#7E587E"));
        deprecatedUiColorsArrayList.add(new UiColor("Medium Orchid", "#B048B5"));
        deprecatedUiColorsArrayList.add(new UiColor("Pale Violet Red1", "#F778A1"));

        deprecatedUiColorsArrayList.add(new UiColor("Sea Green", "#4E8975"));
        deprecatedUiColorsArrayList.add(new UiColor("Dark Sea Green4", "#617C58"));
        deprecatedUiColorsArrayList.add(new UiColor("Lime Green", "#41A317"));
        deprecatedUiColorsArrayList.add(new UiColor("Medium Sea Green", "#306754"));
        deprecatedUiColorsArrayList.add(new UiColor("Dark Green", "#254117"));
        deprecatedUiColorsArrayList.add(new UiColor("Sea Green4", "#387C44"));
        deprecatedUiColorsArrayList.add(new UiColor("Forest Green", "#4E9258"));
        deprecatedUiColorsArrayList.add(new UiColor("Green4", "#347C17"));
        deprecatedUiColorsArrayList.add(new UiColor("Dark Olive Green3", "#A0C544"));

        deprecatedUiColorsArrayList.add(new UiColor("Gold", "#D4A017"));
        deprecatedUiColorsArrayList.add(new UiColor("Gold1", "#FDD017"));
        deprecatedUiColorsArrayList.add(new UiColor("Light Coral", "#E77471"));
        deprecatedUiColorsArrayList.add(new UiColor("Indian Red1", "#F75D59"));
        deprecatedUiColorsArrayList.add(new UiColor("Firebrick3", "#C11B17"));
        deprecatedUiColorsArrayList.add(new UiColor("Light Pink3", "#C48189"));
        deprecatedUiColorsArrayList.add(new UiColor("Light Pink4", "#7F4E52"));
        deprecatedUiColorsArrayList.add(new UiColor("Rosy Brown", "#B38481"));
        deprecatedUiColorsArrayList.add(new UiColor("Lavender Blush4", "#817679"));
        deprecatedUiColorsArrayList.add(new UiColor("Light Salmon4", "#7F462C"));
        deprecatedUiColorsArrayList.add(new UiColor("Thistle4", "#806D7E"));

        deprecatedUiColorsArrayList.add(new UiColor("Khaki", "#ADA96E"));
        deprecatedUiColorsArrayList.add(new UiColor("Khaki3", "#C9BE62"));
        deprecatedUiColorsArrayList.add(new UiColor("Khaki4", "#827839"));
        deprecatedUiColorsArrayList.add(new UiColor("Gray", "#736F6E"));
        deprecatedUiColorsArrayList.add(new UiColor("Light Cyan4", "#717D7D"));
        deprecatedUiColorsArrayList.add(new UiColor("Slate Gray", "#657383"));
        deprecatedUiColorsArrayList.add(new UiColor("Light Slate Gray", "#6D7B8D"));
        deprecatedUiColorsArrayList.add(new UiColor("Dark Slate Gray4", "#4C7D7E"));
        deprecatedUiColorsArrayList.add(new UiColor("Black", "#000000"));

        return deprecatedUiColorsArrayList;
    }

    /**
     * Returns 0 if UI color not found
     */
    private int getDeprecatedUiColorPosition(String colorName) {
        ArrayList<UiColor> deprecatedUiColorsArrayList = getDeprecatedUiColorsArrayList();

        int count = deprecatedUiColorsArrayList.size();
        for (int i = 1; i < count; i++) {
            if (deprecatedUiColorsArrayList.get(i).colorName.equals(colorName)) {
                return i;
            }
        }

        return 0;
    }

    private String getDeprecatedUiColorCode() {
        // Get UI color from preferences
        String selectedUIColorName = MyApp.getInstance().prefs.getString(Prefs.PREF_UI_COLOR, null);

        if (selectedUIColorName != null) {
            int pos = getDeprecatedUiColorPosition(selectedUIColorName);
            UiColor o = getDeprecatedUiColorsArrayList().get(pos);
            return o.colorCode;
        }

        return null;
    }

    public class UiColor {
        public final String colorName;
        public final String colorCode;

        public UiColor(String colorName, String colorCode) {
            this.colorName = colorName;
            this.colorCode = colorCode;
        }
    }
}
