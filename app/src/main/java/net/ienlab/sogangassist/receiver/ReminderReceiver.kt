package net.ienlab.sogangassist.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.activity.SplashActivity
import net.ienlab.sogangassist.activity.TAG
import net.ienlab.sogangassist.constant.ChannelId
import net.ienlab.sogangassist.constant.DefaultValue
import net.ienlab.sogangassist.constant.SharedGroup
import net.ienlab.sogangassist.data.LMSClass
import net.ienlab.sogangassist.database.*
import java.util.*
import kotlin.collections.ArrayList

class ReminderReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive")
        val sharedPreferences = context.getSharedPreferences("${context.packageName}_preferences", Context.MODE_PRIVATE)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val dbHelper = DBHelper(context, dbName, dbVersion)

        val type = intent.getIntExtra(TYPE, -1)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(NotificationChannel(ChannelId.DAILY_REMINDER_ID, context.getString(R.string.channel_daily_reminder), NotificationManager.IMPORTANCE_HIGH))
        }

        val data = dbHelper.getItemAtLastDate(System.currentTimeMillis() + AlarmManager.INTERVAL_DAY) // 오늘까지의 데이터
//        val data = dbHelper.getItemAtLastDate(System.currentTimeMillis()) // 오늘까지의 데이터
        val classes: ArrayList<String> = arrayListOf()
        val supClasses: ArrayList<String> = arrayListOf()
        val homeworks: ArrayList<String> = arrayListOf()
        val zooms: ArrayList<String> = arrayListOf()

        for (d in data) {
            if (!d.isFinished) {
                when (d.type) {
                    LMSClass.TYPE_LESSON -> classes.add(context.getString(R.string.week_lesson_format, d.week, d.lesson))
                    LMSClass.TYPE_SUP_LESSON -> supClasses.add(context.getString(R.string.week_lesson_format, d.week, d.lesson))
                    LMSClass.TYPE_HOMEWORK -> homeworks.add(d.homework_name)
                    LMSClass.TYPE_ZOOM ->zooms.add(d.homework_name)
                }
            }
        }

        Log.d(TAG, classes.toString())
        Log.d(TAG, supClasses.toString())
        Log.d(TAG, homeworks.toString())
        Log.d(TAG, zooms.toString())

//        "오늘까지 해야 할 %d개의 수업, %d개의 보강, %d개의 과제, %d개의 실시간 강의가 있습니다."

        val content: ArrayList<String> = arrayListOf()
        val bigTextContent: ArrayList<String> = arrayListOf()
        if (classes.isNotEmpty()) {
            content.add(context.getString(R.string.daily_reminder_class, classes.size))
            bigTextContent.add("<${context.getString(R.string.classtime)}>\n${classes.joinToString("\n")}")
        }
        if (supClasses.isNotEmpty()) {
            content.add(context.getString(R.string.daily_reminder_sup, supClasses.size))
            bigTextContent.add("<${context.getString(R.string.sup_classtime)}>\n${supClasses.joinToString("\n")}")
        }
        if (homeworks.isNotEmpty()) {
            content.add(context.getString(R.string.daily_reminder_hw, homeworks.size))
            bigTextContent.add("<${context.getString(R.string.assignment)}>\n${homeworks.joinToString("\n")}")
        }
        if (zooms.isNotEmpty()) {
            content.add(context.getString(R.string.daily_reminder_zoom, zooms.size))
            bigTextContent.add("<${context.getString(R.string.zoom)}>\n${zooms.joinToString("\n")}")
        }

        val morningData = sharedPreferences.getInt(SharedGroup.TIME_MORNING_REMINDER, DefaultValue.TIME_MORNING_REMINDER)
        val nightData = sharedPreferences.getInt(SharedGroup.TIME_NIGHT_REMINDER, DefaultValue.TIME_NIGHT_REMINDER)
        val now = Calendar.getInstance()
        val nowInt = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        when (type) {
            MORNING -> {
                if (sharedPreferences.getBoolean(SharedGroup.ALLOW_MORNING_REMINDER, true) && nowInt >= morningData - 2 && nowInt <= morningData + 2) {
                    NotificationCompat.Builder(context, ChannelId.DAILY_REMINDER_ID).apply {
                        setContentTitle(context.getString(R.string.daily_reminder_title_morning))
                        setContentText(content.joinToString(", "))
                        setStyle(NotificationCompat.BigTextStyle()
                            .bigText(context.getString(R.string.daily_reminder_content_format, content.joinToString(", "), bigTextContent.joinToString("\n\n")))
                        )

                        setContentIntent(PendingIntent.getActivity(context, 1, Intent(context, SplashActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
                        setSmallIcon(R.drawable.ic_reminder_icon)
                        setAutoCancel(true)
                        color = ContextCompat.getColor(context, R.color.colorAccent)

                        if (content.isNotEmpty()) {
                            nm.notify(680000, build())
                        }
                    }
                }
            }
            NIGHT -> {
                if (sharedPreferences.getBoolean(SharedGroup.ALLOW_NIGHT_REMINDER, true) && nowInt >= nightData - 2 && nowInt <= nightData + 2) {
                    NotificationCompat.Builder(context, ChannelId.DAILY_REMINDER_ID).apply {
                        setContentTitle(context.getString(R.string.daily_reminder_title_night))
                        setContentText(content.joinToString(", "))
                        setStyle(NotificationCompat.BigTextStyle()
                            .bigText(context.getString(R.string.daily_reminder_content_format, content.joinToString(", "), bigTextContent.joinToString("\n\n")))
                        )

                        setContentIntent(PendingIntent.getActivity(context, 1, Intent(context, SplashActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
                        setSmallIcon(R.drawable.ic_reminder_icon)
                        setAutoCancel(true)
                        color = ContextCompat.getColor(context, R.color.colorAccent)

                        if (content.isNotEmpty()) {
                            nm.notify(680000, build())
                        }
                    }
                }
            }
        }
    }

    companion object {
        val TYPE = "type"

        val MORNING = 0
        val NIGHT = 1
    }
}