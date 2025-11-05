package com.pixelcrater.Diaro.sidemenu;

import android.database.Cursor;
import android.graphics.Color;
import android.view.View;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.model.FolderInfo;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

import org.apache.commons.lang3.StringUtils;

public class SidemenuFolders {

    private final int listItemTextColor;
    private final int listSelectedItemTextColor;
    private String selectedFolderUid;

    // OnOverflowItemClickListener
    private OnOverflowItemClickListener onOverflowItemClickListener;

    public SidemenuFolders() {
        listItemTextColor = MyThemesUtils.getListItemTextColor();
        listSelectedItemTextColor = MyThemesUtils.getSelectedListItemTextColor();
    }

    public void setOnOverflowItemClickListener(OnOverflowItemClickListener l) {
        onOverflowItemClickListener = l;
    }

    public void bindView(SidemenuCursorTreeAdapter.ChildViewHolder holder, Cursor cursor) {
//        AppLog.d("cursor.getPosition(): " + cursor.getPosition());

        final FolderInfo folderInfo = new FolderInfo(cursor);

        // Color
        int folderColor = Color.TRANSPARENT;
        try {
            folderColor = Color.parseColor(folderInfo.color);
        } catch (Exception e) {
        }
        holder.colorView.setBackgroundColor(folderColor);
        holder.colorView.setVisibility(View.VISIBLE);

        // Icon
        holder.iconView.setVisibility(View.GONE);

        // Checkbox
        holder.checkboxView.setVisibility(View.GONE);

        // Title
        holder.titleTextView.setText(folderInfo.title);

        // Count
        holder.countTextView.setText(String.valueOf(folderInfo.entriesCount));

        // Overflow
        if (folderInfo.uid.equals("")) {
            holder.overflowView.setVisibility(View.INVISIBLE);
        } else {
            holder.overflowView.setVisibility(View.VISIBLE);
            holder.overflowView.setOnClickListener(v -> {
                // Send the onClick event back to the host activity
                if (onOverflowItemClickListener != null) {
                    onOverflowItemClickListener.onOverflowItemClick(v, folderInfo.uid);
                }
            });
        }

        // Highlight selected folder
        if (StringUtils.equals(selectedFolderUid, folderInfo.uid)) {
            holder.titleTextView.setTextColor(listSelectedItemTextColor);
            holder.countTextView.setTextColor(listSelectedItemTextColor);
        } else {
            holder.titleTextView.setTextColor(listItemTextColor);
            holder.countTextView.setTextColor(listItemTextColor);
        }
    }

    public String getSelectedFolderUid() {
        return selectedFolderUid;
    }

    public void setSelectedFolderUid(String selectedFolderUid) {
        // Clear folder if the same clicked again
        this.selectedFolderUid = StringUtils.equals(selectedFolderUid, this.selectedFolderUid) ? null : selectedFolderUid;
    }

    public void clearActiveFolder() {
        setSelectedFolderUid(null);
        saveActiveFolderInPrefs();
    }

    public void saveActiveFolderInPrefs() {
        // Save active folder uid to prefs
        MyApp.getInstance().prefs.edit().putString(Prefs.PREF_ACTIVE_FOLDER_UID, selectedFolderUid).apply();
    }

    public interface OnOverflowItemClickListener {
        void onOverflowItemClick(View v, String folderUid);
    }
}
