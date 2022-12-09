package com.example.linkedzone.ui.fragments

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.linkedzone.R
import com.example.linkedzone.databinding.FragmentSearchChatBinding
import com.example.linkedzone.domain.models.FollowersModel
import com.example.linkedzone.domain.models.User
import com.example.linkedzone.ui.adapters.MyFollowersAdapter
import com.example.linkedzone.ui.adapters.MyFollowersTwoAdapter
import com.example.linkedzone.ui.adapters.SearchChatAdapter
import com.example.linkedzone.ui.viewmodel.SearchViewModel
import com.example.linkedzone.ui.viewstate.SearchViewState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@AndroidEntryPoint
class SearchChatFragment : Fragment() {

    private lateinit var binding : FragmentSearchChatBinding

    @Inject
    lateinit var auth : FirebaseAuth

    @Inject
    lateinit var firestore: FirebaseFirestore
    private val searchMvvm : SearchViewModel by viewModels()
    private lateinit var followersAdapter : MyFollowersTwoAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        followersAdapter = MyFollowersTwoAdapter()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentSearchChatBinding.inflate(inflater , container , false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        onSearchBox()
        setupSearchRecView()
        getMyFollowers()
        onSearchChatClick()
    }

    private fun getMyFollowers() {
        lifecycleScope.launch(Dispatchers.IO)
        {
            firestore.collection("users").document(auth.currentUser?.uid.toString())
                .collection("followers").addSnapshotListener{ myFollowers , error ->
                    if (error != null)
                    {
                        Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                    }
                    else
                    {

                      if (myFollowers != null)
                      {
                          val data =   myFollowers.toObjects(FollowersModel::class.java)
                          followersAdapter.differ.submitList(data)
                          binding.mustHave.visibility = View.GONE
                      }
                        else
                      {
                          binding.mustHave.visibility = View.VISIBLE
                      }
                    }
                }
        }
    }

    private fun onSearchChatClick() {

        followersAdapter.onItemClick  = { user ->

            val fragment = ChatFragment()
            val bundle   = Bundle()
                bundle.putSerializable("user" , user )
            fragment.arguments = bundle
          // activity?.supportFragmentManager?.beginTransaction()
              // ?.replace(R.id.search_lay, fragment)
              //  ?.addToBackStack(null)
              //  ?.commit()
            findNavController().navigate(R.id.action_searchChatFragment_to_chatFragment , bundle)

        }


    }



    private fun setupSearchRecView() {
        binding.chatUsersRecView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter       = followersAdapter
        }
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

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            activity?.let { activity?.window?.setStatusBarColor(it.getColor(R.color.white)) }
        }
    }


}