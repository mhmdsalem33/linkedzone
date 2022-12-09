package com.example.linkedzone.Services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.linkedzone.R
import com.example.linkedzone.base.MyApplication.Companion.channel_Id
import com.example.linkedzone.ui.activites.MainActivity
import com.example.linkedzone.ui.viewmodel.AddPostViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage



class PostService() : Service() {

    private lateinit var addPostViewModel: AddPostViewModel

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }


    override fun onCreate() {
        super.onCreate()
        addPostViewModel = AddPostViewModel(FirebaseAuth.getInstance() , FirebaseFirestore.getInstance() , application , FirebaseStorage.getInstance())
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

       // addPostViewModel.imageUri()

        //2 Create Intent
        val   notificationIntent = Intent(this , MainActivity::class.java)
        val   pendingIntent      = PendingIntent.getActivity(this , 0 , notificationIntent , 0)


        val progressMaxValue = 100
        val progressCurrentValue = 0


        // 3 create notification
        val notification = NotificationCompat.Builder(this , channel_Id)
            .setContentTitle("Post Service")
            .setContentText("Success Post")
            .setSmallIcon(R.drawable.ic_baseline_notifications_24)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setProgress(progressMaxValue , 0       , false)
            .setProgress(progressMaxValue , 100 , false)
            //.setProgress(progressMaxValue , thirdProgress  , false)
            //.setProgress(progressMaxValue , lastProgress   , false)
            .build()

        startForeground(1 , notification)
        stopForeground(false)



        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}