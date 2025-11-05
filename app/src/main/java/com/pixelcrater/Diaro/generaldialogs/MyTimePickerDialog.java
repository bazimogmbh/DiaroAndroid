package com.pixelcrater.Diaro.generaldialogs;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.layouts.QustomDialogBuilder;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

public class MyTimePickerDialog extends DialogFragment implements TimePicker.OnTimeChangedListener {

    private static final String SELECTED_HOUR_STATE_KEY = "SELECTED_HOUR_STATE_KEY";
    private static final String SELECTED_MINUTE_STATE_KEY = "SELECTED_MINUTE_STATE_KEY";

    private View customView;
    private TimePicker timePicker;
    private int hour;
    private int minute;
    private boolean isSpinnerShown;
    private QustomDialogBuilder builder;

    // OK button click listener
    private OnDialogTimeSetListener onDialogTimeSetListener;

    public void setDialogTimeSetListener(OnDialogTimeSetListener l) {
        onDialogTimeSetListener = l;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            hour = savedInstanceState.getInt(SELECTED_HOUR_STATE_KEY);
            minute = savedInstanceState.getInt(SELECTED_MINUTE_STATE_KEY);
        }

        // Use the Builder class for convenient dialog construction
        builder = new QustomDialogBuilder(getActivity());

        // Color
        builder.setHeaderBackgroundColor(MyThemesUtils.getPrimaryColorCode());

        // Icon
        builder.setIcon(R.drawable.ic_access_time_white_24dp);

        // Time picker mode
        int layoutResId = R.layout.time_picker_spinner;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            layoutResId = R.layout.time_picker_clock;
        } else {
            isSpinnerShown = true;
        }

        // Set custom view
        builder.setCustomView(layoutResId);
        customView = builder.getCustomView();

        timePicker = (TimePicker) customView.findViewById(R.id.dialog_timepicker);
        timePicker.setIs24HourView(DateFormat.is24HourFormat(getActivity()));
        timePicker.setOnTimeChangedListener(this);

        // Set time
        timePicker.setCurrentHour(hour);
        timePicker.setCurrentMinute(minute);

        onTimeChanged(timePicker, hour, minute);

        // OK
        builder.setPositiveButton(getString(android.R.string.ok), (dialog, which) -> {
                    timePicker.clearFocus();

                    // Send the event back to the host activity
                    if (onDialogTimeSetListener != null) {
                        onDialogTimeSetListener.onDialogTimeSet(timePicker.getCurrentHour(), timePicker.getCurrentMinute());
                    }
                });

        // Cancel
        builder.setNegativeButton(android.R.string.cancel, null);

        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onResume() {
        if (!Static.isLandscape()) {
            Static.resizeDialog(getDialog());
        }

        // Call super onResume after sizing
        super.onResume();
    }

    @Override
    public void onTimeChanged(TimePicker view, int hour, int minute) {
        AppLog.d("hour: " + hour + ", minute: " + minute);

        if (isSpinnerShown) {
            updateTitle(hour, minute);
        }
    }

    private void updateTitle(int hour, int minute) {
        AppLog.d("hour: " + hour + ", minute: " + minute);

        String AM_PM = "";
        if (!timePicker.is24HourView()) {
            if (hour < 12) {
                AM_PM = " AM";
            } else {
                AM_PM = " PM";
            }

            if (hour == 0) {
                hour = 12;
            } else if (hour > 12) {
                hour -= 12;
            }
        }

        String hourStr = hour < 10 ? "0" + hour : "" + hour;
        String minuteStr = minute < 10 ? "0" + minute : "" + minute;

        builder.setTitle(hourStr + ":" + minuteStr + AM_PM);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Prevent dismiss on touch outside
        getDialog().setCanceledOnTouchOutside(false);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void setSelectedTime(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(SELECTED_HOUR_STATE_KEY, hour);
        outState.putInt(SELECTED_MINUTE_STATE_KEY, minute);
    }

    public interface OnDialogTimeSetListener {
        void onDialogTimeSet(int hour, int minute);
    }
}
