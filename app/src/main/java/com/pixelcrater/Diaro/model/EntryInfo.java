package com.pixelcrater.Diaro.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyDateTimeUtils;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.storage.AppLifetimeStorageUtils;
import com.sandstorm.weather.WeatherInfo;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

public class EntryInfo {

    public String uid = "";
    public int archived = 0;
    public long unixEpochMillis = 0;
    public String tzOffset = "";
    public String folderUid = "";
    public String title = "";
    public String text = "";
    public String tags = "";
    public int tagCount = 0;
    public int photoCount = 0;
    public String firstPhotoFilename = "";
    public String locationUid = "";
    public String folderTitle = "";
    public String folderColor;
    public String folderPattern = "";
    public String locationTitle = "";
    public String locationAddress = "";
    public String locationLatitude = "";
    public String locationLongitude = "";
    public int locationZoom = 0;
    public int synced = 0;
    private String firstPhotoFileSyncId;

    public WeatherInfo weatherInfo = null;

    public String moodUid = "";
    public String moodTitle = "";
    public String moodIcon = "";
    public String moodColor = "";

    public EntryInfo() {
        folderColor = "";
    }

    public EntryInfo(Cursor cursor) {
        setUid(cursor.getString(cursor.getColumnIndex(Tables.KEY_UID)));
        setSynced(cursor.getInt(cursor.getColumnIndex(Tables.KEY_SYNCED)));
        setArchived(cursor.getInt(cursor.getColumnIndex(Tables.KEY_ENTRY_ARCHIVED)));
        setDate(cursor.getLong(cursor.getColumnIndex(Tables.KEY_ENTRY_DATE)));
        setTzOffset(cursor.getString(cursor.getColumnIndex(Tables.KEY_ENTRY_TZ_OFFSET)));
        setFolderUid(cursor.getString(cursor.getColumnIndex(Tables.KEY_ENTRY_FOLDER_UID)));
        setTitle(cursor.getString(cursor.getColumnIndex(Tables.KEY_ENTRY_TITLE)));
        setText(cursor.getString(cursor.getColumnIndex(Tables.KEY_ENTRY_TEXT)));
        setTags(cursor.getString(cursor.getColumnIndex(Tables.KEY_ENTRY_TAGS)));
        setLocationUid(cursor.getString(cursor.getColumnIndex(Tables.KEY_ENTRY_LOCATION_UID)));

        double temp = cursor.getDouble(cursor.getColumnIndex(Tables.KEY_ENTRY_WEATHER_TEMPERATURE));
        String weatherIcon = cursor.getString(cursor.getColumnIndex(Tables.KEY_ENTRY_WEATHER_ICON));
        String weatherDescription = cursor.getString(cursor.getColumnIndex(Tables.KEY_ENTRY_WEATHER_DESCRIPTION));

        // AppLog.e("WINFO " + temp + weatherIcon + weatherDescription );

        if (!weatherIcon.isEmpty() && !weatherDescription.isEmpty())
            setWeatherInfo(temp, weatherIcon, weatherDescription);


        // Joined folder
        setFolderTitle(cursor.getString(cursor.getColumnIndex("folder_title")));
        setFolderColor(cursor.getString(cursor.getColumnIndex("folder_color")));
        setFolderPattern(cursor.getString(cursor.getColumnIndex("folder_pattern")));

        // Joined location
        setLocationTitle(cursor.getString(cursor.getColumnIndex("location_title")));
        setLocationAddress(cursor.getString(cursor.getColumnIndex("location_address")));
        setLocationLatitude(cursor.getString(cursor.getColumnIndex("location_latitude")));
        setLocationLongitude(cursor.getString(cursor.getColumnIndex("location_longitude")));
        setLocationZoom(cursor.getInt(cursor.getColumnIndex("location_zoom")));

        // Joined mood

        //   setMood(cursor.getInt(cursor.getColumnIndex(Tables.KEY_ENTRY_MOOD)));
        setMoodUid(cursor.getString(cursor.getColumnIndex(Tables.KEY_ENTRY_MOOD_UID)));
        setMoodTitle(cursor.getString(cursor.getColumnIndex("mood_title")));
        setMoodIcon(cursor.getString(cursor.getColumnIndex("mood_icon")));
        setMoodColor(cursor.getString(cursor.getColumnIndex("mood_color")));

        // Joined photo count
        setPhotoCount(cursor.getInt(cursor.getColumnIndex("photo_count")));

        // Check if primary photo row exists in database
        setFirstPhoto(cursor.getString(cursor.getColumnIndex("primary_photo_filename")),
                cursor.getString(cursor.getColumnIndex("primary_photo_file_sync_id")),
                cursor.getString(cursor.getColumnIndex("first_photo_filename")),
                cursor.getString(cursor.getColumnIndex("first_photo_file_sync_id")));

        // Tags count
        int tagCount = StringUtils.countMatches(tags, ",") - 1;
        if (tagCount < 0) {
            tagCount = 0;
        }
        setTagCount(tagCount);
    }

    public static ArrayList<String> getTagsUidsArrayList(String tags, boolean addQuotes) {
        ArrayList<String> tagsUidsArrayList = new ArrayList<>();

        if (!tags.equals("")) {
            ArrayList<String> splittedArrayList = new ArrayList<>(Arrays.asList(tags.split(",")));

            for (String tag : splittedArrayList) {
                if (tag != null && !tag.equals("")) {
                    tagsUidsArrayList.add(addQuotes ? "'" + tag + "'" : tag);
                }
            }
        }

        return tagsUidsArrayList;
    }

    public static String createJsonString(Cursor cursor) throws Exception {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(Tables.KEY_UID, cursor.getString(cursor.getColumnIndex(Tables.KEY_UID)));
        jsonObject.put(Tables.KEY_ENTRY_ARCHIVED, cursor.getInt(cursor.getColumnIndex(Tables.KEY_ENTRY_ARCHIVED)));
        jsonObject.put(Tables.KEY_ENTRY_DATE, cursor.getLong(cursor.getColumnIndex(Tables.KEY_ENTRY_DATE)));
        jsonObject.put(Tables.KEY_ENTRY_TZ_OFFSET, cursor.getString(cursor.getColumnIndex(Tables.KEY_ENTRY_TZ_OFFSET)));
        jsonObject.put(Tables.KEY_ENTRY_FOLDER_UID, cursor.getString(cursor.getColumnIndex(Tables.KEY_ENTRY_FOLDER_UID)));
        jsonObject.put(Tables.KEY_ENTRY_TITLE, cursor.getString(cursor.getColumnIndex(Tables.KEY_ENTRY_TITLE)));
        jsonObject.put(Tables.KEY_ENTRY_TEXT, cursor.getString(cursor.getColumnIndex(Tables.KEY_ENTRY_TEXT)));
        jsonObject.put(Tables.KEY_ENTRY_TAGS, cursor.getString(cursor.getColumnIndex(Tables.KEY_ENTRY_TAGS)));
        jsonObject.put(Tables.KEY_ENTRY_PRIMARY_PHOTO_UID, cursor.getString(cursor.getColumnIndex(Tables.KEY_ENTRY_PRIMARY_PHOTO_UID)));
        jsonObject.put(Tables.KEY_ENTRY_LOCATION_UID, cursor.getString(cursor.getColumnIndex(Tables.KEY_ENTRY_LOCATION_UID)));

        jsonObject.put(Tables.KEY_ENTRY_WEATHER_TEMPERATURE, cursor.getString(cursor.getColumnIndex(Tables.KEY_ENTRY_WEATHER_TEMPERATURE)));
        jsonObject.put(Tables.KEY_ENTRY_WEATHER_ICON, cursor.getString(cursor.getColumnIndex(Tables.KEY_ENTRY_WEATHER_ICON)));
        jsonObject.put(Tables.KEY_ENTRY_WEATHER_DESCRIPTION, cursor.getString(cursor.getColumnIndex(Tables.KEY_ENTRY_WEATHER_DESCRIPTION)));
        jsonObject.put(Tables.KEY_ENTRY_MOOD_UID, cursor.getString(cursor.getColumnIndex(Tables.KEY_ENTRY_MOOD_UID)));

        return jsonObject.toString();
    }

    public static ContentValues createRowCvFromJsonString(String jsonString) throws Exception {
        JSONObject jsonObject = new JSONObject(jsonString);

        ContentValues cv = new ContentValues();

        cv.put(Tables.KEY_UID, jsonObject.getString(Tables.KEY_UID));

        if (jsonObject.has(Tables.KEY_ENTRY_ARCHIVED)) {
            cv.put(Tables.KEY_ENTRY_ARCHIVED, jsonObject.getInt(Tables.KEY_ENTRY_ARCHIVED));
        }
        if (jsonObject.has(Tables.KEY_ENTRY_DATE)) {
            cv.put(Tables.KEY_ENTRY_DATE, jsonObject.getLong(Tables.KEY_ENTRY_DATE));
        }
        if (jsonObject.has(Tables.KEY_ENTRY_TZ_OFFSET)) {
            cv.put(Tables.KEY_ENTRY_TZ_OFFSET, jsonObject.getString(Tables.KEY_ENTRY_TZ_OFFSET));
        } else {
            // If tz_offset field does not exist in json Set current timezone offset
            cv.put(Tables.KEY_ENTRY_TZ_OFFSET, MyDateTimeUtils.getCurrentTimeZoneOffset(jsonObject.getLong(Tables.KEY_ENTRY_DATE)));
        }
        if (jsonObject.has(Tables.KEY_ENTRY_FOLDER_UID)) {
            cv.put(Tables.KEY_ENTRY_FOLDER_UID, jsonObject.getString(Tables.KEY_ENTRY_FOLDER_UID));
        }
        if (jsonObject.has(Tables.KEY_ENTRY_TITLE)) {
            cv.put(Tables.KEY_ENTRY_TITLE, jsonObject.getString(Tables.KEY_ENTRY_TITLE));
        }
        if (jsonObject.has(Tables.KEY_ENTRY_TEXT)) {
            cv.put(Tables.KEY_ENTRY_TEXT, jsonObject.getString(Tables.KEY_ENTRY_TEXT));
        }
        if (jsonObject.has(Tables.KEY_ENTRY_TAGS)) {
            cv.put(Tables.KEY_ENTRY_TAGS, jsonObject.getString(Tables.KEY_ENTRY_TAGS));
        }
        if (jsonObject.has(Tables.KEY_ENTRY_PRIMARY_PHOTO_UID)) {
            cv.put(Tables.KEY_ENTRY_PRIMARY_PHOTO_UID, jsonObject.getString(Tables.KEY_ENTRY_PRIMARY_PHOTO_UID));
        }
        if (jsonObject.has(Tables.KEY_ENTRY_LOCATION_UID)) {
            cv.put(Tables.KEY_ENTRY_LOCATION_UID, jsonObject.getString(Tables.KEY_ENTRY_LOCATION_UID));
        }

        if (jsonObject.has(Tables.KEY_ENTRY_WEATHER_TEMPERATURE)) {
            cv.put(Tables.KEY_ENTRY_WEATHER_TEMPERATURE, jsonObject.getString(Tables.KEY_ENTRY_WEATHER_TEMPERATURE));
        }
        if (jsonObject.has(Tables.KEY_ENTRY_WEATHER_ICON)) {
            cv.put(Tables.KEY_ENTRY_WEATHER_ICON, jsonObject.getString(Tables.KEY_ENTRY_WEATHER_ICON));
        }
        if (jsonObject.has(Tables.KEY_ENTRY_WEATHER_DESCRIPTION)) {
            cv.put(Tables.KEY_ENTRY_WEATHER_DESCRIPTION, jsonObject.getString(Tables.KEY_ENTRY_WEATHER_DESCRIPTION));
        }

        if (jsonObject.has(Tables.KEY_ENTRY_MOOD_UID)) {
            cv.put(Tables.KEY_ENTRY_MOOD_UID, jsonObject.getString(Tables.KEY_ENTRY_MOOD_UID));
        }

        return cv;
    }

    public String getTagsTitles() {
        if (!tags.equals("")) {
            StringBuilder sb = null;

            Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getRowsCursor(Tables.TABLE_TAGS,
                    "WHERE " + Tables.KEY_UID + " IN (" + TextUtils.join(",", getTagsUidsArrayList(tags, true)) + ")" +
                            " ORDER BY " + " " + Tables.KEY_TAG_TITLE + " COLLATE NOCASE," + " " + Tables.KEY_UID, null);

//            AppLog.d("cursor.getCount(): " + cursor.getCount());

            while (cursor.moveToNext()) {
                if (sb == null) {
                    sb = new StringBuilder();
                } else {
                    sb.append(", ");
                }
                sb.append(cursor.getString(cursor.getColumnIndex(Tables.KEY_TAG_TITLE)));
            }
            cursor.close();

            if (sb != null) {
                return sb.toString();
            }
        }

        return "";
    }

    private void setFirstPhoto(String primaryPhotoFilename, String primaryPhotoFileSyncId, String firstPhotoFilename, String firstPhotoFileSyncId) {
        if (photoCount > 0) {
            if (primaryPhotoFilename == null) {
                this.firstPhotoFilename = firstPhotoFilename;
                this.firstPhotoFileSyncId = firstPhotoFileSyncId;
            } else {
                this.firstPhotoFilename = primaryPhotoFilename;
                this.firstPhotoFileSyncId = primaryPhotoFileSyncId;
            }
        }
    }

    public void setUid(String uid) {
        this.uid = uid == null ? "" : uid;
    }

    public void setSynced(int synced) {
        this.synced = synced;
    }

    public void setArchived(int archived) {
        this.archived = archived;
    }

    public void setDate(long unixEpochMillis) {
        this.unixEpochMillis = unixEpochMillis;
    }

    public String getDayOfMonthString() {
        return Static.getDigitWithFrontZero(getLocalDt().getDayOfMonth());
    }

    public String getDayOfWeekString() {
        int dayOfWeek = getLocalDt().getDayOfWeek();
        return Static.getDayOfWeekShortTitle(Static.convertDayOfWeekFromJodaTimeToCalendar(dayOfWeek));
    }

    public String getDateHM() {
        return getLocalDt().toString(MyDateTimeUtils.getTimeFormat());
    }

    public void setTzOffset(String tzOffset) {
        // Check if tz offset is valid
        if (isTzOffsetValid(tzOffset)) {
            this.tzOffset = tzOffset;
        } else {
            this.tzOffset = MyDateTimeUtils.getCurrentTimeZoneOffset(unixEpochMillis);
        }
    }

    private boolean isTzOffsetValid(String tzOffset) {
        return tzOffset.matches("[+-][0-9]{2}:[0-9]{2}\\b");
    }

    /**
     * Returns local time in a timezone entry was created.
     * Local time is calculated from unixEpochMillis (UTC) and assigning tz_offset
     */
    public DateTime getLocalDt() {
        DateTimeZone dateTimeZone = DateTimeZone.forID(tzOffset);
        return new DateTime(unixEpochMillis).withZone(dateTimeZone);

//        AppLog.d("time in device timezone: " +
//                new DateTime(unixEpochMillis).toString("yyyy-MM-dd hh:mm:ss") +
//                ", local time by tz_offset(" + tzOffset + "): " +
//                localDt.toString("yyyy-MM-dd hh:mm:ss") + " (" + dateTimeZone.toString() + ")");
    }

    public void setFolderUid(String folderUid) {
        this.folderUid = folderUid == null ? "" : folderUid;
    }

    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
    }

    public void setTags(String tags) {
        this.tags = tags == null ? "" : tags;
    }

    public void setTagCount(int tagCount) {
        this.tagCount = tagCount;
    }

    public void setPhotoCount(int photoCount) {
        this.photoCount = photoCount;
    }

//    public void setPrimaryPhotoUid(String primaryPhotoUid) {
//        this.primaryPhotoUid = primaryPhotoUid == null ? "" : primaryPhotoUid;
//    }

    public void setLocationUid(String locationUid) {
        this.locationUid = locationUid == null ? "" : locationUid;
    }

    public void setFolderTitle(String folderTitle) {
        this.folderTitle = folderTitle == null ? "" : folderTitle;
    }

    public void setFolderColor(String folderColor) {
        this.folderColor = folderColor == null ? "" : folderColor;
    }

    public void setFolderPattern(String folderPattern) {
        this.folderPattern = folderPattern == null ? "" : folderPattern;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress == null ? "" : locationAddress;
    }

    public void setLocationLatitude(String locationLatitude) {
        this.locationLatitude = locationLatitude == null ? "" : locationLatitude;
    }

    public void setLocationLongitude(String locationLongitude) {
        this.locationLongitude = locationLongitude == null ? "" : locationLongitude;
    }

    public void setLocationZoom(int locationZoom) {
        this.locationZoom = locationZoom;
    }

    public String getLocationTitle() {
        String title = locationTitle;
        if (title.equals("")) {
            title = locationAddress;
        }
        if (title.equals("") && !locationLatitude.equals("") && !locationLongitude.equals("")) {
            title = locationLatitude + ", " + locationLongitude;
        }
        return title;
    }

    public void setLocationTitle(String locationTitle) {
        this.locationTitle = locationTitle == null ? "" : locationTitle;
    }


    public void setMoodUid(String moodUid) {
        this.moodUid = moodUid == null ? "" : moodUid;
    }

    public void setMoodTitle(String moodTitle) {
        this.moodTitle = moodTitle == null ? "" : moodTitle;
    }

    public void setMoodIcon(String moodIcon) {
        this.moodIcon = moodIcon == null ? "" : moodIcon;
    }

    public void setMoodColor(String moodColor) {
        this.moodColor = moodColor == null ? "" : moodColor;
    }


    public String getFirstPhotoPath() {
        return AppLifetimeStorageUtils.getMediaPhotosDirPath() + "/" + firstPhotoFilename;
    }

    public String getFirstPhotoFileSyncId() {
        return firstPhotoFileSyncId;
    }

    public void setWeatherInfo(double weatherTemprature, String weatherIcon, String weatherDescription) {
        WeatherInfo weatherInfo = new WeatherInfo();
        weatherInfo.setTemperature(weatherTemprature);
        weatherInfo.setIcon(weatherIcon);
        weatherInfo.setDescription(weatherDescription);

        this.weatherInfo = weatherInfo;
    }

    public WeatherInfo getWeatherInfo() {
        return this.weatherInfo;
    }

    public void setMood(int mood) {
        this.moodUid = String.valueOf(mood);
    }

    /**
     * public Mood getMood() {
     * if (moodUid < Mood.MOOD_AWESOME_ID)
     * return null;
     * if (moodUid > Mood.MOOD_AWFUL_ID)
     * return null;
     * <p>
     * return new Mood(moodUid);
     * }
     **/

    public void print() {
        AppLog.e("Uid-> " + uid + ", " + "Title-> " + title);
    }

    public boolean isMoodSet() {
        return moodUid != null && !moodUid.isEmpty() && moodUid.compareTo("0") != 0;

    }
}
