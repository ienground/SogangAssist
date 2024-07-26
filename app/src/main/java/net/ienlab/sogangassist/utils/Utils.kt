package net.ienlab.sogangassist.utils

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.text.Html
import android.text.Spanned
import androidx.core.app.NotificationManagerCompat
import net.ienlab.sogangassist.constant.Intents
import net.ienlab.sogangassist.constant.PendingReq
import net.ienlab.sogangassist.data.lms.Lms
import net.ienlab.sogangassist.receiver.TimeReceiver
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

    fun LocalDateTime.timeInMillis(): Long = atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

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
                val pendingIntent = PendingIntent.getBroadcast(context, PendingReq.LAUNCH_NOTI + (entity.id?.toInt() ?: 0) * 10 + index, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                if (trigger.isAfter(LocalDateTime.now())) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger.timeInMillis(), pendingIntent)
            }
        } else {
            hours.forEachIndexed { index, hour ->
                val trigger = parseLongToLocalDateTime(entity.endTime).minusHours(hour.toLong())
                intent.putExtra(Intents.Key.TRIGGER, trigger.timeInMillis())
                intent.putExtra(Intents.Key.HOUR, hour)
                val pendingIntent = PendingIntent.getBroadcast(context, PendingReq.LAUNCH_NOTI + (entity.id?.toInt() ?: 0) * 10 + index, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                if (trigger.isAfter(LocalDateTime.now())) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger.timeInMillis(), pendingIntent)
            }
        }
    }

    fun deleteLmsSchedule(context: Context, am: AlarmManager, entity: Lms) {
        val intent = Intent(context, TimeReceiver::class.java).apply {
            putExtra(Intents.Key.ITEM_ID, entity.id)
        }

        for (i in 0 until 5) {
            val pendingIntent = PendingIntent.getBroadcast(context, PendingReq.LAUNCH_NOTI + (entity.id?.toInt() ?: 0) * 10 + i, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            am.cancel(pendingIntent)
        }
    }
}