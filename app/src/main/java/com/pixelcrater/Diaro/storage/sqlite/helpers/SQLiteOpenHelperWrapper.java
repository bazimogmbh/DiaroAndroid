package com.pixelcrater.Diaro.storage.sqlite.helpers;

import android.provider.BaseColumns;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyDateTimeUtils;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.Static;

import org.joda.time.DateTime;

public class SQLiteOpenHelperWrapper {
    // Entries table
    private final static String TABLE_ENTRIES_TEMP = "diaro_entries_temp";
    private final static String SQL_TABLE_ENTRIES_TEMP =
            "CREATE TABLE IF NOT EXISTS " + TABLE_ENTRIES_TEMP + " (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    Tables.KEY_UID + " TEXT DEFAULT '' NOT NULL UNIQUE," +
                    Tables.KEY_SYNC_ID + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_SYNCED + " INTEGER DEFAULT 0 NOT NULL," +
                    Tables.KEY_ENTRY_ARCHIVED + " INTEGER DEFAULT 0 NOT NULL," +
                    Tables.KEY_ENTRY_DATE + " LONG DEFAULT 0 NOT NULL," +
                    Tables.KEY_ENTRY_TZ_OFFSET + " TEXT DEFAULT '+00:00' NOT NULL," +
                    Tables.KEY_ENTRY_TITLE + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_ENTRY_TEXT + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_ENTRY_FOLDER_UID + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_ENTRY_LOCATION_UID + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_ENTRY_TAGS + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_ENTRY_PRIMARY_PHOTO_UID + " TEXT DEFAULT '' NOT NULL," +

                    Tables.KEY_ENTRY_WEATHER_TEMPERATURE + " REAL DEFAULT NULL," +
                    Tables.KEY_ENTRY_WEATHER_ICON + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_ENTRY_WEATHER_DESCRIPTION + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_ENTRY_MOOD_UID + " INTEGER DEFAULT 0 NOT NULL)";

    // Attachments table
    private final static String TABLE_ATTACHMENTS_TEMP = "diaro_attachments_temp";
    private final static String SQL_TABLE_ATTACHMENTS_TEMP =
            "CREATE TABLE IF NOT EXISTS " + TABLE_ATTACHMENTS_TEMP + " (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    Tables.KEY_UID + " TEXT DEFAULT '' NOT NULL UNIQUE," +
                    Tables.KEY_SYNC_ID + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_SYNCED + " INTEGER DEFAULT 0 NOT NULL," +
                    Tables.KEY_ATTACHMENT_FILE_SYNC_ID + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_ATTACHMENT_FILE_SYNCED + " INTEGER DEFAULT 0 NOT NULL," +
                    Tables.KEY_ATTACHMENT_ENTRY_UID + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_ATTACHMENT_TYPE + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_ATTACHMENT_FILENAME + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_ATTACHMENT_POSITION + " INTEGER DEFAULT 0 NOT NULL);";

    // Folders table
    private final static String TABLE_FOLDERS_TEMP = "diaro_folders_temp";
    private final static String SQL_TABLE_FOLDERS_TEMP =
            "CREATE TABLE IF NOT EXISTS " + TABLE_FOLDERS_TEMP + " (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    Tables.KEY_UID + " TEXT DEFAULT '' NOT NULL UNIQUE," +
                    Tables.KEY_SYNC_ID + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_SYNCED + " INTEGER DEFAULT 0 NOT NULL," +
                    Tables.KEY_FOLDER_TITLE + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_FOLDER_COLOR + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_FOLDER_PATTERN + " TEXT DEFAULT '' NOT NULL);";

    // Tags table
    private final static String TABLE_TAGS_TEMP = "diaro_tags_temp";
    private final static String SQL_TABLE_TAGS_TEMP =
            "CREATE TABLE IF NOT EXISTS " + TABLE_TAGS_TEMP + " (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    Tables.KEY_UID + " TEXT DEFAULT '' NOT NULL UNIQUE," +
                    Tables.KEY_SYNC_ID + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_SYNCED + " INTEGER DEFAULT 0 NOT NULL," +
                    Tables.KEY_TAG_TITLE + " TEXT DEFAULT '' NOT NULL);";

    // Locations table
    private final static String TABLE_LOCATIONS_TEMP = "diaro_locations_temp";
    private final static String SQL_TABLE_LOCATIONS_TEMP =
            "CREATE TABLE IF NOT EXISTS " + TABLE_LOCATIONS_TEMP + " (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    Tables.KEY_UID + " TEXT DEFAULT '' NOT NULL UNIQUE," +
                    Tables.KEY_SYNC_ID + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_SYNCED + " INTEGER DEFAULT 0 NOT NULL," +
                    Tables.KEY_LOCATION_TITLE + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_LOCATION_ADDRESS + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_LOCATION_LATITUDE + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_LOCATION_LONGITUDE + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_LOCATION_ZOOM + " INTEGER DEFAULT 0 NOT NULL);";

    // Moods table
    public final static String TABLE_MOODS_TEMP = "diaro_moods_temp";
    public final static String SQL_TABLE_MOODS_TEMP =
            "CREATE TABLE IF NOT EXISTS " + TABLE_MOODS_TEMP + " (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    Tables.KEY_UID + " TEXT DEFAULT '' NOT NULL UNIQUE," +
                    Tables.KEY_SYNC_ID + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_SYNCED + " INTEGER DEFAULT 0 NOT NULL," +
                    Tables.KEY_MOOD_TITLE + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_MOOD_ICON + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_MOOD_COLOR + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_MOOD_WEIGHT + " INTEGER DEFAULT 0 NOT NULL);";


    // Templates table
    public final static String TABLE_TEMPLATES_TEMP = "diaro_templates_temp";
    public final static String SQL_TABLE_TEMPLATES_TEMP =
            "CREATE TABLE IF NOT EXISTS " + TABLE_TEMPLATES_TEMP + " (" +
                    BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    Tables.KEY_UID + " TEXT DEFAULT '' NOT NULL UNIQUE," +
                    Tables.KEY_SYNC_ID + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_SYNCED + " INTEGER DEFAULT 0 NOT NULL," +

                    Tables.KEY_TEMPLATE_NAME + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_TEMPLATE_TITLE + " TEXT DEFAULT '' NOT NULL," +
                    Tables.KEY_TEMPLATE_TEXT + " TEXT DEFAULT '' NOT NULL," +

                    Tables.KEY_TEMPLATE_DATE_CREATED + " DATETIME DEFAULT (datetime('now','localtime')));";

    public SQLiteOpenHelperWrapper(int dbNewVersion) {
        AppLog.d("dbNewVersion: " + dbNewVersion);
    }

    /**
     * Create database tables
     */
    public void createTables(MySQLiteWrapper mySQLiteWrapper) {
        AppLog.d("Create database tables");

        dropAllTempTables(mySQLiteWrapper);

        // demo data
        String entryUID = "48b6afc3a21d255ef0cbe3ec758be441";

        // Folders
        String[] demo_folder1 = {"d6e6cb19e3b9c02f89d6cd54cfa7c613", MyApp.getInstance().getString(R.string.folder_business), "#3F51B5"};
        String[] demo_folder2 = {"336fdcf7d540e4b430a890b63da159c9", MyApp.getInstance().getString(R.string.folder_entertainment), "#673AB7"};
        String[] demo_folder3 = {"3d594614f445f6b00014e9b77730b833", MyApp.getInstance().getString(R.string.folder_friends), "#4CAF50"};
        String[] demo_folder4 = {"8bd7a1153a88761ad9d37e2f2394c947", MyApp.getInstance().getString(R.string.folder_love), "#F44336"};
        String[] demo_folder5 = {"bc97e12414b8ea00107c66aa552029ae", MyApp.getInstance().getString(R.string.folder_todo), "#FFEB3B"};
        String[] demo_folder6 = {"ec61fc61f7618879b42d938c07a3eda3", MyApp.getInstance().getString(R.string.folder_vacations), "#FF9800"};

        String[][] demo_folders = {demo_folder1, demo_folder2, demo_folder3, demo_folder4, demo_folder5, demo_folder6};

        // Tags
        String[] demo_tag1 = {"2a2542f9e61a9a1d3b83ae31889ac954", MyApp.getInstance().getString(R.string.tag_dream)};
        String[] demo_tag2 = {"158d7558ee87d9a8caa77b59abcdd9ef", MyApp.getInstance().getString(R.string.tag_idea)};
        String[] demo_tag3 = {"1526e0ac850c241ee1631d9ad5664475", MyApp.getInstance().getString(R.string.tag_movie)};

        String[][] demo_tags = {demo_tag1, demo_tag2, demo_tag3};

        // Locations
        String[] demo_location1 = {"9f5ae4433ea9c6c78192aa16b62f1eed", "Cocoa Island, The Maldives", "Male, Maldives", "3.91786", "73.4676353", "17"};

        String[][] demo_locations = {demo_location1};

        // Attachments
        String[] demo_attachment1 = {"f497a8b650b6d44fb13a61b321176348", "entry_demo_image1.jpg", "1", "photo"};
        String[] demo_attachment2 = {"f497a8b650b6d44fb13a61b321176349", "entry_demo_image2.jpg", "2", "photo"};
        String[] demo_attachment3 = {"f497a8b650b6d44fb13a61b321176350", "entry_demo_image3.jpg", "3", "photo"};

        String[][] demo_attachments = {demo_attachment1, demo_attachment2, demo_attachment3};


        // --- Table folders ---
        if (!mySQLiteWrapper.isTableExists(Tables.TABLE_FOLDERS)) {
            mySQLiteWrapper.execSQL(SQL_TABLE_FOLDERS_TEMP);

            for (String[] demo_folder : demo_folders) {
                mySQLiteWrapper.execSQL("INSERT INTO " + TABLE_FOLDERS_TEMP + " (" + Tables.KEY_UID + "," + Tables.KEY_FOLDER_TITLE + "," + Tables.KEY_FOLDER_COLOR + "," + Tables.KEY_FOLDER_PATTERN + "," + Tables.KEY_SYNC_ID +
                        ") VALUES ('" + demo_folder[0] + "','" + demo_folder[1] + "','" + demo_folder[2] + "','','" + Tables.VALUE_UNSYNCED + "')");
            }

            // Rename table
            mySQLiteWrapper.execSQL("ALTER TABLE " + TABLE_FOLDERS_TEMP + " RENAME TO " + Tables.TABLE_FOLDERS);
        }


        // --- Table tags ---
        if (!mySQLiteWrapper.isTableExists(Tables.TABLE_TAGS)) {
            mySQLiteWrapper.execSQL(SQL_TABLE_TAGS_TEMP);
            // Insert demo data
            for (String[] demo_tag : demo_tags) {
                mySQLiteWrapper.execSQL("INSERT INTO " + TABLE_TAGS_TEMP + " (" + Tables.KEY_UID + "," + Tables.KEY_TAG_TITLE + "," + Tables.KEY_SYNC_ID +
                        ") VALUES ('" + demo_tag[0] + "','" + demo_tag[1] + "'," + "'" + Tables.VALUE_UNSYNCED + "')");
            }

            // Rename table
            mySQLiteWrapper.execSQL("ALTER TABLE " + TABLE_TAGS_TEMP + " RENAME TO " + Tables.TABLE_TAGS);
        }

        String locationTitle = "Cocoa Island, The Maldives";
        // --- Table locations ---
        if (!mySQLiteWrapper.isTableExists(Tables.TABLE_LOCATIONS)) {
            mySQLiteWrapper.execSQL(SQL_TABLE_LOCATIONS_TEMP);

            for (String[] demo_location : demo_locations) {
                // Insert demo data
                mySQLiteWrapper.execSQL("INSERT INTO " + TABLE_LOCATIONS_TEMP + " (" + Tables.KEY_UID + " ," + Tables.KEY_LOCATION_TITLE + " ," + Tables.KEY_LOCATION_ADDRESS + " ," + Tables.KEY_LOCATION_LATITUDE + " ," + Tables.KEY_LOCATION_LONGITUDE + " ," + Tables.KEY_LOCATION_ZOOM + " ," + Tables.KEY_SYNC_ID +
                        ") VALUES ('" + demo_location[0] + "','" + demo_location[1] + "','" + demo_location[2] + "','" + demo_location[3] + "','" + demo_location[4] + "','" + demo_location[5] + "','" + Tables.VALUE_UNSYNCED + "')");
            }

            // Rename table
            mySQLiteWrapper.execSQL("ALTER TABLE " + TABLE_LOCATIONS_TEMP + " RENAME TO " + Tables.TABLE_LOCATIONS);
        }


        // --- Table entries ---
        if (!mySQLiteWrapper.isTableExists(Tables.TABLE_ENTRIES)) {
            mySQLiteWrapper.execSQL(SQL_TABLE_ENTRIES_TEMP);

            // Insert demo data
            String entryTitle = MyApp.getInstance().getString(com.pixelcrater.demodata.R.string.demo_entry_1_title);
            String entryText = MyApp.getInstance().getString(com.pixelcrater.demodata.R.string.demo_entry_1_text);

            try {
                mySQLiteWrapper.execSQL("INSERT INTO " + TABLE_ENTRIES_TEMP + " (" +
                        Tables.KEY_UID + "," +
                        Tables.KEY_ENTRY_DATE + "," +
                        Tables.KEY_ENTRY_TZ_OFFSET + "," +
                        Tables.KEY_ENTRY_TITLE + "," +
                        Tables.KEY_ENTRY_TEXT + "," +
                        Tables.KEY_ENTRY_FOLDER_UID + "," +
                        Tables.KEY_ENTRY_LOCATION_UID + "," +

                        Tables.KEY_ENTRY_WEATHER_TEMPERATURE + "," +
                        Tables.KEY_ENTRY_WEATHER_ICON + "," +
                        Tables.KEY_ENTRY_WEATHER_DESCRIPTION + "," +
                        Tables.KEY_ENTRY_MOOD_UID + "," +

                        Tables.KEY_ENTRY_TAGS + "," +
                        Tables.KEY_SYNC_ID +
                        ") VALUES (" +
                        "'" + entryUID + "'," +
                        "strftime('%s','now')*1000," +
                        "'" + MyDateTimeUtils.getCurrentTimeZoneOffset(DateTime.now().getMillis()) + "'," +
                        "'" + entryTitle.replaceAll("'", "''") + "'," +
                        "'" + entryText.replaceAll("'", "''") + "'," +
                        "'" + demo_folder6[0] + "'," +
                        "'" + demo_location1[0] + "'," +

                        28.6 + "," +
                        "'" + "day-cloudy-gusts" + "'," +
                        "'" + "scattered clouds" + "'," +
                        1 + "," +

                        "'," + demo_tag1[0] + "," + demo_tag2[0] + "," + demo_tag3[0] + ",','" +
                        Tables.VALUE_UNSYNCED + "')");
            } catch (Exception e) {
            }

            // Rename table
            mySQLiteWrapper.execSQL("ALTER TABLE " + TABLE_ENTRIES_TEMP + " RENAME TO " + Tables.TABLE_ENTRIES);

            // Add indexes
            // Do not create this index, it's faulty (bad performance)
            /*mySQLiteWrapper.execSQL("CREATE INDEX " +  Tables.TABLE_ENTRIES + "_" + Tables.KEY_ENTRY_ARCHIVED + "_idx ON " + Tables.TABLE_ENTRIES + "(" + Tables.KEY_ENTRY_ARCHIVED + ");");*/
        }

        // --- Table attachments ---
        if (!mySQLiteWrapper.isTableExists(Tables.TABLE_ATTACHMENTS)) {
            mySQLiteWrapper.execSQL(SQL_TABLE_ATTACHMENTS_TEMP);

            for (String[] demo_attachment : demo_attachments) {
                // Insert demo data
                mySQLiteWrapper.execSQL("INSERT INTO " + TABLE_ATTACHMENTS_TEMP + " (" + Tables.KEY_ATTACHMENT_ENTRY_UID + "," + Tables.KEY_UID + "," + Tables.KEY_ATTACHMENT_FILENAME + "," + Tables.KEY_ATTACHMENT_POSITION + "," + Tables.KEY_ATTACHMENT_TYPE + "," + Tables.KEY_SYNC_ID +
                        ") VALUES ('" + entryUID + "', '" + demo_attachment[0] + "', '" + demo_attachment[1] + "'," + demo_attachment[2] + ", '" + demo_attachment[3] + "' , '" + Tables.VALUE_UNSYNCED + "')");
            }

            // Rename table
            mySQLiteWrapper.execSQL("ALTER TABLE " + TABLE_ATTACHMENTS_TEMP + " RENAME TO " + Tables.TABLE_ATTACHMENTS);

            // Add indexes
            mySQLiteWrapper.execSQL("CREATE INDEX " + Tables.TABLE_ATTACHMENTS + "_" + Tables.KEY_ATTACHMENT_ENTRY_UID + "_idx ON " + Tables.TABLE_ATTACHMENTS + "(" + Tables.KEY_ATTACHMENT_ENTRY_UID + ");");
            mySQLiteWrapper.execSQL("CREATE INDEX " + Tables.TABLE_ATTACHMENTS + "_" + Tables.KEY_ATTACHMENT_POSITION + "_idx ON " + Tables.TABLE_ATTACHMENTS + "(" + Tables.KEY_ATTACHMENT_POSITION + ");");
        }

        // --- Table moods ----
        if (!mySQLiteWrapper.isTableExists(Tables.TABLE_MOODS)) {
            mySQLiteWrapper.execSQL(SQL_TABLE_MOODS_TEMP);

            String defaultMoodColor = MyThemesUtils.getHexColor(MyThemesUtils.getListItemTextColor());
            String[] demo_mood1 = {"1", MyApp.getInstance().getString(R.string.mood_1), "mood_1happy", defaultMoodColor, "5"};
            String[] demo_mood2 = {"2", MyApp.getInstance().getString(R.string.mood_2), "mood_2smile", defaultMoodColor , "4"};
            String[] demo_mood3 = {"3", MyApp.getInstance().getString(R.string.mood_3), "mood_3neutral", defaultMoodColor, "3"};
            String[] demo_mood4 = {"4", MyApp.getInstance().getString(R.string.mood_4), "mood_4unhappy", defaultMoodColor, "2"};
            String[] demo_mood5 = {"5", MyApp.getInstance().getString(R.string.mood_5), "mood_5teardrop", defaultMoodColor , "1"};

            String[][] demo_moods = {demo_mood1, demo_mood2, demo_mood3, demo_mood4, demo_mood5};

            for (String[] demo_mood : demo_moods) {
                try {
                    mySQLiteWrapper.execSQL("INSERT INTO " + TABLE_MOODS_TEMP + " (" + Tables.KEY_UID + "," + Tables.KEY_MOOD_TITLE + "," + Tables.KEY_MOOD_ICON + "," + Tables.KEY_MOOD_COLOR + "," + Tables.KEY_MOOD_WEIGHT + "," + Tables.KEY_SYNC_ID +
                            ") VALUES ('" + demo_mood[0] + "','" + demo_mood[1].replaceAll("'", "''") + "','" + demo_mood[2].replaceAll("'", "''") + "','" + demo_mood[3].replaceAll("'", "''") + "','" + Integer.valueOf(demo_mood[4]) + "','" + Tables.VALUE_UNSYNCED + "')");

                } catch (Exception e) {
                    Static.showToastLong(e.getMessage());
                }
            }

            try {
                // Rename table
                mySQLiteWrapper.execSQL("ALTER TABLE " + TABLE_MOODS_TEMP + " RENAME TO " + Tables.TABLE_MOODS);
            } catch (Exception e) {
                Static.showToastLong(e.getMessage());
            }
        }

        // --- Table Templates ---
        if (!mySQLiteWrapper.isTableExists(Tables.TABLE_TEMPLATES)) {
            mySQLiteWrapper.execSQL(SQL_TABLE_TEMPLATES_TEMP);


            String[] demo_template1 = {"9f5ae4433ea9c6c78192aa16b62f1eey", MyApp.getInstance().getString(com.pixelcrater.demodata.R.string.template_weekly_reflection_name), MyApp.getInstance().getString(com.pixelcrater.demodata.R.string.template_weekly_reflection_title), MyApp.getInstance().getString(com.pixelcrater.demodata.R.string.template_weekly_reflection_text)};
            String[] demo_template2 = {"9f5ae4433ea9c6c78192aa16b62f1eei", MyApp.getInstance().getString(com.pixelcrater.demodata.R.string.template_food_log_name), MyApp.getInstance().getString(com.pixelcrater.demodata.R.string.template_food_log_title), MyApp.getInstance().getString(com.pixelcrater.demodata.R.string.template_food_log_text)};
            String[] demo_template3 = {"9f5ae4433ea9c6c78192aa16b62f1eef", MyApp.getInstance().getString(com.pixelcrater.demodata.R.string.template_five_minutes_name), MyApp.getInstance().getString(com.pixelcrater.demodata.R.string.template_five_minutes_title), MyApp.getInstance().getString(com.pixelcrater.demodata.R.string.template_five_minutes_text)};
            String[] demo_template4 = {"9f5ae4433ea9c6c78192aa16b62f1eeg", MyApp.getInstance().getString(com.pixelcrater.demodata.R.string.template_gratitude_name), MyApp.getInstance().getString(com.pixelcrater.demodata.R.string.template_gratitude_title), MyApp.getInstance().getString(com.pixelcrater.demodata.R.string.template_gratitude_text)};


            String[][] demo_templates = {demo_template1, demo_template2, demo_template3, demo_template4};

            for (String[] demo_template : demo_templates) {

                try {
                    mySQLiteWrapper.execSQL("INSERT INTO " + TABLE_TEMPLATES_TEMP + " (" + Tables.KEY_UID + "," + Tables.KEY_TEMPLATE_NAME + "," + Tables.KEY_TEMPLATE_TITLE + "," + Tables.KEY_TEMPLATE_TEXT + "," + Tables.KEY_SYNC_ID +
                            ") VALUES ('" + demo_template[0] + "','" + demo_template[1].replaceAll("'", "''") + "','" + demo_template[2].replaceAll("'", "''") + "','" + demo_template[3].replaceAll("'", "''") + "','" + Tables.VALUE_UNSYNCED + "')");

                } catch (Exception e) {
                    Static.showToastLong(e.getMessage());
                }

            }

            try {
                // Rename table
                mySQLiteWrapper.execSQL("ALTER TABLE " + TABLE_TEMPLATES_TEMP + " RENAME TO " + Tables.TABLE_TEMPLATES);
            } catch (Exception e) {
                Static.showToastLong(e.getMessage());
            }
        }

    }

    public void dropAllTempTables(MySQLiteWrapper mySQLiteWrapper) {
        AppLog.d("mySQLiteWrapper: " + mySQLiteWrapper);

        mySQLiteWrapper.execSQL("DROP TABLE IF EXISTS " + TABLE_ENTRIES_TEMP);
        mySQLiteWrapper.execSQL("DROP TABLE IF EXISTS " + TABLE_ATTACHMENTS_TEMP);
        mySQLiteWrapper.execSQL("DROP TABLE IF EXISTS " + TABLE_FOLDERS_TEMP);
        mySQLiteWrapper.execSQL("DROP TABLE IF EXISTS " + TABLE_TAGS_TEMP);
        mySQLiteWrapper.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS_TEMP);
        mySQLiteWrapper.execSQL("DROP TABLE IF EXISTS " + TABLE_TEMPLATES_TEMP);

    }
}
