package com.pixelcrater.Diaro.settings;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.permissionx.guolindev.PermissionX;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.activitytypes.TypeActivity;
import com.pixelcrater.Diaro.brreceivers.TimeToWriteAlarmBrReceiver;
import com.pixelcrater.Diaro.generaldialogs.MyTimePickerDialog;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.MyDateTimeUtils;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class TimeToWriteNotificationActivity extends TypeActivity {

    private LinearLayout weekdaysLayout;
    private TextView timeTextView;
    private CheckBox onOffCheckbox;
    private CheckBox muteSoundCheckbox;
    private CheckBox smartReminderCheckbox;
    private int hh;
    private int mm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(addViewToContentContainer(R.layout.time_to_write_notification));
        activityState.setLayoutBackground();

        // Set activity title and subtitle

        activityState.setActionBarTitle(getSupportActionBar(), getString(R.string.settings_reminders));

        // Enable/Disable
        onOffCheckbox = (CheckBox) findViewById(R.id.on_off_checkbox);
        onOffCheckbox.setChecked(MyApp.getInstance().prefs.getBoolean(Prefs.PREF_TIME_TO_WRITE_NOTIFICATION_ENABLED, true));

        onOffCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_TIME_TO_WRITE_NOTIFICATION_ENABLED, isChecked).apply();

                // Register next 'Time to write' notification in AlarmManager
                TimeToWriteAlarmBrReceiver.scheduleNextTimeToWriteAlarm();

                updateUi();
            }
        });

        // Add weekdays
        initWeekdays();

        // - Time -
        timeTextView = (TextView) findViewById(R.id.time);
        timeTextView.setOnClickListener(v -> showTimePickerDialog());

        // Time from preferences
        long timeFromPrefs = MyApp.getInstance().prefs.getLong(Prefs.PREF_TIME_TO_WRITE_NOTIFICATION_TIME, 0);

        // Default time
        hh = 20;
        mm = 0;

        // If time is set in preferences
        if (timeFromPrefs > 0) {
            DateTime dt = new DateTime(timeFromPrefs);

            hh = dt.getHourOfDay();
            mm = dt.getMinuteOfHour();
        }

        AppLog.d("hh: " + hh + ", mm: " + mm);

        showTime();

        // Mute sound
        muteSoundCheckbox = (CheckBox) findViewById(R.id.mute_sound_checkbox);
        muteSoundCheckbox.setChecked(MyApp.getInstance().prefs.getBoolean(Prefs.PREF_TIME_TO_WRITE_NOTIFICATION_MUTE_SOUND, false));

        muteSoundCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_TIME_TO_WRITE_NOTIFICATION_MUTE_SOUND, isChecked).apply();

            // Register next 'Time to write' notification in AlarmManager
            TimeToWriteAlarmBrReceiver.scheduleNextTimeToWriteAlarm();
        });

        // Smart reminder
        smartReminderCheckbox = (CheckBox) findViewById(R.id.smart_reminder_checkbox);
        smartReminderCheckbox.setChecked(MyApp.getInstance().prefs.getBoolean(Prefs.PREF_TIME_TO_WRITE_NOTIFICATION_SMART_REMINDER, true));

        smartReminderCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            MyApp.getInstance().prefs.edit().putBoolean(Prefs.PREF_TIME_TO_WRITE_NOTIFICATION_SMART_REMINDER, isChecked).apply();

            // Register next 'Time to write' notification in AlarmManager
            TimeToWriteAlarmBrReceiver.scheduleNextTimeToWriteAlarm();
        });

        updateUi();

        restoreDialogListeners();

        askNotificationPermission();
    }

    public void restoreDialogListeners() {
        MyTimePickerDialog dialog1 = (MyTimePickerDialog) getSupportFragmentManager().findFragmentByTag(Static.DIALOG_PICKER_TIME);
        if (dialog1 != null) {
            setTimePickerDialogListener(dialog1);
        }
    }

    private void updateUi() {
        if (onOffCheckbox.isChecked()) {
            onOffCheckbox.setText(R.string.turned_on);
        } else {
            onOffCheckbox.setText(R.string.turned_off);
        }

        timeTextView.setEnabled(onOffCheckbox.isChecked());
        muteSoundCheckbox.setEnabled(onOffCheckbox.isChecked());
        smartReminderCheckbox.setEnabled(onOffCheckbox.isChecked());
    }

    private void initWeekdays() {
        // - Weekdays -
        weekdaysLayout = (LinearLayout) findViewById(R.id.weekdays);

        LayoutInflater inflater = getLayoutInflater();

        // Weekdays from preferences, 1 - SUNDAY, 2 - MONDAY, ..., 7 - SATURDAY
        String serializedWeekdays = MyApp.getInstance().prefs.getString(Prefs.PREF_TIME_TO_WRITE_NOTIFICATION_WEEKDAYS, "1,2,3,4,5,6,7");
//		AppLog.d("serializedWeekdays: " + serializedWeekdays);

        ArrayList<String> selectedWeekdaysArrayList = new ArrayList<>();
        if (StringUtils.isNotEmpty(serializedWeekdays)) {
            selectedWeekdaysArrayList = new ArrayList<>(Arrays.asList(serializedWeekdays.split(",")));
        }

        int firstDayOfWeek = MyApp.getInstance().prefs.getInt(Prefs.PREF_FIRST_DAY_OF_WEEK, Calendar.SUNDAY);

        for (int i = 0; i < 7; i++) {
            int dayCode = firstDayOfWeek + i;
            if (firstDayOfWeek == Calendar.MONDAY && i == 6) {
                dayCode = Calendar.SUNDAY;
            }
            if (firstDayOfWeek == Calendar.SATURDAY) {
                if (i == 0) {
                    dayCode = firstDayOfWeek;
                } else {
                    dayCode = i;
                }
            }

            LinearLayout weekdayContainer = new LinearLayout(TimeToWriteNotificationActivity.this);
            LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            weekdayContainer.setLayoutParams(lp);

            inflater.inflate(R.layout.checkbox_weekday, weekdayContainer);
            LinearLayout weekday = weekdayContainer.findViewById(R.id.weekday);

            // Set tag for each weekday
            String tag = String.valueOf(dayCode);
//            AppLog.d("tag: " + tag);
            weekday.setTag(tag);

            TextView weekdayLabel = (TextView) weekday.findViewById(R.id.weekday_label);
            weekdayLabel.setText(Static.getDayOfWeekShortTitle(dayCode).toUpperCase(Locale.getDefault()));

            weekday.setOnClickListener(v -> {
                if (!onOffCheckbox.isChecked()) {
                    return;
                }

                String tag1 = (String) v.getTag();

                ArrayList<String> selectedWeekdaysArrayList1 = getSelectedWeekdaysArrayList();
//                    AppLog.d("selectedWeekdaysArrayList: " + selectedWeekdaysArrayList);

                if (!selectedWeekdaysArrayList1.contains(tag1)) {
                    selectWeekday(tag1);
                } else {
                    unselectWeekday(tag1);
                }

                // Weekdays
                selectedWeekdaysArrayList1 = getSelectedWeekdaysArrayList();
   //                AppLog.e("selectedWeekdaysArrayList: " + selectedWeekdaysArrayList1.toString());

                String serializedWeekDays = "";
                if (selectedWeekdaysArrayList1.size() > 0) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        serializedWeekDays = StringUtils.join(selectedWeekdaysArrayList1, ",");
                    } else {
                        serializedWeekDays = selectedWeekdaysArrayList1.toString().replace("[", "").replace("]", "").replace(", ", ",");
                    }

                }
                // Save selected days of week to preferences, 1 - SUNDAY, 2 - MONDAY, ..., 7 - SATURDAY
                MyApp.getInstance().prefs.edit().putString(Prefs.PREF_TIME_TO_WRITE_NOTIFICATION_WEEKDAYS, serializedWeekDays).apply();

                // Register next 'Time to write' notification in AlarmManager
                TimeToWriteAlarmBrReceiver.scheduleNextTimeToWriteAlarm();
            });
            weekdaysLayout.addView(weekdayContainer);

            // Mark active
            if (selectedWeekdaysArrayList.contains(tag)) {
                selectWeekday(tag);
            } else {
                unselectWeekday(tag);
            }
        }
    }

    private ArrayList<String> getSelectedWeekdaysArrayList() {
        ArrayList<String> selectedWeekdaysArrayList = new ArrayList<>();

        for (int i = 1; i <= 7; i++) {
            String tag = String.valueOf(i);
            LinearLayout weekday = weekdaysLayout.findViewWithTag(tag);
            CheckBox weekdayCheckBox = weekday.findViewById(R.id.weekday_checkbox);

            if (weekdayCheckBox.isChecked()) {
                selectedWeekdaysArrayList.add(tag);
            }
        }

        return selectedWeekdaysArrayList;
    }

    private void selectWeekday(String tag) {
        LinearLayout weekDay =  weekdaysLayout.findViewWithTag(tag);
        CheckBox weekDayCheckBox = weekDay.findViewById(R.id.weekday_checkbox);
        LinearLayout weekDayLine =  weekDay.findViewById(R.id.weekday_line);
        TextView weekDayLabel =  weekDay.findViewById(R.id.weekday_label);

        weekDayCheckBox.setChecked(true);

        int activeTextColor = MyThemesUtils.getAccentColor();
        int activeLineColor = MyThemesUtils.getAccentColor();

        weekDayLabel.setTextColor(activeTextColor);
        weekDayLine.setBackgroundColor(activeLineColor);
    }

    private void unselectWeekday(String tag) {
        LinearLayout weekDay =  weekdaysLayout.findViewWithTag(tag);
        CheckBox weekDayCheckBox =  weekDay.findViewById(R.id.weekday_checkbox);
        LinearLayout weekdayLine =  weekDay.findViewById(R.id.weekday_line);
        TextView weekDayLabel =  weekDay.findViewById(R.id.weekday_label);

        weekDayCheckBox.setChecked(false);

        int regularTextColor = getResources().getColor(R.color.grey_500);
        int regularLineColor = getResources().getColor(R.color.grey_500);

        weekDayLabel.setTextColor(regularTextColor);
        weekdayLine.setBackgroundColor(regularLineColor);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (activityState.isActivityPaused) {
            return true;
        }

        // Handle presses on the action bar items
        switch (item.getItemId()) {
            // Back
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Time picker dialog
     */

    public void showTimePickerDialog() {
        String dialogTag = Static.DIALOG_PICKER_TIME;
        if (getSupportFragmentManager().findFragmentByTag(dialogTag) == null) {
            // Show dialog
            MyTimePickerDialog dialog = new MyTimePickerDialog();
            dialog.setSelectedTime(hh, mm);
            dialog.show(getSupportFragmentManager(), dialogTag);

            // Set dialog listener
            setTimePickerDialogListener(dialog);
        }
    }

    public void setTimePickerDialogListener(MyTimePickerDialog dialog) {
        dialog.setDialogTimeSetListener(new MyTimePickerDialog.OnDialogTimeSetListener() {

            @Override
            public void onDialogTimeSet(int hour, int minute) {
                AppLog.d("hour: " + hour + ", minute: " + minute);

                if (hh != hour || mm != minute) {
                    hh = hour;
                    mm = minute;

                    showTime();

                    DateTime dt = new DateTime().withHourOfDay(hh).withMinuteOfHour(mm);

                    // Save to preferences
                    MyApp.getInstance().prefs.edit().putLong(Prefs.PREF_TIME_TO_WRITE_NOTIFICATION_TIME, dt.getMillis()).apply();

                    // Register next 'Time to write' notification in AlarmManager
                    TimeToWriteAlarmBrReceiver.scheduleNextTimeToWriteAlarm();
                }
            }
        });
    }

    private void showTime() {
        timeTextView.setText(new DateTime().withTime(hh, mm, 0, 0).toString(MyDateTimeUtils.getTimeFormat()));
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionX.init(this)
                    .permissions(Manifest.permission.POST_NOTIFICATIONS)
                    .request((allGranted, grantedList, deniedList) -> AppLog.e("NotificationPermission granted"));
        }
    }

}
