package net.ienlab.sogangassist.decorators

import android.content.Context
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import net.ienlab.sogangassist.utils.MyTypefaceSpan
import net.ienlab.sogangassist.R
import java.util.*


class SundayDecorator(mContext: Context) : DayViewDecorator {

    private val calendar = Calendar.getInstance()
    private val context = mContext

    override fun shouldDecorate(day: CalendarDay): Boolean {
        day.copyTo(calendar)
        val weekDay = calendar.get(Calendar.DAY_OF_WEEK)
        return weekDay == Calendar.SUNDAY
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.red)))
        view.addSpan(MyTypefaceSpan(context, "gmsans_bold.otf"))
    }
}
