package com.pixelcrater.Diaro.storage.sqlite.upgrade;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.storage.sqlite.helpers.MySQLiteWrapper;

public class SQLiteUpgrade_251 {

    private MySQLiteWrapper mySQLiteWrapper;

    public SQLiteUpgrade_251() {
        mySQLiteWrapper = MyApp.getInstance().storageMgr.getSQLiteAdapter().mySQLiteWrapper;

        updateEntriesTable();
    }

    private void updateEntriesTable() {
        if (!mySQLiteWrapper.existsColumnInTable("diaro_entries", Tables.KEY_ENTRY_WEATHER_TEMPERATURE)) {
            mySQLiteWrapper.execSQL("ALTER TABLE diaro_entries ADD COLUMN " + Tables.KEY_ENTRY_WEATHER_TEMPERATURE + " REAL");
        }

        if (!mySQLiteWrapper.existsColumnInTable("diaro_entries", Tables.KEY_ENTRY_WEATHER_ICON)) {
            mySQLiteWrapper.execSQL("ALTER TABLE diaro_entries ADD COLUMN " + Tables.KEY_ENTRY_WEATHER_ICON + " TEXT DEFAULT '' NOT NULL");
        }

        if (!mySQLiteWrapper.existsColumnInTable("diaro_entries", Tables.KEY_ENTRY_WEATHER_DESCRIPTION)) {
            mySQLiteWrapper.execSQL("ALTER TABLE diaro_entries ADD COLUMN " + Tables.KEY_ENTRY_WEATHER_DESCRIPTION + " TEXT DEFAULT '' NOT NULL");
        }

        if (!mySQLiteWrapper.existsColumnInTable("diaro_entries", Tables.KEY_ENTRY_MOOD_UID)) {
            mySQLiteWrapper.execSQL("ALTER TABLE diaro_entries ADD COLUMN " + Tables.KEY_ENTRY_MOOD_UID + " INTEGER DEFAULT 0 NOT NULL");
        }


    }
}
