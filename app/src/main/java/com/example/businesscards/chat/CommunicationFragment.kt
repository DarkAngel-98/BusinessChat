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
import com.example.businesscards.interfaces.BasicListener
import com.example.businesscards.interfaces.UserListener
import com.example.businesscards.models.Chat
import com.example.businesscards.models.ChatID
import com.example.businesscards.models.UserInfo
import com.example.businesscards.startup.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class CommunicationFragment : Fragment(), BasicListener, UserListener {

    private lateinit var binding: FragmentCommunicationBinding
    private var user: UserInfo? = null
    private var prefs: PreferenceClass? = null
    var firebaseUser: FirebaseUser? = null
    var databaseReference: DatabaseReference? = null
    var messagesAdapter: MessagesAdapter? = null
    var chatList: ArrayList<Chat>? = null
    var userAdapter: UsersAdapter? = null
    var usersList: ArrayList<UserInfo>? = null

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
        else {
            onStarted()
            binding.communicationFragmentChatLayout.visibility = View.GONE
            binding.communicationFragmentUserLayout.visibility = View.VISIBLE
            var usersIdList: ArrayList<ChatID> = ArrayList()
            var idDatabaseReference =
                FirebaseDatabase
                    .getInstance()
                    .getReference(HeartSingleton.FireChatIdDB)
                    .child(firebaseUser?.uid!!)
            idDatabaseReference.addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    usersIdList.clear()

                    for(data in snapshot.children){
                        usersIdList.add(data.getValue(ChatID::class.java) as ChatID)
                    }
                    showChatUsers(usersIdList)
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
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

    private fun setupListeners() {
        binding.sendMessage.setOnClickListener {
            binding.rvChat.scrollToPosition(binding.rvChat.adapter!!.itemCount - 1)
        }
        binding.iconSend.setOnClickListener {
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

    override fun onStarted() {
        activity?.let { (activity as MainActivity).showProgress() }
    }

    override fun onStopped() {
        activity?.let { (activity as MainActivity).hideProgress() }
    }

    private fun openCommunicationFragment(user: UserInfo?){
        val bundle = bundleOf(HeartSingleton.BundleChatChat to user)
        this.arguments = bundle
        activity?.let { (activity as MainActivity)
            .setNavigationPanelSelectedTab(R.id.navigate_chat)
        }
        activity?.let {

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                (activity as MainActivity).supportFragmentManager
                    .beginTransaction()
                    .detach(this)
                    .commitNow()
                (activity as MainActivity).supportFragmentManager
                    .beginTransaction()
                    .attach(this)
                    .addToBackStack(null)
                    .commitNow()
            }else{
                (activity as MainActivity).supportFragmentManager
                    .beginTransaction()
                    .detach(this)
                    .attach(this)
                    .addToBackStack(null)
                    .commitNow()
            }
        }
    }

    override fun onUserClicked(user: UserInfo) {
        openCommunicationFragment(user)
        Toast.makeText(requireContext(), "Hello", Toast.LENGTH_SHORT).show()
    }

}