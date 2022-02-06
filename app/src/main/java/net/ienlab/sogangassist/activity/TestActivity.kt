package net.ienlab.sogangassist.activity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.animation.AlphaAnimation
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*
import net.ienlab.sogangassist.BuildConfig
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.constant.SharedKey
import net.ienlab.sogangassist.data.LMSClass
import net.ienlab.sogangassist.databinding.ActivityMain2Binding
import net.ienlab.sogangassist.decorators.CurrentDecorator
import net.ienlab.sogangassist.receiver.TimeReceiver
import net.ienlab.sogangassist.room.LMSDatabase
import net.ienlab.sogangassist.room.LMSEntity
import net.ienlab.sogangassist.utils.AppStorage
import net.ienlab.sogangassist.utils.MyUtils.Companion.timeZero
import net.ienlab.sogangassist.utils.MyUtils.Companion.tomorrowZero
import java.util.*

class TestActivity : AppCompatActivity() {

    lateinit var binding: ActivityMain2Binding

    private var lmsDatabase: LMSDatabase? = null
    lateinit var sharedPreferences: SharedPreferences
    lateinit var am: AlarmManager
    lateinit var fadeOutAnimation: AlphaAnimation
    lateinit var fadeInAnimation: AlphaAnimation

    // StartActivityForResult
    lateinit var editActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var settingsActivityLauncher: ActivityResultLauncher<Intent>

    // 뒤로가기 시간
    private val FINISH_INTERVAL_TIME: Long = 2000
    private var backPressedTime: Long = 0

    lateinit var currentDecorator: CurrentDecorator
    private var thisCurrentDate: Long = 0

    lateinit var typefaceBold: Typeface
    lateinit var typefaceRegular: Typeface

    lateinit var storage: AppStorage

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main2)
        binding.activity = this

        lmsDatabase = LMSDatabase.getInstance(this)
        sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
        fadeOutAnimation = AlphaAnimation(1f, 0f).apply { duration = 300 }
        fadeInAnimation = AlphaAnimation(0f, 1f).apply { duration = 300 }
        am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        currentDecorator = CurrentDecorator(this, Calendar.getInstance())
        typefaceBold = ResourcesCompat.getFont(this, R.font.pretendard_black) ?: Typeface.DEFAULT
        typefaceRegular = ResourcesCompat.getFont(this, R.font.pretendard_regular) ?: Typeface.DEFAULT
        storage = AppStorage(this)

        FirebaseInAppMessaging.getInstance().isAutomaticDataCollectionEnabled = true
        if (BuildConfig.DEBUG) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }
                val token = task.result
                Log.d(TAG, token)
            })

            // Firebase In App Messaging
            FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Installation ID: " + task.result)
                } else {
                    Log.e(TAG, "Unable to get Installation ID")
                }
            }
        }

        sharedPreferences.edit().putBoolean(SharedKey.CURRENT_CALENDAR_ICON_SHOW, sharedPreferences.getBoolean(SharedKey.CALENDAR_ICON_SHOW, true)).apply()

        GlobalScope.launch(Dispatchers.IO) {
            val datas = lmsDatabase?.getDao()?.getAll()
            val todayWorks = lmsDatabase?.getDao()?.getByEndTime(Calendar.getInstance().timeZero().timeInMillis, Calendar.getInstance().tomorrowZero().timeInMillis)?.toTypedArray()?.apply { sortWith( compareBy ({ it.isFinished }, {it.endTime}, {it.type} )) }



            withContext(Dispatchers.Main) {
                if (datas != null) {
                    for (data in datas) {
                        val notiIntent = Intent(applicationContext, TimeReceiver::class.java).apply { putExtra("ID", data.id) }
                        val hours = listOf(1, 2, 6, 12, 24)
                        val minutes = listOf(3, 5, 10, 20, 30)

                        if (data.endTime < System.currentTimeMillis()) continue

                        when (data.type) {
                            LMSClass.TYPE_HOMEWORK, LMSClass.TYPE_LESSON, LMSClass.TYPE_SUP_LESSON, LMSClass.TYPE_TEAMWORK -> {
                                hours.forEachIndexed { index, i ->
                                    val triggerTime = data.endTime - i * 60 * 60 * 1000
                                    notiIntent.putExtra("TRIGGER", triggerTime)
                                    notiIntent.putExtra("TIME", i)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, (data.id ?: 0) * 100 + index + 1, notiIntent, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }
                            }
                            LMSClass.TYPE_ZOOM, LMSClass.TYPE_EXAM -> {
                                minutes.forEachIndexed { index, i ->
                                    val triggerTime = data.endTime - i * 60 * 1000
                                    notiIntent.putExtra("TRIGGER", triggerTime)
                                    notiIntent.putExtra("MINUTE", i)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, (data.id ?: 0) * 100 + index + 1, notiIntent, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}