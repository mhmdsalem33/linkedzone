package com.example.linkedzone.domain.models

data class StoryModel(
  val storyBy : String ,
  val storyAt : Long,
  val storyId : String,
  val storyImage : String
){
  constructor() :this("" , 0 , "" , "")
}