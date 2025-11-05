package com.pixelcrater.Diaro.calendar;

import android.database.Cursor;
import android.os.AsyncTask;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;

public class GetDaysMarkersAsync extends AsyncTask<Object, String, Boolean> {

    private final ArrayList<DateTime> mDaysGridDtArrayList;
    private boolean isCancelled;
    private CalendarView mCalendarView;
    private ArrayList<DayMarker> calendarDayMarkersArrayList = new ArrayList<>();

    public GetDaysMarkersAsync(CalendarView calendarView, ArrayList<DateTime> daysGridDtArrayList) {
        mCalendarView = calendarView;
        mDaysGridDtArrayList = daysGridDtArrayList;
    }

    public void setCancelled(boolean isCancelled) {
        this.isCancelled = isCancelled;
    }

    protected Boolean doInBackground(Object... params) {
//        AppLog.d("");

//        long startMs = SystemClock.uptimeMillis();

        try {
            long fromMillis = mDaysGridDtArrayList.get(0).getMillis();
            long toMillis = mDaysGridDtArrayList.get(mDaysGridDtArrayList.size() - 1).plusDays(1) .getMillis() - 1;

            Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getEntriesCursorForCalendar(fromMillis, toMillis);

            DateTime dayDt = mDaysGridDtArrayList.get(0);
            ArrayList<String> dayFolderColorsArrayList = new ArrayList<>();
            int dayPhotoCount = 0;

            int entryDateColumnIndex = cursor.getColumnIndex(Tables.KEY_ENTRY_DATE);
            int photoCountColumnIndex = cursor.getColumnIndex("photo_count");
            int folderColorColumnIndex = cursor.getColumnIndex("folder_color");

            while (cursor.moveToNext()) {
                if (isCancelled) {
                    cursor.close();
                    return false;
                }

                long entryMillis = cursor.getLong(entryDateColumnIndex);
                DateTime entryDt = new DateTime(entryMillis).withTimeAtStartOfDay();

                if (entryDt.getMillis() != dayDt.getMillis()) {
//                AppLog.d("entryDt.toString(\"yyyy.MM.dd\"): " + entryDt.toString("yyyy.MM.dd") +
//                        "dayDt.toString(\"yyyy.MM.dd\"): " + dayDt.toString("yyyy.MM.dd"));

                    if (dayFolderColorsArrayList.size() > 0) {
                        // Add marker
                        addMarker(dayDt, dayFolderColorsArrayList, dayPhotoCount);

                        dayFolderColorsArrayList = new ArrayList<>();
                        dayPhotoCount = 0;
                    }

                    dayDt = entryDt;
                }

                dayPhotoCount += cursor.getInt(photoCountColumnIndex);

                if (dayFolderColorsArrayList.size() < 4) {
                    String folderColor = cursor.getString(folderColorColumnIndex);

                    if (StringUtils.isEmpty(folderColor)) {
                        // Default color for entries without folders
                        folderColor = MyThemesUtils.getHexColorFromResId(R.color.grey_500);
                    }
                    if (!dayFolderColorsArrayList.contains(folderColor)) {
//                    AppLog.d("entry date: " + new DateTime(entryMillis).toString("yyyy.MM.dd") +
//                            ", folderColor: " + folderColor);
                        // Add folder color to ArrayList
                        dayFolderColorsArrayList.add(folderColor);
                    }
                }

                if (cursor.isLast()) {
                    if (dayFolderColorsArrayList.size() > 0) {
                        // Add marker
                        addMarker(dayDt, dayFolderColorsArrayList, dayPhotoCount);
                    }
                }
            }
            cursor.close();
        } catch (Exception e) {
            AppLog.e("Exception: " + e);

            return false;
        }
//        long endMs = android.os.SystemClock.uptimeMillis();
//        AppLog.d("duration: " + (endMs - startMs) + " ms");

        return true;
    }

    private void addMarker(DateTime dayDt, ArrayList<String> dayFolderColorsArrayList,int dayPhotoCount) {

        calendarDayMarkersArrayList.add(new DayMarker(dayDt, dayFolderColorsArrayList, dayPhotoCount));

//        AppLog.d("new marker added: " + dayDt.toString("yyyy.MM.dd") +
//                ", dayFolderColorsArrayList.size(): " + dayFolderColorsArrayList.size() +
//                ", dayPhotoCount: " + dayPhotoCount);
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            mCalendarView.showMarkersOnDays(calendarDayMarkersArrayList);
        }
    }
}
