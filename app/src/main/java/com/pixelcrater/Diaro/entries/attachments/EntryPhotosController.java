package com.pixelcrater.Diaro.entries.attachments;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.model.AttachmentInfo;
import com.pixelcrater.Diaro.model.EntryInfo;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.MyThemesUtils;

import java.util.ArrayList;

public class EntryPhotosController implements EntryPhotosAdapter.ClickInterface {

    private CollapsingToolbarLayout mCollapsingLayout;
    private View pagerFill;
    private ViewPager mPager;
    private EntryPhotoPagerIndicator mIndicator;
    private Activity mContext;
    private View mLayout;
    private NestedScrollView mScrollView;
    private EntryPhotosCallbacks mCallbacks;
    private EntryPhotosAdapter mPagerAdapter;
    private boolean newPhotosAdded = false;

    public EntryPhotosController(int initialPhotoCount, View pagerLayout, NestedScrollView mainScrollView, Activity context, EntryPhotosCallbacks callbacks) {
        mContext = context;
        mCallbacks = callbacks;
        mLayout = pagerLayout;
        mLayout.setBackgroundColor(MyThemesUtils.getPrimaryColor());
        mCollapsingLayout = (CollapsingToolbarLayout) mLayout.findViewById(R.id.entry_collapsing_photo_Layout);
        mCollapsingLayout.setContentScrimColor(MyThemesUtils.getPrimaryColor());
        mLayout.setOnTouchListener(null);
        mCollapsingLayout.setOnTouchListener(null);
        mPager = (ViewPager) pagerLayout.findViewById(R.id.entry_photo_pager);
        mPager.setBackgroundColor(MyThemesUtils.getPrimaryColor());
        mIndicator = (EntryPhotoPagerIndicator) pagerLayout.findViewById(R.id.viewpager_indicator);
        mScrollView = mainScrollView;
        pagerFill = mLayout.findViewById(R.id.photo_pager_fill);
        pagerFill.setBackgroundColor(MyThemesUtils.getPrimaryColor());
        View gradientView = mLayout.findViewById(R.id.photo_pager_gradient);
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{MyThemesUtils.getPrimaryColorTransparent(), mContext.getResources().getColor(android.R.color.transparent)});
        gd.setCornerRadius(0f);
        gradientView.setBackground(gd);
        setInitialParamsByPhotoCount();
        if (initialPhotoCount == 0) {
            togglePagerVisibility(false);
        }
    }


    private void togglePagerVisibility(boolean setVisible) {
        if (setVisible) {
            mScrollView.setNestedScrollingEnabled(true);
        } else {
            collapseExpandLayout(false, false);
            mScrollView.setNestedScrollingEnabled(false);

        }
    }

    private void setInitialParamsByPhotoCount() {

        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();

        int width = metrics.widthPixels;
        int height = (int) ((double) width * Static.PHOTO_PROPORTION);

        if (Static.isLandscape()) {
            height = metrics.heightPixels / 2;
        }

        ViewGroup.LayoutParams params = mPager.getLayoutParams();
        params.height = height;
        mPager.setLayoutParams(params);
    }

    public void createPhotoList(ArrayList<AttachmentInfo> entryPhotosArrayList, EntryInfo entryInfo) {
        mPagerAdapter = new EntryPhotosAdapter(mContext, entryPhotosArrayList, this, entryInfo);
        mPager.setAdapter(mPagerAdapter);
        mIndicator.setViewPager(mPager);
    }

    public void createOrRefreshPhotoList(ArrayList<AttachmentInfo> entryPhotosArrayList, EntryInfo entryInfo) {
        if (entryPhotosArrayList == null) {
            return;
        }
        int photoCount = entryPhotosArrayList.size();
        if (mPagerAdapter == null) {
            createPhotoList(entryPhotosArrayList, entryInfo);
            if (entryPhotosArrayList.size() == 0) {
                setInitialParamsByPhotoCount();
                togglePagerVisibility(false);
                AppLog.d("onPhotoLayoutHeightChanged from: createOrRefreshPhotoList. When adapter is null, always true");
            } else {
                togglePagerVisibility(true);
                collapseExpandLayout(true, true);
            }
            return;
        }

        mPagerAdapter.attachmentList = entryPhotosArrayList;
        mPagerAdapter.setEntryInfo(entryInfo);
        mPagerAdapter.paramsSet = false;
        mPagerAdapter.notifyDataSetChanged();
        mIndicator.setViewPager(mPager);
        if (photoCount == 0) {
            togglePagerVisibility(false);
        }

        if (newPhotosAdded) {
            mScrollView.smoothScrollTo(0, 0);
            togglePagerVisibility(true);
            mPager.setCurrentItem(mPagerAdapter.getCount() - 1);
            collapseExpandLayout(true, true);
            newPhotosAdded = false;
        }
    }

    public void toggleNestedScroll(boolean enabled) {
        mScrollView.setNestedScrollingEnabled(enabled);
        AppLog.d("mScrollView.isNestedScrollingEnabled(): " + mScrollView.isNestedScrollingEnabled());
    }

    public void collapseExpandLayout(boolean expand, boolean animate) {
        ((AppBarLayout) mLayout).setExpanded(expand, animate);
    }

    public void changeCollapsingLayoutScrollFlagsAndHeight(boolean enabled) {
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) mCollapsingLayout.getLayoutParams();
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) mLayout.getLayoutParams();

        if (enabled) {
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
            lp.setBehavior(new AppBarLayout.Behavior());
            lp.height = ViewPager.LayoutParams.WRAP_CONTENT;
        } else {
            params.setScrollFlags(0);
            lp.setBehavior(null);
            lp.height = 0;
        }
        mLayout.setLayoutParams(lp);
        mCollapsingLayout.setLayoutParams(params);
    }

    public void toggleFillVisibility(boolean enabled) {
        if (enabled) {
            pagerFill.setVisibility(View.VISIBLE);
        } else {
            AppLog.d("mCollapsingLayout.getAnimation(): " + mCollapsingLayout.getAnimation());
            pagerFill.setVisibility(View.GONE);
        }
    }

    public void hideLayout() {
        toggleFillVisibility(true);
        changeCollapsingLayoutScrollFlagsAndHeight(false);
    }

    public void showLayout() {
        changeCollapsingLayoutScrollFlagsAndHeight(true);
        collapseExpandLayout(false, false);
    }

    @Override
    public void onPhotoItemClick(int position) {
        mCallbacks.onPhotoClicked(position);
    }

    public void setNewPhotosAdded() {
        newPhotosAdded = true;
    }

    public interface EntryPhotosCallbacks {

        void onPhotoClicked(int position);
    }

}