package com.pixelcrater.Diaro.generaldialogs;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.pixelcrater.Diaro.R;

import java.util.ArrayList;

public class OptionsAdapter extends ArrayAdapter<String> {

    private final ArrayList<String> mItemsSubtitlesArrayList;
    private ArrayList<String> mItemsFontsArrayList = new ArrayList<>();
    private int mSelectedIndex;
    private int mViewResourceId;
    private OptionsDialog mOptionsDialog;
    private LayoutInflater inflater;
    Context mContext;

    public OptionsAdapter(Context context, OptionsDialog optionsDialog, int viewResourceId, ArrayList<String> itemsTitlesArrayList, ArrayList<String> itemsSubtitlesArrayList, int selectedIndex) {
        super(context, viewResourceId, itemsTitlesArrayList);

        mViewResourceId = viewResourceId;
        mItemsSubtitlesArrayList = itemsSubtitlesArrayList;
        mItemsFontsArrayList = new ArrayList<>();
        mSelectedIndex = selectedIndex;
        mOptionsDialog = optionsDialog;
        inflater = ((Activity) context).getLayoutInflater();
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final String optionTitle = getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(mViewResourceId, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Title
        holder.titleTextView.setText(optionTitle);
        if (!mItemsFontsArrayList.isEmpty()) {
            String fontPath = mItemsFontsArrayList.get(position);

            holder.titleTextView.setTextSize(17);

            if (fontPath.isEmpty()) {
                holder.titleTextView.setTypeface(Typeface.DEFAULT);
            } else {
                holder.titleTextView.setTypeface(Typeface.createFromAsset(mContext.getAssets(), fontPath));
            }
        }

        // Subtitle
        if (mItemsSubtitlesArrayList == null) {
            holder.subtitleTextView.setVisibility(View.GONE);
        } else {
            holder.subtitleTextView.setVisibility(View.VISIBLE);
            holder.subtitleTextView.setText(mItemsSubtitlesArrayList.get(position));
        }

        // RadioButton
        if (mSelectedIndex == -1) {
            holder.radioButton.setVisibility(View.GONE);
        } else {
            holder.radioButton.setVisibility(View.VISIBLE);
            holder.radioButton.setChecked(mSelectedIndex == position);
            holder.radioButton.setTag(position);
            holder.radioButton.setOnClickListener(v -> {
                int pos = (Integer) v.getTag();
                if (mOptionsDialog.onDialogItemClickListener != null) {
                    mOptionsDialog.onDialogItemClickListener.onDialogItemClick(pos);
                }
                if (mOptionsDialog.isAdded()) {
                    mOptionsDialog.dismiss();
                }
            });
        }

        return convertView;
    }

    public void setItemsFonts(ArrayList<String> itemsFontsArrayList) {
        this.mItemsFontsArrayList = itemsFontsArrayList;
    }

    class ViewHolder {

        final TextView titleTextView;
        final TextView subtitleTextView;
        final RadioButton radioButton;

        ViewHolder(View view) {
            titleTextView = view.findViewById(R.id.title);
            subtitleTextView = view.findViewById(R.id.subtitle);
            radioButton = view.findViewById(R.id.radio_button);
        }
    }
}
