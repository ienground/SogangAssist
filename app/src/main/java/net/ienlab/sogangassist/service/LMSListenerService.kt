package net.ienlab.sogangassist.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.datastore.dataStore
import androidx.preference.PreferenceDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.ienlab.sogangassist.*
import net.ienlab.sogangassist.receiver.TimeReceiver
import net.ienlab.sogangassist.activity.*
import net.ienlab.sogangassist.constant.*
import net.ienlab.sogangassist.receiver.DeleteMissReceiver
import net.ienlab.sogangassist.data.lms.LmsDatabase
import net.ienlab.sogangassist.data.lms.Lms
import net.ienlab.sogangassist.data.lms.LmsOfflineRepository
import net.ienlab.sogangassist.data.lms.LmsRepository
import net.ienlab.sogangassist.utils.Utils
import net.ienlab.sogangassist.utils.Utils.checkTimeRange
import net.ienlab.sogangassist.utils.Utils.parseLongToLocalDateTime
import net.ienlab.sogangassist.utils.Utils.setLmsSchedule
import net.ienlab.sogangassist.utils.Utils.timeInMillis
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class LMSListenerService : NotificationListenerService() {

    private lateinit var pm: PackageManager
    private lateinit var am: AlarmManager
    private lateinit var nm: NotificationManager

    private lateinit var timeFormat: DateTimeFormatter

    private lateinit var lmsRepository: LmsRepository
    private var setRegisterAlert: Boolean = Pref.Default.SET_REGISTER_ALERT
    private var dndStartTime: Int = Pref.Default.DND_START_TIME
    private var dndEndTime: Int = Pref.Default.DND_END_TIME
    private var isDndEnabled: Boolean = Pref.Default.DND_CHECK

    override fun onCreate() {
        super.onCreate()
        pm = packageManager
        am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        lmsRepository = LmsOfflineRepository(LmsDatabase.getDatabase(applicationContext).getDao())
        timeFormat = DateTimeFormatter.ofPattern(getString(R.string.format_lms_date))

        CoroutineScope(Dispatchers.IO).launch {
            dataStore.data.map { it[Pref.Key.SET_REGISTER_ALERT] ?: Pref.Default.SET_REGISTER_ALERT }.collect { setRegisterAlert = it }
            dataStore.data.map { it[Pref.Key.DND_START_TIME] ?: Pref.Default.DND_START_TIME }.collect { dndStartTime = it }
            dataStore.data.map { it[Pref.Key.DND_END_TIME] ?: Pref.Default.DND_END_TIME }.collect { dndEndTime = it }
            dataStore.data.map { it[Pref.Key.DND_CHECK] ?: Pref.Default.DND_CHECK }.collect { isDndEnabled = it }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)

        val notification = sbn.notification
        val extras = notification.extras

        nm.createNotificationChannel(NotificationChannel(Notifications.Channel.REGISTER_ID, getString(R.string.channel_lms_register), NotificationManager.IMPORTANCE_HIGH))

        CoroutineScope(Dispatchers.IO).launch {
            if (sbn.packageName != LMS_PACKAGE_NAME) return@launch
            val className = extras.getString(Notification.EXTRA_TITLE) ?: ""
            val content = extras.getString(Notification.EXTRA_TEXT) ?: ""

            with (content) {
                when {
                    contains(getString(R.string.format_new_assignment)) || contains(getString(R.string.format_change_assignment)) -> parseContent(Lms.Type.HOMEWORK, className, this, sbn.postTime)
                    contains(getString(R.string.format_new_lecture)) || contains(getString(R.string.format_change_lecture)) -> parseContent(Lms.Type.LESSON, className, this, sbn.postTime)
                    contains(getString(R.string.format_new_sup_lecture)) || contains(getString(R.string.format_change_sup_lecture)) -> parseContent(Lms.Type.SUP_LESSON, className, this, sbn.postTime)
                    contains(getString(R.string.format_new_teamwork)) || contains(getString(R.string.format_change_teamwork)) -> parseContent(Lms.Type.TEAMWORK, className, this, sbn.postTime)
                    contains(getString(R.string.format_new_test)) || contains(getString(R.string.format_change_test)) -> parseContent(Lms.Type.EXAM, className, this, sbn.postTime)
                    contains(getString(R.string.format_new_zoom)) || contains(getString(R.string.format_change_zoom)) -> parseContent(Lms.Type.ZOOM, className, this, sbn.postTime)
                }
            }
        }

    }

    private suspend fun parseContent(type: Int, className: String, content: String, timestamp: Long) {
        val regex = when (type) {
            Lms.Type.HOMEWORK, Lms.Type.TEAMWORK, Lms.Type.EXAM -> getString(R.string.format_assignment_regex)
            Lms.Type.ZOOM -> getString(R.string.format_zoom_regex)
            Lms.Type.LESSON, Lms.Type.SUP_LESSON -> getString(R.string.format_lecture_regex)
            else -> ""
        }.toRegex()
        val matchResult = regex.matchEntire(content as CharSequence)

        var week = -1
        var lesson = -1
        var homeworkName = ""
        var startTime = parseLongToLocalDateTime(-1L)
        var endTime = parseLongToLocalDateTime(-1L)

        if (matchResult != null) {
            when (type) {
                Lms.Type.HOMEWORK, Lms.Type.TEAMWORK -> {
                    val (_homeworkName, _startTime, _endTime) = matchResult.destructured
                    homeworkName = _homeworkName
                    startTime = LocalDateTime.parse(_startTime, timeFormat)
                    endTime = LocalDateTime.parse(_endTime, timeFormat)
                }
                Lms.Type.LESSON, Lms.Type.SUP_LESSON -> {
                    val (_week, _lesson, _endTime) = matchResult.destructured
                    week = _week.toInt()
                    lesson = _lesson.toInt()
                    endTime = LocalDateTime.parse(_endTime, timeFormat)
                }
                Lms.Type.ZOOM -> {
                    val (_homeworkName, _endTime) = matchResult.destructured
                    homeworkName = _homeworkName
                    endTime = LocalDateTime.parse(_endTime, timeFormat)
                }
                Lms.Type.EXAM -> {
                    val (_homeworkName, _startTime, _endTime) = matchResult.destructured
                    homeworkName = _homeworkName
                    endTime = LocalDateTime.parse(_startTime, timeFormat)
                }
            }
        } else {
            return
        }

        val entity = Lms(className, timestamp, type, startTime.timeInMillis(), endTime.timeInMillis(), isRenewAllowed = true, isFinished = false, week, lesson, homeworkName)
        val prev = lmsRepository.getByDataStream(className, week, lesson, homeworkName).first()

        if (prev == null) {
            val id = lmsRepository.upsert(entity) ?: -1
            if (id != -1L) entity.id = id
        } else {
            entity.id = prev.id
            entity.isFinished = prev.isFinished
            entity.isRenewAllowed = prev.isRenewAllowed

            if (entity.isRenewAllowed) {
                lmsRepository.upsert(entity)
            }
        }

        setLmsSchedule(this, am, entity)

        if (setRegisterAlert) {
            val launchIntent = Intent(applicationContext, MainActivity::class.java).apply { putExtra(Intents.Key.ITEM_ID, entity.id) }
            val launchPending = PendingIntent.getActivity(applicationContext, PendingReq.REGISTER + (entity.id?.toInt() ?: 0), launchIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val deleteIntent = Intent(applicationContext, DeleteMissReceiver::class.java).apply { putExtra(Intents.Key.ITEM_ID, entity.id) }
            val deletePending = PendingIntent.getBroadcast(applicationContext, PendingReq.DELETE + (entity.id?.toInt() ?: 0), deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val noDnd = !(isDndEnabled && checkTimeRange(dndStartTime, dndEndTime, LocalDateTime.now().let { it.hour * 60 + it.minute }))

            val notification = NotificationCompat.Builder(applicationContext, Notifications.Channel.REGISTER_ID).apply {
                setContentTitle(className)
                setContentIntent(launchPending)
                setAutoCancel(true)
                setStyle(NotificationCompat.BigTextStyle())
                setSmallIcon(R.drawable.ic_icon)
                addAction(R.drawable.ic_delete, getString(R.string.deleted_noti), deletePending)
                color = ContextCompat.getColor(applicationContext, R.color.color_sogang)
            }
            val endDateTime = parseLongToLocalDateTime(entity.endTime)
            when (type) {
                Lms.Type.LESSON -> {
                    notification.setContentText(getString(if (prev == null) R.string.reminder_content_lec_register else R.string.reminder_content_lec_update, entity.week, entity.lesson, endDateTime.format(timeFormat)))
                }
                Lms.Type.SUP_LESSON -> {
                    notification.setContentText(getString(if (prev == null) R.string.reminder_content_sup_lec_register else R.string.reminder_content_sup_lec_update, entity.week, entity.lesson, endDateTime.format(timeFormat)))
                }
                Lms.Type.HOMEWORK -> {
                    notification.setContentText(getString(if (prev == null) R.string.reminder_content_hw_register else R.string.reminder_content_hw_update, entity.homework_name, endDateTime.format(timeFormat)))
                }
                Lms.Type.TEAMWORK -> {
                    notification.setContentText(getString(if (prev == null) R.string.reminder_content_team_register else R.string.reminder_content_team_update, entity.homework_name, endDateTime.format(timeFormat)))
                }
                Lms.Type.EXAM -> {
                    notification.setContentText(getString(if (prev == null) R.string.reminder_content_exam_register else R.string.reminder_content_exam_update, entity.homework_name, endDateTime.format(timeFormat)))
                }
                Lms.Type.ZOOM -> {
                    notification.setContentText(getString(if (prev == null) R.string.reminder_content_zoom_register else R.string.reminder_content_zoom_update, entity.homework_name, endDateTime.format(timeFormat)))
                }
            }

            if (noDnd) {
                nm.notify(Notifications.Id.REGISTER + (entity.id?.toInt() ?: 0), notification.build())
            }
        }
    }

    companion object {
        const val LMS_PACKAGE_NAME = "kr.co.imaxsoft.hellolms"
//        const val LMS_PACKAGE_NAME = "zone.ien.sogangassistnoti"
    }
}
