package com.pixelcrater.Diaro.entries;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.core.util.Pair;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.entries.attachments.AttachmentsStatic;
import com.pixelcrater.Diaro.model.AttachmentInfo;
import com.pixelcrater.Diaro.locations.LocationsCursorAdapter;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.settings.PreferencesHelper;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.tags.TagsCursorAdapter;
import com.pixelcrater.Diaro.utils.AppLog;
import com.yariksoffice.lingver.Lingver;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class EntriesStatic {

    public static void archiveEntries(ArrayList<String> entriesUids) throws Exception {
        archiveUnarchiveEntries(entriesUids, 1);
    }

    public static void undoArchiveEntries(ArrayList<String> entriesUids) throws Exception {
        archiveUnarchiveEntries(entriesUids, 0);
    }

    private static void archiveUnarchiveEntries(ArrayList<String> entriesUids, int value) throws Exception {

//		AppLog.d("entriesUids: " + entriesUids + ", value: " + value);

        for (String entryUid : entriesUids) {
            if (!StringUtils.isEmpty(entryUid)) {
                // Change archived value
                ContentValues cv = new ContentValues();
                cv.put(Tables.KEY_ENTRY_ARCHIVED, value);
                MyApp.getInstance().storageMgr.updateRowByUid(Tables.TABLE_ENTRIES, entryUid, cv);
            }
        }

        MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners();
    }

    public static void deleteEntries(ArrayList<String> entriesUids) throws Exception {
//		AppLog.d("entriesUids: " + entriesUids);

        for (String entryUid : entriesUids) {
            MyApp.getInstance().storageMgr.deleteRowByUid(Tables.TABLE_ENTRIES, entryUid);

            // Delete entry attachments
            ArrayList<AttachmentInfo> attachmentsArrayList = AttachmentsStatic.getEntryAttachmentsArrayList(entryUid, null);
            AttachmentsStatic.deleteAttachments(attachmentsArrayList);
        }
    }

    public static void updateEntriesFolder(ArrayList<String> entriesUids, String folderUid) {
        AppLog.d("folderUid: " + folderUid);

        if (folderUid == null) {
            folderUid = "";
        }

        for (String entryUid : entriesUids) {
            if (!StringUtils.isEmpty(entryUid)) {
                AppLog.d("entryUid: " + entryUid);

                // Change entry folder
                ContentValues cv = new ContentValues();
                cv.put(Tables.KEY_ENTRY_FOLDER_UID, folderUid);
                MyApp.getInstance().storageMgr.updateRowByUid(Tables.TABLE_ENTRIES, entryUid, cv);

                AppLog.d("");
            }
        }

        MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners();
    }

    public static void updateEntriesTag(ArrayList<String> entriesUids, String newTags) {
        AppLog.e("tags: " + newTags);

        if (newTags == null) {
            newTags = "";
        }

        for (String entryUid : entriesUids) {
            if (!StringUtils.isEmpty(entryUid)) {

                // first get entries current tags
                String existingTags = MyApp.getInstance().storageMgr.getSQLiteAdapter().getTagsForEntry(entryUid);

                AppLog.e("entryUid: " + entryUid + " , new tags are : " + newTags + "old tags are : " + existingTags);
                // ,55751b83e82db5a97dddaa14a1c45774,   //,2a2542f9e61a9a1d3b83ae31889ac954,e9c1baa156f56d2eedf150afa6226e43,
                String[] bothTagsArray = (String[]) ArrayUtils.addAll(existingTags.split(","), newTags.split(","));

                // remove duplicates
                ArrayList<String> bothTagsArraylist = new ArrayList<>();
                for (String tagUid : bothTagsArray) {
                    if (!bothTagsArraylist.contains(tagUid)) {
                        bothTagsArraylist.add(tagUid);
                    }
                }

                String entryTags = "";

                for (String tagUid : bothTagsArraylist) {
                    if (!tagUid.equals("")) {
                        Cursor tagCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSingleTagCursorByUid(tagUid);
                        tagCursor.close();

                        entryTags += "," + tagUid;
                    }

                }

                if (!entryTags.equals("")) {
                    entryTags += ",";
                }

                // eg. ,55751b83e82db5a97dddaa14a1c45774,2a2542f9e61a9a1d3b83ae31889ac954,e9c1baa156f56d2eedf150afa6226e43,
                AppLog.e("Combined tags are : " + entryTags);
                // Change entry folder
                ContentValues cv = new ContentValues();
                cv.put(Tables.KEY_ENTRY_TAGS, entryTags);
                MyApp.getInstance().storageMgr.updateRowByUid(Tables.TABLE_ENTRIES, entryUid, cv);

            }
        }

        MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners();
    }


    public static void updateEntriesLocation(ArrayList<String> entriesUids, String locationUid) {
        AppLog.d("locationUid: " + locationUid);

        if (locationUid == null) {
            locationUid = "";
        }

        for (String entryUid : entriesUids) {
            if (!StringUtils.isEmpty(entryUid)) {
                AppLog.d("entryUid: " + entryUid);

                // Change entry location
                ContentValues cv = new ContentValues();
                cv.put(Tables.KEY_ENTRY_LOCATION_UID, locationUid);
                MyApp.getInstance().storageMgr.updateRowByUid(Tables.TABLE_ENTRIES, entryUid, cv);

                AppLog.d("");
            }
        }

        MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners();
    }

    public static void clearEntryPrimaryPhotoUidOnPhotoDelete(
            String entryUid, String deletedPhotoUid) {
        if (StringUtils.equals(MyApp.getInstance().storageMgr.getSQLiteAdapter().getSingleRowColumnValueByUid(Tables.TABLE_ENTRIES, Tables.KEY_ENTRY_PRIMARY_PHOTO_UID, entryUid), deletedPhotoUid)) {
            // Clear entry primary photo uid
            ContentValues cv = new ContentValues();
            cv.put(Tables.KEY_ENTRY_PRIMARY_PHOTO_UID, "");
            MyApp.getInstance().storageMgr.updateRowByUid(Tables.TABLE_ENTRIES, entryUid, cv);
        }
    }

    // filter is-> AND e.date>=1579820400000 AND e.date<=1579906799999 AND e.folder_uid='ac89bebdb2db752530be26942606283b'
    // AND (e.tags LIKE '%,4504ad8cfc52f31ac832062334baaf5f,%' OR e.tags LIKE '%,ef1372470a7d3f4af8ab115e391cb539,%')
    // AND (e.location_uid='4ca7097310ffcce2696830660ee9f8a3') AND e.title || e.text LIKE ?
    // second part is-> [%mysearch%]
    public static Pair<String, String[]> getEntriesAndSqlByActiveFilters(String visibleRowUid) {
        StringBuilder andSqlSB = new StringBuilder();
        ArrayList<String> whereArgsArrayList = new ArrayList<>();

        // Include visible row to results
        if (!StringUtils.isEmpty(visibleRowUid)) {
            andSqlSB.append(" AND (e." + Tables.KEY_UID + "=? OR (e." + Tables.KEY_UID + "!=?");
            whereArgsArrayList.add(visibleRowUid);
            whereArgsArrayList.add(visibleRowUid);
        }

        // Calendar
        long selectedRangeFromMillis = MyApp.getInstance().prefs.getLong(Prefs.PREF_ACTIVE_CALENDAR_RANGE_FROM_MILLIS, 0);
        long selectedRangeToMillis = MyApp.getInstance().prefs.getLong(Prefs.PREF_ACTIVE_CALENDAR_RANGE_TO_MILLIS, 0);

        if (selectedRangeFromMillis != 0) {
            andSqlSB.append(" AND e." + Tables.KEY_ENTRY_DATE + ">=").append(selectedRangeFromMillis);
        }
        if (selectedRangeToMillis !=  0) {
            andSqlSB.append(" AND e." + Tables.KEY_ENTRY_DATE + "<=").append(selectedRangeToMillis);
        }

        // Folder
        String activeFolderUid = MyApp.getInstance().prefs.getString(Prefs.PREF_ACTIVE_FOLDER_UID, null);
        if (activeFolderUid != null) {
            andSqlSB.append(" AND e." + Tables.KEY_ENTRY_FOLDER_UID + "='")
                    .append(activeFolderUid)
                    .append("'");
        }

        // Tags
        String activeTags = MyApp.getInstance().prefs.getString(Prefs.PREF_ACTIVE_TAGS, "");

        int logic = MyApp.getInstance().prefs.getInt(Prefs.PREF_TAGS_LOGIC, Prefs.FILTER_LOGIC_OR);

        if (!activeTags.equals("")) {
            int t = 0;
            String[] splitted = activeTags.split(",");
            for (String tagUid : splitted) {
                if (StringUtils.isEmpty(tagUid)) {
                    continue;
                }

                if (t == 0) {
                    andSqlSB.append(" AND (");
                } else {
                    if (logic == Prefs.FILTER_LOGIC_OR) {
                        andSqlSB.append(" OR ");
                    } else {
                        andSqlSB.append(" AND ");
                    }
                }

                if (StringUtils.equals(tagUid, TagsCursorAdapter.NO_TAGS_UID)) {
                    andSqlSB.append("e." + Tables.KEY_ENTRY_TAGS + "=''");
                } else {
                    andSqlSB.append("e." + Tables.KEY_ENTRY_TAGS + " LIKE '%,").append(tagUid).append(",%'");
                }

                t++;
            }

            if (t > 0) {
                andSqlSB.append(")");
            }
        }

        // Locations
        String activeLocations = MyApp.getInstance().prefs.getString(Prefs.PREF_ACTIVE_LOCATIONS, "");

        if (!activeLocations.equals("")) {
            int l = 0;
            String[] splitted = activeLocations.split(",");
            for (String locationUid : splitted) {
                if (StringUtils.isEmpty(locationUid)) {
                    continue;
                }

                if (l == 0) {
                    andSqlSB.append(" AND (");
                } else {
                    andSqlSB.append(" OR ");
                }

                if (locationUid.equals(LocationsCursorAdapter.NO_LOCATION_UID)) {
                    andSqlSB.append("e." + Tables.KEY_ENTRY_LOCATION_UID + "=''");
                } else {
                    andSqlSB.append("e." + Tables.KEY_ENTRY_LOCATION_UID + "='").append(locationUid).append("'");
                }

                l++;
            }

            if (l > 0) {
                andSqlSB.append(")");
            }
        }

        // Mood
        String activeMoodUid = MyApp.getInstance().prefs.getString(Prefs.PREF_ACTIVE_MOOD_UID, null);
        AppLog.d("activeMoodUid  " + activeMoodUid);
        if (activeMoodUid != null) {
            andSqlSB.append(" AND e." + "mood" + "='")
                    .append(activeMoodUid)
                    .append("'");
        }

        // Search
        String activeSearchText = PreferencesHelper.getActiveSearchText();
//        AppLog.d("activeSearchText: " + activeSearchText);

        boolean doubleQuotesSearch = false;
        if (activeSearchText.startsWith("\"") || activeSearchText.endsWith("\""))
            doubleQuotesSearch = true;

        activeSearchText = activeSearchText.replaceAll("\"", "");
        if (!activeSearchText.equals("")) {
            ArrayList<String> wordsArray = new ArrayList<>();
            // Double quotes search

            if (doubleQuotesSearch) {
                wordsArray.add(activeSearchText);
            } else {
                String[] splitted = activeSearchText.split(" ");
                for (String s : splitted) {
                    if (!s.equals(" ") && !s.equals("")) {
                        wordsArray.add(s.trim());
                    }
                }
            }

            int count = wordsArray.size();
            if (count > 0) {
                for (int i = 0; i < count; i++) {
                    andSqlSB.append(" AND e.title || e.text LIKE ?");
                    String word = wordsArray.get(i);
                    whereArgsArrayList.add("%" + word + "%");

                    // workaround for sqlite non ascii like bug
                    if (!isAsciiPrintable(word) && isCapitalizableLanguage()) {
                        AppLog.e("Nonasci && Capitalizable");
                        // make sure first word is also capitalized
                        if (!Character.isUpperCase(word.charAt(0))) {
                            andSqlSB.append(" OR e.title || e.text LIKE ?");
                            String wordWithFirstCapital = word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
                            whereArgsArrayList.add("%" + wordWithFirstCapital + "%");
                        }
                        andSqlSB.append(" OR e.title || e.text LIKE ?");
                        whereArgsArrayList.add("%" + word.toUpperCase() + "%");

                        andSqlSB.append(" OR e.title || e.text LIKE ?");
                        whereArgsArrayList.add("%" + word.toLowerCase() + "%");
                    }
//                    AppLog.d("wordsArray.get(i).toUpperCase(): " + wordsArray.get(i).toUpperCase());

                }
            }
//            AppLog.d("andSql: " + andSql);
        }

        // visibleRowUid
        if (!StringUtils.isEmpty(visibleRowUid)) {
            andSqlSB.append("))");
        }

        // Convert ArrayList to String[] array
        String[] whereArgs = whereArgsArrayList.toArray(new String[whereArgsArrayList.size()]);

        return new Pair<>(andSqlSB.toString(), whereArgs);
    }

    private static boolean isAsciiPrintable(String str) {
        int sz = str.length();
        for (int i = 0; i < sz; i++) {
            if (!isAsciiPrintable(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isCapitalizableLanguage() {
        String langCode = PreferencesHelper.getCurrentLocaleAsCode(MyApp.getInstance());
        AppLog.e("langcode " + langCode);

        if (langCode.startsWith("en"))
            return false;

        switch (langCode) {
            case "ar":
            case "fa":
            case "he":
            case "hi":
            case "en":
            case "iw":
            case "jp":
            case "ko":
            case "th":
            case "zh":
            case "zh_CN":
            case "zh_TW":
                return false;

        }
        return true;
    }

    //There are 128 valid basic ASCII characters, mapped to the values 0 (the NUL byte) to 127 (the DEL character).
    private static boolean isAsciiPrintable(char ch) {
        return ch >= 32 && ch < 127;
    }
}