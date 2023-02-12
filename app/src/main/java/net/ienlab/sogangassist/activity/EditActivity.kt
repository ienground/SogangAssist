package net.ienlab.sogangassist.activity

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.iterator
import androidx.databinding.DataBindingUtil
import com.google.android.gms.ads.*
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.*
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.constant.IntentKey
import net.ienlab.sogangassist.constant.IntentValue
import net.ienlab.sogangassist.constant.PendingIntentReqCode
import net.ienlab.sogangassist.databinding.ActivityEditBinding
import net.ienlab.sogangassist.receiver.TimeReceiver
import net.ienlab.sogangassist.room.LMSDatabase
import net.ienlab.sogangassist.room.LMSEntity
import net.ienlab.sogangassist.utils.AppStorage
import net.ienlab.sogangassist.utils.MyUtils
import java.text.SimpleDateFormat
import java.util.*


class EditActivity : AppCompatActivity() {

    lateinit var binding: ActivityEditBinding

    lateinit var chipButtonGroup: Array<Int>
    lateinit var sharedPreferences: SharedPreferences
    lateinit var am: AlarmManager
    lateinit var storage: AppStorage
    private var lmsDatabase: LMSDatabase? = null
    private lateinit var timeFormat: SimpleDateFormat

    lateinit var currentItem: LMSEntity
    var id = -1L

    private val startCalendar = Calendar.getInstance()
    private val endCalendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    var isFinished = false
    private val timeZone = TimeZone.getDefault()

    private val onBackPressedCallback = object: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackAutoSave(isFinished)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit)
        binding.activity = this

        onBackPressedDispatcher.addCallback(onBackPressedCallback)

        storage = AppStorage(this)
        lmsDatabase = LMSDatabase.getInstance(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        timeFormat = SimpleDateFormat(getString(R.string.timeFormat), Locale.getDefault())
        chipButtonGroup = arrayOf(R.id.chip1, R.id.chip2, R.id.chip3, R.id.chip4, R.id.chip5, R.id.chip6)

        sharedPreferences = getSharedPreferences("${packageName}_preferences",  Context.MODE_PRIVATE)
        am = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            when (checkedIds.first()) {
                R.id.chip1, R.id.chip2 -> {
                    lessonUI(endCalendar)
                    binding.tvClassEndDate.text =  "${MyUtils.getDateLabel(applicationContext, endCalendar.time)} ${timeFormat.format(endCalendar.time)}"
                }

                R.id.chip3, R.id.chip5 -> {
                    homeworkUI(startCalendar, endCalendar)
                    binding.tvStartDate.text = "${MyUtils.getDateLabel(applicationContext, startCalendar.time)} ${timeFormat.format(startCalendar.time)}"
                    binding.tvEndDate.text = "${MyUtils.getDateLabel(applicationContext, endCalendar.time)} ${timeFormat.format(endCalendar.time)}"
                }

                R.id.chip4 ->{
                    zoomUI(endCalendar)
                    binding.tvStartDate.text = "${MyUtils.getDateLabel(applicationContext, endCalendar.time)} ${timeFormat.format(endCalendar.time)}"
                }

                R.id.chip6 ->{
                    examUI(endCalendar)
                    binding.tvStartDate.text = "${MyUtils.getDateLabel(applicationContext, endCalendar.time)} ${timeFormat.format(endCalendar.time)}"
                }
            }

        }
        binding.groupAutoEdit.setOnClickListener { binding.checkAutoEdit.toggle() }

        GlobalScope.launch(Dispatchers.IO) {
            val classList = lmsDatabase?.getDao()?.getClasses()?.distinct()?.toTypedArray() ?: arrayOf()

            id = intent.getLongExtra(IntentKey.ITEM_ID, -1)
            if (id != -1L) {
                currentItem = lmsDatabase?.getDao()?.get(id) ?: LMSEntity("", 0L, 0, 0L, 0L, false, false, -1, -1, "")
            }
            withContext(Dispatchers.Main) {
                binding.etClassAuto.setAdapter(object: ArrayAdapter<String>(applicationContext, android.R.layout.simple_dropdown_item_1line, classList) {
                    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                        val textView = super.getView(position, convertView, parent) as TextView
                        val colorOnSecondaryContainer = TypedValue().apply { theme.resolveAttribute(com.google.android.material.R.attr.colorOnSecondaryContainer, this, true) }
                        textView.setTextColor(colorOnSecondaryContainer.data)
                        return textView
                    }
                })

                if (id != -1L && ::currentItem.isInitialized) {
                    binding.chipGroup.check(chipButtonGroup[currentItem.type])
                    findViewById<Chip>(chipButtonGroup[currentItem.type]).let { it.parent.requestChildFocus(it, it) }
                    binding.etClass.editText?.setText(currentItem.className)
                    binding.checkAutoEdit.isChecked = currentItem.isRenewAllowed
                    isFinished = currentItem.isFinished

                    invalidateOptionsMenu()

                    if (isFinished) {
                        for (i in 0 until binding.chipGroup.childCount) {
                            binding.chipGroup.getChildAt(i).isEnabled = false
                        }

                        binding.checkAutoEdit.isEnabled = false
                        binding.etClass.isEnabled = false
                        binding.etAssignment.isEnabled = false
                        binding.etTimeWeek.isEnabled = false
                        binding.etTimeLesson.isEnabled = false
                        binding.tvStartDate.isEnabled = false
                        binding.tvEndDate.isEnabled = false
                        binding.tvClassEndDate.isEnabled = false
                    }

                    when (currentItem.type) {
                        LMSEntity.TYPE_LESSON, LMSEntity.TYPE_SUP_LESSON -> {
                            lessonType(currentItem)
                        }

                        LMSEntity.TYPE_HOMEWORK, LMSEntity.TYPE_TEAMWORK -> {
                            homeworkType(currentItem)
                        }

                        LMSEntity.TYPE_ZOOM -> {
                            zoomType(currentItem)
                        }

                        LMSEntity.TYPE_EXAM -> {
                            examType(currentItem)
                        }
                    }
                } else {
                    binding.checkAutoEdit.isChecked = true
                    binding.chipGroup.check(R.id.chip1)

                    lessonUI(endCalendar)
                    binding.tvClassEndDate.text = "${MyUtils.getDateLabel(applicationContext, endCalendar.time)} ${timeFormat.format(endCalendar.time)}"

                    currentItem = LMSEntity("", 0L, 0, 0L, 0L, false, false, -1, -1, "")
                }
            }
        }
    }

    private fun lessonUI(endCalendar: Calendar) {
        View.GONE.let {
            binding.etAssignment.visibility = it
            binding.icDate.visibility = it
            binding.tvStartDate.visibility = it
            binding.tvEndDate.visibility = it
        }

        View.VISIBLE.let {
            binding.etTimeWeek.visibility = it
            binding.etTimeLesson.visibility = it
            binding.lineClass?.visibility = it
            binding.icClassDate.visibility = it
            binding.tvClassEndDate.visibility = it
        }

        binding.tvClassEndDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.end_at)
                .setPositiveButtonText(android.R.string.ok)
                .setNegativeButtonText(android.R.string.cancel)
                .setSelection(endCalendar.timeInMillis.let { it + timeZone.getOffset(it) })
                .build()
            datePicker.addOnPositiveButtonClickListener {
                val scheduledTime = Calendar.getInstance().apply { timeInMillis = endCalendar.timeInMillis }
                val timePicker = MaterialTimePicker.Builder()
                    .setHour(scheduledTime.get(Calendar.HOUR_OF_DAY))
                    .setMinute(scheduledTime.get(Calendar.MINUTE))
                    .setTitleText(R.string.end_at)
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .build()
                timePicker.addOnPositiveButtonClickListener { _ ->
                    endCalendar.timeInMillis = it
                    endCalendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                    endCalendar.set(Calendar.MINUTE, timePicker.minute)
                    binding.tvClassEndDate.text = "${MyUtils.getDateLabel(applicationContext, endCalendar.time)} ${timeFormat.format(endCalendar.time)}"
                }
                timePicker.show(supportFragmentManager, "CLASS_END_TIME_PICKER")
            }
            datePicker.show(supportFragmentManager, "CLASS_END_DATE_PICKER")
        }
    }

    private fun homeworkUI(startCalendar: Calendar, endCalendar: Calendar) {
        View.VISIBLE.let {
            binding.etAssignment.visibility = it
            binding.icDate.visibility = it
            binding.tvStartDate.visibility = it
            binding.tvEndDate.visibility = it
        }

        View.INVISIBLE.let {
            binding.etTimeWeek.visibility = it
            binding.etTimeLesson.visibility = it
        }

        View.GONE.let {
            binding.icClassDate.visibility = it
            binding.tvClassEndDate.visibility = it
        }

        binding.etAssignment.setHint(R.string.assignment_name)

        binding.tvStartDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.start_at)
                .setPositiveButtonText(android.R.string.ok)
                .setNegativeButtonText(android.R.string.cancel)
                .setSelection(startCalendar.timeInMillis.let { it + timeZone.getOffset(it) })
                .build()
            datePicker.addOnPositiveButtonClickListener {
                val scheduledTime = Calendar.getInstance().apply { timeInMillis = startCalendar.timeInMillis }
                val timePicker = MaterialTimePicker.Builder()
                    .setHour(scheduledTime.get(Calendar.HOUR_OF_DAY))
                    .setMinute(scheduledTime.get(Calendar.MINUTE))
                    .setTitleText(R.string.start_at)
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .build()
                timePicker.addOnPositiveButtonClickListener { _ ->
                    startCalendar.timeInMillis = it
                    startCalendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                    startCalendar.set(Calendar.MINUTE, timePicker.minute)
                    binding.tvStartDate.text = "${MyUtils.getDateLabel(applicationContext, startCalendar.time)} ${timeFormat.format(startCalendar.time)}"
                }
                timePicker.show(supportFragmentManager, "START_TIME_PICKER")
            }
            datePicker.show(supportFragmentManager, "START_DATE_PICKER")
        }

        binding.tvEndDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.end_at)
                .setPositiveButtonText(android.R.string.ok)
                .setNegativeButtonText(android.R.string.cancel)
                .setSelection(endCalendar.timeInMillis.let { it + timeZone.getOffset(it) })
                .build()
            datePicker.addOnPositiveButtonClickListener {
                val scheduledTime = Calendar.getInstance().apply { timeInMillis = endCalendar.timeInMillis }
                val timePicker = MaterialTimePicker.Builder()
                    .setHour(scheduledTime.get(Calendar.HOUR_OF_DAY))
                    .setMinute(scheduledTime.get(Calendar.MINUTE))
                    .setTitleText(R.string.end_at)
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .build()
                timePicker.addOnPositiveButtonClickListener { _ ->
                    endCalendar.timeInMillis = it
                    endCalendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                    endCalendar.set(Calendar.MINUTE, timePicker.minute)
                    binding.tvEndDate.text = "${MyUtils.getDateLabel(applicationContext, endCalendar.time)} ${timeFormat.format(endCalendar.time)}"
                }
                timePicker.show(supportFragmentManager, "END_TIME_PICKER")
            }
            datePicker.show(supportFragmentManager, "END_DATE_PICKER")
        }
    }

    private fun zoomUI(endCalendar: Calendar) {
        View.VISIBLE.let {
            binding.etAssignment.visibility = it
            binding.icDate.visibility = it
            binding.tvStartDate.visibility = it
        }

        View.INVISIBLE.let {
            binding.etTimeWeek.visibility = it
            binding.etTimeLesson.visibility = it
        }

        View.GONE.let {
            binding.icClassDate.visibility = it
            binding.tvClassEndDate.visibility = it
            binding.tvEndDate.visibility = it
        }

        binding.etAssignment.setHint(R.string.zoom_name)

        binding.tvStartDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.end_at)
                .setPositiveButtonText(android.R.string.ok)
                .setNegativeButtonText(android.R.string.cancel)
                .setSelection(endCalendar.timeInMillis.let { it + timeZone.getOffset(it) })
                .build()
            datePicker.addOnPositiveButtonClickListener {
                val scheduledTime = Calendar.getInstance().apply { timeInMillis = endCalendar.timeInMillis }
                val timePicker = MaterialTimePicker.Builder()
                    .setHour(scheduledTime.get(Calendar.HOUR_OF_DAY))
                    .setMinute(scheduledTime.get(Calendar.MINUTE))
                    .setTitleText(R.string.end_at)
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .build()
                timePicker.addOnPositiveButtonClickListener { _ ->
                    endCalendar.timeInMillis = it
                    endCalendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                    endCalendar.set(Calendar.MINUTE, timePicker.minute)
                    binding.tvStartDate.text = "${MyUtils.getDateLabel(applicationContext, endCalendar.time)} ${timeFormat.format(endCalendar.time)}"
                }
                timePicker.show(supportFragmentManager, "START_TIME_PICKER")
            }
            datePicker.show(supportFragmentManager, "START_DATE_PICKER")
        }
    }

    private fun examUI(endCalendar: Calendar) {
        View.VISIBLE.let {
            binding.etAssignment.visibility = it
            binding.icDate.visibility = it
            binding.tvStartDate.visibility = it
        }

        View.INVISIBLE.let {
            binding.etTimeWeek.visibility = it
            binding.etTimeLesson.visibility = it
        }

        View.GONE.let {
            binding.icClassDate.visibility = it
            binding.tvClassEndDate.visibility = it
            binding.tvEndDate.visibility = it
        }

        binding.etAssignment.setHint(R.string.exam_name)

        binding.tvStartDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.end_at)
                .setPositiveButtonText(android.R.string.ok)
                .setNegativeButtonText(android.R.string.cancel)
                .setSelection(endCalendar.timeInMillis.let { it + timeZone.getOffset(it) })
                .build()
            datePicker.addOnPositiveButtonClickListener {
                val scheduledTime = Calendar.getInstance().apply { timeInMillis = endCalendar.timeInMillis }
                val timePicker = MaterialTimePicker.Builder()
                    .setHour(scheduledTime.get(Calendar.HOUR_OF_DAY))
                    .setMinute(scheduledTime.get(Calendar.MINUTE))
                    .setTitleText(R.string.end_at)
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .build()
                timePicker.addOnPositiveButtonClickListener { _ ->
                    endCalendar.timeInMillis = it
                    endCalendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                    endCalendar.set(Calendar.MINUTE, timePicker.minute)
                    binding.tvStartDate.text = "${MyUtils.getDateLabel(applicationContext, endCalendar.time)} ${timeFormat.format(endCalendar.time)}"
                }
                timePicker.show(supportFragmentManager, "START_TIME_PICKER")
            }
            datePicker.show(supportFragmentManager, "START_DATE_PICKER")
        }
    }

    private fun lessonType(currentItem: LMSEntity) {
        binding.etTimeWeek.editText?.setText(if (currentItem.week != -1) currentItem.week.toString() else "")
        binding.etTimeLesson.editText?.setText(if (currentItem.lesson != -1) currentItem.lesson.toString() else "")
        binding.tvClassEndDate.text = "${MyUtils.getDateLabel(applicationContext, Date(currentItem.endTime))} ${timeFormat.format(Date(currentItem.endTime))}"

        endCalendar.timeInMillis = currentItem.endTime
        lessonUI(endCalendar)
    }

    private fun homeworkType(currentItem: LMSEntity) {
        binding.etAssignment.editText?.setText(if (currentItem.homework_name != "#NONE") currentItem.homework_name else "")
        if (currentItem.startTime != -1L) {
            binding.tvStartDate.text = "${MyUtils.getDateLabel(applicationContext, Date(currentItem.startTime))} ${timeFormat.format(Date(currentItem.startTime))}"
            startCalendar.timeInMillis = currentItem.startTime
        } else {
            val startTime = Date(currentItem.endTime - 24 * 60 * 60 * 1000)
            binding.tvStartDate.text = "${MyUtils.getDateLabel(applicationContext, startTime)} ${timeFormat.format(startTime)}"
        }

        binding.tvEndDate.text = "${MyUtils.getDateLabel(applicationContext, Date(currentItem.endTime))} ${timeFormat.format(Date(currentItem.endTime))}"
        endCalendar.timeInMillis = currentItem.endTime

        homeworkUI(startCalendar, endCalendar)
    }

    private fun zoomType(currentItem: LMSEntity) {
        binding.etAssignment.editText?.setText(if (currentItem.homework_name != "#NONE") currentItem.homework_name else "")
        if (currentItem.endTime != -1L) {
            binding.tvStartDate.text = "${MyUtils.getDateLabel(applicationContext, Date(currentItem.endTime))} ${timeFormat.format(Date(currentItem.endTime))}"
            endCalendar.timeInMillis = currentItem.endTime
        } else {
            val endTime = Date(currentItem.endTime - 24 * 60 * 60 * 1000)
            binding.tvStartDate.text = "${MyUtils.getDateLabel(applicationContext, endTime)} ${timeFormat.format(endTime)}"
            endCalendar.timeInMillis = endTime.time
        }

        zoomUI(endCalendar)
    }

    private fun examType(currentItem: LMSEntity) {
        binding.etAssignment.editText?.setText(if (currentItem.homework_name != "#NONE") currentItem.homework_name else "")
        if (currentItem.endTime != -1L) {
            binding.tvStartDate.text = "${MyUtils.getDateLabel(applicationContext, endCalendar.time)} ${timeFormat.format(endCalendar.time)}"
        } else {
            val endTime = Date(currentItem.endTime - 24 * 60 * 60 * 1000)
            binding.tvStartDate.text = "${MyUtils.getDateLabel(applicationContext, endTime)} ${timeFormat.format(endTime)}"
            endCalendar.timeInMillis = endTime.time
        }

        examUI(endCalendar)
    }

    private fun onBackAutoSave(isFinished: Boolean) {
        if (id != -1L) {
            val week = if (binding.etTimeWeek.editText?.text?.toString() != "") binding.etTimeWeek.editText?.text?.toString()?.toInt() ?: 0 else 0
            val lesson = if (binding.etTimeLesson.editText?.text?.toString() != "") binding.etTimeLesson.editText?.text?.toString()?.toInt() ?: 0 else 0
            val newItem = LMSEntity(binding.etClass.editText?.text?.toString() ?: "", 0L, chipButtonGroup.indexOf(binding.chipGroup.checkedChipId), startCalendar.timeInMillis, endCalendar.timeInMillis, binding.checkAutoEdit.isChecked, isFinished, week, lesson, binding.etAssignment.editText?.text?.toString() ?: "")
            if (!currentItem.same(newItem)) {
                showSaveDialog().show()
            } else {
                val result = Intent()
                result.putExtra(IntentKey.ITEM_ID, id)
                result.putExtra(IntentKey.ACTION_TYPE, IntentValue.ACTION_DELETE)
                finish()
            }
        } else {
            showSaveDialog().show()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun showDeleteDialog(): MaterialAlertDialogBuilder {
        return MaterialAlertDialogBuilder(this, R.style.Theme_SogangAssist_MaterialAlertDialog).apply {
            setIcon(R.drawable.ic_delete)
            setTitle(R.string.delete)
            setMessage(R.string.delete_msg)
            setPositiveButton(android.R.string.ok) { dialog, id ->
                val result = Intent()
                val itemId = intent.getLongExtra(IntentKey.ITEM_ID, -1)
                if (itemId != -1L) {
                    GlobalScope.launch(Dispatchers.IO) {
                        result.putExtra(IntentKey.ITEM_ID, itemId)
                        result.putExtra(IntentKey.ACTION_TYPE, IntentValue.ACTION_DELETE)
                        lmsDatabase?.getDao()?.delete(itemId)
                        for (i in 0 until 5) {
                            val notiIntent = Intent(applicationContext, TimeReceiver::class.java).apply { putExtra(IntentKey.ITEM_ID, itemId) }
                            val pendingIntent = PendingIntent.getBroadcast(applicationContext, PendingIntentReqCode.LAUNCH_NOTI + itemId.toInt() * 100 + i + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                            am.cancel(pendingIntent)
                        }

                        setResult(RESULT_OK, result)
                        finish()
                    }
                }
            }
            setNegativeButton(android.R.string.cancel) { dialog, id ->
                dialog.dismiss()
            }
        }
    }

    private fun showSaveDialog(): MaterialAlertDialogBuilder {
        return MaterialAlertDialogBuilder(this, R.style.Theme_SogangAssist_MaterialAlertDialog).apply {
            setIcon(R.drawable.ic_save)
            setTitle(R.string.ask_to_save)
            setMessage(R.string.ask_to_save_message)
            setPositiveButton(R.string.save) { dialog, id ->
                val itemId = intent.getLongExtra(IntentKey.ITEM_ID, -1)
                if (itemId != -1L) {
                    onAutoSave(isFinished)
                    finish()
                }
            }
            setNegativeButton(R.string.not_save) { dialog, id ->
                dialog.dismiss()
                finish()
            }
        }
    }

    private fun onAutoSave(isFinished: Boolean) {
        when (binding.chipGroup.checkedChipId) {
            R.id.chip1, R.id.chip2 -> {
                if (binding.etClass.editText?.text?.toString() != "" && binding.etTimeWeek.editText?.text?.toString() != "" && binding.etTimeLesson.editText?.text?.toString() != "") {
                    onSave(isFinished)
                } else if (binding.etTimeWeek.editText?.text?.toString() != "" && binding.etTimeLesson.editText?.text?.toString() != "") {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.err_input_class), Snackbar.LENGTH_SHORT).show()
                } else if (binding.etClass.editText?.text?.toString() != "" && binding.etTimeLesson.editText?.text?.toString() != "") {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.err_input_week), Snackbar.LENGTH_SHORT).show()
                } else if (binding.etClass.editText?.text?.toString() != "" && binding.etTimeWeek.editText?.text?.toString() != "") {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.err_input_lesson), Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.err_input_blank), Snackbar.LENGTH_SHORT).show()
                }
            }

            R.id.chip3, R.id.chip5 -> {
                if (binding.etClass.editText?.text?.toString() != "" && binding.etAssignment.editText?.text?.toString() != "" && startCalendar.timeInMillis < endCalendar.timeInMillis) { // 123
                    onSave(isFinished)
                } else if (binding.etAssignment.editText?.text?.toString() != "" && startCalendar.timeInMillis < endCalendar.timeInMillis) { // 23
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.err_input_class), Snackbar.LENGTH_SHORT).show()
                } else if (binding.etClass.editText?.text?.toString() != "" && binding.etAssignment.editText?.text?.toString() != "") { // 12
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.err_time_late), Snackbar.LENGTH_SHORT).show()
                } else if (binding.etClass.editText?.text?.toString() != "" && startCalendar.timeInMillis < endCalendar.timeInMillis) { // 13
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.err_input_assignment), Snackbar.LENGTH_SHORT).show()
                } else if (binding.etClass.editText?.text?.toString() != "") { // 1
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.err_assignment_time), Snackbar.LENGTH_SHORT).show()
                } else if (binding.etAssignment.editText?.text?.toString() != "") { // 2
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.err_class_time), Snackbar.LENGTH_SHORT).show()
                } else if (startCalendar.timeInMillis < endCalendar.timeInMillis) { // 3
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.err_class_assignment), Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.err_all), Snackbar.LENGTH_SHORT).show()
                }
            }

            R.id.chip4 -> {
                if (binding.etClass.editText?.text?.toString() != "" && binding.etAssignment.editText?.text?.toString() != "") { // 12
                    onSave(isFinished)
                } else if (binding.etAssignment.editText?.text?.toString() != "" ) { // 2
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.err_input_class), Snackbar.LENGTH_SHORT).show()
                } else if (binding.etClass.editText?.text?.toString() != "") { // 1
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.err_input_zoom_title), Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.err_all), Snackbar.LENGTH_SHORT).show()
                }
            }

            R.id.chip6 -> {
                if (binding.etClass.editText?.text?.toString() != "" && binding.etAssignment.editText?.text?.toString() != "") { // 12
                    onSave(isFinished)
                } else if (binding.etAssignment.editText?.text?.toString() != "" ) { // 2
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.err_input_class), Snackbar.LENGTH_SHORT).show()
                } else if (binding.etClass.editText?.text?.toString() != "") { // 1
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.err_input_exam_title), Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.err_all), Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun onSave(isFinished: Boolean) {
        val id = intent.getLongExtra(IntentKey.ITEM_ID, -1)
        val data = LMSEntity("", 0L, 0, 0L, 0L, false, false, -1, -1, "")
        if (id != -1L) {
            data.id = id
        }

        data.className = binding.etClass.editText?.text!!.toString()
        data.type = chipButtonGroup.indexOf(binding.chipGroup.checkedChipId)
        data.endTime = endCalendar.timeInMillis
        data.isFinished = isFinished
        data.isRenewAllowed = binding.checkAutoEdit.isChecked

        when (data.type) {
            LMSEntity.TYPE_HOMEWORK, LMSEntity.TYPE_ZOOM, LMSEntity.TYPE_TEAMWORK, LMSEntity.TYPE_EXAM -> {
                data.startTime = startCalendar.timeInMillis
                data.homework_name = binding.etAssignment.editText?.text?.toString() ?: ""
                data.week = -1
                data.lesson = -1
            }
            LMSEntity.TYPE_LESSON, LMSEntity.TYPE_SUP_LESSON -> {
                data.startTime = -1
                data.homework_name = "#NONE"
                data.week = binding.etTimeWeek.editText?.text!!.toString().toInt()
                data.lesson = binding.etTimeLesson.editText?.text!!.toString().toInt()
            }
        }

        GlobalScope.launch(Dispatchers.IO) {
            if (id != -1L) {
                lmsDatabase?.getDao()?.update(data)
            } else {
                data.id = lmsDatabase?.getDao()?.add(data) ?: -1
            }

            withContext(Dispatchers.Main) {
                val notiIntent = Intent(applicationContext, TimeReceiver::class.java).apply { putExtra(IntentKey.ITEM_ID, data.id) }
                val hours = listOf(1, 2, 6, 12, 24)
                val minutes = listOf(3, 5, 10, 20, 30)

                when (data.type) {
                    LMSEntity.TYPE_HOMEWORK, LMSEntity.TYPE_LESSON, LMSEntity.TYPE_SUP_LESSON, LMSEntity.TYPE_TEAMWORK -> {
                        hours.forEachIndexed { index, i ->
                            val triggerTime = data.endTime - i * 60 * 60 * 1000
                            Log.d(TAG, "${triggerTime > System.currentTimeMillis()}")
                            notiIntent.putExtra(IntentKey.TRIGGER, triggerTime)
                            notiIntent.putExtra(IntentKey.TIME, i)
                            val pendingIntent = PendingIntent.getBroadcast(applicationContext, PendingIntentReqCode.LAUNCH_NOTI + id.toInt() * 100 + index + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                            if (triggerTime > System.currentTimeMillis()) am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                        }
                    }
                    LMSEntity.TYPE_ZOOM, LMSEntity.TYPE_EXAM -> {
                        minutes.forEachIndexed { index, i ->
                            val triggerTime = data.endTime - i * 60 * 1000
                            notiIntent.putExtra(IntentKey.TRIGGER, triggerTime)
                            notiIntent.putExtra(IntentKey.MINUTE, i)
                            val pendingIntent = PendingIntent.getBroadcast(applicationContext, PendingIntentReqCode.LAUNCH_NOTI + id.toInt() * 100 + index + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                            if (triggerTime > System.currentTimeMillis()) am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                        }
                    }
                }

                val result = Intent()
                result.putExtra(IntentKey.ITEM_ID, data.id)
                result.putExtra(IntentKey.ACTION_TYPE, IntentValue.ACTION_EDIT)
                setResult(RESULT_OK, result)
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        if (id == -1L) menu.findItem(R.id.menu_mark_as_finish).isVisible = false
        for (menuItem in menu.iterator()) {
            val colorOnSecondaryContainer = TypedValue().apply { theme.resolveAttribute(com.google.android.material.R.attr.colorOnSecondaryContainer, this, true) }
            menuItem.iconTintList = ColorStateList.valueOf(colorOnSecondaryContainer.data)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (isFinished) {
            menu.findItem(R.id.menu_mark_as_finish)?.let {
                it.title = getString(R.string.mark_as_not_finish)
                it.setIcon(R.drawable.ic_undo)
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
            }

            R.id.menu_delete -> {
                showDeleteDialog().show()
            }

            R.id.menu_mark_as_finish -> {
                if (isFinished) { // 현재 완료 처리되어 있는데 다시 누르면
                    isFinished = false
                    item.setIcon(R.drawable.ic_check)
                    item.title = getString(R.string.mark_as_finish)

//                    binding.icCheck.visibility = View.GONE
                    for (i in 0 until binding.chipGroup.childCount) {
                        binding.chipGroup.getChildAt(i).isEnabled = true
                    }
                    binding.checkAutoEdit.isEnabled = true
                    binding.etClass.isEnabled = true
                    binding.etAssignment.isEnabled = true
                    binding.etTimeWeek.isEnabled = true
                    binding.etTimeLesson.isEnabled = true
                    binding.tvStartDate.isEnabled = true
                    binding.tvEndDate.isEnabled = true
                    binding.tvClassEndDate.isEnabled = true
                } else {
                    onAutoSave(true)
                }
            }

            R.id.menu_save -> {
                onAutoSave(isFinished)
            }
        }

        return super.onOptionsItemSelected(item)
    }
}