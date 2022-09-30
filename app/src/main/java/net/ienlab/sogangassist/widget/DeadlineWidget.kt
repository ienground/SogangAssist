
package net.ienlab.sogangassist.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.*
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.core.graphics.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.activity.TAG
import net.ienlab.sogangassist.constant.SharedKey
import net.ienlab.sogangassist.constant.WidgetPrefGroup
import net.ienlab.sogangassist.room.LMSDatabase
import net.ienlab.sogangassist.room.LMSEntity
import net.ienlab.sogangassist.utils.MyUtils.Companion.timeZero
import java.util.*
import kotlin.collections.ArrayList

class DeadlineWidget : AppWidgetProvider() {

    lateinit var widgetPreferences: SharedPreferences
    lateinit var sharedPreferences: SharedPreferences
    lateinit var am: AlarmManager

    lateinit var intent: Intent
    lateinit var prevIntent: Intent
    lateinit var nextIntent: Intent

    lateinit var views: RemoteViews
    lateinit var receiver: BroadcastReceiver
    private var lmsDatabase: LMSDatabase? = null
    private val unfinishedEvents: ArrayList<LMSEntity> = arrayListOf()

    @OptIn(DelicateCoroutinesApi::class)
    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        views = RemoteViews(context.packageName, R.layout.widget_deadline)

        am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        lmsDatabase = LMSDatabase.getInstance(context)
        widgetPreferences = context.getSharedPreferences("WidgetPreferences", Context.MODE_PRIVATE)
        sharedPreferences = context.getSharedPreferences("${context.packageName}_preferences", Context.MODE_PRIVATE)

        intent = Intent(context, DeadlineWidget::class.java).apply {
            action = ACTION_CLICK
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        prevIntent = Intent(context, DeadlineWidget::class.java).apply {
            action = PREV_ITEM
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        nextIntent = Intent(context, DeadlineWidget::class.java).apply {
            action = NEXT_ITEM
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }

        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        GlobalScope.launch(Dispatchers.IO) {
            val calendar = Calendar.getInstance().timeZero()
            val data = (lmsDatabase?.getDao()?.getByEndTime(calendar.timeInMillis, calendar.timeInMillis + AlarmManager.INTERVAL_DAY) as ArrayList).sortedBy { it.endTime }
            unfinishedEvents.clear()

            data.forEach {
                if (!it.isFinished) {
                    unfinishedEvents.add(it)
                }
            }

            setWidgetData(context, views, unfinishedEvents, widgetPreferences.getInt(WidgetPrefGroup.DEADLINE_PAGE, 0))
        }

        views.setInt(R.id.entire_widget, "setBackgroundResource",
            if (context.resources.getBoolean(R.bool.is_dark_mode) && sharedPreferences.getBoolean(SharedKey.WIDGET_DARK_MODE, true)) {
                R.drawable.background_border_widget_black
            } else {
                R.drawable.background_border_widget
            }
        )
        views.setOnClickPendingIntent(R.id.entire_widget, pendingIntent)
        views.setOnClickPendingIntent(R.id.btn_prev, PendingIntent.getBroadcast(context, 1, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
        views.setOnClickPendingIntent(R.id.btn_next, PendingIntent.getBroadcast(context, 2, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    fun setWidgetData(context: Context, views: RemoteViews, item: List<LMSEntity>, i: Int) {
        if (item.isNotEmpty()) {
            val index = if (i < item.size) i else 0
            val timeDifference = (item[index].endTime - System.currentTimeMillis()) / (60 * 1000)
            views.setTextViewText(R.id.tv_event, when (item[index].type) {
                LMSEntity.TYPE_SUP_LESSON, LMSEntity.TYPE_LESSON -> context.getString(R.string.week_lesson_format, item[index].week, item[index].lesson)
                else -> item[index].homework_name
            })
            views.setTextViewText(R.id.tv_class, item[index].className)

            if (item[index].endTime - System.currentTimeMillis() <= 0) {
                views.setTextViewText(R.id.tv_deadline, context.getString(if (item[index].type == LMSEntity.TYPE_ZOOM) R.string.deadline_zoom_format_past else R.string.deadline_format_past, -(timeDifference / 60), -(timeDifference % 60)))
            } else {
                views.setTextViewText(R.id.tv_deadline, context.getString(if (item[index].type == LMSEntity.TYPE_ZOOM) R.string.deadline_zoom_format else R.string.deadline_format, timeDifference / 60, timeDifference % 60))
            }

            views.setImageViewResource(R.id.background_logo, when (item[index].type) {
                LMSEntity.TYPE_LESSON -> R.drawable.ic_video
                LMSEntity.TYPE_SUP_LESSON -> R.drawable.ic_video_sup
                LMSEntity.TYPE_HOMEWORK -> R.drawable.ic_assignment
                LMSEntity.TYPE_ZOOM -> R.drawable.ic_live_class
                LMSEntity.TYPE_TEAMWORK -> R.drawable.ic_team
                LMSEntity.TYPE_EXAM -> R.drawable.ic_test
                else -> R.drawable.ic_icon
            })

            views.setViewVisibility(R.id.btn_prev, if (index == 0) View.INVISIBLE else View.VISIBLE)
            views.setViewVisibility(R.id.btn_next, if (index == item.lastIndex) View.INVISIBLE else View.VISIBLE)
        } else {
            views.setImageViewResource(R.id.background_logo, R.drawable.ic_icon)
            views.setTextViewText(R.id.tv_event, "-")
            views.setTextViewText(R.id.tv_class, context.getString(R.string.no_unfinished))
            views.setTextViewText(R.id.tv_deadline, "-")
            views.setViewVisibility(R.id.btn_prev, View.INVISIBLE)
            views.setViewVisibility(R.id.btn_next, View.INVISIBLE)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
            intent = Intent(context, DeadlineWidget::class.java).apply {
                action = ACTION_CLICK
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
        }

        receiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_TIME_TICK) {
                    Log.d(TAG, "time tick!")
                    for (appWidgetId in appWidgetIds) {
                        updateAppWidget(context, appWidgetManager, appWidgetId)
                    }
                }
            }
        }
        context.applicationContext.registerReceiver(receiver, IntentFilter(Intent.ACTION_TIME_TICK))

        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), widgetUpdateTime, PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        with (PendingIntent.getBroadcast(context, 1, Intent(context, DeadlineWidget::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)) {
            am.cancel(this)
            cancel()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        lmsDatabase = LMSDatabase.getInstance(context)
        widgetPreferences = context.getSharedPreferences("WidgetPreferences", Context.MODE_PRIVATE)

        GlobalScope.launch(Dispatchers.IO) {
            val calendar = Calendar.getInstance().timeZero()
            val data = (lmsDatabase?.getDao()?.getByEndTime(calendar.timeInMillis, calendar.timeInMillis + AlarmManager.INTERVAL_DAY) as ArrayList).sortedBy { it.endTime }
            unfinishedEvents.clear()

            data.forEach {
                if (!it.isFinished) {
                    unfinishedEvents.add(it)
                }
            }

            val action = intent.action
            if (action != null) {
                when (action) {
                    ACTION_CLICK -> {
                        val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                        updateAppWidget(context, AppWidgetManager.getInstance(context), id)
                        //                    val index = widgetPreferences.getInt(WidgetPrefGroup.DEADLINE_PAGE, 0)
                        //                    Log.d(TAG, "index: $index, size: ${unfinishedEvents.size}")
                        //                    if (index < unfinishedEvents.size) {
                        //                        Log.d(TAG, "launch activity")
                        //                        context.startActivity(Intent(context, SplashActivity::class.java).apply { putExtra("ID", unfinishedEvents[index].id); flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                        //                    } else {
                        //                        Log.d(TAG, "not launch activity")
                        //                    }
                    }
                    PREV_ITEM -> {
                        val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                        updateAppWidget(context, AppWidgetManager.getInstance(context), id)
                        widgetPreferences.edit().putInt(WidgetPrefGroup.DEADLINE_PAGE, widgetPreferences.getInt(WidgetPrefGroup.DEADLINE_PAGE, 0) - 1).apply()
                        setWidgetData(context, views, unfinishedEvents, widgetPreferences.getInt(WidgetPrefGroup.DEADLINE_PAGE, 0))
                        updateAppWidget(context, AppWidgetManager.getInstance(context), id)
                    }
                    NEXT_ITEM -> {
                        val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                        updateAppWidget(context, AppWidgetManager.getInstance(context), id)
                        widgetPreferences.edit().putInt(WidgetPrefGroup.DEADLINE_PAGE, widgetPreferences.getInt(WidgetPrefGroup.DEADLINE_PAGE, 0) + 1).apply()
                        setWidgetData(context, views, unfinishedEvents, widgetPreferences.getInt(WidgetPrefGroup.DEADLINE_PAGE, 0))
                        updateAppWidget(context, AppWidgetManager.getInstance(context), id)

                    }
                }
            }
            super.onReceive(context, intent)
        }
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

    fun getScaledDrawableBitmap(context: Context, resId: Int, width: Float, height: Float): Bitmap {
        val bitmap = (ContextCompat.getDrawable(context, resId) as BitmapDrawable).bitmap.scale(width.toInt(), height.toInt(), true)

        val outputBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(outputBitmap)
        val paint = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
        val rect = Rect(0, 0, bitmap.width, bitmap.height)

        paint.isAntiAlias = true
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return outputBitmap
    }

    companion object {
        internal val ACTION_CLICK = "CLICK"
        internal val PREV_ITEM = "PREV_ITEM"
        internal val NEXT_ITEM = "NEXT_ITEM"

        val widgetUpdateTime = 5 * 60 * 1000L
//        val widgetUpdateTime = AlarmManager.INTERVAL_HALF_HOUR
    }
}

