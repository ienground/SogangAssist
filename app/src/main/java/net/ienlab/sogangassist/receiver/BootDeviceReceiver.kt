package net.ienlab.sogangassist.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import net.ienlab.sogangassist.*
import net.ienlab.sogangassist.database.*
import net.ienlab.sogangassist.constant.SharedGroup
import net.ienlab.sogangassist.data.LMSClass
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
            val notiIntent = Intent(context, TimeReceiver::class.java).apply { putExtra("ID", data.id) }
            val hours = listOf(1, 2, 6, 12, 24)
            val minutes = listOf(3, 5, 10, 20, 30)

            if (data.endTime < System.currentTimeMillis()) continue

            when (data.type) {
                LMSClass.TYPE_HOMEWORK, LMSClass.TYPE_LESSON, LMSClass.TYPE_SUP_LESSON -> {
                    hours.forEachIndexed { index, i ->
                        val triggerTime = data.endTime - i * 60 * 60 * 1000
                        notiIntent.putExtra("TRIGGER", triggerTime)
                        notiIntent.putExtra("TIME", i)
                        val pendingIntent = PendingIntent.getBroadcast(context, data.id * 100 + index + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                        am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    }
                }
                LMSClass.TYPE_ZOOM -> {
                    minutes.forEachIndexed { index, i ->
                        val triggerTime = data.endTime - i * 60 * 1000
                        notiIntent.putExtra("TRIGGER", triggerTime)
                        notiIntent.putExtra("MINUTE", i)
                        val pendingIntent = PendingIntent.getBroadcast(context, data.id * 100 + index + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                        am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                    }
                }
            }
        }
    }
}