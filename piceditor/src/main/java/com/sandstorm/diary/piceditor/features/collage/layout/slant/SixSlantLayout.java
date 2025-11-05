package com.sandstorm.diary.piceditor.features.collage.layout.slant;


import com.sandstorm.diary.piceditor.features.collage.Line;
import com.sandstorm.diary.piceditor.features.collage.PuzzleLayout;
import com.sandstorm.diary.piceditor.features.collage.slant.SlantPuzzleLayout;

public class SixSlantLayout extends NumberSlantLayout {
    public int getThemeCount() {
        return 2;
    }

    public SixSlantLayout(SlantPuzzleLayout slantPuzzleLayout, boolean z) {
        super(slantPuzzleLayout, z);
    }

    public SixSlantLayout(int i) {
        super(i);
    }

    public void layout() {
        switch (this.theme) {
            case 0:
                addLine(0, Line.Direction.VERTICAL, 0.33333334f);
                addLine(1, Line.Direction.VERTICAL, 0.5f);
                addLine(0, Line.Direction.HORIZONTAL, 0.7f, 0.7f);
                addLine(1, Line.Direction.HORIZONTAL, 0.5f, 0.5f);
                addLine(2, Line.Direction.HORIZONTAL, 0.3f, 0.3f);
                return;
            case 1:
                addLine(0, Line.Direction.HORIZONTAL, 0.33333334f);
                addLine(1, Line.Direction.HORIZONTAL, 0.5f);
                addLine(0, Line.Direction.VERTICAL, 0.3f, 0.3f);
                addLine(2, Line.Direction.VERTICAL, 0.5f, 0.5f);
                addLine(4, Line.Direction.VERTICAL, 0.7f, 0.7f);
                return;
            default:
                return;
        }
    }

    public PuzzleLayout clone(PuzzleLayout puzzleLayout) {
        return new SixSlantLayout((SlantPuzzleLayout) puzzleLayout, true);
    }
}
