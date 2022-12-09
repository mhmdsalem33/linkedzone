package com.example.linkedzone.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linkedzone.domain.models.PostModel
import com.example.linkedzone.domain.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*
import javax.inject.Inject


@HiltViewModel
class AddPostViewModel @Inject constructor(
    private val auth       : FirebaseAuth      ,
    private val firestore  : FirebaseFirestore ,
    private val application: Application ,
    private val storage: FirebaseStorage
) :ViewModel()
{

    private var _getMyInformationStateFlow = MutableStateFlow<User?>(null)
    var getMyInformationStateFlow : StateFlow<User?> = _getMyInformationStateFlow

    init {
        getMyInformation()

    }

    private fun getMyInformation() = viewModelScope.launch(Dispatchers.IO){
        firestore.collection("users").document("${auth.currentUser?.uid}")
            .addSnapshotListener{
                snapshot , error ->
                if (error  != null)
                {
                    Log.d("testApp" , error.message.toString())
                    return@addSnapshotListener
                }
                else
                {
                    snapshot?.let {
                        val user = snapshot.toObject(User::class.java)
                        viewModelScope.launch { _getMyInformationStateFlow.emit(user) }
                    }
                }
            }
    }


  companion object{
      var uri : Uri ? = null
      var postDescription : String  = ""

  }

    fun imageUri(){
        val postImagePath = storage.getReference().child("posts").child(auth.currentUser?.uid.toString())
            .child(Date().time.toString() + auth.currentUser?.uid)
        uri?.let {
            Log.d("testApp" , "img viewModel $it")
            postImagePath.putFile(it)
                .addOnSuccessListener {
                    postImagePath.downloadUrl.addOnSuccessListener { uri ->
                        val uniqueId = firestore.collection("posts").document().id
                        val post = PostModel(
                            postId          = uniqueId             ,
                            postImage       = uri.toString()       ,
                            postedBy        = auth.uid.toString()  ,
                            postedAt        = Date().time          ,
                            shareTime       = Date().time          ,
                            postDescription = postDescription)
                        uploadPost(post)
                    }
                }
        } ?: viewModelScope.launch {
            val uniqueId = firestore.collection("posts").document().id
            val post = PostModel(
                postId          = uniqueId  ,
                postedBy        = auth.uid.toString(),
                postedAt        = Date().time ,
                shareTime       = Date().time,
                postDescription = postDescription)
                uploadPost(post)
        }
    }

    private var _checkPostStatus = MutableStateFlow<String?>(null)
    var checkPostStatus : StateFlow<String?> = _checkPostStatus

    fun uploadPost(post : PostModel)  = viewModelScope.launch (Dispatchers.IO){
        firestore.collection("posts").document(post.postId).set(post)
            .addOnSuccessListener {
               Toast.makeText(application, "Posted Successfully", Toast.LENGTH_SHORT).show()
                viewModelScope.launch { _checkPostStatus.emit("Success") }
            }
            .addOnFailureListener{
                viewModelScope.launch { _checkPostStatus.emit(it.message) }
            }
    }

}