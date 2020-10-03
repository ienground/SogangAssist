package net.ienlab.sogangassist

import android.app.Notification
import android.app.Service
import android.content.Intent
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
    lateinit var dbHelper: DBHelper

    override fun onCreate() {
        super.onCreate()
        pm = packageManager
        dbHelper = DBHelper(this, dbName, null, dbVersion)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        val notification = sbn.notification
        val extras = notification.extras

        val timeFormat = SimpleDateFormat("yyyy.MM.dd a hh:mm", Locale.KOREA)

//        if (sbn.packageName == "kr.co.imaxsoft.hellolms") {
        if (sbn.packageName == "net.ienlab.notificationtest") {
            val className = extras.getString(Notification.EXTRA_TITLE)!!

            with (extras.getString(Notification.EXTRA_TEXT)) {
                when {
                    this?.contains("새로운 과제가 있습니다") ?: false -> {
                        val regex = "^.+\"(.+)\" \\(기간:(.+) ~ (.+)\\)$".toRegex()
                        val matchResult = regex.matchEntire(this as CharSequence)

                        if (matchResult != null) {
                            val (homework_name, startTime, endTime) = matchResult.destructured
                            LMSClass().let {
                                it.className = className
                                it.timeStamp = sbn.postTime
                                it.type = LMSType.HOMEWORK
                                it.startTime = timeFormat.parse(startTime)!!.time
                                it.endTime = timeFormat.parse(endTime)!!.time
                                it.homework_name = homework_name
                                it.week = -1
                                it.lesson = -1

                                dbHelper.addItem(it)
                            }
                        }
                    }

                    this?.contains("변경된 과제가 있습니다") ?: false -> {
                        val regex = "^.+\"(.+)\" \\(기간:(.+) ~ (.+)\\)$".toRegex()
                        val matchResult = regex.matchEntire(this as CharSequence)

                        if (matchResult != null) {
                            val (homework_name, startTime, endTime) = matchResult.destructured
                            LMSClass().let {
                                it.className = className
                                it.timeStamp = sbn.postTime
                                it.type = LMSType.HOMEWORK
                                it.startTime = timeFormat.parse(startTime)!!.time
                                it.endTime = timeFormat.parse(endTime)!!.time
                                it.homework_name = homework_name
                                it.week = -1
                                it.lesson = -1

                                dbHelper.updateItem(it)
                            }
                        }
                    }

                    this?.contains("온라인 강의를 시작") ?: false -> {
                        val regex = "^(\\d+)주 (\\d+)차시.*\\(학습마감: (.+) 까지\\)$".toRegex()
                        val matchResult = regex.matchEntire(this as CharSequence)

                        if (matchResult != null) {
                            val (week, lesson, endTime) = matchResult.destructured
                            LMSClass().let {
                                it.className = className
                                it.timeStamp = sbn.postTime
                                it.type = LMSType.LESSON
                                it.startTime = -1
                                it.endTime = timeFormat.parse(endTime)!!.time
                                it.homework_name = "#NONE"
                                it.week = week.toInt()
                                it.lesson = lesson.toInt()

                                dbHelper.addItem(it)
                            }
                        }
                    }

                    this?.contains("온라인 강의가 변경") ?: false -> {
                        val regex = "^(\\d+)주 (\\d+)차시.*\\(학습마감: (.+) 까지\\)$".toRegex()
                        val matchResult = regex.matchEntire(this as CharSequence)

                        if (matchResult != null) {
                            val (week, lesson, endTime) = matchResult.destructured
                            LMSClass().let {
                                it.className = className
                                it.timeStamp = sbn.postTime
                                it.type = LMSType.LESSON
                                it.startTime = -1
                                it.endTime = timeFormat.parse(endTime)!!.time
                                it.homework_name = "#NONE"
                                it.week = week.toInt()
                                it.lesson = lesson.toInt()

                                dbHelper.updateItem(it)
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
