package com.pixelcrater.Diaro.sidemenu;

import android.database.Cursor;
import android.view.View;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.model.TagInfo;
import com.pixelcrater.Diaro.tags.TagsCursorAdapter;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class SidemenuTags {
    private final int listItemTextColor;
    private final int listSelectedItemTextColor;
    private ArrayList<String> selectedTagsUidsArrayList = new ArrayList<>();

    // OnOverflowItemClickListener
    private OnOverflowItemClickListener onOverflowItemClickListener;

    public SidemenuTags() {
        listItemTextColor = MyThemesUtils.getListItemTextColor();
        listSelectedItemTextColor = MyThemesUtils.getSelectedListItemTextColor();
    }

    public void setOnOverflowItemClickListener(OnOverflowItemClickListener l) {
        onOverflowItemClickListener = l;
    }

    public void bindView(SidemenuCursorTreeAdapter.ChildViewHolder holder, Cursor cursor) {
//        AppLog.d("cursor.getPosition(): " + cursor.getPosition());

        final TagInfo tagInfo = new TagInfo(cursor);

        // Color
        holder.colorView.setVisibility(View.GONE);

        // Icon
        holder.iconView.setVisibility(View.GONE);

        // Checkbox
        holder.checkboxView.setVisibility(View.VISIBLE);

        // Title
        holder.titleTextView.setText(tagInfo.title);

        // Count
        holder.countTextView.setText(String.valueOf(tagInfo.entriesCount));

        // Overflow
        if (tagInfo.uid.equals(TagsCursorAdapter.NO_TAGS_UID)) {
            holder.overflowView.setVisibility(View.INVISIBLE);
        } else {
            holder.overflowView.setVisibility(View.VISIBLE);
            holder.overflowView.setOnClickListener(v -> {
                // Send the onClick event back to the host activity
                if (onOverflowItemClickListener != null) {
                    onOverflowItemClickListener.onOverflowItemClick(v, tagInfo.uid);
                }
            });
        }

        // Check selected tags
        if (selectedTagsUidsArrayList.contains(tagInfo.uid)) {
            if (!holder.checkboxView.isChecked()) {
                holder.checkboxView.setChecked(true);
            }
            holder.titleTextView.setTextColor(listSelectedItemTextColor);
            holder.countTextView.setTextColor(listSelectedItemTextColor);
        } else {
            if (holder.checkboxView.isChecked()) {
                holder.checkboxView.setChecked(false);
            }
            holder.titleTextView.setTextColor(listItemTextColor);
            holder.countTextView.setTextColor(listItemTextColor);
        }
    }

    public void markUnmarkTag(String tagUid) {
//        AppLog.d("tagUid: " + tagUid + ", selectedTagsUidsArrayList: " + selectedTagsUidsArrayList);

        if (selectedTagsUidsArrayList.contains(tagUid)) {
            selectedTagsUidsArrayList.remove(tagUid);
        } else {
            selectedTagsUidsArrayList.add(tagUid);
        }
//		AppLog.d("selectedTagsArrayList: " + selectedTagsArrayList);
    }

    public ArrayList<String> getSelectedTagsUidsArrayList() {
        return selectedTagsUidsArrayList;
    }

    public String getSelectedTagsUids() {
        String selectedTags = "";

        int size = selectedTagsUidsArrayList.size();
        for (int i = 0; i < size; i++) {
            String tagUid = selectedTagsUidsArrayList.get(i);
            if (StringUtils.isNotEmpty(tagUid)) {
                selectedTags += tagUid;
            }
            if (StringUtils.isNotEmpty(selectedTags)) {
                selectedTags += ",";
            }
        }

        return selectedTags;
    }

    public void setSelectedTagsUids(String tagsUids) {
        selectedTagsUidsArrayList.clear();

        if (tagsUids != null && !tagsUids.equals("")) {
            ArrayList<String> splittedArrayList = new ArrayList<>(Arrays.asList(tagsUids.split(",")));

            for (int i = 0; i < splittedArrayList.size(); i++) {
                String tagUid = splittedArrayList.get(i);
                if (StringUtils.isNotEmpty(tagUid)) {
                    selectedTagsUidsArrayList.add(tagUid);
                }
            }
        }
    }

    public void clearActiveTags() {
        selectedTagsUidsArrayList.clear();
        saveActiveTagsInPrefs();
    }

    public void saveActiveTagsInPrefs() {
        // Save active tags to prefs
        MyApp.getInstance().prefs.edit().putString(Prefs.PREF_ACTIVE_TAGS, getSelectedTagsUids()).apply();
    }

    public void removeSelectedTag(String tagUid) {
        selectedTagsUidsArrayList.remove(tagUid);
    }

    public interface OnOverflowItemClickListener {
        void onOverflowItemClick(View v, String tagUid);
    }
}
