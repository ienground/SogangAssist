package net.ienlab.sogangassist

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.prolificinteractive.materialcalendarview.CalendarDay
import net.ienlab.sogangassist.adapter.MainWorkAdapter
import net.ienlab.sogangassist.constant.LMSType
import net.ienlab.sogangassist.constant.SharedGroup
import net.ienlab.sogangassist.databinding.ActivityMainBinding
import net.ienlab.sogangassist.decorators.*
import net.ienlab.sogangassist.receiver.TimeReceiver
import net.ienlab.sogangassist.utils.MyUtils
import java.text.SimpleDateFormat
import java.util.*

val TAG = "SogangAssistTAG"
val REFRESH_MAIN_WORK = 2
val SETTINGS_CHANGED = 3
val testDevice = "C539956A287753EFC92BF75B93D6D291"

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    lateinit var dbHelper: DBHelper
    lateinit var sharedPreferences: SharedPreferences
    lateinit var am: AlarmManager
    lateinit var fadeOutAnimation: AlphaAnimation
    lateinit var fadeInAnimation: AlphaAnimation

    // 뒤로가기 시간
    private val FINISH_INTERVAL_TIME: Long = 2000
    private var backPressedTime: Long = 0

    lateinit var currentDecorator: CurrentDecorator
    var thisCurrentDate: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.activity = this

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null

        dbHelper = DBHelper(this, dbName, dbVersion)
        sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
        fadeOutAnimation = AlphaAnimation(1f, 0f).apply {
            duration = 300
        }
        fadeInAnimation = AlphaAnimation(0f, 1f).apply {
            duration = 300
        }
        currentDecorator = CurrentDecorator(this, Calendar.getInstance())

        val monthFormat = SimpleDateFormat("MMMM", Locale.ENGLISH)
        val gmsansBold = Typeface.createFromAsset(assets, "fonts/gmsans_bold.otf")
        val gmsansMedium = Typeface.createFromAsset(assets, "fonts/gmsans_medium.otf")

        val dateFormat = SimpleDateFormat(getString(R.string.tag_date), Locale.getDefault())

        binding.month0.typeface = gmsansBold
        binding.month1.typeface = gmsansBold
        binding.month.setText(monthFormat.format(Date(System.currentTimeMillis())))
        binding.tagEvents.text = getString(R.string.events_today, dateFormat.format(Calendar.getInstance().time))

        binding.tagSchedule.typeface = gmsansMedium
        binding.tagEvents.typeface = gmsansMedium
        binding.tvNoDeadline.typeface = gmsansMedium

        if (BuildConfig.DEBUG) binding.adView.visibility = View.GONE

        if (!MyUtils.isNotiPermissionAllowed(this)) {
            AlertDialog.Builder(this).apply {
                setTitle(R.string.intro_page2_title)
                setMessage(R.string.intro_page2_exp)
                setPositiveButton(R.string.ok) { dialog, _ ->
                    startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                    dialog.dismiss()
                }
                setNegativeButton(R.string.cancel) { _, _ ->}
            }.show()
        }

        am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val datas = dbHelper.getAllData()
        for (data in datas) {
            val noti_intent = Intent(this, TimeReceiver::class.java)
            noti_intent.putExtra("ID", data.id)
            val endCalendar = Calendar.getInstance().apply {
                timeInMillis = data.endTime
            }

            if (data.endTime < System.currentTimeMillis()) continue

            if (data.type == LMSType.HOMEWORK) {
                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_1HOUR_HW, false)) {
                    val triggerTime = endCalendar.timeInMillis - 1 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 1)
                    val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 1, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_2HOUR_HW, false)) {
                    val triggerTime = endCalendar.timeInMillis - 2 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 2)
                    val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 2, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_6HOUR_HW, false)) {
                    val triggerTime = endCalendar.timeInMillis - 6 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 6)
                    val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 3, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_12HOUR_HW, false)) {
                    val triggerTime = endCalendar.timeInMillis - 12 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 12)
                    val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 4, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_24HOUR_HW, false)) {
                    val triggerTime = endCalendar.timeInMillis - 24 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 24)
                    val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 5, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } else if (data.type == LMSType.LESSON || data.type == LMSType.SUP_LESSON) {
                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_1HOUR_LEC, false)) {
                    val triggerTime = endCalendar.timeInMillis - 1 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 1)
                    val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 6, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_2HOUR_LEC, false)) {
                    val triggerTime = endCalendar.timeInMillis - 2 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 2)
                    val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 7, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_6HOUR_LEC, false)) {
                    val triggerTime = endCalendar.timeInMillis - 6 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 6)
                    val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 8, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_12HOUR_LEC, false)) {
                    val triggerTime = endCalendar.timeInMillis - 12 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 12)
                    val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 9, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_24HOUR_LEC, false)) {
                    val triggerTime = endCalendar.timeInMillis - 24 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 24)
                    val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 10, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            }
        }

        // AdView
        val adRequest = AdRequest.Builder()
        if (BuildConfig.DEBUG) {
            RequestConfiguration.Builder()
                .setTestDeviceIds(mutableListOf(testDevice)).let {
                    MobileAds.setRequestConfiguration(it.build())
                }
        }

        val data = "새로운 화상강의 일정이 있습니다. \"3/3\" (시작일:2021.03.03 오후 12:00)"
        val regex = "^.+\"(.+)\" \\(시작일:(.+)\\)\$".toRegex()

        val matchResult = regex.matchEntire(data as CharSequence)

        for (i in matchResult?.destructured?.toList() ?: listOf()) {
            Log.d(TAG, i)
        }

        binding.adView.loadAd(adRequest.build())

        val todayWork = dbHelper.getItemAtLastDate(System.currentTimeMillis()).toMutableList().apply {
            sortWith( compareBy ({ it.isFinished }, {it.type}))
        }

        binding.mainWorkView.adapter = MainWorkAdapter(todayWork)
        binding.mainWorkView.layoutManager = LinearLayoutManager(this)
        binding.tvNoDeadline.visibility = if (todayWork.isEmpty()) View.VISIBLE else View.GONE

        thisCurrentDate = System.currentTimeMillis()

        // 달력
        var beforeDate = binding.calendarView.currentDate.date
        binding.calendarView.apply {
            topbarVisible = false
            arrowColor = ContextCompat.getColor(applicationContext, R.color.black)
            setOnDateChangedListener { widget, date, _ ->
                widget.removeDecorator(currentDecorator)
                currentDecorator = CurrentDecorator(this@MainActivity, date.calendar)
                widget.addDecorator(currentDecorator)

                binding.tagEvents.text = getString(R.string.events_today, dateFormat.format(date.date))
                val work = dbHelper.getItemAtLastDate(date.date.time).toMutableList().apply {
                    sortWith( compareBy ({ it.isFinished }, {it.type}))
                }
                binding.mainWorkView.let {
                    it.startAnimation(fadeOutAnimation)
                    it.visibility = View.INVISIBLE

                    it.adapter = MainWorkAdapter(work)
                    it.layoutManager = LinearLayoutManager(applicationContext)
                    thisCurrentDate = date.date.time

                    it.visibility = View.VISIBLE
                    it.startAnimation(fadeInAnimation)
                }

                binding.tvNoDeadline.let {
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
            setOnMonthChangedListener { _, date ->
                if (beforeDate > date.date) {
                    binding.month.setInAnimation(applicationContext, R.anim.slide_in_left)
                    binding.month.setOutAnimation(applicationContext, R.anim.slide_out_right)
                } else {
                    binding.month.setInAnimation(applicationContext, R.anim.slide_in_right)
                    binding.month.setOutAnimation(applicationContext, R.anim.slide_out_left)
                }

                binding.month.setText(monthFormat.format(date.date))
                beforeDate = date.date
            }
        }

        setDecorators()

        val info = packageManager.getPackageInfo(packageName, 0)
        val currentVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode.toInt() else info.versionCode
        val lastVersion = sharedPreferences.getInt(SharedGroup.LAST_VERSION, 0)

        if (currentVersion > lastVersion) {
            sharedPreferences.edit().putInt(SharedGroup.LAST_VERSION, currentVersion).apply()
            AlertDialog.Builder(this).apply {
                val changelogDialogView = layoutInflater.inflate(R.layout.dialog_changelog, LinearLayout(context), false)
                val content: TextView = changelogDialogView.findViewById(R.id.content)

                setTitle("${getString(R.string.real_app_name)} ${BuildConfig.VERSION_NAME}")
                setPositiveButton(R.string.ok) { dialog, _ ->
                    dialog.cancel()
                }
                content.text = MyUtils.fromHtml(MyUtils.readTextFromRaw(resources, R.raw.changelog))
                setView(changelogDialogView)

            }.show()
        }

        val id = intent.getIntExtra("ID", -1)
        if (id != -1) {
            Log.d(TAG, "id: $id")
            Intent(this, EditActivity::class.java).let {
                it.putExtra("ID", id)
                startActivityForResult(it, REFRESH_MAIN_WORK)
            }
        }

    }

    fun setDecorators() {
        binding.calendarView.removeDecorators()

        val weekdayDecorator = WeekdayDecorator(this)
        val sundayDecorator = SundayDecorator(this)
        val saturdayDecorator = SaturdayDecorator(this)
        val todayDecorator = OneDayDecorator(this).apply {
            setDate(Date(System.currentTimeMillis()))
        }

        binding.calendarView.addDecorators(weekdayDecorator, sundayDecorator, saturdayDecorator, todayDecorator)
        if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            binding.calendarView.addDecorator(NightModeDecorator(this))
        }

        val datas = dbHelper.getAllData()
        val timeCount = mutableMapOf<Long, IntArray>()

        for (data in datas) {
            Calendar.getInstance().let {
                it.timeInMillis = data.endTime
                it.set(Calendar.HOUR_OF_DAY, 0)
                it.set(Calendar.MINUTE, 0)
                it.set(Calendar.SECOND, 0)
                it.set(Calendar.MILLISECOND, 0)

                if (it.timeInMillis in timeCount.keys) {
                    val value = timeCount[it.timeInMillis] ?: intArrayOf(0, 0)
                    value[if (data.isFinished) 1 else 0] += 1
                    timeCount[it.timeInMillis] = value
                } else {
                    if (data.isFinished) {
                        timeCount[it.timeInMillis] = intArrayOf(0, 1)
                    } else {
                        timeCount[it.timeInMillis] = intArrayOf(1, 0)
                    }

                }
            }
        }

        for (time in timeCount) {
            val decorator = EventDecorator(ContextCompat.getColor(this, R.color.colorAccent), time.value, arrayListOf(CalendarDay.from(Date(time.key))))
            binding.calendarView.addDecorator(decorator)
        }
    }

    fun refreshData() {
        val work = dbHelper.getItemAtLastDate(thisCurrentDate).toMutableList().apply {
            sortWith( compareBy ({ it.isFinished }, {it.type}))
        }
        binding.mainWorkView.adapter = MainWorkAdapter(work)
        binding.mainWorkView.layoutManager = LinearLayoutManager(this)
        binding.tvNoDeadline.visibility = if (work.isEmpty()) View.VISIBLE else View.GONE

        setDecorators()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        when (requestCode) {
            REFRESH_MAIN_WORK -> {
                if (resultCode == Activity.RESULT_OK) {
                    refreshData()
                }
            }

            SETTINGS_CHANGED -> {
                val datas = dbHelper.getAllData()
                for (data in datas) {
                    val noti_intent = Intent(this, TimeReceiver::class.java)
                    noti_intent.putExtra("ID", data.id)

                    if (data.endTime < System.currentTimeMillis()) continue

                    val endCalendar = Calendar.getInstance().apply {
                        timeInMillis = data.endTime
                    }

                    if (data.type == LMSType.HOMEWORK) {
                        if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_1HOUR_HW, false)) {
                            val triggerTime = endCalendar.timeInMillis - 1 * 60 * 60 * 1000
                            noti_intent.putExtra("TRIGGER", triggerTime)
                            noti_intent.putExtra("TIME", 1)
                            val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 1, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                            am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                        }

                        if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_2HOUR_HW, false)) {
                            val triggerTime = endCalendar.timeInMillis - 2 * 60 * 60 * 1000
                            noti_intent.putExtra("TRIGGER", triggerTime)
                            noti_intent.putExtra("TIME", 2)
                            val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 2, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                            am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                        }

                        if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_6HOUR_HW, false)) {
                            val triggerTime = endCalendar.timeInMillis - 6 * 60 * 60 * 1000
                            noti_intent.putExtra("TRIGGER", triggerTime)
                            noti_intent.putExtra("TIME", 6)
                            val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 3, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                            am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                        }

                        if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_12HOUR_HW, false)) {
                            val triggerTime = endCalendar.timeInMillis - 12 * 60 * 60 * 1000
                            noti_intent.putExtra("TRIGGER", triggerTime)
                            noti_intent.putExtra("TIME", 12)
                            val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 4, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                            am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                        }

                        if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_24HOUR_HW, false)) {
                            val triggerTime = endCalendar.timeInMillis - 24 * 60 * 60 * 1000
                            noti_intent.putExtra("TRIGGER", triggerTime)
                            noti_intent.putExtra("TIME", 24)
                            val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 5, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                            am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                        }
                    } else if (data.type == LMSType.LESSON || data.type == LMSType.SUP_LESSON) {
                        if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_1HOUR_LEC, false)) {
                            val triggerTime = endCalendar.timeInMillis - 1 * 60 * 60 * 1000
                            noti_intent.putExtra("TRIGGER", triggerTime)
                            noti_intent.putExtra("TIME", 1)
                            val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 6, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                            am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                        }

                        if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_2HOUR_LEC, false)) {
                            val triggerTime = endCalendar.timeInMillis - 2 * 60 * 60 * 1000
                            noti_intent.putExtra("TRIGGER", triggerTime)
                            noti_intent.putExtra("TIME", 2)
                            val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 7, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                            am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                        }

                        if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_6HOUR_LEC, false)) {
                            val triggerTime = endCalendar.timeInMillis - 6 * 60 * 60 * 1000
                            noti_intent.putExtra("TRIGGER", triggerTime)
                            noti_intent.putExtra("TIME", 6)
                            val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 8, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                            am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                        }

                        if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_12HOUR_LEC, false)) {
                            val triggerTime = endCalendar.timeInMillis - 12 * 60 * 60 * 1000
                            noti_intent.putExtra("TRIGGER", triggerTime)
                            noti_intent.putExtra("TIME", 12)
                            val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 9, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                            am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                        }

                        if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_24HOUR_LEC, false)) {
                            val triggerTime = endCalendar.timeInMillis - 24 * 60 * 60 * 1000
                            noti_intent.putExtra("TRIGGER", triggerTime)
                            noti_intent.putExtra("TIME", 24)
                            val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 10, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                            am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_set_today -> {
                binding.calendarView.setCurrentDate(Date(System.currentTimeMillis()))
                thisCurrentDate = System.currentTimeMillis()
            }

            R.id.menu_settings -> {
                startActivityForResult(Intent(this, SettingsActivity::class.java), SETTINGS_CHANGED)
            }

            R.id.menu_add -> {
                startActivityForResult(Intent(this, EditActivity::class.java), REFRESH_MAIN_WORK)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val tempTime = System.currentTimeMillis()
        val intervalTime = tempTime - backPressedTime
        if (intervalTime in 0..FINISH_INTERVAL_TIME) {
            super.onBackPressed()
        } else {
            backPressedTime = tempTime
            Toast.makeText(applicationContext, getString(R.string.press_back_to_exit), Toast.LENGTH_SHORT).show()
        }
    }
}