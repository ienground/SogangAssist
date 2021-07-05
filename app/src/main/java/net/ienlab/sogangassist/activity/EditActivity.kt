package net.ienlab.sogangassist.activity

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.snackbar.Snackbar
import net.ienlab.sogangassist.BuildConfig
import net.ienlab.sogangassist.constant.SharedKey
import net.ienlab.sogangassist.data.LMSClass
import net.ienlab.sogangassist.database.*
import net.ienlab.sogangassist.databinding.ActivityEditBinding
import net.ienlab.sogangassist.receiver.TimeReceiver
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.utils.AppStorage
import net.ienlab.sogangassist.utils.MyBottomSheetDialog
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
    lateinit var storage: AppStorage
    var interstitialAd: InterstitialAd? = null

    lateinit var currentItem: LMSClass
    var id = -1

    lateinit var gmSansBold: Typeface
    lateinit var gmSansMedium: Typeface

    val startCalendar = Calendar.getInstance()
    val endCalendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    var isFinished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit)
        binding.activity = this

        storage = AppStorage(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // AdView
        val adRequest = AdRequest.Builder()
        if (BuildConfig.DEBUG) {
            RequestConfiguration.Builder()
                .setTestDeviceIds(arrayListOf(testDevice)).let {
                    MobileAds.setRequestConfiguration(it.build())
                }
        }

        gmSansBold = Typeface.createFromAsset(assets, "fonts/gmsans_bold.otf")
        gmSansMedium = Typeface.createFromAsset(assets, "fonts/gmsans_medium.otf")

        binding.radioButton1.typeface = gmSansMedium
        binding.radioButton2.typeface = gmSansMedium
        binding.radioButton3.typeface = gmSansMedium
        binding.radioButton4.typeface = gmSansMedium
        binding.radioButton5.typeface = gmSansMedium
        binding.radioButton6.typeface = gmSansMedium
        binding.checkAutoEdit.typeface = gmSansMedium
        binding.etClass.typeface = gmSansMedium
        binding.etClass.editText?.typeface = gmSansMedium
        binding.etTimeLesson.typeface = gmSansMedium
        binding.etTimeLesson.editText?.typeface = gmSansMedium
        binding.etTimeWeek.typeface = gmSansMedium
        binding.etTimeWeek.editText?.typeface = gmSansMedium
        binding.etAssignment.typeface = gmSansMedium
        binding.etAssignment.editText?.typeface = gmSansMedium
        binding.tvStartTime.typeface = gmSansMedium
        binding.tvEndTime.typeface = gmSansMedium
        binding.tvStartDate.typeface = gmSansMedium
        binding.tvEndDate.typeface = gmSansMedium
        binding.tvClassEndTime.typeface = gmSansMedium
        binding.tvClassEndDate.typeface = gmSansMedium

        dbHelper = DBHelper(this, DBHelper.dbName, DBHelper.dbVersion)
        radioButtonGroup = arrayOf(R.id.radioButton1, R.id.radioButton2, R.id.radioButton3, R.id.radioButton4, R.id.radioButton5, R.id.radioButton6)

        dateFormat = SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault())
        timeFormat = SimpleDateFormat(getString(R.string.timeFormat), Locale.getDefault())
        sharedPreferences = getSharedPreferences("${packageName}_preferences",  Context.MODE_PRIVATE)
        am = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        setFullAd(this)
        binding.adView.loadAd(adRequest.build())
        if (storage.purchasedAds()) {
            binding.adView.visibility = View.GONE
        } else {
            displayAd(this)
        }

        val classList: ArrayList<String> = arrayListOf()
        for (data in dbHelper.getAllData()) {
            if (data.className !in classList) {
                classList.add(data.className)
            }
        }

        binding.etClassAuto.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, classList))

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
                LMSClass.TYPE_LESSON, LMSClass.TYPE_SUP_LESSON -> {
                    lessonType(currentItem)
                }

                LMSClass.TYPE_HOMEWORK, LMSClass.TYPE_TEAMWORK -> {
                    homeworkType(currentItem)
                }

                LMSClass.TYPE_ZOOM -> {
                    zoomType(currentItem)
                }

                LMSClass.TYPE_EXAM -> {
                    examType(currentItem)
                }
            }

            binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
                when (checkedId) {
                    R.id.radioButton1, R.id.radioButton2 -> {
                        lessonType(currentItem)
                    }

                    R.id.radioButton3, R.id.radioButton5 -> {
                        homeworkType(currentItem)
                    }

                    R.id.radioButton4 -> {
                        zoomType(currentItem)
                    }

                    R.id.radioButton6 -> {
                        examType(currentItem)
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

                    R.id.radioButton3, R.id.radioButton5 -> {
                        homeworkUI(startCalendar, endCalendar)
                        binding.tvStartDate.text = dateFormat.format(startCalendar.time)
                        binding.tvStartTime.text = timeFormat.format(startCalendar.time)
                        binding.tvEndDate.text = dateFormat.format(endCalendar.time)
                        binding.tvEndTime.text = timeFormat.format(endCalendar.time)
                    }

                    R.id.radioButton4 ->{
                        zoomUI(startCalendar)
                        binding.tvStartDate.text = dateFormat.format(startCalendar.time)
                        binding.tvStartTime.text = timeFormat.format(startCalendar.time)
                    }

                    R.id.radioButton6 ->{
                        examUI(startCalendar)
                        binding.tvStartDate.text = dateFormat.format(startCalendar.time)
                        binding.tvStartTime.text = timeFormat.format(startCalendar.time)
                    }
                }
            }
            currentItem = LMSClass(-1, "", 0L, 0, 0L, 0L, false, false, -1, -1, "")
        }
    }

    fun setFullAd(context: Context) {
        if (BuildConfig.DEBUG) {
            RequestConfiguration.Builder()
                .setTestDeviceIds(arrayListOf(testDevice)).apply {
                    MobileAds.setRequestConfiguration(build())
                }
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


    fun displayAd(activity: Activity) {
        sharedPreferences.edit().putInt(SharedKey.FULL_AD_CHARGE, sharedPreferences.getInt(SharedKey.FULL_AD_CHARGE, 0) + 1).apply()

        if (sharedPreferences.getInt(SharedKey.FULL_AD_CHARGE, 0) >= 3) {
            interstitialAd?.show(activity)
            sharedPreferences.edit().putInt(SharedKey.FULL_AD_CHARGE, 0).apply()
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

    fun zoomUI(endCalendar: Calendar) {
        View.VISIBLE.let {
            binding.line.visibility = it
            binding.icAssignment.visibility = it
            binding.etAssignment.visibility = it
            binding.icDate.visibility = it
            binding.tvStartDate.visibility = it
            binding.tvStartTime.visibility = it
        }

        View.GONE.let {
            binding.icTime.visibility = it
            binding.etTimeWeek.visibility = it
            binding.etTimeLesson.visibility = it
            binding.lineClass.visibility = it
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

    fun examUI(endCalendar: Calendar) {
        View.VISIBLE.let {
            binding.line.visibility = it
            binding.icAssignment.visibility = it
            binding.etAssignment.visibility = it
            binding.icDate.visibility = it
            binding.tvStartDate.visibility = it
            binding.tvStartTime.visibility = it
        }

        View.GONE.let {
            binding.icTime.visibility = it
            binding.etTimeWeek.visibility = it
            binding.etTimeLesson.visibility = it
            binding.lineClass.visibility = it
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

    fun zoomType(currentItem: LMSClass) {
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

    fun examType(currentItem: LMSClass) {
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

    fun onBackAutoSave(isFinished: Boolean) {
        if (id != -1) {
            if ((currentItem.className != binding.etClass.editText?.text?.toString()
                || currentItem.type != radioButtonGroup.indexOf(binding.radioGroup.checkedRadioButtonId)
                || currentItem.isFinished != isFinished
                || currentItem.isRenewAllowed != binding.checkAutoEdit.isChecked
                        || currentItem.endTime != endCalendar.timeInMillis
                || ((currentItem.type == LMSClass.TYPE_HOMEWORK || currentItem.type == LMSClass.TYPE_TEAMWORK)
                        && (currentItem.startTime != startCalendar.timeInMillis
                        || currentItem.homework_name != binding.etAssignment.editText?.text?.toString()))
                || ((currentItem.type == LMSClass.TYPE_SUP_LESSON || currentItem.type == LMSClass.TYPE_LESSON)
                        && (currentItem.week != binding.etTimeWeek.editText?.text?.toString()?.toInt()
                        || currentItem.lesson != binding.etTimeLesson.editText?.text?.toString()?.toInt())))
                || ((currentItem.type == LMSClass.TYPE_ZOOM || currentItem.type == LMSClass.TYPE_EXAM)
                        && (currentItem.homework_name != binding.etAssignment.editText?.text?.toString()
                        || currentItem.className != binding.etClass.editText?.text?.toString()
                        || currentItem.type != radioButtonGroup.indexOf(binding.radioGroup.checkedRadioButtonId)
                        || currentItem.isFinished != isFinished
                        || currentItem.isRenewAllowed != binding.checkAutoEdit.isChecked))
            ) {
                showSaveDialog()
            } else {
                finish()
            }
        } else {
            showSaveDialog()
        }
    }

    fun showDeleteDialog() {
        MyBottomSheetDialog(this@EditActivity).apply {
            dismissWithAnimation = true

            val view = layoutInflater.inflate(R.layout.dialog, LinearLayout(context), false)
            val imgLogo: ImageView = view.findViewById(R.id.imgLogo)
            val tvTitle: TextView = view.findViewById(R.id.tv_title)
            val tvContent: TextView = view.findViewById(R.id.tv_content)
            val btnPositive: LinearLayout = view.findViewById(R.id.btn_positive)
            val btnNegative: LinearLayout = view.findViewById(R.id.btn_negative)
            val tvPositive: TextView = view.findViewById(R.id.btn_positive_text)
            val tvNegative: TextView = view.findViewById(R.id.btn_negative_text)

            imgLogo.setImageResource(R.drawable.ic_clear_all)
            tvTitle.typeface = gmSansBold
            tvContent.typeface = gmSansMedium
            tvPositive.typeface = gmSansMedium
            tvNegative.typeface = gmSansMedium

            tvTitle.text = context.getString(R.string.delete)
            tvContent.text = context.getString(R.string.delete_msg)

            btnPositive.setOnClickListener {
                val result = Intent()
                val id = intent.getIntExtra("ID", -1)
                if (id != -1) {
                    result.putExtra("ENDTIME", dbHelper.getItemById(id).endTime)
                    dbHelper.deleteData(id)
                    for (i in 0 until 5) {
                        val notiIntent = Intent(applicationContext, TimeReceiver::class.java).apply { putExtra("ID", id) }
                        val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + i + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                        am.cancel(pendingIntent)
                    }
                }
                setResult(RESULT_OK, result)
                finish()
                dismiss()
            }

            btnNegative.setOnClickListener {
                dismiss()
            }

            setContentView(view)
        }.show()
    }

    fun showSaveDialog() {
        MyBottomSheetDialog(this).apply {
            dismissWithAnimation = true

            val view = layoutInflater.inflate(R.layout.dialog_save, LinearLayout(context), false)
            val tvTitle: TextView = view.findViewById(R.id.tv_title)
            val btnPositive: LinearLayout = view.findViewById(R.id.btn_positive)
            val btnNegative: LinearLayout = view.findViewById(R.id.btn_negative)
            val btnNeutral: LinearLayout = view.findViewById(R.id.btn_neutral)
            val tvPositive: TextView = view.findViewById(R.id.btn_positive_text)
            val tvNegative: TextView = view.findViewById(R.id.btn_negative_text)
            val tvNeutral: TextView = view.findViewById(R.id.btn_neutral_text)

            tvTitle.typeface = gmSansBold
            tvPositive.typeface = gmSansMedium
            tvNegative.typeface = gmSansMedium
            tvNeutral.typeface = gmSansMedium

            btnPositive.setOnClickListener {
                onAutoSave(isFinished)
                dismiss()
            }

            btnNegative.setOnClickListener {
                finish()
                dismiss()
            }

            btnNeutral.setOnClickListener {
                showDeleteDialog()
            }

            setContentView(view)
        }.show()
    }

    fun onAutoSave(isFinished: Boolean) {
        val view = window.decorView.rootView
        when (binding.radioGroup.checkedRadioButtonId) {
            R.id.radioButton1, R.id.radioButton2 -> {
                if (binding.etClass.editText?.text?.toString() != "" && binding.etTimeWeek.editText?.text?.toString() != "" && binding.etTimeLesson.editText?.text?.toString() != "") {
                    val result = Intent()
                    result.putExtra("ENDTIME", onSave(isFinished))
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

            R.id.radioButton3, R.id.radioButton5 -> {
                if (binding.etClass.editText?.text?.toString() != "" && binding.etAssignment.editText?.text?.toString() != "" && startCalendar.timeInMillis < endCalendar.timeInMillis) { // 123
                    val result = Intent()
                    result.putExtra("ENDTIME", onSave(isFinished))
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

            R.id.radioButton4 -> {
                if (binding.etClass.editText?.text?.toString() != "" && binding.etAssignment.editText?.text?.toString() != "") { // 12
                    val result = Intent()
                    result.putExtra("ENDTIME", onSave(isFinished))
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

            R.id.radioButton6 -> {
                if (binding.etClass.editText?.text?.toString() != "" && binding.etAssignment.editText?.text?.toString() != "") { // 12
                    val result = Intent()
                    result.putExtra("ENDTIME", onSave(isFinished))
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

    fun onSave(isFinished: Boolean): Long {
        val id = intent.getIntExtra("ID", -1)
        val data = LMSClass(-1, "", 0L, 0, 0L, 0L, false, false, -1, -1, "")
        if (id != -1) {
            data.id = id
        } else {
            val datas = dbHelper.getAllData().apply { sortedBy { l -> l.id } }
            data.id = if (datas.isNotEmpty()) datas.last().id + 1 else 1
        }

        data.className = binding.etClass.editText?.text!!.toString()
        data.type = radioButtonGroup.indexOf(binding.radioGroup.checkedRadioButtonId)
        data.endTime = endCalendar.timeInMillis
        data.isFinished = isFinished
        data.isRenewAllowed = binding.checkAutoEdit.isChecked

        when (data.type) {
            LMSClass.TYPE_HOMEWORK, LMSClass.TYPE_ZOOM, LMSClass.TYPE_TEAMWORK, LMSClass.TYPE_EXAM -> {
                data.startTime = startCalendar.timeInMillis
                data.homework_name = binding.etAssignment.editText?.text?.toString() ?: ""
                data.week = -1
                data.lesson = -1
            }
            LMSClass.TYPE_LESSON, LMSClass.TYPE_SUP_LESSON -> {
                data.startTime = -1
                data.homework_name = "#NONE"
                data.week = binding.etTimeWeek.editText?.text!!.toString().toInt()
                data.lesson = binding.etTimeLesson.editText?.text!!.toString().toInt()
            }
        }

        if (id != -1) {
            dbHelper.updateItemById(data)
        } else {
            dbHelper.addItem(data)
        }

        val notiIntent = Intent(this, TimeReceiver::class.java).apply { putExtra("ID", data.id) }
        val hours = listOf(1, 2, 6, 12, 24)
        val minutes = listOf(3, 5, 10, 20, 30)

        when (data.type) {
            LMSClass.TYPE_HOMEWORK, LMSClass.TYPE_LESSON, LMSClass.TYPE_SUP_LESSON, LMSClass.TYPE_TEAMWORK -> {
                hours.forEachIndexed { index, i ->
                    val triggerTime = data.endTime - i * 60 * 60 * 1000
                    notiIntent.putExtra("TRIGGER", triggerTime)
                    notiIntent.putExtra("TIME", i)
                    val pendingIntent = PendingIntent.getBroadcast(this, id * 100 + index + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            }
            LMSClass.TYPE_ZOOM, LMSClass.TYPE_EXAM -> {
                minutes.forEachIndexed { index, i ->
                    val triggerTime = data.endTime - i * 60 * 1000
                    notiIntent.putExtra("TRIGGER", triggerTime)
                    notiIntent.putExtra("MINUTE", i)
                    val pendingIntent = PendingIntent.getBroadcast(this, id * 100 + index + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            }
        }

        return data.endTime
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
                showDeleteDialog()
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