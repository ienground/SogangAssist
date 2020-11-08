package net.ienlab.sogangassist

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.snackbar.Snackbar
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.Reader
import java.nio.charset.Charset
import java.util.*


class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(bundle: Bundle?, s: String?) {
        addPreferencesFromResource(R.xml.pref)
        val context = context

        val app_title = findPreference("app_title")
        val changelog = findPreference("changelog")
        val ask_to_dev = findPreference("ask_to_dev")
        val open_source = findPreference("open_source")

        app_title?.layoutResource = R.layout.settings_app_title

        changelog?.setOnPreferenceClickListener {
            AlertDialog.Builder(requireContext()).apply {
                val changelogDialogView = layoutInflater.inflate(R.layout.dialog_changelog, LinearLayout(context), false)
                val content: TextView = changelogDialogView.findViewById(R.id.content)

                setTitle("${getString(R.string.real_app_name)} ${BuildConfig.VERSION_NAME}")
                setPositiveButton(R.string.ok) { dialog, _ ->
                    dialog.cancel()
                }
                content.text = MyUtils.fromHtml(MyUtils.readTextFromRaw(resources, R.raw.changelog))
                setView(changelogDialogView)

            }.show()
            true
        }

        ask_to_dev?.setOnPreferenceClickListener {
            Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_EMAIL, arrayOf("admin@ienlab.net"))
                putExtra(Intent.EXTRA_SUBJECT, "${getString(R.string.real_app_name)} ${BuildConfig.VERSION_NAME} ${getString(R.string.ask)}")
                putExtra(Intent.EXTRA_TEXT, "${getString(R.string.email_text)}\n${Build.BRAND} ${Build.MODEL} Android ${Build.VERSION.RELEASE}\n_\n")
                type = "message/rfc822"
                startActivity(this)
            }
            true
        }

        open_source?.setOnPreferenceClickListener {
            startActivity(Intent(context, OssLicensesMenuActivity::class.java))
            true
        }

    }
}
