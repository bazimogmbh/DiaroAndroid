package com.pixelcrater.Diaro.calendar;

import org.joda.time.DateTime;

import java.util.ArrayList;

public class DayMarker {

    public DateTime dayDt;
    public ArrayList<String> dayFolderColorsArrayList;
    public int dayPhotoCount = 0;

    public DayMarker(DateTime dayDt, ArrayList<String> dayFolderColorsArrayList, int dayPhotoCount) {
        this.dayDt = dayDt;
        this.dayFolderColorsArrayList = dayFolderColorsArrayList;
        this.dayPhotoCount = dayPhotoCount;
    }
}
