package com.pixelcrater.Diaro.atlas;

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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.config.FontsConfig;
import com.pixelcrater.Diaro.model.EntryInfo;
import com.pixelcrater.Diaro.settings.PreferencesHelper;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.Static;
import com.sandstorm.weather.OwmIcons;
import com.sandstorm.weather.WeatherHelper;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AtlasBottomsheetAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<EntryInfo> mEntries = new ArrayList<>();
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;

    private Typeface mFaFont, mMoodsFont, mWeatherFont, mEntryFont;
    private boolean isFahrenheit = false;

    public AtlasBottomsheetAdapter(Context context, List<EntryInfo> items) {
        this.mEntries = items;
        mContext = context;
        mFaFont = Typeface.createFromAsset(context.getAssets(),  FontsConfig.FONT_PATH_FONTAWESOMEFONT);
        mMoodsFont = Typeface.createFromAsset(context.getAssets(), FontsConfig.FONT_PATH_MOODSFONT);
        mWeatherFont = Typeface.createFromAsset(context.getAssets(),  FontsConfig.FONT_PATH_WEATHERFONT);
        mEntryFont = PreferencesHelper.getPrefTypeFace(context);

        if (PreferencesHelper.isPrefUnitFahrenheit())
            isFahrenheit = true;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.entry_atlas_bottomsheet, parent, false);
        v.setBackgroundResource(MyThemesUtils.getEntryListItemColorResId());
        vh = new OriginalViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        EntryInfo entryInfo = mEntries.get(position);

        OriginalViewHolder view = (OriginalViewHolder) holder;
        if (StringUtils.isNotEmpty(entryInfo.folderColor)) {
            int folderColor = Color.parseColor(entryInfo.folderColor);
            view.folderColor.setColorFilter(folderColor, PorterDuff.Mode.SRC_ATOP);
        } else {
            view.folderColor.setVisibility(View.GONE);
        }

        if(entryInfo.title.isEmpty())
            view.title.setVisibility(View.GONE);
        else
            view.title.setVisibility(View.VISIBLE);

        if(entryInfo.text.isEmpty())
            view.subtitle.setVisibility(View.GONE);
        else
            view.subtitle.setVisibility(View.VISIBLE);


        view.title.setText(entryInfo.title);
        view.title.setTypeface(mEntryFont, Typeface.BOLD);
        String newfullText = entryInfo.text.trim().replaceAll("[\\t\\n\\r]", " ");
        view.subtitle.setText(newfullText);
        view.subtitle.setTypeface(mEntryFont);

      //  DateTime dt = entryInfo.getLocalDt();
        String dateText = entryInfo.getDayOfWeekString() + ", " +   android.text.format.DateFormat.format("dd MMM yy", new Date( entryInfo.unixEpochMillis)) + ", " + entryInfo.getDateHM();
        view.date.setText(dateText);
        view.date.setTypeface(mFaFont);

        // Location
        if (entryInfo.getLocationTitle().equals("")) {
            view.locationTextView.setVisibility(View.GONE);
        } else {
            view.locationTextView.setVisibility(View.VISIBLE);
            view.locationTextView.setText(entryInfo.getLocationTitle());
        }

        // Weather
        if (entryInfo.weatherInfo == null) {
            view.weatherTextView.setVisibility(View.GONE);
        } else {
            view.weatherTextView.setVisibility(View.VISIBLE);
            String fontName = OwmIcons.getFontCode(entryInfo.weatherInfo.getIcon());
            double temp = entryInfo.weatherInfo.getTemperature();
            String suffix = WeatherHelper.STRING_CELSIUS;
            if (isFahrenheit) {
                temp = WeatherHelper.celsiusToFahrenheit(temp);
                suffix = WeatherHelper.STRING_FAHRENHEIT;
            }
            view.weatherTextView.setTypeface(mWeatherFont);
            view.weatherTextView.setText( fontName + String.format("%.1f", temp) + suffix);
        }


        // Mood
        if (!entryInfo.isMoodSet())  {
            view.moodTextView.setVisibility(View.GONE);
        } else {

            // TODO : change to icon
            view.moodTextView.setVisibility(View.VISIBLE);
            view.moodTextView.setText(entryInfo.moodTitle);
        }

        DateTime entryDate = new DateTime(entryInfo.getLocalDt());
        DateTime todayDate = new DateTime();
        int yearsDiff = todayDate.getYear() - entryDate.getYear();

        String prefix = "";
        String yearsText = "";

        if (yearsDiff == 0) {
            // TODO : localize
            prefix = "This year";
            yearsText = prefix;
        }

        if (yearsDiff > 0) {
            if (yearsDiff == 1)
                prefix = " %d Year Ago";
            else
                prefix = " %d Years Ago";

            yearsText = mContext.getString(R.string.fa_history) + " " + String.format(prefix, Math.abs(yearsDiff));
        }

        if (yearsDiff < 0) {
            if (yearsDiff == -1)
                prefix = " %d Year Later";
            else
                prefix = " %d Years Later";

            yearsText = mContext.getString(R.string.fa_history) + " " + String.format(prefix, Math.abs(yearsDiff));
        }

        view.yearsAgoInfo.setText(yearsText);
        view.yearsAgoInfo.setTypeface(mFaFont);

        if (entryInfo.photoCount == 0) {
            view.image.setVisibility(View.GONE);
        } else {
            File photoFile = new File(entryInfo.getFirstPhotoPath());
            if (photoFile.exists()) {
                Glide.with(mContext).load(Uri.fromFile(photoFile)).transition(DrawableTransitionOptions.withCrossFade()).signature(Static.getGlideSignature(photoFile)).centerCrop().error(R.drawable.ic_photo_red_24dp).into(view.image);
                // .override(width, height)
            } else {
                view.image.setVisibility(View.GONE);
            }
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
        public TextView title;
        public TextView subtitle;
        public TextView date;
        public TextView yearsAgoInfo;
        public View cardview;
        TextView locationTextView;
        TextView moodTextView;
        TextView weatherTextView;

        public OriginalViewHolder(View v) {
            super(v);
            image = v.findViewById(R.id.image);
            folderColor = v.findViewById(R.id.folderColor);
            title = v.findViewById(R.id.title);
            subtitle = v.findViewById(R.id.subtitle);
            date = v.findViewById(R.id.date);
            yearsAgoInfo = v.findViewById(R.id.yearsAgoInfo);
            cardview = v.findViewById(R.id.cardview_entry);
            locationTextView = v.findViewById(R.id.entry_location);
            moodTextView = v.findViewById(R.id.entry_mood);
            weatherTextView = v.findViewById(R.id.entry_weather);
        }
    }

    public void setData(List<EntryInfo> entries) {
        mEntries = entries;
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, EntryInfo obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

}