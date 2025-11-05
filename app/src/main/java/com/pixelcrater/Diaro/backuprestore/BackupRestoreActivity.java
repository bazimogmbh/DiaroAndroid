package com.pixelcrater.Diaro.backuprestore;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.activitytypes.TypeActivity;
import com.pixelcrater.Diaro.generaldialogs.ConfirmDialog;
import com.pixelcrater.Diaro.storage.dropbox.DropboxAccountManager;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.PermissionsUtils;

import java.io.File;
import java.util.ArrayList;

public class BackupRestoreActivity extends TypeActivity {

    public final static int TAB_SD_CARD = 0;
    public final static int TAB_DROPBOX = 1;

    private ViewPager tabsViewPager;
    private TabsPagerAdapter tabsPagerAdapter;

    // Broadcast Receiver
    private BroadcastReceiver brReceiver = new BrReceiver();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(addViewToContentContainer(R.layout.backup_restore));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        activityState.setLayoutBackground();
        activityState.setActionBarTitle(getSupportActionBar(),  R.string.settings_backup_restore);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setSelectedTabIndicatorColor(MyThemesUtils.getAccentColor());

        tabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        tabsViewPager = (ViewPager) findViewById(R.id.tabs_pager);
        tabsViewPager.setAdapter(tabsPagerAdapter);
        tabsViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // Get backup files list
                getCurrentFragmentTab().getBackupFilesList();
            }
        });

        tabLayout.setupWithViewPager(tabsViewPager);

        // Register broadcast receiver
        ContextCompat.registerReceiver(this,brReceiver, new IntentFilter(Static.BR_IN_BACKUP_RESTORE), ContextCompat.RECEIVER_NOT_EXPORTED);

        // Restore please wait dialogs of active AsyncTasks
        restorePleaseWaitDialogs();

        // Restore active dialog listeners
        restoreDialogListeners(savedInstanceState);
    }

    public BackupRestoreTabFragment getCurrentFragmentTab() {
        return (BackupRestoreTabFragment) tabsPagerAdapter.instantiateItem(tabsViewPager, tabsViewPager.getCurrentItem());

    }

    private void restorePleaseWaitDialogs() {
        // Create backup showPleaseWaitDialog
        if (MyApp.getInstance().asyncsMgr.isAsyncRunning(MyApp.getInstance().asyncsMgr.createBackupAsync)) {
            MyApp.getInstance().asyncsMgr.createBackupAsync.showPleaseWaitDialog(BackupRestoreActivity.this);
        }

        // Restore from backup file showPleaseWaitDialog
        if (MyApp.getInstance().asyncsMgr.isAsyncRunning(MyApp.getInstance().asyncsMgr.restoreFromBackupFileAsync)) {
            MyApp.getInstance().asyncsMgr.restoreFromBackupFileAsync.showPleaseWaitDialog(BackupRestoreActivity.this);

        }

        // Download backup file from Dropbox showPleaseWaitDialog
        if (MyApp.getInstance().asyncsMgr.isAsyncRunning(MyApp.getInstance().asyncsMgr.downloadBackupFileFromDropboxAsync)) {
            MyApp.getInstance().asyncsMgr.downloadBackupFileFromDropboxAsync.showPleaseWaitDialog(BackupRestoreActivity.this);
        }

        // Upload backup file to Dropbox showPleaseWaitDialog
        if (MyApp.getInstance().asyncsMgr.isAsyncRunning(MyApp.getInstance().asyncsMgr.uploadBackupFileToDropboxAsync)) {
            MyApp.getInstance().asyncsMgr.uploadBackupFileToDropboxAsync.showPleaseWaitDialog(BackupRestoreActivity.this);
        }
    }

    private void restoreDialogListeners(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            CreateBackupDialog dialog1 = (CreateBackupDialog) getSupportFragmentManager().findFragmentByTag(Static.DIALOG_BACKUP);
            if (dialog1 != null) {
                setCreateBackupDialogListener(dialog1);
            }

            ConfirmDialog dialog2 = (ConfirmDialog) getSupportFragmentManager().findFragmentByTag(Static.DIALOG_CONFIRM_RATIONALE_STORAGE);
            if (dialog2 != null) {
                PermissionsUtils.setConfirmRationaleDialogListener(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Static.PERMISSION_REQUEST_STORAGE, dialog2);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_backup_restore, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AppLog.d("item: " + item);
        if (activityState.isActivityPaused) {
            return true;
        }

        switch (item.getItemId()) {
            // Back
            case android.R.id.home:
                finish();
                return true;

            // Backup
            case R.id.item_backup:
                showCreateBackupDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AppLog.e("requestCode: " + requestCode + ", resultCode: " + resultCode);

        switch (requestCode) {
            // Result from Sign in activity
            case Static.REQUEST_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    if (tabsViewPager.getCurrentItem() == TAB_DROPBOX) {
                        if (DropboxAccountManager.isLoggedIn(this)) {
                            // Get backup files list
                            getCurrentFragmentTab().getBackupFilesList();
                        }
                    }
                }
                break;

            // Result from Profile activity
            case Static.REQUEST_PROFILE:
                if (tabsViewPager.getCurrentItem() == TAB_DROPBOX) {
                    if (DropboxAccountManager.isLoggedIn(this)) {
                        // Get backup files list
                        getCurrentFragmentTab().getBackupFilesList();
                    }
                }
                break;
        }
    }

    private void setFsBackupPathListener() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (MyApp.getInstance().asyncsMgr.isAsyncRunning(MyApp.getInstance().asyncsMgr.createBackupAsync)) {
            MyApp.getInstance().asyncsMgr.createBackupAsync.dismissPleaseWaitDialog();
        }
        if (MyApp.getInstance().asyncsMgr.isAsyncRunning(MyApp.getInstance().asyncsMgr.restoreFromBackupFileAsync)) {
            MyApp.getInstance().asyncsMgr.restoreFromBackupFileAsync.dismissPleaseWaitDialog();
        }
        if (MyApp.getInstance().asyncsMgr.isAsyncRunning(MyApp.getInstance().asyncsMgr.downloadBackupFileFromDropboxAsync)) {
            MyApp.getInstance().asyncsMgr.downloadBackupFileFromDropboxAsync.dismissPleaseWaitDialog();

        }

        if (isFinishing()) {
            MyApp.getInstance().asyncsMgr.cancelCreateBackupAsync();
            MyApp.getInstance().asyncsMgr.cancelRestoreFromBackupFileAsync();
            MyApp.getInstance().asyncsMgr.cancelDownloadBackupFileFromDropboxAsync();
        }

        unregisterReceiver(this.brReceiver);
    }

    private void showCreateBackupDialog() {
        String dialogTag = Static.DIALOG_BACKUP;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            CreateBackupDialog dialog = new CreateBackupDialog();
            dialog.show(getSupportFragmentManager(), dialogTag);

            // Set dialog listener
            setCreateBackupDialogListener(dialog);
        }
    }

    private void setCreateBackupDialogListener(final CreateBackupDialog dialog) {
        dialog.setDialogBackupClickListener((encrypt, skipAttachments) -> MyApp.getInstance().asyncsMgr.executeCreateBackupAsync(BackupRestoreActivity.this,  tabsViewPager.getCurrentItem(), encrypt, skipAttachments));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case Static.PERMISSION_REQUEST_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCurrentFragmentTab().getBackupFilesList();
                } else {
                    PermissionsUtils.showDeniedOpenSettingsDialog(this, Static.DIALOG_CONFIRM_RATIONALE_STORAGE , R.string.unable_to_access_storage);
                   // Static.showToast(getString(R.string.unable_to_access_storage), Toast.LENGTH_LONG);
                }
            }
        }
    }

    private class BrReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String doWhat = intent.getStringExtra(Static.BROADCAST_DO);
            ArrayList<String> params = intent.getStringArrayListExtra(Static.BROADCAST_PARAMS);
            AppLog.d("BR doWhat: " + doWhat + ", params: " + params);

            switch (doWhat) {
                // - Refresh backup files list -
                case Static.DO_REFRESH_BACKUP_FILES_LIST:
                    // Get backup files list
                    getCurrentFragmentTab().getBackupFilesList();
                    break;

                // - On download from Dropbox complete -
                case Static.DO_ACTIONS_ON_DOWNLOAD_COMPLETE:
                    boolean restore = params.get(0).equals(Static.PARAM_RESTORE);
                    boolean deleteOldData = params.get(1).equals(Static.PARAM_DELETE_OLD_DATA);
                    String tempFilePath = params.get(2);

                    if (restore) {
                        // Restore
                        MyApp.getInstance().asyncsMgr.executeRestoreFromBackupFileAsync(BackupRestoreActivity.this, tempFilePath, deleteOldData);
                        // Delete temp file
                        new File(tempFilePath).deleteOnExit();
                    }
                    break;

                // - On download from Dropbox canceled -
                case Static.DO_ACTIONS_ON_DOWNLOAD_CANCELED:
                    setFsBackupPathListener();
                    break;
            }
        }
    }
}
