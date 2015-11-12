package com.wavlite.WLAndroid;

import java.util.Date;

/**
 * Created by javen on 11/10/15.
 */
public class TrialPeriodTimer  {

    private long startDate;
    private long enddate;
    private boolean doesHaveSpecialPermission;


    public boolean getDoesHaveSpecialPermission() {
        return doesHaveSpecialPermission;
    }

    public void setDoesHaveSpecialPermission(boolean doesHaveSpecialPermission) {
        this.doesHaveSpecialPermission = doesHaveSpecialPermission;
    }

    public long getEnddate() {
        return enddate;
    }

    public void setStartDate(Date startDate) {

        this.startDate = startDate.getTime();

    }
    public void setEndDate(Date startDate) {

        enddate = startDate.getTime() + 691200000;
    }
}
