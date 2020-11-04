package net.ienlab.sogangassist

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlin.math.abs

class TimeReceiver : BroadcastReceiver() {

    lateinit var nm: NotificationManager
    lateinit var dbHelper: DBHelper

    override fun onReceive(context: Context, intent: Intent) {
        nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        dbHelper = DBHelper(context, dbName, dbVersion)

        val channelId = "ReminderLMS"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, context.getString(R.string.channel_name), NotificationManager.IMPORTANCE_HIGH)
            nm.createNotificationChannel(channel)
        }

        val item = dbHelper.getItemById(intent.getIntExtra("ID", -1))
        val time = intent.getIntExtra("TIME", -1)
        val triggerTime = intent.getLongExtra("TRIGGER", -1)

        when (item.type) {
            LMSType.LESSON -> {
                NotificationCompat.Builder(context, channelId).let {
                    val markIntent = Intent(context, MarkFinishReceiver::class.java).apply {
                        putExtra("ID", item.id)
                    }

                    val pendingIntent = PendingIntent.getBroadcast(context, 0, markIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                    it
                        .setContentTitle(item.className)
                        .setContentText(String.format(context.getString(R.string.reminder_content_lec), item.week, item.lesson, time))
                        .setStyle(NotificationCompat.BigTextStyle())
                        .setSmallIcon(R.drawable.ic_video)
                        .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                        .addAction(R.drawable.ic_check, context.getString(R.string.mark_as_finish), pendingIntent)

                    if (abs(System.currentTimeMillis() - triggerTime) <= 3000 && !item.isFinished) {
                        nm.notify(693000 + item.id, it.build())
                    }
                }
            }

            LMSType.SUP_LESSON -> {
                NotificationCompat.Builder(context, channelId).let {
                    val markIntent = Intent(context, MarkFinishReceiver::class.java).apply {
                        putExtra("ID", item.id)
                    }

                    val pendingIntent = PendingIntent.getBroadcast(context, 0, markIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                    it
                        .setContentTitle(item.className)
                        .setContentText(String.format(context.getString(R.string.reminder_content_lec), item.week, item.lesson, time))
                        .setStyle(NotificationCompat.BigTextStyle())
                        .setSmallIcon(R.drawable.ic_video_sup)
                        .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                        .addAction(R.drawable.ic_check, context.getString(R.string.mark_as_finish), pendingIntent)

                    if (abs(System.currentTimeMillis() - triggerTime) <= 3000 && !item.isFinished) {
                        nm.notify(693000 + item.id, it.build())
                    }
                }
            }

            LMSType.HOMEWORK -> {
                NotificationCompat.Builder(context, channelId).let {
                    val markIntent = Intent(context, MarkFinishReceiver::class.java).apply {
                        putExtra("ID", item.id)
                    }

                    val pendingIntent = PendingIntent.getBroadcast(context, 0, markIntent, PendingIntent.FLAG_UPDATE_CURRENT)

                    it
                        .setContentTitle(item.className)
                        .setContentText(String.format(context.getString(R.string.reminder_content_hw), item.homework_name, time))
                        .setStyle(NotificationCompat.BigTextStyle())
                        .setSmallIcon(R.drawable.ic_assignment)
                        .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                        .addAction(R.drawable.ic_check, context.getString(R.string.mark_as_finish), pendingIntent)

                    if (abs(System.currentTimeMillis() - triggerTime) <= 3000 && !item.isFinished) {
                        nm.notify(693000 + item.id, it.build())
                    }
                }
            }
        }

    }
}
