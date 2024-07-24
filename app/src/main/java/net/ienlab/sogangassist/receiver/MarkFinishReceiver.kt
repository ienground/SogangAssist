package net.ienlab.sogangassist.receiver

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.*
import net.ienlab.sogangassist.constant.Intents
import net.ienlab.sogangassist.constant.Notifications
import net.ienlab.sogangassist.data.lms.LmsDatabase

class MarkFinishReceiver : BroadcastReceiver() {

    lateinit var nm: NotificationManager

//    private var lmsDatabase: LmsDatabase? = null
//
    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
//        nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        lmsDatabase = LmsDatabase.getDatabase(context)
//
//        val id = intent.getLongExtra(Intents.Key.ITEM_ID, -1)
//
//        if (id != -1L) {
//            GlobalScope.launch(Dispatchers.IO) {
//                val item = lmsDatabase?.getDao()?.get(id)
//                item?.let {
//                    it.isFinished = true
//                    lmsDatabase?.getDao()?.update(it)
//
//                    withContext(Dispatchers.Main) {
//                        LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(Intents.Id.MARKING_RESULT).apply {
//                            putExtra(Intents.Key.ITEM_ID, id)
//                        })
//                    }
//                }
//            }
//
//            nm.cancel(Notifications.Id.TIME_REMINDER + id.toInt())
//        }
    }
}
