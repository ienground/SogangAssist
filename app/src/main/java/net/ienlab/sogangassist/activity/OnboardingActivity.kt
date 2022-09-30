package net.ienlab.sogangassist.activity

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.adapter.OnboardingFragmentTabAdapter
import net.ienlab.sogangassist.adapter.OnboardingFragmentTabAdapter.Companion.PAGE_NUMBER
import net.ienlab.sogangassist.constant.SharedKey
import net.ienlab.sogangassist.databinding.ActivityOnboardingBinding
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

    lateinit var binding: ActivityOnboardingBinding

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_onboarding)
        binding.activity = this

        sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)

        // 데이터 초기화
        sharedPreferences.edit().clear().apply()
        deleteDatabase("SogangLMSAssistData.db")

        sharedPreferences.edit().putBoolean(SharedKey.NOTIFY_1HOUR_HW, true).apply()
        sharedPreferences.edit().putBoolean(SharedKey.NOTIFY_2HOUR_HW, true).apply()
        sharedPreferences.edit().putBoolean(SharedKey.NOTIFY_6HOUR_HW, true).apply()
        sharedPreferences.edit().putBoolean(SharedKey.NOTIFY_12HOUR_HW, true).apply()
        sharedPreferences.edit().putBoolean(SharedKey.NOTIFY_24HOUR_HW, true).apply()
        sharedPreferences.edit().putBoolean(SharedKey.NOTIFY_1HOUR_LEC, true).apply()
        sharedPreferences.edit().putBoolean(SharedKey.NOTIFY_2HOUR_LEC, true).apply()
        sharedPreferences.edit().putBoolean(SharedKey.NOTIFY_6HOUR_LEC, true).apply()
        sharedPreferences.edit().putBoolean(SharedKey.NOTIFY_12HOUR_LEC, true).apply()
        sharedPreferences.edit().putBoolean(SharedKey.NOTIFY_24HOUR_LEC, true).apply()
        sharedPreferences.edit().putBoolean(SharedKey.NOTIFY_3MIN_ZOOM, true).apply()
        sharedPreferences.edit().putBoolean(SharedKey.NOTIFY_5MIN_ZOOM, true).apply()
        sharedPreferences.edit().putBoolean(SharedKey.NOTIFY_10MIN_ZOOM, true).apply()
        sharedPreferences.edit().putBoolean(SharedKey.NOTIFY_20MIN_ZOOM, true).apply()
        sharedPreferences.edit().putBoolean(SharedKey.NOTIFY_30MIN_ZOOM, true).apply()
        sharedPreferences.edit().putBoolean(SharedKey.NOTIFY_3MIN_EXAM, true).apply()
        sharedPreferences.edit().putBoolean(SharedKey.NOTIFY_5MIN_EXAM, true).apply()
        sharedPreferences.edit().putBoolean(SharedKey.NOTIFY_10MIN_EXAM, true).apply()
        sharedPreferences.edit().putBoolean(SharedKey.NOTIFY_20MIN_EXAM, true).apply()
        sharedPreferences.edit().putBoolean(SharedKey.NOTIFY_30MIN_EXAM, true).apply()

        // adapter
        val adapter = OnboardingFragmentTabAdapter(supportFragmentManager, applicationContext)
        var prePosition = -1

        binding.viewPager.adapter = adapter
        binding.viewPager.setAllowedSwipeDirection(SwipeDirection.all)
        binding.viewPager.pageMargin = MyUtils.dpToPx(applicationContext, 16f).toInt()
        binding.viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageSelected(position: Int) {
                if (position == PAGE_NUMBER - 1) {
                    binding.btnFine.visibility = View.VISIBLE
                    ValueAnimator.ofFloat(0f, 1f).apply {
                        duration = 300
                        addUpdateListener {
                            binding.btnFine.alpha = (it.animatedValue as Float)
                        }
                    }.start()
                } else if (prePosition == PAGE_NUMBER - 1) {
                    ValueAnimator.ofFloat(1f, 0f).apply {
                        duration = 300
                        addUpdateListener {
                            binding.btnFine.alpha = (it.animatedValue as Float)
                            if (it.animatedValue as Float == 0f) {
                                binding.btnFine.visibility = View.GONE
                            }
                        }
                    }.start()
                } else {
                    binding.btnFine.visibility = View.GONE
                }

                prePosition = position
            }
        })

        binding.btnFine.setOnClickListener {
            sharedPreferences.edit().putBoolean(SharedKey.IS_FIRST_VISIT, false).apply()
            finish()
            startActivity(Intent(this, MainActivity::class.java))
        }

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