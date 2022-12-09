package com.example.linkedzone.ui.fragments

import android.net.Uri
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
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.linkedzone.R
import com.example.linkedzone.databinding.FragmentChatBinding
import com.example.linkedzone.domain.models.ChatModel
import com.example.linkedzone.domain.models.User
import com.example.linkedzone.ui.adapters.ChatAdapter
import com.example.linkedzone.ui.viewmodel.NotificationViewModel
import com.example.linkedzone.ui.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mhmd.salem.chatkotlin.Notifications.NotificationData
import mhmd.salem.chatkotlin.Notifications.PushNotification
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap
import kotlin.text.StringBuilder


@AndroidEntryPoint
class ChatFragment : Fragment() {

    private lateinit var binding : FragmentChatBinding

    private val notificationMvvm :  NotificationViewModel by viewModels()
    private val profileMvvm      : ProfileViewModel by viewModels()
    @Inject
    lateinit var auth         : FirebaseAuth

    @Inject
    lateinit var firestore    : FirebaseFirestore

    @Inject
    lateinit var storage      : FirebaseStorage

    @Inject
    lateinit var  database    : FirebaseDatabase
    private var   mdatabase   : DatabaseReference ? = null

    private lateinit var chatAdapter : ChatAdapter

    private lateinit var receiverId  : String

    private var chatList = arrayListOf<ChatModel>()



    private var imgUri      : Uri? = null
    private val getImage = registerForActivityResult( ActivityResultContracts.GetContent(), ActivityResultCallback { uri ->
        imgUri = uri
    } )

    var topic = ""

   private var myName : StringBuilder ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatAdapter = ChatAdapter()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(inflater , container , false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //requireActivity().window.statusBarColor = Color.rgb(100,1,135,)


        getReceiverId()
        getMyPersonalInformation()
        getOtherUserInformation()
        setupChatRecView()
        onButtonSendMessageClick()
        observeChatMessages(auth.currentUser?.uid.toString())
        onButtonSendImage()
        onMessageItemLongClick()
        onArrowBackClick()


        binding.edtMessage.addTextChangedListener(object  : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if (p0.toString().isEmpty())
                {
                    typingStatus("false")
                }
                else
                {
                    typingStatus(receiverId)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })
    }

    private fun onArrowBackClick() {
        binding.arrowBack.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun typingStatus(typing : String) {
        val typingMap = HashMap<String ,Any>()
            typingMap["typing"] = typing

            lifecycleScope.launch(Dispatchers.IO){
                firestore.collection("users").document(auth.uid.toString())
                    .update(typingMap)
            }
    }
    private fun getOtherUserInformation() {
        firestore.collection("users").document(receiverId).addSnapshotListener{ snapshot , error ->
            if (error != null)
            {
                Log.d("testApp" , error.message.toString())
            }
            else
            {
                if (snapshot != null)
                {
                    val user = snapshot.toObject(User::class.java)
                    user?.let {
                        if (user.online){
                            binding.icOffline.visibility = View.GONE
                            binding.icOnline.visibility  = View.VISIBLE
                        }
                        else
                        {
                            binding.icOffline.visibility = View.VISIBLE
                            binding.icOnline.visibility  = View.GONE
                        }
                        val typing = it.typing
                        if (typing == auth.currentUser?.uid)
                        {
                            binding.animationView.visibility = View.VISIBLE
                            binding.animationView.playAnimation()
                        }
                        else
                        {
                            binding.animationView.cancelAnimation()
                            binding.animationView.visibility = View.INVISIBLE
                        }
                    }
                }
            }
        }
    }


    private fun getMyPersonalInformation() {
        lifecycleScope.launchWhenStarted {
            profileMvvm.getMyInformationStateFlow.collect{
                it?.let {
                    myName = StringBuilder().append(it.firstName).append(" ").append(it.lastName)
                }
            }
        }
    }

    private fun onMessageItemLongClick() {
        chatAdapter.onMessageItemLongClick = {
            database.getReference("chats").child(it.messageId).removeValue().addOnSuccessListener {
                Toast.makeText(requireContext(), "Message deleted success", Toast.LENGTH_SHORT).show()
            }
                .addOnFailureListener{ error ->
                    Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun onButtonSendImage() {
        binding.sendImage.setOnClickListener {
            lifecycleScope.launchWhenStarted {
                getImage.launch("image/")
                delay(1000)
                async {
                    launch {
                        val imagePath = FirebaseStorage.getInstance().getReference()
                            .child("chatImages/"+ auth.uid + Calendar.getInstance().time + Date().time)

                        if (imgUri != null )
                        {
                            imagePath.putFile(imgUri!!).addOnSuccessListener {
                                imagePath.downloadUrl.addOnSuccessListener { uri ->
                                    val uniqueId = database.getReference("chats").push().key.toString()
                                    val message = HashMap<String , Any>()
                                        message["senderId"]    =  auth.uid.toString()
                                        message["receiverId"]  =  receiverId
                                        message["image"]       =  uri.toString()
                                        message["messageId"]   =  uniqueId
                                    database.getReference("chats").child(uniqueId).setValue(message)
                                        .addOnSuccessListener {
                                            Toast.makeText(requireContext(), "Image send successfully", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        }
                    }
                }.await()
            }
        }
    }

    private fun onButtonSendMessageClick() {
        binding.sendMessage.setOnClickListener {
            val message = binding.edtMessage.text.toString()
            if (message.isEmpty())
            {
                Toast.makeText(requireContext(), "Message must be not empty!", Toast.LENGTH_SHORT).show()
            }
            else
            {
                    sendMessage(message)
                    topic = "/topics/$receiverId"
                val notification = PushNotification(NotificationData("$myName" , message) , topic)
                    notificationMvvm.sendNotificationWithMessage(notification)
            }
        }
    }

    private fun observeChatMessages(senderId : String) {

        mdatabase =  database.getReference("chats")
        mdatabase?.addValueEventListener(object  : ValueEventListener{
        override fun onDataChange(snapshot: DataSnapshot) {
            chatList.clear()
            for (data in snapshot.children)
            {
                val chatModel = data.getValue(ChatModel::class.java)
                if (chatModel?.senderId   == senderId   && chatModel.receiverId    == receiverId ||
                    chatModel?.senderId   == receiverId && chatModel.receiverId    == senderId
                ){
                    chatList.add(chatModel)
                }
                chatAdapter.differ.submitList(chatList)
                setupChatRecView()
            }
        }
        override fun onCancelled(error: DatabaseError) {
            Toast.makeText(requireContext(), error.message, Toast.LENGTH_SHORT).show()
        }
    })
        mdatabase?.keepSynced(true)

    }
    private fun setupChatRecView() {
        binding.chatRec.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter       = chatAdapter
            scrollToPosition(chatList.size -1)
        }
    }
    private fun getReceiverId() {
        val data = arguments
        if (data != null)
        {
            val receiver   = data.getSerializable("user") as User
                receiverId =    receiver.uid
                Glide.with(requireContext()).load(receiver.profileImage).into(binding.imgUser)
                binding.txtName.text = StringBuilder().append(receiver.firstName).append(" ").append(receiver.lastName)

        }
    }
    private fun sendMessage(message: String) {

            val uniqueId = database.getReference("chats").push().key.toString()
            val messageMap = HashMap<String , Any>()
                messageMap["senderId"]    = auth.currentUser?.uid.toString()
                messageMap["receiverId"]  = receiverId
                messageMap["message"]     = message
                messageMap["messageId"]   = uniqueId

        lifecycleScope.launch(Dispatchers.IO){
            database.getReference().child("chats")
                .child(uniqueId)
                .setValue(messageMap)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Message send success", Toast.LENGTH_SHORT).show()
                    binding.edtMessage.text.clear()
                }
                .addOnFailureListener{
                    Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            activity?.let { activity?.window?.setStatusBarColor(it.getColor(R.color.teal_700)) }
        }
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
           activity?.let { activity?.window?.setStatusBarColor(it.getColor(R.color.white)) }
        }
    }
}