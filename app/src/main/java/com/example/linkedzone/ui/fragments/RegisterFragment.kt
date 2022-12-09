package com.example.linkedzone.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.linkedzone.R
import com.example.linkedzone.databinding.FragmentRegisterBinding
import com.example.linkedzone.domain.models.User
import com.example.linkedzone.ui.viewmodel.RegisterViewModel
import com.google.firebase.auth.FirebaseAuth

import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject


@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private lateinit var binding : FragmentRegisterBinding
    private val registerMvvm : RegisterViewModel by viewModels()


    @Inject
    lateinit var auth : FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentRegisterBinding.inflate(inflater , container , false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        registerUser()
        btnHaveAnAccount()
    }

    private fun btnHaveAnAccount() {
        binding.haveAccount.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun registerUser() {
        binding.btnReg.onSingleClick {

            val firstName     = binding.firstName.text.toString()
            val lastName      = binding.lastName.text.toString()
            val userEmail     = binding.emailReg.text.toString()
            val userPassword  = binding.passwordReg.text.toString()
            val user = User(userEmail = userEmail , firstName = firstName , lastName = lastName)
            if (firstName.isEmpty())
            {
                binding.firstName.apply {
                    requestFocus()
                    error = "Please fill first name"
                }
            }
            else if (lastName.isEmpty())
            {
                binding.lastName.apply {
                    requestFocus()
                    error = "Please fill last name"
                }
            }
            else if (userEmail.isEmpty())
            {
                binding.emailReg.apply {
                    requestFocus()
                    error = "Please fill your email "
                }
            }
            else if (userPassword.isEmpty())
            {
                binding.passwordReg.apply {
                    requestFocus()
                    error = "Please fill your password"
                }
            }
            else
            {
                         binding.btnReg.startAnimation()

                    auth.createUserWithEmailAndPassword(userEmail , userPassword)
                        .addOnSuccessListener {
                            lifecycleScope.launchWhenStarted {
                                registerMvvm.saveUserInformation(user , password = userPassword)
                                delay(500)
                                binding.btnReg.revertAnimation()
                                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_SHORT).show()
                            binding.btnReg.revertAnimation()
                        }
            }

        }
    }
    private inline fun View.onSingleClick( crossinline action: (v: View) -> Unit ) {
        setOnClickListener { v ->
            isEnabled = false
            action(v)
            postDelayed({ isEnabled = true }, 3000)
        }
    }


}