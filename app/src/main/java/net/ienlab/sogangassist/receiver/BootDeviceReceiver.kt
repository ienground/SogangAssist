package net.ienlab.sogangassist.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.ienlab.sogangassist.constant.DefaultValue
import net.ienlab.sogangassist.constant.Intents
import net.ienlab.sogangassist.constant.PendingReq
import net.ienlab.sogangassist.data.lms.LmsDatabase
import net.ienlab.sogangassist.data.lms.Lms
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
//            startServiceByAlarm(context)
        }
    }


//    @OptIn(DelicateCoroutinesApi::class)
//    private fun startServiceByAlarm(context: Context) {
//        val sharedPreferences = context.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE)
//        val lmsDatabase = LmsDatabase.getDatabase(context)
//
//        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
//
//        val message = "Start service use repeat alarm. in ${context.packageName}"
//
//        Log.d(TAG_BOOT_BROADCAST_RECEIVER, message)
//
//        // 하루 시작, 끝 리마인더 알람 만들기
//        val morningReminderCalendar = Calendar.getInstance().apply {
//            val time = sharedPreferences.getInt(SharedKey.TIME_MORNING_REMINDER, DefaultValue.TIME_MORNING_REMINDER)
//
//            set(Calendar.HOUR_OF_DAY, time / 60)
//            set(Calendar.MINUTE, time % 60)
//            set(Calendar.SECOND, 0)
//            set(Calendar.MILLISECOND, 0)
//        }
//
//        val nightReminderCalendar = Calendar.getInstance().apply {
//            val time = sharedPreferences.getInt(SharedKey.TIME_NIGHT_REMINDER, DefaultValue.TIME_NIGHT_REMINDER)
//
//            set(Calendar.HOUR_OF_DAY, time / 60)
//            set(Calendar.MINUTE, time % 60)
//            set(Calendar.SECOND, 0)
//            set(Calendar.MILLISECOND, 0)
//        }
//
//        val morningReminderIntent = Intent(context, ReminderReceiver::class.java).apply { putExtra(ReminderReceiver.TYPE, ReminderReceiver.MORNING) }
//        val nightReminderIntent = Intent(context, ReminderReceiver::class.java).apply { putExtra(ReminderReceiver.TYPE, ReminderReceiver.NIGHT) }
//
//        am.setRepeating(AlarmManager.RTC_WAKEUP, morningReminderCalendar.timeInMillis, AlarmManager.INTERVAL_DAY,
//            PendingIntent.getBroadcast(context, PendingReq.MORNING_REMINDER, morningReminderIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
//
//        am.setRepeating(AlarmManager.RTC_WAKEUP, nightReminderCalendar.timeInMillis, AlarmManager.INTERVAL_DAY,
//            PendingIntent.getBroadcast(context, PendingReq.NIGHT_REMINDER, nightReminderIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
//
//        GlobalScope.launch(Dispatchers.IO) {
//            val datas = lmsDatabase?.getDao()?.getAll()
//            if (datas != null) {
//                for (data in datas) {
//                    val notiIntent = Intent(context, TimeReceiver::class.java).apply { putExtra(Intents.Key.ITEM_ID, data.id) }
//                    val hours = listOf(1, 2, 6, 12, 24)
//                    val minutes = listOf(3, 5, 10, 20, 30)
//
//                    if (data.endTime < System.currentTimeMillis()) continue
//
//                    when (data.type) {
//                        Lms.TYPE_HOMEWORK, Lms.TYPE_LESSON, Lms.TYPE_SUP_LESSON, Lms.TYPE_TEAMWORK -> {
//                            hours.forEachIndexed { index, i ->
//                                val triggerTime = data.endTime - i * 60 * 60 * 1000
//                                notiIntent.putExtra(Intents.Key.TRIGGER, triggerTime)
//                                notiIntent.putExtra(Intents.Key.TIME, i)
//                                val pendingIntent = PendingIntent.getBroadcast(context, PendingReq.LAUNCH_NOTI + (data.id?.toInt()?.times(100) ?: 0) + index + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//                                if (triggerTime > System.currentTimeMillis()) am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
//                            }
//                        }
//                        Lms.TYPE_ZOOM, Lms.TYPE_EXAM -> {
//                            minutes.forEachIndexed { index, i ->
//                                val triggerTime = data.endTime - i * 60 * 1000
//                                notiIntent.putExtra(Intents.Key.TRIGGER, triggerTime)
//                                notiIntent.putExtra(Intents.Key.MINUTE, i)
//                                val pendingIntent = PendingIntent.getBroadcast(context, PendingReq.LAUNCH_NOTI + (data.id?.toInt()?.times(100) ?: 0) + index + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//                                if (triggerTime > System.currentTimeMillis()) am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
}