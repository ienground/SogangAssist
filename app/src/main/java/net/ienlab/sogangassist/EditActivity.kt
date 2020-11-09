package net.ienlab.sogangassist

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.ads.*
import com.google.android.material.snackbar.Snackbar
import net.ienlab.sogangassist.databinding.ActivityEditBinding
import java.text.SimpleDateFormat
import java.util.*

class EditActivity : AppCompatActivity() {

    lateinit var binding: ActivityEditBinding

    lateinit var dbHelper: DBHelper
    lateinit var radioButtonGroup: Array<Int>
    lateinit var dateFormat: SimpleDateFormat
    lateinit var timeFormat: SimpleDateFormat
    lateinit var sharedPreferences: SharedPreferences
    lateinit var am: AlarmManager
    lateinit var interstitialAd: InterstitialAd

    lateinit var currentItem: LMSClass
    var id = -1

    val startCalendar = Calendar.getInstance()
    val endCalendar = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 1)
    }
    var isFinished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit)
        binding.activity = this

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setFullAd(this)
        displayAd(this)

        dbHelper = DBHelper(this, dbName, dbVersion)
        radioButtonGroup = arrayOf(R.id.radioButton1, R.id.radioButton2, R.id.radioButton3)

        dateFormat = SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault())
        timeFormat = SimpleDateFormat(getString(R.string.timeFormat), Locale.getDefault())
        sharedPreferences = getSharedPreferences("${packageName}_preferences",  Context.MODE_PRIVATE)
        am = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        id = intent.getIntExtra("ID", -1)
        if (id != -1) {
            currentItem = dbHelper.getItemById(id)
            binding.radioGroup.check(radioButtonGroup[currentItem.type])
            binding.etClass.editText?.setText(currentItem.className)
            binding.checkAutoEdit.isChecked = currentItem.isRenewAllowed
            isFinished = currentItem.isFinished

            invalidateOptionsMenu()

            if (isFinished) {
                for (i in 0 until binding.radioGroup.childCount) {
                    binding.radioGroup.getChildAt(i).isEnabled = false
                }
                binding.checkAutoEdit.isEnabled = false
                binding.etClass.isEnabled = false
                binding.etAssignment.isEnabled = false
                binding.etTimeWeek.isEnabled = false
                binding.etTimeLesson.isEnabled = false
                binding.tvStartDate.isEnabled = false
                binding.tvStartTime.isEnabled = false
                binding.tvEndDate.isEnabled = false
                binding.tvEndTime.isEnabled = false
                binding.tvClassEndDate.isEnabled = false
                binding.tvClassEndTime.isEnabled = false

                binding.etClass.editText?.paintFlags = binding.etClass.editText?.paintFlags?.or(Paint.STRIKE_THRU_TEXT_FLAG) ?: Paint.STRIKE_THRU_TEXT_FLAG
                binding.etAssignment.editText?.paintFlags = binding.etAssignment.editText?.paintFlags?.or(Paint.STRIKE_THRU_TEXT_FLAG) ?: Paint.STRIKE_THRU_TEXT_FLAG
                binding.etTimeWeek.editText?.paintFlags = binding.etTimeWeek.editText?.paintFlags?.or(Paint.STRIKE_THRU_TEXT_FLAG) ?: Paint.STRIKE_THRU_TEXT_FLAG
                binding.etTimeLesson.editText?.paintFlags = binding.etTimeLesson.editText?.paintFlags?.or(Paint.STRIKE_THRU_TEXT_FLAG) ?: Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvStartDate.paintFlags = binding.tvStartDate.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvStartTime.paintFlags = binding.tvStartTime.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvEndDate.paintFlags = binding.tvEndDate.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvEndTime.paintFlags = binding.tvEndTime.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvClassEndDate.paintFlags = binding.tvClassEndDate.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvClassEndTime.paintFlags = binding.tvClassEndTime.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }

            when (currentItem.type) {
                LMSType.LESSON, LMSType.SUP_LESSON -> {
                    lessonType(currentItem)
                }

                LMSType.HOMEWORK -> {
                    homeworkType(currentItem)
                }
            }

            binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.radioButton1, R.id.radioButton2 -> {
                        lessonType(currentItem)
                    }

                    R.id.radioButton3 -> {
                        homeworkType(currentItem)
                    }
                }
            }
        } else {
            binding.checkAutoEdit.isChecked = true
            binding.radioGroup.check(R.id.radioButton1)

            lessonUI(endCalendar)
            binding.tvClassEndDate.text = dateFormat.format(endCalendar.time)
            binding.tvClassEndTime.text = timeFormat.format(endCalendar.time)

            binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.radioButton1, R.id.radioButton2 -> {
                        lessonUI(endCalendar)
                        binding.tvClassEndDate.text = dateFormat.format(endCalendar.time)
                        binding.tvClassEndTime.text = timeFormat.format(endCalendar.time)
                    }

                    R.id.radioButton3 -> {
                        homeworkUI(startCalendar, endCalendar)
                        binding.tvStartDate.text = dateFormat.format(startCalendar.time)
                        binding.tvStartTime.text = timeFormat.format(startCalendar.time)
                        binding.tvEndDate.text = dateFormat.format(endCalendar.time)
                        binding.tvEndTime.text = timeFormat.format(endCalendar.time)
                    }
                }
            }
            currentItem = LMSClass()
        }


    }

    fun setFullAd(context: Context) {
        interstitialAd = InterstitialAd(context)
        interstitialAd.adUnitId = context.getString(R.string.full_ad_unit_id)
        val adRequest2 = AdRequest.Builder()
        if (BuildConfig.DEBUG) {
            RequestConfiguration.Builder()
                .setTestDeviceIds(mutableListOf(testDevice)).let {
                    MobileAds.setRequestConfiguration(it.build())
                }
        }

        interstitialAd.loadAd(adRequest2.build())
        interstitialAd.adListener = object : AdListener() { //전면 광고의 상태를 확인하는 리스너 등록
            override fun onAdClosed() { //전면 광고가 열린 뒤에 닫혔을 때
                val adRequest3 = AdRequest.Builder()
                if (BuildConfig.DEBUG) {
                    RequestConfiguration.Builder()
                        .setTestDeviceIds(mutableListOf(testDevice)).let {
                            MobileAds.setRequestConfiguration(it.build())
                        }
                }
                interstitialAd.loadAd(adRequest3.build())
            }
        }
    }

    fun displayAd(context: Context) {
        val sharedPreferences = context.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putInt(SharedGroup.FULL_AD_CHARGE,
            sharedPreferences.getInt(SharedGroup.FULL_AD_CHARGE, 0) + 1).apply()
        Log.d("AdTAG", "ad:" + sharedPreferences.getInt(SharedGroup.FULL_AD_CHARGE, 0))
        Log.d("AdTAG", "isLoaded:" + interstitialAd.isLoaded)

        if (interstitialAd.isLoaded && sharedPreferences.getInt(SharedGroup.FULL_AD_CHARGE, 0) >= 3
            && sharedPreferences.getInt(SharedGroup.FULL_AD_CHARGE, 0) != 0) {
            interstitialAd.show()
            sharedPreferences.edit().putInt(SharedGroup.FULL_AD_CHARGE, 0).apply()
        }
    }

    fun lessonUI(endCalendar: Calendar) {
        View.GONE.let {
            binding.line.visibility = it
            binding.icAssignment.visibility = it
            binding.etAssignment.visibility = it
            binding.icDate.visibility = it
            binding.tvStartDate.visibility = it
            binding.tvStartTime.visibility = it
            binding.tvEndDate.visibility = it
            binding.tvEndTime.visibility = it
        }

        View.VISIBLE.let {
            binding.icTime.visibility = it
            binding.etTimeWeek.visibility = it
            binding.etTimeLesson.visibility = it
            binding.lineClass.visibility = it
            binding.icClassDate.visibility = it
            binding.tvClassEndDate.visibility = it
            binding.tvClassEndTime.visibility = it
        }

        binding.tvClassEndDate.setOnClickListener {
            DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                endCalendar.set(Calendar.YEAR, year)
                endCalendar.set(Calendar.MONTH, month)
                endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                binding.tvClassEndDate.text = dateFormat.format(endCalendar.time)
            }, endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH))
                .show()
        }

        binding.tvClassEndTime.setOnClickListener {
            TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                endCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                endCalendar.set(Calendar.MINUTE, minute)

                binding.tvClassEndTime.text = timeFormat.format(endCalendar.time)
            }, endCalendar.get(Calendar.HOUR_OF_DAY), endCalendar.get(Calendar.MINUTE), false)
                .show()
        }
    }

    fun homeworkUI(startCalendar: Calendar, endCalendar: Calendar) {
        View.VISIBLE.let {
            binding.line.visibility = it
            binding.icAssignment.visibility = it
            binding.etAssignment.visibility = it
            binding.icDate.visibility = it
            binding.tvStartDate.visibility = it
            binding.tvStartTime.visibility = it
            binding.tvEndDate.visibility = it
            binding.tvEndTime.visibility = it
        }

        View.GONE.let {
            binding.icTime.visibility = it
            binding.etTimeWeek.visibility = it
            binding.etTimeLesson.visibility = it
            binding.lineClass.visibility = it
            binding.icClassDate.visibility = it
            binding.tvClassEndDate.visibility = it
            binding.tvClassEndTime.visibility = it
        }

        binding.tvStartDate.setOnClickListener {
            DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                startCalendar.set(Calendar.YEAR, year)
                startCalendar.set(Calendar.MONTH, month)
                startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                binding.tvStartDate.text = dateFormat.format(startCalendar.time)
            }, startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH))
                .show()
        }

        binding.tvEndDate.setOnClickListener {
            DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                endCalendar.set(Calendar.YEAR, year)
                endCalendar.set(Calendar.MONTH, month)
                endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                binding.tvEndDate.text = dateFormat.format(endCalendar.time)
            }, endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH))
                .show()
        }

        binding.tvStartTime.setOnClickListener {
            TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                startCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                startCalendar.set(Calendar.MINUTE, minute)

                binding.tvStartTime.text = timeFormat.format(startCalendar.time)
            }, startCalendar.get(Calendar.HOUR_OF_DAY), startCalendar.get(Calendar.MINUTE), false)
                .show()
        }

        binding.tvEndTime.setOnClickListener {
            TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                endCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                endCalendar.set(Calendar.MINUTE, minute)

                binding.tvEndTime.text = timeFormat.format(endCalendar.time)
            }, endCalendar.get(Calendar.HOUR_OF_DAY), endCalendar.get(Calendar.MINUTE), false)
                .show()
        }
    }

    fun lessonType(currentItem: LMSClass) {
        binding.etTimeWeek.editText?.setText(if (currentItem.week != -1) currentItem.week.toString() else "")
        binding.etTimeLesson.editText?.setText(if (currentItem.lesson != -1) currentItem.lesson.toString() else "")
        binding.tvClassEndDate.text = dateFormat.format(Date(currentItem.endTime))
        binding.tvClassEndTime.text = timeFormat.format(Date(currentItem.endTime))

        endCalendar.timeInMillis = currentItem.endTime
        lessonUI(endCalendar)
    }

    fun homeworkType(currentItem: LMSClass) {
        binding.etAssignment.editText?.setText(if (currentItem.homework_name != "#NONE") currentItem.homework_name else "")
        if (currentItem.startTime != -1L) {
            binding.tvStartDate.text = dateFormat.format(Date(currentItem.startTime))
            binding.tvStartTime.text = timeFormat.format(Date(currentItem.startTime))
            startCalendar.timeInMillis = currentItem.startTime
        } else {
            val startTime = Date(currentItem.endTime - 24 * 60 * 60 * 1000)
            binding.tvStartDate.text = dateFormat.format(startTime)
            binding.tvStartTime.text = timeFormat.format(startTime)
            startCalendar.timeInMillis = startTime.time
        }

        binding.tvEndDate.text = dateFormat.format(Date(currentItem.endTime))
        binding.tvEndTime.text = timeFormat.format(Date(currentItem.endTime))
        endCalendar.timeInMillis = currentItem.endTime

        homeworkUI(startCalendar, endCalendar)
    }

    fun onBackAutoSave(isFinished: Boolean) {
        if (id != -1) {
            if (currentItem.className != binding.etClass.editText?.text?.toString()
                || currentItem.type != radioButtonGroup.indexOf(binding.radioGroup.checkedRadioButtonId)
                || currentItem.endTime != endCalendar.timeInMillis
                || currentItem.isFinished != isFinished
                || currentItem.isRenewAllowed != binding.checkAutoEdit.isChecked
                || (currentItem.type == LMSType.HOMEWORK
                        && (currentItem.startTime != startCalendar.timeInMillis
                        || currentItem.homework_name != binding.etAssignment.editText?.text?.toString()))
                || ((currentItem.type == LMSType.SUP_LESSON || currentItem.type == LMSType.LESSON)
                        && (currentItem.week != binding.etTimeWeek.editText?.text?.toString()?.toInt()
                        || currentItem.lesson != binding.etTimeLesson.editText?.text?.toString()?.toInt()))) {
                AlertDialog.Builder(this).apply {
                    setMessage(R.string.save_ask)
                    setPositiveButton(R.string.save) { dialog, _ ->
                        onAutoSave(isFinished)
                        dialog.dismiss()
                    }
                    setNegativeButton(R.string.not_save) { _, _ ->
                        finish()
                    }
                    setNeutralButton(R.string.delete) { _, _ ->
                        AlertDialog.Builder(this@EditActivity)
                            .setTitle(R.string.delete)
                            .setMessage(R.string.delete_msg)
                            .setPositiveButton(R.string.yes) { dialog, _ ->
                                val id = intent.getIntExtra("ID", -1)
                                if (id != -1) {
                                    dbHelper.deleteData(id)
                                }
                                setResult(RESULT_OK)
                                finish()
                            }
                            .setNegativeButton(R.string.no) { dialog, _ ->
                                dialog.cancel()
                            }
                            .show()
                    }
                }.show()
            } else {
                finish()
            }
        } else {
            AlertDialog.Builder(this).apply {
                setMessage(R.string.save_ask)
                setPositiveButton(R.string.save) { dialog, _ ->
                    onAutoSave(isFinished)
                    dialog.dismiss()
                }
                setNegativeButton(R.string.not_save) { _, _ ->
                    finish()
                }
                setNeutralButton(R.string.delete) { _, _ ->
                    AlertDialog.Builder(this@EditActivity)
                        .setTitle(R.string.delete)
                        .setMessage(R.string.delete_msg)
                        .setPositiveButton(R.string.yes) { dialog, _ ->
                            val id = intent.getIntExtra("ID", -1)
                            if (id != -1) {
                                dbHelper.deleteData(id)
                            }
                            setResult(RESULT_OK)
                            finish()
                        }
                        .setNegativeButton(R.string.no) { dialog, _ ->
                            dialog.cancel()
                        }
                        .show()
                }
            }.show()
        }
    }

    fun onAutoSave(isFinished: Boolean) {
        val view = window.decorView.rootView
        when (binding.radioGroup.checkedRadioButtonId) {
            R.id.radioButton1, R.id.radioButton2 -> {
                if (binding.etClass.editText?.text?.toString() != "" && binding.etTimeWeek.editText?.text?.toString() != ""
                    && binding.etTimeLesson.editText?.text?.toString() != "") {
                    onSave(isFinished)
                    setResult(RESULT_OK)
                    finish()
                } else if (binding.etTimeWeek.editText?.text?.toString() != "" && binding.etTimeLesson.editText?.text?.toString() != "") {
                    Snackbar.make(view, getString(R.string.err_input_class), Snackbar.LENGTH_SHORT).show()
                } else if (binding.etClass.editText?.text?.toString() != "" && binding.etTimeLesson.editText?.text?.toString() != "") {
                    Snackbar.make(view, getString(R.string.err_input_week), Snackbar.LENGTH_SHORT).show()
                } else if (binding.etClass.editText?.text?.toString() != "" && binding.etTimeWeek.editText?.text?.toString() != "") {
                    Snackbar.make(view, getString(R.string.err_input_lesson), Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(view, getString(R.string.err_input_blank), Snackbar.LENGTH_SHORT).show()
                }
            }

            R.id.radioButton3 -> {
                if (binding.etClass.editText?.text?.toString() != "" && binding.etAssignment.editText?.text?.toString() != ""
                    && startCalendar.timeInMillis < endCalendar.timeInMillis) { // 123
                    onSave(isFinished)
                    setResult(RESULT_OK)
                    finish()
                } else if (binding.etAssignment.editText?.text?.toString() != "" && startCalendar.timeInMillis < endCalendar.timeInMillis) { // 23
                    Snackbar.make(view, getString(R.string.err_input_class), Snackbar.LENGTH_SHORT).show()
                } else if (binding.etClass.editText?.text?.toString() != "" && binding.etAssignment.editText?.text?.toString() != "") { // 12
                    Snackbar.make(view, getString(R.string.err_time_late), Snackbar.LENGTH_SHORT).show()
                } else if (binding.etClass.editText?.text?.toString() != "" && startCalendar.timeInMillis < endCalendar.timeInMillis) { // 13
                    Snackbar.make(view, getString(R.string.err_input_assignment), Snackbar.LENGTH_SHORT).show()
                } else if (binding.etClass.editText?.text?.toString() != "") { // 1
                    Snackbar.make(view, getString(R.string.err_assignment_time), Snackbar.LENGTH_SHORT).show()
                } else if (binding.etAssignment.editText?.text?.toString() != "") { // 2
                    Snackbar.make(view, getString(R.string.err_class_time), Snackbar.LENGTH_SHORT).show()
                } else if (startCalendar.timeInMillis < endCalendar.timeInMillis) { // 3
                    Snackbar.make(view, getString(R.string.err_class_assignment), Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(view, getString(R.string.err_all), Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun onSave(isFinished: Boolean) {
        val id = intent.getIntExtra("ID", -1)

        LMSClass().let {
            if (id != -1) {
                it.id = id
            } else {
                val data = dbHelper.getAllData().apply {
                    sortedBy { l -> l.id }
                }

                it.id = if (data.isNotEmpty()) data.last().id + 1 else 1
            }

            it.className = binding.etClass.editText?.text!!.toString()
            it.type = radioButtonGroup.indexOf(binding.radioGroup.checkedRadioButtonId)
            it.endTime = endCalendar.timeInMillis
            it.isFinished = isFinished
            it.isRenewAllowed = binding.checkAutoEdit.isChecked

            if (it.type == LMSType.HOMEWORK) {
                it.startTime = startCalendar.timeInMillis
                it.homework_name = binding.etAssignment.editText?.text!!.toString()
                it.week = -1
                it.lesson = -1
            } else if (it.type == LMSType.LESSON || it.type == LMSType.SUP_LESSON) {
                it.startTime = -1
                it.homework_name = "#NONE"
                it.week = binding.etTimeWeek.editText?.text!!.toString().toInt()
                it.lesson = binding.etTimeLesson.editText?.text!!.toString().toInt()
            }

            if (id != -1) {
                dbHelper.updateItemById(it)
            } else {
                dbHelper.addItem(it)
            }

            val noti_intent = Intent(this, TimeReceiver::class.java)
            noti_intent.putExtra("ID", it.id)

            if (it.type == LMSType.HOMEWORK) {
                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_1HOUR_HW, false)) {
                    val triggerTime = endCalendar.timeInMillis - 1 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 1)
                    val pendingIntent = PendingIntent.getBroadcast(this, it.id * 100 + 1, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_2HOUR_HW, false)) {
                    val triggerTime = endCalendar.timeInMillis - 2 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 2)
                    val pendingIntent = PendingIntent.getBroadcast(this, it.id * 100 + 2, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_6HOUR_HW, false)) {
                    val triggerTime = endCalendar.timeInMillis - 6 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 6)
                    val pendingIntent = PendingIntent.getBroadcast(this, it.id * 100 + 3, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_12HOUR_HW, false)) {
                    val triggerTime = endCalendar.timeInMillis - 12 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 12)
                    val pendingIntent = PendingIntent.getBroadcast(this, it.id * 100 + 4, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_24HOUR_HW, false)) {
                    val triggerTime = endCalendar.timeInMillis - 24 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 24)
                    val pendingIntent = PendingIntent.getBroadcast(this, it.id * 100 + 5, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } else if (it.type == LMSType.LESSON || it.type == LMSType.SUP_LESSON) {
                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_1HOUR_LEC, false)) {
                    val triggerTime = endCalendar.timeInMillis - 1 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 1)
                    val pendingIntent = PendingIntent.getBroadcast(this, it.id * 100 + 6, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_2HOUR_LEC, false)) {
                    val triggerTime = endCalendar.timeInMillis - 2 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 2)
                    val pendingIntent = PendingIntent.getBroadcast(this, it.id * 100 + 7, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_6HOUR_LEC, false)) {
                    val triggerTime = endCalendar.timeInMillis - 6 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 6)
                    val pendingIntent = PendingIntent.getBroadcast(this, it.id * 100 + 8, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_12HOUR_LEC, false)) {
                    val triggerTime = endCalendar.timeInMillis - 12 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 12)
                    val pendingIntent = PendingIntent.getBroadcast(this, it.id * 100 + 9, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_24HOUR_LEC, false)) {
                    val triggerTime = endCalendar.timeInMillis - 24 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 24)
                    val pendingIntent = PendingIntent.getBroadcast(this, it.id * 100 + 10, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            }
        }
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
                    .setTitle(R.string.delete)
                    .setMessage(R.string.delete_msg)
                    .setPositiveButton(R.string.yes) { dialog, _ ->
                        val id = intent.getIntExtra("ID", -1)
                        if (id != -1) {
                            dbHelper.deleteData(id)
                        }
                        setResult(RESULT_OK)
                        finish()
                    }
                    .setNegativeButton(R.string.no) { dialog, _ ->
                        dialog.cancel()
                    }
                    .show()
            }

            R.id.menu_mark_as_finish -> {
                if (isFinished) { // 현재 완료 처리되어 있는데 다시 누르면
                    isFinished = false
                    item.setIcon(R.drawable.ic_check)
                    item.title = getString(R.string.mark_as_finish)

                    for (i in 0 until binding.radioGroup.childCount) {
                        binding.radioGroup.getChildAt(i).isEnabled = true
                    }
                    binding.checkAutoEdit.isEnabled = true
                    binding.etClass.isEnabled = true
                    binding.etAssignment.isEnabled = true
                    binding.etTimeWeek.isEnabled = true
                    binding.etTimeLesson.isEnabled = true
                    binding.tvStartDate.isEnabled = true
                    binding.tvStartTime.isEnabled = true
                    binding.tvEndDate.isEnabled = true
                    binding.tvEndTime.isEnabled = true
                    binding.tvClassEndDate.isEnabled = true
                    binding.tvClassEndTime.isEnabled = true

                    binding.etClass.editText?.paintFlags = binding.etClass.editText?.paintFlags?.xor(Paint.STRIKE_THRU_TEXT_FLAG) ?: Paint.STRIKE_THRU_TEXT_FLAG
                    binding.etAssignment.editText?.paintFlags = binding.etAssignment.editText?.paintFlags?.xor(Paint.STRIKE_THRU_TEXT_FLAG) ?: Paint.STRIKE_THRU_TEXT_FLAG
                    binding.etTimeWeek.editText?.paintFlags = binding.etTimeWeek.editText?.paintFlags?.xor(Paint.STRIKE_THRU_TEXT_FLAG) ?: Paint.STRIKE_THRU_TEXT_FLAG
                    binding.etTimeLesson.editText?.paintFlags = binding.etTimeLesson.editText?.paintFlags?.xor(Paint.STRIKE_THRU_TEXT_FLAG) ?: Paint.STRIKE_THRU_TEXT_FLAG
                    binding.tvStartDate.paintFlags = binding.tvStartDate.paintFlags xor Paint.STRIKE_THRU_TEXT_FLAG
                    binding.tvStartTime.paintFlags = binding.tvStartTime.paintFlags xor Paint.STRIKE_THRU_TEXT_FLAG
                    binding.tvEndDate.paintFlags = binding.tvEndDate.paintFlags xor Paint.STRIKE_THRU_TEXT_FLAG
                    binding.tvEndTime.paintFlags = binding.tvEndTime.paintFlags xor Paint.STRIKE_THRU_TEXT_FLAG
                    binding.tvClassEndDate.paintFlags = binding.tvClassEndDate.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    binding.tvClassEndTime.paintFlags = binding.tvClassEndTime.paintFlags xor Paint.STRIKE_THRU_TEXT_FLAG

                } else {
                    onAutoSave(true)
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        onBackAutoSave(isFinished)
    }
}