package com.pixelcrater.Diaro.tags;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cursoradapter.widget.CursorAdapter;

import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.model.TagInfo;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class TagsCursorAdapter extends CursorAdapter {

    public static final String NO_TAGS_UID = "no_tags";

    private final LayoutInflater inflater;
    private final int listItemTextColor;
    private final int listSelectedItemTextColor;
    private TagsSelectDialog mTagsSelectDialog;
    private ArrayList<String> selectedTagsUidsArrayList = new ArrayList<>();

    // OnOverflowItemClickListener
    private OnOverflowItemClickListener onOverflowItemClickListener;

    public TagsCursorAdapter(Context context, Cursor cursor, int flags, TagsSelectDialog tagsSelectDialog) {
        super(context, cursor, flags);

        mTagsSelectDialog = tagsSelectDialog;
        inflater = ((Activity) context).getLayoutInflater();
        listItemTextColor = MyThemesUtils.getListItemTextColor();
        listSelectedItemTextColor = MyThemesUtils.getSelectedListItemTextColor();
    }

    public void setOverflowItemClickListener(OnOverflowItemClickListener l) {
        onOverflowItemClickListener = l;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
//        AppLog.d("cursor.getPosition(): " + cursor.getPosition());

        final View view = inflater.inflate(R.layout.checkbox_list_item, parent, false);
        view.setTag(new TagViewHolder(view));
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
//        AppLog.d("cursor.getPosition(): " + cursor.getPosition());

        final TagInfo tagInfo = new TagInfo(cursor);
        final TagViewHolder holder = (TagViewHolder) view.getTag();

        // Title
        holder.titleTextView.setText(tagInfo.title);

        // Count
        int entriesCount = tagInfo.entriesCount;
        if (mTagsSelectDialog != null) {
            // Increase or decrease count by 1
            if (selectedTagsUidsArrayList.contains(tagInfo.uid) && !mTagsSelectDialog.entryTagsUids.contains(tagInfo.uid)) {
                entriesCount++;
            } else if (mTagsSelectDialog.entryTagsUids.contains(tagInfo.uid) && !selectedTagsUidsArrayList.contains(tagInfo.uid)) {
                entriesCount--;
            }
        }
        holder.countTextView.setText(String.valueOf(entriesCount));

        // Overflow
        holder.overflowView.setVisibility(View.VISIBLE);
        holder.overflowView.setOnClickListener(v -> {
            // Send the onClick event back to the host activity
            if (onOverflowItemClickListener != null) {
                onOverflowItemClickListener.onOverflowItemClick(v, tagInfo.uid);
            }
        });

        //		AppLog.d("selectedTagsArrayList: " + selectedTagsArrayList);

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

    public String getItemUid(int position) {
        Cursor cursor = (Cursor) getItem(position);
        return cursor.getString(cursor.getColumnIndex(Tables.KEY_UID));
    }

    public String getSelectedTagsUids() {
        StringBuilder selectedTags = new StringBuilder();

        int size = selectedTagsUidsArrayList.size();
        for (int i = 0; i < size; i++) {
            String tagUid = selectedTagsUidsArrayList.get(i);
            if (!tagUid.equals("")) {
                selectedTags.append(",").append(tagUid);
            }
        }
        if (!selectedTags.toString().equals("")) {
            selectedTags.append(",");
        }

        return selectedTags.toString();
    }

    public void setSelectedTagsUids(String tagsUids) {
        selectedTagsUidsArrayList.clear();

        if (StringUtils.isNotEmpty(tagsUids)) {
            ArrayList<String> splittedArrayList = new ArrayList<>(Arrays.asList(tagsUids.split(",")));

            for (int i = 0; i < splittedArrayList.size(); i++) {
                String tag = splittedArrayList.get(i);
                if (StringUtils.isNotEmpty(tag)) {
                    selectedTagsUidsArrayList.add(tag);
                }
            }
        }

        notifyDataSetChanged();
    }

    public void markUnmarkTag(View view, int position) {
//		AppLog.d("selectedTagsArrayList: " + selectedTagsArrayList);

        CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkbox);
//		AppLog.d("checkBox.isChecked(): " + checkBox.isChecked());

        String tagUid = getItemUid(position);

        if (checkbox.isChecked()) {
            if (selectedTagsUidsArrayList.contains(tagUid)) {
                selectedTagsUidsArrayList.remove(tagUid);
            }
        } else {
            if (!selectedTagsUidsArrayList.contains(tagUid)) {
                selectedTagsUidsArrayList.add(tagUid);
            }
        }
    }

    public interface OnOverflowItemClickListener {
        void onOverflowItemClick(View v, String folderUID);
    }

    static class TagViewHolder {
        final CheckBox checkboxView;
        final TextView titleTextView;
        final TextView countTextView;
        final ImageView overflowView;

        TagViewHolder(View view) {
            checkboxView =  view.findViewById(R.id.checkbox);
            titleTextView =  view.findViewById(R.id.title);
            countTextView = view.findViewById(R.id.count);
            overflowView =  view.findViewById(R.id.overflow);
        }
    }
}
