package com.sandstorm.diary.piceditor.features.collage.layout.slant;

import com.sandstorm.diary.piceditor.features.collage.Line;
import com.sandstorm.diary.piceditor.features.collage.PuzzleLayout;
import com.sandstorm.diary.piceditor.features.collage.slant.SlantPuzzleLayout;

public class TwoSlantLayout extends NumberSlantLayout {
    public int getThemeCount() {
        return 2;
    }


    public TwoSlantLayout(SlantPuzzleLayout slantPuzzleLayout, boolean z) {
        super(slantPuzzleLayout, z);
    }

    public TwoSlantLayout(int i) {
        super(i);
    }

    public void layout() {
        switch (this.theme) {
            case 0:
                addLine(0, Line.Direction.HORIZONTAL, 0.56f, 0.44f);
                return;
            case 1:
                addLine(0, Line.Direction.VERTICAL, 0.56f, 0.44f);
                return;
            default:
                return;
        }
    }

    public PuzzleLayout clone(PuzzleLayout puzzleLayout) {
        return new TwoSlantLayout((SlantPuzzleLayout) puzzleLayout, true);
    }
}
