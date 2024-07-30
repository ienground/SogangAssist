package net.ienlab.sogangassist.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import net.ienlab.sogangassist.constant.Intents
import net.ienlab.sogangassist.constant.Notifications
import net.ienlab.sogangassist.data.lms.LmsDatabase
import net.ienlab.sogangassist.data.lms.LmsOfflineRepository
import net.ienlab.sogangassist.data.lms.LmsRepository

class MarkFinishReceiver : BroadcastReceiver() {

    private lateinit var nm: NotificationManager
    private lateinit var lmsRepository: LmsRepository

    override fun onReceive(context: Context, intent: Intent) {
        nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        lmsRepository = LmsOfflineRepository(LmsDatabase.getDatabase(context).getDao())

        val id = intent.getLongExtra(Intents.Key.ITEM_ID, -1)

        CoroutineScope(Dispatchers.IO).launch {
            val entity = lmsRepository.getStream(id).first() ?: return@launch
            entity.isFinished = true
            lmsRepository.upsert(entity)

            nm.cancel(Notifications.Id.TIME_REMINDER + id.toInt())
        }
    }
}
