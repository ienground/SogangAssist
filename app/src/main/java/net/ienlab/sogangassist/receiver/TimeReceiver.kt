package net.ienlab.sogangassist.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.ienlab.sogangassist.*
import net.ienlab.sogangassist.activity.*
import net.ienlab.sogangassist.constant.*
import net.ienlab.sogangassist.data.lms.LmsDatabase
import net.ienlab.sogangassist.data.lms.Lms
import net.ienlab.sogangassist.utils.Utils.checkTimeRange
import net.ienlab.sogangassist.utils.Utils.notifyToList
import net.ienlab.sogangassist.utils.Utils.parseLongToLocalDateTime
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class TimeReceiver : BroadcastReceiver() {

    private lateinit var nm: NotificationManager
    private var lmsDatabase: LmsDatabase? = null

    override fun onReceive(context: Context, intent: Intent) {
        val datastore = context.dataStore

        nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        lmsDatabase = LmsDatabase.getDatabase(context)

        nm.createNotificationChannel(NotificationChannel(Notifications.Channel.DEFAULT_ID, context.getString(R.string.channel_name), NotificationManager.IMPORTANCE_HIGH))
        nm.createNotificationChannel(NotificationChannel(Notifications.Channel.TIME_REMINDER_ID, context.getString(R.string.channel_time_reminder), NotificationManager.IMPORTANCE_HIGH))

        val id = intent.getLongExtra(Intents.Key.ITEM_ID, -1)
        val hour = intent.getIntExtra(Intents.Key.HOUR, -1)
        val minute = intent.getIntExtra(Intents.Key.MINUTE, -1)
        val triggerTime = parseLongToLocalDateTime(intent.getLongExtra(Intents.Key.TRIGGER, -1))

        val hours = listOf(1, 2, 6, 12, 24)
        val minutes = listOf(3, 5, 10, 20, 30)

        CoroutineScope(Dispatchers.IO).launch {
            val homeworkEnabled = datastore.data.map { it[Pref.Key.NOTIFY_HOMEWORK] ?: Pref.Default.NOTIFY_ALLOWED }.first().notifyToList()
            val lectureEnabled = datastore.data.map { it[Pref.Key.NOTIFY_LECTURE] ?: Pref.Default.NOTIFY_ALLOWED }.first().notifyToList()
            val zoomEnabled = datastore.data.map { it[Pref.Key.NOTIFY_ZOOM] ?: Pref.Default.NOTIFY_ALLOWED }.first().notifyToList()
            val examEnabled = datastore.data.map { it[Pref.Key.NOTIFY_EXAM] ?: Pref.Default.NOTIFY_ALLOWED }.first().notifyToList()

            val dndStartTime = datastore.data.map { it[Pref.Key.DND_START_TIME] ?: Pref.Default.DND_START_TIME }.first()
            val dndEndTime = datastore.data.map { it[Pref.Key.DND_END_TIME] ?: Pref.Default.DND_END_TIME }.first()
            val isDndEnabled = datastore.data.map { it[Pref.Key.DND_CHECK] ?: Pref.Default.DND_CHECK }.first()

            val entity = lmsDatabase?.getDao()?.get(id)?.first() ?: return@launch
            val launchIntent = Intent(context, MainActivity::class.java).apply { putExtra(Intents.Key.ITEM_ID, id) }
            val launchPending = PendingIntent.getActivity(context, PendingReq.REMINDER + id.toInt(), launchIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val markIntent = Intent(context, MarkFinishReceiver::class.java).apply { putExtra(Intents.Key.ITEM_ID, id) }
            val markPending = PendingIntent.getBroadcast(context, PendingReq.MARKING + id.toInt(), markIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            val notification = NotificationCompat.Builder(context, Notifications.Channel.TIME_REMINDER_ID).apply {
                setContentTitle(entity.className)
                setContentIntent(launchPending)
                setAutoCancel(true)
                setStyle(NotificationCompat.BigTextStyle())
                addAction(R.drawable.ic_check, context.getString(R.string.mark_as_finish), markPending)
                color = ContextCompat.getColor(context, R.color.color_sogang)
            }
            val noDnd = !(isDndEnabled && checkTimeRange(dndStartTime, dndEndTime, LocalDateTime.now().let { it.hour * it.minute }))
            if (!noDnd || entity.isFinished) return@launch

            if (abs(ChronoUnit.MINUTES.between(LocalDateTime.now(), triggerTime)) > 1 || entity.isFinished) return@launch
            when (entity.type) {
                Lms.Type.LESSON -> {
                    if (lectureEnabled[hours.indexOf(hour)]) {
                        notification.let {
                            it.setContentText(context.getString(R.string.reminder_content_lec, entity.week, entity.lesson, hour))
                            it.setSmallIcon(R.drawable.ic_video)
                        }
                        nm.notify(Notifications.Id.TIME_REMINDER + id.toInt(), notification.build())
                    }
                }
                Lms.Type.SUP_LESSON -> {
                    if (lectureEnabled[hours.indexOf(hour)]) {
                        notification.let {
                            it.setContentText(context.getString(R.string.reminder_content_sup_lec, entity.week, entity.lesson, hour))
                            it.setSmallIcon(R.drawable.ic_video_sup)
                        }
                        nm.notify(Notifications.Id.TIME_REMINDER + id.toInt(), notification.build())
                    }
                }
                Lms.Type.HOMEWORK -> {
                    if (homeworkEnabled[hours.indexOf(hour)]) {
                        notification.let {
                            it.setContentText(context.getString(R.string.reminder_content_hw, entity.homework_name, hour))
                            it.setSmallIcon(R.drawable.ic_assignment)
                        }
                        nm.notify(Notifications.Id.TIME_REMINDER + id.toInt(), notification.build())
                    }
                }
                Lms.Type.TEAMWORK -> {
                    if (homeworkEnabled[hours.indexOf(hour)]) {
                        notification.let {
                            it.setContentText(context.getString(R.string.reminder_content_team, entity.homework_name, hour))
                            it.setSmallIcon(R.drawable.ic_team)
                        }
                        nm.notify(Notifications.Id.TIME_REMINDER + id.toInt(), notification.build())
                    }
                }
                Lms.Type.ZOOM -> {
                    if (zoomEnabled[minutes.indexOf(minute)]) {
                        notification.let {
                            it.setContentText(context.getString(R.string.reminder_content_zoom, entity.homework_name, minute))
                            it.setSmallIcon(R.drawable.ic_live_class)
                        }
                        nm.notify(Notifications.Id.TIME_REMINDER + id.toInt(), notification.build())
                    }
                }
                Lms.Type.EXAM -> {
                    if (examEnabled[minutes.indexOf(minute)]) {
                        notification.let {
                            it.setContentText(context.getString(R.string.reminder_content_exam, entity.homework_name, minute))
                            it.setSmallIcon(R.drawable.ic_test)
                        }
                        nm.notify(Notifications.Id.TIME_REMINDER + id.toInt(), notification.build())
                    }
                }
            }
        }
    }
}
