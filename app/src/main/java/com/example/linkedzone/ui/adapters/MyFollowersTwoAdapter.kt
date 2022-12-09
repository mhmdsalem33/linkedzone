package com.example.linkedzone.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.linkedzone.databinding.FriendsRowBinding
import com.example.linkedzone.databinding.SearchUsersRowBinding
import com.example.linkedzone.domain.models.FollowersModel
import com.example.linkedzone.domain.models.User
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso


class MyFollowersTwoAdapter() : RecyclerView.Adapter<MyFollowersTwoAdapter.ViewHolder>() {


    lateinit var onItemClick : ( ( User ) -> Unit )
    private val diffUtilCallback = object : DiffUtil.ItemCallback<FollowersModel>(){
        override fun areItemsTheSame(oldItem: FollowersModel, newItem: FollowersModel): Boolean {
            return oldItem.profileImage == newItem.profileImage
        }
        override fun areContentsTheSame(oldItem: FollowersModel, newItem: FollowersModel): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this , diffUtilCallback)

   inner class ViewHolder ( val binding : SearchUsersRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(SearchUsersRowBinding.inflate(LayoutInflater.from(parent.context) , parent , false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       val followers = differ.currentList[position]

        FirebaseFirestore.getInstance().collection("users").document(followers.followedBy).addSnapshotListener{data , error ->
            if (error != null)
            {
                Log.d("testApp" , error.message.toString())
            }
            else
            {
                if (data != null)
                {
                    val user = data.toObject(User::class.java)

                    holder.itemView.setOnClickListener {
                        user?.let {   onItemClick.invoke(user) }
                    }
                   // Glide.with(holder.itemView).load(user?.profileImage).into(holder.binding.imgUser)
                    Picasso.get().load(user?.profileImage).into(holder.binding.imgUser)
                    holder.binding.userName.text = StringBuilder().append(user?.firstName).append(" ").append(user?.lastName)
                }
            }
        }

    }
    override fun getItemCount() = differ.currentList.size
}