package com.pixelcrater.Diaro.imports;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;


import androidx.annotation.RequiresApi;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.model.EntryInfo;
import com.pixelcrater.Diaro.model.FolderInfo;
import com.pixelcrater.Diaro.model.PersistanceHelper;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.Static;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TxtImport {

    private static String FOLDER_TITLE = "Old Diary";
    private static String FOLDER_COLOR = "#4E4E4E";
    private static int DEFAULT_ZOOM = 11;
    private static String OFFSET = "+08:00";
    private static String dateFormat = "dd/MM/yyyy";

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public TxtImport(String filePath, Activity activity) {

        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);

        ProgressDialog progress = new ProgressDialog(activity);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(false);
        progress.setTitle(R.string.importing_data);
        progress.setMessage(fileName);
        progress.setCancelable(false);
        progress.show();

        AsyncTask.execute(() -> {
            FolderInfo folder = new FolderInfo(null, FOLDER_TITLE, FOLDER_COLOR);
            folder.setPattern("");
            String tmpfolderUid = PersistanceHelper.saveFolder(folder);

            try {


                File file = new File(filePath);
                FileInputStream fileInputStream = new FileInputStream(file);

                List<String> lines = IOUtils.readLines(fileInputStream, StandardCharsets.UTF_8);

                // Process read data
                progress.setMax(lines.size());
                int txtC = lines.size();
                AppLog.e("Found-> " + txtC + " entries!");

                int entriesCounter = 0;

                //2 Iterate through each diarium entry and parse the data

                String year = "";

                for (String line : lines) {
                    final int displayCount = ++entriesCounter;
                    activity.runOnUiThread(() -> progress.setProgress(displayCount));

                    if (!line.trim().isEmpty()) {

                        if (line.length() == 4) {
                            year = line;
                        } else {

                            String textWithYear = year + "/" + line;

                            String[] parts = line.split(" ");
                            String ddmm = parts[0];
                            String ddmmyy = parts[0] + "/" + year;
                            StringBuilder text = new StringBuilder();

                            for (int i = 0; i < parts.length; i++) {
                                if (i != 0) {
                                    text.append(parts[i]);
                                    text.append(" ");
                                }
                            }


                            String entry_title = "";
                            String entry_text = text.toString().trim();


                            String fullDate = ddmmyy;
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
                        }


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


}