package com.pixelcrater.Diaro.storage.sqlite.upgrade;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.storage.sqlite.helpers.MySQLiteWrapper;

public class SQLiteUpgrade_225 {
    private MySQLiteWrapper mySQLiteWrapper;

    public SQLiteUpgrade_225() {
        mySQLiteWrapper = MyApp.getInstance().storageMgr.getSQLiteAdapter().mySQLiteWrapper;
        mySQLiteWrapper.execSQL(String.format("DROP INDEX IF EXISTS %s_%s_idx", Tables.TABLE_ENTRIES, Tables.KEY_ENTRY_ARCHIVED));
    }
}
