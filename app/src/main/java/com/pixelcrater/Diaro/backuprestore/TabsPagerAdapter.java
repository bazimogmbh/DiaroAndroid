package com.pixelcrater.Diaro.backuprestore;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;

public class TabsPagerAdapter extends FragmentStatePagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {
        return BackupRestoreTabFragment.newInstance(index);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return MyApp.getInstance().getString(R.string.sdcard);
        } else {
            return MyApp.getInstance().getString(R.string.dropbox);
        }
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 2;
    }
}
