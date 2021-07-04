package net.ienlab.sogangassist.utils

import android.app.ActivityManager
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.TypedValue
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat.getSystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.net.URL
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs


class MyUtils {
    companion object {
        fun fromHtml(source: String): Spanned {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(source)
            }
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

    }
}