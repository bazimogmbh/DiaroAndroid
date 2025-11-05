package com.pixelcrater.Diaro.generaldialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.layouts.QustomDialogBuilder;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

import org.joda.time.DateTime;

import java.util.Calendar;

public class MyDatePickerDialog extends DialogFragment implements DatePicker.OnDateChangedListener {

    // State vars
    private static final String SELECTED_YEAR_STATE_KEY = "SELECTED_YEAR_STATE_KEY";
    private static final String SELECTED_MONTH_STATE_KEY = "SELECTED_MONTH_STATE_KEY";
    private static final String SELECTED_DAY_STATE_KEY = "SELECTED_DAY_STATE_KEY";
    private static final String SHOW_TODAY_BUTTON_STATE_KEY = "SHOW_TODAY_BUTTON_STATE_KEY";
    private static final String MIN_DATE_STATE_KEY = "MIN_DATE_STATE_KEY";
    private static final String MAX_DATE_STATE_KEY = "MAX_DATE_STATE_KEY";

    private DatePicker datePicker;
    private DateTime dt;
    private int year;
    private int month;
    private int day;
    private boolean showTodayButton = false;
    private long minDateMillis = -1;
    private long maxDateMillis = -1;
    private QustomDialogBuilder builder;
    private boolean isSpinnerShown;

    // OK button click listener
    private OnDialogDateSetListener onDialogDateSetListener;

    public void setDialogDateSetListener(OnDialogDateSetListener l) {
        onDialogDateSetListener = l;
    }

    @NonNull
    @SuppressLint("NewApi")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // If date picker bug with translations on Samsung devices with Lollipop
        if (isBrokenSamsungDevice()) {
            Static.setLocale("en");
        }

        if (savedInstanceState != null) {
            year = savedInstanceState.getInt(SELECTED_YEAR_STATE_KEY);
            month = savedInstanceState.getInt(SELECTED_MONTH_STATE_KEY);
            day = savedInstanceState.getInt(SELECTED_DAY_STATE_KEY);
            showTodayButton = savedInstanceState.getBoolean(SHOW_TODAY_BUTTON_STATE_KEY);
            minDateMillis = savedInstanceState.getLong(MIN_DATE_STATE_KEY);
            maxDateMillis = savedInstanceState.getLong(MAX_DATE_STATE_KEY);
        }

        // Use the Builder class for convenient dialog construction
        builder = new QustomDialogBuilder(getActivity());

        // Color
        builder.setHeaderBackgroundColor(MyThemesUtils.getPrimaryColorCode());

        // Icon
        builder.setIcon(R.drawable.ic_today_white_24dp);

        // Date picker mode
        int layoutResId = R.layout.date_picker_spinner;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            layoutResId = R.layout.date_picker_calendar;
        } else {
            isSpinnerShown = true;
        }

        // Set custom view
        builder.setCustomView(layoutResId);
        View customView = builder.getCustomView();

        if (year < 1900) {
            year = 1900;
        }
        if (year > 2100) {
            year = 2100;
        }

        datePicker = (DatePicker) customView.findViewById(R.id.dialog_datepicker);

        if (!isSpinnerShown) {
            // Set calendar first day of week
            int firstDayOfWeek = MyApp.getInstance().prefs.getInt(Prefs.PREF_FIRST_DAY_OF_WEEK, Calendar.SUNDAY);
            datePicker.setFirstDayOfWeek(firstDayOfWeek);
        }

        // Init to check min/max date onDateChanged
        datePicker.init(year, month - 1, day, this);
        onDateChanged(datePicker, year, month - 1, day);

        // OK
        builder.setPositiveButton(getString(android.R.string.ok), (dialog, which) -> {
            // AppLog.d("datePicker.getYear(): " + datePicker.getYear() +", datePicker.getMonth(): " + datePicker.getMonth() +
            // ", datePicker.getDayOfMonth(): " +datePicker.getDayOfMonth());

            datePicker.clearFocus();

            // Send the event back to the host activity
            if (onDialogDateSetListener != null) {
                checkAndCorrectDate(datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth());
                onDialogDateSetListener.onDialogDateSet(dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth());
            }
        });

        if (showTodayButton) {
            // Today
            builder.setNeutralButton(getString(R.string.today), (dialog, which) -> {
                DateTime todayDt = new DateTime();

                // Send the event back to the host activity
                if (onDialogDateSetListener != null) {
                    onDialogDateSetListener.onDialogDateSet(todayDt.getYear(), todayDt.getMonthOfYear(), todayDt.getDayOfMonth());
                }
            });
        }

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
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        // Set back locale from preferences
        Static.refreshLocale();
    }

    private boolean isBrokenSamsungDevice() {
        return (Build.MANUFACTURER.equalsIgnoreCase("samsung") && isBetweenAndroidVersions(Build.VERSION_CODES.LOLLIPOP, Build.VERSION_CODES.LOLLIPOP_MR1));
    }

    private boolean isBetweenAndroidVersions(int min, int max) {
        return Build.VERSION.SDK_INT >= min && Build.VERSION.SDK_INT <= max;
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int month, int day) {
        // AppLog.d("year: " + year + ", month: " + month + ", day: " + day);
        checkAndCorrectDate(year, month + 1, day);

        if (isSpinnerShown) {
            updateTitle();
        }
    }

    private void checkAndCorrectDate(int year, int month, int day) {
        long newMillis = new DateTime().withTimeAtStartOfDay().withDate(year, month, day).getMillis();
        dt = new DateTime(newMillis);

        if (minDateMillis != -1 && newMillis < minDateMillis) {
            dt = new DateTime(minDateMillis);
            datePicker.init(dt.getYear(), dt.getMonthOfYear() - 1, dt.getDayOfMonth(), this);
        }
        if (maxDateMillis != -1 && newMillis > maxDateMillis) {
            dt = new DateTime(maxDateMillis);
            datePicker.init(dt.getYear(), dt.getMonthOfYear() - 1, dt.getDayOfMonth(), this);
        }
    }

    private void updateTitle() {
        // Day of week
        int dayOfWeek = dt.getDayOfWeek();
        String dateWD = Static.getDayOfWeekShortTitle(Static.convertDayOfWeekFromJodaTimeToCalendar(dayOfWeek));

        builder.setTitle(dateWD + ", " + Static.getMonthShortTitle(dt.getMonthOfYear()) + " " + dt.getDayOfMonth() + ", " + dt.getYear());
    }

    public long getMaxDateMillis() {
        return maxDateMillis;
    }

    public void setMaxDateMillis(long maxDateMillis) {
        this.maxDateMillis = maxDateMillis;
    }

    public long getMinDateMillis() {
        return minDateMillis;
    }

    public void setMinDateMillis(long minDateMillis) {
        this.minDateMillis = minDateMillis;
    }

    public void setSelectedDate(int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public void setShowTodayButton(boolean showTodayButton) {
        this.showTodayButton = showTodayButton;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(SELECTED_YEAR_STATE_KEY, dt.getYear());
        outState.putInt(SELECTED_MONTH_STATE_KEY, dt.getMonthOfYear());
        outState.putInt(SELECTED_DAY_STATE_KEY, dt.getDayOfMonth());
        outState.putBoolean(SHOW_TODAY_BUTTON_STATE_KEY, showTodayButton);
        outState.putLong(MIN_DATE_STATE_KEY, minDateMillis);
        outState.putLong(MAX_DATE_STATE_KEY, maxDateMillis);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Prevent dismiss on touch outside
        getDialog().setCanceledOnTouchOutside(false);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public interface OnDialogDateSetListener {
        void onDialogDateSet(int year, int month, int day);
    }
}