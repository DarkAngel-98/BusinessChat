package com.example.businesscards.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.businesscards.R
import com.example.businesscards.adapters.MessagesAdapter
import com.example.businesscards.constants.HeartSingleton
import com.example.businesscards.constants.PreferenceClass
import com.example.businesscards.databinding.FragmentCommunicationBinding
import com.example.businesscards.interfaces.BasicListener
import com.example.businesscards.interfaces.UserListener
import com.example.businesscards.models.*
import com.example.businesscards.startup.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso


class ChatFragment : Fragment(), BasicListener, UserListener {

    private lateinit var binding: FragmentCommunicationBinding
    private var user: UserInfo? = null
    private var prefs: PreferenceClass? = null
    var firebaseUser: FirebaseUser? = null
    var databaseReference: DatabaseReference? = null
    var messagesAdapter: MessagesAdapter? = null
    var chatList: ArrayList<Chat>? = null
    private val TAG = "CHAT_FRAGMENT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseUser = FirebaseAuth.getInstance().currentUser
        prefs = PreferenceClass(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil
                .inflate(inflater, R.layout.fragment_communication, container, false)
        arguments?.let {
            user =
                arguments?.get(HeartSingleton.BundleChatChat) as UserInfo
            Log.d("USERNAME", user?.username!!)
        }
        activity?.let { (activity as MainActivity).hideNavigationPanel() }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvChat.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        if (user != null) {
            onStarted()
            // CURRENT_USER and CHAT_USER are correct!
            Log.d("CURRENT_USER", firebaseUser?.uid!!)
            Log.d("CHAT_USER", user?.id!!)

            // CHAT_USER_IMAGE_URL is correct!
            Log.d("CHAT_USER_IMAGE_URL", user?.imageURL.toString())

            if(user?.status == 0)
                binding.usersStatus.setImageResource(R.color.gray_status_offline)
            else if(user?.status == 1)
                binding.usersStatus.setImageResource(R.color.green_status_online)


            binding.usersName.text = user?.firstName
            binding.usersSurname.text = user?.lastName
            //Picasso.get().load(user?.imageURL).into(binding.communicationProfilePicture)
            if (!user?.imageURL.isNullOrEmpty() && !user?.imageURL.equals(HeartSingleton.FireDefault)) {
                Picasso.get().load(user?.imageURL).into(binding.communicationProfilePicture)
            } else
                binding.communicationProfilePicture.setImageResource(R.drawable.ic_default_profile_picture)

            readMessages()
            setupListeners()
        }
    }

    private fun setupListeners() {
        binding.sendMessage.setOnClickListener {
            binding.rvChat.scrollToPosition(binding.rvChat.adapter!!.itemCount - 1)
        }
        binding.iconSend.setOnClickListener {
            var message = binding.sendMessage.text.toString()
            if (!message.isNullOrEmpty()) {
                sendMessage(firebaseUser?.uid!!, user?.id!!, message)
                UsersFragment.newMessage = 1
                UsersFragment.senderId = firebaseUser?.uid!!
                binding.sendMessage.text.clear()
                var title = "New Message"
                PushNotification(
                    NotificationData(
                        prefs?.getUsername(), message, title
                    ), user?.token!!
                ).also {
                    (activity as MainActivity).sendNotifications(it)
                }
            } else {
                Log.d(TAG, message.toString())
            }
        }
        onBackPressed()
    }

    private fun setNewMessage(newMessage: Int){
        var currentUserId = firebaseUser?.uid!!

        FirebaseDatabase.getInstance()
            .getReference(HeartSingleton.FireUsersDB)
            .child(currentUserId).child(HeartSingleton.FireNewMessage).setValue(newMessage)
    }

    private fun setNewMessageSeen(newMessage: Int, user: UserInfo){
        FirebaseDatabase.getInstance()
            .getReference(HeartSingleton.FireUsersDB)
            .child(user.id!!).child(HeartSingleton.FireNewMessage).setValue(newMessage)
    }

    private fun sendMessage(senderId: String, receiverId: String, message: String) {
        databaseReference = FirebaseDatabase.getInstance().reference

        var hashMap: HashMap<String, Any> = HashMap()
        hashMap[HeartSingleton.FireSenderId] = senderId
        hashMap[HeartSingleton.FireReceiverId] = receiverId
        hashMap[HeartSingleton.FireMessage] = message

        setNewMessage(1)

        databaseReference?.child(HeartSingleton.FireChatDB)?.push()?.setValue(hashMap)

        var chatReference: DatabaseReference =
            FirebaseDatabase
                .getInstance()
                .getReference(HeartSingleton.FireChatIdDB)
                .child(firebaseUser?.uid!!)
                .child(user?.id!!)

        chatReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    chatReference.child(HeartSingleton.FireId).setValue(user?.id)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    fun onBackPressed(){
//        Toast.makeText(requireContext(), "Back is pressed", Toast.LENGTH_SHORT).show()
        setNewMessageSeen(0, user!!)
    }

    private fun readMessages() {
        chatList = ArrayList()
        var currentUserId = prefs?.getUserId()
        var currentUserImageUrl = prefs?.getImageUrl()
        var remoteUserImageUrl = user?.imageURL
        var databaseReference =
            FirebaseDatabase.getInstance().getReference(HeartSingleton.FireChatDB)
        databaseReference.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList?.clear()
                for (chatData in snapshot.children) {
                    var chat: Chat = chatData.getValue(Chat::class.java) as Chat
                    if (chat.receiverId.equals(firebaseUser?.uid) && chat.senderId.equals(user?.id) ||
                        chat.receiverId.equals(user?.id) && chat.senderId.equals(firebaseUser?.uid)
                    ) {
                        chatList?.add(chat)

                    }
                }
                messagesAdapter =
                    MessagesAdapter(
                        chatList!!,
                        R.layout.chat_left_item_layout,
                        R.layout.chat_right_item_layout,
                        currentUserId,
                        currentUserImageUrl,
                        remoteUserImageUrl
                    )
                messagesAdapter?.notifyItemRangeChanged(0, chatList!!.size - 1)
                binding.rvChat.adapter = messagesAdapter
                binding.rvChat.scrollToPosition(binding.rvChat.adapter!!.itemCount - 1)
                onStopped()
            }

            override fun onCancelled(error: DatabaseError) {
                // not needed here
            }

        })
    }


    override fun onStarted() {
        activity?.let { (activity as MainActivity).showProgress() }
    }

    override fun onStopped() {
        activity?.let { (activity as MainActivity).hideProgress() }
    }

    override fun onUserClicked(user: UserInfo) {
        Toast.makeText(requireContext(), "Hello", Toast.LENGTH_SHORT).show()
    }

}