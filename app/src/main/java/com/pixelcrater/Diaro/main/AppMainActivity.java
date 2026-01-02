package com.pixelcrater.Diaro.main;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ActionMode;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryProductDetailsResult;
import com.android.billingclient.api.QueryPurchasesParams;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.activitytypes.TypeSlidingActivity;
import com.pixelcrater.Diaro.analytics.AnalyticsConstants;
import com.pixelcrater.Diaro.asynctasks.CheckIfAllEntriesExistAsync;
import com.pixelcrater.Diaro.asynctasks.SelectAllEntriesAsync;
import com.pixelcrater.Diaro.atlas.AtlasFragment;
import com.pixelcrater.Diaro.config.GlobalConstants;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.entries.async.DeleteEntriesAsync;
import com.pixelcrater.Diaro.entries.async.UndoArchiveEntriesAsync;
import com.pixelcrater.Diaro.entries.viewedit.EntryViewEditActivity;
import com.pixelcrater.Diaro.folders.FolderAddEditActivity;
import com.pixelcrater.Diaro.gallery.activities.MediaFragment;
import com.pixelcrater.Diaro.generaldialogs.ConfirmDialog;
import com.pixelcrater.Diaro.locations.LocationAddEditActivity;
import com.pixelcrater.Diaro.moods.MoodsAddEditActivity;
import com.pixelcrater.Diaro.premium.PremiumActivity;
import com.pixelcrater.Diaro.premium.billing.PaymentUtils;
import com.pixelcrater.Diaro.premium.billing.Security;
import com.pixelcrater.Diaro.profile.ProfileActivity;
import com.pixelcrater.Diaro.profile.UserMgr;
import com.pixelcrater.Diaro.settings.PreferencesHelper;
import com.pixelcrater.Diaro.settings.SettingsActivity;
import com.pixelcrater.Diaro.sidemenu.SidemenuFragment;
import com.pixelcrater.Diaro.stats.StatsFragment;
import com.pixelcrater.Diaro.storage.dropbox.DbxUserInfo;
import com.pixelcrater.Diaro.storage.dropbox.DropboxAccountManager;
import com.pixelcrater.Diaro.storage.dropbox.DropboxAccountManager.GetCurrentUserAsyncTask;
import com.pixelcrater.Diaro.storage.sqlite.SQLiteQueryHelper;
import com.pixelcrater.Diaro.tags.TagAddEditActivity;
import com.pixelcrater.Diaro.utils.AppLaunchHelper;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.GeneralUtils;
import com.pixelcrater.Diaro.utils.Static;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import berlin.volders.badger.BadgeShape;
import berlin.volders.badger.Badger;
import berlin.volders.badger.CountBadge;

import static com.pixelcrater.Diaro.BuildConfig.DEBUG;
import static com.pixelcrater.Diaro.utils.Static.EXTRA_SKIP_SC;
import static com.pixelcrater.Diaro.utils.Static.REQUEST_GET_PRO;

public class AppMainActivity extends TypeSlidingActivity implements SidemenuFragment.OnFragmentInteractionListener, ContentFragment.OnFragmentInteractionListener,
        SelectAllEntriesAsync.OnAsyncInteractionListener, CheckIfAllEntriesExistAsync.OnAsyncInteractionListener, SharedPreferences.OnSharedPreferenceChangeListener, PurchasesUpdatedListener, ProductDetailsResponseListener {

    private static final IntentFilter actionTimeIntentFilter;
    private static final String TAG = "AppMainActivity";

    private BillingClient mBillingClient;

    BottomNavigationView bottom_navigation;

    static {
        actionTimeIntentFilter = new IntentFilter();
        actionTimeIntentFilter.addAction(Intent.ACTION_TIME_TICK);
        actionTimeIntentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        actionTimeIntentFilter.addAction(Intent.ACTION_TIME_CHANGED);
    }

    private final BroadcastReceiver timeChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            // AppLog.d("action: " + action);
            if (action.equals(Intent.ACTION_TIME_CHANGED) || action.equals(Intent.ACTION_TIMEZONE_CHANGED) || action.equals(Intent.ACTION_TIME_TICK)) {
                DateTime dt = new DateTime().withTimeAtStartOfDay();
                // If day has changed
                if (sidemenuFragment != null && dt.getMillis() != sidemenuFragment.calendarView.getTodayDt().getMillis()) {
                    sidemenuFragment.calendarView.drawDayCells();
                }
            }
        }
    };

    private BroadcastReceiver brReceiver = new BrReceiver();
    private ActionMode actionMode;
    private int proPurchaseYear = 2020;

    FragmentTransaction fragmentTransaction;
    private int menuToChoose = R.menu.menu_main;

    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = item -> {
        int itemId = item.getItemId();

        if (itemId == R.id.item_entires) {
            activityState.setActionBarTitle(Objects.requireNonNull(getSupportActionBar()), R.string.entries);
            menuToChoose = R.menu.menu_main;
            invalidateOptionsMenu();

            currentFragment = getSupportFragmentManager().findFragmentByTag("item_entires");
            if (currentFragment == null) {
                AppLog.e("currentFragment was null");
                currentFragment = new ContentFragment();
            } else
                AppLog.e("currentFragment was found");

            contentFragment = (ContentFragment) currentFragment;

            fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, currentFragment, "item_entires");
            fragmentTransaction.addToBackStack("item_entires");
            fragmentTransaction.commit();
            return true;
        }
        else if (itemId == R.id.item_stats) {
            if (actionMode != null) {
                finishActionMode();
            }

            activityState.setActionBarTitle(Objects.requireNonNull(getSupportActionBar()), R.string.stats);
            menuToChoose = R.menu.menu_share;
            invalidateOptionsMenu();

            currentFragment = getSupportFragmentManager().findFragmentByTag("item_stats");
            if (currentFragment == null)
                currentFragment = new StatsFragment();
            fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, currentFragment, "item_stats");
            fragmentTransaction.addToBackStack("item_stats");
            fragmentTransaction.commit();

            logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_FRAGMENT_STATS);
            return true;
        }
        else if (itemId == R.id.item_atlas) {
            if (actionMode != null) {
                finishActionMode();
            }

            mToolbar.getMenu().clear();

            activityState.setActionBarTitle(Objects.requireNonNull(getSupportActionBar()), R.string.atlas);
            currentFragment = getSupportFragmentManager().findFragmentByTag("item_atlas");
            if (currentFragment == null)
                currentFragment = new AtlasFragment();
            fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, currentFragment, "item_atlas");
            fragmentTransaction.addToBackStack("item_atlas");
            fragmentTransaction.commit();

            logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_FRAGMENT_ATLAS);
            return true;
        }
        else if (itemId == R.id.item_photos) {

            if (actionMode != null) {
                finishActionMode();
            }

            mToolbar.getMenu().clear();
            activityState.setActionBarTitle(Objects.requireNonNull(getSupportActionBar()), R.string.photos);

            currentFragment = getSupportFragmentManager().findFragmentByTag("item_photos");
            if (currentFragment == null) {
                AppLog.e("currentFragment was null");
                currentFragment = new MediaFragment();
            } else
                AppLog.e("currentFragment was found");

            fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.content_frame, currentFragment, "item_photos");
            fragmentTransaction.addToBackStack("item_photos");
            fragmentTransaction.commit();

            logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_FRAGMENT_MEDIA);
            return true;
        }


        return false;
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bottom_navigation = findViewById(R.id.bottom_navigation);
        bottom_navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if (!PreferencesHelper.isBottomTabEnabled())
            bottom_navigation.setVisibility(View.GONE);

        // Handle bottom navigation insets for Android 15+ edge-to-edge
        applyBottomInsets(bottom_navigation);

        // Show ask to rate and follow us on Facebook dialogs
        showPromotionalDialogs();

        ContextCompat.registerReceiver(this, brReceiver, new IntentFilter(Static.BR_IN_MAIN), ContextCompat.RECEIVER_NOT_EXPORTED);

        restoreDialogListeners(savedInstanceState);
        ContextCompat.registerReceiver(this, timeChangedReceiver, actionTimeIntentFilter, ContextCompat.RECEIVER_NOT_EXPORTED);

        // check after usage
        int resetAfterUsageCount = 8;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefsEditor = prefs.edit();

        String prefString = "__appusagecount__";
        int appUsageCount = prefs.getInt(prefString, 0);
        // appUsageCount = 0 ; // test
        if (appUsageCount == 0) {
            prefsEditor.putInt(prefString, ++appUsageCount).apply();
            //   checkDropboxUsage();
            checkProUserDropboxConnected();
            notifyBuyPro();
        } else {
            if (appUsageCount == resetAfterUsageCount) {
                prefsEditor.putInt(prefString, 0).apply();
            } else
                prefsEditor.putInt(prefString, ++appUsageCount).apply();
        }

        checkIAP();
        fetchRemoteConfig();

        if (appUsageCount == 2) {
            // if (!Static.isProUser())
            //   downloadMoney();

            if (!Static.isProUser()) {
                Intent intent = new Intent(this, PremiumActivity.class);
                intent.putExtra(EXTRA_SKIP_SC, true);
                startActivityForResult(intent, REQUEST_GET_PRO);
            }

        }

        MyApp.getInstance().prefs.registerOnSharedPreferenceChangeListener(this);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            AppLaunchHelper.changeBackupFilesDirOnce(AppMainActivity.this);
        }
    }

    private void fetchRemoteConfig() {
        FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        boolean updated = task.getResult();
                        AppLog.e("Config params updated: " + updated);
                    }
                });
    }


    private void notifyBuyPro() {
        // show buy pro & sync for non pro, signed in users
        if (!Static.isProUser() && MyApp.getInstance().userMgr.isSignedIn()) {
            int unsynced = SQLiteQueryHelper.getUnsyncedCount();
            String title = "";

            // TODO : Fix it
            try {
                title = getString(R.string.unsynced_entries_info, unsynced);
            } catch (Exception e) {
                title = getString(R.string.get_diaro_pro);
            }
            String msg = getString(R.string.upgrade_info);

            new BottomDialog.Builder(AppMainActivity.this).setTitle(title).setContent(msg).setPositiveText(R.string.get_diaro_pro)
                    .onPositive(dialog -> {
                        dialog.dismiss();
                        Intent intent = new Intent(AppMainActivity.this, ProfileActivity.class);
                        startActivity(intent);
                    }).show();
        }

    }

    // notify user about dropbox quota full
    private void checkDropboxUsage() {
        final int warningPercent = 95;
        if (DropboxAccountManager.isLoggedIn(this) && Static.isProUser()) {

            new GetCurrentUserAsyncTask(this) {
                @Override
                protected void onPostExecute(DbxUserInfo dbxUserInfo) {
                    super.onPostExecute(dbxUserInfo);
                    if (dbxUserInfo != null) {
                        double usage = dbxUserInfo.getUsagePercentage();

                        if (usage < warningPercent) {
                            return;
                        }

                        String title = "";
                        String text = "You are using " + dbxUserInfo.getUsageInfo() + ".\n" + "Please free up space in your dropbox to avoid data loss.";
                        if (usage >= warningPercent && usage < 100) {
                            title = "Dropbox Storage Almost Full!";
                        }
                        if (usage >= 100) {
                            title = getString(R.string.please_check_dropbox_quota);
                        }
                        new BottomDialog.Builder(AppMainActivity.this).setTitle(title).setContent(text).setPositiveText(android.R.string.ok).show();

                        logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_DROPBOX_FULL);
                    }
                }
            }.execute();
        }
    }

    private void checkProUserDropboxConnected() {
        if (Static.isProUser()) {
            if (!DropboxAccountManager.isLoggedIn(this)) {
                int unsynced = SQLiteQueryHelper.getUnsyncedCount();
                new BottomDialog.Builder(AppMainActivity.this).setTitle(getString(R.string.unsynced_entries_info, unsynced)).
                        setContent(R.string.cloud_backup_restore_description).
                        setPositiveText(R.string.settings_dropbox_connect)
                        .onPositive(dialog -> {
                            dialog.dismiss();
                            Intent intent = new Intent(AppMainActivity.this, ProfileActivity.class);
                            startActivity(intent);
                        }).show();

                logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_DROPBOX_NOT_CONNECTED_PRO);
            }
        }
    }

    private void restoreDialogListeners(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            ConfirmDialog dialog3 = (ConfirmDialog) getSupportFragmentManager().findFragmentByTag(Static.DIALOG_FOLLOW_US_ON_FACEBOOK);
            if (dialog3 != null) {
                setFollowUsOnFacebookDialogListener(dialog3);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(menuToChoose, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu != null) {
            if (menuToChoose == R.menu.menu_main) {
                menu.findItem(R.id.item_search).setVisible(drawerLayout != null);

                VectorDrawableCompat vectorDrawableCompatSearch = VectorDrawableCompat.create(getResources(), R.drawable.ic_search_white_24dp, null);
                menu.findItem(R.id.item_search).setIcon(vectorDrawableCompatSearch);

                if (PreferencesHelper.isOnThisDayEnabled()) {
                    VectorDrawableCompat vectorDrawableCompatOnThisDay = VectorDrawableCompat.create(getResources(), R.drawable.ic_memories, null);
                    menu.findItem(R.id.item_on_this_day).setIcon(vectorDrawableCompatOnThisDay).setVisible(true);

                    boolean includeCurrentYear = MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ON_THIS_DAY_INCLUDE_CURRENT_YEAR, false);

                    int onThisDayEntryCount = SQLiteQueryHelper.getEntriesByThisDayCount(includeCurrentYear);
                    if (onThisDayEntryCount > 0) {
                        CountBadge.Factory circleFactory = new CountBadge.Factory(this, BadgeShape.circle(.5f, Gravity.END | Gravity.TOP));
                        try {
                            Badger.sett(menu.findItem(R.id.item_on_this_day), circleFactory).setCount(onThisDayEntryCount);
                        } catch (Exception ignored) {
                        }
                    }
                } else {
                    menu.findItem(R.id.item_on_this_day).setVisible(false);
                }

                if (DEBUG) {
                    menu.findItem(R.id.item_clear_data).setVisible(true);
                    menu.findItem(R.id.item_upload_db_dbx).setVisible(true);
                } else {
                    menu.findItem(R.id.item_clear_data).setVisible(false);
                    menu.findItem(R.id.item_upload_db_dbx).setVisible(false);
                }


                menu.findItem(R.id.item_atlas).setVisible(!PreferencesHelper.isBottomTabEnabled());
                menu.findItem(R.id.item_stats).setVisible(!PreferencesHelper.isBottomTabEnabled());
                menu.findItem(R.id.item_photos).setVisible(!PreferencesHelper.isBottomTabEnabled());

            }

            if (menuToChoose == R.menu.menu_share) {
                VectorDrawableCompat vectorDrawableCompatSearch = VectorDrawableCompat.create(getResources(), R.drawable.ic_share_white_24dp, null);
                menu.findItem(R.id.item_share).setIcon(vectorDrawableCompatSearch);
            }

        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (activityState.isActivityPaused) {
            return true;
        }

        int itemId = item.getItemId();

        // Search
        if (itemId == R.id.item_search) {
            startSearch();
            return true;
        }
        // Multi select
        else if (itemId == R.id.item_multiselect) {
            if (contentFragment != null)
                contentFragment.turnOnMultiSelectMode();
            return true;
        }
        // Sort
        else if (itemId == R.id.item_sort) {
            if (contentFragment != null)
                contentFragment.showSortDialog();
            return true;
        }
        // All photos
        else if (itemId == R.id.item_photos) {
            Static.startAllPhotosActivity(AppMainActivity.this, activityState);
            return true;
        }
        else if (itemId == R.id.item_on_this_day) {
            sidemenuFragment.clearAllActiveFilters();
            Static.startOnThisDayActivity(AppMainActivity.this, activityState);
            return true;
        }
        else if (itemId == R.id.item_stats) {
            Static.startStatsActivity(AppMainActivity.this, activityState);
            return true;
        }
        else if (itemId == R.id.item_atlas) {
            Static.startMapsActivity(AppMainActivity.this, activityState);
            return true;
        }
        else if (itemId == R.id.item_templates) {
            Static.startTemplatesActivity(AppMainActivity.this, activityState);
            return true;
        }
        else if (itemId == R.id.item_diaro_web) {
            logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_DIARO_WEB);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GlobalConstants.DIARO_HOME_URL));
            startActivity(browserIntent);
            return true;
        }
        else if (itemId == R.id.item_settings) {
            Static.startSettingsActivity(AppMainActivity.this, activityState);
            return true;
        }
        else if (itemId == R.id.item_clear_data) {
            MyApp.getInstance().storageMgr.clearAllData();
            return true;
        }
        else if (itemId == R.id.item_upload_db_dbx) {
            Static.uploadDatabaseToDropbox();
            return true;
        }
        // Share
        else if (itemId == R.id.item_share) {
            GeneralUtils.shareBitmap(this, R.id.layout_container, "Diaro Stats");
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void startSearch() {
        sidemenuFragment.expandableListView.smoothScrollToPosition(0);
        if (drawerLayout != null) {
            if (drawerOpen) {
                Static.showSoftKeyboard(sidemenuFragment.searchEditText);
            } else {
                isStartingSearch = true;
                drawerLayout.openDrawer(menuFrame);
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        // On hardware search press
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            startSearch();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            // Result from sign in activity
            case Static.REQUEST_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    Static.startProfileActivity(AppMainActivity.this, activityState);
                }
                break;

            // Result from settings
            case Static.REQUEST_SETTINGS:

                if (contentFragment != null) {
                    // Update text size
                    contentFragment.entriesCursorAdapter.setTextSizes();
                    contentFragment.entriesListView.invalidateViews();
                }

                if (data != null) {
                    Bundle extras = data.getExtras();
                    boolean resultRestart = Objects.requireNonNull(extras).getBoolean("resultRestart");

                    if (resultRestart) {
                        if (resultCode == RESULT_FIRST_USER) {
                            Intent intent = new Intent(AppMainActivity.this, SettingsActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            intent.putExtra(Static.EXTRA_SKIP_SC, true);
                            intent.putExtra("resultRestart", true);
                            startActivityForResult(intent, Static.REQUEST_SETTINGS);
                        } else {
                            // Restart current activity
                            restartActivity();
                        }
                    }
                }
                break;

            // Result from view/edit entry activity
            case Static.REQUEST_VIEW_EDIT_ENTRY:
                if (resultCode == FragmentActivity.RESULT_OK) {
                    Bundle extras = data.getExtras();
                    String entryUid = Objects.requireNonNull(extras).getString("entryUid");
                    boolean entryArchived = extras.getBoolean("entryArchived");
                    // AppLog.d("entryUid: " + entryUid + ", entryArchived: " + entryArchived);

                    if (!TextUtils.isEmpty(entryUid)) {
                        if (entryArchived) {
                            onEntryArchived(entryUid);
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (actionMode != null) {
            finishActionMode();
        } else if (drawerLayout != null && drawerLayout.isDrawerVisible(menuFrame)) {
            drawerLayout.closeDrawer(menuFrame);
        } else {

            int selectedId = bottom_navigation.getSelectedItemId();
            if (selectedId == R.id.item_entires) {
                // super.onBackPressed();
                MyApp.getInstance().securityCodeMgr.setPostDelayedLock();
                finish();
            } else if (selectedId == R.id.item_stats) {
                bottom_navigation.setSelectedItemId(R.id.item_entires);
            } else if (selectedId == R.id.item_photos) {
                bottom_navigation.setSelectedItemId(R.id.item_entires);
            } else if (selectedId == R.id.item_atlas) {
                AtlasFragment fragment = (AtlasFragment) getSupportFragmentManager().findFragmentByTag("item_atlas");
                if (fragment != null && !fragment.onBackPressed())
                    bottom_navigation.setSelectedItemId(R.id.item_entires);
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        supportInvalidateOptionsMenu();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBillingClient != null) {
            if (mBillingClient.isReady()) {
                AppLog.e("ending billing connection..");
                mBillingClient.endConnection();
            }
            mBillingClient = null;
        }
        unregisterReceiver(timeChangedReceiver);
        unregisterReceiver(brReceiver);
    }

    private void restartActivity() {
        Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra(Static.EXTRA_SKIP_SC, true);
        intent.putExtra("restarted", true);

        finish();
        startActivity(intent);
    }

    public static class MyUndoListener implements View.OnClickListener {
        ArrayList<String> mEntriesUids;

        public MyUndoListener(ArrayList<String> entriesUids) {
            mEntriesUids = entriesUids;
        }

        @Override
        public void onClick(View v) {
            // Undo archive entries in background
            AppLog.e("undo " + mEntriesUids.get(0));
            UndoArchiveEntriesAsync undoArchiveEntriesAsync = new UndoArchiveEntriesAsync(mEntriesUids);
            undoArchiveEntriesAsync.execute();
        }
    }

    private void showUndoDeleteEntries(String serializedUids) {
        AppLog.d("serializedUids: " + serializedUids);
        final ArrayList<String> entriesUids = serializedUids.equals("") ? new ArrayList<>() : new ArrayList<>(Arrays.asList(serializedUids.split(",")));
        Snackbar mySnackbar = Snackbar.make(findViewById(R.id.content_frame), R.string.deleted, Snackbar.LENGTH_LONG);
        mySnackbar.setAction(R.string.undo_delete, new MyUndoListener(entriesUids));
        mySnackbar.show();

        mySnackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                    // Delete entries in background
                    AppLog.e("deleting -> " + entriesUids.get(0));
                    DeleteEntriesAsync deleteEntriesAsync = new DeleteEntriesAsync(entriesUids);
                    deleteEntriesAsync.execute();
                }
            }

            @Override
            public void onShown(Snackbar snackbar) {
            }
        });
    }

    private void showPromotionalDialogs() {
        if (MyApp.getInstance().openedCounterIncreased) {
            return;
        }

        int appOpenedTimesFromPrefs = MyApp.getInstance().prefs.getInt(Prefs.PREF_APP_OPENED_COUNTER, 0);
        if (appOpenedTimesFromPrefs == 3) {

        }

        String prefString = "isGooglePlayRated";
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isGooglePlayRated = prefs.getBoolean(prefString, false);

        if (appOpenedTimesFromPrefs > 3 && !isGooglePlayRated) {
            showAskToRateDialogGoogle();
        }

        if (appOpenedTimesFromPrefs == 26) {
            showFollowUsOnFacebookDialog();
        }

        // Increase count
        MyApp.getInstance().prefs.edit().putInt(Prefs.PREF_APP_OPENED_COUNTER, appOpenedTimesFromPrefs + 1).apply();
        MyApp.getInstance().openedCounterIncreased = true;
    }


    private void showAskToRateDialogGoogle() {
        ReviewManager reviewManager = ReviewManagerFactory.create(getApplicationContext());
        reviewManager.requestReviewFlow().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ReviewInfo reviewInfo = task.getResult();
                reviewManager.launchReviewFlow(AppMainActivity.this, reviewInfo).addOnFailureListener(e -> {

                }).addOnCompleteListener(task1 -> {
                    String prefString = "isGooglePlayRated";
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(AppMainActivity.this);
                    SharedPreferences.Editor prefsEditor = prefs.edit();
                    prefsEditor.putBoolean(prefString, true).apply();
                });
            }

        });
    }

    private void showFollowUsOnFacebookDialog() {
        String dialogTag = Static.DIALOG_FOLLOW_US_ON_FACEBOOK;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setTitle(getString(R.string.follow_us_on_facebook));
            dialog.setMessage(getString(R.string.follow_us_on_facebook_summary));
            dialog.setNeutralButtonText(getString(R.string.later));
            dialog.show(getSupportFragmentManager(), dialogTag);
            setFollowUsOnFacebookDialogListener(dialog);
        }
    }

    private void setFollowUsOnFacebookDialogListener(ConfirmDialog dialog) {
        dialog.setDialogPositiveClickListener(() -> GeneralUtils.openFacebookPage(AppMainActivity.this));
        dialog.setDialogNeutralClickListener(() -> MyApp.getInstance().prefs.edit().putInt(Prefs.PREF_APP_OPENED_COUNTER, 26).apply());
    }

    private void startFolderAddEditActivity(final String folderUid) {
        if (activityState.isActivityPaused) {
            return;
        }
        Intent intent = new Intent(this, FolderAddEditActivity.class);
        intent.putExtra(Static.EXTRA_SKIP_SC, true);
        intent.putExtra("folderUid", folderUid);
        startActivityForResult(intent, Static.REQUEST_FOLDER_ADDEDIT);
    }

    private void startTagAddEditActivity(String tagUid) {
        if (activityState.isActivityPaused) {
            return;
        }
        Intent intent = new Intent(this, TagAddEditActivity.class);
        intent.putExtra(Static.EXTRA_SKIP_SC, true);
        intent.putExtra("tagUid", tagUid);
        startActivityForResult(intent, Static.REQUEST_TAG_ADDEDIT);
    }

    private void startLocationAddEditActivity(String locationUid) {
        if (activityState.isActivityPaused) {
            return;
        }
        Intent intent = new Intent(this, LocationAddEditActivity.class);
        intent.putExtra(Static.EXTRA_SKIP_SC, true);
        intent.putExtra("locationUid", locationUid);
        startActivityForResult(intent, Static.REQUEST_LOCATION_ADDEDIT);
    }

    private void startMoodsAddEditActivity(String moodUid) {
        if (activityState.isActivityPaused) {
            return;
        }
        Intent intent = new Intent(this, MoodsAddEditActivity.class);
        intent.putExtra(Static.EXTRA_SKIP_SC, true);
        intent.putExtra("moodUid", moodUid);
        startActivityForResult(intent, Static.REQUEST_MOODS_ADDEDIT);
    }


    private void startEntryViewEditActivity(String entryUid) {
        if (activityState.isActivityPaused) {
            return;
        }
        Intent intent = new Intent(this, EntryViewEditActivity.class);
        intent.putExtra(Static.EXTRA_SKIP_SC, true);
        intent.putExtra("entryUid", entryUid);
        // If creating new entry
        if (entryUid == null) {
            // Pass selected folder
            String activeFolderUid = MyApp.getInstance().prefs.getString(Prefs.PREF_ACTIVE_FOLDER_UID, null);
            intent.putExtra("activeFolderUid", activeFolderUid);

            // Pass selected tags
            String activeTags = MyApp.getInstance().prefs.getString(Prefs.PREF_ACTIVE_TAGS, null);
            intent.putExtra("activeTags", activeTags);

            // Pass selected day in calendar
            long selectedRangeFromMillis = MyApp.getInstance().prefs.getLong(Prefs.PREF_ACTIVE_CALENDAR_RANGE_FROM_MILLIS, 0);
            intent.putExtra("activeDate", selectedRangeFromMillis);
        }
        startActivityForResult(intent, Static.REQUEST_VIEW_EDIT_ENTRY);
    }

    @Override
    public void onSettingsButtonClicked() {
        Static.startSettingsActivity(AppMainActivity.this, activityState);
    }

    @Override
    public void onHomeButtonClicked() {
        // Refresh entries list
        if (contentFragment != null)
            contentFragment.refreshEntriesList();
    }

    @Override
    public void onProfilePhotoAreaClicked() {
        if (!MyApp.getInstance().userMgr.isSignedIn()) {
            Static.startSignInActivity(this, activityState);
        } else {
            Static.startProfileActivity(this, activityState);
        }
    }

    @Override
    public void onUpgradeToProButtonClicked() {
        Static.startProActivity(this, activityState);
    }

    @Override
    public void onActiveFiltersChanged() {
        // Refresh entries list
        if (contentFragment != null)
            contentFragment.refreshEntriesList();
    }

    @Override
    public void onShouldStartFolderAddEditActivity(String folderUid) {
        startFolderAddEditActivity(folderUid);
    }

    @Override
    public void onShouldStartTagAddEditActivity(String tagUid) {
        startTagAddEditActivity(tagUid);
    }

    @Override
    public void onShouldStartLocationAddEditActivity(String locationUid) {
        startLocationAddEditActivity(locationUid);
    }

    @Override
    public void onShouldStartMoodAddEditActivity(String moodUid) {
        startMoodsAddEditActivity(moodUid);
    }

    @Override
    public void onSelectAllEntriesAsyncFinished(ArrayList<String> entriesArrayList) {
        if (contentFragment != null && contentFragment.isMultiSelectMode) {
            contentFragment.multiSelectedEntriesUids = new ArrayList<>(entriesArrayList);
            // Refresh selected entries count
            actionMode.invalidate();

            contentFragment.entriesListView.invalidateViews();
        }
    }

    @Override
    public void onCheckIfAllEntriesExistAsyncFinished(ArrayList<String> notExistingEntriesUids) {
        if (contentFragment != null && contentFragment.isMultiSelectMode) {
            for (int i = 0; i < notExistingEntriesUids.size(); i++) {
                contentFragment.multiSelectedEntriesUids.remove(notExistingEntriesUids.get(i));
            }
            // Refresh selected entries count
            actionMode.invalidate();
            contentFragment.entriesListView.invalidateViews();
        }
    }

    @Override
    public void onEntryArchived(String entryUid) {
        // Show undo archive toast
        showUndoDeleteEntries(entryUid);
    }

    @Override
    public void onTopLineClearSearchButtonClicked() {
        sidemenuFragment.clearActiveSearch();
    }

    @Override
    public void onTopLineClearCalendarButtonClicked() {
        sidemenuFragment.clearActiveCalendarRange();
    }

    @Override
    public void onTopLineClearFolderButtonClicked() {
        sidemenuFragment.cursorTreeAdapter.sidemenuFolders.clearActiveFolder();
    }

    @Override
    public void onTopLineClearTagsButtonClicked() {
        sidemenuFragment.cursorTreeAdapter.sidemenuTags.clearActiveTags();
    }

    @Override
    public void onTopLineClearLocationsButtonClicked() {
        sidemenuFragment.cursorTreeAdapter.sidemenuLocations.clearActiveLocations();
    }

    @Override
    public void onTopLineClearMoodButtonClicked() {
        sidemenuFragment.cursorTreeAdapter.sidemenuMoods.clearActiveMood();
    }

    @Override
    public void onTopLineClearAllFiltersButtonClicked() {
        sidemenuFragment.clearAllActiveFilters();
    }

    @Override
    public void openSideMenu() {
        // drawerlayout is null on tablet! so just clear the calender button
        if (drawerLayout != null) {
            drawerLayout.openDrawer(menuFrame);
        } else {
            onTopLineClearCalendarButtonClicked();
        }

    }

    @Override
    public void onSortChanged() {
        // Get calendar days markers
        sidemenuFragment.calendarView.executeGetDaysMarkersAsync();
    }

    @Override
    public void onActiveFolderNotFound() {
        sidemenuFragment.cursorTreeAdapter.sidemenuFolders.clearActiveFolder();
    }

    @Override
    public void onActiveMoodNotFound() {
        sidemenuFragment.cursorTreeAdapter.sidemenuMoods.clearActiveMood();
    }

    @Override
    public void onActiveTagNotFound(String tagUid) {
        sidemenuFragment.cursorTreeAdapter.sidemenuTags.removeSelectedTag(tagUid);
        sidemenuFragment.cursorTreeAdapter.sidemenuTags.saveActiveTagsInPrefs();
    }

    @Override
    public void onActiveLocationNotFound(String locationUid) {
        sidemenuFragment.cursorTreeAdapter.sidemenuLocations.removeSelectedLocation(locationUid);
        sidemenuFragment.cursorTreeAdapter.sidemenuLocations.saveActiveLocationsInPrefs();
    }

    @Override
    public void onStartedEntriesRefresh() {
        // Invalidate side menu list and update home icon
        sidemenuFragment.expandableListView.invalidateViews();
        sidemenuFragment.updateHomeIcon();
    }

    @Override
    public void onSelectUnselectEntry() {
        if (actionMode != null) {
            // Refresh selected entries count
            actionMode.invalidate();
        }
    }

    @Override
    public void onTurnOnMultiSelectMode(ActionMode.Callback actionModeCallback) {
        actionMode = startSupportActionMode(actionModeCallback);
    }

    @Override
    public void onTurnOffMultiSelectMode() {
        finishActionMode();
    }

    @Override
    public void onShouldStartEntryViewEditActivity(String entryUid) {
        startEntryViewEditActivity(entryUid);
    }

    private void finishActionMode() {
        if (actionMode != null) {
            actionMode.finish();
            actionMode = null;
        }
    }

    private void checkIAP() {

        mBillingClient = BillingClient.newBuilder(this)
                .setListener(this)
                .enablePendingPurchases(
                        PendingPurchasesParams.newBuilder()
                                .enableOneTimeProducts()
                                .enablePrepaidPlans()
                                .build()
                )
                .enableAutoServiceReconnection()
                .build();

        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {

                    // ONE TIME PURCHASE
                    QueryPurchasesParams inAppParams = QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build();

                    mBillingClient.queryPurchasesAsync(inAppParams, (billingResult1, purchasesList) -> {
                        if (billingResult1.getResponseCode() == BillingClient.BillingResponseCode.OK){
                            for (Purchase purchase : purchasesList) {
                                AppLog.e("The IAP purchases" + purchase.toString());

                                DateTime date = new DateTime(purchase.getPurchaseTime());
                                AppLog.e("Purchase year" + date.getYear());
                                proPurchaseYear = date.getYear();

                                String orderID = purchase.getOrderId();
                                if (StringUtils.isEmpty(orderID)) {
                                    // Its a google play nbo
                                    Static.turnOnPlayNboSubscription();
                                } else {
                                    Static.turnOffPlayNboSubscription();
                                }

                                if (!purchase.isAcknowledged()) {
                                    acknowledgePurchase(purchase.getPurchaseToken());
                                } else {
                                    AppLog.e("Purchase is acknowledged!");
                                }

                                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                    Static.turnOnPro();
                                } else {
                                    if (MyApp.getInstance().userMgr.isSignedIn()) {
                                        MyApp.getInstance().asyncsMgr.executeCheckProAsync(MyApp.getInstance().userMgr.getSignedInEmail());
                                    } else {
                                        Static.turnOffPro();
                                    }
                                }


                            }
                        } });

                    // SUBSCRIPTIONS
                    QueryPurchasesParams subsParams = QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build();

                    mBillingClient.queryPurchasesAsync(subsParams, (billingResult2, purchasesSubList) -> {
                        if (billingResult2.getResponseCode() == BillingClient.BillingResponseCode.OK){
                            if (purchasesSubList != null) {
                                for (Purchase purchase : purchasesSubList) {
                                    AppLog.e("The Subscription purchases" + purchase.toString());

                                    if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                        if (!purchase.isAcknowledged()) {
                                            acknowledgePurchase(purchase.getPurchaseToken());
                                        }

                                        String orderID = purchase.getOrderId();
                                        if (StringUtils.isEmpty(orderID)) {
                                            // Its a google play nbo
                                            Static.turnOnPlayNboSubscription();
                                        } else {
                                            Static.turnOffPlayNboSubscription();
                                        }
                                    }

                                    if (GlobalConstants.activeSubscriptionsList.contains(purchase.getProducts().get(0))) {
                                        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                            if (!Static.isSubscribedCurrently()) {
                                                Static.turnOnSubscribedCurrently();
                                            }

                                            // Just send payment info once for each order id!
                                            String prefString = "send_subsinfo_to_server" + purchase.getOrderId();
                                            SharedPreferences preferences = MyApp.getInstance().prefs;
                                            if (!preferences.getBoolean(prefString, false)) {
                                                if (MyApp.getInstance().userMgr.isSignedIn()) {
                                                    List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
                                                    productList.add(
                                                        QueryProductDetailsParams.Product.newBuilder()
                                                            .setProductId(purchase.getProducts().get(0))
                                                            .setProductType(BillingClient.ProductType.SUBS)
                                                            .build()
                                                    );

                                                    QueryProductDetailsParams productDetailsParams = QueryProductDetailsParams.newBuilder()
                                                            .setProductList(productList)
                                                            .build();

                                                    mBillingClient.queryProductDetailsAsync(productDetailsParams, (billingResult1, queryProductDetailsResult) -> {
                                                        try {
                                                            if (queryProductDetailsResult.getProductDetailsList() != null &&
                                                                !queryProductDetailsResult.getProductDetailsList().isEmpty()) {
                                                                ProductDetails productDetails = queryProductDetailsResult.getProductDetailsList().get(0);
                                                                PaymentUtils.sendGoogleInAppPaymentToAPI(purchase, productDetails);
                                                            }

                                                        } catch (Exception ignored) {

                                                        }
                                                    });
                                                }

                                                preferences.edit().putBoolean(prefString, true).apply();
                                            }

                                        } else {
                                            if (MyApp.getInstance().userMgr.isSignedIn()) {
                                                MyApp.getInstance().asyncsMgr.executeCheckProAsync(MyApp.getInstance().userMgr.getSignedInEmail());
                                            } else {
                                                Static.turnOffSubscribedCurrently();
                                            }
                                        }
                                    }

                                }

                            }
                        }
                    });

                    // NO PURCHASES FOUND
                    if (!Static.isProUser()) {
                        AppLog.i(" No purchases found");
                        // Get purchase info from server
                        if (MyApp.getInstance().userMgr.isSignedIn()) {
                            MyApp.getInstance().asyncsMgr.executeCheckProAsync(MyApp.getInstance().userMgr.getSignedInEmail());
                        } else {
                            Static.turnOffPro();
                            Static.turnOffSubscribedCurrently();
                            Static.turnOffPlayNboSubscription();
                        }
                    } else {
                        AppLog.i("Purchases found-> Pro: " + Static.isPro() + ", SubscribedYearly: " + Static.isSubscribedCurrently());
                        // User is Pro but does not have a subscription yet, ask him to upgrade to subscription
                        askBuyProYearly();
                    }

                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to Google Play by calling the startConnection() method.
            }
        });
    }

    // This receives updates for all purchases in your app.
    @Override
    public void onPurchasesUpdated(@NonNull BillingResult billingResult, @Nullable List<Purchase> purchases) {
        int responseCode = billingResult.getResponseCode();
        String debugMessage = billingResult.getDebugMessage();
        AppLog.e("onPurchasesUpdated: " +  responseCode  + ", " + debugMessage);

        if (responseCode == BillingClient.BillingResponseCode.OK) {
            if (purchases == null) {
                AppLog.e("onPurchasesUpdated: null purchase list");
            } else {
                AppLog.e("onPurchasesUpdated: successful");
                // Purchase is valid - checkIAP() will handle status updates on next app launch

                // TODO: Uncomment signature verification for production
                /*
                // ✅ SECURITY: Verify purchase signatures
                String base64PublicKey = getBase64PublicKey();
                for (Purchase purchase : purchases) {
                    if (Security.verifyPurchase(base64PublicKey, purchase.getOriginalJson(), purchase.getSignature())) {
                        AppLog.e("Purchase signature VERIFIED for: " + purchase.getOrderId());
                        // Purchase is valid - checkIAP() will handle status updates on next app launch
                    } else {
                        AppLog.e("SECURITY ALERT: Purchase signature INVALID for: " + purchase.getOrderId());
                    }
                }
                */
            }
        } else if (responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            AppLog.e("onPurchasesUpdated: User canceled the purchase");
        } else if (responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            AppLog.e("onPurchasesUpdated: The user already owns this item");
        } else if (responseCode == BillingClient.BillingResponseCode.DEVELOPER_ERROR) {
            AppLog.e("onPurchasesUpdated: Developer error means that Google Play does not recognize the configuration. ");
        }
    }

    public void acknowledgePurchase(String purchaseToken) {
        AppLog.e("Acknowledging purchase");
        AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchaseToken).build();
        mBillingClient.acknowledgePurchase(params, new AcknowledgePurchaseResponseListener() {
            @Override
            public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
                int responseCode = billingResult.getResponseCode();
                String debugMessage = billingResult.getDebugMessage();
                AppLog.e( "Acknowledged purchase : " + responseCode + " " + debugMessage);
            }
        });
    }

    /**
     * Gets the base64-encoded public key for purchase signature verification.
     * @return The public key string from GlobalConstants
     */
    private String getBase64PublicKey() {
        return GlobalConstants.base64EncodedPublicKey;
    }

    @Override
    public void onProductDetailsResponse(@NonNull BillingResult billingResult, @NonNull QueryProductDetailsResult queryProductDetailsResult) {
        int responseCode = billingResult.getResponseCode();
        String debugMessage = billingResult.getDebugMessage();

        if (responseCode == BillingClient.BillingResponseCode.OK) {
            List<ProductDetails> productDetailsList = queryProductDetailsResult.getProductDetailsList();
            if (productDetailsList == null || productDetailsList.isEmpty()) {
                AppLog.e("onProductDetailsResponse: null or empty ProductDetails list");
            } else {
                AppLog.e("onProductDetailsResponse: received " + productDetailsList.size() + " product details");
                // Product details received - used in checkIAP() for sending subscription info to server
            }
        } else {
            AppLog.e("onProductDetailsResponse: " + responseCode + " " + debugMessage);
        }
    }

    // show this dialog only once, if user has pro but is not subscribed to yearly subscription
    private void askBuyProYearly() {
        String prefString = "askBuyProYearly_____";
        SharedPreferences preferences = MyApp.getInstance().prefs;
        if (!preferences.getBoolean(prefString, false)) {
            int askYearBefore = 2019;
            // show buy pro & sync for non pro, signed in users
            if (Static.isPro() && !Static.isSubscribedCurrently() && proPurchaseYear < askYearBefore) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.diaro_needs_help_text, proPurchaseYear)).setTitle(getString(R.string.diaro_needs_help));

                builder.setPositiveButton(android.R.string.ok, (dialog1, id) -> {
                    Intent intent = new Intent(AppMainActivity.this, PremiumActivity.class);
                    startActivity(intent);
                });
                builder.setNegativeButton(android.R.string.no, (dialog12, id) -> {

                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
            preferences.edit().putBoolean(prefString, true).apply();
        }

    }



    private class BrReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String doWhat = intent.getStringExtra(Static.BROADCAST_DO);
            ArrayList<String> params = intent.getStringArrayListExtra(Static.BROADCAST_PARAMS);
            if (params != null && !params.isEmpty() && params.get(0) == null) {
                params = null;
            }
            if (doWhat != null) {
                AppLog.d("BR doWhat: " + doWhat + ", params: " + params);

                switch (doWhat) {
                    // - Recreate calendar -
                    case Static.DO_RECREATE_CALENDAR:
                        sidemenuFragment.createCalendarView(null);
                        break;

                    // - Show/hide banners -
                    case Static.DO_SHOW_HIDE_BANNER:
                    //    activityState.showHideBanner();
                        break;

                    // - Show/hide pro label -
                    case Static.DO_SHOW_HIDE_PRO_LABEL:
                        sidemenuFragment.showHideProLabel();
                        break;

                    // - Update profile photo -
                    case Static.DO_UPDATE_PROFILE_PHOTO:
                        sidemenuFragment.updateProfilePhoto();
                        break;

                    // - Show entry archive undo toast -
                    case Static.DO_SHOW_ENTRY_ARCHIVE_UNDO_TOAST:
                        if (params != null) {
                            String serializedUids = params.get(0);
                            // AppLog.d("serializedUids: " + serializedUids);
                            showUndoDeleteEntries(serializedUids);
                        }
                        break;
                }
            }
        }

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(Prefs.PREF_BOTTOM_TAB_ENABLED)) {
            if (!PreferencesHelper.isBottomTabEnabled())
                bottom_navigation.setVisibility(View.GONE);
            else
                bottom_navigation.setVisibility(View.VISIBLE);

            supportInvalidateOptionsMenu();
        }

        if (key.equals(Prefs.PREF_MOODS_ENABLED)) {
            // sidemenuFragment.refreshSideMenuItems();
        }

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (currentFragment != null && currentFragment.getTag() != null) {
            getSupportFragmentManager().putFragment(outState, currentFragment.getTag(), currentFragment);
        }
    }

}