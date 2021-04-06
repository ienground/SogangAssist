package net.ienlab.sogangassist.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import net.ienlab.sogangassist.*
import net.ienlab.sogangassist.constant.ChannelId
import net.ienlab.sogangassist.constant.SharedKey
import net.ienlab.sogangassist.data.LMSClass
import net.ienlab.sogangassist.database.*
import net.ienlab.sogangassist.receiver.TimeReceiver
import net.ienlab.sogangassist.activity.*
import net.ienlab.sogangassist.constant.DefaultValue
import net.ienlab.sogangassist.data.NotificationItem
import net.ienlab.sogangassist.receiver.DeleteMissReceiver
import net.ienlab.sogangassist.receiver.SetReadReceiver
import net.ienlab.sogangassist.utils.MyUtils
import java.text.SimpleDateFormat
import java.util.*

class LMSListenerService : NotificationListenerService() {

    lateinit var pm: PackageManager
    lateinit var am: AlarmManager
    lateinit var nm: NotificationManager
    lateinit var sharedPreferences: SharedPreferences
    lateinit var dbHelper: DBHelper
    lateinit var notiDBHelper: NotiDBHelper

    override fun onCreate() {
        super.onCreate()
        pm = packageManager
        am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        dbHelper = DBHelper(this, DBHelper.dbName, DBHelper.dbVersion)
        notiDBHelper = NotiDBHelper(this, NotiDBHelper.dbName, NotiDBHelper.dbVersion)
        sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        val notification = sbn.notification
        val extras = notification.extras
        val timeFormat = SimpleDateFormat(getString(R.string.format_lms_date), Locale.getDefault())

        val hours = listOf(1, 2, 6, 12, 24)
        val minutes = listOf(3, 5, 10, 20, 30)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(ChannelId.DEFAULT_ID, getString(R.string.channel_name), NotificationManager.IMPORTANCE_HIGH)
            nm.createNotificationChannel(channel)
        }

        val dndStartData = sharedPreferences.getInt(SharedKey.DND_START_TIME, DefaultValue.DND_START_TIME)
        val dndEndData = sharedPreferences.getInt(SharedKey.DND_END_TIME, DefaultValue.DND_END_TIME)
        val now = Calendar.getInstance()
        val nowInt = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

//        if (sbn.packageName == "net.ienlab.notificationtest") {
        if (sbn.packageName == "kr.co.imaxsoft.hellolms") {
            val className = extras.getString(Notification.EXTRA_TITLE) ?: ""

            with (extras.getString(Notification.EXTRA_TEXT) ?: "") {
                when {
                    contains(getString(R.string.format_new_assignment)) -> {
                        val regex = getString(R.string.format_assignment_regex).toRegex()
                        val matchResult = regex.matchEntire(this as CharSequence)

                        if (matchResult != null) {
                            val (homework_name, startTime, endTime) = matchResult.destructured
                            val data = LMSClass(-1, className, sbn.postTime, LMSClass.TYPE_HOMEWORK, timeFormat.parse(startTime)?.time ?: 0L, timeFormat.parse(endTime)?.time ?: 0L, isRenewAllowed = true,isFinished = false, -1, -1,  homework_name)

                            if (!dbHelper.checkItemByData(data)) {
                                val id = dbHelper.addItem(data)
                                val notiIntent = Intent(applicationContext, TimeReceiver::class.java).apply { putExtra("ID", id) }

                                hours.forEachIndexed { index, i ->
                                    val triggerTime = data.endTime - i * 60 * 60 * 1000
                                    notiIntent.putExtra("TRIGGER", triggerTime)
                                    notiIntent.putExtra("TIME", i)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + index + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedKey.SET_REGISTER_ALERT, true)) {
                                    val notiId = notiDBHelper.addItem(NotificationItem(-1, className, getString(R.string.reminder_content_hw_register, data.homework_name, timeFormat.format(data.endTime)), System.currentTimeMillis(), NotificationItem.TYPE_REGISTER, id, false))
                                    val clickIntent = Intent(applicationContext, SplashActivity::class.java).apply { putExtra("ID", id) }
                                    val clickPendingIntent = PendingIntent.getActivity(applicationContext, id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    val deleteIntent = Intent(applicationContext, DeleteMissReceiver::class.java).apply { putExtra("ID", id) }
                                    val deletePendingIntent = PendingIntent.getBroadcast(applicationContext, id, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    val setReadIntent = Intent(applicationContext, SetReadReceiver::class.java).apply { putExtra("NOTI_ID", notiId); putExtra("CANCEL_ID", 699000 + id) }
                                    val setReadPendingIntent = PendingIntent.getBroadcast(applicationContext, notiId, setReadIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                                    NotificationCompat.Builder(applicationContext, ChannelId.DEFAULT_ID).apply {
                                        setContentTitle(className)
                                        setContentText(getString(R.string.reminder_content_hw_register, data.homework_name, timeFormat.format(data.endTime)))
                                        setContentIntent(clickPendingIntent)
                                        setAutoCancel(true)
                                        setStyle(NotificationCompat.BigTextStyle())
                                        setSmallIcon(R.drawable.ic_icon)
                                        addAction(R.drawable.ic_delete, getString(R.string.deleted_noti), deletePendingIntent)
                                        addAction(R.drawable.ic_mark_as_read, getString(R.string.mark_as_read), setReadPendingIntent)
                                        color = ContextCompat.getColor(applicationContext, R.color.colorAccent)

                                        if (!sharedPreferences.getBoolean(SharedKey.DND_CHECK, false) || (!MyUtils.isDNDTime(dndStartData, dndEndData, nowInt))) {
                                            nm.notify(699000 + id, build())
                                        }
                                    }
                                }
                            }
                        }
                    }
                    contains(getString(R.string.format_change_assignment)) -> {
                        val regex = getString(R.string.format_assignment_regex).toRegex()
                        val matchResult = regex.matchEntire(this as CharSequence)

                        if (matchResult != null) {
                            val (homework_name, startTime, endTime) = matchResult.destructured
                            val data = LMSClass(-1, className, sbn.postTime, LMSClass.TYPE_HOMEWORK, timeFormat.parse(startTime)?.time ?: 0L, timeFormat.parse(endTime)?.time ?: 0L, isRenewAllowed = true, isFinished = false, -1, -1, homework_name)

                            val id: Int
                            if (dbHelper.checkItemByData(data)) {
                                val oldItem = dbHelper.getItemById(dbHelper.getIdByCondition(data))
                                data.id = oldItem.id
                                data.isFinished = oldItem.isFinished
                                data.isRenewAllowed = oldItem.isRenewAllowed
                                if (oldItem.isRenewAllowed) {
                                    dbHelper.updateItem(data) // check
                                }
                                id = data.id
                            } else {
                                dbHelper.addItem(data)
                                id = dbHelper.getAllData().last().id
                            }

                            val notiIntent = Intent(applicationContext, TimeReceiver::class.java).apply { putExtra("ID", id) }

                            hours.forEachIndexed { index, i ->
                                val triggerTime = data.endTime - i * 60 * 60 * 1000
                                notiIntent.putExtra("TRIGGER", triggerTime)
                                notiIntent.putExtra("TIME", i)
                                val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + index + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                            }

                            if (sharedPreferences.getBoolean(SharedKey.SET_REGISTER_ALERT, true)) {
                                val notiId = notiDBHelper.addItem(NotificationItem(-1, className, getString(R.string.reminder_content_hw_update, data.homework_name, timeFormat.format(data.endTime)), System.currentTimeMillis(), NotificationItem.TYPE_REGISTER, id, false))
                                val clickIntent = Intent(applicationContext, SplashActivity::class.java).apply { putExtra("ID", id) }
                                val clickPendingIntent = PendingIntent.getActivity(applicationContext, id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                val deleteIntent = Intent(applicationContext, DeleteMissReceiver::class.java).apply { putExtra("ID", id) }
                                val deletePendingIntent = PendingIntent.getBroadcast(applicationContext, id, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                val setReadIntent = Intent(applicationContext, SetReadReceiver::class.java).apply { putExtra("NOTI_ID", notiId); putExtra("CANCEL_ID", 699000 + id) }
                                val setReadPendingIntent = PendingIntent.getBroadcast(applicationContext, notiId, setReadIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                                NotificationCompat.Builder(applicationContext, ChannelId.DEFAULT_ID).apply {
                                    setContentTitle(className)
                                    setContentText(getString(R.string.reminder_content_hw_update, data.homework_name, timeFormat.format(data.endTime)))
                                    setContentIntent(clickPendingIntent)
                                    setAutoCancel(true)
                                    setStyle(NotificationCompat.BigTextStyle())
                                    setSmallIcon(R.drawable.ic_icon)
                                    addAction(R.drawable.ic_delete, getString(R.string.deleted_noti), deletePendingIntent)
                                    addAction(R.drawable.ic_mark_as_read, getString(R.string.mark_as_read), setReadPendingIntent)
                                    color = ContextCompat.getColor(applicationContext, R.color.colorAccent)

                                    if (!sharedPreferences.getBoolean(SharedKey.DND_CHECK, false) || (!MyUtils.isDNDTime(dndStartData, dndEndData, nowInt))) {
                                        nm.notify(699000 + id, build())
                                    }
                                }
                            }
                        }
                    }
                    contains(getString(R.string.format_new_lecture)) -> {
                        val regex = getString(R.string.format_lecture_regex).toRegex()
                        val matchResult = regex.matchEntire(this as CharSequence)

                        if (matchResult != null) {
                            val (week, lesson, endTime) = matchResult.destructured
                            val data = LMSClass(-1, className, sbn.postTime, LMSClass.TYPE_LESSON, -1, timeFormat.parse(endTime)?.time ?: 0L, isRenewAllowed = false, isFinished = false, week.toInt(), lesson.toInt(), "#NONE")

                            if (!dbHelper.checkItemByData(data)) {
                                val id = dbHelper.addItem(data)
                                val notiIntent = Intent(applicationContext, TimeReceiver::class.java).apply { putExtra("ID", id) }

                                hours.forEachIndexed { index, i ->
                                    val triggerTime = data.endTime - i * 60 * 60 * 1000
                                    notiIntent.putExtra("TRIGGER", triggerTime)
                                    notiIntent.putExtra("TIME", i)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + index + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedKey.SET_REGISTER_ALERT, true)) {
                                    val notiId = notiDBHelper.addItem(NotificationItem(-1, className, getString(R.string.reminder_content_lec_register, data.week, data.lesson, timeFormat.format(data.endTime)), System.currentTimeMillis(), NotificationItem.TYPE_REGISTER, id, false))
                                    val clickIntent = Intent(applicationContext, SplashActivity::class.java).apply { putExtra("ID", id) }
                                    val clickPendingIntent = PendingIntent.getActivity(applicationContext, id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    val deleteIntent = Intent(applicationContext, DeleteMissReceiver::class.java).apply { putExtra("ID", id) }
                                    val deletePendingIntent = PendingIntent.getBroadcast(applicationContext, id, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    val setReadIntent = Intent(applicationContext, SetReadReceiver::class.java).apply { putExtra("NOTI_ID", notiId); putExtra("CANCEL_ID", 699000 + id) }
                                    val setReadPendingIntent = PendingIntent.getBroadcast(applicationContext, notiId, setReadIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                                    NotificationCompat.Builder(applicationContext, ChannelId.DEFAULT_ID).apply {
                                        setContentTitle(className)
                                        setContentText(getString(R.string.reminder_content_lec_register, data.week, data.lesson, timeFormat.format(data.endTime)))
                                        setContentIntent(clickPendingIntent)
                                        setAutoCancel(true)
                                        setStyle(NotificationCompat.BigTextStyle())
                                        setSmallIcon(R.drawable.ic_icon)
                                        addAction(R.drawable.ic_delete, getString(R.string.deleted_noti), deletePendingIntent)
                                        addAction(R.drawable.ic_mark_as_read, getString(R.string.mark_as_read), setReadPendingIntent)
                                        color = ContextCompat.getColor(applicationContext, R.color.colorAccent)

                                        if (!sharedPreferences.getBoolean(SharedKey.DND_CHECK, false) || (!MyUtils.isDNDTime(dndStartData, dndEndData, nowInt))) {
                                            nm.notify(699000 + id, build())
                                        }
                                    }
                                }
                            }
                        }
                    }
                    contains(getString(R.string.format_change_lecture)) -> {
                        val regex = getString(R.string.format_lecture_regex).toRegex()
                        val matchResult = regex.matchEntire(this as CharSequence)

                        if (matchResult != null) {
                            val (week, lesson, endTime) = matchResult.destructured
                            val data = LMSClass(-1, className, sbn.postTime, LMSClass.TYPE_LESSON, -1, timeFormat.parse(endTime)?.time ?: 0L, isRenewAllowed = true, isFinished = false, week.toInt(), lesson.toInt(), "#NONE")

                            val id: Int
                            if (dbHelper.checkItemByData(data)) {
                                val oldItem = dbHelper.getItemById(dbHelper.getIdByCondition(data))
                                data.id = oldItem.id
                                data.isFinished = oldItem.isFinished
                                data.isRenewAllowed = oldItem.isRenewAllowed
                                if (oldItem.isRenewAllowed) {
                                    dbHelper.updateItem(data) // check
                                }
                                id = data.id
                            } else {
                                dbHelper.addItem(data)
                                id = dbHelper.getAllData().last().id
                            }

                            val notiIntent = Intent(applicationContext, TimeReceiver::class.java).apply { putExtra("ID", id) }

                            hours.forEachIndexed { index, i ->
                                val triggerTime = data.endTime - i * 60 * 60 * 1000
                                notiIntent.putExtra("TRIGGER", triggerTime)
                                notiIntent.putExtra("TIME", i)
                                val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + index + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                            }

                            if (sharedPreferences.getBoolean(SharedKey.SET_REGISTER_ALERT, true)) {
                                val notiId = notiDBHelper.addItem(NotificationItem(-1, className, getString(R.string.reminder_content_lec_update, data.week, data.lesson, timeFormat.format(data.endTime)), System.currentTimeMillis(), NotificationItem.TYPE_REGISTER, id, false))
                                val clickIntent = Intent(applicationContext, SplashActivity::class.java).apply { putExtra("ID", id) }
                                val clickPendingIntent = PendingIntent.getActivity(applicationContext, id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                val deleteIntent = Intent(applicationContext, DeleteMissReceiver::class.java).apply { putExtra("ID", id) }
                                val deletePendingIntent = PendingIntent.getBroadcast(applicationContext, id, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                val setReadIntent = Intent(applicationContext, SetReadReceiver::class.java).apply { putExtra("NOTI_ID", notiId); putExtra("CANCEL_ID", 699000 + id) }
                                val setReadPendingIntent = PendingIntent.getBroadcast(applicationContext, notiId, setReadIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                                NotificationCompat.Builder(applicationContext, ChannelId.DEFAULT_ID).apply {
                                    setContentTitle(className)
                                    setContentText(getString(R.string.reminder_content_lec_update, data.week, data.lesson, timeFormat.format(data.endTime)))
                                    setContentIntent(clickPendingIntent)
                                    setAutoCancel(true)
                                    setStyle(NotificationCompat.BigTextStyle())
                                    setSmallIcon(R.drawable.ic_icon)
                                    addAction(R.drawable.ic_delete, getString(R.string.deleted_noti), deletePendingIntent)
                                    addAction(R.drawable.ic_mark_as_read, getString(R.string.mark_as_read), setReadPendingIntent)
                                    color = ContextCompat.getColor(applicationContext, R.color.colorAccent)

                                    if (!sharedPreferences.getBoolean(SharedKey.DND_CHECK, false) || (!MyUtils.isDNDTime(dndStartData, dndEndData, nowInt))) {
                                        nm.notify(699000 + id, build())
                                    }
                                }
                            }

                        }
                    }
                    contains(getString(R.string.format_new_sup_lecture)) -> {
                        val regex = getString(R.string.format_lecture_regex).toRegex()
                        val matchResult = regex.matchEntire(this as CharSequence)

                        if (matchResult != null) {
                            val (week, lesson, endTime) = matchResult.destructured
                            val data = LMSClass(-1, className, sbn.postTime, LMSClass.TYPE_SUP_LESSON, -1, timeFormat.parse(endTime)?.time ?: 0L, isRenewAllowed = false, isFinished = false, week.toInt(), lesson.toInt(), "#NONE")

                            val id = dbHelper.addItem(data)
                            val notiIntent = Intent(applicationContext, TimeReceiver::class.java).apply { putExtra("ID", id) }

                            hours.forEachIndexed { index, i ->
                                val triggerTime = data.endTime - i * 60 * 60 * 1000
                                notiIntent.putExtra("TRIGGER", triggerTime)
                                notiIntent.putExtra("TIME", i)
                                val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + index + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                            }

                            if (sharedPreferences.getBoolean(SharedKey.SET_REGISTER_ALERT, true)) {
                                val notiId = notiDBHelper.addItem(NotificationItem(-1, className, getString(R.string.reminder_content_sup_lec_register, data.week, data.lesson, timeFormat.format(data.endTime)), System.currentTimeMillis(), NotificationItem.TYPE_REGISTER, id, false))
                                val clickIntent = Intent(applicationContext, SplashActivity::class.java).apply { putExtra("ID", id) }
                                val clickPendingIntent = PendingIntent.getActivity(applicationContext, id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                val deleteIntent = Intent(applicationContext, DeleteMissReceiver::class.java).apply { putExtra("ID", id) }
                                val deletePendingIntent = PendingIntent.getBroadcast(applicationContext, id, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                val setReadIntent = Intent(applicationContext, SetReadReceiver::class.java).apply { putExtra("NOTI_ID", notiId); putExtra("CANCEL_ID", 699000 + id) }
                                val setReadPendingIntent = PendingIntent.getBroadcast(applicationContext, notiId, setReadIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                                NotificationCompat.Builder(applicationContext, ChannelId.DEFAULT_ID).apply {
                                    setContentTitle(className)
                                    setContentText(getString(R.string.reminder_content_sup_lec_register, data.week, data.lesson, timeFormat.format(data.endTime)))
                                    setContentIntent(clickPendingIntent)
                                    setAutoCancel(true)
                                    setStyle(NotificationCompat.BigTextStyle())
                                    setSmallIcon(R.drawable.ic_icon)
                                    addAction(R.drawable.ic_delete, getString(R.string.deleted_noti), deletePendingIntent)
                                    addAction(R.drawable.ic_mark_as_read, getString(R.string.mark_as_read), setReadPendingIntent)
                                    color = ContextCompat.getColor(applicationContext, R.color.colorAccent)

                                    if (!sharedPreferences.getBoolean(SharedKey.DND_CHECK, false) || (!MyUtils.isDNDTime(dndStartData, dndEndData, nowInt))) {
                                        nm.notify(699000 + id, build())
                                    }
                                }
                            }
                        }
                    }
                    contains(getString(R.string.format_change_sup_lecture)) -> {
                        val regex = getString(R.string.format_lecture_regex).toRegex()
                        val matchResult = regex.matchEntire(this as CharSequence)

                        if (matchResult != null) {
                            val (week, lesson, endTime) = matchResult.destructured
                            val data = LMSClass(-1, className, sbn.postTime, LMSClass.TYPE_SUP_LESSON, -1, timeFormat.parse(endTime)?.time ?: 0L, isRenewAllowed = true, isFinished = false, week.toInt(), lesson.toInt(), "#NONE")

                            val id: Int
                            if (dbHelper.checkItemByData(data)) {
                                val oldItem = dbHelper.getItemById(dbHelper.getIdByCondition(data))
                                data.id = oldItem.id
                                data.isFinished = oldItem.isFinished
                                data.isRenewAllowed = oldItem.isRenewAllowed
                                if (oldItem.isRenewAllowed) {
                                    dbHelper.updateItem(data) // check
                                }
                                id = data.id
                            } else {
                                dbHelper.addItem(data)
                                id = dbHelper.getAllData().last().id
                            }

                            val notiIntent = Intent(applicationContext, TimeReceiver::class.java).apply { putExtra("ID", id) }

                            hours.forEachIndexed { index, i ->
                                val triggerTime = data.endTime - i * 60 * 60 * 1000
                                notiIntent.putExtra("TRIGGER", triggerTime)
                                notiIntent.putExtra("TIME", i)
                                val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + index + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                            }

                            if (sharedPreferences.getBoolean(SharedKey.SET_REGISTER_ALERT, true)) {
                                val notiId = notiDBHelper.addItem(NotificationItem(-1, className, getString(R.string.reminder_content_sup_lec_update, data.week, data.lesson, timeFormat.format(data.endTime)), System.currentTimeMillis(), NotificationItem.TYPE_REGISTER, id, false))
                                val clickIntent = Intent(applicationContext, SplashActivity::class.java).apply { putExtra("ID", id) }
                                val clickPendingIntent = PendingIntent.getActivity(applicationContext, id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                val deleteIntent = Intent(applicationContext, DeleteMissReceiver::class.java).apply { putExtra("ID", id) }
                                val deletePendingIntent = PendingIntent.getBroadcast(applicationContext, id, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                val setReadIntent = Intent(applicationContext, SetReadReceiver::class.java).apply { putExtra("NOTI_ID", notiId); putExtra("CANCEL_ID", 699000 + id) }
                                val setReadPendingIntent = PendingIntent.getBroadcast(applicationContext, notiId, setReadIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                                NotificationCompat.Builder(applicationContext, ChannelId.DEFAULT_ID).apply {
                                    setContentTitle(className)
                                    setContentText(getString(R.string.reminder_content_sup_lec_update, data.week, data.lesson, timeFormat.format(data.endTime)))
                                    setContentIntent(clickPendingIntent)
                                    setAutoCancel(true)
                                    setStyle(NotificationCompat.BigTextStyle())
                                    setSmallIcon(R.drawable.ic_icon)
                                    addAction(R.drawable.ic_delete, getString(R.string.deleted_noti), deletePendingIntent)
                                    addAction(R.drawable.ic_mark_as_read, getString(R.string.mark_as_read), setReadPendingIntent)
                                    color = ContextCompat.getColor(applicationContext, R.color.colorAccent)

                                    if (!sharedPreferences.getBoolean(SharedKey.DND_CHECK, false) || (!MyUtils.isDNDTime(dndStartData, dndEndData, nowInt))) {
                                        nm.notify(699000 + id, build())
                                    }
                                }
                            }
                        }
                    }
                    contains(getString(R.string.format_new_zoom)) -> {
                        val regex = getString(R.string.format_zoom_regex).toRegex()
                        val matchResult = regex.matchEntire(this as CharSequence)

                        if (matchResult != null) {
                            val (homework_name, endTime) = matchResult.destructured
                            val data = LMSClass(-1, className, sbn.postTime, LMSClass.TYPE_ZOOM, 0L, timeFormat.parse(endTime)?.time ?: 0L, isRenewAllowed = true, isFinished = false, -1, -1, homework_name)

                            val id = dbHelper.addItem(data)
                            val notiIntent = Intent(applicationContext, TimeReceiver::class.java).apply { putExtra("ID", id) }

                            minutes.forEachIndexed { index, i ->
                                val triggerTime = data.endTime - i * 60 * 1000
                                notiIntent.putExtra("TRIGGER", triggerTime)
                                notiIntent.putExtra("MINUTE", i)
                                val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + index + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                            }

                            if (sharedPreferences.getBoolean(SharedKey.SET_REGISTER_ALERT, true)) {
                                val notiId = notiDBHelper.addItem(NotificationItem(-1, className, getString(R.string.reminder_content_zoom_register, data.homework_name, timeFormat.format(data.endTime)), System.currentTimeMillis(), NotificationItem.TYPE_REGISTER, id, false))
                                val clickIntent = Intent(applicationContext, SplashActivity::class.java).apply { putExtra("ID", id) }
                                val clickPendingIntent = PendingIntent.getActivity(applicationContext, id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                val deleteIntent = Intent(applicationContext, DeleteMissReceiver::class.java).apply { putExtra("ID", id) }
                                val deletePendingIntent = PendingIntent.getBroadcast(applicationContext, id, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                val setReadIntent = Intent(applicationContext, SetReadReceiver::class.java).apply { putExtra("NOTI_ID", notiId); putExtra("CANCEL_ID", 699000 + id) }
                                val setReadPendingIntent = PendingIntent.getBroadcast(applicationContext, notiId, setReadIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                                NotificationCompat.Builder(applicationContext, ChannelId.DEFAULT_ID).apply {
                                    setContentTitle(className)
                                    setContentText(getString(R.string.reminder_content_zoom_register, data.homework_name, timeFormat.format(data.endTime)))
                                    setContentIntent(clickPendingIntent)
                                    setAutoCancel(true)
                                    setStyle(NotificationCompat.BigTextStyle())
                                    setSmallIcon(R.drawable.ic_icon)
                                    addAction(R.drawable.ic_delete, getString(R.string.deleted_noti), deletePendingIntent)
                                    addAction(R.drawable.ic_mark_as_read, getString(R.string.mark_as_read), setReadPendingIntent)
                                    color = ContextCompat.getColor(applicationContext, R.color.colorAccent)

                                    if (!sharedPreferences.getBoolean(SharedKey.DND_CHECK, false) || (!MyUtils.isDNDTime(dndStartData, dndEndData, nowInt))) {
                                        nm.notify(699000 + id, build())
                                    }
                                }
                            }
                        }
                    }
                    contains(getString(R.string.format_change_zoom)) -> {
                        val regex = getString(R.string.format_zoom_regex).toRegex()
                        val matchResult = regex.matchEntire(this as CharSequence)

                        if (matchResult != null) {
                            val (homework_name, endTime) = matchResult.destructured
                            val data = LMSClass(-1, className, sbn.postTime, LMSClass.TYPE_ZOOM, 0L, timeFormat.parse(endTime)?.time ?: 0L, isRenewAllowed = true, isFinished = false, -1, -1, homework_name)

                            val id: Int
                            if (dbHelper.checkItemByData(data)) {
                                val oldItem = dbHelper.getItemById(dbHelper.getIdByCondition(data))
                                data.id = oldItem.id
                                data.isFinished = oldItem.isFinished
                                data.isRenewAllowed = oldItem.isRenewAllowed
                                if (oldItem.isRenewAllowed) {
                                    dbHelper.updateItem(data) // check
                                }
                                id = data.id
                            } else {
                                dbHelper.addItem(data)
                                id = dbHelper.getAllData().last().id
                            }

                            val notiIntent = Intent(applicationContext, TimeReceiver::class.java).apply { putExtra("ID", id) }

                            minutes.forEachIndexed { index, i ->
                                val triggerTime = data.endTime - i * 60 * 1000
                                notiIntent.putExtra("TRIGGER", triggerTime)
                                notiIntent.putExtra("MINUTE", i)
                                val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + index + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                            }

                            if (sharedPreferences.getBoolean(SharedKey.SET_REGISTER_ALERT, true)) {
                                val notiId = notiDBHelper.addItem(NotificationItem(-1, className, getString(R.string.reminder_content_zoom_update, data.homework_name, timeFormat.format(data.endTime)), System.currentTimeMillis(), NotificationItem.TYPE_REGISTER, id, false))
                                val clickIntent = Intent(applicationContext, SplashActivity::class.java).apply { putExtra("ID", id) }
                                val clickPendingIntent = PendingIntent.getActivity(applicationContext, id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                val deleteIntent = Intent(applicationContext, DeleteMissReceiver::class.java).apply { putExtra("ID", id) }
                                val deletePendingIntent = PendingIntent.getBroadcast(applicationContext, id, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                val setReadIntent = Intent(applicationContext, SetReadReceiver::class.java).apply { putExtra("NOTI_ID", notiId); putExtra("CANCEL_ID", 699000 + id) }
                                val setReadPendingIntent = PendingIntent.getBroadcast(applicationContext, notiId, setReadIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                                NotificationCompat.Builder(applicationContext, ChannelId.DEFAULT_ID).apply {
                                    setContentTitle(className)
                                    setContentText(getString(R.string.reminder_content_zoom_update, data.homework_name, timeFormat.format(data.endTime)))
                                    setContentIntent(clickPendingIntent)
                                    setAutoCancel(true)
                                    setStyle(NotificationCompat.BigTextStyle())
                                    setSmallIcon(R.drawable.ic_icon)
                                    addAction(R.drawable.ic_delete, getString(R.string.deleted_noti), deletePendingIntent)
                                    addAction(R.drawable.ic_mark_as_read, getString(R.string.mark_as_read), setReadPendingIntent)
                                    color = ContextCompat.getColor(applicationContext, R.color.colorAccent)

                                    if (!sharedPreferences.getBoolean(SharedKey.DND_CHECK, false) || (!MyUtils.isDNDTime(dndStartData, dndEndData, nowInt))) {
                                        nm.notify(699000 + id, build())
                                    }
                                }
                            }
                        }
                    }
                    else -> {

                    }
                }
            }
        }
    }
}
