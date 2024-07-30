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
import net.ienlab.sogangassist.Dlog
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.TAG
import net.ienlab.sogangassist.activity.MainActivity
import net.ienlab.sogangassist.constant.Intents
import net.ienlab.sogangassist.constant.Notifications
import net.ienlab.sogangassist.constant.PendingReq
import net.ienlab.sogangassist.constant.Pref
import net.ienlab.sogangassist.data.lms.Lms
import net.ienlab.sogangassist.data.lms.LmsDatabase
import net.ienlab.sogangassist.data.lms.LmsOfflineRepository
import net.ienlab.sogangassist.data.lms.LmsRepository
import net.ienlab.sogangassist.dataStore
import net.ienlab.sogangassist.utils.Utils.timeInMillis
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs

class ReminderReceiver: BroadcastReceiver() {

    private lateinit var nm: NotificationManager
    private lateinit var lmsRepository: LmsRepository

    override fun onReceive(context: Context, intent: Intent) {
        nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        lmsRepository = LmsOfflineRepository(LmsDatabase.getDatabase(context).getDao())

        val datastore = context.dataStore
        val type = intent.getIntExtra(Intents.Key.REMINDER_TYPE, -1)
        val typeName = mapOf(
            Lms.Type.LESSON to Pair(context.getString(R.string.daily_reminder_class), context.getString(R.string.classtime)),
            Lms.Type.SUP_LESSON to Pair(context.getString(R.string.daily_reminder_sup), context.getString(R.string.sup_classtime)),
            Lms.Type.HOMEWORK to Pair(context.getString(R.string.daily_reminder_hw), context.getString(R.string.assignment)),
            Lms.Type.ZOOM to Pair(context.getString(R.string.daily_reminder_zoom), context.getString(R.string.zoom)),
            Lms.Type.TEAMWORK to Pair(context.getString(R.string.daily_reminder_teamwork), context.getString(R.string.team_project)),
            Lms.Type.EXAM to Pair(context.getString(R.string.daily_reminder_exam), context.getString(R.string.exam)),
        )
        Dlog.d(TAG, "$type")

        nm.createNotificationChannel(NotificationChannel(Notifications.Channel.DAILY_REMINDER_ID, context.getString(
            R.string.channel_daily_reminder), NotificationManager.IMPORTANCE_HIGH))

        CoroutineScope(Dispatchers.IO).launch {
            val morningReminder = datastore.data.map { it[Pref.Key.TIME_MORNING_REMINDER] ?: Pref.Default.TIME_MORNING_REMINDER }.first()
            val nightReminder = datastore.data.map { it[Pref.Key.TIME_NIGHT_REMINDER] ?: Pref.Default.TIME_NIGHT_REMINDER }.first()
            val morningEnabled = datastore.data.map { it[Pref.Key.ALLOW_MORNING_REMINDER] ?: Pref.Default.ALLOW_MORNING_REMINDER }.first()
            val nightEnabled = datastore.data.map { it[Pref.Key.ALLOW_NIGHT_REMINDER] ?: Pref.Default.ALLOW_NIGHT_REMINDER }.first()

            val morningDate = LocalDateTime.now().withHour(morningReminder / 60).withMinute(morningReminder % 60).withSecond(0)
            val nightDate = LocalDateTime.now().withHour(nightReminder / 60).withMinute(nightReminder % 60).withSecond(0)


            val entities = lmsRepository.getByEndTimeStream(LocalDate.now()).first()
            val groups = entities.groupBy { it.type }.map { (type, items) ->
                type to items.map {
                    when (type) {
                        Lms.Type.LESSON -> context.getString(R.string.reminder_class_format, it.className, it.week, it.lesson)
                        Lms.Type.SUP_LESSON -> context.getString(R.string.reminder_class_format, it.className, it.week, it.lesson)
                        Lms.Type.HOMEWORK -> context.getString(R.string.reminder_zoom_format, it.className, it.homework_name)
                        Lms.Type.ZOOM -> context.getString(R.string.reminder_zoom_format, it.className, it.homework_name)
                        Lms.Type.TEAMWORK -> context.getString(R.string.reminder_zoom_format, it.className, it.homework_name)
                        Lms.Type.EXAM -> context.getString(R.string.reminder_zoom_format, it.className, it.homework_name)
                        else -> ""
                    }
                }
            }.toMap()

            val content = arrayListOf<String>()
            val bigTextContent = arrayListOf<String>()

            for ((t, items) in groups) {
                typeName[t]?.let {
                    content.add(String.format(it.first, items.size))
                    bigTextContent.add("<${it.second}>\n${items.joinToString("\n")}")
                }
            }

            val notification = NotificationCompat.Builder(context, Notifications.Channel.DAILY_REMINDER_ID).apply {
                setContentText(content.joinToString(", "))
                setStyle(NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.daily_reminder_content_format, content.joinToString(", "), bigTextContent.joinToString("\n\n")))
                )
                setSmallIcon(R.drawable.ic_reminder_icon)
                setAutoCancel(true)
                color = ContextCompat.getColor(context, R.color.color_sogang)
            }
            val launchIntent = Intent(context, MainActivity::class.java)

            when (type) {
                Intents.Value.ReminderType.MORNING -> {
                    if (morningEnabled && abs(ChronoUnit.MINUTES.between(LocalDateTime.now(), morningDate)) <= 1) {
                        notification
                            .setContentTitle(context.getString(R.string.daily_reminder_title_morning))
                            .setContentIntent(PendingIntent.getActivity(context, PendingReq.MORNING_REMINDER_LAUNCH, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))

                        nm.notify(Notifications.Id.REMINDER, notification.build())
                    }
                }
                Intents.Value.ReminderType.NIGHT -> {
                    if (nightEnabled && abs(ChronoUnit.MINUTES.between(LocalDateTime.now(), nightDate)) <= 1) {
                        notification
                            .setContentTitle(context.getString(R.string.daily_reminder_title_night))
                            .setContentIntent(PendingIntent.getActivity(context, PendingReq.NIGHT_REMINDER_LAUNCH, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))

                        nm.notify(Notifications.Id.REMINDER, notification.build())
                    }
                }
            }
        }
    }
}