package com.pixelcrater.Diaro.utils;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.viewpager.widget.ViewPager;

public class CustomViewPager extends ViewPager {

    private boolean swipingEnabled;

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        swipingEnabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (swipingEnabled) {
            return super.onTouchEvent(event);
        }

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (swipingEnabled) {
            return super.onInterceptTouchEvent(event);
        }

        return false;
    }

    public void setSwipingEnabled(boolean value) {
        AppLog.d("value: " + value);
        swipingEnabled = value;
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        return super.canScroll(v, checkV, dx, x, y);
    }
}
