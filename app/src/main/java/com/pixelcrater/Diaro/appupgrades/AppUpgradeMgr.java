package com.pixelcrater.Diaro.appupgrades;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.storage.sqlite.upgrade.SQLiteUpgrade_335;
import com.pixelcrater.Diaro.storage.sqlite.upgrade.SQLiteUpgrade_354;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.storage.sqlite.upgrade.SQLiteUpgrade_110;
import com.pixelcrater.Diaro.storage.sqlite.upgrade.SQLiteUpgrade_118;
import com.pixelcrater.Diaro.storage.sqlite.upgrade.SQLiteUpgrade_120;
import com.pixelcrater.Diaro.storage.sqlite.upgrade.SQLiteUpgrade_189;
import com.pixelcrater.Diaro.storage.sqlite.upgrade.SQLiteUpgrade_225;
import com.pixelcrater.Diaro.storage.sqlite.upgrade.SQLiteUpgrade_25;
import com.pixelcrater.Diaro.storage.sqlite.upgrade.SQLiteUpgrade_251;
import com.pixelcrater.Diaro.storage.sqlite.upgrade.SQLiteUpgrade_60;
import com.pixelcrater.Diaro.storage.sqlite.upgrade.SQLiteUpgrade_76;
import com.pixelcrater.Diaro.utils.AppLog;

public class AppUpgradeMgr {

    public AppUpgradeMgr() {
        int appVersionCode = Static.getAppVersionCode();
        AppLog.i("appVersionCode: " + appVersionCode + ", getDbVersion(): " + getDbVersion());

        // NOTE: SQLite database is not upgraded to the latest version yet

        // If it's not the first app launch after installation when database is created
        if (getDbVersion() > 1) {
            if (getDbVersion() < appVersionCode) {
                if (getDbVersion() < 25) {
                    beginTransaction();
                    new SQLiteUpgrade_25();
                    endTransaction();
                    setDbVersion(25);
                }

                if (getDbVersion() < 50) {
                    new AppUpgrade_50();
                    setDbVersion(50);
                }

                if (getDbVersion() < 58) {
                    new AppUpgrade_58();
                    setDbVersion(58);
                }

                if (getDbVersion() < 60) {
                    beginTransaction();
                    new SQLiteUpgrade_60();
                    endTransaction();
                    setDbVersion(60);
                }

                if (getDbVersion() < 61) {
                    new AppUpgrade_61();
                    setDbVersion(61);
                }

                if (getDbVersion() < 76) {
                    beginTransaction();
                    new SQLiteUpgrade_76();
                    endTransaction();
                    setDbVersion(76);
                }

                if (getDbVersion() < 94) {
                    new AppUpgrade_94();
                    setDbVersion(94);
                }

                if (getDbVersion() < 97) {
                    new AppUpgrade_97();
                    setDbVersion(97);
                }

                if (getDbVersion() < 110) {
                    beginTransaction();
                    new SQLiteUpgrade_110();
                    endTransaction();
                    setDbVersion(110);
                }

                if (getDbVersion() < 111) {
                    new AppUpgrade_111();
                    setDbVersion(111);
                }

                if (getDbVersion() < 118) {
                    beginTransaction();
                    new SQLiteUpgrade_118();
                    endTransaction();
                    setDbVersion(118);
                }

                if (getDbVersion() < 119) {
                    new AppUpgrade_119();
                    setDbVersion(119);
                }

                if (getDbVersion() < 120) {
                    beginTransaction();
                    new SQLiteUpgrade_120();
                    endTransaction();
                    setDbVersion(120);
                }

                if (getDbVersion() < 189) {
                    beginTransaction();
                    new SQLiteUpgrade_189();
                    endTransaction();
                    setDbVersion(189);
                }

                if (getDbVersion() < 190) {
                    new AppUpgrade_190();
                    setDbVersion(190);
                }

                if (getDbVersion() < 209) {
                    new AppUpgrade_209();
                    setDbVersion(209);
                }

                if (getDbVersion() < 225) {
                    new SQLiteUpgrade_225();
                    setDbVersion(225);
                }

                if (getDbVersion() < 251) {
                    beginTransaction();
                    new SQLiteUpgrade_251();
                    endTransaction();
                    setDbVersion(251);
                }

               if (getDbVersion() < 335) {
                    beginTransaction();
                    new SQLiteUpgrade_335();
                    endTransaction();
                    setDbVersion(335);
                }

                if (getDbVersion() < 354) {
                    beginTransaction();
                    new SQLiteUpgrade_354();
                    endTransaction();
                    setDbVersion(354);
                }


                AppLog.d("App upgrade successful");
            }
        }

        if (getDbVersion() < appVersionCode) {
            // Set database version to app versionCode
            setDbVersion(appVersionCode);
        }

        AppLog.d("getDbVersion(): " + getDbVersion());
    }

    private void beginTransaction() {
        MyApp.getInstance().storageMgr.getSQLiteAdapter().mySQLiteWrapper.beginTransaction();
    }

    private void endTransaction() {
        MyApp.getInstance().storageMgr.getSQLiteAdapter().mySQLiteWrapper.setTransactionSuccessful();
        MyApp.getInstance().storageMgr.getSQLiteAdapter().mySQLiteWrapper.endTransaction();
    }

    private int getDbVersion() {
        return MyApp.getInstance().storageMgr.getSQLiteAdapter().mySQLiteWrapper.getDbVersion();
    }

    private void setDbVersion(int newVersion) {
        MyApp.getInstance().storageMgr.getSQLiteAdapter().mySQLiteWrapper.setDbVersion(newVersion);
        // It's needed to reopen SQLite database when its structure changes
        MyApp.getInstance().storageMgr.resetSQLiteAdapter();
    }
}
