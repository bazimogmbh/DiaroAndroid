package com.pixelcrater.Diaro.storage.sqlite.helpers;

import android.content.Context;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.utils.AppLog;

import net.zetetic.database.DatabaseErrorHandler;
import net.zetetic.database.sqlcipher.SQLiteDatabase;
import net.zetetic.database.sqlcipher.SQLiteOpenHelper;


public class SQLiteOpenHelperCipher extends SQLiteOpenHelper {

    private final SQLiteOpenHelperWrapper sqliteOpenHelperWrapper;

    public SQLiteOpenHelperCipher(int dbNewVersion, String dbName, String password) {
        super(MyApp.getInstance(), dbName, password, null, dbNewVersion, 1, null, null, false);

        AppLog.d("dbName: " + dbName);

        sqliteOpenHelperWrapper = new SQLiteOpenHelperWrapper(dbNewVersion);
    }



    @Override
    public void onOpen(SQLiteDatabase db) {
        AppLog.d("Cipher SQLite opened, getDbVersion(): " + db.getVersion());
        super.onOpen(db);

        // If database exists but has no tables (empty database), create them
        // This can happen when the database file was created but onCreate wasn't called
        MySQLiteWrapper wrapper = new MySQLiteWrapper(db);
        if (!wrapper.isTableExists("diaro_entries") && !wrapper.isTableExists("diaro_attachments")) {
            AppLog.d("Database exists but has no tables, creating them now");
            // sqliteOpenHelperWrapper.createTables(wrapper);
        }

        sqliteOpenHelperWrapper.dropAllTempTables(wrapper);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        sqliteOpenHelperWrapper.createTables(new MySQLiteWrapper(db));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        sqliteOpenHelperWrapper.onUpgrade(new MySQLiteWrapper(db), oldVersion, newVersion);
        // SQLite upgrade is performed in AppUpgradeMgr
    }
}
