package net.ienlab.sogangassist

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import android.widget.RadioButton
import com.google.android.material.datepicker.MaterialStyledDatePickerDialog
import kotlinx.android.synthetic.main.activity_edit.*
import java.text.SimpleDateFormat
import java.util.*

class EditActivity : AppCompatActivity() {

    var menu: Menu? = null
    lateinit var dbHelper: DBHelper
    lateinit var radioButtonGroup: Array<Int>

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

        val dateFormat = SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault())
        val timeFormat = SimpleDateFormat(getString(R.string.timeFormat), Locale.getDefault())
        radioButtonGroup = arrayOf(R.id.radioButton1, R.id.radioButton2, R.id.radioButton3)

        if (intent.getIntExtra("ID", -1) != -1) {
            val currentItem = dbHelper.getItemById(intent.getIntExtra("ID", -1))
            radioGroup.check(radioButtonGroup[currentItem.type])
            et_class.editText?.setText(currentItem.className)
            check_auto_edit.isChecked = currentItem.isRenewAllowed
            isFinished = currentItem.isFinished

            Log.d(TAG, "isFinished : ${isFinished}")
            if (isFinished) {
                menu?.findItem(R.id.menu_mark_as_finish)?.setIcon(R.drawable.ic_undo)
                menu?.findItem(R.id.menu_mark_as_finish)?.title = getString(R.string.mark_as_not_finish)
            }

            when (currentItem.type) {
                LMSType.LESSON, LMSType.SUP_LESSON -> {
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

                    et_time_week.editText?.setText(currentItem.week.toString())
                    et_time_lesson.editText?.setText(currentItem.lesson.toString())
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

                LMSType.HOMEWORK -> {
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

                    et_assignment.editText?.setText(currentItem.homework_name)
                    tv_start_date.text = dateFormat.format(Date(currentItem.startTime))
                    tv_start_time.text = timeFormat.format(Date(currentItem.startTime))
                    tv_end_date.text = dateFormat.format(Date(currentItem.endTime))
                    tv_end_time.text = timeFormat.format(Date(currentItem.endTime))

                    startCalendar.timeInMillis = currentItem.startTime
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
            }
        }


    }

    fun onBackAutoSave(isFinished: Boolean) {
        if (intent.getIntExtra("ID", -1) != -1) {
            LMSClass().let {
                it.id = intent.getIntExtra("ID", -1)
                it.className = et_class.editText?.text!!.toString()
                it.type = radioButtonGroup.indexOf(radioGroup.checkedRadioButtonId)
                it.endTime = endCalendar.timeInMillis
                it.isFinished = isFinished

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
            }
        } else {
            LMSClass().let {
//                        it.className =
            }
        }

        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        this.menu = menu
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackAutoSave(isFinished)
            }

            R.id.menu_delete -> {

            }

            R.id.menu_mark_as_finish -> {
                if (isFinished) { // 현재 완료 처리되어 있는데 다시 누르면
                    isFinished = false
                    item.setIcon(R.drawable.ic_check)
                    item.title = getString(R.string.mark_as_finish)
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