package com.pixelcrater.Diaro.imports;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.model.EntryInfo;
import com.pixelcrater.Diaro.model.FolderInfo;
import com.pixelcrater.Diaro.model.PersistanceHelper;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.Static;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class RedNotebookImport {

    private static String FOLDER_TITLE = "Red Notebook";
    private static String FOLDER_COLOR = "#B02F27";
    private static int DEFAULT_ZOOM = 11;
    private static String OFFSET = "+08:00";
    private String dateFormat = "yyyy-MM-dd";

    public RedNotebookImport(String filePath, Activity activity) {

        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

        ProgressDialog progress = new ProgressDialog(activity);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(false);
        progress.setTitle(R.string.importing_data);
        progress.setMessage(fileName);
        progress.setCancelable(false);
        progress.show();

        AsyncTask.execute(() -> {
            // Put all entries under "Red Notebook folder"
            FolderInfo folder = new FolderInfo(null, FOLDER_TITLE, FOLDER_COLOR);
            folder.setPattern("");
            String tmpfolderUid = PersistanceHelper.saveFolder(folder);
            ZipFile zipFile = null;

            try {
                String txtContent = "";
                zipFile = new ZipFile(filePath);
                Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();

                HashMap<String, String> listOfEntriesAsTxt = new HashMap<>();

                // 1) READ all zip entries
                while (zipEntries.hasMoreElements()) {
                    ZipEntry zipEntry = zipEntries.nextElement();
                    String zipEntryName = zipEntry.getName();
                    String name = FilenameUtils.getName(zipEntryName);
                    String fileNameWithOutExt = FilenameUtils.removeExtension(name);
                    String extension = FilenameUtils.getExtension(zipEntryName);

                    //check if the entry is json object and not a dir
                    if ("txt".equals(extension) && !zipEntry.isDirectory()) {
                        try {
                            InputStream stream = zipFile.getInputStream(zipEntry);
                            txtContent = IOUtils.toString(stream, "UTF-8");
                            listOfEntriesAsTxt.put(fileNameWithOutExt, txtContent);
                            stream.close();
                        } catch (Exception e) {
                            AppLog.e(e.getMessage());
                        }
                    }
                }

                // DONE READING ZIP ENTRIES
                progress.setMax(listOfEntriesAsTxt.size());

                int txtC = listOfEntriesAsTxt.size();
                AppLog.e("Found-> " + txtC + " entries!");

                int entriesCounter = 0;

                //2 Iterate through each diarium entry and parse the data
                for (Map.Entry me : listOfEntriesAsTxt.entrySet()) {

                    String filename = (String) me.getKey();
                    String entryAsText = (String) me.getValue();

                    AppLog.i(entriesCounter + "  " + filename);
                    final int displayCount = ++entriesCounter;

                    activity.runOnUiThread(() -> progress.setProgress(displayCount));

                    ArrayList<String> tagUids = new ArrayList<>();
                    //--saving text and titles

                    String entry_text = "";
                    String entry_title = "";
                    long entry_date = 0L;
                    String location_uid = "";

                    List<String> dates = getTagValues(entryAsText, TAG_REGEX_DATES);
                    List<String> entries = getTagValues(entryAsText, TAG_REGEX_TEXT);

                    for (int j = 0; j < entries.size(); j++) {

                        String entryText = entries.get(j).trim();
                        String curDate = filename.trim() + "-" + dates.get(j);

                        DateTimeFormatter formatter = DateTimeFormat.forPattern(dateFormat).withZoneUTC();
                        DateTime dt = formatter.parseDateTime(curDate);
                        entry_date = dt.getMillis();

                        // entryText = StringEscapeUtils.escapeXml11(entryText);
                        if (entryText.startsWith("'")) {
                            entryText = entryText.substring(1, entryText.length() - 1);
                        }

                        String[] lines = entryText.split("\\n");
                        StringBuilder newText = new StringBuilder();

                        for (int x = 0; x < lines.length; x++) {
                            String s = lines[x];
                            if (!s.isEmpty()) {
                                newText.append(s.trim()).append(" ");

                                // Only append a new line if the next line is empty!!
                                int nextIndex = x + 1;
                                if (nextIndex < lines.length) {
                                    String nextVal = lines[nextIndex];
                                    if (nextVal.isEmpty())
                                        newText.append("\n");
                                }
                            } else {
                                newText.append("\n");
                            }

                        }

                        entry_text = newText.toString().replaceAll("''", "'");

                        String entry_uid = Static.generateRandomUid();
                        //--saving the entries
                        EntryInfo entry = new EntryInfo();
                        entry.setUid(entry_uid);
                        entry.setDate(entry_date);
                        entry.setText(entry_text);
                        entry.setTitle(entry_title);
                        entry.setFolderUid(tmpfolderUid);
                        entry.setLocationUid(location_uid);
                        entry.setTags("");
                        entry.setTzOffset(OFFSET);
                        // entry.setWeatherInfo(weather_temp, weather_icon, weather_description);
                        //  entry.setMood(entry_mood);
                        PersistanceHelper.saveEntry(entry);
                    }
                }

                // Done importing
                zipFile.close();

                activity.runOnUiThread(() -> {
                    Static.showToastLong("Imported " + listOfEntriesAsTxt.size() + " entries successfully!");
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

    private static final Pattern TAG_REGEX_TEXT = Pattern.compile("\\{text:(.+?)\\}", Pattern.DOTALL);
    private static final Pattern TAG_REGEX_DATES = Pattern.compile("(\\d+): (.+?)\\}", Pattern.DOTALL);

    private static List<String> getTagValues(final String str, Pattern pattern) {
        final List<String> tagValues = new ArrayList<String>();
        final Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            tagValues.add(matcher.group(1));
        }
        return tagValues;
    }


}