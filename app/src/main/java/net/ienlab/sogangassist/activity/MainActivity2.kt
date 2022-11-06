package net.ienlab.sogangassist.activity

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.coroutines.*
import net.ienlab.sogangassist.BuildConfig
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.adapter.MainWorkAdapter
import net.ienlab.sogangassist.constant.IntentKey
import net.ienlab.sogangassist.constant.SharedKey
import net.ienlab.sogangassist.databinding.ActivityMain2Binding
import net.ienlab.sogangassist.decorators.*
import net.ienlab.sogangassist.fragment.MainAllListFragment
import net.ienlab.sogangassist.receiver.TimeReceiver
import net.ienlab.sogangassist.room.LMSDatabase
import net.ienlab.sogangassist.room.LMSEntity
import net.ienlab.sogangassist.utils.AppStorage
import net.ienlab.sogangassist.utils.ClickCallbackListener
import net.ienlab.sogangassist.utils.MyUtils.Companion.timeZero
import net.ienlab.sogangassist.utils.MyUtils.Companion.tomorrowZero
import java.text.SimpleDateFormat
import java.util.*

class MainActivity2 : AppCompatActivity(),
        MainAllListFragment.OnFragmentInteractionListener
{

    lateinit var binding: ActivityMain2Binding

    private var lmsDatabase: LMSDatabase? = null
    lateinit var sharedPreferences: SharedPreferences
    lateinit var am: AlarmManager
    lateinit var fadeOutAnimation: AlphaAnimation
    lateinit var fadeInAnimation: AlphaAnimation

    // StartActivityForResult
    lateinit var editActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var settingsActivityLauncher: ActivityResultLauncher<Intent>

    // 뒤로가기 시간
    private val FINISH_INTERVAL_TIME: Long = 2000
    private var backPressedTime: Long = 0

    private lateinit var currentDecorator: CurrentDecorator
    private var thisCurrentDate: Long = 0
    private lateinit var monthFormat: SimpleDateFormat

    lateinit var storage: AppStorage
    private val decorators: MutableMap<Long, DayViewDecorator> = mutableMapOf()

    private val deleteCallbackListener = object: ClickCallbackListener {
        override fun callBack(position: Int, items: List<LMSEntity>, adapter: MainWorkAdapter) {
//            setEachDecorator(items[position].endTime)
            Snackbar.make(window.decorView.rootView, if (items[position].isFinished) getString(R.string.marked_as_finish) else getString(R.string.marked_as_not_finish), Snackbar.LENGTH_SHORT).setAction(R.string.undo) {
                items[position].isFinished = !items[position].isFinished
//                dbHelper.updateItemById(items[position])
                adapter.notifyItemChanged(position)
//                setEachDecorator(items[position].endTime)
            }.show()
        }
    }

    private val clickCallbackListener = object: ClickCallbackListener {
        override fun callBack(position: Int, items: List<LMSEntity>, adapter: MainWorkAdapter) {
            Intent(applicationContext, EditActivity2::class.java).apply {
                putExtra(IntentKey.ID, items[position].id)
                editActivityLauncher.launch(this)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main2)
        binding.activity = this

        lmsDatabase = LMSDatabase.getInstance(this)
        sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
        fadeOutAnimation = AlphaAnimation(1f, 0f).apply { duration = 300 }
        fadeInAnimation = AlphaAnimation(0f, 1f).apply { duration = 300 }
        am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        currentDecorator = CurrentDecorator(this, Calendar.getInstance())
        storage = AppStorage(this)
        monthFormat = SimpleDateFormat(getString(R.string.calendarFormat), Locale.getDefault())

        var beforeDate = binding.calendarView.currentDate.date

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

        sharedPreferences.edit().putBoolean(SharedKey.CURRENT_CALENDAR_ICON_SHOW, sharedPreferences.getBoolean(SharedKey.CALENDAR_ICON_SHOW, true)).apply()
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.all)))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.classtime)))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.sup_classtime)))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.assignment)))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.zoom)))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.team_project)))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.exam)))

        // StartActiviyForResult 객체
        editActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                GlobalScope.launch(Dispatchers.IO) {
                    val calendar = Calendar.getInstance().apply { timeInMillis = thisCurrentDate }
                    val work = lmsDatabase?.getDao()?.getByEndTime(calendar.timeInMillis, calendar.timeInMillis + AlarmManager.INTERVAL_DAY)

                }
                /*
                val work = dbHelper.getItemAtLastDate(thisCurrentDate).toMutableList().apply { sortWith( compareBy ({ it.isFinished }, {it.endTime}, {it.type} )) } as ArrayList
                binding.mainWorkView.adapter = MainWorkAdapter(work).apply {
                    setDeleteCallback(deleteCallbackListener)
                    setClickCallback(clickCallbackListener)
                }
                binding.tvNoDeadline.visibility = if (work.isEmpty()) View.VISIBLE else View.GONE

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

                 */
            }
            binding.adView.visibility = if (storage.purchasedAds()) View.GONE else View.VISIBLE
        }

        settingsActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            binding.adView.visibility = if (storage.purchasedAds()) View.GONE else View.VISIBLE
        }

        binding.btnAdd.setOnClickListener {
            editActivityLauncher.launch(Intent(this, EditActivity2::class.java))
        }

        // SlidingPaneLayout

        binding.slidingPaneLayout.addPanelSlideListener(object: SlidingUpPanelLayout.PanelSlideListener {
            override fun onPanelSlide(panel: View, slideOffset: Float) {
                binding.btnSlide.rotation = 180f * slideOffset
            }

            override fun onPanelStateChanged(panel: View, previousState: SlidingUpPanelLayout.PanelState, newState: SlidingUpPanelLayout.PanelState) {}
        })
        binding.btnSlide.setOnClickListener {
            when (binding.slidingPaneLayout.panelState) {
                SlidingUpPanelLayout.PanelState.COLLAPSED -> binding.slidingPaneLayout.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
                SlidingUpPanelLayout.PanelState.EXPANDED -> binding.slidingPaneLayout.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            }
        }

//        binding.calendarView.addDecorators(weekdayDecorator, sundayDecorator, saturdayDecorator, todayDecorator, currentDecorator)
        binding.calendarView.topbarVisible = false
        binding.calendarView.selectedDate = CalendarDay.from(Calendar.getInstance())
        binding.calendarView.setOnMonthChangedListener { _, date ->
            if (beforeDate > date.date) {
                binding.tvMonth.setInAnimation(this, R.anim.slide_in_top)
                binding.tvMonth.setOutAnimation(this, R.anim.slide_out_bottom)
            } else {
                binding.tvMonth.setInAnimation(this, R.anim.slide_in_bottom)
                binding.tvMonth.setOutAnimation(this, R.anim.slide_out_top)
            }

            binding.tvMonth.setText(monthFormat.format(date.date))
            beforeDate = date.date
        }

        binding.tvMonth.setText(monthFormat.format(binding.calendarView.selectedDate.date))
        binding.tvMonth.setOnClickListener {
            DatePickerDialog(this, { _, year, month, day ->
                Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, day)
                    binding.calendarView.setCurrentDate(this)
                    binding.calendarView.selectedDate = CalendarDay.from(this)
                }
            }, binding.calendarView.selectedDate.year, binding.calendarView.selectedDate.month, binding.calendarView.selectedDate.day).show()

        }

        binding.btnMoveToday.setOnClickListener {
            binding.calendarView.setCurrentDate(Calendar.getInstance())
        }

        binding.calendarView.setOnDateChangedListener { widget, date, selected ->
            widget.removeDecorator(currentDecorator)
            currentDecorator = CurrentDecorator(applicationContext, date.calendar)
            widget.addDecorator(currentDecorator)

            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container, when (binding.tabLayout.selectedTabPosition) {
                    0 -> MainAllListFragment(date.calendar)
//                    1 -> PostListFragment(date.calendar, mainAccountId)
                    else -> MainAllListFragment(date.calendar)
                }
            ).commit()
        }

        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, MainAllListFragment(binding.calendarView.selectedDate.calendar)).commit()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                supportFragmentManager.beginTransaction().replace(
                    R.id.fragment_container, when (tab.position) {
                        0 -> MainAllListFragment(binding.calendarView.selectedDate.calendar)
//                        1 -> PostListFragment(binding.calendarView.selectedDate.calendar, mainAccountId)
                        else -> MainAllListFragment(binding.calendarView.selectedDate.calendar)
                    }
                ).commit()
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}

            override fun onTabUnselected(tab: TabLayout.Tab) {}
        })

        GlobalScope.launch(Dispatchers.IO) {
            val datas = lmsDatabase?.getDao()?.getAll()
            val todayWorks = lmsDatabase?.getDao()?.getByEndTime(Calendar.getInstance().timeZero().timeInMillis, Calendar.getInstance().tomorrowZero().timeInMillis)?.toMutableList()?.apply { sortWith( compareBy ({ it.isFinished }, {it.endTime}, {it.type} )) }

            withContext(Dispatchers.Main) {
                if (datas != null && todayWorks != null) {
                    val count = arrayListOf(0, 0, 0, 0, 0, 0)
                    var todayFinishedCount = 0
                    for (data in datas) {
                        val notiIntent = Intent(applicationContext, TimeReceiver::class.java).apply { putExtra(IntentKey.ID, data.id) }
                        val hours = listOf(1, 2, 6, 12, 24)
                        val minutes = listOf(3, 5, 10, 20, 30)

                        if (data.endTime < System.currentTimeMillis()) continue

                        when (data.type) {
                            LMSEntity.TYPE_HOMEWORK, LMSEntity.TYPE_LESSON, LMSEntity.TYPE_SUP_LESSON, LMSEntity.TYPE_TEAMWORK -> {
                                hours.forEachIndexed { index, i ->
                                    val triggerTime = data.endTime - i * 60 * 60 * 1000
                                    notiIntent.putExtra(IntentKey.TRIGGER, triggerTime)
                                    notiIntent.putExtra(IntentKey.TIME, i)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, (data.id ?: 0) * 100 + index + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }
                            }
                            LMSEntity.TYPE_ZOOM, LMSEntity.TYPE_EXAM -> {
                                minutes.forEachIndexed { index, i ->
                                    val triggerTime = data.endTime - i * 60 * 1000
                                    notiIntent.putExtra(IntentKey.TRIGGER, triggerTime)
                                    notiIntent.putExtra(IntentKey.MINUTE, i)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, (data.id ?: 0) * 100 + index + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }
                            }
                        }
                    }
                    for (data in todayWorks) {
                        count[data.type]++
                        if (data.isFinished) todayFinishedCount++
                    }

                    binding.viewPager.adapter = MainWorkAdapter(todayWorks as ArrayList<LMSEntity>)
                    binding.tvEventProgress.text = getString(R.string.event_progress_format, todayWorks?.size ?: 0, todayFinishedCount)

                    TabLayoutMediator(binding.viewPagerTab, binding.viewPager) { _, _ -> }.attach()
                    binding.tvLesson.text = "${count[LMSEntity.TYPE_LESSON]}"
                    binding.tvLessonSup.text = "${count[LMSEntity.TYPE_SUP_LESSON]}"
                    binding.tvHomework.text = "${count[LMSEntity.TYPE_HOMEWORK]}"
                    binding.tvTeamwork.text = "${count[LMSEntity.TYPE_TEAMWORK]}"
                    binding.tvZoom.text = "${count[LMSEntity.TYPE_ZOOM]}"
                    binding.tvTest.text = "${count[LMSEntity.TYPE_EXAM]}"

                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, MainAllListFragment(binding.calendarView.selectedDate.calendar)).commit()

                    binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                        override fun onTabSelected(tab: TabLayout.Tab) {
                            supportFragmentManager.beginTransaction().replace(
                                R.id.fragment_container, when (tab.position) {
                                    0 -> MainAllListFragment(binding.calendarView.selectedDate.calendar)
//                                    1 -> PostListFragment(binding.calendarView.selectedDate.calendar, mainAccountId)
                                    else -> MainAllListFragment(binding.calendarView.selectedDate.calendar)
                                }
                            ).commit()
                        }

                        override fun onTabReselected(tab: TabLayout.Tab) {}

                        override fun onTabUnselected(tab: TabLayout.Tab) {}
                    })

                    setDecorators(datas)
                }
            }
        }
    }

    override fun onPlanListItemClicked(position: Int, data: LMSEntity) {
        Log.d(TAG, "onPlanListItemClicked")
    }

    override fun onPlanListItemEdited(endTime: Long) {
        setEachDecorator(endTime)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun setEachDecorator(time: Long) {
        val sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
        GlobalScope.launch(Dispatchers.IO) {
            val calendar = Calendar.getInstance().apply { timeInMillis = time }.timeZero()
            val data = lmsDatabase?.getDao()?.getByEndTime(calendar.timeInMillis, calendar.timeInMillis + AlarmManager.INTERVAL_DAY)
            val timeCount = intArrayOf(0, 0)
            val decoratorTime = Calendar.getInstance().apply {
                timeInMillis = time
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            data?.forEach {
                timeCount[if (it.isFinished) 1 else 0] += 1
            }

            withContext(Dispatchers.Main) {
                binding.calendarView.removeDecorator(decorators[decoratorTime])
            }

            if (sharedPreferences.getBoolean(SharedKey.CURRENT_CALENDAR_ICON_SHOW, true)) {
                val decorator = EventDecorator2(applicationContext, decoratorTime, lmsDatabase?.getDao()?.getByEndTime(decoratorTime, decoratorTime + AlarmManager.INTERVAL_DAY) ?: listOf())
                withContext(Dispatchers.Main) {
                    binding.calendarView.addDecorator(decorator)
                    decorators[time] = decorator
                }
            } else {
                val decorator = EventDecorator(ContextCompat.getColor(applicationContext, R.color.colorAccent), timeCount, arrayListOf(CalendarDay.from(Date(time))))
                binding.calendarView.addDecorator(decorator)
                decorators[decoratorTime] = decorator
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun setDecorators(datas: List<LMSEntity>) {
        val sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)

        val endTimes = ArrayList<Long>()
        val timeCount = mutableMapOf<Long, IntArray>()

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
                GlobalScope.launch(Dispatchers.IO) {
                    val decorator = EventDecorator2(applicationContext, time, lmsDatabase?.getDao()?.getByEndTime(time, time + AlarmManager.INTERVAL_DAY) ?: listOf())
                    withContext(Dispatchers.Main) {
                        binding.calendarView.addDecorator(decorator)
                        decorators[time] = decorator
                    }
                }
            }
        } else {
            for (time in timeCount) {
                val decorator = EventDecorator(ContextCompat.getColor(this, R.color.colorAccent), time.value, arrayListOf(CalendarDay.from(Date(time.key))))
                binding.calendarView.addDecorator(decorator)
                decorators[time.key] = decorator
            }
        }
    }
}