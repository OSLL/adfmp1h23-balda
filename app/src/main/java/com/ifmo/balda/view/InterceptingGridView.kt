package com.ifmo.balda.view

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.GridView
import kotlin.math.roundToInt

class InterceptingGridView : GridView {
  private val mHitRect = Rect()

  constructor(context: Context?) : super(context)
  constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

  override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
    return true
  }

  override fun onTouchEvent(ev: MotionEvent): Boolean {
    val x = ev.x.roundToInt()
    val y = ev.y.roundToInt()
    for (i in 0 until childCount) {
      val child = getChildAt(i)
      child.getHitRect(mHitRect)
      if (mHitRect.contains(x, y)) {
        child.dispatchTouchEvent(ev)
      }
    }
    return super.onTouchEvent(ev)
  }
}
