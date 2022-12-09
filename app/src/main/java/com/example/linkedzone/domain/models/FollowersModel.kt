package com.example.linkedzone.domain.models

import java.io.Serializable

data class FollowersModel(
    val followedBy    : String ,
    val followedAt    : String ,
    val profileImage  : String ,
    val followersName : String ,
) : Serializable
{
    constructor() :this("" ,  "" , "" , "")
}
