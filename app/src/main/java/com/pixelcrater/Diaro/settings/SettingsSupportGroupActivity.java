package com.pixelcrater.Diaro.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.preference.Preference;

import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.activitytypes.TypePreferenceActivity;
import com.pixelcrater.Diaro.licenses.LicensesDialog;
import com.pixelcrater.Diaro.utils.GeneralUtils;

public class SettingsSupportGroupActivity extends TypePreferenceActivity implements Preference.OnPreferenceClickListener {

    private final String PROBLEM_REPORT = "PROBLEM_REPORT";
    private final String RATE_APP = "RATE_APP";
    private final String RECOMMEND_TO_FRIEND = "RECOMMEND_TO_FRIEND";
    private final String LICENSES = "LICENSES";
    private final String ABOUT = "ABOUT";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityState.setActionBarTitle(getSupportActionBar(), R.string.settings_support);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Add preferences from XML
        myPreferenceFragment.addPreferencesFromResource(R.xml.preferences_support);

        setupPreference(PROBLEM_REPORT);
        setupPreference(RATE_APP);
        setupPreference(RECOMMEND_TO_FRIEND);
        setupPreference(ABOUT);
        setupPreference(LICENSES);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (activityState.isActivityPaused)
            return true;

        switch (item.getItemId()) {
            // Back
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupPreference(String key) {
        Preference preference = (Preference) myPreferenceFragment.findPreference(key);
        preference.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();

        switch (key) {
            // Problem report
            case PROBLEM_REPORT: {
                GeneralUtils.sendSupportEmail(this);
                break;
            }
            // Open Diaro app window in Google Play
            case RATE_APP: {
                GeneralUtils.openMarket(SettingsSupportGroupActivity.this);
                break;
            }
            // Recommend to a friend
            case RECOMMEND_TO_FRIEND: {
                GeneralUtils.recommendToFriend(SettingsSupportGroupActivity.this);
                break;
            }
            // Open About activity
            case ABOUT: {
                Intent intent = new Intent(this, AboutActivity.class);
                intent.putExtra(Static.EXTRA_SKIP_SC, true);
                startActivityForResult(intent, Static.REQUEST_ABOUT);
                break;
            }
            // Open Licenses dialog
            case LICENSES: {
                showLicensesDialog();
                break;
            }
        }

        return false;
    }

    public void showLicensesDialog() {
        String dialogTag = Static.DIALOG_LICENSES;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            LicensesDialog dialog = new LicensesDialog();
            dialog.show(getSupportFragmentManager(), dialogTag);
        }
    }

}
