package com.pixelcrater.Diaro.main;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.cursoradapter.widget.CursorAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.config.FontsConfig;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.model.EntryInfo;
import com.pixelcrater.Diaro.settings.PreferencesHelper;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.Static;
import com.sandstorm.moods.DefaultMoodAssets;
import com.sandstorm.weather.OwmIcons;
import com.sandstorm.weather.WeatherHelper;

import org.apache.commons.lang3.StringUtils;

import java.io.File;


// Main entries list
public class EntriesCursorAdapter extends CursorAdapter {

    private final LayoutInflater inflater;
    private final int overlayUiColor;
    private final int overlayColor;
    private String searchKeyword;
    private int textSize;
    private int dayTextSize;
    private int timeTextSize;
    private ContentFragment mContentFragment;
    private Typeface mFaFont, mWeatherFont, mEntryFont;
    private boolean isFahrenheit = false;

    public EntriesCursorAdapter(Context context, ContentFragment contentFragment, Cursor cursor, int flags) {

        super(context, cursor, flags);

        if (PreferencesHelper.isPrefUnitFahrenheit())
            isFahrenheit = true;

        mContentFragment = contentFragment;
        inflater = ((Activity) context).getLayoutInflater();

        overlayUiColor = MyThemesUtils.getOverlayPrimaryColor();
        overlayColor = context.getResources().getColor(R.color.row_overlay);

        mFaFont = Typeface.createFromAsset(context.getAssets(), FontsConfig.FONT_PATH_FONTAWESOMEFONT);
        mWeatherFont = Typeface.createFromAsset(context.getAssets(), FontsConfig.FONT_PATH_WEATHERFONT);
        mEntryFont = PreferencesHelper.getPrefTypeFace(context);

        setTextSizes();
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = inflater.inflate(R.layout.entry_list_item, parent, false);

        EntryViewHolder holder = new EntryViewHolder(view);
        view.setTag(holder);

        holder.folderTextView.setTypeface(mFaFont);
        holder.tagsCountTextView.setTypeface(mFaFont);
        holder.photoCountTextView.setTypeface(mFaFont);
        holder.locationTextView.setTypeface(mFaFont);
        holder.weatherTextView.setTypeface(mWeatherFont);

        holder.titleView.setTypeface(mEntryFont, Typeface.BOLD);
        holder.textView.setTypeface(mEntryFont);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
//        AppLog.d("cursor.getPosition(): " + cursor.getPosition());

        if (cursor == null || cursor.isClosed() || cursor.getCount() == 0 || cursor.getColumnCount() == 0) {
            AppLog.e("Cursor is closed");
            return;
        }

        // Get single row cursor
        Cursor rowCursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getSingleEntryCursorByUid(cursor.getString(cursor.getColumnIndex(Tables.KEY_UID)), true);

        // If row not found
        if (rowCursor.getCount() == 0) {
            rowCursor.close();
            return;
        }

        final EntryInfo entryInfo = new EntryInfo(rowCursor);
        rowCursor.close();

        final EntryViewHolder holder = (EntryViewHolder) view.getTag();

        boolean iconsLineVisible = false;

        holder.titleView.setMaxLines(1);
        int rowLinesCount = 3;

        int dispayDensity = PreferencesHelper.getDisplayDensity();
        if (dispayDensity == 1) {
            rowLinesCount = 2;
        }
        if (dispayDensity == 2) {
            rowLinesCount = 4;
            holder.titleView.setMaxLines(3);
        }

        // Item color
        holder.entryContainerViewGroup.setBackgroundResource(MyThemesUtils.getEntryListItemColorResId());

        // Title
        if (entryInfo.title.equals("") || !PreferencesHelper.isTitleEnabled()) {
            rowLinesCount++;
            holder.titleView.setVisibility(View.GONE);
        } else {
            Static.highlightSearchText(holder.titleView, entryInfo.title, searchKeyword);
//            holder.titleTextView.setText(entryInfo.title);
            holder.titleView.setVisibility(View.VISIBLE);
        }

        holder.titleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);

//        AppLog.d("uid: " + entryInfo.uid +   ", title: " + entryInfo.title +  ", unixEpochMillis: " + entryInfo.unixEpochMillis+  ", tzOffset: " + entryInfo.tzOffset);

        // Small date
        if (MyApp.getInstance().prefs.getInt(Prefs.PREF_ENTRY_DATE_STYLE, Prefs.ENTRY_DATE_STYLE_LARGE) == Prefs.ENTRY_DATE_STYLE_SMALL) {
            holder.smallDateTextView.setVisibility(View.VISIBLE);
            holder.smallDateTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_today_grey500_18dp, 0, 0, 0);
            holder.smallDateTextView.setText(String.format("%s %s, %s", entryInfo.getDayOfMonthString(), entryInfo.getDayOfWeekString(), entryInfo.getDateHM()));
        } else {
            holder.smallDateTextView.setVisibility(View.GONE);
        }

        // Folder
        int folderColor = Color.TRANSPARENT;
        if (entryInfo.folderTitle.equals("")) {
            holder.folderTextView.setVisibility(View.GONE);
        } else {
            iconsLineVisible = true;
            holder.folderTextView.setVisibility(View.VISIBLE);
            holder.folderTextView.setText(context.getString(R.string.fa_folder) + " " + entryInfo.folderTitle);

            try {
                if (StringUtils.isNotEmpty(entryInfo.folderColor)) {
                    folderColor = Color.parseColor(entryInfo.folderColor);
                }
            } catch (Exception ignored) {
            }
        }
        holder.folderColorView.setBackgroundColor(folderColor);

        // Tags
        if (entryInfo.tagCount > 0) {
            iconsLineVisible = true;
            holder.tagsCountTextView.setVisibility(View.VISIBLE);

            if (Static.isTablet()) {
                holder.tagsCountTextView.setText(context.getString(R.string.fa_tag) + " " + entryInfo.getTagsTitles());
            } else {
                holder.tagsCountTextView.setText(context.getString(R.string.fa_tag) + " " + entryInfo.tagCount);
            }
        } else {
            holder.tagsCountTextView.setVisibility(View.GONE);
        }

        // Photo icon
        if (entryInfo.photoCount > 0) {
            iconsLineVisible = true;
            holder.photoCountTextView.setVisibility(View.VISIBLE);
            holder.photoCountTextView.setText(context.getString(R.string.fa_picture_o) + " " + entryInfo.photoCount);
        } else {
            holder.photoCountTextView.setVisibility(View.GONE);
        }

        // Location
        if (entryInfo.getLocationTitle().equals("")) {
            holder.locationTextView.setVisibility(View.GONE);
        } else {
            iconsLineVisible = true;
            holder.locationTextView.setVisibility(View.VISIBLE);
            holder.locationTextView.setText(context.getString(R.string.fa_map_marker) + " " + entryInfo.getLocationTitle());
        }

        // Weather
        if (entryInfo.weatherInfo == null || !PreferencesHelper.isWeatherEnabled()) {
            holder.weatherTextView.setVisibility(View.GONE);
        } else {
            iconsLineVisible = true;
            holder.weatherTextView.setVisibility(View.VISIBLE);
            String fontName = OwmIcons.getFontCode(entryInfo.weatherInfo.getIcon());

            if (StringUtils.isEmpty(fontName))
                AppLog.e("fontname = " + entryInfo.weatherInfo.getIcon() + " -> " + fontName);

            double temp = entryInfo.weatherInfo.getTemperature();
            String suffix = WeatherHelper.STRING_CELSIUS;
            if (isFahrenheit) {
                temp = WeatherHelper.celsiusToFahrenheit(temp);
                suffix = WeatherHelper.STRING_FAHRENHEIT;
            }

            holder.weatherTextView.setText(fontName + String.format("%.1f", temp) + suffix);
        }

        // Mood
        if (!entryInfo.isMoodSet() || !PreferencesHelper.isMoodsEnabled()) {
            holder.moodImageView.setVisibility(View.GONE);
        } else {

            iconsLineVisible = true;
            holder.moodImageView.setVisibility(View.VISIBLE);
            DefaultMoodAssets moodAsset = DefaultMoodAssets.getByIconIdentifier(entryInfo.moodIcon);
            // TODO : add try catch!
            try {
                holder.moodImageView.setImageDrawable(ContextCompat.getDrawable(context, moodAsset.getIconRes()));
            } catch (Exception e){

            }

            try {
                int tintColor = Color.parseColor(entryInfo.moodColor);
                holder.moodImageView.setImageTintList(ColorStateList.valueOf(tintColor));
            } catch (Exception e){
            }
        }

        if (!iconsLineVisible) {
            rowLinesCount++;
        }
//		AppLog.d("rowLinesCount: " + rowLinesCount);

        // Text | replace line breaks: holder.textTextView.replace("\n", " ")
        //  Static.highlightSearchText(holder.textTextView, entryInfo.text, searchKeyword);

        String newfullText = entryInfo.text;
        if (dispayDensity == 1) {
            newfullText = entryInfo.text.trim().replaceAll("[\\t\\n\\r]", " ");
        }

        Static.highlightSearchText(holder.textView, newfullText, searchKeyword);

        // the text
        holder.textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize - 2);
        holder.textView.setMinLines(rowLinesCount);
        holder.textView.setMaxLines(rowLinesCount);

        // Primary photo
        if (entryInfo.photoCount > 0 && !entryInfo.firstPhotoFilename.equals("")) {
            // Calculate list item height

            /**   view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

             if (thumbHeight == 0) {
             thumbHeight = (int) (view.getMeasuredHeight() * 0.9);
             thumbWidth = (int) (thumbHeight / Static.PHOTO_PROPORTION);
             AppLog.e(", thumbWidth: " + thumbWidth + ", thumbHeight: " + thumbHeight);

             holder.photoImageView.getLayoutParams().width = thumbWidth;
             //     holder.photoImageView.getLayoutParams().height = thumbHeight;
             } **/

            // Thumbnail size
            holder.photoImageView.setVisibility(View.VISIBLE);

            File photoFile = new File(entryInfo.getFirstPhotoPath());
//            AppLog.d("entryInfo.title: " + entryInfo.title +   ", entryInfo.primaryPhotoPath: " + entryInfo.getFirstPhotoPath() + ", photoFile.exists(): " + photoFile.exists());
//
            if (photoFile.exists() && photoFile.length() > 0) {
                // Show local photo
                Glide.with(mContentFragment).load(photoFile).transform(new CenterCrop(), new RoundedCorners(10)).transition(DrawableTransitionOptions.withCrossFade())
                        .signature(Static.getGlideSignature(photoFile)).error(R.drawable.ic_photo_red_24dp).into(holder.photoImageView);
            } else {
                // Show photo icon
                Glide.with(mContentFragment).load(R.drawable.ic_photo_grey600_24dp).centerInside().into(holder.photoImageView);
            }
        } else {
            holder.photoImageView.setVisibility(View.GONE);
        }

        // Large date
        if (MyApp.getInstance().prefs.getInt(Prefs.PREF_ENTRY_DATE_STYLE, Prefs.ENTRY_DATE_STYLE_LARGE) == Prefs.ENTRY_DATE_STYLE_LARGE) {
            holder.largeDateContainerViewGroup.setVisibility(View.VISIBLE);

            holder.dateDayTextView.setText(entryInfo.getDayOfMonthString());
            holder.dateDayTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, dayTextSize);

            holder.dateWeekdayTextView.setText(entryInfo.getDayOfWeekString());
            holder.dateWeekdayTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, timeTextSize);

            holder.timeTextView.setText(entryInfo.getDateHM());
            holder.timeTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, timeTextSize - 2);
        } else {
            holder.largeDateContainerViewGroup.setVisibility(View.GONE);
        }

        // Multi select overlay
        if (mContentFragment.isMultiSelectMode) {
            holder.overlayView.setVisibility(View.VISIBLE);

            if (mContentFragment.multiSelectedEntriesUids.contains(entryInfo.uid)) {
                holder.overlayView.setBackgroundColor(overlayUiColor);
                holder.overlayCheckedView.setVisibility(View.VISIBLE);
            } else {
                holder.overlayView.setBackgroundColor(overlayColor);
                holder.overlayCheckedView.setVisibility(View.GONE);
            }
        } else {
            holder.overlayView.setVisibility(View.GONE);
        }

        // Not synced indicator
        //if (MyApp.getInstance().storageMgr.isStorageDropbox() && entryInfo.synced == 0) {
        if (MyApp.getInstance().userMgr.isSignedIn() && entryInfo.synced == 0) {
            holder.notSyncedIndicatorImageView.setVisibility(View.VISIBLE);
        } else {
            holder.notSyncedIndicatorImageView.setVisibility(View.GONE);
        }
    }

    String getItemUid(int position) {
        try {
            Cursor cursor = (Cursor) getItem(position);
            if (cursor != null) {
                int index = cursor.getColumnIndex(Tables.KEY_UID);
                if (index != -1 && cursor.getColumnCount() > index) {
                    return cursor.getString(index);
                }
            }
        } catch (Exception e) {
            AppLog.e("Exception: " + e);
        }

        return null;
    }

    void setTextSizes() {
        // Normal
        textSize = 15;
        timeTextSize = 15;
        dayTextSize = 31;

        int dispayDensity = PreferencesHelper.getDisplayDensity();

        if (dispayDensity == 1) {
            textSize = 14;
            timeTextSize = 12;
            dayTextSize = 22;
        }

        switch (MyApp.getInstance().prefs.getInt(Prefs.PREF_TEXT_SIZE, Prefs.SIZE_NORMAL)) {
            // Small
            case Prefs.SIZE_SMALL:
                textSize -= 2;
                timeTextSize -= 2;
                dayTextSize -= 2;
                break;

            // Large
            case Prefs.SIZE_LARGE:
                textSize += 1;
                timeTextSize += 1;
                dayTextSize += 1;
                break;

            // Extra Large
            case Prefs.SIZE_X_LARGE:
                textSize += 2;
                timeTextSize += 2;
                dayTextSize += 2;
                break;
        }
    }

    void setIsFahrenheit(boolean val) {
        isFahrenheit = val;
    }

    static class EntryViewHolder {
        final ViewGroup entryContainerViewGroup;
        final TextView titleView;
        final TextView textView;
        final TextView smallDateTextView;
        final TextView folderTextView;
        final TextView photoCountTextView;
        final TextView tagsCountTextView;
        final ViewGroup largeDateContainerViewGroup;
        final TextView dateDayTextView;
        final TextView dateWeekdayTextView;
        final TextView timeTextView;
        final TextView locationTextView;
        final ImageView moodImageView;
        final TextView weatherTextView;

        final ImageView photoImageView;

        final View folderColorView;
        final ViewGroup overlayView;
        final View overlayCheckedView;
        final ImageView notSyncedIndicatorImageView;

        EntryViewHolder(View view) {
            entryContainerViewGroup = (ViewGroup) view.findViewById(R.id.entry_container);
            titleView = (TextView) view.findViewById(R.id.entry_title);
            textView = (TextView) view.findViewById(R.id.entry_text);
            smallDateTextView = (TextView) view.findViewById(R.id.small_entry_date);
            folderTextView = (TextView) view.findViewById(R.id.entry_folder);
            tagsCountTextView = (TextView) view.findViewById(R.id.entry_tags_count);
            photoCountTextView = (TextView) view.findViewById(R.id.entry_photo_count);
            locationTextView = (TextView) view.findViewById(R.id.entry_location);

            moodImageView = (ImageView) view.findViewById(R.id.entry_mood);
            weatherTextView = (TextView) view.findViewById(R.id.entry_weather);

            largeDateContainerViewGroup = (ViewGroup) view.findViewById(R.id.large_entry_date_container);
            dateDayTextView = (TextView) view.findViewById(R.id.entry_date_day);
            dateWeekdayTextView = (TextView) view.findViewById(R.id.entry_date_weekday);
            timeTextView = (TextView) view.findViewById(R.id.entry_time);
            photoImageView = (ImageView) view.findViewById(R.id.entry_photo);
            folderColorView = view.findViewById(R.id.entry_folder_color_line);
            overlayView = (ViewGroup) view.findViewById(R.id.overlay);
            overlayCheckedView = overlayView.getChildAt(0);
            notSyncedIndicatorImageView = (ImageView) view.findViewById(R.id.not_synced_indicator);
        }

    }

}
