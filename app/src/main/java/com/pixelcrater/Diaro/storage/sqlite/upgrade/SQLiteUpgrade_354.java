package com.pixelcrater.Diaro.storage.sqlite.upgrade;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.storage.sqlite.helpers.MySQLiteWrapper;
import com.pixelcrater.Diaro.utils.MyThemesUtils;
import com.pixelcrater.Diaro.utils.Static;

import static com.pixelcrater.Diaro.storage.sqlite.helpers.SQLiteOpenHelperWrapper.SQL_TABLE_MOODS_TEMP;
import static com.pixelcrater.Diaro.storage.sqlite.helpers.SQLiteOpenHelperWrapper.TABLE_MOODS_TEMP;

public class SQLiteUpgrade_354 {

    private MySQLiteWrapper mySQLiteWrapper;

    public SQLiteUpgrade_354() {
        mySQLiteWrapper = MyApp.getInstance().storageMgr.getSQLiteAdapter().mySQLiteWrapper;

        try {
            createMoodsTable();
        } catch (Exception ignored) {
        }

    }

    private void createMoodsTable() {
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

    }


}
