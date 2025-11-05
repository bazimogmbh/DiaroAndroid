package com.pixelcrater.Diaro.generaldialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.layouts.QustomDialogBuilder;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

public class ConfirmDialog extends DialogFragment {

    private static final String CUSTOM_STRING_STATE_KEY = "CUSTOM_STRING_STATE_KEY";
    private static final String POSITIVE_BUTTON_TEXT_STATE_KEY = "POSITIVE_BUTTON_TEXT_STATE_KEY";
    private static final String NEUTRAL_BUTTON_TEXT_STATE_KEY = "NEUTRAL_BUTTON_TEXT_STATE_KEY";
    private static final String SHOW_NEGATIVE_BUTTON_STATE_KEY = "SHOW_NEGATIVE_BUTTON_STATE_KEY";
    private static final String TITLE_STATE_KEY = "TITLE_STATE_KEY";
    private static final String ICON_STATE_KEY = "ICON_STATE_KEY";
    private static final String MESSAGE_STATE_KEY = "MESSAGE_STATE_KEY";

    private String customString;
    private String title;
    private int icon = -1;
    private String message;
    private String positiveButtonText;
    private String neutralButtonText;
    private boolean showNegativeButton = true;

    // Positive button click listener
    private OnDialogPositiveClickListener onDialogPositiveClickListener;
    // Neutral button click listener
    private OnDialogNeutralClickListener onDialogNeutralClickListener;
    // Negative button click listener
    private OnDialogNegativeClickListener onDialogNegativeClickListener;

    public void setDialogPositiveClickListener(OnDialogPositiveClickListener l) {
        onDialogPositiveClickListener = l;
    }

    public void setDialogNeutralClickListener(OnDialogNeutralClickListener l) {
        onDialogNeutralClickListener = l;
    }

    public void setDialogNegativeClickListener(OnDialogNegativeClickListener l) {
        onDialogNegativeClickListener = l;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            customString = savedInstanceState.getString(CUSTOM_STRING_STATE_KEY);
            positiveButtonText = savedInstanceState.getString(POSITIVE_BUTTON_TEXT_STATE_KEY);
            neutralButtonText = savedInstanceState.getString(NEUTRAL_BUTTON_TEXT_STATE_KEY);
            showNegativeButton = savedInstanceState.getBoolean(SHOW_NEGATIVE_BUTTON_STATE_KEY);
            title = savedInstanceState.getString(TITLE_STATE_KEY);
            icon = savedInstanceState.getInt(ICON_STATE_KEY);
            message = savedInstanceState.getString(MESSAGE_STATE_KEY);
        }

        // Use the Builder class for convenient dialog construction
        QustomDialogBuilder builder = new QustomDialogBuilder(getActivity());

        // Color
        builder.setHeaderBackgroundColor(MyThemesUtils.getPrimaryColorCode());

        // Title
        if (title != null) {
            builder.setTitle(title);
        }

        // Icon
        if (icon != -1) {
            builder.setIcon(icon);
        }

        builder.setMessage(message);

        builder.setPositiveButton(
                (positiveButtonText != null) ? positiveButtonText : getString(android.R.string.yes), (dialog, which) -> {
                    // Send the positive button event back to the host activity
                    if (onDialogPositiveClickListener != null)
                        onDialogPositiveClickListener.onDialogPositiveClick();
                });

        if (neutralButtonText != null) {
            builder.setNeutralButton(neutralButtonText, (dialog, which) -> {
                // Send the neutral button event back to the host activity
                if (onDialogNeutralClickListener != null)
                    onDialogNeutralClickListener.onDialogNeutralClick();
            });
        }

        if (showNegativeButton) {
            builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                // Send the negative button event back to the host activity
                if (onDialogNegativeClickListener != null)
                    onDialogNegativeClickListener.onDialogNegativeClick();
            });
        }

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

    public void setPositiveButtonText(String positiveButtonText) {
        this.positiveButtonText = positiveButtonText;
    }

    public void setNeutralButtonText(String neutralButtonText) {
        this.neutralButtonText = neutralButtonText;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        if (message == null) {
            message = "";
        }
        this.message = message;
    }

    public void hideNegativeButton() {
        showNegativeButton = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(CUSTOM_STRING_STATE_KEY, customString);
        outState.putString(POSITIVE_BUTTON_TEXT_STATE_KEY, positiveButtonText);
        outState.putString(NEUTRAL_BUTTON_TEXT_STATE_KEY, neutralButtonText);
        outState.putBoolean(SHOW_NEGATIVE_BUTTON_STATE_KEY, showNegativeButton);
        outState.putString(TITLE_STATE_KEY, title);
        outState.putInt(ICON_STATE_KEY, icon);
        outState.putString(MESSAGE_STATE_KEY, message);
    }

    public interface OnDialogPositiveClickListener {
        void onDialogPositiveClick();
    }

    public interface OnDialogNeutralClickListener {
        void onDialogNeutralClick();
    }

    public interface OnDialogNegativeClickListener {
        void onDialogNegativeClick();
    }
}
