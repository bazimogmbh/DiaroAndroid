package com.pixelcrater.Diaro.imports;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.model.AttachmentInfo;
import com.pixelcrater.Diaro.model.EntryInfo;
import com.pixelcrater.Diaro.model.FolderInfo;
import com.pixelcrater.Diaro.model.LocationInfo;
import com.pixelcrater.Diaro.moods.Mood;
import com.pixelcrater.Diaro.model.PersistanceHelper;
import com.pixelcrater.Diaro.model.TagInfo;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyDateTimeUtils;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class JourneyImport {

    //journey fields
    private static final String KEY_JOURNEY_TEXT = "text";                          //DIARO_KEY_ENTRY_TEXT
    private static final String KEY_JOURNEY_DATE_MODIFIED = "date_modified";
    private static final String KEY_JOURNEY_DATE_JOURNAL = "date_journal";          //DIARO_KEY_ENTRY_DATE
    private static final String KEY_JOURNEY_TIMEZONE = "timezone";
    private static final String KEY_JOURNEY_PREVIEW_TEXT = "preview_text";          //DIARO_KEY_ENTRY_TITLE
    private static final String KEY_JOURNEY_ADDRESS = "address";                    //DIARO_KEY_ENTRY_LOCATION_ADDRESS
    private static final String KEY_JOURNEY_LATITUDE = "lat";                       //DIARO_ KEY_ENTRY_LOCATION_LATITUDE
    private static final String KEY_JOURNEY_LONGITUDE = "lon";                      //DIARO_ KEY_ENTRY_LOCATION_LONGITUDE
    private static final String KEY_JOURNEY__MOOD = "mood";
    private static final String KEY_JOURNEY__SENTIMENT = "sentiment";
    private static final String KEY_JOURNEY__WEATHER = "weather";                   //DIARO_KEY_ENTRY_WEATHER
    private static final String KEY_JOURNEY__WEATHER_DEGREE = "degree_c";           //DIARO_KEY_KEY_ENTRY_WEATHER_TEMP
    private static final String KEY_JOURNEY__WEATHER_DESCRIPTION = "description";   //DIARO_KEY_ENTRY_WEATHER_DESC
    private static final String KEY_JOURNEY__WEATHER_ICON = "icon";                 //DIARO_KEY_ENTRY_WEATHER_ICON
    private static final String KEY_JOURNEY__WEATHER_PLACE = "place";               //DIARO_KEY_ENTRY_LOCATION_ADDRESS
    private static final String KEY_JOURNEY_PHOTOS = "photos";                      //DIARO_KEY_ATTACHMENT
    private static final String KEY_JOURNEY_TAGS = "tags";                          //DIARO_KEY_ENTRY_TAGS

    private static String FOLDER_TITLE = "Journey";
    private static String FOLDER_COLOR = "#F0B913";
    private static int DEFAULT_ZOOM = 11;
    private static String OFFSET = "+00:00";


    public JourneyImport(String filePath, Activity activity) {

        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

        ProgressDialog progress = new ProgressDialog(activity);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(false);
        progress.setTitle(R.string.importing_data);
       // progress.setMessage(fileName);
        progress.setCancelable(false);
        progress.show();

        AsyncTask.execute(() -> {

            // Put all entries under "Joureny folder"
            FolderInfo folder = new FolderInfo(null, FOLDER_TITLE, FOLDER_COLOR);
            folder.setPattern("");
            String tmpfolderUid = PersistanceHelper.saveFolder(folder);

            ArrayList<String> listOfEntriesAsJson = new ArrayList<>();
            ZipFile journeyZipFile = null;

            try {
                String json = "";
                journeyZipFile = new ZipFile(filePath);
                Enumeration<? extends ZipEntry> zipEntries = journeyZipFile.entries();

                ArrayList<String> jsonFiles = new ArrayList<>();
                ArrayList<String> imagesFile = new ArrayList<>();
                ArrayList<String> stickerFile = new ArrayList<>();
                ArrayList<String> videoFiles = new ArrayList<>();

                HashMap<String, ZipEntry> imagesFileMap = new HashMap<>();

                // 1) READ all zip entries
                while (zipEntries.hasMoreElements()) {
                    ZipEntry zipEntry = zipEntries.nextElement();

                    String zipEntryName = zipEntry.getName();
                    String name = FilenameUtils.getName(zipEntryName); //
                    String extension = FilenameUtils.getExtension(zipEntryName);

                    //check if the entry is json object and not a dir
                    if (extension.equals("json") && !zipEntry.isDirectory()) {
                        jsonFiles.add(zipEntryName);
                        try {
                            InputStream stream = journeyZipFile.getInputStream(zipEntry);
                            json = IOUtils.toString(stream, "UTF-8");
                            listOfEntriesAsJson.add(json);
                            stream.close();
                        } catch (Exception e) {
                            AppLog.e(e.getMessage());
                        }

                    }

                    if (ImportHelper.checkIsValidImageFile(extension)) {
                        imagesFile.add(zipEntryName);
                        imagesFileMap.put(name, zipEntry);
                        //  AppLog.e("putting "+ file.getName() + " ," + zipEntryName + ", " + zipEntry.getSize());
                    }
                    if (zipEntryName.endsWith(".sticker")) {
                        stickerFile.add(zipEntryName);
                    }
                    if (zipEntryName.endsWith(".mp4")) {
                        videoFiles.add(zipEntryName);
                    }

                }

                // DONE READING ZIP ENTRIES
                progress.setMax(listOfEntriesAsJson.size());

                int jsonsC = jsonFiles.size();
                int imgC = imagesFile.size();
                int stickerC = stickerFile.size();
                int videoC = videoFiles.size();

                AppLog.e("Found-> " + jsonsC + " entries, " + imgC + " images, " + stickerC + " stickers, &" + videoC + " videos!");

                int entriesCounter = 0;

                //2 Iterate through each journey entry and parse the data
                for (String journeyEntry : listOfEntriesAsJson) {
                    AppLog.i(entriesCounter + "  " + journeyEntry);
                    final int displayCount = ++entriesCounter;

                    activity.runOnUiThread(() -> progress.setProgress(displayCount));

                    //create new entry JSONObject with entry as root
                    JSONObject rootJsonObject = new JSONObject(journeyEntry);

                    JSONObject journey_weather = rootJsonObject.getJSONObject(KEY_JOURNEY__WEATHER);

                    //-- saving TAGS
                    String tag_uid = "";
                    String tag_title = "";
                    String tags = "";
                    ArrayList<String> tagUids = new ArrayList<>();

                    if (!rootJsonObject.isNull(KEY_JOURNEY_TAGS)) {
                        JSONArray tagsArray = rootJsonObject.getJSONArray(KEY_JOURNEY_TAGS);
                        for (int i = 0; i < tagsArray.length(); i++) {
                            tag_title = tagsArray.getString(i);

                            TagInfo tagInfo = new TagInfo(Static.generateRandomUid(), tag_title);
                            String tagUid = PersistanceHelper.saveTag(tagInfo);
                            tagUids.add(tagUid);
                        }

                    }
                    //-- saving location
                    String location_uid = "";
                    String title = "";
                    String latitude = "";
                    String longitude = "";
                    if (!rootJsonObject.isNull(KEY_JOURNEY_LATITUDE) && !rootJsonObject.isNull(KEY_JOURNEY_LONGITUDE)) {

                        double lat = rootJsonObject.getDouble(KEY_JOURNEY_LATITUDE);
                        double lng = rootJsonObject.getDouble(KEY_JOURNEY_LONGITUDE);

                        //a bug in journey just saves lat and lng as Double.MAX_VALUE(1.7976931348623157E308)
                        //do not skip this check
                        if (lat != 1.7976931348623157E308 && lng != 1.7976931348623157E308) {
                            latitude = String.format(Locale.ENGLISH, "%.5f", lat);
                            longitude = String.format(Locale.ENGLISH, "%.5f", lng);

                            //if address is available check for address in weather, if no address found, display address as {Lat,Lng}
                            if (!ImportHelper.isNullOrEmpty(rootJsonObject, KEY_JOURNEY_ADDRESS)) {
                                title = rootJsonObject.getString(KEY_JOURNEY_ADDRESS);

                            } else if (!ImportHelper.isNullOrEmpty(journey_weather, KEY_JOURNEY__WEATHER_PLACE)) {
                                title = journey_weather.getString(KEY_JOURNEY__WEATHER_PLACE);

                            } else if (!latitude.isEmpty() && !longitude.isEmpty()) {
                                title = ImportHelper.concatLatLng(latitude, longitude);
                            }

                            LocationInfo locationInfo = new LocationInfo(Static.generateRandomUid(), title, title, latitude, longitude, DEFAULT_ZOOM);
                            location_uid = PersistanceHelper.saveLocation(locationInfo, false);
                        }
                    }

                    //-- collecting weather info
                    //checking if weather exists
                    double weather_temp = 0.0;
                    String weather_description = "";
                    String weather_icon = "";
                    if (journey_weather != null) {
                        if (!ImportHelper.isNullOrEmpty(journey_weather, KEY_JOURNEY__WEATHER_DEGREE) && !ImportHelper.isNullOrEmpty(journey_weather, KEY_JOURNEY__WEATHER_DESCRIPTION) && !ImportHelper.isNullOrEmpty(journey_weather, KEY_JOURNEY__WEATHER_ICON)) {
                            weather_temp = journey_weather.getDouble(KEY_JOURNEY__WEATHER_DEGREE);

                            if(weather_temp < -60  || weather_temp > 60 )
                                weather_temp = 0.0;

                            weather_description = journey_weather.getString(KEY_JOURNEY__WEATHER_DESCRIPTION).toLowerCase();
                            weather_icon = ImportHelper.iconCodeToName(journey_weather.getString(KEY_JOURNEY__WEATHER_ICON), ImportHelper.iconAndNameMap);
                        }
                    }

                    //--saving text and titles
                    String entry_uid = Static.generateRandomUid();
                    String entry_text = "";
                    String entry_title = "";
                    long entry_date = 0L;

                    String journey_text = rootJsonObject.getString(KEY_JOURNEY_TEXT);
                    //   String journey_preview_text = rootJsonObject.getString(KEY_JOURNEY_PREVIEW_TEXT);
                    Long journey_date = rootJsonObject.getLong(KEY_JOURNEY_DATE_JOURNAL);

                    if (!ImportHelper.isNullOrEmpty(rootJsonObject, KEY_JOURNEY_TEXT)) {
                        entry_text = cleanPreserveLineBreaks(journey_text);
                    }

                    if (!ImportHelper.isNullOrEmpty(rootJsonObject, KEY_JOURNEY_PREVIEW_TEXT)) {
                        // Journey does not has a title
                        //   entry_title = journey_preview_text;
                    }

                    if (!ImportHelper.isNullOrEmpty(rootJsonObject, KEY_JOURNEY_DATE_JOURNAL)) {
                        entry_date = journey_date;
                    }


                    OFFSET = "+00:00"; // set offset to utc if the key is ""
                    if (!ImportHelper.isNullOrEmpty(rootJsonObject, KEY_JOURNEY_TIMEZONE)) {
                        String timeZone = rootJsonObject.getString(KEY_JOURNEY_TIMEZONE);

                        // Take "Europe\/Stockholm" and return "+01:00" // journey stores  "Europe\/Stockholm" instead of  "+01:00"
                        TimeZone tz = TimeZone.getTimeZone(timeZone);
                        int offset = tz.getOffset(entry_date);
                        OFFSET = MyDateTimeUtils.printTimeZoneOffset(offset);
                    } else {
                        OFFSET = MyDateTimeUtils.getCurrentTimeZoneOffset(entry_date);
                    }

                    int entry_mood = 0;
                    if (!ImportHelper.isNullOrEmpty(rootJsonObject, KEY_JOURNEY__MOOD) && !ImportHelper.isNullOrEmpty(rootJsonObject, KEY_JOURNEY__SENTIMENT)) {
                        int mood = rootJsonObject.getInt(KEY_JOURNEY__MOOD);

                        if (mood == 1) {
                            double sentiment = rootJsonObject.getDouble(KEY_JOURNEY__SENTIMENT);

                            if (sentiment == 1.75) { // 1.75 = happy
                                entry_mood = Mood.MOOD_AWESOME_ID;
                            }
                            if (sentiment == 1.25) {
                                entry_mood = Mood.MOOD_HAPPY_ID;
                            }
                            if (sentiment == 1) { // 1
                                entry_mood = Mood.MOOD_NEUTRAL_ID;
                            }
                            if (sentiment == .75) { // 0.75 =  bad
                                entry_mood = Mood.MOOD_BAD_ID;
                            }
                            if (sentiment == .25) { //0.25 = sad
                                entry_mood = Mood.MOOD_AWFUL_ID;
                            }
                        }

                    }

                    //looping through all the tags appending the tags in a String
                    for (int i = 0; i < tagUids.size(); i++) {
                        tags += "," + tagUids.get(i);
                        if (i == (tagUids.size() - 1)) {
                            tags += ",";
                        }
                    }

                    //--saving the entries
                    EntryInfo entry = new EntryInfo();
                    entry.setUid(entry_uid);
                    entry.setDate(entry_date);
                    entry.setText(entry_text);
                    entry.setTitle(entry_title);
                    entry.setFolderUid(tmpfolderUid); // journey
                    entry.setLocationUid(location_uid);
                    entry.setTags(tags);
                    entry.setTzOffset(OFFSET);
                    entry.setWeatherInfo(weather_temp, weather_icon, weather_description);
                    entry.setMood(entry_mood);
                    PersistanceHelper.saveEntry(entry);

                    //--collecting and renaming attachments
                    String type = "photo";
                    long position = 1;

                    if (!ImportHelper.isNullOrEmpty(rootJsonObject, KEY_JOURNEY_PHOTOS)) {
                        JSONArray photosArray = rootJsonObject.getJSONArray(KEY_JOURNEY_PHOTOS);

                        AppLog.d("Found " + photosArray.length() + " photos -> " + photosArray);
                        for (int i = 0; i < photosArray.length(); i++) {
                            //  1560637101025-3fdef30b69eb762e-3fd9a2e1e587e96e.jpg
                            String currentFileName = photosArray.getString(i);
                            String extension = FilenameUtils.getExtension(currentFileName); // returns "jpg"

                            if (!ImportHelper.checkIsValidImageFile(extension)) {
                                continue;
                            }

                            if (extension.equals("sticker")) {
                                extension = "jpg";
                            }

                            // photo_20200103_603835.jpg
                            String newFileName = ImportHelper.getNewFilename(extension, type);

                            AppLog.i(i + " -> " + currentFileName + " " + newFileName);

                            AttachmentInfo attachment = new AttachmentInfo(entry.uid, type, newFileName, position++);
                            PersistanceHelper.saveAttachment(attachment);

                            // Query
                            ZipEntry mediaZipEntry = imagesFileMap.get(currentFileName);
                            if (mediaZipEntry != null) {
                                try {
                                    ImportHelper.writeFileAndCompress(AppLifetimeStorageUtils.getMediaDirPath() + "/" + type + "/" + newFileName, ImportHelper.getBytecodeForZipEntry(mediaZipEntry, journeyZipFile));
                                } catch (Exception e) {
                                    AppLog.e(e.getMessage());
                                    activity.runOnUiThread(() -> Static.showToast(e.getMessage(), Toast.LENGTH_LONG));
                                }
                            }
                        }

                    }

                }

                // Done importing
                journeyZipFile.close();

                activity.runOnUiThread(() -> {
                    Static.showToastLong("Imported " + listOfEntriesAsJson.size() + " entries successfully!");
                    MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners();
                    progress.cancel();
                });


            } catch (Exception e) {
                activity.runOnUiThread(() -> {
                    AppLog.e(e.getStackTrace() + "" + e.getMessage());
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
