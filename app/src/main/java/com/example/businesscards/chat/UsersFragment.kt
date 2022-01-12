package com.example.businesscards.chat

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.businesscards.R
import com.example.businesscards.adapters.UsersAdapter
import com.example.businesscards.constants.HeartSingleton
import com.example.businesscards.databinding.FragmentUsersBinding
import com.example.businesscards.interfaces.BasicListener
import com.example.businesscards.interfaces.UserListener
import com.example.businesscards.models.UserInfo
import com.example.businesscards.startup.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class UsersFragment : Fragment(), BasicListener, UserListener {
    private lateinit var binding: FragmentUsersBinding
    private var communicationFragment: CommunicationFragment = CommunicationFragment()
    private var usersAdapter: UsersAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_users, container, false)
        binding.lifecycleOwner = this
        onStarted()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvAllUsers.layoutManager =
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )
        getAllUsers()

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getAllUsers(){
        var usersList: ArrayList<UserInfo> = ArrayList()
        usersAdapter = UsersAdapter(usersList, R.layout.user_row_layout, this)
        var firebaseUser = FirebaseAuth.getInstance().currentUser
        var databaseReference =
            FirebaseDatabase
                .getInstance()
                .getReference(HeartSingleton.FireUsersDB)
        databaseReference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                usersList.clear()
                for(dataRow in snapshot.children){
                    var chatInfo: UserInfo = dataRow.getValue(UserInfo::class.java) as UserInfo

                    if(!chatInfo.id.equals(firebaseUser?.uid)){
                        usersList.add(chatInfo)
                    }
                }
                binding.rvAllUsers.apply {

                    adapter = usersAdapter
                    usersAdapter?.notifyDataSetChanged()
                }
                onStopped()
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
    private fun openCommunicationFragment(user: UserInfo?){
        val bundle = bundleOf(HeartSingleton.BundleChatChat to user)
        communicationFragment.arguments = bundle
        activity?.let { (activity as MainActivity)
            .setNavigationPanelSelectedTab(R.id.navigate_chat)
        }
        activity?.let { (activity as MainActivity).supportFragmentManager
            .beginTransaction()
            .replace(R.id.main_frame_layout, communicationFragment)
            .commit()
        }
    }
    override fun onUserClicked(user: UserInfo) {
        openCommunicationFragment(user)
    }
}