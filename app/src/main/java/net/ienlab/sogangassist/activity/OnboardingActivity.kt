package net.ienlab.sogangassist.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import com.rd.animation.type.AnimationType
import net.ienlab.sogangassist.adapter.OnboardingFragmentTabAdapter
import net.ienlab.sogangassist.adapter.OnboardingFragmentTabAdapter.Companion.PAGE_NUMBER
import net.ienlab.sogangassist.constant.SharedKey
import net.ienlab.sogangassist.databinding.ActivityOnboardingBinding
import net.ienlab.sogangassist.database.*
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.fragment.*
import net.ienlab.sogangassist.utils.MyUtils
import net.ienlab.sogangassist.utils.SwipeDirection
import kotlin.system.exitProcess

class OnboardingActivity : AppCompatActivity(),
    OnboardingFragment0.OnFragmentInteractionListener,
    OnboardingFragment1.OnFragmentInteractionListener,
    OnboardingFragment2.OnFragmentInteractionListener,
    OnboardingFragment3.OnFragmentInteractionListener,
    OnboardingFragment4.OnFragmentInteractionListener,
    OnboardingFragment5.OnFragmentInteractionListener,
    OnboardingFragment6.OnFragmentInteractionListener {

    private val FINISH_INTERVAL_TIME: Long = 2000
    private var backPressedTime: Long = 0

    var page = 0
    var lastPage = 0

    lateinit var binding: ActivityOnboardingBinding

    lateinit var permissionActivityLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_onboarding)
        binding.activity = this

        permissionActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, getString(R.string.permission_allow_msg), Toast.LENGTH_SHORT).show()
                    finish()
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, getString(R.string.permission_allow_msg), Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        val sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
        val lmsPackageName = "kr.co.imaxsoft.hellolms"

        // 데이터 초기화
//        sharedPreferences.edit().clear().apply()
//        deleteDatabase(DBHelper.dbName)

        // Fragment Tab 설정
        val adapter = OnboardingFragmentTabAdapter(supportFragmentManager, applicationContext)
        var mLastOffset = 0f

        // Theme
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.navigationBarColor = ContextCompat.getColor(applicationContext, R.color.colorAccent)
    }

    fun isPackageInstalled(packageName: String, pm: PackageManager): Boolean {
        return try {
            pm.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun isNotiPermissionAllowed(): Boolean {
        val notiListenerSet = NotificationManagerCompat.getEnabledListenerPackages(this)
        val myPackageName = packageName
        for (packageName in notiListenerSet) {
            if (packageName == null) {
                continue
            }
            if (packageName == myPackageName) {
                return true
            }
        }
        return false
    }

    override fun onFragmentInteraction(uri: Uri) {}

    override fun onBackPressed() {
        val tempTime = System.currentTimeMillis()
        val intervalTime = tempTime - backPressedTime
        if (intervalTime in 0..FINISH_INTERVAL_TIME) {
            finishAffinity()
            System.runFinalization()
            exitProcess(0)
        } else {
            backPressedTime = tempTime
            Toast.makeText(applicationContext, R.string.press_back_to_exit, Toast.LENGTH_SHORT).show()
        }
    }
}