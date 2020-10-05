package net.ienlab.sogangassist

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class MarkFinishReceiver : BroadcastReceiver() {

    lateinit var nm: NotificationManager
    lateinit var dbHelper: DBHelper

    override fun onReceive(context: Context, intent: Intent) {
        nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        dbHelper = DBHelper(context, dbName, null, dbVersion)

        val id = intent.getIntExtra("ID", -1)
        dbHelper.getItemById(id).let {
            it.isFinished = true
            dbHelper.updateItemById(it)
        }

        nm.cancel(693000 + id)
    }
}
