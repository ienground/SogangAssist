package net.ienlab.sogangassist

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Paint
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_edit.*
import java.text.SimpleDateFormat
import java.util.*

class EditActivity : AppCompatActivity() {

    lateinit var dbHelper: DBHelper
    lateinit var radioButtonGroup: Array<Int>
    lateinit var dateFormat: SimpleDateFormat
    lateinit var timeFormat: SimpleDateFormat
    lateinit var sharedPreferences: SharedPreferences
    lateinit var am: AlarmManager

    val startCalendar = Calendar.getInstance()
    val endCalendar = Calendar.getInstance()
    var isFinished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        dbHelper = DBHelper(this, dbName, null, dbVersion)
        radioButtonGroup = arrayOf(R.id.radioButton1, R.id.radioButton2, R.id.radioButton3)

        dateFormat = SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault())
        timeFormat = SimpleDateFormat(getString(R.string.timeFormat), Locale.getDefault())
        sharedPreferences = getSharedPreferences("${packageName}_preferences",  Context.MODE_PRIVATE)
        am = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val id = intent.getIntExtra("ID", -1)
        if (id != -1) {
            val currentItem = dbHelper.getItemById(id)
            radioGroup.check(radioButtonGroup[currentItem.type])
            et_class.editText?.setText(currentItem.className)
            check_auto_edit.isChecked = currentItem.isRenewAllowed
            isFinished = currentItem.isFinished

            invalidateOptionsMenu()

            if (isFinished) {
                for (i in 0 until radioGroup.childCount) {
                    radioGroup.getChildAt(i).isEnabled = false
                }
                check_auto_edit.isEnabled = false
                et_class.isEnabled = false
                et_assignment.isEnabled = false
                et_time_week.isEnabled = false
                et_time_lesson.isEnabled = false
                tv_start_date.isEnabled = false
                tv_start_time.isEnabled = false
                tv_end_date.isEnabled = false
                tv_end_time.isEnabled = false
                tv_class_end_date.isEnabled = false
                tv_class_end_time.isEnabled = false

                et_class.editText?.paintFlags = et_class.editText?.paintFlags?.or(Paint.STRIKE_THRU_TEXT_FLAG) ?: Paint.STRIKE_THRU_TEXT_FLAG
                et_assignment.editText?.paintFlags = et_assignment.editText?.paintFlags?.or(Paint.STRIKE_THRU_TEXT_FLAG) ?: Paint.STRIKE_THRU_TEXT_FLAG
                et_time_week.editText?.paintFlags = et_time_week.editText?.paintFlags?.or(Paint.STRIKE_THRU_TEXT_FLAG) ?: Paint.STRIKE_THRU_TEXT_FLAG
                et_time_lesson.editText?.paintFlags = et_time_lesson.editText?.paintFlags?.or(Paint.STRIKE_THRU_TEXT_FLAG) ?: Paint.STRIKE_THRU_TEXT_FLAG
                tv_start_date.paintFlags = tv_start_date.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tv_start_time.paintFlags = tv_start_time.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tv_end_date.paintFlags = tv_end_date.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tv_end_time.paintFlags = tv_end_time.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tv_class_end_date.paintFlags = tv_class_end_date.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                tv_class_end_time.paintFlags = tv_class_end_time.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }

            when (currentItem.type) {
                LMSType.LESSON, LMSType.SUP_LESSON -> {
                    lessonType(currentItem)
                }

                LMSType.HOMEWORK -> {
                    homeworkType(currentItem)
                }
            }

            radioGroup.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.radioButton1, R.id.radioButton2 -> {
                        lessonType(currentItem)
                    }

                    R.id.radioButton3 -> {
                        homeworkType(currentItem)
                    }
                }
            }


        }


    }

    fun lessonType(currentItem: LMSClass) {
        View.GONE.let {
            line.visibility = it
            ic_assignment.visibility = it
            et_assignment.visibility = it
            ic_date.visibility = it
            tv_start_date.visibility = it
            tv_start_time.visibility = it
            tv_end_date.visibility = it
            tv_end_time.visibility = it
        }

        View.VISIBLE.let {
            ic_time.visibility = it
            et_time_week.visibility = it
            et_time_lesson.visibility = it
            line_class.visibility = it
            ic_class_date.visibility = it
            tv_class_end_date.visibility = it
            tv_class_end_time.visibility = it
        }

        et_time_week.editText?.setText(if (currentItem.week != -1) currentItem.week.toString() else "")
        et_time_lesson.editText?.setText(if (currentItem.lesson != -1) currentItem.lesson.toString() else "")
        tv_class_end_date.text = dateFormat.format(Date(currentItem.endTime))
        tv_class_end_time.text = timeFormat.format(Date(currentItem.endTime))

        endCalendar.timeInMillis = currentItem.endTime

        tv_class_end_date.setOnClickListener {
            DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                endCalendar.set(Calendar.YEAR, year)
                endCalendar.set(Calendar.MONTH, month)
                endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                tv_class_end_date.text = dateFormat.format(endCalendar.time)
            }, endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH))
                .show()
        }

        tv_class_end_time.setOnClickListener {
            TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                endCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                endCalendar.set(Calendar.MINUTE, minute)

                tv_class_end_time.text = timeFormat.format(endCalendar.time)
            }, endCalendar.get(Calendar.HOUR_OF_DAY), endCalendar.get(Calendar.MINUTE), false)
                .show()
        }
    }

    fun homeworkType(currentItem: LMSClass) {
        View.VISIBLE.let {
            line.visibility = it
            ic_assignment.visibility = it
            et_assignment.visibility = it
            ic_date.visibility = it
            tv_start_date.visibility = it
            tv_start_time.visibility = it
            tv_end_date.visibility = it
            tv_end_time.visibility = it
        }

        View.GONE.let {
            ic_time.visibility = it
            et_time_week.visibility = it
            et_time_lesson.visibility = it
            line_class.visibility = it
            ic_class_date.visibility = it
            tv_class_end_date.visibility = it
            tv_class_end_time.visibility = it
        }

        et_assignment.editText?.setText(if (currentItem.homework_name != "#NONE") currentItem.homework_name else "")
        if (currentItem.startTime != -1L) {
            tv_start_date.text = dateFormat.format(Date(currentItem.startTime))
            tv_start_time.text = timeFormat.format(Date(currentItem.startTime))
            startCalendar.timeInMillis = currentItem.startTime
        } else {
            val startTime = Date(currentItem.endTime - 24 * 60 * 60 * 1000)
            tv_start_date.text = dateFormat.format(startTime)
            tv_start_time.text = timeFormat.format(startTime)
            startCalendar.timeInMillis = startTime.time
        }

        tv_end_date.text = dateFormat.format(Date(currentItem.endTime))
        tv_end_time.text = timeFormat.format(Date(currentItem.endTime))
        endCalendar.timeInMillis = currentItem.endTime

        tv_start_date.setOnClickListener {
            DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                startCalendar.set(Calendar.YEAR, year)
                startCalendar.set(Calendar.MONTH, month)
                startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                tv_start_date.text = dateFormat.format(startCalendar.time)
            }, startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH))
                .show()
        }

        tv_end_date.setOnClickListener {
            DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                endCalendar.set(Calendar.YEAR, year)
                endCalendar.set(Calendar.MONTH, month)
                endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                tv_end_date.text = dateFormat.format(endCalendar.time)
            }, endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH))
                .show()
        }

        tv_start_time.setOnClickListener {
            TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                startCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                startCalendar.set(Calendar.MINUTE, minute)

                tv_start_time.text = timeFormat.format(startCalendar.time)
            }, startCalendar.get(Calendar.HOUR_OF_DAY), startCalendar.get(Calendar.MINUTE), false)
                .show()
        }

        tv_end_time.setOnClickListener {
            TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                endCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                endCalendar.set(Calendar.MINUTE, minute)

                tv_end_time.text = timeFormat.format(endCalendar.time)
            }, endCalendar.get(Calendar.HOUR_OF_DAY), endCalendar.get(Calendar.MINUTE), false)
                .show()
        }
    }

    fun onBackAutoSave(isFinished: Boolean) {
        val id = intent.getIntExtra("ID", -1)
        if (id != -1) {
            LMSClass().let {
                it.id = id
                it.className = et_class.editText?.text!!.toString()
                it.type = radioButtonGroup.indexOf(radioGroup.checkedRadioButtonId)
                it.endTime = endCalendar.timeInMillis
                it.isFinished = isFinished
                it.isRenewAllowed = check_auto_edit.isChecked

                if (it.type == LMSType.HOMEWORK) {
                    it.startTime = startCalendar.timeInMillis
                    it.homework_name = et_assignment.editText?.text!!.toString()
                    it.week = -1
                    it.lesson = -1
                } else if (it.type == LMSType.LESSON || it.type == LMSType.SUP_LESSON) {
                    it.startTime = -1
                    it.homework_name = "#NONE"
                    it.week = et_time_week.editText?.text!!.toString().toInt()
                    it.lesson = et_time_lesson.editText?.text!!.toString().toInt()
                }

                dbHelper.updateItemById(it)

                val noti_intent = Intent(this, TimeReceiver::class.java)
                noti_intent.putExtra("ID", it.id)

                if (it.type == LMSType.HOMEWORK) {
                    if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_1HOUR_HW, true)) {
                        val triggerTime = endCalendar.timeInMillis - 1 * 60 * 60 * 1000
                        noti_intent.putExtra("TRIGGER", triggerTime)
                        noti_intent.putExtra("TIME", 1)
                        val pendingIntent = PendingIntent.getBroadcast(this, it.id * 100 + 1, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                        am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    }

                    if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_2HOUR_HW, true)) {
                        val triggerTime = endCalendar.timeInMillis - 2 * 60 * 60 * 1000
                        noti_intent.putExtra("TRIGGER", triggerTime)
                        noti_intent.putExtra("TIME", 2)
                        val pendingIntent = PendingIntent.getBroadcast(this, it.id * 100 + 2, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                        am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    }

                    if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_6HOUR_HW, true)) {
                        val triggerTime = endCalendar.timeInMillis - 6 * 60 * 60 * 1000
                        noti_intent.putExtra("TRIGGER", triggerTime)
                        noti_intent.putExtra("TIME", 6)
                        val pendingIntent = PendingIntent.getBroadcast(this, it.id * 100 + 3, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                        am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    }

                    if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_12HOUR_HW, true)) {
                        val triggerTime = endCalendar.timeInMillis - 12 * 60 * 60 * 1000
                        noti_intent.putExtra("TRIGGER", triggerTime)
                        noti_intent.putExtra("TIME", 12)
                        val pendingIntent = PendingIntent.getBroadcast(this, it.id * 100 + 4, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                        am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    }

                    if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_24HOUR_HW, true)) {
                        val triggerTime = endCalendar.timeInMillis - 24 * 60 * 60 * 1000
                        noti_intent.putExtra("TRIGGER", triggerTime)
                        noti_intent.putExtra("TIME", 24)
                        val pendingIntent = PendingIntent.getBroadcast(this, it.id * 100 + 5, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                        am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    }
                } else if (it.type == LMSType.LESSON || it.type == LMSType.SUP_LESSON) {
                    if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_1HOUR_LEC, true)) {
                        val triggerTime = endCalendar.timeInMillis - 1 * 60 * 60 * 1000
                        noti_intent.putExtra("TRIGGER", triggerTime)
                        noti_intent.putExtra("TIME", 1)
                        val pendingIntent = PendingIntent.getBroadcast(this, it.id * 100 + 6, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                        am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    }

                    if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_2HOUR_LEC, true)) {
                        val triggerTime = endCalendar.timeInMillis - 2 * 60 * 60 * 1000
                        noti_intent.putExtra("TRIGGER", triggerTime)
                        noti_intent.putExtra("TIME", 2)
                        val pendingIntent = PendingIntent.getBroadcast(this, it.id * 100 + 7, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                        am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    }

                    if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_6HOUR_LEC, true)) {
                        val triggerTime = endCalendar.timeInMillis - 6 * 60 * 60 * 1000
                        noti_intent.putExtra("TRIGGER", triggerTime)
                        noti_intent.putExtra("TIME", 6)
                        val pendingIntent = PendingIntent.getBroadcast(this, it.id * 100 + 8, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                        am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    }

                    if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_12HOUR_LEC, true)) {
                        val triggerTime = endCalendar.timeInMillis - 12 * 60 * 60 * 1000
                        noti_intent.putExtra("TRIGGER", triggerTime)
                        noti_intent.putExtra("TIME", 12)
                        val pendingIntent = PendingIntent.getBroadcast(this, it.id * 100 + 9, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                        am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    }

                    if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_24HOUR_LEC, true)) {
                        val triggerTime = endCalendar.timeInMillis - 24 * 60 * 60 * 1000
                        noti_intent.putExtra("TRIGGER", triggerTime)
                        noti_intent.putExtra("TIME", 24)
                        val pendingIntent = PendingIntent.getBroadcast(this, it.id * 100 + 10, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                        am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    }
                }
            }
        } else {
            LMSClass().let {
//                        it.className =
            }
        }

        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (isFinished) {
            menu?.findItem(R.id.menu_mark_as_finish)?.let {
                it.title = getString(R.string.mark_as_not_finish)
                it.setIcon(R.drawable.ic_undo)
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackAutoSave(isFinished)
            }

            R.id.menu_delete -> {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.delete))
                    .setMessage(getString(R.string.delete_msg))
                    .setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                        val id = intent.getIntExtra("ID", -1)
                        if (id != -1) {
                            dbHelper.deleteData(id)
                        }
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                    .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                        dialog.cancel()
                    }
                    .show()
            }

            R.id.menu_mark_as_finish -> {
                if (isFinished) { // 현재 완료 처리되어 있는데 다시 누르면
                    isFinished = false
                    item.setIcon(R.drawable.ic_check)
                    item.title = getString(R.string.mark_as_finish)

                    for (i in 0 until radioGroup.childCount) {
                        radioGroup.getChildAt(i).isEnabled = true
                    }
                    check_auto_edit.isEnabled = true
                    et_class.isEnabled = true
                    et_assignment.isEnabled = true
                    et_time_week.isEnabled = true
                    et_time_lesson.isEnabled = true
                    tv_start_date.isEnabled = true
                    tv_start_time.isEnabled = true
                    tv_end_date.isEnabled = true
                    tv_end_time.isEnabled = true
                    tv_class_end_date.isEnabled = true
                    tv_class_end_time.isEnabled = true

                    et_class.editText?.paintFlags = 0
                    et_assignment.editText?.paintFlags = 0
                    et_time_week.editText?.paintFlags = 0
                    et_time_lesson.editText?.paintFlags = 0
                    tv_start_date.paintFlags = 0
                    tv_start_time.paintFlags = 0
                    tv_end_date.paintFlags = 0
                    tv_end_time.paintFlags = 0
                    tv_class_end_date.paintFlags = 0
                    tv_class_end_time.paintFlags = 0

                } else {
                    onBackAutoSave(true)
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        onBackAutoSave(isFinished)
    }
}