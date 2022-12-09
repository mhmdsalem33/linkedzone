package com.example.linkedzone.ui.activites


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.linkedzone.R
import com.example.linkedzone.databinding.ActivityMainBinding
import com.example.linkedzone.ui.fragments.AddPostFragment
import com.example.linkedzone.ui.fragments.HomeFragment
import com.example.linkedzone.ui.viewmodel.NotificationViewModel
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private val notificationMvvm : NotificationViewModel by viewModels()

    @Inject
    lateinit var auth : FirebaseAuth
    @Inject
    lateinit var firestore: FirebaseFirestore
    private lateinit var viewPager2   : ViewPager2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

         val navController = findNavController(R.id.host_fragment)
          binding.bottomView.setupWithNavController(navController)


        notificationMvvm.observeCountOfUnReadNotifications.observe(this){
            it?.let {  binding.bottomView.getOrCreateBadge(R.id.notificationFragment).number = it }
          //  it?.let { binding.tapLayout.getTabAt(2)?.orCreateBadge?.number = it }
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
                Log.d("testApp" , "you are online")
            }
    }


}