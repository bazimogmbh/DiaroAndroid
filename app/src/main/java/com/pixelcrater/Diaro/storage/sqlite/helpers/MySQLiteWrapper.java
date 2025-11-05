package com.pixelcrater.Diaro.storage.sqlite.helpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;

import net.zetetic.database.sqlcipher.SQLiteDatabase;

public class MySQLiteWrapper {

    private SQLiteDatabase cipherDb;

    public MySQLiteWrapper(SQLiteDatabase db) {
        cipherDb = db;
    }

    public boolean isOpen() {
        return cipherDb.isOpen();
    }

    public void close() {
        cipherDb.close();
    }

    public int getDbVersion() {
        return cipherDb.getVersion();
    }

    public void setDbVersion(int version) {
        cipherDb.setVersion(version);
    }

    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return cipherDb.rawQuery(sql, selectionArgs);
    }

    public void execSQL(String sql) throws SQLException {
        //   AppLog.e("sql: " + sql);

        cipherDb.execSQL(sql);
    }

    public void beginTransaction() {
        cipherDb.beginTransaction();
    }

    public void endTransaction() {
        cipherDb.endTransaction();
    }

    public void setTransactionSuccessful() {
        cipherDb.setTransactionSuccessful();
    }

    public long insertOrThrow(String table, String nullColumnHack, ContentValues values)
            throws SQLException {

        return cipherDb.insertOrThrow(table, nullColumnHack, values);
    }

    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        return cipherDb.update(table, values, whereClause, whereArgs);
    }

    public int delete(String table, String whereClause, String[] whereArgs) {
        return cipherDb.delete(table, whereClause, whereArgs);
    }

    public boolean isTableExists(String table) {
        Cursor cursor = rawQuery("SELECT DISTINCT tbl_name FROM sqlite_master " +  "WHERE tbl_name = '" + table + "'", null);
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

    public boolean existsColumnInTable(String fullTableName, String columnToCheck) {
        Cursor cursor = rawQuery("SELECT * FROM " + fullTableName + " LIMIT 0", null);
        // getColumnIndex gives us the index (0 to ...) of the column - otherwise we get a -1
        if (cursor.getColumnIndex(columnToCheck) != -1) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }

}
