package com.sandstorm.diary.piceditor.features.crop.adapter;

import com.steelkiwi.cropiwa.AspectRatio;

public class AspectRatioCustom extends AspectRatio {

    private final int selectedIem;
    private final int unselectItem;

    public AspectRatioCustom(int w, int h, int i3, int i4) {
        super(w, h);
        this.selectedIem = i4;
        this.unselectItem = i3;
    }

    public int getSelectedIem() {
        return this.selectedIem;
    }

    public int getUnselectItem() {
        return this.unselectItem;
    }
}
