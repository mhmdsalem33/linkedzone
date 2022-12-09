package com.example.linkedzone.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.linkedzone.databinding.FriendsRowBinding
import com.example.linkedzone.domain.models.FollowersModel


class MyFollowersAdapter() : RecyclerView.Adapter<MyFollowersAdapter.ViewHolder>() {

    private val diffUtilCallback = object : DiffUtil.ItemCallback<FollowersModel>(){
        override fun areItemsTheSame(oldItem: FollowersModel, newItem: FollowersModel): Boolean {
            return oldItem.profileImage == newItem.profileImage
        }
        override fun areContentsTheSame(oldItem: FollowersModel, newItem: FollowersModel): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this , diffUtilCallback)

   inner class ViewHolder ( val binding : FriendsRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(FriendsRowBinding.inflate(LayoutInflater.from(parent.context) , parent , false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       val followers = differ.currentList[position]
        Glide.with(holder.itemView).load(followers.profileImage).into(holder.binding.imgFriends)
        holder.binding.userName.text = followers.followersName
    }
    override fun getItemCount() = differ.currentList.size
}