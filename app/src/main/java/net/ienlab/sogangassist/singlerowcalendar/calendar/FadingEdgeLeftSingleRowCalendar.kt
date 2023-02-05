package net.ienlab.sogangassist.singlerowcalendar.calendar

import android.content.Context
import android.util.AttributeSet


class FadingEdgeLeftSingleRowCalendar(context: Context, attrs: AttributeSet) : SingleRowCalendar(context, attrs) {
    override fun getRightFadingEdgeStrength(): Float = 0f
}
