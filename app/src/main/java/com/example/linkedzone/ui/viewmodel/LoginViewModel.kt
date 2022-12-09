package com.example.linkedzone.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linkedzone.ui.viewstate.LoginViewState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor( private val auth : FirebaseAuth ) : ViewModel() {


    private var _login = MutableStateFlow<LoginViewState>(LoginViewState.Idle)
    var login : StateFlow<LoginViewState> = _login

    fun loginUser(email : String , password : String){
        runBlocking { _login.emit(LoginViewState.Loading) }
        auth.signInWithEmailAndPassword(email , password)
            .addOnSuccessListener {
                viewModelScope.launch {
                    it.user?.let {
                        _login.emit(LoginViewState.Success(it))

                    } ?: _login.emit(LoginViewState.EmptyData)
                }
            }
            .addOnFailureListener {
               viewModelScope.launch {  _login.emit(LoginViewState.Error(it.message.toString())) }
            }
    }

}