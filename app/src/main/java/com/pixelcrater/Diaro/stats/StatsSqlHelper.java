package com.pixelcrater.Diaro.stats;

import android.database.Cursor;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieEntry;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.moods.Mood;
import com.pixelcrater.Diaro.utils.AppLog;

import java.util.ArrayList;
import java.util.List;

public class StatsSqlHelper {

    public enum ChartDataType {
        ENTRYBYWEEKDAY, ENTRYBYMONTH,
        WORDBYWEEKDAY, WORDBYMONTH,
        MOODCOUNTBYTYPE, MOODAVGBYWEEKDAY
    }

    private static SparseIntArray getChartDataFromDB(ChartDataType chartDataType, long startDate, long endDate, boolean lifetime, boolean filtered) {
        SparseIntArray valuesMap = new SparseIntArray();
        Cursor cursor = null;
        try {
            cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getStatsCursor(chartDataType, startDate, endDate, lifetime, filtered);
            while (cursor.moveToNext()) {
                // we get a string - int pair as result ( cursor.getInt(0) will cause bugs such as 07 turns to 0
                String val = cursor.getString(0);
                if(!val.isEmpty()){
                    try {
                        int intVal = Integer.parseInt(val);
                        valuesMap.put(intVal , cursor.getInt(1));
                    }catch (Exception e){
                    }

                } else
                AppLog.e(    val +  " - " +cursor.getInt(1));
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return valuesMap;
    }

    static List<BarEntry> getEntryByWeekday(long startDate, long endDate, boolean lifetime, boolean filtered) {
        SparseIntArray valuesMap = getChartDataFromDB(ChartDataType.ENTRYBYWEEKDAY, startDate, endDate, lifetime, filtered);

        ArrayList<BarEntry> retVal = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            retVal.add(new BarEntry((float) i, (float) valuesMap.get(i, 0)));
        }
        return retVal;
    }

    static List<Entry> getEntryByMonth(long startDate, long endDate, boolean lifetime, boolean filtered) {
        SparseIntArray valuesMap = getChartDataFromDB(ChartDataType.ENTRYBYMONTH, startDate, endDate, lifetime, filtered);

        ArrayList<Entry> retVal = new ArrayList<>();
        for (int i = 1; i < 13; i++) {
            retVal.add(new Entry(i*10 , (float) valuesMap.get(i, 0)));
        }
        return retVal;
    }

    // word stats
    static List<BarEntry> getWordByWeekday(long startDate, long endDate, boolean lifetime, boolean filtered) {
        SparseIntArray valuesMap = getChartDataFromDB(ChartDataType.WORDBYWEEKDAY, startDate, endDate, lifetime, filtered);

        ArrayList<BarEntry> retVal = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            retVal.add(new BarEntry((float) i, (float) valuesMap.get(i, 0)));
        }
        return retVal;
    }

    static List<Entry> getWordByMonth(long startDate, long endDate, boolean lifetime, boolean filtered) {
        SparseIntArray valuesMap = getChartDataFromDB(ChartDataType.WORDBYMONTH, startDate, endDate, lifetime, filtered);

        ArrayList<Entry> retVal = new ArrayList<>();
        for (int i = 1; i < 13; i++) {
            retVal.add(new Entry(i *10, (float) valuesMap.get(i, 0)));
        }
        return retVal;
    }


    static List<PieEntry> getMoodCount(long startDate, long endDate, boolean lifetime, boolean filtered) {
        SparseIntArray valuesMap = getChartDataFromDB(ChartDataType.MOODCOUNTBYTYPE, startDate, endDate, lifetime, filtered);

        ArrayList<PieEntry> retVal = new ArrayList<>();
        // moods are 1 to 5, 1 is awesome,5 is bad
        for (int i = 1; i < 6; i++) {
            if (valuesMap.get(i) != 0)
                // value, label
                retVal.add(new PieEntry((float) valuesMap.get(i, 0), MyApp.getInstance().getString(new Mood(i).getMoodTextResId()) + " " + valuesMap.get(i)));
        }
        return retVal;
    }


    static List<BarEntry> getMoodAvgByWeekday(long startDate, long endDate, boolean lifetime, boolean filtered) {
        SparseArray valuesMap = new SparseArray();

        ArrayList<BarEntry> retVal = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getStatsCursor(ChartDataType.MOODAVGBYWEEKDAY, startDate, endDate, lifetime, filtered);
            while (cursor.moveToNext()) {
                valuesMap.put(cursor.getInt(0), cursor.getFloat(1));
            }

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        for (int i = 0; i < 7; i++) {
            float val = (float) valuesMap.get(i, 6.0f);
            retVal.add(new BarEntry(i, (6.0f - val)));
        }
        return retVal;
    }
}
