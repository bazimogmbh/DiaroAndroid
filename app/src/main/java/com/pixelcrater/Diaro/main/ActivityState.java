package com.pixelcrater.Diaro.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.appcompat.app.ActionBar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.config.AppConfig;
import com.pixelcrater.Diaro.securitycode.SecurityCodeActivity;
import com.pixelcrater.Diaro.settings.PreferencesHelper;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.Static;

import java.lang.reflect.Field;

public class ActivityState {

    public Activity mActivity;
    public boolean isActivityPaused = false;
    public boolean startedFromWidget;
    public boolean isBannerAllowed;
    private ViewGroup adsViewGroup;
    private LayoutInflater inflater;
    private AdView mAdView;

    private FirebaseAnalytics mFirebaseAnalytics;

    public ActivityState(Activity activity, Bundle savedInstanceState) {
        mActivity = activity;

        onCreate(savedInstanceState);
    }

    public void onCreate(Bundle savedInstanceState) {
        AppLog.d("getLocalClassName(): " + mActivity.getLocalClassName() + ", savedInstanceState: " + savedInstanceState);

        // Get intent extras
        Bundle extras = mActivity.getIntent().getExtras();

        if (extras != null) {
            startedFromWidget = extras.getBoolean("widget");
        }

        if (!isSecurityCodeActivity()) {
            if (savedInstanceState == null && extras != null && extras.getBoolean(Static.EXTRA_SKIP_SC)) {
                // AppLog.d("extras.keySet(): " + extras.keySet());
                MyApp.getInstance().securityCodeMgr.setUnlocked();
            }
        }

        // Refresh locale
        //  Static.refreshLocale();

        if (!PreferencesHelper.isScreenshotEnabled()) {
            mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mActivity);
    }

    public void logAnalyticsEvent(String event) {
        if (mFirebaseAnalytics != null)
            mFirebaseAnalytics.logEvent(event, new Bundle());
    }

    public void setLayoutBackground() {
        mActivity.findViewById(R.id.layout_container).setBackgroundResource(MyThemesUtils.getBackgroundColorResId());
    }

    public void setLayoutBackgroundGray() {
        mActivity.findViewById(R.id.layout_container).setBackgroundResource(MyThemesUtils.getBackgroundColorResIdMainView());
    }

    public void onUserLeaveHint() {
        AppLog.d("getLocalClassName(): " + mActivity.getLocalClassName() + ", isFinishing(): " + mActivity.isFinishing());
        MyApp.getInstance().securityCodeMgr.setPostDelayedLock();
    }

    public void onPause() {
        AppLog.d("getLocalClassName(): " + mActivity.getLocalClassName() + ", isFinishing(): " + mActivity.isFinishing());
        isActivityPaused = true;
        MyApp.getInstance().pauseUsingApp();

        if (!isSecurityCodeActivity()) {
            // Used when device screen is locked (onUserLeaveHint() isn't called)
            MyApp.getInstance().securityCodeMgr.setPostDelayedLock();
        }
    }

    public void onStop() {
        AppLog.d("getLocalClassName(): " + mActivity.getLocalClassName() + ", isFinishing(): " + mActivity.isFinishing());

        if (!AppConfig.isDeveloperMode()) {
            // Stop the analytics tracking

        }
    }

    public void onDestroy() {
        AppLog.d("getLocalClassName(): " + mActivity.getLocalClassName());
        try {
            if (mAdView != null) {
                mAdView.destroy();
            }
        } catch (Exception ignored) {
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        AppLog.d("getLocalClassName(): " + mActivity.getLocalClassName() + ", requestCode: " + requestCode + ", resultCode: " + resultCode + ", data: " + data);

        isActivityPaused = false;

        if (!(requestCode == Static.REQUEST_SECURITY_CODE && resultCode == Activity.RESULT_OK) && requestCode != Static.REQUEST_SHARE_PHOTO) {
            MyApp.getInstance().securityCodeMgr.setUnlocked();
        }
    }

    public void onNewIntent(Intent intent) {
        AppLog.d("getLocalClassName(): " + mActivity.getLocalClassName() + ", intent: " + intent);
        MyApp.getInstance().securityCodeMgr.setLocked();
    }

    public void onStart() {
        AppLog.d("getLocalClassName(): " + mActivity.getLocalClassName());

        if (!AppConfig.isDeveloperMode()) {
            // Obtain the shared Tracker instance.
        }
    }

    public void onResume() {
        AppLog.d("getLocalClassName(): " + mActivity.getLocalClassName());

        isActivityPaused = false;

        if (!isSecurityCodeActivity()) {
            MyApp.getInstance().securityCodeMgr.clearPostDelayedLock();
            if (MyApp.getInstance().securityCodeMgr.isLocked()) {
                // Ask for security code
                showSecurityCodeActivity();
            }
        }

        MyApp.getInstance().resumeUsingApp();
    }

    private boolean isSecurityCodeActivity() {
        return mActivity instanceof SecurityCodeActivity && ((SecurityCodeActivity) mActivity).mode == SecurityCodeActivity.MODE_LOCK_SCREEN;
    }

    private void forceOverflowMenu() {

        try {
            ViewConfiguration config = ViewConfiguration.get(mActivity);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            // Ignore
        }

    }

    @SuppressLint("NewApi")
    public void setupActionBar(ActionBar actionBar) {
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Set action bar color
        actionBar.setBackgroundDrawable(new ColorDrawable(MyThemesUtils.getPrimaryColor()));

        mActivity.getWindow().setStatusBarColor(MyThemesUtils.getDarkColor(MyThemesUtils.getPrimaryColorCode()));

        // Change card color in running apps switcher (Recents)
        overrideOverviewCardStyle(MyThemesUtils.getPrimaryColor());

        // Force overflow menu on ICS
        forceOverflowMenu();
    }

    @SuppressLint("NewApi")
    private void overrideOverviewCardStyle(int color) {
        ActivityManager.TaskDescription td = new ActivityManager.TaskDescription(null, null, color);
        mActivity.setTaskDescription(td);
    }

    public void setActionBarTitle(ActionBar actionBar, int resId) {
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(resId);
    }

    public void setActionBarTitle(ActionBar actionBar, String title) {
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(title);
    }

    public void setActionBarSubtitle(ActionBar actionBar, String title) {
        actionBar.setSubtitle(title);
    }

    private void showSecurityCodeActivity() {
        if (AppConfig.SKIP_SECURITY_CODE) {
            return;
        }

        if (MyApp.getInstance().securityCodeMgr.isSecurityCodeSet()) {
            // Show security code activity
            Intent intent = new Intent(mActivity, SecurityCodeActivity.class);
            intent.putExtra(Static.EXTRA_SKIP_SC, true);
            intent.putExtra("mode", SecurityCodeActivity.MODE_LOCK_SCREEN);

            mActivity.startActivityForResult(intent, Static.REQUEST_SECURITY_CODE);
        }
    }

    public void showHideBanner() {
        AppLog.d("getLocalClassName(): " + mActivity.getLocalClassName() + ", PREMIUM: " + Static.isProUser());


        if (adsViewGroup == null) {
            adsViewGroup = (ViewGroup) mActivity.findViewById(R.id.ads);
        }
        if (inflater == null) {
            inflater = mActivity.getLayoutInflater();
        }

        if (!Static.isProUser() && MyApp.getInstance().networkStateMgr.isConnectedToInternet()) {
            isBannerAllowed = true;
            loadGoogleAd();
        } else {
            hideBanner();
            isBannerAllowed = false;
        }
    }

    public void hideBanner() {
        AppLog.d("getLocalClassName(): " + mActivity.getLocalClassName());

        if (adsViewGroup == null) {
            adsViewGroup = (ViewGroup) mActivity.findViewById(R.id.ads);
        }
        // adsViewGroup.removeAllViews();
        adsViewGroup.setVisibility(View.GONE);
    }

    private void loadGoogleAd() {
        if (adsViewGroup.findViewById(R.id.adview) == null) {
            inflater.inflate(R.layout.google_banner, adsViewGroup);
        }

        try {
            if (adsViewGroup.findViewById(R.id.adview) == null) {
                inflater.inflate(R.layout.google_banner, adsViewGroup);
            }

            mAdView = adsViewGroup.findViewById(R.id.adview);
            if (isBannerAllowed) {
                adsViewGroup.setVisibility(View.VISIBLE);
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
            }

        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }
    }

    public void restart() {
        Intent intent = mActivity.getIntent();
        mActivity.finish();
        mActivity.startActivity(intent);
    }

}
