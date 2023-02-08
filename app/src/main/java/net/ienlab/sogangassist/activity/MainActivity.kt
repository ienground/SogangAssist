package net.ienlab.sogangassist.activity

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*
import net.ienlab.sogangassist.BuildConfig
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.adapter.MainLMSEventAdapter
import net.ienlab.sogangassist.constant.IntentID
import net.ienlab.sogangassist.constant.IntentKey
import net.ienlab.sogangassist.constant.IntentValue
import net.ienlab.sogangassist.constant.PendingIntentReqCode
import net.ienlab.sogangassist.databinding.ActivityMainBinding
import net.ienlab.sogangassist.decorators.*
import net.ienlab.sogangassist.receiver.TimeReceiver
import net.ienlab.sogangassist.room.LMSDatabase
import net.ienlab.sogangassist.room.LMSEntity
import net.ienlab.sogangassist.singlerowcalendar.calendar.CalendarChangesObserver
import net.ienlab.sogangassist.singlerowcalendar.calendar.CalendarViewManager
import net.ienlab.sogangassist.singlerowcalendar.calendar.SingleRowCalendar
import net.ienlab.sogangassist.singlerowcalendar.calendar.SingleRowCalendarAdapter
import net.ienlab.sogangassist.singlerowcalendar.selection.CalendarSelectionManager
import net.ienlab.sogangassist.utils.AppStorage
import net.ienlab.sogangassist.utils.ClickCallbackListener
import net.ienlab.sogangassist.utils.MyUtils
import net.ienlab.sogangassist.utils.MyUtils.Companion.timeZero
import net.ienlab.sogangassist.utils.MyUtils.Companion.tomorrowZero
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

const val TAG = "SogangAssistTAG"

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    private var lmsDatabase: LMSDatabase? = null
    lateinit var sharedPreferences: SharedPreferences
    lateinit var am: AlarmManager

    // StartActivityForResult
    lateinit var editActivityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var settingsActivityLauncher: ActivityResultLauncher<Intent>

    // 뒤로가기 시간
    private val FINISH_INTERVAL_TIME: Long = 2000
    private var backPressedTime: Long = 0

    private lateinit var monthFormat: SimpleDateFormat

    lateinit var storage: AppStorage

    private var adapter: MainLMSEventAdapter? = null
    var calendarViewSelected = ArrayList(Collections.nCopies(61, false))

    private var markingResultReceiver: BroadcastReceiver? = null

    @OptIn(DelicateCoroutinesApi::class)
    private val clickCallbackListener = object: ClickCallbackListener {
        override fun callBack(position: Int, entity: LMSEntity) {
            Intent(applicationContext, EditActivity::class.java).apply {
                putExtra(IntentKey.ITEM_ID, entity.id)
                editActivityResultLauncher.launch(this)
            }
        }

        override fun longClick(position: Int, entity: LMSEntity) {
            MaterialAlertDialogBuilder(this@MainActivity, R.style.Theme_SogangAssist_MaterialAlertDialog).apply {
                setIcon(if (!entity.isFinished) R.drawable.ic_done_all else R.drawable.ic_remove_done)
                setTitle(if (!entity.isFinished) R.string.mark_as_finish else R.string.mark_as_not_finish)
                setMessage(if (!entity.isFinished) R.string.ask_mark_as_finish else R.string.ask_mark_as_not_finish)
                setPositiveButton(android.R.string.ok) { dialog, id ->
                    entity.id?.let {
                        if (it != -1L) {
                            entity.isFinished = !entity.isFinished
                            GlobalScope.launch(Dispatchers.IO) {
                                lmsDatabase?.getDao()?.update(entity)
                                withContext(Dispatchers.Main) {
                                    adapter?.edit(it, entity)
                                    setAppTitle()
                                }
                            }
                        }
                    }
                }
                setNegativeButton(android.R.string.cancel) { dialog, id ->
                    dialog.dismiss()
                }
            }.show()
        }

        override fun delete(position: Int, entity: LMSEntity) {
            MaterialAlertDialogBuilder(this@MainActivity, R.style.Theme_SogangAssist_MaterialAlertDialog).apply {
                setIcon(R.drawable.ic_delete)
                setTitle(R.string.delete)
                setMessage(R.string.delete_msg)
                setPositiveButton(android.R.string.ok) { dialog, id ->
                    entity.id?.let {
                        if (it != -1L) {
                            GlobalScope.launch(Dispatchers.IO) {
                                lmsDatabase?.getDao()?.delete(it)
                                for (i in 0 until 5) {
                                    val notiIntent = Intent(applicationContext, TimeReceiver::class.java).apply { putExtra(IntentKey.ITEM_ID, it) }
                                    val pendingIntent =
                                        PendingIntent.getBroadcast(applicationContext, PendingIntentReqCode.LAUNCH_NOTI + it.toInt() * 100 + i + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                                    am.cancel(pendingIntent)

                                    withContext(Dispatchers.Main) {
                                        adapter?.delete(it)
                                        setAppTitle()
                                    }
                                }
                            }
                        }
                    }
                }
                setNegativeButton(android.R.string.cancel) { dialog, id ->
                    dialog.dismiss()
                }
            }.show()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.activity = this

        lmsDatabase = LMSDatabase.getInstance(this)
        sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
        am = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        storage = AppStorage(this)
        monthFormat = SimpleDateFormat(getString(R.string.calendarFormat), Locale.getDefault())

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

        val permissions: ArrayList<String> = arrayListOf()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { group ->
            group.forEach {
                when (it.key) {
                    Manifest.permission.POST_NOTIFICATIONS -> {}
                }
            }
        }
        permissionLauncher.launch(permissions.toTypedArray())

        setAppTitle()

        binding.subTitle.text = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 6..10 -> getString(R.string.user_hello_morning)
            in 11..16 -> getString(R.string.user_hello_afternoon)
            in 17..20 -> getString(R.string.user_hello_evening)
            else -> getString(R.string.user_hello_night)
        }

        val calendarViewManager = object: CalendarViewManager {
            override fun setCalendarViewResourceId(position: Int, date: Date, isSelected: Boolean): Int = R.layout.adapter_main_calarm_date
            override fun bindDataToCalendarView(holder: SingleRowCalendarAdapter.CalendarViewHolder, date: Date, position: Int, isSelected: Boolean) {
                val calendar = Calendar.getInstance().apply { time = date }
                val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
                val dateFormat = SimpleDateFormat("dd", Locale.getDefault())
                val monthFormat = SimpleDateFormat("M", Locale.getDefault())
                val cardSelected: MaterialCardView = holder.itemView.findViewById(R.id.card_selected)
                val cardUnselected: MaterialCardView = holder.itemView.findViewById(R.id.card_unselected)
                val tvDateSelected: MaterialTextView = holder.itemView.findViewById(R.id.tv_date_selected)
                val tvDaySelected: MaterialTextView = holder.itemView.findViewById(R.id.tv_day_selected)
                val tvMonthSelected: MaterialTextView = holder.itemView.findViewById(R.id.tv_month_selected)
                val tvDateUnselected: MaterialTextView = holder.itemView.findViewById(R.id.tv_date_unselected)
                val tvDayUnselected: MaterialTextView = holder.itemView.findViewById(R.id.tv_day_unselected)
                val tvMonthUnselected: MaterialTextView = holder.itemView.findViewById(R.id.tv_month_unselected)

                if (position == 60) {
                    val params = holder.itemView.layoutParams as (ViewGroup.MarginLayoutParams)
                    params.marginEnd = MyUtils.dpToPx(applicationContext, 16f).toInt()
                }

                tvDaySelected.text = dayFormat.format(calendar.time)
                tvDateSelected.text = dateFormat.format(calendar.time)
                tvDayUnselected.text = dayFormat.format(calendar.time)
                tvDateUnselected.text = dateFormat.format(calendar.time)
                tvMonthSelected.text = monthFormat.format(calendar.time) + "/"
                tvMonthUnselected.text = monthFormat.format(calendar.time) + "/"

                if (Calendar.getInstance().get(Calendar.MONTH) == calendar.get(Calendar.MONTH)) {
                    tvMonthSelected.visibility = View.GONE
                    tvMonthUnselected.visibility = View.GONE
                } else {
                    tvMonthSelected.visibility = View.VISIBLE
                    tvMonthUnselected.visibility = View.VISIBLE
                }

                if (calendarViewSelected[position]) {
                    if (isSelected) {
                        cardSelected.alpha = 1f
                        cardUnselected.alpha = 0f
                    } else {
                        ValueAnimator.ofFloat(1f, 0f).apply {
                            duration = 300
                            addUpdateListener {
                                cardSelected.alpha = it.animatedValue as Float
                                cardUnselected.alpha = 1f - it.animatedValue as Float
                            }
                        }.start()
                        calendarViewSelected[position] = false
                    }
                } else {
                    if (isSelected) {
                        ValueAnimator.ofFloat(0f, 1f).apply {
                            duration = 300
                            addUpdateListener {
                                cardSelected.alpha = it.animatedValue as Float
                                cardUnselected.alpha = 1f - it.animatedValue as Float
                            }
                        }.start()
                        calendarViewSelected[position] = true
                    } else {
                        cardSelected.alpha = 0f
                        cardUnselected.alpha = 1f
                    }
                }
            }
        }
        val calendarSelectionManager = object: CalendarSelectionManager {
            override fun canBeItemSelected(position: Int, date: Date): Boolean {
                if ((binding.listDate as SingleRowCalendar).getSelectedIndexes().let { it.isEmpty() || it.first() != position }) {
                    binding.shimmerFrame.startShimmer()
                    binding.shimmerFrame.visibility = View.VISIBLE
                    binding.listEvent.visibility = View.INVISIBLE
                    binding.icNoCalarms.alpha = 0f
                    binding.tvNoCalarms.alpha = 0f
                    binding.shimmerFrame.alpha = 1f
                    binding.listEvent.alpha = 0f

                    GlobalScope.launch(Dispatchers.IO) {
                        val calendar = Calendar.getInstance().apply { time = date }
                        val datas = lmsDatabase?.getDao()?.getByEndTime(calendar.timeZero().timeInMillis, calendar.tomorrowZero().timeInMillis)
                        withContext(Dispatchers.Main) {
                            adapter =
                                MainLMSEventAdapter(datas as ArrayList<LMSEntity>, calendar).apply { setClickCallback(clickCallbackListener) }
                            binding.listEvent.adapter = adapter
                            binding.shimmerFrame.stopShimmer()

                            ValueAnimator.ofFloat(0f, 1f).apply {
                                duration = 300
                                addUpdateListener {
                                    binding.listEvent.alpha = it.animatedValue as Float
                                    binding.shimmerFrame.alpha = 1f - it.animatedValue as Float

                                    if (datas.isEmpty()) {
                                        binding.icNoCalarms.alpha =
                                            (it.animatedValue as Float) * 0.4f
                                        binding.tvNoCalarms.alpha =
                                            (it.animatedValue as Float) * 0.4f
                                    }
                                }
                                addListener(object : AnimatorListenerAdapter() {
                                    override fun onAnimationStart(animation: Animator) {
                                        super.onAnimationStart(animation)
                                        binding.listEvent.visibility = View.VISIBLE
                                    }

                                    override fun onAnimationEnd(animation: Animator) {
                                        super.onAnimationEnd(animation)
                                        binding.shimmerFrame.visibility = View.INVISIBLE
                                    }
                                })
                            }.start()
                        }
                    }
                    binding.listEvent.adapter = adapter
                }
                return true
            }
        }
        val calendarChangesObserver = object: CalendarChangesObserver {}

        (binding.listDate as SingleRowCalendar).apply {
            this.calendarViewManager = calendarViewManager
            this.calendarSelectionManager = calendarSelectionManager
            this.calendarChangesObserver = calendarChangesObserver

            pastDaysCount = 30
            futureDaysCount = 30
            includeCurrentDate = true
            initialPositionIndex = 30
            init()
        }

        calendarViewSelected[30] = true
        (binding.listDate as SingleRowCalendar).select(30)

        editActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val id = result.data?.getLongExtra(IntentKey.ITEM_ID, -1) ?: -1
                when (result.data?.getIntExtra(IntentKey.ACTION_TYPE, -1)) {
                    IntentValue.ACTION_EDIT -> {
                        GlobalScope.launch(Dispatchers.IO) {
                            val item = lmsDatabase?.getDao()?.get(id)
                            if (item != null) {
                                withContext(Dispatchers.Main) {
                                    adapter?.edit(id, item)
                                    setAppTitle()
                                }
                            }
                        }
                    }
                    IntentValue.ACTION_DELETE -> {
                        if (id != -1L) {
                            adapter?.delete(id)
                            setAppTitle()
                        }
                    }
                }
            }
        }
        settingsActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        }

        binding.btnAdd.setOnClickListener {
            editActivityResultLauncher.launch(Intent(this, EditActivity::class.java))
        }

        markingResultReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getLongExtra(IntentKey.ITEM_ID, -1)
                if (id != -1L) {
                    GlobalScope.launch(Dispatchers.IO) {
                        val entity = lmsDatabase?.getDao()?.get(id)
                        entity?.let {
                            withContext(Dispatchers.Main) {
                                adapter?.edit(entity.id ?: -1, entity)
                                setAppTitle()
                            }
                        }
                    }
                }
            }
        }

        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(markingResultReceiver as BroadcastReceiver, IntentFilter(IntentID.MARKING_RESULT))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> {
                settingsActivityLauncher.launch(Intent(this, SettingsActivity::class.java))
            }
            R.id.menu_today -> {
                (binding.listDate as SingleRowCalendar).select(30)
                (binding.listDate as SingleRowCalendar).smoothScrollToPosition(30)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun setAppTitle() {
        GlobalScope.launch(Dispatchers.IO) {
            val calendar = Calendar.getInstance()
            val datas = lmsDatabase?.getDao()?.getByEndTime(calendar.timeZero().timeInMillis, calendar.tomorrowZero().timeInMillis)

            withContext(Dispatchers.Main) {
                binding.appTitle.text = datas?.filter { !it.isFinished }?.size?.let {
                    if (it > 1) getString(R.string.event_count, it)
                    else if (it == 1) getString(R.string.event_count_one)
                    else getString(R.string.event_count_none)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        markingResultReceiver?.let { LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(it) }
    }

}