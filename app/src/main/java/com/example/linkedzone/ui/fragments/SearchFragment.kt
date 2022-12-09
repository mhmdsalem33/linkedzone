package com.example.linkedzone.ui.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.linkedzone.R

import com.example.linkedzone.databinding.FragmentSearchBinding
import com.example.linkedzone.domain.models.User
import com.example.linkedzone.ui.activites.ProfileActivity
import com.example.linkedzone.ui.adapters.SearchUsersAdapter
import com.example.linkedzone.ui.viewmodel.SearchViewModel
import com.example.linkedzone.ui.viewstate.SearchViewState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private lateinit var binding       : FragmentSearchBinding
    private val searchMvvm             : SearchViewModel by viewModels()
    private lateinit var searchAdapter : SearchUsersAdapter


    @Inject
    lateinit var auth : FirebaseAuth
    @Inject
    lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        searchAdapter = SearchUsersAdapter()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentSearchBinding.inflate(inflater , container , false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prepareRecView()
        onSearchBox()
        getSearchData()
        onSearchItemClick()


    }


    private fun onSearchItemClick() {
        searchAdapter.onSearchItemClick = {
            val intent = Intent(requireContext() , ProfileActivity::class.java)
                intent.putExtra("userUid" , it.uid )
            startActivity(intent)
        }
    }


    private fun prepareRecView() {
        binding.searchRec.hasFixedSize()
        binding.searchRec.layoutManager = LinearLayoutManager(requireContext())
        binding.searchRec.adapter       = searchAdapter
    }

    private fun onSearchBox() {
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val searchText  :String =  binding.edtSearch.text.toString()
                searchMvvm.searchInFirestore(searchText.lowercase(Locale.getDefault()))
            }
            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    private fun getSearchData() {
        lifecycleScope.launchWhenStarted {
            searchMvvm.getAllUsersStateFlow.collect{
                when(it)
                {
                    is SearchViewState.Loading -> {
                        Log.d("testApp" , "isLoading")}
                    is SearchViewState.Success -> {
                        searchAdapter.searchList(searchList = it.data as ArrayList<User>)
                    }
                    is  SearchViewState.Error -> {
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT).show()
                    }
                    is  SearchViewState.EmptyData -> { Log.d("testApp", "No users found") }
                    else -> Unit
                }
            }

        }
    }
    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            activity?.let { activity?.window?.setStatusBarColor(it.getColor(R.color.white)) }
        }
    }

}