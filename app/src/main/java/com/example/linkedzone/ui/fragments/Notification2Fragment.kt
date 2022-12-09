package com.example.linkedzone.ui.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.linkedzone.databinding.FragmentNotification2Binding
import com.example.linkedzone.domain.models.PostModel
import com.example.linkedzone.ui.activites.ProfileActivity
import com.example.linkedzone.ui.adapters.NotificationAdapter
import com.example.linkedzone.ui.fragments.bottomsheets.CommentsFragment
import com.example.linkedzone.ui.viewmodel.NotificationViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class Notification2Fragment : Fragment() {

    private lateinit var binding : FragmentNotification2Binding
    private val notificationMvvm : NotificationViewModel by viewModels()

    private lateinit var notificationAdapter: NotificationAdapter




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationAdapter = NotificationAdapter()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentNotification2Binding.inflate(inflater , container , false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


       setupNotificationRecView()
       fetchNotificationData()
       onNotificationItemClick()


    }

    private fun onNotificationItemClick() {
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
        }
    }


    private fun setupNotificationRecView() {
        binding.notificationRecView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter       = notificationAdapter
        }
    }
    private fun fetchNotificationData() {
        lifecycleScope.launchWhenStarted {
            notificationMvvm.getMyNotification.collect{
                notificationAdapter.differ.submitList(it)
            }
        }
    }


}