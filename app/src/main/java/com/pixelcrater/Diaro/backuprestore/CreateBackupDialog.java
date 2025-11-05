package com.pixelcrater.Diaro.backuprestore;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.layouts.QustomDialogBuilder;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

public class CreateBackupDialog extends DialogFragment {
    private View customView;
    private CheckBox encryptCheckBox;
    private CheckBox skipAttachmentsCheckBox;

    // Backup button click listener
    private OnDialogBackupClickListener onDialogBackupClickListener;

    public void setDialogBackupClickListener(OnDialogBackupClickListener l) {
        onDialogBackupClickListener = l;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AppLog.d("");

        // Use the Builder class for convenient dialog construction
        QustomDialogBuilder builder = new QustomDialogBuilder(getActivity());

        // Color
        builder.setHeaderBackgroundColor(MyThemesUtils.getPrimaryColorCode());

        // Title
        builder.setTitle(getActivity().getResources().getString(R.string.backup));

        // Set custom view
        builder.setCustomView(R.layout.create_backup_dialog);
        customView = builder.getCustomView();

        encryptCheckBox = (CheckBox) customView.findViewById(R.id.encrypt);
        skipAttachmentsCheckBox = (CheckBox) customView.findViewById(R.id.skip_attachments);

        // Export button
        builder.setPositiveButton(R.string.backup, (dialog, which) -> {
            // Send export button event back to the host activity
            if (onDialogBackupClickListener != null) {
                onDialogBackupClickListener.onDialogBackupClick(encryptCheckBox.isChecked(), skipAttachmentsCheckBox.isChecked());
            }
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

    public interface OnDialogBackupClickListener {
        void onDialogBackupClick(boolean encrypt, boolean skipAttachments);
    }
}
