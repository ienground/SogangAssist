package net.ienlab.sogangassist.decorators

import android.content.Context
import android.graphics.Color
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import net.ienlab.sogangassist.R
import java.util.*

class CurrentDecorator(mContext: Context, var date: Calendar) : DayViewDecorator {

    private val context = mContext

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return day.year == date.get(Calendar.YEAR) && day.month == date.get(Calendar.MONTH) && day.day == date.get(Calendar.DAY_OF_MONTH)
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(ForegroundColorSpan(Color.WHITE))
    }
}