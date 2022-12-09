package com.example.linkedzone.ui.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

@HiltViewModel
class EditedProfileViewModel @Inject constructor(
    private val auth        : FirebaseAuth ,
    private val firestore   : FirebaseFirestore,
    private val application : Application
    ) : ViewModel()
{


        private var _observeUpdateUserInformation = MutableLiveData<String?>()
        var observeUpdateUserInformation : LiveData<String?> = _observeUpdateUserInformation
        fun updateUserInformation(firstName : String , lastName :String , userAbout : String ) = viewModelScope.launch(Dispatchers.IO)
        {
            val userMap = HashMap<String , Any>()
            userMap["firstName"] =  firstName
            userMap["lastName"]  =  lastName
            userMap["about"]     =  userAbout
            userMap["searchName"] = firstName.filterNot { it.isWhitespace() }.lowercase(Locale.getDefault()) +
                    " " + lastName.filterNot { it.isWhitespace() }.lowercase(Locale.getDefault())
            firestore.collection("users").document(auth.currentUser?.uid.toString())
                .update(userMap)
                .addOnSuccessListener {
                    Toast.makeText(application , "your information updated success", Toast.LENGTH_SHORT).show()
                    _observeUpdateUserInformation.value = "Success to update information"

                }
        }

}