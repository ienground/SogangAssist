package net.ienlab.sogangassist

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.widget.TextView
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
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

        val app_title: Preference = findPreference("app_title")!!
        val changelog: Preference = findPreference("changelog")!!
        val ask_to_dev: Preference = findPreference("ask_to_dev")!!
        val open_source: Preference = findPreference("open_source")!!

        app_title.layoutResource = R.layout.settings_app_title

        changelog.setOnPreferenceClickListener {
            val changelog_dialog_builder = android.app.AlertDialog.Builder(context)

            val inflator = layoutInflater

            // 체인지로그 Dialog

            val changelog_dialog_view = inflator.inflate(R.layout.dialog_changelog, null)
            changelog_dialog_builder.setView(changelog_dialog_view)

            val changelog_content = changelog_dialog_view.findViewById<TextView>(R.id.changelog_content)

            changelog_dialog_builder.setPositiveButton(getString(R.string.ok)) { dialog, id ->
                dialog.cancel()
            }

            val version: String
            try {
                val i = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
                version = i.versionName
                changelog_dialog_builder.setTitle("${getString(R.string.real_app_name)} $version ${getString(R.string.changelog)}")
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

            changelog_dialog.show()
            true
        }

        ask_to_dev.setOnPreferenceClickListener {
            var email = Intent(Intent.ACTION_SEND)
            email.type = "plain/text"
            val address = arrayOf("admin@ienlab.net")
            email.putExtra(Intent.EXTRA_EMAIL, address)
            email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.real_app_name) + " " + BuildConfig.VERSION_NAME + " " + getString(R.string.ask))
            email.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_text) + "\n${Build.BRAND} ${Build.MODEL} Android ${Build.VERSION.RELEASE}" + "\n_\n")
            email.type = "message/rfc822"
            startActivity(email)
            true
        }

        open_source.setOnPreferenceClickListener {
//            startActivity(Intent(context, LicenseActivity::class.java))
            true
        }

    }
}
