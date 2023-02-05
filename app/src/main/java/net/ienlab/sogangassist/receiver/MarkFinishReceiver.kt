package net.ienlab.sogangassist.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.ienlab.sogangassist.constant.IntentKey
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
                val item = lmsDatabase?.getDao()?.get(id.toLong())
                if (item != null) {
                    item.isFinished = true
                    lmsDatabase?.getDao()?.update(item)
                }
            }

            nm.cancel(693000 + id.toInt())
        }
    }
}
