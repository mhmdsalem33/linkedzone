package com.example.linkedzone.domain.models

data class PostModel(
        val postId          : String ="" ,
        val postImage       : String ="",
        val postDescription : String ="",
        val postedBy        : String ="",
        val postedAt        : Long = 0,
        val shareName       : String ="",
        val shareTime       : Long = 0 ,
        val sharedId        : String =""

) {
        constructor() : this("" , "" , "","" , 0,"" , 0 , "")
}
