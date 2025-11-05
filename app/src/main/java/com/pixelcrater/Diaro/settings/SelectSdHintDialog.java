package com.pixelcrater.Diaro.settings;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.layouts.QustomDialogBuilder;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

public class SelectSdHintDialog extends DialogFragment {

    private static final String CUSTOM_STRING_STATE_KEY = "CUSTOM_STRING_STATE_KEY";

    private View customView;
    private String customString;

    // Positive button click listener
    private OnDialogPositiveClickListener onDialogPositiveClickListener;
    // Negative button click listener
    private OnDialogNegativeClickListener onDialogNegativeClickListener;

    public void setDialogPositiveClickListener(OnDialogPositiveClickListener l) {
        onDialogPositiveClickListener = l;
    }

    public void setDialogNegativeClickListener(OnDialogNegativeClickListener l) {
        onDialogNegativeClickListener = l;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            customString = savedInstanceState.getString(CUSTOM_STRING_STATE_KEY);
        }

        // Use the Builder class for convenient dialog construction
        QustomDialogBuilder builder = new QustomDialogBuilder(getActivity());

        // Color
        builder.setHeaderBackgroundColor(MyThemesUtils.getPrimaryColorCode());

        // Title
        builder.setTitle(getString(R.string.hint));

        // Set custom view
        builder.setCustomView(R.layout.select_sd_hint_dialog);
        customView = builder.getCustomView();

        // Message
        TextView messageTextView = (TextView) customView.findViewById(R.id.message);

        try {
            String message = getString(R.string.select_sd_hint).replace("%s", getCustomString());
            messageTextView.setText(message);
        } catch (Exception e) {

        }


        builder.setPositiveButton(getString(R.string.select), (dialog, which) -> {
            // Send the positive button event back to the host activity
            if (onDialogPositiveClickListener != null)
                onDialogPositiveClickListener.onDialogPositiveClick();
        });

        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            // Send the negative button event back to the host activity
            if (onDialogNegativeClickListener != null)
                onDialogNegativeClickListener.onDialogNegativeClick();
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

    public String getCustomString() {
        return customString;
    }

    public void setCustomString(String customString) {
        this.customString = customString;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(CUSTOM_STRING_STATE_KEY, customString);
    }

    public interface OnDialogPositiveClickListener {
        void onDialogPositiveClick();
    }

    public interface OnDialogNegativeClickListener {
        void onDialogNegativeClick();
    }
}
