package com.example.linkedzone.ui.viewmodel

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linkedzone.domain.models.User
import com.example.linkedzone.ui.viewstate.SearchViewState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


@HiltViewModel
class SearchViewModel @Inject constructor(
    private val firestore   : FirebaseFirestore,
    private val auth        : FirebaseAuth
    ) : ViewModel(){


    private var _getAllUsersStateFlow = MutableStateFlow<SearchViewState>(SearchViewState.Idle)
    var getAllUsersStateFlow : StateFlow<SearchViewState> = _getAllUsersStateFlow

    fun searchInFirestore(searchText: String) {
        runBlocking { _getAllUsersStateFlow.emit(SearchViewState.Loading) }

        viewModelScope.launch (Dispatchers.IO){
            delay(1000)
            firestore.collection("users")
                  .orderBy("searchName")
                  .startAt(searchText)
                  .endAt("$searchText\uF8FF")
                //.limit(10)
                .get()
                .addOnCompleteListener {

                    val data = it.result.toObjects(User::class.java)
                    val filter = data.filter { users ->  users.uid != auth.currentUser?.uid } //get all other users and not get me
                    viewModelScope.launch {
                        if (data.isNotEmpty())
                        {
                            _getAllUsersStateFlow.emit(SearchViewState.Success(filter))
                        }else
                        {
                            _getAllUsersStateFlow.emit(SearchViewState.EmptyData)
                        }
                    }
                }
                .addOnFailureListener{
                    viewModelScope.launch { _getAllUsersStateFlow.emit(SearchViewState.Error(it.message.toString())) }
                }

        }

    }
}