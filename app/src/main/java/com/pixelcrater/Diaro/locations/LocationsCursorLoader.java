package com.pixelcrater.Diaro.locations;

import android.content.Context;
import android.database.Cursor;

import androidx.loader.content.CursorLoader;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.storage.OnStorageDataChangeListener;
import com.pixelcrater.Diaro.storage.Tables;

public class LocationsCursorLoader extends CursorLoader implements OnStorageDataChangeListener {

    private final String mDialogSearchKeyword;

    public LocationsCursorLoader(Context context, String dialogSearchKeyword) {
        super(context);
//        AppLog.d("Loader instance: " + this);

        mDialogSearchKeyword = dialogSearchKeyword;
        MyApp.getInstance().storageMgr.addOnStorageDataChangeListener(this);
    }

    @Override
    protected void onReset() {
        super.onReset();
//        AppLog.d("Loader instance: " + this);

        MyApp.getInstance().storageMgr.removeOnStorageDataChangeListener(this);
    }

    @Override
    public Cursor loadInBackground() {
//        Static.makePause(1000);
//        AppLog.d("Loader instance: " + this + ", mDialogSearchKeyword: " + mDialogSearchKeyword);

        String andSql = "";
        String[] whereArgs = null;

        // Filter folders by dialog search keyword
        if (!mDialogSearchKeyword.equals("")) {
            andSql = "AND l." + Tables.KEY_LOCATION_TITLE + "||l." + Tables.KEY_LOCATION_ADDRESS + " LIKE ?";
            whereArgs = new String[1];
            whereArgs[0] = "%" + mDialogSearchKeyword + "%";
        }

       // long startMs = android.os.SystemClock.uptimeMillis();

        Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getLocationsCursor(andSql, whereArgs, true);
        // Execute SQLite query by accessing the cursor
        cursor.getCount();

        //long endMs = android.os.SystemClock.uptimeMillis();
       // AppLog.d("duration: " + (endMs - startMs) + " ms");

        return cursor;
    }

    @Override
    public void onStorageDataChange() {
        onContentChanged();
    }
}
