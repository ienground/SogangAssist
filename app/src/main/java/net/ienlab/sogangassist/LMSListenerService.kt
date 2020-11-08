package net.ienlab.sogangassist

import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

class LMSListenerService : NotificationListenerService() {

    lateinit var pm: PackageManager
    lateinit var am: AlarmManager
    lateinit var sharedPreferences: SharedPreferences
    lateinit var dbHelper: DBHelper

    override fun onCreate() {
        super.onCreate()
        pm = packageManager
        am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        dbHelper = DBHelper(this, dbName, dbVersion)
        sharedPreferences = getSharedPreferences("${packageName}_preferences", Context.MODE_PRIVATE)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.d(TAG, "onNotificationPosted")
        super.onNotificationPosted(sbn)
        val notification = sbn.notification
        val extras = notification.extras

        val timeFormat = SimpleDateFormat(getString(R.string.format_lms_date), Locale.getDefault())

//        if (sbn.packageName == "kr.co.imaxsoft.hellolms") {
        if (sbn.packageName == "net.ienlab.notificationtest") {
            val className = extras.getString(Notification.EXTRA_TITLE) ?: ""

            with (extras.getString(Notification.EXTRA_TEXT)) {
                when {
                    this?.contains(getString(R.string.format_new_assignment)) ?: false -> {
                        val regex = getString(R.string.format_assignment_regex).toRegex()
                        val matchResult = regex.matchEntire(this as CharSequence)

                        if (matchResult != null) {
                            val (homework_name, startTime, endTime) = matchResult.destructured
                            LMSClass().let {
                                it.className = className
                                it.timeStamp = sbn.postTime
                                it.type = LMSType.HOMEWORK
                                it.startTime = timeFormat.parse(startTime)?.time ?: 0L
                                it.endTime = timeFormat.parse(endTime)?.time ?: 0L
                                it.homework_name = homework_name
                                it.week = -1
                                it.lesson = -1
                                it.isRenewAllowed = true

                                dbHelper.addItem(it)


                                val noti_intent = Intent(applicationContext, TimeReceiver::class.java)
                                val id = dbHelper.getAllData().last().id
                                noti_intent.putExtra("ID", id)

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_1HOUR_HW, false)) {
                                    val triggerTime = it.endTime - 1 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 1)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 1, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_2HOUR_HW, false)) {
                                    val triggerTime = it.endTime - 2 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 2)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 2, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_6HOUR_HW, false)) {
                                    val triggerTime = it.endTime - 6 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 6)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 3, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_12HOUR_HW, false)) {
                                    val triggerTime = it.endTime - 12 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 12)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 4, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_24HOUR_HW, false)) {
                                    val triggerTime = it.endTime - 24 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 24)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 5, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }
                            }
                        }
                    }

                    this?.contains(getString(R.string.format_change_assignment)) ?: false -> {
                        val regex = getString(R.string.format_assignment_regex).toRegex()
                        val matchResult = regex.matchEntire(this as CharSequence)

                        if (matchResult != null) {
                            val (homework_name, startTime, endTime) = matchResult.destructured
                            LMSClass().let {
                                it.className = className
                                it.timeStamp = sbn.postTime
                                it.type = LMSType.HOMEWORK
                                it.startTime = timeFormat.parse(startTime)?.time ?: 0L
                                it.endTime = timeFormat.parse(endTime)?.time ?: 0L
                                it.homework_name = homework_name
                                it.week = -1
                                it.lesson = -1

                                val id: Int
                                if (dbHelper.checkItemByData(it)) {
                                    val oldItem = dbHelper.getItemById(dbHelper.getIdByCondition(it))
                                    it.id = oldItem.id
                                    it.isFinished = oldItem.isFinished
                                    it.isRenewAllowed = oldItem.isRenewAllowed
                                    if (oldItem.isRenewAllowed) {
                                        dbHelper.updateItem(it) // check
                                    }
                                    id = it.id
                                } else {
                                    dbHelper.addItem(it)
                                    id = dbHelper.getAllData().last().id
                                }

                                val noti_intent = Intent(applicationContext, TimeReceiver::class.java)
                                noti_intent.putExtra("ID", id)

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_1HOUR_HW, false)) {
                                    val triggerTime = it.endTime - 1 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 1)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 1, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_2HOUR_HW, false)) {
                                    val triggerTime = it.endTime - 2 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 2)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 2, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_6HOUR_HW, false)) {
                                    val triggerTime = it.endTime - 6 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 6)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 3, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_12HOUR_HW, false)) {
                                    val triggerTime = it.endTime - 12 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 12)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 4, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_24HOUR_HW, false)) {
                                    val triggerTime = it.endTime - 24 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 24)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 5, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }
                            }
                        }
                    }

                    this?.contains(getString(R.string.format_new_lecture)) ?: false -> {
                        val regex = getString(R.string.format_lecture_regex).toRegex()
                        val matchResult = regex.matchEntire(this as CharSequence)

                        if (matchResult != null) {
                            val (week, lesson, endTime) = matchResult.destructured
                            LMSClass().let {
                                it.className = className
                                it.timeStamp = sbn.postTime
                                it.type = LMSType.LESSON
                                it.startTime = -1
                                it.endTime = timeFormat.parse(endTime)?.time ?: 0L
                                it.homework_name = "#NONE"
                                it.week = week.toInt()
                                it.lesson = lesson.toInt()

                                dbHelper.addItem(it)

                                val noti_intent = Intent(applicationContext, TimeReceiver::class.java)
                                val id = dbHelper.getAllData().last().id
                                noti_intent.putExtra("ID", id)

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_1HOUR_LEC, false)) {
                                    val triggerTime = it.endTime - 1 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 1)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 1, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_2HOUR_LEC, false)) {
                                    val triggerTime = it.endTime - 2 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 2)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 2, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_6HOUR_LEC, false)) {
                                    val triggerTime = it.endTime - 6 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 6)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 3, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_12HOUR_LEC, false)) {
                                    val triggerTime = it.endTime - 12 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 12)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 4, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_24HOUR_LEC, false)) {
                                    val triggerTime = it.endTime - 24 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 24)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 5, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }
                            }
                        }
                    }

                    this?.contains(getString(R.string.format_change_lecture)) ?: false -> {
                        val regex = getString(R.string.format_lecture_regex).toRegex()
                        val matchResult = regex.matchEntire(this as CharSequence)

                        if (matchResult != null) {
                            val (week, lesson, endTime) = matchResult.destructured
                            LMSClass().let {
                                it.className = className
                                it.timeStamp = sbn.postTime
                                it.type = LMSType.LESSON
                                it.startTime = -1
                                it.endTime = timeFormat.parse(endTime)?.time ?: 0L
                                it.homework_name = "#NONE"
                                it.week = week.toInt()
                                it.lesson = lesson.toInt()

                                val id: Int
                                if (dbHelper.checkItemByData(it)) {
                                    val oldItem = dbHelper.getItemById(dbHelper.getIdByCondition(it))
                                    it.id = oldItem.id
                                    it.isFinished = oldItem.isFinished
                                    it.isRenewAllowed = oldItem.isRenewAllowed
                                    if (oldItem.isRenewAllowed) {
                                        dbHelper.updateItem(it) // check
                                    }
                                    id = it.id
                                } else {
                                    dbHelper.addItem(it)
                                    id = dbHelper.getAllData().last().id
                                }

                                val noti_intent = Intent(applicationContext, TimeReceiver::class.java)
                                noti_intent.putExtra("ID", id)

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_1HOUR_LEC, false)) {
                                    val triggerTime = it.endTime - 1 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 1)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 1, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_2HOUR_LEC, false)) {
                                    val triggerTime = it.endTime - 2 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 2)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 2, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_6HOUR_LEC, false)) {
                                    val triggerTime = it.endTime - 6 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 6)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 3, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_12HOUR_LEC, false)) {
                                    val triggerTime = it.endTime - 12 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 12)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 4, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_24HOUR_LEC, false)) {
                                    val triggerTime = it.endTime - 24 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 24)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 5, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }
                            }
                        }
                    }

                    this?.contains(getString(R.string.format_new_sup_lecture)) ?: false -> {
                        val regex = getString(R.string.format_lecture_regex).toRegex()
                        val matchResult = regex.matchEntire(this as CharSequence)

                        if (matchResult != null) {
                            val (week, lesson, endTime) = matchResult.destructured
                            LMSClass().let {
                                it.className = className
                                it.timeStamp = sbn.postTime
                                it.type = LMSType.SUP_LESSON
                                it.startTime = -1
                                it.endTime = timeFormat.parse(endTime)?.time ?: 0L
                                it.homework_name = "#NONE"
                                it.week = week.toInt()
                                it.lesson = lesson.toInt()

                                dbHelper.addItem(it)

                                val noti_intent = Intent(applicationContext, TimeReceiver::class.java)
                                val id = dbHelper.getAllData().last().id
                                noti_intent.putExtra("ID", id)

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_1HOUR_LEC, false)) {
                                    val triggerTime = it.endTime - 1 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 1)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 1, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_2HOUR_LEC, false)) {
                                    val triggerTime = it.endTime - 2 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 2)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 2, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_6HOUR_LEC, false)) {
                                    val triggerTime = it.endTime - 6 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 6)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 3, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_12HOUR_LEC, false)) {
                                    val triggerTime = it.endTime - 12 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 12)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 4, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_24HOUR_LEC, false)) {
                                    val triggerTime = it.endTime - 24 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 24)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 5, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }
                            }
                        }
                    }

                    this?.contains(getString(R.string.format_change_sup_lecture)) ?: false -> {
                        val regex = getString(R.string.format_lecture_regex).toRegex()
                        val matchResult = regex.matchEntire(this as CharSequence)

                        if (matchResult != null) {
                            val (week, lesson, endTime) = matchResult.destructured
                            LMSClass().let {
                                it.className = className
                                it.timeStamp = sbn.postTime
                                it.type = LMSType.SUP_LESSON
                                it.startTime = -1
                                it.endTime = timeFormat.parse(endTime)?.time ?: 0L
                                it.homework_name = "#NONE"
                                it.week = week.toInt()
                                it.lesson = lesson.toInt()

                                val id: Int
                                if (dbHelper.checkItemByData(it)) {
                                    val oldItem = dbHelper.getItemById(dbHelper.getIdByCondition(it))
                                    it.id = oldItem.id
                                    it.isFinished = oldItem.isFinished
                                    it.isRenewAllowed = oldItem.isRenewAllowed
                                    if (oldItem.isRenewAllowed) {
                                        dbHelper.updateItem(it) // check
                                    }
                                    id = it.id
                                } else {
                                    dbHelper.addItem(it)
                                    id = dbHelper.getAllData().last().id
                                }

                                val noti_intent = Intent(applicationContext, TimeReceiver::class.java)
                                noti_intent.putExtra("ID", id)

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_1HOUR_LEC, false)) {
                                    val triggerTime = it.endTime - 1 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 1)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 1, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_2HOUR_LEC, false)) {
                                    val triggerTime = it.endTime - 2 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 2)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 2, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_6HOUR_LEC, false)) {
                                    val triggerTime = it.endTime - 6 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 6)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 3, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_12HOUR_LEC, false)) {
                                    val triggerTime = it.endTime - 12 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 12)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 4, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                                }

                                if (sharedPreferences.getBoolean(SharedGroup.NOTIFY_24HOUR_LEC, false)) {
                                    val triggerTime = it.endTime - 24 * 60 * 60 * 1000
                                    noti_intent.putExtra("TRIGGER", triggerTime)
                                    noti_intent.putExtra("TIME", 24)
                                    val pendingIntent = PendingIntent.getBroadcast(applicationContext, id * 100 + 5, noti_intent, PendingIntent.FLAG_UPDATE_CURRENT)
                                    am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
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
