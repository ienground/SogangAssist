package net.ienlab.sogangassist

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.material.snackbar.Snackbar
import com.rd.animation.type.AnimationType
import kotlinx.android.synthetic.main.activity_onboarding.*
import net.ienlab.sogangassist.OnboardingFragmentTabAdapter.Companion.PAGE_NUMBER
import kotlin.system.exitProcess

class OnboardingActivity : AppCompatActivity(),
    OnboardingFragment0.OnFragmentInteractionListener,
    OnboardingFragment1.OnFragmentInteractionListener,
    OnboardingFragment2.OnFragmentInteractionListener,
    OnboardingFragment3.OnFragmentInteractionListener {

    private val FINISH_INTERVAL_TIME: Long = 2000
    private var backPressedTime: Long = 0

    var page = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
        val lmsPackageName = "kr.co.imaxsoft.hellolms"

        // 데이터 초기화
        sharedPreferences.edit().clear().apply()
        deleteDatabase(dbName)

        // Fragment Tab 설정
        val adapter = OnboardingFragmentTabAdapter(supportFragmentManager, applicationContext)
        with (viewPager) {
            this.adapter = adapter
            currentItem = page
            setAllowedSwipeDirection(SwipeDirection.none)
            addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
                override fun onPageScrollStateChanged(state: Int) {}
                override fun onPageSelected(position: Int) {
                    page = position
                    pageIndicator.selection = position

                    when (position) {
                        0 -> {
                            with (intro_btn_next) {
                                isEnabled = true
                                alpha = 1f
                            }
                        }

                        1 -> {
                            with (intro_btn_next) {
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
                            with (intro_btn_next) {
                                if (true in OnboardingFragment2.hours) {
                                    isEnabled = true
                                    alpha = 1f
                                } else {
                                    isEnabled = false
                                    alpha = 0.2f
                                }
                            }
                        }

                        3 -> {
                            with (intro_btn_fine) {
                                if (true in OnboardingFragment3.hours) {
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
                        intro_btn_fine.visibility = View.VISIBLE
                        intro_btn_next.visibility = View.GONE
                    } else {
                        intro_btn_fine.visibility = View.GONE
                        intro_btn_next.visibility = View.VISIBLE
                    }

                    intro_btn_prev.visibility = if (position == 0) View.GONE
                    else View.VISIBLE
                }
            })
        }

        pageIndicator.selection = page
        pageIndicator.setAnimationType(AnimationType.WORM)

        intro_btn_prev.visibility = View.GONE
        intro_btn_fine.visibility = View.GONE

        intro_btn_prev.setOnClickListener {
            page -= 1
            viewPager.currentItem = page
            pageIndicator.selection = page
        }

        intro_btn_next.setOnClickListener {
            page += 1
            viewPager.currentItem = page
            pageIndicator.selection = page
        }

        intro_btn_fine.setOnClickListener {
            if (true in OnboardingFragment3.hours) {
                sharedPreferences.edit().putBoolean(SharedGroup.IS_FIRST_VISIT, false).apply()
                finish()
                startActivity(Intent(this, MainActivity::class.java))
            } else {

            }
        }

        intro_btn_next.visibility = if (page + 1 == PAGE_NUMBER) View.GONE else View.VISIBLE
        intro_btn_fine.visibility = if (page + 1 == PAGE_NUMBER) View.VISIBLE else View.GONE

        if (page == 0) {
            intro_btn_next.let {
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