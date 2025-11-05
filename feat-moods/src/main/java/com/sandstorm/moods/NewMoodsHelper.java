package com.sandstorm.moods;

import android.content.Context;

import com.sandstorm.diary.moods.R;

import java.util.ArrayList;
import java.util.List;

public class NewMoodsHelper {


    private List<NewMood> getDefaultMoods(Context ctx) {

        ArrayList<NewMood> retVal = new ArrayList();
        String defaultMoodColor = "#9e9e9e";

        retVal.add(new NewMood("1", ctx.getString(R.string.mood_1), "mood_1happy", defaultMoodColor, 5));
        retVal.add(new NewMood("2", ctx.getString(R.string.mood_2), "mood_2smile", defaultMoodColor, 4));
        retVal.add(new NewMood("3", ctx.getString(R.string.mood_3), "mood_3neutral", defaultMoodColor, 3));
        retVal.add(new NewMood("4", ctx.getString(R.string.mood_4), "mood_4unhappy", defaultMoodColor, 2));
        retVal.add(new NewMood("5", ctx.getString(R.string.mood_5), "mood_5teardrop", defaultMoodColor, 1));

        return retVal;
    }




}

