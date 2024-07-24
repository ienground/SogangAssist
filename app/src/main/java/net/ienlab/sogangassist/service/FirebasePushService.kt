package net.ienlab.sogangassist.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.activity.*
import net.ienlab.sogangassist.constant.FirebasePushKey
import net.ienlab.sogangassist.constant.Intents
import net.ienlab.sogangassist.constant.Notifications
import java.util.Locale

class FirebasePushService : FirebaseMessagingService() {
    private lateinit var nm: NotificationManager

    override fun handleIntent(intent: Intent?) {
        super.handleIntent(intent)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(NotificationChannel(Notifications.Channel.FIREBASE_ID, getString(R.string.real_app_name), NotificationManager.IMPORTANCE_HIGH))

        if (remoteMessage.data.isNotEmpty()) {
            sendNotification(remoteMessage.data)
        }
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    private fun sendNotification(data: Map<String, String>) {
        val title = data[FirebasePushKey.TITLE]
        val titleKor = data[FirebasePushKey.TITLE_KOR]
        val message = data[FirebasePushKey.MESSAGE]
        val messageKor = data[FirebasePushKey.MESSAGE_KOR]
        val type = data[FirebasePushKey.TYPE]?.toInt() ?: -1

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(Intents.Key.NOTI_TYPE, Intents.Value.NotiType.FIREBASE_PUSH)
            putExtra(FirebasePushKey.TITLE, title)
            putExtra(FirebasePushKey.TITLE_KOR, titleKor)
            putExtra(FirebasePushKey.MESSAGE, message)
            putExtra(FirebasePushKey.MESSAGE_KOR, messageKor)
            putExtra(FirebasePushKey.TYPE, type)
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT)

        NotificationCompat.Builder(this, Notifications.Channel.FIREBASE_ID).apply {
            setContentTitle(if (Locale.getDefault() == Locale.KOREA) titleKor else title)
            setContentText(if (Locale.getDefault() == Locale.KOREA) messageKor else message)
            setAutoCancel(true)
            setContentIntent(pendingIntent)
            color = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
            setSmallIcon(R.drawable.ic_icon)
//            setSmallIcon(when (type) {
//                FirebasePushKey.Types.NONE -> R.drawable.ic_calarm
//                FirebasePushKey.Types.LOGO -> R.drawable.ic_calarm
//                FirebasePushKey.Types.VERSION_UP -> R.drawable.ic_calarm_update
//                FirebasePushKey.Types.WARNING -> R.drawable.ic_warning
//
//                else -> R.drawable.ic_calarm
//            })

            nm.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), build())
        }
    }
}

