package com.pixelcrater.Diaro.export;

import java.util.ArrayList;

public class ExportEntry {

    public String uid;
    public String title;
    public String text;

    public String folder_title;
    public String folder_color;

    public String tags;
    public boolean hasMood = false;
    public int mood;
    public String moodTitle;
    public String moodIcon;
    public String location;

    public ArrayList<String> attachmentsList = new ArrayList<>();

    public int day;
    public String day_of_week_full;
    public String month_name;
    public String time;
    public int year;

    public String unit_name;
    public String weather_description_display;
    public String weather_icon;
    public String weather_temperature_display;

}
