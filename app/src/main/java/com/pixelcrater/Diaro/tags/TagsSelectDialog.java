package com.pixelcrater.Diaro.tags;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.DialogFragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.model.TagInfo;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.generaldialogs.ConfirmDialog;
import com.pixelcrater.Diaro.layouts.QustomDialogBuilder;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

public class TagsSelectDialog extends DialogFragment implements LoaderManager.LoaderCallbacks<Cursor>, QustomDialogBuilder.OnSearchTextChangeListener {

    // State vars
    private static final String ENTRY_TAGS_UIDS_STATE_KEY = "ENTRY_TAGS_UIDS_STATE_KEY";
    private static final String SELECTED_TAGS_UIDS_STATE_KEY = "SELECTED_TAGS_UIDS_STATE_KEY";

    private static final int TAGS_LIST_LOADER = 0;
    public String entryTagsUids;
    public TagsCursorAdapter tagsCursorAdapter;
    private AlertDialog dialog;
    private String selectedTagsUids;
    private ListView tagsListView;
    private ProgressBar tagsListProgressBar;
    private QustomDialogBuilder builder;

    // Save click listener
    private OnDialogSaveClickListener onDialogSaveClickListener;

    public void setOnDialogSaveClickListener(OnDialogSaveClickListener l) {
        onDialogSaveClickListener = l;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            entryTagsUids = savedInstanceState.getString(ENTRY_TAGS_UIDS_STATE_KEY);
            selectedTagsUids = savedInstanceState.getString(SELECTED_TAGS_UIDS_STATE_KEY);
        }

        // Use the Builder class for convenient dialog construction
        builder = new QustomDialogBuilder(getActivity());

        // Color
        builder.setHeaderBackgroundColor(MyThemesUtils.getPrimaryColorCode());

        // Title
        builder.setTitle(getActivity().getResources().getString(R.string.select_tags));

        // Set custom view
        builder.setCustomView(R.layout.tags_list);
        View customView = builder.getCustomView();

        // Save button
        builder.setPositiveButton(R.string.save, (dialog, which) -> {
            if (onDialogSaveClickListener != null) {
                onDialogSaveClickListener.onDialogSaveClick(tagsCursorAdapter.getSelectedTagsUids());
            }
        });

        // Cancel button
        builder.setNegativeButton(android.R.string.cancel, null);

        // Search button
        builder.showSearchButton();
        builder.setOnSearchTextChangeListener(this);

        // Add new button
        builder.setAddNewButtonOnClick(v -> startTagAddEditActivity(null));

        // Tags list
        tagsListView = (ListView) customView.findViewById(R.id.tags_list);

        tagsListProgressBar = (ProgressBar) customView.findViewById(R.id.tags_list_progress);

        // Set list adapter
        tagsCursorAdapter = new TagsCursorAdapter(getActivity(), null, 0, TagsSelectDialog.this);
        tagsListView.setAdapter(tagsCursorAdapter);

        // OnOverflowItemClickListener
        tagsCursorAdapter.setOverflowItemClickListener((v, tagUid) -> showTagPopupMenu(v, tagUid));

        // Highlight tags
        if (selectedTagsUids != null) {
            tagsCursorAdapter.setSelectedTagsUids(selectedTagsUids);
        }

        // OnItemClickListener
        tagsListView.setOnItemClickListener((a, view, position, id) -> {
            // Mark/Unmark tag
            tagsCursorAdapter.markUnmarkTag(view, position);
            tagsListView.invalidateViews();
        });

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

        // Init tags cursor loader
        LoaderManager.getInstance(this).initLoader(TAGS_LIST_LOADER, null, this);
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

    public void setSelectedTagsUids(String selectedTagsUids) {
        this.entryTagsUids = selectedTagsUids;
        this.selectedTagsUids = selectedTagsUids;
    }

    private void restoreDialogListeners(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            ConfirmDialog dialog1 = (ConfirmDialog) getChildFragmentManager().findFragmentByTag(Static.DIALOG_CONFIRM_TAG_DELETE);
            if (dialog1 != null) setTagDeleteConfirmDialogListener(dialog1);
        }
    }

    private void showTagPopupMenu(View v, final String tagUid) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), v);
        popupMenu.getMenuInflater().inflate(R.menu.popupmenu_tag, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                // Edit tag
                case R.id.edit:
                    startTagAddEditActivity(tagUid);
                    return true;

                // Delete tag
                case R.id.delete:
                    showTagDeleteConfirmDialog(tagUid);
                    return true;
                default:
                    return false;
            }
        });

        popupMenu.show();
    }

    private void startTagAddEditActivity(String tagUid) {
        Intent intent = new Intent(getActivity(), TagAddEditActivity.class);
        intent.putExtra(Static.EXTRA_SKIP_SC, true);
        intent.putExtra("tagUid", tagUid);
        startActivityForResult(intent, Static.REQUEST_TAG_ADDEDIT);
    }

    private void showTagDeleteConfirmDialog(final String tagUid) {
        String dialogTag = Static.DIALOG_CONFIRM_TAG_DELETE;
        if (getChildFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            ConfirmDialog dialog = new ConfirmDialog();
            dialog.setCustomString(tagUid);
            dialog.setTitle(getString(R.string.delete));
            dialog.setMessage(getString(R.string.tag_confirm_delete));
            dialog.show(getChildFragmentManager(), dialogTag);

            // Set dialog listener
            setTagDeleteConfirmDialogListener(dialog);
        }
    }

    private void setTagDeleteConfirmDialogListener(final ConfirmDialog dialog) {
        dialog.setDialogPositiveClickListener(() -> {
            if (!isAdded()) return;

            // Delete tag in background
            TagsStatic.deleteTagInBackground(dialog.getCustomString());
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(ENTRY_TAGS_UIDS_STATE_KEY, entryTagsUids);
        outState.putString(SELECTED_TAGS_UIDS_STATE_KEY, tagsCursorAdapter.getSelectedTagsUids());

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result from tag add/edit dialog
        if (requestCode == Static.REQUEST_TAG_ADDEDIT) {
            if (resultCode == Activity.RESULT_OK) {
                Bundle extras = data.getExtras();
                String tagUid = extras.getString("tagUid");

                // Check if this tag was newly created
                if (extras.getBoolean("created")) {
                    selectedTagsUids = tagsCursorAdapter.getSelectedTagsUids();

                    // Add this tag to selected tags
                    if (selectedTagsUids.equals("")) {
                        selectedTagsUids += ",";
                    }
                    selectedTagsUids += tagUid + ",";
                    tagsCursorAdapter.setSelectedTagsUids(selectedTagsUids);
                }
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        return new TagsCursorLoader(getActivity(), false, args == null ? "" : args.getString("searchKeyword"));
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        tagsCursorAdapter.swapCursor(cursor);
        tagsListView.setVisibility(View.VISIBLE);
        tagsListProgressBar.setVisibility(View.GONE);

        int index = 0;
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                TagInfo tagInfo = new TagInfo(cursor);
                if (selectedTagsUids.contains(tagInfo.uid)) {
                    tagsListView.setSelection(index);
                    break;
                }
                index++;
            }
        }

        // Save button
        Button saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        saveButton.setEnabled(tagsCursorAdapter.getCount() > 0);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        tagsCursorAdapter.swapCursor(null);
    }

    @Override
    public void onSearchTextChange(String text) {
//        AppLog.d("Search text: " + text);

        // Init loader
        Bundle bundle = new Bundle();
        bundle.putString("searchKeyword", text);
        LoaderManager.getInstance(this).restartLoader(TAGS_LIST_LOADER, bundle, this);
    }

    public interface OnDialogSaveClickListener {
        void onDialogSaveClick(String selectedTagsUids);
    }
}
