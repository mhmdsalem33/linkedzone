package com.example.linkedzone.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.linkedzone.databinding.NotificationRowBinding
import com.example.linkedzone.databinding.PostRowBinding
import com.example.linkedzone.databinding.StoryRowBinding
import com.example.linkedzone.domain.models.NotificationModel
import com.example.linkedzone.domain.models.PostModel
import com.example.linkedzone.domain.models.StoryModel
import com.example.linkedzone.domain.models.User
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.StringBuilder

class NotificationAdapter() : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {


    lateinit var onNotificationItemClick : (( NotificationModel) -> Unit )
    lateinit var onItemImageClick        : (( NotificationModel) -> Unit )

    private val diffUtilCallback = object : DiffUtil.ItemCallback<NotificationModel>(){
        override fun areItemsTheSame(oldItem: NotificationModel, newItem: NotificationModel): Boolean {
            return oldItem.notificationAt == newItem.notificationAt
        }

        override fun areContentsTheSame(oldItem: NotificationModel, newItem: NotificationModel): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this , diffUtilCallback)

   inner class ViewHolder ( val binding : NotificationRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(NotificationRowBinding.inflate(LayoutInflater.from(parent.context) , parent , false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       val notification = differ.currentList[position]

        val time  = TimeAgo.using(notification.notificationAt)
        holder.binding.tvNotificationTime.text = time

        holder.itemView.setOnClickListener {
            onNotificationItemClick.invoke(notification)
        }

        if (notification.checkOpen)
        {
            holder.binding.checkNotification.visibility = View.GONE
        }
        else
        {
            holder.binding.checkNotification.visibility = View.VISIBLE
        }

        FirebaseFirestore.getInstance().collection("users").document(notification.notificationBy)
            .addSnapshotListener{ snapshot , error ->
                if ( error != null)
                {
                    Log.d("testApp" , error.message.toString())
                }
                else
                {
                    if (snapshot != null)
                    {
                        val user = snapshot.toObject(User::class.java)
                        Glide.with(holder.itemView).load(user?.profileImage).into(holder.binding.imgUser)
                        if (notification.type  == "Comment")
                        {
                            holder.binding.tvNotificationMessage.text = StringBuilder().append(user?.firstName).append(" ").append(user?.lastName).append(" is commented in your post.")
                        }
                        else if (notification.type == "Follow")
                        {
                            holder.binding.tvNotificationMessage.text = StringBuilder().append(user?.firstName).append(" ").append(user?.lastName).append(" is followed you.")
                        }
                        else if (notification.type == "Like")
                        {
                            holder.binding.tvNotificationMessage.text = StringBuilder().append(user?.firstName).append(" ").append(user?.lastName).append(" liked your post.")
                        }
                    }
                }
            }


        holder.binding.imgUser.setOnClickListener {
            onItemImageClick.invoke(notification)
        }

    }
    override fun getItemCount() = differ.currentList.size
}

