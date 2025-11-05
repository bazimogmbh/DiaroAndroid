package com.pixelcrater.Diaro.moods

import com.pixelcrater.Diaro.R

class Mood(var id: Int) {

    val moodTextResId: Int
        get() {
            var retVal = 0
            if (id == MOOD_NONE) {
                retVal = R.string.mood_none
            }
            if (id == MOOD_AWESOME_ID) {
                retVal = R.string.mood_1
            }
            if (id == MOOD_HAPPY_ID) {
                retVal = R.string.mood_2
            }
            if (id == MOOD_NEUTRAL_ID) {
                retVal = R.string.mood_3
            }
            if (id == MOOD_BAD_ID) {
                retVal = R.string.mood_4
            }
            if (id == MOOD_AWFUL_ID) {
                retVal = R.string.mood_5
            }
            return retVal
        }

    val fontResId: Int
        get() {
            var retVal = 0
            if (id == MOOD_AWESOME_ID) {
                retVal = MOOD_AWESOME_RES_ID
            }
            if (id == MOOD_HAPPY_ID) {
                retVal = MOOD_HAPPY_RES_ID
            }
            if (id == MOOD_NEUTRAL_ID) {
                retVal = MOOD_NEUTRAL_RES_ID
            }
            if (id == MOOD_BAD_ID) {
                retVal = MOOD_BAD_RES_ID
            }
            if (id == MOOD_AWFUL_ID) {
                retVal = MOOD_AWFUL_RES_ID
            }
            return retVal
        }

    companion object {

        const val MOOD_NONE = 0
        const val MOOD_AWESOME_ID = 1
        const val MOOD_HAPPY_ID = 2
        const val MOOD_NEUTRAL_ID = 3
        const val MOOD_BAD_ID = 4
        const val MOOD_AWFUL_ID = 5

        const val MOOD_AWESOME_RES_ID = R.string.mood_1happy
        const val MOOD_HAPPY_RES_ID = R.string.mood_2smile
        const val MOOD_NEUTRAL_RES_ID = R.string.mood_3neutral
        const val MOOD_BAD_RES_ID = R.string.mood_4unhappy
        const val MOOD_AWFUL_RES_ID = R.string.mood_5teardrop

        fun getMoodResIdbyIcon(icon: String): Int {
            if (icon.compareTo("mood_1happy") == 0) return R.string.mood_1happy
            if (icon.compareTo("mood_2smile") == 0) return R.string.mood_2smile
            if (icon.compareTo("mood_3neutral") == 0) return R.string.mood_3neutral
            if (icon.compareTo("mood_4unhappy") == 0) return R.string.mood_4unhappy
            return if (icon.compareTo("mood_5teardrop") == 0) R.string.mood_5teardrop else 0


        }
    }

    /**
     * Great (motivated, silly, euphoric
    Happy (content, productive,
    neutral (steady, peaceful, uneventful)
    Bad (sad, tired, sick, unmotivated...)
    Awful (depressed, frustrated, anxious, furious)
     */


}