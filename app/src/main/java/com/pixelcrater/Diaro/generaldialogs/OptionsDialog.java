package com.pixelcrater.Diaro.generaldialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.layouts.QustomDialogBuilder;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

import java.util.ArrayList;

public class OptionsDialog extends DialogFragment {

    private static final String CUSTOM_STRING_STATE_KEY = "CUSTOM_STRING_STATE_KEY";
    private static final String TITLE_STATE_KEY = "TITLE_STATE_KEY";
    private static final String ICON_STATE_KEY = "ICON_STATE_KEY";
    private static final String TIP_STATE_KEY = "TIP_STATE_KEY";
    private static final String ITEMS_TITLES_STATE_KEY = "ITEMS_TITLES_STATE_KEY";
    private static final String ITEMS_SUBTITLES_STATE_KEY = "ITEMS_SUBTITLES_STATE_KEY";
    private static final String SELECTED_INDEX_STATE_KEY = "SELECTED_INDEX_STATE_KEY";

    // Item click listener
    public OnDialogItemClickListener onDialogItemClickListener;
    private AlertDialog dialog;
    private String customString;
    private String title;
    private int icon = -1;
    private String tip;
    private ArrayList<String> itemsTitlesArrayList;
    private ArrayList<String> itemsSubtitlesArrayList;
    private ArrayList<String> itemsFontsArrayList  = new ArrayList<>();
    private int selectedIndex = -1;

    public void setDialogItemClickListener(OnDialogItemClickListener l) {
        onDialogItemClickListener = l;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            customString = savedInstanceState.getString(CUSTOM_STRING_STATE_KEY);
            title = savedInstanceState.getString(TITLE_STATE_KEY);
            icon = savedInstanceState.getInt(ICON_STATE_KEY);
            tip = savedInstanceState.getString(TIP_STATE_KEY);
            itemsTitlesArrayList = savedInstanceState.getStringArrayList(ITEMS_TITLES_STATE_KEY);
            itemsSubtitlesArrayList = savedInstanceState.getStringArrayList(ITEMS_SUBTITLES_STATE_KEY);
            selectedIndex = savedInstanceState.getInt(SELECTED_INDEX_STATE_KEY);
        }

        // Use the Builder class for convenient dialog construction
        QustomDialogBuilder builder = new QustomDialogBuilder(getActivity());

        // Color
        builder.setHeaderBackgroundColor(MyThemesUtils.getPrimaryColorCode());

        // Title
        if (title != null) {
            builder.setTitle(title);
        }

        // Icon
        if (icon != -1) {
            builder.setIcon(icon);
        }

        // Set custom view
        builder.setCustomView(R.layout.options_dialog);
        View customView = builder.getCustomView();

        // Show tip
        if (tip != null) {
            TextView tipView =  customView.findViewById(R.id.tip);
            tipView.setVisibility(View.VISIBLE);
            tipView.setText(tip);
        }

        // Check titles and subtitles ArrayLists
        if (itemsSubtitlesArrayList != null && itemsTitlesArrayList.size() != itemsSubtitlesArrayList.size()) {
            AppLog.e("Titles and subtitles ArrayLists sizes do no match");
        }

        // Options ListView
        ListView optionsListView = (ListView) customView.findViewById(R.id.items_listview);
        OptionsAdapter optionsAdapter = new OptionsAdapter(getActivity(), OptionsDialog.this, R.layout.options_list_item, itemsTitlesArrayList, itemsSubtitlesArrayList, selectedIndex);
        optionsAdapter.setItemsFonts(itemsFontsArrayList);
        optionsListView.setAdapter(optionsAdapter);

        // OnItemClickListener
        optionsListView.setOnItemClickListener((a, view, position, id) -> {
            AppLog.d("position: " + position);

            if (onDialogItemClickListener != null) {
                onDialogItemClickListener.onDialogItemClick(position);
            }
            dialog.dismiss();
        });

        if (selectedIndex != -1) {
            builder.setNegativeButton(android.R.string.cancel, null);
        }

        dialog = builder.create();
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Prevent dismiss on touch outside
        getDialog().setCanceledOnTouchOutside(false);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public void setItemsTitles(ArrayList<String> itemsTitlesArrayList) {
        this.itemsTitlesArrayList = itemsTitlesArrayList;
    }

    public void setItemsSubtitles(ArrayList<String> itemsSubtitlesArrayList) {
        this.itemsSubtitlesArrayList = itemsSubtitlesArrayList;
    }

    public void setItemsFontPaths(ArrayList<String> itemsFontsArrayList) {
        this.itemsFontsArrayList = itemsFontsArrayList;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(CUSTOM_STRING_STATE_KEY, customString);
        outState.putString(TITLE_STATE_KEY, title);
        outState.putInt(ICON_STATE_KEY, icon);
        outState.putString(TIP_STATE_KEY, tip);
        outState.putStringArrayList(ITEMS_TITLES_STATE_KEY, itemsTitlesArrayList);
        outState.putStringArrayList(ITEMS_SUBTITLES_STATE_KEY, itemsSubtitlesArrayList);
        outState.putInt(SELECTED_INDEX_STATE_KEY, selectedIndex);
    }

    public interface OnDialogItemClickListener {
        void onDialogItemClick(int which);
    }
}
