package com.pixelcrater.Diaro.profile;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.layouts.QustomDialogBuilder;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

public class SignInDialog extends DialogFragment {

    private AlertDialog dialog;
    private EditText emailEditText;
    private EditText passwordEditText;

    // Positive button click listener
    private OnDialogPositiveClickListener onDialogPositiveClickListener;

    public void setDialogPositiveClickListener(OnDialogPositiveClickListener l) {
        onDialogPositiveClickListener = l;
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
        builder.setTitle(getActivity().getResources().getString(R.string.sign_in_with_diaro_account));

        // Set custom view
        builder.setCustomView(R.layout.sign_in_dialog);
        View customView = builder.getCustomView();

        emailEditText = (EditText) customView.findViewById(R.id.email);

        passwordEditText = (EditText) customView.findViewById(R.id.password);
        passwordEditText.setTypeface(Typeface.DEFAULT);

        View forgotPasswordLink = customView.findViewById(R.id.forgot_password);
        forgotPasswordLink.setOnClickListener(arg0 -> {
            dialog.dismiss();
            showForgotPasswordDialog();
        });

        builder.setPositiveButton(R.string.sign_in, null);
        builder.setNegativeButton(android.R.string.cancel, null);

        dialog = builder.create();

        // Create the AlertDialog object and return it
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Prevent dismiss on touch outside
        getDialog().setCanceledOnTouchOutside(false);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        button.setOnClickListener(v -> {
            AppLog.d("");

            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (email.equals("") || password.equals("")) {
                Static.showToastError(getString(R.string.empty_fields_error));
            } else if (!Static.isValidEmail(email)) {
                Static.showToastError(getString(R.string.invalid_email));
            } else if (!MyApp.getInstance().networkStateMgr.isConnectedToInternet()) {
                Static.showToastError(getString(R.string.error_internet_connection));
            } else {
                // Send the positive button event back to the host activity
                if (onDialogPositiveClickListener != null) {
                    onDialogPositiveClickListener.onDialogPositiveClick(email, password);
                }
            }
        });
    }

    private void showForgotPasswordDialog() {
        String dialogTag = Static.DIALOG_FORGOT_PASSWORD;
        if (getFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            ForgotPasswordDialog dialog = new ForgotPasswordDialog();
            dialog.show(getFragmentManager(), dialogTag);
        }
    }

    public interface OnDialogPositiveClickListener {
        void onDialogPositiveClick(String email, String password);
    }
}
