package com.kedzie.drawer.drag;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import com.kedzie.drawer.R;

/**
 * Draggable drawer
 */
public class DraggedViewGroup extends ViewGroup {
    private static final String TAG = "DraggedViewGroup";

    public static final int DRAWER_LEFT=1;
    public static final int DRAWER_RIGHT=2;
    public static final int DRAWER_TOP=3;
    public static final int DRAWER_BOTTOM=4;

    /** Handle size */
    private int mHandleSize;
    /** Drawer orientation */
    private int mDrawerType;

    private int mHandleId;
    private int mContentId;
    private int mShadowId;

    private View mHandle;
    private View mContent;

    private Drawable mShadowDrawable;

    private boolean mInLayout;

    public DraggedViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Drawer, 0, 0);
        try {
            mDrawerType = a.getInt(R.styleable.Drawer_type, 0);
            mHandleId = a.getResourceId(R.styleable.Drawer_handle, 0);
            mContentId = a.getResourceId(R.styleable.Drawer_content, 0);
            mShadowDrawable = a.getDrawable(R.styleable.Drawer_shadow);
        } finally {
            a.recycle();
        }
    }

    /**
     * @param context
     */
    public DraggedViewGroup(Context context) {
        super(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHandle = findViewById(mHandleId);
        mContent = findViewById(mContentId);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        measureChild(mHandle, widthMeasureSpec, heightMeasureSpec);
        if(mHandle!=null) {
            mHandleSize = (mDrawerType==DRAWER_LEFT || mDrawerType==DRAWER_RIGHT) ?
                    mHandle.getMeasuredWidth() : mHandle.getMeasuredHeight();
        }

        int dw = getPaddingLeft() + getPaddingRight();
        int dh = getPaddingTop() + getPaddingBottom();

        switch(mDrawerType) {
            case DRAWER_BOTTOM:
            case DRAWER_TOP:
                measureChild(mContent, widthMeasureSpec,
                        MeasureSpec.makeMeasureSpec(heightSpecSize-mHandle.getMeasuredHeight(), heightSpecMode));
                dw += Math.max(mHandle.getMeasuredWidth(), mContent.getMeasuredWidth());
                dh += mHandle.getMeasuredHeight()+mContent.getMeasuredHeight();
                break;
            case DRAWER_LEFT:
            case DRAWER_RIGHT:
                measureChild(mContent, MeasureSpec.makeMeasureSpec(widthSpecSize-mHandle.getMeasuredWidth(), widthSpecMode),
                        heightMeasureSpec);
                dw += mHandle.getMeasuredWidth()+mContent.getMeasuredWidth();
                dh += Math.max(mHandle.getMeasuredHeight(), mContent.getMeasuredHeight());
                break;
        }

        setMeasuredDimension(resolveSize(dw, widthMeasureSpec), resolveSize(dh, heightMeasureSpec));
    }

    @Override
    public void requestLayout() {
        if(!mInLayout)
            super.requestLayout();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mInLayout=true;
        LayoutParams lp = (LayoutParams)mHandle.getLayoutParams();
        int handleTop=0, handleLeft=0;
        if(mDrawerType==DRAWER_LEFT || mDrawerType==DRAWER_RIGHT) { //horizontal drawers
            switch(lp.gravity) {
                case Gravity.START:
                case Gravity.TOP:
                    handleTop = 0;
                    break;
                case Gravity.END:
                case Gravity.BOTTOM:
                    handleTop = mContent.getMeasuredHeight()-mHandle.getMeasuredHeight();
                    break;
                case Gravity.CENTER_HORIZONTAL:
                case Gravity.CENTER:
                default:
                    handleTop = (mContent.getMeasuredHeight()-mHandle.getMeasuredHeight())/2;
                    break;
            }
        } else {    //vertical drawers
            switch(lp.gravity) {
                case Gravity.START:
                case Gravity.LEFT:
                    handleLeft = 0;
                    break;
                case Gravity.END:
                case Gravity.RIGHT:
                    handleLeft = mContent.getMeasuredWidth()-mHandle.getMeasuredWidth();
                    break;
                case Gravity.CENTER_VERTICAL:
                case Gravity.CENTER:
                    handleLeft = (mContent.getMeasuredWidth()-mHandle.getMeasuredWidth())/2;
                    break;
            }
        }

        switch(mDrawerType) {
            case DRAWER_LEFT:
                mContent.layout(0, 0, mContent.getMeasuredWidth(), mContent.getMeasuredHeight());
                mHandle.layout(mContent.getMeasuredWidth(), handleTop, mContent.getMeasuredWidth()+mHandle.getMeasuredWidth(), handleTop+mHandle.getMeasuredHeight());
                break;
            case DRAWER_RIGHT:
                mHandle.layout(0, handleTop, mHandle.getMeasuredWidth(), handleTop+mHandle.getMeasuredHeight());
                mContent.layout(mHandle.getMeasuredWidth(), 0, mHandle.getMeasuredWidth()+mContent.getMeasuredWidth(), mContent.getMeasuredHeight());
                break;
            case DRAWER_TOP:
                mContent.layout(0, 0, mContent.getMeasuredWidth(), mContent.getMeasuredHeight());
                mHandle.layout(handleLeft, mContent.getMeasuredHeight(), handleLeft+mHandle.getMeasuredWidth(),mContent.getMeasuredHeight()+mHandle.getMeasuredHeight());
                break;
            case DRAWER_BOTTOM:
                mHandle.layout(handleLeft, 0, handleLeft+mHandle.getMeasuredWidth(), mHandle.getMeasuredHeight());
                mContent.layout(0, mHandle.getMeasuredHeight(), mContent.getMeasuredWidth(), mHandle.getMeasuredHeight()+mContent.getMeasuredHeight());
                break;
        }
        mInLayout=false;
    }

    /**
     * Handle view size
     * @return size of handle (width for horizontal drawers, height for vertical drawers)
     */
    public int getHandleSize() {
        return mHandleSize;
    }

    /**
     * Drawer orientation
     * @return drawer orientation, i.e. DRAWER_LEFT, DRAWER_TOP, etc.
     */
    public int getDrawerType() {
        return mDrawerType;
    }

    /**
     * Get drawable to represent the drawer's shadow
     * @return
     */
    public Drawable getShadowDrawable() {
        return mShadowDrawable;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams
                ? new LayoutParams((LayoutParams) p)
                : p instanceof ViewGroup.MarginLayoutParams
                ? new LayoutParams((MarginLayoutParams) p)
                : new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams && super.checkLayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    /**
     * Margin layout params with gravity for handle position
     */
    public static class LayoutParams extends ViewGroup.MarginLayoutParams {

        private static final int[] LAYOUT_ATTRS = new int[] {
                android.R.attr.layout_gravity
        };

        public int gravity = Gravity.NO_GRAVITY;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            final TypedArray a = c.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
            this.gravity = a.getInt(0, Gravity.NO_GRAVITY);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            this(width, height);
            this.gravity = gravity;
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            this.gravity = source.gravity;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }
    }
}