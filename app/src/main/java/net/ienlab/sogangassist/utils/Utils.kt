package net.ienlab.sogangassist.utils

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.TypedValue
import androidx.core.app.NotificationManagerCompat
import net.ienlab.sogangassist.R
import java.io.*
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*


class Utils {
    companion object {
        fun getDateLabel(context: Context, date: Date): String {
            val time = date.time
            val dateFormat = SimpleDateFormat(context.getString(R.string.dateFormat), Locale.getDefault())
            val dateFormatNoYear = SimpleDateFormat(context.getString(R.string.dateFormatNoYear), Locale.getDefault())
            val calendar = Calendar.getInstance().apply { timeInMillis = time }
            return if (calendar.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR)) {
                when (calendar.get(Calendar.DAY_OF_YEAR) - Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) {
                    0 -> context.getString(R.string.today)
                    1 -> context.getString(R.string.tomorrow)
                    -1 -> context.getString(R.string.yesterday)
                    else -> dateFormatNoYear.format(calendar.time)
                }
            } else {
                dateFormat.format(calendar.time)
            }
        }

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

        fun isDNDTime(startTime: Int, endTime: Int, currentTime: Int): Boolean {
            return if (endTime <= startTime) {
                currentTime in startTime..(24 * 60) || currentTime in 0..endTime
            } else {
                currentTime in startTime..endTime
            }
        }

        fun dpToPx(context: Context, dpValue: Float): Float {
            val metrics = context.resources.displayMetrics
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, metrics)
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

        fun Calendar.timeZero(): Calendar {
            val calendar = this.clone() as Calendar
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            return calendar
        }

        fun Calendar.tomorrowZero(): Calendar {
            val calendar = this.clone() as Calendar
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            return calendar
        }

    }
}