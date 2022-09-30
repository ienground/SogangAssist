package net.ienlab.sogangassist.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import net.ienlab.sogangassist.constant.SharedKey
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.constant.IntentKey
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

        val isFirstVisit = sharedPreferences.getBoolean(SharedKey.IS_FIRST_VISIT, true)
        val id = intent.getIntExtra("ID", -1)
        val notiId = intent.getIntExtra("NOTI_ID", -1)

        val typefaceBold = ResourcesCompat.getFont(this, R.font.pretendard_black) ?: Typeface.DEFAULT
        binding.appTitle?.typeface = typefaceBold
        binding.appTitle?.text = getString(R.string.real_app_name).split(" ").joinToString("\n") + "."

        val widgetPreferences = getSharedPreferences("WidgetPreferences", Context.MODE_PRIVATE)
        widgetPreferences.edit().clear().apply()

        Handler(Looper.getMainLooper()).postDelayed({
            val mainIntent = Intent(this, MainActivity::class.java).apply {
                putExtra(IntentKey.ID, id)
                putExtra(IntentKey.NOTI_ID, notiId)
            }
            val welcomeIntent = Intent(this, OnboardingActivity::class.java)
            if (isFirstVisit) {
                startActivity(welcomeIntent)
            } else {
                val handler = MainActivityOpenHandler(this, mainIntent)
                Thread {
                    handler.sendEmptyMessage(0)
                }.start()
            }
            finish()
        }, SPLASH_DISPLAY_LENGTH.toLong())
    }


}

class MainActivityOpenHandler(val activity: Activity, val intent: Intent): Handler(Looper.getMainLooper()) {
    override fun handleMessage(msg: Message) {
        super.handleMessage(msg)
        activity.startActivity(intent)
        Log.d(TAG, "open activity in handler")
    }
}
