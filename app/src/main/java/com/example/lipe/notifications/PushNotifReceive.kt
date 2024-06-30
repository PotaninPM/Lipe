package com.example.lipe.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.lipe.MainActivity
import com.example.lipe.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.Locale

class PushNotifReceive : FirebaseMessagingService() {

    private lateinit var auth: FirebaseAuth

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser?.uid
        if (user != null) {
            if (remoteMessage.data.isNotEmpty()) {
                val title = remoteMessage.data["title"]
                val message = remoteMessage.data["message"]
                val type = remoteMessage.data["type"]!!

                Log.d("INFOG", title.toString())

                understandType(title, message, type)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            Log.d("INFOG1", "$userId!!!")
            val dbRef_user = FirebaseDatabase.getInstance().getReference("users/$userId/userToken")
            dbRef_user.setValue(token)
        } else {
            Log.d("INFOG1", "Текущий пользователь равен null")
        }
    }

    private fun understandType(title: String?, message: String?, type: String) {

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("fragmentToLoad", "RatingFragment")
        }

        showNotification(title, message, type, intent)
    }

    private fun showNotification(title: String?, message: String?, type: String, intent: Intent) {

        val notificationId = when (type) {
            "friendship_request" -> 0
            "new_event" -> 1
            "accept_friendship" -> 2
            "rating_update" -> 3
            "new_message_chat" -> 4
            "new_message_group" -> 5
            else -> 4
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = when (type) {
            "friendship_request" -> "friendship_request"
            "new_event" -> "new_event"
            "accept_friendship" -> "accept_friendship"
            "rating_update" -> "rating_update"
            else -> "channel_id"
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.planet_icon)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = when (type) {
                "friendship_request" -> "friendship_request"
                "new_event" -> "new_event"
                "accept_friendship" -> "accept_friendship"
                "rating_update" -> "rating_update"
                else -> "channel_id"
            }
            val channel =
                NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // Use unique notificationId here
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}
