package net.ienlab.sogangassist.activity

import android.animation.ValueAnimator
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Typeface
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
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.radiobutton.MaterialRadioButton
import net.ienlab.sogangassist.BuildConfig
import net.ienlab.sogangassist.data.LMSClass
import net.ienlab.sogangassist.databinding.ActivitySettingsBinding
import net.ienlab.sogangassist.utils.MyUtils
import net.ienlab.sogangassist.database.*
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.constant.DefaultValue
import net.ienlab.sogangassist.constant.SharedKey
import net.ienlab.sogangassist.utils.AppStorage
import net.ienlab.sogangassist.utils.MyBottomSheetDialog
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

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
        binding.appTitle.typeface = Typeface.createFromAsset(assets, "fonts/Pretendard-Black.otf")

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment(), null).commit()

        storage = AppStorage(this)
        bp = BillingProcessor(this, getString(R.string.iab_license), this)
        bp.initialize()
        bp.loadOwnedPurchasesFromGoogle()

        binding.appTitle.setOnLongClickListener {
            bp.consumePurchase(AppStorage.ADS_FREE)
            true
        }
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
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

        lateinit var dbHelper: DBHelper
        lateinit var sharedPreferences: SharedPreferences

        val timeFormat = SimpleDateFormat("a h:mm", Locale.getDefault())

        lateinit var typefaceBold: Typeface
        lateinit var typefaceRegular: Typeface

        override fun onCreatePreferences(bundle: Bundle?, str: String?) {
            addPreferencesFromResource(R.xml.root_preferences)
            val appInfo = findPreference("app_title")
            val dndTime = findPreference("dnd_time")
            val notifyHw = findPreference("notify_hw_group")
            val notifyLec = findPreference("notify_lec_group")
            val notifyZoom = findPreference("notify_zoom_group")
            val notifyExam = findPreference("notify_exam_group")
            val timeMorningReminder = findPreference(SharedKey.TIME_MORNING_REMINDER)
            val timeNightReminder = findPreference(SharedKey.TIME_NIGHT_REMINDER)
            val calendarIconCheck = findPreference(SharedKey.CALENDAR_ICON_SHOW) as CheckBoxPreference
            val dateDelete = findPreference("date_delete")
            val changelog = findPreference("changelog")
            val email = findPreference("ask_to_dev")
            val openSource = findPreference("open_source")
            val backup = findPreference("backup")
            val restore = findPreference("restore")

            typefaceRegular = Typeface.createFromAsset(requireActivity().assets, "fonts/Pretendard-Regular.otf")
            typefaceBold = Typeface.createFromAsset(requireActivity().assets, "fonts/Pretendard-Black.otf")

            dbHelper = DBHelper(requireContext(), DBHelper.dbName, DBHelper.dbVersion)
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

            notifyHw?.summary = if (hwHoursOn.isNotEmpty()) getString(R.string.notify_hw_on, hwHoursOn.joinToString(", ")) else getString(R.string.notify_all_off)
            notifyLec?.summary = if (lecHoursOn.isNotEmpty()) getString(R.string.notify_lec_on, lecHoursOn.joinToString(", ")) else getString(R.string.notify_all_off)
            notifyZoom?.summary = if (zoomMinutesOn.isNotEmpty()) getString(R.string.notify_zoom_on, zoomMinutesOn.joinToString(", ")) else getString(R.string.notify_all_off)
            notifyExam?.summary = if (zoomMinutesOn.isNotEmpty()) getString(R.string.notify_exam_on, examMinutesOn.joinToString(", ")) else getString(R.string.notify_all_off)

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

            timeMorningReminder?.summary = timeFormat.format(morningCalendar.time)
            timeNightReminder?.summary = timeFormat.format(nightCalendar.time)
            dndTime?.summary = "${timeFormat.format(dndStartCalendar.time)} ~ ${timeFormat.format(dndEndCalendar.time)}"

            calendarIconCheck.setOnPreferenceChangeListener { _, _ ->
                Toast.makeText(context, requireContext().getString(R.string.restart_to_apply), Toast.LENGTH_SHORT).show()
                true
            }

            appInfo?.setOnPreferenceClickListener {
                MyBottomSheetDialog(requireContext()).apply {
                    val view = layoutInflater.inflate(R.layout.dialog_changelog, LinearLayout(requireContext()), false)
                    val tvVersion: TextView = view.findViewById(R.id.tv_version)
                    val tvContent: TextView = view.findViewById(R.id.content)

                    tvVersion.typeface = typefaceBold
                    tvContent.typeface = typefaceRegular

                    tvVersion.text = getString(R.string.real_app_name)
                    tvContent.text = getString(R.string.dev_ienlab)

                    setContentView(view)
                }.show()

                true
            }
            dndTime?.setOnPreferenceClickListener { preference ->
                MyBottomSheetDialog(requireContext()).apply {
                    dismissWithAnimation = true

                    val view = layoutInflater.inflate(R.layout.dialog_dnd, LinearLayout(requireContext()), false)
                    val tvTitle: TextView = view.findViewById(R.id.tv_title)
                    val tvDndStartTag: TextView = view.findViewById(R.id.tv_dnd_start_tag)
                    val tvDndEndTag: TextView = view.findViewById(R.id.tv_dnd_end_tag)
                    val tvDndStart: TextView = view.findViewById(R.id.tv_dnd_start)
                    val tvDndEnd: TextView = view.findViewById(R.id.tv_dnd_end)
                    val btnPositive: LinearLayout = view.findViewById(R.id.btn_positive)
                    val btnNegative: LinearLayout = view.findViewById(R.id.btn_negative)
                    val tvPositive: TextView = view.findViewById(R.id.btn_positive_text)
                    val tvNegative: TextView = view.findViewById(R.id.btn_negative_text)

                    tvTitle.typeface = typefaceBold
                    tvDndStartTag.typeface = typefaceRegular
                    tvDndEndTag.typeface = typefaceRegular
                    tvDndStart.typeface = typefaceBold
                    tvDndEnd.typeface = typefaceBold
                    tvPositive.typeface = typefaceRegular
                    tvNegative.typeface = typefaceRegular

                    tvDndStart.text = timeFormat.format(dndStartCalendar.time)
                    tvDndEnd.text = timeFormat.format(dndEndCalendar.time)

                    val startCalendar = dndStartCalendar.clone() as Calendar
                    val endCalendar = dndEndCalendar.clone() as Calendar

                    tvDndStart.setOnClickListener {
                        MyBottomSheetDialog(requireContext()).apply {
                            dismissWithAnimation = true

                            val dialogView = layoutInflater.inflate(R.layout.dialog_time_picker, LinearLayout(requireContext()), false)

                            val innerTvTitle: TextView = dialogView.findViewById(R.id.tv_title)
                            val timePicker: TimePicker = dialogView.findViewById(R.id.time_picker)
                            val innerBtnPositive: LinearLayout = dialogView.findViewById(R.id.btn_positive)
                            val innerBtnNegative: LinearLayout = dialogView.findViewById(R.id.btn_negative)
                            val innerTvPositive: TextView = dialogView.findViewById(R.id.btn_positive_text)
                            val innerTvNegative: TextView = dialogView.findViewById(R.id.btn_negative_text)
                            val calendar = startCalendar.clone() as Calendar

                            innerTvTitle.typeface = typefaceBold
                            innerTvPositive.typeface = typefaceRegular
                            innerTvNegative.typeface = typefaceRegular

                            val hoursId = Resources.getSystem().getIdentifier("hours", "id", "android")
                            val separatorId = Resources.getSystem().getIdentifier("separator", "id", "android")
                            val minutesId = Resources.getSystem().getIdentifier("minutes", "id", "android")
                            val amLabelId = Resources.getSystem().getIdentifier("am_label", "id", "android")
                            val pmLabelId = Resources.getSystem().getIdentifier("pm_label", "id", "android")

                            val textViews: ArrayList<TextView> = arrayListOf(timePicker.findViewById(hoursId), timePicker.findViewById(separatorId), timePicker.findViewById(minutesId))
                            val apmLabels: ArrayList<MaterialRadioButton> = arrayListOf(timePicker.findViewById(amLabelId), timePicker.findViewById(pmLabelId))

                            textViews.forEach {
                                it.typeface = typefaceRegular
                                it.textSize = 42f
                            }
                            apmLabels.forEachIndexed { index, button ->
                                button.typeface = typefaceRegular
                                button.textSize = 12f
                                button.gravity = (if (index == 0) Gravity.BOTTOM else Gravity.TOP) or Gravity.END
                            }

                            innerTvTitle.text = getString(R.string.start_at)

                            innerBtnNegative.setOnClickListener {
                                dismiss()
                            }

                            with (timePicker) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    hour = calendar.get(Calendar.HOUR_OF_DAY)
                                    minute = calendar.get(Calendar.MINUTE)
                                } else {
                                    currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                                    currentMinute = calendar.get(Calendar.MINUTE)
                                }

                                setOnTimeChangedListener { v, hourOfDay, minute ->
                                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                    calendar.set(Calendar.MINUTE, minute)
                                }
                            }

                            innerBtnPositive.setOnClickListener {
                                startCalendar.time = calendar.time
                                tvDndStart.text = timeFormat.format(startCalendar.time)
                                dismiss()
                            }

                            setContentView(dialogView)
                        }.show()
                    }
                    tvDndEnd.setOnClickListener {
                        MyBottomSheetDialog(requireContext()).apply {
                            dismissWithAnimation = true

                            val dialogView = layoutInflater.inflate(R.layout.dialog_time_picker, LinearLayout(requireContext()), false)

                            val innerTvTitle: TextView = dialogView.findViewById(R.id.tv_title)
                            val timePicker: TimePicker = dialogView.findViewById(R.id.time_picker)
                            val innerBtnPositive: LinearLayout = dialogView.findViewById(R.id.btn_positive)
                            val innerBtnNegative: LinearLayout = dialogView.findViewById(R.id.btn_negative)
                            val innerTvPositive: TextView = dialogView.findViewById(R.id.btn_positive_text)
                            val innerTvNegative: TextView = dialogView.findViewById(R.id.btn_negative_text)
                            val calendar = endCalendar.clone() as Calendar

                            innerTvTitle.typeface = typefaceBold
                            innerTvPositive.typeface = typefaceRegular
                            innerTvNegative.typeface = typefaceRegular

                            val hoursId = Resources.getSystem().getIdentifier("hours", "id", "android")
                            val separatorId = Resources.getSystem().getIdentifier("separator", "id", "android")
                            val minutesId = Resources.getSystem().getIdentifier("minutes", "id", "android")
                            val amLabelId = Resources.getSystem().getIdentifier("am_label", "id", "android")
                            val pmLabelId = Resources.getSystem().getIdentifier("pm_label", "id", "android")

                            val textViews: ArrayList<TextView> = arrayListOf(timePicker.findViewById(hoursId), timePicker.findViewById(separatorId), timePicker.findViewById(minutesId))
                            val apmLabels: ArrayList<MaterialRadioButton> = arrayListOf(timePicker.findViewById(amLabelId), timePicker.findViewById(pmLabelId))

                            textViews.forEach {
                                it.typeface = typefaceRegular
                                it.textSize = 42f
                            }
                            apmLabels.forEachIndexed { index, button ->
                                button.typeface = typefaceRegular
                                button.textSize = 12f
                                button.gravity = (if (index == 0) Gravity.BOTTOM else Gravity.TOP) or Gravity.END
                            }

                            innerTvTitle.text = getString(R.string.end_at)

                            innerBtnNegative.setOnClickListener {
                                dismiss()
                            }

                            with (timePicker) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    hour = calendar.get(Calendar.HOUR_OF_DAY)
                                    minute = calendar.get(Calendar.MINUTE)
                                } else {
                                    currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                                    currentMinute = calendar.get(Calendar.MINUTE)
                                }

                                setOnTimeChangedListener { v, hourOfDay, minute ->
                                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                    calendar.set(Calendar.MINUTE, minute)
                                }
                            }

                            innerBtnPositive.setOnClickListener {
                                endCalendar.time = calendar.time
                                tvDndEnd.text = timeFormat.format(endCalendar.time)
                                dismiss()
                            }

                            setContentView(dialogView)
                        }.show()
                    }

                    btnPositive.setOnClickListener {
                        dndStartCalendar.time = startCalendar.time
                        dndEndCalendar.time = endCalendar.time
                        sharedPreferences.edit().putInt(SharedKey.DND_START_TIME, dndStartCalendar.get(Calendar.HOUR_OF_DAY) * 60 + dndStartCalendar.get(Calendar.MINUTE)).apply()
                        sharedPreferences.edit().putInt(SharedKey.DND_END_TIME, dndEndCalendar.get(Calendar.HOUR_OF_DAY) * 60 + dndEndCalendar.get(Calendar.MINUTE)).apply()
                        preference.summary = "${timeFormat.format(dndStartCalendar.time)} ~ ${timeFormat.format(dndEndCalendar.time)}"
                        dismiss()
                    }

                    btnNegative.setOnClickListener {
                        dismiss()
                    }

                    setContentView(view)
                }.show()

                true
            }
            notifyHw?.setOnPreferenceClickListener {
                MyBottomSheetDialog(requireContext()).apply {
                    dismissWithAnimation = true

                    val view = layoutInflater.inflate(R.layout.dialog_notify_time, LinearLayout(requireContext()), false)
                    val imgLogo: ImageView = view.findViewById(R.id.imgLogo)
                    val tvTitle: TextView = view.findViewById(R.id.tv_title)
                    val btnClose: ImageButton = view.findViewById(R.id.btn_close)
                    val hours = arrayListOf<Boolean>()

                    tvTitle.typeface = typefaceBold
                    tvTitle.text = getString(R.string.notify_hw)
                    imgLogo.setImageResource(R.drawable.ic_assignment)

                    val buttons = listOf<ImageButton>(
                        view.findViewById(R.id.btn_1hour),
                        view.findViewById(R.id.btn_2hour),
                        view.findViewById(R.id.btn_6hour),
                        view.findViewById(R.id.btn_12hour),
                        view.findViewById(R.id.btn_24hour)
                    )

                    hwSharedKeys.forEach { hours.add(sharedPreferences.getBoolean(it, true)) }

                    buttons.forEachIndexed { index, imageButton ->
                        imageButton.alpha = if (hours[index]) 1f else 0.3f
                        imageButton.setOnClickListener {
                            ValueAnimator.ofFloat(if (hours[index]) 1f else 0.3f, if (hours[index]) 0.3f else 1f).apply {
                                duration = 300
                                addUpdateListener {
                                    imageButton.alpha = (it.animatedValue as Float)
                                }
                            }.start()
                            sharedPreferences.edit().putBoolean(hwSharedKeys[index], !hours[index]).apply()
                            hours[index] = !hours[index]

                            Toast.makeText(requireContext(), getString(if (hours[index]) R.string.notify_hw_on else R.string.notify_hw_off, hourData[index]),
                                Toast.LENGTH_SHORT).apply { setGravity(Gravity.CENTER, 0, 0) }.show()
                        }
                    }

                    btnClose.setOnClickListener {
                        dismiss()
                    }

                    setOnDismissListener {
                        val hoursOn = arrayListOf<String>()
                        hours.forEachIndexed { index, b ->  if (b) hoursOn.add(hourData[index]) }
                        notifyHw.summary = if (hoursOn.isNotEmpty()) getString(R.string.notify_hw_on, hoursOn.joinToString(", ")) else getString(R.string.notify_all_off)
                    }

                    setContentView(view)
                }.show()

                true
            }
            notifyLec?.setOnPreferenceClickListener {
                MyBottomSheetDialog(requireContext()).apply {
                    dismissWithAnimation = true

                    val view = layoutInflater.inflate(R.layout.dialog_notify_time, LinearLayout(requireContext()), false)
                    val imgLogo: ImageView = view.findViewById(R.id.imgLogo)
                    val tvTitle: TextView = view.findViewById(R.id.tv_title)
                    val btnClose: ImageButton = view.findViewById(R.id.btn_close)
                    val hours = arrayListOf<Boolean>()

                    tvTitle.typeface = typefaceBold
                    tvTitle.text = getString(R.string.notify_lec)
                    imgLogo.setImageResource(R.drawable.ic_video)

                    val buttons = listOf<ImageButton>(
                        view.findViewById(R.id.btn_1hour),
                        view.findViewById(R.id.btn_2hour),
                        view.findViewById(R.id.btn_6hour),
                        view.findViewById(R.id.btn_12hour),
                        view.findViewById(R.id.btn_24hour)
                    )

                    lecSharedKeys.forEach { hours.add(sharedPreferences.getBoolean(it, true)) }

                    buttons.forEachIndexed { index, imageButton ->
                        imageButton.alpha = if (hours[index]) 1f else 0.3f
                        imageButton.setOnClickListener {
                            ValueAnimator.ofFloat(if (hours[index]) 1f else 0.3f, if (hours[index]) 0.3f else 1f).apply {
                                duration = 300
                                addUpdateListener {
                                    imageButton.alpha = (it.animatedValue as Float)
                                }
                            }.start()
                            sharedPreferences.edit().putBoolean(lecSharedKeys[index], !hours[index]).apply()
                            hours[index] = !hours[index]

                            Toast.makeText(requireContext(), getString(if (hours[index]) R.string.notify_lec_on else R.string.notify_lec_off, hourData[index]),
                                Toast.LENGTH_SHORT).apply { setGravity(Gravity.CENTER, 0, 0) }.show()
                        }
                    }

                    btnClose.setOnClickListener {
                        dismiss()
                    }

                    setOnDismissListener {
                        val hoursOn = arrayListOf<String>()
                        hours.forEachIndexed { index, b ->  if (b) hoursOn.add(hourData[index]) }
                        notifyLec.summary = if (hoursOn.isNotEmpty()) getString(R.string.notify_lec_on, hoursOn.joinToString(", ")) else getString(R.string.notify_all_off)
                    }

                    setContentView(view)
                }.show()

                true
            }
            notifyZoom?.setOnPreferenceClickListener {
                MyBottomSheetDialog(requireContext()).apply {
                    dismissWithAnimation = true

                    val view = layoutInflater.inflate(R.layout.dialog_notify_time, LinearLayout(requireContext()), false)
                    val imgLogo: ImageView = view.findViewById(R.id.imgLogo)
                    val tvTitle: TextView = view.findViewById(R.id.tv_title)
                    val btnClose: ImageButton = view.findViewById(R.id.btn_close)
                    val minutes = arrayListOf<Boolean>()

                    tvTitle.typeface = typefaceBold
                    tvTitle.text = getString(R.string.notify_zoom)
                    imgLogo.setImageResource(R.drawable.ic_live_class)

                    val buttonRes = listOf(R.drawable.ic_3minute, R.drawable.ic_5minute, R.drawable.ic_10minute, R.drawable.ic_20minute, R.drawable.ic_30minute)
                    val buttons = listOf<ImageButton>(
                        view.findViewById(R.id.btn_1hour),
                        view.findViewById(R.id.btn_2hour),
                        view.findViewById(R.id.btn_6hour),
                        view.findViewById(R.id.btn_12hour),
                        view.findViewById(R.id.btn_24hour)
                    )

                    buttons.forEachIndexed { index, imageButton ->  imageButton.setImageResource(buttonRes[index])}
                    zoomSharedKeys.forEach { minutes.add(sharedPreferences.getBoolean(it, true)) }

                    buttons.forEachIndexed { index, imageButton ->
                        imageButton.alpha = if (minutes[index]) 1f else 0.3f
                        imageButton.setOnClickListener {
                            ValueAnimator.ofFloat(if (minutes[index]) 1f else 0.3f, if (minutes[index]) 0.3f else 1f).apply {
                                duration = 300
                                addUpdateListener {
                                    imageButton.alpha = (it.animatedValue as Float)
                                }
                            }.start()
                            sharedPreferences.edit().putBoolean(zoomSharedKeys[index], !minutes[index]).apply()
                            minutes[index] = !minutes[index]

                            Toast.makeText(requireContext(), getString(if (minutes[index]) R.string.notify_zoom_on else R.string.notify_zoom_off, minuteData[index]),
                                Toast.LENGTH_SHORT).apply { setGravity(Gravity.CENTER, 0, 0) }.show()
                        }
                    }

                    btnClose.setOnClickListener {
                        dismiss()
                    }

                    setOnDismissListener {
                        val minutesOn = arrayListOf<String>()
                        minutes.forEachIndexed { index, b ->  if (b) minutesOn.add(minuteData[index]) }
                        notifyZoom.summary = if (minutesOn.isNotEmpty()) getString(R.string.notify_zoom_on, minutesOn.joinToString(", ")) else getString(R.string.notify_all_off)
                    }

                    setContentView(view)
                }.show()

                true
            }
            notifyExam?.setOnPreferenceClickListener {
                MyBottomSheetDialog(requireContext()).apply {
                    dismissWithAnimation = true

                    val view = layoutInflater.inflate(R.layout.dialog_notify_time, LinearLayout(requireContext()), false)
                    val imgLogo: ImageView = view.findViewById(R.id.imgLogo)
                    val tvTitle: TextView = view.findViewById(R.id.tv_title)
                    val btnClose: ImageButton = view.findViewById(R.id.btn_close)
                    val minutes = arrayListOf<Boolean>()

                    tvTitle.typeface = typefaceBold
                    tvTitle.text = getString(R.string.notify_exam)
                    imgLogo.setImageResource(R.drawable.ic_test)

                    val buttonRes = listOf(R.drawable.ic_3minute, R.drawable.ic_5minute, R.drawable.ic_10minute, R.drawable.ic_20minute, R.drawable.ic_30minute)
                    val buttons = listOf<ImageButton>(
                        view.findViewById(R.id.btn_1hour),
                        view.findViewById(R.id.btn_2hour),
                        view.findViewById(R.id.btn_6hour),
                        view.findViewById(R.id.btn_12hour),
                        view.findViewById(R.id.btn_24hour)
                    )

                    buttons.forEachIndexed { index, imageButton ->  imageButton.setImageResource(buttonRes[index])}
                    examSharedKeys.forEach { minutes.add(sharedPreferences.getBoolean(it, true)) }

                    buttons.forEachIndexed { index, imageButton ->
                        imageButton.alpha = if (minutes[index]) 1f else 0.3f
                        imageButton.setOnClickListener {
                            ValueAnimator.ofFloat(if (minutes[index]) 1f else 0.3f, if (minutes[index]) 0.3f else 1f).apply {
                                duration = 300
                                addUpdateListener {
                                    imageButton.alpha = (it.animatedValue as Float)
                                }
                            }.start()
                            sharedPreferences.edit().putBoolean(examSharedKeys[index], !minutes[index]).apply()
                            minutes[index] = !minutes[index]

                            Toast.makeText(requireContext(), getString(if (minutes[index]) R.string.notify_exam_on else R.string.notify_exam_off, minuteData[index]),
                                Toast.LENGTH_SHORT).apply { setGravity(Gravity.CENTER, 0, 0) }.show()
                        }
                    }

                    btnClose.setOnClickListener {
                        dismiss()
                    }

                    setOnDismissListener {
                        val minutesOn = arrayListOf<String>()
                        minutes.forEachIndexed { index, b ->  if (b) minutesOn.add(minuteData[index]) }
                        notifyExam.summary = if (minutesOn.isNotEmpty()) getString(R.string.notify_exam_on, minutesOn.joinToString(", ")) else getString(R.string.notify_all_off)
                    }

                    setContentView(view)
                }.show()

                true
            }
            timeMorningReminder?.setOnPreferenceClickListener { preference ->
                MyBottomSheetDialog(requireContext()).apply {
                    dismissWithAnimation = true

                    val view = layoutInflater.inflate(R.layout.dialog_time_picker, LinearLayout(requireContext()), false)

                    val tvTitle: TextView = view.findViewById(R.id.tv_title)
                    val timePicker: TimePicker = view.findViewById(R.id.time_picker)
                    val btnPositive: LinearLayout = view.findViewById(R.id.btn_positive)
                    val btnNegative: LinearLayout = view.findViewById(R.id.btn_negative)
                    val tvPositive: TextView = view.findViewById(R.id.btn_positive_text)
                    val tvNegative: TextView = view.findViewById(R.id.btn_negative_text)
                    val calendar = morningCalendar.clone() as Calendar

                    tvTitle.typeface = typefaceBold
                    tvPositive.typeface = typefaceRegular
                    tvNegative.typeface = typefaceRegular

                    val hoursId = Resources.getSystem().getIdentifier("hours", "id", "android")
                    val separatorId = Resources.getSystem().getIdentifier("separator", "id", "android")
                    val minutesId = Resources.getSystem().getIdentifier("minutes", "id", "android")
                    val amLabelId = Resources.getSystem().getIdentifier("am_label", "id", "android")
                    val pmLabelId = Resources.getSystem().getIdentifier("pm_label", "id", "android")

                    val textViews: ArrayList<TextView> = arrayListOf(timePicker.findViewById(hoursId), timePicker.findViewById(separatorId), timePicker.findViewById(minutesId))
                    val apmLabels: ArrayList<MaterialRadioButton> = arrayListOf(timePicker.findViewById(amLabelId), timePicker.findViewById(pmLabelId))

                    textViews.forEach {
                        it.typeface = typefaceRegular
                        it.textSize = 42f
                    }
                    apmLabels.forEachIndexed { index, button ->
                        button.typeface = typefaceRegular
                        button.textSize = 12f
                        button.gravity = (if (index == 0) Gravity.BOTTOM else Gravity.TOP) or Gravity.END
                    }

                    tvTitle.text = getString(R.string.morning_reminder)

                    btnNegative.setOnClickListener {
                        dismiss()
                    }

                    with (timePicker) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            hour = calendar.get(Calendar.HOUR_OF_DAY)
                            minute = calendar.get(Calendar.MINUTE)
                        } else {
                            currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                            currentMinute = calendar.get(Calendar.MINUTE)
                        }

                        setOnTimeChangedListener { v, hourOfDay, minute ->
                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            calendar.set(Calendar.MINUTE, minute)
                        }
                    }

                    btnPositive.setOnClickListener {
                        morningCalendar.time = calendar.time
                        sharedPreferences.edit().putInt(SharedKey.TIME_MORNING_REMINDER, morningCalendar.get(Calendar.HOUR_OF_DAY) * 60 + morningCalendar.get(Calendar.MINUTE)).apply()
                        preference.summary = timeFormat.format(morningCalendar.time)
                        dismiss()
                    }

                    setContentView(view)
                }.show()

                true
            }
            timeNightReminder?.setOnPreferenceClickListener { preference ->
                MyBottomSheetDialog(requireContext()).apply {
                    dismissWithAnimation = true

                    val view = layoutInflater.inflate(R.layout.dialog_time_picker, LinearLayout(requireContext()), false)

                    val tvTitle: TextView = view.findViewById(R.id.tv_title)
                    val timePicker: TimePicker = view.findViewById(R.id.time_picker)
                    val btnPositive: LinearLayout = view.findViewById(R.id.btn_positive)
                    val btnNegative: LinearLayout = view.findViewById(R.id.btn_negative)
                    val tvPositive: TextView = view.findViewById(R.id.btn_positive_text)
                    val tvNegative: TextView = view.findViewById(R.id.btn_negative_text)
                    val calendar = nightCalendar.clone() as Calendar

                    tvTitle.typeface = typefaceBold
                    tvPositive.typeface = typefaceRegular
                    tvNegative.typeface = typefaceRegular

                    val hoursId = Resources.getSystem().getIdentifier("hours", "id", "android")
                    val separatorId = Resources.getSystem().getIdentifier("separator", "id", "android")
                    val minutesId = Resources.getSystem().getIdentifier("minutes", "id", "android")
                    val amLabelId = Resources.getSystem().getIdentifier("am_label", "id", "android")
                    val pmLabelId = Resources.getSystem().getIdentifier("pm_label", "id", "android")

                    val textViews: ArrayList<TextView> = arrayListOf(timePicker.findViewById(hoursId), timePicker.findViewById(separatorId), timePicker.findViewById(minutesId))
                    val apmLabels: ArrayList<MaterialRadioButton> = arrayListOf(timePicker.findViewById(amLabelId), timePicker.findViewById(pmLabelId))

                    textViews.forEach {
                        it.typeface = typefaceRegular
                        it.textSize = 42f
                    }
                    apmLabels.forEachIndexed { index, button ->
                        button.typeface = typefaceRegular
                        button.textSize = 12f
                        button.gravity = (if (index == 0) Gravity.BOTTOM else Gravity.TOP) or Gravity.END
                    }

                    tvTitle.text = getString(R.string.night_reminder)

                    btnNegative.setOnClickListener {
                        dismiss()
                    }

                    with (timePicker) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            hour = calendar.get(Calendar.HOUR_OF_DAY)
                            minute = calendar.get(Calendar.MINUTE)
                        } else {
                            currentHour = calendar.get(Calendar.HOUR_OF_DAY)
                            currentMinute = calendar.get(Calendar.MINUTE)
                        }

                        setOnTimeChangedListener { v, hourOfDay, minute ->
                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            calendar.set(Calendar.MINUTE, minute)
                        }
                    }

                    btnPositive.setOnClickListener {
                        nightCalendar.time = calendar.time
                        sharedPreferences.edit().putInt(SharedKey.TIME_NIGHT_REMINDER, nightCalendar.get(Calendar.HOUR_OF_DAY) * 60 + nightCalendar.get(Calendar.MINUTE)).apply()
                        preference.summary = timeFormat.format(nightCalendar.time)
                        dismiss()
                    }

                    setContentView(view)
                }.show()

                true
            }
            changelog?.setOnPreferenceClickListener {
                MyBottomSheetDialog(requireContext()).apply {
                    val view = layoutInflater.inflate(R.layout.dialog_changelog, LinearLayout(requireContext()), false)
                    val tvVersion: TextView = view.findViewById(R.id.tv_version)
                    val tvContent: TextView = view.findViewById(R.id.content)

                    tvVersion.typeface = typefaceBold
                    tvContent.typeface = typefaceRegular

                    tvVersion.text = "${getString(R.string.real_app_name)} ${BuildConfig.VERSION_NAME}"
                    tvContent.text = MyUtils.fromHtml(MyUtils.readTextFromRaw(resources, R.raw.changelog))

                    setContentView(view)
                }.show()

                true
            }
            email?.setOnPreferenceClickListener {
                Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("admin@ienlab.net"))
                    putExtra(Intent.EXTRA_SUBJECT, "${getString(R.string.real_app_name)} ${BuildConfig.VERSION_NAME} ${getString(
                                R.string.ask
                            )}")
                    putExtra(Intent.EXTRA_TEXT, "${getString(R.string.email_text)}\n${Build.BRAND} ${Build.MODEL} Android ${Build.VERSION.RELEASE}\n_\n")
                    type = "message/rfc822"
                    startActivity(this)
                }
                true
            }
            openSource?.setOnPreferenceClickListener {
                startActivity(Intent(requireContext(), OssLicensesMenuActivity::class.java))
                true
            }

            // StartActiviyForResult 객체
            val saveFileLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val output = JSONArray()
                    for (lms in dbHelper.getAllData()) {
                        val jObject = JSONObject()
                        jObject.put("id", lms.id)
                        jObject.put("className", lms.className)
                        jObject.put("timeStamp", lms.timeStamp)
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

                    MyBottomSheetDialog(requireContext()).apply {
                        dismissWithAnimation = true

                        val view = layoutInflater.inflate(R.layout.dialog, LinearLayout(context), false)
                        val imgLogo: ImageView = view.findViewById(R.id.imgLogo)
                        val tvTitle: TextView = view.findViewById(R.id.tv_title)
                        val tvContent: TextView = view.findViewById(R.id.tv_content)
                        val btnPositive: LinearLayout = view.findViewById(R.id.btn_positive)
                        val btnNegative: LinearLayout = view.findViewById(R.id.btn_negative)
                        val tvPositive: TextView = view.findViewById(R.id.btn_positive_text)
                        val tvNegative: TextView = view.findViewById(R.id.btn_negative_text)

                        imgLogo.setImageResource(R.drawable.ic_notification)
                        tvTitle.typeface = typefaceBold
                        tvContent.typeface = typefaceRegular
                        tvPositive.typeface = typefaceRegular
                        tvNegative.typeface = typefaceRegular

                        tvTitle.text = getString(R.string.restore)
                        tvContent.text = getString(R.string.restore_msg)
                        tvPositive.text = getString(R.string.agree)
                        tvNegative.text = getString(R.string.disagree)

                        btnPositive.setOnClickListener {
                            requireActivity().deleteDatabase(DBHelper.dbName)
                            for (i in 0 until jsonResult.length()) {
                                val jObject = jsonResult.getJSONObject(i)
                                val lms = LMSClass(
                                    -1,
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

                                dbHelper.addItem(lms)
                            }

                            Toast.makeText(requireContext(), getString(R.string.restore_finish), Toast.LENGTH_SHORT).show()
                            requireActivity().finishAffinity()
                            dismiss()
                        }

                        btnNegative.setOnClickListener {
                            dismiss()
                        }

                        setContentView(view)
                    }.show()
                }
            }

            backup?.setOnPreferenceClickListener {
                val saveFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())

                Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TITLE, "albatross_backup_${saveFormat.format(Calendar.getInstance().time)}.txt")
                    saveFileLauncher.launch(this)
                }
                true
            }
            restore?.setOnPreferenceClickListener {
                Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "text/plain"
                    loadFileLauncher.launch(this)
                }
                true
            }
            dateDelete?.setOnPreferenceClickListener {
                MyBottomSheetDialog(requireContext()).apply {
                    val view = layoutInflater.inflate(R.layout.dialog_date_delete, LinearLayout(requireContext()), false)
                    val tvTitle: TextView = view.findViewById(R.id.tv_title)
                    val tvStartDate: TextView = view.findViewById(R.id.tv_start_date)
                    val tvEndDate: TextView = view.findViewById(R.id.tv_end_date)
                    val tvWave: TextView = view.findViewById(R.id.tv_wave)
                    val checkDeleteFinish: MaterialCheckBox = view.findViewById(R.id.check_delete_finish)
                    val tvPositive: TextView = view.findViewById(R.id.btn_positive_text)
                    val tvNegative: TextView = view.findViewById(R.id.btn_negative_text)
                    val btnPositive: LinearLayout = view.findViewById(R.id.btn_positive)
                    val btnNegative: LinearLayout = view.findViewById(R.id.btn_negative)

                    tvTitle.typeface = typefaceBold
                    tvStartDate.typeface = typefaceRegular
                    tvEndDate.typeface = typefaceRegular
                    tvWave.typeface = typefaceRegular
                    checkDeleteFinish.typeface = typefaceRegular
                    tvPositive.typeface = typefaceRegular
                    tvNegative.typeface = typefaceRegular

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

                    btnPositive.setOnClickListener {
                        if (startCalendar.timeInMillis > endCalendar.timeInMillis) {
                            Toast.makeText(requireContext(), getString(R.string.err_end_date), Toast.LENGTH_SHORT).show()
                        } else {
                            var count = 0
                            val datas = dbHelper.getItemDateRange(startCalendar.timeInMillis, endCalendar.timeInMillis)
                            for (data in datas) {
                                if (checkDeleteFinish.isChecked) {
                                    if (data.isFinished) {
                                        dbHelper.deleteData(data.id)
                                        count++
                                    }
                                } else {
                                    dbHelper.deleteData(data.id)
                                    count++
                                }
                            }

                            if (count != 0) {
                                Toast.makeText(requireContext(), getString(R.string.delete_successfully, count), Toast.LENGTH_SHORT).show()
                                requireActivity().finishAffinity()
                            } else {
                                Toast.makeText(requireContext(), getString(R.string.no_delete_event), Toast.LENGTH_SHORT).show()
                            }
                            dismiss()
                        }
                    }

                    btnNegative.setOnClickListener {
                        dismiss()
                    }

                    setContentView(view)
                }.show()

                true
            }
        }
    }
}