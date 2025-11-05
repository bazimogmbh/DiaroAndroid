package com.pixelcrater.Diaro.folders;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cursoradapter.widget.CursorAdapter;

import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.model.FolderInfo;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

import org.apache.commons.lang3.StringUtils;

public class FoldersCursorAdapter extends CursorAdapter {

    private final int listItemTextColor;
    private final int listSelectedItemTextColor;
    private final LayoutInflater inflater;
    private String selectedFolderUid;

    // OnOverflowItemClickListener
    private OnOverflowItemClickListener onOverflowItemClickListener;
    private boolean isNewEntry;

    public FoldersCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);

        listItemTextColor = MyThemesUtils.getListItemTextColor();
        listSelectedItemTextColor = MyThemesUtils.getSelectedListItemTextColor();
        inflater = ((Activity) context).getLayoutInflater();
    }

    public void setOnOverflowItemClickListener(OnOverflowItemClickListener l) {
        onOverflowItemClickListener = l;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
//        AppLog.d("cursor.getPosition(): " + cursor.getPosition());

        final View view = inflater.inflate(R.layout.folder_list_item, parent, false);
        view.setTag(new FolderViewHolder(view));
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
//        AppLog.d("cursor.getPosition(): " + cursor.getPosition());

        final FolderInfo folderInfo = new FolderInfo(cursor);
        final FolderViewHolder holder = (FolderViewHolder) view.getTag();

        // Color
        int folderColor = Color.TRANSPARENT;
        try {
            folderColor = Color.parseColor(folderInfo.color);
        } catch (Exception e) {
        }
        holder.colorView.setBackgroundColor(folderColor);

        // Title
        holder.titleTextView.setText(folderInfo.title); //  + " " + folderInfo.uid

        // Count
        int entriesCount = folderInfo.entriesCount;
//        AppLog.d("folderInfo.uid: " + folderInfo.uid + ", selectedFolderUid: " +
//                selectedFolderUid + ", entriesCount: " + entriesCount);

        // +1 for new entry
        if (isNewEntry && StringUtils.equals(folderInfo.uid, selectedFolderUid)) {
            entriesCount++;
        }
        holder.countTextView.setText(String.valueOf(entriesCount));

        // Overflow
        if (folderInfo.uid.equals("")) {
            holder.overflowView.setVisibility(View.INVISIBLE);
        } else {
            holder.overflowView.setVisibility(View.VISIBLE);
            holder.overflowView.setOnClickListener(v -> {
                // Send the onClick event back to the host activity/fragment
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

    public String getItemUid(int position) {
        Cursor cursor = (Cursor) getItem(position);
        return cursor.getString(cursor.getColumnIndex(Tables.KEY_UID));
    }

    public void setSelectedFolderUid(String selectedFolderUid) {
        this.selectedFolderUid = selectedFolderUid;
    }

    public void setIsNewEntry(boolean isNewEntry) {
        this.isNewEntry = isNewEntry;
    }

    public interface OnOverflowItemClickListener {
        void onOverflowItemClick(View v, String folderUID);
    }

    static class FolderViewHolder {
        final View colorView;
        final TextView titleTextView;
        final TextView countTextView;
        final ImageView overflowView;

        FolderViewHolder(View view) {
            colorView = view.findViewById(R.id.color);
            titleTextView = (TextView) view.findViewById(R.id.folder_title);
            countTextView = (TextView) view.findViewById(R.id.folder_count);
            overflowView = (ImageView) view.findViewById(R.id.overflow);
        }
    }
}
