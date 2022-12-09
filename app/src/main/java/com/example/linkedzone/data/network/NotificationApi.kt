package com.example.linkedzone.data.network

import com.example.linkedzone.base.Constants.Companion.SERVER_KEY
import mhmd.salem.chatkotlin.Notifications.PushNotification
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationApi {

    @Headers("Authorization: Key= $SERVER_KEY" , "Content_Type:application/json")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification : PushNotification
    ): Response<ResponseBody>
}