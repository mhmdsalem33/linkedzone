package com.example.linkedzone.domain.models

data class NotificationModel(
    val notificationBy: String = "",
    val notificationAt: Long = 0,
    val type: String ="",
    val postId: String ="",
    val postedBy: String ="",
    val checkOpen: Boolean = false,
    val notificationId : String = ""
)
{
    constructor():this("" , 0 , "" , "" , "" ,false , "")
}
