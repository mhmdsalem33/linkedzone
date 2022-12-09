package com.example.linkedzone.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.linkedzone.databinding.PostRowBinding
import com.example.linkedzone.domain.models.PostModel
import com.example.linkedzone.domain.models.User
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.StringBuilder

class ProfilePostsAdapter() :RecyclerView.Adapter<ProfilePostsAdapter.ViewHolder>()  {
    private val diffUtilCallback = object :DiffUtil.ItemCallback<PostModel>(){
        override fun areItemsTheSame(oldItem: PostModel, newItem: PostModel): Boolean {
            return oldItem.postId == newItem.postId
        }

        override fun areContentsTheSame(oldItem: PostModel, newItem: PostModel): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this , diffUtilCallback)
    class ViewHolder (val binding : PostRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(PostRowBinding.inflate(LayoutInflater.from(parent.context) , parent , false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = differ.currentList[position]
        if (post.postImage.isNotEmpty())
        {
            holder.binding.postImage.visibility = View.VISIBLE
        }

        val time =   TimeAgo.using(post.postedAt)
        holder.binding.postTime.text = time

        Glide.with(holder.itemView).load(post.postImage).into(holder.binding.postImage)
        holder.binding.textDescription.text = post.postDescription


        FirebaseFirestore.getInstance().collection("users").document(post.postedBy)
            .addSnapshotListener{
                    values , error ->
                if (error != null)
                {
                    Log.d("testApp" , error.message.toString())
                }
                else
                {
                    values?.let {
                        val user = values.toObject(User::class.java)
                        Glide.with(holder.itemView).load(user?.profileImage).into(holder.binding.userProfileImg)
                        holder.binding.userName.text = StringBuilder().append(user?.firstName) .append(" ").append(user?.lastName)
                    }
                }
            }

    }

    override fun getItemCount() = differ.currentList.size
}