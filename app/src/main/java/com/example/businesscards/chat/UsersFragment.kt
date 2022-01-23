package com.example.businesscards.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.businesscards.models.UserInfo
import com.example.businesscards.startup.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UsersFragment : Fragment(), BasicListener, UserListener {
    private lateinit var binding: FragmentUsersBinding
    private var businessCardFragment: MyBusinessCardBottomSheetFragment =
        MyBusinessCardBottomSheetFragment()
    private var usersAdapter: UsersAdapter? = null
    private var prefs: PreferenceClass? = null

    companion object {
        var newMessage = 0
        var senderId = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {

        }
        prefs = PreferenceClass(requireActivity())
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
                for (dataRow in snapshot.children) {
                    var chatInfo: UserInfo = dataRow.getValue(UserInfo::class.java) as UserInfo

                    if (!chatInfo.id.equals(firebaseUser?.uid)) {
                        usersList.add(chatInfo)
                    }
                    else if (chatInfo.id.equals(senderId)) {
                        chatInfo.newMessage = newMessage
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

    private fun openMyCard(user: UserInfo) {

        val bundle = bundleOf(HeartSingleton.BundleBusinessCard to user)
        businessCardFragment.arguments = bundle
        MyBusinessCardBottomSheetFragment.showReportComment(user, requireActivity())

    }

    private fun showAlertDialog(title: String, user: UserInfo) {
        val alertDialog = AlertDialog.Builder(requireContext())
        alertDialog.setTitle(title)
        alertDialog.setPositiveButton("Card") { _, _ ->
            Handler(Looper.getMainLooper()).postDelayed({ openMyCard(user) }, 500)
        }

        alertDialog.setNegativeButton("Chat") { _, _ ->
            Handler(Looper.getMainLooper()).postDelayed({ navigateToChatFragment(user) }, 500)
        }

        alertDialog.create()
        alertDialog.show()
    }

    private fun navigateToChatFragment(user: UserInfo) {
        val bundle = bundleOf(HeartSingleton.BundleChatChat to user)
        findNavController().navigate(R.id.chatFragment, bundle, null)
    }

    override fun onUserClicked(user: UserInfo) {
        showAlertDialog(HeartSingleton.AlertDialogCardOrChat, user)
    }
}