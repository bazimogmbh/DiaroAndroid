package com.pixelcrater.Diaro.folders;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.DialogFragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.model.FolderInfo;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.generaldialogs.ConfirmDialog;
import com.pixelcrater.Diaro.layouts.QustomDialogBuilder;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

public class FolderSelectDialog extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor>, QustomDialogBuilder.OnSearchTextChangeListener {

    private static final String SELECTED_FOLDER_UID_STATE_KEY = "SELECTED_FOLDER_UID_STATE_KEY";

    private static final int FOLDERS_LIST_LOADER = 0;
    private FoldersCursorAdapter foldersCursorAdapter;
    private boolean isNewEntry;
    private AlertDialog dialog;
    private String selectedFolderUid;
    private ListView foldersListView;
    private ProgressBar foldersListProgressBar;
    private QustomDialogBuilder builder;

    // Item click listener
    private OnDialogItemClickListener onDialogItemClickListener;

    public void setOnDialogItemClickListener(OnDialogItemClickListener l) {
        onDialogItemClickListener = l;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            selectedFolderUid = savedInstanceState.getString(SELECTED_FOLDER_UID_STATE_KEY);
        }

        // Use the Builder class for convenient dialog construction
        builder = new QustomDialogBuilder(getActivity());

        // Color
        builder.setHeaderBackgroundColor(MyThemesUtils.getPrimaryColorCode());

        // Title
        builder.setTitle(getActivity().getResources().getString(R.string.select_folder));

        // Set custom view
        builder.setCustomView(R.layout.folders_list);
        View customView = builder.getCustomView();

        // Cancel button
        builder.setNegativeButton(android.R.string.cancel, null);

        // Search button
        builder.showSearchButton();
        builder.setOnSearchTextChangeListener(this);

        // Add new button
        builder.setAddNewButtonOnClick(v -> startFolderAddEditActivity(null));

        // Folders list
        foldersListView =  customView.findViewById(R.id.folders_list);

        foldersListProgressBar = customView.findViewById(R.id.folders_list_progress);

        // Set list adapter
        foldersCursorAdapter = new FoldersCursorAdapter(getActivity(), null, 0);
        foldersListView.setAdapter(foldersCursorAdapter);

        // OnItemClickListener
        foldersListView.setOnItemClickListener((parent, view, position, id) -> {
//                AppLog.d("foldersCursorAdapter.getItemUid(" + position + "): " +foldersCursorAdapter.getItemUid(position));

            if (onDialogItemClickListener != null) {
                onDialogItemClickListener.onDialogItemClick(foldersCursorAdapter.getItemUid(position));
            }

            dialog.dismiss();
        });

        // OnOverflowItemClickListener
        foldersCursorAdapter.setOnOverflowItemClickListener(this::showFolderPopupMenu);

        // Mark folder
        foldersCursorAdapter.setSelectedFolderUid(selectedFolderUid);

        foldersCursorAdapter.setIsNewEntry(isNewEntry);

        // Restore active dialog listeners
        restoreDialogListeners(savedInstanceState);

        dialog = builder.create();

        // Create the AlertDialog object and return it
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Prevent dismiss on touch outside
        getDialog().setCanceledOnTouchOutside(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Init folders cursor loader
        LoaderManager.getInstance(this).initLoader(FOLDERS_LIST_LOADER, null, this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!builder.getSearchFieldText().equals("")) {
            builder.showHideSearchField(true);
        }
    }

    private void restoreDialogListeners(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            ConfirmDialog dialog1 = (ConfirmDialog)getChildFragmentManager().findFragmentByTag(Static.DIALOG_CONFIRM_FOLDER_DELETE);

            if (dialog1 != null) {
                setFolderDeleteConfirmDialogListener(dialog1);
            }
        }
    }

    public void setSelectedFolderUid(String selectedFolderUid) {
        this.selectedFolderUid = selectedFolderUid;
    }

    public void setIsNewEntry(boolean isNewEntry) {
        this.isNewEntry = isNewEntry;
    }

    private void showFolderPopupMenu(View v, final String folderUid) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), v);
        popupMenu.getMenuInflater().inflate(R.menu.popupmenu_folder, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            // Edit folder
            if (itemId == R.id.edit) {
                startFolderAddEditActivity(folderUid);
                return true;
            }

            // Delete folder
            else if (itemId == R.id.delete) {
                showFolderDeleteConfirmDialog(folderUid);
                return true;
            }

            else {
                return false;
            }
        });

        popupMenu.show();
    }

    private void startFolderAddEditActivity(String folderUid) {
        Intent intent = new Intent(getActivity(), FolderAddEditActivity.class);
        intent.putExtra(Static.EXTRA_SKIP_SC, true);
        intent.putExtra("folderUid", folderUid);
        startActivityForResult(intent, Static.REQUEST_FOLDER_ADDEDIT);
    }

    private void showFolderDeleteConfirmDialog(final String folderUid) {
        String dialogTag = Static.DIALOG_CONFIRM_FOLDER_DELETE;
        if (getChildFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setCustomString(folderUid);
            dialog.setTitle(getString(R.string.delete));
            dialog.setMessage(getString(R.string.folder_confirm_delete));
            dialog.show(getChildFragmentManager(), dialogTag);

            // Set dialog listener
            setFolderDeleteConfirmDialogListener(dialog);
        }
    }

    private void setFolderDeleteConfirmDialogListener(final ConfirmDialog dialog) {
        dialog.setDialogPositiveClickListener(() -> {
            if (!isAdded()) {
                return;
            }

            // Delete folder in background
            FoldersStatic.deleteFolderInBackground(dialog.getCustomString());
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SELECTED_FOLDER_UID_STATE_KEY, selectedFolderUid);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        return new FoldersCursorLoader(getActivity(),  args == null ? "" : args.getString("searchKeyword"));
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        foldersCursorAdapter.swapCursor(cursor);
        foldersListView.setVisibility(View.VISIBLE);
        foldersListProgressBar.setVisibility(View.GONE);

        int index = 0;
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                FolderInfo folderInfo = new FolderInfo(cursor);
                if(folderInfo.uid != null && selectedFolderUid!= null){
                    if (folderInfo.uid.compareToIgnoreCase(selectedFolderUid) == 0) {
                        foldersListView.setSelection(index);
                        break;

                    }
                    index++;
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        foldersCursorAdapter.swapCursor(null);
    }

    @Override
    public void onSearchTextChange(String text) {
//        AppLog.d("Search text: " + text);

        // Init loader
        Bundle bundle = new Bundle();
        bundle.putString("searchKeyword", text);
        LoaderManager.getInstance(this).restartLoader(FOLDERS_LIST_LOADER, bundle, this);
    }

    public interface OnDialogItemClickListener {
        void onDialogItemClick(String selectedFolderUid);
    }
}
