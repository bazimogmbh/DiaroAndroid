package com.pixelcrater.Diaro.settings;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.MenuItem;

import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;

import com.permissionx.guolindev.PermissionX;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.activitytypes.TypePreferenceActivity;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

import cn.nekocode.badge.BadgeDrawable;

public class SettingsActivity extends TypePreferenceActivity implements Preference.OnPreferenceClickListener {

    private boolean resultRestart = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        resultRestart = extras.getBoolean("resultRestart");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Add preferences from XML
        myPreferenceFragment.addPreferencesFromResource(R.xml.preferences);

        // Set actions to preferences
        setupPreference("GROUP_PREFERENCES");
        setupPreference("GROUP_DATA");
        setupPreference("TIME_TO_WRITE_NOTIFICATION");
        setupPreference("GROUP_SUPPORT");

        updateTimeToWriteNotificationPreference();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (activityState.isActivityPaused)
            return true;

        if (item.getItemId() == android.R.id.home) {
            exitSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
//		super.onBackPressed();
        exitSettings();
    }

    private void exitSettings() {
        Intent i = new Intent();
        i.putExtra("resultRestart", resultRestart);
        setResult(RESULT_CANCELED, i);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AppLog.d("requestCode: " + requestCode + ", resultCode: " + resultCode + ", data: " + data);

        switch (requestCode) {
            // Result from sign in activity
            case Static.REQUEST_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    Static.startProfileActivity(SettingsActivity.this, activityState);
                }
                break;

            // Result from settings group activity
            case Static.REQUEST_SETTINGS_GROUP:
                if (data != null) {
                    Bundle extras = data.getExtras();
                    resultRestart = extras.getBoolean("resultRestart");
                }

                AppLog.d("resultRestart: " + resultRestart);

                if (resultRestart) {
                    if (resultCode == RESULT_FIRST_USER) {
                        Intent intent = new Intent(SettingsActivity.this, SettingsPreferencesGroupActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        intent.putExtra(Static.EXTRA_SKIP_SC, true);
                        intent.putExtra("resultRestart", true);
                        startActivityForResult(intent, Static.REQUEST_SETTINGS_GROUP);
                    } else {
                        // Finish current dialog with result to restart again from parent activity
                        Intent i = new Intent();
                        i.putExtra("resultRestart", true);
                        setResult(RESULT_FIRST_USER, i);
                        finish();
                    }
                }
                break;

            // Result from time to write notification activity
            case Static.REQUEST_SETTINGS_SET_TIME_TO_WRITE_NOTIFICATION:
                updateTimeToWriteNotificationPreference();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupPreference("PROFILE");
        setupPreference("PRO_VERSION");
    }

    public void setPrefIcon(Preference preference, String drawableName) {
        preference.setIcon(MyThemesUtils.getDrawableResId(drawableName));
    }

    private void setupPreference(String key) {
        Preference preference = myPreferenceFragment.findPreference(key);
        preference.setOnPreferenceClickListener(this);

        switch (key) {
            case "PROFILE":
                setPrefIcon(preference, "ic_profile_%s_36dp");

                BadgeDrawable drawable = new BadgeDrawable.Builder().type(BadgeDrawable.TYPE_ONLY_ONE_TEXT).badgeColor(MyThemesUtils.getAccentColor()).build();

                if (MyApp.getInstance().userMgr.isSignedIn()) {
                    String summary= getResources().getString(R.string.signed_in) + ": " + MyApp.getInstance().userMgr.getSignedInEmail();
                    preference.setSummary(summary);
                    //SpannableString spannableStringSummary = new SpannableString(TextUtils.concat(summary, " ", drawable.toSpannable(), " "));
                } else {
                    preference.setSummary(R.string.profile_summary);
                }

                if (Static.isProUser()) {
                    String text = "PRO";
                    if(Static.isSubscribedCurrently())
                        text = "PREMIUM";
                    drawable.setText1(text);
                } else {
                    drawable.setText1("BASIC");
                }

                SpannableString spannableString = new SpannableString(TextUtils.concat(getResources().getString(R.string.profile), "  ", drawable.toSpannable(), " "));
                preference.setTitle(spannableString);

                break;

            case "PRO_VERSION":
                setPrefIcon(preference, "ic_upgrade_%s_36dp");

                if (Static.isProUser()) {
                    preference.setTitle(R.string.diaro_pro_version);
                    if(Static.isSubscribedCurrently() || Static.isPlayNboSubscription() )
                         preference.setSummary(R.string.pro_version_active);
                    else
                        preference.setSummary(R.string.pro_summary);
                } else {
                    preference.setTitle(R.string.get_diaro_pro);
                    preference.setSummary(R.string.pro_summary);
                }
                break;

            case "GROUP_PREFERENCES":
                setPrefIcon(preference, "ic_settings3_%s_36dp");
                break;

            case "GROUP_DATA":
                setPrefIcon(preference, "ic_file_cloud_%s_36dp");
                break;

            case "TIME_TO_WRITE_NOTIFICATION":
                setPrefIcon(preference, "ic_alarm_clock_%s_36dp");
                break;

            case "GROUP_SUPPORT":
                setPrefIcon(preference, "ic_help_%s_36dp");
                break;
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (activityState.isActivityPaused) {
            return false;
        }
        String key = preference.getKey();

        switch (key) {
            // Open profile activity
            case "PROFILE": {
                if (MyApp.getInstance().userMgr.isSignedIn()) {
                    Static.startProfileActivity(SettingsActivity.this, activityState);
                } else {
                    Static.startSignInActivity(SettingsActivity.this, activityState);
                }
                break;
            }
            // Open get PRO activity
            case "PRO_VERSION": {
                Static.startProActivity(SettingsActivity.this, activityState);
                break;
            }
            // Open PREFERENCES group
            case "GROUP_PREFERENCES": {
                Intent intent = new Intent(this, SettingsPreferencesGroupActivity.class);
                intent.putExtra(Static.EXTRA_SKIP_SC, true);
                startActivityForResult(intent, Static.REQUEST_SETTINGS_GROUP);
                break;
            }
            // Open DATA group
            case "GROUP_DATA": {
                Intent intent = new Intent(this, SettingsDataGroupActivity.class);
                intent.putExtra(Static.EXTRA_SKIP_SC, true);
                startActivityForResult(intent, Static.REQUEST_SETTINGS_GROUP);
                break;
            }
            // Open TIME_TO_WRITE_NOTIFICATION activity
            case "TIME_TO_WRITE_NOTIFICATION": {
                askNotificationPermission();
                updateTimeToWriteNotificationPreference();

                Intent intent = new Intent(this, TimeToWriteNotificationActivity.class);
                intent.putExtra(Static.EXTRA_SKIP_SC, true);
                startActivityForResult(intent, Static.REQUEST_SETTINGS_SET_TIME_TO_WRITE_NOTIFICATION);
                break;
            }
            // Open SUPPORT group
            case "GROUP_SUPPORT": {
                Intent intent = new Intent(this, SettingsSupportGroupActivity.class);
                intent.putExtra(Static.EXTRA_SKIP_SC, true);
                startActivityForResult(intent, Static.REQUEST_SETTINGS_GROUP);
                break;
            }
        }

        return false;
    }

    private void updateTimeToWriteNotificationPreference() {
        CheckBoxPreference preference = (CheckBoxPreference) myPreferenceFragment.findPreference("TIME_TO_WRITE_NOTIFICATION");
        preference.setChecked(MyApp.getInstance().prefs.getBoolean(Prefs.PREF_TIME_TO_WRITE_NOTIFICATION_ENABLED, true));
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionX.init(this)
                    .permissions(Manifest.permission.POST_NOTIFICATIONS)
                    .request((allGranted, grantedList, deniedList) -> AppLog.e("NotificationPermission granted"));
        }

    }
}
