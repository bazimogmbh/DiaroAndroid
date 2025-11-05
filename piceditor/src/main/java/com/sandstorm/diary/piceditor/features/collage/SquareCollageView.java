package com.sandstorm.diary.piceditor.features.collage;

import android.content.Context;
import android.util.AttributeSet;

public class SquareCollageView extends CollageView {
    public SquareCollageView(Context context) {
        super(context);
    }

    public SquareCollageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public SquareCollageView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }


    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        int measuredWidth = getMeasuredWidth();
        int measuredHeight = getMeasuredHeight();
        if (measuredWidth > measuredHeight) {
            measuredWidth = measuredHeight;
        }
        setMeasuredDimension(measuredWidth, measuredWidth);
    }
}
