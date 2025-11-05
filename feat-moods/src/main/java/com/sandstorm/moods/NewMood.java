package com.sandstorm.moods;

public class NewMood {

    public String uid;
    public String title;
    public String icon;
    public String color;
    private int weight; // user_order

    public NewMood(String uid, String title, String icon, String color, int weight ) {
        this.uid = uid;
        this.title = title;
        this.icon = icon;
        this.color = color;
        this.weight = weight;
    }


}
