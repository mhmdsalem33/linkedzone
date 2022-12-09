package com.example.linkedzone.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.linkedzone.R
import com.example.linkedzone.databinding.FragmentAuthBinding


class AuthFragment : Fragment() {

    private lateinit var binding : FragmentAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentAuthBinding.inflate(inflater , container , false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_authFragment_to_registerFragment)
        }
        binding.btnLogin.setOnClickListener {
            findNavController().navigate(R.id.action_authFragment_to_loginFragment)
        }

    }

}