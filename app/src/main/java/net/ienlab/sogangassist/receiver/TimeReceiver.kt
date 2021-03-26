package net.ienlab.sogangassist.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import net.ienlab.sogangassist.*
import net.ienlab.sogangassist.constant.ChannelId
import net.ienlab.sogangassist.database.*
import net.ienlab.sogangassist.activity.*
import net.ienlab.sogangassist.constant.DefaultValue
import net.ienlab.sogangassist.constant.SharedGroup
import net.ienlab.sogangassist.data.LMSClass
import net.ienlab.sogangassist.data.NotificationItem
import net.ienlab.sogangassist.utils.MyUtils
import java.util.*
import kotlin.math.abs

class TimeReceiver : BroadcastReceiver() {

    lateinit var nm: NotificationManager
    lateinit var dbHelper: DBHelper
    lateinit var notiDBHelper: NotiDBHelper
    lateinit var sharedPreferences: SharedPreferences

    override fun onReceive(context: Context, intent: Intent) {
        nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        dbHelper = DBHelper(context, DBHelper.dbName, DBHelper.dbVersion)
        notiDBHelper = NotiDBHelper(context, NotiDBHelper.dbName, NotiDBHelper.dbVersion)
        sharedPreferences = context.getSharedPreferences("${context.packageName}_preferences", Context.MODE_PRIVATE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(ChannelId.DEFAULT_ID, context.getString(R.string.channel_name), NotificationManager.IMPORTANCE_HIGH)
            nm.createNotificationChannel(channel)
        }

        val id = intent.getIntExtra("ID", -1)
        val item = dbHelper.getItemById(id)
        val time = intent.getIntExtra("TIME", -1)
        val minute = intent.getIntExtra("MINUTE", -1)
        val triggerTime = intent.getLongExtra("TRIGGER", -1)

        val hourData = listOf(1, 2, 6, 12, 24)
        val minuteData = listOf(3, 5, 10, 20, 30)

        val hwSharedKeys = listOf(SharedGroup.NOTIFY_1HOUR_HW, SharedGroup.NOTIFY_2HOUR_HW, SharedGroup.NOTIFY_6HOUR_HW, SharedGroup.NOTIFY_12HOUR_HW, SharedGroup.NOTIFY_24HOUR_HW)
        val lecSharedKeys = listOf(SharedGroup.NOTIFY_1HOUR_LEC, SharedGroup.NOTIFY_2HOUR_LEC, SharedGroup.NOTIFY_6HOUR_LEC, SharedGroup.NOTIFY_12HOUR_LEC, SharedGroup.NOTIFY_24HOUR_LEC)
        val zoomSharedKeys = listOf(SharedGroup.NOTIFY_3MIN_ZOOM, SharedGroup.NOTIFY_5MIN_ZOOM, SharedGroup.NOTIFY_10MIN_ZOOM, SharedGroup.NOTIFY_20MIN_ZOOM, SharedGroup.NOTIFY_30MIN_ZOOM)

        val hwHoursOn = mutableListOf<Int>()
        val lecHoursOn = mutableListOf<Int>()
        val zoomMinutesOn = mutableListOf<Int>()

        hwSharedKeys.forEachIndexed { index, s -> if (sharedPreferences.getBoolean(s, true)) hwHoursOn.add(hourData[index]) }
        lecSharedKeys.forEachIndexed { index, s -> if (sharedPreferences.getBoolean(s, true)) lecHoursOn.add(hourData[index]) }
        zoomSharedKeys.forEachIndexed { index, s -> if (sharedPreferences.getBoolean(s, true)) zoomMinutesOn.add(minuteData[index]) }

        val dndStartData = sharedPreferences.getInt(SharedGroup.DND_START_TIME, DefaultValue.DND_START_TIME)
        val dndEndData = sharedPreferences.getInt(SharedGroup.DND_END_TIME, DefaultValue.DND_END_TIME)
        val now = Calendar.getInstance()
        val nowInt = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        when (item.type) {
            LMSClass.TYPE_LESSON -> {
                if (abs(System.currentTimeMillis() - triggerTime) <= 3000 && !item.isFinished && (time in lecHoursOn)) {
                    if (item.className != "") {
                        NotificationCompat.Builder(context, ChannelId.DEFAULT_ID).apply {
                            val notiId = notiDBHelper.addItem(NotificationItem(-1, item.className, context.getString(R.string.reminder_content_lec, item.week, item.lesson, time), System.currentTimeMillis(), NotificationItem.TYPE_LESSON, id, false))
                            val clickIntent = Intent(context, SplashActivity::class.java).apply { putExtra("ID", id); putExtra("NOTI_ID", notiId) }
                            val clickPendingIntent = PendingIntent.getActivity(context, id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                            val markIntent = Intent(context, MarkFinishReceiver::class.java).apply { putExtra("ID", id); putExtra("NOTI_ID", notiId) }
                            val pendingIntent = PendingIntent.getBroadcast(context, id, markIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                            val setReadIntent = Intent(context, SetReadReceiver::class.java).apply { putExtra("NOTI_ID", notiId); putExtra("CANCEL_ID", 693000 + id) }
                            val setReadPendingIntent = PendingIntent.getBroadcast(context, notiId, setReadIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                            setContentTitle(item.className)
                            setContentText(context.getString(R.string.reminder_content_lec, item.week, item.lesson, time))
                            setContentIntent(clickPendingIntent)
                            setAutoCancel(true)
                            setStyle(NotificationCompat.BigTextStyle())
                            setSmallIcon(R.drawable.ic_video)
                            addAction(R.drawable.ic_check, context.getString(R.string.mark_as_finish), pendingIntent)
                            addAction(R.drawable.ic_mark_as_read, context.getString(R.string.mark_as_read), setReadPendingIntent)
                            color = ContextCompat.getColor(context, R.color.colorAccent)

                            if (!sharedPreferences.getBoolean(SharedGroup.DND_CHECK, false) || (!MyUtils.isDNDTime(dndStartData, dndEndData, nowInt))) {
                                nm.notify(693000 + id, build())
                            }
                        }
                    }
                }
            }

            LMSClass.TYPE_SUP_LESSON -> {
                if (abs(System.currentTimeMillis() - triggerTime) <= 3000 && !item.isFinished && (time in lecHoursOn)) {
                    if (item.className != "") {
                        NotificationCompat.Builder(context, ChannelId.DEFAULT_ID).apply {
                            val notiId = notiDBHelper.addItem(NotificationItem(-1, item.className, context.getString(R.string.reminder_content_lec, item.week, item.lesson, time), System.currentTimeMillis(), NotificationItem.TYPE_SUP_LESSON, id, false))
                            val clickIntent = Intent(context, SplashActivity::class.java).apply { putExtra("ID", id); putExtra("NOTI_ID", notiId) }
                            val clickPendingIntent = PendingIntent.getActivity(context, id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                            val markIntent = Intent(context, MarkFinishReceiver::class.java).apply { putExtra("ID", id); putExtra("NOTI_ID", notiId) }
                            val pendingIntent = PendingIntent.getBroadcast(context, id, markIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                            val setReadIntent = Intent(context, SetReadReceiver::class.java).apply { putExtra("NOTI_ID", notiId); putExtra("CANCEL_ID", 693000 + id) }
                            val setReadPendingIntent = PendingIntent.getBroadcast(context, notiId, setReadIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                            setContentTitle(item.className)
                            setContentText(context.getString(R.string.reminder_content_sup_lec, item.week, item.lesson, time))
                            setContentIntent(clickPendingIntent)
                            setAutoCancel(true)
                            setStyle(NotificationCompat.BigTextStyle())
                            setSmallIcon(R.drawable.ic_video_sup)
                            addAction(R.drawable.ic_check, context.getString(R.string.mark_as_finish), pendingIntent)
                            addAction(R.drawable.ic_mark_as_read, context.getString(R.string.mark_as_read), setReadPendingIntent)
                            color = ContextCompat.getColor(context, R.color.colorAccent)

                            if (!sharedPreferences.getBoolean(SharedGroup.DND_CHECK, false) || (!MyUtils.isDNDTime(dndStartData, dndEndData, nowInt))) {
                                nm.notify(693000 + id, build())
                            }
                        }
                    }
                }
            }

            LMSClass.TYPE_HOMEWORK -> {
                if (abs(System.currentTimeMillis() - triggerTime) <= 3000 && !item.isFinished && (time in hwHoursOn)) {
                    if (item.className != "") {
                        NotificationCompat.Builder(context, ChannelId.DEFAULT_ID).apply {
                            val notiId = notiDBHelper.addItem(NotificationItem(-1, item.className, context.getString(R.string.reminder_content_hw, item.homework_name, time), System.currentTimeMillis(), NotificationItem.TYPE_HOMEWORK, id, false))
                            val clickIntent = Intent(context, SplashActivity::class.java).apply { putExtra("ID", id); putExtra("NOTI_ID", notiId) }
                            val clickPendingIntent = PendingIntent.getActivity(context, id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                            val markIntent = Intent(context, MarkFinishReceiver::class.java).apply { putExtra("ID", id); putExtra("NOTI_ID", notiId) }
                            val pendingIntent = PendingIntent.getBroadcast(context, id, markIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                            val setReadIntent = Intent(context, SetReadReceiver::class.java).apply { putExtra("NOTI_ID", notiId); putExtra("CANCEL_ID", 693000 + id) }
                            val setReadPendingIntent = PendingIntent.getBroadcast(context, notiId, setReadIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                            setContentTitle(item.className)
                            setContentText(context.getString(R.string.reminder_content_hw, item.homework_name, time))
                            setContentIntent(clickPendingIntent)
                            setAutoCancel(true)
                            setStyle(NotificationCompat.BigTextStyle())
                            setSmallIcon(R.drawable.ic_assignment)
                            addAction(R.drawable.ic_check, context.getString(R.string.mark_as_finish), pendingIntent)
                            addAction(R.drawable.ic_mark_as_read, context.getString(R.string.mark_as_read), setReadPendingIntent)
                            color = ContextCompat.getColor(context, R.color.colorAccent)

                            if (!sharedPreferences.getBoolean(SharedGroup.DND_CHECK, false) || (!MyUtils.isDNDTime(dndStartData, dndEndData, nowInt))) {
                                nm.notify(693000 + id, build())
                            }
                        }
                    }
                }
            }

            LMSClass.TYPE_ZOOM -> {
                if (abs(System.currentTimeMillis() - triggerTime) <= 3000 && !item.isFinished && (minute in zoomMinutesOn)) {
                    if (item.className != "") {
                        NotificationCompat.Builder(context, ChannelId.DEFAULT_ID).apply {
                            val notiId = notiDBHelper.addItem(NotificationItem(-1, item.className, context.getString(R.string.reminder_content_zoom, item.homework_name, minute), System.currentTimeMillis(), NotificationItem.TYPE_ZOOM, id, false))
                            val clickIntent = Intent(context, SplashActivity::class.java).apply { putExtra("ID", id); putExtra("NOTI_ID", notiId) }
                            val clickPendingIntent = PendingIntent.getActivity(context, id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                            val markIntent = Intent(context, MarkFinishReceiver::class.java).apply { putExtra("ID", id); putExtra("NOTI_ID", notiId) }
                            val pendingIntent = PendingIntent.getBroadcast(context, id, markIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                            val setReadIntent = Intent(context, SetReadReceiver::class.java).apply { putExtra("NOTI_ID", notiId); putExtra("CANCEL_ID", 693000 + id) }
                            val setReadPendingIntent = PendingIntent.getBroadcast(context, notiId, setReadIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                            setContentTitle(item.className)
                            setContentText(context.getString(R.string.reminder_content_zoom, item.homework_name, minute))
                            setContentIntent(clickPendingIntent)
                            setAutoCancel(true)
                            setStyle(NotificationCompat.BigTextStyle())
                            setSmallIcon(R.drawable.ic_groups)
                            addAction(R.drawable.ic_check, context.getString(R.string.mark_as_finish), pendingIntent)
                            addAction(R.drawable.ic_mark_as_read, context.getString(R.string.mark_as_read), setReadPendingIntent)
                            color = ContextCompat.getColor(context, R.color.colorAccent)

                            if (!sharedPreferences.getBoolean(SharedGroup.DND_CHECK, false) || (!MyUtils.isDNDTime(dndStartData, dndEndData, nowInt))) {
                                nm.notify(693000 + id, build())
                            }
                        }
                    }
                }
            }
        }
    }
}
