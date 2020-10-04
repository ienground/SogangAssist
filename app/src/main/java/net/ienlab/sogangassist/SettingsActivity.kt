package net.ienlab.sogangassist

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity(), Preference.OnPreferenceClickListener {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

//        titles.typeface = Typeface.createFromAsset(assets, "fonts/gmsans_bold.otf")

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment(), null).commit()

        btn_back.setOnClickListener {
            setResult(Activity.RESULT_OK)
            super.onBackPressed()
        }
    }

    // ActionBar 메뉴 각각 클릭 시
    override fun onOptionsItemSelected(menu: MenuItem): Boolean {
        when (menu.itemId) {
            android.R.id.home -> finish()
        }
        return true
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        return true
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        super.onBackPressed()
    }
}


