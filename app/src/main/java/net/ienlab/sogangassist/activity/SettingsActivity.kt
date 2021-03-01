package net.ienlab.sogangassist

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.snackbar.Snackbar
import net.ienlab.sogangassist.databinding.ActivitySettingsBinding
import net.ienlab.sogangassist.utils.MyUtils
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
        val SAVE_FILE = 4
        val LOAD_FILE = 5

        override fun onCreatePreferences(bundle: Bundle?, s: String?) {
            addPreferencesFromResource(R.xml.pref)
            val changelog = findPreference("changelog")
            val email = findPreference("ask_to_dev")
            val openSource = findPreference("open_source")
            val backup = findPreference("backup")
            val restore = findPreference("restore")

            dbHelper = DBHelper(requireContext(), dbName, dbVersion)

            changelog?.setOnPreferenceClickListener {
                AlertDialog.Builder(requireContext()).apply {
                    val changelogDialogView = layoutInflater.inflate(
                        R.layout.dialog_changelog, LinearLayout(requireContext()), false
                    )
                    val content: TextView = changelogDialogView.findViewById(R.id.content)

                    setTitle("${getString(R.string.real_app_name)} ${BuildConfig.VERSION_NAME}")
                    setPositiveButton(R.string.ok) { dialog, _ ->
                        dialog.cancel()
                    }
                    content.text = MyUtils.fromHtml(
                        MyUtils.readTextFromRaw(
                            resources,
                            R.raw.changelog
                        )
                    )
                    setView(changelogDialogView)

                }.show()
                true
            }

            email?.setOnPreferenceClickListener {
                Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("admin@ienlab.net"))
                    putExtra(
                        Intent.EXTRA_SUBJECT,
                        "${getString(R.string.real_app_name)} ${BuildConfig.VERSION_NAME} ${
                            getString(
                                R.string.ask
                            )
                        }"
                    )
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "${getString(R.string.email_text)}\n${Build.BRAND} ${Build.MODEL} Android ${Build.VERSION.RELEASE}\n_\n"
                    )
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
                    putExtra(
                        Intent.EXTRA_TITLE,
                        "albatross_backup_${saveFormat.format(Calendar.getInstance().time)}.txt"
                    )
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
                                    LMSClass().apply {
                                        className = jObject.getString("className")
                                        timeStamp = jObject.getLong("timeStamp")
                                        type = jObject.getInt("type")
                                        startTime = jObject.getLong("startTime")
                                        endTime = jObject.getLong("endTime")
                                        isRenewAllowed = jObject.getBoolean("isRenewAllowed")
                                        isFinished = jObject.getBoolean("isFinished")
                                        week = jObject.getInt("week")
                                        lesson = jObject.getInt("lesson")
                                        homework_name = jObject.getString("homework_name")

                                        dbHelper.addItem(this)
                                    }
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