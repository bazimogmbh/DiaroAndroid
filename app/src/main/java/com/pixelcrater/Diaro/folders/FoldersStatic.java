package com.pixelcrater.Diaro.folders;

import android.content.ContentValues;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.storage.Tables;

public class FoldersStatic {

    public static void deleteFolderInBackground(final String folderUid) {
        MyApp.executeInBackground(() -> {
            MyApp.getInstance().storageMgr.deleteRowByUid(Tables.TABLE_FOLDERS, folderUid);

            // Clear folder_uid for entries
            ContentValues cv = new ContentValues();
            cv.put(Tables.KEY_ENTRY_FOLDER_UID, "");

            String[] whereArgs = new String[1];
            whereArgs[0] = folderUid;

            MyApp.getInstance().storageMgr.updateRows(Tables.TABLE_ENTRIES,"WHERE " + Tables.KEY_ENTRY_FOLDER_UID + "=?", whereArgs, cv);

        });
    }
}
