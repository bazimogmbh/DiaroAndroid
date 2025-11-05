package com.pixelcrater.Diaro.profile;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.layouts.QustomDialogBuilder;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

public class ForgotPasswordDialog extends DialogFragment {

    private androidx.appcompat.app.AlertDialog dialog;
    private EditText emailEditText;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        QustomDialogBuilder builder = new QustomDialogBuilder(getActivity());

        // Color
        builder.setHeaderBackgroundColor(MyThemesUtils.getPrimaryColorCode());

        // Title
        builder.setTitle(getActivity().getResources().getString(R.string.forgot_password));

        // Set custom view
        builder.setCustomView(R.layout.forgot_password_dialog);
        View customView = builder.getCustomView();

        emailEditText = (EditText) customView.findViewById(R.id.email);

        builder.setPositiveButton(android.R.string.ok, null);
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

            String email = emailEditText.getText().toString();

            if (email.equals("")) {
                Static.showToastError(getString(R.string.enter_diaro_account_email_error));
            } else if (!Static.isValidEmail(email)) {
                Static.showToastError(getString(R.string.invalid_email));
            } else if (!MyApp.getInstance().networkStateMgr.isConnectedToInternet()) {
                Static.showToastError(getString(R.string.error_internet_connection));
            } else {
                MyApp.getInstance().asyncsMgr.executeForgotPasswordAsync(getActivity(), email);
                dialog.dismiss();
            }
        });
    }
}
