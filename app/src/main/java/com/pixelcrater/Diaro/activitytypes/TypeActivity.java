package com.pixelcrater.Diaro.activitytypes;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.pixelcrater.Diaro.main.ActivityState;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.WindowInsetsUtils;

import java.util.Objects;

public class TypeActivity extends AppCompatActivity {

    public ActivityState activityState;
    public View toolbarLayout;
    public ViewGroup contentContainer;
    public boolean useCollapsingToolbar = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        try {
            setTheme(MyThemesUtils.getStyleResId());
        }catch(Exception ignored) {
        }
        super.onCreate(savedInstanceState);

        if (useCollapsingToolbar) {
            if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ENTRY_PHOTOS_POSITION, true)) {
                toolbarLayout = getLayoutInflater().inflate(R.layout.collapsing_toolbar_layout_no_elevation, null);
            } else {
                toolbarLayout = getLayoutInflater().inflate(R.layout.collapsing_toolbar_layout, null);
            }
            // Set status bar color for edge-to-edge
            int color = MyThemesUtils.getPrimaryColor();
            getWindow().setStatusBarColor(color);

            // Handle window insets for Android 15+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) { // Android 15+
                getWindow().getDecorView().setOnApplyWindowInsetsListener((view, insets) -> {
                    android.graphics.Insets statusBarInsets = insets.getInsets(WindowInsets.Type.statusBars());
                    view.setBackgroundColor(color);

                    // Adjust padding to avoid overlap
                    view.setPadding(0, statusBarInsets.top, 0, 0);
                    return insets;
                });
            }
        } else {
            toolbarLayout = getLayoutInflater().inflate(R.layout.toolbar_layout, null);
        }

        contentContainer = (ViewGroup)toolbarLayout.findViewById(R.id.content);

        if (contentContainer instanceof NestedScrollView) {
            ((NestedScrollView) contentContainer).setFillViewport(true);
        }

        Toolbar toolbar = (Toolbar) toolbarLayout.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        activityState = new ActivityState(TypeActivity.this, savedInstanceState);
        activityState.setupActionBar(Objects.requireNonNull(getSupportActionBar()));
    }

    // Inflates given view and returns toolbar layout for setContentView()
    public View addViewToContentContainer(int resId) {
        getLayoutInflater().inflate(resId, contentContainer);
        return toolbarLayout;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (activityState.isActivityPaused) {
            return true;
        }

        // Back
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addVectorIconToMenu(MenuItem menuItem, int resId) {
        VectorDrawableCompat itemAddPhotoVector = VectorDrawableCompat.create(getResources(), resId, null);
        menuItem.setIcon(itemAddPhotoVector);
    }

    // ---------------------- EDGE-TO-EDGE INSETS UTILITIES ----------------------
    // Delegate to WindowInsetsUtils for reusability across the app

    protected void applyBottomInsets(View view) {
        WindowInsetsUtils.applyBottomInsets(view);
    }

    protected void applyBottomInsetsWithPadding(View view, int additionalPaddingDp) {
        WindowInsetsUtils.applyBottomInsetsWithPadding(view, additionalPaddingDp);
    }

    protected void applyBottomInsetsAsMargin(View view, int additionalMarginDp) {
        WindowInsetsUtils.applyBottomInsetsAsMargin(view, additionalMarginDp);
    }

    protected void applyTopInsetsAsMargin(View view, int additionalMarginDp) {
        WindowInsetsUtils.applyTopInsetsAsMargin(view, additionalMarginDp);
    }

    protected void applyKeyboardAndBottomInsets(View view) {
        WindowInsetsUtils.applyKeyboardAndBottomInsets(view);
    }
}
