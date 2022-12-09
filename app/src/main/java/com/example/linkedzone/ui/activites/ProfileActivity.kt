package com.example.linkedzone.ui.activites

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.linkedzone.ui.fragments.bottomsheets.EditedProfileBottomSheet
import com.example.linkedzone.ui.fragments.EditedPostFragment
import com.example.linkedzone.R
import com.example.linkedzone.databinding.ActivityProfileBinding
import com.example.linkedzone.domain.models.NotificationModel
import com.example.linkedzone.domain.models.User
import com.example.linkedzone.ui.adapters.MyFollowersAdapter
import com.example.linkedzone.ui.adapters.PostsAdapter
import com.example.linkedzone.ui.fragments.AddPostFragment
import com.example.linkedzone.ui.fragments.ChatFragment
import com.example.linkedzone.ui.fragments.bottomsheets.CommentsFragment
import com.example.linkedzone.ui.viewmodel.ProfileViewModel
import com.example.linkedzone.ui.viewstate.ProfileViewState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding        : ActivityProfileBinding
    private val profileMvvm             : ProfileViewModel by viewModels()
    private lateinit var myFollowersAdapter : MyFollowersAdapter
    private lateinit var postAdapter : PostsAdapter

    @Inject
    lateinit var auth       : FirebaseAuth

    private lateinit var uid  : String


    @Inject
    lateinit var firestore      : FirebaseFirestore

    @Inject
    lateinit var storage        : FirebaseStorage

    private var imgUri          : Uri ? = null
    private val getCoverImage = registerForActivityResult( ActivityResultContracts.GetContent(), ActivityResultCallback { uri ->
                binding.imgCover.setImageURI(uri)
                imgUri = uri
        } )
    private val getProfileImage = registerForActivityResult( ActivityResultContracts.GetContent(), ActivityResultCallback { uri ->
        binding.imgProfile.setImageURI(uri)
        imgUri = uri
    } )


   private var userInfromation : User ? = null

   private var myInformation : User ? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        myFollowersAdapter = MyFollowersAdapter()
        postAdapter       = PostsAdapter()



        getUserInformationByIntent()
        getMyInformation()
        getUserInformationByFirebase()

        setupFriendsRecView()
        getMyFollowersInformation()

        uploadingCoverImage()
        uploadingProfileImage()

        onFollowButtonClick()
        onUnFollowClick()

        checkFollowStatus()
        checkUserFollowersCount()

        onCommentItemClick()

        setupPostsRecView()
        getProfilePosts()
        onEditedPostCLick()
        onSettingImageClick()
        onWhatsOnMindClick()
        onSettingImageClick()
        onSharePostClick()
        onProfileImgPostCLick()
        onMessagesClick()
        // getProfilePosts()

       if (auth.currentUser?.uid == uid)
       {
            binding.linearFollow.visibility   = View.GONE  // هنخفى المتابعة عشان مش هينفع المستخدك يتابع نفسو

       }
        else  // if it's not my account hide add image cover and profile
        {
            binding.addImgCover.visibility    = View.GONE
            binding.addImgProfile.visibility  = View.GONE
            binding.imgSetting.visibility     = View.GONE
            binding.linearPost.visibility     = View.GONE
        }



    }

    private fun onMessagesClick() {
        binding.imgMesseges.setOnClickListener {
            val fragment = ChatFragment()
            val bundle   = Bundle()
                bundle.putSerializable("user" , userInfromation)
            fragment.arguments = bundle
            supportFragmentManager.beginTransaction()
                .replace(R.id.myProfile, fragment)
                .addToBackStack(null)
                .commit()
        }
    }


    private fun onProfileImgPostCLick() {
        postAdapter.onProfileImgPostClick = {
            Toast.makeText(applicationContext, "Hmm", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText( applicationContext, "Post Shared Success", Toast.LENGTH_SHORT).show()
                    val map = HashMap<String , Any>()
                    map["shareTime"] = Date().time
                    firestore.collection("posts").document(post.postId).update(map)
                }
                .addOnFailureListener{ erro ->
                    Toast.makeText( applicationContext, erro.message.toString(), Toast.LENGTH_SHORT).show()
                }

        }
    }


    private fun onWhatsOnMindClick() {
        binding.onMind.setOnClickListener {
            val fragment = AddPostFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.myProfile, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun onSettingImageClick() {
        binding.imgSetting.setOnClickListener {
            val modalBottomSheet = EditedProfileBottomSheet()
                modalBottomSheet.show(supportFragmentManager , "User Info")
        }
    }

    private fun onEditedPostCLick() {
        postAdapter.onEditedPostImageClick = {
            val modalBottomSheet = EditedPostFragment.newInstance(it)
                modalBottomSheet.show(supportFragmentManager , "User Info")
        }
    }
    private fun getMyInformation() {
        lifecycleScope.launchWhenStarted {
            profileMvvm.getMyInformationStateFlow.collect{ myPersonalInformation ->
                myInformation = myPersonalInformation
                Glide.with(applicationContext).load(myPersonalInformation?.profileImage).error(R.drawable.one).placeholder(R.drawable.one).into(binding.myProfilePost)
                //Picasso.get().load(myPersonalInformation?.profileImage).error(R.drawable.one).placeholder(R.drawable.one).into(binding.myProfilePost)
            }
        }
    }


    private fun getProfilePosts() {
        profileMvvm.getProfilePosts(uid)
        lifecycleScope.launchWhenStarted {
            profileMvvm.getUserPostsStateFlow.collect{
               when(it){
                   is ProfileViewState.SuccessPosts -> postAdapter.differ.submitList(it.data)
                   else -> Unit
               }
            }
        }
    }

    private fun setupPostsRecView() {
        binding.postsRecView.apply {
            layoutManager = LinearLayoutManager(this@ProfileActivity , RecyclerView.VERTICAL , false)
            adapter       = postAdapter
        }
    }




    private fun getUserInformationByIntent() {
        val intent = intent
        if (intent != null)
        {
            val userId = intent.getStringExtra("userUid").toString()
                this.uid = userId
        }
    }

     private fun getUserInformationByFirebase() {
         profileMvvm.getUserInformation(uid)
         lifecycleScope.launchWhenStarted {
            profileMvvm.getUserInformationStateFlow.collect{ user ->
                userInfromation = user
                Log.d("testApp" , userInfromation.toString())
                user?.let {
                    Glide.with(this@ProfileActivity).load(user.userCover).error(R.drawable.one).into(binding.imgCover)
                    Glide.with(this@ProfileActivity).load(user.profileImage).error(R.drawable.one).into(binding.imgProfile)
                    binding.apply {
                        userName.text = StringBuilder().append(user.firstName).append(" ") .append(user.lastName)
                        userAbout.text = user.about
                    }
                }
            }
        }
     }
     private fun setupFriendsRecView() {
         binding.friendsRecView.apply {
             layoutManager = LinearLayoutManager(this@ProfileActivity , RecyclerView.HORIZONTAL , false)
             adapter       = myFollowersAdapter
         }
     }

   private fun getMyFollowersInformation() {
       profileMvvm.getMyFollowersInformation(uid.toString())
     lifecycleScope.launchWhenStarted {
         profileMvvm.getMyFollowersInformationStateFlow.collect {
            when(it)
            {
                is ProfileViewState.EmptyData -> { Log.d("testApp" , "No followers found")}
                is ProfileViewState.Success   -> { myFollowersAdapter.differ.submitList(it.data)}
                is ProfileViewState.Error     -> {
                    Toast.makeText(applicationContext, it.message, Toast.LENGTH_SHORT).show()
                }
                else -> Unit
            }
         }
     }
   }

   private fun uploadingCoverImage() {
       if (auth.currentUser?.uid == uid)
       {
           binding.addImgCover.setOnClickListener {
               getCoverImage.launch("image/")
               lifecycleScope.launchWhenStarted {
                   delay(1000)
                   imgUri?.let {
                       val coverPath = storage.getReference().child("cover/"+auth.currentUser?.uid)
                       coverPath.putFile(imgUri!!)
                           .addOnFailureListener{
                               Toast.makeText(this@ProfileActivity, it.message, Toast.LENGTH_SHORT).show()
                           }
                           .addOnCompleteListener{
                               coverPath.downloadUrl.addOnSuccessListener {
                                   val coverMap = HashMap<String , Any>()
                                       coverMap["userCover"]    = it.toString()
                                   lifecycleScope.launch(Dispatchers.IO)
                                   {
                                       async {
                                           launch {
                                               firestore.collection("users").document(auth.currentUser?.uid.toString())
                                                   .update(coverMap)
                                                   .addOnSuccessListener {
                                                       Toast.makeText(this@ProfileActivity, "Cover Saved success", Toast.LENGTH_SHORT).show()
                                                   }.addOnFailureListener { e ->
                                                       Toast.makeText(this@ProfileActivity, e.message.toString(), Toast.LENGTH_SHORT).show()
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

   }
   private fun uploadingProfileImage() {
       if (auth.currentUser?.uid  == uid)
       {
           binding.addImgProfile.setOnClickListener {
               getProfileImage.launch("image/")
               lifecycleScope.launchWhenStarted {
                   delay(1000)
                   imgUri?.let {
                       val profileImagePath = storage.getReference().child("profile/"+auth.currentUser?.uid)
                       profileImagePath.putFile(imgUri!!)
                           .addOnFailureListener{
                               Toast.makeText(this@ProfileActivity, it.message, Toast.LENGTH_SHORT).show()
                           }
                           .addOnCompleteListener {
                               profileImagePath.downloadUrl.addOnSuccessListener {
                                   val coverMap = HashMap<String , Any>()
                                       coverMap["profileImage"]  = it.toString()
                                   lifecycleScope.launch(Dispatchers.IO)
                                   {
                                       async {
                                           launch {
                                               firestore.collection("users").document(auth.currentUser?.uid.toString())
                                                   .update(coverMap)
                                                   .addOnSuccessListener {
                                                       Toast.makeText(this@ProfileActivity, "Profile picture Saved success", Toast.LENGTH_SHORT).show()
                                                   }.addOnFailureListener{ e ->
                                                       Toast.makeText(this@ProfileActivity, e.message.toString(), Toast.LENGTH_SHORT).show()
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
   }
   private fun onFollowButtonClick() {
       binding.userFollow.setOnClickListener{
       if (userInfromation !=  null)
       {
         val followMap = HashMap<String , Any>()
             followMap["followedBy"]    =  auth.currentUser?.uid.toString()
             followMap["followedAt"]    =  Date().time.toString()
             followMap["profileImage"]  =  myInformation?.profileImage.toString()
             followMap["followersName"] =  myInformation?.firstName + " " + myInformation?.lastName

        lifecycleScope.launch(Dispatchers.IO){
                   async {
                       launch {
                           firestore.collection("users").document(userInfromation?.uid.toString()).collection("followers")
                               .document(auth.currentUser?.uid.toString())
                               .set(followMap)
                               .addOnSuccessListener {
                                   profileMvvm.getCountOfMyFollower(uid.toString())
                                   Toast.makeText(applicationContext, "You followed ${userInfromation?.firstName + " " + userInfromation?.lastName }", Toast.LENGTH_SHORT).show()
                                   val uniqueId = FirebaseFirestore.getInstance().collection("notification").document().id
                                   val notification = NotificationModel(
                                       notificationBy =  auth.currentUser?.uid.toString(),
                                       notificationAt =  Date().time ,
                                       type           = "Follow" ,
                                       notificationId =  uniqueId,
                                       checkOpen = false ,
                                       postedBy = userInfromation?.uid.toString()

                                   )

                                   firestore.collection("notification")
                                       .document(userInfromation?.uid.toString())
                                       .collection("myNotification")
                                       .document(uniqueId)
                                       .set(notification)
                                       .addOnSuccessListener {
                                           Log.d("testApp" , "Success to send notification")
                                       }
                               }
                       }
                   }.await()
               }
           }
       }
   }
   private fun onUnFollowClick() {
       binding.userUnfollow.setOnClickListener {
           firestore.collection("users").document(uid.toString()).collection("followers")
               .document(auth.currentUser?.uid.toString())
               .delete()
               .addOnSuccessListener {
                   profileMvvm.getCountOfMyFollower(uid)
                   Toast.makeText(applicationContext, "You have removed ${userInfromation?.firstName + " " + userInfromation?.lastName }", Toast.LENGTH_SHORT).show()
               }
       }
   }
   private fun checkFollowStatus() {
       firestore.collection("users").document(uid.toString()).collection("followers")
           .document(auth.currentUser?.uid.toString()).addSnapshotListener { snapshot, e ->
               if (e != null) {
                   Log.d("testApp", "Listen failed.", e)
                   return@addSnapshotListener
               }

               if (snapshot != null && snapshot.exists()) { // لو موجود هيبقى عامل فولو
                   binding.userUnfollow.visibility = View.VISIBLE
                   binding.userFollow.visibility   = View.GONE
               } else // مش عامل فولو
               {
                   binding.userUnfollow.visibility = View.GONE
                   binding.userFollow.visibility   = View.VISIBLE
               }
           }
   }
   private fun checkUserFollowersCount() {
       profileMvvm.getCountOfMyFollower(uid.toString())
       lifecycleScope.launchWhenStarted {
           profileMvvm.getMyFollowersCount.collect{
               binding.tvFollowers.text = StringBuilder().append(it.toString()).append(" ").append("followers")
           }
       }
   }

    private fun onCommentItemClick() {
        postAdapter.onPostItemClick = { post ->

            val modalBottomSheet = CommentsFragment.newInstance(post)
            modalBottomSheet.show(supportFragmentManager , "User Info")
        }
    }


    override fun onResume() {
        super.onResume()
        val map = HashMap<String , Any>()
        map["online"] =  true

        firestore.collection("users").document(auth.currentUser?.uid.toString()).update(map)
            .addOnSuccessListener {
                Log.d("testApp" , "you are online")
            }
    }

    override fun onPause() {
        super.onPause()
        val map = HashMap<String , Any>()
        map["online"] =  false

        firestore.collection("users").document(auth.currentUser?.uid.toString()).update(map)
            .addOnSuccessListener {
                Log.d("testApp" , "you are offline")
            }
    }



}