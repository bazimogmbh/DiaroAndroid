package com.pixelcrater.Diaro.storage.sqlite;

import android.database.Cursor;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.atlas.EntryIDAndLocation;
import com.pixelcrater.Diaro.gallery.GalleryItem;
import com.pixelcrater.Diaro.model.EntryInfo;
import com.pixelcrater.Diaro.model.MoodInfo;
import com.pixelcrater.Diaro.model.TagInfo;
import com.pixelcrater.Diaro.moods.Mood;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.templates.Template;
import com.pixelcrater.Diaro.utils.AppLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by abhishek9851 on 01.12.17.
 */

public class SQLiteQueryHelper {

    public static int getUnsyncedCount() {
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getUnsyncedCursorCount();
            //  AppLog.e("cursor size is-> " + cursor.getCount());
            cursor.moveToFirst();
            count = cursor.getInt(0);

        } catch (Exception e) {
            AppLog.e(e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        //   AppLog.e("Memories count is-> " + count);
        return count;
    }

    public static int getMoodsCount() {
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getMoodsCountCursor();
            //  AppLog.e("cursor size is-> " + cursor.getCount());
            cursor.moveToFirst();
            count = cursor.getInt(0);

        } catch (Exception e) {
            AppLog.e(e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        AppLog.e("Moods count is-> " + count);
        return count;
    }

    public static int getEntriesByThisDayCount(boolean includeCurrentYear) {
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getEntriesMemoryCursorCount(includeCurrentYear);
          //  AppLog.e("cursor size is-> " + cursor.getCount());
            cursor.moveToFirst();
            count = cursor.getInt(0);

        } catch (Exception e) {
            AppLog.e(e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
     //   AppLog.e("Memories count is-> " + count);
        return count;
    }

    public static ArrayList<EntryInfo> getEntriesByThisDay(boolean includeCurrentYear) {
        ArrayList<EntryInfo> entriesList;
        ArrayList<String> uidList = new ArrayList<>();
        // get the list of uids
        Cursor memoriesCursor = null;
        try {
            memoriesCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getEntriesMemoryCursor(includeCurrentYear);
           // AppLog.e("MemoriesCursor count is-> " + memoriesCursor.getCount());
            while (memoriesCursor.moveToNext()) {
                String uid = memoriesCursor.getString(0);
                uidList.add(uid);
                // Get single row cursor
            }
        } finally {
            if (memoriesCursor != null) {
                memoriesCursor.close();
            }
        }

        // fetch the entries of the uids
        entriesList = getEntriesByUids(uidList, true);
        return entriesList;
    }

    public static String getSqlArrayFromList(List<String> items) {
        StringBuilder uidsAsString = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            uidsAsString.append("'").append(items.get(i)).append("'");
            if (i < items.size() - 1) {
                uidsAsString.append(",");
            }
        }
        return uidsAsString.toString();
    }

    public void getEntriesCountByMonth() {
        // SELECT    COUNT(*)  , datetime(e.date / 1000, 'unixepoch', tz_offset) AS localtime ,strftime('%Y', datetime(e.date/1000, 'unixepoch', e.tz_offset)) as year, strftime('%m', datetime(e.date/1000, 'unixepoch', e.tz_offset)) as month
        // FROM      diaro_entries  e
        // WHERE  year =  "2014"
        //GROUP BY  month


        // get moods
        //  SELECT COUNT(*) , mood from diaro_entries group by mood


        //SELECT COUNT(*) , avg(mood), datetime(e.date / 1000, 'unixepoch', tz_offset) AS localtime ,strftime('%Y', datetime(e.date/1000, 'unixepoch', e.tz_offset)) as year, strftime('%m', datetime(e.date/1000, 'unixepoch', e.tz_offset)) as month  , strftime('%d', datetime(e.date/1000, 'unixepoch', e.tz_offset)) as day,  strftime('%W', datetime(e.date/1000, 'unixepoch', e.tz_offset)) as weekNumber
        // FROM     diaro_entries  e  where year = "2013" group by weekNumber

        //SELECT COUNT(*) , avg(mood), datetime(e.date / 1000, 'unixepoch', tz_offset) AS localtime ,strftime('%Y', datetime(e.date/1000, 'unixepoch', e.tz_offset)) as year, strftime('%m', datetime(e.date/1000, 'unixepoch', e.tz_offset)) as month  , strftime('%d', datetime(e.date/1000, 'unixepoch', e.tz_offset)) as day,  strftime('%W', datetime(e.date/1000, 'unixepoch', e.tz_offset)) as weekNumber
        //       FROM     diaro_entries  e  where mood != "" AND mood != "0" group by day
        //


        //SELECT    COUNT(*)  as count
        //FROM      diaro_entries  e where strftime('%m', datetime(e.date/1000, 'unixepoch', e.tz_offset)) =   strftime('%m', datetime('now', 'localtime'))
    }

    public static List<EntryIDAndLocation> getAtlasData() {
        List<EntryIDAndLocation> entryIdAndLocationsList = new ArrayList<>();
        try (Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getAtlastData()) {
            EntryIDAndLocation eal;
            while (cursor.moveToNext()) {   // entry uid, lat, long
                eal = new EntryIDAndLocation(cursor.getString(0), cursor.getDouble(1), cursor.getDouble(2));
                entryIdAndLocationsList.add(eal);
            }
        }
        return entryIdAndLocationsList;
    }


    // Return entries for given uids
    public static ArrayList<EntryInfo> getEntriesByUids(List<String> uidList, boolean cropText) {
        ArrayList<EntryInfo> entriesList = new ArrayList<>();
        try (Cursor rowCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getEntriesCursorByUids(uidList, cropText)) {
            while (rowCursor.moveToNext()) {
                final EntryInfo entryInfo = new EntryInfo(rowCursor);
                entriesList.add(entryInfo);
                //entryInfo.print();
            }
        }
        return entriesList;
    }

    // Templates
    public static ArrayList<Template> getTemplates() {
        ArrayList<Template> templatesList = new ArrayList<>();
        try (Cursor templatesCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getTemplatesCursor()) {
            AppLog.e("templatesCursor count is-> " + templatesCursor.getCount());
            while (templatesCursor.moveToNext()) {
                templatesList.add(getTemplateFromCursor(templatesCursor));
            }
        }
        return templatesList;
    }

    public static Template getTemplateFromCursor(Cursor c) {
       return new Template(c.getString(c.getColumnIndex("uid" )), c.getString(c.getColumnIndex("name" )), c.getString(c.getColumnIndex("title" )), c.getString(c.getColumnIndex("text" )));
    }

    // Moods
    public static ArrayList<MoodInfo> getMoods() {
        ArrayList<MoodInfo> retList = new ArrayList<>();
        try (Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getMoodsCursor()) {
            while (cursor.moveToNext()) {
                retList.add(new MoodInfo(cursor));
            }
        }
        return retList;
    }

    // Gallery


    public static ArrayList<GalleryItem> getGalleryItems() {
        ArrayList<GalleryItem> templatesList = new ArrayList<>();
        try (Cursor templatesCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getGalleryData()) {
            AppLog.e("getGalleryItemsCursor count is-> " + templatesCursor.getCount());
            while (templatesCursor.moveToNext()) {
                templatesList.add(getGalleryItemFromCursor(templatesCursor));
            }
        }
        return templatesList;
    }

    public static GalleryItem getGalleryItemFromCursor(Cursor c) {
        return new GalleryItem(c.getString(c.getColumnIndex("filename" )), c.getString(c.getColumnIndex("localtime" )), c.getString(c.getColumnIndex("entryUid" ) ));
    }

    public static ArrayList<TagInfo> getTagsInfos() {
        ArrayList<TagInfo> tasgList = new ArrayList<>();

        try (Cursor tagsCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getAllTagsCursor()) {
            while (tagsCursor.moveToNext()) {
                tasgList.add(getTagsInFoItemFromCursor(tagsCursor));
            }
        }
        return tasgList;
    }

    public static TagInfo getTagsInFoItemFromCursor(Cursor c) {
        return new TagInfo(c.getString(c.getColumnIndex(Tables.KEY_UID)), c.getString(c.getColumnIndex(Tables.KEY_TAG_TITLE) ));
    }


}
