package com.pixelcrater.Diaro.layouts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import androidx.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.EditText;

public class LinedEditText extends EditText {

    private Rect mRect;
    private Paint mPaint;

    public LinedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        setTextAppearance(context, android.R.style.TextAppearance);
        setGravity(Gravity.NO_GRAVITY);

        mRect = new Rect();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setARGB(15, 0, 0, 0);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        int height = getHeight();
        int line_height = getLineHeight();

        int count = height / line_height;

        if (getLineCount() > count)
            count = getLineCount();

        Rect r = mRect;
        Paint paint = mPaint;

        int baseline = getLineBounds(0, r); //first line
        for (int i = 0; i < count; i++) {

          //  canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
            baseline += getLineHeight(); //next line
        }

        super.onDraw(canvas);
    }
}
