package com.example.linkedzone.domain.models

data class CommentModel(val commentBody  : String , val commentedAt : Long , val commentedBy : String , val commentId : String , val sortingTime : String )
{
    constructor() : this("",0,"","" , "")
}
