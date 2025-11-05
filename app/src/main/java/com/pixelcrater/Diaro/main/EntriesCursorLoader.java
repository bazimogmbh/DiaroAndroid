package com.pixelcrater.Diaro.main;

import android.content.Context;
import android.database.Cursor;

import androidx.core.util.Pair;
import androidx.loader.content.CursorLoader;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.entries.EntriesStatic;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.storage.OnStorageDataChangeListener;
import com.pixelcrater.Diaro.utils.AppLog;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;

import dev.dworks.libs.astickyheader.SimpleSectionedListAdapter;

public class EntriesCursorLoader extends CursorLoader implements OnStorageDataChangeListener {

    SimpleSectionedListAdapter.Section[] entriesSectionsArray;

    EntriesCursorLoader(Context context) {
        super(context);
        AppLog.d("");

        MyApp.getInstance().storageMgr.addOnStorageDataChangeListener(this);
    }

    @Override
    protected void onReset() {
        super.onReset();

        MyApp.getInstance().storageMgr.removeOnStorageDataChangeListener(this);
    }

    @Override
    public Cursor loadInBackground() {
        Pair<String, String[]> pair = EntriesStatic.getEntriesAndSqlByActiveFilters(null);
        getSectionsFromSQLite(pair.first, pair.second);

        // Get only _id and uid fields to avoid 1MB cursor limit
        Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getEntriesCursorIdsAndUidsOnly(pair.first, pair.second);

        // Execute SQLite query by accessing the cursor
        int count = cursor.getCount();
        AppLog.d("count: " + count);

        return cursor;
    }

    @Override
    public void onStorageDataChange() {
        onContentChanged();
    }

    private void getSectionsFromSQLite(String andSql, String[] whereArgs) {
        AppLog.d("andSql: " + andSql);

        ArrayList<SimpleSectionedListAdapter.Section> sections = new ArrayList<>();
        // Execute SQLite query by accessing the cursor
        Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getEntriesSectionsCursor(andSql, whereArgs);
        cursor.getCount();

        int yearColumnIndex = cursor.getColumnIndex("year");
        int monthColumnIndex = cursor.getColumnIndex("month");
        int sectionEntriesCountColumnIndex = cursor.getColumnIndex("section_entries_count");

        int sectionPos = 0;
        SimpleSectionedListAdapter.Section section;
        while (cursor.moveToNext()) {
            // SQLCipher 3.5.2 returns 6 digits using getInt()
            String yearStr = cursor.getString(yearColumnIndex);
            if (StringUtils.isEmpty(yearStr)) {
                yearStr = String.valueOf(DateTime.now().getYear());
            }
            int dateY = Integer.parseInt(yearStr);
            int dateM = getMonthIntegerFromString(cursor.getString(monthColumnIndex));
            int sectionEntriesCount = cursor.getInt(sectionEntriesCountColumnIndex);
//            AppLog.d("dateY: " + dateY + ", dateM: " + dateM +   ", sectionEntriesCount: " + sectionEntriesCount +", sectionPos: " + sectionPos);

            if (dateY > 0 && dateM != -1) {
                section = new SimpleSectionedListAdapter.Section(sectionPos, Static.getMonthTitleStandAlone(dateM) + " " + dateY);
                sections.add(section);
            }

            sectionPos += sectionEntriesCount;
        }
        cursor.close();

        entriesSectionsArray = sections.toArray(new SimpleSectionedListAdapter.Section[sections.size()]);

    }

    private int getMonthIntegerFromString(String s) {
        try {
            return Integer.parseInt(s.startsWith("0") ? s.replace("0", "") : s);
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }
        return -1;
    }
}
