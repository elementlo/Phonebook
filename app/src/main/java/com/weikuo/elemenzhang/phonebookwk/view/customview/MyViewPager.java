package com.weikuo.elemenzhang.phonebookwk.view.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.weikuo.elemenzhang.phonebookwk.R;

/**
 * Created by elemenzhang on 2017/6/23.
 */

public class MyViewPager extends ViewPager {
    public boolean isScroll = true;

    public boolean isScroll() {
        return isScroll;
    }

    public void setScroll(boolean scroll) {
        isScroll = scroll;
    }

    public MyViewPager(Context context) {
        super(context);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MyViewPager);
         /*获取布局中设置的属性*/
        isScroll = array.getBoolean(R.styleable.MyViewPager_isScroll, false);
        array.recycle();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isScroll) {
            return super.onTouchEvent(ev);
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isScroll) {
            return super.onInterceptTouchEvent(ev);
        }
        return false;

    }
}
