package com.pixelcrater.Diaro.entries.viewedit;

import android.content.Context;
import android.database.Cursor;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.ViewGroup;

import com.pixelcrater.Diaro.storage.Tables;

public abstract class CursorFragmentStatePagerAdapter extends MyFixedFragmentStatePagerAdapter {
    protected boolean mDataValid;
    protected Cursor mCursor;
    protected Context mContext;

    public CursorFragmentStatePagerAdapter(Context context, FragmentManager fm, Cursor cursor) {
        super(fm);

        init(context, cursor);
    }

    void init(Context context, Cursor c) {
        boolean cursorPresent = c != null;
        mCursor = c;
        mDataValid = cursorPresent;
        mContext = context;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    @Override
    public int getItemPosition(Object object) {
        EntryFragment entryFragment = (EntryFragment) object;
//		AppLog.d("entryFragment.rowUid: " + entryFragment.rowUid);

        if (entryFragment != null && entryFragment.rowUid != null) {
            int pos = findPositionByEntryUid(entryFragment.rowUid);
            if (pos != -1) {
                changeFragmentPosition(pos, entryFragment);
                return pos;
            }
        }
        return POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
//		AppLog.d("position: " + position);

        if (mDataValid) {
            mCursor.moveToPosition(position);
            return getItem(mContext, mCursor);
        } else {
            return null;
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//		AppLog.d("position: " + position);

        super.destroyItem(container, position, object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
//		AppLog.d("position: " + position);

        if (mCursor == null || !mCursor.moveToPosition(position)) {
            return super.instantiateItem(container, position);
        }

        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }

        Object obj = super.instantiateItem(container, position);
//		AppLog.d("entryFragment.rowUid: " + ((EntryFragment) obj).rowUid);

        return obj;
    }

    public abstract Fragment getItem(Context context, Cursor cursor);

    @Override
    public int getCount() {
        if (mDataValid) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    private Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        mCursor = newCursor;
        mDataValid = newCursor != null;

        if (mDataValid) {
            notifyDataSetChanged();
        }

        return oldCursor;
    }

    public int findPositionByEntryUid(String entryUid) {
//		AppLog.d("entryUid: " + entryUid);

        if (entryUid != null && mCursor != null) {
            mCursor.moveToPosition(-1);

            int entryUidColumnIndex = mCursor.getColumnIndex(Tables.KEY_UID);

            while (mCursor.moveToNext()) {
                if (mCursor.getString(entryUidColumnIndex).equals(entryUid)) {
//					AppLog.d("mCursor.getPosition(): " + mCursor.getPosition());
                    return mCursor.getPosition();
                }
            }
        }

        return -1;
    }
}
