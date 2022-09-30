package net.ienlab.sogangassist.decorators

import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.util.Log

import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import kotlinx.coroutines.*
import net.ienlab.sogangassist.activity.TAG
import net.ienlab.sogangassist.room.LMSDatabase
import net.ienlab.sogangassist.room.LMSEntity
import net.ienlab.sogangassist.utils.MyUtils.Companion.timeZero
import java.util.*
import kotlin.collections.ArrayList

/**
 * Decorate several days with a dot
 */
class EventDecorator(private val color: Int, private val amount: IntArray, dates: Collection<CalendarDay>) : DayViewDecorator {

//    var drawable = ContextCompat.getDrawable(context, R.drawable.ic_add)!!
    var dates = HashSet(dates)

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(MyDotSpan(10f, color, amount)) // 날자밑에 점
    }
}

class EventDecorator2(private val context: Context, private val endTime: Long, private val item: List<LMSEntity>) : DayViewDecorator {

    var dates = HashSet(arrayListOf(CalendarDay.from(Date(endTime))))

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun decorate(view: DayViewFacade) {
        val data = (item as ArrayList).apply {
            sortWith( compareBy ({ it.isFinished }, {it.endTime}, {it.type} ))
        }
        view.addSpan(MyDotSpan2(context, 32f, data)) // 날자밑에 점
    }
}
