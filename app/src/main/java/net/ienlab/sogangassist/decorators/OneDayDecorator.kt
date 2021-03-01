package net.ienlab.sogangassist.decorators

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.core.content.ContextCompat

import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import net.ienlab.sogangassist.utils.MyTypefaceSpan
import net.ienlab.sogangassist.R

import java.util.Date

class OneDayDecorator(mContext: Context) : DayViewDecorator {

    private var date: CalendarDay? = null
    private val context = mContext

    init {
        date = CalendarDay.today()
    }

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return date != null && day == date
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(RelativeSizeSpan(1.4f))
        view.addSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.dark_blue)))
        view.addSpan(MyTypefaceSpan(context, "gmsans_bold.otf"))
    }

    /**
     * We're changing the internals, so make sure to call [MaterialCalendarView.invalidateDecorators]
     */
    fun setDate(date: Date) {
        this.date = CalendarDay.from(date)
    }
}
