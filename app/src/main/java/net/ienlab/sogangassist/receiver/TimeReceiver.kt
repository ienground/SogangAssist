package net.ienlab.sogangassist.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import net.ienlab.sogangassist.*
import net.ienlab.sogangassist.constant.ChannelId
import net.ienlab.sogangassist.database.*
import net.ienlab.sogangassist.activity.*
import net.ienlab.sogangassist.data.LMSClass
import kotlin.math.abs

class TimeReceiver : BroadcastReceiver() {

    lateinit var nm: NotificationManager
    lateinit var dbHelper: DBHelper

    override fun onReceive(context: Context, intent: Intent) {
        nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        dbHelper = DBHelper(context, dbName, dbVersion)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(ChannelId.DEFAULT_ID, context.getString(R.string.channel_name), NotificationManager.IMPORTANCE_HIGH)
            nm.createNotificationChannel(channel)
        }

        val id = intent.getIntExtra("ID", -1)
        val item = dbHelper.getItemById(id)
        val time = intent.getIntExtra("TIME", -1)
        val triggerTime = intent.getLongExtra("TRIGGER", -1)

        val clickIntent = Intent(context, SplashActivity::class.java).apply { putExtra("ID", id) }
        val clickPendingIntent = PendingIntent.getActivity(context, id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        when (item.type) {
            LMSClass.TYPE_LESSON -> {
                NotificationCompat.Builder(context, ChannelId.DEFAULT_ID).apply {
                    val markIntent = Intent(context, MarkFinishReceiver::class.java).apply { putExtra("ID", id) }
                    val pendingIntent = PendingIntent.getBroadcast(context, id, markIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                    setContentTitle(item.className)
                    setContentText(context.getString(R.string.reminder_content_lec, item.week, item.lesson, time))
                    setContentIntent(clickPendingIntent)
                    setAutoCancel(true)
                    setStyle(NotificationCompat.BigTextStyle())
                    setSmallIcon(R.drawable.ic_video)
                    addAction(R.drawable.ic_check, context.getString(R.string.mark_as_finish), pendingIntent)
                    color = ContextCompat.getColor(context, R.color.colorAccent)

                    if (abs(System.currentTimeMillis() - triggerTime) <= 3000 && !item.isFinished) {
                        if (item.className != "") {
                            nm.notify(693000 + id, build())
                        }
                    }
                }
            }

            LMSClass.TYPE_SUP_LESSON -> {
                NotificationCompat.Builder(context, ChannelId.DEFAULT_ID).apply {
                    val markIntent = Intent(context, MarkFinishReceiver::class.java).apply { putExtra("ID", id) }
                    val pendingIntent = PendingIntent.getBroadcast(context, id, markIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                    setContentTitle(item.className)
                    setContentText(context.getString(R.string.reminder_content_sup_lec, item.week, item.lesson, time))
                    setContentIntent(clickPendingIntent)
                    setAutoCancel(true)
                    setStyle(NotificationCompat.BigTextStyle())
                    setSmallIcon(R.drawable.ic_video_sup)
                    addAction(R.drawable.ic_check, context.getString(R.string.mark_as_finish), pendingIntent)
                    color = ContextCompat.getColor(context, R.color.colorAccent)

                    if (abs(System.currentTimeMillis() - triggerTime) <= 3000 && !item.isFinished) {
                        if (item.className != "") {
                            nm.notify(693000 + id, build())
                        }
                    }
                }
            }

            LMSClass.TYPE_HOMEWORK -> {
                NotificationCompat.Builder(context, ChannelId.DEFAULT_ID).apply {
                    val markIntent = Intent(context, MarkFinishReceiver::class.java).apply { putExtra("ID", id) }
                    val pendingIntent = PendingIntent.getBroadcast(context, id, markIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                    setContentTitle(item.className)
                    setContentText(context.getString(R.string.reminder_content_hw, item.homework_name, time))
                    setContentIntent(clickPendingIntent)
                    setAutoCancel(true)
                    setStyle(NotificationCompat.BigTextStyle())
                    setSmallIcon(R.drawable.ic_assignment)
                    addAction(R.drawable.ic_check, context.getString(R.string.mark_as_finish), pendingIntent)
                    color = ContextCompat.getColor(context, R.color.colorAccent)

                    if (abs(System.currentTimeMillis() - triggerTime) <= 3000 && !item.isFinished) {
                        if (item.className != "") {
                            nm.notify(693000 + id, build())
                        }
                    }
                }
            }

            LMSClass.TYPE_ZOOM -> {
                NotificationCompat.Builder(context, ChannelId.DEFAULT_ID).apply {
                    val markIntent = Intent(context, MarkFinishReceiver::class.java).apply { putExtra("ID", id) }
                    val pendingIntent = PendingIntent.getBroadcast(context, id, markIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                    setContentTitle(item.className)
                    setContentText(context.getString(R.string.reminder_content_zoom, item.homework_name, time))
                    setContentIntent(clickPendingIntent)
                    setAutoCancel(true)
                    setStyle(NotificationCompat.BigTextStyle())
                    setSmallIcon(R.drawable.ic_groups)
                    addAction(R.drawable.ic_check, context.getString(R.string.mark_as_finish), pendingIntent)
                    color = ContextCompat.getColor(context, R.color.colorAccent)

                    if (abs(System.currentTimeMillis() - triggerTime) <= 3000 && !item.isFinished) {
                        if (item.className != "") {
                            nm.notify(693000 + id, build())
                        }
                    }
                }
            }
        }

    }
}
