package net.ienlab.sogangassist.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.ienlab.sogangassist.*
import net.ienlab.sogangassist.activity.*
import net.ienlab.sogangassist.constant.*
import net.ienlab.sogangassist.data.lms.LmsDatabase
import net.ienlab.sogangassist.data.lms.Lms
import net.ienlab.sogangassist.utils.Utils
import java.util.*
import kotlin.math.abs

class TimeReceiver : BroadcastReceiver() {

//    lateinit var nm: NotificationManager
//    lateinit var sharedPreferences: SharedPreferences
//
//    private var lmsDatabase: LmsDatabase? = null
//
    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
//        nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        sharedPreferences = context.getSharedPreferences("${context.packageName}_preferences", Context.MODE_PRIVATE)
//        lmsDatabase = LmsDatabase.getDatabase(context)
//
//        nm.createNotificationChannel(NotificationChannel(Notifications.Channel.DEFAULT_ID, context.getString(R.string.channel_name), NotificationManager.IMPORTANCE_HIGH))
//        nm.createNotificationChannel(NotificationChannel(Notifications.Channel.TIME_REMINDER_ID, context.getString(R.string.channel_time_reminder), NotificationManager.IMPORTANCE_HIGH))
//
//        val id = intent.getLongExtra(Intents.Key.ITEM_ID, -1)
//        val time = intent.getIntExtra(Intents.Key.TIME, -1)
//        val minute = intent.getIntExtra(Intents.Key.MINUTE, -1)
//        val triggerTime = intent.getLongExtra(Intents.Key.TRIGGER, -1)
//
//
//        Log.d(TAG, "TimeReceiver ${id} $time ${minute} ${Date(triggerTime)}")
//
//        val hourData = listOf(1, 2, 6, 12, 24)
//        val minuteData = listOf(3, 5, 10, 20, 30)
//
//        val hwSharedKeys = listOf(SharedKey.NOTIFY_1HOUR_HW, SharedKey.NOTIFY_2HOUR_HW, SharedKey.NOTIFY_6HOUR_HW, SharedKey.NOTIFY_12HOUR_HW, SharedKey.NOTIFY_24HOUR_HW)
//        val lecSharedKeys = listOf(SharedKey.NOTIFY_1HOUR_LEC, SharedKey.NOTIFY_2HOUR_LEC, SharedKey.NOTIFY_6HOUR_LEC, SharedKey.NOTIFY_12HOUR_LEC, SharedKey.NOTIFY_24HOUR_LEC)
//        val zoomSharedKeys = listOf(SharedKey.NOTIFY_3MIN_ZOOM, SharedKey.NOTIFY_5MIN_ZOOM, SharedKey.NOTIFY_10MIN_ZOOM, SharedKey.NOTIFY_20MIN_ZOOM, SharedKey.NOTIFY_30MIN_ZOOM)
//        val examSharedKeys = listOf(SharedKey.NOTIFY_3MIN_EXAM, SharedKey.NOTIFY_5MIN_EXAM, SharedKey.NOTIFY_10MIN_EXAM, SharedKey.NOTIFY_20MIN_EXAM, SharedKey.NOTIFY_30MIN_EXAM)
//
//        val hwHoursOn = arrayListOf<Int>()
//        val lecHoursOn = arrayListOf<Int>()
//        val zoomMinutesOn = arrayListOf<Int>()
//        val examMinutesOn = arrayListOf<Int>()
//
//        hwSharedKeys.forEachIndexed { index, s -> if (sharedPreferences.getBoolean(s, true)) hwHoursOn.add(hourData[index]) }
//        lecSharedKeys.forEachIndexed { index, s -> if (sharedPreferences.getBoolean(s, true)) lecHoursOn.add(hourData[index]) }
//        zoomSharedKeys.forEachIndexed { index, s -> if (sharedPreferences.getBoolean(s, true)) zoomMinutesOn.add(minuteData[index]) }
//        examSharedKeys.forEachIndexed { index, s -> if (sharedPreferences.getBoolean(s, true)) examMinutesOn.add(minuteData[index]) }
//
//        val dndStartData = sharedPreferences.getInt(SharedKey.DND_START_TIME, DefaultValue.DND_START_TIME)
//        val dndEndData = sharedPreferences.getInt(SharedKey.DND_END_TIME, DefaultValue.DND_END_TIME)
//        val now = Calendar.getInstance()
//        val nowInt = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
//
//        GlobalScope.launch(Dispatchers.IO) {
//            val item = lmsDatabase?.getDao()?.get(id) ?: Lms("", 0L, 0, 0L, 0L, false, false, -1, -1, "")
//            Log.d(TAG, "${item}")
//            when (item.type) {
//                Lms.TYPE_LESSON -> {
//                    if (abs(System.currentTimeMillis() - triggerTime) <= 3000 && !item.isFinished && (time in lecHoursOn)) {
//                        if (item.className != "") {
//                            NotificationCompat.Builder(context, Notifications.Channel.TIME_REMINDER_ID).apply {
//                                val clickIntent = Intent(context, MainActivity::class.java).apply { putExtra(Intents.Key.ITEM_ID, id) }
//                                val clickPendingIntent = PendingIntent.getActivity(context, PendingReq.LAUNCH_NOTI + id.toInt(), clickIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//                                val markIntent = Intent(context, MarkFinishReceiver::class.java).apply { putExtra(Intents.Key.ITEM_ID, id) }
//                                val pendingIntent = PendingIntent.getBroadcast(context, PendingReq.MARKING + id.toInt(), markIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//
//                                setContentTitle(item.className)
//                                setContentText(context.getString(R.string.reminder_content_lec, item.week, item.lesson, time))
//                                setContentIntent(clickPendingIntent)
//                                setAutoCancel(true)
//                                setStyle(NotificationCompat.BigTextStyle())
//                                setSmallIcon(R.drawable.ic_video)
//                                addAction(R.drawable.ic_check, context.getString(R.string.mark_as_finish), pendingIntent)
//                                color = ContextCompat.getColor(context, R.color.colorPrimary)
//
//                                if (!sharedPreferences.getBoolean(SharedKey.DND_CHECK, false) || (!Utils.isDNDTime(dndStartData, dndEndData, nowInt))) {
//                                    nm.notify(Notifications.Id.TIME_REMINDER + id.toInt(), build())
//                                }
//                            }
//                        }
//                    }
//                }
//                Lms.TYPE_SUP_LESSON -> {
//                    if (abs(System.currentTimeMillis() - triggerTime) <= 3000 && !item.isFinished && (time in lecHoursOn)) {
//                        if (item.className != "") {
//                            NotificationCompat.Builder(context, Notifications.Channel.TIME_REMINDER_ID).apply {
//                                val clickIntent = Intent(context, MainActivity::class.java).apply { putExtra(Intents.Key.ITEM_ID, id) }
//                                val clickPendingIntent = PendingIntent.getActivity(context, PendingReq.LAUNCH_NOTI + id.toInt(), clickIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//                                val markIntent = Intent(context, MarkFinishReceiver::class.java).apply { putExtra(Intents.Key.ITEM_ID, id) }
//                                val pendingIntent = PendingIntent.getBroadcast(context, PendingReq.MARKING + id.toInt(), markIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//
//                                setContentTitle(item.className)
//                                setContentText(context.getString(R.string.reminder_content_sup_lec, item.week, item.lesson, time))
//                                setContentIntent(clickPendingIntent)
//                                setAutoCancel(true)
//                                setStyle(NotificationCompat.BigTextStyle())
//                                setSmallIcon(R.drawable.ic_video_sup)
//                                addAction(R.drawable.ic_check, context.getString(R.string.mark_as_finish), pendingIntent)
//                                color = ContextCompat.getColor(context, R.color.colorPrimary)
//
//                                if (!sharedPreferences.getBoolean(SharedKey.DND_CHECK, false) || (!Utils.isDNDTime(dndStartData, dndEndData, nowInt))) {
//                                    nm.notify(Notifications.Id.TIME_REMINDER + id.toInt(), build())
//                                }
//                            }
//                        }
//                    }
//                }
//                Lms.TYPE_HOMEWORK -> {
//                    if (abs(System.currentTimeMillis() - triggerTime) <= 30 * 1000 && !item.isFinished && (time in hwHoursOn)) {
//                        if (item.className != "") {
//                            NotificationCompat.Builder(context, Notifications.Channel.TIME_REMINDER_ID).apply {
//                                val clickIntent = Intent(context, MainActivity::class.java).apply { putExtra(Intents.Key.ITEM_ID, id) }
//                                val clickPendingIntent = PendingIntent.getActivity(context, PendingReq.LAUNCH_NOTI + id.toInt(), clickIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//                                val markIntent = Intent(context, MarkFinishReceiver::class.java).apply { putExtra(Intents.Key.ITEM_ID, id) }
//                                val pendingIntent = PendingIntent.getBroadcast(context, PendingReq.MARKING + id.toInt(), markIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//
//                                setContentTitle(item.className)
//                                setContentText(context.getString(R.string.reminder_content_hw, item.homework_name, time))
//                                setContentIntent(clickPendingIntent)
//                                setAutoCancel(true)
//                                setStyle(NotificationCompat.BigTextStyle())
//                                setSmallIcon(R.drawable.ic_assignment)
//                                addAction(R.drawable.ic_check, context.getString(R.string.mark_as_finish), pendingIntent)
//                                color = ContextCompat.getColor(context, R.color.colorPrimary)
//
//                                if (!sharedPreferences.getBoolean(SharedKey.DND_CHECK, false) || (!Utils.isDNDTime(dndStartData, dndEndData, nowInt))) {
//                                    nm.notify(Notifications.Id.TIME_REMINDER + id.toInt(), build())
//                                }
//                            }
//                        }
//                    }
//                }
//                Lms.TYPE_ZOOM -> {
//                    if (abs(System.currentTimeMillis() - triggerTime) <= 3000 && !item.isFinished && (minute in zoomMinutesOn)) {
//                        if (item.className != "") {
//                            NotificationCompat.Builder(context, Notifications.Channel.TIME_REMINDER_ID).apply {
//                                val clickIntent = Intent(context, MainActivity::class.java).apply { putExtra(Intents.Key.ITEM_ID, id) }
//                                val clickPendingIntent = PendingIntent.getActivity(context, PendingReq.LAUNCH_NOTI + id.toInt(), clickIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//                                val markIntent = Intent(context, MarkFinishReceiver::class.java).apply { putExtra(Intents.Key.ITEM_ID, id) }
//                                val pendingIntent = PendingIntent.getBroadcast(context, PendingReq.MARKING + id.toInt(), markIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//
//                                setContentTitle(item.className)
//                                setContentText(context.getString(R.string.reminder_content_zoom, item.homework_name, minute))
//                                setContentIntent(clickPendingIntent)
//                                setAutoCancel(true)
//                                setStyle(NotificationCompat.BigTextStyle())
//                                setSmallIcon(R.drawable.ic_live_class)
//                                addAction(R.drawable.ic_check, context.getString(R.string.mark_as_finish), pendingIntent)
//                                color = ContextCompat.getColor(context, R.color.colorPrimary)
//
//                                if (!sharedPreferences.getBoolean(SharedKey.DND_CHECK, false) || (!Utils.isDNDTime(dndStartData, dndEndData, nowInt))) {
//                                    nm.notify(Notifications.Id.TIME_REMINDER + id.toInt(), build())
//                                }
//                            }
//                        }
//                    }
//                }
//                Lms.TYPE_TEAMWORK -> {
//                    if (abs(System.currentTimeMillis() - triggerTime) <= 3000 && !item.isFinished && (time in hwHoursOn)) {
//                        if (item.className != "") {
//                            NotificationCompat.Builder(context, Notifications.Channel.TIME_REMINDER_ID).apply {
//                                val clickIntent = Intent(context, MainActivity::class.java).apply { putExtra(Intents.Key.ITEM_ID, id) }
//                                val clickPendingIntent = PendingIntent.getActivity(context, PendingReq.LAUNCH_NOTI + id.toInt(), clickIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//                                val markIntent = Intent(context, MarkFinishReceiver::class.java).apply { putExtra(Intents.Key.ITEM_ID, id) }
//                                val pendingIntent = PendingIntent.getBroadcast(context, PendingReq.MARKING + id.toInt(), markIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//
//                                setContentTitle(item.className)
//                                setContentText(context.getString(R.string.reminder_content_team, item.homework_name, time))
//                                setContentIntent(clickPendingIntent)
//                                setAutoCancel(true)
//                                setStyle(NotificationCompat.BigTextStyle())
//                                setSmallIcon(R.drawable.ic_team)
//                                addAction(R.drawable.ic_check, context.getString(R.string.mark_as_finish), pendingIntent)
//                                color = ContextCompat.getColor(context, R.color.colorPrimary)
//
//                                if (!sharedPreferences.getBoolean(SharedKey.DND_CHECK, false) || (!Utils.isDNDTime(dndStartData, dndEndData, nowInt))) {
//                                    nm.notify(Notifications.Id.TIME_REMINDER + id.toInt(), build())
//                                }
//                            }
//                        }
//                    }
//                }
//                Lms.TYPE_EXAM -> {
//                    if (abs(System.currentTimeMillis() - triggerTime) <= 3000 && !item.isFinished && (minute in examMinutesOn)) {
//                        if (item.className != "") {
//                            NotificationCompat.Builder(context, Notifications.Channel.TIME_REMINDER_ID).apply {
//                                val clickIntent = Intent(context, MainActivity::class.java).apply { putExtra(Intents.Key.ITEM_ID, id) }
//                                val clickPendingIntent = PendingIntent.getActivity(context, PendingReq.LAUNCH_NOTI + id.toInt(), clickIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//                                val markIntent = Intent(context, MarkFinishReceiver::class.java).apply { putExtra(Intents.Key.ITEM_ID, id) }
//                                val pendingIntent = PendingIntent.getBroadcast(context, PendingReq.MARKING + id.toInt(), markIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//
//                                setContentTitle(item.className)
//                                setContentText(context.getString(R.string.reminder_content_exam, item.homework_name, minute))
//                                setContentIntent(clickPendingIntent)
//                                setAutoCancel(true)
//                                setStyle(NotificationCompat.BigTextStyle())
//                                setSmallIcon(R.drawable.ic_test)
//                                addAction(R.drawable.ic_check, context.getString(R.string.mark_as_finish), pendingIntent)
//                                color = ContextCompat.getColor(context, R.color.colorPrimary)
//
//                                if (!sharedPreferences.getBoolean(SharedKey.DND_CHECK, false) || (!Utils.isDNDTime(dndStartData, dndEndData, nowInt))) {
//                                    nm.notify(Notifications.Id.TIME_REMINDER + id.toInt(), build())
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }


    }
}
