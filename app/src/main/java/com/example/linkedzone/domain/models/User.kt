package com.example.linkedzone.domain.models

import java.io.Serializable

data class User(
    val userEmail    : String = "",
    val firstName    : String = "",
    val lastName     : String = "",
    val uid          : String = "",
    var profileImage : String = "",
    var userCover    : String = "" ,
    var followersCount : Int = 0   ,
    val about : String = "" ,
    val online : Boolean = false,
    val typing : String = ""

):Serializable {
    constructor() : this("", "", "", "" ,"" ,"" , 0 , "" , false , "")
}
