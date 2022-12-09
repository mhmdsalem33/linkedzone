package com.example.linkedzone.ui.viewstate

import com.example.linkedzone.domain.models.User

sealed class SearchViewState {
    object Idle                                 : SearchViewState()
    object Loading                              : SearchViewState()
    object EmptyData                            : SearchViewState()
    data class Success(val data : List<User>)   : SearchViewState()
    data class Error(val message : String)      : SearchViewState()


}