package net.ienlab.sogangassist.decorators

import android.app.Activity
import android.content.Context

import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import net.ienlab.sogangassist.database.DBHelper
import java.util.*

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

class EventDecorator2(private val context: Context, private val endTime: Long) : DayViewDecorator {

    //    var drawable = ContextCompat.getDrawable(context, R.drawable.ic_add)!!
    lateinit var dbHelper: DBHelper
    var dates = HashSet(arrayListOf(CalendarDay.from(Date(endTime))))

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return dates.contains(day)
    }

    override fun decorate(view: DayViewFacade) {
        dbHelper = DBHelper(context, DBHelper.dbName, DBHelper.dbVersion)
        val data = dbHelper.getItemAtLastDate(endTime).toMutableList().apply {
            sortWith( compareBy ({ it.isFinished }, {it.endTime}, {it.type} ))
        }

        view.addSpan(MyDotSpan2(context, 30f, data)) // 날자밑에 점
    }
}
