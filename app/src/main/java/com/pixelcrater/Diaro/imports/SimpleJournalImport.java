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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.Instant;
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
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SimpleJournalImport {

    private static String OFFSET = "+00:00";
    private static int DEFAULT_ZOOM = 11;
    // 2020-09-18T19:04:56.133+0200
    private String dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private ArrayList<Integer> colorsArrayList = new ArrayList<>();

    public SimpleJournalImport(String filePath, Activity activity) {

        int[] folderColorsArray = activity.getResources().getIntArray(R.array.folder_colors);

        for (int i = 0; i < folderColorsArray.length; i++) {
            colorsArrayList.add(folderColorsArray[i]);
        }

        ProgressDialog progress = new ProgressDialog(activity);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(false);
        progress.setTitle(R.string.importing_data);

        progress.setCancelable(false);
        progress.show();

        TagInfo tagSimpleJournal = new TagInfo(Static.generateRandomUid(), "Simple Journal");
        String tagSimpleJournalUid = PersistanceHelper.saveTag(tagSimpleJournal);

        AsyncTask.execute(() -> {

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

                            String findString = "{\"entries\"";
                            int firstIndex = listOfEntriesAsJson.indexOf(findString);
                            listOfEntriesAsJson = listOfEntriesAsJson.substring(firstIndex);

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

                JSONObject jsonObject = new JSONObject(listOfEntriesAsJson);
                JSONArray entriesJsonArray = jsonObject.getJSONArray("entries");
                JSONArray journalsJsonArray = jsonObject.getJSONArray("journals");
                JSONArray imagesJsonArray = jsonObject.getJSONArray("images");

                HashMap<String, String> journalUidFolderMap = new HashMap<>();
                HashMap<String, String> imageFileNamesUidMap = new HashMap<>();
                HashMap<String, String> photoEntryUidMap = new HashMap<>();
                int txtC = entriesJsonArray.length();
                int imgC = imagesFile.size();
                int total = txtC + imgC;

                progress.setMax(total);

                AppLog.e("Found-> " + txtC + " entries, " + journalsJsonArray.length() + " journals, " + imgC + " images!");


                // create random object - reuse this as often as possible
                Random random = new Random();

                // Journals -> Folder ( uuid, name )
                for (int i = 0; i < journalsJsonArray.length(); i++) {

                    JSONObject oneObject = journalsJsonArray.getJSONObject(i);
                    String folderTitle = oneObject.getString("name");
                    String uuid = oneObject.getString("uuid");

                    int nextInt = random.nextInt(colorsArrayList.size());

                    String folderColor = String.format("#%06x", colorsArrayList.get(nextInt));

                    FolderInfo folder = new FolderInfo(null, folderTitle, folderColor);
                    folder.setPattern("");
                    String tmpfolderUid = PersistanceHelper.saveFolder(folder);

                    journalUidFolderMap.put(uuid, tmpfolderUid);
                }

                // Images -> ( uuid, filename )
                for (int i = 0; i < imagesJsonArray.length(); i++) {
                    JSONObject oneObject = imagesJsonArray.getJSONObject(i);
                    String filename = oneObject.getString("filename");
                    String uuid = oneObject.getString("uuid");
                    imageFileNamesUidMap.put(filename, uuid);
                }

                int entriesCounter = 0;

                //2 Iterate through each entry and parse the data
                for (int i = 0; i < entriesJsonArray.length(); i++) {

                    final int displayCount = ++entriesCounter;
                    activity.runOnUiThread(() -> progress.setProgress(displayCount));

                    JSONObject entryJsonObject = entriesJsonArray.getJSONObject(i);

                    String entry_title = "";
                    String entry_text = "";
                    if (entryJsonObject.has("text")) {
                        entry_text = entryJsonObject.getString("text");
                        entry_text = entry_text.trim();

                        entry_text = entry_text.replaceAll("_", " ");
                    }

                    String created_at = entryJsonObject.getString("created_at");

                    long entry_date = Instant.parse(created_at).getMillis();
                    OFFSET = MyDateTimeUtils.getCurrentTimeZoneOffset(entry_date);
                    AppLog.e("Offset->" + OFFSET);

                    String tmpfolderUid = "";
                    if (entryJsonObject.has("journal_uuid")) {
                        String journalUid  = entryJsonObject.getString("journal_uuid");
                        tmpfolderUid =  journalUidFolderMap.get(journalUid);
                    }

                    // Import Tags
                    ArrayList<String> tagUids = new ArrayList<>();
                    tagUids.add(tagSimpleJournalUid);

                    // Locations
                    String tmpLocationUid = "";
                    double latitude = 0.0;
                    if (entryJsonObject.has("location_latitude")) {
                        latitude = entryJsonObject.getDouble("location_latitude");
                        latitude =   ImportHelper.round(latitude,6);
                    }

                    double longitude = 0.0;
                    if (entryJsonObject.has("location_longitude")) {
                        longitude = entryJsonObject.getDouble("location_longitude");
                        longitude =   ImportHelper.round(longitude,6);
                    }

                    if (latitude != 0.0 && longitude != 0.0) {
                        LocationInfo locationInfo = new LocationInfo(Static.generateRandomUid(), "", "", latitude + "", longitude + "", DEFAULT_ZOOM);
                        tmpLocationUid = PersistanceHelper.saveLocation(locationInfo, false);
                    }

                    //--saving the entries
                    EntryInfo entry = new EntryInfo();
                    entry.setUid(Static.generateRandomUid());
                    entry.setDate(entry_date);
                    entry.setText(entry_text);
                    entry.setTitle(entry_title);
                    entry.setFolderUid(tmpfolderUid);
                    entry.setLocationUid(tmpLocationUid);

                    StringBuilder tags = new StringBuilder();
                    for (int tagIndex = 0; tagIndex < tagUids.size(); tagIndex++) {
                        tags.append(",").append(tagUids.get(tagIndex));

                        if (tagIndex == (tagUids.size() - 1)) {
                            tags.append(",");
                        }
                    }
                    entry.setTags(tags.toString());
                    entry.setTzOffset(OFFSET);

                    PersistanceHelper.saveEntry(entry);

                    JSONArray photosArray = entryJsonObject.getJSONArray("images");
                    for (int j = 0; j < photosArray.length(); j++) {
                        JSONObject photosArrayJSONObject = photosArray.getJSONObject(j);
                        String photoUID = photosArrayJSONObject.getString("uuid");

                        photoEntryUidMap.put(photoUID, entry.uid);
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
                    String photoUid = imageFileNamesUidMap.get(currentFileName);
                    String entryUid = photoEntryUidMap.get(photoUid);

                    AppLog.e(currentFileName + " , " + photoUid + ", " + photoUid);

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