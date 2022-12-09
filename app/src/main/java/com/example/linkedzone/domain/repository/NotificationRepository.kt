package com.example.linkedzone.domain.repository

import com.example.linkedzone.data.network.NotificationApi
import mhmd.salem.chatkotlin.Notifications.PushNotification
import javax.inject.Inject

class NotificationRepository @Inject constructor(private val notificationApi: NotificationApi) {
    suspend fun sendNotificationWithMessage( notification : PushNotification ) = notificationApi.postNotification(notification)
}