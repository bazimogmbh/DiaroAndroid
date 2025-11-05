package com.pixelcrater.Diaro.storage.sqlite.upgrade;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.storage.sqlite.helpers.MySQLiteWrapper;

import static com.pixelcrater.Diaro.storage.sqlite.helpers.SQLiteOpenHelperWrapper.SQL_TABLE_TEMPLATES_TEMP;
import static com.pixelcrater.Diaro.storage.sqlite.helpers.SQLiteOpenHelperWrapper.TABLE_TEMPLATES_TEMP;

public class SQLiteUpgrade_335 {

    private MySQLiteWrapper mySQLiteWrapper;

    public SQLiteUpgrade_335() {
        mySQLiteWrapper = MyApp.getInstance().storageMgr.getSQLiteAdapter().mySQLiteWrapper;

        try {
            createTemplatesTable();
        } catch (Exception ignored) {
        }

    }

    private void createTemplatesTable() {
        if (!mySQLiteWrapper.isTableExists(Tables.TABLE_TEMPLATES)) {
            mySQLiteWrapper.execSQL(SQL_TABLE_TEMPLATES_TEMP);

            String[] demo_template1 = {"9f5ae4433ea9c6c78192aa16b62f1eey", MyApp.getInstance().getString(R.string.template_weekly_reflection_name), MyApp.getInstance().getString(R.string.template_weekly_reflection_title), MyApp.getInstance().getString(R.string.template_weekly_reflection_text)};
            String[] demo_template2 = {"9f5ae4433ea9c6c78192aa16b62f1eei", MyApp.getInstance().getString(R.string.template_food_log_name), MyApp.getInstance().getString(R.string.template_food_log_title), MyApp.getInstance().getString(R.string.template_food_log_text)};
            String[] demo_template3 = {"9f5ae4433ea9c6c78192aa16b62f1eef", MyApp.getInstance().getString(R.string.template_five_minutes_name), MyApp.getInstance().getString(R.string.template_five_minutes_title), MyApp.getInstance().getString(R.string.template_five_minutes_text)};
            String[] demo_template4 = {"9f5ae4433ea9c6c78192aa16b62f1eeg", MyApp.getInstance().getString(R.string.template_gratitude_name), MyApp.getInstance().getString(R.string.template_gratitude_title), MyApp.getInstance().getString(R.string.template_gratitude_text)};

            String[][] demo_templates = {demo_template1, demo_template2, demo_template3, demo_template4};

            for (String[] demo_template : demo_templates) {
                mySQLiteWrapper.execSQL("INSERT INTO " + TABLE_TEMPLATES_TEMP + " (" + Tables.KEY_UID + "," + Tables.KEY_TEMPLATE_NAME + "," + Tables.KEY_TEMPLATE_TITLE + "," + Tables.KEY_TEMPLATE_TEXT + "," + Tables.KEY_SYNC_ID +
                        ") VALUES ('" + demo_template[0].replaceAll("'", "''")  + "','" + demo_template[1].replaceAll("'", "''") + "','" +  demo_template[2].replaceAll("'", "''") +  "','" +  demo_template[3].replaceAll("'", "''") + "','" + Tables.VALUE_UNSYNCED + "')");

            }

            // Rename table
            mySQLiteWrapper.execSQL("ALTER TABLE " + TABLE_TEMPLATES_TEMP + " RENAME TO " + Tables.TABLE_TEMPLATES);
        }

    }



}
