package com.pixelcrater.Diaro.export;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.premium.PremiumActivity;
import com.pixelcrater.Diaro.settings.PreferencesHelper;
import com.pixelcrater.Diaro.utils.Static;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.pixelcrater.Diaro.utils.Static.EXTRA_SKIP_SC;
import static com.pixelcrater.Diaro.utils.Static.REQUEST_GET_PRO;

public class TxtExport {

    private static final String NEW_LINE = "\r\n";
    private static final String FORMAT_HEADER = "%d %s %d, %s %s";
    private static final String FORMAT_TITLE = NEW_LINE + "::: %s :::";
    private static final String FORMAT_TEXT = NEW_LINE + "%s";
    private static final String FORMAT_WEATHER = NEW_LINE + "Weather: %s %s";
    private static final String FORMAT_FOLDER = NEW_LINE + "Folder: %s";
    private static final String FORMAT_TAG = NEW_LINE + "Tag: %s";
    private static final String FORMAT_MOOD = NEW_LINE + "Mood: %s";
    private static final String FORMAT_LOCATION = NEW_LINE + "Location: %s";
    //private static final String ENTRY_SEPRATOR = "------------------------------------------------------------------------------------------------";

    private static final String ENTRY_SEPRATOR = "";

    public static String outputTextString = "";

    public static void export(ArrayList<String> uids, Boolean withAttachments, Context ctx, Activity activity) {

      //  activity.logAnalyticsEvent(AnalyticsConstants.EVENT_LOG_PRINT_TXT));

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
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TITLE, ExportUtils.generateTxtFileName());
                activity.startActivityForResult(intent, Static.REQUEST_CODE_TXT_SAVE);

            });

            AlertDialog alert = builder.create();
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(Objects.requireNonNull(alert.getWindow()).getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            alert.show();
            alert.getWindow().setAttributes(lp);
            ((AlertDialog) alert).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            TextView textView = previewLayout.findViewById(R.id.txt_view_preview);

            AsyncTask.execute(() -> {
                outputTextString = "";
                List<ExportEntry> exportEntries = ExportUtils.getExportEntries(uids, withAttachments, ctx);
                // convert the entries to text output format
                outputTextString = formatTextOutputString(exportEntries);
                activity.runOnUiThread(() -> {
                    ((AlertDialog) alert).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    textView.setText(outputTextString);
                    progressBar.setVisibility(View.GONE);
                });

            });

        } else {
            Intent intent = new Intent(activity, PremiumActivity.class);
            intent.putExtra(EXTRA_SKIP_SC, true);
            activity.startActivityForResult(intent, REQUEST_GET_PRO);
        }

    }


    private static String formatTextOutputString(@NonNull List<ExportEntry> entries) {

        StringBuilder textOutputStringBuilder = new StringBuilder();
        int counter = 0;

        StringBuilder output;
        for (ExportEntry entry : entries) {

            output = new StringBuilder();

            if (counter != 0)
                output.append(NEW_LINE);

            output.append(String.format(Locale.getDefault(), FORMAT_HEADER, entry.day, entry.month_name, entry.year, entry.day_of_week_full, entry.time));
            output.append(NEW_LINE);

            if (entry.title != null && !entry.title.isEmpty()) {
                output.append(String.format(FORMAT_TITLE, entry.title));
                output.append(NEW_LINE);
            }
            if (entry.text != null && !entry.text.isEmpty()) {
                String newEntry = entry.text.replaceAll("\\R", NEW_LINE + "");
                newEntry = newEntry.replaceAll("<br>", NEW_LINE + "");
                output.append(String.format(FORMAT_TEXT, newEntry));
                output.append(NEW_LINE);
            }
            if (entry.weather_temperature_display != null && !entry.weather_temperature_display.isEmpty()) {
                output.append(String.format(FORMAT_WEATHER, entry.weather_temperature_display, entry.unit_name));
                output.append(NEW_LINE);
            }
            if (entry.folder_title != null && !entry.folder_title.isEmpty()) {
                output.append(String.format(FORMAT_FOLDER, entry.folder_title));
                output.append(NEW_LINE);
            }
            if (entry.tags != null && !entry.tags.isEmpty()) {
                output.append(String.format(FORMAT_TAG, entry.tags));
                output.append(NEW_LINE);
            }
            if (entry.hasMood  && PreferencesHelper.isMoodsEnabled()) {
                output.append(String.format(FORMAT_MOOD, entry.moodTitle));
                output.append(NEW_LINE);
            }
            if (entry.location != null && !entry.location.isEmpty()) {
                output.append(String.format(FORMAT_LOCATION, entry.location));
                output.append(NEW_LINE);
            }

            if (entries.size() - 1 != counter) {
                output.append((NEW_LINE + ENTRY_SEPRATOR + NEW_LINE)); //to prevent dashes at the end of the file
            }
            counter++;
            textOutputStringBuilder.append(output);
        }

        return textOutputStringBuilder.toString();
    }

}