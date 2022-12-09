package com.example.linkedzone.ui.fragments

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton
import com.example.linkedzone.R
import com.example.linkedzone.databinding.FragmentLoginBinding
import com.example.linkedzone.ui.activites.MainActivity
import com.example.linkedzone.ui.viewmodel.LoginViewModel
import com.example.linkedzone.ui.viewstate.LoginViewState
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LoginFragment : Fragment() {


    private lateinit var binding: FragmentLoginBinding
    private val loginMvvm       : LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater  , container , false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        login()
        checkLoginStatus()
        onDontHaveAccountClick()

    }

    private fun onDontHaveAccountClick() {
        binding.haveacount.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun login() {
        binding.loginBtn.onSingleClick {
            val userEmail    = binding.emailLog.text.toString()
            val userPassword = binding.passwordLog.text.toString()
            if (userEmail.isEmpty())
            {
                binding.emailLog.apply {
                    requestFocus()
                    error = "Please fill your email"
                }
            }
            else if (userPassword.isEmpty())
            {
                binding.passwordLog.apply {
                    requestFocus()
                    error  = "Please fill your password"
                }
            }
            else
            {
                loginMvvm.loginUser(email = userEmail , password =  userPassword)
            }
        }
    }

    private fun checkLoginStatus() {
        lifecycleScope.launchWhenStarted {
            loginMvvm.login.collect{
                when(it)
                {
                    is LoginViewState.Loading ->  binding.loginBtn.startAnimation()
                    is LoginViewState.Success ->  {
                        binding.loginBtn.revertAnimation()
                        val intent = Intent(requireContext() , MainActivity::class.java)
                            startActivity(intent)
                            activity?.finish()
                    }
                    is  LoginViewState.Error ->{
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                        binding.loginBtn.revertAnimation()
                    }
                    is LoginViewState.EmptyData -> {
                        Toast.makeText(requireContext(), "Data is empty", Toast.LENGTH_SHORT).show()
                        binding.loginBtn.revertAnimation()
                    }
                    else -> Unit
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