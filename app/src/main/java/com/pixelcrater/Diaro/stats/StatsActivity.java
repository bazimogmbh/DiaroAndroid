package com.pixelcrater.Diaro.stats;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.util.Pair;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.activitytypes.TypeActivity;
import com.pixelcrater.Diaro.analytics.AnalyticsConstants;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.GeneralUtils;
import com.pixelcrater.Diaro.utils.MyDateTimeUtils;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

import org.joda.time.DateTime;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StatsActivity extends TypeActivity {


    //Lists
    private List<BarEntry> list_entryByDay, list_wordByDay, list_avgMoodByDay = new ArrayList<>();
    private List<Entry> list_entryByMonth, list_wordByMonth = new ArrayList<>();
    private List<PieEntry> listMoodsCount = new ArrayList<>();

    // Charts
    private BarChart bc_entryByDay, bc_wordByDay, bc_avgMoodByDay;
    private LineChart lc_entryByMonth, lc_wordByMonth;
    private PieChart pc_moodsCount;

    private List<String> weekDays = new ArrayList<>();

    private ArrayList chartColors;

    private Spinner spinner;
    private TextView tv_dailyEntryCount, tv_dailyWordCount;

    private LinearLayout tvParent;
    private ImageView arrowPrev, arrowNext;
    private TextView infoTextView;

    int mCurselection = 0;

    DateTime yearDateTime = new DateTime();
    DateTime monthDateTime = new DateTime();

    private Typeface mMoodsFont;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(addViewToContentContainer(R.layout.stats));
        activityState.setLayoutBackgroundGray();
        activityState.setActionBarTitle(getSupportActionBar(), "Stats");

        mMoodsFont = Typeface.createFromAsset(getAssets(), "fonts/diaro_moods.ttf");

        weekDays = Arrays.asList(new DateFormatSymbols(getApplicationContext().getResources().getConfiguration().locale).getShortWeekdays()).subList(1, 8);

        tvParent = findViewById(R.id.tvParent);
        arrowPrev = findViewById(R.id.arrow_prev);
        arrowNext = findViewById(R.id.arrow_next);
        infoTextView = findViewById(R.id.info_text);

        arrowPrev.setOnClickListener(v -> {
            if (mCurselection == 0) {
                monthDateTime = monthDateTime.minusMonths(1);
            }
            if (mCurselection == 1) {
                yearDateTime = yearDateTime.minusYears(1);
            }

            handleSelection(mCurselection);
        });

        arrowNext.setOnClickListener(v -> {
            if (mCurselection == 0) {
                monthDateTime = monthDateTime.plusMonths(1);
            }
            if (mCurselection == 1) {
                yearDateTime = yearDateTime.plusYears(1);
            }
            handleSelection(mCurselection);
        });


        tv_dailyEntryCount = findViewById(R.id.tv_entryDaily);
        tv_dailyWordCount = findViewById(R.id.tv_wordDaily);

        spinner = findViewById(R.id.spinner_select_days);

        bc_entryByDay = findViewById(R.id.bc_entryByDay);
        lc_entryByMonth = findViewById(R.id.lc_entryByMonth);

        bc_wordByDay = findViewById(R.id.bc_wordByDay);
        lc_wordByMonth = findViewById(R.id.lc_wordByMonth);

        bc_avgMoodByDay = findViewById(R.id.bc_avgMoodByDay);
        pc_moodsCount = findViewById(R.id.pc_moodsCount);
        pc_moodsCount.setUsePercentValues(true);
        pc_moodsCount.getDescription().setEnabled(false);
        pc_moodsCount.setDrawHoleEnabled(true);
        pc_moodsCount.setHoleRadius(50f);
        pc_moodsCount.setTransparentCircleRadius(58f);
        pc_moodsCount.setHighlightPerTapEnabled(true);
        pc_moodsCount.setRotationEnabled(true);
        pc_moodsCount.setDrawCenterText(true);
        pc_moodsCount.setEntryLabelColor(MyThemesUtils.getTextColorDark());

        chartColors = new ArrayList<>();

        for (int c : ColorTemplate.MATERIAL_COLORS)
            chartColors.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS) {
            //&& c!=-7818053
            chartColors.add(c);
        }

        chartColors.add(ColorTemplate.getHoloBlue());

        String[] selectionArray = {getString(R.string.monthly), getString(R.string.yearly), getString(R.string.date_range), getString(R.string.filtered), getString(R.string.lifetime)};
        ArrayAdapter arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, selectionArray);
        spinner.setAdapter(arrayAdapter);

        int position = MyApp.getInstance().prefs.getInt(Prefs.PREF_STATS_SELECTION, 0);
        if (position < arrayAdapter.getCount()) {
            spinner.setSelection(position);
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCurselection = position;
                MyApp.getInstance().prefs.edit().putInt(Prefs.PREF_STATS_SELECTION, mCurselection).apply();
                handleSelection(mCurselection);
                try {
                    ((TextView) spinner.getSelectedView()).setTextColor(MyThemesUtils.getThemeAwareBWColor());
                } catch (Exception ignored) {

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                AppLog.e("onNothingSelected");
            }
        });

    }

    public void handleSelection(int position) {
        tvParent.setVisibility(View.GONE);
        arrowPrev.setVisibility(View.VISIBLE);
        arrowNext.setVisibility(View.VISIBLE);

        if (position == 0) { //monthly
            tvParent.setVisibility(View.VISIBLE);

            long startDate = MyDateTimeUtils.getStartOfTheDay(monthDateTime.dayOfMonth().withMinimumValue());
            long endDate = MyDateTimeUtils.getEndoftheDay(monthDateTime.dayOfMonth().withMaximumValue());

            String pattern = "MMM yyyy";
            String text = MyDateTimeUtils.getMillisAsString(startDate, pattern);
            infoTextView.setText(text);

            fetchData(startDate, endDate, false, false);
        }
        if (position == 1) { //yearly
            tvParent.setVisibility(View.VISIBLE);

            long startDate = MyDateTimeUtils.getStartOfTheDay(yearDateTime.dayOfYear().withMinimumValue());
            long endDate = MyDateTimeUtils.getEndoftheDay(yearDateTime.dayOfYear().withMaximumValue());

            String pattern = "yyyy";
            String text = MyDateTimeUtils.getMillisAsString(startDate, pattern);
            infoTextView.setText(text);

            fetchData(startDate, endDate, false, false);
        }
        if (position == 2) {  //data range
            tvParent.setVisibility(View.INVISIBLE);
            selectDateRange();
        }
        if (position == 3) { // filtered
            tvParent.setVisibility(View.INVISIBLE);
            fetchData(-1, -1, false, true);
        }
        if (position == 4) {  //lifetime
            tvParent.setVisibility(View.INVISIBLE);
            fetchData(-1, -1, true, false);
        }
    }

    private void fetchData(long startDate, long endDate, boolean lifeTime, boolean filtered) {

        AppLog.e("fetchData " + startDate + ", " + endDate + ", " + lifeTime + ", " + filtered);
        MyDateTimeUtils.printDate(startDate, endDate);

        list_entryByDay = StatsSqlHelper.getEntryByWeekday(startDate, endDate, lifeTime, filtered);
        int entry_count_weekly = 0;
        for (BarEntry e : list_entryByDay) {
            entry_count_weekly += e.getY();
        }
        tv_dailyEntryCount.setText(getString(R.string.entries_daily) + " : " + entry_count_weekly);
        list_entryByMonth = StatsSqlHelper.getEntryByMonth(startDate, endDate, lifeTime, filtered);
        list_wordByMonth = StatsSqlHelper.getWordByMonth(startDate, endDate, lifeTime, filtered);
        list_wordByDay = StatsSqlHelper.getWordByWeekday(startDate, endDate, lifeTime, filtered);

        int word_count_total_weekly = 0;
        for (BarEntry e : list_wordByDay) {
            word_count_total_weekly += e.getY();
        }
        tv_dailyWordCount.setText(getString(R.string.words_daily) + " : " + word_count_total_weekly);
        listMoodsCount = StatsSqlHelper.getMoodCount(startDate, endDate, lifeTime, filtered);
        list_avgMoodByDay = StatsSqlHelper.getMoodAvgByWeekday(startDate, endDate, lifeTime, filtered);

        // set the data
        StatsHelper.setDataToBarChart(bc_entryByDay, list_entryByDay, chartColors, weekDays);
        StatsHelper.setDataToLineChart(lc_entryByMonth, list_entryByMonth, "#46db6e");

        StatsHelper.setDataToBarChart(bc_wordByDay, list_wordByDay, chartColors, weekDays);
        StatsHelper.setDataToLineChart(lc_wordByMonth, list_wordByMonth, "#5ab4f4");

        StatsHelper.setDataToPieChart(pc_moodsCount, listMoodsCount, chartColors);
        StatsHelper.setDataToBarChart(bc_avgMoodByDay, list_avgMoodByDay, chartColors, weekDays);

        StatsHelper.modifYAxisForMoodsChart(bc_avgMoodByDay.getAxisLeft(), mMoodsFont, getApplicationContext());
    }

    private void selectDateRange() {
        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        int dialogTheme = MyThemesUtils.resolveOrThrow(this, com.google.android.material.R.attr.materialCalendarTheme);
        builder.setTheme(dialogTheme);
        MaterialDatePicker materialDatePicker = builder.build();
        materialDatePicker.show(getSupportFragmentManager(), materialDatePicker.toString());

        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            Pair<Long, Long> aPair = (Pair) selection;
            long startDate = MyDateTimeUtils.getStartOfTheDay(aPair.first);
            long endDate = MyDateTimeUtils.getEndoftheDay(aPair.second);

            String pattern = "dd MMM yy";
            String text = MyDateTimeUtils.getMillisAsString(startDate, pattern) + " - " + MyDateTimeUtils.getMillisAsString(endDate, pattern);
            infoTextView.setText(text);

            tvParent.setVisibility(View.VISIBLE);
            arrowPrev.setVisibility(View.GONE);
            arrowNext.setVisibility(View.GONE);
            materialDatePicker.dismiss();

            fetchData(startDate, endDate, false, false);
            // radio_date_range.setText(getString(R.string.select_range) + " ( " + materialDatePicker.getHeaderText() + " )");
        });

        materialDatePicker.addOnCancelListener(dialogInterface -> {
            materialDatePicker.dismiss();
            tvParent.setVisibility(View.GONE);
            arrowPrev.setVisibility(View.VISIBLE);
            arrowNext.setVisibility(View.VISIBLE);
            // radio_date_range.setText(R.string.select_range);
            // radioGroup.check(R.id.radio_all_entries);
        });

    }

    public void onClick(View view) {
        activityState.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_SHARE_STATS);
        int viewId = view.getId();

        if (viewId == R.id.share_entryDaily) {
            GeneralUtils.shareBitmap(StatsActivity.this, R.id.card_entryDaily, "Diaro Stats");
        }
        else if (viewId == R.id.share_entryMonthly) {
            GeneralUtils.shareBitmap(StatsActivity.this, R.id.card_entryMonthly, "Diaro Stats");
        }
        else if (viewId == R.id.share_wordDaily) {
            GeneralUtils.shareBitmap(StatsActivity.this, R.id.card_wordDaily, "Diaro Stats");
        }
        else if (viewId == R.id.share_wordMonthly) {
            GeneralUtils.shareBitmap(StatsActivity.this, R.id.card_wordMonthly, "Diaro Stats");
        }
        else if (viewId == R.id.share_moodCount) {
            GeneralUtils.shareBitmap(StatsActivity.this, R.id.card_moodCount, "Diaro Stats");
        }
        else if (viewId == R.id.share_avgMood) {
            GeneralUtils.shareBitmap(StatsActivity.this, R.id.card_avgMood, "Diaro Stats");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_share, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu != null) {
            VectorDrawableCompat vectorDrawableCompatSearch = VectorDrawableCompat.create(getResources(), R.drawable.ic_share_white_24dp, null);
            menu.findItem(R.id.item_share).setIcon(vectorDrawableCompatSearch);

        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (activityState.isActivityPaused) {
            return true;
        }

        // Share
        if (item.getItemId() == R.id.item_share) {
            GeneralUtils.shareBitmap(StatsActivity.this, R.id.layout_container, "Diaro Stats");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
