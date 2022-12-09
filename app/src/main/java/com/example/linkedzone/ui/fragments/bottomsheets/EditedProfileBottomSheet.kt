package com.example.linkedzone.ui.fragments.bottomsheets

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.linkedzone.databinding.FragmentEditedProfileBottomSheetBinding
import com.example.linkedzone.ui.activites.StartActivity
import com.example.linkedzone.ui.viewmodel.EditedProfileViewModel
import com.example.linkedzone.ui.viewmodel.ProfileViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class EditedProfileBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding : FragmentEditedProfileBottomSheetBinding


    @Inject
    lateinit var auth     : FirebaseAuth
    @Inject
    lateinit var firestore: FirebaseFirestore

    private val editedMvvm : EditedProfileViewModel by viewModels()
    private val profileMvvm : ProfileViewModel by viewModels()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentEditedProfileBottomSheetBinding.inflate(inflater , container , false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        openBottomSheetWithHalfHeightOfFullScreen()
        onButtonUpdateMyInformationCLick()
        observerStatusOfUpdateMyInformation()
        onButtonLogout()


        lifecycleScope.launchWhenStarted {
            profileMvvm.getMyInformationStateFlow.collect{ user ->
                user?.let {
                    binding.welcome.text = StringBuilder().append("welcome ").append(user.firstName).append(" ").append(user.lastName).append(" looking for change?")
                }
            }
        }




    }

    private fun onButtonLogout() {
        binding.logout.setOnClickListener {
            auth.signOut()
            this.dismiss()
            val intent = Intent(requireContext() , StartActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or  Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
        }
    }

    private fun onButtonUpdateMyInformationCLick() {
        binding.update.setOnClickListener {
            val firstName  =  binding.tvFirstName.text.toString()
            val lastName   =  binding.tvLastName.text.toString()
            val userAbout  =  binding.tvAbout.text.toString()

            if (firstName.isNotEmpty() && lastName.isNotEmpty() && userAbout.isNotEmpty())
            {
                editedMvvm.updateUserInformation(firstName, lastName, userAbout)
            }
        }
    }

    private fun observerStatusOfUpdateMyInformation() {
        editedMvvm.observeUpdateUserInformation.observe(viewLifecycleOwner){
            it?.let {
             if (it == "Success to update information")
             {
                 this.dismiss()
             }
            }
        }
    }


    private fun openBottomSheetWithHalfHeightOfFullScreen() {
        // To Open Bottom Sheet with half height of  screen
        val sheetContainer = requireView().parent as? ViewGroup ?: return
        sheetContainer.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
    }


}