package com.example.linkedzone.ui.fragments

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.linkedzone.R
import com.example.linkedzone.Services.PostService
import com.example.linkedzone.databinding.FragmentAddPostBinding
import com.example.linkedzone.domain.models.PostModel
import com.example.linkedzone.ui.viewmodel.AddPostViewModel
import com.example.linkedzone.ui.viewmodel.AddPostViewModel.Companion.postDescription
import com.example.linkedzone.ui.viewmodel.AddPostViewModel.Companion.uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

import javax.inject.Inject


@AndroidEntryPoint
class AddPostFragment : Fragment() {

    private lateinit var binding : FragmentAddPostBinding
    private val addPostMvvm      : AddPostViewModel by viewModels()

    @Inject
    lateinit var auth       : FirebaseAuth
    @Inject
    lateinit var firestore  : FirebaseFirestore
    @Inject
    lateinit var storage    : FirebaseStorage

    private var imgUri      : Uri? = null
    private val getImage = registerForActivityResult( ActivityResultContracts.GetContent(), ActivityResultCallback { uri ->
        binding.imagePost.setImageURI(uri)
          imgUri = uri


    } )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddPostBinding.inflate(inflater , container , false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onHandleOnBackPressed()
        observeMyInformation()
        onUploadImageClick()
        onBtnPostClick()
        checkStatusOfLanuchPost()

        binding.edtPost.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val description = binding.edtPost.text.toString()
                if (description.isNotEmpty())
                {
                    binding.btnPost.setBackgroundResource(R.drawable.button_backgrounds)
                    binding.btnPost.setTextColor(ContextCompat.getColor(requireContext()    ,
                        R.color.white
                    ))
                    binding.btnPost.isEnabled = true
                }
                else
                {
                    binding.btnPost.setBackgroundResource(R.drawable.post_radius)
                    binding.btnPost.setTextColor(ContextCompat.getColor(requireContext()    ,
                        R.color.g_gray700
                    ))
                    binding.btnPost.isEnabled = false
                }
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun checkStatusOfLanuchPost() {
              lifecycleScope.launchWhenStarted {
                  addPostMvvm.checkPostStatus.collect{
                      it?.let {
                          if (it == "Success")
                          {
                              delay(500)
                              val intent = Intent(requireContext() ,  PostService::class.java)
                              ContextCompat.startForegroundService(requireContext() , intent)
                              delay(500)
                              activity?.onBackPressed()
                          }
                      }
                  }
              }

    }

    private fun onBtnPostClick() {
        binding.btnPost.onSingleClick {
            lifecycleScope.launchWhenStarted {
                delay(1000)
                async {
                    launch {
                        val postImagePath = storage.getReference().child("posts").child(auth.currentUser?.uid.toString())
                            .child(Date().time.toString() + auth.currentUser?.uid)
                        imgUri?.let {
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
                                            postDescription = binding.edtPost.text.toString())
                                        addPostMvvm.uploadPost(post)
                                    }
                                }
                        } ?: launch {
                            val uniqueId = firestore.collection("posts").document().id
                            val post = PostModel(
                                postId          = uniqueId  ,
                                postedBy        = auth.uid.toString(),
                                postedAt        = Date().time ,
                                shareTime       = Date().time,
                                postDescription = binding.edtPost.text.toString())
                            addPostMvvm.uploadPost(post)
                        }
                    }
                }.await()
            }
        }
    }
    private fun observeMyInformation() {
        lifecycleScope.launchWhenStarted {
            addPostMvvm.getMyInformationStateFlow.collect{
                it?.let {
                    Glide.with(requireContext()).load(it.profileImage).into(binding.imgUser)
                    binding.apply {
                        userName.text = StringBuilder().append(it.firstName).append(" ").append(it.lastName)
                        binding.about.text = it.about
                    }
                }
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
                    uri  = it

                }
            }
        }
    }
    override fun onResume() {
        super.onResume()
        //بقفل الفراجمنت من ان الروتيشن
        getActivity()?.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            activity?.let { activity?.window?.setStatusBarColor(it.getColor(R.color.white)) }
        }
    }
    override fun onPause() {
        super.onPause()
        //هنا هيشغل الروتشن على باقى الفراجمنت
        getActivity()?.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
    }
    private inline fun View.onSingleClick( crossinline action: (v: View) -> Unit ) {
        setOnClickListener { v ->
            isEnabled = false
            action(v)
            postDelayed({ isEnabled = true } , 5000)
        }
    }


    private fun onHandleOnBackPressed() {
        val callback = object : OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed() {
                activity?.supportFragmentManager?.popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner , callback)
    }






}