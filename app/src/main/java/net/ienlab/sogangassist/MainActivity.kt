package net.ienlab.sogangassist

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.android.synthetic.main.activity_main.*
import net.ienlab.sogangassist.decorators.*
import java.text.SimpleDateFormat
import java.util.*

val TAG = "SogangAssistTAG"
val REFRESH_MAIN_WORK = 2

class MainActivity : AppCompatActivity() {

    lateinit var dbHelper: DBHelper
    var currentDate: Long = 0
    lateinit var fadeOutAnimation: AlphaAnimation
    lateinit var fadeInAnimation: AlphaAnimation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DBHelper(this, dbName, null, dbVersion)
        fadeOutAnimation = AlphaAnimation(1f, 1f).apply {
            duration = 300
        }
        fadeInAnimation = AlphaAnimation(0f, 1f).apply {
            duration = 300
        }
        val monthFormat = SimpleDateFormat("MMMM", Locale.ENGLISH)
        month.typeface = Typeface.createFromAsset(assets, "fonts/gmsans_bold.otf")
        month.text = monthFormat.format(Date(System.currentTimeMillis()))
//        startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))

        val todayWork = dbHelper.getItemAtLastDate(System.currentTimeMillis()).toMutableList().apply {
            sortWith( compareBy ({ it.isFinished }, {it.type}))
        }
        mainWorkView.adapter = MainWorkAdapter(todayWork)
        mainWorkView.layoutManager = LinearLayoutManager(this)
        tv_no_deadline.visibility = if (todayWork.isEmpty()) View.VISIBLE else View.GONE

        val sundayDecorator = SundayDecorator(this)
        val saturdayDecorator = SaturdayDecorator(this)
        val todayDecorator = OneDayDecorator(this)

        todayDecorator.setDate(Date(System.currentTimeMillis()))

        val arr = mutableListOf("안녕")
        arr.size
        arr.reverse()

        calendarView.topbarVisible = false
        calendarView.addDecorators(sundayDecorator, saturdayDecorator, todayDecorator)
        calendarView.arrowColor = ContextCompat.getColor(this, R.color.black)
        currentDate = System.currentTimeMillis()
        calendarView.setOnDateChangedListener { _, date, _ ->
            val work = dbHelper.getItemAtLastDate(date.date.time).toMutableList().apply {
                sortWith( compareBy ({ it.isFinished }, {it.type}))
            }
            mainWorkView.let {
                it.startAnimation(fadeOutAnimation)
                it.visibility = View.INVISIBLE

                it.adapter = MainWorkAdapter(work)
                it.layoutManager = LinearLayoutManager(this)
                currentDate = date.date.time

                it.visibility = View.VISIBLE
                it.startAnimation(fadeInAnimation)
            }

            tv_no_deadline.let {
                if (work.isEmpty()) {
                    it.startAnimation(fadeOutAnimation)
                    it.visibility = View.INVISIBLE
                    it.visibility = View.VISIBLE
                    it.startAnimation(fadeInAnimation)
                } else {
                    it.visibility = View.INVISIBLE
                }
            }
        }
        calendarView.setOnMonthChangedListener { _, date ->
            month.text = monthFormat.format(date.date)
        }

        if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            calendarView.addDecorator(NightModeDecorator(this))
        }

        // 달력 설정
        val datas = dbHelper.getAllData()
        val timeCount = mutableMapOf<Long, Int>()

        for (data in datas) {
            Calendar.getInstance().let {
                it.timeInMillis = data.endTime
                it.set(Calendar.HOUR_OF_DAY, 0)
                it.set(Calendar.MINUTE, 0)
                it.set(Calendar.SECOND, 0)
                it.set(Calendar.MILLISECOND, 0)

                if (it.timeInMillis in timeCount.keys) {
                    timeCount.put(it.timeInMillis, timeCount[it.timeInMillis]?.plus(1) ?: 1)
                } else {
                    timeCount.put(it.timeInMillis, 1)
                }
            }
        }

        for (time in timeCount) {
            val decorator = EventDecorator(ContextCompat.getColor(this, R.color.colorAccent), time.value, arrayListOf(CalendarDay.from(Date(time.key))))
            calendarView.addDecorator(decorator)
        }

        btn_set_today.setOnClickListener {
            calendarView.setCurrentDate(Date(System.currentTimeMillis()))
            currentDate = System.currentTimeMillis()
        }

        btn_settings.setOnClickListener {
            Intent(this, SettingsActivity::class.java).let {
                startActivity(it)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REFRESH_MAIN_WORK -> {
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(TAG, calendarView.currentDate.date.time.toString())
                    val work = dbHelper.getItemAtLastDate(currentDate).toMutableList().apply {
                        sortWith( compareBy ({ it.isFinished }, {it.type}))
                    }
                    mainWorkView.adapter = MainWorkAdapter(work)
                    mainWorkView.layoutManager = LinearLayoutManager(this)
                    tv_no_deadline.visibility = if (work.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }
}