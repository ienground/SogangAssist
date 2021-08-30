package net.ienlab.sogangassist.activity

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.*
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import kotlinx.coroutines.*
import net.ienlab.sogangassist.BuildConfig
import net.ienlab.sogangassist.adapter.MainWorkAdapter
import net.ienlab.sogangassist.constant.SharedKey
import net.ienlab.sogangassist.data.LMSClass
import net.ienlab.sogangassist.database.DBHelper
import net.ienlab.sogangassist.databinding.ActivityMainBinding
import net.ienlab.sogangassist.decorators.*
import net.ienlab.sogangassist.receiver.TimeReceiver
import net.ienlab.sogangassist.database.*
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.adapter.MainWorkAdapter2
import net.ienlab.sogangassist.constant.DefaultValue
import net.ienlab.sogangassist.receiver.ReminderReceiver
import net.ienlab.sogangassist.utils.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs

const val TAG = "SogangAssistTAG"
const val testDevice = "48BC2075D1B2D4652C27A690C6EF0D6F"

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    lateinit var dbHelper: DBHelper
    lateinit var notiDBHelper: NotiDBHelper
    lateinit var sharedPreferences: SharedPreferences
    lateinit var am: AlarmManager
    lateinit var fadeOutAnimation: AlphaAnimation
    lateinit var fadeInAnimation: AlphaAnimation

    // StartActivityForResult
    lateinit var editActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var notificationsActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var settingsActivityLauncher: ActivityResultLauncher<Intent>

    // 뒤로가기 시간
    private val FINISH_INTERVAL_TIME: Long = 2000
    private var backPressedTime: Long = 0

    lateinit var currentDecorator: CurrentDecorator
    var thisCurrentDate: Long = 0

    lateinit var typefaceBold: Typeface
    lateinit var typefaceRegular: Typeface

    lateinit var notiBadgeText: TextView
    lateinit var storage: AppStorage

    val decorators: MutableMap<Long, DayViewDecorator> = mutableMapOf()

    private val deleteCallbackListener = object: ClickCallbackListener {
        override fun callBack(position: Int, items: List<LMSClass>, adapter: MainWorkAdapter) {
            setEachDecorator(items[position].endTime)
            Snackbar.make(window.decorView.rootView, if (items[position].isFinished) getString(R.string.marked_as_finish) else getString(R.string.marked_as_not_finish), Snackbar.LENGTH_SHORT).setAction(R.string.undo) {
                items[position].isFinished = !items[position].isFinished
                dbHelper.updateItemById(items[position])
                adapter.notifyItemChanged(position)
                setEachDecorator(items[position].endTime)
            }.show()
        }
    }

    private val clickCallbackListener = object: ClickCallbackListener {
        override fun callBack(position: Int, items: List<LMSClass>, adapter: MainWorkAdapter) {
            Intent(applicationContext, EditActivity::class.java).apply {
                putExtra("ID", items[position].id)
                editActivityLauncher.launch(this)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.activity = this

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null

        FirebaseInAppMessaging.getInstance().isAutomaticDataCollectionEnabled = true
        if (BuildConfig.DEBUG) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }
                val token = task.result
                Log.d(TAG, token)
            })

            // Firebase In App Messaging
            FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Installation ID: " + task.result)
                } else {
                    Log.e(TAG, "Unable to get Installation ID")
                }
            }
        }


        dbHelper = DBHelper(this, DBHelper.dbName, DBHelper.dbVersion)
        notiDBHelper = NotiDBHelper(this, NotiDBHelper.dbName, NotiDBHelper.dbVersion)
        sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
        fadeOutAnimation = AlphaAnimation(1f, 0f).apply { duration = 300 }
        fadeInAnimation = AlphaAnimation(0f, 1f).apply { duration = 300 }
        am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        currentDecorator = CurrentDecorator(this, Calendar.getInstance())
        typefaceBold = Typeface.createFromAsset(assets, "fonts/Pretendard-Black.otf")
        typefaceRegular = Typeface.createFromAsset(assets, "fonts/Pretendard-Regular.otf")
        storage = AppStorage(this)

        sharedPreferences.edit().putBoolean(SharedKey.CURRENT_CALENDAR_ICON_SHOW, sharedPreferences.getBoolean(SharedKey.CALENDAR_ICON_SHOW, true)).apply()

        val monthFormat = SimpleDateFormat("MMMM", Locale.ENGLISH)
        val dateFormat = SimpleDateFormat(getString(R.string.tag_date), Locale.getDefault())

//        binding.month0.typeface = typefaceBold
//        binding.month1.typeface = typefaceBold
//        binding.month.setText(monthFormat.format(Date(System.currentTimeMillis())))
//        binding.tvNoDeadline.typeface = typefaceRegular

        binding.btnAdd.typeface = typefaceRegular

        if (storage.purchasedAds()) binding.adView.visibility = View.GONE
        val adRequest = AdRequest.Builder()
        if (BuildConfig.DEBUG) {
            RequestConfiguration.Builder()
                .setTestDeviceIds(arrayListOf(testDevice)).let {
                    MobileAds.setRequestConfiguration(it.build())
                }
        }

        binding.adView.loadAd(adRequest.build())

        // StartActiviyForResult 객체
        editActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val work = dbHelper.getItemAtLastDate(thisCurrentDate).toMutableList().apply { sortWith( compareBy ({ it.isFinished }, {it.endTime}, {it.type} )) } as ArrayList
                binding.mainWorkView.adapter = MainWorkAdapter2(work).apply {
                    setDeleteCallback(deleteCallbackListener)
                    setClickCallback(clickCallbackListener)
                }
                binding.mainWorkView.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
                binding.mainWorkView.addItemDecoration(PagerIndicator(ContextCompat.getColor(applicationContext, R.color.colorAccent), ContextCompat.getColor(applicationContext, R.color.colorPrimary)))
                PagerSnapHelper().attachToRecyclerView(binding.mainWorkView)
//                binding.tvNoDeadline.visibility = if (work.isEmpty()) View.VISIBLE else View.GONE

                val dialog = AlertDialog.Builder(this).apply {
                    val linearLayout = LinearLayout(applicationContext).apply {
                        val size = MyUtils.dpToPx(applicationContext, 16f).toInt()
                        orientation = LinearLayout.HORIZONTAL
                        setPadding(size, size, size, size)
                    }
                    val progressBar = ProgressBar(applicationContext, null, android.R.attr.progressBarStyleHorizontal).apply {
                        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                        isIndeterminate = true
                        indeterminateTintList = ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.colorAccent))
                    }

                    linearLayout.addView(progressBar)
                    setView(linearLayout)
                    setTitle(getString(R.string.load_data))
                    setCancelable(false)

                }.create()

                dialog.show()

                Handler(Looper.getMainLooper()).postDelayed({
                    setDecorators(applicationContext)
                    dialog.dismiss()
                    binding.calendarView.visibility = View.VISIBLE
                }, 1000)
            }
            binding.adView.visibility = if (storage.purchasedAds()) View.GONE else View.VISIBLE
        }

        notificationsActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            setupBadge()
        }

        settingsActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            binding.adView.visibility = if (storage.purchasedAds()) View.GONE else View.VISIBLE
        }

        // 리뷰 요청
        val installedDate = packageManager.getPackageInfo(packageName, 0).firstInstallTime
        if (abs(installedDate - System.currentTimeMillis()) >= 6 * AlarmManager.INTERVAL_DAY) {
            val reviewManager = ReviewManagerFactory.create(this)
            val reviewRequest = reviewManager.requestReviewFlow()
            reviewRequest.addOnCompleteListener {
                if (reviewRequest.isSuccessful && !sharedPreferences.getBoolean(SharedKey.REVIEW_WRITE, false)) {
                    MyBottomSheetDialog(this).apply {
                        val view = layoutInflater.inflate(R.layout.dialog, LinearLayout(applicationContext), false)
                        val icon: ImageView = view.findViewById(R.id.imgLogo)
                        val tvTitle: TextView = view.findViewById(R.id.tv_title)
                        val tvContent: TextView = view.findViewById(R.id.tv_content)
                        val btnPositive: LinearLayout = view.findViewById(R.id.btn_positive)
                        val btnNegative: LinearLayout = view.findViewById(R.id.btn_negative)
                        val tvPositive: TextView = view.findViewById(R.id.btn_positive_text)
                        val tvNegative: TextView = view.findViewById(R.id.btn_negative_text)

                        tvTitle.typeface = typefaceBold
                        tvContent.typeface = typefaceRegular
                        tvPositive.typeface = typefaceRegular
                        tvNegative.typeface = typefaceRegular

                        icon.setImageResource(R.drawable.ic_rate_review)
                        tvTitle.text = getString(R.string.review_title)
                        tvContent.text = getString(R.string.review_content)

                        btnPositive.setOnClickListener {
                            dismiss()
                            val reviewInfo = reviewRequest.result
                            val reviewFlow = reviewManager.launchReviewFlow(this@MainActivity, reviewInfo)
                            reviewFlow.addOnCompleteListener {
                                sharedPreferences.edit().putBoolean(SharedKey.REVIEW_WRITE, true).apply()
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

        // NotificationListener 꺼져 있을 때
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
                tvTitle.typeface = typefaceBold
                tvContent.typeface = typefaceRegular
                tvPositive.typeface = typefaceRegular
                tvNegative.typeface = typefaceRegular

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

        // 하루 시작, 끝 리마인더 알람 만들기
        val morningReminderCalendar = Calendar.getInstance().apply {
            val time = sharedPreferences.getInt(SharedKey.TIME_MORNING_REMINDER, DefaultValue.TIME_MORNING_REMINDER)

            set(Calendar.HOUR_OF_DAY, time / 60)
            set(Calendar.MINUTE, time % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val nightReminderCalendar = Calendar.getInstance().apply {
            val time = sharedPreferences.getInt(SharedKey.TIME_NIGHT_REMINDER, DefaultValue.TIME_NIGHT_REMINDER)

            set(Calendar.HOUR_OF_DAY, time / 60)
            set(Calendar.MINUTE, time % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // set reminder alarm

        val morningReminderIntent = Intent(this, ReminderReceiver::class.java).apply { putExtra(ReminderReceiver.TYPE, ReminderReceiver.MORNING) }
        val nightReminderIntent = Intent(this, ReminderReceiver::class.java).apply { putExtra(ReminderReceiver.TYPE, ReminderReceiver.NIGHT) }

        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
        am.setRepeating(AlarmManager.RTC_WAKEUP, morningReminderCalendar.timeInMillis, AlarmManager.INTERVAL_DAY, PendingIntent.getBroadcast(this, 14402, morningReminderIntent, flag))

        am.setRepeating(AlarmManager.RTC_WAKEUP, nightReminderCalendar.timeInMillis, AlarmManager.INTERVAL_DAY, PendingIntent.getBroadcast(this, 14502, nightReminderIntent, flag))

        val datas = dbHelper.getAllData()
        for (data in datas) {
            val notiIntent = Intent(this, TimeReceiver::class.java).apply { putExtra("ID", data.id) }
            val hours = listOf(1, 2, 6, 12, 24)
            val minutes = listOf(3, 5, 10, 20, 30)

            if (data.endTime < System.currentTimeMillis()) continue

            when (data.type) {
                LMSClass.TYPE_HOMEWORK, LMSClass.TYPE_LESSON, LMSClass.TYPE_SUP_LESSON, LMSClass.TYPE_TEAMWORK -> {
                    hours.forEachIndexed { index, i ->
                        val triggerTime = data.endTime - i * 60 * 60 * 1000
                        notiIntent.putExtra("TRIGGER", triggerTime)
                        notiIntent.putExtra("TIME", i)
                        val pendingIntent = PendingIntent.getBroadcast(this, data.id * 100 + index + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                        am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    }
                }
                LMSClass.TYPE_ZOOM, LMSClass.TYPE_EXAM -> {
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

        val todayWork = dbHelper.getItemAtLastDate(System.currentTimeMillis()).toMutableList().apply { sortWith( compareBy ({ it.isFinished }, {it.endTime}, {it.type} )) } as ArrayList

        binding.mainWorkView.adapter = MainWorkAdapter2(todayWork).apply {
            setDeleteCallback(deleteCallbackListener)
            setClickCallback(clickCallbackListener)
        }
        binding.mainWorkView.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
        binding.mainWorkView.addItemDecoration(PagerIndicator(ContextCompat.getColor(applicationContext, R.color.colorAccent), ContextCompat.getColor(applicationContext, R.color.colorPrimary)))
        PagerSnapHelper().attachToRecyclerView(binding.mainWorkView)
//        binding.tvNoDeadline.visibility = if (todayWork.isEmpty()) View.VISIBLE else View.GONE
        binding.btnAdd.setOnClickListener {
            editActivityLauncher.launch(Intent(this, EditActivity::class.java))
        }
        thisCurrentDate = System.currentTimeMillis()

        // 달력
        var beforeDate = binding.calendarView.currentDate.date
        binding.calendarView.apply {
            topbarVisible = false
            arrowColor = ContextCompat.getColor(applicationContext, R.color.black)
            setOnDateChangedListener { widget, date, _ ->
//                binding.tagEvents.text = getString(R.string.events_today, dateFormat.format(date.date))
                val work = dbHelper.getItemAtLastDate(date.date.time).toMutableList().apply {
                    sortWith( compareBy ({ it.isFinished }, {it.endTime}, {it.type} ))
                } as ArrayList

                binding.mainWorkView.apply {
                    startAnimation(fadeOutAnimation)
                    visibility = View.INVISIBLE

                    adapter = MainWorkAdapter2(work).apply {
                        setDeleteCallback(deleteCallbackListener)
                        setClickCallback(clickCallbackListener)
                    }
                    layoutManager = LinearLayoutManager(applicationContext)
                    thisCurrentDate = date.date.time

                    visibility = View.VISIBLE
                    startAnimation(fadeInAnimation)
                }

//                binding.tvNoDeadline.apply {
//                    if (work.isEmpty()) {
//                        startAnimation(fadeOutAnimation)
//                        visibility = View.INVISIBLE
//                        visibility = View.VISIBLE
//                        startAnimation(fadeInAnimation)
//                    } else {
//                        visibility = View.INVISIBLE
//                    }
//                }
            }
            setOnMonthChangedListener { _, date ->
//                if (beforeDate > date.date) {
//                    binding.month.setInAnimation(applicationContext, R.anim.slide_in_left)
//                    binding.month.setOutAnimation(applicationContext, R.anim.slide_out_right)
//                } else {
//                    binding.month.setInAnimation(applicationContext, R.anim.slide_in_right)
//                    binding.month.setOutAnimation(applicationContext, R.anim.slide_out_left)
//                }
//
//                binding.month.setText(monthFormat.format(date.date))
                beforeDate = date.date
            }
        }

        val dialog = AlertDialog.Builder(this).apply {
            val linearLayout = LinearLayout(applicationContext).apply {
                val size = MyUtils.dpToPx(applicationContext, 16f).toInt()
                orientation = LinearLayout.HORIZONTAL
                setPadding(size, size, size, size)
            }
            val progressBar = ProgressBar(applicationContext, null, android.R.attr.progressBarStyleHorizontal).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                isIndeterminate = true
                indeterminateTintList = ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.colorAccent))
            }

            linearLayout.addView(progressBar)
            setView(linearLayout)
            setTitle(getString(R.string.load_data))
            setCancelable(false)

        }.create()

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            setDecorators(applicationContext)
            dialog.dismiss()
            binding.calendarView.visibility = View.VISIBLE
        }, 1000)

        binding.btnMoveToday.setOnClickListener {
            binding.calendarView.setCurrentDate(Date(System.currentTimeMillis()))
            thisCurrentDate = System.currentTimeMillis()
        }

        // 체인지로그
        val info = packageManager.getPackageInfo(packageName, 0)
        val currentVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode.toInt() else info.versionCode
        val lastVersion = sharedPreferences.getInt(SharedKey.LAST_VERSION, 0)

        if (currentVersion > lastVersion) {
            sharedPreferences.edit().putInt(SharedKey.LAST_VERSION, currentVersion).apply()
            MyBottomSheetDialog(this).apply {
                val view = layoutInflater.inflate(R.layout.dialog_changelog, LinearLayout(applicationContext), false)
                val tvVersion: TextView = view.findViewById(R.id.tv_version)
                val tvContent: TextView = view.findViewById(R.id.content)

                tvVersion.typeface = typefaceBold
                tvContent.typeface = typefaceRegular

                tvVersion.text = "${getString(R.string.real_app_name)} ${BuildConfig.VERSION_NAME}"
                tvContent.text = MyUtils.fromHtml(MyUtils.readTextFromRaw(resources, R.raw.changelog))

                setContentView(view)
            }.show()
        }

        val id = intent.getIntExtra("ID", -1)
        val notiId = intent.getIntExtra("NOTI_ID", -1)
        if (id != -1) {
            editActivityLauncher.launch(Intent(this, EditActivity::class.java).apply { putExtra("ID", id) })
        }
        if (notiId != -1) {
            val data = notiDBHelper.getItemById(notiId).apply { isRead = true }
            notiDBHelper.updateItemById(data)
        }
    }

    fun setDecorators(context: Context) {
        val sharedPreferences = context.getSharedPreferences("${context.packageName}_preferences", Context.MODE_PRIVATE)
        val dbHelper = DBHelper(context, DBHelper.dbName, DBHelper.dbVersion)

        val datas = dbHelper.getAllData()
        val endTimes = ArrayList<Long>()
        val timeCount = mutableMapOf<Long, IntArray>()

        binding.calendarView.removeDecorators()

        val weekdayDecorator = WeekdayDecorator(context)
        val sundayDecorator = SundayDecorator(context)
        val saturdayDecorator = SaturdayDecorator(context)
        val todayDecorator = OneDayDecorator(context).apply {
            setDate(Date(System.currentTimeMillis()))
        }

        binding.calendarView.addDecorators(weekdayDecorator, sundayDecorator, saturdayDecorator, todayDecorator)
        if (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            binding.calendarView.addDecorator(NightModeDecorator(context))
        }

        for (data in datas) {
            Calendar.getInstance().apply {
                timeInMillis = data.endTime
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                if (timeInMillis !in endTimes) {
                    endTimes.add(timeInMillis)
                }

                if (timeInMillis in timeCount.keys) {
                    val value = timeCount[timeInMillis] ?: intArrayOf(0, 0)
                    value[if (data.isFinished) 1 else 0] += 1
                    timeCount[timeInMillis] = value
                } else {
                    if (data.isFinished) {
                        timeCount[timeInMillis] = intArrayOf(0, 1)
                    } else {
                        timeCount[timeInMillis] = intArrayOf(1, 0)
                    }
                }
            }
        }

        if (sharedPreferences.getBoolean(SharedKey.CURRENT_CALENDAR_ICON_SHOW, true)) {
            for (time in endTimes) {
                val decorator = EventDecorator2(context, time)
                binding.calendarView.addDecorator(decorator)
                decorators[time] = decorator
            }
        } else {
            for (time in timeCount) {
                val decorator = EventDecorator(ContextCompat.getColor(context, R.color.colorAccent), time.value, arrayListOf(CalendarDay.from(Date(time.key))))
                binding.calendarView.addDecorator(decorator)
                decorators[time.key] = decorator
            }
        }
    }

    fun setupBadge() {
        val notificationData = notiDBHelper.getAllItem()
        var count = 0
        notificationData.forEach { if (!it.isRead) count++ }
        if (::notiBadgeText.isInitialized) {
            when {
                count == 0 -> notiBadgeText.visibility = View.GONE
                count <= 9 -> {
                    notiBadgeText.text = count.toString()
                    notiBadgeText.visibility = View.VISIBLE
                }
                else -> {
                    notiBadgeText.text = "9+"
                    notiBadgeText.textSize = 8f
                    notiBadgeText.visibility = View.VISIBLE
                }
            }
        }
    }

    fun setEachDecorator(time: Long) {
        val sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
        val dbHelper = DBHelper(this, DBHelper.dbName, DBHelper.dbVersion)
        val data = dbHelper.getItemAtLastDate(time)
        val timeCount = intArrayOf(0, 0)
        val decoratorTime = Calendar.getInstance().apply {
            timeInMillis = time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        data.forEach {
            timeCount[if (it.isFinished) 1 else 0] += 1
        }

        binding.calendarView.removeDecorator(decorators[decoratorTime])
        if (sharedPreferences.getBoolean(SharedKey.CURRENT_CALENDAR_ICON_SHOW, true)) {
            val decorator = EventDecorator2(this, decoratorTime)
            binding.calendarView.addDecorator(decorator)
            decorators[decoratorTime] = decorator
        } else {
            val decorator = EventDecorator(ContextCompat.getColor(this, R.color.colorAccent), timeCount, arrayListOf(CalendarDay.from(Date(time))))
            binding.calendarView.addDecorator(decorator)
            decorators[decoratorTime] = decorator
        }
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        val menuNoti = menu.findItem(R.id.menu_notifications)
        val actionView = menuNoti.actionView
        notiBadgeText = actionView.findViewById(R.id.tv_badge)
        notiBadgeText.typeface = typefaceRegular

        setupBadge()

        actionView.setOnClickListener {
            onOptionsItemSelected(menuNoti)
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_notifications -> {
                notificationsActivityLauncher.launch(Intent(this, NotificationsActivity::class.java))
            }

            R.id.menu_settings -> {
                settingsActivityLauncher.launch(Intent(this, SettingsActivity::class.java))
            }

            R.id.menu_help -> {
                MyBottomSheetDialog(this).apply {
                    val view = layoutInflater.inflate(R.layout.dialog_help, LinearLayout(applicationContext), false)
                    val tvVersion: TextView = view.findViewById(R.id.tv_version)
                    val tvContent: TextView = view.findViewById(R.id.content)
                    val btnNoti: Button = view.findViewById(R.id.btn_noti)

                    tvVersion.typeface = typefaceBold
                    tvContent.typeface = typefaceRegular
                    btnNoti.typeface = typefaceRegular

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
}