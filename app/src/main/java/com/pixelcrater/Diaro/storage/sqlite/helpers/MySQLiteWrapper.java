package com.pixelcrater.Diaro.storage.sqlite.helpers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;

public class MySQLiteWrapper {
    private boolean isCipherDb;
    private android.database.sqlite.SQLiteDatabase androidDb;
    private net.sqlcipher.database.SQLiteDatabase cipherDb;

    public MySQLiteWrapper(android.database.sqlite.SQLiteDatabase db) {
        androidDb = db;
        isCipherDb = false;
    }

    public MySQLiteWrapper(net.sqlcipher.database.SQLiteDatabase db) {
        cipherDb = db;
        isCipherDb = true;
    }

    public boolean isOpen() {
        if (isCipherDb) {
            return cipherDb.isOpen();
        } else {
            return androidDb.isOpen();
        }
    }

    public void close() {
        if (isCipherDb) {
            cipherDb.close();
        } else {
            androidDb.close();
        }
    }

    public int getDbVersion() {
        if (isCipherDb) {
            return cipherDb.getVersion();
        } else {
            return androidDb.getVersion();
        }
    }

    public void setDbVersion(int version) {
        if (isCipherDb) {
            cipherDb.setVersion(version);
        } else {
            androidDb.setVersion(version);
        }
    }

    public Cursor rawQuery(String sql, String[] selectionArgs) {
        if (isCipherDb) {
            return cipherDb.rawQuery(sql, selectionArgs);
        } else {
            return androidDb.rawQuery(sql, selectionArgs);
        }
    }

    public void execSQL(String sql) throws SQLException {
     //   AppLog.e("sql: " + sql);

        if (isCipherDb) {
            cipherDb.execSQL(sql);
        } else {
            androidDb.execSQL(sql);
        }
    }

    public void beginTransaction() {
        if (isCipherDb) {
            cipherDb.beginTransaction();
        } else {
            androidDb.beginTransaction();
        }
    }

    public void endTransaction() {
        if (isCipherDb) {
            cipherDb.endTransaction();
        } else {
            androidDb.endTransaction();
        }
    }

    public void setTransactionSuccessful() {
        if (isCipherDb) {
            cipherDb.setTransactionSuccessful();
        } else {
            androidDb.setTransactionSuccessful();
        }
    }

    public long insertOrThrow(String table, String nullColumnHack, ContentValues values)
            throws android.database.SQLException {

        if (isCipherDb) {
            return cipherDb.insertOrThrow(table, nullColumnHack, values);
        } else {
            return androidDb.insertOrThrow(table, nullColumnHack, values);
        }
    }

    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        if (isCipherDb) {
            return cipherDb.update(table, values, whereClause, whereArgs);
        } else {
            return androidDb.update(table, values, whereClause, whereArgs);
        }
    }

    public int delete(String table, String whereClause, String[] whereArgs) {
        if (isCipherDb) {
            return cipherDb.delete(table, whereClause, whereArgs);
        } else {
            return androidDb.delete(table, whereClause, whereArgs);
        }
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
