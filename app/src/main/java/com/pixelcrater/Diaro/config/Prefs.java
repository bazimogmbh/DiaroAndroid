package com.pixelcrater.Diaro.config;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.Static;

public class Prefs {
    // Preferences
    public static final String NAME = "diaro";

    // These prefs are encrypted with MyDevice.deviceUid
    public static final String PREF_ENC_SIGNED_IN_EMAIL = NAME + ".signed_in_email";
    public static final String PREF_ENC_SIGNED_IN_ACCOUNT_TYPE = NAME + ".signed_in_account_type";
    public static final String PREF_ENC_PRO = NAME + ".pro";
    public static final String PREF_ENC_SUBSCRIBED_CURRENTLY = NAME + ".pro_subs_yearly";
    public static final String PREF_ENC_SUBSCRIBED_PLAY_NBO = NAME + ".pro_subs_play_nbo";
    public static final String PREF_ENC_FORGOT_SECURITY_CODE_EMAIL = NAME + ".forgot_email";
    public static final String PREF_ENC_SECURITY_CODE = NAME + ".passcode";

    // Partial encryption key (for SQLite encryption)
    public static final String PREF_PARTIAL_ENCRYPTION_KEY = NAME + ".pek";

    public static final String PREF_ALLOW_FINGERPRINT = NAME + ".allow_fingerprint";
    public static final String PREF_SKIP_SECURITY_CODE_FOR_WIDGET = NAME + ".skip_passcode_for_widget";
    public static final String PREF_SC_REQUEST_PERIOD = NAME + ".sc_request_period";
    public static final String PREF_ADD_ENTRY_ONGOING_NOTIFICATION_ICON = NAME + ".notification";
    public static final String PREF_TIME_TO_WRITE_NOTIFICATION_ENABLED = NAME + ".ttw_notification_enabled";
    public static final String PREF_TIME_TO_WRITE_NOTIFICATION_TIME = NAME + ".ttw_notification_time";
    public static final String PREF_TIME_TO_WRITE_NOTIFICATION_WEEKDAYS = NAME + ".ttw_notification_weekdays";
    public static final String PREF_TIME_TO_WRITE_NOTIFICATION_MUTE_SOUND = NAME + ".ttw_notification_mute_sound";
    public static final String PREF_TIME_TO_WRITE_NOTIFICATION_SMART_REMINDER = NAME + ".ttw_notification_smart_reminder";
    public static final String PREF_ENTRY_DATE_STYLE = NAME + ".entry_date_style";
    public static final String PREF_TEXT_SIZE = NAME + ".text_size";
    public static final String PREF_LOCALE = NAME + ".locale";
    public static final String PREF_UI_THEME = NAME + ".ui_theme";
    public static final String PREF_UI_COLOR = NAME + ".ui_color";
    public static final String PREF_UI_ACCENT_COLOR = NAME + ".ui_accent_color";
    public static final String PREF_DISPLAY_DENSITY = NAME + ".display_density";
    public static final String PREF_FONT = NAME + ".font";
    public static final String PREF_UNITS = NAME + ".units";
    public static final String PREF_FIRST_DAY_OF_WEEK = NAME + ".first_day_of_week";            // 0 - Sunday, 1 - Monday, 7 - Saturday
    public static final String PREF_AUTOMATIC_LOCATION = NAME + ".automatic_location";
    public static final String PREF_SYNC_ON_WIFI_ONLY = NAME + ".sync_on_wifi_only";
    public static final String PREF_ALLOW_ROAMING_SYNC = NAME + ".allow_roaming_sync";
    public static final String PREF_FREE_UP_DEVICE_STORAGE = NAME + ".free_up_device_storage";
    public static final String PREF_SHOW_SYNC_NOTIFICATION = NAME + ".show_sync_notification";
    public static final String PREF_ACTIVE_CALENDAR_RANGE_FROM_MILLIS = NAME + ".active_calendar_range_from";
    public static final String PREF_ACTIVE_CALENDAR_RANGE_TO_MILLIS = NAME + ".active_calendar_range_to";
    public static final String PREF_ACTIVE_FOLDER_UID = NAME + ".active_folder_uid";
    public static final String PREF_ACTIVE_MOOD_UID = NAME + ".active_mood_uid";
    public static final String PREF_ACTIVE_TAGS = NAME + ".active_tags";
    public static final String PREF_ACTIVE_LOCATIONS = NAME + ".active_locations";
    public static final String PREF_ACTIVE_SEARCH_TEXT = NAME + ".active_search_text";
    public static final String PREF_TAP_ENTRY_TO_EDIT = NAME + ".tap_entry_to_edit";
    public static final String PREF_SIDEMENU_CALENDAR_OPEN = NAME + ".calendar_open";
    public static final String PREF_SIDEMENU_FOLDERS_OPEN = NAME + ".folders_open";
    public static final String PREF_SIDEMENU_TAGS_OPEN = NAME + ".tags_open";
    public static final String PREF_SIDEMENU_LOCATIONS_OPEN = NAME + ".locations_open";
    public static final String PREF_SIDEMENU_MOODS_OPEN = NAME + ".moods_open";
    public static final String PREF_APP_OPENED_COUNTER = NAME + ".app_opened_counter";
    public static final String PREF_ENTRIES_SORT = NAME + ".entries_sort";
    public static final String PREF_SHOW_PROFILE_PHOTO = NAME + ".show_profile_photo";
    public static final String PREF_PERMANENT_STORAGE = NAME + ".permanent_storage";
    public static final String PREF_PERMANENT_STORAGE_TREE_URI = NAME + ".permanent_storage_tree_uri";
    public static final String PREF_APP_LIFETIME_STORAGE = NAME + ".app_lifetime_storage";
    public static final String PREF_FOLDERS_SORT = NAME + ".folders_sort";
    public static final String PREF_TAGS_SORT = NAME + ".tags_sort";
    public static final String PREF_LOCATIONS_SORT = NAME + ".locations_sort";
    public static final String PREF_MOODS_SORT = NAME + ".moods_sort";
    public static final String PREF_TAGS_LOGIC = NAME + ".tags_logic";
    public static final String PREF_SHOW_MAP_IN_ENTRY = NAME + ".show_map_in_entry";
    public static final String PREF_MAP_TYPE = NAME + ".map_type";
    public static final String PREF_ENTRY_PHOTOS_POSITION = NAME + ".entry_photos_position";
    public static final String PREF_DETECT_PHONE_NUMBERS = NAME + ".detect_phone_numbers";
    public static final String PREF_MOODS_ENABLED = NAME + ".moods_enabled";
    public static final String PREF_WEATHER_ENABLED = NAME + ".weather_enabled";
    public static final String PREF_SCREENSHOT_ENABLED = NAME + ".screenshot_enabled";
    public static final String PREF_ON_THIS_DAY_ENABLED = NAME + ".on_this_day_enabled";
    public static final String PREF_TITLE_ENABLED = NAME + ".title_enabled";
    public static final String PREF_FAST_SCROLL_ENABLED = NAME + ".fast_scroll_enabled";
    public static final String PREF_BOTTOM_TAB_ENABLED = NAME + ".bottom_tab_enabled";
    public static final String isMigratedToSql4Key = "__isMigratedToSql4__";

    public static final String PREF_STATS_SELECTION = NAME + ".stats_selection";
    public static final String PREF_ON_THIS_DAY_INCLUDE_CURRENT_YEAR = NAME + ".on_this_day_include_current_year";

    public static final String PREF_EXPORT_PHOTOS_OPTION = NAME + ".export_photos_option";
    public static final String PREF_EXPORT_LAYOUT_OPTION = NAME + ".export_layout_option";

    public static final String PREF_EXPORT_INCLUDE_SUMMARY_OPTION = NAME + ".export_summary_option";
    public static final String PREF_EXPORT_INCLUDE_LOGO_OPTION = NAME + ".export_logo_option";

    // Possible values
    public static final int ENTRY_DATE_STYLE_SMALL = 0;
    public static final int ENTRY_DATE_STYLE_LARGE = 1;

    public static final int SIZE_SMALL = 0;
    public static final int SIZE_NORMAL = 1;
    public static final int SIZE_LARGE = 2;
    public static final int SIZE_X_LARGE = 3;

    public static final int SORT_NEWEST_FIRST = 0;
    public static final int SORT_OLDEST_FIRST = 1;

    public static final int SORT_ALPHABETICALLY = 0;
    public static final int SORT_BY_ENTRIES_COUNT = 1;

    public static final int FILTER_LOGIC_OR = 0;
    public static final int FILTER_LOGIC_AND = 1;

    private static void setRandomPartialDbEncryptionKey() {
        MyApp.getInstance().prefs.edit().putString(PREF_PARTIAL_ENCRYPTION_KEY, Static.generateRandomUid()).apply();
    }

    public static String getFullDbEncryptionKey() {
        String partialEncryptionKey = MyApp.getInstance().prefs.getString(PREF_PARTIAL_ENCRYPTION_KEY, "");
        if (partialEncryptionKey.equals("")) {
            setRandomPartialDbEncryptionKey();
            return getFullDbEncryptionKey();
        }

        String fullEncryptionKey = Static.md5(partialEncryptionKey + "|" + GlobalConstants.ENCRYPTION_KEY);
      //     AppLog.e("fullEncryptionKey: " + fullEncryptionKey);


        return fullEncryptionKey;
    }

}
