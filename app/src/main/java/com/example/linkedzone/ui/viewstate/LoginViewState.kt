package com.example.linkedzone.ui.viewstate

import com.google.firebase.auth.FirebaseUser

sealed class LoginViewState {
    object Idle                                 : LoginViewState()
    object Loading                              : LoginViewState()
    object EmptyData                            : LoginViewState()
    data class Success(val data : FirebaseUser) : LoginViewState()
    data class Error(val message : String)      : LoginViewState()

}