package com.pixelcrater.Diaro.stats;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.pixelcrater.Diaro.moods.Mood;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.Static;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class StatsHelper {

    public static void setDataToLineChart(LineChart lineChart, List<Entry> lineEntry, String color) {

        /**for(Entry e : lineEntry)
         AppLog.e(e.getX() + " " + e.getY());**/

        ValueFormatter valueFormatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                // There is a bug in getAxisLabel if valaue is 1.0 , it returns value between 1.0 and 1.9 , so we multiply and divide by 10
                int monthIndex = ((int) value) / 10;
                return Static.getMonthShortTitle(monthIndex);
            }
        };

        LineDataSet lineDataSet = new LineDataSet(lineEntry, "");
        lineDataSet.setValueFormatter(new DecimalRemover(new DecimalFormat("#")));
        lineDataSet.setLineWidth(2f);
       // lineDataSet.setCircleRadius(4f);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setColors(Color.parseColor(color));
        lineDataSet.setCircleColor(Color.parseColor(color));


        lineDataSet.setHighLightColor(Color.parseColor("#ff5131"));
        lineDataSet.setHighlightEnabled(true);
        lineDataSet.setDrawHighlightIndicators(true);
        lineDataSet.setDrawHorizontalHighlightIndicator(false);

      //  lineDataSet.setDrawCircles(false);
      //  lineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        lineDataSet.setDrawFilled(false);
        lineDataSet.setValueTextColor(MyThemesUtils.getTextColorDark());
        lineDataSet.setValueTextSize(12);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setLabelCount(12);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(valueFormatter);
        xAxis.setTextColor(MyThemesUtils.getTextColorDark());

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setEnabled(true);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(MyThemesUtils.getTextColorDark());

        // chart properties
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setDrawBorders(false);

        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);
        Legend l = lineChart.getLegend();
        l.setEnabled(false);

        lineChart.invalidate();
    }


    static void modifYAxisForMoodsChart(YAxis yAxisLeft,Typeface moodsFont,  Context ctx) {
        yAxisLeft.setGranularity(1f);
        yAxisLeft.setAxisMinimum(0);
        yAxisLeft.setAxisMaximum(5);
        yAxisLeft.setSpaceTop(15f);
        yAxisLeft.setTextColor(MyThemesUtils.getTextColorDark());
        ValueFormatter valueFormatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                int moodId = 6 - (int) value;
                Mood mood = new Mood(moodId);
                if (mood.getMoodTextResId() == 0) {
                    return "";
                } else {
                    String retVal = "";
                    try {
                        retVal = ctx.getString(mood.getFontResId());
                    } catch (Exception e) {
                        retVal = "";
                        AppLog.e(" mood int was " + moodId);
                    }
                    return retVal;
                }
            }
        };
        yAxisLeft.setValueFormatter(valueFormatter);
        yAxisLeft.setTypeface(moodsFont);
        yAxisLeft.setTextSize(14f);

    }

    public static void setDataToBarChart(BarChart barChart, List<BarEntry> barEntry, ArrayList decodedColors, List<String> weekDays) {

        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return weekDays.get((int) value);
            }
        };

        BarDataSet barDataSet = new BarDataSet(barEntry, "");
        barDataSet.setColors(decodedColors);
        barDataSet.setValueFormatter(new DecimalRemover(new DecimalFormat("#")));
        barDataSet.setValueTextColor(MyThemesUtils.getTextColorDark());
        barDataSet.setValueTextSize(11);
        BarData barData = new BarData(barDataSet);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelCount(7);
        xAxis.setValueFormatter(formatter);
        xAxis.setTextColor(MyThemesUtils.getTextColorDark());

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setEnabled(true);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(MyThemesUtils.getTextColorDark());

        Legend legend = barChart.getLegend();
        legend.setEnabled(false);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawValueAboveBar(true);
        barChart.invalidate();
    }

    public static void setDataToPieChart(PieChart pieChart, List<PieEntry> pieEntry, List<Integer> decodedColors) {
        PieDataSet pieChartMoodsDataSet = new PieDataSet(pieEntry, "");
        pieChartMoodsDataSet.setSliceSpace(1f);
        pieChartMoodsDataSet.setDrawValues(true);
        pieChartMoodsDataSet.setColors(decodedColors);
        pieChartMoodsDataSet.setSliceSpace(3f);
        pieChartMoodsDataSet.setSelectionShift(5f);
        pieChartMoodsDataSet.setDrawIcons(false);

        pieChartMoodsDataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        pieChartMoodsDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        pieChartMoodsDataSet.setUsingSliceColorAsValueLineColor(true);

        PieData pieData = new PieData(pieChartMoodsDataSet);
        pieData.setValueFormatter(new PercentFormatter(pieChart));
        pieData.setValueTextColor(MyThemesUtils.getTextColorDark());
        pieData.setValueTextSize(12f);

        pieChart.setData(pieData);

      //  pieChart.setEntryLabelTextSize(12f);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setTextColor(MyThemesUtils.getTextColorDark());

        // undo all highlights
        pieChart.highlightValues(null);
        pieChart.invalidate();
    }


    static class DecimalRemover extends ValueFormatter {

        private DecimalFormat mFormat;

        private DecimalRemover(DecimalFormat format) {
            this.mFormat = format;
        }

        @Override
        public String getFormattedValue(float value) {
            return mFormat.format(value);
        }
    }
}
