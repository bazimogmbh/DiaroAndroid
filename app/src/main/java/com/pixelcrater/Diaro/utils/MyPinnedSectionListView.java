package com.pixelcrater.Diaro.utils;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.SectionIndexer;
import android.widget.WrapperListAdapter;

import dev.dworks.libs.astickyheader.ui.AutoScrollListView;
import dev.dworks.libs.astickyheader.ui.PinnedSectionListView;

public class MyPinnedSectionListView extends AutoScrollListView {

    private final Rect mTouchRect = new Rect();
    private final PointF mTouchPoint = new PointF();
    OnScrollListener mDelegateOnScrollListener;
    MyPinnedSectionListView.PinnedSection mRecycleSection;
    MyPinnedSectionListView.PinnedSection mPinnedSection;
    int mTranslateY;
    private int mTouchSlop;
    private View mTouchTarget;
    private MotionEvent mDownEvent;
    private GradientDrawable mShadowDrawable;
    private int mSectionsDistanceY;
    private final OnScrollListener mOnScrollListener = new OnScrollListener() {
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (mDelegateOnScrollListener != null) {
                mDelegateOnScrollListener.onScrollStateChanged(view, scrollState);
            }
        }

        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//            AppLog.d("firstVisibleItem: " + firstVisibleItem + ", visibleItemCount: " + visibleItemCount +
//                    ", totalItemCount: " + totalItemCount);

            if (mDelegateOnScrollListener != null) {
                mDelegateOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }

            PinnedSectionListView.PinnedSectionListAdapter adapter = getPinnedAdapter();
            if (adapter != null && visibleItemCount != 0) {
                boolean isFirstVisibleItemSection = PinnedSectionListView.isItemViewTypePinned(adapter, firstVisibleItem);
                if (isFirstVisibleItemSection) {
                    View sectionPosition = getChildAt(0);
                    if (sectionPosition.getTop() == getPaddingTop()) {
                        destroyPinnedShadow();
                    } else {
                        ensureShadowForPosition(firstVisibleItem, firstVisibleItem, visibleItemCount);
                    }
                } else {
                    int sectionPosition1 = findCurrentSectionPosition(firstVisibleItem);
                    if (sectionPosition1 > -1) {
                        ensureShadowForPosition(sectionPosition1, firstVisibleItem, visibleItemCount);
                    } else {
                        destroyPinnedShadow();
                    }
                }
            }
        }
    };
    private final DataSetObserver mDataSetObserver = new DataSetObserver() {
        public void onChanged() {
            recreatePinnedShadow();
        }

        public void onInvalidated() {
            recreatePinnedShadow();
        }
    };
    private int mShadowHeight;
    private int[] shadowColor = new int[]{Color.parseColor("#30000000"),
            Color.parseColor("#15000000"), Color.parseColor("#00000000")};

    public MyPinnedSectionListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initView();
    }

    public MyPinnedSectionListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.initView();
    }

    public static boolean isItemViewTypePinned(ListAdapter adapter, int position) {
        if (adapter instanceof HeaderViewListAdapter) {
            adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        }

        return ((PinnedSectionListView.PinnedSectionListAdapter) adapter).isItemViewTypePinned(position);
    }

    private void initView() {
        this.setOnScrollListener(this.mOnScrollListener);
        this.mTouchSlop = ViewConfiguration.get(this.getContext()).getScaledTouchSlop();
        this.initShadow(true);
    }

    public void setShadowVisible(boolean visible) {
        this.initShadow(visible);
        if (this.mPinnedSection != null) {
            View v = this.mPinnedSection.view;
            this.invalidate(v.getLeft(), v.getTop(), v.getRight(), v.getBottom() + this.mShadowHeight);
        }

    }

    public void initShadow(boolean visible) {
        if (visible) {
            if (this.mShadowDrawable == null) {
                this.mShadowDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                        shadowColor);
                this.mShadowHeight = (int) (8.0F * this.getResources().getDisplayMetrics().density);
            }
        } else if (this.mShadowDrawable != null) {
            this.mShadowDrawable = null;
            this.mShadowHeight = 0;
        }

    }

    void createPinnedShadow(int position) {
        MyPinnedSectionListView.PinnedSection pinnedShadow = this.mRecycleSection;
        this.mRecycleSection = null;
        if (pinnedShadow == null) {
            pinnedShadow = new MyPinnedSectionListView.PinnedSection();
        }

        View pinnedView = this.getAdapter().getView(position, pinnedShadow.view, this);
        pinnedView.setBackgroundColor(-1);
        LayoutParams layoutParams = (LayoutParams) pinnedView.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new LayoutParams(-1, -2);
        }

        int heightMode = MeasureSpec.getMode(layoutParams.height);
        int heightSize = MeasureSpec.getSize(layoutParams.height);
        if (heightMode == 0) {
            heightMode = 1073741824;
        }

        int maxHeight = this.getHeight() - this.getListPaddingTop() - this.getListPaddingBottom();
        if (heightSize > maxHeight) {
            heightSize = maxHeight;
        }

        int ws = MeasureSpec.makeMeasureSpec(this.getWidth() - this.getListPaddingLeft() - this.getListPaddingRight(), 1073741824);
        int hs = MeasureSpec.makeMeasureSpec(heightSize, heightMode);
        pinnedView.measure(ws, hs);
        pinnedView.layout(0, 0, pinnedView.getMeasuredWidth(), pinnedView.getMeasuredHeight());
        this.mTranslateY = 0;
        pinnedShadow.view = pinnedView;
        pinnedShadow.position = position;
        pinnedShadow.id = this.getAdapter().getItemId(position);
        this.mPinnedSection = pinnedShadow;
    }

    void destroyPinnedShadow() {
//        AppLog.d("mPinnedSection: " + mPinnedSection);

        if (this.mPinnedSection != null) {
            this.mRecycleSection = this.mPinnedSection;
            this.mPinnedSection = null;
        }

    }

    void ensureShadowForPosition(int sectionPosition, int firstVisibleItem, int visibleItemCount) {
//        AppLog.d("sectionPosition: " + sectionPosition + ", firstVisibleItem: " + firstVisibleItem +
//                ", visibleItemCount: " + visibleItemCount);

        if (visibleItemCount < 2) {
            this.destroyPinnedShadow();
        } else {
            if (this.mPinnedSection != null && this.mPinnedSection.position != sectionPosition) {
                this.destroyPinnedShadow();
            }

            if (this.mPinnedSection == null) {
                this.createPinnedShadow(sectionPosition);
            }

            int nextPosition = sectionPosition + 1;
            if (nextPosition < this.getCount()) {
                int nextSectionPosition = this.findFirstVisibleSectionPosition(nextPosition, visibleItemCount - (nextPosition - firstVisibleItem));
                if (nextSectionPosition > -1) {
                    View nextSectionView = this.getChildAt(nextSectionPosition - firstVisibleItem);
                    int bottom = this.mPinnedSection.view.getBottom() + this.getPaddingTop();
                    this.mSectionsDistanceY = nextSectionView.getTop() - bottom;
                    if (this.mSectionsDistanceY < 0) {
                        this.mTranslateY = this.mSectionsDistanceY;
                    } else {
                        this.mTranslateY = 0;
                    }
                } else {
                    this.mTranslateY = 0;
                    this.mSectionsDistanceY = 2147483647;
                }
            }

        }
    }

    int findFirstVisibleSectionPosition(int firstVisibleItem, int visibleItemCount) {
        PinnedSectionListView.PinnedSectionListAdapter adapter = this.getPinnedAdapter();

        for (int childIndex = 0; childIndex < visibleItemCount; ++childIndex) {
            int position = firstVisibleItem + childIndex;
            if (isItemViewTypePinned(adapter, position)) {
                return position;
            }
        }

        return -1;
    }

    int findCurrentSectionPosition(int fromPosition) {
        PinnedSectionListView.PinnedSectionListAdapter adapter = this.getPinnedAdapter();
        if (adapter instanceof SectionIndexer) {
            SectionIndexer position = (SectionIndexer) adapter;
            int sectionPosition = position.getSectionForPosition(fromPosition);
            int itemPosition = position.getPositionForSection(sectionPosition);
            if (isItemViewTypePinned(adapter, itemPosition)) {
                return itemPosition;
            }
        }

        for (int var6 = fromPosition; var6 >= 0; --var6) {
            if (isItemViewTypePinned(adapter, var6)) {
                return var6;
            }
        }

        return -1;
    }

    void recreatePinnedShadow() {
        this.destroyPinnedShadow();
        ListAdapter adapter = this.getAdapter();
//        AppLog.d("adapter.getCount(): " + adapter.getCount());

        if (adapter != null && adapter.getCount() > 0) {
            int firstVisiblePosition = this.getFirstVisiblePosition();
            int sectionPosition = this.findCurrentSectionPosition(firstVisiblePosition);
//            AppLog.d("firstVisiblePosition: " + firstVisiblePosition +
//                    ", sectionPosition: " + sectionPosition);

            if (sectionPosition == -1) {
                return;
            }

            this.ensureShadowForPosition(sectionPosition, firstVisiblePosition,
                    this.getLastVisiblePosition() - firstVisiblePosition);
        }

    }

    public void setOnScrollListener(OnScrollListener listener) {
        if (listener == this.mOnScrollListener) {
            super.setOnScrollListener(listener);
        } else {
            this.mDelegateOnScrollListener = listener;
        }

    }

    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
//        this.post(new Runnable() {
//            public void run() {
//                AppLog.d("");
//                recreatePinnedShadow();
//            }
//        });
    }

    public void setAdapter(ListAdapter adapter) {
        ListAdapter oldAdapter = this.getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterDataSetObserver(this.mDataSetObserver);
        }

        if (adapter != null) {
            adapter.registerDataSetObserver(this.mDataSetObserver);
        }

        if (oldAdapter != adapter) {
            this.destroyPinnedShadow();
        }

        super.setAdapter(adapter);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (this.mPinnedSection != null) {
            int parentWidth = r - l - this.getPaddingLeft() - this.getPaddingRight();
            int shadowWidth = this.mPinnedSection.view.getWidth();
            if (parentWidth != shadowWidth) {
                this.recreatePinnedShadow();
            }
        }

    }

    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (this.mPinnedSection != null) {
            int pLeft = this.getListPaddingLeft();
            int pTop = this.getListPaddingTop();
            View view = this.mPinnedSection.view;
            canvas.save();
            int clipHeight = view.getHeight() + (this.mShadowDrawable == null ? 0 : Math.min(this.mShadowHeight, this.mSectionsDistanceY));
            canvas.clipRect(pLeft, pTop, pLeft + view.getWidth(), pTop + clipHeight);
            canvas.translate((float) pLeft, (float) (pTop + this.mTranslateY));
            this.drawChild(canvas, this.mPinnedSection.view, this.getDrawingTime());
            if (this.mShadowDrawable != null && this.mSectionsDistanceY > 0) {
                this.mShadowDrawable.setBounds(this.mPinnedSection.view.getLeft(), this.mPinnedSection.view.getBottom(), this.mPinnedSection.view.getRight(), this.mPinnedSection.view.getBottom() + this.mShadowHeight);
                this.mShadowDrawable.draw(canvas);
            }

            canvas.restore();
        }

    }

    public boolean dispatchTouchEvent(MotionEvent ev) {
        float x = ev.getX();
        float y = ev.getY();
        int action = ev.getAction();
        if (action == 0 && this.mTouchTarget == null && this.mPinnedSection != null && this.isPinnedViewTouched(this.mPinnedSection.view, x, y)) {
            this.mTouchTarget = this.mPinnedSection.view;
            this.mTouchPoint.x = x;
            this.mTouchPoint.y = y;
            this.mDownEvent = MotionEvent.obtain(ev);
        }

        if (this.mTouchTarget != null) {
            if (this.isPinnedViewTouched(this.mTouchTarget, x, y)) {
                this.mTouchTarget.dispatchTouchEvent(ev);
            }

            if (action == 1) {
                super.dispatchTouchEvent(ev);
                this.performPinnedItemClick();
                this.clearTouchTarget();
            } else if (action == 3) {
                this.clearTouchTarget();
            } else if (action == 2 && Math.abs(y - this.mTouchPoint.y) > (float) this.mTouchSlop) {
                MotionEvent event = MotionEvent.obtain(ev);
                event.setAction(3);
                this.mTouchTarget.dispatchTouchEvent(event);
                event.recycle();
                super.dispatchTouchEvent(this.mDownEvent);
                super.dispatchTouchEvent(ev);
                this.clearTouchTarget();
            }

            return true;
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }

    private boolean isPinnedViewTouched(View view, float x, float y) {
        view.getHitRect(this.mTouchRect);
        this.mTouchRect.top += this.mTranslateY;
        this.mTouchRect.bottom += this.mTranslateY + this.getPaddingTop();
        this.mTouchRect.left += this.getPaddingLeft();
        this.mTouchRect.right -= this.getPaddingRight();
        return this.mTouchRect.contains((int) x, (int) y);
    }

    private void clearTouchTarget() {
        this.mTouchTarget = null;
        if (this.mDownEvent != null) {
            this.mDownEvent.recycle();
            this.mDownEvent = null;
        }

    }

    private boolean performPinnedItemClick() {
        if (this.mPinnedSection == null) {
            return false;
        } else {
            OnItemClickListener listener = this.getOnItemClickListener();
            if (listener != null) {
                View view = this.mPinnedSection.view;
                this.playSoundEffect(0);
                if (view != null) {
                    view.sendAccessibilityEvent(1);
                }

                listener.onItemClick(this, view, this.mPinnedSection.position, this.mPinnedSection.id);
                return true;
            } else {
                return false;
            }
        }
    }

    private PinnedSectionListView.PinnedSectionListAdapter getPinnedAdapter() {
        PinnedSectionListView.PinnedSectionListAdapter adapter;
        if (this.getAdapter() instanceof WrapperListAdapter) {
            adapter = (PinnedSectionListView.PinnedSectionListAdapter) ((WrapperListAdapter) this.getAdapter()).getWrappedAdapter();
        } else {
            adapter = (PinnedSectionListView.PinnedSectionListAdapter) this.getAdapter();
        }

        return adapter;
    }

    public interface PinnedSectionListAdapter extends ListAdapter {
        boolean isItemViewTypePinned(int var1);
    }

    static class PinnedSection {
        public View view;
        public int position;
        public long id;

        PinnedSection() {
        }
    }
}
