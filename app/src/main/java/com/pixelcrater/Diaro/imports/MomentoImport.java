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
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MomentoImport {

    private static String FOLDER_TITLE = "Momento";
    private static String FOLDER_COLOR = "#C26C2E";
    private static int DEFAULT_ZOOM = 11;
    private static String OFFSET = "+00:00";
    private String dateFormat = "dd MMMM yyyy:HH:mm";

    public MomentoImport(String filePath, Activity activity) {

        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        ProgressDialog progress = new ProgressDialog(activity);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(false);
        progress.setTitle(R.string.importing_data);
        //   progress.setMessage(fileName);
        progress.setCancelable(false);
        progress.show();

        AsyncTask.execute(() -> {
            //Put all entries under Momento folder"
            FolderInfo folder = new FolderInfo(null, FOLDER_TITLE, FOLDER_COLOR);
            folder.setPattern("");
            String tmpfolderUid = PersistanceHelper.saveFolder(folder);

            String txtContent = "";
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
                    if ("txt".equals(extension) && !zipEntry.isDirectory() && !zipEntryName.contains("MACOSX")) {
                        AppLog.e("reading" + zipEntryName);
                        try {
                            InputStream stream = zipFile.getInputStream(zipEntry);
                            txtContent = IOUtils.toString(stream, "UTF-8");
                            stream.close();
                        } catch (Exception e) {
                            System.out.println(e.getLocalizedMessage());
                        }
                    } else if ("jpg".equals(extension) && !zipEntry.isDirectory()) {
                        imagesFile.add(zipEntry);
                    }

                }

                // DONE READING ZIP ENTRIES
                // Process read data
                String[] lines = txtContent.split("\\r?\\n");
                ArrayList<MomentoEnty> entries = new ArrayList<>();
                int entriesCount = 0;

                MomentoEnty momentoEnty = new MomentoEnty();
                String dateString = "";

                for (String line : lines) {


                    if (isValidDate(line)) {
                        dateString = line;
                    } else {
                        if (line.matches("^([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$")) {

                            entriesCount++;
                            momentoEnty = new MomentoEnty();
                            momentoEnty.time = line;
                            momentoEnty.date = dateString;
                            entries.add(momentoEnty);
                        } else if (line.contains("Media:")) {
                            momentoEnty.media = line.replaceAll("Media: ", "");
                        } else if (line.contains("At:")) {
                            line = line.replaceAll("At:", "").trim();

                            String locationName = "";
                            String address = "";

                            String lat = "";
                            String lng = "";

                            if (line.contains(":")) {
                                locationName = line.split(":")[0];
                                String locationRest = line.split(":")[1];

                                if (locationRest.contains("(")) {
                                    address = locationRest.split("\\(")[0];

                                    String latLngSTring = locationRest.split("\\(")[1];

                                    latLngSTring = latLngSTring.replaceAll("\\)", "");

                                    lat = latLngSTring.split(",")[0];
                                    lng = latLngSTring.split(",")[1];

                                }
                            }

                            if (!lat.isEmpty() && !lng.isEmpty()) {
                                LocationInfo locationInfo = new LocationInfo(Static.generateRandomUid(), locationName, address, lat, lng, DEFAULT_ZOOM);
                                momentoEnty.at = PersistanceHelper.saveLocation(locationInfo, false);
                            } else
                            {
                                AppLog.e(line);
                                momentoEnty.text = momentoEnty.text + "\n" + line;
                            }

                            //AppLog.e(locationName + " ** " + address + " **  " + lat + " ** " + lng);
                        } else if (line.contains("===========") || line.contains("==========")) {

                        } else {
                            momentoEnty.text = momentoEnty.text + "\n" + line;
                        }
                    }

                }

                int txtC = entries.size();
                int imgC = imagesFile.size();
                int total = txtC + imgC;

                progress.setMax(total);
                AppLog.e("Found-> " + txtC + " entries, " + imgC + " images!");
                HashMap<String, String> photoEntryUidMap = new HashMap<>();
                int entriesCounter = 0;

                //2 Iterate through each entry and parse the data
                for (MomentoEnty momentoEnty1 : entries) {

                    if (!momentoEnty1.text.trim().isEmpty()) {
                        final int displayCount = ++entriesCounter;
                        activity.runOnUiThread(() -> progress.setProgress(displayCount));


                        String dateCreated = momentoEnty1.date + ":" + momentoEnty1.time;
                        DateTimeFormatter formatter = DateTimeFormat.forPattern(dateFormat).withZoneUTC();
                        DateTime dt = formatter.parseDateTime(dateCreated);
                        long entry_date = dt.getMillis();
                        // OFFSET = MyDateTimeUtils.getCurrentTimeZoneOffset(entry_date);

                        //--saving the entries
                        EntryInfo entry = new EntryInfo();
                        entry.setUid(Static.generateRandomUid());
                        entry.setDate(entry_date);

                        String txt = momentoEnty1.text.trim().replaceAll("Feed:", "\nFeed:") ;
                       // txt = txt.replaceAll("\nFeed:", "Feed:") ;
                        entry.setText(txt);
                        entry.setTitle("");
                        entry.setFolderUid(tmpfolderUid);
                        entry.setLocationUid(momentoEnty1.at);
                        entry.setTzOffset(OFFSET);

                        //    entry.setMood(entry_mood);
                        PersistanceHelper.saveEntry(entry);

                        if (!StringUtils.isEmpty(momentoEnty1.media)) {
                            photoEntryUidMap.put(momentoEnty1.media, entry.uid);
                        }
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

                    String entryUid = photoEntryUidMap.get(currentFileName);

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

    public static boolean isValidDate(String inDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(inDate.trim());
        } catch (ParseException pe) {
            return false;
        }
        return true;
    }

    static class MomentoEnty {
        String date = "";
        String time = "";
        String text = "";
        String media = "";
        String at = "";

    }


}