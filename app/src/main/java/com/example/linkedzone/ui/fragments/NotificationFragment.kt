package com.example.linkedzone.ui.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.linkedzone.R
import com.example.linkedzone.databinding.FragmentNotificationBinding
import com.example.linkedzone.domain.models.PostModel
import com.example.linkedzone.ui.activites.ProfileActivity
import com.example.linkedzone.ui.adapters.NotificationAdapter
import com.example.linkedzone.ui.fragments.bottomsheets.CommentsFragment
import com.example.linkedzone.ui.viewmodel.NotificationViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject






@AndroidEntryPoint
class NotificationFragment : Fragment() {

    private lateinit var binding : FragmentNotificationBinding
    private val notificationMvvm : NotificationViewModel by viewModels()

    private lateinit var notificationAdapter: NotificationAdapter


    @Inject
    lateinit var auth  : FirebaseAuth
    @Inject
    lateinit var firestore: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notificationAdapter = NotificationAdapter()

}

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNotificationBinding.inflate(inflater  , container , false)
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNotificationRecView()
        fetchNotificationData()
        onNotificationItemClick()
        onBackClick()
        onImageNotificationClick()
        onHandleOnBackPressed()

    }


    private fun onImageNotificationClick() {
        notificationAdapter.onItemImageClick = {
            val intent = Intent(requireContext() ,  ProfileActivity::class.java)
                intent.putExtra("userUid"   , it.notificationBy)
            startActivity(intent)
        }
    }

    private fun onBackClick() {
        binding.imgBack.setOnClickListener {
            activity?.onBackPressed()
        }
    }


    private fun onNotificationItemClick()   {
        notificationAdapter.onNotificationItemClick = { notification ->
            if (notification.type == "Comment")
            {
                val postModel = PostModel( postedBy = notification.postedBy, postId = notification.postId)
                val modalBottomSheet = CommentsFragment.newInstance(postModel)
                modalBottomSheet.show(childFragmentManager , "Meal Info")
            }
            else if (notification.type == "Follow")
            {
                val intent = Intent(requireContext() , ProfileActivity::class.java)
                intent.putExtra("userUid" , notification.notificationBy)
                startActivity(intent)
            }
            else if (notification.type == "Like")
            {
                val postModel = PostModel( postedBy = notification.postedBy, postId = notification.postId)
                val modalBottomSheet = CommentsFragment.newInstance(postModel)
                    modalBottomSheet.show(childFragmentManager , "Meal Info")
            }
            // change status of notification
            lifecycleScope.launch(Dispatchers.IO){
                async {
                    val map = HashMap<String , Any>()
                        map["checkOpen"] = true
                    firestore.collection("notification").document(notification.postedBy)
                        .collection("myNotification").document(notification.notificationId)
                        .update(map)
                }.await()
            }
        }
    }
    private fun setupNotificationRecView()  {
        binding.notificationRecView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter       = notificationAdapter
        }
    }
    private fun fetchNotificationData()     {
        lifecycleScope.launchWhenStarted {
            notificationMvvm.getMyNotification.collect{
                notificationAdapter.differ.submitList(it)
            }
        }
    }


    private fun onHandleOnBackPressed() {
        val callback = object : OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_notificationFragment_to_homeFragment)
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
