package com.pixelcrater.Diaro.activitytypes;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.pixelcrater.Diaro.main.ActivityState;
import com.pixelcrater.Diaro.main.ContentFragment;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.stats.StatsActivity;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.sidemenu.SidemenuFragment;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.WindowInsetsUtils;

public class TypeSlidingActivity extends AppCompatActivity {

    public SidemenuFragment sidemenuFragment;
    public ViewGroup contentFrame;
    public ViewGroup menuFrame;
    public DrawerLayout drawerLayout;
    public ActivityState activityState;
    public boolean drawerOpen = false;
    public boolean isStartingSearch;

    public Fragment currentFragment = null;
    public ContentFragment contentFragment;

    private ActionBarDrawerToggle drawerToggle;

    private FirebaseAnalytics mFirebaseAnalytics;

   public Toolbar mToolbar ;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        try {
            setTheme(MyThemesUtils.getStyleResId());
        }catch(Exception ignored) {
        }

        super.onCreate(savedInstanceState);

        ViewGroup toolbarLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.toolbar_layout, null);

        mToolbar =  (Toolbar) toolbarLayout.findViewById(R.id.toolbar);

        try {
            setSupportActionBar(mToolbar);
        }catch(Exception ignored) {
        }

        activityState = new ActivityState(TypeSlidingActivity.this, savedInstanceState);
        activityState.setupActionBar(getSupportActionBar());
        activityState.setActionBarTitle(getSupportActionBar(), getString(R.string.entries));

        ViewGroup contentContainer = (ViewGroup) toolbarLayout.findViewById(R.id.content);
        getLayoutInflater().inflate(R.layout.main_responsive, contentContainer);

        setContentView(toolbarLayout);

        if (findViewById(R.id.drawer_layout) != null) {
            drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        }

        // Main content
        contentFrame = (ViewGroup) findViewById(R.id.content_frame);
        contentFrame.setBackgroundResource(MyThemesUtils.getBackgroundColorResIdMainView());

        // Side menu
        menuFrame = (ViewGroup) findViewById(R.id.menu_frame);
        menuFrame.setBackgroundResource(MyThemesUtils.getBackgroundColorResId());

        if (drawerLayout == null) {
            // Disable ActionBar app icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
        } else {
            // Enable ActionBar app icon to behave as action to toggle nav drawer
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);

            try {
                // Set a custom shadow that overlays the main content when the drawer opens
                drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
            } catch (Exception ignored) {
            }
            drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close) {
                @Override
                public void onDrawerStateChanged(int newState) {
                    super.onDrawerStateChanged(newState);
                    drawerOpen = (!(newState == DrawerLayout.STATE_IDLE && !drawerLayout.isDrawerOpen(menuFrame)));
//                    AppLog.d("newState: " + newState + ", drawerOpen: " + drawerOpen  + ", isStartingSearch: " + isStartingSearch);
                    if (drawerOpen) {
                        if (isStartingSearch) {
                            Static.showSoftKeyboard(sidemenuFragment.searchEditText);
                        }
                    } else {
                        // Hide keyboard
                        Static.hideSoftKeyboard(sidemenuFragment.searchEditText);
                        isStartingSearch = false;
                    }
                    supportInvalidateOptionsMenu();
                }

            };

            // Set the drawer toggle as the DrawerListener
            drawerLayout.addDrawerListener(drawerToggle);
        }

        // Add fragments
        if (savedInstanceState != null) {

           // Static.showToastLong( "savedinstance was not null");
            sidemenuFragment = (SidemenuFragment) getSupportFragmentManager().findFragmentById( R.id.menu_frame);
            currentFragment =  getSupportFragmentManager().findFragmentById(R.id.content_frame);

            if(getSupportFragmentManager().findFragmentByTag("item_entires")!=null) {
                contentFragment = (ContentFragment) getSupportFragmentManager().findFragmentByTag("item_entires");
                currentFragment = contentFragment;
            }

            if(currentFragment == null) {
                // Create content Fragment, ideally this should not happen
                contentFragment = ContentFragment.newInstance();
                currentFragment = contentFragment;
                getSupportFragmentManager().beginTransaction().add(R.id.content_frame, contentFragment, "item_entires").commit();
            }

        } else {
            // Create side menu Fragment
            sidemenuFragment = SidemenuFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.menu_frame, sidemenuFragment).commit();
            // Create content Fragment
            contentFragment = ContentFragment.newInstance();
            currentFragment = contentFragment;
            getSupportFragmentManager().beginTransaction().add(R.id.content_frame, contentFragment, "item_entires").commit();
        }

        mFirebaseAnalytics =  FirebaseAnalytics.getInstance(this);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (drawerLayout != null) {
            // Sync the toggle state after onRestoreInstanceState has occurred.
            drawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (drawerLayout != null) {
            // Pass any configuration change to the drawer toggle
            drawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerLayout != null) {
            // Pass the event to ActionBarDrawerToggle, if it returns// true, then it has handled the app icon touch event

            if (drawerToggle.onOptionsItemSelected(item)) {
                return true;
            }
        }
        // Handle your other action bar items...
        return super.onOptionsItemSelected(item);
    }

    public void logAnalyticsEvent(String event) {
        if(mFirebaseAnalytics!=null)
            mFirebaseAnalytics.logEvent(event, new Bundle());
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

    // ---------------------- EDGE-TO-EDGE INSETS UTILITIES ----------------------

    protected void applyBottomInsets(View view) {
        WindowInsetsUtils.applyBottomInsets(view);
    }
}
