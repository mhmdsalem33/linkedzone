package com.example.linkedzone.ui.adapters

import android.view.LayoutInflater

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.linkedzone.R

import com.example.linkedzone.databinding.SearchUsersRowBinding
import com.example.linkedzone.domain.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class SearchUsersAdapter() :RecyclerView.Adapter<SearchUsersAdapter.ViewHolder>() {


    lateinit var onSearchItemClick   : ( (User) -> Unit )

    private var searchList = ArrayList<User>()
    fun searchList(searchList :ArrayList<User>){
        this.searchList = searchList
        notifyDataSetChanged()
    }

    class ViewHolder(val binding : SearchUsersRowBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      return  ViewHolder(SearchUsersRowBinding.inflate(LayoutInflater.from(parent.context) , parent , false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = searchList[position]

        Glide.with(holder.itemView).load(data.profileImage).error(R.drawable.ic_baseline_account_circle_24).into(holder.binding.imgUser)
        holder.binding.userName.text = StringBuilder().append(data.firstName).append(" ").append(data.lastName)

        holder.itemView.setOnClickListener {
            onSearchItemClick.invoke(data)
        }





    }
    override fun getItemCount() = searchList.size
}