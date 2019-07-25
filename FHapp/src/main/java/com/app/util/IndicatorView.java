package com.app.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ccwxf on 2016/6/29.
 */
public class IndicatorView extends View {

    private int colorDefault;
    private int colorSelected;
    private int colorBg;
    private int textSize;
    private String[] texts = {"text1"};
    private int width;
    private int height;
    private int measureWidth;
    private int[] padding = new int[]{0,0,0,0};
    private int lineHeight;
    private int selectItem;
    private List<Item> items = new ArrayList<Item>();
    private Paint mPaint = new Paint();
    private int mTouchX;
    private int mTouchY;
    private int mMoveX;
    private int mTouchSlop;
    private Scroller mScroller = new Scroller(getContext());
    private OnIndicatorChangedListener listener = null;
    private Type type;
    private VelocityTracker mVelocityTracker;

    public enum Type{
        SelectByLine,
        SelectByFill
    }

    public IndicatorView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
    }

    public IndicatorView(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public IndicatorView(Context context){
        super(context);
    }

    public IndicatorView color(int colorDefault, int colorSelected, int colorBg){
        this.colorDefault = colorDefault;
        this.colorSelected = colorSelected;
        this.colorBg = colorBg;
        return this;
    }

    public IndicatorView textSize(int textSize){
        this.textSize = textSize;
        return this;
    }

    public IndicatorView text(String[] texts){
        this.texts = texts;
        return this;
    }

    public IndicatorView padding(int[] padding){
        this.padding = padding;
        return this;
    }

    public IndicatorView defaultSelect(int defaultSelect){
        this.selectItem = defaultSelect;
        return this;
    }

    public IndicatorView lineHeight(int lineHeight){
        this.lineHeight = lineHeight;
        return this;
    }

    public IndicatorView listener(OnIndicatorChangedListener listener){
        this.listener = listener;
        return this;
    }

    public IndicatorView type(Type type){
        this.type = type;
        return this;
    }

    public void commit(){
        if(colorDefault == 0 || colorSelected == 0 || colorBg == 0 || textSize == 0 || texts == null || padding == null){
            throw new IllegalAccessError("you should invoke method as [color] [textSize] [text] [padding]");
        }
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mScroller = new Scroller(getContext());
    }

    public void setSelected(int position){
        if(position >= items.size()){
            return;
        }
        for(int i = 0; i < items.size(); i++){
            if(i == position){
                items.get(i).isSelected = true;
                if(i != selectItem){
                    selectItem = i;
                	int offsetX = 0;
                  //判断是否需要滑动到完全可见  
                    if(mScroller.getCurrX() + width < items.get(i).rect.right){
                    	if (selectItem != items.size()-1)
                    		offsetX = 100;
                        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), items.get(i).rect.right - mScroller.getCurrX() - width + offsetX, mScroller.getFinalY());
                    }
                    if(items.get(i).rect.left < mScroller.getCurrX()){
                    	if (selectItem != 0)
                    		offsetX = 100;
                        mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), items.get(i).rect.left - mScroller.getCurrX() - offsetX, mScroller.getFinalY());
                    }
                    if(listener != null){
                        listener.onChanged(selectItem);
                    }
                }
            }else{
                items.get(i).isSelected = false;
            }
        }
        invalidate();
    }

    /**
     * @return 鑻ユ病鏈夎閫変腑鐨勶紝鍒欎负-1
     */
    public int getSelected(){
        for(int i = 0; i < items.size(); i++){
            if(items.get(i).isSelected){
                return i;
            }
        }
        return -1;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);
      //初始化Item  
        initItems();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void initItems(){
        items.clear();
        measureWidth = 0;
        for(int i = 0; i < texts.length; i++){
            Item item = new Item();
            item.text = texts[i];
            item.colorDefault = colorDefault;
            item.colorSelected = colorSelected;
            item.textSize = textSize;
            for(int j = 0; j < item.padding.length; j++){
                item.padding[j] = padding[j];
            }
            mPaint.setTextSize(item.textSize);
            item.width = (int)mPaint.measureText(item.text);
            int dx = 0;
            if(i - 1 < 0){
                dx = 0;
            }else{
                for(int j = 0; j < i; j++){
                    dx += items.get(j).padding[0] + items.get(j).width + items.get(j).padding[2];
                }
            }
            int startX = item.padding[0] + dx;
            Paint.FontMetrics metrics =  mPaint.getFontMetrics();
            int startY = (int)(height / 2 + (metrics.bottom - metrics.top) / 2 - metrics.bottom);
            item.drawPoint = new Point(startX, startY);
          //设置区域 
            item.rect.left = item.drawPoint.x - item.padding[0];
            item.rect.top = 0;
            item.rect.right = item.drawPoint.x + item.width + item.padding[2];
            item.rect.bottom = height;
          //设置默认 
            if(i == selectItem){
                item.isSelected = true;
            }
            measureWidth += item.rect.width();
            items.add(item);
        }
      //重绘  
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mTouchX = (int)event.getX();
                mTouchY = (int)event.getY();
                mMoveX = mTouchX;
                return true;

            case MotionEvent.ACTION_MOVE:
                if(measureWidth > width){
                    int dx = (int)event.getX() - mMoveX;
                    if(dx > 0){ // 右滑
                        if(mScroller.getFinalX() > 0){
                            mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), -dx, 0);
                        }else{
                            mScroller.setFinalX(0);
                        }
                    }else{  //左滑 
                        if(mScroller.getFinalX() + width - dx < measureWidth){
                            mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), -dx, 0);
                        }else{
                            mScroller.setFinalX(measureWidth - width);
                        }
                    }
                    mMoveX = (int)event.getX();
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(measureWidth > width){
                    mVelocityTracker.computeCurrentVelocity(1000);
                    int max = Math.max(Math.abs(mScroller.getCurrX()), Math.abs(measureWidth - width - mScroller.getCurrX()));
                    mScroller.fling(mScroller.getFinalX(), mScroller.getFinalY(), (int)-mVelocityTracker.getXVelocity(), (int)-mVelocityTracker.getYVelocity(), 0, max, mScroller.getFinalY(), mScroller.getFinalY());
                  //手指抬起时，根据滚动偏移量初始化位置 
                    if(mScroller.getCurrX() < 0){
                        mScroller.abortAnimation();
                        mScroller.startScroll(mScroller.getCurrX(), mScroller.getCurrY(), -mScroller.getCurrX(), 0);
                    }else if(mScroller.getCurrX() + width > measureWidth){
                        mScroller.abortAnimation();
                        mScroller.startScroll(mScroller.getCurrX(), mScroller.getCurrY(), measureWidth - width - mScroller.getCurrX(), 0);
                    }
                }
                if(event.getAction() == MotionEvent.ACTION_UP){
                    int mUpX = (int)event.getX();
                    int mUpY = (int)event.getY();
                  //模拟点击操作 
                    if(Math.abs(mUpX - mTouchX) <= mTouchSlop && Math.abs(mUpY - mTouchY) <= mTouchSlop){
                        for(int i = 0; i < items.size(); i++){
                            if(items.get(i).rect.contains(mScroller.getCurrX() + mUpX, getScrollY() + mUpY)){
                                setSelected(i);
                                return super.onTouchEvent(event);
                            }
                        }
                    }
                }
                break;

            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void computeScroll(){
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
        super.computeScroll();
    }

    @Override
    protected void onDraw(Canvas canvas){
        mPaint.setAntiAlias(true);
        canvas.drawColor(colorBg);
        for(Item item : items){
            mPaint.setTextSize(item.textSize);
            if(item.isSelected){
                if(type == Type.SelectByLine){
                	//绘制红线  
                    mPaint.setColor(item.colorSelected);
                    mPaint.setStyle(Paint.Style.FILL);
                    canvas.drawRoundRect(new RectF(item.rect.left, item.rect.bottom - lineHeight, item.rect.right, item.rect.bottom), 3, 3, mPaint);
                }else if(type == Type.SelectByFill){
                	//绘制红色背景 
                    mPaint.setColor(getContext().getResources().getColor(android.R.color.holo_red_light));
                    mPaint.setStyle(Paint.Style.FILL);
                    canvas.drawRoundRect(new RectF(item.rect.left + 6, item.rect.top, item.rect.right - 6, item.rect.bottom), item.rect.height() * 5 / 12, item.rect.height() * 5 / 12, mPaint);
                }
                mPaint.setColor(item.colorSelected);
            }else{
                mPaint.setColor(item.colorDefault);
            }
            canvas.drawText(item.text, item.drawPoint.x, item.drawPoint.y, mPaint);
        }
    }

    public void release(){
        if(mVelocityTracker != null){
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    public class Item {
        String text;
        int colorDefault;
        int colorSelected;
        int textSize;
        boolean isSelected = false;
        int width;
        Point drawPoint;
        int[] padding = new int[4];
        Rect rect = new Rect();
    }

    public interface OnIndicatorChangedListener{
        void onChanged(int position);
    }
}
