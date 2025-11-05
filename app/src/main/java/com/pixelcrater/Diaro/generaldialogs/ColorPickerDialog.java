package com.pixelcrater.Diaro.generaldialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.layouts.QustomDialogBuilder;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

import top.defaults.colorpicker.ColorPickerView;

public class ColorPickerDialog extends DialogFragment {

    private ColorPickerView colorPicker;
    private String initialColorCode;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        QustomDialogBuilder builder = new QustomDialogBuilder(getActivity());

        // Color
        builder.setHeaderBackgroundColor(MyThemesUtils.getPrimaryColorCode());

        // Title
        builder.setTitle(getString(R.string.pick_color));

        // Set custom view
        builder.setCustomView(R.layout.color_picker);
        View customView = builder.getCustomView();

        colorPicker = (ColorPickerView) customView.findViewById(R.id.color_picker);

        if (savedInstanceState == null) {
            colorPicker.setInitialColor(Color.parseColor(initialColorCode));
        }

        colorPicker.subscribe((color, fromUser, shouldPropagate) -> builder.setHeaderBackgroundColor(MyThemesUtils.getHexColor(color)));

        // OK
        builder.setPositiveButton(getString(android.R.string.ok), (dialog, which) -> {
            // Send the event back to the host activity
            if (onOkButtonClickListener != null) {
                onOkButtonClickListener.OnOkButtonClick(colorPicker.getColor());
            }
        });

        // Cancel
        builder.setNegativeButton(android.R.string.cancel, null);

        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Prevent dismiss on touch outside
        getDialog().setCanceledOnTouchOutside(false);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void setInitialColorCode(String initialColorCode) {
        this.initialColorCode = initialColorCode;
//        AppLog.d("initialColorCode: " + initialColorCode);
    }

    public interface OnOkButtonClickListener {
        void OnOkButtonClick(int color);
    }

    // OK button click listener
    private OnOkButtonClickListener onOkButtonClickListener;


    public void setOnOkButtonClickListener(OnOkButtonClickListener l) {
        onOkButtonClickListener = l;
    }
}
