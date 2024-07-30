package net.ienlab.sogangassist.receiver

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.ienlab.sogangassist.constant.Intents
import net.ienlab.sogangassist.constant.Notifications
import net.ienlab.sogangassist.constant.PendingReq
import net.ienlab.sogangassist.data.lms.LmsDatabase
import net.ienlab.sogangassist.data.lms.LmsOfflineRepository
import net.ienlab.sogangassist.data.lms.LmsRepository
import net.ienlab.sogangassist.utils.Utils.deleteLmsSchedule

class DeleteMissReceiver : BroadcastReceiver() {

    private lateinit var nm: NotificationManager
    private lateinit var am: AlarmManager

    private lateinit var lmsRepository: LmsRepository

    override fun onReceive(context: Context, intent: Intent) {
        nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        lmsRepository = LmsOfflineRepository(LmsDatabase.getDatabase(context).getDao())

        val id = intent.getLongExtra(Intents.Key.ITEM_ID, -1)

        nm.cancel(Notifications.Id.REGISTER + id.toInt())

        CoroutineScope(Dispatchers.IO).launch {
            val entity = lmsRepository.getStream(id).first() ?: return@launch
            lmsRepository.delete(entity)
            deleteLmsSchedule(context, am, entity)
        }
    }
}
