package net.ienlab.sogangassist.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.ienlab.sogangassist.constant.DefaultValue
import net.ienlab.sogangassist.constant.Intents
import net.ienlab.sogangassist.constant.PendingReq
import net.ienlab.sogangassist.constant.Pref
import net.ienlab.sogangassist.data.lms.LmsDatabase
import net.ienlab.sogangassist.data.lms.Lms
import net.ienlab.sogangassist.dataStore
import net.ienlab.sogangassist.utils.Utils.setDayReminder
import net.ienlab.sogangassist.utils.Utils.setLmsSchedule
import net.ienlab.sogangassist.utils.Utils.timeInMillis
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class BootDeviceReceiver : BroadcastReceiver() {

    private lateinit var am: AlarmManager
    override fun onReceive(context: Context, intent: Intent) {
        am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> {
                scheduleLms(context)
                scheduleReminder(context)
            }
        }
    }

    private fun scheduleLms(context: Context) {
        val lmsDatabase = LmsDatabase.getDatabase(context)

        CoroutineScope(Dispatchers.IO).launch {
            val flow = lmsDatabase.getDao().getAll()
            for (entity in flow.first()) {
                if (!entity.isFinished && entity.endTime > System.currentTimeMillis()) {
                    setLmsSchedule(context, am, entity)
                }
            }
        }
    }

    private fun scheduleReminder(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val datastore = context.dataStore

            val enableMorningReminder = datastore.data.map { it[Pref.Key.ALLOW_MORNING_REMINDER] ?: Pref.Default.ALLOW_MORNING_REMINDER }.first()
            val enableNightReminder = datastore.data.map { it[Pref.Key.ALLOW_NIGHT_REMINDER] ?: Pref.Default.ALLOW_NIGHT_REMINDER }.first()
            val morningReminder = datastore.data.map { it[Pref.Key.TIME_MORNING_REMINDER] ?: Pref.Default.TIME_MORNING_REMINDER }.first()
            val nightReminder = datastore.data.map { it[Pref.Key.TIME_NIGHT_REMINDER] ?: Pref.Default.TIME_NIGHT_REMINDER }.first()

            setDayReminder(context, Intents.Value.ReminderType.MORNING, enableMorningReminder, morningReminder)
            setDayReminder(context, Intents.Value.ReminderType.NIGHT, enableNightReminder, nightReminder)
        }
    }
}