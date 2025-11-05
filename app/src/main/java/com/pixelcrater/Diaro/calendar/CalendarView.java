package com.pixelcrater.Diaro.calendar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.util.ArrayList;
import java.util.Calendar;

public class CalendarView extends LinearLayout implements OnClickListener {

    private final TextView todayTextView;
    private final ViewGroup weekdaysHeaders;
    private final ViewGroup daysTable;
    private final TextView monthLabelTextView;
    private final TextView yearLabelTextView;
    private ArrayList<DateTime> daysGridDtArrayList;
    private GetDaysMarkersAsync getDaysMarkersAsync;
    private DateTime todayDt;
    private DateTime visibleDt;
    private int firstDayOfWeekInJodaTime;
    private long selectedRangeFromMillis;
    private long selectedRangeToMillis;
    private LayoutParams colorViewParams;
    private LayoutParams photoFrameParams;
    // OnDateRangeChangedListener
    private OnDateRangeChangedListener onDateRangeChangedListener;
    // OnDayClickedListener
    private OnDayClickedListener onDayClickedListener;

    public CalendarView(Context context) {
        super(context);

        // Oval color marker params
        int size = Static.getPixelsFromDip(5);
        int margin = Static.getPixelsFromDip(1);
        colorViewParams = new LinearLayout.LayoutParams(size, size);
        colorViewParams.setMargins(margin, 0, margin, 0);

        // Rectangle frame (entries in this day have photos) params
        photoFrameParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        // Set LayoutParams to calendar view
        LinearLayout.LayoutParams calendarParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        setLayoutParams(calendarParams);

        int firstDayOfWeek = MyApp.getInstance().prefs.getInt(Prefs.PREF_FIRST_DAY_OF_WEEK, Calendar.SUNDAY);

        // Get first day of week converted from Calendar constant to JodaTime constant
        firstDayOfWeekInJodaTime = Static.convertDayOfWeekFromCalendarToJodaTime(firstDayOfWeek);

        View v = LayoutInflater.from(context).inflate(R.layout.calendar, this, true);

        weekdaysHeaders = (ViewGroup) v.findViewById(R.id.weekdays_headers);
        daysTable = (ViewGroup) v.findViewById(R.id.days_table);

        monthLabelTextView = (TextView) v.findViewById(R.id.month_label);

        ImageButton prevMonthButton = (ImageButton) v.findViewById(R.id.previous_month);
        prevMonthButton.setOnClickListener(this);

        ImageButton nextMonthButton = (ImageButton) v.findViewById(R.id.next_month);
        nextMonthButton.setOnClickListener(this);

        yearLabelTextView = (TextView) v.findViewById(R.id.year_label);

        ImageButton prevYearButton = (ImageButton) v.findViewById(R.id.previous_year);
        prevYearButton.setOnClickListener(this);

        ImageButton nextYearButton = (ImageButton) v.findViewById(R.id.next_year);
        nextYearButton.setOnClickListener(this);

        todayTextView = (TextView) v.findViewById(R.id.today);
//        todayTextView.setOnClickListener(this);

        // Weekdays headers
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

            TextView dayTextView = (TextView) weekdaysHeaders.getChildAt(i);
            dayTextView.setText(Static.getDayOfWeekShortTitle(dayCode));
            dayTextView.setTextColor(MyThemesUtils.getTextColor());
        }
    }

    public TextView getMonthLabelTextView() {
        return monthLabelTextView;
    }

    public TextView getYearLabelTextView() {
        return yearLabelTextView;
    }

    public TextView getTodayTextView() {
        return todayTextView;
    }

    public DateTime getTodayDt() {
        return todayDt;
    }

    public void setOnDateRangeChangedListener(OnDateRangeChangedListener l) {
        onDateRangeChangedListener = l;
    }

    public void setOnDayClickedListener(OnDayClickedListener l) {
        onDayClickedListener = l;
    }

    public DateTime getVisibleDt() {
        return visibleDt;
    }

    public void setVisibleDt(DateTime dt) {
        if (dt == null) {
            dt = new DateTime().withDayOfMonth(1).withTimeAtStartOfDay();
        }

        if (visibleDt == null || visibleDt.getMillis() != dt.getMillis()) {
            visibleDt = dt;
            drawDayCells();
        }
    }

    public long getSelectedRangeFromMillis() {
        return selectedRangeFromMillis;
    }

    public long getSelectedRangeToMillis() {
        return selectedRangeToMillis;
    }

    public void clearSelectedRange() {
        setSelectedDateRange(0, 0);
    }

    public void clearSelectedRangeFrom() {
        setSelectedDateRange(0, selectedRangeToMillis);
    }

    public void clearSelectedRangeTo() {
        setSelectedDateRange(selectedRangeFromMillis, 0);
    }

    private void clearDayMarkers(ViewGroup dayContainerViewGroup) {
        // Clear photo frame
        View photoFrameView = dayContainerViewGroup.findViewWithTag("photo_frame");
        if (photoFrameView != null) {
            dayContainerViewGroup.removeView(photoFrameView);
        }

        // Clear folder markers
        LinearLayout markersLinearLayout = (LinearLayout) dayContainerViewGroup.getChildAt(1);
        markersLinearLayout.removeAllViews();
    }

    private void showSelectedDateRangeOnCalendar() {
        int row = 0;
        int col = 0;

        int size = daysGridDtArrayList.size();
        for (int d = 0; d < size; d++) {
            DateTime dayDt = daysGridDtArrayList.get(d);

            ViewGroup tr = (ViewGroup) daysTable.getChildAt(row);
            ViewGroup dayContainerViewGroup = (ViewGroup) tr.getChildAt(col);

            if (dayDt.getMillis() >= selectedRangeFromMillis && (dayDt.getMillis() <= selectedRangeToMillis || (selectedRangeToMillis == 0 && selectedRangeFromMillis > 0))) {
                dayContainerViewGroup.setBackgroundColor(MyThemesUtils.getOverlayPrimaryColor());
            } else {
                dayContainerViewGroup.setBackgroundResource(R.drawable.bg_ripple);
            }

            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }
    }

    public void setSelectedDateRange(long selectedRangeFromMillis, long selectedRangeToMillis) {
        this.selectedRangeFromMillis = selectedRangeFromMillis;
        this.selectedRangeToMillis = selectedRangeToMillis;

        showSelectedDateRangeOnCalendar();
        if (onDateRangeChangedListener != null) {
            onDateRangeChangedListener.onDateRangeChanged();
        }
    }

    public void showMarkersOnDays(ArrayList<DayMarker> calendarDayMarkersArrayList) {
        AppLog.d("");

        int row = 0;
        int col = 0;

        int size = daysGridDtArrayList.size();
        for (int d = 0; d < size; d++) {
            DateTime dayDt = daysGridDtArrayList.get(d);

            ViewGroup tr = (ViewGroup) daysTable.getChildAt(row);
            ViewGroup dayContainerViewGroup = (ViewGroup) tr.getChildAt(col);

            // Clear current markers, if any
            clearDayMarkers(dayContainerViewGroup);

            for (int m = 0; m < calendarDayMarkersArrayList.size(); m++) {
                DayMarker dayMarker = calendarDayMarkersArrayList.get(m);
                if (dayMarker.dayDt.getMillis() == dayDt.getMillis()) {
                    // Mark day with photos
                    if (dayMarker.dayPhotoCount > 0) {
                        // Create rectangle frame (entries in this day have photos)
                        LinearLayout rectangleView = new LinearLayout(MyApp.getInstance());
                        rectangleView.setBackgroundResource(R.drawable.rectangle);
                        rectangleView.setTag("photo_frame");

                        dayContainerViewGroup.addView(rectangleView, photoFrameParams);
                    }

                    ViewGroup dayMarkersViewGroup = (ViewGroup) dayContainerViewGroup.getChildAt(1);

                    for (int c = 0; c < dayMarker.dayFolderColorsArrayList.size(); c++) {
                        // Create oval color marker
                        ImageView colorView = new ImageView(MyApp.getInstance());
                        colorView.setImageResource(R.drawable.oval);

                        int color = getParsedColor(dayMarker.dayFolderColorsArrayList.get(c));
                        colorView.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

                        dayMarkersViewGroup.addView(colorView, colorViewParams);
                    }

                    calendarDayMarkersArrayList.remove(m);
                    m--;
                }
            }

            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }
    }

    private int getParsedColor(String color) {
        try {
            return Color.parseColor(color);
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }
        // No folder color in calendar
        return R.color.grey_500;
    }

    public void drawDayCells() {
        AppLog.d("");

        monthLabelTextView.setText(Static.getMonthShortTitle(visibleDt.getMonthOfYear()));
        yearLabelTextView.setText(String.valueOf(visibleDt.getYear()));

        daysGridDtArrayList = generateDaysGridDtArrayList();

        int row = 0;
        int col = 0;

        todayDt = new DateTime().withTimeAtStartOfDay();

        int size = daysGridDtArrayList.size();
        for (int d = 0; d < size; d++) {
            final DateTime dayDt = daysGridDtArrayList.get(d);

            ViewGroup tr = (ViewGroup) daysTable.getChildAt(row);
            ViewGroup dayContainerViewGroup = (ViewGroup) tr.getChildAt(col);

            dayContainerViewGroup.setOnClickListener(v -> {
                if (onDayClickedListener != null) {
                    onDayClickedListener.onDayClicked(dayDt);
                }
                if (onDateRangeChangedListener != null) {
                    onDateRangeChangedListener.onDateRangeChanged();
                }
            });

            TextView dayTextView = (TextView) dayContainerViewGroup.getChildAt(0);
            dayTextView.setText(String.valueOf(dayDt.getDayOfMonth()));

            // Day text color
            int cellTextColor = getResources().getColor(R.color.grey_600);
            int typeface = Typeface.NORMAL;
            if (dayDt.getMonthOfYear() != visibleDt.getMonthOfYear()) {
                cellTextColor = getResources().getColor(R.color.grey_500);
            }
            if (dayDt.getMillis() == todayDt.getMillis()) {
                cellTextColor = MyThemesUtils.getThemeAwarePrimartyTextColor();
                typeface = Typeface.BOLD;
            }
            dayTextView.setTextColor(cellTextColor);
            dayTextView.setTypeface(null, typeface);

            // Clear current markers, if any
            clearDayMarkers(dayContainerViewGroup);

            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }

        showSelectedDateRangeOnCalendar();

        // Get calendar days markers
        executeGetDaysMarkersAsync();
    }

    private int getFirstDayOfMonthPositionInGrid() {
        int position = 0;
        int dayOfWeek = visibleDt.getDayOfWeek();

        if (firstDayOfWeekInJodaTime == DateTimeConstants.SUNDAY) {
            if (dayOfWeek == DateTimeConstants.SUNDAY) {
                position = 0;
            } else {
                position = dayOfWeek;
            }
        } else if (firstDayOfWeekInJodaTime == DateTimeConstants.MONDAY) {
            position = dayOfWeek - 1;
        } else if (firstDayOfWeekInJodaTime == DateTimeConstants.SATURDAY) {
            if (dayOfWeek == DateTimeConstants.SATURDAY) {
                position = 0;
            } else if (dayOfWeek == DateTimeConstants.SUNDAY) {
                position = 1;
            } else {
                position = dayOfWeek + 1;
            }
        }

        return position;
    }

    public ArrayList<DateTime> generateDaysGridDtArrayList() {
        daysGridDtArrayList = new ArrayList<>();

        //		DateTime lastDayOfMonthDt = visibleDt.withDayOfMonth(visibleDt.dayOfMonth().getMaximumValue());
        int firstDayOfMonthPositionInGrid = getFirstDayOfMonthPositionInGrid();

        for (int i = 0; i < 42; i++) {
            DateTime dayDt;
            if (i < firstDayOfMonthPositionInGrid) {
                dayDt = visibleDt.minusDays(firstDayOfMonthPositionInGrid - i);
            } else {
                dayDt = visibleDt.plusDays(i - firstDayOfMonthPositionInGrid);
            }

            daysGridDtArrayList.add(dayDt);
        }

        return daysGridDtArrayList;
    }

    public void executeGetDaysMarkersAsync() {
        cancelGetDaysMarkersAsync();

        getDaysMarkersAsync = new GetDaysMarkersAsync(CalendarView.this, daysGridDtArrayList);
        // Execute on a separate thread
        Static.startMyTask(getDaysMarkersAsync);
    }

    public void cancelGetDaysMarkersAsync() {
        try {
            if (getDaysMarkersAsync != null) {
                getDaysMarkersAsync.setCancelled(true);
                getDaysMarkersAsync.cancel(true);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.previous_month:
                setVisibleDt(visibleDt.minusMonths(1));
                break;

            case R.id.next_month:
                setVisibleDt(visibleDt.plusMonths(1));
                break;

            case R.id.previous_year:
                setVisibleDt(visibleDt.minusYears(1));
                break;

            case R.id.next_year:
                setVisibleDt(visibleDt.plusYears(1));
                break;

            case R.id.today:
                setVisibleDt(null);
                break;

            default:
                break;
        }
    }

    public interface OnDateRangeChangedListener {
        void onDateRangeChanged();
    }

    public interface OnDayClickedListener {
        void onDayClicked(DateTime dayDt);
    }
}
