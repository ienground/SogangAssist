package net.ienlab.sogangassist.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.core.graphics.*
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.activity.TAG
import java.text.SimpleDateFormat
import java.util.*

class CalendarWidget : AppWidgetProvider() {

    val goalDataIdFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    lateinit var sharedPreferences: SharedPreferences
    lateinit var am: AlarmManager
    lateinit var intent: Intent

    lateinit var views: RemoteViews
    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        views = RemoteViews(context.packageName, R.layout.widget_calendar)

        am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        intent = Intent(context, CalendarWidget::class.java).apply {
            action = MONTH_BACK
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }

        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)


//        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
//        val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
//        val maxWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
//        val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
//        val maxHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
//
//        Log.d(TAG, "mW : $minWidth, MW: $maxWidth, mH: $minHeight, MH: $maxHeight")
        val calendar = Calendar.getInstance()
//        views.setImageViewBitmap(R.id.calendar, getCalendarBitmap(context, 800, 1000, calendar))
//        views.setOnClickPendingIntent(R.id.refresh, pendingIntent)
//        views.setOnClickPendingIntent(R.id.entire_widget, pendingIntent)
//        views.setImageViewBitmap(R.id.app_name, setTextTypeface(context, "fonts/Pretendard-Black.otf", context.getString(R.string.bp_app_name), 30f, color, 240, 46, Paint.Align.LEFT))

        views.setOnClickPendingIntent(R.id.btn_month_back, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }

        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), widgetUpdateTime, PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
    }

    override fun onDisabled(context: Context) {
        with (PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)) {
            am.cancel(this)
            cancel()
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != null) {
            when (action) {
                ACTION_CLICK -> {
                    val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                    updateAppWidget(context, AppWidgetManager.getInstance(context), id)

                    return
                }
                MONTH_BACK -> {
                    Log.d(TAG, "monthBack")
                    val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                    updateAppWidget(context, AppWidgetManager.getInstance(context), id)
                }
            }
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

    companion object {
        internal val ACTION_CLICK = "CLICK"
        internal val MONTH_BACK = "MONTH_BACK"
        internal val MONTH_FWD = "MONTH_FWD"
        internal val SET_TODAY = "SET_TODAY"
        internal val ADD_EVENT = "ADD_EVENT"

        val widgetUpdateTime = 5 * 60 * 1000L
//        val widgetUpdateTime = AlarmManager.INTERVAL_HALF_HOUR
    }
}

