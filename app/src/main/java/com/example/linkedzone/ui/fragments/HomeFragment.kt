package com.example.linkedzone.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.linkedzone.R
import com.example.linkedzone.databinding.FragmentHomeBinding
import com.example.linkedzone.ui.adapters.PostsAdapter
import com.example.linkedzone.ui.adapters.StoryAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.linkedzone.domain.models.User
import com.example.linkedzone.ui.activites.ProfileActivity
import com.example.linkedzone.ui.fragments.bottomsheets.CommentsFragment
import com.example.linkedzone.ui.viewmodel.HomeViewModel
import com.example.linkedzone.ui.viewmodel.NotificationViewModel
import com.example.linkedzone.ui.viewstate.HomeViewState
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.StringBuilder
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap
import omari.hamza.storyview.callback.StoryClickListeners

import omari.hamza.storyview.StoryView


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private lateinit var binding      : FragmentHomeBinding

    private lateinit var postAdapter  : PostsAdapter
    private lateinit var storyAdapter : StoryAdapter

    private val homeMvvm              : HomeViewModel by viewModels()
    private val notificationMvvm      : NotificationViewModel by viewModels()

    @Inject
    lateinit var auth : FirebaseAuth
    @Inject
    lateinit var firestore : FirebaseFirestore
    @Inject
    lateinit var storage   : FirebaseStorage

    private var imgUri          : Uri? = null
    private val getImage = registerForActivityResult( ActivityResultContracts.GetContent(), ActivityResultCallback { uri ->
        binding.imgStory.setImageURI(uri)
        imgUri = uri
    } )

    private var myInformation : User ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storyAdapter = StoryAdapter()
        postAdapter  = PostsAdapter()


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater , container , false)
        return  binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)





        FirebaseMessaging.getInstance().subscribeToTopic("/topics/${auth.currentUser?.uid}")

        onSearchClick()
        setupStoryRecView()
        fetchStoryData()
        onImgStoryClick()

        setupPostRecView()
        observeGetPosts()
        onCommentItemClick()

        onAddPostClick()
        onNotificationIconClick()

        onPostEdtClick()
        getMyInformation()

        getUnReadNotificationCount()
        onProfileImgPostCLick()

        onMyProfileImageCLick()
        onEditedPostCLick()

        onChatCLick()

        onSharePostClick()
        onStoriesItemClick()



        onHandleOnBackPressed()


    }

    private fun onStoriesItemClick() {
        storyAdapter.onStoriesItemClick = { storiesArrayList , user ->
            StoryView.Builder(activity?.supportFragmentManager)
                .setStoriesList(storiesArrayList) // Required
                .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                .setTitleText(user.firstName + " " + user.lastName) // Default is Hidden
                .setSubtitleText(user.about) // Default is Hidden
                .setTitleLogoUrl(user.profileImage) // Default is Hidden
                 .setStoryClickListeners(object : StoryClickListeners {
                            override fun onDescriptionClickListener(position: Int) {
                                if (auth.currentUser?.uid == user.uid )
                                {
                                    uploadStory()
                                }
                                else
                                {
                                    Toast.makeText(requireContext(), "Please try with your stories ", Toast.LENGTH_LONG).show()
                                }


                            }

                            override fun onTitleIconClickListener(position: Int) {
                                //your action
                                val intent = Intent(requireContext()  , ProfileActivity::class.java)
                                    intent.putExtra("userUid"   , user.uid )
                                startActivity(intent)
                            }
                        }) // Optional Listeners
                            .build() // Must be called before calling show method
                            .show()
        }
    }
    private fun onSharePostClick() {
        postAdapter.onSharePostClick = { post ->
            val shareMap = HashMap<String , Any>()
                shareMap["shareName"]         =  "${myInformation?.firstName} ${myInformation?.lastName}"
                shareMap["SharedId"]          =  auth.currentUser?.uid.toString()
                shareMap["postId"]            =  post.postId
                shareMap["shareTime"]         =  Date().time
                shareMap["shareProfileImage"] =  myInformation?.profileImage.toString()

            firestore.collection("posts")
                .document(post.postId)
                .collection("shared")
                .document(post.postId)
                //.document(auth.currentUser?.uid.toString())
                .set(shareMap)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Post Shared Success", Toast.LENGTH_SHORT).show()
                    val map = HashMap<String , Any>()
                        map["shareTime"] = Date().time
                    firestore.collection("posts").document(post.postId).update(map)
                }
                .addOnFailureListener{ erro ->
                    Toast.makeText(requireContext(), erro.message.toString(), Toast.LENGTH_SHORT).show()
                }

        }
    }
    private fun onChatCLick() {
        binding.homeHeader.messageIcon.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchChatFragment)
        }
    }
    private fun onEditedPostCLick() {
        postAdapter.onEditedPostImageClick = {
            val modalBottomSheet = EditedPostFragment.newInstance(it)
                modalBottomSheet.show(childFragmentManager , "Meal Info")
        }
    }
    private fun onMyProfileImageCLick() {
        binding.userImage.setOnClickListener {
            val intent = Intent(requireContext()  , ProfileActivity::class.java)
            intent.putExtra("userUid"   , auth.currentUser?.uid.toString() )
            startActivity(intent)
        }
    }
    private fun onProfileImgPostCLick() {
     postAdapter.onProfileImgPostClick = {
            val intent = Intent(requireContext() , ProfileActivity::class.java)
                intent.putExtra("userUid"   , it.postedBy)
            startActivity(intent)
     }
    }
    private fun getUnReadNotificationCount() {
        notificationMvvm.observeCountOfUnReadNotifications.observe(viewLifecycleOwner){
            it?.let { binding.homeHeader.tvCountOfNotifications.text = it.toString() }
        }
    }
    private fun getMyInformation() {
        lifecycleScope.launchWhenStarted {
            homeMvvm.getUserInformationStateFlow.collect{
                when(it)
                {
                    is HomeViewState.Success -> {
                        Glide.with(binding.userImage).load(it.data.profileImage)
                            .error(R.drawable.one)
                            .placeholder(resources.getDrawable(R.drawable.one))
                          .into(binding.userImage)
                     //   Picasso.get().load(it.data.profileImage).error(R.drawable.loupe).into(binding.userImage)
                        binding.edtPostHome.hint = StringBuilder().append("What's on your mind, ").append("${it.data.firstName}?")
                        myInformation = it.data
                    }
                    else -> Unit
                }
            }
        }
    }
    private fun onPostEdtClick() {
        binding.edtPostHome.setOnClickListener {
        //    findNavController().navigate(R.id.action_homeFragment_to_addPostFragment)
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(R.id.homeFragment, AddPostFragment())
                ?.addToBackStack(null)?.commit()
        }
    }
    private fun onCommentItemClick() {
        postAdapter.onPostItemClick = { post ->
            val modalBottomSheet = CommentsFragment.newInstance(post)
                modalBottomSheet.show(childFragmentManager , "User Info")
        }
    }
    private fun observeGetPosts() {
        lifecycleScope.launchWhenStarted {
            homeMvvm.getPostsStateFlow.collect{
                when(it)
                {
                    is HomeViewState.SuccessPosts -> {
                        postAdapter.differ.submitList(it.data)
                        // binding.shimmer.stopShimmer()
                        binding.shimmer.visibility = View.GONE
                        binding.homeLayout.visibility = View.VISIBLE
                    }
                    else -> Unit
                }
            }
        }
    }
    private fun onAddPostClick() {
       binding.homeHeader.addPost.setOnClickListener {
           findNavController().navigate(R.id.action_homeFragment_to_addPostFragment)
       }
    }
    private fun onNotificationIconClick() {
        binding.homeHeader.notification.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_notificationFragment)
        }
    }
    private fun onImgStoryClick() {
        binding.imgStory.setOnClickListener {
            uploadStory()
        }
    }
    private fun uploadStory(){
        getImage.launch("image/")
        lifecycleScope.launchWhenStarted {
            delay(1000)
            imgUri?.let {
                val storyPath = storage.getReference().child("stories").child(auth.currentUser?.uid.toString())
                    .child(Date().time.toString())
                storyPath.putFile(imgUri!!)
                    .addOnFailureListener{
                        Toast.makeText( requireContext(), it.message, Toast.LENGTH_SHORT ).show()
                    }
                    .addOnCompleteListener{
                        storyPath.downloadUrl.addOnSuccessListener { url ->

                            val myMap = HashMap<String,Any>()
                            myMap["storedBy"] = auth.currentUser?.uid.toString()
                            myMap["time"]     = Date().time

                            firestore.collection("stories")
                                .document(auth.currentUser?.uid.toString())
                                .set(myMap)
                                .addOnSuccessListener {

                                    lifecycleScope.launch(Dispatchers.IO)
                                    {
                                        async {
                                            launch {
                                                val storyMap = HashMap<String , Any>()
                                                storyMap["description"]      = "add new story"
                                                storyMap["date"]             = Date()
                                                storyMap["url"]              = url.toString()
                                                firestore.collection("stories")
                                                    .document(auth.currentUser?.uid.toString())
                                                    .collection("mystory")
                                                    .add(storyMap)
                                                    .addOnSuccessListener {
                                                        Toast.makeText(requireContext(), "Success to upload story", Toast.LENGTH_SHORT).show()
                                                    }
                                            }
                                        }.await()
                                    }
                                }
                        }
                    }
            }
        }
    }
    private fun fetchStoryData() {
        lifecycleScope.launchWhenStarted {
            homeMvvm.getStoriesStateFlow.collect{
                when(it){
                    is HomeViewState.SuccessStory -> {
                        storyAdapter.differ.submitList(it.data)
                    }
                    else -> Unit
                }
           }
        }
    }
    private fun setupStoryRecView() {
        binding.createStoryRecView.apply {
            layoutManager = LinearLayoutManager(requireContext() , RecyclerView.HORIZONTAL , false)
            adapter       = storyAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (recyclerView.canScrollHorizontally(-1)) {
                        binding.createStory.visibility = View.GONE
                    }
                    else
                    {
                        binding.createStory.visibility = View.VISIBLE
                    }
                }
            })
            // To make rec view scroll to left
            storyAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    binding.createStoryRecView.smoothScrollToPosition(0)
                }
            })
        }
    }
    private fun setupPostRecView() {
        binding.postRecView.apply {
            layoutManager = LinearLayoutManager(requireContext() , RecyclerView.VERTICAL , false)
            adapter       = postAdapter
        }
    }
    private fun onSearchClick() {
        binding.homeHeader.search.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_searchFragment)
        }
    }


    private fun onHandleOnBackPressed() {
        val callback = object : OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed() {
             activity?.finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner , callback)
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            activity?.let { activity?.window?.setStatusBarColor(it.getColor(R.color.white)) }
        }
    }

}