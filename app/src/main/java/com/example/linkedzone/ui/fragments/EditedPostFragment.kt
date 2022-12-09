package com.example.linkedzone.ui.fragments

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.linkedzone.R
import com.example.linkedzone.databinding.FragmentEditedPostBinding
import com.example.linkedzone.domain.models.PostModel
import com.example.linkedzone.ui.viewmodel.EditedPostViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.StringBuilder
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class EditedPostFragment : BottomSheetDialogFragment() {


    private lateinit var binding : FragmentEditedPostBinding
    private val editedMvvm : EditedPostViewModel by viewModels()

    @Inject
    lateinit var auth      : FirebaseAuth
    @Inject
    lateinit var firestore : FirebaseFirestore
    @Inject
    lateinit var storage   : FirebaseStorage


    private lateinit var postId   : String
    private lateinit var postedBy : String

    private var imgUri : Uri ? = null

    private val getImage = registerForActivityResult( ActivityResultContracts.GetContent(), ActivityResultCallback { uri ->
        binding.imagePost.setImageURI(uri)
        imgUri = uri
    } )



    companion object {
        fun newInstance(post : PostModel) =
            EditedPostFragment().apply {
                arguments = Bundle().apply {
                       postId    = post.postId
                       postedBy  = post.postedBy
                }
            }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentEditedPostBinding.inflate(inflater , container , false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        openBottomSheetWithFullScreen()
        getUserInformation()
        getPostInformation()
        onUploadImageClick()
        updatePosts()
        checkStatusOfUpdatePost()
        onDeletePostClick()
        observeStatusOfDeletePost()





    }

    private fun observeStatusOfDeletePost() {
        editedMvvm.getStatusOfDeletePost.observe(viewLifecycleOwner){
            it?.let {
                if (it == "Success to delete")
                {
                    this.dismiss()
                    Toast.makeText(requireContext(), "Post deleted successfully", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun onDeletePostClick() {
        binding.deletePost.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
                builder.setTitle("Attention")
                builder.setMessage("Do you want to delete this post?")
                builder.setNegativeButton("Cancel"){ dialog , which ->
                    dialog.dismiss()
                }
                builder.setPositiveButton("Delete"){ dialog , which ->
                      editedMvvm.deletePost(postId)
                      dialog.dismiss()
            }
            builder.show()
        }
    }

    private fun checkStatusOfUpdatePost() {
        editedMvvm.statusOfUpdatePost.observe(viewLifecycleOwner){ update ->
            update?.let {
                if (update == "Success")
                {
                    this.dismiss()
                }
            }
        }
    }


    private fun updatePosts() {
        binding.btnPost.onSingleClick {
           lifecycleScope.launchWhenStarted {
               async {
                        launch {
                            val description = binding.edtPostDescription.text.toString()
                            if (description.isNotEmpty() && imgUri != null)
                            {
                                val postImagePath = storage.getReference().child("posts").child(auth.currentUser?.uid.toString())
                                    .child(Date().time.toString() + auth.currentUser?.uid)
                                    postImagePath.putFile(imgUri!!).addOnSuccessListener {
                                    postImagePath.downloadUrl.addOnSuccessListener {
                                        editedMvvm.updatePost(postDescription =  description ,postImage = it.toString(), postId = postId  )
                                    }
                                }
                            }
                            else if (description.isNotEmpty() && imgUri == null)
                            {
                                editedMvvm.updatePostDescription(description , postId)
                            }
                            else if (imgUri != null && description.isEmpty())
                            {
                                val postImagePath = storage.getReference().child("posts").child(auth.currentUser?.uid.toString())
                                    .child(Date().time.toString() + auth.currentUser?.uid)
                                     postImagePath.putFile(imgUri!!).addOnSuccessListener {
                                     postImagePath.downloadUrl.addOnSuccessListener {
                                        editedMvvm.updatePostImage( it.toString(),  postId)
                                    }
                                }
                            }
                   }
               }.await()
           }
        }
    }
    private fun onUploadImageClick() {
        binding.uploadImage.setOnClickListener {
            getImage.launch("image/")
            lifecycleScope.launchWhenStarted {
                delay(1000)
                imgUri?.let {
                    binding.imagePost.visibility = View.VISIBLE
                }
            }
        }
    }
    private fun getPostInformation() {
        editedMvvm.getPostInformation(postId)
        editedMvvm.getPostInformationLiveData.observe(viewLifecycleOwner){ postInformation ->
            postInformation?.let {
                if (postInformation.postDescription.isNotEmpty())
                {
                    val edt =  binding.root.findViewById<TextView>(R.id.edt_post_description)
                        edt.text =  it.postDescription
                }
                if (postInformation.postImage.isNotEmpty())
                {
                    Glide.with(requireContext()).load(it.postImage).into(binding.imagePost)
                }
            }
        }
    }
    private fun getUserInformation() {
        editedMvvm.getUserInformation(postedBy)
        editedMvvm.getUserInformationLiveData.observe(viewLifecycleOwner){ user ->
            user?.let {
                binding.userName.text =  StringBuilder().append(it.firstName).append(" ").append(it.lastName)
                Glide.with(requireContext()).load(it.profileImage).into(binding.imgUser)
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
    private inline fun View.onSingleClick( crossinline action: (v: View) -> Unit ) {
        setOnClickListener { v ->
            isEnabled = false
            action(v)
            postDelayed({ isEnabled = true }, 5000)
        }
    }

}