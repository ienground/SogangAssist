package net.ienlab.sogangassist.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import net.ienlab.sogangassist.database.*

class MarkFinishReceiver : BroadcastReceiver() {

    lateinit var nm: NotificationManager
    lateinit var dbHelper: DBHelper

    override fun onReceive(context: Context, intent: Intent) {
        nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        dbHelper = DBHelper(context, dbName, dbVersion)

        val id = intent.getIntExtra("ID", -1)
        dbHelper.getItemById(id).apply {
            isFinished = true
            dbHelper.updateItemById(this)
        }

        nm.cancel(693000 + id)
    }
}
