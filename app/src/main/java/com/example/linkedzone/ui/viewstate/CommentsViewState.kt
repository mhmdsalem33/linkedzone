package com.example.linkedzone.ui.viewstate

import com.example.linkedzone.domain.models.CommentModel

sealed class CommentsViewState {
    object Loading : CommentsViewState()
    object Idle    : CommentsViewState()
    object EmptyData : CommentsViewState()
    data class Success(val data : List<CommentModel>) : CommentsViewState()
    data class Error(val message : String) : CommentsViewState()
}