package com.example.linkedzone.ui.viewstate

import com.example.linkedzone.domain.models.FollowersModel
import com.example.linkedzone.domain.models.PostModel

sealed class ProfileViewState {
    object Loading : ProfileViewState()
    object Idle    : ProfileViewState()
    data class Success(val data : List<FollowersModel>)   : ProfileViewState()
    data class SuccessPosts(val data : List<PostModel>)   : ProfileViewState()
    data class Error(val message : String)                : ProfileViewState()
    object EmptyData                                      : ProfileViewState()
}
