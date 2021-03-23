package net.ienlab.sogangassist.widget

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.util.Log
import android.webkit.CookieManager
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.activity.TAG
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.net.URLEncoder
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.net.ssl.HttpsURLConnection

class CalendarWidget : AppWidgetProvider() {

    val goalDataIdFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    lateinit var visitorPreferences: SharedPreferences
    lateinit var am: AlarmManager
    lateinit var intent: Intent

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_calendar)
        val sharedPreferences = context.getSharedPreferences(context.packageName + "_preferences", Context.MODE_PRIVATE)

        visitorPreferences = context.getSharedPreferences("VisitorPreferences", Context.MODE_PRIVATE)
        am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        intent = Intent(context, CalendarWidget::class.java).apply {
            action = ACTION_CLICK
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }

        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)


        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val maxWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
        val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        val maxHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)

        Log.d(TAG, "mW : $minWidth, MW: $maxWidth, mH: $minHeight, MH: $maxHeight")
//        views.setImageViewBitmap(R.id.calendar, getCalendarBitmap(context, ))
//        views.setOnClickPendingIntent(R.id.refresh, pendingIntent)
//        views.setOnClickPendingIntent(R.id.entire_widget, pendingIntent)
//        views.setImageViewBitmap(R.id.app_name, setTextTypeface(context, "fonts/gmsans_bold.otf", context.getString(R.string.bp_app_name), 30f, color, 240, 46, Paint.Align.LEFT))

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }

        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), widgetUpdateTime, PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT))
    }

    override fun onDisabled(context: Context) {
        with (PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)) {
            am.cancel(this)
            cancel()
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != null && action == ACTION_CLICK) {
            val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            updateAppWidget(context, AppWidgetManager.getInstance(context), id)

            return
        }
        super.onReceive(context, intent)
    }

    fun setTextTypeface(context: Context, font: String, text: String, size: Float, textColor: Int, width: Int, height: Int, align: Paint.Align): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val typeface = Typeface.createFromAsset(context.assets, font)
        val paint = Paint().apply {
            isAntiAlias = true
            isSubpixelText = true
            setTypeface(typeface)
            style = Paint.Style.FILL
            color = textColor
            textSize = size
            textAlign = align
        }

        when (align) {
            Paint.Align.LEFT -> {
                canvas.drawText(text, 0f, size, paint)
            }

            Paint.Align.RIGHT -> {
                canvas.drawText(text, width.toFloat(), size, paint)
            }

            Paint.Align.CENTER -> {
                canvas.drawText(text, width.toFloat() / 2, size, paint)
            }
        }

        return bitmap
    }

    fun getCalendarBitmap(context: Context, width: Int, height: Int, month: Int, year: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)

        val gmSansBold = Typeface.createFromAsset(context.assets, "fonts/gmsans_bold.otf")
        val gmSansMedium = Typeface.createFromAsset(context.assets, "fonts/gmsans_medium.otf")

        val shapeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val shapeCanvas = Canvas(shapeBitmap)

        canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 20f, 20f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)





        return bitmap
    }

    companion object {
        internal val ACTION_CLICK = "CLICK"
        val widgetUpdateTime = 5 * 60 * 1000L
//        val widgetUpdateTime = AlarmManager.INTERVAL_HALF_HOUR
    }
}

