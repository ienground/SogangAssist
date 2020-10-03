package net.ienlab.sogangassist

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import net.ienlab.sogangassist.decorators.NightModeDecorator
import net.ienlab.sogangassist.decorators.OneDayDecorator
import net.ienlab.sogangassist.decorators.SaturdayDecorator
import net.ienlab.sogangassist.decorators.SundayDecorator
import java.text.SimpleDateFormat
import java.util.*

val TAG = "SogangAssistTAG"

class MainActivity : AppCompatActivity() {

    lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DBHelper(this, dbName, null, dbVersion)
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

        btn_set_today.setOnClickListener {
            val timeFormat = SimpleDateFormat("yyyy.MM.dd a hh:mm", Locale.KOREA)

            val list = dbHelper.getAllData()
            for (i in list) {
                Log.d(TAG, "id:${i.id}, className:${i.className}, timeStamp:${timeFormat.format(Date(i.timeStamp))}")
                Log.d(TAG, "type:${i.type}, startTime:${timeFormat.format(Date(i.startTime))}, endTime:${timeFormat.format(Date(i.endTime))}")
                Log.d(TAG, "week:${i.week}, lesson:${i.lesson}, hw_name:${i.homework_name}")
                Log.d(TAG, "----------------------------------------------------------------------------")
            }
        }
    }
}