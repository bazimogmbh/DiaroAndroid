package com.pixelcrater.Diaro.imports;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Build;
import android.widget.Toast;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.entries.attachments.AttachmentsStatic;
import com.pixelcrater.Diaro.model.AttachmentInfo;
import com.pixelcrater.Diaro.model.EntryInfo;
import com.pixelcrater.Diaro.model.FolderInfo;
import com.pixelcrater.Diaro.model.LocationInfo;
import com.pixelcrater.Diaro.model.PersistanceHelper;
import com.pixelcrater.Diaro.model.TagInfo;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;
import com.pixelcrater.Diaro.utils.storage.FileUtil;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class DayoneImport {

    //dayOne Fields
    private static final String KEY_DAYONE_TEXT = "text";                                           //DIARO_ KEY_ENTRY_TEXT
    private static final String KEY_DAYONE_LOCATION = "location";                                   //DIARO_KEY_LOCATION
    private static final String KEY_DAYONE_LOCATION_LOCALITY_NAME = "localityName";                 //DIARO_KEY_ENTRY_LOCATION_ADDRESS
    private static final String KEY_DAYONE_LOCATION_COUNTRY = "country";                            //DIARO_KEY_ENTRY_LOCATION_ADDRESS
    private static final String KEY_DAYONE_LOCATION_LONGITUDE = "longitude";                        //DIARO_ KEY_ENTRY_LOCATION_LONGITUDE
    private static final String KEY_DAYONE_LOCATION_LATITUDE = "latitude";                          //DIARO_KEY_ENTRY_LOCATION_LATITUDE
    private static final String KEY_DAYONE_LOCATION_PLACE_NAME = "placeName";                       //DIARO_KEY_ENTRY_LOCATION_ADDRESS
    private static final String KEY_DAYONE_LOCATION_ADMINISTRATIVE_AREA = "administrativeArea";
    private static final String KEY_DAYONE_CREATION_DATE = "creationDate";                           //DIARO_KEY_ENTRY_DATE
    private static final String KEY_DAYONE_TAGS = "tags";                                           //DIARO_KEY_ENTRY_TAGS
    private static final String KEY_DAYONE_TIMEZONE = "timeZone";                                   //DIARO_KEY_ENTRY_TZ_OFFSET
    private static final String KEY_DAYONE_PHOTOS = "photos";                                       //DIARO_KEY_ATTACHMENT
    private static final String KEY_DAYONE_PHOTOS_IDENTIFIER = "identifier";
    private static final String KEY_DAYONE_PHOTOS_MD5 = "md5";                                      //DIARO_KEY_ATTACHMENT_UID
    private static final String KEY_DAYONE_PHOTOS_TYPE = "type";                                    //DIARO_KEY_ATTACHMENT_TYPE

    private static final String KEY_DAYONE_IMAGE_TAG = "![](dayone-moment://";

    private static HashMap<String, String> photosWithOldAndNewName;
    private static HashMap<String, String> photosNamesWithAndWithoutExts;

    public static String dateFormatDayOne = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static String OFFSET = "+00:00";
    private static int DEFAULT_ZOOM = 11;
    private static String FOLDER_TITLE = "Day One";
    private static String FOLDER_COLOR = "#2784B0";

    private static String dayOneEntriesJson;

    public DayoneImport(String filePath, Activity activity) {

        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

        ProgressDialog progress = new ProgressDialog(activity);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(false);
        progress.setTitle(R.string.importing_data);
       // progress.setMessage(fileName);
        progress.setCancelable(false);
        progress.show();

        Runnable importTask = () -> {
            try {
                dayOneEntriesJson = ImportHelper.mergeJsonArrays(getEntriesArrayAndImgFileName(filePath)).toString();

                JSONArray entries = new JSONArray(dayOneEntriesJson);
                photosWithOldAndNewName = new LinkedHashMap<>();
                // Import folders
                FolderInfo folder = new FolderInfo(null, FOLDER_TITLE, FOLDER_COLOR);
                folder.setPattern("");
                String tmpfolderUid = PersistanceHelper.saveFolder(folder);
                String folderUid = "";

                progress.setMax(entries.length());

                //check if entries exists
                JSONObject objAtIndex;
                if (entries.length() > 0) {
                    for (int i = 0; i < entries.length(); i++) {

                        final int displayCount = i;
                        activity.runOnUiThread(() -> progress.setProgress(displayCount));

                        folderUid = tmpfolderUid;

                        //create an Object at index
                        objAtIndex = entries.getJSONObject(i);

                        //-- collecting TAGS
                        StringBuilder tagsEach = new StringBuilder();
                        String tagTitle = "";
                        ArrayList<String> tagUids = new ArrayList<>();
                        //if tags Array is present
                        if (objAtIndex.optJSONArray(KEY_DAYONE_TAGS) != null) {
                            JSONArray tags = objAtIndex.getJSONArray(KEY_DAYONE_TAGS);
                            for (int j = 0; j < tags.length(); j++) {

                                if (tags.getString(j) != null && !tags.getString(j).isEmpty()) {
                                    tagTitle = tags.getString(j);
                                    TagInfo tagInfo = new TagInfo(Static.generateRandomUid(), tagTitle);
                                    String tagUid = PersistanceHelper.saveTag(tagInfo);
                                    tagUids.add(tagUid);
                                }
                            }
                        }
                        //-- collecting location
                        String locationUid = "";
                        String title = "";
                        String latitude = "";
                        String longitude = "";
                        if (!ImportHelper.isNullOrEmpty(objAtIndex, KEY_DAYONE_LOCATION)) {

                            JSONObject dayOneLocation = objAtIndex.optJSONObject(KEY_DAYONE_LOCATION);

                            if (!ImportHelper.isNullOrEmpty(dayOneLocation, KEY_DAYONE_LOCATION_LATITUDE) && !ImportHelper.isNullOrEmpty(dayOneLocation, KEY_DAYONE_LOCATION_LONGITUDE)) {
                                latitude = dayOneLocation.optString(KEY_DAYONE_LOCATION_LATITUDE);
                                longitude = dayOneLocation.optString(KEY_DAYONE_LOCATION_LONGITUDE);
                            }
                            //if place Name is present
                            //get name,country,administrativeArea and placeName
                            String administrativeArea = dayOneLocation.optString(KEY_DAYONE_LOCATION_ADMINISTRATIVE_AREA);
                            String placeName = dayOneLocation.optString(KEY_DAYONE_LOCATION_PLACE_NAME);
                            String localityName = dayOneLocation.optString(KEY_DAYONE_LOCATION_LOCALITY_NAME);
                            String country = dayOneLocation.optString(KEY_DAYONE_LOCATION_COUNTRY);

                            List<String> locationTitleValues = new ArrayList<>(Arrays.asList(placeName, localityName, administrativeArea, country));
                            StringBuilder csvBuilder = new StringBuilder();

                            //filtering out the empty values
                            locationTitleValues.removeAll(Arrays.asList("", null));

                            //if locationTitleValues is notEmpty
                            //append the values in a String
                            if (locationTitleValues.size() != 0) {

                                for (String locationTitle : locationTitleValues) {
                                    csvBuilder.append(locationTitle);
                                    csvBuilder.append(",");
                                }

                                String csv = csvBuilder.toString();
                                title = csv.substring(0, csv.length() - "".length());
                            } else {
                                title = ImportHelper.concatLatLng(latitude, longitude);
                            }
                            //if title is not empty
                            //some lat,lng can be 0, ignore them
                            if (!title.isEmpty() && !latitude.equals("0.0") && !longitude.equals("0.0") && !latitude.equals("0") && !longitude.equals("0")) {
                                LocationInfo location = new LocationInfo(Static.generateRandomUid(), title, title, latitude, longitude, DEFAULT_ZOOM);
                                locationUid = PersistanceHelper.saveLocation(location, false);
                                locationTitleValues.clear();
                            }
                        }

                        //--collect entries info
                        String entryUid = Static.generateRandomUid();
                        String entryTitle = "";
                        String[] entryTextAndTitle;
                        long timeStamp = 0L;
                        String timezoneOffset = "";
                        DateTimeZone timeZone;
                        String entryText = "";
                        EntryInfo entry;

                        //collecting text and titles
                        if (!ImportHelper.isNullOrEmpty(objAtIndex, KEY_DAYONE_TEXT)) {
                            entryText = objAtIndex.optString(KEY_DAYONE_TEXT);
                            entryTextAndTitle = formatDayOneTextAndTitle(entryText);
                            entryTitle = entryTextAndTitle[0];
                            entryText = entryTextAndTitle[1];

                            entryTitle = entryTitle.replaceAll("\\\\.", ".");
                            entryTitle = entryTitle.replaceAll("\\(", "(");
                            entryTitle = entryTitle.replaceAll("\\)", ")");
                            entryTitle = entryTitle.replaceAll("\\[", "[");
                            entryTitle = entryTitle.replaceAll("\\]", "]");
                            entryTitle = entryTitle.replaceAll("\\-", "-");
                            entryTitle = entryTitle.replaceAll("\\!", "!");
                            entryTitle = entryTitle.replaceAll("\\*", "*");
                            entryTitle = entryTitle.replaceAll("\\&", "&");
                            entryTitle = entryTitle.replaceAll("#", "").trim();

                            // entryText = StringEscapeUtils.unescapeJava(entryTextAndTitle[1]);
                            entryText = entryText.replaceAll("\\\\.", ".");
                            entryText = entryText.replaceAll("\\(", "(");
                            entryText = entryText.replaceAll("\\)", ")");
                            entryText = entryText.replaceAll("\\[", "[");
                            entryText = entryText.replaceAll("\\]", "]");
                            entryText = entryText.replaceAll("\\-", "-");
                            entryText = entryText.replaceAll("\\!", "!");
                            entryText = entryText.replaceAll("\\*", "*");
                            entryText = entryText.replaceAll("\\&", "&");
                        }

                        //timezone offset and timeStamp  for DayOne
                        if (!ImportHelper.isNullOrEmpty(objAtIndex, KEY_DAYONE_CREATION_DATE)) {
                            String dateString = objAtIndex.optString(KEY_DAYONE_CREATION_DATE);
                            DateTimeFormatter formatter = DateTimeFormat.forPattern(dateFormatDayOne).withZoneUTC();
                            DateTime dt = formatter.parseDateTime(dateString);
                            timeStamp = dt.getMillis();

                            //check for timezone info in JSON
                            if (!ImportHelper.isNullOrEmpty(objAtIndex, KEY_DAYONE_TIMEZONE)) {
                                timeZone = DateTimeZone.forID(objAtIndex.getString(KEY_DAYONE_TIMEZONE));

                            } else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    timeZone = DateTimeZone.forID(TimeZone.getTimeZone(ZoneId.systemDefault()).getID());
                                } else {
                                    timeZone = DateTimeZone.forID("UTC");
                                }
                            }

                            timezoneOffset = ImportHelper.getUTCOffset(dateFormatDayOne, timeZone, dateString);
                        }

                        //looping through all the tags appending the tags in a String
                        for (int m = 0; m < tagUids.size(); m++) {
                            tagsEach.append(",").append(tagUids.get(m));

                            if (m == (tagUids.size() - 1)) {
                                tagsEach.append(",");
                            }
                        }
                        entry = new EntryInfo();
                        entry.setUid(Static.generateRandomUid());
                        entry.setTzOffset(timezoneOffset);
                        entry.setTitle(entryTitle);
                        entry.setDate(timeStamp);
                        entry.setText(entryText);
                        entry.setFolderUid(folderUid);
                        entry.setLocationUid(locationUid);
                        entry.setTags(tagsEach.toString());

                        //--collect attachments(photos) info
                        String fileName1 = "";
                        String newFileName = "";
                        String type = "photo";
                        int position = 0;

                        AttachmentInfo attachment = null;
                        if (!objAtIndex.isNull(KEY_DAYONE_PHOTOS)) {

                            JSONArray photosArray = objAtIndex.getJSONArray(KEY_DAYONE_PHOTOS);
                            JSONObject attachmentObjAtIndx;
                            for (int k = 0; k < photosArray.length(); k++) {

                                attachmentObjAtIndx = photosArray.optJSONObject(k);

                                //if photo has a identifier String,  entry text will contain this String
                                //replace the identifier name from entry text or title
                                if (!ImportHelper.isNullOrEmpty(attachmentObjAtIndx, KEY_DAYONE_PHOTOS_IDENTIFIER)) {
                                    String identifier = attachmentObjAtIndx.optString(KEY_DAYONE_PHOTOS_IDENTIFIER);
                                    String stringToReplace = KEY_DAYONE_IMAGE_TAG + identifier + ")";
                                    entry.text = StringUtils.replace(entry.text, stringToReplace, "");
                                    entry.title = StringUtils.replace(entry.title, stringToReplace, "");
                                }

                                //generate a new file name
                                String fileNameWithoutExt = "";
                                if (!ImportHelper.isNullOrEmpty(attachmentObjAtIndx, KEY_DAYONE_PHOTOS_MD5)) {
                                    fileNameWithoutExt = attachmentObjAtIndx.optString(KEY_DAYONE_PHOTOS_MD5);

                                    //if the attachment type key is present in JSON (new DayOne JSON)
                                    if (!ImportHelper.isNullOrEmpty(attachmentObjAtIndx, KEY_DAYONE_PHOTOS_TYPE)) {
                                        fileName1 = fileNameWithoutExt + "." + attachmentObjAtIndx.optString(KEY_DAYONE_PHOTOS_TYPE);

                                        DateTime dt = new DateTime();
                                        String millis = String.valueOf(dt.getMillis());
                                        newFileName = type + "_" + dt.toString("yyyyMMdd") + "_" + millis.substring(millis.length() - 6) + "." + attachmentObjAtIndx.optString(KEY_DAYONE_PHOTOS_TYPE);
                                        newFileName = AttachmentsStatic.getNewAttachmentFilenameIfExists(newFileName, type);

                                        attachment = new AttachmentInfo(entry.uid, type, newFileName, position++);
                                        PersistanceHelper.saveAttachment(attachment);

                                        //else loop through the photos name list from zip (old DayOne JSON)
                                    } else {

                                        if (photosNamesWithAndWithoutExts.containsKey(fileNameWithoutExt)) {

                                            fileName1 = photosNamesWithAndWithoutExts.get(fileNameWithoutExt);
                                            String extension = Static.getFileExtension(fileName1);

                                            newFileName = ImportHelper.getNewFilename(extension, type);

                                            attachment = new AttachmentInfo(entry.uid, type, newFileName, position++);
                                            PersistanceHelper.saveAttachment(attachment);
                                        }

                                    }
                                    //add the old name and the new name in a list
                                    //add the attachment to attachments list
                                    if (!photosWithOldAndNewName.containsKey(fileName1)) {
                                        photosWithOldAndNewName.put(fileName1, newFileName);
                                    }

                                }
                            }

                        }

                        PersistanceHelper.saveEntry(entry);
                    }
                }


                // Start importing images
                progress.setMax(photosWithOldAndNewName.size());

                String type = "photo";

                //read the images from zip file
                ZipFile dayOneZipFile = new ZipFile(filePath);
                Enumeration<? extends ZipEntry> zipEntries = dayOneZipFile.entries();
                //  AppLog.e("zipEntries.size" + zipEntries.);

                int read = 0;
                while (zipEntries.hasMoreElements()) {

                    ZipEntry zipEntry = zipEntries.nextElement();
                    String zipEntryName = new File(zipEntry.getName()).getName();
                    String extension = FileUtil.getExtension(zipEntryName);

                    //check if the entry is imageMime object and not a dir checking for compatible attachments read the image
                    if (ImportHelper.checkIsValidImageFile(extension)) {

                        final int displayCount = read++;
                        activity.runOnUiThread(() -> {
                            progress.setMessage(zipEntryName);
                            progress.setProgress(displayCount);
                        });

                        if (photosWithOldAndNewName.containsKey(zipEntryName)) {

                            try {
                                ImportHelper.writeFileAndCompress(AppLifetimeStorageUtils.getMediaDirPath() + "/" + type + "/" + photosWithOldAndNewName.get(zipEntryName), Objects.requireNonNull(ImportHelper.getBytecodeForZipEntry(zipEntry, dayOneZipFile)));
                            } catch (Exception e) {
                                AppLog.e(e.getMessage());
                                activity.runOnUiThread(() -> Static.showToast(e.getMessage(), Toast.LENGTH_LONG));
                            }
                        }
                    }


                }

                dayOneZipFile.close();

                activity.runOnUiThread(() -> {
                    // Done importing
                    Static.showToastLong("Imported " + entries.length() + " entries successfully!");
                    MyApp.getInstance().storageMgr.scheduleNotifyOnStorageDataChangeListeners();
                    progress.cancel();
                });
                // Done importing

            } catch (Exception e) {

                activity.runOnUiThread(() -> {
                    Static.showToastLong("Import failed! " + e.getMessage());
                    progress.cancel();
                });

            }
        };


        MyApp.executeInBackground(importTask);

    }

    public static ArrayList<JSONArray> getEntriesArrayAndImgFileName(String zipFile) throws Exception {
        String json = "";
        ArrayList<JSONArray> entriesJsonArrays = new ArrayList<>();
        photosNamesWithAndWithoutExts = new HashMap<>();

        ZipFile dayOneZip = new ZipFile(zipFile);
        //enumerate though zip to find the json file
        Enumeration<? extends ZipEntry> dayOneZipEntries = dayOneZip.entries();

        while (dayOneZipEntries.hasMoreElements()) {
            ZipEntry entry = dayOneZipEntries.nextElement();
            //if the file is JSON
            //collect all JSON entries in a list
            if (entry.getName().endsWith(".json") && !entry.isDirectory() && !entry.getName().startsWith("__MACOSX") && !entry.getName().startsWith(".")) {
                InputStream stream = dayOneZip.getInputStream(entry);
                json = IOUtils.toString(stream, "UTF-8");
                JSONObject jsonObject = new JSONObject(json);
                JSONArray entries = jsonObject.getJSONArray("entries");
                entriesJsonArrays.add(entries);
                stream.close();
            }

            //if the file is a compatible attachment
            //get file name from file path
            //add the fileNames in a list
            if (ImportHelper.checkPhotosExtension(entry)) {
                String entryName = new File(entry.getName()).getName();
                String entryNameWithoutExt = entryName.substring(0, entryName.indexOf("."));
                photosNamesWithAndWithoutExts.put(entryNameWithoutExt, entryName);
            }
        }
        dayOneZip.close();
        return entriesJsonArrays;
    }

    /**
     * @param text entry text
     * @return String array of formatted tile,text
     */
    public static String[] formatDayOneTextAndTitle(String text) {
        //if EOL is found inside the first 100 chars
        String title;
        if (text.contains("\n") && text.indexOf("\n") <= 100) {

            title = StringUtils.substring(text, 0, text.indexOf("\n"));
            text = StringUtils.substring(text, text.indexOf("\n") + 1);
            return new String[]{title, text};

        } else {
            //if text is smaller than 100 chars with no EOL within 100 chars
            if (text.length() < 100) {
                title = text;
                text = "";
                return new String[]{title, text};
            } else {
                title = "";
                return new String[]{title, text};
            }
        }
    }

}
