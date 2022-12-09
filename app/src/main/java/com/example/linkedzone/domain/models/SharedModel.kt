package com.example.linkedzone.domain.models

data class SharedModel(
    val shareName       : String ="",
    val shareTime       : Long = 0 ,
    val sharedId        : String ="",
    val postId          : String ="",
    val shareProfileImage : String = ""
){
    constructor() : this("" , 0 , "" ,"" , "")
}
