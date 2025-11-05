package com.pixelcrater.Diaro.storage.sqlite.helpers;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.utils.AppLog;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

public class SQLiteOpenHelperCipher extends SQLiteOpenHelper {

    private final SQLiteOpenHelperWrapper sqliteOpenHelperWrapper;

    public SQLiteOpenHelperCipher(int dbNewVersion, String dbName) {
        super(MyApp.getInstance(), dbName, null, dbNewVersion);
        AppLog.d("dbName: " + dbName);

        sqliteOpenHelperWrapper = new SQLiteOpenHelperWrapper(dbNewVersion);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        AppLog.d("Cipher SQLite opened, getDbVersion(): " + db.getVersion());
        super.onOpen(db);

        sqliteOpenHelperWrapper.dropAllTempTables(new MySQLiteWrapper(db));
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
