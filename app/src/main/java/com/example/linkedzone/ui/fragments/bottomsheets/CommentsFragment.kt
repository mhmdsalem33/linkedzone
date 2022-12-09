package com.example.linkedzone.ui.fragments.bottomsheets

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.bumptech.glide.Glide
import com.example.linkedzone.R
import com.example.linkedzone.databinding.FragmentCommentsBinding
import com.example.linkedzone.domain.models.CommentModel
import com.example.linkedzone.domain.models.NotificationModel
import com.example.linkedzone.domain.models.PostModel
import com.example.linkedzone.ui.adapters.CommentsAdapter
import com.example.linkedzone.ui.viewmodel.CommentViewModel
import com.example.linkedzone.ui.viewstate.CommentsViewState
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

@AndroidEntryPoint
class CommentsFragment : BottomSheetDialogFragment() {

    private lateinit var binding : FragmentCommentsBinding

    @Inject
    lateinit var auth      : FirebaseAuth
    @Inject
    lateinit var firestore : FirebaseFirestore

    private val commentedMvvm : CommentViewModel by viewModels()
    private lateinit var commentsAdapter: CommentsAdapter

    private lateinit var postId  : String
    private lateinit var postedBy : String


    companion object {
        fun newInstance(post : PostModel) =
            CommentsFragment().apply {
                arguments = Bundle().apply {
                    postId    = post.postId
                    postedBy  = post.postedBy
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        commentsAdapter = CommentsAdapter()

    }

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCommentsBinding.inflate(inflater , container , false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       // setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
        //setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)

      //  dialog?.window?.attributes?.gravity = Gravity.BOTTOM




        openBottomSheetWithFullScreen()

        onButtonCommentClick()
        checkIfPostSuccessToPostOrNot()
        getLikesCount()
        getCommentsCount()

        observePostComments()
        setupRecViewComments()

        onButtonLikeClick()
        onButtonDislikeClick()
        getStatusOfButtonLike()

        getPostInformation()
        onButtonExitClick()



    }


    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        dialog.window?.setSoftInputMode(             WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );
    }
    private fun onButtonExitClick() {
        binding.exit.setOnClickListener {
            this.dismiss()
        }
    }

    private fun getPostInformation() {
        lifecycleScope.launchWhenStarted {
            commentedMvvm.getPostInformation(postId)
            commentedMvvm.getPostInformation.collect{
                it?.let {
                    binding.description.text = it.postDescription
                    if (it.postImage.isNotEmpty())
                    {
                        Glide.with(requireContext()).load(it.postImage).into(binding.commentImage)
                        binding.commentImage.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun checkIfPostSuccessToPostOrNot() {
        lifecycleScope.launchWhenStarted {
            commentedMvvm.observeStatusOfPost.collect{
                it?.let {
                    if (it == "Success")
                    {
                        binding.edtComment.text.clear()
                    }
                }
            }
        }
    }

    private fun getStatusOfButtonLike() {
        firestore.collection("posts").document(postId).collection("likes")
            .document(auth.currentUser?.uid.toString())
            .addSnapshotListener{ snapshot ,  error ->
                if (error != null)
                {
                    Toast.makeText(requireContext(), error.message.toString(), Toast.LENGTH_SHORT).show()
                }
                else{
                    if (snapshot != null && snapshot.exists())
                    {
                        binding.postLike.visibility = View.GONE
                        binding.postLiked.visibility = View.VISIBLE
                    }
                    else
                    {
                        binding.postLike.visibility = View.VISIBLE
                        binding.postLiked.visibility = View.GONE
                    }
                }
            }
    }

    private fun onButtonDislikeClick() {
        binding.postLiked.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO){
                firestore.collection("posts").document(postId).collection("likes")
                    .document(auth.currentUser?.uid.toString())
                    .delete()
                    .addOnSuccessListener {
                        binding.postLike.visibility = View.VISIBLE
                        binding.postLiked.visibility = View.GONE
                    }
            }
        }
    }

    private fun onButtonLikeClick() {
        binding.postLike.setOnClickListener {
         lifecycleScope.launch(Dispatchers.IO)
         {
             val likeMap = HashMap<String , Any >()
                 likeMap["liked"] = true
             firestore.collection("posts").document(postId).collection("likes")
                 .document(auth.currentUser?.uid.toString())
                 .set(likeMap)
                 .addOnSuccessListener {
                     binding.postLike.visibility = View.GONE
                     binding.postLiked.visibility = View.VISIBLE

                     val uniqueId = FirebaseFirestore.getInstance().collection("notification").document().id
                     val notification = NotificationModel(
                         notificationAt = Date().time ,
                         notificationBy = auth.currentUser?.uid.toString(),
                         checkOpen      = false,
                         postId         = postId ,
                         postedBy       = postedBy,
                         type           = "Like" ,
                         notificationId = uniqueId
                     )
                     if (auth.currentUser?.uid  != postedBy)
                     {
                         lifecycleScope.launch(Dispatchers.IO){
                             async {
                                 firestore.collection("notification")
                                     .document(postedBy)
                                     .collection("myNotification")
                                     .document(uniqueId)
                                     .set(notification)
                                     .addOnSuccessListener {
                                         Log.d("testApp" , "Success to send Like with notification")
                                     }
                             }.await()
                         }
                     }

                 }
         }
        }
    }


    private fun getCommentsCount() {
        commentedMvvm.getCommentsCount(postId)
        lifecycleScope.launchWhenStarted {
            commentedMvvm.getCommentCount.collect{ commentsSize ->
                binding.tvComments.text = commentsSize.toString()
            }
        }
    }

    private fun getLikesCount() {
        commentedMvvm.getPostCountLikes(postId)
        lifecycleScope.launchWhenStarted {
            commentedMvvm.getLikesCount.collect{ LikesSize ->
                LikesSize?.let {
                    binding.tvFavorites.text =  it.toString()
                }
            }
        }
    }


    private fun openBottomSheetWithFullScreen() {
        // To Open Bottom Sheet with full screen
        val sheetContainer = requireView().parent as? ViewGroup ?: return
            sheetContainer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        dialog?.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout
            val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun onButtonCommentClick() {
        binding.btnPostComment.setOnClickListener {
            val sortingPostTime = SimpleDateFormat("hh:mm:ss")
            val currentDate = sortingPostTime.format(Date())

            val uniqueId = firestore.collection("posts").document().id
        val comment  = CommentModel(
            commentedBy = auth.currentUser?.uid.toString(),
            commentedAt = Date().time,
            commentBody = binding.edtComment.text.toString(),
            commentId = uniqueId,
            sortingTime = currentDate.toString()
        )
            commentedMvvm.postComment(postId , comment  , postedBy)
            checkIfPostSuccessToPostOrNot()
        }
    }

    private fun observePostComments() {
        commentedMvvm.getPostComments(postId)
        lifecycleScope.launchWhenStarted {
            commentedMvvm.getPostCommentsStateFlow.collect{
                when(it)
                {
                    is CommentsViewState.Success -> { commentsAdapter.differ.submitList(it.data) }
                    else -> Unit
                }
            }
        }
    }
    private fun setupRecViewComments() {
        binding.recViewComments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = commentsAdapter
        }
        // To make rec view scroll to bottom
        commentsAdapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
              binding.recViewComments.smoothScrollToPosition(commentsAdapter.itemCount)
            }
        })
    }
}
