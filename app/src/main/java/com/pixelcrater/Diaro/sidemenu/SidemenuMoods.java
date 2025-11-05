package com.pixelcrater.Diaro.sidemenu;

import android.database.Cursor;
import android.graphics.Color;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.model.MoodInfo;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.sandstorm.moods.DefaultMoodAssets;

import org.apache.commons.lang3.StringUtils;

public class SidemenuMoods {

    private final int listItemTextColor;
    private final int listSelectedItemTextColor;
    private String selectedMoodUid;

    // OnOverflowItemClickListener
    private OnOverflowItemClickListener onOverflowItemClickListener;

    public SidemenuMoods() {
        listItemTextColor = MyThemesUtils.getListItemTextColor();
        listSelectedItemTextColor = MyThemesUtils.getSelectedListItemTextColor();
    }

    public void setOnOverflowItemClickListener(OnOverflowItemClickListener l) {
        onOverflowItemClickListener = l;
    }

    public void bindView(SidemenuCursorTreeAdapter.ChildViewHolder holder, Cursor cursor) {
//        AppLog.d("cursor.getPosition(): " + cursor.getPosition());

        final MoodInfo moodInfo = new MoodInfo(cursor);

        // Color
        holder.colorView.setVisibility(View.GONE);

        // Icon
        holder.iconView.setVisibility(View.VISIBLE);

        // Checkbox
        holder.checkboxView.setVisibility(View.GONE);

        // Title
        holder.titleTextView.setText(moodInfo.title);

        holder.overflowView.setVisibility(View.INVISIBLE);


        // Color
        int moodColor = Color.TRANSPARENT;
        try {
            moodColor = Color.parseColor(moodInfo.color);
        } catch (Exception ignored) {
        }

        try {
            DefaultMoodAssets moodAsset = DefaultMoodAssets.getByIconIdentifier(moodInfo.icon);
            holder.iconView.setImageDrawable(ContextCompat.getDrawable(holder.iconView.getContext(), moodAsset.getIconRes()));
        } catch (Exception ignored) {
        }

        // Count
        holder.countTextView.setText(String.valueOf(moodInfo.entriesCount));

        // Overflow
    /**    if (moodInfo.uid.equals("")) {
            holder.overflowView.setVisibility(View.INVISIBLE);
        } else {
            holder.overflowView.setVisibility(View.VISIBLE);
            holder.overflowView.setOnClickListener(v -> {
                // Send the onClick event back to the host activity
                if (onOverflowItemClickListener != null) {
                    onOverflowItemClickListener.onOverflowItemClick(v, moodInfo.uid);
                }
            });
        }**/

        // Highlight selected folder
        if (StringUtils.equals(selectedMoodUid, moodInfo.uid)) {
            holder.titleTextView.setTextColor(listSelectedItemTextColor);
            holder.countTextView.setTextColor(listSelectedItemTextColor);
        } else {
            holder.titleTextView.setTextColor(listItemTextColor);
            holder.countTextView.setTextColor(listItemTextColor);
        }
    }

    public String getSelectedMoodUid() {
        return selectedMoodUid;
    }

    public void setSelectedMoodUid(String selectedMoodUid) {
        // Clear mood if the same clicked again
        this.selectedMoodUid = StringUtils.equals(selectedMoodUid, this.selectedMoodUid) ? null : selectedMoodUid;
    }

    public void clearActiveMood() {
        setSelectedMoodUid(null);
        saveActiveMoodInPrefs();
    }

    public void saveActiveMoodInPrefs() {
        // Save active folder uid to prefs
        MyApp.getInstance().prefs.edit().putString(Prefs.PREF_ACTIVE_MOOD_UID, selectedMoodUid).apply();
    }

    public interface OnOverflowItemClickListener {
        void onOverflowItemClick(View v, String moodUid);
    }

}
