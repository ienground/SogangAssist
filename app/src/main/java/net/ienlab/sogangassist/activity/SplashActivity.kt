package net.ienlab.sogangassist.activity

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import net.ienlab.sogangassist.constant.SharedGroup
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    lateinit var binding: ActivitySplashBinding
    // 로딩 화면이 떠있는 시간(밀리초단위)
    private val SPLASH_DISPLAY_LENGTH = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        binding.activity = this

        val sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)

        val isFirstVisit = sharedPreferences.getBoolean(SharedGroup.IS_FIRST_VISIT, true)
        val id = intent.getIntExtra("ID", -1)
        val notiId = intent.getIntExtra("NOTI_ID", -1)

        Log.d(TAG, "id: $id")

        val gmSansBold = Typeface.createFromAsset(assets, "fonts/gmsans_bold.otf")
        binding.appTitle?.typeface = gmSansBold
        binding.appTitle?.text = getString(R.string.real_app_name).split(" ").joinToString("\n") + "."

        Handler(Looper.getMainLooper()).postDelayed({
            val mainIntent = Intent(this, MainActivity::class.java).apply {
                putExtra("ID", id)
                putExtra("NOTI_ID", notiId)
            }
            val welcomeIntent = Intent(this, OnboardingActivity::class.java)
            if (isFirstVisit) {
                startActivity(welcomeIntent)
            } else {
                startActivity(mainIntent)
            }
            finish()
        }, SPLASH_DISPLAY_LENGTH.toLong())
    }
}
