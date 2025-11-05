package com.pixelcrater.Diaro.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.activitytypes.TypePreferenceActivity;
import com.pixelcrater.Diaro.analytics.AnalyticsConstants;
import com.pixelcrater.Diaro.backuprestore.BackupRestoreActivity;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.export.CSVExport;
import com.pixelcrater.Diaro.export.PdfTxtCsvExportDialog;
import com.pixelcrater.Diaro.export.TxtExport;
import com.pixelcrater.Diaro.generaldialogs.ConfirmDialog;
import com.pixelcrater.Diaro.generaldialogs.OptionsDialog;
import com.pixelcrater.Diaro.imports.CSVImport;
import com.pixelcrater.Diaro.imports.DayoneImport;
import com.pixelcrater.Diaro.imports.DiariumImport;
import com.pixelcrater.Diaro.imports.EvernoteImport;
import com.pixelcrater.Diaro.imports.JourneyImport;
import com.pixelcrater.Diaro.imports.MemorizeImport;
import com.pixelcrater.Diaro.imports.RedNotebookImport;
import com.pixelcrater.Diaro.imports.SimpleJournalImport;
import com.pixelcrater.Diaro.imports.UniversumImport;
import com.pixelcrater.Diaro.storage.dropbox.DropboxAccountManager;
import com.pixelcrater.Diaro.storage.dropbox.SyncService;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.KeyValuePair;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;
import com.pixelcrater.Diaro.utils.storage.FileUtil;
import com.pixelcrater.Diaro.utils.storage.PermanentStorageUtils;
import com.pixelcrater.Diaro.utils.storage.StorageUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class SettingsDataGroupActivity extends TypePreferenceActivity implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private static final String NEW_BACKUP_STORAGE_PATH_STATE_KEY = "NEW_BACKUP_STORAGE_PATH_STATE_KEY";

    private boolean showChooseBackupStorageDialogOnResume;
    private boolean showHintDialogOnResume;
    private String newPermanentStoragePath;
    public static boolean SKIP_PASSCODE = false;

    // Broadcast Receiver
    private BroadcastReceiver brReceiver = new BrReceiver();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityState.setActionBarTitle(Objects.requireNonNull(getSupportActionBar()), getString(R.string.settings_data));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (savedInstanceState != null) {
            newPermanentStoragePath = savedInstanceState.getString(NEW_BACKUP_STORAGE_PATH_STATE_KEY);
        }

        // Add preferences from XML
        myPreferenceFragment.addPreferencesFromResource(R.xml.preferences_data);

        // Set actions to preferences
        setupCheckboxPreference("SYNC_ON_WIFI_ONLY");
        setupCheckboxPreference("ALLOW_ROAMING_SYNC");
        setupCheckboxPreference("SHOW_SYNC_NOTIFICATION");
        setupPreference("EXPORT_PDF");
        setupPreference("EXPORT_TXT");
        setupPreference("EXPORT_CSV");
        setupPreferenceAndSummary("IMPORT_EVERNOTE", getString(R.string.import_summary, "Evernote"));
        setupPreferenceAndSummary("IMPORT_DAYONE", getString(R.string.import_summary, "Dayone"));
        setupPreferenceAndSummary("IMPORT_JOURNEY", getString(R.string.import_summary, "Journey"));
        setupPreferenceAndSummary("IMPORT_DIARIUM", getString(R.string.import_summary, "Diarium"));
        setupPreferenceAndSummary("IMPORT_MEMORIZE", getString(R.string.import_summary, "Memorize"));
        setupPreferenceAndSummary("IMPORT_REDNOTEBOOK", getString(R.string.import_summary, "Red Notebook"));
        setupPreferenceAndSummary("IMPORT_UNIVERSUM", getString(R.string.import_summary, "Universum"));
        setupPreferenceAndSummary("IMPORT_CSV", getString(R.string.import_summary, "CSV"));
        setupPreferenceAndSummary("IMPORT_SIMPLE_JOURNAL", getString(R.string.import_summary, "Simple Journal"));
        setupPreference("BACKUP_RESTORE");
        setupPreference("BACKUP_STORAGE_DIRECTORY");
        setupPreference("ATTACHMENTS_STORAGE_DIRECTORY");

        updateBackupStorageDirectoryPreference();
        updateAttachmentsStorageDirectoryPreference();

        // Register broadcast receiver
        ContextCompat.registerReceiver(this,brReceiver, new IntentFilter(Static.BR_IN_SETTINGS_DATA), ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putString(NEW_BACKUP_STORAGE_PATH_STATE_KEY, newPermanentStoragePath);
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

    @Override
    protected void onResume() {

        if (SKIP_PASSCODE) {
            MyApp.getInstance().securityCodeMgr.setUnlocked();
            SKIP_PASSCODE = false;
        }

        super.onResume();

        if (showChooseBackupStorageDialogOnResume) {
            showChooseBackupStorageDialog();
            showChooseBackupStorageDialogOnResume = false;
        }
        if (showHintDialogOnResume) {
            showHintDialog();
            showHintDialogOnResume = false;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(this.brReceiver);
    }

    private void updateBackupStorageDirectoryPreference() {
        Preference preference = myPreferenceFragment.findPreference("BACKUP_STORAGE_DIRECTORY");
        Objects.requireNonNull(preference).setSummary(PermanentStorageUtils.getDiaroBackupDirPath());
    }

    private void updateAttachmentsStorageDirectoryPreference() {
        Preference preference = myPreferenceFragment.findPreference("ATTACHMENTS_STORAGE_DIRECTORY");
        Objects.requireNonNull(preference).setSummary(AppLifetimeStorageUtils.getAppFilesDirPath());
    }

    private void setupCheckboxPreference(String key) {
        CheckBoxPreference preference = myPreferenceFragment.findPreference(key);

        if (preference != null) {
            preference.setOnPreferenceChangeListener(this);

            switch (key) {
                case "SYNC_ON_WIFI_ONLY":
                    preference.setEnabled(MyApp.getInstance().storageMgr.isStorageDropbox());

                    if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_SYNC_ON_WIFI_ONLY, true)) {
                        preference.setChecked(true);
                    }
                    break;
                case "ALLOW_ROAMING_SYNC":
                    preference.setEnabled(MyApp.getInstance().storageMgr.isStorageDropbox());

                    if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_ALLOW_ROAMING_SYNC, false)) {
                        preference.setChecked(true);
                    }
                    break;
                case "SHOW_SYNC_NOTIFICATION":
                    preference.setEnabled(MyApp.getInstance().storageMgr.isStorageDropbox());

                    if (MyApp.getInstance().prefs.getBoolean(Prefs.PREF_SHOW_SYNC_NOTIFICATION, false)) {
                        preference.setChecked(true);
                    }
                    break;
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();

        switch (key) {
            case "SYNC_ON_WIFI_ONLY":
                MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_SYNC_ON_WIFI_ONLY, (Boolean) newValue).apply();

                if (newValue.equals(true)) {
                    // If sync is active but connected not on WiFi, cancel it
                    if (MyApp.getInstance().asyncsMgr.isSyncAsyncRunning() && !MyApp.getInstance().networkStateMgr.isConnectedToInternetUsingWiFi()) {
                        // Cancel sync
                        MyApp.getInstance().asyncsMgr.cancelSyncAsync();
                    }
                } else {
                    showMobileInternetWarningConfirmDialog();

                    if (MyApp.getInstance().networkStateMgr.isConnectedToInternet() && DropboxAccountManager.isLoggedIn(this)) {
                        // Start sync service
                        SyncService.startService();
                    }
                }
                return true;
            case "ALLOW_ROAMING_SYNC":
                MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_ALLOW_ROAMING_SYNC, (Boolean) newValue).apply();

                if (newValue.equals(false)) {
                    // If sync is active but roaming
                    if (MyApp.getInstance().asyncsMgr.isSyncAsyncRunning() && MyApp.getInstance().networkStateMgr.isConnectedToInternetUsingDataNetwork() && Static.IsNetworkRoaming()) {
                        // Cancel sync
                        MyApp.getInstance().asyncsMgr.cancelSyncAsync();
                    }
                } else {
                    if (MyApp.getInstance().networkStateMgr.isConnectedToInternet() && DropboxAccountManager.isLoggedIn(this)) {
                        // Start sync service
                        SyncService.startService();
                    }
                }

                return true;
            case "SHOW_SYNC_NOTIFICATION":
                MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_SHOW_SYNC_NOTIFICATION, (Boolean) newValue).apply();
                return true;
        }

        return false;
    }

    private void setupPreference(String key) {
        Preference preference = myPreferenceFragment.findPreference(key);
        if (preference != null) {
            preference.setOnPreferenceClickListener(this);
        }
    }

    private void setupPreferenceAndSummary(String key, String summary) {
        Preference preference = myPreferenceFragment.findPreference(key);
        if (preference != null) {
            preference.setOnPreferenceClickListener(this);
            preference.setSummary(summary);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        if (activityState.isActivityPaused) {
            return false;
        }

        switch (key) {
            case "EXPORT_PDF": {
                showExportPDFDialog(0);
                break;
            }

            case "EXPORT_TXT": {
                showExportPDFDialog(1);
                break;
            }

            case "EXPORT_CSV": {
                showExportPDFDialog(2);
                break;
            }

            case "BACKUP_RESTORE": {
                Intent intent = new Intent(this, BackupRestoreActivity.class);
                intent.putExtra(Static.EXTRA_SKIP_SC, true);
                startActivityForResult(intent, Static.REQUEST_SETTINGS_BACKUP_RESTORE);
                break;
            }

            case "BACKUP_STORAGE_DIRECTORY":
                newPermanentStoragePath = null;
                showChooseBackupStorageDialog();
                break;

            case "ATTACHMENTS_STORAGE_DIRECTORY":
                showChooseAttachmentsStorageDialog();
                break;

            case "IMPORT_EVERNOTE":
                FirebaseAnalytics.getInstance(this).logEvent(AnalyticsConstants.EVENT_LOG_IMPORT_EVERNOTE, new Bundle());
                Intent evernoteintent = new Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(evernoteintent, "Select Evernote .enex file"), Static.REQUEST_CODE_EVERNOTE_IMPORT);
                break;

            case "IMPORT_DAYONE":
                FirebaseAnalytics.getInstance(this).logEvent(AnalyticsConstants.EVENT_LOG_IMPORT_DAYONE, new Bundle());
                Intent dayoneintent = new Intent().setType("application/zip").setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(dayoneintent, "Select Dayone .zip file"), Static.REQUEST_CODE_DAYONE_IMPORT);
                break;

            case "IMPORT_JOURNEY":
                FirebaseAnalytics.getInstance(this).logEvent(AnalyticsConstants.EVENT_LOG_IMPORT_JOURNEY, new Bundle());
                Intent journeyintent = new Intent().setType("application/zip").setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(journeyintent, "Select Journey .zip file"), Static.REQUEST_CODE_JOURNEY_IMPORT);
                break;

            case "IMPORT_DIARIUM":
                FirebaseAnalytics.getInstance(this).logEvent(AnalyticsConstants.EVENT_LOG_IMPORT_DIARIUM, new Bundle());
                Intent diariumintent = new Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(diariumintent, "Select Diarium .diary database file"), Static.REQUEST_CODE_DIARIUM_IMPORT);
                break;

            case "IMPORT_MEMORIZE":
                FirebaseAnalytics.getInstance(this).logEvent(AnalyticsConstants.EVENT_LOG_IMPORT_MEMORIZE, new Bundle());
                Intent memorizeintent = new Intent().setType("application/zip").setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(memorizeintent, "Select Memorize .zip file"), Static.REQUEST_CODE_MEMORIZE_IMPORT);
                break;

            case "IMPORT_REDNOTEBOOK":
                FirebaseAnalytics.getInstance(this).logEvent(AnalyticsConstants.EVENT_LOG_IMPORT_REDNOTEBOOK, new Bundle());
                Intent rednotebookIntent = new Intent().setType("application/zip").setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(rednotebookIntent, "Select Red Notebook .zip file"), Static.REQUEST_CODE_REDNOTEBOOK_IMPORT);
                break;
            case "IMPORT_UNIVERSUM":
                FirebaseAnalytics.getInstance(this).logEvent(AnalyticsConstants.EVENT_LOG_IMPORT_UNIVERSUM, new Bundle());
                Intent universumIntent = new Intent().setType("application/zip").setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(universumIntent, "Select Universum .zip file"), Static.REQUEST_CODE_UNIVERSUM_IMPORT);
                break;
            case "IMPORT_CSV":
                // FirebaseAnalytics.getInstance(this).logEvent(AnalyticsConstants.EVENT_LOG_IMPORT_UNIVERSUM, new Bundle());
                Intent csvIntent = new Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(csvIntent, "Select .csv file"), Static.REQUEST_CODE_CSV_IMPORT);
                break;

            case "IMPORT_SIMPLE_JOURNAL":
                // FirebaseAnalytics.getInstance(this).logEvent(AnalyticsConstants.EVENT_LOG_IMPORT_UNIVERSUM, new Bundle());
                Intent simpleJournalIntent = new Intent().setType("application/zip").setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(simpleJournalIntent, "Select .zip file"), Static.REQUEST_CODE_SIMPLE_JOURNAL_IMPORT);
                break;
        }

        return false;
    }

    private void startSelectSdcardActivity() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        // Verify that the intent will resolve to an activity
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, Static.REQUEST_SELECT_SD_CARD);
        } else {
            Static.showToast(getString(R.string.error), Toast.LENGTH_LONG);
        }

    }

    private void showHintDialog() {
        String dialogTag = Static.DIALOG_SELECT_SD_HINT;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            SelectSdHintDialog dialog = new SelectSdHintDialog();
            dialog.setCustomString(newPermanentStoragePath);
            dialog.show(getSupportFragmentManager(), dialogTag);

            // Set dialog listener
            setHintDialogListener(dialog);
        }
    }

    public void setHintDialogListener(final SelectSdHintDialog dialog) {
        // User must select SD card directory to grant permission
        dialog.setDialogPositiveClickListener(this::startSelectSdcardActivity);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AppLog.e("requestCode: " + requestCode + ", resultCode: " + resultCode + ", data: " + data);

        if (requestCode == Static.REQUEST_SELECT_SD_CARD && resultCode == RESULT_OK) {
            AppLog.d("newPermanentStoragePath: " + newPermanentStoragePath);

            Uri treeUri = data.getData();
            AppLog.d("treeUri.getPath(): " + Objects.requireNonNull(treeUri).getPath());

            String fullPathFromTreeUri = FileUtil.getFullPathFromTreeUri(treeUri, this);
            AppLog.d("fullPathFromTreeUri: " + fullPathFromTreeUri);

            if (StringUtils.equals(newPermanentStoragePath, fullPathFromTreeUri)) {
                // Persist access permissions.
                getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                MyApp.getInstance().asyncsMgr.executeChangePermanentStorageAsync(SettingsDataGroupActivity.this, newPermanentStoragePath, treeUri.toString());

            } else {
                Static.showToastError(getString(R.string.wrong_directory_selected));
                showHintDialogOnResume = true;
            }

        } else if (requestCode == Static.REQUEST_CODE_TXT_SAVE && resultCode == RESULT_OK) {
            Uri currFileURI = data.getData();
            StorageUtils.writeToFile(TxtExport.outputTextString, currFileURI, this);
        } else if (requestCode == Static.REQUEST_CODE_CSV_SAVE && resultCode == RESULT_OK) {
            Uri currFileURI = data.getData();
            StorageUtils.writeToFile(CSVExport.outputCSVString, currFileURI, this);
        } else if (requestCode == Static.REQUEST_CODE_EVERNOTE_IMPORT && resultCode == RESULT_OK) {
            new EvernoteImport(Static.getPhotoFilePathFromUri(data.getData()), this);
        } else if (requestCode == Static.REQUEST_CODE_JOURNEY_IMPORT && resultCode == RESULT_OK) {
            new JourneyImport(Static.getPhotoFilePathFromUri(data.getData()), this);
        } else if (requestCode == Static.REQUEST_CODE_DAYONE_IMPORT && resultCode == RESULT_OK) {
            new DayoneImport(Static.getPhotoFilePathFromUri(data.getData()), this);
        } else if (requestCode == Static.REQUEST_CODE_DIARIUM_IMPORT && resultCode == RESULT_OK) {
            new DiariumImport(data.getData(), this);
        } else if (requestCode == Static.REQUEST_CODE_MEMORIZE_IMPORT && resultCode == RESULT_OK) {
            new MemorizeImport(Static.getPhotoFilePathFromUri(data.getData()), this);
        } else if (requestCode == Static.REQUEST_CODE_REDNOTEBOOK_IMPORT && resultCode == RESULT_OK) {
            new RedNotebookImport(Static.getPhotoFilePathFromUri(data.getData()), this);
        } else if (requestCode == Static.REQUEST_CODE_UNIVERSUM_IMPORT && resultCode == RESULT_OK) {
            new UniversumImport(Static.getPhotoFilePathFromUri(data.getData()), this);
        } else if (requestCode == Static.REQUEST_CODE_CSV_IMPORT && resultCode == RESULT_OK) {
            new CSVImport(Static.getPhotoFilePathFromUri(data.getData()), this);
        } else if (requestCode == Static.REQUEST_CODE_SIMPLE_JOURNAL_IMPORT && resultCode == RESULT_OK) {
            new SimpleJournalImport(Static.getPhotoFilePathFromUri(data.getData()), this);
        }

    }

    private void showMobileInternetWarningConfirmDialog() {
        String dialogTag = Static.DIALOG_CONFIRM_MOBILE_INTERNET;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setTitle(getString(R.string.tip));
            dialog.setMessage(getString(R.string.using_mobile_internet_warning));
            dialog.hideNegativeButton();
            dialog.show(getSupportFragmentManager(), dialogTag);
        }
    }

    private void showChooseBackupStorageDialog() {
        AppLog.d("");

        String dialogTag = Static.DIALOG_CHOOSE_BACKUP_STORAGE;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            OptionsDialog dialog = new OptionsDialog();

            long backupDirSize = StorageUtils.getUsedSizeInBytes(new File(PermanentStorageUtils.getDiaroBackupDirPath()));
            AppLog.d("Needed space for /backup: " + backupDirSize + "B");

//            backupDirSize = FileUtils.sizeOfDirectory(
//                    new File(PermanentStorageUtils.getDiaroBackupDirPath()));
//            AppLog.d("Needed space for /backup: " + backupDirSize + "B");

            String sizeWithUnits = StorageUtils.getSizeWithUnits(backupDirSize, null);

            dialog.setTitle(getString(R.string.backup_storage) + " (" + sizeWithUnits + ")");

            // Set selected value
            String selectedValue = PermanentStorageUtils.getPermanentStoragePref();
            int selectedIndex = 0;

            ArrayList<KeyValuePair> options = getStoragesOptions(false);

            ArrayList<String> itemsTitlesArrayList = new ArrayList<>();
            for (int i = 0; i < options.size(); i++) {
                KeyValuePair option = options.get(i);
                itemsTitlesArrayList.add(option.value);
                if (option.key.equals(selectedValue)) {
                    selectedIndex = i;
                }
            }

            dialog.setItemsTitles(itemsTitlesArrayList);
            dialog.setItemsSubtitles(getItemsSubtitlesArrayList(itemsTitlesArrayList));
            dialog.setSelectedIndex(selectedIndex);

            dialog.show(getSupportFragmentManager(), dialogTag);

            // Set dialog listener
            setChooseBackupStorageDialogListener(dialog);
        }
    }

    private void setChooseBackupStorageDialogListener(OptionsDialog dialog) {
        dialog.setDialogItemClickListener(which -> {
            //    AppLog.e("getStoragesOptions(false).get(" + which + ").key: " + getStoragesOptions(showInternal).get(which).key);

            String permanentStoragePath = PermanentStorageUtils.getPermanentStoragePref();

            newPermanentStoragePath = getStoragesOptions(false).get(which).key;

            AppLog.e("permanentStoragePath: " + permanentStoragePath);
            AppLog.e("newPermanentStoragePath: " + newPermanentStoragePath);

            // If the same path chosen
            if (StringUtils.equals(permanentStoragePath, newPermanentStoragePath)) {
                return;
            }

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                MyApp.getInstance().asyncsMgr.executeChangePermanentStorageAsync(SettingsDataGroupActivity.this, newPermanentStoragePath, null);
            } else {
                Uri newStorageUri = Uri.parse(newPermanentStoragePath);
                boolean isDocumentUri = DocumentFile.isDocumentUri(MyApp.getInstance(), newStorageUri);
                AppLog.d("isDocumentUri: " + isDocumentUri);

                File newPermanentStorage = new File(newPermanentStoragePath);
                AppLog.d("newPermanentStorage.canRead(): " + newPermanentStorage.canRead() + ", newPermanentStorage.canWrite(): " + newPermanentStorage.canWrite());

                DocumentFile newStorageDf = DocumentFile.fromFile(newPermanentStorage);
                AppLog.d("newStorageDf.canRead(): " + newStorageDf.canRead() + ", newStorageDf.canWrite(): " + newStorageDf.canWrite());

                ArrayList<String> extSdCardPaths = FileUtil.getExtSdCardPaths();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && extSdCardPaths.contains(newPermanentStoragePath)) {
                    Uri persistedTreeUri = PermanentStorageUtils.getStorageTreeUriFromPersistedPermissions(newPermanentStoragePath);
                    // If already has persisted permission for this storage
                    if (persistedTreeUri != null) {
                        MyApp.getInstance().asyncsMgr.executeChangePermanentStorageAsync(SettingsDataGroupActivity.this, newPermanentStoragePath, persistedTreeUri.toString());
                    } else {
                        showHintDialog();
                    }
                } else {
                    MyApp.getInstance().asyncsMgr.executeChangePermanentStorageAsync(SettingsDataGroupActivity.this, newPermanentStoragePath, null);
                }
            }

        });
    }

    private void showChooseAttachmentsStorageDialog() {
        AppLog.d("");

        String dialogTag = Static.DIALOG_CHOOSE_ATTACHMENTS_STORAGE;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            OptionsDialog dialog = new OptionsDialog();

            long attachmentsDirSize = StorageUtils.getUsedSizeInBytes(new File(AppLifetimeStorageUtils.getMediaDirPath()));
//            AppLog.d("#2 Needed space for /media: " + attachmentsDirSize + "B");

            long profileDirSize = StorageUtils.getUsedSizeInBytes(new File(AppLifetimeStorageUtils.getProfilePhotoDirPath()));
//            AppLog.d("#2 Needed space for /profile: " + profileDirSize + "B");

            String sizeWithUnits = StorageUtils.getSizeWithUnits(attachmentsDirSize + profileDirSize, null);
            dialog.setTitle(getString(R.string.attachments_storage) + " (" + sizeWithUnits + ")");

            // Set selected value
            String selectedValue = AppLifetimeStorageUtils.getAppLifetimeStoragePref();
            int selectedIndex = 0;

            ArrayList<KeyValuePair> options = getStoragesOptions(false);
            ArrayList<String> itemsTitlesArrayList = new ArrayList<>();
            for (int i = 0; i < options.size(); i++) {
                KeyValuePair option = options.get(i);
                itemsTitlesArrayList.add(option.value);
                if (option.key.equals(selectedValue)) {
                    selectedIndex = i;
                }
            }

            dialog.setItemsTitles(itemsTitlesArrayList);
            dialog.setItemsSubtitles(getItemsSubtitlesArrayList(itemsTitlesArrayList));
            dialog.setSelectedIndex(selectedIndex);

            dialog.show(getSupportFragmentManager(), dialogTag);

            // Set dialog listener
            setChooseAttachmentsStorageDialogListener(dialog);
        }
    }

    private ArrayList<String> getItemsSubtitlesArrayList(ArrayList<String> itemsTitlesArrayList) {
        ArrayList<String> itemsSubtitlesArrayList = new ArrayList<>();

        for (String storagePath : itemsTitlesArrayList) {
            itemsSubtitlesArrayList.add(StorageUtils.getAvailableSpaceWithUnits(new File(storagePath)) + " " + MyApp.getInstance().getString(R.string.free_of) + " " +
                    StorageUtils.getTotalSpaceWithUnits(new File(storagePath))
            );
        }
        return itemsSubtitlesArrayList;
    }

    private ArrayList<KeyValuePair> getStoragesOptions(boolean showInternal) {
        String[] storagesArray = StorageUtils.getExternalStorageDirectories();
//        ArrayList<String> storagesArrayList = FileUtil.getExtSdCardPaths();

        ArrayList<KeyValuePair> options = new ArrayList<>();
        if (showInternal) {
            options.add(new KeyValuePair("/data/data", "/data/data"));
        }

        for (String storage : storagesArray) {
            options.add(new KeyValuePair(storage, storage));
            // + " (" + getAvailableSpaceWithUnits(storage) + "GB)"
        }

        return options;
    }

    private void setChooseAttachmentsStorageDialogListener(OptionsDialog dialog) {
        dialog.setDialogItemClickListener(which -> {
            String oldStoragePrefValue = AppLifetimeStorageUtils.getAppLifetimeStoragePref();
            String newStoragePrefValue = getStoragesOptions(false).get(which).key;

            // If the same path chosen
            if (StringUtils.equals(oldStoragePrefValue, newStoragePrefValue)) {
                return;
            }

            MyApp.getInstance().asyncsMgr.executeChangeAppLifetimeStorageAsync(SettingsDataGroupActivity.this, oldStoragePrefValue, newStoragePrefValue);
        });
    }

    private void showExportPDFDialog(int exportType) {
        String dialogTag = Static.DIALOG_EXPORT_PDF;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            PdfTxtCsvExportDialog dialog = new PdfTxtCsvExportDialog(exportType);
            dialog.show(getSupportFragmentManager(), dialogTag);
        }
    }

    private class BrReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String doWhat = intent.getStringExtra(Static.BROADCAST_DO);
            ArrayList<String> params = intent.getStringArrayListExtra(Static.BROADCAST_PARAMS);
            AppLog.d("BR doWhat: " + doWhat + ", params: " + params);

            // - Update UI -
            if (Static.DO_UPDATE_UI.equals(Objects.requireNonNull(doWhat))) {
                updateBackupStorageDirectoryPreference();
                updateAttachmentsStorageDirectoryPreference();
            }
        }
    }

}
