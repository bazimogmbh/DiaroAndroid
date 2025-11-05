package com.pixelcrater.Diaro.onthisday;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.config.FontsConfig;
import com.pixelcrater.Diaro.model.EntryInfo;
import com.pixelcrater.Diaro.settings.PreferencesHelper;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.Static;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OnThisDayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<EntryInfo> mEntries = new ArrayList<>();
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;

    private Typeface mFaFont, mEntryFont;

    private final RequestManager glide;

    public OnThisDayAdapter(RequestManager glide, Context context, List<EntryInfo> items) {
        this.glide = glide;
        this.mEntries = items;
        mContext = context;

        mFaFont = Typeface.createFromAsset(context.getAssets(), FontsConfig.FONT_PATH_FONTAWESOMEFONT);
        mEntryFont = PreferencesHelper.getPrefTypeFace(context);
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_on_this_day, parent, false);
        v.setBackgroundResource(MyThemesUtils.getEntryListItemColorResId());
        vh = new OriginalViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holder, final int position) {
        EntryInfo entryInfo = mEntries.get(position);

        OriginalViewHolder view = (OriginalViewHolder) holder;
        if (StringUtils.isNotEmpty(entryInfo.folderColor)) {
            view.folderColor.setVisibility(View.VISIBLE);
            view.folderTitle.setVisibility(View.VISIBLE);

            int folderColor = Color.parseColor(entryInfo.folderColor);
            view.folderColor.setColorFilter(folderColor, PorterDuff.Mode.SRC_ATOP);
            view.folderTitle.setText(entryInfo.folderTitle);

        } else {
            view.folderColor.setVisibility(View.GONE);
            view.folderTitle.setVisibility(View.GONE);
        }
        if (StringUtils.isNotEmpty(entryInfo.title)) {
            view.title.setText(entryInfo.title);
            view.title.setVisibility(View.VISIBLE);
        } else {
            view.title.setVisibility(View.GONE);
        }

        view.title.setText(entryInfo.title);
        view.title.setTypeface(mEntryFont, Typeface.BOLD);
        String newfullText = entryInfo.text.trim().replaceAll("[\\t\\n\\r]", " ");
        view.subtitle.setText(newfullText);
        view.subtitle.setTypeface(mEntryFont);

        DateTime dt = entryInfo.getLocalDt();
        String dateText = entryInfo.getDayOfWeekString() + ", " + android.text.format.DateFormat.format("dd MMM yy", new Date( entryInfo.unixEpochMillis)) + ", " + entryInfo.getDateHM();
        view.date.setText(dateText);

        DateTime entryDate = new DateTime(entryInfo.getLocalDt());
        DateTime todayDate = new DateTime();
        int yearsDiff = todayDate.getYear() - entryDate.getYear();

        String prefix = "";
        String yearsText = "";

        if (yearsDiff == 0) {
            prefix = mContext.getString(R.string.today);
            yearsText = prefix;
        }

        if (yearsDiff > 0) {
            yearsDiff = Math.abs(yearsDiff);
            yearsText = mContext.getString(R.string.fa_history) + " " + mContext.getResources().getQuantityString(R.plurals.years_ago, yearsDiff, yearsDiff);
        }

        if (yearsDiff < 0) {
            yearsDiff = Math.abs(yearsDiff);
            yearsText = mContext.getString(R.string.fa_history) + " " + mContext.getResources().getQuantityString(R.plurals.years_later, yearsDiff, yearsDiff);
        }

        view.yearsAgoInfo.setTypeface(mFaFont);
        view.yearsAgoInfo.setText(yearsText);

        if (entryInfo.photoCount == 0) {
            view.image.setVisibility(View.GONE);
        } else {
            view.image.setVisibility(View.VISIBLE);
            File photoFile = new File(entryInfo.getFirstPhotoPath());
            glide.load(Uri.fromFile(photoFile)).signature(Static.getGlideSignature(photoFile)).centerCrop().error(R.drawable.ic_photo_red_24dp).into(view.image);
        }

        view.cardview.setOnClickListener(view1 -> {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(view1, mEntries.get(position), position);
            }
        });
    }


    @Override
    public int getItemCount() {
        return mEntries.size();
    }


    static class OriginalViewHolder extends RecyclerView.ViewHolder {

        public ImageView image;
        public ImageView folderColor;
        public TextView folderTitle;
        public TextView title;
        public TextView subtitle;
        public TextView date;
        public TextView yearsAgoInfo;
        public View cardview;

        public OriginalViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.image);
            folderColor = v.findViewById(R.id.folderColor);
            folderTitle = v.findViewById(R.id.folderTitle);
            title = v.findViewById(R.id.title);
            subtitle = v.findViewById(R.id.subtitle);
            date = v.findViewById(R.id.date);
            yearsAgoInfo = v.findViewById(R.id.yearsAgoInfo);
            cardview = v.findViewById(R.id.cardview_entry);
        }
    }


    public interface OnItemClickListener {
        void onItemClick(View view, EntryInfo obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }
}