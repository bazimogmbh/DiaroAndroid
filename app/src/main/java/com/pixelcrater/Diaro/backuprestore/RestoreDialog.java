package com.pixelcrater.Diaro.backuprestore;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.layouts.QustomDialogBuilder;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.storage.PermanentStorageUtils;

public class RestoreDialog extends DialogFragment {

    // State vars
    private static final String FILE_URI_STRING_STATE_KEY = "FILE_URI_STRING_STATE_KEY";

    private String fileUriString;

    // Merge button click listener
    private OnDialogMergeClickListener onDialogMergeClickListener;
    // Delete button click listener
    private OnDialogDeleteClickListener onDialogDeleteClickListener;

    public void setDialogMergeClickListener(OnDialogMergeClickListener l) {
        onDialogMergeClickListener = l;
    }

    public void setDialogDeleteClickListener(OnDialogDeleteClickListener l) {
        onDialogDeleteClickListener = l;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AppLog.d("");

        if (savedInstanceState != null) {
            fileUriString = savedInstanceState.getString(FILE_URI_STRING_STATE_KEY);
        }

        // Use the Builder class for convenient dialog construction
        QustomDialogBuilder builder = new QustomDialogBuilder(getActivity());

        // Color
        builder.setHeaderBackgroundColor(MyThemesUtils.getPrimaryColorCode());

        // Title
        builder.setTitle(getActivity().getResources().getString(R.string.restore));

        // Set message
        Uri fileUri = Uri.parse(fileUriString);
        String filename = PermanentStorageUtils.getBackupFilename(fileUri);
        String message = getString(R.string.settings_merge_or_delete).replace("%s", filename);
        builder.setMessage(message);

        // Merge button
        builder.setPositiveButton(R.string.merge, (dialog, which) -> {
            // Send merge button event back to the host activity
            if (onDialogMergeClickListener != null)
                onDialogMergeClickListener.onDialogMergeClick();
        });

        // Delete button
        builder.setNeutralButton(R.string.delete, (dialog, which) -> {
            // Send delete button event back to the host activity
            if (onDialogDeleteClickListener != null)
                onDialogDeleteClickListener.onDialogDeleteClick();
        });

        // Cancel button
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
        });

        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Prevent dismiss on touch outside
        getDialog().setCanceledOnTouchOutside(false);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public String getFileUriString() {
        return fileUriString;
    }

    public void setFileUriString(String fileUriString) {
        this.fileUriString = fileUriString;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(FILE_URI_STRING_STATE_KEY, fileUriString);
    }

    public interface OnDialogMergeClickListener {
        void onDialogMergeClick();
    }

    public interface OnDialogDeleteClickListener {
        void onDialogDeleteClick();
    }
}
