package net.ienlab.sogangassist.decorators

import android.app.Activity

import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import java.util.HashSet

/**
 * Decorate several days with a dot
 */
class EventDecorator(private val color: Int, private val amount: Int, dates: Collection<CalendarDay>) : DayViewDecorator {

//    var drawable = ContextCompat.getDrawable(context, R.drawable.ic_add)!!
    var dates = HashSet(dates)

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(MyDotSpan(10f, color, amount)) // 날자밑에 점
    }
}
