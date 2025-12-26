package com.pixelcrater.Diaro.backuprestore;

import static com.pixelcrater.Diaro.config.GlobalConstants.DROPBOX_PATH_BACKUP;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.google.android.material.snackbar.Snackbar;
import com.pixelcrater.Diaro.BuildConfig;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.generaldialogs.ConfirmDialog;
import com.pixelcrater.Diaro.main.ActivityState;
import com.pixelcrater.Diaro.storage.dropbox.DropboxAccountManager;
import com.pixelcrater.Diaro.storage.dropbox.DropboxLocalHelper;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.PermissionsUtils;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.storage.PermanentStorageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BackupRestoreTabFragment extends Fragment {

    public ArrayList<BackupFile> backupFilesArrayList = new ArrayList<>();
    private ActivityState activityState;
    private BackupFilesListAdapter backupFilesListAdapter;
    private int tabId;
    private ListView backupFilesListView;
    private ViewGroup noBackupFilesFound;
    private ViewGroup notConnectedWithDropbox;
    private View view;
    private ProgressDialog mloadingProgressView;

    public static BackupRestoreTabFragment newInstance(int tabId) {
        BackupRestoreTabFragment f = new BackupRestoreTabFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("tab_id", tabId);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tabId = getArguments().getInt("tab_id");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.backup_restore_tab_fragment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activityState = ((BackupRestoreActivity) getActivity()).activityState;
        noBackupFilesFound = (ViewGroup) view.findViewById(R.id.no_backup_files_found);
        mloadingProgressView = new ProgressDialog(getActivity());

        // Not connected with Dropbox
        notConnectedWithDropbox = (ViewGroup) view.findViewById(R.id.not_connected_with_dropbox);
        View connectWithDropboxLink = view.findViewById(R.id.connect_with_dropbox_link);
        connectWithDropboxLink.setOnClickListener(v -> {
            if (MyApp.getInstance().userMgr.isSignedIn()) {
                Static.startProfileActivity(getActivity(), activityState);
            } else {
                Static.startSignInActivity(getActivity(), activityState);
            }
        });

        backupFilesListView = (ListView) view.findViewById(R.id.backup_files_list);
        backupFilesListAdapter = new BackupFilesListAdapter(getActivity(), tabId, backupFilesArrayList);

        backupFilesListView.setAdapter(backupFilesListAdapter);

        backupFilesListView.setOnItemClickListener((arg0, v, position, id) -> {
            if (backupFilesArrayList.size() > position) {
                BackupFile o = backupFilesArrayList.get(position);
                showRestoreDataConfirmDialog(o.fileUri.toString());
            }
        });

        backupFilesListAdapter.setOverflowItemClickListener((v, position) -> {
            if (backupFilesArrayList.size() > position) {
                BackupFile o = backupFilesArrayList.get(position);
                showBackupFilePopupMenu(v, o.fileUri.toString());
            }
        });

        getBackupFilesList();

        // Restore active dialog listeners
        restoreDialogListeners(savedInstanceState);
    }

    private void restoreDialogListeners(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            ConfirmDialog dialog1 = (ConfirmDialog) getFragmentManager().findFragmentByTag(Static.DIALOG_CONFIRM_BACKUP_FILE_DELETE);
            if (dialog1 != null) {
                setBackupFileDeleteConfirmDialogListener(dialog1);
            }

            ConfirmDialog dialog2 = (ConfirmDialog) getFragmentManager().findFragmentByTag(Static.DIALOG_CONFIRM_CURRENT_DATA_DELETE);
            if (dialog2 != null) {
                setCurrentDataDeleteConfirmDialogListener(dialog2);
            }

            ConfirmDialog dialog3 = (ConfirmDialog) getFragmentManager().findFragmentByTag(Static.DIALOG_CONFIRM_RESTORE);
            if (dialog3 != null) {
                setRestoreDataConfirmDialogListener(dialog3);
            }

            RestoreDialog dialog4 = (RestoreDialog) getFragmentManager().findFragmentByTag(Static.DIALOG_RESTORE);
            if (dialog4 != null) {
                setRestoreDialogListener(dialog4);
            }

            ConfirmDialog dialog5 = (ConfirmDialog) getFragmentManager().findFragmentByTag(Static.DIALOG_CONFIRM_MOBILE_INTERNET_UPLOAD);
            if (dialog5 != null) {
                setMobileInternetWarningConfirmDialogListener(dialog5);
            }

            ConfirmDialog dialog6 = (ConfirmDialog) getFragmentManager().findFragmentByTag(Static.DIALOG_CONFIRM_MOBILE_INTERNET_DOWNLOAD);
            if (dialog6 != null) {
                setMobileInternetWarningConfirmDialogListener(dialog6);
            }
        }
    }

    public void getBackupFilesList() {
        AppLog.d("mTabId: " + tabId);

        // Make sure fragment is attached
        if (!isAdded() || getActivity() == null) {
            AppLog.e("Fragment not attached, skipping refresh");
            return;
        }

        backupFilesArrayList.clear();
        notConnectedWithDropbox.setVisibility(View.GONE);

        try {
            switch (tabId) {
                // SD card
                case BackupRestoreActivity.TAB_SD_CARD:

                    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
                        readLocalBackupDirectory();
                    }  else {
                        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                            readLocalBackupDirectory();
                        } else {
                            // Ask for permission
                            PermissionsUtils.askForPermission((AppCompatActivity) getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, Static.PERMISSION_REQUEST_STORAGE,
                                    Static.DIALOG_CONFIRM_RATIONALE_STORAGE, R.string.storage_permission_rationale_text);
                        }
                    }

                    break;

                // Dropbox
                case BackupRestoreActivity.TAB_DROPBOX:
                    if (DropboxAccountManager.isLoggedIn(getContext())) {
                        readDropboxBackupDirectory();
                    } else {
                        notConnectedWithDropbox.setVisibility(View.VISIBLE);
                    }

                    break;
            }

            if (!backupFilesArrayList.isEmpty()) {
                Collections.sort(backupFilesArrayList, new Static.ComparatorByBackupLastModified());
            }
        } catch (Exception e) {
            AppLog.e("Exception: " + e);

            String error = e.getMessage();
            if (error == null) {
                error = e.toString();
            }

            // Show error
            Static.showToastError(String.format("%s: %s", getString(R.string.error), error));
        }

//        AppLog.d("backupFilesArrayList.size(): " + backupFilesArrayList.size());

        // Update visibility of noBackupFilesFound for SD card tab
        if (tabId == BackupRestoreActivity.TAB_SD_CARD) {
            if (backupFilesArrayList.size() > 0) {
                noBackupFilesFound.setVisibility(View.GONE);
                backupFilesListView.setVisibility(View.VISIBLE);
            } else {
                noBackupFilesFound.setVisibility(View.VISIBLE);
                // Set drawable top
                TextView textView = (TextView) noBackupFilesFound.getChildAt(0);
                textView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_database_grey600_96dp, 0, 0);
            }
        }

        AppLog.d("backupFilesArrayList.size(): " + backupFilesArrayList.size());
        backupFilesListAdapter.notifyDataSetChanged();

        // Force list view to refresh
        backupFilesListView.invalidateViews();
    }

    private void readLocalBackupDirectory() {

        // Create backup directory if it doesn't exist
        PermanentStorageUtils.createDiaroBackupDirectory();

        Uri[] backupFilesUris = PermanentStorageUtils.getBackupFilesUris();
        AppLog.d("Found " + backupFilesUris.length + " backup files");

        for (Uri backupFileUri : backupFilesUris) {
            String backupFileName = PermanentStorageUtils.getBackupFilename(backupFileUri);
            long backupFileLength = PermanentStorageUtils.getBackupFileLength(backupFileUri);
            long backupFileLastModified = PermanentStorageUtils.getBackupFileLastModified(backupFileUri);
            String backupFileExtension = Static.getFileExtension(backupFileName);
            AppLog.d("backupFileName: " + backupFileName + ", backupFileLength: " + backupFileLength + ", backupFileLastModified: " + backupFileLastModified + ", backupFileExtension: " + backupFileExtension);

            if (backupFileExtension.equals("diaro") || backupFileExtension.equals("denc") || backupFileExtension.equals("xml") || backupFileExtension.equals("zip")) {
                BackupFile backupFile = new BackupFile(backupFileName, backupFileUri, Static.readableFileSize(backupFileLength), backupFileLastModified);
                backupFilesArrayList.add(backupFile);
            }
        }
    }




    private void readDropboxBackupDirectory() {
        mloadingProgressView.setMessage("Loading..");
        mloadingProgressView.setIndeterminate(true);
        mloadingProgressView.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mloadingProgressView.show();

        new AsyncTask<Void, Void,  List<Metadata>>() {
            @Override
            protected  List<Metadata> doInBackground(Void... voids) {
                try {
                    if (DropboxLocalHelper.exists(DropboxAccountManager.getDropboxClient(getContext()), "/Backups")) {
                        // Rename /Backups directory to /backup
                        DropboxAccountManager.getDropboxClient(getContext()).files().move("/Backups", DROPBOX_PATH_BACKUP);
                    }


                    List<Metadata> entries = new ArrayList<>();
                    ListFolderResult listFolderResult = null;

                    if (DropboxLocalHelper.exists(DropboxAccountManager.getDropboxClient(getContext()), DROPBOX_PATH_BACKUP)) {

                        while (listFolderResult == null || listFolderResult.getHasMore()) {
                            if (listFolderResult == null) {
                                listFolderResult = DropboxAccountManager.getDropboxClient(getContext()).files().listFolderBuilder(DROPBOX_PATH_BACKUP).withIncludeDeleted(false).withRecursive(true).start();
                            } else {
                                listFolderResult =  DropboxAccountManager.getDropboxClient(getContext()).files().listFolderContinue(listFolderResult.getCursor());
                            }
                            entries.addAll(listFolderResult.getEntries());
                        }

                        return entries;

                    } else {
                        return null;
                    }
                } catch (Exception e) {
                    AppLog.e(String.format("Error getting Dropbox folder list: %s", e.getMessage()));

                    dismissLoadingProgressDialog();

                    return null;
                }
            }

            @Override
            protected void onPostExecute(List<Metadata> entries) {
                super.onPostExecute(entries);

                dismissLoadingProgressDialog();

                if (entries != null) {
                    for (Metadata metadata : entries) {
                        if (metadata instanceof FileMetadata) {
                            String filename = metadata.getName();
                            String fileExtension = Static.getFileExtension(filename);

                            if (fileExtension.equals("diaro") || fileExtension.equals("denc") || fileExtension.equals("xml") || fileExtension.equals("zip")) {

                                BackupFile backupFile = new BackupFile(filename, Uri.parse(String.format("%s/%s", DROPBOX_PATH_BACKUP, metadata.getName())),
                                        Static.readableFileSize(((FileMetadata) metadata).getSize()), ((FileMetadata) metadata).getClientModified().getTime());

                                backupFilesArrayList.add(backupFile);
                                if (!backupFilesArrayList.isEmpty()) {
                                    Collections.sort(backupFilesArrayList, new Static.ComparatorByBackupLastModified());
                                }
                                if (notConnectedWithDropbox.getVisibility() == View.GONE) {
                                    // Show backup files list
                                    if (backupFilesArrayList.size() > 0) {
                                        noBackupFilesFound.setVisibility(View.GONE);
                                    }
                                    // No backup files found
                                    else {
                                        noBackupFilesFound.setVisibility(View.VISIBLE);

                                        // Set drawable top
                                        TextView textView = (TextView) noBackupFilesFound.getChildAt(0);
                                        int iconResId = R.drawable.ic_database_grey600_96dp;
                                        if (tabId == BackupRestoreActivity.TAB_DROPBOX) {
                                            iconResId = R.drawable.ic_backup_file_dropbox_grey600_96dp;
                                        }
                                        textView.setCompoundDrawablesWithIntrinsicBounds(0, iconResId, 0, 0);
                                    }
                                }
                                backupFilesListAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
            }
        }.execute();
    }

    public void uploadBackupFileToDropbox(String localFileUriString) {
        if (DropboxAccountManager.isLoggedIn(getContext())) {
            // Upload backup file to Dropbox
            MyApp.getInstance().asyncsMgr.executeUploadBackupFileToDropboxAsync(getActivity(), localFileUriString);
        } else {
            if (MyApp.getInstance().userMgr.isSignedIn()) {
                Static.startProfileActivity(getActivity(), activityState);
            } else {
                Static.startSignInActivity(getActivity(), activityState);
            }
        }
    }

    private void showBackupFilePopupMenu(View v, final String fileUriString) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), v);

        if (tabId == BackupRestoreActivity.TAB_SD_CARD) {
            popupMenu.getMenuInflater().inflate(R.menu.popupmenu_local_file, popupMenu.getMenu());
        } else if (tabId == BackupRestoreActivity.TAB_DROPBOX) {
            popupMenu.getMenuInflater().inflate(R.menu.popupmenu_dropbox_file, popupMenu.getMenu());
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            // Upload
            if (itemId == R.id.upload_to_dropbox) {
                if (MyApp.getInstance().networkStateMgr.isConnectedToInternet()) {
                    if (!MyApp.getInstance().networkStateMgr.isConnectedToInternetUsingWiFi()) {
                        // Show warning if on cellular internet
                        showMobileInternetWarningConfirmDialog(Static.DIALOG_CONFIRM_MOBILE_INTERNET_UPLOAD, fileUriString);
                        return true;
                    }

                    // Upload backup file to Dropbox
                    uploadBackupFileToDropbox(fileUriString);
                } else {
                    Snackbar.make(getView(), R.string.error_internet_connection, Snackbar.LENGTH_SHORT).show();
                }

                return true;
            }

            // Download
            else if (itemId == R.id.download_to_sd) {
                if (MyApp.getInstance().networkStateMgr.isConnectedToInternet()) {
                    if (!MyApp.getInstance().networkStateMgr.isConnectedToInternetUsingWiFi()) {
                        // Show warning if on cellular internet
                        showMobileInternetWarningConfirmDialog(Static.DIALOG_CONFIRM_MOBILE_INTERNET_DOWNLOAD, fileUriString);
                        return true;
                    }

                    // Download backup file from Dropbox
                    MyApp.getInstance().asyncsMgr.executeDownloadBackupFileFromDropboxAsync(getActivity(), fileUriString, false, false);

                } else {
                    Snackbar.make(getView(), R.string.error_internet_connection, Snackbar.LENGTH_SHORT).show();

                }

                return true;
            }

            // Share file
            else if (itemId == R.id.share) {
                Intent intentShareFile = new Intent(Intent.ACTION_SEND);
                intentShareFile.setType("application/zip");
                File fileWithinMyDir = new File(fileUriString);

                Uri uri = FileProvider.getUriForFile(getContext(), BuildConfig.APPLICATION_ID + ".provider", fileWithinMyDir);
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intentShareFile.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                    List<ResolveInfo> resInfoList = getActivity().getPackageManager().queryIntentActivities(intentShareFile, PackageManager.MATCH_DEFAULT_ONLY);
                    for (ResolveInfo resolveInfo : resInfoList) {
                        String packageName = resolveInfo.activityInfo.packageName;
                        getActivity().grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                }

                intentShareFile.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(intentShareFile, "Share File"));

                // TODO : export zip to any location
                /**
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("application/zip");
                intent.putExtra(Intent.EXTRA_TITLE, fileWithinMyDir.getName());
                getActivity().startActivityForResult(intent, 657); **/
                return true;
            }

            // Delete file
            else if (itemId == R.id.delete) {
                showBackupFileDeleteConfirmDialog(fileUriString);
                return true;
            }

            else {
                return false;
            }
        });

        popupMenu.show();
    }

    private void showMobileInternetWarningConfirmDialog(String dialogTag, String filePath) {
        if (getFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            ConfirmDialog dialog = new ConfirmDialog();

            int titleResId = R.string.upload_to_dropbox;
            if (dialogTag.equals(Static.DIALOG_CONFIRM_MOBILE_INTERNET_DOWNLOAD)) {
                titleResId = R.string.download_to_sd;
            }

            dialog.setTitle(getString(titleResId));
            dialog.setMessage(getString(R.string.using_mobile_internet_warning));
            dialog.setCustomString(filePath);
            dialog.setPositiveButtonText(getString(R.string.continue_action));
            dialog.show(getFragmentManager(), dialogTag);

            // Set dialog listener
            setMobileInternetWarningConfirmDialogListener(dialog);
        }
    }

    private void setMobileInternetWarningConfirmDialogListener(final ConfirmDialog dialog) {
        dialog.setDialogPositiveClickListener(() -> {
            if (dialog.getTag().equals(Static.DIALOG_CONFIRM_MOBILE_INTERNET_UPLOAD)) {
                // Upload backup file to Dropbox
                uploadBackupFileToDropbox(dialog.getCustomString());
            } else if (dialog.getTag().equals(Static.DIALOG_CONFIRM_MOBILE_INTERNET_DOWNLOAD)) {
                // Download backup file from Dropbox
                MyApp.getInstance().asyncsMgr.executeDownloadBackupFileFromDropboxAsync(getActivity(), dialog.getCustomString(), false, false);
            }
        });
    }

    private void showBackupFileDeleteConfirmDialog(String fileUriString) {
        String dialogTag = Static.DIALOG_CONFIRM_BACKUP_FILE_DELETE;
        if (getFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setTitle(getString(R.string.delete));
            dialog.setCustomString(fileUriString);

            Uri fileUri = Uri.parse(fileUriString);
            String filename = PermanentStorageUtils.getBackupFilename(fileUri);
            String message = getString(R.string.settings_confirm_file_delete).replace("%s", filename);
            dialog.setMessage(message);

            dialog.show(getFragmentManager(), dialogTag);

            // Set dialog listener
            setBackupFileDeleteConfirmDialogListener(dialog);
        }
    }

    private void setBackupFileDeleteConfirmDialogListener(final ConfirmDialog dialog) {
        dialog.setDialogPositiveClickListener(() -> {
            if (!isAdded()) {
                return;
            }

            String fileUriString = dialog.getCustomString();
            deleteBackupFile(fileUriString);
        });
    }

    private void showCurrentDataDeleteConfirmDialog(String fileUriString) {
        String dialogTag = Static.DIALOG_CONFIRM_CURRENT_DATA_DELETE;
        if (getFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setTitle(getString(R.string.delete));
            dialog.setCustomString(fileUriString);

            String message = getString(R.string.data_delete_warning);
            dialog.setMessage(message);

            dialog.show(getFragmentManager(), dialogTag);

            // Set dialog listener
            setCurrentDataDeleteConfirmDialogListener(dialog);
        }
    }

    private void setCurrentDataDeleteConfirmDialogListener(final ConfirmDialog dialog) {
        dialog.setDialogPositiveClickListener(() -> {
            if (!isAdded()) {
                return;
            }

            String fileUriString = dialog.getCustomString();
            restoreFromBackupFile(fileUriString, true);
        });
    }

    private void showRestoreDataConfirmDialog(String fileUriString) {
        AppLog.d("fileUriString: " + fileUriString);

        String dialogTag = Static.DIALOG_CONFIRM_RESTORE;
        if (getFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setTitle(getString(R.string.restore));

            Uri fileUri = Uri.parse(fileUriString);
            String filename = PermanentStorageUtils.getBackupFilename(fileUri);
            String message = getString(R.string.restore_data_confirmation_text).replace("%s",
                    filename);
            dialog.setMessage(message);

            dialog.setCustomString(fileUriString);
            dialog.setPositiveButtonText(getString(R.string.continue_action));
            dialog.show(getFragmentManager(), dialogTag);

            // Set dialog listener
            setRestoreDataConfirmDialogListener(dialog);
        }
    }

    private void setRestoreDataConfirmDialogListener(final ConfirmDialog dialog) {
        dialog.setDialogPositiveClickListener(() -> {
            // Show data Merge/Delete confirmation dialog
            showRestoreDialog(dialog.getCustomString());
        });
    }

    private void showRestoreDialog(String fileUriString) {
        AppLog.d("fileUriString: " + fileUriString);

        String dialogTag = Static.DIALOG_RESTORE;
        if (getFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            RestoreDialog dialog = new RestoreDialog();
            dialog.setFileUriString(fileUriString);
            dialog.show(getFragmentManager(), dialogTag);

            // Set dialog listener
            setRestoreDialogListener(dialog);
        }
    }

    private void setRestoreDialogListener(final RestoreDialog dialog) {
        dialog.setDialogMergeClickListener(() -> {
            if (!isAdded()) {
                return;
            }

            restoreFromBackupFile(dialog.getFileUriString(), false);
        });

        dialog.setDialogDeleteClickListener(() -> showCurrentDataDeleteConfirmDialog(dialog.getFileUriString()));
    }

    private void deleteBackupFile(String fileUriString) {
        Uri fileUri = Uri.parse(fileUriString);
        String filename = PermanentStorageUtils.getBackupFilename(fileUri);
        String message = getString(R.string.file_was_deleted).replace("%s", filename);

        // SD card
        if (tabId == BackupRestoreActivity.TAB_SD_CARD) {
            // Delete local file
            boolean deleted = PermanentStorageUtils.deleteBackupFile(fileUri);
            if (!deleted) {
                Static.showToastError(getString(R.string.unable_to_delete));
            } else {
                Static.showToastSuccess(getString(R.string.file_was_deleted).replace("%s", filename));
            }
        }
        // Dropbox
        else if (tabId == BackupRestoreActivity.TAB_DROPBOX) {
            try {
                if (MyApp.getInstance().storageMgr.getDbxFsAdapter() != null) {
                    // Delete file from Dropbox
                    new AsyncTask<String, Void, Metadata>() {

                        @Override
                        protected Metadata doInBackground(String... strings) {
                            try {
                                return DropboxAccountManager.getDropboxClient(getContext()).files().delete(strings[0]);

                            } catch (Exception e) {
                                AppLog.e(String.format("Error deleting Dropbox file: %s", e.getMessage()));

                                return null;
                            }
                        }

                        @Override
                        protected void onPostExecute(Metadata metadata) {
                            super.onPostExecute(metadata);
                            if (metadata == null) {
                                Static.showToastError(getString(R.string.unable_to_delete));

                            } else {
                                Static.showToastSuccess(getString(R.string.file_was_deleted).replace("%s", metadata.getName()));
                            }
                        }
                    }.execute(fileUriString);
                }
            } catch (Exception e) {
                AppLog.e("Exception: " + e);

                message = e.getMessage();
                if (message == null) {
                    message = e.toString();
                }
            }
        }

        getBackupFilesList();
    }

    private void restoreFromBackupFile(String fileUriString, boolean deleteOldData) {
        AppLog.d("fileUriString: " + fileUriString + ", deleteOldData: " + deleteOldData);

        Uri fileUri = Uri.parse(fileUriString);
        String filename = PermanentStorageUtils.getBackupFilename(fileUri);
        String fileExtension = Static.getFileExtension(filename);

        if (!fileExtension.equals("diaro") && !fileExtension.equals("denc") && !fileExtension.equals("zip") && !fileExtension.equals("xml")) {
            Snackbar.make(getView(), R.string.not_diaro_backup_file, Snackbar.LENGTH_SHORT).show();

        } else {
            // Restore from SD
            if (tabId == BackupRestoreActivity.TAB_SD_CARD) {
                // Restore
                MyApp.getInstance().asyncsMgr.executeRestoreFromBackupFileAsync(getActivity(), fileUriString, deleteOldData);
            }
            // Restore from Dropbox
            else if (tabId == BackupRestoreActivity.TAB_DROPBOX) {
                if (MyApp.getInstance().networkStateMgr.isConnectedToInternet()) {
                    // Download file from Dropbox
                    MyApp.getInstance().asyncsMgr.executeDownloadBackupFileFromDropboxAsync(getActivity(), fileUriString, true, deleteOldData);

                } else {
                    Snackbar.make(getView(), R.string.error_internet_connection, Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        dismissLoadingProgressDialog();
        super.onDestroy();
    }

    private void dismissLoadingProgressDialog() {
        try {
            if (mloadingProgressView != null) {
                if (mloadingProgressView.isShowing())
                    mloadingProgressView.dismiss();
            }
        } catch (final Exception e) {
        }
    }
}
