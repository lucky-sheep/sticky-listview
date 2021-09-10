package com.yc.stickylistviewlibrary;

import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

public class StickyRecyclerHeadersTouchListener implements RecyclerView.OnItemTouchListener {
    private final GestureDetector mTapDetector;
    private final RecyclerView mRecyclerView;
    private final StickyRecyclerHeadersDecoration mDecor;

    public StickyRecyclerHeadersTouchListener(
            final RecyclerView recyclerView, final StickyRecyclerHeadersDecoration decor) {
        mTapDetector = new GestureDetector(recyclerView.getContext(), new SingleTapDetector());
        mRecyclerView = recyclerView;
        mDecor = decor;
    }

    public StickyRecyclerHeadersAdapter getAdapter() {
        if (mRecyclerView.getAdapter() instanceof StickyRecyclerHeadersAdapter) {
            return (StickyRecyclerHeadersAdapter) mRecyclerView.getAdapter();
        } else {
            throw new IllegalStateException("A RecyclerView with " +
                    StickyRecyclerHeadersTouchListener.class.getSimpleName() + " requires a " +
                    StickyRecyclerHeadersAdapter.class.getSimpleName());
        }
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
            boolean tapDetectorResponse = this.mTapDetector.onTouchEvent(e);
            if (tapDetectorResponse) {
                // Don't return false if a single tap is detected
                return true;
            }
//            if (e.getAction() == MotionEvent.ACTION_DOWN) {
//                int position = mDecor.findHeaderPositionUnder((int) e.getX(), (int) e.getY());
//                return position != -1;
//            }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent e) { /* do nothing? */ }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        // do nothing
    }

    private class SingleTapDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            int position = mDecor.findHeaderPositionUnder((int) e.getX(), (int) e.getY());
            if (position != -1) {
                View headerView = mDecor.getHeaderView(mRecyclerView, position);
                long headerId = getAdapter().getHeaderId(position);
                Rect rect = mDecor.getHeaderRect(position);
                int hOffset = rect.left - headerView.getLeft();
                int vOffset = rect.top - headerView.getTop();
                if (isChildClicked(headerView, hOffset, vOffset, e)) {
                    return false;
                }
                mRecyclerView.playSoundEffect(SoundEffectConstants.CLICK);
                headerView.onTouchEvent(e);
                return true;
            }
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }
    }

    private boolean isChildClicked(View headerView, int hOffset, int vOffset, MotionEvent e) {
        if (headerView instanceof ViewGroup == false) {
            return false;
        }
        if (isViewTouched(headerView, hOffset, vOffset, e) && headerView.isClickable()) {
            headerView.performClick();
            return true;
        }
        ViewGroup vg = (ViewGroup) headerView;
        int childCount = vg.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (vg.getChildAt(i) instanceof ViewGroup) {
                return isChildClicked(vg.getChildAt(i), hOffset, vOffset, e);
            } else {
                View child = vg.getChildAt(i);
                if (isViewTouched(child, hOffset, vOffset, e) && child.isClickable()) {
                    child.performClick();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isViewTouched(View view, int hOffset, int vOffset, MotionEvent e) {
        Rect rect = new Rect();
        rect.left = view.getLeft() + hOffset;
        rect.right = view.getRight() + hOffset;
        rect.top = view.getTop() + vOffset;
        rect.bottom = view.getBottom() + vOffset;
        return rect.contains((int) e.getX(), (int) e.getY());
    }
}
