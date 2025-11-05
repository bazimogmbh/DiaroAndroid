package com.pixelcrater.Diaro.tags;

import android.content.ContentValues;
import android.database.Cursor;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.model.EntryInfo;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.storage.Tables;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class TagsStatic {

    public static void deleteTagInBackground(final String tagUid) {
        MyApp.executeInBackground(() -> {
            MyApp.getInstance().storageMgr.deleteRowByUid(Tables.TABLE_TAGS, tagUid);

            // Remove tag from entries
            removeTagFromEntries(tagUid);
        });
    }

    private static void removeTagFromEntries(String tagToRemoveUid) {
        if (tagToRemoveUid == null) {
            tagToRemoveUid = "";
        }

        // Get all entries
        Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getRowsColumnsCursor(
                Tables.TABLE_ENTRIES, new String[]{Tables.KEY_UID, Tables.KEY_ENTRY_TAGS}
                , "WHERE " + Tables.KEY_ENTRY_TAGS + " LIKE '%," + tagToRemoveUid + ",%'"
                , null);

        int tagUidColumnIndex = cursor.getColumnIndex(Tables.KEY_UID);
        int entryTagsColumnIndex = cursor.getColumnIndex(Tables.KEY_ENTRY_TAGS);

        while (cursor.moveToNext()) {
            ArrayList<String> tagsArrayList = EntryInfo.getTagsUidsArrayList(
                    cursor.getString(entryTagsColumnIndex), false);
            tagsArrayList.remove(tagToRemoveUid);

            String entryTags = "";

            for (String tagUid : tagsArrayList) {
                if (!StringUtils.isEmpty(tagUid)) {
                    entryTags += "," + tagUid;
                }
            }
            if (!entryTags.equals("")) {
                entryTags += ",";
            }

            // Update entry row
            ContentValues cv = new ContentValues();
            cv.put(Tables.KEY_ENTRY_TAGS, entryTags);
            MyApp.getInstance().storageMgr.updateRowByUid(Tables.TABLE_ENTRIES, cursor.getString(tagUidColumnIndex), cv);
        }
        cursor.close();
    }

    public static ArrayList<String> getActiveTagsUidsArrayList() {
        String activeTags = MyApp.getInstance().prefs.getString(Prefs.PREF_ACTIVE_TAGS, "");
        if (!activeTags.equals("")) {
            return new ArrayList<>(Arrays.asList(activeTags.split(",")));
        }
        return new ArrayList<>();
    }
}
