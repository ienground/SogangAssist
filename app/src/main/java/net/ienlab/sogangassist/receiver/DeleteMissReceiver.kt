package net.ienlab.sogangassist.receiver

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import net.ienlab.sogangassist.database.*

class DeleteMissReceiver : BroadcastReceiver() {

    lateinit var nm: NotificationManager
    lateinit var am: AlarmManager
    lateinit var dbHelper: DBHelper

    override fun onReceive(context: Context, intent: Intent) {
        nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        dbHelper = DBHelper(context, DBHelper.dbName, DBHelper.dbVersion)

        val id = intent.getIntExtra("ID", -1)
        dbHelper.deleteData(id)

        for (i in 0 until 5) {
            val notiIntent = Intent(context, TimeReceiver::class.java).apply { putExtra("ID", id) }
            val pendingIntent = PendingIntent.getBroadcast(context, id * 100 + i + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            am.cancel(pendingIntent)
        }

        nm.cancel(699000 + id)
    }
}
