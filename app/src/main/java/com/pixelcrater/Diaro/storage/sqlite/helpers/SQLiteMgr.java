package com.pixelcrater.Diaro.storage.sqlite.helpers;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.settings.PreferencesHelper;
import com.pixelcrater.Diaro.config.Prefs;
import com.pixelcrater.Diaro.utils.MyDevice;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;

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
        public void preKey(SQLiteDatabase database) {
        }

        public void postKey(SQLiteDatabase database) {
           // AppLog.e("Migrating.....");
            singleValueFromQuery(database, "PRAGMA cipher_migrate");
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

    public net.sqlcipher.database.SQLiteDatabase getCipherEncryptedDb() {

        // upgrade the old db first , if it exists
        if (encryptedDbFileV1.exists()) {
            upgradeEncryptedDbFileFromV1ToV2();
        }

        //Prevent crash on marshmellow
        if (!encryptedDbFileV2.exists() || encryptedDbFileV2.length() == 0) {
            encryptedDbFileV2.mkdirs();
            encryptedDbFileV2.delete();
        }


        net.sqlcipher.database.SQLiteDatabase encryptedDbFile;

     //   AppLog.e("db path ->" + encryptedDbFileV2.getAbsolutePath());

        if (!PreferencesHelper.isMigratedToSql4()) {
            try {
                encryptedDbFile = net.sqlcipher.database.SQLiteDatabase.openOrCreateDatabase(encryptedDbFileV2, Prefs.getFullDbEncryptionKey(), null, hook);
                PreferencesHelper.setIsMigratedToSql4Key(true);
            } catch (Exception e) {
                encryptedDbFile = net.sqlcipher.database.SQLiteDatabase.openOrCreateDatabase(encryptedDbFileV2, Prefs.getFullDbEncryptionKey(), null);
            }

        } else {
            encryptedDbFile = net.sqlcipher.database.SQLiteDatabase.openOrCreateDatabase(encryptedDbFileV2, Prefs.getFullDbEncryptionKey(), null);

        }

        // Open Cipher encrypted database to get its version
        int encryptedDbVersion = encryptedDbFile.getVersion();
        encryptedDbFile.close();

        if (encryptedDbVersion == 0) {
            encryptedDbVersion = 1;
        }

        SQLiteOpenHelperCipher sqliteOpenHelperCipher = new SQLiteOpenHelperCipher(encryptedDbVersion, DB_NAME_ENC2);
        net.sqlcipher.database.SQLiteDatabase encryptedDb = sqliteOpenHelperCipher.getWritableDatabase(Prefs.getFullDbEncryptionKey());

        // turn of the cipher_memory_security, very slow otherwise
        encryptedDb.execSQL("PRAGMA cipher_memory_security = OFF");
        // encryptedDb.execSQL("VACUUM");
        //  AppLog.e("oath ->" + encryptedDbFileV2.getAbsolutePath());
        return encryptedDb;
    }


    private void upgradeEncryptedDbFileFromV1ToV2() {

        SQLiteDatabase cipherEncryptedDbV1 = net.sqlcipher.database.SQLiteDatabase.openOrCreateDatabase(encryptedDbFileV1, MyDevice.getInstance().deviceUid, null, hook);

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
        net.sqlcipher.Cursor cursor = database.rawQuery(query, new String[]{});
        String value = "";
        if (cursor != null) {
            cursor.moveToFirst();
            value = cursor.getString(0);
            cursor.close();
        }
        return value;
    }

}
