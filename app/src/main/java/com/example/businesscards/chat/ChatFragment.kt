package com.example.businesscards.chat

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.businesscards.R
import com.example.businesscards.adapters.MessagesAdapter
import com.example.businesscards.adapters.UsersAdapter
import com.example.businesscards.constants.HeartSingleton
import com.example.businesscards.constants.PreferenceClass
import com.example.businesscards.databinding.FragmentCommunicationBinding
import com.example.businesscards.interfaces.APIService
import com.example.businesscards.interfaces.BasicListener
import com.example.businesscards.interfaces.UserListener
import com.example.businesscards.models.*
import com.example.businesscards.startup.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Response

class ChatFragment : Fragment(), BasicListener, UserListener {

    private lateinit var binding: FragmentCommunicationBinding
    private var user: UserInfo? = null
    private var prefs: PreferenceClass? = null
    var firebaseUser: FirebaseUser? = null
    var databaseReference: DatabaseReference? = null
    var messagesAdapter: MessagesAdapter? = null
    var chatList: ArrayList<Chat>? = null
    var userAdapter: UsersAdapter? = null
    var usersList: ArrayList<UserInfo>? = null
    var notify = false

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
        binding.rvChatUsers.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        if (user != null) {
            onStarted()
            // CURRENT_USER and CHAT_USER are correct!
            Log.d("CURRENT_USER", firebaseUser?.uid!!)
            Log.d("CHAT_USER", user?.id!!)

            // CHAT_USER_IMAGE_URL is correct!
            Log.d("CHAT_USER_IMAGE_URL", user?.imageURL.toString())

            binding.communicationFragmentChatLayout.visibility = View.VISIBLE
            binding.communicationFragmentUserLayout.visibility = View.GONE

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
//        else {
//            onStarted()
//            binding.communicationFragmentChatLayout.visibility = View.GONE
//            binding.communicationFragmentUserLayout.visibility = View.VISIBLE
//            var usersIdList: ArrayList<ChatID> = ArrayList()
//            var idDatabaseReference =
//                FirebaseDatabase
//                    .getInstance()
//                    .getReference(HeartSingleton.FireChatIdDB)
//                    .child(firebaseUser?.uid!!)
//            idDatabaseReference.addValueEventListener(object: ValueEventListener{
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    usersIdList.clear()
//
//                    for(data in snapshot.children){
//                        usersIdList.add(data.getValue(ChatID::class.java) as ChatID)
//                    }
//                    showChatUsers(usersIdList)
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//
//                }
//
//            })
//        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun showChatUsers(userIdPassedList: ArrayList<ChatID>){
        usersList = ArrayList()
        userAdapter = UsersAdapter(usersList!!, R.layout.user_row_layout, this)
        databaseReference = FirebaseDatabase.getInstance().getReference(HeartSingleton.FireUsersDB)
        databaseReference?.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                usersList?.clear()
                for(data in snapshot.children){
                    var singleUser: UserInfo = data.getValue(UserInfo::class.java) as UserInfo
                    for(userId in userIdPassedList){
                        if(singleUser.id.equals(userId.id)){
                            usersList?.add(singleUser)
                        }
                    }
                    binding.rvChatUsers.apply {
                        adapter = userAdapter
                        userAdapter?.notifyDataSetChanged()
                    }
                }
                onStopped()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun updateToken(token: String){
        var reference = FirebaseDatabase.getInstance().getReference(HeartSingleton.FireTokenDB)
        var token1 = TokenModel(token)
        reference.child(firebaseUser!!.uid).setValue(token1)
    }

    var apiService: APIService = NotificationClient().getClient("https://fcm.googleapis.com/").create(
        APIService::class.java)

    private fun setupListeners() {
        binding.sendMessage.setOnClickListener {
            binding.rvChat.scrollToPosition(binding.rvChat.adapter!!.itemCount - 1)
        }
        binding.iconSend.setOnClickListener {
            notify = true
            var message = binding.sendMessage.text.toString()
            if (!message.isNullOrEmpty()) {
                sendMessage(firebaseUser?.uid!!, user?.id!!, message)
                binding.sendMessage.text.clear()
                Toast.makeText(requireContext(), "Message was sent", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Message was NOT sent", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendMessage(senderId: String, receiverId: String, message: String) {
        databaseReference = FirebaseDatabase.getInstance().reference

        var hashMap: HashMap<String, Any> = HashMap()
        hashMap[HeartSingleton.FireSenderId] = senderId
        hashMap[HeartSingleton.FireReceiverId] = receiverId
        hashMap[HeartSingleton.FireMessage] = message

        databaseReference?.child(HeartSingleton.FireChatDB)?.push()?.setValue(hashMap)

        var chatReference: DatabaseReference =
            FirebaseDatabase
                .getInstance()
                .getReference(HeartSingleton.FireChatIdDB)
                .child(firebaseUser?.uid!!)
                .child(user?.id!!)

        chatReference.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.exists()){
                    chatReference.child(HeartSingleton.FireId).setValue(user?.id)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        updateToken(FirebaseMessaging.getInstance().token.toString())

        var msg = message
        var reference =
            FirebaseDatabase
                .getInstance()
                .getReference(HeartSingleton.FireUsersDB)
                .child(firebaseUser!!.uid)
        reference.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var user:UserInfo = snapshot.getValue(UserInfo::class.java) as UserInfo
                if(notify)
                    sendNotification(receiverId, user.username!!, msg)
                notify = false
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

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
                messagesAdapter?.notifyItemRangeChanged(0, chatList!!.size-1)
                binding.rvChat.adapter = messagesAdapter
                binding.rvChat.scrollToPosition(binding.rvChat.adapter!!.itemCount - 1)
                onStopped()
            }

            override fun onCancelled(error: DatabaseError) {
                // not needed here
            }

        })
    }



    fun sendNotification(receiverId: String, username: String, message: String){
        var tokens = FirebaseDatabase.getInstance().getReference(HeartSingleton.FireTokenDB)
        var query = tokens.orderByKey().equalTo(receiverId)
        query.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(everyToken in snapshot.children){
                    var token = everyToken.getValue(TokenModel::class.java) as TokenModel
                    var data = NotificationDataModel(firebaseUser!!.uid, R.mipmap.ic_launcher, username + ": ", "New Message",user?.id)

                    var sender = NotificationSender(data, token.token)
                    apiService.sendNotification(sender).enqueue(object: retrofit2.Callback<NotificationMyResponse>{
                        override fun onResponse(
                            call: Call<NotificationMyResponse>,
                            response: Response<NotificationMyResponse>
                        ) {
                            if(response.code() == 200){
                                if(response.body()?.success != 1){
                                    Toast.makeText(requireContext(), "FAILED", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        override fun onFailure(call: Call<NotificationMyResponse>, t: Throwable) {

                        }

                    })
//                        if(apiService.sendNotification(sender).body()?.success != 1){
//                            Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
//                        }
                    }
                }

            override fun onCancelled(error: DatabaseError) {

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