package com.pixelcrater.Diaro.storage.dropbox;

import com.dropbox.core.v2.users.FullAccount;
import com.dropbox.core.v2.users.SpaceUsage;

import java.text.DecimalFormat;

/**
 * Created by abhishek9851 on 11.05.17.
 */

public class DbxUserInfo {

    public SpaceUsage mSpaceUsage;
    public FullAccount mFullaccount;
    public String mUsageInfo;
    public double mUsagePercentage = 1;
    public Exception mException = null;

    private DecimalFormat formatter = new DecimalFormat("#.#");

    public DbxUserInfo(SpaceUsage spaceUsage, FullAccount fullaccount) {
        this.mSpaceUsage = spaceUsage;
        this.mFullaccount = fullaccount;
        // this.mUsageInfo =( mSpaceUsage.getUsed()/1000000000 ) +  " gb of " + (mSpaceUsage.getAllocation().getIndividualValue().getAllocated() /1000000000 ) + "";

        double spaceTotal = 1;

        if (mSpaceUsage.getAllocation().isIndividual()) {
            mUsagePercentage = ((double)mSpaceUsage.getUsed() * 100 / mSpaceUsage.getAllocation().getIndividualValue().getAllocated());
            spaceTotal = ((double)mSpaceUsage.getAllocation().getIndividualValue().getAllocated() / 1073741824);
        }
        if (mSpaceUsage.getAllocation().isTeam()) {
            mUsagePercentage = ((double)mSpaceUsage.getUsed() * 100 / mSpaceUsage.getAllocation().getTeamValue().getAllocated());
            spaceTotal = ((double)mSpaceUsage.getAllocation().getTeamValue().getAllocated() / 1073741824);
        }
        if (mSpaceUsage.getAllocation().isOther()) {
        }

        this.mUsageInfo = formatter.format(mUsagePercentage) + "% of " + formatter.format(spaceTotal) + " GB";
    }


    public FullAccount getFullaccount() {
        return mFullaccount;
    }


    public String getUsageInfo() {
        return mUsageInfo;

    }

    public double getUsagePercentage() {
        return (double) mUsagePercentage;
    }
}