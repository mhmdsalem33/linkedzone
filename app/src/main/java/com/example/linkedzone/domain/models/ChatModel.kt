package com.example.linkedzone.domain.models

data class ChatModel(
    val message    : String,
    val receiverId : String,
    val senderId   : String,
    val image      : String,
    val messageId  : String
){
    constructor() :this("" ,"" ,"" ,""  ,"")
}
