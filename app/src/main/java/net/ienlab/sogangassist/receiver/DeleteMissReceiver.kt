package net.ienlab.sogangassist.receiver

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.ienlab.sogangassist.constant.IntentKey
import net.ienlab.sogangassist.constant.NotificationId
import net.ienlab.sogangassist.constant.PendingIntentReqCode
import net.ienlab.sogangassist.room.LMSDatabase

class DeleteMissReceiver : BroadcastReceiver() {

    lateinit var nm: NotificationManager
    lateinit var am: AlarmManager

    private var lmsDatabase: LMSDatabase? = null

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        lmsDatabase = LMSDatabase.getInstance(context)

        val id = intent.getLongExtra(IntentKey.ITEM_ID, -1)
        GlobalScope.launch(Dispatchers.IO) {
            lmsDatabase?.getDao()?.delete(id)
        }

        for (i in 0 until 5) {
            val notiIntent = Intent(context, TimeReceiver::class.java).apply { putExtra(IntentKey.ITEM_ID, id) }
            val pendingIntent = PendingIntent.getBroadcast(context, PendingIntentReqCode.LAUNCH_NOTI + id.toInt() * 100 + i + 1, notiIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            am.cancel(pendingIntent)
        }

        nm.cancel(NotificationId.REGISTER + id.toInt())
    }
}
