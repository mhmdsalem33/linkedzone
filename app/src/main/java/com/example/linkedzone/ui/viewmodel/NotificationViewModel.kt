package com.example.linkedzone.ui.viewmodel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linkedzone.domain.models.NotificationModel
import com.example.linkedzone.domain.repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import mhmd.salem.chatkotlin.Notifications.PushNotification
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val firestore: FirebaseFirestore ,
    private val auth : FirebaseAuth ,
    private val application: Application ,
    private val notificationRepository: NotificationRepository
) : ViewModel()
{


    private var _getMyNotification = MutableStateFlow<List<NotificationModel>>(emptyList())
    var getMyNotification : StateFlow<List<NotificationModel>> = _getMyNotification

    init {
        getMyNotification()
    }

    private fun getMyNotification() = viewModelScope.launch(Dispatchers.IO){
        firestore.collection("notification").document(auth.currentUser?.uid.toString()).collection("myNotification")
            .orderBy("notificationAt" , Query.Direction.DESCENDING)
            .addSnapshotListener{ snapshot ,  error ->
                error?.let {
                    Toast.makeText(application ,  error.message.toString(), Toast.LENGTH_SHORT).show()
                } ?: snapshot?.let {
                    val notification = snapshot.toObjects(NotificationModel::class.java)
                    viewModelScope.launch { _getMyNotification.emit(notification) }
                }
            }
    }

    private var _observeCountOfUnReadNotifications = MutableLiveData<Int?>()
    var observeCountOfUnReadNotifications : LiveData<Int?> = _observeCountOfUnReadNotifications

    init {
        getUnReadNotificationCount()
    }
   private fun getUnReadNotificationCount() = viewModelScope.launch(Dispatchers.IO){
        firestore.collection("notification").document("${auth.currentUser?.uid}")
            .collection("myNotification").whereEqualTo("checkOpen" , false)
            .addSnapshotListener{ unReadNotifications , error ->
                if (error != null)
                {
                    Log.d("testApp" , error.message.toString())
                }
                else
                {
                    if (unReadNotifications != null)
                    {
                        val count = unReadNotifications.size()
                        _observeCountOfUnReadNotifications.postValue(count)
                    }
                }
            }
    }


    fun sendNotificationWithMessage(notification : PushNotification) = viewModelScope.launch(Dispatchers.IO){
        try {
            val response = notificationRepository.sendNotificationWithMessage(notification)
            if (response.isSuccessful)
            {
                Log.d("testApp" , "Success to send notification with message")
                Log.d("testApp" , response.body().toString())
            }
            else
            {
                Log.d("testApp" , response.message().toString())
                Log.d("testApp" , response.errorBody().toString())
                Log.d("testApp" , response.code().toString())
            }

        }catch (e : Exception)
        {
           // Toast.makeText(application, e.message, Toast.LENGTH_SHORT).show()
            Log.d("testApp" , e.message.toString())
        }
    }

}