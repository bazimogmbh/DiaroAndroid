package com.pixelcrater.Diaro.imports;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.OpenableColumns;
import android.widget.Toast;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.model.AttachmentInfo;
import com.pixelcrater.Diaro.model.EntryInfo;
import com.pixelcrater.Diaro.model.FolderInfo;
import com.pixelcrater.Diaro.model.LocationInfo;
import com.pixelcrater.Diaro.model.PersistanceHelper;
import com.pixelcrater.Diaro.model.TagInfo;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DiariumImport {

    private static String FOLDER_TITLE = "Diarium";
    private static String FOLDER_COLOR = "#3478D0";
    private static int DEFAULT_ZOOM = 11;
    private static String OFFSET = "+00:00";

    public DiariumImport(Uri uri, Activity activity) {

        String fileName = getFileName(uri);

        ProgressDialog progress = new ProgressDialog(activity);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(false);
        progress.setTitle(R.string.importing_data);
        progress.setMessage(fileName);
        progress.setCancelable(true);
        progress.show();
        AtomicInteger entriesCount = new AtomicInteger();

        AsyncTask.execute(() -> {

            FolderInfo folder = new FolderInfo(null, FOLDER_TITLE, FOLDER_COLOR);
            folder.setPattern("");
            String tmpfolderUid = PersistanceHelper.saveFolder(folder);

            try {
                InputStream inputStream = MyApp.getInstance().getContentResolver().openInputStream(uri);

                File file = File.createTempFile("diariumsqlite", "");

                FileOutputStream outputStream = new FileOutputStream(file);
                byte[] buff = new byte[1024];
                int read;
                while ((read = inputStream.read(buff, 0, buff.length)) > 0)
                    outputStream.write(buff, 0, read);
                inputStream.close();
                outputStream.close();

                SQLiteDatabase database = SQLiteDatabase.openDatabase(file.getPath(), null, SQLiteDatabase.OPEN_READONLY);

                // Tags
                HashMap<Integer, String> tagIDUIDHashMap = new HashMap<>();
                Cursor tagCursor = database.rawQuery("SELECT * from Tags", null);
                int tagIdColumnIndex = tagCursor.getColumnIndex("DiaryTagId");
                int tagTitleColumnIndex = tagCursor.getColumnIndex("Value");

                try {
                    while (tagCursor.moveToNext()) {

                        int tagID = tagCursor.getInt(tagIdColumnIndex);
                        String tagTitle = tagCursor.getString(tagTitleColumnIndex);

                        TagInfo tagInfo = new TagInfo(Static.generateRandomUid(), tagTitle);
                        String tagUid = PersistanceHelper.saveTag(tagInfo);

                        tagIDUIDHashMap.put(tagID, tagUid);

                    }
                } finally {
                    tagCursor.close();
                }

                // EntryTags
                HashMap<String, ArrayList<String>> entryIDTagIDDHashMap = new HashMap<>();
                Cursor entryTagCursor = database.rawQuery("SELECT * from EntryTags", null);
                int diaryEntryTagsIdColumnIndex = entryTagCursor.getColumnIndex("DiaryEntryId");
                int diaryTagIdColumnIndex = entryTagCursor.getColumnIndex("DiaryTagId");

                try {
                    while (entryTagCursor.moveToNext()) {
                        String diaryEntryId = entryTagCursor.getString(diaryEntryTagsIdColumnIndex);
                        int diaryTagID = entryTagCursor.getInt(diaryTagIdColumnIndex);

                        String tagUid = tagIDUIDHashMap.get(diaryTagID);

                        if (entryIDTagIDDHashMap.containsKey(diaryEntryId)) {
                            ArrayList<String> tagIds = entryIDTagIDDHashMap.get(diaryEntryId);
                            tagIds.add(tagUid);

                            entryIDTagIDDHashMap.put(diaryEntryId, tagIds);
                        } else {
                            ArrayList<String> tagIds = new ArrayList<>();
                            tagIds.add(tagUid);

                            entryIDTagIDDHashMap.put(diaryEntryId, tagIds);
                        }
                    }
                } finally {
                    entryTagCursor.close();
                }

                // Cursor cursor = database.rawQuery("SELECT e.DiaryEntryId, e.Heading, e.Text, e.Rating, e.Latitude, e.Longitude, t.value as TagName FROM Entries e  LEFT JOIN EntryTags et ON e.DiaryEntryId = et.DiaryEntryId  LEFT JOIN Tags t ON t.DiaryTagId = et.DiaryTagId", null);

                Cursor entryCursor = database.rawQuery("SELECT * from Entries", null);
                int diaryEntryIdColumnIndex = entryCursor.getColumnIndex("DiaryEntryId");
                int headingColumnIndex = entryCursor.getColumnIndex("Heading");
                int textColumnIndex = entryCursor.getColumnIndex("Text");
                int ratingColumnIndex = entryCursor.getColumnIndex("Rating");
                int latitudeColumnIndex = entryCursor.getColumnIndex("Latitude");
                int longitudeColumnIndex = entryCursor.getColumnIndex("Longitude");

                HashMap<String, String> entryIDUIDHashMap = new HashMap<>();

                entriesCount.set(entryCursor.getCount());
                try {
                    while (entryCursor.moveToNext()) {

                        String diaryEntryId = entryCursor.getString(diaryEntryIdColumnIndex);
                        long time = Long.parseLong(diaryEntryId);

                        long tick = 621355968000000000L;

                        // every tick is 1/10000000 of second)
                        long timeinTicks = time - tick;
                        long entry_date = timeinTicks / 10000;

                        String entry_title = entryCursor.getString(headingColumnIndex);
                        entry_title = cleanPreserveLineBreaks(entry_title);
                        String entry_text = entryCursor.getString(textColumnIndex);
                        entry_text = cleanPreserveLineBreaks(entry_text);
                        int rating = entryCursor.getInt(ratingColumnIndex);
                        float latitude = entryCursor.getFloat(latitudeColumnIndex);
                        float longitude = entryCursor.getFloat(longitudeColumnIndex);

                        //  String tagName = entryCursor.getString(tagColumnIndex);

                        StringBuilder tags = new StringBuilder();
                        ArrayList<String> tagUids = entryIDTagIDDHashMap.get(diaryEntryId);

                        if (tagUids != null) {
                            for (int tagIndex = 0; tagIndex < tagUids.size(); tagIndex++) {
                                tags.append(",").append(tagUids.get(tagIndex));

                                if (tagIndex == (tagUids.size() - 1)) {
                                    tags.append(",");
                                }
                            }
                        }

                        String tmpLocationUid = "";

                        if (latitude != 0.0 && longitude != 0.0) {
                            LocationInfo locationInfo = new LocationInfo(Static.generateRandomUid(), "", "", latitude + "", longitude + "", DEFAULT_ZOOM);
                            tmpLocationUid = PersistanceHelper.saveLocation(locationInfo, false);
                        }


                        //   AppLog.e(entry_date + "  - " + entry_title + " - " + rating + " - " + latitude + "," + longitude + " - " + tags);


                        String entry_uid = Static.generateRandomUid();
                        //--saving the entries
                        EntryInfo entry = new EntryInfo();
                        entry.setUid(entry_uid);
                        entry.setDate(entry_date);
                        entry.setText(entry_text);
                        entry.setTitle(entry_title);
                        entry.setFolderUid(tmpfolderUid);
                        entry.setLocationUid(tmpLocationUid);
                        entry.setTags(tags.toString());
                        entry.setTzOffset(OFFSET);
                        //   entry.setWeatherInfo(weather_temp, weather_icon, weather_description);
                        //   entry.setMood(entry_mood);
                        PersistanceHelper.saveEntry(entry);

                        entryIDUIDHashMap.put(diaryEntryId, entry_uid);
                    }
                } finally {
                    entryCursor.close();
                }

                // Media
                Cursor cursorMedia = database.rawQuery("SELECT * from Media", null);
                int mediaTypeColumnIndex = cursorMedia.getColumnIndex("Type");
                int dataColumnIndex = cursorMedia.getColumnIndex("Data");

                int fileEndingColumnIndex = cursorMedia.getColumnIndex("FileEnding");
                int indexColumnIndex = cursorMedia.getColumnIndex("Index");
                int diaryIdColumnIndex = cursorMedia.getColumnIndex("DiaryEntryId");

                try {
                    while (cursorMedia.moveToNext()) {
                        String fileEnding = cursorMedia.getString(fileEndingColumnIndex);

                        if(checkIsValidImageFile(fileEnding) ) {
                            byte[] imageData = cursorMedia.getBlob(dataColumnIndex);

                            int imageIndex = cursorMedia.getInt(indexColumnIndex);
                            String diaryId = cursorMedia.getString(diaryIdColumnIndex);

                            String type = "photo";
                            String entryUid = entryIDUIDHashMap.get(diaryId);

                            try {
                                String newFileName = ImportHelper.getNewFilename(fileEnding.replaceAll(".", ""), type);

                                AttachmentInfo attachment = new AttachmentInfo(entryUid, type, newFileName, imageIndex);
                                PersistanceHelper.saveAttachment(attachment);

                                // Save the image
                                ImportHelper.writeFileAndCompress(AppLifetimeStorageUtils.getMediaDirPath() + "/" + type + "/" + newFileName, imageData);
                            } catch (Exception e) {
                                AppLog.e(e.getMessage());
                                activity.runOnUiThread(() -> Static.showToast(e.getMessage(), Toast.LENGTH_LONG));
                                imageData = null;
                            }
                        }

                    }
                } finally {
                    cursorMedia.close();
                }

                activity.runOnUiThread(() -> {
                    Static.showToastLong("Imported " + entriesCount + " entries successfully!");
                    MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners();
                    progress.cancel();
                });


            } catch (Exception e) {
                activity.runOnUiThread(() -> {
                    AppLog.e(Arrays.toString(e.getStackTrace()) + ", " + e.getMessage());
                    Static.showToastLong("Import failed! " + e.getMessage());
                    progress.cancel();
                });
            }
        });

    }

    public static boolean checkIsValidImageFile(String extension) {
        return ".png".equalsIgnoreCase(extension) || ".gif".equalsIgnoreCase(extension) || ".jpg".equalsIgnoreCase(extension) || ".jpeg".equalsIgnoreCase(extension) || ".sticker".equalsIgnoreCase(extension);
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = MyApp.getInstance().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static String cleanPreserveLineBreaks(String html) {
        // get pretty printed html with preserved br and p tags
        String prettyPrintedBodyFragment = Jsoup.clean(html, "", Safelist.none().addTags("br", "p"), new Document.OutputSettings().prettyPrint(true));
        // get plain text with preserved line breaks by disabled prettyPrint
        String lineWithBreaks = Jsoup.clean(prettyPrintedBodyFragment, "", Safelist.none(), new Document.OutputSettings().prettyPrint(false));
        lineWithBreaks = lineWithBreaks.replaceAll("&nbsp;", "");
        lineWithBreaks=  lineWithBreaks.replaceAll("&amp;", "&");
        return lineWithBreaks;
    }


}