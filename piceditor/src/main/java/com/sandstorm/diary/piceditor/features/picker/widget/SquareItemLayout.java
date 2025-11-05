package com.sandstorm.diary.piceditor.features.picker.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class SquareItemLayout extends RelativeLayout {

    public SquareItemLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public SquareItemLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public SquareItemLayout(Context context) {
        super(context);
    }


    public void onMeasure(int i, int i2) {
        setMeasuredDimension(getDefaultSize(0, i), getDefaultSize(0, i2));
        int makeMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), 1073741824);
        super.onMeasure(makeMeasureSpec, makeMeasureSpec);
    }
}
