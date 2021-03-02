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
import net.ienlab.sogangassist.constant.SharedGroup
import net.ienlab.sogangassist.data.LMSClass
import net.ienlab.sogangassist.receiver.TimeReceiver
import java.text.SimpleDateFormat
import java.util.*

class LMSListenerService : NotificationListenerService() {

    lateinit var pm: PackageManager
    lateinit var am: AlarmManager
    lateinit var nm: NotificationManager
    lateinit var sharedPreferences: SharedPreferences
    lateinit var dbHelper: DBHelper

    override fun onCreate() {
        super.onCreate()
        pm = packageManager
        am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        dbHelper = DBHelper(this, dbName, dbVersion)
        sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        val notification = sbn.notification
        val extras = notification.extras
        val timeFormat = SimpleDateFormat(getString(R.string.format_lms_date), Locale.getDefault())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(ChannelId.DEFAULT_ID, getString(R.string.channel_name), NotificationManager.IMPORTANCE_HIGH)
            nm.createNotificationChannel(channel)
        }

        if (sbn.packageName == "kr.co.imaxsoft.hellolms") {
            val className = extras.getString(Notification.EXTRA_TITLE) ?: ""

            with (extras.getString(Notification.EXTRA_TEXT) ?: "") {
                when {
                    contains(getString(R.string.format_new_assignment)) -> {
                        val regex = getString(R.string.format_assignment_regex).toRegex()
                        val matchResult = regex.matchEntire(this as CharSequence)

                        if (matchResult != null) {
                            val (homework_name, startTime, endTime) = matchResult.destructured
                            val data = LMSClass(-1, className, sbn.postTime, LMSClass.HOMEWORK, timeFormat.parse(startTime)?.time ?: 0L, timeFormat.parse(endTime)?.time ?: 0L, isRenewAllowed = true,isFinished = false, -1, -1,  homework_name)

                            if (!dbHelper.checkItemByData(data)) {
                                dbHelper.addItem(data)

                                val notiIntent = Intent(applicationContext, TimeReceiver::class.java)
                                val id = dbHelper.getAllData().last().id
                                notiIntent.putExtra("ID", id)

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_1HOUR_HW, false)) {
                                    val triggerTime = data.endTime - 1 * 60 * 60 * 1000
                                    notiIntent.putExtra("TRIGGER", triggerTime)
                                    notiIntent.putExtra("TIME", 1)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_2HOUR_HW, false)) {
                                    val triggerTime = data.endTime - 2 * 60 * 60 * 1000
                                    notiIntent.putExtra("TRIGGER", triggerTime)
                                    notiIntent.putExtra("TIME", 2)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 2, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_6HOUR_HW, false)) {
                                    val triggerTime = data.endTime - 6 * 60 * 60 * 1000
                                    notiIntent.putExtra("TRIGGER", triggerTime)
                                    notiIntent.putExtra("TIME", 6)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 3, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_12HOUR_HW, false)) {
                                    val triggerTime = data.endTime - 12 * 60 * 60 * 1000
                                    notiIntent.putExtra("TRIGGER", triggerTime)
                                    notiIntent.putExtra("TIME", 12)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 4, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_24HOUR_HW, false)) {
                                    val triggerTime = data.endTime - 24 * 60 * 60 * 1000
                                    notiIntent.putExtra("TRIGGER", triggerTime)
                                    notiIntent.putExtra("TIME", 24)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 5, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.SET_REGISTER_ALERT, true)) {
                                    val clickIntent = Intent(applicationContext, SplashActivity::class.java).apply { putExtra("ID", id) }
                                    val clickPendingIntent = PendingIntent.getActivity(applicationContext, id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                                    NotificationCompat.Builder(applicationContext, ChannelId.DEFAULT_ID).apply {
                                        setContentTitle(className)
                                        setContentText(getString(R.string.reminder_content_hw_register, data.homework_name, timeFormat.format(data.endTime)))
                                        setContentIntent(clickPendingIntent)
                                        setAutoCancel(true)
                                        setStyle(NotificationCompat.BigTextStyle())
                                        setSmallIcon(R.drawable.ic_icon)
                                        color = ContextCompat.getColor(applicationContext, R.color.colorAccent)

                                        nm.notify(699000 + id, build())
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
                            val data = LMSClass(-1, className, sbn.postTime, LMSClass.HOMEWORK, timeFormat.parse(startTime)?.time ?: 0L, timeFormat.parse(endTime)?.time ?: 0L, isRenewAllowed = true, isFinished = false, -1, -1, homework_name)

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

                            val notiIntent = Intent(applicationContext, TimeReceiver::class.java)
                            notiIntent.putExtra("ID", id)

                            if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_1HOUR_HW, false)) {
                                val triggerTime = data.endTime - 1 * 60 * 60 * 1000
                                notiIntent.putExtra("TRIGGER", triggerTime)
                                notiIntent.putExtra("TIME", 1)
                                val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                            }

                            if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_2HOUR_HW, false)) {
                                val triggerTime = data.endTime - 2 * 60 * 60 * 1000
                                notiIntent.putExtra("TRIGGER", triggerTime)
                                notiIntent.putExtra("TIME", 2)
                                val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 2, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                            }

                            if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_6HOUR_HW, false)) {
                                val triggerTime = data.endTime - 6 * 60 * 60 * 1000
                                notiIntent.putExtra("TRIGGER", triggerTime)
                                notiIntent.putExtra("TIME", 6)
                                val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 3, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                            }

                            if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_12HOUR_HW, false)) {
                                val triggerTime = data.endTime - 12 * 60 * 60 * 1000
                                notiIntent.putExtra("TRIGGER", triggerTime)
                                notiIntent.putExtra("TIME", 12)
                                val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 4, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                            }

                            if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_24HOUR_HW, false)) {
                                val triggerTime = data.endTime - 24 * 60 * 60 * 1000
                                notiIntent.putExtra("TRIGGER", triggerTime)
                                notiIntent.putExtra("TIME", 24)
                                val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 5, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                            }

                            if (sharedPreferences.getBoolean(SharedGroup.SET_REGISTER_ALERT, true)) {
                                val clickIntent = Intent(applicationContext, SplashActivity::class.java).apply { putExtra("ID", id) }
                                val clickPendingIntent = PendingIntent.getActivity(applicationContext, id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                                NotificationCompat.Builder(applicationContext, ChannelId.DEFAULT_ID).apply {
                                    setContentTitle(className)
                                    setContentText(getString(R.string.reminder_content_hw_update, data.homework_name, timeFormat.format(data.endTime)))
                                    setContentIntent(clickPendingIntent)
                                    setAutoCancel(true)
                                    setStyle(NotificationCompat.BigTextStyle())
                                    setSmallIcon(R.drawable.ic_icon)
                                    setColor(ContextCompat.getColor(applicationContext,
                                        R.color.colorAccent
                                    ))

                                    nm.notify(699000 + id, build())
                                }
                            }
                        }
                    }
                    contains(getString(R.string.format_new_lecture)) -> {
                        val regex = getString(R.string.format_lecture_regex).toRegex()
                        val matchResult = regex.matchEntire(this as CharSequence)

                        if (matchResult != null) {
                            val (week, lesson, endTime) = matchResult.destructured
                            val data = LMSClass(-1, className, sbn.postTime, LMSClass.LESSON, -1, timeFormat.parse(endTime)?.time ?: 0L, isRenewAllowed = false, isFinished = false, week.toInt(), lesson.toInt(), "#NONE")

                            if (!dbHelper.checkItemByData(data)) {
                                dbHelper.addItem(data)

                                val notiIntent = Intent(applicationContext, TimeReceiver::class.java)
                                val id = dbHelper.getAllData().last().id
                                notiIntent.putExtra("ID", id)

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_1HOUR_LEC, false)) {
                                    val triggerTime = data.endTime - 1 * 60 * 60 * 1000
                                    notiIntent.putExtra("TRIGGER", triggerTime)
                                    notiIntent.putExtra("TIME", 1)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_2HOUR_LEC, false)) {
                                    val triggerTime = data.endTime - 2 * 60 * 60 * 1000
                                    notiIntent.putExtra("TRIGGER", triggerTime)
                                    notiIntent.putExtra("TIME", 2)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 2, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_6HOUR_LEC, false)) {
                                    val triggerTime = data.endTime - 6 * 60 * 60 * 1000
                                    notiIntent.putExtra("TRIGGER", triggerTime)
                                    notiIntent.putExtra("TIME", 6)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 3, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_12HOUR_LEC, false)) {
                                    val triggerTime = data.endTime - 12 * 60 * 60 * 1000
                                    notiIntent.putExtra("TRIGGER", triggerTime)
                                    notiIntent.putExtra("TIME", 12)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 4, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_24HOUR_LEC, false)) {
                                    val triggerTime = data.endTime - 24 * 60 * 60 * 1000
                                    notiIntent.putExtra("TRIGGER", triggerTime)
                                    notiIntent.putExtra("TIME", 24)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 5, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.SET_REGISTER_ALERT, true)) {
                                    val clickIntent = Intent(applicationContext, SplashActivity::class.java).apply { putExtra("ID", id) }
                                    val clickPendingIntent = PendingIntent.getActivity(applicationContext, id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                                    NotificationCompat.Builder(applicationContext, ChannelId.DEFAULT_ID).apply {
                                        setContentTitle(className)
                                        setContentText(getString(R.string.reminder_content_lec_register, data.week, data.lesson, timeFormat.format(data.endTime)))
                                        setContentIntent(clickPendingIntent)
                                        setAutoCancel(true)
                                        setStyle(NotificationCompat.BigTextStyle())
                                        setSmallIcon(R.drawable.ic_icon)
                                        setColor(ContextCompat.getColor(applicationContext,
                                            R.color.colorAccent
                                        ))

                                        nm.notify(699000 + id, build())
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
                            val data = LMSClass(-1, className, sbn.postTime, LMSClass.LESSON, -1, timeFormat.parse(endTime)?.time ?: 0L, isRenewAllowed = true, isFinished = false, week.toInt(), lesson.toInt(), "#NONE")

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

                            if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_1HOUR_LEC, false)) {
                                val triggerTime = data.endTime - 1 * 60 * 60 * 1000
                                notiIntent.putExtra("TRIGGER", triggerTime)
                                notiIntent.putExtra("TIME", 1)
                                val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                            }

                            if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_2HOUR_LEC, false)) {
                                val triggerTime = data.endTime - 2 * 60 * 60 * 1000
                                notiIntent.putExtra("TRIGGER", triggerTime)
                                notiIntent.putExtra("TIME", 2)
                                val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 2, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                            }

                            if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_6HOUR_LEC, false)) {
                                val triggerTime = data.endTime - 6 * 60 * 60 * 1000
                                notiIntent.putExtra("TRIGGER", triggerTime)
                                notiIntent.putExtra("TIME", 6)
                                val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 3, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                            }

                            if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_12HOUR_LEC, false)) {
                                val triggerTime = data.endTime - 12 * 60 * 60 * 1000
                                notiIntent.putExtra("TRIGGER", triggerTime)
                                notiIntent.putExtra("TIME", 12)
                                val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 4, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                            }

                            if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_24HOUR_LEC, false)) {
                                val triggerTime = data.endTime - 24 * 60 * 60 * 1000
                                notiIntent.putExtra("TRIGGER", triggerTime)
                                notiIntent.putExtra("TIME", 24)
                                val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 5, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                            }

                            if (sharedPreferences.getBoolean(SharedGroup.SET_REGISTER_ALERT, true)) {
                                val clickIntent = Intent(applicationContext, SplashActivity::class.java).apply { putExtra("ID", id) }
                                val clickPendingIntent = PendingIntent.getActivity(applicationContext, id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                                NotificationCompat.Builder(applicationContext, ChannelId.DEFAULT_ID).apply {
                                    setContentTitle(className)
                                    setContentText(getString(R.string.reminder_content_lec_update, data.week, data.lesson, timeFormat.format(data.endTime)))
                                    setContentIntent(clickPendingIntent)
                                    setAutoCancel(true)
                                    setStyle(NotificationCompat.BigTextStyle())
                                    setSmallIcon(R.drawable.ic_icon)
                                    color = ContextCompat.getColor(applicationContext, R.color.colorAccent)

                                    nm.notify(699000 + id, build())
                                }
                            }

                        }
                    }
                    contains(getString(R.string.format_new_sup_lecture)) -> {
                        val regex = getString(R.string.format_lecture_regex).toRegex()
                        val matchResult = regex.matchEntire(this as CharSequence)

                        if (matchResult != null) {
                            val (week, lesson, endTime) = matchResult.destructured
                            val data = LMSClass(-1, className, sbn.postTime, LMSClass.SUP_LESSON, -1, timeFormat.parse(endTime)?.time ?: 0L, isRenewAllowed = false, isFinished = false, week.toInt(), lesson.toInt(), "#NONE")

                            dbHelper.addItem(data)

                            val notiIntent = Intent(applicationContext, TimeReceiver::class.java)
                            val id = dbHelper.getAllData().last().id
                            notiIntent.putExtra("ID", id)

                            if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_1HOUR_LEC, false)) {
                                val triggerTime = data.endTime - 1 * 60 * 60 * 1000
                                notiIntent.putExtra("TRIGGER", triggerTime)
                                notiIntent.putExtra("TIME", 1)
                                val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                            }

                            if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_2HOUR_LEC, false)) {
                                val triggerTime = data.endTime - 2 * 60 * 60 * 1000
                                notiIntent.putExtra("TRIGGER", triggerTime)
                                notiIntent.putExtra("TIME", 2)
                                val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 2, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                            }

                            if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_6HOUR_LEC, false)) {
                                val triggerTime = data.endTime - 6 * 60 * 60 * 1000
                                notiIntent.putExtra("TRIGGER", triggerTime)
                                notiIntent.putExtra("TIME", 6)
                                val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 3, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                            }

                            if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_12HOUR_LEC, false)) {
                                val triggerTime = data.endTime - 12 * 60 * 60 * 1000
                                notiIntent.putExtra("TRIGGER", triggerTime)
                                notiIntent.putExtra("TIME", 12)
                                val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 4, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                            }

                            if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_24HOUR_LEC, false)) {
                                val triggerTime = data.endTime - 24 * 60 * 60 * 1000
                                notiIntent.putExtra("TRIGGER", triggerTime)
                                notiIntent.putExtra("TIME", 24)
                                val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 5, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                            }

                            if (sharedPreferences.getBoolean(SharedGroup.SET_REGISTER_ALERT, true)) {
                                val clickIntent = Intent(applicationContext, SplashActivity::class.java).apply { putExtra("ID", id) }
                                val clickPendingIntent = PendingIntent.getActivity(applicationContext, id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                                NotificationCompat.Builder(applicationContext, ChannelId.DEFAULT_ID).apply {
                                    setContentTitle(className)
                                    setContentText(getString(R.string.reminder_content_sup_lec_register, data.week, data.lesson, timeFormat.format(data.endTime)))
                                    setContentIntent(clickPendingIntent)
                                    setAutoCancel(true)
                                    setStyle(NotificationCompat.BigTextStyle())
                                    setSmallIcon(R.drawable.ic_icon)
                                    color = ContextCompat.getColor(applicationContext, R.color.colorAccent)

                                    nm.notify(699000 + id, build())
                                }
                            }
                        }
                    }
                    contains(getString(R.string.format_change_sup_lecture)) -> {
                        val regex = getString(R.string.format_lecture_regex).toRegex()
                        val matchResult = regex.matchEntire(this as CharSequence)

                        if (matchResult != null) {
                            val (week, lesson, endTime) = matchResult.destructured
                            val data = LMSClass(-1, className, sbn.postTime, LMSClass.SUP_LESSON, -1, timeFormat.parse(endTime)?.time ?: 0L, isRenewAllowed = true, isFinished = false, week.toInt(), lesson.toInt(), "#NONE")

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

                            val notiIntent = Intent(applicationContext, TimeReceiver::class.java)
                            notiIntent.putExtra("ID", id)

                            if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_1HOUR_LEC, false)) {
                                val triggerTime = data.endTime - 1 * 60 * 60 * 1000
                                notiIntent.putExtra("TRIGGER", triggerTime)
                                notiIntent.putExtra("TIME", 1)
                                val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                            }

                            if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_2HOUR_LEC, false)) {
                                val triggerTime = data.endTime - 2 * 60 * 60 * 1000
                                notiIntent.putExtra("TRIGGER", triggerTime)
                                notiIntent.putExtra("TIME", 2)
                                val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 2, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                            }

                            if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_6HOUR_LEC, false)) {
                                val triggerTime = data.endTime - 6 * 60 * 60 * 1000
                                notiIntent.putExtra("TRIGGER", triggerTime)
                                notiIntent.putExtra("TIME", 6)
                                val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 3, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                            }

                            if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_12HOUR_LEC, false)) {
                                val triggerTime = data.endTime - 12 * 60 * 60 * 1000
                                notiIntent.putExtra("TRIGGER", triggerTime)
                                notiIntent.putExtra("TIME", 12)
                                val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 4, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                            }

                            if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_24HOUR_LEC, false)) {
                                val triggerTime = data.endTime - 24 * 60 * 60 * 1000
                                notiIntent.putExtra("TRIGGER", triggerTime)
                                notiIntent.putExtra("TIME", 24)
                                val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 5, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                            }

                            if (sharedPreferences.getBoolean(SharedGroup.SET_REGISTER_ALERT, true)) {
                                val clickIntent = Intent(applicationContext, SplashActivity::class.java).apply { putExtra("ID", id) }
                                val clickPendingIntent = PendingIntent.getActivity(applicationContext, id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                                NotificationCompat.Builder(applicationContext, ChannelId.DEFAULT_ID).apply {
                                    setContentTitle(className)
                                    setContentText(getString(R.string.reminder_content_sup_lec_update, data.week, data.lesson, timeFormat.format(data.endTime)))
                                    setContentIntent(clickPendingIntent)
                                    setAutoCancel(true)
                                    setStyle(NotificationCompat.BigTextStyle())
                                    setSmallIcon(R.drawable.ic_icon)
                                    color = ContextCompat.getColor(applicationContext, R.color.colorAccent)

                                    nm.notify(699000 + id, build())
                                }
                            }
                        }
                    }
                    contains(getString(R.string.format_new_zoom)) -> {
                        val regex = getString(R.string.format_zoom_regex).toRegex()
                        val matchResult = regex.matchEntire(this as CharSequence)

                        if (matchResult != null) {
                            val (homework_name, startTime, endTime) = matchResult.destructured
                            val data = LMSClass(-1, className, sbn.postTime, LMSClass.ZOOM, timeFormat.parse(startTime)?.time ?: 0L, timeFormat.parse(endTime)?.time ?: 0L, isRenewAllowed = true, isFinished = false, -1, -1, homework_name)

                            if (!dbHelper.checkItemByData(data)) {
                                dbHelper.addItem(data)

                                val notiIntent = Intent(applicationContext, TimeReceiver::class.java)
                                val id = dbHelper.getAllData().last().id
                                notiIntent.putExtra("ID", id)

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_1HOUR_HW, false)) {
                                    val triggerTime = data.endTime - 1 * 60 * 60 * 1000
                                    notiIntent.putExtra("TRIGGER", triggerTime)
                                    notiIntent.putExtra("TIME", 1)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_2HOUR_HW, false)) {
                                    val triggerTime = data.endTime - 2 * 60 * 60 * 1000
                                    notiIntent.putExtra("TRIGGER", triggerTime)
                                    notiIntent.putExtra("TIME", 2)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 2, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_6HOUR_HW, false)) {
                                    val triggerTime = data.endTime - 6 * 60 * 60 * 1000
                                    notiIntent.putExtra("TRIGGER", triggerTime)
                                    notiIntent.putExtra("TIME", 6)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 3, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_12HOUR_HW, false)) {
                                    val triggerTime = data.endTime - 12 * 60 * 60 * 1000
                                    notiIntent.putExtra("TRIGGER", triggerTime)
                                    notiIntent.putExtra("TIME", 12)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 4, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_24HOUR_HW, false)) {
                                    val triggerTime = data.endTime - 24 * 60 * 60 * 1000
                                    notiIntent.putExtra("TRIGGER", triggerTime)
                                    notiIntent.putExtra("TIME", 24)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 5, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.SET_REGISTER_ALERT, true)) {
                                    val clickIntent = Intent(applicationContext, SplashActivity::class.java).apply { putExtra("ID", id) }
                                    val clickPendingIntent = PendingIntent.getActivity(applicationContext, id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                                    NotificationCompat.Builder(applicationContext, ChannelId.DEFAULT_ID).apply {
                                        setContentTitle(className)
                                        setContentText(getString(R.string.reminder_content_hw_register, data.homework_name, timeFormat.format(data.endTime)))
                                        setContentIntent(clickPendingIntent)
                                        setAutoCancel(true)
                                        setStyle(NotificationCompat.BigTextStyle())
                                        setSmallIcon(R.drawable.ic_icon)
                                        color = ContextCompat.getColor(applicationContext, R.color.colorAccent)

                                        nm.notify(699000 + id, build())
                                    }
                                }
                            }
                        }
                    }
                    contains(getString(R.string.format_change_zoom)) -> {

                    }
                    else -> {

                    }
                }
            }
        }
    }
}
