package net.ienlab.sogangassist

import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.prolificinteractive.materialcalendarview.CalendarDay
import net.ienlab.sogangassist.databinding.ActivityMainBinding
import net.ienlab.sogangassist.decorators.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

val TAG = "SogangAssistTAG"
val REFRESH_MAIN_WORK = 2
val SETTINGS_CHANGED = 3
val testDevice = "5EB5321DADDD6ABD85DAB10C76FE8EFA"

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    lateinit var dbHelper: DBHelper
    lateinit var sharedPreferences: SharedPreferences
    lateinit var am: AlarmManager
    var currentDate: Long = 0
    lateinit var fadeOutAnimation: AlphaAnimation
    lateinit var fadeInAnimation: AlphaAnimation

    // 뒤로가기 시간
    private val FINISH_INTERVAL_TIME: Long = 2000
    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.activity = this

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null

        dbHelper = DBHelper(this, dbName, dbVersion)
        sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
        fadeOutAnimation = AlphaAnimation(1f, 1f).apply {
            duration = 300
        }
        fadeInAnimation = AlphaAnimation(0f, 1f).apply {
            duration = 300
        }
        val monthFormat = SimpleDateFormat("MMMM", Locale.ENGLISH)
        binding.month.typeface = Typeface.createFromAsset(assets, "fonts/gmsans_bold.otf")
        binding.month.text = monthFormat.format(Date(System.currentTimeMillis()))
//        startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))

        am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val datas = dbHelper.getAllData()
        for (data in datas) {
            val noti_intent = Intent(this, TimeReceiver::class.java)
            noti_intent.putExtra("ID", data.id)

            val endCalendar = Calendar.getInstance().apply {
                timeInMillis = data.endTime
            }

            if (data.type == LMSType.HOMEWORK) {
                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_1HOUR_HW, false)) {
                    val triggerTime = endCalendar.timeInMillis - 1 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 1)
                    val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 1, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_2HOUR_HW, false)) {
                    val triggerTime = endCalendar.timeInMillis - 2 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 2)
                    val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 2, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_6HOUR_HW, false)) {
                    val triggerTime = endCalendar.timeInMillis - 6 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 6)
                    val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 3, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_12HOUR_HW, false)) {
                    val triggerTime = endCalendar.timeInMillis - 12 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 12)
                    val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 4, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_24HOUR_HW, false)) {
                    val triggerTime = endCalendar.timeInMillis - 24 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 24)
                    val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 5, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } else if (data.type == LMSType.LESSON || data.type == LMSType.SUP_LESSON) {
                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_1HOUR_LEC, false)) {
                    val triggerTime = endCalendar.timeInMillis - 1 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 1)
                    val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 6, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_2HOUR_LEC, false)) {
                    val triggerTime = endCalendar.timeInMillis - 2 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 2)
                    val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 7, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_6HOUR_LEC, false)) {
                    val triggerTime = endCalendar.timeInMillis - 6 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 6)
                    val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 8, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_12HOUR_LEC, false)) {
                    val triggerTime = endCalendar.timeInMillis - 12 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 12)
                    val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 9, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_24HOUR_LEC, false)) {
                    val triggerTime = endCalendar.timeInMillis - 24 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 24)
                    val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 10, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
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

        binding.adView.loadAd(adRequest.build())

        val todayWork = dbHelper.getItemAtLastDate(System.currentTimeMillis()).toMutableList().apply {
            sortWith( compareBy ({ it.isFinished }, {it.type}))
        }
        binding.mainWorkView.adapter = MainWorkAdapter(todayWork)
        binding.mainWorkView.layoutManager = LinearLayoutManager(this)
        binding.tvNoDeadline.visibility = if (todayWork.isEmpty()) View.VISIBLE else View.GONE

        binding.calendarView.topbarVisible = false
        binding.calendarView.arrowColor = ContextCompat.getColor(this, R.color.black)
        currentDate = System.currentTimeMillis()
        binding.calendarView.setOnDateChangedListener { _, date, _ ->
            val work = dbHelper.getItemAtLastDate(date.date.time).toMutableList().apply {
                sortWith( compareBy ({ it.isFinished }, {it.type}))
            }
            binding.mainWorkView.let {
                it.startAnimation(fadeOutAnimation)
                it.visibility = View.INVISIBLE

                it.adapter = MainWorkAdapter(work)
                it.layoutManager = LinearLayoutManager(this)
                currentDate = date.date.time

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
        binding.calendarView.setOnMonthChangedListener { _, date ->
            binding.month.text = monthFormat.format(date.date)
        }

        setDecorators()

        // 체인지로그 Dialog

        val changelog_dialog_builder = AlertDialog.Builder(this)
        val inflator = layoutInflater

        val changelog_dialog_view = inflator.inflate(R.layout.dialog_changelog, null)
        changelog_dialog_builder.setView(changelog_dialog_view)

        val changelog_content = changelog_dialog_view.findViewById<TextView>(R.id.changelog_content)

        changelog_dialog_builder.setPositiveButton(R.string.ok) { dialog, id ->
            dialog.cancel()
        }

        val version: String
        try {
            val i = packageManager.getPackageInfo(packageName, 0)
            version = i.versionName
            changelog_dialog_builder.setTitle(String.format("%s %s", getString(R.string.real_app_name), version + " " + getString(R.string.changelog)))
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        val changelog_dialog = changelog_dialog_builder.create()

        // raw에서 체인지로그 파일 불러오기
        try {
            val inputStream = resources.openRawResource(R.raw.thischangelog)
            if (inputStream != null) {
                val stream = InputStreamReader(inputStream, Charset.forName("utf-8"))
                val buffer = BufferedReader(stream as Reader)

                var read: String
                val sb = StringBuilder()


                buffer.lineSequence().forEach {
                    sb.append(it)
                }
                inputStream.close()

                changelog_content.text = Html.fromHtml(sb.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val pi = packageManager.getPackageInfo(packageName, 0)
            val nowVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pi.longVersionCode.toInt()
            else pi.versionCode
            val getVersion = sharedPreferences.getInt(SharedGroup.LAST_VERSION, 0)

            if (nowVersion > getVersion) {
                sharedPreferences.edit().putInt(SharedGroup.LAST_VERSION, nowVersion).apply()
                changelog_dialog.show()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

    }

    fun setDecorators() {
        binding.calendarView.removeDecorators()

        val sundayDecorator = SundayDecorator(this)
        val saturdayDecorator = SaturdayDecorator(this)
        val todayDecorator = OneDayDecorator(this).apply {
            setDate(Date(System.currentTimeMillis()))
        }

        binding.calendarView.addDecorators(sundayDecorator, saturdayDecorator, todayDecorator)
        if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            binding.calendarView.addDecorator(NightModeDecorator(this))
        }

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
            binding.calendarView.addDecorator(decorator)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        when (requestCode) {
            REFRESH_MAIN_WORK -> {
                if (resultCode == Activity.RESULT_OK) {
                    val work = dbHelper.getItemAtLastDate(currentDate).toMutableList().apply {
                        sortWith( compareBy ({ it.isFinished }, {it.type}))
                    }
                    binding.mainWorkView.adapter = MainWorkAdapter(work)
                    binding.mainWorkView.layoutManager = LinearLayoutManager(this)
                    binding.tvNoDeadline.visibility = if (work.isEmpty()) View.VISIBLE else View.GONE

                    setDecorators()
                }
            }

            SETTINGS_CHANGED -> {
                val datas = dbHelper.getAllData()
                for (data in datas) {
                    val noti_intent = Intent(this, TimeReceiver::class.java)
                    noti_intent.putExtra("ID", data.id)

                    val endCalendar = Calendar.getInstance().apply {
                        timeInMillis = data.endTime
                    }

                    if (data.type == LMSType.HOMEWORK) {
                        if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_1HOUR_HW, false)) {
                            val triggerTime = endCalendar.timeInMillis - 1 * 60 * 60 * 1000
                            noti_intent.putExtra("TRIGGER", triggerTime)
                            noti_intent.putExtra("TIME", 1)
                            val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 1, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                            am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                        }

                        if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_2HOUR_HW, false)) {
                            val triggerTime = endCalendar.timeInMillis - 2 * 60 * 60 * 1000
                            noti_intent.putExtra("TRIGGER", triggerTime)
                            noti_intent.putExtra("TIME", 2)
                            val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 2, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                            am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                        }

                        if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_6HOUR_HW, false)) {
                            val triggerTime = endCalendar.timeInMillis - 6 * 60 * 60 * 1000
                            noti_intent.putExtra("TRIGGER", triggerTime)
                            noti_intent.putExtra("TIME", 6)
                            val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 3, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                            am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                        }

                        if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_12HOUR_HW, false)) {
                            val triggerTime = endCalendar.timeInMillis - 12 * 60 * 60 * 1000
                            noti_intent.putExtra("TRIGGER", triggerTime)
                            noti_intent.putExtra("TIME", 12)
                            val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 4, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                            am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                        }

                        if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_24HOUR_HW, false)) {
                            val triggerTime = endCalendar.timeInMillis - 24 * 60 * 60 * 1000
                            noti_intent.putExtra("TRIGGER", triggerTime)
                            noti_intent.putExtra("TIME", 24)
                            val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 5, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                            am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                        }
                    } else if (data.type == LMSType.LESSON || data.type == LMSType.SUP_LESSON) {
                        if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_1HOUR_LEC, false)) {
                            val triggerTime = endCalendar.timeInMillis - 1 * 60 * 60 * 1000
                            noti_intent.putExtra("TRIGGER", triggerTime)
                            noti_intent.putExtra("TIME", 1)
                            val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 6, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                            am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                        }

                        if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_2HOUR_LEC, false)) {
                            val triggerTime = endCalendar.timeInMillis - 2 * 60 * 60 * 1000
                            noti_intent.putExtra("TRIGGER", triggerTime)
                            noti_intent.putExtra("TIME", 2)
                            val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 7, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                            am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                        }

                        if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_6HOUR_LEC, false)) {
                            val triggerTime = endCalendar.timeInMillis - 6 * 60 * 60 * 1000
                            noti_intent.putExtra("TRIGGER", triggerTime)
                            noti_intent.putExtra("TIME", 6)
                            val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 8, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                            am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                        }

                        if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_12HOUR_LEC, false)) {
                            val triggerTime = endCalendar.timeInMillis - 12 * 60 * 60 * 1000
                            noti_intent.putExtra("TRIGGER", triggerTime)
                            noti_intent.putExtra("TIME", 12)
                            val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 9, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                            am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                        }

                        if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_24HOUR_LEC, false)) {
                            val triggerTime = endCalendar.timeInMillis - 24 * 60 * 60 * 1000
                            noti_intent.putExtra("TRIGGER", triggerTime)
                            noti_intent.putExtra("TIME", 24)
                            val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + 10, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                            am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                        }
                    }
                }
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {

        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            val icon = item.icon.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    colorFilter = BlendModeColorFilter(Color.WHITE, BlendMode.SRC_IN)
                } else {
                    setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                }
            }
            item.icon = icon
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_set_today -> {
                binding.calendarView.setCurrentDate(Date(System.currentTimeMillis()))
                currentDate = System.currentTimeMillis()
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