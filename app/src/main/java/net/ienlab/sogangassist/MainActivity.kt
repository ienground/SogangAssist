package net.ienlab.sogangassist

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.android.synthetic.main.activity_main.*
import net.ienlab.sogangassist.decorators.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*

val TAG = "SogangAssistTAG"
val REFRESH_MAIN_WORK = 2

class MainActivity : AppCompatActivity() {

    lateinit var dbHelper: DBHelper
    lateinit var sharedPreferences: SharedPreferences
    var currentDate: Long = 0
    lateinit var fadeOutAnimation: AlphaAnimation
    lateinit var fadeInAnimation: AlphaAnimation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        supportActionBar?.title = null

        dbHelper = DBHelper(this, dbName, null, dbVersion)
        sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
        fadeOutAnimation = AlphaAnimation(1f, 1f).apply {
            duration = 300
        }
        fadeInAnimation = AlphaAnimation(0f, 1f).apply {
            duration = 300
        }
        val monthFormat = SimpleDateFormat("MMMM", Locale.ENGLISH)
        month.typeface = Typeface.createFromAsset(assets, "fonts/gmsans_bold.otf")
        month.text = monthFormat.format(Date(System.currentTimeMillis()))
//        startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))

        val todayWork = dbHelper.getItemAtLastDate(System.currentTimeMillis()).toMutableList().apply {
            sortWith( compareBy ({ it.isFinished }, {it.type}))
        }
        mainWorkView.adapter = MainWorkAdapter(todayWork)
        mainWorkView.layoutManager = LinearLayoutManager(this)
        tv_no_deadline.visibility = if (todayWork.isEmpty()) View.VISIBLE else View.GONE

        val arr = mutableListOf("안녕")
        arr.size
        arr.reverse()

        calendarView.topbarVisible = false

        calendarView.arrowColor = ContextCompat.getColor(this, R.color.black)
        currentDate = System.currentTimeMillis()
        calendarView.setOnDateChangedListener { _, date, _ ->
            val work = dbHelper.getItemAtLastDate(date.date.time).toMutableList().apply {
                sortWith( compareBy ({ it.isFinished }, {it.type}))
            }
            mainWorkView.let {
                it.startAnimation(fadeOutAnimation)
                it.visibility = View.INVISIBLE

                it.adapter = MainWorkAdapter(work)
                it.layoutManager = LinearLayoutManager(this)
                currentDate = date.date.time

                it.visibility = View.VISIBLE
                it.startAnimation(fadeInAnimation)
            }

            tv_no_deadline.let {
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
        calendarView.setOnMonthChangedListener { _, date ->
            month.text = monthFormat.format(date.date)
        }

        setDecorators()

        // 체인지로그 Dialog

        val changelog_dialog_builder = AlertDialog.Builder(this)
        val inflator = layoutInflater

        val changelog_dialog_view = inflator.inflate(R.layout.dialog_changelog, null)
        changelog_dialog_builder.setView(changelog_dialog_view)

        val changelog_content = changelog_dialog_view.findViewById<TextView>(R.id.changelog_content)

        changelog_dialog_builder.setPositiveButton(R.string.ok) { dialog, id ->
            dialog.cancel()
        }

        val version: String
        try {
            val i = packageManager.getPackageInfo(packageName, 0)
            version = i.versionName
            changelog_dialog_builder.setTitle(String.format("%s %s", getString(R.string.real_app_name), version + " " + getString(R.string.changelog)))
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        val changelog_dialog = changelog_dialog_builder.create()

        // raw에서 체인지로그 파일 불러오기
        try {
            val inputStream = resources.openRawResource(R.raw.thischangelog)
            if (inputStream != null) {
                val stream = InputStreamReader(inputStream, Charset.forName("utf-8"))
                val buffer = BufferedReader(stream as Reader)

                var read: String
                val sb = StringBuilder()


                buffer.lineSequence().forEach {
                    sb.append(it)
                }
                inputStream.close()

                changelog_content.text = Html.fromHtml(sb.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val pi = packageManager.getPackageInfo(packageName, 0)
            val nowVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pi.longVersionCode.toInt()
            else pi.versionCode
            val getVersion = sharedPreferences.getInt(SharedGroup.LAST_VERSION, 0)

            if (nowVersion > getVersion) {
                sharedPreferences.edit().putInt(SharedGroup.LAST_VERSION, nowVersion).apply()
                changelog_dialog.show()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    fun setDecorators() {
        calendarView.removeDecorators()

        val sundayDecorator = SundayDecorator(this)
        val saturdayDecorator = SaturdayDecorator(this)
        val todayDecorator = OneDayDecorator(this).apply {
            setDate(Date(System.currentTimeMillis()))
        }

        calendarView.addDecorators(sundayDecorator, saturdayDecorator, todayDecorator)
        if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
            calendarView.addDecorator(NightModeDecorator(this))
        }

        val datas = dbHelper.getAllData()
        val timeCount = mutableMapOf<Long, Int>()

        for (data in datas) {
            Calendar.getInstance().let {
                it.timeInMillis = data.endTime
                it.set(Calendar.HOUR_OF_DAY, 0)
                it.set(Calendar.MINUTE, 0)
                it.set(Calendar.SECOND, 0)
                it.set(Calendar.MILLISECOND, 0)

                if (it.timeInMillis in timeCount.keys) {
                    timeCount.put(it.timeInMillis, timeCount[it.timeInMillis]?.plus(1) ?: 1)
                } else {
                    timeCount.put(it.timeInMillis, 1)
                }
            }
        }

        for (time in timeCount) {
            val decorator = EventDecorator(ContextCompat.getColor(this, R.color.colorAccent), time.value, arrayListOf(CalendarDay.from(Date(time.key))))
            calendarView.addDecorator(decorator)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REFRESH_MAIN_WORK -> {
                if (resultCode == Activity.RESULT_OK) {
                    val work = dbHelper.getItemAtLastDate(currentDate).toMutableList().apply {
                        sortWith( compareBy ({ it.isFinished }, {it.type}))
                    }
                    mainWorkView.adapter = MainWorkAdapter(work)
                    mainWorkView.layoutManager = LinearLayoutManager(this)
                    tv_no_deadline.visibility = if (work.isEmpty()) View.VISIBLE else View.GONE

                    setDecorators()
                }
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {

        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            val icon = item.icon.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    colorFilter = BlendModeColorFilter(Color.WHITE, BlendMode.SRC_IN)
                } else {
                    setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                }
            }
            item.icon = icon
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_set_today -> {
                calendarView.setCurrentDate(Date(System.currentTimeMillis()))
                currentDate = System.currentTimeMillis()
            }

            R.id.menu_settings -> {
                Intent(this, SettingsActivity::class.java).let {
                    startActivity(it)
                }
            }

            R.id.menu_add -> {
                startActivityForResult(Intent(this, EditActivity::class.java), REFRESH_MAIN_WORK)
            }
        }

        return super.onOptionsItemSelected(item)
    }
}