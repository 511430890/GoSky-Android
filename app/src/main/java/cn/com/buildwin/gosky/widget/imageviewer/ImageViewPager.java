package cn.com.buildwin.gosky.widget.imageviewer;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ImageViewPager extends ViewPager {

    private boolean isLocked;

    public ImageViewPager(Context context) {
        super(context);
        isLocked = false;
    }

    public ImageViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        isLocked = false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isLocked) {
            try {
                return super.onInterceptTouchEvent(ev);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return !isLocked && super.onTouchEvent(ev);
    }

    public void toggleLock() {
        isLocked = !isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public boolean isLocked() {
        return isLocked;
    }
}
