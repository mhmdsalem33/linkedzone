package com.example.linkedzone.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.linkedzone.databinding.CommentsRowBinding
import com.example.linkedzone.domain.models.CommentModel
import com.example.linkedzone.domain.models.User
import com.google.firebase.firestore.FirebaseFirestore
import com.github.marlonlom.utilities.timeago.TimeAgo




class CommentsAdapter() : RecyclerView.Adapter<CommentsAdapter.ViewHolder>() {

    private val diffUtilCallback =  object :DiffUtil.ItemCallback<CommentModel>(){
        override fun areItemsTheSame(oldItem: CommentModel, newItem: CommentModel): Boolean {
          return  oldItem.commentId == newItem.commentId
        }

        override fun areContentsTheSame(oldItem: CommentModel, newItem: CommentModel): Boolean {
           return oldItem == newItem
        }
    }

    var differ =  AsyncListDiffer(this , diffUtilCallback)

    class ViewHolder (val binding : CommentsRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       return ViewHolder(CommentsRowBinding.inflate(LayoutInflater.from(parent.context) , parent , false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = differ.currentList[position]

        val time  = TimeAgo.using(comment.commentedAt)
        holder.binding.tvComments.text = comment.commentBody
        holder.binding.tvTime.text = time


        FirebaseFirestore.getInstance().collection("users").document(comment.commentedBy)
            .addSnapshotListener{ snapshot , error ->
                error?.let {
                    Toast.makeText(holder.itemView.context, it.message.toString(), Toast.LENGTH_SHORT).show()
                } ?: snapshot?.let {
                    val user = snapshot.toObject(User::class.java)
                    holder.binding.tvName.text = StringBuilder().append(user?.firstName).append(" ").append(user?.lastName)
                    Glide.with(holder.itemView).load(user?.profileImage).into(holder.binding.imgComment)
                }
            }



    }

    override fun getItemCount() = differ.currentList.size
}