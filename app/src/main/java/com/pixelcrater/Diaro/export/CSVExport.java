package com.pixelcrater.Diaro.export;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.analytics.AnalyticsConstants;
import com.pixelcrater.Diaro.premium.PremiumActivity;
import com.pixelcrater.Diaro.settings.PreferencesHelper;
import com.pixelcrater.Diaro.utils.Static;

import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;

import static com.pixelcrater.Diaro.utils.Static.EXTRA_SKIP_SC;
import static com.pixelcrater.Diaro.utils.Static.REQUEST_GET_PRO;

public class CSVExport {

    private static final String NEW_LINE = "\n";
    private static final String COMMA = ",";
    public static String outputCSVString = "";

    public static void export(ArrayList<String> uids, Boolean withAttachments, Context ctx, Activity activity) {
        FirebaseAnalytics.getInstance(ctx).logEvent(AnalyticsConstants.EVENT_LOG_PRINT_CSV, new Bundle());

        if (Static.isProUser()) {

            // display the export preview
            LayoutInflater inflater = activity.getLayoutInflater();
            View previewLayout = inflater.inflate(R.layout.txt_preview, null);

            ProgressBar progressBar = previewLayout.findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.VISIBLE);

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setView(previewLayout);
            builder.setNegativeButton(android.R.string.cancel, (dialog, id) -> dialog.dismiss());

            builder.setPositiveButton(R.string.export, (dialog, id) -> {
                dialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/csv");
                intent.putExtra(Intent.EXTRA_TITLE, ExportUtils.generateCSVFileName());
                activity.startActivityForResult(intent, Static.REQUEST_CODE_CSV_SAVE);
            });

            AlertDialog alert = builder.create();
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(alert.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            alert.show();
            alert.getWindow().setAttributes(lp);
            ((AlertDialog) alert).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

            TextView textView = previewLayout.findViewById(R.id.txt_view_preview);

            AsyncTask.execute(() -> {
                outputCSVString = "";
                List<ExportEntry> exportEntries = ExportUtils.getExportEntries(uids, withAttachments, ctx);
                // convert the entries to text output format
                outputCSVString = formatCSVOutputString(exportEntries, ctx);
                activity.runOnUiThread(() -> {
                    ((AlertDialog) alert).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    textView.setText(outputCSVString);
                    progressBar.setVisibility(View.GONE);
                });
            });

        } else {
            Intent intent = new Intent(activity, PremiumActivity.class);
            intent.putExtra(EXTRA_SKIP_SC, true);
            activity.startActivityForResult(intent, REQUEST_GET_PRO);
        }
    }


    private static String formatCSVOutputString(@NonNull List<ExportEntry> entries, Context ctx) {

        StringBuilder textOutputStringBuilder = new StringBuilder();

        String header = "Date, Title, Text, Folder, Tag, Mood, Location, Weather";

        textOutputStringBuilder.append(header);
        textOutputStringBuilder.append(NEW_LINE);

        int counter = 0;
        StringBuilder output;

        for (ExportEntry entry : entries) {
            output = new StringBuilder();
            if (counter != 0)
                output.append(NEW_LINE);

            //Date
            String date = entry.day + " " + entry.month_name + " " + entry.year + ", " + entry.time;
            date = StringEscapeUtils.escapeCsv(date);
            output.append(date);
            output.append(COMMA);

            // Title
            if (entry.title != null && !entry.title.isEmpty()) {
                String title = StringEscapeUtils.escapeCsv(entry.title);
                output.append(title);
            } else {
                output.append("");
            }
            output.append(COMMA);

            // Text
            if (entry.text != null && !entry.text.isEmpty()) {
                String newEntry = entry.text.replaceAll("<br>", NEW_LINE + "");
                newEntry = StringEscapeUtils.escapeCsv(newEntry);
                output.append(newEntry);
            } else {
                output.append("");
            }
            output.append(COMMA);

            // Folder
            if (entry.folder_title != null && !entry.folder_title.isEmpty()) {
                output.append(entry.folder_title);
            } else {
                output.append("");
            }
            output.append(COMMA);

            // Tags
            if (entry.tags != null && !entry.tags.isEmpty()) {
                String tags = StringEscapeUtils.escapeCsv(entry.tags);
                output.append(tags);
            } else {
                output.append("");
            }
            output.append(COMMA);

            // Mood
            if (entry.hasMood  && PreferencesHelper.isMoodsEnabled() ) {
                output.append(entry.moodTitle);
            } else {
                output.append("");
            }
            output.append(COMMA);

            // Location
            if (entry.location != null && !entry.location.isEmpty()) {
                String location = StringEscapeUtils.escapeCsv(entry.location);
                output.append(location);
            } else {
                output.append("");
            }
            output.append(COMMA);

            // Weather
            if (entry.weather_temperature_display != null && !entry.weather_temperature_display.isEmpty()) {

                String weatherInfo = entry.weather_temperature_display + entry.unit_name + ", " + entry.weather_description_display;
                weatherInfo = StringEscapeUtils.escapeCsv(weatherInfo);
                output.append(weatherInfo);
            } else {
                output.append("");
            }

            counter++;
            textOutputStringBuilder.append(output);
        }

        return textOutputStringBuilder.toString();
    }

}