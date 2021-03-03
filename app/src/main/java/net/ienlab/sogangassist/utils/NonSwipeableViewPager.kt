package net.ienlab.sogangassist.utils

/**
 * https://stackoverflow.com/questions/19602369/how-to-disable-viewpager-from-swiping-in-one-direction/34076649
 */

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

import androidx.viewpager.widget.ViewPager

class NonSwipeableViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {
    private var initialXValue: Float = 0.toFloat()
    private var direction: SwipeDirection? = null

    init {
        this.direction = SwipeDirection.all
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (this.isSwipeAllowed(event)) {
            super.onTouchEvent(event)
        } else false

    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if (this.isSwipeAllowed(event)) {
            super.onInterceptTouchEvent(event)
        } else false

    }

    private fun isSwipeAllowed(event: MotionEvent): Boolean {
        if (this.direction == SwipeDirection.all) return true

        if (direction == SwipeDirection.none)
        //disable any swipe
            return false

        if (event.action == MotionEvent.ACTION_DOWN) {
            initialXValue = event.x
            return true
        }

        if (event.action == MotionEvent.ACTION_MOVE) {
            try {
                val diffX = event.x - initialXValue
                if (diffX > 0 && direction == SwipeDirection.right) {
                    // swipe from left to right detected
                    return false
                } else if (diffX < 0 && direction == SwipeDirection.left) {
                    // swipe from right to left detected
                    return false
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }

        }

        return true
    }

    fun setAllowedSwipeDirection(direction: SwipeDirection) {
        this.direction = direction
    }
}

enum class SwipeDirection {
    all, left, right, none
}