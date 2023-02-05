package net.ienlab.sogangassist.activity

import android.animation.ValueAnimator
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.ienlab.sogangassist.BuildConfig
import net.ienlab.sogangassist.databinding.ActivitySettingsBinding
import net.ienlab.sogangassist.utils.MyUtils
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.constant.DefaultValue
import net.ienlab.sogangassist.constant.SharedKey
import net.ienlab.sogangassist.room.LMSDatabase
import net.ienlab.sogangassist.room.LMSEntity
import net.ienlab.sogangassist.utils.AppStorage
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class SettingsActivity : AppCompatActivity(), Preference.OnPreferenceClickListener, BillingProcessor.IBillingHandler {

    lateinit var binding: ActivitySettingsBinding

    lateinit var storage: AppStorage
    lateinit var bp: BillingProcessor

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)
        binding.activity = this

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = null
        supportFragmentManager.beginTransaction().replace(R.id.settings, SettingsFragment(), null).commit()

        storage = AppStorage(this)
        bp = BillingProcessor(this, getString(R.string.iab_license), this)
        bp.initialize()
        bp.loadOwnedPurchasesFromGoogle()
    }

    // ActionBar 메뉴 각각 클릭 시

    override fun onPreferenceClick(preference: Preference?): Boolean {
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        bp.loadOwnedPurchasesFromGoogle()

        if (bp.isPurchased(AppStorage.ADS_FREE)) {
            menu.findItem(R.id.menu_ads_free).isVisible = false
            storage.setPurchasedAds(bp.isPurchased(AppStorage.ADS_FREE))
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_settings, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                setResult(Activity.RESULT_OK)
                super.onBackPressed()
            }
            R.id.menu_ads_free -> {
                bp.purchase(this, AppStorage.ADS_FREE)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBillingInitialized() {
        bp.loadOwnedPurchasesFromGoogle()
    }

    override fun onPurchaseHistoryRestored() {
        storage.setPurchasedAds(bp.isPurchased(AppStorage.ADS_FREE))
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        bp.loadOwnedPurchasesFromGoogle()
        storage.setPurchasedAds(bp.isPurchased(AppStorage.ADS_FREE))
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {}

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        super.onBackPressed()
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        lateinit var sharedPreferences: SharedPreferences

        private val timeFormat = SimpleDateFormat("a h:mm", Locale.getDefault())
        private var lmsDatabase: LMSDatabase? = null


        @OptIn(DelicateCoroutinesApi::class)
        override fun onCreatePreferences(bundle: Bundle?, str: String?) {
            addPreferencesFromResource(R.xml.root_preferences)
            val prefAppInfo = findPreference("app_title")
            val prefDndTime = findPreference("dnd_time")
            val prefNotifyHw = findPreference("notify_hw_group")
            val prefNotifyLec = findPreference("notify_lec_group")
            val prefNotifyZoom = findPreference("notify_zoom_group")
            val prefNotifyExam = findPreference("notify_exam_group")
            val prefTimeMorningReminder = findPreference(SharedKey.TIME_MORNING_REMINDER)
            val prefTimeNightReminder = findPreference(SharedKey.TIME_NIGHT_REMINDER)
            val prefCalendarIconCheck = findPreference(SharedKey.CALENDAR_ICON_SHOW) as CheckBoxPreference
            val prefDateDelete = findPreference("date_delete")
            val prefChangelog = findPreference("changelog")
            val prefEmail = findPreference("ask_to_dev")
            val prefOpenSource = findPreference("open_source")
            val prefBackup = findPreference("backup")
            val prefRestore = findPreference("restore")

            lmsDatabase = LMSDatabase.getInstance(requireContext())
            sharedPreferences = requireContext().getSharedPreferences("${requireContext().packageName}_preferences", Context.MODE_PRIVATE)

            val hourData = listOf("1", "2", "6", "12", "24")
            val minuteData = listOf("3", "5", "10", "20", "30")

            val hwSharedKeys = listOf(SharedKey.NOTIFY_1HOUR_HW, SharedKey.NOTIFY_2HOUR_HW, SharedKey.NOTIFY_6HOUR_HW, SharedKey.NOTIFY_12HOUR_HW, SharedKey.NOTIFY_24HOUR_HW)
            val lecSharedKeys = listOf(SharedKey.NOTIFY_1HOUR_LEC, SharedKey.NOTIFY_2HOUR_LEC, SharedKey.NOTIFY_6HOUR_LEC, SharedKey.NOTIFY_12HOUR_LEC, SharedKey.NOTIFY_24HOUR_LEC)
            val zoomSharedKeys = listOf(SharedKey.NOTIFY_3MIN_ZOOM, SharedKey.NOTIFY_5MIN_ZOOM, SharedKey.NOTIFY_10MIN_ZOOM, SharedKey.NOTIFY_20MIN_ZOOM, SharedKey.NOTIFY_30MIN_ZOOM)
            val examSharedKeys = listOf(SharedKey.NOTIFY_3MIN_EXAM, SharedKey.NOTIFY_5MIN_EXAM, SharedKey.NOTIFY_10MIN_EXAM, SharedKey.NOTIFY_20MIN_EXAM, SharedKey.NOTIFY_30MIN_EXAM)

            val hwHoursOn = arrayListOf<String>()
            val lecHoursOn = arrayListOf<String>()
            val zoomMinutesOn = arrayListOf<String>()
            val examMinutesOn = arrayListOf<String>()

            hwSharedKeys.forEachIndexed { index, s -> if (sharedPreferences.getBoolean(s, true)) hwHoursOn.add(hourData[index]) }
            lecSharedKeys.forEachIndexed { index, s -> if (sharedPreferences.getBoolean(s, true)) lecHoursOn.add(hourData[index]) }
            zoomSharedKeys.forEachIndexed { index, s -> if (sharedPreferences.getBoolean(s, true)) zoomMinutesOn.add(minuteData[index]) }
            examSharedKeys.forEachIndexed { index, s -> if (sharedPreferences.getBoolean(s, true)) examMinutesOn.add(minuteData[index]) }

            prefNotifyHw?.summary = if (hwHoursOn.isNotEmpty()) getString(R.string.notify_hw_on, hwHoursOn.joinToString(", ")) else getString(R.string.notify_all_off)
            prefNotifyLec?.summary = if (lecHoursOn.isNotEmpty()) getString(R.string.notify_lec_on, lecHoursOn.joinToString(", ")) else getString(R.string.notify_all_off)
            prefNotifyZoom?.summary = if (zoomMinutesOn.isNotEmpty()) getString(R.string.notify_zoom_on, zoomMinutesOn.joinToString(", ")) else getString(R.string.notify_all_off)
            prefNotifyExam?.summary = if (zoomMinutesOn.isNotEmpty()) getString(R.string.notify_exam_on, examMinutesOn.joinToString(", ")) else getString(R.string.notify_all_off)

            val morningCalendar = Calendar.getInstance().apply {
                val time = sharedPreferences.getInt(SharedKey.TIME_MORNING_REMINDER, DefaultValue.TIME_MORNING_REMINDER)
                set(Calendar.HOUR_OF_DAY, time / 60)
                set(Calendar.MINUTE, time % 60)
            }
            val nightCalendar = Calendar.getInstance().apply {
                val time = sharedPreferences.getInt(SharedKey.TIME_NIGHT_REMINDER, DefaultValue.TIME_NIGHT_REMINDER)
                set(Calendar.HOUR_OF_DAY, time / 60)
                set(Calendar.MINUTE, time % 60)
            }
            val dndStartCalendar = Calendar.getInstance().apply {
                val time = sharedPreferences.getInt(SharedKey.DND_START_TIME, DefaultValue.DND_START_TIME)
                set(Calendar.HOUR_OF_DAY, time / 60)
                set(Calendar.MINUTE, time % 60)
            }
            val dndEndCalendar = Calendar.getInstance().apply {
                val time = sharedPreferences.getInt(SharedKey.DND_END_TIME, DefaultValue.DND_END_TIME)
                set(Calendar.HOUR_OF_DAY, time / 60)
                set(Calendar.MINUTE, time % 60)
            }

            prefTimeMorningReminder?.summary = timeFormat.format(morningCalendar.time)
            prefTimeNightReminder?.summary = timeFormat.format(nightCalendar.time)
            prefDndTime?.summary = "${timeFormat.format(dndStartCalendar.time)} ~ ${timeFormat.format(dndEndCalendar.time)}"

            prefCalendarIconCheck.setOnPreferenceChangeListener { _, _ ->
                Toast.makeText(context, requireContext().getString(R.string.restart_to_apply), Toast.LENGTH_SHORT).show()
                true
            }

            prefAppInfo?.setOnPreferenceClickListener {
//                MyBottomSheetDialog(requireContext()).apply {
//                    val view = layoutInflater.inflate(R.layout.dialog_changelog, LinearLayout(requireContext()), false)
//                    val tvVersion: TextView = view.findViewById(R.id.tv_version)
//                    val tvContent: TextView = view.findViewById(R.id.content)
//
//                    tvVersion.text = getString(R.string.real_app_name)
//                    tvContent.text = getString(R.string.dev_ienlab)
//
//                    setContentView(view)
//                }.show()

                true
            }
            prefDndTime?.setOnPreferenceClickListener { preference ->
                MaterialAlertDialogBuilder(requireContext(), R.style.Theme_SogangAssist_MaterialAlertDialog).apply {
                    setTitle(R.string.dnd_time)
                    setIcon(R.drawable.ic_dnd)
                    val view = layoutInflater.inflate(R.layout.dialog_dnd, LinearLayout(requireContext()), false)
                    val tvDndStart: TextView = view.findViewById(R.id.tv_dnd_start)
                    val tvDndEnd: TextView = view.findViewById(R.id.tv_dnd_end)

                    tvDndStart.text = timeFormat.format(dndStartCalendar.time)
                    tvDndEnd.text = timeFormat.format(dndEndCalendar.time)

                    val startCalendar = dndStartCalendar.clone() as Calendar
                    val endCalendar = dndEndCalendar.clone() as Calendar

                    tvDndStart.setOnClickListener {
                        val timePicker = MaterialTimePicker.Builder()
                            .setHour(startCalendar.get(Calendar.HOUR_OF_DAY))
                            .setMinute(startCalendar.get(Calendar.MINUTE))
                            .setTitleText(R.string.start_at)
                            .setTimeFormat(TimeFormat.CLOCK_12H)
                            .build()
                        timePicker.addOnPositiveButtonClickListener {
                            startCalendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                            startCalendar.set(Calendar.MINUTE, timePicker.minute)
                            tvDndStart.text = timeFormat.format(startCalendar.time)
                        }
                        timePicker.show(parentFragmentManager, "DND_START_TIME_PICKER")
                    }
                    tvDndEnd.setOnClickListener {
                        val timePicker = MaterialTimePicker.Builder()
                            .setHour(endCalendar.get(Calendar.HOUR_OF_DAY))
                            .setMinute(endCalendar.get(Calendar.MINUTE))
                            .setTitleText(R.string.end_at)
                            .setTimeFormat(TimeFormat.CLOCK_12H)
                            .build()
                        timePicker.addOnPositiveButtonClickListener {
                            endCalendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                            endCalendar.set(Calendar.MINUTE, timePicker.minute)
                            tvDndEnd.text = timeFormat.format(endCalendar.time)
                        }
                        timePicker.show(parentFragmentManager, "DND_END_TIME_PICKER")
                    }

                    setPositiveButton(android.R.string.ok) { dialog, id ->
                        dndStartCalendar.time = startCalendar.time
                        dndEndCalendar.time = endCalendar.time
                        sharedPreferences.edit().putInt(SharedKey.DND_START_TIME, dndStartCalendar.get(Calendar.HOUR_OF_DAY) * 60 + dndStartCalendar.get(Calendar.MINUTE)).apply()
                        sharedPreferences.edit().putInt(SharedKey.DND_END_TIME, dndEndCalendar.get(Calendar.HOUR_OF_DAY) * 60 + dndEndCalendar.get(Calendar.MINUTE)).apply()
                        preference.summary = "${timeFormat.format(dndStartCalendar.time)} ~ ${timeFormat.format(dndEndCalendar.time)}"
                    }

                    setNegativeButton(android.R.string.cancel) { dialog, id -> }

                    setView(view)
                }.show()

                true
            }
            prefNotifyHw?.setOnPreferenceClickListener {
                MaterialAlertDialogBuilder(requireContext(), R.style.Theme_SogangAssist_MaterialAlertDialog).apply {
                    setTitle(R.string.notify_hw)
                    setIcon(R.drawable.ic_assignment)

                    val view = layoutInflater.inflate(R.layout.dialog_notify_time, LinearLayout(requireContext()), false)
                    val hours = arrayListOf<Boolean>()

                    val buttons = listOf<MaterialButton>(
                        view.findViewById(R.id.btn_1hour),
                        view.findViewById(R.id.btn_2hour),
                        view.findViewById(R.id.btn_6hour),
                        view.findViewById(R.id.btn_12hour),
                        view.findViewById(R.id.btn_24hour)
                    )

                    hwSharedKeys.forEach { hours.add(sharedPreferences.getBoolean(it, true)) }

                    buttons.forEachIndexed { index, button ->
                        button.isChecked = hours[index]
                        button.addOnCheckedChangeListener { _, isChecked ->
                            sharedPreferences.edit().putBoolean(hwSharedKeys[index], !hours[index]).apply()
                            hours[index] = !hours[index]

                            Toast.makeText(requireContext(), getString(if (hours[index]) R.string.notify_hw_on else R.string.notify_hw_off, hourData[index]),
                                Toast.LENGTH_SHORT).apply { setGravity(Gravity.CENTER, 0, 0) }.show()
                        }
                    }

                    setPositiveButton(R.string.close) { _, _ -> }

                    setOnDismissListener {
                        val hoursOn = arrayListOf<String>()
                        hours.forEachIndexed { index, b ->  if (b) hoursOn.add(hourData[index]) }
                        prefNotifyHw.summary = if (hoursOn.isNotEmpty()) getString(R.string.notify_hw_on, hoursOn.joinToString(", ")) else getString(R.string.notify_all_off)
                    }

                    setView(view)
                }.show()

                true
            }
            prefNotifyLec?.setOnPreferenceClickListener {
                MaterialAlertDialogBuilder(requireContext(), R.style.Theme_SogangAssist_MaterialAlertDialog).apply {
                    setTitle(R.string.notify_lec)
                    setIcon(R.drawable.ic_video)

                    val view = layoutInflater.inflate(R.layout.dialog_notify_time, LinearLayout(requireContext()), false)
                    val hours = arrayListOf<Boolean>()

                    val buttons = listOf<MaterialButton>(
                        view.findViewById(R.id.btn_1hour),
                        view.findViewById(R.id.btn_2hour),
                        view.findViewById(R.id.btn_6hour),
                        view.findViewById(R.id.btn_12hour),
                        view.findViewById(R.id.btn_24hour)
                    )

                    lecSharedKeys.forEach { hours.add(sharedPreferences.getBoolean(it, true)) }

                    buttons.forEachIndexed { index, button ->
                        button.isChecked = hours[index]
                        button.addOnCheckedChangeListener { _, isChecked ->
                            sharedPreferences.edit().putBoolean(lecSharedKeys[index], !hours[index]).apply()
                            hours[index] = !hours[index]

                            Toast.makeText(requireContext(), getString(if (hours[index]) R.string.notify_lec_on else R.string.notify_lec_off, hourData[index]),
                                Toast.LENGTH_SHORT).apply { setGravity(Gravity.CENTER, 0, 0) }.show()
                        }
                    }

                    setPositiveButton(R.string.close) { _, _ -> }

                    setOnDismissListener {
                        val hoursOn = arrayListOf<String>()
                        hours.forEachIndexed { index, b ->  if (b) hoursOn.add(hourData[index]) }
                        prefNotifyLec.summary = if (hoursOn.isNotEmpty()) getString(R.string.notify_lec_on, hoursOn.joinToString(", ")) else getString(R.string.notify_all_off)
                    }

                    setView(view)
                }.show()

                true
            }
            prefNotifyZoom?.setOnPreferenceClickListener {
                MaterialAlertDialogBuilder(requireContext(), R.style.Theme_SogangAssist_MaterialAlertDialog).apply {
                    setTitle(R.string.notify_zoom)
                    setIcon(R.drawable.ic_live_class)

                    val view = layoutInflater.inflate(R.layout.dialog_notify_time, LinearLayout(requireContext()), false)
                    val minutes = arrayListOf<Boolean>()

                    val buttonText = listOf(R.string.three_minute, R.string.five_minute, R.string.ten_minute, R.string.twenty_minute, R.string.thirty_minute)
                    val buttons = listOf<MaterialButton>(
                        view.findViewById(R.id.btn_1hour),
                        view.findViewById(R.id.btn_2hour),
                        view.findViewById(R.id.btn_6hour),
                        view.findViewById(R.id.btn_12hour),
                        view.findViewById(R.id.btn_24hour)
                    )

                    buttons.forEachIndexed { index, button ->  button.text = getString(buttonText[index]) }
                    zoomSharedKeys.forEach { minutes.add(sharedPreferences.getBoolean(it, true)) }

                    buttons.forEachIndexed { index, button ->
                        button.isChecked = minutes[index]
                        button.addOnCheckedChangeListener { _, isChecked ->
                            sharedPreferences.edit().putBoolean(zoomSharedKeys[index], !minutes[index]).apply()
                            minutes[index] = !minutes[index]

                            Toast.makeText(requireContext(), getString(if (minutes[index]) R.string.notify_zoom_on else R.string.notify_zoom_off, minuteData[index]),
                                Toast.LENGTH_SHORT).apply { setGravity(Gravity.CENTER, 0, 0) }.show()
                        }
                    }

                    setPositiveButton(R.string.close) { _, _ -> }

                    setOnDismissListener {
                        val minutesOn = arrayListOf<String>()
                        minutes.forEachIndexed { index, b ->  if (b) minutesOn.add(minuteData[index]) }
                        prefNotifyZoom.summary = if (minutesOn.isNotEmpty()) getString(R.string.notify_zoom_on, minutesOn.joinToString(", ")) else getString(R.string.notify_all_off)
                    }

                    setView(view)
                }.show()

                true
            }
            prefNotifyExam?.setOnPreferenceClickListener {
                MaterialAlertDialogBuilder(requireContext(), R.style.Theme_SogangAssist_MaterialAlertDialog).apply {
                    setTitle(R.string.notify_exam)
                    setIcon(R.drawable.ic_test)

                    val view = layoutInflater.inflate(R.layout.dialog_notify_time, LinearLayout(requireContext()), false)
                    val minutes = arrayListOf<Boolean>()

                    val buttonText = listOf(R.string.three_minute, R.string.five_minute, R.string.ten_minute, R.string.twenty_minute, R.string.thirty_minute)
                    val buttons = listOf<MaterialButton>(
                        view.findViewById(R.id.btn_1hour),
                        view.findViewById(R.id.btn_2hour),
                        view.findViewById(R.id.btn_6hour),
                        view.findViewById(R.id.btn_12hour),
                        view.findViewById(R.id.btn_24hour)
                    )

                    buttons.forEachIndexed { index, button ->  button.text = getString(buttonText[index]) }
                    examSharedKeys.forEach { minutes.add(sharedPreferences.getBoolean(it, true)) }

                    buttons.forEachIndexed { index, button ->
                        button.isChecked = minutes[index]
                        button.addOnCheckedChangeListener { _, isChecked ->
                            sharedPreferences.edit().putBoolean(examSharedKeys[index], !minutes[index]).apply()
                            minutes[index] = !minutes[index]

                            Toast.makeText(requireContext(), getString(if (minutes[index]) R.string.notify_exam_on else R.string.notify_exam_off, minuteData[index]),
                                Toast.LENGTH_SHORT).apply { setGravity(Gravity.CENTER, 0, 0) }.show()
                        }
                    }

                    setPositiveButton(R.string.close) { _, _ -> }

                    setOnDismissListener {
                        val minutesOn = arrayListOf<String>()
                        minutes.forEachIndexed { index, b ->  if (b) minutesOn.add(minuteData[index]) }
                        prefNotifyExam.summary = if (minutesOn.isNotEmpty()) getString(R.string.notify_exam_on, minutesOn.joinToString(", ")) else getString(R.string.notify_all_off)
                    }

                    setView(view)
                }.show()

                true
            }
            prefTimeMorningReminder?.setOnPreferenceClickListener { preference ->
                val timePicker = MaterialTimePicker.Builder()
                    .setHour(morningCalendar.get(Calendar.HOUR_OF_DAY))
                    .setMinute(morningCalendar.get(Calendar.MINUTE))
                    .setTitleText(R.string.morning_reminder)
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .build()
                timePicker.addOnPositiveButtonClickListener {
                    morningCalendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                    morningCalendar.set(Calendar.MINUTE, timePicker.minute)

                    sharedPreferences.edit().putInt(SharedKey.TIME_MORNING_REMINDER, morningCalendar.get(Calendar.HOUR_OF_DAY) * 60 + morningCalendar.get(Calendar.MINUTE)).apply()
                    preference.summary = timeFormat.format(morningCalendar.time)
                }
                timePicker.show(parentFragmentManager, "MORNING_TIME_PICKER")

                true
            }
            prefTimeNightReminder?.setOnPreferenceClickListener { preference ->
                val timePicker = MaterialTimePicker.Builder()
                    .setHour(nightCalendar.get(Calendar.HOUR_OF_DAY))
                    .setMinute(nightCalendar.get(Calendar.MINUTE))
                    .setTitleText(R.string.night_reminder)
                    .setTimeFormat(TimeFormat.CLOCK_12H)
                    .build()
                timePicker.addOnPositiveButtonClickListener {
                    nightCalendar.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                    nightCalendar.set(Calendar.MINUTE, timePicker.minute)

                    sharedPreferences.edit().putInt(SharedKey.TIME_NIGHT_REMINDER, nightCalendar.get(Calendar.HOUR_OF_DAY) * 60 + nightCalendar.get(Calendar.MINUTE)).apply()
                    preference.summary = timeFormat.format(nightCalendar.time)
                }
                timePicker.show(parentFragmentManager, "NIGHT_TIME_PICKER")

                true
            }
            prefChangelog?.setOnPreferenceClickListener {
                MaterialAlertDialogBuilder(requireContext(), R.style.Theme_SogangAssist_MaterialAlertDialog).apply {
                    setIcon(R.drawable.ic_icon)
                    setTitle("${getString(R.string.real_app_name)} ${BuildConfig.VERSION_NAME} ${getString(R.string.changelog)}")
                    setMessage(R.string.changelog_content)
                    setPositiveButton(R.string.close) { dialog, id ->
                        dialog.dismiss()
                    }
                }.show()

                true
            }
            prefEmail?.setOnPreferenceClickListener {
                Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("my@ien.zone"))
                    putExtra(Intent.EXTRA_SUBJECT, "${getString(R.string.real_app_name)} ${BuildConfig.VERSION_NAME} ${getString(
                                R.string.ask
                            )}")
                    putExtra(Intent.EXTRA_TEXT, "${getString(R.string.email_text)}\n${Build.BRAND} ${Build.MODEL} Android ${Build.VERSION.RELEASE}\n_\n")
                    type = "message/rfc822"
                    startActivity(this)
                }
                true
            }
            prefOpenSource?.setOnPreferenceClickListener {
                startActivity(Intent(requireContext(), OssLicensesMenuActivity::class.java))
                true
            }

            // StartActiviyForResult 객체
            val saveFileLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val output = JSONArray()
                    GlobalScope.launch(Dispatchers.IO) {
                        val datas = lmsDatabase?.getDao()?.getAll()
                        if (datas != null) {
                            for (lms in datas) {
                                val jObject = JSONObject()
                                jObject.put("id", lms.id)
                                jObject.put("className", lms.className)
                                jObject.put("timeStamp", lms.timestamp)
                                jObject.put("type", lms.type)
                                jObject.put("startTime", lms.startTime)
                                jObject.put("endTime", lms.endTime)
                                jObject.put("isRenewAllowed", lms.isRenewAllowed)
                                jObject.put("isFinished", lms.isFinished)
                                jObject.put("week", lms.week)
                                jObject.put("lesson", lms.lesson)
                                jObject.put("homework_name", lms.homework_name)
                                output.put(jObject)
                            }

                            Log.d(TAG, output.toString())

                            val uri = result.data?.data ?: Uri.EMPTY
                            val outputStream = requireActivity().contentResolver.openOutputStream(uri)
                            outputStream?.write(output.toString().toByteArray())
                            outputStream?.close()

                            Toast.makeText(requireContext(), getString(R.string.backup_msg), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            val loadFileLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val uri = result.data?.data ?: Uri.EMPTY
                    val reader: BufferedReader
                    val builder = StringBuilder()
                    try {
                        reader = BufferedReader(InputStreamReader(requireActivity().contentResolver.openInputStream(uri)))
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            builder.append(line)
                        }
                        reader.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    val jsonResult = JSONArray(builder.toString())

                    MaterialAlertDialogBuilder(requireContext(), R.style.Theme_SogangAssist_MaterialAlertDialog).apply {
                        setIcon(R.drawable.ic_notification)
                        setTitle(R.string.restore)
                        setMessage(R.string.restore_msg)
                        setPositiveButton(R.string.agree) { dialog, id ->
                            requireActivity().deleteDatabase("SogangLMSAssistData.db")
                            for (i in 0 until jsonResult.length()) {
                                val jObject = jsonResult.getJSONObject(i)
                                val lms = LMSEntity(
                                    jObject.getString("className"),
                                    jObject.getLong("timeStamp"),
                                    jObject.getInt("type"),
                                    jObject.getLong("startTime"),
                                    jObject.getLong("endTime"),
                                    jObject.getBoolean("isRenewAllowed"),
                                    jObject.getBoolean("isFinished"),
                                    jObject.getInt("week"),
                                    jObject.getInt("lesson"),
                                    jObject.getString("homework_name")
                                )

                                GlobalScope.launch(Dispatchers.IO) {
                                    lmsDatabase?.getDao()?.add(lms)
                                }
                            }

                            Toast.makeText(requireContext(), getString(R.string.restore_finish), Toast.LENGTH_SHORT).show()
                            requireActivity().finishAffinity()
                            dialog.dismiss()
                        }
                        setNegativeButton(R.string.disagree) { dialog, id ->
                            dialog.dismiss()
                        }
                    }.show()
                }
            }

            prefBackup?.setOnPreferenceClickListener {
                val saveFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())

                Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TITLE, "albatross_backup_${saveFormat.format(Calendar.getInstance().time)}.txt")
                    saveFileLauncher.launch(this)
                }
                true
            }
            prefRestore?.setOnPreferenceClickListener {
                Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "text/plain"
                    loadFileLauncher.launch(this)
                }
                true
            }
            prefDateDelete?.setOnPreferenceClickListener {
                MaterialAlertDialogBuilder(requireContext(), R.style.Theme_SogangAssist_MaterialAlertDialog).apply {
                    setTitle(R.string.date_delete_title)
                    setIcon(R.drawable.ic_delete)

                    val view = layoutInflater.inflate(R.layout.dialog_date_delete, LinearLayout(requireContext()), false)
                    val tvStartDate: TextView = view.findViewById(R.id.tv_start_date)
                    val tvEndDate: TextView = view.findViewById(R.id.tv_end_date)
                    val checkDeleteFinish: MaterialCheckBox = view.findViewById(R.id.check_delete_finish)

                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val startCalendar = Calendar.getInstance().apply { add(Calendar.MONTH, -2) }
                    val endCalendar = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }

                    tvStartDate.text = dateFormat.format(startCalendar.time)
                    tvEndDate.text = dateFormat.format(endCalendar.time)

                    tvStartDate.setOnClickListener {
                        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                            startCalendar.set(Calendar.YEAR, year)
                            startCalendar.set(Calendar.MONTH, month)
                            startCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            startCalendar.set(Calendar.HOUR_OF_DAY, 0)
                            startCalendar.set(Calendar.MINUTE, 0)
                            startCalendar.set(Calendar.SECOND, 0)

                            tvStartDate.text = dateFormat.format(startCalendar.time)

                            if (startCalendar.timeInMillis > endCalendar.timeInMillis) {
                                tvStartDate.error = getString(R.string.err_start_date)
                                Toast.makeText(requireContext(), getString(R.string.err_start_date), Toast.LENGTH_SHORT).show()
                            } else {
                                tvStartDate.error = null
                                tvEndDate.error = null
                            }
                        }, startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH)).show()
                    }

                    tvEndDate.setOnClickListener {
                        DatePickerDialog(requireContext(), { _, year, month, dayOfMonth ->
                            endCalendar.set(Calendar.YEAR, year)
                            endCalendar.set(Calendar.MONTH, month)
                            endCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            startCalendar.set(Calendar.HOUR_OF_DAY, 23)
                            startCalendar.set(Calendar.MINUTE, 59)
                            startCalendar.set(Calendar.SECOND, 59)

                            tvEndDate.text = dateFormat.format(endCalendar.time)

                            if (startCalendar.timeInMillis > endCalendar.timeInMillis) {
                                tvEndDate.error = getString(R.string.err_end_date)
                                Toast.makeText(requireContext(), getString(R.string.err_end_date), Toast.LENGTH_SHORT).show()
                            } else {
                                tvStartDate.error = null
                                tvEndDate.error = null
                            }
                        }, endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH)).show()
                    }

                    setPositiveButton(R.string.delete) { dialog, id ->
                        if (startCalendar.timeInMillis > endCalendar.timeInMillis) {
                            Toast.makeText(requireContext(), getString(R.string.err_end_date), Toast.LENGTH_SHORT).show()
                        } else {
                            GlobalScope.launch(Dispatchers.IO) {
                                var count = 0
                                val datas = lmsDatabase?.getDao()?.getByEndTime(startCalendar.timeInMillis, endCalendar.timeInMillis)
                                if (datas != null) {
                                    for (data in datas) {
                                        if (checkDeleteFinish.isChecked) {
                                            if (data.isFinished) {
                                                count++
                                            }
                                        } else {
                                            lmsDatabase?.getDao()?.delete(data.id?.toLong() ?: -1)
                                            count++
                                        }
                                    }
                                }

                                if (count != 0) {
                                    Toast.makeText(requireContext(), getString(R.string.delete_successfully, count), Toast.LENGTH_SHORT).show()
                                    requireActivity().finishAffinity()
                                } else {
                                    Toast.makeText(requireContext(), getString(R.string.no_delete_event), Toast.LENGTH_SHORT).show()
                                }
                                dialog.dismiss()
                            }
                        }
                    }

                    setPositiveButton(android.R.string.cancel) { dialog, id ->
                        dialog.dismiss()
                    }

                    setView(view)
                }.show()

                true
            }
        }
    }
}