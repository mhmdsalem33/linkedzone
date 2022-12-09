package com.example.linkedzone.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devlomi.circularstatusview.CircularStatusView
import com.example.linkedzone.R
import com.example.linkedzone.databinding.StoryRowBinding
import com.example.linkedzone.domain.models.Story
import com.example.linkedzone.domain.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import omari.hamza.storyview.model.MyStory


class StoryAdapter() : RecyclerView.Adapter<StoryAdapter.ViewHolder>() {

    lateinit var onStoriesItemClick : ((ArrayList<MyStory> , User) -> Unit)

    private val diffUtilCallback = object : DiffUtil.ItemCallback<Story>(){
        override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
            return  oldItem.storedBy == newItem.storedBy
        }

        override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this , diffUtilCallback)


   inner class ViewHolder ( val binding : StoryRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(StoryRowBinding.inflate(LayoutInflater.from(parent.context) , parent , false))
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = differ.currentList[position]



        FirebaseFirestore.getInstance().collection("users").document(story.storedBy)
                .addSnapshotListener{ snapshot , error ->
                    if (error != null)
                    {
                        Log.d("testApp" , error.message.toString())
                    }
                    else
                    {
                        if (snapshot != null && snapshot.exists())
                        {
                            val user = snapshot.toObject(User::class.java)
                            user?.let {
                                if (user.profileImage.isNotEmpty())
                                {
                                    Glide.with(holder.itemView).load(user.profileImage).error(holder.itemView.context.resources.getDrawable(R.drawable.one)).placeholder(holder.itemView.context.resources.getDrawable(R.drawable.one)).into(holder.binding.imgStory)
                                }
                            }

                            //Picasso.get().load(user?.profileImage).error(holder.itemView.context.resources.getDrawable(R.drawable.one)).placeholder(holder.itemView.context.resources.getDrawable(R.drawable.one)).into(holder.binding.imgStory)
                            holder.binding.userName.text = StringBuilder().append(user?.firstName).append(" ").append(user?.lastName)
                           // Glide.with(holder.itemView).load(user?.profileImage).into(holder.binding.imgProfile)


                                // get stories
                            FirebaseFirestore.getInstance().collection("stories").document(story.storedBy)
                                .collection("mystory")
                                .addSnapshotListener{ data , e ->
                                    if(e != null)
                                    {
                                     Log.d("testApp" , e.message.toString())
                                    }
                                    else
                                    {
                                       if (data != null)
                                       {
                                           val listStory  : ArrayList<MyStory> = ArrayList()
                                           val myStories = data.toObjects(MyStory::class.java)
                                                listStory.addAll(myStories)
                                           val status : CircularStatusView = holder.binding.root.findViewById(R.id.circle_status)
                                               status.setPortionsCount(data.size())
                                               status.setPortionsColor(holder.itemView.context.getResources().getColor(R.color.zahry));
                                            holder.itemView.setOnClickListener {
                                                user?.let { onStoriesItemClick.invoke( listStory , user )  }

                                            }
                                       }
                                    }
                                }
                        }
                    }
                }


    }

    override fun getItemCount() = differ.currentList.size
}