package net.ienlab.sogangassist.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.*
import net.ienlab.sogangassist.constant.IntentID
import net.ienlab.sogangassist.constant.IntentKey
import net.ienlab.sogangassist.constant.NotificationId
import net.ienlab.sogangassist.room.LMSDatabase

class MarkFinishReceiver : BroadcastReceiver() {

    lateinit var nm: NotificationManager

    private var lmsDatabase: LMSDatabase? = null

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        lmsDatabase = LMSDatabase.getInstance(context)

        val id = intent.getLongExtra(IntentKey.ITEM_ID, -1)

        if (id != -1L) {
            GlobalScope.launch(Dispatchers.IO) {
                val item = lmsDatabase?.getDao()?.get(id)
                item?.let {
                    it.isFinished = true
                    lmsDatabase?.getDao()?.update(it)

                    withContext(Dispatchers.Main) {
                        LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(IntentID.MARKING_RESULT).apply {
                            putExtra(IntentKey.ITEM_ID, id)
                        })
                    }
                }
            }

            nm.cancel(NotificationId.TIME_REMINDER + id.toInt())
        }
    }
}
