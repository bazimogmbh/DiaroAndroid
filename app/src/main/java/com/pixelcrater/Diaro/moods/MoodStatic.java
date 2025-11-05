package com.pixelcrater.Diaro.moods;

import android.content.ContentValues;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.storage.Tables;

public class MoodStatic {

    public static void deleteMoodInBackground(final String moodUid) {
        MyApp.executeInBackground(() -> {
            MyApp.getInstance().storageMgr.deleteRowByUid(Tables.TABLE_MOODS, moodUid);

            // Clear mood for entries
            ContentValues cv = new ContentValues();
            cv.put(Tables.KEY_ENTRY_MOOD_UID, "");

            String[] whereArgs = new String[1];
            whereArgs[0] = moodUid;

            MyApp.getInstance().storageMgr.updateRows(Tables.TABLE_ENTRIES,"WHERE " + Tables.KEY_ENTRY_MOOD_UID + "=?", whereArgs, cv);

        });
    }
}
