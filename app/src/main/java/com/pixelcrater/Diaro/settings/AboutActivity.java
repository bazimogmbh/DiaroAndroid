package com.pixelcrater.Diaro.settings;

import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.pixelcrater.Diaro.config.GlobalConstants;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.activitytypes.TypeActivity;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

public class AboutActivity extends TypeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(addViewToContentContainer(R.layout.about));
        activityState.setLayoutBackground();
        activityState.setActionBarTitle(getSupportActionBar(), getString(R.string.about_diaro));

        ((ImageView) findViewById(R.id.app_logo)).setImageResource(MyThemesUtils.getDrawableResId("ic_diaro_logo_%s_85"));

        TextView version = (TextView) findViewById(R.id.settings_app_version);
        version.setText(String.format("%s: %s (%d)", getString(R.string.version), Static.getAppVersionName(), Static.getAppVersionCode()));

        TextView aboutTextView = (TextView) findViewById(R.id.settings_about_text);
        aboutTextView.setText(Html.fromHtml(
                getString(R.string.website) + ": diaroapp.com<br/>" +
                        getString(R.string.email) + ": " + GlobalConstants.SUPPORT_EMAIL +"<br/>" +
                        "Facebook: facebook.com/DiaroApp<br/>" +
                        "Twitter: twitter.com/DiaroApp<br/>" +
                        "FAQ: diaroapp.com/faq<br/>" +
                        "Blog: diaroapp.com/blog<br/>" +
                        getString(R.string.copyright) + " © Pixel Crater Ltd."));
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
}
