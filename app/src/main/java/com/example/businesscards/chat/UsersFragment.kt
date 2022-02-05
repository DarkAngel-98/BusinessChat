package com.example.businesscards.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.businesscards.R
import com.example.businesscards.adapters.UsersAdapter
import com.example.businesscards.constants.HeartSingleton
import com.example.businesscards.constants.PreferenceClass
import com.example.businesscards.databinding.FragmentUsersBinding
import com.example.businesscards.interfaces.BasicListener
import com.example.businesscards.interfaces.UserListener
import com.example.businesscards.models.BusinessCardModel
import com.example.businesscards.models.UserInfo
import com.example.businesscards.startup.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class UsersFragment : Fragment(), BasicListener, UserListener {
    private lateinit var binding: FragmentUsersBinding
    private var usersAdapter: UsersAdapter? = null
    private var prefs: PreferenceClass? = null
    var firebaseUser: FirebaseUser? = null
    private val TAG = "USERS_FRAGMENT"

    companion object {
        var newMessage = 0
        var senderId = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {

        }
        prefs = PreferenceClass(requireActivity())
        firebaseUser = FirebaseAuth.getInstance().currentUser
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil
            .inflate(inflater, R.layout.fragment_users, container, false)
        binding.lifecycleOwner = this
        onStarted()
        activity?.let { (activity as MainActivity).showNavigationPanel() }
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

    private fun initListeners(){
        usersAdapter?.userLongPressedListener{
            showAlertDialog(it)
        }
    }

    private fun showAlertDialog(user: UserInfo) {
        val alertDialog = AlertDialog.Builder(requireContext())
        if(user.jobPosition.isNullOrEmpty())
            alertDialog.setMessage("Works at " + user.companyName)
        else
            alertDialog.setMessage(user.jobPosition + " at " + user.companyName)

        alertDialog.setNegativeButton(HeartSingleton.AlertDialogOK) { alertDialog, _ ->
            alertDialog.dismiss()
        }

        alertDialog.create()
        alertDialog.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getAllUsers() {
        var usersList: ArrayList<UserInfo> = ArrayList()
        usersAdapter = UsersAdapter(usersList, R.layout.user_row_layout, this)
        var firebaseUser = FirebaseAuth.getInstance().currentUser
        var databaseReference =
            FirebaseDatabase
                .getInstance()
                .getReference(HeartSingleton.FireUsersDB)
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                usersList.clear()
                try {
                    for (dataRow in snapshot.children) {
                        var chatInfo: UserInfo = dataRow.getValue(UserInfo::class.java) as UserInfo

                        if (!chatInfo.id.equals(firebaseUser?.uid)) {
                            usersList.add(chatInfo)
                        }
                    }
                }catch (er: DatabaseException){
                    Log.e(TAG, er.toString())
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
        initListeners()
    }


    override fun onStarted() {
        activity?.let { (activity as MainActivity).showProgress() }
    }

    override fun onStopped() {
        activity?.let { (activity as MainActivity).hideProgress() }
    }

    private fun openChat(user: UserInfo){
        if(user.newMessage == 1)
            Handler(Looper.getMainLooper()).postDelayed({ setNewMessage(0, user) }, 200)
        Handler(Looper.getMainLooper()).postDelayed({ navigateToChatFragment(user) }, 500)
    }

    private fun setNewMessage(newMessage: Int, user: UserInfo){
        FirebaseDatabase.getInstance()
            .getReference(HeartSingleton.FireUsersDB)
            .child(user.id!!).child(HeartSingleton.FireNewMessage).setValue(newMessage)
    }

    private fun navigateToChatFragment(user: UserInfo) {
        val bundle = bundleOf(HeartSingleton.BundleChatChat to user)
        findNavController().navigate(R.id.chatFragment, bundle, null)
    }

    override fun onUserClicked(user: UserInfo) {
        openChat(user)
    }
}