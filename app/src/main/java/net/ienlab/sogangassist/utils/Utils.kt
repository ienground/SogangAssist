package net.ienlab.sogangassist.utils

import android.Manifest
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.os.PowerManager
import android.service.notification.NotificationListenerService
import android.text.Html
import android.text.Spanned
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import net.ienlab.sogangassist.Dlog
import net.ienlab.sogangassist.TAG
import net.ienlab.sogangassist.constant.Intents
import net.ienlab.sogangassist.constant.PendingReq
import net.ienlab.sogangassist.constant.Pref
import net.ienlab.sogangassist.data.Permissions
import net.ienlab.sogangassist.data.lms.Lms
import net.ienlab.sogangassist.dataStore
import net.ienlab.sogangassist.receiver.ReminderReceiver
import net.ienlab.sogangassist.receiver.TimeReceiver
import net.ienlab.sogangassist.service.LMSListenerService
import java.io.*
import java.nio.charset.Charset
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId


object Utils {
    fun isPackageInstalled(packageName: String, pm: PackageManager): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0L))
            } else {
                pm.getPackageInfo(packageName, 0)
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun fromHtml(source: String): Spanned {
        return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
    }

    fun readTextFromRaw(res: Resources, rawId: Int): String {
        val inputStream = res.openRawResource(rawId)
        val stream = InputStreamReader(inputStream, Charset.forName("UTF-8"))
        val buffer = BufferedReader(stream)
        val builder = StringBuilder()

        buffer.lineSequence().forEach { builder.append(it) }
        inputStream.close()

        return builder.toString()
    }

    fun checkTimeRange(startTime: Int, endTime: Int, currentTime: Int): Boolean {
        return if (endTime <= startTime) {
            currentTime in startTime..(24 * 60) || currentTime in 0..endTime
        } else {
            currentTime in startTime..endTime
        }
    }

    fun isNotiPermissionAllowed(context: Context): Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(context).any { it == context.packageName }
    }

    fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun LocalDateTime.timeInMillis(zoneId: ZoneId = ZoneId.systemDefault()): Long = atZone(zoneId).toInstant().toEpochMilli()

    fun LocalDate.timeInMillis(): Long = atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    fun parseLongToLocalDateTime(time: Long): LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault())

    fun parseLongToLocalDate(time: Long): LocalDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()).toLocalDate()

    fun String.toSafeInt(): Int {
        return try {
            if (this.isNotEmpty()) toInt()
            else -1
        } catch (_: NumberFormatException) { -1 }
    }

    fun Int.toSafeString() = if (this != -1) toString() else ""

    fun setLmsSchedule(context: Context, am: AlarmManager, entity: Lms) {
        if (entity.isFinished) return
        val listLiveSession = listOf(Lms.Type.ZOOM, Lms.Type.EXAM)

        val hours = listOf(1, 2, 6, 12, 24)
        val minutes = listOf(3, 5, 10, 20, 30)

        val intent = Intent(context, TimeReceiver::class.java).apply {
            putExtra(Intents.Key.ITEM_ID, entity.id)
        }

        if (entity.type in listLiveSession) {
            minutes.forEachIndexed { index, minute ->
                val trigger = parseLongToLocalDateTime(entity.endTime).minusMinutes(minute.toLong())
                intent.putExtra(Intents.Key.TRIGGER, trigger.timeInMillis())
                intent.putExtra(Intents.Key.MINUTE, minute)
                val pendingIntent = PendingIntent.getBroadcast(context, PendingReq.REMINDER + (entity.id?.toInt() ?: 0) * 10 + index, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                if (trigger.isAfter(LocalDateTime.now())) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger.timeInMillis(), pendingIntent)
            }
        } else {
            hours.forEachIndexed { index, hour ->
                val trigger = parseLongToLocalDateTime(entity.endTime).minusHours(hour.toLong())
                intent.putExtra(Intents.Key.TRIGGER, trigger.timeInMillis())
                intent.putExtra(Intents.Key.HOUR, hour)
                val pendingIntent = PendingIntent.getBroadcast(context, PendingReq.REMINDER + (entity.id?.toInt() ?: 0) * 10 + index, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                if (trigger.isAfter(LocalDateTime.now())) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger.timeInMillis(), pendingIntent)
            }
        }
    }

    fun deleteLmsSchedule(context: Context, am: AlarmManager, entity: Lms) {
        val intent = Intent(context, TimeReceiver::class.java).apply {
            putExtra(Intents.Key.ITEM_ID, entity.id)
        }

        for (i in 0 until 5) {
            val pendingIntent = PendingIntent.getBroadcast(context, PendingReq.REMINDER + (entity.id?.toInt() ?: 0) * 10 + i, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            am.cancel(pendingIntent)
        }
    }

    fun setDayReminder(context: Context, type: Int, enableReminder: Boolean, timeInt: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val requestCode = if (type == Intents.Value.ReminderType.MORNING) PendingReq.MORNING_REMINDER else PendingReq.NIGHT_REMINDER
        val reminderTime = LocalDateTime.now().withHour(timeInt / 60).withMinute(timeInt % 60).withSecond(0)
        val pending = PendingIntent.getBroadcast(context, requestCode, Intent(context, ReminderReceiver::class.java).apply { putExtra(Intents.Key.REMINDER_TYPE, type) },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        if (enableReminder) {
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, reminderTime.timeInMillis(), AlarmManager.INTERVAL_DAY, pending)
        } else {
            am.cancel(pending)
        }
    }

    fun Int.notifyToList() = this.toUInt().toString(radix = 2).padStart(5, '0').toList().reversed().map { it == '1' }

    fun isNotificationPermissionGranted(context: Context): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.isNotificationListenerAccessGranted(ComponentName(context, LMSListenerService::class.java))
    }

    fun isNotificationPolicyGranted(context: Context): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.isNotificationPolicyAccessGranted
    }

    fun checkPermissions(context: Context, list: List<Permissions>): Boolean {
        var result = true
        list.forEach { permissions ->
            if (permissions.permissions.any { it == Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE }) {
                if (!isNotificationPermissionGranted(context)) {
                    result = false
                }
            } else if (permissions.permissions.any { it == Manifest.permission.ACCESS_NOTIFICATION_POLICY }) {
                if (!isNotificationPolicyGranted(context)) {
                    result = false
                }
            } else if (permissions.permissions.any { it == Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS }) {
                val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
                    result = false
                }
            } else if (permissions.permissions.any { it == LMSListenerService.LMS_PACKAGE_NAME }) {
                if (!isPackageInstalled(context, LMSListenerService.LMS_PACKAGE_NAME)) {
                    result = false
                }
            } else if (!checkPermission(context, permissions.permissions)) {
                result = false
            }
        }
        return result
    }

    fun checkPermission(context: Context, permissions: List<String>): Boolean {
        var result = true
        permissions.forEach { permission ->
            if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                result = false
            }
        }
        return result
    }

    fun isPackageInstalled(context: Context, packageName: String): Boolean {
        val pm = context.packageManager
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0L))
            } else {
                pm.getPackageInfo(packageName, 0)
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}