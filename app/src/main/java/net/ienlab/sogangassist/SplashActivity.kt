package net.ienlab.sogangassist

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginHandler
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URL

class SplashActivity : AppCompatActivity() {

    // 로딩 화면이 떠있는 시간(밀리초단위)
    private val SPLASH_DISPLAY_LENGTH = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        textLogo.typeface = Typeface.createFromAsset(assets, "fonts/gmsans_bold.otf")

        val sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)

        val isFirstVisit = sharedPreferences.getBoolean(SharedGroup.IS_FIRST_VISIT, true)

        Handler(Looper.getMainLooper()).postDelayed({
            val mainIntent = Intent(this, MainActivity::class.java)
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
