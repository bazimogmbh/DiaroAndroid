package com.pixelcrater.Diaro.activitytypes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.pixelcrater.Diaro.main.ActivityState;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.settings.MyPreferenceFragment;
import com.pixelcrater.Diaro.utils.WindowInsetsUtils;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

public class TypePreferenceActivity extends AppCompatActivity {

    public ActivityState activityState;
    public MyPreferenceFragment myPreferenceFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        setTheme(MyThemesUtils.getStyleResId());

        super.onCreate(savedInstanceState);

        ViewGroup toolbarLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.toolbar_layout, null);
        setSupportActionBar((Toolbar) toolbarLayout.findViewById(R.id.toolbar));

        activityState = new ActivityState(this, savedInstanceState);
        activityState.setupActionBar(getSupportActionBar());
        activityState.setActionBarTitle(getSupportActionBar(), getString(R.string.settings));

        ViewGroup contentContainer = (ViewGroup) toolbarLayout.findViewById(R.id.content);
        getLayoutInflater().inflate(R.layout.settings, contentContainer);

        setContentView(toolbarLayout);
        activityState.setLayoutBackground();

        // Handle bottom insets for edge-to-edge on Android 15+
        applyBottomInsets(findViewById(R.id.settings_frame));

        // Create FragmentSettings
        myPreferenceFragment = new MyPreferenceFragment();

        getSupportFragmentManager().beginTransaction().replace(R.id.settings_frame, myPreferenceFragment).commit();
    }

    protected void applyBottomInsets(View view) {
        WindowInsetsUtils.applyBottomInsets(view);
    }

    @Override
    protected void onUserLeaveHint() {
        activityState.onUserLeaveHint();
        super.onUserLeaveHint();
    }

    @Override
    protected void onPause() {
        activityState.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        activityState.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        activityState.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        activityState.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        activityState.onNewIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        activityState.onStart();
        super.onStart();
    }

    @Override
    protected void onResume() {
        activityState.onResume();
        super.onResume();
    }

}
