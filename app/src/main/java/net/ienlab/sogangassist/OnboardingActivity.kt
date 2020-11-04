package net.ienlab.sogangassist

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import com.rd.animation.type.AnimationType
import net.ienlab.sogangassist.OnboardingFragmentTabAdapter.Companion.PAGE_NUMBER
import net.ienlab.sogangassist.databinding.ActivityOnboardingBinding
import kotlin.system.exitProcess

class OnboardingActivity : AppCompatActivity(),
    OnboardingFragment0.OnFragmentInteractionListener,
    OnboardingFragment1.OnFragmentInteractionListener,
    OnboardingFragment2.OnFragmentInteractionListener,
    OnboardingFragment3.OnFragmentInteractionListener,
    OnboardingFragment4.OnFragmentInteractionListener {

    private val FINISH_INTERVAL_TIME: Long = 2000
    private var backPressedTime: Long = 0

    var page = 0
    lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_onboarding)
        binding.activity = this

        val sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
        val lmsPackageName = "kr.co.imaxsoft.hellolms"

        // 데이터 초기화
        sharedPreferences.edit().clear().apply()
        deleteDatabase(dbName)

        // Fragment Tab 설정
        val adapter = OnboardingFragmentTabAdapter(supportFragmentManager, applicationContext)
        with (binding.viewPager) {
            this.adapter = adapter
            currentItem = page
            setAllowedSwipeDirection(SwipeDirection.none)
            addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
                override fun onPageScrollStateChanged(state: Int) {}
                override fun onPageSelected(position: Int) {
                    page = position
                    binding.pageIndicator.selection = position

                    when (position) {
                        0 -> {
                            with (binding.introBtnNext) {
                                isEnabled = true
                                alpha = 1f
                            }
                        }

                        1 -> {
                            with (binding.introBtnNext) {
                                if (isPackageInstalled(lmsPackageName, packageManager) || BuildConfig.DEBUG) {
                                    isEnabled = true
                                    alpha = 1f
                                } else {
                                    isEnabled = false
                                    alpha = 0.2f
                                }
                            }
                        }

                        2 -> {
                            with (binding.introBtnNext) {
                                if (isNotiPermissionAllowed()) {
                                    isEnabled = true
                                    alpha = 1f
                                } else {
                                    isEnabled = false
                                    alpha = 0.2f
                                }
                            }
                        }

                        3 -> {
                            with (binding.introBtnNext) {
                                if (true in OnboardingFragment3.hours) {
                                    isEnabled = true
                                    alpha = 1f
                                } else {
                                    isEnabled = false
                                    alpha = 0.2f
                                }
                            }
                        }

                        4 -> {
                            with (binding.introBtnFine) {
                                if (true in OnboardingFragment4.hours) {
                                    isEnabled = true
                                    alpha = 1f
                                } else {
                                    isEnabled = false
                                    alpha = 0.2f
                                }
                            }
                        }
                    }

                    if (position == PAGE_NUMBER - 1) {
                        binding.introBtnFine.visibility = View.VISIBLE
                        binding.introBtnNext.visibility = View.GONE
                    } else {
                        binding.introBtnFine.visibility = View.GONE
                        binding.introBtnNext.visibility = View.VISIBLE
                    }

                    binding.introBtnPrev.visibility = if (position == 0) View.GONE
                    else View.VISIBLE
                }
            })
        }

        binding.pageIndicator.selection = page
        binding.pageIndicator.setAnimationType(AnimationType.WORM)

        binding.introBtnPrev.visibility = View.GONE
        binding.introBtnFine.visibility = View.GONE

        binding.introBtnPrev.setOnClickListener {
            page -= 1
            binding.viewPager.currentItem = page
            binding.pageIndicator.selection = page
        }

        binding.introBtnNext.setOnClickListener {
            page += 1
            binding.viewPager.currentItem = page
            binding.pageIndicator.selection = page
        }

        binding.introBtnFine.setOnClickListener {
            if (true in OnboardingFragment4.hours) {
                sharedPreferences.edit().putBoolean(SharedGroup.IS_FIRST_VISIT, false).apply()
                finish()
                startActivity(Intent(this, MainActivity::class.java))
            }
        }

        binding.introBtnNext.visibility = if (page + 1 == PAGE_NUMBER) View.GONE else View.VISIBLE
        binding.introBtnFine.visibility = if (page + 1 == PAGE_NUMBER) View.VISIBLE else View.GONE

        if (page == 0) {
            binding.introBtnNext.let {
                it.isEnabled = true
                it.alpha = 0.5f
            }
        }

        // Theme
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.navigationBarColor = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
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