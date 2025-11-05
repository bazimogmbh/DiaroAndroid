package com.pixelcrater.Diaro.entries.attachments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.AppCompatTextView;

public class EntryPhotoPagerIndicator extends AppCompatTextView implements ViewPager.OnPageChangeListener {

    private ViewPager mViewPager;
    private int mNumberOfItems;
    private int mActiveItem;

    public EntryPhotoPagerIndicator(Context context) {
        super(context);
    }

    public EntryPhotoPagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EntryPhotoPagerIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @SuppressLint("SetTextI18n")
    public void setActiveItem(int activeItem) {
        mActiveItem = activeItem;
        this.setText("" + activeItem + "/" + mNumberOfItems);
    }

    public void setViewPager(ViewPager viewPager) {
        mViewPager = viewPager;
        mNumberOfItems = viewPager.getAdapter().getCount();
        setActiveItem(viewPager.getCurrentItem() + 1);
        mViewPager.addOnPageChangeListener(this);
        if (mNumberOfItems == 0) {
            this.setVisibility(GONE);
        } else {
            this.setVisibility(VISIBLE);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        setActiveItem(position + 1);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
