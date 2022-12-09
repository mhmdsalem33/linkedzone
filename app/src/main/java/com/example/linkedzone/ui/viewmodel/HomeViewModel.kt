package com.example.linkedzone.ui.viewmodel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linkedzone.domain.models.PostModel
import com.example.linkedzone.domain.models.Story
import com.example.linkedzone.domain.models.StoryModel
import com.example.linkedzone.domain.models.User
import com.example.linkedzone.ui.viewstate.HomeViewState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import omari.hamza.storyview.model.MyStory
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val auth     : FirebaseAuth     ,
    private val firestore: FirebaseFirestore,
    private val application: Application
):ViewModel()
{

    private var _getUserInformationStateFlow = MutableStateFlow<HomeViewState>(HomeViewState.Idle)
    var getUserInformationStateFlow : StateFlow<HomeViewState> = _getUserInformationStateFlow

    init {
        getMyInformation()
    }
    private fun getMyInformation() =  viewModelScope.launch (Dispatchers.IO){
        firestore.collection("users").document(auth.currentUser?.uid.toString())
            .addSnapshotListener{ snapshot , error ->
                if (error != null)
                {
                    Toast.makeText(application, error.message.toString(), Toast.LENGTH_SHORT).show()
                }
                else
                {
                    if (snapshot != null && snapshot.exists())
                    {
                        val user = snapshot.toObject(User::class.java)
                        viewModelScope.launch {
                            user?.let {
                                _getUserInformationStateFlow.emit(HomeViewState.Success(it))
                            }
                        }
                    }
                }
            }
    }

    private var _getPostsStateFlow = MutableStateFlow<HomeViewState>(HomeViewState.Idle)
    var getPostsStateFlow : StateFlow<HomeViewState> = _getPostsStateFlow

    init {
        getPosts()
    }

  private fun getPosts() = viewModelScope.launch(Dispatchers.IO){
        firestore.collection("posts").orderBy("shareTime", Query.Direction.DESCENDING).addSnapshotListener{
            snapshot , error ->
            if (error != null)
            {
                Log.d("testApp" , error.message.toString())
            }
            else
            {
                snapshot?.let {
                    val posts  =  snapshot.toObjects(PostModel::class.java)
                    val filter =  posts.reversed()
                    viewModelScope.launch { _getPostsStateFlow.emit(HomeViewState.SuccessPosts(posts)) }
                }
            }
        }
    }

    private var _getStoriesStateFlow = MutableStateFlow<HomeViewState>(HomeViewState.Idle)
    var getStoriesStateFlow : StateFlow<HomeViewState> = _getStoriesStateFlow

    init
    {
        getStories()
    }

   private fun getStories() = viewModelScope.launch(Dispatchers.IO){
       firestore.collection("stories")
           .orderBy("time" , Query.Direction.DESCENDING)
           .addSnapshotListener{ data , error ->
               if (error !=  null)
               {
                   Toast.makeText(application, error.message, Toast.LENGTH_SHORT).show()
               }
               else
               {
                   if (data != null)
                   {
                       val story = data.toObjects(Story::class.java)
                      viewModelScope.launch {  _getStoriesStateFlow.emit(HomeViewState.SuccessStory(story)) }
                   }
               }
           }
    }
}