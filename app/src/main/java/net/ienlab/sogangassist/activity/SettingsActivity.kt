package net.ienlab.sogangassist.activity

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import net.ienlab.sogangassist.BuildConfig
import net.ienlab.sogangassist.data.LMSClass
import net.ienlab.sogangassist.databinding.ActivitySettingsBinding
import net.ienlab.sogangassist.utils.MyUtils
import net.ienlab.sogangassist.database.*
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.constant.SharedGroup
import net.ienlab.sogangassist.utils.MyBottomSheetDialog
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class SettingsActivity : AppCompatActivity(), Preference.OnPreferenceClickListener {

    lateinit var binding: ActivitySettingsBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)
        binding.activity = this

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.appTitle.typeface = Typeface.createFromAsset(assets, "fonts/gmsans_bold.otf")

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment(), null).commit()

    }

    // ActionBar 메뉴 각각 클릭 시

    override fun onPreferenceClick(preference: Preference?): Boolean {
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                setResult(Activity.RESULT_OK)
                super.onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        super.onBackPressed()
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        lateinit var dbHelper: DBHelper
        lateinit var sharedPreferences: SharedPreferences

        val SAVE_FILE = 4
        val LOAD_FILE = 5

        override fun onCreatePreferences(bundle: Bundle?, str: String?) {
            addPreferencesFromResource(R.xml.pref)
            val appInfo = findPreference("app_title")
            val notifyHw = findPreference("notify_hw_group")
            val notifyLec = findPreference("notify_lec_group")
            val notifyZoom = findPreference("notify_zoom_group")
            val changelog = findPreference("changelog")
            val email = findPreference("ask_to_dev")
            val openSource = findPreference("open_source")
            val backup = findPreference("backup")
            val restore = findPreference("restore")

            val gmSansMedium = Typeface.createFromAsset(requireActivity().assets, "fonts/gmsans_medium.otf")
            val gmSansBold = Typeface.createFromAsset(requireActivity().assets, "fonts/gmsans_bold.otf")

            dbHelper = DBHelper(requireContext(), dbName, dbVersion)
            sharedPreferences = requireContext().getSharedPreferences("${requireContext().packageName}_preferences", Context.MODE_PRIVATE)

            val hourData = listOf("1", "2", "6", "12", "24")
            val minuteData = listOf("3", "5", "10", "20", "30")

            val hwSharedKeys = listOf(SharedGroup.NOTIFY_1HOUR_HW, SharedGroup.NOTIFY_2HOUR_HW, SharedGroup.NOTIFY_6HOUR_HW, SharedGroup.NOTIFY_12HOUR_HW, SharedGroup.NOTIFY_24HOUR_HW)
            val lecSharedKeys = listOf(SharedGroup.NOTIFY_1HOUR_LEC, SharedGroup.NOTIFY_2HOUR_LEC, SharedGroup.NOTIFY_6HOUR_LEC, SharedGroup.NOTIFY_12HOUR_LEC, SharedGroup.NOTIFY_24HOUR_LEC)
            val zoomSharedKeys = listOf(SharedGroup.NOTIFY_3MIN_ZOOM, SharedGroup.NOTIFY_5MIN_ZOOM, SharedGroup.NOTIFY_10MIN_ZOOM, SharedGroup.NOTIFY_20MIN_ZOOM, SharedGroup.NOTIFY_30MIN_ZOOM)

            val hwHoursOn = mutableListOf<String>()
            val lecHoursOn = mutableListOf<String>()
            val zoomMinutesOn = mutableListOf<String>()

            hwSharedKeys.forEachIndexed { index, s -> if (sharedPreferences.getBoolean(s, true)) hwHoursOn.add(hourData[index]) }
            lecSharedKeys.forEachIndexed { index, s -> if (sharedPreferences.getBoolean(s, true)) lecHoursOn.add(hourData[index]) }
            zoomSharedKeys.forEachIndexed { index, s -> if (sharedPreferences.getBoolean(s, true)) zoomMinutesOn.add(minuteData[index]) }

            notifyHw?.summary = if (hwHoursOn.isNotEmpty()) getString(R.string.notify_hw_on, hwHoursOn.joinToString(", ")) else getString(R.string.notify_all_off)
            notifyLec?.summary = if (lecHoursOn.isNotEmpty()) getString(R.string.notify_lec_on, lecHoursOn.joinToString(", ")) else getString(R.string.notify_all_off)
            notifyZoom?.summary = if (zoomMinutesOn.isNotEmpty()) getString(R.string.notify_zoom_on, zoomMinutesOn.joinToString(", ")) else getString(R.string.notify_all_off)

            appInfo?.setOnPreferenceClickListener {
                MyBottomSheetDialog(requireContext()).apply {
                    val view = layoutInflater.inflate(R.layout.dialog_changelog, LinearLayout(requireContext()), false)
                    val tvVersion: TextView = view.findViewById(R.id.tv_version)
                    val tvContent: TextView = view.findViewById(R.id.content)

                    tvVersion.typeface = gmSansBold
                    tvContent.typeface = gmSansMedium

                    tvVersion.text = getString(R.string.real_app_name)
                    tvContent.text = getString(R.string.dev_ienlab)

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
                    val hours = mutableListOf<Boolean>()

                    tvTitle.typeface = gmSansBold
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
                        val hoursOn = mutableListOf<String>()
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
                    val hours = mutableListOf<Boolean>()

                    tvTitle.typeface = gmSansBold
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
                        val hoursOn = mutableListOf<String>()
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
                    val minutes = mutableListOf<Boolean>()

                    tvTitle.typeface = gmSansBold
                    tvTitle.text = getString(R.string.notify_zoom)
                    imgLogo.setImageResource(R.drawable.ic_groups)

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
                        val minutesOn = mutableListOf<String>()
                        minutes.forEachIndexed { index, b ->  if (b) minutesOn.add(minuteData[index]) }
                        notifyZoom.summary = if (minutesOn.isNotEmpty()) getString(R.string.notify_zoom_on, minutesOn.joinToString(", ")) else getString(R.string.notify_all_off)
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

                    tvVersion.typeface = gmSansBold
                    tvContent.typeface = gmSansMedium

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
            backup?.setOnPreferenceClickListener {
                val saveFormat = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())

                Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TITLE, "albatross_backup_${saveFormat.format(Calendar.getInstance().time)}.txt")
                    startActivityForResult(this, SAVE_FILE)
                }
                true
            }
            restore?.setOnPreferenceClickListener {
                Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "text/plain"
                    startActivityForResult(this, LOAD_FILE)
                }
                true
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            when (requestCode) {
                SAVE_FILE -> {
                    if (resultCode == RESULT_OK) {
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

                        val uri = data?.data ?: Uri.EMPTY
                        val outputStream = requireActivity().contentResolver.openOutputStream(uri)
                        outputStream?.write(output.toString().toByteArray())
                        outputStream?.close()

                        Toast.makeText(requireContext(), getString(R.string.backup_msg), Toast.LENGTH_SHORT).show()
                    }
                }

                LOAD_FILE -> {
                    if (resultCode == RESULT_OK) {
                        val uri = data?.data ?: Uri.EMPTY
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
                        val result = JSONArray(builder.toString())

                        AlertDialog.Builder(requireContext()).apply {
                            setTitle(R.string.restore)
                            setMessage(R.string.restore_msg)
                            setPositiveButton(R.string.agree) { _, _ ->
                                requireActivity().deleteDatabase(dbName)
                                for (i in 0 until result.length()) {
                                    val jObject = result.getJSONObject(i)
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
                            }
                            setNegativeButton(R.string.disagree) { dialog, _ ->
                                dialog.cancel()
                            }
                        }.show()
                    }
                }
            }
        }
    }
}