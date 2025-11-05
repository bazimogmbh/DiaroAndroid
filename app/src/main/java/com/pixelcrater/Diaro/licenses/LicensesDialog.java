package com.pixelcrater.Diaro.licenses;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.DialogFragment;

import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.layouts.QustomDialogBuilder;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

public class LicensesDialog extends DialogFragment {

    private View customView;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AppLog.d("");

        // Use the Builder class for convenient dialog construction
        QustomDialogBuilder builder = new QustomDialogBuilder(getActivity());

        // Color
        builder.setHeaderBackgroundColor(MyThemesUtils.getPrimaryColorCode());

        // Title
        builder.setTitle(getActivity().getResources().getString(R.string.licenses));

        // Set custom view
        builder.setCustomView(R.layout.licenses);
        customView = builder.getCustomView();

        // Add licenses
        new Licenses(getActivity(), (ViewGroup) customView.findViewById(R.id.licenses));

        builder.setNegativeButton(android.R.string.ok, (dialog, which) -> {
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
}
