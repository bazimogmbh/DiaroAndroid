package com.sandstorm.diary.piceditor.utils;


import com.sandstorm.diary.piceditor.features.collage.PuzzleLayout;
import com.sandstorm.diary.piceditor.features.collage.layout.slant.SlantLayoutHelper;
import com.sandstorm.diary.piceditor.features.collage.layout.straight.StraightLayoutHelper;

import java.util.ArrayList;
import java.util.List;

public class CollageUtils {

    private CollageUtils() {
    }

    public static List<PuzzleLayout> getPuzzleLayouts(int i) {
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(SlantLayoutHelper.getAllThemeLayout(i));
        arrayList.addAll(StraightLayoutHelper.getAllThemeLayout(i));
        return arrayList;
    }
}
