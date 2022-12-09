package com.example.linkedzone.notifications.firebaseservices


import android.app.*
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent.FLAG_ONE_SHOT
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.linkedzone.R
import com.example.linkedzone.base.MyApplication
import com.example.linkedzone.ui.activites.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random



class FirebaseServices :FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val   notificationIntent = Intent(this , MainActivity::class.java)
        val   pendingIntent      = PendingIntent.getActivity(this , 0 , notificationIntent , 0)

        // 3 create notification
        val notification = NotificationCompat.Builder(this , MyApplication.channel_Id)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["message"])
            .setSmallIcon(R.drawable.ic_baseline_notifications_24)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1 , notification)
        stopForeground(false)
    }
}










