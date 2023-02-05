package net.ienlab.sogangassist.activity

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Paint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import net.ienlab.sogangassist.BuildConfig
import net.ienlab.sogangassist.constant.SharedKey
import net.ienlab.sogangassist.databinding.ActivityEdit2Binding
import net.ienlab.sogangassist.receiver.TimeReceiver
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.constant.IntentKey
import net.ienlab.sogangassist.constant.IntentValue
import net.ienlab.sogangassist.room.LMSDatabase
import net.ienlab.sogangassist.room.LMSEntity
import net.ienlab.sogangassist.utils.AppStorage
import net.ienlab.sogangassist.utils.MyBottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*

class EditActivity : AppCompatActivity() {

    lateinit var binding: ActivityEdit2Binding

    lateinit var chipButtonGroup: Array<Int>
    lateinit var dateFormat: SimpleDateFormat
    lateinit var timeFormat: SimpleDateFormat
    lateinit var sharedPreferences: SharedPreferences
    lateinit var am: AlarmManager
    lateinit var storage: AppStorage
    private var interstitialAd: InterstitialAd? = null
    private var lmsDatabase: LMSDatabase? = null

    lateinit var currentItem: LMSEntity
    var id = -1L

    val startCalendar = Calendar.getInstance()
    val endCalendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    var isFinished = false

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit2)
        binding.activity = this

        storage = AppStorage(this)
        lmsDatabase = LMSDatabase.getInstance(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // AdView
        val adRequest = AdRequest.Builder()
        if (BuildConfig.DEBUG) {
//            RequestConfiguration.Builder().setTestDeviceIds(arrayListOf(testDevice)).let {
//                    MobileAds.setRequestConfiguration(it.build())
//                }
        }

        chipButtonGroup = arrayOf(R.id.chip1, R.id.chip2, R.id.chip3, R.id.chip4, R.id.chip5, R.id.chip6)

        dateFormat = SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault())
        timeFormat = SimpleDateFormat(getString(R.string.timeFormat), Locale.getDefault())
        sharedPreferences = getSharedPreferences("${packageName}_preferences",  Context.MODE_PRIVATE)
        am = getSystemService(Context.ALARM_SERVICE) as AlarmManager

//        setFullAd(this)
//        binding.adView.loadAd(adRequest.build())
        if (true) {
            binding.adView.visibility = View.GONE
        } else {
            displayAd(this)
        }

        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            when (checkedIds.first()) {
                R.id.chip1, R.id.chip2 -> {
                    lessonUI(endCalendar)
                    binding.tvClassEndDate.text = dateFormat.format(endCalendar.time)
                    binding.tvClassEndTime.text = timeFormat.format(endCalendar.time)
                }

                R.id.chip3, R.id.chip5 -> {
                    homeworkUI(startCalendar, endCalendar)
                    binding.tvStartDate.text = dateFormat.format(startCalendar.time)
                    binding.tvStartTime.text = timeFormat.format(startCalendar.time)
                    binding.tvEndDate.text = dateFormat.format(endCalendar.time)
                    binding.tvEndTime.text = timeFormat.format(endCalendar.time)
                }

                R.id.chip4 ->{
                    zoomUI(endCalendar)
                    binding.tvStartDate.text = dateFormat.format(endCalendar.time)
                    binding.tvStartTime.text = timeFormat.format(endCalendar.time)
                }

                R.id.chip6 ->{
                    examUI(endCalendar)
                    binding.tvStartDate.text = dateFormat.format(endCalendar.time)
                    binding.tvStartTime.text = timeFormat.format(endCalendar.time)
                }
            }

        }

        GlobalScope.launch(Dispatchers.IO) {
            val classList = lmsDatabase?.getDao()?.getClasses()?.distinct()?.toTypedArray() ?: arrayOf()

            id = intent.getLongExtra(IntentKey.ITEM_ID, -1)
            if (id != -1L) {
                currentItem = lmsDatabase?.getDao()?.get(id.toLong()) ?: LMSEntity("", 0L, 0, 0L, 0L, false, false, -1, -1, "")
            }
            withContext(Dispatchers.Main) {
                binding.etClassAuto.setAdapter(ArrayAdapter(applicationContext, android.R.layout.simple_dropdown_item_1line, classList))

                if (id != -1L && ::currentItem.isInitialized) {
                    binding.chipGroup.check(chipButtonGroup[currentItem.type])
                    findViewById<Chip>(chipButtonGroup[currentItem.type]).let { it.parent.requestChildFocus(it, it) }
                    binding.etClass.editText?.setText(currentItem.className)
                    binding.checkAutoEdit.isChecked = currentItem.isRenewAllowed
                    isFinished = currentItem.isFinished

                    invalidateOptionsMenu()

//                    binding.tvTime.text = getString(if (currentItem.type != LMSClass.TYPE_ZOOM && currentItem.type != LMSClass.TYPE_EXAM) R.string.deadline else R.string.start, timeFormat.format(Date(currentItem.endTime)))
//                    binding.tvSubName.text = currentItem.homework_name
//                    binding.tv_class_name.text = currentItem.className

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

                    when (currentItem?.type ?: LMSEntity.TYPE_LESSON) {
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
//                    binding.icCheck.visibility = View.GONE

                    binding.checkAutoEdit.isChecked = true
                    binding.chipGroup.check(R.id.chip1)

                    lessonUI(endCalendar)
                    binding.tvClassEndDate.text = dateFormat.format(endCalendar.time)
                    binding.tvClassEndTime.text = timeFormat.format(endCalendar.time)

                    currentItem = LMSEntity("", 0L, 0, 0L, 0L, false, false, -1, -1, "")
                }

            }

        }
    }

    private fun setFullAd(context: Context) {
        if (BuildConfig.DEBUG) {
//            RequestConfiguration.Builder()
//                .setTestDeviceIds(arrayListOf(testDevice)).apply {
//                    MobileAds.setRequestConfiguration(build())
//                }
        }

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, context.getString(R.string.full_ad_unit_id), adRequest, object: InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                interstitialAd = null
            }

            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
            }
        })
    }

    private fun displayAd(activity: Activity) {
        sharedPreferences.edit().putInt(SharedKey.FULL_AD_CHARGE, sharedPreferences.getInt(SharedKey.FULL_AD_CHARGE, 0) + 1).apply()

        if (sharedPreferences.getInt(SharedKey.FULL_AD_CHARGE, 0) >= 3) {
            interstitialAd?.show(activity)
            sharedPreferences.edit().putInt(SharedKey.FULL_AD_CHARGE, 0).apply()
        }
    }

    private fun lessonUI(endCalendar: Calendar) {
        View.GONE.let {
            binding.etAssignment.visibility = it
            binding.icDate.visibility = it
            binding.tvStartDate.visibility = it
            binding.tvStartTime.visibility = it
            binding.tvEndDate.visibility = it
            binding.tvEndTime.visibility = it
        }

        View.VISIBLE.let {
            binding.etTimeWeek.visibility = it
            binding.etTimeLesson.visibility = it
            binding.lineClass.visibility = it
            binding.icClassDate.visibility = it
            binding.tvClassEndDate.visibility = it
            binding.tvClassEndTime.visibility = it
        }

        binding.tvClassEndDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                endCalendar.set(Calendar.YEAR, year)
                endCalendar.set(Calendar.MONTH, month)
                endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                binding.tvClassEndDate.text = dateFormat.format(endCalendar.time)
            }, endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.tvClassEndTime.setOnClickListener {
            TimePickerDialog(this, { _, hourOfDay, minute ->
                endCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                endCalendar.set(Calendar.MINUTE, minute)

                binding.tvClassEndTime.text = timeFormat.format(endCalendar.time)
            }, endCalendar.get(Calendar.HOUR_OF_DAY), endCalendar.get(Calendar.MINUTE), false).show()
        }
    }

    private fun homeworkUI(startCalendar: Calendar, endCalendar: Calendar) {
        View.VISIBLE.let {
            binding.etAssignment.visibility = it
            binding.icDate.visibility = it
            binding.tvStartDate.visibility = it
            binding.tvStartTime.visibility = it
            binding.tvEndDate.visibility = it
            binding.tvEndTime.visibility = it
        }

        View.INVISIBLE.let {
            binding.etTimeWeek.visibility = it
            binding.etTimeLesson.visibility = it
        }

        View.GONE.let {
            binding.icClassDate.visibility = it
            binding.tvClassEndDate.visibility = it
            binding.tvClassEndTime.visibility = it
        }

        binding.etAssignment.setHint(R.string.assignment_name)

        binding.tvStartDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                startCalendar.set(Calendar.YEAR, year)
                startCalendar.set(Calendar.MONTH, month)
                startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                binding.tvStartDate.text = dateFormat.format(startCalendar.time)
            }, startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH))
                .show()
        }

        binding.tvEndDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                endCalendar.set(Calendar.YEAR, year)
                endCalendar.set(Calendar.MONTH, month)
                endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                binding.tvEndDate.text = dateFormat.format(endCalendar.time)
            }, endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH))
                .show()
        }

        binding.tvStartTime.setOnClickListener {
            TimePickerDialog(this, { view, hourOfDay, minute ->
                startCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                startCalendar.set(Calendar.MINUTE, minute)

                binding.tvStartTime.text = timeFormat.format(startCalendar.time)
            }, startCalendar.get(Calendar.HOUR_OF_DAY), startCalendar.get(Calendar.MINUTE), false)
                .show()
        }

        binding.tvEndTime.setOnClickListener {
            TimePickerDialog(this, { view, hourOfDay, minute ->
                endCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                endCalendar.set(Calendar.MINUTE, minute)

                binding.tvEndTime.text = timeFormat.format(endCalendar.time)
            }, endCalendar.get(Calendar.HOUR_OF_DAY), endCalendar.get(Calendar.MINUTE), false).show()
        }
    }

    private fun zoomUI(endCalendar: Calendar) {
        View.VISIBLE.let {
            binding.etAssignment.visibility = it
            binding.icDate.visibility = it
            binding.tvStartDate.visibility = it
            binding.tvStartTime.visibility = it
        }

        View.INVISIBLE.let {
            binding.etTimeWeek.visibility = it
            binding.etTimeLesson.visibility = it
        }

        View.GONE.let {
            binding.icClassDate.visibility = it
            binding.tvClassEndDate.visibility = it
            binding.tvClassEndTime.visibility = it
            binding.tvEndDate.visibility = it
            binding.tvEndTime.visibility = it
        }

        binding.etAssignment.setHint(R.string.zoom_name)

        binding.tvStartDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                endCalendar.set(Calendar.YEAR, year)
                endCalendar.set(Calendar.MONTH, month)
                endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                binding.tvStartDate.text = dateFormat.format(endCalendar.time)
            }, endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.tvStartTime.setOnClickListener {
            TimePickerDialog(this, { view, hourOfDay, minute ->
                endCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                endCalendar.set(Calendar.MINUTE, minute)

                binding.tvStartTime.text = timeFormat.format(endCalendar.time)
            }, endCalendar.get(Calendar.HOUR_OF_DAY), endCalendar.get(Calendar.MINUTE), false).show()
        }
    }

    private fun examUI(endCalendar: Calendar) {
        View.VISIBLE.let {
            binding.etAssignment.visibility = it
            binding.icDate.visibility = it
            binding.tvStartDate.visibility = it
            binding.tvStartTime.visibility = it
        }

        View.INVISIBLE.let {
            binding.etTimeWeek.visibility = it
            binding.etTimeLesson.visibility = it
        }

        View.GONE.let {
            binding.icClassDate.visibility = it
            binding.tvClassEndDate.visibility = it
            binding.tvClassEndTime.visibility = it
            binding.tvEndDate.visibility = it
            binding.tvEndTime.visibility = it
        }

        binding.etAssignment.setHint(R.string.exam_name)

        binding.tvStartDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                endCalendar.set(Calendar.YEAR, year)
                endCalendar.set(Calendar.MONTH, month)
                endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                binding.tvStartDate.text = dateFormat.format(endCalendar.time)
            }, endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.tvStartTime.setOnClickListener {
            TimePickerDialog(this, { view, hourOfDay, minute ->
                endCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                endCalendar.set(Calendar.MINUTE, minute)

                binding.tvStartTime.text = timeFormat.format(endCalendar.time)
            }, endCalendar.get(Calendar.HOUR_OF_DAY), endCalendar.get(Calendar.MINUTE), false).show()
        }
    }

    private fun lessonType(currentItem: LMSEntity) {
        binding.etTimeWeek.editText?.setText(if (currentItem.week != -1) currentItem.week.toString() else "")
        binding.etTimeLesson.editText?.setText(if (currentItem.lesson != -1) currentItem.lesson.toString() else "")
        binding.tvClassEndDate.text = dateFormat.format(Date(currentItem.endTime))
        binding.tvClassEndTime.text = timeFormat.format(Date(currentItem.endTime))

        endCalendar.timeInMillis = currentItem.endTime
        lessonUI(endCalendar)
    }

    private fun homeworkType(currentItem: LMSEntity) {
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

    private fun zoomType(currentItem: LMSEntity) {
        binding.etAssignment.editText?.setText(if (currentItem.homework_name != "#NONE") currentItem.homework_name else "")
        if (currentItem.endTime != -1L) {
            binding.tvStartDate.text = dateFormat.format(Date(currentItem.endTime))
            binding.tvStartTime.text = timeFormat.format(Date(currentItem.endTime))
            endCalendar.timeInMillis = currentItem.endTime
        } else {
            val endTime = Date(currentItem.endTime - 24 * 60 * 60 * 1000)
            binding.tvStartDate.text = dateFormat.format(endTime)
            binding.tvStartTime.text = timeFormat.format(endTime)
            endCalendar.timeInMillis = endTime.time
        }

        zoomUI(endCalendar)
    }

    private fun examType(currentItem: LMSEntity) {
        binding.etAssignment.editText?.setText(if (currentItem.homework_name != "#NONE") currentItem.homework_name else "")
        if (currentItem.endTime != -1L) {
            binding.tvStartDate.text = dateFormat.format(Date(currentItem.endTime))
            binding.tvStartTime.text = timeFormat.format(Date(currentItem.endTime))
            endCalendar.timeInMillis = currentItem.endTime
        } else {
            val endTime = Date(currentItem.endTime - 24 * 60 * 60 * 1000)
            binding.tvStartDate.text = dateFormat.format(endTime)
            binding.tvStartTime.text = timeFormat.format(endTime)
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
                        result.putExtra(IntentKey.ENDTIME, lmsDatabase?.getDao()?.get(itemId)?.endTime ?: 0L)
                        result.putExtra(IntentKey.ITEM_ID, itemId)
                        result.putExtra(IntentKey.ACTION_TYPE, IntentValue.ACTION_DELETE)
                        lmsDatabase?.getDao()?.delete(itemId)
                        for (i in 0 until 5) {
                            val notiIntent = Intent(applicationContext, TimeReceiver::class.java).apply { putExtra(IntentKey.ITEM_ID, itemId) }
                            val pendingIntent = PendingIntent.getBroadcast(applicationContext, itemId.toInt() * 100 + i + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
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
            }
        }
    }

    private fun onAutoSave(isFinished: Boolean) {
        val view = window.decorView.rootView
        when (binding.chipGroup.checkedChipId) {
            R.id.chip1, R.id.chip2 -> {
                if (binding.etClass.editText?.text?.toString() != "" && binding.etTimeWeek.editText?.text?.toString() != "" && binding.etTimeLesson.editText?.text?.toString() != "") {
                    val result = Intent()
                    result.putExtra(IntentKey.ENDTIME, onSave(isFinished))
                    result.putExtra(IntentKey.ITEM_ID, id)
                    result.putExtra(IntentKey.ACTION_TYPE, IntentValue.ACTION_EDIT)
                    setResult(RESULT_OK, result)
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

            R.id.chip3, R.id.chip5 -> {
                if (binding.etClass.editText?.text?.toString() != "" && binding.etAssignment.editText?.text?.toString() != "" && startCalendar.timeInMillis < endCalendar.timeInMillis) { // 123
                    val result = Intent()
                    result.putExtra(IntentKey.ENDTIME, onSave(isFinished))
                    result.putExtra(IntentKey.ITEM_ID, id)
                    result.putExtra(IntentKey.ACTION_TYPE, IntentValue.ACTION_EDIT)
                    setResult(RESULT_OK, result)
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

            R.id.chip4 -> {
                if (binding.etClass.editText?.text?.toString() != "" && binding.etAssignment.editText?.text?.toString() != "") { // 12
                    val result = Intent()
                    result.putExtra(IntentKey.ENDTIME, onSave(isFinished))
                    result.putExtra(IntentKey.ITEM_ID, id)
                    result.putExtra(IntentKey.ACTION_TYPE, IntentValue.ACTION_EDIT)
                    setResult(RESULT_OK, result)
                    finish()
                } else if (binding.etAssignment.editText?.text?.toString() != "" ) { // 2
                    Snackbar.make(view, getString(R.string.err_input_class), Snackbar.LENGTH_SHORT).show()
                } else if (binding.etClass.editText?.text?.toString() != "") { // 1
                    Snackbar.make(view, getString(R.string.err_input_zoom_title), Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(view, getString(R.string.err_all), Snackbar.LENGTH_SHORT).show()
                }
            }

            R.id.chip6 -> {
                if (binding.etClass.editText?.text?.toString() != "" && binding.etAssignment.editText?.text?.toString() != "") { // 12
                    val result = Intent()
                    result.putExtra(IntentKey.ENDTIME, onSave(isFinished))
                    result.putExtra(IntentKey.ITEM_ID, id)
                    result.putExtra(IntentKey.ACTION_TYPE, IntentValue.ACTION_EDIT)
                    setResult(RESULT_OK, result)
                    finish()
                } else if (binding.etAssignment.editText?.text?.toString() != "" ) { // 2
                    Snackbar.make(view, getString(R.string.err_input_class), Snackbar.LENGTH_SHORT).show()
                } else if (binding.etClass.editText?.text?.toString() != "") { // 1
                    Snackbar.make(view, getString(R.string.err_input_exam_title), Snackbar.LENGTH_SHORT).show()
                } else {
                    Snackbar.make(view, getString(R.string.err_all), Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun onSave(isFinished: Boolean): Long {
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
        }

        val notiIntent = Intent(this, TimeReceiver::class.java).apply { putExtra(IntentKey.ITEM_ID, data.id) }
        val hours = listOf(1, 2, 6, 12, 24)
        val minutes = listOf(3, 5, 10, 20, 30)

        when (data.type) {
            LMSEntity.TYPE_HOMEWORK, LMSEntity.TYPE_LESSON, LMSEntity.TYPE_SUP_LESSON, LMSEntity.TYPE_TEAMWORK -> {
                hours.forEachIndexed { index, i ->
                    val triggerTime = data.endTime - i * 60 * 60 * 1000
                    notiIntent.putExtra(IntentKey.TRIGGER, triggerTime)
                    notiIntent.putExtra(IntentKey.TIME, i)
                    val pendingIntent = PendingIntent.getBroadcast(this, id.toInt() * 100 + index + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            }
            LMSEntity.TYPE_ZOOM, LMSEntity.TYPE_EXAM -> {
                minutes.forEachIndexed { index, i ->
                    val triggerTime = data.endTime - i * 60 * 1000
                    notiIntent.putExtra(IntentKey.TRIGGER, triggerTime)
                    notiIntent.putExtra(IntentKey.MINUTE, i)
                    val pendingIntent = PendingIntent.getBroadcast(this, id.toInt() * 100 + index + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            }
        }

        return data.endTime
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
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
                onBackAutoSave(isFinished)
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
                    binding.tvClassEndDate.paintFlags = binding.tvClassEndDate.paintFlags xor Paint.STRIKE_THRU_TEXT_FLAG
                    binding.tvClassEndTime.paintFlags = binding.tvClassEndTime.paintFlags xor Paint.STRIKE_THRU_TEXT_FLAG

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

    override fun onBackPressed() {
        onBackAutoSave(isFinished)
    }
}