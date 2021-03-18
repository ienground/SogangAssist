package net.ienlab.sogangassist.activity

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
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.play.core.review.ReviewManagerFactory
import com.prolificinteractive.materialcalendarview.CalendarDay
import net.ienlab.sogangassist.BuildConfig
import net.ienlab.sogangassist.adapter.MainWorkAdapter
import net.ienlab.sogangassist.constant.SharedGroup
import net.ienlab.sogangassist.data.LMSClass
import net.ienlab.sogangassist.database.DBHelper
import net.ienlab.sogangassist.databinding.ActivityMainBinding
import net.ienlab.sogangassist.decorators.*
import net.ienlab.sogangassist.receiver.TimeReceiver
import net.ienlab.sogangassist.utils.MyUtils
import net.ienlab.sogangassist.database.*
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.constant.DefaultValue
import net.ienlab.sogangassist.receiver.ReminderReceiver
import net.ienlab.sogangassist.utils.MyBottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

val TAG = "SogangAssistTAG"
val REFRESH_MAIN_WORK = 2
val SETTINGS_CHANGED = 3
val NOTI_REFRESH = 4
val testDevice = "C539956A287753EFC92BF75B93D6D291"

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    lateinit var dbHelper: DBHelper
    lateinit var notiDBHelper: NotiDBHelper
    lateinit var sharedPreferences: SharedPreferences
    lateinit var am: AlarmManager
    lateinit var fadeOutAnimation: AlphaAnimation
    lateinit var fadeInAnimation: AlphaAnimation

    // 뒤로가기 시간
    private val FINISH_INTERVAL_TIME: Long = 2000
    private var backPressedTime: Long = 0

    lateinit var currentDecorator: CurrentDecorator
    var thisCurrentDate: Long = 0

    lateinit var gmSansBold: Typeface
    lateinit var gmSansMedium: Typeface

    lateinit var notiBadgeText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.activity = this

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null

        dbHelper = DBHelper(this, DBHelper.dbName, DBHelper.dbVersion)
        notiDBHelper = NotiDBHelper(this, NotiDBHelper.dbName, NotiDBHelper.dbVersion)
        view = window.decorView.rootView
        sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
        fadeOutAnimation = AlphaAnimation(1f, 0f).apply { duration = 300 }
        fadeInAnimation = AlphaAnimation(0f, 1f).apply { duration = 300 }
        currentDecorator = CurrentDecorator(this, Calendar.getInstance())

        val monthFormat = SimpleDateFormat("MMMM", Locale.ENGLISH)
        gmSansBold = Typeface.createFromAsset(assets, "fonts/gmsans_bold.otf")
        gmSansMedium = Typeface.createFromAsset(assets, "fonts/gmsans_medium.otf")

        val dateFormat = SimpleDateFormat(getString(R.string.tag_date), Locale.getDefault())

        binding.month0.typeface = gmSansBold
        binding.month1.typeface = gmSansBold
        binding.month.setText(monthFormat.format(Date(System.currentTimeMillis())))
        binding.tagEvents.text = getString(R.string.events_today, dateFormat.format(Calendar.getInstance().time))

        binding.tagSchedule.typeface = gmSansMedium
        binding.tagEvents.typeface = gmSansMedium
        binding.tvNoDeadline.typeface = gmSansMedium
        binding.tvAdd.typeface = gmSansMedium

        if (BuildConfig.DEBUG) binding.adView.visibility = View.GONE

        val installedDate = packageManager.getPackageInfo(packageName, 0).firstInstallTime
        if (abs(installedDate - System.currentTimeMillis()) >= 6 * AlarmManager.INTERVAL_DAY) {
            val reviewManager = ReviewManagerFactory.create(this)
            val reviewRequest = reviewManager.requestReviewFlow()
            reviewRequest.addOnCompleteListener {
                if (reviewRequest.isSuccessful && !sharedPreferences.getBoolean(SharedGroup.REVIEW_WRITE, false)) {
                    MyBottomSheetDialog(this).apply {
                        val view = layoutInflater.inflate(R.layout.dialog, LinearLayout(applicationContext), false)
                        val icon: ImageView = view.findViewById(R.id.imgLogo)
                        val tvTitle: TextView = view.findViewById(R.id.tv_title)
                        val tvContent: TextView = view.findViewById(R.id.tv_content)
                        val btnPositive: LinearLayout = view.findViewById(R.id.btn_positive)
                        val btnNegative: LinearLayout = view.findViewById(R.id.btn_negative)
                        val imgPositive: ImageView = view.findViewById(R.id.btn_positive_img)
                        val imgNegative: ImageView = view.findViewById(R.id.btn_negative_img)
                        val tvPositive: TextView = view.findViewById(R.id.btn_positive_text)
                        val tvNegative: TextView = view.findViewById(R.id.btn_negative_text)

                        tvTitle.typeface = gmSansBold
                        tvContent.typeface = gmSansMedium
                        tvPositive.typeface = gmSansMedium
                        tvNegative.typeface = gmSansMedium

                        icon.setImageResource(R.drawable.ic_rate_review)
                        tvTitle.text = getString(R.string.review_title)
                        tvContent.text = getString(R.string.review_content)

                        btnPositive.setOnClickListener {
                            dismiss()
                            val reviewInfo = reviewRequest.result
                            val reviewFlow = reviewManager.launchReviewFlow(this@MainActivity, reviewInfo)
                            reviewFlow.addOnCompleteListener {
                                sharedPreferences.edit().putBoolean(SharedGroup.REVIEW_WRITE, true).apply()
                            }
                        }
                        btnNegative.setOnClickListener {
                            dismiss()
                        }

                        setContentView(view)
                    }.show()
                }
            }
        }

        // 하루 시작, 끝 리마인더 알람 만들기
        val morningReminderCalendar = Calendar.getInstance().apply {
            val time = sharedPreferences.getInt(SharedGroup.TIME_MORNING_REMINDER, DefaultValue.TIME_MORNING_REMINDER)

            set(Calendar.HOUR_OF_DAY, time / 60)
            set(Calendar.MINUTE, time % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val nightReminderCalendar = Calendar.getInstance().apply {
            val time = sharedPreferences.getInt(SharedGroup.TIME_NIGHT_REMINDER, DefaultValue.TIME_NIGHT_REMINDER)

            set(Calendar.HOUR_OF_DAY, time / 60)
            set(Calendar.MINUTE, time % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val morningReminderIntent = Intent(this, ReminderReceiver::class.java).apply { putExtra(
            ReminderReceiver.TYPE, ReminderReceiver.MORNING) }
        val nightReminderIntent = Intent(this, ReminderReceiver::class.java).apply { putExtra(
            ReminderReceiver.TYPE, ReminderReceiver.NIGHT) }

        if (!MyUtils.isNotiPermissionAllowed(this)) {
            MyBottomSheetDialog(this).apply {
                dismissWithAnimation = true

                val view = layoutInflater.inflate(R.layout.dialog, LinearLayout(context), false)
                val imgLogo: ImageView = view.findViewById(R.id.imgLogo)
                val tvTitle: TextView = view.findViewById(R.id.tv_title)
                val tvContent: TextView = view.findViewById(R.id.tv_content)
                val btnPositive: LinearLayout = view.findViewById(R.id.btn_positive)
                val btnNegative: LinearLayout = view.findViewById(R.id.btn_negative)
                val tvPositive: TextView = view.findViewById(R.id.btn_positive_text)
                val tvNegative: TextView = view.findViewById(R.id.btn_negative_text)

                imgLogo.setImageResource(R.drawable.ic_notification)
                tvTitle.typeface = gmSansBold
                tvContent.typeface = gmSansMedium
                tvPositive.typeface = gmSansMedium
                tvNegative.typeface = gmSansMedium

                tvTitle.text = getString(R.string.intro_page2_title)
                tvContent.text = getString(R.string.intro_page2_exp)
                tvPositive.text = getString(R.string.ok)
                tvNegative.text = getString(R.string.cancel)

                btnPositive.setOnClickListener {
                    startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                    dismiss()
                }

                btnNegative.setOnClickListener {
                    dismiss()
                }

                setContentView(view)
            }.show()
        }

        am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setRepeating(AlarmManager.RTC_WAKEUP, morningReminderCalendar.timeInMillis, AlarmManager.INTERVAL_DAY,
            PendingIntent.getBroadcast(this, 14402, morningReminderIntent, PendingIntent.FLAG_UPDATE_CURRENT))

        am.setRepeating(AlarmManager.RTC_WAKEUP, nightReminderCalendar.timeInMillis, AlarmManager.INTERVAL_DAY,
            PendingIntent.getBroadcast(this, 14502, nightReminderIntent, PendingIntent.FLAG_UPDATE_CURRENT))

        val datas = dbHelper.getAllData()
        for (data in datas) {
            val notiIntent = Intent(this, TimeReceiver::class.java).apply { putExtra("ID", data.id) }
            val hours = listOf(1, 2, 6, 12, 24)
            val minutes = listOf(3, 5, 10, 20, 30)

            if (data.endTime < System.currentTimeMillis()) continue

            when (data.type) {
                LMSClass.TYPE_HOMEWORK, LMSClass.TYPE_LESSON, LMSClass.TYPE_SUP_LESSON -> {
                    hours.forEachIndexed { index, i ->
                        val triggerTime = data.endTime - i * 60 * 60 * 1000
                        notiIntent.putExtra("TRIGGER", triggerTime)
                        notiIntent.putExtra("TIME", i)
                        val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + index + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                        am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    }
                }
                LMSClass.TYPE_ZOOM -> {
                    minutes.forEachIndexed { index, i ->
                        val triggerTime = data.endTime - i * 60 * 1000
                        notiIntent.putExtra("TRIGGER", triggerTime)
                        notiIntent.putExtra("MINUTE", i)
                        val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + index + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                        am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    }
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
        binding.btnAdd.setOnClickListener {
            startActivityForResult(Intent(this, EditActivity::class.java), REFRESH_MAIN_WORK)
        }

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

        binding.btnMoveToday.setOnClickListener {
                binding.calendarView.setCurrentDate(Date(System.currentTimeMillis()))
                thisCurrentDate = System.currentTimeMillis()
        }

        val info = packageManager.getPackageInfo(packageName, 0)
        val currentVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode.toInt() else info.versionCode
        val lastVersion = sharedPreferences.getInt(SharedGroup.LAST_VERSION, 0)

        if (currentVersion > lastVersion) {
            sharedPreferences.edit().putInt(SharedGroup.LAST_VERSION, currentVersion).apply()
            MyBottomSheetDialog(this).apply {
                val view = layoutInflater.inflate(R.layout.dialog_changelog, LinearLayout(applicationContext), false)
                val tvVersion: TextView = view.findViewById(R.id.tv_version)
                val tvContent: TextView = view.findViewById(R.id.content)

                tvVersion.typeface = gmSansBold
                tvContent.typeface = gmSansMedium

                tvVersion.text = "${getString(R.string.real_app_name)} ${BuildConfig.VERSION_NAME}"
                tvContent.text = MyUtils.fromHtml(MyUtils.readTextFromRaw(resources, R.raw.changelog))

                setContentView(view)
            }.show()
        }

        val id = intent.getIntExtra("ID", -1)
        val notiId = intent.getIntExtra("NOTI_ID", -1)
        if (id != -1) {
            Intent(this, EditActivity::class.java).let {
                it.putExtra("ID", id)
                startActivityForResult(it, REFRESH_MAIN_WORK)
            }
        }
        if (notiId != -1) {
            val data = notiDBHelper.getItemById(notiId).apply { isRead = true }
            notiDBHelper.updateItemById(data)
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

            NOTI_REFRESH -> {
                setupBadge()
            }
        }
    }

    fun setupBadge() {
        val notificationData = notiDBHelper.getAllItem()
        var count = 0
        notificationData.forEach { if (!it.isRead) count++ }

        if (::notiBadgeText.isInitialized) {
            if (count == 0) notiBadgeText.visibility = View.GONE
            else {
                notiBadgeText.text = count.toString()
                notiBadgeText.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val menuNoti = menu.findItem(R.id.menu_notifications)
        val actionView = menuNoti.actionView
        notiBadgeText = actionView.findViewById(R.id.tv_badge)
        notiBadgeText.typeface = gmSansMedium

        setupBadge()

        actionView.setOnClickListener {
            onOptionsItemSelected(menuNoti)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_notifications -> {
                startActivityForResult(Intent(this, NotificationsActivity::class.java), NOTI_REFRESH)
            }

            R.id.menu_settings -> {
                startActivityForResult(Intent(this, SettingsActivity::class.java), SETTINGS_CHANGED)
            }

            R.id.menu_help -> {
                MyBottomSheetDialog(this).apply {
                    val view = layoutInflater.inflate(R.layout.dialog_help, LinearLayout(applicationContext), false)
                    val tvVersion: TextView = view.findViewById(R.id.tv_version)
                    val tvContent: TextView = view.findViewById(R.id.content)
                    val btnNoti: Button = view.findViewById(R.id.btn_noti)

                    tvVersion.typeface = gmSansBold
                    tvContent.typeface = gmSansMedium
                    btnNoti.typeface = gmSansMedium

                    tvVersion.text = getString(R.string.help)
                    tvContent.text = MyUtils.fromHtml(MyUtils.readTextFromRaw(resources, R.raw.help))
                    btnNoti.setOnClickListener {
                        startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                    }

                    setContentView(view)
                }.show()
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

    companion object {
        lateinit var view: View
    }
}