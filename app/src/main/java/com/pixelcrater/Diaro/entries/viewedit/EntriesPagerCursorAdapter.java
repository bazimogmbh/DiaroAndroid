package com.pixelcrater.Diaro.entries.viewedit;

import android.content.Context;
import android.database.Cursor;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.pixelcrater.Diaro.storage.Tables;
import com.pixelcrater.Diaro.utils.AppLog;

public class EntriesPagerCursorAdapter extends CursorFragmentStatePagerAdapter {
    private EntryViewEditActivity entryViewEditActivity;

    public EntriesPagerCursorAdapter(Context context, FragmentManager fm, Cursor cursor) {
        super(context, fm, cursor);
        entryViewEditActivity = (EntryViewEditActivity) context;
    }

    @Override
    public Fragment getItem(int position) {
//		AppLog.d("position: " + position);

        if (entryViewEditActivity.clickedEntryUid.equals("")) {
            return EntryFragment.newInstance(null);
        }
        return super.getItem(position);
    }

    @Override
    public Fragment getItem(Context context, Cursor cursor) {
//		AppLog.d("cursor: " + cursor);

        if (cursor != null) {
//			AppLog.d("position: " + cursor.getPosition() + ", uid: " + cursor.getString(cursor.getColumnIndex(Tables.KEY_UID)));

            try {
                int index = cursor.getColumnIndex(Tables.KEY_UID);
                if (index != -1 && cursor.getColumnCount() > index) {
                    return EntryFragment.newInstance(cursor.getString(index));
                }
            } catch (Exception e) {
                AppLog.e("Exception: " + e);
            }
        }
        return EntryFragment.newInstance(null);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

        if (getCount() == 0 && entryViewEditActivity != null &&  !entryViewEditActivity.isFinishing()) {
            entryViewEditActivity.finish();
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return String.valueOf(position); //  + "-" + getTag(position)
    }

    @Override
    public int getCount() {
        if (entryViewEditActivity.clickedEntryUid.equals("")) {
            return 1;
        }
        return super.getCount();
    }

    @Override
    public String getTag(int position) {
//		AppLog.d("position: " + position + ", getCount(): " + getCount());
        return getItemUid(position);
    }

    private String getItemUid(int position) {
        if (entryViewEditActivity.clickedEntryUid.equals("")) {
            return null;
        }

        if (!mDataValid) {
            return null;
        }
        mCursor.moveToPosition(position);

        AppLog.d("mCursor.getColumnCount(): " + mCursor.getColumnCount());
        AppLog.d("mCursor.getCount(): " + mCursor.getCount());
        if (mCursor.getCount() == 0) {
            return null;
        }

        return mCursor.getString(mCursor.getColumnIndex(Tables.KEY_UID));
    }
}
