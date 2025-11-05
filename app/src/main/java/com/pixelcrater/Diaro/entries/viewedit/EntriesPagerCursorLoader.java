package com.pixelcrater.Diaro.entries.viewedit;

import android.content.Context;
import android.database.Cursor;
import androidx.loader.content.CursorLoader;
import androidx.core.util.Pair;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.entries.EntriesStatic;
import com.pixelcrater.Diaro.storage.OnStorageDataChangeListener;

public class EntriesPagerCursorLoader extends CursorLoader implements OnStorageDataChangeListener {
    private EntryViewEditActivity entryViewEditActivity;

    public EntriesPagerCursorLoader(Context context) {
        super(context);

        entryViewEditActivity = (EntryViewEditActivity) context;

        MyApp.getInstance().storageMgr.addOnStorageDataChangeListener(this);
    }

    @Override
    protected void onReset() {
        super.onReset();

        MyApp.getInstance().storageMgr.removeOnStorageDataChangeListener(this);
    }

    @Override
    public Cursor loadInBackground() {
//		Static.makePause(1000);

        if (entryViewEditActivity.clickedEntryUid.equals("")) {
            return null;
        } else {
            // Include visible row to results
            String visibleRowUid = null;
            EntryFragment currentEntryFragment = entryViewEditActivity.getCurrentEntryFragment();
            if (currentEntryFragment != null) {
                visibleRowUid = currentEntryFragment.rowUid;
            }

            Pair<String, String[]> pair = EntriesStatic.getEntriesAndSqlByActiveFilters(visibleRowUid);
            Cursor cursor = MyApp.getInstance().storageMgr.getSQLiteAdapter().getEntriesCursorUidsOnly(pair.first, pair.second);

            // Execute SQLite query by accessing the cursor
            cursor.getCount();

            return cursor;
        }
    }

    @Override
    public void onStorageDataChange() {
//        AppLog.d("");
        onContentChanged();
    }
}
