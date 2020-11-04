package net.ienlab.sogangassist

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*

class BootDeviceReceiver : BroadcastReceiver() {

    /**
     * https://www.dev2qa.com/how-to-start-android-service-automatically-at-boot-time/
     */

    val TAG_BOOT_BROADCAST_RECEIVER = "BOOT_BROADCAST_RECEIVER"

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val message = "BootDeviceReceiver onReceive, action is $action"

        Log.d(TAG_BOOT_BROADCAST_RECEIVER, message)

        if (Intent.ACTION_BOOT_COMPLETED == action) {
            startServiceByAlarm(context)
        }
    }


    private fun startServiceByAlarm(context: Context) {
        val sharedPreferences = context.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE)
        val dbHelper = DBHelper(context, dbName, dbVersion)

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val message = "Start service use repeat alarm. in ${context.packageName}"

        Log.d(TAG_BOOT_BROADCAST_RECEIVER, message)

        val datas = dbHelper.getAllData()
        for (data in datas) {
            val noti_intent = Intent(context, TimeReceiver::class.java)
            noti_intent.putExtra("ID", data.id)

            val endCalendar = Calendar.getInstance().apply {
                timeInMillis = data.endTime
            }

            if (data.type == LMSType.HOMEWORK) {
                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_1HOUR_HW, false)) {
                    val triggerTime = endCalendar.timeInMillis - 1 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 1)
                    val pendingIntent = PendingIntent.getBroadcast(context, data.id * 100 + 1, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_2HOUR_HW, false)) {
                    val triggerTime = endCalendar.timeInMillis - 2 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 2)
                    val pendingIntent = PendingIntent.getBroadcast(context, data.id * 100 + 2, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_6HOUR_HW, false)) {
                    val triggerTime = endCalendar.timeInMillis - 6 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 6)
                    val pendingIntent = PendingIntent.getBroadcast(context, data.id * 100 + 3, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_12HOUR_HW, false)) {
                    val triggerTime = endCalendar.timeInMillis - 12 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 12)
                    val pendingIntent = PendingIntent.getBroadcast(context, data.id * 100 + 4, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_24HOUR_HW, false)) {
                    val triggerTime = endCalendar.timeInMillis - 24 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 24)
                    val pendingIntent = PendingIntent.getBroadcast(context, data.id * 100 + 5, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            } else if (data.type == LMSType.LESSON || data.type == LMSType.SUP_LESSON) {
                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_1HOUR_LEC, false)) {
                    val triggerTime = endCalendar.timeInMillis - 1 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 1)
                    val pendingIntent = PendingIntent.getBroadcast(context, data.id * 100 + 6, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_2HOUR_LEC, false)) {
                    val triggerTime = endCalendar.timeInMillis - 2 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 2)
                    val pendingIntent = PendingIntent.getBroadcast(context, data.id * 100 + 7, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_6HOUR_LEC, false)) {
                    val triggerTime = endCalendar.timeInMillis - 6 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 6)
                    val pendingIntent = PendingIntent.getBroadcast(context, data.id * 100 + 8, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_12HOUR_LEC, false)) {
                    val triggerTime = endCalendar.timeInMillis - 12 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 12)
                    val pendingIntent = PendingIntent.getBroadcast(context, data.id * 100 + 9, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }

                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_24HOUR_LEC, false)) {
                    val triggerTime = endCalendar.timeInMillis - 24 * 60 * 60 * 1000
                    noti_intent.putExtra("TRIGGER", triggerTime)
                    noti_intent.putExtra("TIME", 24)
                    val pendingIntent = PendingIntent.getBroadcast(context, data.id * 100 + 10, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
            }
        }
    }
}