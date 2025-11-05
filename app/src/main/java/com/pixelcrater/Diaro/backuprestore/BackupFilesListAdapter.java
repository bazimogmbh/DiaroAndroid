package com.pixelcrater.Diaro.backuprestore;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;

import java.util.ArrayList;
import java.util.Date;

public class BackupFilesListAdapter extends ArrayAdapter<BackupFile> {

    private final Context mContext;
    private int mTabId;
    private ArrayList<BackupFile> mItems;
    private BackupFileViewHolder holder;
    private LayoutInflater inflater = null;

    // OnOverflowItemClickListener
    private OnOverflowItemClickListener onOverflowItemClickListener;

    public BackupFilesListAdapter(Context context, int tabId, ArrayList<BackupFile> items) {
        super(MyApp.getInstance(), R.layout.backup_file_list_item, items);

        mContext = context;
        mTabId = tabId;
        mItems = items;
        inflater = ((Activity) mContext).getLayoutInflater();
    }

    public void setOverflowItemClickListener(OnOverflowItemClickListener l) {
        onOverflowItemClickListener = l;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.backup_file_list_item, parent, false);

            holder = new BackupFileViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (BackupFileViewHolder) convertView.getTag();
        }

        if (mItems.size() > position) {
            BackupFile o = mItems.get(position);
            if (o != null) {
                // Set icon
                if (mTabId == BackupRestoreActivity.TAB_SD_CARD) {
                    holder.iconImageView.setImageResource(R.drawable.ic_database_grey600_24dp);
                } else if (mTabId == BackupRestoreActivity.TAB_DROPBOX) {
                    holder.iconImageView.setImageResource(R.drawable.ic_backup_file_dropbox_grey600_24dp);
                }

                holder.backupFilenameTextView.setText(o.filename);

                Date dt = new Date(o.lastModified);

               // String formattedDate =  dt.toString("dd MMMM yyyy " + MyDateTimeUtils.getTimeFormat());
                String formattedDate =  android.text.format.DateFormat.format("dd MMM yyyy", dt).toString();

                holder.backupInfoTextView.setText(String.format("%s | %s", formattedDate, o.fileSize));
            }
        }

        // Overflow
        holder.overflowImageView.setOnClickListener(v -> {
            // Send the onClick event back to the host activity
            if (onOverflowItemClickListener != null)
                onOverflowItemClickListener.onOverflowItemClick(v, position);
        });

        return convertView;
    }

    public interface OnOverflowItemClickListener {
        void onOverflowItemClick(View v, int position);
    }

    static class BackupFileViewHolder {
        final ImageView iconImageView;
        final TextView backupFilenameTextView;
        final TextView backupInfoTextView;
        final ImageView overflowImageView;

        BackupFileViewHolder(View view) {
            iconImageView = (ImageView) view.findViewById(R.id.backup_icon);
            backupFilenameTextView = (TextView) view.findViewById(R.id.backup_file_name);
            backupInfoTextView = (TextView) view.findViewById(R.id.backup_info);
            overflowImageView = (ImageView) view.findViewById(R.id.overflow);
        }
    }
}
