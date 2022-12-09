package com.example.linkedzone.ui.viewmodel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.linkedzone.domain.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore   : FirebaseFirestore,
    private val application : Application
) : ViewModel() {


     fun saveUserInformation(user: User , password: String){
        val userMap = HashMap<String, Any>()
            userMap["userEmail"]     = user.userEmail
            userMap["firstName"]     = user.firstName
            userMap["lastName"]      = user.lastName
            userMap["userPassword"]  = password
            userMap["uid"]           = firebaseAuth.currentUser?.uid.toString()
            userMap["typing"]        = "false"
            userMap["searchName"]    = user.firstName.filterNot { it.isWhitespace() }.lowercase(Locale.getDefault())+
                    " "+ user.lastName.filterNot { it.isWhitespace() }.lowercase(Locale.getDefault())

         viewModelScope.launch(Dispatchers.IO) {
             async {
                 launch {
                     firestore.collection("users").document(firebaseAuth.currentUser?.uid.toString())
                         .set(userMap)
                         .addOnSuccessListener {
                             Log.d("testApp" , "Success to SAVED")
                         }
                         .addOnFailureListener {
                             Toast.makeText(application, it.message.toString(), Toast.LENGTH_SHORT).show()
                         }
                 }
             }.await()
         }
    }
}



