package net.ienlab.sogangassist

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import net.ienlab.sogangassist.decorators.NightModeDecorator
import net.ienlab.sogangassist.decorators.OneDayDecorator
import net.ienlab.sogangassist.decorators.SaturdayDecorator
import net.ienlab.sogangassist.decorators.SundayDecorator
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))

        val sundayDecorator = SundayDecorator(this)
        val saturdayDecorator = SaturdayDecorator(this)
        val todayDecorator = OneDayDecorator(this)

        todayDecorator.setDate(Date(System.currentTimeMillis()))

        calendarView.topbarVisible = false
        calendarView.addDecorators(sundayDecorator, saturdayDecorator, todayDecorator)
        calendarView.arrowColor = ContextCompat.getColor(this, R.color.black)

        if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            calendarView.addDecorator(NightModeDecorator(this))
        }
    }
}