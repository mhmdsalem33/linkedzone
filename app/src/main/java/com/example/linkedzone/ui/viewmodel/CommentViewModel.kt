package com.example.linkedzone.ui.viewmodel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linkedzone.domain.models.CommentModel
import com.example.linkedzone.domain.models.NotificationModel
import com.example.linkedzone.domain.models.PostModel
import com.example.linkedzone.ui.viewstate.CommentsViewState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CommentViewModel @Inject constructor(
    private val auth     : FirebaseAuth ,
    private val firestore: FirebaseFirestore,
    private val application: Application
) : ViewModel()
{

    private var _getLikesCount = MutableStateFlow<Int?>(null)
    var getLikesCount :StateFlow<Int?> = _getLikesCount

    fun getPostCountLikes(postId : String)  = viewModelScope.launch(Dispatchers.IO){
        firestore.collection("posts").document(postId).collection("likes")
            .addSnapshotListener{ snapshot , error ->
                if (error != null)
                {
                    Log.d("testApp" , error.message.toString())
                }
                else
                {
                  snapshot?.let { likes ->
                      viewModelScope.launch { _getLikesCount.emit(likes.size()) }
                  }
                }

            }
    }

    private var _observeStatusOfPost = MutableStateFlow<String?>(null)
    var observeStatusOfPost : StateFlow<String?> = _observeStatusOfPost
    fun postComment(postId : String ,comment : CommentModel , postedBy: String) =  viewModelScope.launch(Dispatchers.IO){
        firestore.collection("posts").document(postId).collection("comments").document(comment.commentId)
            .set(comment)
            .addOnSuccessListener {
                Toast.makeText(application, "Success to comment", Toast.LENGTH_SHORT).show()
                viewModelScope.launch { _observeStatusOfPost.emit("Success") }
                val uniqueId = firestore.collection("notification").document().id
                val notification = NotificationModel(
                    notificationAt = Date().time ,
                    notificationBy = auth.currentUser?.uid.toString(),
                    checkOpen      = false,
                    postId         = postId ,
                    postedBy       = postedBy,
                    type = "Comment",
                    notificationId = uniqueId

                )
                if (auth.currentUser?.uid  != postedBy)
                {
                        firestore.collection("notification")
                            .document(postedBy)
                            .collection("myNotification")
                            .document(uniqueId)
                            .set(notification)
                        .addOnSuccessListener {
                            Log.d("testApp" , "Success to comment with notification")
                        }
                }

            }
    }

    private var _getCommentsCount  = MutableStateFlow<Int?>(null)
    var getCommentCount : StateFlow<Int?> = _getCommentsCount

    // get Count of all comments
    fun getCommentsCount(postsId : String) =  viewModelScope.launch(Dispatchers.IO){
        firestore.collection("posts").document(postsId).collection("comments")
            .addSnapshotListener{  snapshot , error ->
                if (error != null)
                {
                    Toast.makeText(application, error.message.toString(), Toast.LENGTH_SHORT).show()
                }
                else
                {
                    snapshot?.let { comments ->
                        viewModelScope.launch { _getCommentsCount.emit(comments.size()) }
                    }
                }
            }
    }

    private var _getPostCommentsStateFlow = MutableStateFlow<CommentsViewState>(CommentsViewState.Idle)
    var getPostCommentsStateFlow : StateFlow<CommentsViewState> = _getPostCommentsStateFlow

    // Get Post Comments
    fun getPostComments(postId : String) = viewModelScope.launch(Dispatchers.IO){
        firestore.collection("posts")
            .document(postId)
            .collection("comments")
            .orderBy("commentedAt" , Query.Direction.ASCENDING)
            .addSnapshotListener{ snapshot ,  error ->
                if (error != null)
                {
                    Toast.makeText(application, error.message.toString(), Toast.LENGTH_SHORT).show()
                }
                else
                {
                       if (snapshot != null)
                       {
                           val comments = snapshot.toObjects(CommentModel::class.java)
                           comments.let {
                               viewModelScope.launch { _getPostCommentsStateFlow.emit(CommentsViewState.Success(it)) }
                           }
                       }
                }
            }
    }

    private var _getPostInformation = MutableStateFlow<PostModel?>(null)
    var getPostInformation : StateFlow<PostModel?> = _getPostInformation

    fun getPostInformation(postId: String) = viewModelScope.launch(Dispatchers.IO)
    {
        firestore.collection("posts").document(postId).addSnapshotListener{snapshot ,  error ->
            if (error != null)
            {
                Log.d("testApp" , error.message.toString())
            }
            else
            {
                if (snapshot != null && snapshot.exists())
                {
                    val post = snapshot.toObject(PostModel::class.java)
                    viewModelScope.launch { _getPostInformation.emit(post) }
                }
            }
        }
    }


}