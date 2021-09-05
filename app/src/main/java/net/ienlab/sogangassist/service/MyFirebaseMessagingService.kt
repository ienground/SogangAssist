package net.ienlab.sogangassist.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import net.ienlab.sogangassist.R
import net.ienlab.sogangassist.activity.*
import net.ienlab.sogangassist.constant.ChannelId

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.from!!)
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            if (true) {
            } else {
                handleNow()
            }
        }
        if (remoteMessage.notification != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.notification!!.body!!)
            sendNotification(remoteMessage.notification!!.title, remoteMessage.notification!!.body)
        }
    }

    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.d(TAG, p0)
    }

    private fun sendNotification(messageTitle: String?, messageBody: String?) {
        val intent = Intent(this, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, ChannelId.FIREBASE_ID)
                .setSmallIcon(R.drawable.ic_icon)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setColor(ContextCompat.getColor(applicationContext, R.color.colorAccent))

                .setContentIntent(pendingIntent)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = getString(R.string.real_app_name)
            val channel = NotificationChannel(ChannelId.FIREBASE_ID, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}

