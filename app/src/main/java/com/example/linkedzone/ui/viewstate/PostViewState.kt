package com.example.linkedzone.ui.viewstate

sealed class PostViewState {
    object Idle  : PostViewState()
    data class Success(val success : String)   : PostViewState()
    data class Error(val message   : String)   : PostViewState()
}