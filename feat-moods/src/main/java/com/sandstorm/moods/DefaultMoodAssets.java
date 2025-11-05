package com.sandstorm.moods;

import com.sandstorm.diary.moods.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * mood_1happy ( identifier)  is the asset id, shared across all platform.
 * mood_1happy is stored in db as image identifier & fetch respective vector drawable for it
 */
public enum DefaultMoodAssets {

    // Default 5 moods with titles
    mood_1happy("mood_1happy", R.drawable.mood_1happy),
    mood_2smile("mood_2smile", R.drawable.mood2_smile),
    mood_3neutral("mood_3neutral", R.drawable.mood3_neutral),
    mood_4unhappy("mood_4unhappy", R.drawable.mood_4unhappy),
    mood_5teardrop("mood_5teardrop", R.drawable.mood_5teardrop),

    //Icons for add/ edit moods
    amazed("mood_amazed", R.drawable.mood_amazed),
    angry("mood_angry", R.drawable.mood_angry),
    bleh("mood_bleh", R.drawable.mood_bleh),
    cash("mood_cash", R.drawable.mood_cash),
    cheeky("mood_cheeky", R.drawable.mood_cheeky),
    confused("mood_confused", R.drawable.mood_confused),
    cool("mood_cool", R.drawable.mood_cool),
    crazy("mood_crazy", R.drawable.mood_crazy),
    cryover("mood_cryover", R.drawable.mood_cryover),
    dead("mood_dead", R.drawable.mood_dead),
    disgusted("mood_disgusted", R.drawable.mood_disgusted),
    dizzy("mood_dizzy", R.drawable.mood_dizzy),
    doubt("mood_doubt", R.drawable.mood_doubt),
    grinning("mood_grinning", R.drawable.mood_grinning),
    happy1("mood_happy1", R.drawable.mood_happy1),
    happy2("mood_happy2", R.drawable.mood_happy2),
    hungry("mood_hungry", R.drawable.mood_hungry),
    inlove("mood_inlove", R.drawable.mood_inlove),
    kiss("mood_kiss", R.drawable.mood_kiss),
    mask("mood_mask", R.drawable.mood_mask),
    muted("mood_muted", R.drawable.mood_muted),
    nauseous("mood_nauseous", R.drawable.mood_nauseous),
    nerd("mood_nerd", R.drawable.mood_nerd),
    ohno("mood_ohno", R.drawable.mood_ohno),
    pensive("mood_pensive", R.drawable.mood_pensive),
    proud("mood_proud", R.drawable.mood_proud),
    satisfied("mood_satisfied", R.drawable.mood_satisfied),
    serious("mood_serious", R.drawable.mood_serious),
    shocked("mood_shocked", R.drawable.mood_shocked),
    shy("mood_shy", R.drawable.mood_shy),
    silly("mood_silly", R.drawable.mood_silly),
    sad("mood_sad", R.drawable.mood_sad),
    sleepy("mood_sleepy", R.drawable.mood_sleepy),
    sosad("mood_sosad", R.drawable.mood_sosad),
    stupid("mood_stupid", R.drawable.mood_stupid),
    surprised("mood_surprised", R.drawable.mood_surprised),
    thankful("mood_thankful", R.drawable.mood_thankful),
    tired("mood_tired", R.drawable.mood_tired),
    very_angry("mood_very_angry", R.drawable.mood_very_angry),
    very_happy("mood_very_happy", R.drawable.mood_very_happy),
    wink("mood_wink", R.drawable.mood_wink),
    worried("mood_worried", R.drawable.mood_worried),
    ;

    String identifier;
    int icon_resId;

    DefaultMoodAssets(String identifier, int icon_resId) {
        this.identifier = identifier;
        this.icon_resId = icon_resId;
    }

    public int getIconRes() {
        return icon_resId;
    }

    public String getIdentifier(){
        return identifier;
    }


    private static final Map<String, DefaultMoodAssets> lookup = new HashMap<String, DefaultMoodAssets>();

    static {
        for (DefaultMoodAssets d : DefaultMoodAssets.values()) {
            lookup.put(d.identifier, d);
        }
    }

    public static DefaultMoodAssets getByIconIdentifier(String identifier) {
        return lookup.get(identifier);
    }

    public static int getIcon(String identifier) {
        return Objects.requireNonNull(lookup.get(identifier)).icon_resId;
    }

}



