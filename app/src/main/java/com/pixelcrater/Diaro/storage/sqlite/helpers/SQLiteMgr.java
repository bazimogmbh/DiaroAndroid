package com.pixelcrater.Diaro.storage.sqlite.helpers;

import android.database.Cursor;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.settings.PreferencesHelper;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.utils.MyDevice;


import net.zetetic.database.sqlcipher.SQLiteConnection;
import net.zetetic.database.sqlcipher.SQLiteDatabase;
import net.zetetic.database.sqlcipher.SQLiteDatabaseHook;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class SQLiteMgr {

    private final static String DB_NAME_PLAIN = "DiaroDB";
    private final static String DB_NAME_ENC = "DiaroDB.enc";
    private final static String DB_NAME_ENC2 = "DiaroDB.enc2";

    public final static File plainDbFile = MyApp.getInstance().getDatabasePath(DB_NAME_PLAIN);
    public final static File encryptedDbFileV1 = MyApp.getInstance().getDatabasePath(DB_NAME_ENC);
    public final static File encryptedDbFileV2 = MyApp.getInstance().getDatabasePath(DB_NAME_ENC2);

    SQLiteDatabaseHook hook = new SQLiteDatabaseHook() {
        @Override
        public void preKey(SQLiteConnection connection) {

        }

        @Override
        public void postKey(SQLiteConnection connection) {
            //  singleValueFromQuery(database, "PRAGMA cipher_migrate");
        }


    };

    public SQLiteMgr() {
    }

    // Android plain database
    public android.database.sqlite.SQLiteDatabase getAndroidPlainDb() {
        // Set database new version to 1, because 0 is not allowed
        int dbVersion = 1;
        if (plainDbFile.exists()) {
            // Open Android plain database to get its version
            android.database.sqlite.SQLiteDatabase dbFile = android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(plainDbFile, null);
            dbVersion = dbFile.getVersion();
            dbFile.close();
        }

        SQLiteOpenHelperAndroid sqliteOpenHelperAndroid = new SQLiteOpenHelperAndroid(dbVersion, DB_NAME_PLAIN);
        android.database.sqlite.SQLiteDatabase db = sqliteOpenHelperAndroid.getWritableDatabase();

        return db;
    }

    public SQLiteDatabase getCipherEncryptedDb() {

        // upgrade the old db first , if it exists
        if (encryptedDbFileV1.exists()) {
            upgradeEncryptedDbFileFromV1ToV2();
        }

        //Prevent crash on marshmellow
        if (!encryptedDbFileV2.exists() || encryptedDbFileV2.length() == 0) {
            encryptedDbFileV2.mkdirs();
            encryptedDbFileV2.delete();
        }


        SQLiteDatabase encryptedDbFile;

        //   AppLog.e("db path ->" + encryptedDbFileV2.getAbsolutePath());

        if (!PreferencesHelper.isMigratedToSql4()) {
            try {

                encryptedDbFile = SQLiteDatabase.openOrCreateDatabase(encryptedDbFileV2, Prefs.getFullDbEncryptionKey(), null, null, hook);
                PreferencesHelper.setIsMigratedToSql4Key(true);
            } catch (Exception e) {
                encryptedDbFile = SQLiteDatabase.openOrCreateDatabase(encryptedDbFileV2, Prefs.getFullDbEncryptionKey(), null,null);
            }

        } else {
            encryptedDbFile = SQLiteDatabase.openOrCreateDatabase(encryptedDbFileV2, Prefs.getFullDbEncryptionKey(),null, null);

        }

        // Open Cipher encrypted database to get its version
        int encryptedDbVersion = encryptedDbFile.getVersion();
        encryptedDbFile.close();

        if (encryptedDbVersion == 0) {
            encryptedDbVersion = 1;
        }

        SQLiteOpenHelperCipher sqliteOpenHelperCipher = new SQLiteOpenHelperCipher(encryptedDbVersion, DB_NAME_ENC2, Prefs.getFullDbEncryptionKey());
        SQLiteDatabase encryptedDb = sqliteOpenHelperCipher.getWritableDatabase();

        // turn of the cipher_memory_security, very slow otherwise
        encryptedDb.execSQL("PRAGMA cipher_memory_security = OFF");

        return encryptedDb;
    }


    private void upgradeEncryptedDbFileFromV1ToV2() {

        SQLiteDatabase cipherEncryptedDbV1 = SQLiteDatabase.openOrCreateDatabase(encryptedDbFileV1, MyDevice.getInstance().deviceUid, null, null, hook);

        // If Cipher database is encrypted, its password can be changed
        cipherEncryptedDbV1.changePassword(Prefs.getFullDbEncryptionKey());
        cipherEncryptedDbV1.close();

        // Rename database file
        try {
            FileUtils.moveFile(encryptedDbFileV1, encryptedDbFileV2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String singleValueFromQuery(SQLiteDatabase database, String query) {
        Cursor cursor = database.rawQuery(query, new String[]{});
        String value = "";
        if (cursor != null) {
            cursor.moveToFirst();
            value = cursor.getString(0);
            cursor.close();
        }
        return value;
    }

}
