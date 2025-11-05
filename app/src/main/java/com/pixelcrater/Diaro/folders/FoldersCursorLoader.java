package com.pixelcrater.Diaro.folders;

import android.content.Context;
import android.database.Cursor;
import androidx.loader.content.CursorLoader;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.storage.OnStorageDataChangeListener;
import com.pixelcrater.Diaro.storage.Tables;

public class FoldersCursorLoader extends CursorLoader implements OnStorageDataChangeListener {

    private final String mDialogSearchKeyword;

    public FoldersCursorLoader(Context context, String dialogSearchKeyword) {
        super(context);
        mDialogSearchKeyword = dialogSearchKeyword;
        MyApp.getInstance().storageMgr.addOnStorageDataChangeListener(this);
    }

    @Override
    protected void onReset() {
        super.onReset();
        MyApp.getInstance().storageMgr.removeOnStorageDataChangeListener(this);
    }

    @Override
    public Cursor loadInBackground() {
        String andSql = "";
        String[] whereArgs = null;

        // Filter folders by dialog search keyword
        if (!mDialogSearchKeyword.equals("")) {
            andSql = "AND f." + Tables.KEY_FOLDER_TITLE + " LIKE ?";
            whereArgs = new String[1];
            whereArgs[0] = "%" + mDialogSearchKeyword + "%";
        }

        Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getFoldersCursor( andSql, whereArgs, true);

        // Execute SQLite query by accessing the cursor
        cursor.getCount();

        return cursor;
    }

    @Override
    public void onStorageDataChange() {
        onContentChanged();
    }
}
