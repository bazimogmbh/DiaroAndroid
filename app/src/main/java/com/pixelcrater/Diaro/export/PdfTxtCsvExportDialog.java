package com.pixelcrater.Diaro.export;

import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.util.Pair;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.entries.EntriesStatic;
import com.pixelcrater.Diaro.layouts.QustomDialogBuilder;
import com.pixelcrater.Diaro.settings.PreferencesHelper;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.tags.TagsStatic;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PdfTxtCsvExportDialog extends DialogFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private RadioGroup radioGroup;
    private RadioButton radio_all_entries, radio_filtered, radio_date_range;
    private Spinner spn_photos, spn_exportType, spn_layouts;
    private CheckBox cbx_includeSummary, cbx_includeLogo;
    int exportSelectionIndex = 0;

    private ArrayList<String> entriesUidsArrayList = new ArrayList<>();
    private ExportOptions options = null;
    private ExportSummary summary = null;

    private int entriesFilterIndex = 0;
    private int exportType = 0;

    private long startDate = 0;
    private long endDate = 0;

    public PdfTxtCsvExportDialog(){
    }

    public PdfTxtCsvExportDialog(int exportType) {
        this.exportType = exportType;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String[] formats = {"PDF", "TXT", "CSV"};
        String[] photos = {getString(R.string.no_photos), getString(R.string.small), getString(R.string.medium), getString(R.string.large)}; // none , 190px, 300px, 768px
        String[] layouts = {getString(R.string.export_compact), getString(R.string.export_normal)};

        QustomDialogBuilder builder = new QustomDialogBuilder(getActivity());
        builder.setHeaderBackgroundColor(MyThemesUtils.getPrimaryColorCode());
        builder.setTitle(getString(R.string.export_to_pdf));

        // Set custom view
        builder.setCustomView(R.layout.export_pdf_dialog);
        View customView = builder.getCustomView();

        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(R.string.export, null);

        spn_photos = customView.findViewById(R.id.spn_photos);
        spn_exportType = customView.findViewById(R.id.spn_exportType);
        spn_layouts = customView.findViewById(R.id.spn_layout);

        radio_all_entries = customView.findViewById(R.id.radio_all_entries);
        radio_filtered = customView.findViewById(R.id.radio_filtered);
        radio_date_range = customView.findViewById(R.id.radio_date_range);
        radio_date_range.setOnClickListener(this);

        cbx_includeSummary = customView.findViewById(R.id.cbx_includeSummary);
        cbx_includeSummary.setOnClickListener(this);
        cbx_includeSummary.setChecked(PreferencesHelper.getIncludeSummary());

        cbx_includeLogo = customView.findViewById(R.id.cbx_includeLogo);
        cbx_includeLogo.setOnClickListener(this);
        cbx_includeLogo.setChecked(PreferencesHelper.getIncludeLogo());

        radioGroup = customView.findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radio_all_entries:
                    entriesFilterIndex = 0;
                    break;

                case R.id.radio_filtered:
                    entriesFilterIndex = 1;
                    break;

                case R.id.radio_date_range:
                    entriesFilterIndex = 2;
                    break;
            }

        });

        // Photos selection
        ArrayAdapter<String> photosArray = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_dropdown_item, photos);
        spn_photos.setAdapter(photosArray);
        spn_photos.setSelection(MyApp.getInstance().prefs.getInt(Prefs.PREF_EXPORT_PHOTOS_OPTION, 1)); // small photos ( not no photos)
        spn_photos.setOnItemSelectedListener(this);

        // Format Selection
        ArrayAdapter<String> formatArray = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_dropdown_item, formats);
        spn_exportType.setAdapter(formatArray);
        spn_exportType.setSelection(exportType);
        spn_exportType.setOnItemSelectedListener(this);

        // Layout selection
        ArrayAdapter<String> layoutArray = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_dropdown_item, layouts);
        spn_layouts.setAdapter(layoutArray);
        spn_layouts.setSelection(MyApp.getInstance().prefs.getInt(Prefs.PREF_EXPORT_LAYOUT_OPTION, 0));
        spn_layouts.setOnItemSelectedListener(this);

        String activeFolderUid = MyApp.getInstance().prefs.getString(Prefs.PREF_ACTIVE_FOLDER_UID, "");
        String activeTags = MyApp.getInstance().prefs.getString(Prefs.PREF_ACTIVE_TAGS, "");
        String activeLocations = MyApp.getInstance().prefs.getString(Prefs.PREF_ACTIVE_LOCATIONS, "");
        String activeSearchText = MyApp.getInstance().prefs.getString(Prefs.PREF_ACTIVE_SEARCH_TEXT, "");

        if (activeFolderUid.isEmpty() && activeTags.isEmpty() && activeLocations.isEmpty() && activeSearchText.isEmpty()) {
            radio_all_entries.setChecked(true);
        } else {
            radio_filtered.setChecked(true);
        }

        // Create the AlertDialog object and return it
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {
            Button positiveButton = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> export());
        });

        return dialog;
    }

    private void export() {

        entriesUidsArrayList.clear();

        String activeFolderUid = MyApp.getInstance().prefs.getString(Prefs.PREF_ACTIVE_FOLDER_UID, "");
        String activeTags = MyApp.getInstance().prefs.getString(Prefs.PREF_ACTIVE_TAGS, "");
        String activeLocations = MyApp.getInstance().prefs.getString(Prefs.PREF_ACTIVE_LOCATIONS, "");
        String activeSearchText = MyApp.getInstance().prefs.getString(Prefs.PREF_ACTIVE_SEARCH_TEXT, "");

        if (entriesFilterIndex == 0) {
            Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getAllEntriesCursorUidsOnly();
            int entryUidColumnIndex = cursor.getColumnIndex(Tables.KEY_UID);
            while (cursor.moveToNext()) {
                entriesUidsArrayList.add(cursor.getString(entryUidColumnIndex));
            }
            cursor.close();

        } else if (entriesFilterIndex == 1) {
            Pair<String, String[]> pair = EntriesStatic.getEntriesAndSqlByActiveFilters(null);
            Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getEntriesCursorUidsOnly(pair.first, pair.second);
            int entryUidColumnIndex = cursor.getColumnIndex(Tables.KEY_UID);
            while (cursor.moveToNext()) {
                entriesUidsArrayList.add(cursor.getString(entryUidColumnIndex));
            }
            cursor.close();
        } else if (entriesFilterIndex == 2) {

            if (startDate != 0 && endDate != 0) {
                Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getEntriesCursorUidsOnlyByDateRange(startDate, endDate);
                int entryUidColumnIndex = cursor.getColumnIndex(Tables.KEY_UID);
                while (cursor.moveToNext()) {
                    entriesUidsArrayList.add(cursor.getString(entryUidColumnIndex));
                }
                cursor.close();
            } else {
                selectDateRange();
                return;
            }

        }

        switch (exportSelectionIndex) {
            case 0:
                boolean includeSummary = PreferencesHelper.getIncludeSummary();

                AppLog.e("includeSummary-> " + includeSummary);
                if (includeSummary) {
                    String pattern =  "dd MMMM yyyy";

                    SimpleDateFormat s = new SimpleDateFormat(pattern);
                    String date = s.format(new Date());

                    String folderName = null;
                    Cursor folderCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSingleFolderCursorByUid(activeFolderUid);
                    if (folderCursor.getCount() != 0) {
                        folderName = folderCursor.getString(folderCursor.getColumnIndex(Tables.KEY_FOLDER_TITLE));
                    }
                    folderCursor.close();

                    String tags = "";

                    // TODO: tags & locations

                    ArrayList<String> activeTagsUidsArrayList = TagsStatic.getActiveTagsUidsArrayList();

                    if(activeSearchText.equals(""))
                        activeSearchText = null;

                    if (entriesFilterIndex == 0)
                        summary = new ExportSummary(date, null, null, null, null);

                    else if (entriesFilterIndex == 1)
                        summary = new ExportSummary(date, folderName, "", activeSearchText, "" );
                } else{
                    summary = null;
                }

                String layout = PreferencesHelper.getExportLayout();
                String photoHeight = PreferencesHelper.getExportPhotoHeight();
                options = new ExportOptions(layout, photoHeight);

                PdfExport.export(entriesUidsArrayList, options, summary, getActivity());
                break;

            case 1:
                TxtExport.export(entriesUidsArrayList, false, getContext(), getActivity());
                break;
            case 2:
                CSVExport.export(entriesUidsArrayList, false, getContext(), getActivity());
                break;
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Prevent dismiss on touch outside
        getDialog().setCanceledOnTouchOutside(false);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

        if (parent.getId() == R.id.spn_photos) {
            PreferencesHelper.setExportPhotoPref(pos);

        } else if (parent.getId() == R.id.spn_exportType) {
            switch (pos) {
                case 0:
                    exportSelectionIndex = 0;
                    spn_photos.setEnabled(true);
                    break;
                case 1:
                    exportSelectionIndex = 1;
                    spn_photos.setEnabled(false);
                    break;
                case 2:
                    exportSelectionIndex = 2;
                    spn_photos.setEnabled(false);
                    break;
            }
        } else if (parent.getId() == R.id.spn_layout) {
            PreferencesHelper.setExportLayoutPref(pos);
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cbx_includeSummary:
                PreferencesHelper.setIncludeSummary(cbx_includeSummary.isChecked());
                break;

            case R.id.cbx_includeLogo:
                PreferencesHelper.setIncludeLogo(cbx_includeLogo.isChecked());
                break;

            case R.id.radio_date_range:
                selectDateRange();
                break;
        }
    }

    private void selectDateRange() {

        // long today = MaterialDatePicker.todayInUtcMilliseconds();
        int dialogTheme = MyThemesUtils.resolveOrThrow(getContext(), R.attr.materialCalendarTheme);

        MaterialDatePicker.Builder<Pair<Long, Long>> builder = MaterialDatePicker.Builder.dateRangePicker();
        //  builder.setSelection(today);
        builder.setTheme(dialogTheme);
        MaterialDatePicker materialDatePicker = builder.build();
        materialDatePicker.show(getChildFragmentManager(), materialDatePicker.toString());

        materialDatePicker.addOnPositiveButtonClickListener(selection -> {
            Pair<Long, Long> aPair = (Pair) selection;

            startDate = aPair.first;

            // add 1 day to end date.
            DateTime dateTime = new DateTime(aPair.second);
            dateTime = dateTime.plusDays(1);

            endDate = dateTime.getMillis();

            radio_date_range.setText(String.format("%s ( %s )", getString(R.string.date_range), materialDatePicker.getHeaderText()));

            AppLog.e(startDate + " " + endDate);

            Tables.getLocalTime(startDate);
        });

        materialDatePicker.addOnCancelListener(dialogInterface -> {
            radio_date_range.setText(R.string.date_range);
            radioGroup.check(R.id.radio_all_entries);
        });

    }

}
