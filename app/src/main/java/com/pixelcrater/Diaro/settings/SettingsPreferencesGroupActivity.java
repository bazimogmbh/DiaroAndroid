package com.pixelcrater.Diaro.settings;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.biometric.BiometricManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.TwoStatePreference;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.activitytypes.TypePreferenceActivity;
import com.pixelcrater.Diaro.config.FontsConfig;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.generaldialogs.OptionsDialog;
import com.pixelcrater.Diaro.securitycode.SecurityCodeActivity;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.KeyValuePair;
import com.pixelcrater.Diaro.utils.MyDevice;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.Static;
import com.sandstorm.weather.WeatherHelper;

import java.util.ArrayList;
import java.util.Calendar;

public class SettingsPreferencesGroupActivity extends TypePreferenceActivity implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityState.setActionBarTitle(getSupportActionBar(), getString(R.string.settings_preferences));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Add preferences from XML
        myPreferenceFragment.addPreferencesFromResource(R.xml.preferences_general);

        // Security code grid_section
        setupPreference("SECURITY_CODE");
        setupCheckboxPreference("FINGERPRINT");
        setupCheckboxPreference("SECURITY_CODE");
        setupCheckboxPreference("DONT_ASK_SECURITY_CODE_FROM_WIDGET");
        setupPreference("SECURITY_CODE_REQUEST_PERIOD");
        setupCheckboxPreference("ALLOW_SCREENSHOT");

        // Appearance grid_section
        setupPreference("LOCALE");
        setupPreference("UI_THEME");
        setupPreference("UI_COLOR");
        setupPreference("UI_ACCENT_COLOR");
        setupPreference("DISPLAY_DENSITY");
        setupPreference("FONT");
        setupPreference("TEXT_SIZE");
        setupPreference("UNITS");
        setupPreference("FIRST_DAY_OF_WEEK");
        setupPreference("ENTRY_DATE_STYLE");
        setupPreference("MAP_TYPE");
        setupCheckboxPreference("SHOW_PROFILE_PHOTO");
        setupCheckboxPreference("ENTRY_PHOTOS_POSITION");
        setupCheckboxPreference("SHOW_MAP_IN_ENTRY");

        // Notification icon grid_section
        setupCheckboxPreference("NOTIFICATION");

        // Other preferences grid_section
        setupCheckboxPreference("AUTOMATIC_LOCATION");

        setupCheckboxPreference("TAP_ENTRY_TO_EDIT");
        setupCheckboxPreference("DETECT_PHONE_NUMBERS");
        setupCheckboxPreference("FAST_SCROLL");
        setupCheckboxPreference("BOTTOM_TAB");
        setupCheckboxPreference("TITLE");
        setupCheckboxPreference("MOODS");
        setupCheckboxPreference("WEATHER");
        setupCheckboxPreference("ON_THIS_DAY");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (activityState.isActivityPaused) {
            return true;
        }

        switch (item.getItemId()) {
            // Back
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    // initialize the listners and set default summary text
    private void setupPreference(String key) {
        AppLog.d("key: " + key);
        Preference preference = (Preference) myPreferenceFragment.findPreference(key);

        if (preference == null)
            return;

        preference.setOnPreferenceChangeListener(this);

        switch (key) {
            case "LOCALE":
                setLocalePrefSummary();
                break;

            case "UI_THEME":
                setThemePrefSummary();
                break;

            case "UI_COLOR":
                //   setLocalePrefText();
                break;

            case "UI_ACCENT_COLOR":
                //    setLocalePrefText();
                break;

            case "DISPLAY_DENSITY":
                setDisplayDensitySummary();
                break;

            case "FONT":
                setFontPrefSummary();
                break;

            case "TEXT_SIZE":
                setTextSizeSummary();
                break;

            case "UNITS":
                setUnitsPrefSummary();
                break;

            case "FIRST_DAY_OF_WEEK":
                setFirstDayOfWeekSummary();
                break;

            case "ENTRY_DATE_STYLE":
                setEntryDateStyleSummary();
                break;


            case "MAP_TYPE":
                if (!MyDevice.getInstance().isGooglePlayServicesAvailable()) {
                    PreferenceCategory categoryOtherPreferences = myPreferenceFragment.findPreference("CATEGORY_APPEARANCE");
                    categoryOtherPreferences.removePreference(preference);
                } else {
                    setMapTypeSummary();
                }
                break;
        }

        preference.setOnPreferenceClickListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Static.REQUEST_SECURITY_CODE:
                setupCheckboxPreference("SECURITY_CODE");
                break;
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        AppLog.e("onPreferenceChange" + key);

        switch (key) {
            case "FINGERPRINT":
                MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_ALLOW_FINGERPRINT, (Boolean) newValue).apply();
                return true;

            case "DONT_ASK_SECURITY_CODE_FROM_WIDGET":
                MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_SKIP_SECURITY_CODE_FOR_WIDGET, (Boolean) newValue).apply();
                // Check if add entry ongoing notification is enabled
                if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ADD_ENTRY_ONGOING_NOTIFICATION_ICON, false)) {
                    MyApp.getInstance().notificationsMgr.addEntryOngoingNotification.showNotification();
                }

                // Update widget
                Intent widgetUpdateIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                sendBroadcast(widgetUpdateIntent);

                return true;

            case "ALLOW_SCREENSHOT":
                MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_SCREENSHOT_ENABLED, (Boolean) newValue).apply();
                activityState.restart();
                return true;

            case "LOCALE":
                setLocalePrefSummary();
                return true;

            case "UI_THEME":
                setThemePrefSummary();
                return true;

            case "DISPLAY_DENSITY":
                setDisplayDensitySummary();
                return true;

            case "FONT":
                setFontPrefSummary();
                return true;


            case "TEXT_SIZE":
                setTextSizeSummary();
                return true;

            case "UNITS":
                setUnitsPrefSummary();
                return true;

            case "FIRST_DAY_OF_WEEK":
                setFirstDayOfWeekSummary();
                return true;

            case "ENTRY_DATE_STYLE":
                setEntryDateStyleSummary();
                return true;

            case "SHOW_PROFILE_PHOTO":
                MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_SHOW_PROFILE_PHOTO, (Boolean) newValue).apply();
                Static.sendBroadcast(Static.BR_IN_MAIN, Static.DO_UPDATE_PROFILE_PHOTO, null);
                return true;

            // On permanent notification checkbox value change
            case "NOTIFICATION":
                if (newValue.equals(true)) {
                    MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_ADD_ENTRY_ONGOING_NOTIFICATION_ICON, true).apply();
                    MyApp.getInstance().notificationsMgr.addEntryOngoingNotification.showNotification();
                } else {
                    MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_ADD_ENTRY_ONGOING_NOTIFICATION_ICON, false).apply();
                    MyApp.getInstance().notificationsMgr.addEntryOngoingNotification.cancelNotification();
                }
                return true;

            // On automatic location checkbox value change
            case "AUTOMATIC_LOCATION":
                MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_AUTOMATIC_LOCATION, (Boolean) newValue).apply();
                return true;

            // On tap entry to edit checkbox value change
            case "TAP_ENTRY_TO_EDIT":
                MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_TAP_ENTRY_TO_EDIT, (Boolean) newValue).apply();
                return true;

            // On show map in entry checkbox value change
            case "SHOW_MAP_IN_ENTRY":
                MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_SHOW_MAP_IN_ENTRY, (Boolean) newValue).apply();
                return true;

            // On entry photos position checkbox value change
            case "ENTRY_PHOTOS_POSITION":
                MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_ENTRY_PHOTOS_POSITION, (Boolean) newValue).apply();
                return true;

            // On detet phone numbers checkbox value change
            case "DETECT_PHONE_NUMBERS":
                MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_DETECT_PHONE_NUMBERS, (Boolean) newValue).apply();
                return true;

            case "FAST_SCROLL":
                MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_FAST_SCROLL_ENABLED, (Boolean) newValue).apply();
                return true;

            case "BOTTOM_TAB":
                MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_BOTTOM_TAB_ENABLED, (Boolean) newValue).apply();
                return true;

            case "TITLE":
                MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_TITLE_ENABLED, (Boolean) newValue).apply();
                return true;


            case "MOODS":
                MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_MOODS_ENABLED, (Boolean) newValue).apply();
                return true;

            case "WEATHER":
                MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_WEATHER_ENABLED, (Boolean) newValue).apply();
                return true;

            case "ON_THIS_DAY":
                MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_ON_THIS_DAY_ENABLED, (Boolean) newValue).apply();
                restartActivity();
                return true;
        }

        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        AppLog.d("key: " + key);

        switch (key) {
            case "SECURITY_CODE":
                openSecurityCodeActivity();
                return true;

            case "SECURITY_CODE_REQUEST_PERIOD":
                showScRequestPeriodSelectDialog();
                return true;

            case "LOCALE":
                showLocaleSelectDialog();
                return true;

            case "UI_THEME":
                showUIThemeSelectDialog();
                return true;

            case "UI_COLOR":
                showUIColorSelectDialog();
                return true;

            case "UI_ACCENT_COLOR":
                showUiAccentColorSelectDialog();
                return true;

            case "DISPLAY_DENSITY":
                showDisplayDensitySelectDialog();
                return true;

            case "FONT":
                showFontSelectDialog();
                return true;

            case "UNITS":
                showUnitsSelectDialog();
                return true;

            case "FIRST_DAY_OF_WEEK":
                showFirstDayOfWeekSelectDialog();
                return true;

            case "ENTRY_DATE_STYLE":
                showEntryDateStyleSelectDialog();
                return true;

            case "TEXT_SIZE":
                showTextSizeSelectDialog();
                return true;

            case "MAP_TYPE":
                showMapTypeSelectDialog();
                return true;
        }

        return false;
    }


    private void setupCheckboxPreference(String key) {
        // AppLog.d("key: " + key);
        TwoStatePreference preference = myPreferenceFragment.findPreference(key);
        preference.setOnPreferenceChangeListener(this);
        preference.setChecked(false);

        switch (key) {
            case "SECURITY_CODE":
                if (MyApp.getInstance().securityCodeMgr.isSecurityCodeSet()) {
                    preference.setChecked(true);
                    preference.setSummary(R.string.remove_security_code);
                } else {
                    preference.setSummary(R.string.settings_security_summary);
                }
                break;

            case "FINGERPRINT":
                if (BiometricManager.from(this).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==BiometricManager.BIOMETRIC_SUCCESS) {
                    if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ALLOW_FINGERPRINT, true)) {
                        preference.setChecked(true);
                    }
                } else {
                    // Remove fingerprint preference if no sensor available
                    PreferenceCategory categorySecurity = myPreferenceFragment.findPreference("CATEGORY_SECURITY");
                    categorySecurity.removePreference(preference);
                }
                break;

            case "DONT_ASK_SECURITY_CODE_FROM_WIDGET":
                if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_SKIP_SECURITY_CODE_FOR_WIDGET, true)) {
                    preference.setChecked(true);
                }
                break;

            case "ALLOW_SCREENSHOT":
                if (PreferencesHelper.isScreenshotEnabled()) {
                    preference.setChecked(true);
                }
                break;

            case "SHOW_PROFILE_PHOTO":
                preference.setChecked(MyApp.getInstance().prefs.getBoolean(Prefs.PREF_SHOW_PROFILE_PHOTO, true));
                break;


            case "NOTIFICATION":
                if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ADD_ENTRY_ONGOING_NOTIFICATION_ICON, false)) {
                    MyApp.getInstance().notificationsMgr.addEntryOngoingNotification.showNotification();
                    preference.setChecked(true);
                }
                break;

            case "AUTOMATIC_LOCATION":
                if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_AUTOMATIC_LOCATION, true)) {
                    preference.setChecked(true);
                }
                break;

            case "TAP_ENTRY_TO_EDIT":
                if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_TAP_ENTRY_TO_EDIT, true)) {
                    preference.setChecked(true);
                }
                break;

            case "SHOW_MAP_IN_ENTRY":
                if (MyDevice.getInstance().isGooglePlayServicesAvailable()) {
                    if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_SHOW_MAP_IN_ENTRY, true)) {
                        preference.setChecked(true);
                    }
                } else {
                    // Remove show map in entry preference on devices without Google
                    // Play Services
                    PreferenceCategory categoryOtherPreferences = myPreferenceFragment.findPreference("CATEGORY_OTHER_PREFERENCES");
                    categoryOtherPreferences.removePreference(preference);
                }
                break;
            case "ENTRY_PHOTOS_POSITION":
                if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ENTRY_PHOTOS_POSITION, true)) {
                    preference.setChecked(true);
                }
                break;
            case "DETECT_PHONE_NUMBERS":
                if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_DETECT_PHONE_NUMBERS, true)) {
                    preference.setChecked(true);
                }
                break;

            case "FAST_SCROLL":
                if (PreferencesHelper.isFastScrollEnabled()) {
                    preference.setChecked(true);
                }
                break;

            case "BOTTOM_TAB":
                if (PreferencesHelper.isBottomTabEnabled()) {
                    preference.setChecked(true);
                }
                break;

            case "TITLE":
                if (PreferencesHelper.isTitleEnabled()) {
                    preference.setChecked(true);
                }
                break;
            case "MOODS":
                if (PreferencesHelper.isMoodsEnabled()) {
                    preference.setChecked(true);
                }
                break;

            case "WEATHER":
                if (PreferencesHelper.isWeatherEnabled()) {
                    preference.setChecked(true);
                }
                break;

            case "ON_THIS_DAY":
                if (PreferencesHelper.isOnThisDayEnabled()) {
                    preference.setChecked(true);
                }
                break;


        }
    }

    private void openSecurityCodeActivity() {
        AppLog.d("");
        Intent intent = new Intent(this, SecurityCodeActivity.class);
        intent.putExtra(Static.EXTRA_SKIP_SC, true);

        if (MyApp.getInstance().securityCodeMgr.isSecurityCodeSet()) {
            intent.putExtra("mode", SecurityCodeActivity.MODE_REMOVE);
        } else {
            intent.putExtra("mode", SecurityCodeActivity.MODE_STEP1_NEW);
        }

        startActivityForResult(intent, Static.REQUEST_SECURITY_CODE);
    }


    private void showScRequestPeriodSelectDialog() {
        String dialogTag = Static.DIALOG_SELECT_SETTINGS_SC_REQUEST_PERIOD;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            OptionsDialog dialog = new OptionsDialog();
            dialog.setTitle(getString(R.string.settings_security_code_request_period));

            // Set selected value
            String selectedValue = String.valueOf(MyApp.getInstance().prefs.getInt(Prefs.PREF_SC_REQUEST_PERIOD, 0));
            int selectedIndex = 0;
            ArrayList<KeyValuePair> options = PreferencesHelper.getScRequestPeriodOptions(getApplicationContext());

            ArrayList<String> items = new ArrayList<>();
            for (int i = 0; i < options.size(); i++) {
                KeyValuePair option = options.get(i);
                items.add(option.value);
                if (option.key.equals(selectedValue)) {
                    selectedIndex = i;
                }
            }
            dialog.setItemsTitles(items);
            dialog.setSelectedIndex(selectedIndex);

            dialog.show(getSupportFragmentManager(), dialogTag);
            // Set dialog listener
            setScRequestPeriodSelectDialogListener(dialog);
        }
    }

    private void setScRequestPeriodSelectDialogListener(OptionsDialog dialog) {
        dialog.setDialogItemClickListener(which -> {
            MyApp.getInstance().prefs.edit().putInt(Prefs.PREF_SC_REQUEST_PERIOD, Integer.parseInt(PreferencesHelper.getScRequestPeriodOptions(getApplicationContext()).get(which).key)).apply();
        });
    }

    private void showFontSelectDialog() {
        String dialogTag = Static.DIALOG_SELECT_SETTINGS_FONT;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            OptionsDialog dialog = new OptionsDialog();
            dialog.setTitle(getString(R.string.settings_font_style));

            String selectedFont = PreferencesHelper.getPrefFont();
            ArrayList<KeyValuePair> options = FontsConfig.getFontOptions();

            ArrayList<String> fontNames = new ArrayList<>();
            ArrayList<String> fontsPaths = new ArrayList<>();

            int selectedIndex = 0;
            for (int i = 0; i < options.size(); i++) {
                KeyValuePair option = options.get(i);
                fontNames.add(option.key);
                fontsPaths.add(option.value);
                if (option.key.equals(String.valueOf(selectedFont))) {
                    selectedIndex = i;
                }
            }

            dialog.setItemsTitles(fontNames);
            dialog.setItemsFontPaths(fontsPaths);
            dialog.setSelectedIndex(selectedIndex);

            dialog.show(getSupportFragmentManager(), dialogTag);
            dialog.setDialogItemClickListener(which -> {
                PreferencesHelper.setPrefFont(fontNames.get(which));
                setFontPrefSummary();
            });
        }
    }


    private void showUnitsSelectDialog() {
        String dialogTag = Static.DIALOG_SELECT_SETTINGS_UNITS;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            OptionsDialog dialog = new OptionsDialog();
            dialog.setTitle(getString(R.string.settings_units));

            ArrayList<KeyValuePair> options = PreferencesHelper.getUnitsOptions();
            int selectedIndex = PreferencesHelper.getPrefUnit();

            ArrayList<String> items = new ArrayList<>();
            for (int i = 0; i < options.size(); i++) {
                KeyValuePair option = options.get(i);
                items.add(option.value);
                if (option.key.equals(String.valueOf(selectedIndex))) {
                    selectedIndex = i;
                }
            }

            dialog.setItemsTitles(items);
            dialog.setSelectedIndex(selectedIndex);
            dialog.show(getSupportFragmentManager(), dialogTag);

            dialog.setDialogItemClickListener(which -> {
                PreferencesHelper.setPrefUnit(which);
                setUnitsPrefSummary();
            });

        }
    }


    private void showLocaleSelectDialog() {
        String dialogTag = Static.DIALOG_SELECT_SETTINGS_LOCALE;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            ArrayList<String> items = new ArrayList<>();
            ArrayList<KeyValuePair> locales = PreferencesHelper.getLocales();
            for (int i = 0; i < locales.size(); i++) {
                KeyValuePair locale = locales.get(i);
                items.add(locale.value);
            }

            // Show dialog
            OptionsDialog dialog = new OptionsDialog();
            dialog.setTitle(getString(R.string.language));
            dialog.setTip(getText(R.string.settings_translate_tip).toString());
            dialog.setItemsTitles(items);
            dialog.setSelectedIndex(PreferencesHelper.getLocaleIndex(getApplicationContext()));

            dialog.show(getSupportFragmentManager(), dialogTag);
            // Set dialog listener
            dialog.setDialogItemClickListener(which -> {
                KeyValuePair locale = PreferencesHelper.getLocales().get(which);
                String valueFromPrefs = MyApp.getInstance().prefs.getString(Prefs.PREF_LOCALE, null);

                // If value changed
                if (valueFromPrefs == null || !locale.key.equals(valueFromPrefs)) {
                    MyApp.getInstance().prefs.edit().putString(Prefs.PREF_LOCALE, locale.key).apply();
                    // Refresh locale
                    Static.refreshLocale();
                    // Check if add entry ongoing notification is enabled
                    if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ADD_ENTRY_ONGOING_NOTIFICATION_ICON, false)) {
                        MyApp.getInstance().notificationsMgr.addEntryOngoingNotification.showNotification();
                    }

                    restartActivity();
                }
            });
        }
    }


    private void showUIThemeSelectDialog() {
        String dialogTag = Static.DIALOG_SELECT_SETTINGS_UI_THEME;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            OptionsDialog dialog = new OptionsDialog();
            dialog.setTitle(getString(R.string.settings_ui_theme));

            // Set selected value
            String selectedValue = MyThemesUtils.getTheme();
            ArrayList<KeyValuePair> options = PreferencesHelper.getUIThemeOptions(getApplicationContext());

            int selectedIndex = 0;
            ArrayList<String> items = new ArrayList<>();
            for (int i = 0; i < options.size(); i++) {
                KeyValuePair option = options.get(i);
                items.add(option.value);
                if (option.key.equals(selectedValue)) {
                    selectedIndex = i;
                }
            }
            dialog.setItemsTitles(items);
            dialog.setSelectedIndex(selectedIndex);

            dialog.show(getSupportFragmentManager(), dialogTag);

            // Set dialog listener
            dialog.setDialogItemClickListener(which -> {
                MyApp.getInstance().prefs.edit().putString(Prefs.PREF_UI_THEME, PreferencesHelper.getUIThemeOptions(getApplicationContext()).get(which).key).apply();
                // Clear static patternsArrayList
                Static.patternsArrayList = null;
                restartActivity();
            });
        }
    }


    private void restartActivity() {
        // Finish current dialog with result to restart again from parent activity
        Intent i = new Intent();
        i.putExtra("resultRestart", true);
        setResult(RESULT_FIRST_USER, i);
        finish();
        overridePendingTransition(0, 0);
    }

    private void showUIColorSelectDialog() {
        String dialogTag = Static.DIALOG_SELECT_SETTINGS_UI_COLOR;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            UiColorSelectDialog dialog = new UiColorSelectDialog();
            dialog.setUiColorCode(MyThemesUtils.getPrimaryColorCode());
            dialog.show(getSupportFragmentManager(), dialogTag);

            // Set dialog listeners
            dialog.setOnUiColorSelectedListener(new UiColorSelectDialog.OnUiColorSelectedListener() {
                @Override
                public void onUiColorSelected(String colorCode) {
                    activityState.setupActionBar(getSupportActionBar());
                }

                @Override
                public void restart() {
                    // Update widget
                    Intent widgetUpdateIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    sendBroadcast(widgetUpdateIntent);
                    restartActivity();
                }
            });
        }
    }


    private void showUiAccentColorSelectDialog() {
        String dialogTag = Static.DIALOG_SELECT_SETTINGS_UI_ACCENT_COLOR;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            UiAccentColorSelectDialog dialog = new UiAccentColorSelectDialog();
            dialog.setUiAccentColorCode(MyThemesUtils.getAccentColorCode());
            dialog.show(getSupportFragmentManager(), dialogTag);

            // Set dialog listeners
            dialog.setOnUiAccentColorSelectedListener(colorCode -> {
                // Get UI color from preferences
                String valueFromPrefs = MyApp.getInstance().prefs.getString(Prefs.PREF_UI_ACCENT_COLOR, null);
                // If value changed
                if (valueFromPrefs == null || !colorCode.equals(valueFromPrefs)) {
                    MyApp.getInstance().prefs.edit().putString(Prefs.PREF_UI_ACCENT_COLOR, colorCode).apply();
                    restartActivity();
                }
            });
        }
    }

    private void showDisplayDensitySelectDialog() {
        String dialogTag = Static.DIALOG_SELECT_SETTINGS_DISPLAY_DENSITY;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            OptionsDialog dialog = new OptionsDialog();
            dialog.setTitle(getString(R.string.display));

            // Set selected value
            int selectedIndex = PreferencesHelper.getDisplayDensity();
            dialog.setItemsTitles(PreferencesHelper.getDisplayDensityList());
            dialog.setSelectedIndex(selectedIndex);

            dialog.show(getSupportFragmentManager(), dialogTag);
            dialog.setDialogItemClickListener(which -> {
                PreferencesHelper.setDisplayDensity(which);
                setDisplayDensitySummary();
            });
        }
    }

    private void showFirstDayOfWeekSelectDialog() {
        String dialogTag = Static.DIALOG_SELECT_SETTINGS_FIRST_DAY_OF_WEEK;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            OptionsDialog dialog = new OptionsDialog();
            dialog.setTitle(getString(R.string.settings_first_day_of_week));

            // Set selected value
            String selectedValue = String.valueOf(MyApp.getInstance().prefs.getInt(Prefs.PREF_FIRST_DAY_OF_WEEK, Calendar.SUNDAY));

            int selectedIndex = 0;

            ArrayList<KeyValuePair> options = PreferencesHelper.getFirstDayOfWeekOptions();

            ArrayList<String> items = new ArrayList<>();
            for (int i = 0; i < options.size(); i++) {
                KeyValuePair option = options.get(i);
                items.add(option.value);
                if (option.key.equals(selectedValue)) {
                    selectedIndex = i;
                }
            }
            dialog.setItemsTitles(items);
            dialog.setSelectedIndex(selectedIndex);

            dialog.show(getSupportFragmentManager(), dialogTag);
            dialog.setDialogItemClickListener(which -> {
                MyApp.getInstance().prefs.edit().putInt(Prefs.PREF_FIRST_DAY_OF_WEEK, Integer.parseInt(PreferencesHelper.getFirstDayOfWeekOptions().get(which).key)).apply();
                Static.sendBroadcastToRedrawCalendar();
                setFirstDayOfWeekSummary();
            });
        }
    }


    private void showEntryDateStyleSelectDialog() {
        String dialogTag = Static.DIALOG_SELECT_SETTINGS_ENTRY_DATE_STYLE;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            OptionsDialog dialog = new OptionsDialog();
            dialog.setTitle(getString(R.string.settings_entry_date_style));

            // Set selected value
            String selectedValue = String.valueOf(MyApp.getInstance().prefs.getInt(Prefs.PREF_ENTRY_DATE_STYLE, Prefs.ENTRY_DATE_STYLE_LARGE));
            int selectedIndex = 0;

            ArrayList<KeyValuePair> options = PreferencesHelper.getEntryDateStyleOptions(getApplicationContext());

            ArrayList<String> items = new ArrayList<>();
            for (int i = 0; i < options.size(); i++) {
                KeyValuePair option = options.get(i);
                items.add(option.value);
                if (option.key.equals(selectedValue)) {
                    selectedIndex = i;
                }
            }
            dialog.setItemsTitles(items);
            dialog.setSelectedIndex(selectedIndex);

            dialog.show(getSupportFragmentManager(), dialogTag);
            dialog.setDialogItemClickListener(which -> {
                MyApp.getInstance().prefs.edit().putInt(Prefs.PREF_ENTRY_DATE_STYLE, Integer.parseInt(PreferencesHelper.getEntryDateStyleOptions(getApplicationContext()).get(which).key)).apply();
                setEntryDateStyleSummary();
            });
        }
    }

    private void showTextSizeSelectDialog() {
        String dialogTag = Static.DIALOG_SELECT_SETTINGS_TEXT_SIZE;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            OptionsDialog dialog = new OptionsDialog();
            dialog.setTitle(getString(R.string.settings_text_size));

            // Set selected value
            String selectedValue = String.valueOf(MyApp.getInstance().prefs.getInt(Prefs.PREF_TEXT_SIZE, Prefs.SIZE_NORMAL));
            int selectedIndex = 0;

            ArrayList<KeyValuePair> options = PreferencesHelper.getTextSizeOptions(getApplicationContext());

            ArrayList<String> items = new ArrayList<>();
            for (int i = 0; i < options.size(); i++) {
                KeyValuePair option = options.get(i);
                items.add(option.value);
                if (option.key.equals(selectedValue)) {
                    selectedIndex = i;
                }
            }
            dialog.setItemsTitles(items);
            dialog.setSelectedIndex(selectedIndex);

            dialog.show(getSupportFragmentManager(), dialogTag);
            dialog.setDialogItemClickListener(which -> {
                MyApp.getInstance().prefs.edit().putInt(Prefs.PREF_TEXT_SIZE, Integer.parseInt(PreferencesHelper.getTextSizeOptions(getApplicationContext()).get(which).key)).apply();
                setTextSizeSummary();
            });
        }
    }

    private void showMapTypeSelectDialog() {
        String dialogTag = Static.DIALOG_SELECT_SETTINGS_MAP_TYPE;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            OptionsDialog dialog = new OptionsDialog();
            dialog.setTitle(getString(R.string.settings_map_type));

            // Set selected value
            String selectedValue = String.valueOf(PreferencesHelper.getMapType());
            int selectedIndex = 0;

            ArrayList<KeyValuePair> options = PreferencesHelper.getMapTypeOptions(getApplicationContext());

            ArrayList<String> items = new ArrayList<>();
            for (int i = 0; i < options.size(); i++) {
                KeyValuePair option = options.get(i);
                items.add(option.value);
                if (option.key.equals(selectedValue)) {
                    selectedIndex = i;
                }
            }
            dialog.setItemsTitles(items);
            dialog.setSelectedIndex(selectedIndex);

            dialog.show(getSupportFragmentManager(), dialogTag);

            dialog.setDialogItemClickListener(which -> {
                MyApp.getInstance().prefs.edit().putInt(Prefs.PREF_MAP_TYPE, Integer.parseInt(PreferencesHelper.getMapTypeOptions(getApplicationContext()).get(which).key)).apply();
                setMapTypeSummary();
            });

        }
    }


    private void setLocalePrefSummary() {
        Preference preference = myPreferenceFragment.findPreference("LOCALE");
        if (preference != null) {
            preference.setSummary(PreferencesHelper.getCurrentLocaleAsLanguage(this));
        }
    }

    private void setThemePrefSummary() {
        Preference preference = myPreferenceFragment.findPreference("UI_THEME");
        if (preference != null) {
            preference.setSummary(PreferencesHelper.getThemeString());
        }
    }

    private void setDisplayDensitySummary() {
        Preference preference = myPreferenceFragment.findPreference("DISPLAY_DENSITY");
        if (preference != null) {
            preference.setSummary(PreferencesHelper.getDisplayDensityString());
        }
    }

    private void setUnitsPrefSummary() {
        Preference preference = myPreferenceFragment.findPreference("UNITS");
        if (preference != null) {
            if (PreferencesHelper.isPrefUnitFahrenheit()) {
                preference.setSummary(WeatherHelper.STRING_FAHRENHEIT);
            } else {
                preference.setSummary(WeatherHelper.STRING_CELSIUS);
            }
        }
    }

    private void setFontPrefSummary() {
        Preference preference = myPreferenceFragment.findPreference("FONT");
        if (preference != null) {
            preference.setSummary(PreferencesHelper.getPrefFont());
        }
    }

    private void setTextSizeSummary() {
        Preference preference = myPreferenceFragment.findPreference("TEXT_SIZE");
        if (preference != null) {
            preference.setSummary(PreferencesHelper.getTextSizeString());
        }
    }

    private void setFirstDayOfWeekSummary() {
        Preference preference = myPreferenceFragment.findPreference("FIRST_DAY_OF_WEEK");
        if (preference != null) {
            preference.setSummary(PreferencesHelper.getFirstDayOfWeekString());
        }
    }

    private void setEntryDateStyleSummary() {
        Preference preference = myPreferenceFragment.findPreference("ENTRY_DATE_STYLE");
        if (preference != null) {
            preference.setSummary(PreferencesHelper.getEntryDateStyleString());
        }
    }

    private void setMapTypeSummary() {
        Preference preference = myPreferenceFragment.findPreference("MAP_TYPE");
        if (preference != null) {
            preference.setSummary(PreferencesHelper.getMapTypeString());
        }
    }

}
