package com.pixelcrater.Diaro.imports;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
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
import com.pixelcrater.Diaro.utils.MyDateTimeUtils;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;
import com.sandstorm.weather.WeatherHelper;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

//https://play.google.com/store/apps/details?id=com.fairapps.memorize&hl=en
public class MemorizeImport {

    private static String FOLDER_TITLE = "Memorize";
    private static String FOLDER_COLOR = "#61B198";
    private static int DEFAULT_ZOOM = 11;
    private static String OFFSET = "+00:00";
    private String dateFormat = "yyyy-MM-dd-HH:mm:ss";

    public MemorizeImport(String filePath, Activity activity) {

        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        ProgressDialog progress = new ProgressDialog(activity);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(false);
        progress.setTitle(R.string.importing_data);
        //   progress.setMessage(fileName);
        progress.setCancelable(false);
        progress.show();

        TagInfo tagMemorize = new TagInfo(Static.generateRandomUid(), "Memorize");
        String tagMemorizeUid = PersistanceHelper.saveTag(tagMemorize);

        AsyncTask.execute(() -> {
            // Put all entries under "Universum" folder"
            // FolderInfo folder = new FolderInfo(null, FOLDER_TITLE, FOLDER_COLOR);
            //  folder.setPattern("");
            // String tmpfolderUid = PersistanceHelper.saveFolder(folder);

            String listOfEntriesAsJson = "";
            ZipFile zipFile = null;

            try {
                zipFile = new ZipFile(filePath);
                Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();

                ArrayList<ZipEntry> imagesFile = new ArrayList<>();

                // 1) READ all zip entries
                while (zipEntries.hasMoreElements()) {
                    ZipEntry zipEntry = zipEntries.nextElement();
                    String zipEntryName = zipEntry.getName();
                    String name = FilenameUtils.getName(zipEntryName);
                    String extension = FilenameUtils.getExtension(zipEntryName);

                    //check if the entry is json object and not a dir
                    if ("json".equals(extension) && !zipEntry.isDirectory()) {
                        try {
                            InputStream stream = zipFile.getInputStream(zipEntry);
                            listOfEntriesAsJson = IOUtils.toString(stream, "UTF-8");
                            stream.close();
                        } catch (Exception e) {
                            System.out.println(e.getLocalizedMessage());
                        }
                    } else if ("jpg".equals(extension) && !zipEntry.isDirectory()) {
                        //    AppLog.e("add->" + zipEntry.getName());
                        imagesFile.add(zipEntry);
                    }

                }

                // DONE READING ZIP ENTRIES
                // Process read data
                JSONArray entriesJsonArray = new JSONArray(listOfEntriesAsJson);
                int txtC = entriesJsonArray.length();

                int imgC = imagesFile.size();

                int total = txtC + imgC;

                progress.setMax(total);

                AppLog.e("Found-> " + txtC + " entries, " + imgC + " images!");

                HashMap<String, String> photoEntryUidMap = new HashMap<>();
                int entriesCounter = 0;

                //2 Iterate through each entry and parse the data
                for (int i = 0; i < entriesJsonArray.length(); i++) {

                    final int displayCount = ++entriesCounter;
                    activity.runOnUiThread(() -> progress.setProgress(displayCount));

                    JSONObject entryJsonObject = entriesJsonArray.getJSONObject(i);
                    String entry_title = "";
                    if (entryJsonObject.has("title")) {
                        entry_title = entryJsonObject.getString("title");
                    }

                    String entry_text = "";
                    if (entryJsonObject.has("text")) {
                        entry_text = entryJsonObject.getString("text");
                        entry_text = entry_text.trim();
                        entry_text = entry_text.replaceAll("(.*?.jpg\\))", "");
                        entry_text = entry_text.replaceAll("#", "");
                        entry_text = entry_text.replaceAll("_", "");
                        entry_text = entry_text.replaceAll("\\*\\*", "");
                        entry_text = entry_text.replaceAll("- ", "• ");

                        entry_text = entry_text.replaceAll("<FONT COLOR=.*?.>", "");
                        entry_text = entry_text.replaceAll("</FONT>", "");
                        entry_text = entry_text.replaceAll("\n\n\n", "\n\n");
                    }

                    long entry_date = entryJsonObject.getLong("createdDate");
                    OFFSET = MyDateTimeUtils.getCurrentTimeZoneOffset(entry_date);
                    AppLog.e("Offset->" + OFFSET);

                    JSONArray photosArray = entryJsonObject.getJSONArray("photos");
                    JSONArray tagsArray = entryJsonObject.getJSONArray("tags");

                    String tmpfolderUid = "";
                    String tmpLocationUid = "";

                    // Import Tags
                    ArrayList<String> tagUids = new ArrayList<>();
                    StringBuilder tags = new StringBuilder();

                    for (int j = 0; j < tagsArray.length(); j++) {
                        String tagTitle = tagsArray.getString(j);

                        TagInfo tagInfo = new TagInfo(Static.generateRandomUid(), tagTitle);
                        String tagUid = PersistanceHelper.saveTag(tagInfo);
                        tagUids.add(tagUid);
                    }
                    tagUids.add(tagMemorizeUid);


                    long categoryColor = -1L;
                    if (entryJsonObject.has("categoryColor")) {
                        categoryColor = entryJsonObject.getLong("categoryColor");
                    }

                    String categoryName = "";
                    if (entryJsonObject.has("categoryName")) {

                        String hexColor = "";
                        if (categoryColor != -1L)
                            hexColor = String.format("#%06X", (0xFFFFFF & categoryColor));

                        categoryName = entryJsonObject.getString("categoryName");
                        FolderInfo folder = new FolderInfo(null, categoryName, hexColor);
                        folder.setPattern("");
                        tmpfolderUid = PersistanceHelper.saveFolder(folder);
                    }

                    double latitude = 0.0;
                    if (entryJsonObject.has("latitude")) {
                        latitude = entryJsonObject.getDouble("latitude");
                        latitude =   ImportHelper.round(latitude,6);
                    }

                    double longitude = 0.0;
                    if (entryJsonObject.has("longitude")) {
                        longitude = entryJsonObject.getDouble("longitude");
                        longitude =   ImportHelper.round(longitude,6);
                    }

                    String address = "";
                    if (entryJsonObject.has("address")) {
                        address = entryJsonObject.getString("address");
                    }

                    String placeTitle = "";
                    if (entryJsonObject.has("placeName")) {
                        placeTitle = entryJsonObject.getString("placeName");
                    }

                    if (latitude != 0.0 && longitude != 0.0) {
                        LocationInfo locationInfo = new LocationInfo(Static.generateRandomUid(), placeTitle, address, latitude + "", longitude + "", DEFAULT_ZOOM);
                        tmpLocationUid = PersistanceHelper.saveLocation(locationInfo, false);
                    }


                    double weather_temp = 0.0;
                    if (entryJsonObject.has("temperature")) {
                        weather_temp = entryJsonObject.getDouble("temperature");
                        weather_temp = WeatherHelper.fahrenheitToCelcius(weather_temp);
                    }
                    String weather_icon = "";
                    if (entryJsonObject.has("weatherCode")) {
                        weather_icon = entryJsonObject.getString("weatherCode");
                        if (weather_icon.compareTo("clear-night") == 0)
                            weather_icon = "night-clear";
                        if (weather_icon.compareTo("clear-day") == 0)
                            weather_icon = "day-sunny";
                        if (weather_icon.compareTo("partly-cloudy-day") == 0)
                            weather_icon = "day-cloudy";
                        if (weather_icon.compareTo("partly-cloudy-night") == 0)
                            weather_icon = "night-alt-cloudy";
                    }

                    String weather_description = "";
                    if (entryJsonObject.has("weatherDescription")) {
                        weather_description = entryJsonObject.getString("weatherDescription");
                    }

                    //--saving the entries
                    EntryInfo entry = new EntryInfo();
                    entry.setUid(Static.generateRandomUid());
                    entry.setDate(entry_date);
                    entry.setText(entry_text);
                    entry.setTitle(entry_title);
                    entry.setFolderUid(tmpfolderUid);
                    entry.setLocationUid(tmpLocationUid);
                    for (int tagIndex = 0; tagIndex < tagUids.size(); tagIndex++) {
                        tags.append(",").append(tagUids.get(tagIndex));

                        if (tagIndex == (tagUids.size() - 1)) {
                            tags.append(",");
                        }
                    }
                    entry.setTags(tags.toString());
                    entry.setTzOffset(OFFSET);
                    if (weather_temp != 0.0 && !StringUtils.isEmpty(weather_icon))
                        entry.setWeatherInfo(weather_temp, weather_icon, weather_description);
                    //    entry.setMood(entry_mood);
                    PersistanceHelper.saveEntry(entry);

                    for (int j = 0; j < photosArray.length(); j++) {
                        String photosArrayJSONObject = photosArray.getString(j);
                        photoEntryUidMap.put(photosArrayJSONObject, entry.uid);
                    }
                }

                // Process images
                //--collecting and renaming attachments
                String type = "photo";
                long position = 1;
                // Iterate through images
                for (ZipEntry imageEntry : imagesFile) {
                    final int displayCount = ++entriesCounter;

                    activity.runOnUiThread(() -> progress.setProgress(displayCount));

                    String zipEntryName = imageEntry.getName();
                    String currentFileName = FilenameUtils.getName(zipEntryName);
                    String extension = FilenameUtils.getExtension(currentFileName);
                    String newFileName = ImportHelper.getNewFilename(extension, type);

                   // activity.runOnUiThread(() -> progress.setMessage(currentFileName));

                    String entryUid = photoEntryUidMap.get(zipEntryName);

                    if (!StringUtils.isEmpty(entryUid)) {
                        AttachmentInfo attachment = new AttachmentInfo(entryUid, type, newFileName, position);
                        PersistanceHelper.saveAttachment(attachment);

                        try {
                            ImportHelper.writeFileAndCompress(AppLifetimeStorageUtils.getMediaDirPath() + "/" + type + "/" + newFileName, ImportHelper.getBytecodeForZipEntry(imageEntry, zipFile));
                        } catch (Exception e) {
                            AppLog.e(e.getMessage());
                            activity.runOnUiThread(() -> Static.showToast(e.getMessage(), Toast.LENGTH_LONG));
                        }
                    }

                }


                // Done importing
                zipFile.close();

                activity.runOnUiThread(() -> {
                    Static.showToastLong("Imported " + txtC + " entries successfully!");
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

    public static String cleanPreserveLineBreaks(String html) {
        // get pretty printed html with preserved br and p tags
        String prettyPrintedBodyFragment = Jsoup.clean(html, "", Safelist.none().addTags("br", "p"), new Document.OutputSettings().prettyPrint(true));
        // get plain text with preserved line breaks by disabled prettyPrint
        String lineWithBreaks = Jsoup.clean(prettyPrintedBodyFragment, "", Safelist.none(), new Document.OutputSettings().prettyPrint(false));
        lineWithBreaks = lineWithBreaks.replaceAll("&nbsp;", "");
        return lineWithBreaks;
    }


}