package com.example.linkedzone.ui.viewmodel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linkedzone.domain.models.FollowersModel
import com.example.linkedzone.domain.models.PostModel
import com.example.linkedzone.domain.models.User
import com.example.linkedzone.ui.viewstate.ProfileViewState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.internal.artificialFrame
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firestore  : FirebaseFirestore ,
    private val auth       : FirebaseAuth      ,
    private val application: Application       ,
 ) : ViewModel() {


     private var _getUserInformationStateFlow = MutableStateFlow<User?>(null)
     var getUserInformationStateFlow : StateFlow<User?> = _getUserInformationStateFlow

     fun getUserInformation(uid: String) = viewModelScope.launch(Dispatchers.IO){
        firestore.collection("users").document(uid).addSnapshotListener{
            user , error ->
            if (error != null)
            {
                Toast.makeText( application , error.message.toString(), Toast.LENGTH_SHORT).show()
            }
            else
            {
                user?.let {
                    viewModelScope.launch {
                        val userInformation = user.toObject(User::class.java)
                        _getUserInformationStateFlow.emit(userInformation)
                    }
                }
            }
        }
    }

    private var _getMyInformationStateFlow = MutableStateFlow<User?>(null)
    var getMyInformationStateFlow : StateFlow<User?> = _getMyInformationStateFlow

    init {
        getMyInformation()
    }
   private fun getMyInformation() = viewModelScope.launch(Dispatchers.IO){
        firestore.collection("users").document(auth.uid.toString()).addSnapshotListener{
                user , error ->
            if (error != null)
            {
                Toast.makeText( application , error.message.toString(), Toast.LENGTH_SHORT).show()
            }
            else
            {
                user?.let {
                    viewModelScope.launch {
                        val userInformation = user.toObject(User::class.java)
                        _getMyInformationStateFlow.emit(userInformation)
                    }
                }
            }
        }
    }

    private var _getMyFollowersInformationStateFlow = MutableStateFlow<ProfileViewState>(ProfileViewState.Idle)
     var getMyFollowersInformationStateFlow : StateFlow<ProfileViewState> = _getMyFollowersInformationStateFlow

     fun getMyFollowersInformation( uid : String ) = viewModelScope.launch(Dispatchers.IO){
        firestore.collection("users").document(uid).collection("followers")
            .addSnapshotListener{
                followers  , error ->
                if (error != null)
                {
                   viewModelScope.launch {  _getMyFollowersInformationStateFlow.emit(ProfileViewState.Error(error.message.toString())) }
                }
                else
                {
                    viewModelScope.launch {
                        followers?.let {
                            val myFollowers = followers.toObjects(FollowersModel::class.java)
                              _getMyFollowersInformationStateFlow.emit(ProfileViewState.Success(myFollowers))
                        } ?:  _getMyFollowersInformationStateFlow.emit(ProfileViewState.EmptyData)
                    }
                }
            }
    }

    private var _getMyFollowersCount = MutableStateFlow<Int?>(null)
    var getMyFollowersCount : StateFlow<Int?> = _getMyFollowersCount

    fun getCountOfMyFollower(uid : String) = viewModelScope.launch(Dispatchers.IO){
        firestore.collection("users").document(uid).collection("followers")
            .addSnapshotListener{
                snapshot , error ->
                if (error != null)
                {
                    Log.d("testApp" , error.message.toString())
                    return@addSnapshotListener
                }
                else
                {
                    if (snapshot != null)
                    {
                        viewModelScope.launch { _getMyFollowersCount.emit(snapshot.size()) }
                    }
                }
            }
    }


    private var _getUsersPostsStateFlow = MutableStateFlow<ProfileViewState>(ProfileViewState.Idle)
    var getUserPostsStateFlow : StateFlow<ProfileViewState> = _getUsersPostsStateFlow

    fun getProfilePosts(uid: String) =  viewModelScope.launch(Dispatchers.IO){
        firestore.collection("posts")
           // .whereEqualTo("postedBy"   , uid)
            .orderBy("postedAt"        , Query.Direction.DESCENDING)
            .addSnapshotListener{ snapshot  , error ->
            if(error != null)
            {
                Log.d("testApp" , error.message.toString())
                return@addSnapshotListener
            }
            else
            {
                if (snapshot != null)
                {
                    val posts  =  snapshot.toObjects(PostModel::class.java)
                    val filter =  posts.filter { it.postedBy == uid }
                    viewModelScope.launch { _getUsersPostsStateFlow.emit(ProfileViewState.SuccessPosts(filter)) }
                }
            }
        }
    }


}