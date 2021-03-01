package net.ienlab.sogangassist.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import net.ienlab.sogangassist.DBHelper
import net.ienlab.sogangassist.dbName
import net.ienlab.sogangassist.dbVersion

class MarkFinishReceiver : BroadcastReceiver() {

    lateinit var nm: NotificationManager
    lateinit var dbHelper: DBHelper

    override fun onReceive(context: Context, intent: Intent) {
        nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        dbHelper = DBHelper(context, dbName, dbVersion)

        val id = intent.getIntExtra("ID", -1)
        dbHelper.getItemById(id).let {
            it.isFinished = true
            dbHelper.updateItemById(it)
        }

        nm.cancel(693000 + id)
    }
}
