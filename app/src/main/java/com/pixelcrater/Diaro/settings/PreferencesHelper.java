package com.pixelcrater.Diaro.settings;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Build;

import com.google.android.gms.maps.GoogleMap;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.config.FontsConfig;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.export.ExportOptions;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.KeyValuePair;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.Static;
import com.sandstorm.weather.WeatherHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class PreferencesHelper {

    // the default config values
    private static boolean TITLE_ENABLED_DEFAULT = true;
    private static boolean MOODS_ENABLED_DEFAULT = true;
    private static boolean WEATHER_ENABLED_DEFAULT = true;
    private static boolean SCREENSHOT_ENABLED_DEFAULT = true;
    private static boolean ON_THIS_DAY_ENABLED_DEFAULT = true;
    private static boolean FAST_SCROLL_ENABLED_DEFAULT = true;
    private static boolean BOTTOM_TAB_ENABLED_DEFAULT = true;

    // Temprature Unit Helpers
    public static int getPrefUnit() {
        return MyApp.getInstance().prefs.getInt(Prefs.PREF_UNITS, -1);
    }

    public static void setPrefUnit(int val) {
        MyApp.getInstance().prefs.edit().putInt(Prefs.PREF_UNITS, val).apply();
    }

    public static void setPrefUnitDefault() {
        int val = getPrefUnit();
        // nothing has been set yet
        if (val == -1) {
            String localeCountry = MyApp.getInstance().getResources().getConfiguration().locale.getCountry().toUpperCase();
            if (Arrays.asList(WeatherHelper.FAHRENHEIT_COUNTRIES).contains(localeCountry)) {
                setPrefUnit(WeatherHelper.FAHRENHEIT);
            } else {
                setPrefUnit(WeatherHelper.CELSIUS);
            }
        }
    }

    public static boolean isPrefUnitFahrenheit() {
        int val = MyApp.getInstance().prefs.getInt(Prefs.PREF_UNITS, -1);

        if (val == WeatherHelper.FAHRENHEIT)
            return true;
        else
            return false;
    }

    public static boolean isFastScrollEnabled() {
        return MyApp.getInstance().prefs.getBoolean(Prefs.PREF_FAST_SCROLL_ENABLED, FAST_SCROLL_ENABLED_DEFAULT);
    }

    public static boolean isBottomTabEnabled() {
        return MyApp.getInstance().prefs.getBoolean(Prefs.PREF_BOTTOM_TAB_ENABLED, BOTTOM_TAB_ENABLED_DEFAULT);
    }

    public static boolean isTitleEnabled() {
        return MyApp.getInstance().prefs.getBoolean(Prefs.PREF_TITLE_ENABLED, TITLE_ENABLED_DEFAULT);
    }

    public static boolean isMoodsEnabled() {
        return MyApp.getInstance().prefs.getBoolean(Prefs.PREF_MOODS_ENABLED, MOODS_ENABLED_DEFAULT);
    }

    public static boolean isWeatherEnabled() {
        return MyApp.getInstance().prefs.getBoolean(Prefs.PREF_WEATHER_ENABLED, WEATHER_ENABLED_DEFAULT);
    }

    public static boolean isScreenshotEnabled() {
        return MyApp.getInstance().prefs.getBoolean(Prefs.PREF_SCREENSHOT_ENABLED, SCREENSHOT_ENABLED_DEFAULT);
    }

    public static boolean isOnThisDayEnabled() {
        return MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ON_THIS_DAY_ENABLED, ON_THIS_DAY_ENABLED_DEFAULT);
    }

    public static ArrayList<KeyValuePair> getUIThemeOptions(Context ctx) {
        ArrayList<KeyValuePair> options = new ArrayList<>();
        options.add(new KeyValuePair(MyThemesUtils.THEME_LIGHT, ctx.getString(R.string.light)));
        options.add(new KeyValuePair(MyThemesUtils.THEME_DARK, ctx.getString(R.string.dark)));
        options.add(new KeyValuePair(MyThemesUtils.THEME_BLACK, ctx.getString(R.string.black_amoled)));

        return options;
    }


    public static ArrayList<KeyValuePair> getUnitsOptions() {
        ArrayList<KeyValuePair> options = new ArrayList<>();
        options.add(new KeyValuePair(String.valueOf(WeatherHelper.CELSIUS), WeatherHelper.STRING_CELSIUS));
        options.add(new KeyValuePair(String.valueOf(WeatherHelper.FAHRENHEIT), WeatherHelper.STRING_FAHRENHEIT));

        return options;
    }

    public static ArrayList<KeyValuePair> getFirstDayOfWeekOptions() {

        ArrayList<KeyValuePair> options = new ArrayList<>();
        options.add(new KeyValuePair(String.valueOf(Calendar.SUNDAY), Static.getDayOfWeekTitle(1)));
        options.add(new KeyValuePair(String.valueOf(Calendar.MONDAY), Static.getDayOfWeekTitle(2)));
        options.add(new KeyValuePair(String.valueOf(Calendar.SATURDAY), Static.getDayOfWeekTitle(7)));

        return options;
    }

    // as language code e.g de
    public static String getCurrentLocaleAsCode(Context ctx) {
        Configuration config = ctx.getResources().getConfiguration();
        String androidLocale = config.locale.toString();
        String prefLocale = MyApp.getInstance().prefs.getString(Prefs.PREF_LOCALE, androidLocale);

        if (prefLocale == null) {
            prefLocale = "en";
        }

        return prefLocale;
    }

    // as display language e.g German
    public static String getCurrentLocaleAsLanguage(Context ctx) {
        Locale androidLocale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            androidLocale = ctx.getResources().getConfiguration().getLocales().get(0);
        } else {
            androidLocale = ctx.getResources().getConfiguration().locale;
        }

        String prefLocale = MyApp.getInstance().prefs.getString(Prefs.PREF_LOCALE, androidLocale.getLanguage());

        return buildLanguageString(prefLocale, androidLocale.getDisplayLanguage(new Locale(prefLocale)));
    }

    private static String buildLanguageString(String langCode, String defaultValue) {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            locale = Locale.forLanguageTag(langCode);

        } else
            locale = new Locale(langCode);

        if (locale.getDisplayLanguage().length() < 4)
            return locale.getDisplayLanguage(new Locale(langCode));
        else
            return locale.getDisplayLanguage();

    }

    public static ArrayList<KeyValuePair> getLocales() {
        ArrayList<KeyValuePair> locales = new ArrayList<>();

        // Main languages
        locales.add(new KeyValuePair("en", buildLanguageString("en", "English")));
        locales.add(new KeyValuePair("de", buildLanguageString("de", "German")));
        locales.add(new KeyValuePair("ru", buildLanguageString("ru", "Russian")));
        locales.add(new KeyValuePair("es", buildLanguageString("es", "Spanish")));
        locales.add(new KeyValuePair("it", buildLanguageString("it", "Italian")));
        locales.add(new KeyValuePair("pt", buildLanguageString("pt", "Portuguese")));
        locales.add(new KeyValuePair("fr", buildLanguageString("fr", "French")));

        // Other languages
        locales.add(new KeyValuePair("ar", buildLanguageString("ar", "Arabic")));
        locales.add(new KeyValuePair("bs_BA", buildLanguageString("bs", "Bosnian")));
        locales.add(new KeyValuePair("bg_BG", buildLanguageString("bg", "Bulgarian")));
        locales.add(new KeyValuePair("ca", buildLanguageString("ca", "Catalan")));
        locales.add(new KeyValuePair("zh_CN", "Chinese Simplified "));
        locales.add(new KeyValuePair("zh_TW", "Chinese Traditional "));
        locales.add(new KeyValuePair("hr", buildLanguageString("hr", "Croatian")));
        locales.add(new KeyValuePair("cs", buildLanguageString("cs", "Czech")));
        locales.add(new KeyValuePair("da", buildLanguageString("da", "Danish")));
        locales.add(new KeyValuePair("nl", buildLanguageString("nl", "Dutch")));
        locales.add(new KeyValuePair("fi", buildLanguageString("fi", "Finnish")));
        locales.add(new KeyValuePair("gl_ES", buildLanguageString("gl", "Galician (Spain)")));
        locales.add(new KeyValuePair("el", buildLanguageString("el", "Greek")));
        locales.add(new KeyValuePair("he", buildLanguageString("he", "Hebrew")));
        locales.add(new KeyValuePair("hu", buildLanguageString("hu", "Hungarian")));
        locales.add(new KeyValuePair("de", buildLanguageString("hi", "Hindi")));
        locales.add(new KeyValuePair("in", buildLanguageString("in", "Indonesian"))); // "id" is deprecated
        locales.add(new KeyValuePair("ja", buildLanguageString("ja", "Japanese")));
        locales.add(new KeyValuePair("ko", buildLanguageString("ko", "Korean")));
        locales.add(new KeyValuePair("lv_LV", buildLanguageString("lv", "Latvian")));
        locales.add(new KeyValuePair("lt_LT", buildLanguageString("lt", "Lithuanian")));
        locales.add(new KeyValuePair("no", buildLanguageString("no", "Norwegian")));
        locales.add(new KeyValuePair("fa", buildLanguageString("fa", "Persian")));
        locales.add(new KeyValuePair("pl", buildLanguageString("pl", "Polish")));
        locales.add(new KeyValuePair("pt_BR", "Portuguese (Brazil)"));
        locales.add(new KeyValuePair("ro", buildLanguageString("ro", "Romanian")));
        locales.add(new KeyValuePair("sk", buildLanguageString("sk", "Slovak")));
        locales.add(new KeyValuePair("sl_SI", buildLanguageString("sl", "Slovenian")));
        locales.add(new KeyValuePair("sv", buildLanguageString("sv", "Swedish")));
        locales.add(new KeyValuePair("th", buildLanguageString("th", "Thai")));
        locales.add(new KeyValuePair("tr", buildLanguageString("tr", "Turkish")));
        locales.add(new KeyValuePair("uk", buildLanguageString("uk", "Ukrainian")));

        return locales;
    }

    public static int getLocaleIndex(Context ctx) {

        String prefLocale = getCurrentLocaleAsCode(ctx);
        ArrayList<KeyValuePair> options = PreferencesHelper.getLocales();

        // Try to find the same locale
        for (int i = 0; i < options.size(); i++) {
            KeyValuePair option = options.get(i);
            if (option.key.equals(prefLocale)) {
                return i;
            }
        }

        // If still not found, compare only language from locale (without country)
        String language = prefLocale.substring(0, 2);
        for (int i = 0; i < options.size(); i++) {
            KeyValuePair option = options.get(i);
            if (option.key.equals(language)) {
                return i;
            }
        }

        return 0;
    }


    public static ArrayList<KeyValuePair> getEntryDateStyleOptions(Context ctx) {
        ArrayList<KeyValuePair> options = new ArrayList<>();
        options.add(new KeyValuePair(String.valueOf(Prefs.ENTRY_DATE_STYLE_SMALL), ctx.getString(R.string.small_date)));
        options.add(new KeyValuePair(String.valueOf(Prefs.ENTRY_DATE_STYLE_LARGE), ctx.getString(R.string.large_date)));

        return options;
    }


    public static ArrayList<KeyValuePair> getTextSizeOptions(Context ctx) {
        ArrayList<KeyValuePair> options = new ArrayList<>();
        options.add(new KeyValuePair(String.valueOf(Prefs.SIZE_SMALL), ctx.getString(R.string.small)));
        options.add(new KeyValuePair(String.valueOf(Prefs.SIZE_NORMAL), ctx.getString(R.string.normal)));
        options.add(new KeyValuePair(String.valueOf(Prefs.SIZE_LARGE), ctx.getString(R.string.large)));
        options.add(new KeyValuePair(String.valueOf(Prefs.SIZE_X_LARGE), ctx.getString(R.string.extra_large)));

        return options;
    }

    public static ArrayList<KeyValuePair> getMapTypeOptions(Context ctx) {
        ArrayList<KeyValuePair> options = new ArrayList<>();
        options.add(new KeyValuePair(String.valueOf(GoogleMap.MAP_TYPE_NORMAL), ctx.getString(R.string.normal)));
        options.add(new KeyValuePair(String.valueOf(GoogleMap.MAP_TYPE_SATELLITE), ctx.getString(R.string.satellite)));
        options.add(new KeyValuePair(String.valueOf(GoogleMap.MAP_TYPE_TERRAIN), ctx.getString(R.string.terrain)));
        options.add(new KeyValuePair(String.valueOf(GoogleMap.MAP_TYPE_HYBRID), ctx.getString(R.string.hybrid)));

        return options;
    }


    public static ArrayList<KeyValuePair> getScRequestPeriodOptions(Context ctx) {
        ArrayList<KeyValuePair> options = new ArrayList<>();
        options.add(new KeyValuePair("0", ctx.getString(R.string.settings_immediately)));
        options.add(new KeyValuePair("10", ctx.getString(R.string.settings_10_seconds)));
        options.add(new KeyValuePair("30", ctx.getString(R.string.settings_30_seconds)));
        options.add(new KeyValuePair("60", ctx.getString(R.string.settings_1_minute)));
        options.add(new KeyValuePair("180", ctx.getString(R.string.settings_3_minutes)));
        options.add(new KeyValuePair("300", ctx.getString(R.string.settings_5_minutes)));

        return options;
    }


    /**
     * @return name of the font stored in prefrences, eg Roboto
     */
    public static String getPrefFont() {
        return MyApp.getInstance().prefs.getString(Prefs.PREF_FONT, MyApp.getInstance().getString(R.string.settings_default));
    }

    public static void setPrefFont(String fontName) {
        MyApp.getInstance().prefs.edit().putString(Prefs.PREF_FONT, fontName).apply();
    }

    public static Typeface getPrefTypeFace(Context ctx) {
        String currentFontName = getPrefFont();
        String fontPath = FontsConfig.getFontPathByName(currentFontName);

        Typeface typeface = Typeface.DEFAULT;
        try {
            if (!fontPath.isEmpty()) {
                typeface = Typeface.createFromAsset(ctx.getAssets(), fontPath);
            }

        } catch (Exception e) {
            AppLog.e(currentFontName + "- " + fontPath + " ," + e.getStackTrace().toString());
        }

        return typeface;
    }

    public static int getMapType() {
        int mapType = MyApp.getInstance().prefs.getInt(Prefs.PREF_MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL);
        if (mapType < 1 || mapType > 4) {
            mapType = GoogleMap.MAP_TYPE_NORMAL;
        }
        return mapType;
    }

    public static boolean isMigratedToSql4() {
        return MyApp.getInstance().prefs.getBoolean(Prefs.isMigratedToSql4Key, false);
    }

    public static void setIsMigratedToSql4Key(boolean val) {
        MyApp.getInstance().prefs.edit().putBoolean(Prefs.isMigratedToSql4Key, val).apply();
    }

    public static void setExportPhotoPref(int index) {
        MyApp.getInstance().prefs.edit().putInt(Prefs.PREF_EXPORT_PHOTOS_OPTION, index).apply();
    }

    public static String getExportPhotoHeight() {
        int photoHeightIndex = MyApp.getInstance().prefs.getInt(Prefs.PREF_EXPORT_PHOTOS_OPTION, 1); // small is default
        String retVal = "";
        if (photoHeightIndex == 0) {
            retVal = "";
        }
        if (photoHeightIndex == 1) {
            retVal = ExportOptions.PHOTO_HEIGHT_SMALL;
        }
        if (photoHeightIndex == 2) {
            retVal = ExportOptions.PHOTO_HEIGHT_MEDIUM;
        }
        if (photoHeightIndex == 3) {
            retVal = ExportOptions.PHOTO_HEIGHT_LARGE;
        }
        return retVal;
    }

    public static void setExportLayoutPref(int index) {
        MyApp.getInstance().prefs.edit().putInt(Prefs.PREF_EXPORT_LAYOUT_OPTION, index).apply();
    }

    public static String getExportLayout() {
        String retVal = ExportOptions.LAYOUT_COMPACT;
        int layoutIndex = MyApp.getInstance().prefs.getInt(Prefs.PREF_EXPORT_LAYOUT_OPTION, 0); // compact is default

        if (layoutIndex == 0) {
            retVal = ExportOptions.LAYOUT_COMPACT;
        }
        if (layoutIndex == 1) {
            retVal = ExportOptions.LAYOUT_NORMAL;
        }

        return retVal;
    }

    public static Boolean getIncludeSummary() {
       return MyApp.getInstance().prefs.getBoolean(Prefs.PREF_EXPORT_INCLUDE_SUMMARY_OPTION, false);
    }

    public static void setIncludeSummary(Boolean includeSummary) {
        MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_EXPORT_INCLUDE_SUMMARY_OPTION, includeSummary).apply();
    }

    public static Boolean getIncludeLogo() {
        return MyApp.getInstance().prefs.getBoolean(Prefs.PREF_EXPORT_INCLUDE_LOGO_OPTION, true);
    }

    public static void setIncludeLogo(Boolean includeLogo) {
        MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_EXPORT_INCLUDE_LOGO_OPTION, includeLogo).apply();
    }

    // Current search , Entriesstatic.getEntriesAndSqlByActiveFilters does the actual search
    public static String getActiveSearchText() {
        return MyApp.getInstance().prefs.getString(Prefs.PREF_ACTIVE_SEARCH_TEXT, "");
    }

    public static void setActiveSearchText(String searchText) {
        // Save active search text to prefs
        MyApp.getInstance().prefs.edit().putString(Prefs.PREF_ACTIVE_SEARCH_TEXT, searchText).apply();
    }


    // Display density
    public static ArrayList<String> getDisplayDensityList() {
        ArrayList<String> items = new ArrayList<>();
        items.add(MyApp.getInstance().getString(R.string.cozy));
        items.add(MyApp.getInstance().getString(R.string.compact));
        items.add(MyApp.getInstance().getString(R.string.detailed));
        return items;
    }


    //  cozy = 0, compact = 1, detailed = 2
    public static void setDisplayDensity(int display_density) {
        MyApp.getInstance().prefs.edit().putInt(Prefs.PREF_DISPLAY_DENSITY, display_density).apply();
    }

    public static int getDisplayDensity() {
        return MyApp.getInstance().prefs.getInt(Prefs.PREF_DISPLAY_DENSITY, 0);
    }

    public static String getDisplayDensityString() {
        int index = getDisplayDensity();
        String value = getDisplayDensityList().get(index);
        return value;
    }


    // Text Size
    public static int getTextSize() {
        return MyApp.getInstance().prefs.getInt(Prefs.PREF_TEXT_SIZE, Prefs.SIZE_NORMAL);
    }

    public static String getTextSizeString() {
        String selectedValue = String.valueOf(getTextSize());
        ArrayList<KeyValuePair> options = PreferencesHelper.getTextSizeOptions(MyApp.getInstance().getApplicationContext());

        return getSelectedIndex(selectedValue, options);
    }


    public static String getThemeString() {
        String selectedValue = MyThemesUtils.getTheme();
        ArrayList<KeyValuePair> options = getUIThemeOptions(MyApp.getInstance().getApplicationContext());

        return getSelectedIndex(selectedValue, options);

    }

    public static String getFirstDayOfWeekString() {
        String selectedValue = String.valueOf(MyApp.getInstance().prefs.getInt(Prefs.PREF_FIRST_DAY_OF_WEEK, Calendar.SUNDAY));
        ArrayList<KeyValuePair> options = PreferencesHelper.getFirstDayOfWeekOptions();

        return getSelectedIndex(selectedValue, options);
    }

    public static String getEntryDateStyleString() {
        String selectedValue = String.valueOf(MyApp.getInstance().prefs.getInt(Prefs.PREF_ENTRY_DATE_STYLE, Prefs.ENTRY_DATE_STYLE_LARGE));
        ArrayList<KeyValuePair> options = getEntryDateStyleOptions(MyApp.getInstance().getApplicationContext());

        return getSelectedIndex(selectedValue, options);
    }

    public static String getMapTypeString() {
        String selectedValue = String.valueOf(PreferencesHelper.getMapType());
        ArrayList<KeyValuePair> options = PreferencesHelper.getMapTypeOptions(MyApp.getInstance().getApplicationContext());

        return getSelectedIndex(selectedValue, options);
    }


    private static String getSelectedIndex(String selectedValue, ArrayList<KeyValuePair> options) {
        int selectedIndex = 0;
        for (int i = 0; i < options.size(); i++) {
            KeyValuePair option = options.get(i);
            if (option.key.equals(selectedValue)) {
                selectedIndex = i;
            }
        }
        return options.get(selectedIndex).value;
    }


}
