package com.example.linkedzone.ui.adapters


import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.linkedzone.R
import com.example.linkedzone.databinding.PostRowBinding
import com.example.linkedzone.domain.models.NotificationModel
import com.example.linkedzone.domain.models.PostModel
import com.example.linkedzone.domain.models.SharedModel
import com.example.linkedzone.domain.models.User
import com.github.marlonlom.utilities.timeago.TimeAgo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.HashMap


class PostsAdapter() : RecyclerView.Adapter<PostsAdapter.ViewHolder>() {

    lateinit var onPostItemClick        : (( PostModel )  -> Unit)
    lateinit var onProfileImgPostClick  : (( PostModel )  -> Unit)
    lateinit var onEditedPostImageClick : (( PostModel )  -> Unit)
    lateinit var onSharePostClick       : (( PostModel )  -> Unit)


    private val diffUtilCallback = object : DiffUtil.ItemCallback<PostModel>(){
        override fun areItemsTheSame(oldItem: PostModel, newItem: PostModel): Boolean {
            return oldItem.postId == newItem.postId
        }

        override fun areContentsTheSame(oldItem: PostModel, newItem: PostModel): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this , diffUtilCallback)

   inner class ViewHolder ( val binding : PostRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(PostRowBinding.inflate(LayoutInflater.from(parent.context) , parent , false))
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       val post = differ.currentList[position]

        holder.binding.btnComment.setOnClickListener {
            onPostItemClick.invoke(post)
        }

        if (post.postImage.isNotEmpty())
        {
            holder.binding.postImage.visibility = View.VISIBLE
        }
        else
        {
            holder.binding.postImage.visibility = View.GONE
        }

        val time =   TimeAgo.using(post.postedAt)
        holder.binding.postTime.text = time

        Glide.with(holder.itemView).load(post.postImage).into(holder.binding.postImage)
        holder.binding.textDescription.text = post.postDescription


        holder.binding.userProfileImg.setOnClickListener {
            onProfileImgPostClick.invoke(post)
        }
        holder.binding.editedPost.setOnClickListener {
            onEditedPostImageClick.invoke(post)
        }

        if (post.postedBy != FirebaseAuth.getInstance().currentUser?.uid)
        {
            holder.binding.editedPost.visibility = View.GONE
        }

        // on post share click
        holder.binding.sharePost.setOnClickListener {
                onSharePostClick.invoke(post)
        }




        // user shared information
        val uniqueId = FirebaseFirestore.getInstance().collection("posts").document().id
         FirebaseFirestore.getInstance().collection("posts").document(post.postId)
              .collection("shared").document(post.postId)
            .addSnapshotListener{ data , error ->
                if (error != null)
                {
                    Log.d("testApp" , error.message.toString())
                }
                else
                {
                    if (data != null && data.exists() )
                    {
                        val shared = data.toObject(SharedModel::class.java)
                            if (shared != null)
                            {
                                val sharedTime = TimeAgo.using(shared.shareTime)
                                holder.binding.tvShareName.text = shared.shareName
                                holder.binding.tvShareTime.text = sharedTime
                                Glide.with(holder.itemView).load(shared?.shareProfileImage).error(R.drawable.one).placeholder(R.drawable.one).into(holder.binding.userImageShare)
                                holder.binding.userImageShare.visibility = View.VISIBLE
                                holder.binding.linearShare.visibility = View.VISIBLE
                                holder.binding.tvShare.visibility = View.VISIBLE
                                holder.binding.tvShareP.visibility = View.VISIBLE
                               // Picasso.get().load(shared.shareProfileImage).error(R.drawable.one).into(holder.binding.userImageShare)
                            }
                    }
                    else
                    {
                        holder.binding.userImageShare.visibility = View.GONE
                        holder.binding.linearShare.visibility = View.GONE
                        holder.binding.tvShare.visibility = View.GONE
                        holder.binding.tvShareP.visibility = View.GONE
                    }
                }
            }

        // get user post information
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

                        if (user?.profileImage != null && user.profileImage.isNotEmpty())
                        {
                                Picasso.get().load(user.profileImage).error(R.drawable.one).placeholder(R.drawable.one).into(holder.binding.userProfileImg)
                            //Glide.with(holder.itemView).load(user.profileImage).error(holder.itemView.context.resources.getDrawable(R.drawable.one)).placeholder(R.drawable.one).into(holder.binding.userProfileImg)

                        }
                            holder.binding.userName.text = StringBuilder().append(user?.firstName) .append(" ").append(user?.lastName)
                            holder.binding.userAbout.text = user?.about
                    }
                }
            }


        // فى حالة مكنش عامل لايك
        holder.binding.postLike.setOnClickListener {
            val likeMap = HashMap<String , Any>()
                likeMap["liked"]  = true
            FirebaseFirestore.getInstance().collection("posts").document(post.postId)
                .collection("likes")
                .document(FirebaseAuth.getInstance().currentUser?.uid.toString())
                .set(likeMap)
                .addOnSuccessListener {
                    holder.binding.postLike.visibility = View.GONE
                    holder.binding.postLiked.visibility = View.VISIBLE

                   // val uniqueId = FirebaseFirestore.getInstance().collection("notification").document().id
                    val notification   = NotificationModel(
                        notificationAt = Date().time ,
                        notificationBy = FirebaseAuth.getInstance().currentUser?.uid.toString(),
                        checkOpen      = false,
                        postId         = post.postId ,
                        postedBy       = post.postedBy,
                        type           = "Like" ,
                        notificationId = uniqueId
                    )
                    if (FirebaseAuth.getInstance().currentUser?.uid  != post.postedBy)
                    {
                        FirebaseFirestore.getInstance().collection("notification")
                            .document(post.postedBy)
                            .collection("myNotification")
                            .document(uniqueId)
                            .set(notification)
                            .addOnSuccessListener {
                                Log.d("testApp" , "Success to send Like with notification")
                            }
                    }
                }
        }

        // فى حالة اذا كان عامل لايك
        holder.binding.postLiked.setOnClickListener {
            FirebaseFirestore.getInstance().collection("posts").document(post.postId)
                .collection("likes").document(FirebaseAuth.getInstance().currentUser?.uid.toString())
                .delete()
                .addOnSuccessListener {
                    holder.binding.postLike.visibility = View.VISIBLE
                    holder.binding.postLiked.visibility = View.GONE
                }
        }

        // هنضيف تسنت على حالة ال Like
        FirebaseFirestore.getInstance().collection("posts").document(post.postId)
            .collection("likes")
            .document(FirebaseAuth.getInstance().currentUser?.uid.toString())
            .addSnapshotListener{ myLike , error ->
                if (error != null)
                {
                    Log.d("testApp" , error.message.toString())
                    return@addSnapshotListener
                }
                else
                {
                    if (myLike != null && myLike.exists())
                    {
                        holder.binding.postLike.visibility    = View.GONE
                        holder.binding.postLiked.visibility   = View.VISIBLE
                    }
                    else
                    {
                        holder.binding.postLike.visibility     = View.VISIBLE
                        holder.binding.postLiked.visibility    = View.GONE
                    }
                }
            }
        // هنضيف تسنت على العدد بتاع اللى الناس اللى عملت لايك
        FirebaseFirestore.getInstance().collection("posts")
            .document(post.postId).collection("likes")
            .addSnapshotListener{ countLikes ,  error ->
                error?.let { Log.d("testAp" , it.message.toString()) }
                countLikes?.let {
                    holder.binding.tvFavorites.text = it.size().toString()
                }
            }

        // هنجيب العدد بتاع الكومنتات
        FirebaseFirestore.getInstance().collection("posts").document(post.postId).collection("comments")
            .addSnapshotListener{ snapshot   , error ->
                error?.let { Log.d("testApp" , it.message.toString()) }
                snapshot?.let { comments ->
                    holder.binding.tvComments.text = comments.size().toString()
                }
            }
    }

    override fun getItemCount() = differ.currentList.size
}