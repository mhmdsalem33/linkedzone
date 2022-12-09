package com.example.linkedzone.ui.viewstate

import com.example.linkedzone.domain.models.PostModel
import com.example.linkedzone.domain.models.Story
import com.example.linkedzone.domain.models.StoryModel
import com.example.linkedzone.domain.models.User
import omari.hamza.storyview.model.MyStory

sealed class HomeViewState {
    object Idle : HomeViewState()
    object Loading : HomeViewState()
    object EmptyData : HomeViewState()
    data class Success(val data : User) : HomeViewState()
    data class SuccessStory(val data :List<Story>)  : HomeViewState()
    data class SuccessPosts(val data : List<PostModel>)  : HomeViewState()
    data class Error(val message : String) : HomeViewState()
}