package net.ienlab.sogangassist.receiver

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import net.ienlab.sogangassist.database.*

class SetReadReceiver : BroadcastReceiver() {

    lateinit var nm: NotificationManager
    lateinit var notiDBHelper: NotiDBHelper

    override fun onReceive(context: Context, intent: Intent) {
        nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notiDBHelper = NotiDBHelper(context, NotiDBHelper.dbName, NotiDBHelper.dbVersion)

        val notiId = intent.getIntExtra("NOTI_ID", -1)
        val cancelId = intent.getIntExtra("CANCEL_ID", -1)

        if (notiId != -1) {
            notiDBHelper.getItemById(notiId).apply {
                isRead = true
                notiDBHelper.updateItemById(this)
            }
        }

        if (cancelId != -1) {
            nm.cancel(cancelId)
        }
    }
}
