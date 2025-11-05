package com.pixelcrater.Diaro.storage;

import android.content.ContentValues;

import java.util.HashMap;
import java.util.Map;

public class Tables {

    // Fields for all tables
    public final static String KEY_UID = "uid";
    public final static String KEY_SYNC_ID = "sync_id";
    public final static String KEY_SYNCED = "synced";

    // diaro_entries table fields
    public final static String TABLE_ENTRIES = "diaro_entries";
    public final static String KEY_ENTRY_ARCHIVED = "archived";
    public final static String KEY_ENTRY_DATE = "date";
    public final static String KEY_ENTRY_TZ_OFFSET = "tz_offset";
    public final static String KEY_ENTRY_TITLE = "title"; // deprecated: entry_name
    public final static String KEY_ENTRY_TEXT = "text";
    public final static String KEY_ENTRY_FOLDER_UID = "folder_uid"; // deprecated: parent
    public final static String KEY_ENTRY_LOCATION_UID = "location_uid"; // deprecated: location and location_coords
    public final static String KEY_ENTRY_TAGS = "tags";
    public final static String KEY_ENTRY_PRIMARY_PHOTO_UID = "primary_photo_uid";
    public final static String KEY_ENTRY_WEATHER_TEMPERATURE = "weather_temperature";
    public final static String KEY_ENTRY_WEATHER_ICON = "weather_icon";
    public final static String KEY_ENTRY_WEATHER_DESCRIPTION = "weather_description";
    public final static String KEY_ENTRY_MOOD_UID = "mood";

    // diaro_folders table fields
    public final static String TABLE_FOLDERS = "diaro_folders"; // deprecated: categories
    public final static String KEY_FOLDER_TITLE = "title"; // deprecated: category_name
    public final static String KEY_FOLDER_COLOR = "color"; // deprecated:category_color
    public final static String KEY_FOLDER_PATTERN = "pattern";

    // diaro_tags table fields
    public final static String TABLE_TAGS = "diaro_tags";
    public final static String KEY_TAG_TITLE = "title"; // deprecated: tag_name

    // diaro_locations table fields
    public final static String TABLE_LOCATIONS = "diaro_locations";
    public final static String KEY_LOCATION_TITLE = "title";
    public final static String KEY_LOCATION_ADDRESS = "address";
    public final static String KEY_LOCATION_LATITUDE = "lat";
    public final static String KEY_LOCATION_LONGITUDE = "lng"; // deprecated: long
    public final static String KEY_LOCATION_ZOOM = "zoom";

    // diaro_attachments table fields
    public final static String TABLE_ATTACHMENTS = "diaro_attachments";
    public final static String KEY_ATTACHMENT_FILE_SYNC_ID = "file_sync_id";
    public final static String KEY_ATTACHMENT_FILE_SYNCED = "file_synced";
    public final static String KEY_ATTACHMENT_ENTRY_UID = "entry_uid";
    public final static String KEY_ATTACHMENT_TYPE = "type";
    public final static String KEY_ATTACHMENT_FILENAME = "filename";
    public final static String KEY_ATTACHMENT_POSITION = "position";

    // diaro_moods table fields
    public final static String TABLE_MOODS = "diaro_moods";
    public final static String KEY_MOOD_TITLE = "title";
    public final static String KEY_MOOD_ICON = "icon";
    public final static String KEY_MOOD_COLOR = "color";
    public final static String KEY_MOOD_WEIGHT = "weight";

    // diaro_templates table fields
    public final static String TABLE_TEMPLATES= "diaro_templates";
    public final static String KEY_TEMPLATE_NAME = "name";
    public final static String KEY_TEMPLATE_TITLE = "title";
    public final static String KEY_TEMPLATE_TEXT = "text";
    public final static String KEY_TEMPLATE_DATE_CREATED = "date_created";

    // Table field types
    public final static String FIELD_TYPE_STRING = "string";
    public final static String FIELD_TYPE_LONG = "long";
    public final static String FIELD_TYPE_DATE = "date";
    public final static String FIELD_TYPE_REAL = "real";
    public final static String FIELD_TYPE_INTEGER = "integer";

    public final static String VALUE_ATTACHMENT_NOT_FOUND = "not_found";
    public final static String VALUE_UNSYNCED = "unsynced";

    public final static Map<String, ContentValues> tablesMap = new HashMap<>();

    public static ContentValues getTableFieldsCv(String fullTableName) {
        if (tablesMap.containsKey(fullTableName)) {
            return tablesMap.get(fullTableName);
        }

        // If this table not found
        ContentValues tableFieldsCv = new ContentValues();
        tableFieldsCv.put(Tables.KEY_UID, Tables.FIELD_TYPE_STRING);

        switch (fullTableName) {
            case Tables.TABLE_ENTRIES:
                tableFieldsCv.put(Tables.KEY_ENTRY_ARCHIVED, Tables.FIELD_TYPE_LONG);
                tableFieldsCv.put(Tables.KEY_ENTRY_DATE, Tables.FIELD_TYPE_DATE);
                tableFieldsCv.put(Tables.KEY_ENTRY_TZ_OFFSET, Tables.FIELD_TYPE_STRING);
                tableFieldsCv.put(Tables.KEY_ENTRY_TITLE, Tables.FIELD_TYPE_STRING);
                tableFieldsCv.put(Tables.KEY_ENTRY_TEXT, Tables.FIELD_TYPE_STRING);
                tableFieldsCv.put(Tables.KEY_ENTRY_FOLDER_UID, Tables.FIELD_TYPE_STRING);
                tableFieldsCv.put(Tables.KEY_ENTRY_LOCATION_UID, Tables.FIELD_TYPE_STRING);
                tableFieldsCv.put(Tables.KEY_ENTRY_TAGS, Tables.FIELD_TYPE_STRING);
                tableFieldsCv.put(Tables.KEY_ENTRY_PRIMARY_PHOTO_UID, Tables.FIELD_TYPE_STRING);

                tableFieldsCv.put(Tables.KEY_ENTRY_WEATHER_TEMPERATURE, Tables.FIELD_TYPE_REAL);
                tableFieldsCv.put(Tables.KEY_ENTRY_WEATHER_ICON, Tables.FIELD_TYPE_STRING);
                tableFieldsCv.put(Tables.KEY_ENTRY_WEATHER_DESCRIPTION, Tables.FIELD_TYPE_STRING);
                tableFieldsCv.put(Tables.KEY_ENTRY_MOOD_UID, Tables.FIELD_TYPE_INTEGER);
                break;

            case Tables.TABLE_FOLDERS:
                tableFieldsCv.put(Tables.KEY_FOLDER_TITLE, Tables.FIELD_TYPE_STRING);
                tableFieldsCv.put(Tables.KEY_FOLDER_COLOR, Tables.FIELD_TYPE_STRING);
                tableFieldsCv.put(Tables.KEY_FOLDER_PATTERN, Tables.FIELD_TYPE_STRING);
                break;

            case Tables.TABLE_TAGS:
                tableFieldsCv.put(Tables.KEY_TAG_TITLE, Tables.FIELD_TYPE_STRING);
                break;

            case Tables.TABLE_LOCATIONS:
                tableFieldsCv.put(Tables.KEY_LOCATION_TITLE, Tables.FIELD_TYPE_STRING);
                tableFieldsCv.put(Tables.KEY_LOCATION_ADDRESS, Tables.FIELD_TYPE_STRING);
                tableFieldsCv.put(Tables.KEY_LOCATION_LATITUDE, Tables.FIELD_TYPE_STRING);
                tableFieldsCv.put(Tables.KEY_LOCATION_LONGITUDE, Tables.FIELD_TYPE_STRING);
                tableFieldsCv.put(Tables.KEY_LOCATION_ZOOM, Tables.FIELD_TYPE_LONG);
                break;

            case Tables.TABLE_ATTACHMENTS:
                tableFieldsCv.put(Tables.KEY_ATTACHMENT_ENTRY_UID, Tables.FIELD_TYPE_STRING);
                tableFieldsCv.put(Tables.KEY_ATTACHMENT_TYPE, Tables.FIELD_TYPE_STRING);
                tableFieldsCv.put(Tables.KEY_ATTACHMENT_FILENAME, Tables.FIELD_TYPE_STRING);
                tableFieldsCv.put(Tables.KEY_ATTACHMENT_POSITION, Tables.FIELD_TYPE_LONG);
                break;

            case Tables.TABLE_MOODS:
                tableFieldsCv.put(Tables.KEY_MOOD_TITLE, Tables.FIELD_TYPE_STRING);
                tableFieldsCv.put(Tables.KEY_MOOD_ICON, Tables.FIELD_TYPE_STRING);
                tableFieldsCv.put(Tables.KEY_MOOD_COLOR, Tables.FIELD_TYPE_STRING);
                tableFieldsCv.put(Tables.KEY_MOOD_WEIGHT, Tables.FIELD_TYPE_INTEGER);
                break;

            case Tables.TABLE_TEMPLATES:
                tableFieldsCv.put(Tables.KEY_TEMPLATE_NAME, Tables.FIELD_TYPE_STRING);
                tableFieldsCv.put(Tables.KEY_TEMPLATE_TITLE, Tables.FIELD_TYPE_STRING);
                tableFieldsCv.put(Tables.KEY_TEMPLATE_TEXT, Tables.FIELD_TYPE_STRING);
                tableFieldsCv.put(Tables.KEY_TEMPLATE_DATE_CREATED, Tables.FIELD_TYPE_DATE);
                break;
        }

        tablesMap.put(fullTableName, tableFieldsCv);

        return tableFieldsCv;
    }

    public static String getFieldType(String fullTableName, String key) {
        ContentValues tableFieldsCv = getTableFieldsCv(fullTableName);
        return tableFieldsCv.getAsString(key);
    }

    public static String getLocalTime(String key) {
        return "datetime(" + key + " / 1000, 'unixepoch', " + Tables.KEY_ENTRY_TZ_OFFSET + ")";
    }

    public static String getLocalTime(long millis) {
        return getLocalTime(String.valueOf(millis));
    }

    public static String getOnlyMs(String key) {
        return key + " - " + key + "/1000*1000";
    }

}
