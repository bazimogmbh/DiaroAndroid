package com.pixelcrater.Diaro.imports;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.Toast;

import com.opencsv.CSVReader;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class CSVImport {

    private static String FOLDER_TITLE = "CSV";
    private static String FOLDER_COLOR = "#4E4E4E";
    private static int DEFAULT_ZOOM = 11;
    private static String OFFSET = "+08:00";

  //  The T is just a literal to separate the date from the time, and the Z means "zero hour offset" also known as "Zulu time" (UTC). If your strings always have a "Z" you can use:
    private String dateFormat = "yyyyMMdd'T'HHmmssSSS'Z'";

    // Remente
    // createdAt, notes, taskTitle, goalTitle, Tag

    public CSVImport(String filePath, Activity activity) {

        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

        ProgressDialog progress = new ProgressDialog(activity);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(false);
        progress.setTitle(R.string.importing_data);
        progress.setMessage(fileName);
        progress.setCancelable(false);
        progress.show();

        AsyncTask.execute(() -> {
            // Put all entries under "FOLDER_TITLE" folder"
            FolderInfo folder = new FolderInfo(null, FOLDER_TITLE, FOLDER_COLOR);
            folder.setPattern("");
            String tmpfolderUid = PersistanceHelper.saveFolder(folder);

            try {

                File file = new File(filePath);
                FileInputStream fileInputStream = new FileInputStream(file);

                CSVReader reader = new CSVReader(new InputStreamReader(fileInputStream));
                List<String[]> list = reader.readAll();
                reader.close();
                // DONE READING ZIP ENTRIES
                // Process read data
                progress.setMax(list.size());
                int txtC = list.size();
                AppLog.e("Found-> " + txtC + " entries!");

                int entriesCounter = 0;

                //2 Iterate through each diarium entry and parse the data

                for (int i = 0; i < list.size(); i++) {

                    final int displayCount = ++entriesCounter;
                    activity.runOnUiThread(() -> progress.setProgress(displayCount));

                    try {

                        String[] row = list.get(i);
                        String entry_title = row[2];
                        String entry_text = row[1];

                        String fullDate = row[0];
                        DateTimeFormatter formatter = DateTimeFormat.forPattern(dateFormat).withZoneUTC();
                        DateTime dt = formatter.parseDateTime(fullDate);

                        String entry_uid = Static.generateRandomUid();
                        long entry_date = dt.getMillis();
                        //  OFFSET = MyDateTimeUtils.getCurrentTimeZoneOffset(entry_date);

                        String location_uid = "";


                        //--saving the entries
                        EntryInfo entry = new EntryInfo();
                        entry.setUid(entry_uid);
                        entry.setDate(entry_date);
                        entry.setText(entry_text);
                        entry.setTitle(entry_title);
                        entry.setFolderUid(tmpfolderUid); // journey
                        entry.setLocationUid(location_uid);

                        entry.setTzOffset(OFFSET);
                        // entry.setWeatherInfo(weather_temp, weather_icon, weather_description);

                        PersistanceHelper.saveEntry(entry);


                    } catch (Exception e) {
                        // Oops
                    }
                }


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

    public List<String[]> readAll(String filePath) throws Exception {
        CSVReader reader = new CSVReader(new FileReader(filePath));
        List<String[]> list = new ArrayList<>();
        list = reader.readAll();
        reader.close();
        reader.close();
        return list;
    }


}