package com.pixelcrater.Diaro.storage.sqlite.upgrade;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.storage.sqlite.helpers.MySQLiteWrapper;
import com.pixelcrater.Diaro.utils.AppLog;

public class SQLiteUpgrade_118 {
    private MySQLiteWrapper mySQLiteWrapper;

    public SQLiteUpgrade_118() {
        AppLog.d("");
        mySQLiteWrapper = MyApp.getInstance().storageMgr.getSQLiteAdapter().mySQLiteWrapper;

        // Add synced column to all tables
        addSyncedColumnToTable("diaro_entries");
        addSyncedColumnToTable("diaro_attachments");
        addSyncedColumnToTable("diaro_folders");
        addSyncedColumnToTable("diaro_tags");
        addSyncedColumnToTable("diaro_locations");

        AppLog.d("");
    }

    private void addSyncedColumnToTable(String fullTableName) {
        if (!mySQLiteWrapper.existsColumnInTable(fullTableName, "synced")) {
            AppLog.d("synced column not found in " + fullTableName + " table");

            mySQLiteWrapper.execSQL("ALTER TABLE " + fullTableName + " ADD COLUMN " +
                    "synced INTEGER DEFAULT 0 NOT NULL");
        }
    }
}
