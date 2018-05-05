package com.hrrock.snapbook.utils

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * Created by hp-u on 2/23/2018.
 */
class ScrollableViewPager : ViewPager {
    private var canScroll = true

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    fun setCanScroll(canScroll: Boolean) {    //set true to enable swipe in viewpager, or set false to disable swipe.
        this.canScroll = canScroll
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return canScroll && super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return canScroll && super.onInterceptTouchEvent(ev)
    }
}
