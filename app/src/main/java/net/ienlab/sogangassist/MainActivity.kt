package net.ienlab.sogangassist

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import net.ienlab.sogangassist.decorators.NightModeDecorator
import net.ienlab.sogangassist.decorators.OneDayDecorator
import net.ienlab.sogangassist.decorators.SaturdayDecorator
import net.ienlab.sogangassist.decorators.SundayDecorator
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

val TAG = "SogangAssistTAG"

class MainActivity : AppCompatActivity() {

    lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DBHelper(this, dbName, null, dbVersion)
        val monthFormat = SimpleDateFormat("MMMM", Locale.ENGLISH)
        month.text = monthFormat.format(Date(System.currentTimeMillis()))
//        startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))

        val todayWork = dbHelper.getItemAtLastDate(System.currentTimeMillis()).toMutableList().apply {
            sortBy { it.type }
        }
        mainWorkView.adapter = MainWorkAdapter(todayWork)
        mainWorkView.layoutManager = LinearLayoutManager(this)

        val sundayDecorator = SundayDecorator(this)
        val saturdayDecorator = SaturdayDecorator(this)
        val todayDecorator = OneDayDecorator(this)

        todayDecorator.setDate(Date(System.currentTimeMillis()))

        calendarView.topbarVisible = false
        calendarView.addDecorators(sundayDecorator, saturdayDecorator, todayDecorator)
        calendarView.arrowColor = ContextCompat.getColor(this, R.color.black)
        calendarView.setOnDateChangedListener { _, date, _ ->
            val work = dbHelper.getItemAtLastDate(date.date.time).toMutableList().apply {
                sortBy { it.type }
            }
            mainWorkView.adapter = MainWorkAdapter(work)
            mainWorkView.layoutManager = LinearLayoutManager(this)
        }
        calendarView.setOnMonthChangedListener { _, date ->
            month.text = monthFormat.format(date.date)
        }

        if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            calendarView.addDecorator(NightModeDecorator(this))
        }

        btn_set_today.setOnClickListener {
            calendarView.setCurrentDate(Date(System.currentTimeMillis()))
        }
    }
}