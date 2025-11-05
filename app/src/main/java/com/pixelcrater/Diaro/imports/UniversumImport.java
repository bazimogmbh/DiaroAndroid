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
import com.pixelcrater.Diaro.model.PersistanceHelper;
import com.pixelcrater.Diaro.model.TagInfo;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

//https://play.google.com/store/apps/details?id=ru.schustovd.diary&hl=en
public class UniversumImport {

    private static String FOLDER_TITLE = "Universum";
    private static String FOLDER_COLOR = "#4E4E4E";
    private static int DEFAULT_ZOOM = 11;
    private static String OFFSET = "+00:00";
    private String dateFormat = "yyyy-MM-dd-HH:mm:ss";

    public UniversumImport(String filePath, Activity activity) {

        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

        ProgressDialog progress = new ProgressDialog(activity);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(false);
        progress.setTitle(R.string.importing_data);
        progress.setMessage(fileName);
        progress.setCancelable(false);
        progress.show();

        AsyncTask.execute(() -> {
            // Put all entries under "Universum" folder"
            FolderInfo folder = new FolderInfo(null, FOLDER_TITLE, FOLDER_COLOR);
            folder.setPattern("");
            String tmpfolderUid = PersistanceHelper.saveFolder(folder);

            TagInfo photoTag = new TagInfo(Static.generateRandomUid(), "Photo");
            String photoTagUid = PersistanceHelper.saveTag(photoTag);

            TagInfo noteTag = new TagInfo(Static.generateRandomUid(), "Note");
            String noteTagUid = PersistanceHelper.saveTag(noteTag);

            TagInfo paintingTag = new TagInfo(Static.generateRandomUid(), "Painting");
            String paintingUid = PersistanceHelper.saveTag(paintingTag);

            TagInfo rateTag = new TagInfo(Static.generateRandomUid(), "Mood");
            String rateTagUid = PersistanceHelper.saveTag(rateTag);

            TagInfo financeTag = new TagInfo(Static.generateRandomUid(), "Finance");
            String financeTagUid = PersistanceHelper.saveTag(financeTag);

            TagInfo taskTag = new TagInfo(Static.generateRandomUid(), "Task");
            String taskTagUid = PersistanceHelper.saveTag(taskTag);

            TagInfo ideaTag = new TagInfo(Static.generateRandomUid(), "Idea");
            String ideaTagUid = PersistanceHelper.saveTag(ideaTag);

            String listOfEntriesAsJson = "";
            ZipFile zipFile = null;

            try {
                String txtContent = "";
                zipFile = new ZipFile(filePath);
                Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();

                ArrayList<ZipEntry> imagesFile = new ArrayList<>();
                HashMap<String, String> photoEntryUidMap = new HashMap<>();
                HashMap<String, String> paintEntryUidMap = new HashMap<>();

                // 1) READ all zip entries
                while (zipEntries.hasMoreElements()) {
                    ZipEntry zipEntry = zipEntries.nextElement();
                    String zipEntryName = zipEntry.getName();
                    String name = FilenameUtils.getName(zipEntryName);
                    String extension = FilenameUtils.getExtension(zipEntryName);

                    //check if the entry is json object and not a dir
                    if ("pr".equals(extension) && !zipEntry.isDirectory()) {
                        try {
                            InputStream stream = zipFile.getInputStream(zipEntry);
                            listOfEntriesAsJson = IOUtils.toString(stream, "UTF-8");
                            stream.close();
                        } catch (Exception e) {
                            System.out.println(e.getLocalizedMessage());
                        }
                    } else if (!zipEntry.isDirectory()) {
                        AppLog.e("add->" + zipEntry.getName());
                        imagesFile.add(zipEntry);
                    }

                }

                // DONE READING ZIP ENTRIES
                // Process read data
                JSONObject jObject = new JSONObject(listOfEntriesAsJson);
                JSONArray jArray = jObject.getJSONArray("marks");
                progress.setMax(jArray.length());

                int txtC = jArray.length();

                AppLog.e("Found-> " + txtC + " entries!");

                int entriesCounter = 0;

                //2 Iterate through each diarium entry and parse the data

                for (int i = 0; i < jArray.length(); i++) {

                    final int displayCount = ++entriesCounter;
                    activity.runOnUiThread(() -> progress.setProgress(displayCount));

                    try {
                        String entry_title = "";
                        String entry_text = "";
                        ArrayList<String> tagUids = new ArrayList<>();
                        StringBuilder tags = new StringBuilder();

                        JSONObject oneObject = jArray.getJSONObject(i);
                        // Pulling items from the array
                        String typeItem = oneObject.getString("type");
                        String dateItem = oneObject.getString("date");
                        String timeItem = oneObject.getString("time");


                        if (typeItem.compareTo("ru.schustovd.diary.api.ShapeMark") == 0) {
                            continue;
                        }


                        if (typeItem.compareTo("ru.schustovd.diary.api.TaskMark") == 0) {
                            entry_title = oneObject.getString("comment");
                            entry_text = oneObject.getString("conclusion");
                            tagUids.add(taskTagUid);
                        }

                        if (typeItem.compareTo("ru.schustovd.diary.api.CommentMark") == 0) {
                            entry_text = oneObject.getString("comment");
                            tagUids.add(noteTagUid);
                        }

                        if (typeItem.compareTo("ru.schustovd.diary.api.MoneyMark") == 0) {
                            entry_title = "" + oneObject.getDouble("money") + " €";
                            tagUids.add(financeTagUid);

                            if (oneObject.has("comment"))
                                entry_text = oneObject.getString("comment");
                        }

                        if (typeItem.compareTo("ru.schustovd.diary.api.IdeaMark") == 0) {
                            entry_title = "" + oneObject.getString("comment");
                            tagUids.add(ideaTagUid);
                        }

                        if (typeItem.compareTo("ru.schustovd.diary.api.PhotoMark") == 0) {
                            entry_text = oneObject.getString("comment");
                            tagUids.add(photoTagUid);
                        }

                        if (typeItem.compareTo("ru.schustovd.diary.api.PaintMark") == 0) {
                            entry_text = "";
                            tagUids.add(paintingUid);
                        }

                        int moodItem = 0;

                        if (typeItem.compareTo("ru.schustovd.diary.api.RateMark") == 0) {
                            continue;
                            /**   int mood = oneObject.getInt("grade");
                             // 1 sad, 2 ok , 3 great
                             if (mood == 1)
                             moodItem = 5;
                             if (mood == 2)
                             moodItem = 3;
                             if (mood == 3)
                             moodItem = 1;
                             **/


                        }

                        String fullDate = dateItem + "-" + timeItem;
                        DateTimeFormatter formatter = DateTimeFormat.forPattern(dateFormat).withZoneUTC();
                        DateTime dt = formatter.parseDateTime(fullDate);

                        String entry_uid = Static.generateRandomUid();
                        long entry_date = dt.getMillis();
                        //  OFFSET = MyDateTimeUtils.getCurrentTimeZoneOffset(entry_date);

                        String location_uid = "";

                        for (int j = 0; j < tagUids.size(); j++) {
                            tags.append(",").append(tagUids.get(j));

                            if (j == (tagUids.size() - 1)) {
                                tags.append(",");
                            }
                        }

                        int entry_mood = moodItem;

                        //--saving the entries
                        EntryInfo entry = new EntryInfo();
                        entry.setUid(entry_uid);
                        entry.setDate(entry_date);
                        entry.setText(entry_text.toString());
                        entry.setTitle(entry_title);
                        entry.setFolderUid(tmpfolderUid); // journey
                        entry.setLocationUid(location_uid);
                        entry.setTags(tags.toString());
                        entry.setTzOffset(OFFSET);
                        // entry.setWeatherInfo(weather_temp, weather_icon, weather_description);
                        entry.setMood(entry_mood);
                        PersistanceHelper.saveEntry(entry);

                        String photoItem = "";
                        if (typeItem.compareTo("ru.schustovd.diary.api.PhotoMark") == 0) {
                            photoItem = oneObject.getString("photo");
                            photoEntryUidMap.put(photoItem, entry_uid);
                        }
                        if (typeItem.compareTo("ru.schustovd.diary.api.PaintMark") == 0) {
                            photoItem = oneObject.getString("paint");
                            paintEntryUidMap.put(photoItem, entry_uid);
                        }

                    } catch (JSONException e) {
                        // Oops
                    }
                }

                //--collecting and renaming attachments
                String type = "photo";
                long position = 1;
                // Iterate through images
                for (ZipEntry imageEntry : imagesFile) {
                    String zipEntryName = imageEntry.getName();
                    String currentFileName = FilenameUtils.getName(zipEntryName);
                    String extension = ".jpg";
                    String newFileName = ImportHelper.getNewFilename(extension, type);

                    String entryUid = "";
                    if (zipEntryName.startsWith("Photo/"))
                        entryUid = photoEntryUidMap.get(zipEntryName);
                    if (zipEntryName.startsWith("Paint/"))
                        entryUid = paintEntryUidMap.get(zipEntryName);

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


}