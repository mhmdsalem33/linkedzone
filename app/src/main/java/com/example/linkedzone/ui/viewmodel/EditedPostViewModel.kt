package com.example.linkedzone.ui.viewmodel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linkedzone.domain.models.PostModel
import com.example.linkedzone.domain.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditedPostViewModel @Inject constructor(
    private val auth : FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val application: Application
) : ViewModel()
{

    private var _getUserInformationLiveData = MutableLiveData<User?>()
    var getUserInformationLiveData : LiveData<User?> = _getUserInformationLiveData

    fun getUserInformation(uid :String) =  viewModelScope.launch(Dispatchers.IO){
        firestore.collection("users").document(uid).addSnapshotListener{ snapshot , error ->
            if (error != null)
            {
                Toast.makeText(application, error.message.toString(), Toast.LENGTH_SHORT).show()
            }
            else
            {
                snapshot?.let {
                    val user = it.toObject(User::class.java)
                        user?.let {
                            _getUserInformationLiveData.postValue(user)
                    }

                }
            }
        }
    }

    private var _getPostInformationLiveData = MutableLiveData<PostModel?>()
    var getPostInformationLiveData : LiveData<PostModel?> = _getPostInformationLiveData

    fun getPostInformation(postId: String) = viewModelScope.launch(Dispatchers.IO){
        firestore.collection("posts").document(postId).addSnapshotListener{snapshot , error ->
            if (error != null)
            {
                Log.d("testApp" , error.message.toString())
            }
            else
            {
                snapshot?.let {
                    val post = it.toObject(PostModel::class.java)
                        post?.let { postInformation ->
                            _getPostInformationLiveData.postValue(postInformation)
                        }
                }
            }
        }
    }

    private var _statusOfUpdatePost = MutableLiveData<String?>()
    var statusOfUpdatePost :LiveData<String?> = _statusOfUpdatePost

    fun updatePost(postDescription : String , postImage : String , postId: String) =  viewModelScope.launch(Dispatchers.IO){
            val postMap = HashMap<String , Any>()
                postMap["postDescription"] = postDescription
                postMap["postImage"]       = postImage
        firestore.collection("posts").document(postId).update(postMap)
            .addOnSuccessListener {
                Toast.makeText(application, "Post updated success ", Toast.LENGTH_LONG).show()
                _statusOfUpdatePost.value = "Success"
            }
            .addOnFailureListener{
                Toast.makeText(application, it.message.toString(), Toast.LENGTH_SHORT).show()
            }
    }
    fun updatePostDescription(postDescription : String , postId: String) =  viewModelScope.launch(Dispatchers.IO){
        val postMap = HashMap<String , Any>()
            postMap["postDescription"] = postDescription
        firestore.collection("posts").document(postId).update(postMap)
            .addOnSuccessListener {
                Toast.makeText(application, "Post updated success ", Toast.LENGTH_LONG).show()
                _statusOfUpdatePost.value = "Success"
            }
            .addOnFailureListener{
                Toast.makeText(application, it.message.toString(), Toast.LENGTH_SHORT).show()
            }
    }
    fun updatePostImage(postImage : String , postId: String) =  viewModelScope.launch(Dispatchers.IO){
        val postMap = HashMap<String , Any>()
            postMap["postImage"] = postImage
        firestore.collection("posts").document(postId).update(postMap)
            .addOnSuccessListener {
                Toast.makeText(application, "Post updated success ", Toast.LENGTH_LONG).show()
                _statusOfUpdatePost.value   = "Success"
            }
            .addOnFailureListener{
                Toast.makeText(application, it.message.toString(), Toast.LENGTH_SHORT).show()
            }
    }


    private var _getStatusOfDeletePost = MutableLiveData<String?>()
    var getStatusOfDeletePost :LiveData<String?> = _getStatusOfDeletePost

    fun deletePost(postId : String) = viewModelScope.launch(Dispatchers.IO){
        firestore.collection("posts").document(postId).delete()
            .addOnSuccessListener {
                _getStatusOfDeletePost.value = "Success to delete"
            }
    }

}